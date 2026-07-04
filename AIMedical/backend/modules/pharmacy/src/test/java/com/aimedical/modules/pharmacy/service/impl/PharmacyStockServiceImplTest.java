package com.aimedical.modules.pharmacy.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.PageResponse;
import com.aimedical.common.result.Result;
import com.aimedical.modules.pharmacy.PharmacyErrorCode;
import com.aimedical.modules.pharmacy.converter.PharmacyConverter;
import com.aimedical.modules.pharmacy.dto.PharmacyStockQueryRequest;
import com.aimedical.modules.pharmacy.dto.PharmacyStockResponse;
import com.aimedical.modules.pharmacy.entity.PharmacyStockEntity;
import com.aimedical.modules.pharmacy.repository.PharmacyStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PharmacyStockServiceImplTest {

    @Mock private PharmacyStockRepository stockRepository;
    @Mock private PharmacyConverter converter;

    private PharmacyStockServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PharmacyStockServiceImpl(stockRepository, converter);
    }

    // ==================== query ====================

    @Test
    void queryShouldUseBothCodeAndNameWhenBothProvided() {
        PharmacyStockQueryRequest req = new PharmacyStockQueryRequest();
        req.setDrugCode("DRG");
        req.setDrugName("阿莫");

        PharmacyStockEntity entity = buildStock(1L, "DRG001", "阿莫西林");
        Page<PharmacyStockEntity> page = new PageImpl<>(List.of(entity));
        when(stockRepository.findByDrugCodeContainingAndDrugNameContaining(eq("DRG"), eq("阿莫"), any(Pageable.class)))
                .thenReturn(page);
        PharmacyStockResponse resp = new PharmacyStockResponse();
        resp.setId(1L);
        when(converter.toStockResponseList(page.getContent())).thenReturn(List.of(resp));

        Result<PageResponse<PharmacyStockResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getContent().size());
        assertEquals(1L, result.getData().getContent().get(0).getId());
        assertEquals(1, result.getData().getTotalElements());
    }

    @Test
    void queryShouldUseCodeOnlyWhenOnlyCodeProvided() {
        PharmacyStockQueryRequest req = new PharmacyStockQueryRequest();
        req.setDrugCode("DRG");

        Page<PharmacyStockEntity> page = new PageImpl<>(Collections.emptyList());
        when(stockRepository.findByDrugCodeContaining(eq("DRG"), any(Pageable.class))).thenReturn(page);
        when(converter.toStockResponseList(any())).thenReturn(Collections.emptyList());

        Result<PageResponse<PharmacyStockResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().getContent().isEmpty());
    }

    @Test
    void queryShouldUseNameOnlyWhenOnlyNameProvided() {
        PharmacyStockQueryRequest req = new PharmacyStockQueryRequest();
        req.setDrugName("阿莫");

        Page<PharmacyStockEntity> page = new PageImpl<>(Collections.emptyList());
        when(stockRepository.findByDrugNameContaining(eq("阿莫"), any(Pageable.class))).thenReturn(page);
        when(converter.toStockResponseList(any())).thenReturn(Collections.emptyList());

        Result<PageResponse<PharmacyStockResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().getContent().isEmpty());
    }

    @Test
    void queryShouldFindAllWhenNeitherCodeNorNameProvided() {
        PharmacyStockQueryRequest req = new PharmacyStockQueryRequest();

        Page<PharmacyStockEntity> page = new PageImpl<>(Collections.emptyList());
        when(stockRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(converter.toStockResponseList(any())).thenReturn(Collections.emptyList());

        Result<PageResponse<PharmacyStockResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().getContent().isEmpty());
    }

    @Test
    void queryShouldTrimBlankCodeAndName() {
        // 空白字符串应被视为未提供，走 findAll
        PharmacyStockQueryRequest req = new PharmacyStockQueryRequest();
        req.setDrugCode("   ");
        req.setDrugName("   ");

        Page<PharmacyStockEntity> page = new PageImpl<>(Collections.emptyList());
        when(stockRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(converter.toStockResponseList(any())).thenReturn(Collections.emptyList());

        Result<PageResponse<PharmacyStockResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
    }

    // ==================== getByDrugCode ====================

    @Test
    void getByDrugCodeShouldFailWhenCodeNull() {
        Result<PharmacyStockResponse> result = service.getByDrugCode(null);
        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByDrugCodeShouldFailWhenCodeBlank() {
        Result<PharmacyStockResponse> result = service.getByDrugCode("   ");
        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByDrugCodeShouldFailWhenNoStock() {
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(Collections.emptyList());

        Result<PharmacyStockResponse> result = service.getByDrugCode("DRG001");

        assertEquals(PharmacyErrorCode.STOCK_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByDrugCodeShouldReturnFirstBatch() {
        PharmacyStockEntity first = buildStock(1L, "DRG001", "阿莫西林");
        first.setBatchNo("B1");
        PharmacyStockEntity second = buildStock(2L, "DRG001", "阿莫西林");
        second.setBatchNo("B2");
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(List.of(first, second));
        PharmacyStockResponse resp = new PharmacyStockResponse();
        resp.setId(1L);
        when(converter.toResponse(first)).thenReturn(resp);

        Result<PharmacyStockResponse> result = service.getByDrugCode("DRG001");

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
    }

    @Test
    void getByDrugCodeShouldTrimCode() {
        PharmacyStockEntity entity = buildStock(1L, "DRG001", "阿莫西林");
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(List.of(entity));
        PharmacyStockResponse resp = new PharmacyStockResponse();
        when(converter.toResponse(entity)).thenReturn(resp);

        Result<PharmacyStockResponse> result = service.getByDrugCode("  DRG001  ");

        assertEquals("SUCCESS", result.getCode());
        verify(stockRepository).findByDrugCode("DRG001");
    }

    // ==================== adjustStock ====================

    @Test
    void adjustStockShouldFailWhenDrugCodeNull() {
        Result<PharmacyStockResponse> result = service.adjustStock(null, "B1", new BigDecimal("5"), "r");
        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void adjustStockShouldFailWhenDrugCodeBlank() {
        Result<PharmacyStockResponse> result = service.adjustStock("  ", "B1", new BigDecimal("5"), "r");
        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void adjustStockShouldFailWhenQuantityNull() {
        Result<PharmacyStockResponse> result = service.adjustStock("DRG001", "B1", null, "r");
        assertEquals(PharmacyErrorCode.STOCK_ADJUST_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void adjustStockShouldFailWhenQuantityZero() {
        Result<PharmacyStockResponse> result = service.adjustStock("DRG001", "B1", BigDecimal.ZERO, "r");
        assertEquals(PharmacyErrorCode.STOCK_ADJUST_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void adjustStockShouldFailWhenBatchNoNull() {
        Result<PharmacyStockResponse> result = service.adjustStock("DRG001", null, new BigDecimal("5"), "r");
        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void adjustStockShouldFailWhenBatchNoBlank() {
        Result<PharmacyStockResponse> result = service.adjustStock("DRG001", "  ", new BigDecimal("5"), "r");
        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void adjustStockShouldFailWhenBatchNotFound() {
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.empty());

        Result<PharmacyStockResponse> result = service.adjustStock("DRG001", "B1", new BigDecimal("5"), "r");

        assertEquals(PharmacyErrorCode.STOCK_BATCH_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void adjustStockShouldFailWhenAdjustmentResultsInNegative() {
        PharmacyStockEntity entity = buildStock(1L, "DRG001", "阿莫西林");
        entity.setQuantity(new BigDecimal("5"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(entity));

        Result<PharmacyStockResponse> result = service.adjustStock("DRG001", "B1", new BigDecimal("-10"), "r");

        assertEquals(PharmacyErrorCode.STOCK_INSUFFICIENT.getCode(), result.getCode());
        assertNull(result.getData());
        verify(stockRepository, never()).save(any());
    }

    @Test
    void adjustStockShouldIncreaseAndSaveWithRemark() {
        PharmacyStockEntity entity = buildStock(1L, "DRG001", "阿莫西林");
        entity.setQuantity(new BigDecimal("10"));
        entity.setBatchNo("B1");
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(entity));
        when(stockRepository.save(entity)).thenReturn(entity);
        PharmacyStockResponse resp = new PharmacyStockResponse();
        resp.setId(1L);
        when(converter.toResponse(entity)).thenReturn(resp);

        Result<PharmacyStockResponse> result = service.adjustStock("DRG001", "B1", new BigDecimal("5"), "人工增加");

        assertEquals("SUCCESS", result.getCode());
        assertEquals(new BigDecimal("15"), entity.getQuantity());
        assertEquals("人工增加", entity.getRemark());

        ArgumentCaptor<PharmacyStockEntity> captor = ArgumentCaptor.forClass(PharmacyStockEntity.class);
        verify(stockRepository).save(captor.capture());
        assertEquals(new BigDecimal("15"), captor.getValue().getQuantity());
    }

    @Test
    void adjustStockShouldDecreaseAndSaveWithoutRemarkWhenBlank() {
        PharmacyStockEntity entity = buildStock(1L, "DRG001", "阿莫西林");
        entity.setQuantity(new BigDecimal("10"));
        entity.setRemark("原备注");
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(entity));
        when(stockRepository.save(entity)).thenReturn(entity);
        when(converter.toResponse(entity)).thenReturn(new PharmacyStockResponse());

        Result<PharmacyStockResponse> result = service.adjustStock("DRG001", "B1", new BigDecimal("-3"), "  ");

        assertEquals("SUCCESS", result.getCode());
        assertEquals(new BigDecimal("7"), entity.getQuantity());
        // 空白备注不覆盖原备注
        assertEquals("原备注", entity.getRemark());
    }

    @Test
    void adjustStockShouldReturnConflictOnOptimisticLock() {
        PharmacyStockEntity entity = buildStock(1L, "DRG001", "阿莫西林");
        entity.setQuantity(new BigDecimal("10"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(entity));
        when(stockRepository.save(entity)).thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<PharmacyStockResponse> result = service.adjustStock("DRG001", "B1", new BigDecimal("5"), "r");

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void adjustStockShouldTrimCodeAndBatchNo() {
        PharmacyStockEntity entity = buildStock(1L, "DRG001", "阿莫西林");
        entity.setQuantity(new BigDecimal("10"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(entity));
        when(stockRepository.save(entity)).thenReturn(entity);
        when(converter.toResponse(entity)).thenReturn(new PharmacyStockResponse());

        Result<PharmacyStockResponse> result = service.adjustStock("  DRG001  ", "  B1  ", new BigDecimal("5"), "r");

        assertEquals("SUCCESS", result.getCode());
        verify(stockRepository).findByDrugCodeAndBatchNo("DRG001", "B1");
    }

    // ==================== listLowStock ====================

    @Test
    void listLowStockShouldReturnResponses() {
        PharmacyStockEntity entity = buildStock(1L, "DRG001", "阿莫西林");
        when(stockRepository.findLowStock()).thenReturn(List.of(entity));
        PharmacyStockResponse resp = new PharmacyStockResponse();
        resp.setId(1L);
        when(converter.toStockResponseList(List.of(entity))).thenReturn(List.of(resp));

        Result<List<PharmacyStockResponse>> result = service.listLowStock();

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getData().get(0).getId());
    }

    @Test
    void listLowStockShouldReturnEmptyWhenNone() {
        when(stockRepository.findLowStock()).thenReturn(Collections.emptyList());
        when(converter.toStockResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        Result<List<PharmacyStockResponse>> result = service.listLowStock();

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    // ==================== helpers ====================

    private PharmacyStockEntity buildStock(Long id, String drugCode, String drugName) {
        PharmacyStockEntity entity = new PharmacyStockEntity();
        entity.setId(id);
        entity.setDrugCode(drugCode);
        entity.setDrugName(drugName);
        entity.setQuantity(BigDecimal.ZERO);
        return entity;
    }
}
