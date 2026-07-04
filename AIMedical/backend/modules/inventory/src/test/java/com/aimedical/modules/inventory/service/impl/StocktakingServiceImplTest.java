package com.aimedical.modules.inventory.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.InventoryErrorCode;
import com.aimedical.modules.inventory.converter.InventoryConverter;
import com.aimedical.modules.inventory.dto.request.StocktakingCreateRequest;
import com.aimedical.modules.inventory.dto.request.StocktakingItemRequest;
import com.aimedical.modules.inventory.dto.request.StocktakingQueryRequest;
import com.aimedical.modules.inventory.dto.response.StocktakingResponse;
import com.aimedical.modules.inventory.entity.InventoryStockEntity;
import com.aimedical.modules.inventory.entity.StocktakingEntity;
import com.aimedical.modules.inventory.entity.StocktakingItemEntity;
import com.aimedical.modules.inventory.repository.InventoryStockRepository;
import com.aimedical.modules.inventory.repository.StocktakingItemRepository;
import com.aimedical.modules.inventory.repository.StocktakingRepository;
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
 * {@link StocktakingServiceImpl} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class StocktakingServiceImplTest {

    @Mock private StocktakingRepository stocktakingRepository;
    @Mock private StocktakingItemRepository stocktakingItemRepository;
    @Mock private InventoryStockRepository stockRepository;
    @Mock private InventoryConverter converter;

    private StocktakingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StocktakingServiceImpl(stocktakingRepository, stocktakingItemRepository,
                stockRepository, converter);
    }

    // ==================== create ====================

    @Test
    void createShouldFailWhenItemsNull() {
        StocktakingCreateRequest req = new StocktakingCreateRequest();
        req.setItems(null);

        Result<StocktakingResponse> result = service.create(req, 1L, "张三");

        assertEquals(InventoryErrorCode.STOCKTAKING_ITEM_EMPTY.getCode(), result.getCode());
        assertNull(result.getData());
        verify(stocktakingRepository, never()).save(any());
    }

    @Test
    void createShouldFailWhenItemsEmpty() {
        StocktakingCreateRequest req = new StocktakingCreateRequest();
        req.setItems(Collections.emptyList());

        Result<StocktakingResponse> result = service.create(req, 1L, "张三");

        assertEquals(InventoryErrorCode.STOCKTAKING_ITEM_EMPTY.getCode(), result.getCode());
        verify(stocktakingRepository, never()).save(any());
    }

    @Test
    void createShouldSaveWithBookQuantityFromExactBatch() {
        StocktakingItemRequest itemReq = new StocktakingItemRequest();
        itemReq.setDrugCode("DRG001");
        itemReq.setDrugName("阿莫西林");
        itemReq.setBatchNo("B01");
        itemReq.setUnit("盒");
        StocktakingCreateRequest req = new StocktakingCreateRequest();
        req.setStocktakingType("PARTIAL");
        req.setRemark("备注");
        req.setItems(List.of(itemReq));

        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> {
            StocktakingEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });
        InventoryStockEntity stock = new InventoryStockEntity();
        stock.setQuantity(new BigDecimal("100"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B01"))
                .thenReturn(Optional.of(stock));
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.create(req, 1L, "张三");

        assertEquals("SUCCESS", result.getCode());

        ArgumentCaptor<StocktakingEntity> entityCaptor = ArgumentCaptor.forClass(StocktakingEntity.class);
        verify(stocktakingRepository).save(entityCaptor.capture());
        StocktakingEntity saved = entityCaptor.getValue();
        assertEquals("PARTIAL", saved.getStocktakingType());
        assertEquals("DRAFT", saved.getStatus());
        assertEquals(1L, saved.getOperatorId());
        assertEquals("张三", saved.getOperatorName());
        assertEquals(1, saved.getTotalItems());
        assertEquals(0, saved.getSurplusItems());
        assertEquals(0, saved.getLossItems());
        assertTrue(saved.getStocktakingNo().startsWith("STK"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StocktakingItemEntity>> itemsCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(stocktakingItemRepository).saveAll(itemsCaptor.capture());
        List<StocktakingItemEntity> items = itemsCaptor.getValue();
        assertEquals(1, items.size());
        assertEquals(10L, items.get(0).getStocktakingId());
        assertEquals(new BigDecimal("100"), items.get(0).getBookQuantity());
        assertNull(items.get(0).getActualQuantity());
        assertNull(items.get(0).getDifference());
        assertEquals("NONE", items.get(0).getDifferenceType());
    }

    @Test
    void createShouldDefaultTypeToFullWhenNull() {
        StocktakingItemRequest itemReq = new StocktakingItemRequest();
        itemReq.setDrugCode("DRG001");
        StocktakingCreateRequest req = new StocktakingCreateRequest();
        req.setStocktakingType(null);
        req.setItems(List.of(itemReq));

        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> {
            StocktakingEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(Collections.emptyList());
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.create(req, 1L, "张三");

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<StocktakingEntity> captor = ArgumentCaptor.forClass(StocktakingEntity.class);
        verify(stocktakingRepository).save(captor.capture());
        assertEquals("FULL", captor.getValue().getStocktakingType());
    }

    @Test
    void createShouldDefaultTypeToFullWhenBlank() {
        StocktakingItemRequest itemReq = new StocktakingItemRequest();
        itemReq.setDrugCode("DRG001");
        StocktakingCreateRequest req = new StocktakingCreateRequest();
        req.setStocktakingType("  ");
        req.setItems(List.of(itemReq));

        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> {
            StocktakingEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(Collections.emptyList());
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.create(req, 1L, "张三");

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<StocktakingEntity> captor = ArgumentCaptor.forClass(StocktakingEntity.class);
        verify(stocktakingRepository).save(captor.capture());
        assertEquals("FULL", captor.getValue().getStocktakingType());
    }

    @Test
    void createShouldSumBatchesWhenBatchNoBlank() {
        StocktakingItemRequest itemReq = new StocktakingItemRequest();
        itemReq.setDrugCode("DRG001");
        itemReq.setBatchNo("  ");
        StocktakingCreateRequest req = new StocktakingCreateRequest();
        req.setItems(List.of(itemReq));

        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> {
            StocktakingEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });
        InventoryStockEntity s1 = new InventoryStockEntity();
        s1.setQuantity(new BigDecimal("30"));
        InventoryStockEntity s2 = new InventoryStockEntity();
        s2.setQuantity(new BigDecimal("70"));
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(List.of(s1, s2));
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.create(req, 1L, "张三");

        assertEquals("SUCCESS", result.getCode());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StocktakingItemEntity>> itemsCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(stocktakingItemRepository).saveAll(itemsCaptor.capture());
        assertEquals(new BigDecimal("100"), itemsCaptor.getValue().get(0).getBookQuantity());
    }

    @Test
    void createShouldHandleNullQuantityWhenSummingBatches() {
        StocktakingItemRequest itemReq = new StocktakingItemRequest();
        itemReq.setDrugCode("DRG001");
        StocktakingCreateRequest req = new StocktakingCreateRequest();
        req.setItems(List.of(itemReq));

        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> {
            StocktakingEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });
        InventoryStockEntity s1 = new InventoryStockEntity();
        s1.setQuantity(null);
        InventoryStockEntity s2 = new InventoryStockEntity();
        s2.setQuantity(new BigDecimal("50"));
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(List.of(s1, s2));
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.create(req, 1L, "张三");

        assertEquals("SUCCESS", result.getCode());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StocktakingItemEntity>> itemsCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(stocktakingItemRepository).saveAll(itemsCaptor.capture());
        assertEquals(new BigDecimal("50"), itemsCaptor.getValue().get(0).getBookQuantity());
    }

    @Test
    void createShouldUseZeroBookQuantityWhenBatchNotFound() {
        StocktakingItemRequest itemReq = new StocktakingItemRequest();
        itemReq.setDrugCode("DRG001");
        itemReq.setBatchNo("B99");
        StocktakingCreateRequest req = new StocktakingCreateRequest();
        req.setItems(List.of(itemReq));

        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> {
            StocktakingEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B99"))
                .thenReturn(Optional.empty());
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.create(req, 1L, "张三");

        assertEquals("SUCCESS", result.getCode());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StocktakingItemEntity>> itemsCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(stocktakingItemRepository).saveAll(itemsCaptor.capture());
        assertEquals(BigDecimal.ZERO, itemsCaptor.getValue().get(0).getBookQuantity());
    }

    // ==================== start ====================

    @Test
    void startShouldFailWhenNotFound() {
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.empty());

        Result<StocktakingResponse> result = service.start(1L);

        assertEquals(InventoryErrorCode.STOCKTAKING_NOT_FOUND.getCode(), result.getCode());
        verify(stocktakingRepository, never()).save(any());
    }

    @Test
    void startShouldFailWhenNotDraft() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setStatus("IN_PROGRESS");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<StocktakingResponse> result = service.start(1L);

        assertEquals(InventoryErrorCode.STOCKTAKING_INVALID_STATE.getCode(), result.getCode());
        verify(stocktakingRepository, never()).save(any());
    }

    @Test
    void startShouldTransitionToInProgress() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("DRAFT");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(Collections.emptyList());
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.start(1L);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<StocktakingEntity> captor = ArgumentCaptor.forClass(StocktakingEntity.class);
        verify(stocktakingRepository).save(captor.capture());
        assertEquals("IN_PROGRESS", captor.getValue().getStatus());
        assertNotNull(captor.getValue().getStartTime());
    }

    @Test
    void startShouldReturnConflictOnOptimisticLock() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("DRAFT");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(stocktakingRepository.save(any(StocktakingEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<StocktakingResponse> result = service.start(1L);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
    }

    // ==================== submitActual ====================

    @Test
    void submitActualShouldFailWhenNotFound() {
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.empty());

        Result<StocktakingResponse> result = service.submitActual(1L, List.of(new StocktakingItemRequest()));

        assertEquals(InventoryErrorCode.STOCKTAKING_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    void submitActualShouldFailWhenNotInProgress() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setStatus("DRAFT");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<StocktakingResponse> result = service.submitActual(1L, List.of(new StocktakingItemRequest()));

        assertEquals(InventoryErrorCode.STOCKTAKING_INVALID_STATE.getCode(), result.getCode());
    }

    @Test
    void submitActualShouldFailWhenItemsNull() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setStatus("IN_PROGRESS");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<StocktakingResponse> result = service.submitActual(1L, null);

        assertEquals(InventoryErrorCode.STOCKTAKING_ACTUAL_EMPTY.getCode(), result.getCode());
    }

    @Test
    void submitActualShouldFailWhenItemsEmpty() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setStatus("IN_PROGRESS");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<StocktakingResponse> result = service.submitActual(1L, Collections.emptyList());

        assertEquals(InventoryErrorCode.STOCKTAKING_ACTUAL_EMPTY.getCode(), result.getCode());
    }

    @Test
    void submitActualShouldUpdateMatchingItems() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("IN_PROGRESS");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity existing = new StocktakingItemEntity();
        existing.setId(11L);
        existing.setStocktakingId(1L);
        existing.setDrugCode("DRG001");
        existing.setBatchNo("B01");
        existing.setBookQuantity(new BigDecimal("100"));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(existing));

        StocktakingItemRequest req = new StocktakingItemRequest();
        req.setDrugCode("DRG001");
        req.setBatchNo("B01");
        req.setActualQuantity(new BigDecimal("98"));
        req.setRemark("实盘备注");

        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.submitActual(1L, List.of(req));

        assertEquals("SUCCESS", result.getCode());
        assertEquals(new BigDecimal("98"), existing.getActualQuantity());
        assertEquals("实盘备注", existing.getRemark());
        verify(stocktakingItemRepository).saveAll(List.of(existing));
    }

    @Test
    void submitActualShouldNotUpdateRemarkWhenBlank() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("IN_PROGRESS");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity existing = new StocktakingItemEntity();
        existing.setDrugCode("DRG001");
        existing.setBatchNo("B01");
        existing.setRemark("原备注");
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(existing));

        StocktakingItemRequest req = new StocktakingItemRequest();
        req.setDrugCode("DRG001");
        req.setBatchNo("B01");
        req.setActualQuantity(new BigDecimal("98"));
        req.setRemark("  ");

        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.submitActual(1L, List.of(req));

        assertEquals("SUCCESS", result.getCode());
        assertEquals("原备注", existing.getRemark());
    }

    @Test
    void submitActualShouldMatchByDrugCodeWhenBatchNoBlank() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("IN_PROGRESS");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity existing = new StocktakingItemEntity();
        existing.setDrugCode("DRG001");
        existing.setBatchNo(null);
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(existing));

        StocktakingItemRequest req = new StocktakingItemRequest();
        req.setDrugCode("DRG001");
        req.setBatchNo(null);
        req.setActualQuantity(new BigDecimal("50"));

        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.submitActual(1L, List.of(req));

        assertEquals("SUCCESS", result.getCode());
        assertEquals(new BigDecimal("50"), existing.getActualQuantity());
    }

    @Test
    void submitActualShouldNotMatchWhenBatchNoDiffers() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("IN_PROGRESS");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity existing = new StocktakingItemEntity();
        existing.setDrugCode("DRG001");
        existing.setBatchNo("B01");
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(existing));

        StocktakingItemRequest req = new StocktakingItemRequest();
        req.setDrugCode("DRG001");
        req.setBatchNo("B02");
        req.setActualQuantity(new BigDecimal("50"));

        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        service.submitActual(1L, List.of(req));

        // 不匹配时 actualQuantity 不应被设置
        assertNull(existing.getActualQuantity());
    }

    // ==================== submitForApproval ====================

    @Test
    void submitForApprovalShouldFailWhenNotFound() {
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.empty());

        Result<StocktakingResponse> result = service.submitForApproval(1L, 9L, "审批员");

        assertEquals(InventoryErrorCode.STOCKTAKING_NOT_FOUND.getCode(), result.getCode());
        verify(stocktakingRepository, never()).save(any());
    }

    @Test
    void submitForApprovalShouldFailWhenNotInProgress() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setStatus("DRAFT");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<StocktakingResponse> result = service.submitForApproval(1L, 9L, "审批员");

        assertEquals(InventoryErrorCode.STOCKTAKING_INVALID_STATE.getCode(), result.getCode());
        verify(stocktakingRepository, never()).save(any());
    }

    @Test
    void submitForApprovalShouldTransitionToPendingApproval() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("IN_PROGRESS");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(Collections.emptyList());
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.submitForApproval(1L, 9L, "审批员");

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<StocktakingEntity> captor = ArgumentCaptor.forClass(StocktakingEntity.class);
        verify(stocktakingRepository).save(captor.capture());
        assertEquals("PENDING_APPROVAL", captor.getValue().getStatus());
    }

    @Test
    void submitForApprovalShouldReturnConflictOnOptimisticLock() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("IN_PROGRESS");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(stocktakingRepository.save(any(StocktakingEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<StocktakingResponse> result = service.submitForApproval(1L, 9L, "审批员");

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
    }

    // ==================== approve ====================

    @Test
    void approveShouldFailWhenNotFound() {
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.empty());

        Result<StocktakingResponse> result = service.approve(1L, 9L, "审批员");

        assertEquals(InventoryErrorCode.STOCKTAKING_NOT_FOUND.getCode(), result.getCode());
        verify(stocktakingRepository, never()).save(any());
    }

    @Test
    void approveShouldFailWhenNotPendingApproval() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setStatus("IN_PROGRESS");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<StocktakingResponse> result = service.approve(1L, 9L, "审批员");

        assertEquals(InventoryErrorCode.STOCKTAKING_INVALID_STATE.getCode(), result.getCode());
        verify(stocktakingRepository, never()).save(any());
    }

    @Test
    void approveShouldTransitionToApprovedAndRecordApprover() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("PENDING_APPROVAL");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(Collections.emptyList());
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.approve(1L, 9L, "审批员");

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<StocktakingEntity> captor = ArgumentCaptor.forClass(StocktakingEntity.class);
        verify(stocktakingRepository).save(captor.capture());
        StocktakingEntity saved = captor.getValue();
        assertEquals("APPROVED", saved.getStatus());
        assertEquals(9L, saved.getApproverId());
        assertEquals("审批员", saved.getApproverName());
        assertNotNull(saved.getApprovedAt());
    }

    @Test
    void approveShouldReturnConflictOnOptimisticLock() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("PENDING_APPROVAL");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(stocktakingRepository.save(any(StocktakingEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<StocktakingResponse> result = service.approve(1L, 9L, "审批员");

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
    }

    // ==================== reject ====================

    @Test
    void rejectShouldFailWhenNotFound() {
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.empty());

        Result<StocktakingResponse> result = service.reject(1L, 9L, "审批员", "数量不符");

        assertEquals(InventoryErrorCode.STOCKTAKING_NOT_FOUND.getCode(), result.getCode());
        verify(stocktakingRepository, never()).save(any());
    }

    @Test
    void rejectShouldFailWhenNotPendingApproval() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setStatus("APPROVED");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<StocktakingResponse> result = service.reject(1L, 9L, "审批员", "数量不符");

        assertEquals(InventoryErrorCode.STOCKTAKING_INVALID_STATE.getCode(), result.getCode());
        verify(stocktakingRepository, never()).save(any());
    }

    @Test
    void rejectShouldTransitionToRejectedAndRecordReason() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("PENDING_APPROVAL");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(Collections.emptyList());
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.reject(1L, 9L, "审批员", "数量不符");

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<StocktakingEntity> captor = ArgumentCaptor.forClass(StocktakingEntity.class);
        verify(stocktakingRepository).save(captor.capture());
        StocktakingEntity saved = captor.getValue();
        assertEquals("REJECTED", saved.getStatus());
        assertEquals(9L, saved.getApproverId());
        assertEquals("审批员", saved.getApproverName());
        assertNotNull(saved.getApprovedAt());
        assertEquals("数量不符", saved.getRejectReason());
    }

    @Test
    void rejectShouldReturnConflictOnOptimisticLock() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("PENDING_APPROVAL");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(stocktakingRepository.save(any(StocktakingEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<StocktakingResponse> result = service.reject(1L, 9L, "审批员", "数量不符");

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
    }

    // ==================== complete ====================

    @Test
    void completeShouldFailWhenNotFound() {
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.empty());

        Result<StocktakingResponse> result = service.complete(1L);

        assertEquals(InventoryErrorCode.STOCKTAKING_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    void completeShouldFailWhenNotApproved() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setStatus("IN_PROGRESS");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<StocktakingResponse> result = service.complete(1L);

        assertEquals(InventoryErrorCode.STOCKTAKING_INVALID_STATE.getCode(), result.getCode());
    }

    @Test
    void completeShouldComputeSurplusAndAdjustStock() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity item = new StocktakingItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B01");
        item.setBookQuantity(new BigDecimal("100"));
        item.setActualQuantity(new BigDecimal("120"));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(item));

        InventoryStockEntity stock = new InventoryStockEntity();
        stock.setQuantity(new BigDecimal("100"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B01"))
                .thenReturn(Optional.of(stock));
        when(stockRepository.save(any(InventoryStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.complete(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals("SURPLUS", item.getDifferenceType());
        assertEquals(new BigDecimal("20"), item.getDifference());
        assertEquals(new BigDecimal("120"), stock.getQuantity());

        ArgumentCaptor<StocktakingEntity> captor = ArgumentCaptor.forClass(StocktakingEntity.class);
        verify(stocktakingRepository).save(captor.capture());
        assertEquals("COMPLETED", captor.getValue().getStatus());
        assertEquals(1, captor.getValue().getSurplusItems());
        assertEquals(0, captor.getValue().getLossItems());
        assertNotNull(captor.getValue().getEndTime());
    }

    @Test
    void completeShouldComputeLossAndAdjustStock() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity item = new StocktakingItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B01");
        item.setBookQuantity(new BigDecimal("100"));
        item.setActualQuantity(new BigDecimal("80"));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(item));

        InventoryStockEntity stock = new InventoryStockEntity();
        stock.setQuantity(new BigDecimal("100"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B01"))
                .thenReturn(Optional.of(stock));
        when(stockRepository.save(any(InventoryStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.complete(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals("LOSS", item.getDifferenceType());
        assertEquals(new BigDecimal("-20"), item.getDifference());
        assertEquals(new BigDecimal("80"), stock.getQuantity());

        ArgumentCaptor<StocktakingEntity> captor = ArgumentCaptor.forClass(StocktakingEntity.class);
        verify(stocktakingRepository).save(captor.capture());
        assertEquals(0, captor.getValue().getSurplusItems());
        assertEquals(1, captor.getValue().getLossItems());
    }

    @Test
    void completeShouldClampNegativeStockToZero() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity item = new StocktakingItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B01");
        item.setBookQuantity(new BigDecimal("100"));
        item.setActualQuantity(BigDecimal.ZERO);
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(item));

        InventoryStockEntity stock = new InventoryStockEntity();
        stock.setQuantity(new BigDecimal("50"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B01"))
                .thenReturn(Optional.of(stock));
        when(stockRepository.save(any(InventoryStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        service.complete(1L);

        // 50 + (-100) = -50 < 0 → clamp to 0
        assertEquals(BigDecimal.ZERO, stock.getQuantity());
    }

    @Test
    void completeShouldSetNoneWhenNoDifference() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity item = new StocktakingItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B01");
        item.setBookQuantity(new BigDecimal("100"));
        item.setActualQuantity(new BigDecimal("100"));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(item));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.complete(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals("NONE", item.getDifferenceType());
        assertEquals(BigDecimal.ZERO, item.getDifference());

        ArgumentCaptor<StocktakingEntity> captor = ArgumentCaptor.forClass(StocktakingEntity.class);
        verify(stocktakingRepository).save(captor.capture());
        assertEquals(0, captor.getValue().getSurplusItems());
        assertEquals(0, captor.getValue().getLossItems());
        // 差异为 0 时不应回写库存
        verify(stockRepository, never()).save(any());
    }

    @Test
    void completeShouldUseBookWhenActualIsNull() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity item = new StocktakingItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B01");
        item.setBookQuantity(new BigDecimal("100"));
        item.setActualQuantity(null);
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(item));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.complete(1L);

        assertEquals("SUCCESS", result.getCode());
        // actual=null → 用 book，差异=0 → NONE
        assertEquals("NONE", item.getDifferenceType());
        assertEquals(BigDecimal.ZERO, item.getDifference());
    }

    @Test
    void completeShouldHandleNullBookQuantity() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity item = new StocktakingItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B01");
        item.setBookQuantity(null);
        item.setActualQuantity(new BigDecimal("50"));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(item));

        InventoryStockEntity stock = new InventoryStockEntity();
        stock.setQuantity(new BigDecimal("100"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B01"))
                .thenReturn(Optional.of(stock));
        when(stockRepository.save(any(InventoryStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        service.complete(1L);

        // book=null → 0，actual=50，差异=50 → SURPLUS
        assertEquals("SURPLUS", item.getDifferenceType());
        assertEquals(new BigDecimal("50"), item.getDifference());
    }

    @Test
    void completeShouldSkipStockAdjustWhenBatchNoBlank() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity item = new StocktakingItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("  ");
        item.setBookQuantity(new BigDecimal("100"));
        item.setActualQuantity(new BigDecimal("120"));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(item));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.complete(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals("SURPLUS", item.getDifferenceType());
        // batchNo 为空时不应回写库存
        verify(stockRepository, never()).save(any());
        verify(stockRepository, never()).findByDrugCodeAndBatchNo(any(), any());
    }

    @Test
    void completeShouldSkipStockAdjustWhenStockNotFound() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity item = new StocktakingItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B01");
        item.setBookQuantity(new BigDecimal("100"));
        item.setActualQuantity(new BigDecimal("120"));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(item));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B01"))
                .thenReturn(Optional.empty());
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.complete(1L);

        assertEquals("SUCCESS", result.getCode());
        // 库存批次不存在时不应保存
        verify(stockRepository, never()).save(any());
    }

    @Test
    void completeShouldReturnConflictOnOptimisticLock() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("APPROVED");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        StocktakingItemEntity item = new StocktakingItemEntity();
        item.setDrugCode("DRG001");
        item.setBatchNo("B01");
        item.setBookQuantity(new BigDecimal("100"));
        item.setActualQuantity(new BigDecimal("100"));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(item));
        when(stocktakingRepository.save(any(StocktakingEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<StocktakingResponse> result = service.complete(1L);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
    }

    // ==================== cancel ====================

    @Test
    void cancelShouldFailWhenNotFound() {
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.empty());

        Result<StocktakingResponse> result = service.cancel(1L);

        assertEquals(InventoryErrorCode.STOCKTAKING_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    void cancelShouldFailWhenCompleted() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setStatus("COMPLETED");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<StocktakingResponse> result = service.cancel(1L);

        assertEquals(InventoryErrorCode.STOCKTAKING_INVALID_STATE.getCode(), result.getCode());
        verify(stocktakingRepository, never()).save(any());
    }

    @Test
    void cancelShouldFailWhenAlreadyCancelled() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setStatus("CANCELLED");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<StocktakingResponse> result = service.cancel(1L);

        assertEquals(InventoryErrorCode.STOCKTAKING_INVALID_STATE.getCode(), result.getCode());
    }

    @Test
    void cancelShouldTransitionFromDraft() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("DRAFT");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(Collections.emptyList());
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.cancel(1L);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<StocktakingEntity> captor = ArgumentCaptor.forClass(StocktakingEntity.class);
        verify(stocktakingRepository).save(captor.capture());
        assertEquals("CANCELLED", captor.getValue().getStatus());
    }

    @Test
    void cancelShouldTransitionFromInProgress() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("IN_PROGRESS");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(stocktakingRepository.save(any(StocktakingEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(Collections.emptyList());
        when(converter.toStocktakingResponse(any(StocktakingEntity.class), any()))
                .thenReturn(new StocktakingResponse());

        Result<StocktakingResponse> result = service.cancel(1L);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<StocktakingEntity> captor = ArgumentCaptor.forClass(StocktakingEntity.class);
        verify(stocktakingRepository).save(captor.capture());
        assertEquals("CANCELLED", captor.getValue().getStatus());
    }

    @Test
    void cancelShouldReturnConflictOnOptimisticLock() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStatus("DRAFT");
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(stocktakingRepository.save(any(StocktakingEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<StocktakingResponse> result = service.cancel(1L);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
    }

    // ==================== getById ====================

    @Test
    void getByIdShouldFailWhenNotFound() {
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.empty());

        Result<StocktakingResponse> result = service.getById(1L);

        assertEquals(InventoryErrorCode.STOCKTAKING_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    void getByIdShouldReturnResponseWithItems() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        when(stocktakingRepository.findById(1L)).thenReturn(Optional.of(entity));
        StocktakingItemEntity item = new StocktakingItemEntity();
        item.setId(11L);
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(List.of(item));
        StocktakingResponse expected = new StocktakingResponse();
        expected.setId(1L);
        when(converter.toStocktakingResponse(entity, List.of(item))).thenReturn(expected);

        Result<StocktakingResponse> result = service.getById(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1L, result.getData().getId());
    }

    // ==================== query ====================

    @Test
    void queryShouldFilterByStatusWhenProvided() {
        StocktakingQueryRequest req = new StocktakingQueryRequest();
        req.setStatus("DRAFT");
        when(stocktakingRepository.findByStatus(eq("DRAFT"), any(Pageable.class)))
                .thenReturn(emptyPage());

        Result<Page<StocktakingResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(stocktakingRepository).findByStatus(eq("DRAFT"), any(Pageable.class));
    }

    @Test
    void queryShouldFindAllWhenStatusBlank() {
        StocktakingQueryRequest req = new StocktakingQueryRequest();
        req.setStatus("  ");
        when(stocktakingRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        Result<Page<StocktakingResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(stocktakingRepository).findAll(any(Pageable.class));
    }

    @Test
    void queryShouldMapEntitiesWithItems() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        Page<StocktakingEntity> page = new PageImpl<>(List.of(entity));
        when(stocktakingRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(stocktakingItemRepository.findByStocktakingId(1L)).thenReturn(Collections.emptyList());
        StocktakingResponse resp = new StocktakingResponse();
        resp.setId(1L);
        when(converter.toStocktakingResponse(entity, Collections.emptyList())).thenReturn(resp);

        Result<Page<StocktakingResponse>> result = service.query(new StocktakingQueryRequest());

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData().getContent().size());
        assertEquals(1L, result.getData().getContent().get(0).getId());
    }

    @Test
    void queryShouldUseDefaultPageAndSizeWhenNull() {
        StocktakingQueryRequest req = new StocktakingQueryRequest();
        req.setPage(null);
        req.setSize(null);
        when(stocktakingRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        service.query(req);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(stocktakingRepository).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
    }

    // ==================== helpers ====================

    private Page<StocktakingEntity> emptyPage() {
        return new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 20), 0);
    }
}
