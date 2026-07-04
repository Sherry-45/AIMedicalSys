package com.aimedical.modules.inventory.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.InventoryErrorCode;
import com.aimedical.modules.inventory.converter.InventoryConverter;
import com.aimedical.modules.inventory.dto.request.InventoryStockQueryRequest;
import com.aimedical.modules.inventory.dto.request.StockAdjustRequest;
import com.aimedical.modules.inventory.dto.response.InventoryStockResponse;
import com.aimedical.modules.inventory.entity.InventoryStockEntity;
import com.aimedical.modules.inventory.repository.InventoryStockRepository;
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
import java.time.LocalDate;
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
 * {@link InventoryStockServiceImpl} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class InventoryStockServiceImplTest {

    @Mock private InventoryStockRepository stockRepository;
    @Mock private InventoryConverter converter;

    private InventoryStockServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InventoryStockServiceImpl(stockRepository, converter);
    }

    // ==================== query ====================

    @Test
    void queryShouldUseDrugCodeAndBatchNoWhenBothProvided() {
        InventoryStockQueryRequest req = new InventoryStockQueryRequest();
        req.setDrugCode("DRG001");
        req.setBatchNo("BATCH01");
        when(stockRepository.findByDrugCodeAndBatchNo(
                eq("DRG001"), eq("BATCH01"), any(Pageable.class)))
                .thenReturn(emptyPage());

        Result<Page<InventoryStockResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(stockRepository).findByDrugCodeAndBatchNo(
                eq("DRG001"), eq("BATCH01"), any(Pageable.class));
    }

    @Test
    void queryShouldUseDrugCodeOnlyWhenDrugCodeProvided() {
        InventoryStockQueryRequest req = new InventoryStockQueryRequest();
        req.setDrugCode("DRG001");
        when(stockRepository.findByDrugCode(eq("DRG001"), any(Pageable.class)))
                .thenReturn(emptyPage());

        Result<Page<InventoryStockResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(stockRepository).findByDrugCode(eq("DRG001"), any(Pageable.class));
    }

    @Test
    void queryShouldFindAllWhenNoDrugCode() {
        InventoryStockQueryRequest req = new InventoryStockQueryRequest();
        when(stockRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        Result<Page<InventoryStockResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(stockRepository).findAll(any(Pageable.class));
    }

    @Test
    void queryShouldUseDefaultPageAndSizeWhenNull() {
        InventoryStockQueryRequest req = new InventoryStockQueryRequest();
        req.setPage(null);
        req.setSize(null);
        when(stockRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        service.query(req);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(stockRepository).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
    }

    @Test
    void queryShouldHandleBlankDrugCodeAsAbsent() {
        InventoryStockQueryRequest req = new InventoryStockQueryRequest();
        req.setDrugCode("  ");
        when(stockRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        Result<Page<InventoryStockResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(stockRepository).findAll(any(Pageable.class));
    }

    @Test
    void queryShouldMapEntitiesToResponses() {
        InventoryStockEntity entity = new InventoryStockEntity();
        entity.setId(1L);
        Page<InventoryStockEntity> page = new PageImpl<>(List.of(entity));
        when(stockRepository.findAll(any(Pageable.class))).thenReturn(page);
        InventoryStockResponse resp = new InventoryStockResponse();
        resp.setId(1L);
        when(converter.toStockResponse(entity)).thenReturn(resp);

        Result<Page<InventoryStockResponse>> result = service.query(new InventoryStockQueryRequest());

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData().getContent().size());
        assertEquals(1L, result.getData().getContent().get(0).getId());
    }

    // ==================== getByDrugCode (list) ====================

    @Test
    void getByDrugCodeShouldReturnMappedList() {
        InventoryStockEntity e1 = new InventoryStockEntity();
        e1.setId(1L);
        InventoryStockEntity e2 = new InventoryStockEntity();
        e2.setId(2L);
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(List.of(e1, e2));
        InventoryStockResponse r1 = new InventoryStockResponse();
        r1.setId(1L);
        InventoryStockResponse r2 = new InventoryStockResponse();
        r2.setId(2L);
        when(converter.toStockResponse(e1)).thenReturn(r1);
        when(converter.toStockResponse(e2)).thenReturn(r2);

        Result<List<InventoryStockResponse>> result = service.getByDrugCode("DRG001");

        assertEquals("SUCCESS", result.getCode());
        assertEquals(2, result.getData().size());
        assertEquals(1L, result.getData().get(0).getId());
        assertEquals(2L, result.getData().get(1).getId());
    }

    @Test
    void getByDrugCodeShouldReturnEmptyListWhenNoStock() {
        when(stockRepository.findByDrugCode("DRG999")).thenReturn(Collections.emptyList());

        Result<List<InventoryStockResponse>> result = service.getByDrugCode("DRG999");

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    // ==================== adjustStock ====================

    @Test
    void adjustStockShouldFailWhenBatchNotFound() {
        StockAdjustRequest req = buildAdjustRequest("DRG001", "BATCH01", new BigDecimal("10"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "BATCH01"))
                .thenReturn(Optional.empty());

        Result<InventoryStockResponse> result = service.adjustStock(req);

        assertEquals(InventoryErrorCode.STOCK_BATCH_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
        verify(stockRepository, never()).save(any());
    }

    @Test
    void adjustStockShouldFailWhenInsufficientStock() {
        StockAdjustRequest req = buildAdjustRequest("DRG001", "BATCH01", new BigDecimal("-100"));
        InventoryStockEntity entity = new InventoryStockEntity();
        entity.setQuantity(new BigDecimal("50"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "BATCH01"))
                .thenReturn(Optional.of(entity));

        Result<InventoryStockResponse> result = service.adjustStock(req);

        assertEquals(InventoryErrorCode.STOCK_INSUFFICIENT.getCode(), result.getCode());
        assertNull(result.getData());
        verify(stockRepository, never()).save(any());
    }

    @Test
    void adjustStockShouldSucceedAndNotUpdateRemarkWhenBlank() {
        StockAdjustRequest req = buildAdjustRequest("DRG001", "BATCH01", new BigDecimal("10"));
        req.setRemark("  ");
        InventoryStockEntity entity = new InventoryStockEntity();
        entity.setId(1L);
        entity.setQuantity(new BigDecimal("50"));
        entity.setRemark("原备注");
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "BATCH01"))
                .thenReturn(Optional.of(entity));
        when(stockRepository.save(any(InventoryStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        InventoryStockResponse expected = new InventoryStockResponse();
        when(converter.toStockResponse(any(InventoryStockEntity.class))).thenReturn(expected);

        Result<InventoryStockResponse> result = service.adjustStock(req);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<InventoryStockEntity> captor = ArgumentCaptor.forClass(InventoryStockEntity.class);
        verify(stockRepository).save(captor.capture());
        assertEquals(new BigDecimal("60"), captor.getValue().getQuantity());
        // 空白备注不应覆盖原备注
        assertEquals("原备注", captor.getValue().getRemark());
    }

    @Test
    void adjustStockShouldSucceedAndUpdateRemarkWhenProvided() {
        StockAdjustRequest req = buildAdjustRequest("DRG001", "BATCH01", new BigDecimal("-10"));
        req.setRemark("出库");
        InventoryStockEntity entity = new InventoryStockEntity();
        entity.setId(1L);
        entity.setQuantity(new BigDecimal("50"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "BATCH01"))
                .thenReturn(Optional.of(entity));
        when(stockRepository.save(any(InventoryStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toStockResponse(any(InventoryStockEntity.class))).thenReturn(new InventoryStockResponse());

        Result<InventoryStockResponse> result = service.adjustStock(req);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<InventoryStockEntity> captor = ArgumentCaptor.forClass(InventoryStockEntity.class);
        verify(stockRepository).save(captor.capture());
        assertEquals(new BigDecimal("40"), captor.getValue().getQuantity());
        assertEquals("出库", captor.getValue().getRemark());
    }

    @Test
    void adjustStockShouldAllowZeroQuantity() {
        StockAdjustRequest req = buildAdjustRequest("DRG001", "BATCH01", new BigDecimal("-50"));
        InventoryStockEntity entity = new InventoryStockEntity();
        entity.setQuantity(new BigDecimal("50"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "BATCH01"))
                .thenReturn(Optional.of(entity));
        when(stockRepository.save(any(InventoryStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toStockResponse(any(InventoryStockEntity.class))).thenReturn(new InventoryStockResponse());

        Result<InventoryStockResponse> result = service.adjustStock(req);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<InventoryStockEntity> captor = ArgumentCaptor.forClass(InventoryStockEntity.class);
        verify(stockRepository).save(captor.capture());
        assertEquals(BigDecimal.ZERO, captor.getValue().getQuantity());
    }

    @Test
    void adjustStockShouldReturnConflictOnOptimisticLock() {
        StockAdjustRequest req = buildAdjustRequest("DRG001", "BATCH01", new BigDecimal("10"));
        InventoryStockEntity entity = new InventoryStockEntity();
        entity.setQuantity(new BigDecimal("50"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "BATCH01"))
                .thenReturn(Optional.of(entity));
        when(stockRepository.save(any(InventoryStockEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<InventoryStockResponse> result = service.adjustStock(req);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    // ==================== listExpiringSoon ====================

    @Test
    void listExpiringSoonShouldReturnMappedList() {
        InventoryStockEntity e1 = new InventoryStockEntity();
        e1.setId(1L);
        InventoryStockEntity e2 = new InventoryStockEntity();
        e2.setId(2L);
        when(stockRepository.findByExpiryDateBefore(any(LocalDate.class))).thenReturn(List.of(e1, e2));
        when(converter.toStockResponse(e1)).thenReturn(new InventoryStockResponse());
        when(converter.toStockResponse(e2)).thenReturn(new InventoryStockResponse());

        Result<List<InventoryStockResponse>> result = service.listExpiringSoon(30);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(2, result.getData().size());
        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(stockRepository).findByExpiryDateBefore(captor.capture());
        assertEquals(LocalDate.now().plusDays(30), captor.getValue());
    }

    @Test
    void listExpiringSoonShouldReturnEmptyWhenNone() {
        when(stockRepository.findByExpiryDateBefore(any(LocalDate.class))).thenReturn(Collections.emptyList());

        Result<List<InventoryStockResponse>> result = service.listExpiringSoon(60);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    // ==================== listLowStock ====================

    @Test
    void listLowStockShouldFilterQuantityAtOrBelowThreshold() {
        InventoryStockEntity low = new InventoryStockEntity();
        low.setId(1L);
        low.setQuantity(new BigDecimal("5"));
        InventoryStockEntity zero = new InventoryStockEntity();
        zero.setId(2L);
        zero.setQuantity(BigDecimal.ZERO);
        InventoryStockEntity atThreshold = new InventoryStockEntity();
        atThreshold.setId(3L);
        atThreshold.setQuantity(new BigDecimal("10"));
        InventoryStockEntity high = new InventoryStockEntity();
        high.setId(4L);
        high.setQuantity(new BigDecimal("100"));
        when(stockRepository.findAll()).thenReturn(List.of(low, zero, atThreshold, high));
        when(converter.toStockResponse(any(InventoryStockEntity.class)))
                .thenAnswer(inv -> {
                    InventoryStockEntity e = inv.getArgument(0);
                    InventoryStockResponse r = new InventoryStockResponse();
                    r.setId(e.getId());
                    return r;
                });

        Result<List<InventoryStockResponse>> result = service.listLowStock();

        assertEquals("SUCCESS", result.getCode());
        assertEquals(3, result.getData().size());
        List<Long> ids = result.getData().stream().map(InventoryStockResponse::getId).toList();
        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(2L));
        assertTrue(ids.contains(3L));
        assertFalse(ids.contains(4L));
    }

    @Test
    void listLowStockShouldSkipNullQuantity() {
        InventoryStockEntity nullQty = new InventoryStockEntity();
        nullQty.setId(1L);
        nullQty.setQuantity(null);
        InventoryStockEntity low = new InventoryStockEntity();
        low.setId(2L);
        low.setQuantity(new BigDecimal("3"));
        when(stockRepository.findAll()).thenReturn(List.of(nullQty, low));
        when(converter.toStockResponse(low)).thenReturn(new InventoryStockResponse());

        Result<List<InventoryStockResponse>> result = service.listLowStock();

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void listLowStockShouldReturnEmptyWhenAllHigh() {
        InventoryStockEntity high = new InventoryStockEntity();
        high.setQuantity(new BigDecimal("100"));
        when(stockRepository.findAll()).thenReturn(List.of(high));

        Result<List<InventoryStockResponse>> result = service.listLowStock();

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void listLowStockShouldReturnEmptyWhenRepositoryEmpty() {
        when(stockRepository.findAll()).thenReturn(Collections.emptyList());

        Result<List<InventoryStockResponse>> result = service.listLowStock();

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    // ==================== helpers ====================

    private StockAdjustRequest buildAdjustRequest(String drugCode, String batchNo, BigDecimal quantity) {
        StockAdjustRequest req = new StockAdjustRequest();
        req.setDrugCode(drugCode);
        req.setBatchNo(batchNo);
        req.setQuantity(quantity);
        return req;
    }

    private Page<InventoryStockEntity> emptyPage() {
        return new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 20), 0);
    }
}
