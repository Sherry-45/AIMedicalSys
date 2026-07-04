package com.aimedical.modules.pharmacy.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.PageResponse;
import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.event.HealthRecordArchiveEvent;
import com.aimedical.modules.pharmacy.PharmacyErrorCode;
import com.aimedical.modules.pharmacy.converter.PharmacyConverter;
import com.aimedical.modules.pharmacy.dto.DispensingCreateRequest;
import com.aimedical.modules.pharmacy.dto.DispensingItemRequest;
import com.aimedical.modules.pharmacy.dto.DispensingQueryRequest;
import com.aimedical.modules.pharmacy.dto.DispensingResponse;
import com.aimedical.modules.pharmacy.entity.DispensingItemEntity;
import com.aimedical.modules.pharmacy.entity.DispensingRecordEntity;
import com.aimedical.modules.pharmacy.entity.PharmacyStockEntity;
import com.aimedical.modules.pharmacy.enums.DispensingStatus;
import com.aimedical.modules.pharmacy.repository.DispensingItemRepository;
import com.aimedical.modules.pharmacy.repository.DispensingRecordRepository;
import com.aimedical.modules.pharmacy.repository.PharmacyStockRepository;
import com.aimedical.modules.pharmacy.service.DispensingService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 发药服务实现。
 * <p>
 * 业务规则：
 * <ul>
 *     <li>创建发药记录时校验库存是否充足（按药品编码 + 批次号匹配）</li>
 *     <li>执行发药时扣减药房库存，状态从 PENDING 变更为 DISPENSED</li>
 *     <li>取消发药仅允许 PENDING 状态，无库存变动</li>
 * </ul>
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class DispensingServiceImpl implements DispensingService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final DispensingRecordRepository dispensingRepository;
    private final DispensingItemRepository dispensingItemRepository;
    private final PharmacyStockRepository stockRepository;
    private final PharmacyConverter converter;
    private final ApplicationEventPublisher eventPublisher;

    public DispensingServiceImpl(DispensingRecordRepository dispensingRepository,
                                 DispensingItemRepository dispensingItemRepository,
                                 PharmacyStockRepository stockRepository,
                                 PharmacyConverter converter,
                                 ApplicationEventPublisher eventPublisher) {
        this.dispensingRepository = dispensingRepository;
        this.dispensingItemRepository = dispensingItemRepository;
        this.stockRepository = stockRepository;
        this.converter = converter;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Result<DispensingResponse> create(DispensingCreateRequest request, Long pharmacistId, String pharmacistName) {
        if (pharmacistId == null) {
            return Result.fail(GlobalErrorCode.UNAUTHORIZED, "无法获取当前登录药师ID");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return Result.fail(PharmacyErrorCode.DISPENSING_ITEM_EMPTY);
        }

        // 防重复：同一处方不可重复创建发药记录
        if (request.getPrescriptionId() != null) {
            Optional<DispensingRecordEntity> existing = dispensingRepository.findByPrescriptionId(request.getPrescriptionId());
            if (existing.isPresent()) {
                return Result.fail(PharmacyErrorCode.DISPENSING_DUPLICATE);
            }
        }

        // 校验库存充足性（按药品编码 + 批次号匹配）
        for (DispensingItemRequest itemReq : request.getItems()) {
            if (itemReq.getBatchNo() != null && !itemReq.getBatchNo().trim().isEmpty()) {
                Optional<PharmacyStockEntity> stockOpt = stockRepository.findByDrugCodeAndBatchNo(
                        itemReq.getDrugCode(), itemReq.getBatchNo());
                if (stockOpt.isEmpty()) {
                    return Result.fail(PharmacyErrorCode.STOCK_BATCH_NOT_FOUND,
                            "药品 " + itemReq.getDrugCode() + " 批次 " + itemReq.getBatchNo() + " 库存不存在");
                }
                if (stockOpt.get().getQuantity().compareTo(itemReq.getQuantity()) < 0) {
                    return Result.fail(PharmacyErrorCode.STOCK_INSUFFICIENT,
                            "药品 " + itemReq.getDrugCode() + " 库存不足，当前: " + stockOpt.get().getQuantity()
                                    + ", 需要: " + itemReq.getQuantity());
                }
            } else {
                // 未指定批次时按药品编码汇总校验
                BigDecimal total = stockRepository.sumQuantityByDrugCode(itemReq.getDrugCode());
                if (total.compareTo(itemReq.getQuantity()) < 0) {
                    return Result.fail(PharmacyErrorCode.STOCK_INSUFFICIENT,
                            "药品 " + itemReq.getDrugCode() + " 总库存不足，当前: " + total
                                    + ", 需要: " + itemReq.getQuantity());
                }
            }
        }

        // 创建发药记录（PENDING 状态）
        DispensingRecordEntity record = new DispensingRecordEntity();
        record.setDispensingNo(generateDispensingNo());
        record.setPrescriptionId(request.getPrescriptionId());
        record.setMedicalOrderId(request.getMedicalOrderId());
        record.setPatientId(request.getPatientId());
        record.setPatientName(request.getPatientName());
        record.setPharmacistId(pharmacistId);
        record.setPharmacistName(pharmacistName);
        record.setStatus(DispensingStatus.PENDING.getCode());
        record.setRemark(request.getRemark());

        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<DispensingItemEntity> itemsToSave = new ArrayList<>();
        for (DispensingItemRequest itemReq : request.getItems()) {
            DispensingItemEntity item = new DispensingItemEntity();
            item.setDrugCode(itemReq.getDrugCode());
            item.setDrugName(itemReq.getDrugName());
            item.setSpecification(itemReq.getSpecification());
            item.setBatchNo(itemReq.getBatchNo());
            item.setQuantity(itemReq.getQuantity());
            item.setUnit(itemReq.getUnit());
            item.setUnitPrice(itemReq.getUnitPrice());
            BigDecimal amount = itemReq.getUnitPrice() != null && itemReq.getQuantity() != null
                    ? itemReq.getUnitPrice().multiply(itemReq.getQuantity())
                    : BigDecimal.ZERO;
            item.setAmount(amount);
            item.setDosage(itemReq.getDosage());
            item.setUsageMethod(itemReq.getUsageMethod());
            item.setFrequency(itemReq.getFrequency());
            item.setDays(itemReq.getDays());
            itemsToSave.add(item);

            totalQuantity = totalQuantity.add(itemReq.getQuantity());
            totalAmount = totalAmount.add(amount);
        }
        record.setTotalQuantity(totalQuantity);
        record.setTotalAmount(totalAmount);

        try {
            record = dispensingRepository.save(record);
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }

        for (DispensingItemEntity item : itemsToSave) {
            item.setDispensingId(record.getId());
        }
        dispensingItemRepository.saveAll(itemsToSave);

        return Result.success(converter.toResponse(record, itemsToSave));
    }

    @Override
    @Transactional
    public Result<DispensingResponse> dispense(Long dispensingId, Long pharmacistId, String pharmacistName) {
        if (dispensingId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID, "发药记录ID不能为空");
        }
        Optional<DispensingRecordEntity> recordOpt = dispensingRepository.findById(dispensingId);
        if (recordOpt.isEmpty()) {
            return Result.fail(PharmacyErrorCode.DISPENSING_NOT_FOUND);
        }
        DispensingRecordEntity record = recordOpt.get();

        // 状态校验：仅 PENDING 可执行发药
        if (!DispensingStatus.PENDING.getCode().equals(record.getStatus())) {
            return Result.fail(PharmacyErrorCode.DISPENSING_INVALID_STATE,
                    "仅待发药状态可执行发药，当前状态: " + record.getStatus());
        }

        List<DispensingItemEntity> items = dispensingItemRepository.findByDispensingId(dispensingId);
        if (items.isEmpty()) {
            return Result.fail(PharmacyErrorCode.DISPENSING_ITEM_EMPTY);
        }

        // 扣减库存（按药品编码 + 批次号匹配，未指定批次时按 FIFO 取最早批次）
        for (DispensingItemEntity item : items) {
            Result<Void> reduceResult = reduceStock(item.getDrugCode(), item.getBatchNo(), item.getQuantity());
            if (!reduceResult.getCode().equals(GlobalErrorCode.SUCCESS.getCode())) {
                // 库存扣减失败，事务回滚
                return Result.fail(reduceResult.getCode(), reduceResult.getMessage());
            }
        }

        // 更新发药记录状态
        record.setStatus(DispensingStatus.DISPENSED.getCode());
        record.setDispensedAt(LocalDateTime.now());
        if (pharmacistId != null) {
            record.setPharmacistId(pharmacistId);
        }
        if (pharmacistName != null) {
            record.setPharmacistName(pharmacistName);
        }

        try {
            record = dispensingRepository.save(record);
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }

        // 发药成功后发布健康档案归档事件（事务提交前）
        publishDispensedEvent(record, items);

        return Result.success(converter.toResponse(record, items));
    }

    @Override
    @Transactional
    public Result<DispensingResponse> cancel(Long dispensingId) {
        if (dispensingId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID, "发药记录ID不能为空");
        }
        Optional<DispensingRecordEntity> recordOpt = dispensingRepository.findById(dispensingId);
        if (recordOpt.isEmpty()) {
            return Result.fail(PharmacyErrorCode.DISPENSING_NOT_FOUND);
        }
        DispensingRecordEntity record = recordOpt.get();

        // 仅 PENDING 状态可取消
        if (!DispensingStatus.PENDING.getCode().equals(record.getStatus())) {
            return Result.fail(PharmacyErrorCode.DISPENSING_INVALID_STATE,
                    "仅待发药状态可取消，当前状态: " + record.getStatus());
        }

        record.setStatus(DispensingStatus.CANCELLED.getCode());

        try {
            record = dispensingRepository.save(record);
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }

        List<DispensingItemEntity> items = dispensingItemRepository.findByDispensingId(dispensingId);
        return Result.success(converter.toResponse(record, items));
    }

    @Override
    public Result<DispensingResponse> getById(Long dispensingId) {
        if (dispensingId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID, "发药记录ID不能为空");
        }
        Optional<DispensingRecordEntity> recordOpt = dispensingRepository.findById(dispensingId);
        if (recordOpt.isEmpty()) {
            return Result.fail(PharmacyErrorCode.DISPENSING_NOT_FOUND);
        }
        DispensingRecordEntity record = recordOpt.get();
        List<DispensingItemEntity> items = dispensingItemRepository.findByDispensingId(dispensingId);
        return Result.success(converter.toResponse(record, items));
    }

    @Override
    public Result<PageResponse<DispensingResponse>> query(DispensingQueryRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Long patientId = request.getPatientId();
        String status = request.getStatus();

        Page<DispensingRecordEntity> page;
        boolean hasPatient = patientId != null;
        boolean hasStatus = status != null && !status.trim().isEmpty();
        if (hasPatient && hasStatus) {
            page = dispensingRepository.findByPatientIdAndStatusOrderByCreatedAtDesc(patientId, status.trim(), pageable);
        } else if (hasPatient) {
            page = dispensingRepository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable);
        } else if (hasStatus) {
            page = dispensingRepository.findByStatusOrderByCreatedAtDesc(status.trim(), pageable);
        } else {
            page = dispensingRepository.findAll(pageable);
        }

        // 批量加载明细避免 N+1
        List<DispensingResponse> content = new ArrayList<>();
        if (!page.isEmpty()) {
            List<Long> dispensingIds = page.getContent().stream()
                    .map(DispensingRecordEntity::getId)
                    .toList();
            List<DispensingItemEntity> allItems = dispensingItemRepository.findByDispensingIdIn(dispensingIds);
            for (DispensingRecordEntity record : page.getContent()) {
                List<DispensingItemEntity> recordItems = allItems.stream()
                        .filter(item -> item.getDispensingId().equals(record.getId()))
                        .toList();
                content.add(converter.toResponse(record, recordItems));
            }
        }

        PageResponse<DispensingResponse> pageResponse = PageResponse.of(content, page.getTotalElements(),
                request.getPage(), request.getSize());
        return Result.success(pageResponse);
    }

    /**
     * 扣减药房库存。
     * 指定批次号时精确扣减；未指定时按 FIFO（创建时间最早）扣减。
     */
    private Result<Void> reduceStock(String drugCode, String batchNo, BigDecimal quantity) {
        if (batchNo != null && !batchNo.trim().isEmpty()) {
            Optional<PharmacyStockEntity> stockOpt = stockRepository.findByDrugCodeAndBatchNo(drugCode, batchNo);
            if (stockOpt.isEmpty()) {
                return Result.fail(PharmacyErrorCode.STOCK_BATCH_NOT_FOUND,
                        "药品 " + drugCode + " 批次 " + batchNo + " 库存不存在");
            }
            PharmacyStockEntity stock = stockOpt.get();
            if (stock.getQuantity().compareTo(quantity) < 0) {
                return Result.fail(PharmacyErrorCode.STOCK_INSUFFICIENT,
                        "药品 " + drugCode + " 库存不足，当前: " + stock.getQuantity() + ", 需要: " + quantity);
            }
            stock.setQuantity(stock.getQuantity().subtract(quantity));
            try {
                stockRepository.save(stock);
            } catch (OptimisticLockingFailureException e) {
                return Result.fail(GlobalErrorCode.CONFLICT);
            }
            return Result.success(null);
        }

        // FIFO：按创建时间升序扣减
        List<PharmacyStockEntity> stocks = stockRepository.findByDrugCode(drugCode);
        if (stocks.isEmpty()) {
            return Result.fail(PharmacyErrorCode.STOCK_NOT_FOUND, "药品 " + drugCode + " 库存不存在");
        }
        // 按 createdAt 升序排序（最早的批次优先扣减）
        stocks.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) {
                return 0;
            }
            if (a.getCreatedAt() == null) {
                return 1;
            }
            if (b.getCreatedAt() == null) {
                return -1;
            }
            return a.getCreatedAt().compareTo(b.getCreatedAt());
        });

        BigDecimal remaining = quantity;
        for (PharmacyStockEntity stock : stocks) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            if (stock.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal deduct = stock.getQuantity().min(remaining);
            stock.setQuantity(stock.getQuantity().subtract(deduct));
            remaining = remaining.subtract(deduct);
            try {
                stockRepository.save(stock);
            } catch (OptimisticLockingFailureException e) {
                return Result.fail(GlobalErrorCode.CONFLICT);
            }
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            return Result.fail(PharmacyErrorCode.STOCK_INSUFFICIENT,
                    "药品 " + drugCode + " 库存不足，剩余未扣减: " + remaining);
        }
        return Result.success(null);
    }

    /**
     * 生成发药单号：DISP + 时间戳 + 4位随机数。
     */
    private String generateDispensingNo() {
        return "DISP" + System.currentTimeMillis() + String.format("%04d", SECURE_RANDOM.nextInt(10000));
    }

    /**
     * 发布发药归档事件，通知 patient 模块归档到患者健康档案。
     */
    private void publishDispensedEvent(DispensingRecordEntity record, List<DispensingItemEntity> items) {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent();
        event.setType(HealthRecordArchiveEvent.Type.DISPENSED);
        event.setPatientId(record.getPatientId());
        event.setPatientName(record.getPatientName());
        event.setRecordId(record.getId());
        event.setRecordNo(record.getDispensingNo());
        event.setOrganizationName("药房");
        int itemCount = items != null ? items.size() : 0;
        event.setSummary("发药成功，发药单号：" + record.getDispensingNo()
                + "，共" + itemCount + "项，总数量：" + record.getTotalQuantity());
        event.setOccurredAt(record.getDispensedAt() != null ? record.getDispensedAt() : LocalDateTime.now());
        eventPublisher.publishEvent(event);
    }
}
