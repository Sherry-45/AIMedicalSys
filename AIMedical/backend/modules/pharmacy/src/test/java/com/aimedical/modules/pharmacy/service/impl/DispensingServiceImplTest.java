package com.aimedical.modules.pharmacy.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.PageResponse;
import com.aimedical.common.result.Result;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispensingServiceImplTest {

    @Mock private DispensingRecordRepository dispensingRepository;
    @Mock private DispensingItemRepository dispensingItemRepository;
    @Mock private PharmacyStockRepository stockRepository;
    @Mock private PharmacyConverter converter;
    @Mock private ApplicationEventPublisher eventPublisher;

    private DispensingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DispensingServiceImpl(dispensingRepository, dispensingItemRepository,
                stockRepository, converter, eventPublisher);
    }

    // ==================== create ====================

    @Test
    void createShouldFailWhenPharmacistIdNull() {
        DispensingCreateRequest req = buildCreateRequest(null);
        req.setItems(List.of(buildItemRequest("DRG001", "B1", new BigDecimal("1"))));

        Result<DispensingResponse> result = service.create(req, null, "药师");

        assertEquals(GlobalErrorCode.UNAUTHORIZED.getCode(), result.getCode());
        assertNull(result.getData());
        verify(dispensingRepository, never()).save(any());
    }

    @Test
    void createShouldFailWhenItemsNull() {
        DispensingCreateRequest req = new DispensingCreateRequest();
        req.setPatientId(1L);
        req.setItems(null);

        Result<DispensingResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.DISPENSING_ITEM_EMPTY.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenItemsEmpty() {
        DispensingCreateRequest req = buildCreateRequest(null);
        req.setItems(Collections.emptyList());

        Result<DispensingResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.DISPENSING_ITEM_EMPTY.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenPrescriptionAlreadyHasDispensing() {
        DispensingCreateRequest req = buildCreateRequest(100L);
        req.setItems(List.of(buildItemRequest("DRG001", "B1", new BigDecimal("1"))));
        when(dispensingRepository.findByPrescriptionId(100L))
                .thenReturn(Optional.of(new DispensingRecordEntity()));

        Result<DispensingResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.DISPENSING_DUPLICATE.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenBatchStockNotFound() {
        DispensingCreateRequest req = buildCreateRequest(null);
        req.setItems(List.of(buildItemRequest("DRG001", "B1", new BigDecimal("1"))));
        // findByPrescriptionId skipped: prescriptionId is null
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.empty());

        Result<DispensingResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.STOCK_BATCH_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenBatchStockInsufficient() {
        DispensingCreateRequest req = buildCreateRequest(null);
        req.setItems(List.of(buildItemRequest("DRG001", "B1", new BigDecimal("10"))));
        // findByPrescriptionId skipped: prescriptionId is null
        PharmacyStockEntity stock = buildStock("DRG001", "B1", new BigDecimal("5"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(stock));

        Result<DispensingResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.STOCK_INSUFFICIENT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenTotalStockInsufficientWithoutBatch() {
        DispensingCreateRequest req = buildCreateRequest(null);
        DispensingItemRequest item = buildItemRequest("DRG001", null, new BigDecimal("10"));
        req.setItems(List.of(item));
        // findByPrescriptionId skipped: prescriptionId is null
        when(stockRepository.sumQuantityByDrugCode("DRG001")).thenReturn(new BigDecimal("5"));

        Result<DispensingResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.STOCK_INSUFFICIENT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenBatchNoBlankFallsBackToSum() {
        // 空白批次号应按汇总校验
        DispensingCreateRequest req = buildCreateRequest(null);
        DispensingItemRequest item = buildItemRequest("DRG001", "  ", new BigDecimal("10"));
        req.setItems(List.of(item));
        // findByPrescriptionId skipped: prescriptionId is null
        when(stockRepository.sumQuantityByDrugCode("DRG001")).thenReturn(new BigDecimal("5"));

        Result<DispensingResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.STOCK_INSUFFICIENT.getCode(), result.getCode());
    }

    @Test
    void createShouldFailOnOptimisticLockWhenSavingRecord() {
        DispensingCreateRequest req = buildCreateRequest(null);
        req.setItems(List.of(buildItemRequest("DRG001", "B1", new BigDecimal("1"))));
        // findByPrescriptionId skipped: prescriptionId is null
        PharmacyStockEntity stock = buildStock("DRG001", "B1", new BigDecimal("5"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(stock));
        when(dispensingRepository.save(any(DispensingRecordEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<DispensingResponse> result = service.create(req, 10L, "药师");

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
        verify(dispensingItemRepository, never()).saveAll(any());
    }

    @Test
    void createShouldSucceedWithBatchAndComputeAmounts() {
        DispensingCreateRequest req = buildCreateRequest(100L);
        req.setRemark("发药备注");
        DispensingItemRequest item = buildItemRequest("DRG001", "B1", new BigDecimal("3"));
        item.setUnitPrice(new BigDecimal("12.50"));
        item.setDrugName("阿莫西林");
        item.setSpecification("0.25g");
        item.setUnit("盒");
        item.setDosage("1片");
        item.setUsageMethod("口服");
        item.setFrequency("tid");
        item.setDays(7);
        req.setItems(List.of(item));
        when(dispensingRepository.findByPrescriptionId(100L)).thenReturn(Optional.empty());
        PharmacyStockEntity stock = buildStock("DRG001", "B1", new BigDecimal("10"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(stock));
        when(dispensingRepository.save(any(DispensingRecordEntity.class))).thenAnswer(inv -> {
            DispensingRecordEntity r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        DispensingResponse expected = new DispensingResponse();
        expected.setId(1L);
        when(converter.toResponse(any(DispensingRecordEntity.class), anyList())).thenReturn(expected);

        Result<DispensingResponse> result = service.create(req, 10L, "李药师");

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());

        ArgumentCaptor<DispensingRecordEntity> recordCaptor = ArgumentCaptor.forClass(DispensingRecordEntity.class);
        verify(dispensingRepository).save(recordCaptor.capture());
        DispensingRecordEntity saved = recordCaptor.getValue();
        assertEquals(DispensingStatus.PENDING.getCode(), saved.getStatus());
        assertEquals(10L, saved.getPharmacistId());
        assertEquals("李药师", saved.getPharmacistName());
        assertEquals(new BigDecimal("3"), saved.getTotalQuantity());
        assertEquals(new BigDecimal("37.50"), saved.getTotalAmount());
        assertNotNull(saved.getDispensingNo());
        assertTrue(saved.getDispensingNo().startsWith("DISP"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DispensingItemEntity>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(dispensingItemRepository).saveAll(itemsCaptor.capture());
        DispensingItemEntity savedItem = itemsCaptor.getValue().get(0);
        assertEquals(1L, savedItem.getDispensingId());
        assertEquals("阿莫西林", savedItem.getDrugName());
        assertEquals(new BigDecimal("37.50"), savedItem.getAmount());
    }

    @Test
    void createShouldHandleNullUnitPriceWithZeroAmount() {
        DispensingCreateRequest req = buildCreateRequest(null);
        DispensingItemRequest item = buildItemRequest("DRG001", null, new BigDecimal("3"));
        item.setUnitPrice(null);
        req.setItems(List.of(item));
        when(stockRepository.sumQuantityByDrugCode("DRG001")).thenReturn(new BigDecimal("10"));
        when(dispensingRepository.save(any(DispensingRecordEntity.class))).thenAnswer(inv -> {
            DispensingRecordEntity r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(converter.toResponse(any(DispensingRecordEntity.class), anyList())).thenReturn(new DispensingResponse());

        Result<DispensingResponse> result = service.create(req, 10L, "李药师");

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<DispensingRecordEntity> recordCaptor = ArgumentCaptor.forClass(DispensingRecordEntity.class);
        verify(dispensingRepository).save(recordCaptor.capture());
        assertEquals(BigDecimal.ZERO, recordCaptor.getValue().getTotalAmount());
    }

    // ==================== dispense ====================

    @Test
    void dispenseShouldFailWhenIdNull() {
        Result<DispensingResponse> result = service.dispense(null, 10L, "药师");

        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void dispenseShouldFailWhenNotFound() {
        when(dispensingRepository.findById(1L)).thenReturn(Optional.empty());

        Result<DispensingResponse> result = service.dispense(1L, 10L, "药师");

        assertEquals(PharmacyErrorCode.DISPENSING_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void dispenseShouldFailWhenNotPending() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));

        Result<DispensingResponse> result = service.dispense(1L, 10L, "药师");

        assertEquals(PharmacyErrorCode.DISPENSING_INVALID_STATE.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void dispenseShouldFailWhenItemsEmpty() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(Collections.emptyList());

        Result<DispensingResponse> result = service.dispense(1L, 10L, "药师");

        assertEquals(PharmacyErrorCode.DISPENSING_ITEM_EMPTY.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void dispenseShouldFailWhenBatchStockNotFoundDuringReduction() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", "B1", new BigDecimal("1"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.empty());

        Result<DispensingResponse> result = service.dispense(1L, 10L, "药师");

        assertEquals(PharmacyErrorCode.STOCK_BATCH_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void dispenseShouldFailWhenBatchStockInsufficientDuringReduction() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", "B1", new BigDecimal("10"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        PharmacyStockEntity stock = buildStock("DRG001", "B1", new BigDecimal("5"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(stock));

        Result<DispensingResponse> result = service.dispense(1L, 10L, "药师");

        assertEquals(PharmacyErrorCode.STOCK_INSUFFICIENT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void dispenseShouldFailWhenNoStockForDrugCodeFifo() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", null, new BigDecimal("1"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(Collections.emptyList());

        Result<DispensingResponse> result = service.dispense(1L, 10L, "药师");

        assertEquals(PharmacyErrorCode.STOCK_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void dispenseShouldFailWhenFifoInsufficient() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", null, new BigDecimal("10"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        PharmacyStockEntity s1 = buildStock("DRG001", "B1", new BigDecimal("3"));
        s1.setCreatedAt(LocalDateTime.of(2024, 1, 1, 9, 0));
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(new ArrayList<>(List.of(s1)));
        when(stockRepository.save(s1)).thenReturn(s1);

        Result<DispensingResponse> result = service.dispense(1L, 10L, "药师");

        assertEquals(PharmacyErrorCode.STOCK_INSUFFICIENT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void dispenseShouldFailOnOptimisticLockWhenReducingStock() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", "B1", new BigDecimal("1"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        PharmacyStockEntity stock = buildStock("DRG001", "B1", new BigDecimal("5"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<DispensingResponse> result = service.dispense(1L, 10L, "药师");

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void dispenseShouldSucceedWithBatchReduction() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", "B1", new BigDecimal("3"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        PharmacyStockEntity stock = buildStock("DRG001", "B1", new BigDecimal("10"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenReturn(stock);
        when(dispensingRepository.save(record)).thenReturn(record);
        DispensingResponse expected = new DispensingResponse();
        expected.setId(1L);
        when(converter.toResponse(record, List.of(item))).thenReturn(expected);

        Result<DispensingResponse> result = service.dispense(1L, 20L, "王药师");

        assertEquals("SUCCESS", result.getCode());
        assertEquals(DispensingStatus.DISPENSED.getCode(), record.getStatus());
        assertNotNull(record.getDispensedAt());
        assertEquals(20L, record.getPharmacistId());
        assertEquals("王药师", record.getPharmacistName());
        assertEquals(new BigDecimal("7"), stock.getQuantity());
    }

    @Test
    void dispenseShouldNotOverridePharmacistWhenNull() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        record.setPharmacistId(99L);
        record.setPharmacistName("原药师");
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", "B1", new BigDecimal("1"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        PharmacyStockEntity stock = buildStock("DRG001", "B1", new BigDecimal("10"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenReturn(stock);
        when(dispensingRepository.save(record)).thenReturn(record);
        when(converter.toResponse(record, List.of(item))).thenReturn(new DispensingResponse());

        Result<DispensingResponse> result = service.dispense(1L, null, null);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(99L, record.getPharmacistId());
        assertEquals("原药师", record.getPharmacistName());
    }

    @Test
    void dispenseShouldSucceedWithFifoAcrossMultipleBatches() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", null, new BigDecimal("7"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        // 较晚批次在前，应按 createdAt 升序优先扣较早批次
        PharmacyStockEntity late = buildStock("DRG001", "B2", new BigDecimal("5"));
        late.setCreatedAt(LocalDateTime.of(2024, 6, 1, 9, 0));
        PharmacyStockEntity early = buildStock("DRG001", "B1", new BigDecimal("5"));
        early.setCreatedAt(LocalDateTime.of(2024, 1, 1, 9, 0));
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(new ArrayList<>(List.of(late, early)));
        when(stockRepository.save(any(PharmacyStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dispensingRepository.save(record)).thenReturn(record);
        when(converter.toResponse(record, List.of(item))).thenReturn(new DispensingResponse());

        Result<DispensingResponse> result = service.dispense(1L, 10L, "药师");

        assertEquals("SUCCESS", result.getCode());
        // early 批次先扣完 5，late 批次扣 2
        assertEquals(BigDecimal.ZERO, early.getQuantity());
        assertEquals(new BigDecimal("3"), late.getQuantity());
    }

    @Test
    void dispenseShouldHandleNullCreatedAtInFifoSort() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", null, new BigDecimal("3"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        PharmacyStockEntity withDate = buildStock("DRG001", "B1", new BigDecimal("2"));
        withDate.setCreatedAt(LocalDateTime.of(2024, 1, 1, 9, 0));
        PharmacyStockEntity noDate = buildStock("DRG001", "B2", new BigDecimal("5"));
        noDate.setCreatedAt(null);
        // withDate 在前（有日期排前），noDate 排后
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(new ArrayList<>(List.of(withDate, noDate)));
        when(stockRepository.save(any(PharmacyStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dispensingRepository.save(record)).thenReturn(record);
        when(converter.toResponse(record, List.of(item))).thenReturn(new DispensingResponse());

        Result<DispensingResponse> result = service.dispense(1L, 10L, "药师");

        assertEquals("SUCCESS", result.getCode());
        // withDate 先扣完 2，noDate 扣 1
        assertEquals(BigDecimal.ZERO, withDate.getQuantity());
        assertEquals(new BigDecimal("4"), noDate.getQuantity());
    }

    @Test
    void dispenseShouldSkipZeroQuantityBatchesInFifo() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", null, new BigDecimal("3"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        PharmacyStockEntity zero = buildStock("DRG001", "B0", BigDecimal.ZERO);
        zero.setCreatedAt(LocalDateTime.of(2024, 1, 1, 9, 0));
        PharmacyStockEntity avail = buildStock("DRG001", "B1", new BigDecimal("5"));
        avail.setCreatedAt(LocalDateTime.of(2024, 2, 1, 9, 0));
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(new ArrayList<>(List.of(zero, avail)));
        when(stockRepository.save(any(PharmacyStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dispensingRepository.save(record)).thenReturn(record);
        when(converter.toResponse(record, List.of(item))).thenReturn(new DispensingResponse());

        Result<DispensingResponse> result = service.dispense(1L, 10L, "药师");

        assertEquals("SUCCESS", result.getCode());
        assertEquals(new BigDecimal("2"), avail.getQuantity());
    }

    @Test
    void dispenseShouldFailOnOptimisticLockWhenSavingRecord() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", "B1", new BigDecimal("1"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        PharmacyStockEntity stock = buildStock("DRG001", "B1", new BigDecimal("5"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenReturn(stock);
        when(dispensingRepository.save(record)).thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<DispensingResponse> result = service.dispense(1L, 10L, "药师");

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    // ==================== cancel ====================

    @Test
    void cancelShouldFailWhenIdNull() {
        Result<DispensingResponse> result = service.cancel(null);
        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void cancelShouldFailWhenNotFound() {
        when(dispensingRepository.findById(1L)).thenReturn(Optional.empty());

        Result<DispensingResponse> result = service.cancel(1L);

        assertEquals(PharmacyErrorCode.DISPENSING_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void cancelShouldFailWhenNotPending() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));

        Result<DispensingResponse> result = service.cancel(1L);

        assertEquals(PharmacyErrorCode.DISPENSING_INVALID_STATE.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void cancelShouldSucceed() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        when(dispensingRepository.save(record)).thenReturn(record);
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", null, new BigDecimal("1"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        DispensingResponse expected = new DispensingResponse();
        expected.setId(1L);
        when(converter.toResponse(record, List.of(item))).thenReturn(expected);

        Result<DispensingResponse> result = service.cancel(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(DispensingStatus.CANCELLED.getCode(), record.getStatus());
        assertNotNull(result.getData());
    }

    @Test
    void cancelShouldFailOnOptimisticLock() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        when(dispensingRepository.save(record)).thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<DispensingResponse> result = service.cancel(1L);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    // ==================== getById ====================

    @Test
    void getByIdShouldFailWhenIdNull() {
        Result<DispensingResponse> result = service.getById(null);
        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByIdShouldFailWhenNotFound() {
        when(dispensingRepository.findById(1L)).thenReturn(Optional.empty());

        Result<DispensingResponse> result = service.getById(1L);

        assertEquals(PharmacyErrorCode.DISPENSING_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByIdShouldReturnResponseWithItems() {
        DispensingRecordEntity record = buildRecord(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(record));
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", null, new BigDecimal("1"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(item));
        DispensingResponse expected = new DispensingResponse();
        expected.setId(1L);
        when(converter.toResponse(record, List.of(item))).thenReturn(expected);

        Result<DispensingResponse> result = service.getById(1L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
    }

    // ==================== query ====================

    @Test
    void queryShouldUsePatientAndStatusWhenBothProvided() {
        DispensingQueryRequest req = new DispensingQueryRequest();
        req.setPatientId(1L);
        req.setStatus("PENDING");

        DispensingRecordEntity record = buildRecord(1L, "PENDING");
        Page<DispensingRecordEntity> page = new PageImpl<>(List.of(record));
        when(dispensingRepository.findByPatientIdAndStatusOrderByCreatedAtDesc(eq(1L), eq("PENDING"), any(Pageable.class)))
                .thenReturn(page);
        DispensingItemEntity item = buildItemEntity(10L, "DRG001", null, new BigDecimal("1"));
        item.setDispensingId(1L);
        when(dispensingItemRepository.findByDispensingIdIn(List.of(1L))).thenReturn(List.of(item));
        when(converter.toResponse(record, List.of(item))).thenReturn(new DispensingResponse());

        Result<PageResponse<DispensingResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData().getContent().size());
    }

    @Test
    void queryShouldTrimStatus() {
        DispensingQueryRequest req = new DispensingQueryRequest();
        req.setPatientId(1L);
        req.setStatus("  PENDING  ");

        Page<DispensingRecordEntity> page = new PageImpl<>(Collections.emptyList());
        when(dispensingRepository.findByPatientIdAndStatusOrderByCreatedAtDesc(eq(1L), eq("PENDING"), any(Pageable.class)))
                .thenReturn(page);

        Result<PageResponse<DispensingResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().getContent().isEmpty());
    }

    @Test
    void queryShouldUsePatientOnlyWhenOnlyPatientProvided() {
        DispensingQueryRequest req = new DispensingQueryRequest();
        req.setPatientId(1L);

        Page<DispensingRecordEntity> page = new PageImpl<>(Collections.emptyList());
        when(dispensingRepository.findByPatientIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        Result<PageResponse<DispensingResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().getContent().isEmpty());
    }

    @Test
    void queryShouldUseStatusOnlyWhenOnlyStatusProvided() {
        DispensingQueryRequest req = new DispensingQueryRequest();
        req.setStatus("PENDING");

        Page<DispensingRecordEntity> page = new PageImpl<>(Collections.emptyList());
        when(dispensingRepository.findByStatusOrderByCreatedAtDesc(eq("PENDING"), any(Pageable.class)))
                .thenReturn(page);

        Result<PageResponse<DispensingResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().getContent().isEmpty());
    }

    @Test
    void queryShouldFindAllWhenNeitherProvided() {
        DispensingQueryRequest req = new DispensingQueryRequest();

        Page<DispensingRecordEntity> page = new PageImpl<>(Collections.emptyList());
        when(dispensingRepository.findAll(any(Pageable.class))).thenReturn(page);

        Result<PageResponse<DispensingResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().getContent().isEmpty());
    }

    @Test
    void queryShouldNotLoadItemsWhenPageEmpty() {
        DispensingQueryRequest req = new DispensingQueryRequest();
        req.setStatus("  ");

        Page<DispensingRecordEntity> page = new PageImpl<>(Collections.emptyList());
        when(dispensingRepository.findAll(any(Pageable.class))).thenReturn(page);

        Result<PageResponse<DispensingResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(dispensingItemRepository, never()).findByDispensingIdIn(any());
    }

    @Test
    void queryShouldBatchLoadAndFilterItemsForMultipleRecords() {
        DispensingQueryRequest req = new DispensingQueryRequest();
        req.setPatientId(1L);

        DispensingRecordEntity r1 = buildRecord(1L, "PENDING");
        DispensingRecordEntity r2 = buildRecord(2L, "DISPENSED");
        Page<DispensingRecordEntity> page = new PageImpl<>(List.of(r1, r2));
        when(dispensingRepository.findByPatientIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        DispensingItemEntity item1 = buildItemEntity(10L, "DRG001", null, new BigDecimal("1"));
        item1.setDispensingId(1L);
        DispensingItemEntity item2 = buildItemEntity(11L, "DRG002", null, new BigDecimal("2"));
        item2.setDispensingId(2L);
        when(dispensingItemRepository.findByDispensingIdIn(List.of(1L, 2L))).thenReturn(List.of(item1, item2));
        when(converter.toResponse(eq(r1), anyList())).thenReturn(new DispensingResponse());
        when(converter.toResponse(eq(r2), anyList())).thenReturn(new DispensingResponse());

        Result<PageResponse<DispensingResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(2, result.getData().getContent().size());
    }

    // ==================== helpers ====================

    private DispensingCreateRequest buildCreateRequest(Long prescriptionId) {
        DispensingCreateRequest req = new DispensingCreateRequest();
        req.setPrescriptionId(prescriptionId);
        req.setPatientId(1L);
        req.setPatientName("张三");
        req.setItems(new ArrayList<>());
        return req;
    }

    private DispensingItemRequest buildItemRequest(String drugCode, String batchNo, BigDecimal quantity) {
        DispensingItemRequest item = new DispensingItemRequest();
        item.setDrugCode(drugCode);
        item.setDrugName("药品-" + drugCode);
        item.setBatchNo(batchNo);
        item.setQuantity(quantity);
        return item;
    }

    private DispensingRecordEntity buildRecord(Long id, String status) {
        DispensingRecordEntity record = new DispensingRecordEntity();
        record.setId(id);
        record.setDispensingNo("DISP" + id);
        record.setStatus(status);
        record.setPatientId(1L);
        return record;
    }

    private DispensingItemEntity buildItemEntity(Long id, String drugCode, String batchNo, BigDecimal quantity) {
        DispensingItemEntity item = new DispensingItemEntity();
        item.setId(id);
        item.setDispensingId(1L);
        item.setDrugCode(drugCode);
        item.setDrugName("药品-" + drugCode);
        item.setBatchNo(batchNo);
        item.setQuantity(quantity);
        return item;
    }

    private PharmacyStockEntity buildStock(String drugCode, String batchNo, BigDecimal quantity) {
        PharmacyStockEntity stock = new PharmacyStockEntity();
        stock.setId(1L);
        stock.setDrugCode(drugCode);
        stock.setDrugName("药品-" + drugCode);
        stock.setBatchNo(batchNo);
        stock.setQuantity(quantity);
        return stock;
    }
}
