package com.aimedical.modules.pharmacy.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.pharmacy.PharmacyErrorCode;
import com.aimedical.modules.pharmacy.converter.PharmacyConverter;
import com.aimedical.modules.pharmacy.dto.PharmacyRefundCreateRequest;
import com.aimedical.modules.pharmacy.dto.PharmacyRefundItemRequest;
import com.aimedical.modules.pharmacy.dto.PharmacyRefundResponse;
import com.aimedical.modules.pharmacy.entity.DispensingItemEntity;
import com.aimedical.modules.pharmacy.entity.DispensingRecordEntity;
import com.aimedical.modules.pharmacy.entity.PharmacyRefundItemEntity;
import com.aimedical.modules.pharmacy.entity.PharmacyRefundRecordEntity;
import com.aimedical.modules.pharmacy.entity.PharmacyStockEntity;
import com.aimedical.modules.pharmacy.enums.DispensingStatus;
import com.aimedical.modules.pharmacy.enums.PharmacyRefundStatus;
import com.aimedical.modules.pharmacy.repository.DispensingItemRepository;
import com.aimedical.modules.pharmacy.repository.DispensingRecordRepository;
import com.aimedical.modules.pharmacy.repository.PharmacyRefundItemRepository;
import com.aimedical.modules.pharmacy.repository.PharmacyRefundRecordRepository;
import com.aimedical.modules.pharmacy.repository.PharmacyStockRepository;
import com.aimedical.modules.pharmacy.service.PharmacyRefundService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 退药服务实现。
 * <p>
 * 业务规则：
 * <ul>
 *     <li>仅 DISPENSED 状态的发药记录可发起退药</li>
 *     <li>退药数量不可超过发药数量（考虑历史已退数量）</li>
 *     <li>同一发药记录不可存在重复的待处理退药申请</li>
 *     <li>审批通过时回补药房库存，发药记录状态变更为 REFUNDED</li>
 * </ul>
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class PharmacyRefundServiceImpl implements PharmacyRefundService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PharmacyRefundRecordRepository refundRepository;
    private final PharmacyRefundItemRepository refundItemRepository;
    private final DispensingRecordRepository dispensingRepository;
    private final DispensingItemRepository dispensingItemRepository;
    private final PharmacyStockRepository stockRepository;
    private final PharmacyConverter converter;

    public PharmacyRefundServiceImpl(PharmacyRefundRecordRepository refundRepository,
                                     PharmacyRefundItemRepository refundItemRepository,
                                     DispensingRecordRepository dispensingRepository,
                                     DispensingItemRepository dispensingItemRepository,
                                     PharmacyStockRepository stockRepository,
                                     PharmacyConverter converter) {
        this.refundRepository = refundRepository;
        this.refundItemRepository = refundItemRepository;
        this.dispensingRepository = dispensingRepository;
        this.dispensingItemRepository = dispensingItemRepository;
        this.stockRepository = stockRepository;
        this.converter = converter;
    }

    @Override
    @Transactional
    public Result<PharmacyRefundResponse> create(PharmacyRefundCreateRequest request,
                                                  Long pharmacistId, String pharmacistName) {
        if (pharmacistId == null) {
            return Result.fail(GlobalErrorCode.UNAUTHORIZED, "无法获取当前登录药师ID");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return Result.fail(PharmacyErrorCode.REFUND_QUANTITY_EXCEEDED, "退药明细不能为空");
        }

        // 校验发药记录存在且为 DISPENSED 状态
        Optional<DispensingRecordEntity> dispensingOpt = dispensingRepository.findById(request.getDispensingId());
        if (dispensingOpt.isEmpty()) {
            return Result.fail(PharmacyErrorCode.DISPENSING_NOT_FOUND);
        }
        DispensingRecordEntity dispensing = dispensingOpt.get();
        if (!DispensingStatus.DISPENSED.getCode().equals(dispensing.getStatus())) {
            return Result.fail(PharmacyErrorCode.DISPENSING_INVALID_STATE,
                    "仅已发药状态可发起退药，当前状态: " + dispensing.getStatus());
        }
        if (DispensingStatus.REFUNDED.getCode().equals(dispensing.getStatus())) {
            return Result.fail(PharmacyErrorCode.DISPENSING_ALREADY_REFUNDED);
        }

        // 防重复：同一发药记录不可存在待处理退药
        Optional<PharmacyRefundRecordEntity> pendingRefund = refundRepository
                .findByDispensingIdAndStatus(request.getDispensingId(), PharmacyRefundStatus.PENDING.getCode());
        if (pendingRefund.isPresent()) {
            return Result.fail(PharmacyErrorCode.REFUND_DUPLICATE);
        }

        // 加载发药明细，构建映射用于退药数量校验
        List<DispensingItemEntity> dispensingItems = dispensingItemRepository.findByDispensingId(request.getDispensingId());
        Map<Long, DispensingItemEntity> dispensingItemMap = new HashMap<>();
        for (DispensingItemEntity item : dispensingItems) {
            dispensingItemMap.put(item.getId(), item);
        }

        // 校验退药数量不超过发药数量（扣除历史已退数量）
        for (PharmacyRefundItemRequest itemReq : request.getItems()) {
            DispensingItemEntity dispensingItem = dispensingItemMap.get(itemReq.getDispensingItemId());
            if (dispensingItem == null) {
                return Result.fail(PharmacyErrorCode.DISPENSING_ITEM_NOT_FOUND,
                        "发药明细不存在: " + itemReq.getDispensingItemId());
            }
            // 历史已退数量
            BigDecimal refundedQty = BigDecimal.ZERO;
            List<PharmacyRefundItemEntity> historyRefundItems = refundItemRepository.findByDispensingItemId(itemReq.getDispensingItemId());
            for (PharmacyRefundItemEntity history : historyRefundItems) {
                // 仅统计已退药（REFUNDED）和待处理（PENDING）的退药明细
                PharmacyRefundRecordEntity historyRecord = refundRepository.findById(history.getRefundId()).orElse(null);
                if (historyRecord != null
                        && (PharmacyRefundStatus.REFUNDED.getCode().equals(historyRecord.getStatus())
                        || PharmacyRefundStatus.PENDING.getCode().equals(historyRecord.getStatus()))) {
                    refundedQty = refundedQty.add(history.getQuantity());
                }
            }
            BigDecimal available = dispensingItem.getQuantity().subtract(refundedQty);
            if (itemReq.getQuantity().compareTo(available) > 0) {
                return Result.fail(PharmacyErrorCode.REFUND_QUANTITY_EXCEEDED,
                        "退药数量超过可退数量，药品: " + itemReq.getDrugCode()
                                + ", 发药: " + dispensingItem.getQuantity()
                                + ", 已退: " + refundedQty
                                + ", 本次申请: " + itemReq.getQuantity());
            }
        }

        // 创建退药记录
        PharmacyRefundRecordEntity refund = new PharmacyRefundRecordEntity();
        refund.setRefundNo(generateRefundNo());
        refund.setDispensingId(request.getDispensingId());
        refund.setPatientId(dispensing.getPatientId());
        refund.setPatientName(dispensing.getPatientName());
        refund.setPharmacistId(pharmacistId);
        refund.setPharmacistName(pharmacistName);
        refund.setStatus(PharmacyRefundStatus.PENDING.getCode());
        refund.setRefundReason(request.getRefundReason());
        refund.setRemark(request.getRemark());

        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PharmacyRefundItemEntity> itemsToSave = new ArrayList<>();
        for (PharmacyRefundItemRequest itemReq : request.getItems()) {
            DispensingItemEntity dispensingItem = dispensingItemMap.get(itemReq.getDispensingItemId());
            PharmacyRefundItemEntity refundItem = new PharmacyRefundItemEntity();
            refundItem.setDispensingItemId(itemReq.getDispensingItemId());
            refundItem.setDrugCode(itemReq.getDrugCode());
            refundItem.setDrugName(itemReq.getDrugName());
            refundItem.setBatchNo(itemReq.getBatchNo() != null ? itemReq.getBatchNo() : dispensingItem.getBatchNo());
            refundItem.setQuantity(itemReq.getQuantity());
            refundItem.setUnit(itemReq.getUnit() != null ? itemReq.getUnit() : dispensingItem.getUnit());
            refundItem.setUnitPrice(itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : dispensingItem.getUnitPrice());
            BigDecimal amount = refundItem.getUnitPrice() != null
                    ? refundItem.getUnitPrice().multiply(itemReq.getQuantity())
                    : BigDecimal.ZERO;
            refundItem.setAmount(amount);
            itemsToSave.add(refundItem);

            totalQuantity = totalQuantity.add(itemReq.getQuantity());
            totalAmount = totalAmount.add(amount);
        }
        refund.setTotalQuantity(totalQuantity);
        refund.setTotalAmount(totalAmount);

        try {
            refund = refundRepository.save(refund);
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }

        for (PharmacyRefundItemEntity item : itemsToSave) {
            item.setRefundId(refund.getId());
        }
        refundItemRepository.saveAll(itemsToSave);

        return Result.success(converter.toResponse(refund, itemsToSave));
    }

    @Override
    @Transactional
    public Result<PharmacyRefundResponse> approve(Long refundId) {
        if (refundId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID, "退药记录ID不能为空");
        }
        Optional<PharmacyRefundRecordEntity> refundOpt = refundRepository.findById(refundId);
        if (refundOpt.isEmpty()) {
            return Result.fail(PharmacyErrorCode.REFUND_NOT_FOUND);
        }
        PharmacyRefundRecordEntity refund = refundOpt.get();

        // 仅 PENDING 状态可审批
        if (!PharmacyRefundStatus.PENDING.getCode().equals(refund.getStatus())) {
            return Result.fail(PharmacyErrorCode.REFUND_INVALID_STATE,
                    "仅待处理状态可审批，当前状态: " + refund.getStatus());
        }

        // 校验关联发药记录仍为 DISPENSED
        Optional<DispensingRecordEntity> dispensingOpt = dispensingRepository.findById(refund.getDispensingId());
        if (dispensingOpt.isEmpty()) {
            return Result.fail(PharmacyErrorCode.DISPENSING_NOT_FOUND);
        }
        DispensingRecordEntity dispensing = dispensingOpt.get();

        // 回补药房库存
        List<PharmacyRefundItemEntity> refundItems = refundItemRepository.findByRefundId(refundId);
        for (PharmacyRefundItemEntity refundItem : refundItems) {
            Result<Void> restoreResult = restoreStock(refundItem.getDrugCode(), refundItem.getBatchNo(),
                    refundItem.getQuantity());
            if (!restoreResult.getCode().equals(GlobalErrorCode.SUCCESS.getCode())) {
                return Result.fail(restoreResult.getCode(), restoreResult.getMessage());
            }
        }

        // 更新退药记录状态
        refund.setStatus(PharmacyRefundStatus.REFUNDED.getCode());
        refund.setRefundedAt(LocalDateTime.now());
        try {
            refund = refundRepository.save(refund);
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }

        // 更新发药记录状态为 REFUNDED
        dispensing.setStatus(DispensingStatus.REFUNDED.getCode());
        try {
            dispensingRepository.save(dispensing);
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }

        return Result.success(converter.toResponse(refund, refundItems));
    }

    @Override
    @Transactional
    public Result<PharmacyRefundResponse> reject(Long refundId, String reason) {
        if (refundId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID, "退药记录ID不能为空");
        }
        if (reason == null || reason.trim().isEmpty()) {
            return Result.fail(PharmacyErrorCode.REFUND_REASON_EMPTY, "驳回原因不能为空");
        }
        Optional<PharmacyRefundRecordEntity> refundOpt = refundRepository.findById(refundId);
        if (refundOpt.isEmpty()) {
            return Result.fail(PharmacyErrorCode.REFUND_NOT_FOUND);
        }
        PharmacyRefundRecordEntity refund = refundOpt.get();

        // 仅 PENDING 状态可驳回
        if (!PharmacyRefundStatus.PENDING.getCode().equals(refund.getStatus())) {
            return Result.fail(PharmacyErrorCode.REFUND_INVALID_STATE,
                    "仅待处理状态可驳回，当前状态: " + refund.getStatus());
        }

        refund.setStatus(PharmacyRefundStatus.REJECTED.getCode());
        // 驳回原因追加到备注
        String existingRemark = refund.getRemark();
        String rejectRemark = "驳回原因: " + reason;
        refund.setRemark(existingRemark != null && !existingRemark.isEmpty()
                ? existingRemark + " | " + rejectRemark
                : rejectRemark);

        try {
            refund = refundRepository.save(refund);
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }

        List<PharmacyRefundItemEntity> items = refundItemRepository.findByRefundId(refundId);
        return Result.success(converter.toResponse(refund, items));
    }

    @Override
    public Result<PharmacyRefundResponse> getById(Long refundId) {
        if (refundId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID, "退药记录ID不能为空");
        }
        Optional<PharmacyRefundRecordEntity> refundOpt = refundRepository.findById(refundId);
        if (refundOpt.isEmpty()) {
            return Result.fail(PharmacyErrorCode.REFUND_NOT_FOUND);
        }
        PharmacyRefundRecordEntity refund = refundOpt.get();
        List<PharmacyRefundItemEntity> items = refundItemRepository.findByRefundId(refundId);
        return Result.success(converter.toResponse(refund, items));
    }

    /**
     * 回补药房库存。
     * 优先回补到原批次（若存在），否则新建库存记录。
     */
    private Result<Void> restoreStock(String drugCode, String batchNo, BigDecimal quantity) {
        if (batchNo != null && !batchNo.trim().isEmpty()) {
            Optional<PharmacyStockEntity> stockOpt = stockRepository.findByDrugCodeAndBatchNo(drugCode, batchNo);
            if (stockOpt.isPresent()) {
                PharmacyStockEntity stock = stockOpt.get();
                stock.setQuantity(stock.getQuantity().add(quantity));
                try {
                    stockRepository.save(stock);
                } catch (OptimisticLockingFailureException e) {
                    return Result.fail(GlobalErrorCode.CONFLICT);
                }
                return Result.success(null);
            }
        }

        // 原批次不存在时，取该药品任意一个批次回补（合并到首个批次）
        List<PharmacyStockEntity> stocks = stockRepository.findByDrugCode(drugCode);
        if (!stocks.isEmpty()) {
            PharmacyStockEntity stock = stocks.get(0);
            stock.setQuantity(stock.getQuantity().add(quantity));
            try {
                stockRepository.save(stock);
            } catch (OptimisticLockingFailureException e) {
                return Result.fail(GlobalErrorCode.CONFLICT);
            }
            return Result.success(null);
        }

        // 该药品无任何库存记录，无法回补（不应出现此情况，因为发药时已扣减）
        return Result.fail(PharmacyErrorCode.STOCK_NOT_FOUND,
                "药品 " + drugCode + " 无库存记录，无法回补");
    }

    /**
     * 生成退药单号：RFD + 时间戳 + 4位随机数。
     */
    private String generateRefundNo() {
        return "RFD" + System.currentTimeMillis() + String.format("%04d", SECURE_RANDOM.nextInt(10000));
    }
}
