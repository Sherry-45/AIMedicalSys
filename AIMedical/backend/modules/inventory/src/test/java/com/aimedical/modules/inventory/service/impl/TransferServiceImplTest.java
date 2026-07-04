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
import com.aimedical.modules.inventory.repository.InventoryStockRepository;
import com.aimedical.modules.inventory.repository.TransferItemRepository;
import com.aimedical.modules.inventory.repository.TransferOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link TransferServiceImpl} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock private TransferOrderRepository transferOrderRepository;
    @Mock private TransferItemRepository transferItemRepository;
    @Mock private InventoryStockRepository stockRepository;
    @Mock private InventoryConverter converter;

    private TransferServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TransferServiceImpl(transferOrderRepository, transferItemRepository,
                stockRepository, converter);
    }

    // ==================== create ====================

    @Test
    void createShouldFailWhenItemsNull() {
        TransferCreateRequest req = new TransferCreateRequest();
        req.setTransferType("INVENTORY_TO_PHARMACY");
        req.setItems(null);

        Result<TransferOrderResponse> result = service.create(req, 1L, "张三");

        assertEquals(InventoryErrorCode.TRANSFER_ITEM_EMPTY.getCode(), result.getCode());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void createShouldFailWhenItemsEmpty() {
        TransferCreateRequest req = new TransferCreateRequest();
        req.setTransferType("INVENTORY_TO_PHARMACY");
        req.setItems(Collections.emptyList());

        Result<TransferOrderResponse> result = service.create(req, 1L, "张三");

        assertEquals(InventoryErrorCode.TRANSFER_ITEM_EMPTY.getCode(), result.getCode());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void createShouldFailWhenDeptSame() {
        TransferCreateRequest req = new TransferCreateRequest();
        req.setTransferType("PHARMACY_TO_PHARMACY");
        req.setSourceDept("药房A");
        req.setTargetDept("药房A");
        req.setItems(List.of(buildItemRequest("DRG001", "B01", new BigDecimal("5"), new BigDecimal("10"))));

        Result<TransferOrderResponse> result = service.create(req, 1L, "张三");

        assertEquals(InventoryErrorCode.TRANSFER_DEPT_SAME.getCode(), result.getCode());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void createShouldSaveAndComputeTotals() {
        TransferItemRequest item1 = buildItemRequest("DRG001", "B01",
                new BigDecimal("5"), new BigDecimal("10"));
        TransferItemRequest item2 = buildItemRequest("DRG002", "B02",
                new BigDecimal("3"), new BigDecimal("20"));
        TransferCreateRequest req = new TransferCreateRequest();
        req.setTransferType("INVENTORY_TO_PHARMACY");
        req.setSourceDept("药库");
        req.setTargetDept("门诊药房");
        req.setRemark("调拨备注");
        req.setItems(List.of(item1, item2));

        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> {
            TransferOrderEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());

        Result<TransferOrderResponse> result = service.create(req, 1L, "张三");

        assertEquals("SUCCESS", result.getCode());

        ArgumentCaptor<TransferOrderEntity> orderCaptor = ArgumentCaptor.forClass(TransferOrderEntity.class);
        verify(transferOrderRepository).save(orderCaptor.capture());
        TransferOrderEntity saved = orderCaptor.getValue();
        assertEquals("INVENTORY_TO_PHARMACY", saved.getTransferType());
        assertEquals("DRAFT", saved.getStatus());
        assertEquals("药库", saved.getSourceDept());
        assertEquals("门诊药房", saved.getTargetDept());
        assertEquals(1L, saved.getApplicantId());
        assertEquals("张三", saved.getApplicantName());
        assertEquals(2, saved.getTotalItems());
        assertEquals(new BigDecimal("110"), saved.getTotalAmount());
        assertTrue(saved.getTransferNo().startsWith("TRF"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TransferItemEntity>> itemsCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(transferItemRepository).saveAll(itemsCaptor.capture());
        List<TransferItemEntity> items = itemsCaptor.getValue();
        assertEquals(2, items.size());
        assertEquals(1L, items.get(0).getTransferId());
        assertEquals(new BigDecimal("50"), items.get(0).getAmount());
        assertEquals(new BigDecimal("60"), items.get(1).getAmount());
    }

    @Test
    void createShouldDefaultNullPriceAndQuantityToZero() {
        TransferItemRequest item = new TransferItemRequest();
        item.setDrugCode("DRG001");
        item.setUnitPrice(null);
        item.setQuantity(null);
        TransferCreateRequest req = new TransferCreateRequest();
        req.setTransferType("INVENTORY_TO_PHARMACY");
        req.setItems(List.of(item));

        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> {
            TransferOrderEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());

        Result<TransferOrderResponse> result = service.create(req, 1L, "张三");

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<TransferOrderEntity> captor = ArgumentCaptor.forClass(TransferOrderEntity.class);
        verify(transferOrderRepository).save(captor.capture());
        assertEquals(BigDecimal.ZERO, captor.getValue().getTotalAmount());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TransferItemEntity>> itemsCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(transferItemRepository).saveAll(itemsCaptor.capture());
        assertEquals(BigDecimal.ZERO, itemsCaptor.getValue().get(0).getAmount());
    }

    @Test
    void createShouldAllowNullSourceDept() {
        // sourceDept 为 null 时，equals 检查不会触发 TRANSFER_DEPT_SAME
        TransferItemRequest item = buildItemRequest("DRG001", "B01",
                new BigDecimal("1"), new BigDecimal("10"));
        TransferCreateRequest req = new TransferCreateRequest();
        req.setTransferType("INVENTORY_TO_PHARMACY");
        req.setSourceDept(null);
        req.setTargetDept("药房");
        req.setItems(List.of(item));

        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> {
            TransferOrderEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());

        Result<TransferOrderResponse> result = service.create(req, 1L, "张三");

        assertEquals("SUCCESS", result.getCode());
    }

    // ==================== submit ====================

    @Test
    void submitShouldFailWhenNotFound() {
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.empty());

        Result<TransferOrderResponse> result = service.submit(1L);

        assertEquals(InventoryErrorCode.TRANSFER_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    void submitShouldFailWhenNotDraft() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setStatus("PENDING_APPROVAL");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<TransferOrderResponse> result = service.submit(1L);

        assertEquals(InventoryErrorCode.TRANSFER_INVALID_STATE.getCode(), result.getCode());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void submitShouldFailWhenItemsEmpty() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("DRAFT");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(Collections.emptyList());

        Result<TransferOrderResponse> result = service.submit(1L);

        assertEquals(InventoryErrorCode.TRANSFER_ITEM_EMPTY.getCode(), result.getCode());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void submitShouldTransitionToPendingApproval() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("DRAFT");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferItemEntity item = new TransferItemEntity();
        when(transferItemRepository.findByTransferId(1L)).thenReturn(List.of(item));
        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());

        Result<TransferOrderResponse> result = service.submit(1L);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<TransferOrderEntity> captor = ArgumentCaptor.forClass(TransferOrderEntity.class);
        verify(transferOrderRepository).save(captor.capture());
        assertEquals("PENDING_APPROVAL", captor.getValue().getStatus());
    }

    @Test
    void submitShouldReturnConflictOnOptimisticLock() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("DRAFT");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferItemEntity item = new TransferItemEntity();
        when(transferItemRepository.findByTransferId(1L)).thenReturn(List.of(item));
        when(transferOrderRepository.save(any(TransferOrderEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<TransferOrderResponse> result = service.submit(1L);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
    }

    // ==================== approve ====================

    @Test
    void approveShouldFailWhenNotFound() {
        TransferApproveRequest req = new TransferApproveRequest();
        req.setApproved(true);
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.empty());

        Result<TransferOrderResponse> result = service.approve(1L, req, 2L, "审批人");

        assertEquals(InventoryErrorCode.TRANSFER_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    void approveShouldFailWhenNotPendingApproval() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setStatus("DRAFT");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferApproveRequest req = new TransferApproveRequest();
        req.setApproved(true);

        Result<TransferOrderResponse> result = service.approve(1L, req, 2L, "审批人");

        assertEquals(InventoryErrorCode.TRANSFER_INVALID_STATE.getCode(), result.getCode());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void approveShouldFailWhenRejectWithoutReason() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setStatus("PENDING_APPROVAL");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferApproveRequest req = new TransferApproveRequest();
        req.setApproved(false);
        req.setRejectReason(null);

        Result<TransferOrderResponse> result = service.approve(1L, req, 2L, "审批人");

        assertEquals(InventoryErrorCode.TRANSFER_APPROVE_REJECT_REASON_REQUIRED.getCode(), result.getCode());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void approveShouldFailWhenRejectWithBlankReason() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setStatus("PENDING_APPROVAL");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferApproveRequest req = new TransferApproveRequest();
        req.setApproved(false);
        req.setRejectReason("  ");

        Result<TransferOrderResponse> result = service.approve(1L, req, 2L, "审批人");

        assertEquals(InventoryErrorCode.TRANSFER_APPROVE_REJECT_REASON_REQUIRED.getCode(), result.getCode());
    }

    @Test
    void approveShouldTransitionToApprovedWhenApproved() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("PENDING_APPROVAL");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(Collections.emptyList());
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());
        TransferApproveRequest req = new TransferApproveRequest();
        req.setApproved(true);

        Result<TransferOrderResponse> result = service.approve(1L, req, 2L, "审批人");

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<TransferOrderEntity> captor = ArgumentCaptor.forClass(TransferOrderEntity.class);
        verify(transferOrderRepository).save(captor.capture());
        assertEquals("APPROVED", captor.getValue().getStatus());
        assertEquals(2L, captor.getValue().getApproverId());
        assertEquals("审批人", captor.getValue().getApproverName());
        assertNotNull(captor.getValue().getApprovedAt());
        assertNull(captor.getValue().getRejectReason());
    }

    @Test
    void approveShouldTransitionToRejectedWhenRejectedWithReason() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("PENDING_APPROVAL");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(Collections.emptyList());
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());
        TransferApproveRequest req = new TransferApproveRequest();
        req.setApproved(false);
        req.setRejectReason("数量不符");

        Result<TransferOrderResponse> result = service.approve(1L, req, 2L, "审批人");

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<TransferOrderEntity> captor = ArgumentCaptor.forClass(TransferOrderEntity.class);
        verify(transferOrderRepository).save(captor.capture());
        assertEquals("REJECTED", captor.getValue().getStatus());
        assertEquals("数量不符", captor.getValue().getRejectReason());
    }

    @Test
    void approveShouldHandleNullApprovedAsRejected() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setStatus("PENDING_APPROVAL");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferApproveRequest req = new TransferApproveRequest();
        req.setApproved(null);
        req.setRejectReason("原因");

        Result<TransferOrderResponse> result = service.approve(1L, req, 2L, "审批人");

        // approved=null → Boolean.TRUE.equals(null)=false → 拒绝，有原因 → 通过
        assertEquals("SUCCESS", result.getCode());
    }

    @Test
    void approveShouldReturnConflictOnOptimisticLock() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("PENDING_APPROVAL");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(transferOrderRepository.save(any(TransferOrderEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));
        TransferApproveRequest req = new TransferApproveRequest();
        req.setApproved(true);

        Result<TransferOrderResponse> result = service.approve(1L, req, 2L, "审批人");

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
    }

    // ==================== ship ====================

    @Test
    void shipShouldFailWhenNotFound() {
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.empty());

        Result<TransferOrderResponse> result = service.ship(1L);

        assertEquals(InventoryErrorCode.TRANSFER_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    void shipShouldFailWhenNotApproved() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setStatus("DRAFT");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<TransferOrderResponse> result = service.ship(1L);

        assertEquals(InventoryErrorCode.TRANSFER_INVALID_STATE.getCode(), result.getCode());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void shipShouldFailWhenStockBatchNotFound() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferItemEntity item = new TransferItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B01");
        item.setQuantity(new BigDecimal("5"));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(List.of(item));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B01"))
                .thenReturn(Optional.empty());

        Result<TransferOrderResponse> result = service.ship(1L);

        assertEquals(InventoryErrorCode.STOCK_BATCH_NOT_FOUND.getCode(), result.getCode());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void shipShouldFailWhenStockInsufficient() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferItemEntity item = new TransferItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B01");
        item.setQuantity(new BigDecimal("100"));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(List.of(item));
        InventoryStockEntity stock = new InventoryStockEntity();
        stock.setQuantity(new BigDecimal("50"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B01"))
                .thenReturn(Optional.of(stock));

        Result<TransferOrderResponse> result = service.ship(1L);

        assertEquals(InventoryErrorCode.STOCK_INSUFFICIENT.getCode(), result.getCode());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void shipShouldReduceStockAndTransitionToInTransit() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferItemEntity item = new TransferItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B01");
        item.setQuantity(new BigDecimal("5"));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(List.of(item));
        InventoryStockEntity stock = new InventoryStockEntity();
        stock.setQuantity(new BigDecimal("100"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B01"))
                .thenReturn(Optional.of(stock));
        when(stockRepository.save(any(InventoryStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());

        Result<TransferOrderResponse> result = service.ship(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(new BigDecimal("95"), stock.getQuantity());
        ArgumentCaptor<TransferOrderEntity> captor = ArgumentCaptor.forClass(TransferOrderEntity.class);
        verify(transferOrderRepository).save(captor.capture());
        assertEquals("IN_TRANSIT", captor.getValue().getStatus());
        assertNotNull(captor.getValue().getShippedAt());
    }

    @Test
    void shipShouldSkipReduceWhenBatchNoBlank() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferItemEntity item = new TransferItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("  ");
        item.setQuantity(new BigDecimal("5"));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(List.of(item));
        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());

        Result<TransferOrderResponse> result = service.ship(1L);

        assertEquals("SUCCESS", result.getCode());
        verify(stockRepository, never()).findByDrugCodeAndBatchNo(any(), any());
        verify(stockRepository, never()).save(any());
    }

    @Test
    void shipShouldReturnConflictOnOptimisticLock() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferItemEntity item = new TransferItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("  ");
        item.setQuantity(new BigDecimal("5"));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(List.of(item));
        when(transferOrderRepository.save(any(TransferOrderEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<TransferOrderResponse> result = service.ship(1L);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
    }

    // ==================== receive ====================

    @Test
    void receiveShouldFailWhenNotFound() {
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.empty());

        Result<TransferOrderResponse> result = service.receive(1L);

        assertEquals(InventoryErrorCode.TRANSFER_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    void receiveShouldFailWhenNotInTransit() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setStatus("APPROVED");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<TransferOrderResponse> result = service.receive(1L);

        assertEquals(InventoryErrorCode.TRANSFER_INVALID_STATE.getCode(), result.getCode());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void receiveShouldAddToExistingStock() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("IN_TRANSIT");
        entity.setTargetDept("门诊药房");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferItemEntity item = new TransferItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B01");
        item.setQuantity(new BigDecimal("5"));
        item.setUnit("盒");
        item.setUnitPrice(new BigDecimal("10"));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(List.of(item));
        InventoryStockEntity stock = new InventoryStockEntity();
        stock.setQuantity(new BigDecimal("100"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B01"))
                .thenReturn(Optional.of(stock));
        when(stockRepository.save(any(InventoryStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());

        Result<TransferOrderResponse> result = service.receive(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(new BigDecimal("105"), stock.getQuantity());
        ArgumentCaptor<TransferOrderEntity> captor = ArgumentCaptor.forClass(TransferOrderEntity.class);
        verify(transferOrderRepository).save(captor.capture());
        assertEquals("RECEIVED", captor.getValue().getStatus());
        assertNotNull(captor.getValue().getReceivedAt());
    }

    @Test
    void receiveShouldCreateNewStockWhenBatchNotFound() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("IN_TRANSIT");
        entity.setTargetDept("门诊药房");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferItemEntity item = new TransferItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B02");
        item.setQuantity(new BigDecimal("50"));
        item.setUnit("盒");
        item.setUnitPrice(new BigDecimal("15"));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(List.of(item));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B02"))
                .thenReturn(Optional.empty());
        when(stockRepository.save(any(InventoryStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());

        Result<TransferOrderResponse> result = service.receive(1L);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<InventoryStockEntity> stockCaptor = ArgumentCaptor.forClass(InventoryStockEntity.class);
        verify(stockRepository).save(stockCaptor.capture());
        InventoryStockEntity created = stockCaptor.getValue();
        assertEquals("DRG001", created.getDrugCode());
        assertEquals("B02", created.getBatchNo());
        assertEquals(new BigDecimal("50"), created.getQuantity());
        assertEquals("盒", created.getUnit());
        assertEquals(new BigDecimal("15"), created.getPurchasePrice());
        assertEquals(new BigDecimal("15"), created.getRetailPrice());
        assertEquals("门诊药房", created.getWarehouseLocation());
    }

    @Test
    void receiveShouldUseDefaultBatchWhenBatchNoBlank() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("IN_TRANSIT");
        entity.setTargetDept("门诊药房");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferItemEntity item = new TransferItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("  ");
        item.setQuantity(new BigDecimal("10"));
        item.setUnit("盒");
        item.setUnitPrice(new BigDecimal("20"));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(List.of(item));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "DEFAULT"))
                .thenReturn(Optional.empty());
        when(stockRepository.save(any(InventoryStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());

        Result<TransferOrderResponse> result = service.receive(1L);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<InventoryStockEntity> stockCaptor = ArgumentCaptor.forClass(InventoryStockEntity.class);
        verify(stockRepository).save(stockCaptor.capture());
        assertEquals("DEFAULT", stockCaptor.getValue().getBatchNo());
    }

    @Test
    void receiveShouldReturnConflictOnOptimisticLock() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("IN_TRANSIT");
        entity.setTargetDept("门诊药房");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferItemEntity item = new TransferItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("  ");
        item.setQuantity(new BigDecimal("10"));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(List.of(item));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "DEFAULT"))
                .thenReturn(Optional.empty());
        when(stockRepository.save(any(InventoryStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transferOrderRepository.save(any(TransferOrderEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<TransferOrderResponse> result = service.receive(1L);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
    }

    // ==================== cancel ====================

    @Test
    void cancelShouldFailWhenNotFound() {
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.empty());

        Result<TransferOrderResponse> result = service.cancel(1L);

        assertEquals(InventoryErrorCode.TRANSFER_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    void cancelShouldFailWhenApproved() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setStatus("APPROVED");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<TransferOrderResponse> result = service.cancel(1L);

        assertEquals(InventoryErrorCode.TRANSFER_INVALID_STATE.getCode(), result.getCode());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void cancelShouldFailWhenReceived() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setStatus("RECEIVED");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<TransferOrderResponse> result = service.cancel(1L);

        assertEquals(InventoryErrorCode.TRANSFER_INVALID_STATE.getCode(), result.getCode());
    }

    @Test
    void cancelShouldTransitionFromDraft() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("DRAFT");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(Collections.emptyList());
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());

        Result<TransferOrderResponse> result = service.cancel(1L);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<TransferOrderEntity> captor = ArgumentCaptor.forClass(TransferOrderEntity.class);
        verify(transferOrderRepository).save(captor.capture());
        assertEquals("CANCELLED", captor.getValue().getStatus());
    }

    @Test
    void cancelShouldTransitionFromPendingApproval() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("PENDING_APPROVAL");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(transferOrderRepository.save(any(TransferOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transferItemRepository.findByTransferId(1L)).thenReturn(Collections.emptyList());
        when(converter.toTransferResponse(any(TransferOrderEntity.class), any()))
                .thenReturn(new TransferOrderResponse());

        Result<TransferOrderResponse> result = service.cancel(1L);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<TransferOrderEntity> captor = ArgumentCaptor.forClass(TransferOrderEntity.class);
        verify(transferOrderRepository).save(captor.capture());
        assertEquals("CANCELLED", captor.getValue().getStatus());
    }

    @Test
    void cancelShouldReturnConflictOnOptimisticLock() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setStatus("DRAFT");
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(transferOrderRepository.save(any(TransferOrderEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<TransferOrderResponse> result = service.cancel(1L);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
    }

    // ==================== getById ====================

    @Test
    void getByIdShouldFailWhenNotFound() {
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.empty());

        Result<TransferOrderResponse> result = service.getById(1L);

        assertEquals(InventoryErrorCode.TRANSFER_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    void getByIdShouldReturnResponseWithItems() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(entity));
        TransferItemEntity item = new TransferItemEntity();
        item.setId(11L);
        when(transferItemRepository.findByTransferId(1L)).thenReturn(List.of(item));
        TransferOrderResponse expected = new TransferOrderResponse();
        expected.setId(1L);
        when(converter.toTransferResponse(entity, List.of(item))).thenReturn(expected);

        Result<TransferOrderResponse> result = service.getById(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1L, result.getData().getId());
    }

    // ==================== query ====================

    @Test
    void queryShouldFilterByStatusAndTypeWhenBothProvided() {
        TransferQueryRequest req = new TransferQueryRequest();
        req.setStatus("DRAFT");
        req.setTransferType("INVENTORY_TO_PHARMACY");
        when(transferOrderRepository.findByStatusAndTransferType(
                eq("DRAFT"), eq("INVENTORY_TO_PHARMACY"), any(Pageable.class)))
                .thenReturn(emptyPage());

        Result<Page<TransferOrderResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(transferOrderRepository).findByStatusAndTransferType(
                eq("DRAFT"), eq("INVENTORY_TO_PHARMACY"), any(Pageable.class));
    }

    @Test
    void queryShouldFilterByStatusOnly() {
        TransferQueryRequest req = new TransferQueryRequest();
        req.setStatus("DRAFT");
        when(transferOrderRepository.findByStatus(eq("DRAFT"), any(Pageable.class)))
                .thenReturn(emptyPage());

        Result<Page<TransferOrderResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(transferOrderRepository).findByStatus(eq("DRAFT"), any(Pageable.class));
    }

    @Test
    void queryShouldFilterByTypeOnly() {
        TransferQueryRequest req = new TransferQueryRequest();
        req.setTransferType("INVENTORY_TO_PHARMACY");
        when(transferOrderRepository.findByTransferType(eq("INVENTORY_TO_PHARMACY"), any(Pageable.class)))
                .thenReturn(emptyPage());

        Result<Page<TransferOrderResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(transferOrderRepository).findByTransferType(eq("INVENTORY_TO_PHARMACY"), any(Pageable.class));
    }

    @Test
    void queryShouldFindAllWhenNoFilter() {
        TransferQueryRequest req = new TransferQueryRequest();
        when(transferOrderRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        Result<Page<TransferOrderResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(transferOrderRepository).findAll(any(Pageable.class));
    }

    @Test
    void queryShouldMapEntitiesWithItems() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        Page<TransferOrderEntity> page = new PageImpl<>(List.of(entity));
        when(transferOrderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(transferItemRepository.findByTransferId(1L)).thenReturn(Collections.emptyList());
        TransferOrderResponse resp = new TransferOrderResponse();
        resp.setId(1L);
        when(converter.toTransferResponse(entity, Collections.emptyList())).thenReturn(resp);

        Result<Page<TransferOrderResponse>> result = service.query(new TransferQueryRequest());

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData().getContent().size());
        assertEquals(1L, result.getData().getContent().get(0).getId());
    }

    @Test
    void queryShouldUseDefaultPageAndSizeWhenNull() {
        TransferQueryRequest req = new TransferQueryRequest();
        req.setPage(null);
        req.setSize(null);
        when(transferOrderRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        service.query(req);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(transferOrderRepository).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
    }

    @Test
    void queryShouldHandleBlankStatusAndTypeAsAbsent() {
        TransferQueryRequest req = new TransferQueryRequest();
        req.setStatus("  ");
        req.setTransferType("  ");
        when(transferOrderRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        Result<Page<TransferOrderResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(transferOrderRepository).findAll(any(Pageable.class));
    }

    // ==================== helpers ====================

    private TransferItemRequest buildItemRequest(String drugCode, String batchNo,
                                                  BigDecimal quantity, BigDecimal unitPrice) {
        TransferItemRequest item = new TransferItemRequest();
        item.setDrugCode(drugCode);
        item.setBatchNo(batchNo);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setUnit("盒");
        return item;
    }

    private Page<TransferOrderEntity> emptyPage() {
        return new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 20), 0);
    }
}
