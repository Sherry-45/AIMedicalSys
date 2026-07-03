package com.aimedical.modules.inventory.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.InventoryErrorCode;
import com.aimedical.modules.inventory.converter.InventoryConverter;
import com.aimedical.modules.inventory.dto.request.TransferApproveRequest;
import com.aimedical.modules.inventory.dto.request.TransferCreateRequest;
import com.aimedical.modules.inventory.dto.request.TransferItemRequest;
import com.aimedical.modules.inventory.dto.request.TransferQueryRequest;
import com.aimedical.modules.inventory.dto.response.TransferOrderResponse;
import com.aimedical.modules.inventory.entity.InventoryStockEntity;
import com.aimedical.modules.inventory.entity.TransferItemEntity;
import com.aimedical.modules.inventory.entity.TransferOrderEntity;
import com.aimedical.modules.inventory.entity.TransferStatus;
import com.aimedical.modules.inventory.repository.InventoryStockRepository;
import com.aimedical.modules.inventory.repository.TransferItemRepository;
import com.aimedical.modules.inventory.repository.TransferOrderRepository;
import com.aimedical.modules.inventory.service.TransferService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 调拨服务实现。
 *
 * <p>状态机：DRAFT -> PENDING_APPROVAL -> APPROVED -> IN_TRANSIT -> RECEIVED。
 * <ul>
 *   <li>create：创建调拨单与明细，计算总项数与总金额</li>
 *   <li>submit：DRAFT -> PENDING_APPROVAL</li>
 *   <li>approve：PENDING_APPROVAL -> APPROVED 或 REJECTED（驳回需填原因）</li>
 *   <li>ship：APPROVED -> IN_TRANSIT，扣减调出方库存</li>
 *   <li>receive：IN_TRANSIT -> RECEIVED，增加调入方库存</li>
 *   <li>cancel：DRAFT/PENDING_APPROVAL -> CANCELLED</li>
 * </ul>
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class TransferServiceImpl implements TransferService {

    private final TransferOrderRepository transferOrderRepository;
    private final TransferItemRepository transferItemRepository;
    private final InventoryStockRepository stockRepository;
    private final InventoryConverter converter;

    public TransferServiceImpl(TransferOrderRepository transferOrderRepository,
                               TransferItemRepository transferItemRepository,
                               InventoryStockRepository stockRepository,
                               InventoryConverter converter) {
        this.transferOrderRepository = transferOrderRepository;
        this.transferItemRepository = transferItemRepository;
        this.stockRepository = stockRepository;
        this.converter = converter;
    }

    @Override
    @Transactional
    public Result<TransferOrderResponse> create(TransferCreateRequest request, Long applicantId, String applicantName) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return Result.fail(InventoryErrorCode.TRANSFER_ITEM_EMPTY);
        }
        // 调出与调入部门不能相同
        if (request.getSourceDept() != null && request.getSourceDept().equals(request.getTargetDept())) {
            return Result.fail(InventoryErrorCode.TRANSFER_DEPT_SAME);
        }

        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setTransferNo(generateTransferNo());
        entity.setTransferType(request.getTransferType());
        entity.setStatus(TransferStatus.DRAFT.getCode());
        entity.setSourceDept(request.getSourceDept());
        entity.setTargetDept(request.getTargetDept());
        entity.setApplicantId(applicantId);
        entity.setApplicantName(applicantName);
        entity.setRemark(request.getRemark());

        // 计算总项数与总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<TransferItemEntity> items = new ArrayList<>();
        for (TransferItemRequest itemReq : request.getItems()) {
            TransferItemEntity item = new TransferItemEntity();
            BigDecimal unitPrice = itemReq.getUnitPrice() == null ? BigDecimal.ZERO : itemReq.getUnitPrice();
            BigDecimal quantity = itemReq.getQuantity() == null ? BigDecimal.ZERO : itemReq.getQuantity();
            BigDecimal amount = unitPrice.multiply(quantity);

            item.setDrugCode(itemReq.getDrugCode());
            item.setDrugName(itemReq.getDrugName());
            item.setSpecification(itemReq.getSpecification());
            item.setBatchNo(itemReq.getBatchNo());
            item.setQuantity(quantity);
            item.setUnit(itemReq.getUnit());
            item.setUnitPrice(unitPrice);
            item.setAmount(amount);
            items.add(item);
            totalAmount = totalAmount.add(amount);
        }

        entity.setTotalItems(items.size());
        entity.setTotalAmount(totalAmount);

        TransferOrderEntity saved = transferOrderRepository.save(entity);

        // 设置明细外键
        for (TransferItemEntity item : items) {
            item.setTransferId(saved.getId());
        }
        transferItemRepository.saveAll(items);

        return Result.success(converter.toTransferResponse(saved, items));
    }

    @Override
    @Transactional
    public Result<TransferOrderResponse> submit(Long id) {
        Optional<TransferOrderEntity> opt = transferOrderRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.TRANSFER_NOT_FOUND);
        }
        TransferOrderEntity entity = opt.get();
        if (!TransferStatus.DRAFT.getCode().equals(entity.getStatus())) {
            return Result.fail(InventoryErrorCode.TRANSFER_INVALID_STATE);
        }
        // 校验明细非空
        List<TransferItemEntity> items = transferItemRepository.findByTransferId(id);
        if (items.isEmpty()) {
            return Result.fail(InventoryErrorCode.TRANSFER_ITEM_EMPTY);
        }
        entity.setStatus(TransferStatus.PENDING_APPROVAL.getCode());
        try {
            TransferOrderEntity saved = transferOrderRepository.save(entity);
            return Result.success(converter.toTransferResponse(saved, items));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<TransferOrderResponse> approve(Long id, TransferApproveRequest request, Long approverId, String approverName) {
        Optional<TransferOrderEntity> opt = transferOrderRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.TRANSFER_NOT_FOUND);
        }
        TransferOrderEntity entity = opt.get();
        if (!TransferStatus.PENDING_APPROVAL.getCode().equals(entity.getStatus())) {
            return Result.fail(InventoryErrorCode.TRANSFER_INVALID_STATE);
        }
        boolean approved = Boolean.TRUE.equals(request.getApproved());
        if (!approved && (request.getRejectReason() == null || request.getRejectReason().isBlank())) {
            return Result.fail(InventoryErrorCode.TRANSFER_APPROVE_REJECT_REASON_REQUIRED);
        }

        entity.setStatus(approved
                ? TransferStatus.APPROVED.getCode()
                : TransferStatus.REJECTED.getCode());
        entity.setApproverId(approverId);
        entity.setApproverName(approverName);
        entity.setApprovedAt(LocalDateTime.now());
        if (!approved) {
            entity.setRejectReason(request.getRejectReason());
        }

        try {
            TransferOrderEntity saved = transferOrderRepository.save(entity);
            List<TransferItemEntity> items = transferItemRepository.findByTransferId(id);
            return Result.success(converter.toTransferResponse(saved, items));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<TransferOrderResponse> ship(Long id) {
        Optional<TransferOrderEntity> opt = transferOrderRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.TRANSFER_NOT_FOUND);
        }
        TransferOrderEntity entity = opt.get();
        if (!TransferStatus.APPROVED.getCode().equals(entity.getStatus())) {
            return Result.fail(InventoryErrorCode.TRANSFER_INVALID_STATE);
        }

        List<TransferItemEntity> items = transferItemRepository.findByTransferId(id);
        // 扣减调出方库存
        for (TransferItemEntity item : items) {
            Result<Void> reduceResult = reduceStock(item);
            if (!reduceResult.getCode().equals(GlobalErrorCode.SUCCESS.getCode())) {
                // 库存不足，回滚由 @Transactional 保证
                return Result.fail(reduceResult.getCode(), reduceResult.getMessage());
            }
        }

        entity.setStatus(TransferStatus.IN_TRANSIT.getCode());
        entity.setShippedAt(LocalDateTime.now());
        try {
            TransferOrderEntity saved = transferOrderRepository.save(entity);
            return Result.success(converter.toTransferResponse(saved, items));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<TransferOrderResponse> receive(Long id) {
        Optional<TransferOrderEntity> opt = transferOrderRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.TRANSFER_NOT_FOUND);
        }
        TransferOrderEntity entity = opt.get();
        if (!TransferStatus.IN_TRANSIT.getCode().equals(entity.getStatus())) {
            return Result.fail(InventoryErrorCode.TRANSFER_INVALID_STATE);
        }

        List<TransferItemEntity> items = transferItemRepository.findByTransferId(id);
        // 增加调入方库存
        for (TransferItemEntity item : items) {
            addStock(item, entity.getTargetDept());
        }

        entity.setStatus(TransferStatus.RECEIVED.getCode());
        entity.setReceivedAt(LocalDateTime.now());
        try {
            TransferOrderEntity saved = transferOrderRepository.save(entity);
            return Result.success(converter.toTransferResponse(saved, items));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<TransferOrderResponse> cancel(Long id) {
        Optional<TransferOrderEntity> opt = transferOrderRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.TRANSFER_NOT_FOUND);
        }
        TransferOrderEntity entity = opt.get();
        String status = entity.getStatus();
        // 仅 DRAFT / PENDING_APPROVAL 可取消
        if (!TransferStatus.DRAFT.getCode().equals(status)
                && !TransferStatus.PENDING_APPROVAL.getCode().equals(status)) {
            return Result.fail(InventoryErrorCode.TRANSFER_INVALID_STATE);
        }
        entity.setStatus(TransferStatus.CANCELLED.getCode());
        try {
            TransferOrderEntity saved = transferOrderRepository.save(entity);
            List<TransferItemEntity> items = transferItemRepository.findByTransferId(id);
            return Result.success(converter.toTransferResponse(saved, items));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Result<TransferOrderResponse> getById(Long id) {
        return transferOrderRepository.findById(id)
                .map(entity -> {
                    List<TransferItemEntity> items = transferItemRepository.findByTransferId(id);
                    return Result.success(converter.toTransferResponse(entity, items));
                })
                .orElseGet(() -> Result.fail(InventoryErrorCode.TRANSFER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Page<TransferOrderResponse>> query(TransferQueryRequest request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 20 : request.getSize();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<TransferOrderEntity> entityPage;
        boolean hasStatus = request.getStatus() != null && !request.getStatus().isBlank();
        boolean hasType = request.getTransferType() != null && !request.getTransferType().isBlank();

        if (hasStatus && hasType) {
            entityPage = transferOrderRepository.findByStatusAndTransferType(
                    request.getStatus(), request.getTransferType(), pageable);
        } else if (hasStatus) {
            entityPage = transferOrderRepository.findByStatus(request.getStatus(), pageable);
        } else if (hasType) {
            entityPage = transferOrderRepository.findByTransferType(request.getTransferType(), pageable);
        } else {
            entityPage = transferOrderRepository.findAll(pageable);
        }

        Page<TransferOrderResponse> responsePage = entityPage.map(entity -> {
            List<TransferItemEntity> items = transferItemRepository.findByTransferId(entity.getId());
            return converter.toTransferResponse(entity, items);
        });
        return Result.success(responsePage);
    }

    /**
     * 生成调拨单号：TRF + 时间戳。
     */
    private String generateTransferNo() {
        return "TRF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    /**
     * 扣减调出方库存。按药品编码+批次号定位批次，扣减对应数量。
     */
    private Result<Void> reduceStock(TransferItemEntity item) {
        String batchNo = item.getBatchNo();
        if (batchNo == null || batchNo.isBlank()) {
            // 无批次号时无法扣减，跳过（简化处理）
            return Result.success(null);
        }
        Optional<InventoryStockEntity> opt = stockRepository.findByDrugCodeAndBatchNo(item.getDrugCode(), batchNo);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.STOCK_BATCH_NOT_FOUND);
        }
        InventoryStockEntity stock = opt.get();
        BigDecimal newQuantity = stock.getQuantity().subtract(item.getQuantity());
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            return Result.fail(InventoryErrorCode.STOCK_INSUFFICIENT);
        }
        stock.setQuantity(newQuantity);
        stockRepository.save(stock);
        return Result.success(null);
    }

    /**
     * 增加调入方库存。若批次已存在则累加，否则新建库存批次。
     */
    private void addStock(TransferItemEntity item, String targetDept) {
        String batchNo = item.getBatchNo();
        if (batchNo == null || batchNo.isBlank()) {
            batchNo = "DEFAULT";
        }
        Optional<InventoryStockEntity> opt = stockRepository.findByDrugCodeAndBatchNo(item.getDrugCode(), batchNo);
        if (opt.isPresent()) {
            InventoryStockEntity stock = opt.get();
            stock.setQuantity(stock.getQuantity().add(item.getQuantity()));
            stockRepository.save(stock);
        } else {
            InventoryStockEntity stock = new InventoryStockEntity();
            stock.setDrugCode(item.getDrugCode());
            stock.setBatchNo(batchNo);
            stock.setQuantity(item.getQuantity());
            stock.setUnit(item.getUnit());
            stock.setPurchasePrice(item.getUnitPrice());
            stock.setRetailPrice(item.getUnitPrice());
            stock.setWarehouseLocation(targetDept);
            stockRepository.save(stock);
        }
    }
}
