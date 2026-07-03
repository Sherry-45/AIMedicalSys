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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PharmacyRefundServiceImplTest {

    @Mock private PharmacyRefundRecordRepository refundRepository;
    @Mock private PharmacyRefundItemRepository refundItemRepository;
    @Mock private DispensingRecordRepository dispensingRepository;
    @Mock private DispensingItemRepository dispensingItemRepository;
    @Mock private PharmacyStockRepository stockRepository;
    @Mock private PharmacyConverter converter;

    private PharmacyRefundServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PharmacyRefundServiceImpl(refundRepository, refundItemRepository,
                dispensingRepository, dispensingItemRepository, stockRepository, converter);
    }

    // ==================== create ====================

    @Test
    void createShouldFailWhenPharmacistIdNull() {
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);

        Result<PharmacyRefundResponse> result = service.create(req, null, "药师");

        assertEquals(GlobalErrorCode.UNAUTHORIZED.getCode(), result.getCode());
        assertNull(result.getData());
        verify(refundRepository, never()).save(any());
    }

    @Test
    void createShouldFailWhenItemsNull() {
        PharmacyRefundCreateRequest req = new PharmacyRefundCreateRequest();
        req.setDispensingId(1L);
        req.setItems(null);

        Result<PharmacyRefundResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.REFUND_QUANTITY_EXCEEDED.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenItemsEmpty() {
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        req.setItems(Collections.emptyList());

        Result<PharmacyRefundResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.REFUND_QUANTITY_EXCEEDED.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenDispensingNotFound() {
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        when(dispensingRepository.findById(1L)).thenReturn(Optional.empty());

        Result<PharmacyRefundResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.DISPENSING_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenDispensingNotDispensed() {
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        DispensingRecordEntity dispensing = buildDispensing(1L, DispensingStatus.PENDING.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(dispensing));

        Result<PharmacyRefundResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.DISPENSING_INVALID_STATE.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenPendingRefundAlreadyExists() {
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        DispensingRecordEntity dispensing = buildDispensing(1L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(dispensing));
        when(refundRepository.findByDispensingIdAndStatus(1L, PharmacyRefundStatus.PENDING.getCode()))
                .thenReturn(Optional.of(new PharmacyRefundRecordEntity()));

        Result<PharmacyRefundResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.REFUND_DUPLICATE.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenDispensingItemNotFound() {
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        req.setItems(List.of(buildRefundItemRequest(999L, "DRG001", new BigDecimal("1"))));
        DispensingRecordEntity dispensing = buildDispensing(1L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(dispensing));
        when(refundRepository.findByDispensingIdAndStatus(1L, PharmacyRefundStatus.PENDING.getCode()))
                .thenReturn(Optional.empty());
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(Collections.emptyList());

        Result<PharmacyRefundResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.DISPENSING_ITEM_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenRefundQuantityExceededWithNoHistory() {
        // 发药数量 5，申请退 10 -> 超过可退数量
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        req.setItems(List.of(buildRefundItemRequest(100L, "DRG001", new BigDecimal("10"))));
        DispensingRecordEntity dispensing = buildDispensing(1L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(dispensing));
        when(refundRepository.findByDispensingIdAndStatus(1L, PharmacyRefundStatus.PENDING.getCode()))
                .thenReturn(Optional.empty());
        DispensingItemEntity dispensingItem = buildDispensingItem(100L, "DRG001", new BigDecimal("5"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(dispensingItem));
        when(refundItemRepository.findByDispensingItemId(100L)).thenReturn(Collections.emptyList());

        Result<PharmacyRefundResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.REFUND_QUANTITY_EXCEEDED.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenRefundQuantityExceededDueToHistoryRefunded() {
        // 发药数量 10，历史已退（REFUNDED）6，申请退 5 -> 5 > (10-6)=4
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        req.setItems(List.of(buildRefundItemRequest(100L, "DRG001", new BigDecimal("5"))));
        DispensingRecordEntity dispensing = buildDispensing(1L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(dispensing));
        when(refundRepository.findByDispensingIdAndStatus(1L, PharmacyRefundStatus.PENDING.getCode()))
                .thenReturn(Optional.empty());
        DispensingItemEntity dispensingItem = buildDispensingItem(100L, "DRG001", new BigDecimal("10"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(dispensingItem));

        PharmacyRefundItemEntity historyItem = new PharmacyRefundItemEntity();
        historyItem.setRefundId(500L);
        historyItem.setQuantity(new BigDecimal("6"));
        when(refundItemRepository.findByDispensingItemId(100L)).thenReturn(List.of(historyItem));
        PharmacyRefundRecordEntity historyRecord = buildRefundRecord(500L, PharmacyRefundStatus.REFUNDED.getCode());
        when(refundRepository.findById(500L)).thenReturn(Optional.of(historyRecord));

        Result<PharmacyRefundResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.REFUND_QUANTITY_EXCEEDED.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldFailWhenRefundQuantityExceededDueToHistoryPending() {
        // 发药数量 10，历史待处理（PENDING）6，申请退 5 -> 5 > (10-6)=4
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        req.setItems(List.of(buildRefundItemRequest(100L, "DRG001", new BigDecimal("5"))));
        DispensingRecordEntity dispensing = buildDispensing(1L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(dispensing));
        when(refundRepository.findByDispensingIdAndStatus(1L, PharmacyRefundStatus.PENDING.getCode()))
                .thenReturn(Optional.empty());
        DispensingItemEntity dispensingItem = buildDispensingItem(100L, "DRG001", new BigDecimal("10"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(dispensingItem));

        PharmacyRefundItemEntity historyItem = new PharmacyRefundItemEntity();
        historyItem.setRefundId(500L);
        historyItem.setQuantity(new BigDecimal("6"));
        when(refundItemRepository.findByDispensingItemId(100L)).thenReturn(List.of(historyItem));
        PharmacyRefundRecordEntity historyRecord = buildRefundRecord(500L, PharmacyRefundStatus.PENDING.getCode());
        when(refundRepository.findById(500L)).thenReturn(Optional.of(historyRecord));

        Result<PharmacyRefundResponse> result = service.create(req, 10L, "药师");

        assertEquals(PharmacyErrorCode.REFUND_QUANTITY_EXCEEDED.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void createShouldIgnoreHistoryRecordNotFound() {
        // 历史明细存在但其退药记录找不到 -> 不计入已退数量
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        req.setItems(List.of(buildRefundItemRequest(100L, "DRG001", new BigDecimal("5"))));
        DispensingRecordEntity dispensing = buildDispensing(1L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(dispensing));
        when(refundRepository.findByDispensingIdAndStatus(1L, PharmacyRefundStatus.PENDING.getCode()))
                .thenReturn(Optional.empty());
        DispensingItemEntity dispensingItem = buildDispensingItem(100L, "DRG001", new BigDecimal("10"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(dispensingItem));

        PharmacyRefundItemEntity historyItem = new PharmacyRefundItemEntity();
        historyItem.setRefundId(500L);
        historyItem.setQuantity(new BigDecimal("6"));
        when(refundItemRepository.findByDispensingItemId(100L)).thenReturn(List.of(historyItem));
        when(refundRepository.findById(500L)).thenReturn(Optional.empty());
        when(refundRepository.save(any(PharmacyRefundRecordEntity.class))).thenAnswer(inv -> {
            PharmacyRefundRecordEntity r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });
        when(converter.toResponse(any(PharmacyRefundRecordEntity.class), anyList())).thenReturn(new PharmacyRefundResponse());

        Result<PharmacyRefundResponse> result = service.create(req, 10L, "药师");

        assertEquals("SUCCESS", result.getCode());
    }

    @Test
    void createShouldIgnoreHistoryRecordWithRejectedStatus() {
        // 历史明细的退药记录状态为 REJECTED -> 不计入已退数量
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        req.setItems(List.of(buildRefundItemRequest(100L, "DRG001", new BigDecimal("5"))));
        DispensingRecordEntity dispensing = buildDispensing(1L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(dispensing));
        when(refundRepository.findByDispensingIdAndStatus(1L, PharmacyRefundStatus.PENDING.getCode()))
                .thenReturn(Optional.empty());
        DispensingItemEntity dispensingItem = buildDispensingItem(100L, "DRG001", new BigDecimal("10"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(dispensingItem));

        PharmacyRefundItemEntity historyItem = new PharmacyRefundItemEntity();
        historyItem.setRefundId(500L);
        historyItem.setQuantity(new BigDecimal("6"));
        when(refundItemRepository.findByDispensingItemId(100L)).thenReturn(List.of(historyItem));
        PharmacyRefundRecordEntity historyRecord = buildRefundRecord(500L, PharmacyRefundStatus.REJECTED.getCode());
        when(refundRepository.findById(500L)).thenReturn(Optional.of(historyRecord));
        when(refundRepository.save(any(PharmacyRefundRecordEntity.class))).thenAnswer(inv -> {
            PharmacyRefundRecordEntity r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });
        when(converter.toResponse(any(PharmacyRefundRecordEntity.class), anyList())).thenReturn(new PharmacyRefundResponse());

        Result<PharmacyRefundResponse> result = service.create(req, 10L, "药师");

        assertEquals("SUCCESS", result.getCode());
    }

    @Test
    void createShouldFailOnOptimisticLockWhenSavingRefund() {
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        req.setItems(List.of(buildRefundItemRequest(100L, "DRG001", new BigDecimal("2"))));
        DispensingRecordEntity dispensing = buildDispensing(1L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(dispensing));
        when(refundRepository.findByDispensingIdAndStatus(1L, PharmacyRefundStatus.PENDING.getCode()))
                .thenReturn(Optional.empty());
        DispensingItemEntity dispensingItem = buildDispensingItem(100L, "DRG001", new BigDecimal("10"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(dispensingItem));
        when(refundItemRepository.findByDispensingItemId(100L)).thenReturn(Collections.emptyList());
        when(refundRepository.save(any(PharmacyRefundRecordEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<PharmacyRefundResponse> result = service.create(req, 10L, "药师");

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
        verify(refundItemRepository, never()).saveAll(any());
    }

    @Test
    void createShouldSucceedAndComputeTotals() {
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        req.setRefundReason("药品过敏");
        req.setRemark("备注");
        PharmacyRefundItemRequest itemReq = buildRefundItemRequest(100L, "DRG001", new BigDecimal("3"));
        itemReq.setUnitPrice(new BigDecimal("12.50"));
        itemReq.setDrugName("阿莫西林");
        req.setItems(List.of(itemReq));
        DispensingRecordEntity dispensing = buildDispensing(1L, DispensingStatus.DISPENSED.getCode());
        dispensing.setPatientId(20L);
        dispensing.setPatientName("张三");
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(dispensing));
        when(refundRepository.findByDispensingIdAndStatus(1L, PharmacyRefundStatus.PENDING.getCode()))
                .thenReturn(Optional.empty());
        DispensingItemEntity dispensingItem = buildDispensingItem(100L, "DRG001", new BigDecimal("10"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(dispensingItem));
        when(refundItemRepository.findByDispensingItemId(100L)).thenReturn(Collections.emptyList());
        when(refundRepository.save(any(PharmacyRefundRecordEntity.class))).thenAnswer(inv -> {
            PharmacyRefundRecordEntity r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });
        when(converter.toResponse(any(PharmacyRefundRecordEntity.class), anyList())).thenReturn(new PharmacyRefundResponse());

        Result<PharmacyRefundResponse> result = service.create(req, 30L, "李药师");

        assertEquals("SUCCESS", result.getCode());

        ArgumentCaptor<PharmacyRefundRecordEntity> refundCaptor = ArgumentCaptor.forClass(PharmacyRefundRecordEntity.class);
        verify(refundRepository).save(refundCaptor.capture());
        PharmacyRefundRecordEntity savedRefund = refundCaptor.getValue();
        assertEquals(PharmacyRefundStatus.PENDING.getCode(), savedRefund.getStatus());
        assertEquals(1L, savedRefund.getDispensingId());
        assertEquals(20L, savedRefund.getPatientId());
        assertEquals("张三", savedRefund.getPatientName());
        assertEquals(30L, savedRefund.getPharmacistId());
        assertEquals("李药师", savedRefund.getPharmacistName());
        assertEquals("药品过敏", savedRefund.getRefundReason());
        assertEquals(new BigDecimal("3"), savedRefund.getTotalQuantity());
        assertEquals(new BigDecimal("37.50"), savedRefund.getTotalAmount());
        assertNotNull(savedRefund.getRefundNo());
        assertTrue(savedRefund.getRefundNo().startsWith("RFD"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PharmacyRefundItemEntity>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(refundItemRepository).saveAll(itemsCaptor.capture());
        PharmacyRefundItemEntity savedItem = itemsCaptor.getValue().get(0);
        assertEquals(2L, savedItem.getRefundId());
        assertEquals(100L, savedItem.getDispensingItemId());
        assertEquals("阿莫西林", savedItem.getDrugName());
        assertEquals(new BigDecimal("37.50"), savedItem.getAmount());
    }

    @Test
    void createShouldFallbackToDispensingItemFieldsWhenRequestFieldsNull() {
        // 请求中 batchNo/unit/unitPrice 为 null，应回退到发药明细的值
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        PharmacyRefundItemRequest itemReq = new PharmacyRefundItemRequest();
        itemReq.setDispensingItemId(100L);
        itemReq.setDrugCode("DRG001");
        itemReq.setDrugName("阿莫西林");
        itemReq.setQuantity(new BigDecimal("2"));
        // batchNo, unit, unitPrice 均为 null
        req.setItems(List.of(itemReq));
        DispensingRecordEntity dispensing = buildDispensing(1L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(dispensing));
        when(refundRepository.findByDispensingIdAndStatus(1L, PharmacyRefundStatus.PENDING.getCode()))
                .thenReturn(Optional.empty());
        DispensingItemEntity dispensingItem = buildDispensingItem(100L, "DRG001", new BigDecimal("10"));
        dispensingItem.setBatchNo("B1");
        dispensingItem.setUnit("盒");
        dispensingItem.setUnitPrice(new BigDecimal("12.50"));
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(dispensingItem));
        when(refundItemRepository.findByDispensingItemId(100L)).thenReturn(Collections.emptyList());
        when(refundRepository.save(any(PharmacyRefundRecordEntity.class))).thenAnswer(inv -> {
            PharmacyRefundRecordEntity r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });
        when(converter.toResponse(any(PharmacyRefundRecordEntity.class), anyList())).thenReturn(new PharmacyRefundResponse());

        Result<PharmacyRefundResponse> result = service.create(req, 30L, "李药师");

        assertEquals("SUCCESS", result.getCode());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PharmacyRefundItemEntity>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(refundItemRepository).saveAll(itemsCaptor.capture());
        PharmacyRefundItemEntity savedItem = itemsCaptor.getValue().get(0);
        assertEquals("B1", savedItem.getBatchNo());
        assertEquals("盒", savedItem.getUnit());
        assertEquals(new BigDecimal("12.50"), savedItem.getUnitPrice());
        assertEquals(new BigDecimal("25.00"), savedItem.getAmount());
    }

    @Test
    void createShouldHandleNullUnitPriceWithZeroAmount() {
        // 请求和发药明细的 unitPrice 均为 null -> amount = ZERO
        PharmacyRefundCreateRequest req = buildRefundRequest(1L);
        PharmacyRefundItemRequest itemReq = buildRefundItemRequest(100L, "DRG001", new BigDecimal("2"));
        itemReq.setUnitPrice(null);
        req.setItems(List.of(itemReq));
        DispensingRecordEntity dispensing = buildDispensing(1L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(1L)).thenReturn(Optional.of(dispensing));
        when(refundRepository.findByDispensingIdAndStatus(1L, PharmacyRefundStatus.PENDING.getCode()))
                .thenReturn(Optional.empty());
        DispensingItemEntity dispensingItem = buildDispensingItem(100L, "DRG001", new BigDecimal("10"));
        dispensingItem.setUnitPrice(null);
        when(dispensingItemRepository.findByDispensingId(1L)).thenReturn(List.of(dispensingItem));
        when(refundItemRepository.findByDispensingItemId(100L)).thenReturn(Collections.emptyList());
        when(refundRepository.save(any(PharmacyRefundRecordEntity.class))).thenAnswer(inv -> {
            PharmacyRefundRecordEntity r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });
        when(converter.toResponse(any(PharmacyRefundRecordEntity.class), anyList())).thenReturn(new PharmacyRefundResponse());

        Result<PharmacyRefundResponse> result = service.create(req, 30L, "李药师");

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<PharmacyRefundRecordEntity> refundCaptor = ArgumentCaptor.forClass(PharmacyRefundRecordEntity.class);
        verify(refundRepository).save(refundCaptor.capture());
        assertEquals(BigDecimal.ZERO, refundCaptor.getValue().getTotalAmount());
    }

    // ==================== approve ====================

    @Test
    void approveShouldFailWhenIdNull() {
        Result<PharmacyRefundResponse> result = service.approve(null);
        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void approveShouldFailWhenNotFound() {
        when(refundRepository.findById(1L)).thenReturn(Optional.empty());

        Result<PharmacyRefundResponse> result = service.approve(1L);

        assertEquals(PharmacyErrorCode.REFUND_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void approveShouldFailWhenNotPending() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.REFUNDED.getCode());
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        Result<PharmacyRefundResponse> result = service.approve(1L);

        assertEquals(PharmacyErrorCode.REFUND_INVALID_STATE.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void approveShouldFailWhenDispensingNotFound() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        refund.setDispensingId(10L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        when(dispensingRepository.findById(10L)).thenReturn(Optional.empty());

        Result<PharmacyRefundResponse> result = service.approve(1L);

        assertEquals(PharmacyErrorCode.DISPENSING_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void approveShouldFailWhenNoStockToRestore() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        refund.setDispensingId(10L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        when(dispensingRepository.findById(10L)).thenReturn(Optional.of(buildDispensing(10L, DispensingStatus.DISPENSED.getCode())));
        PharmacyRefundItemEntity refundItem = buildRefundItem(100L, "DRG001", "B1", new BigDecimal("2"));
        when(refundItemRepository.findByRefundId(1L)).thenReturn(List.of(refundItem));
        // 原批次不存在，按药品编码也没有库存
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.empty());
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(Collections.emptyList());

        Result<PharmacyRefundResponse> result = service.approve(1L);

        assertEquals(PharmacyErrorCode.STOCK_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void approveShouldRestoreToOriginalBatchAndSucceed() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        refund.setDispensingId(10L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        DispensingRecordEntity dispensing = buildDispensing(10L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(10L)).thenReturn(Optional.of(dispensing));
        PharmacyRefundItemEntity refundItem = buildRefundItem(100L, "DRG001", "B1", new BigDecimal("2"));
        when(refundItemRepository.findByRefundId(1L)).thenReturn(List.of(refundItem));
        PharmacyStockEntity stock = buildStock("DRG001", "B1", new BigDecimal("3"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenReturn(stock);
        when(refundRepository.save(refund)).thenReturn(refund);
        when(dispensingRepository.save(dispensing)).thenReturn(dispensing);
        when(converter.toResponse(refund, List.of(refundItem))).thenReturn(new PharmacyRefundResponse());

        Result<PharmacyRefundResponse> result = service.approve(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(PharmacyRefundStatus.REFUNDED.getCode(), refund.getStatus());
        assertNotNull(refund.getRefundedAt());
        assertEquals(DispensingStatus.REFUNDED.getCode(), dispensing.getStatus());
        assertEquals(new BigDecimal("5"), stock.getQuantity());
    }

    @Test
    void approveShouldRestoreToFirstBatchWhenOriginalBatchMissing() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        refund.setDispensingId(10L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        DispensingRecordEntity dispensing = buildDispensing(10L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(10L)).thenReturn(Optional.of(dispensing));
        // 退款明细无批次号
        PharmacyRefundItemEntity refundItem = buildRefundItem(100L, "DRG001", null, new BigDecimal("2"));
        when(refundItemRepository.findByRefundId(1L)).thenReturn(List.of(refundItem));
        PharmacyStockEntity stock = buildStock("DRG001", "B2", new BigDecimal("3"));
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(List.of(stock));
        when(stockRepository.save(stock)).thenReturn(stock);
        when(refundRepository.save(refund)).thenReturn(refund);
        when(dispensingRepository.save(dispensing)).thenReturn(dispensing);
        when(converter.toResponse(refund, List.of(refundItem))).thenReturn(new PharmacyRefundResponse());

        Result<PharmacyRefundResponse> result = service.approve(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(new BigDecimal("5"), stock.getQuantity());
    }

    @Test
    void approveShouldRestoreToFirstBatchWhenBatchNoBlank() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        refund.setDispensingId(10L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        DispensingRecordEntity dispensing = buildDispensing(10L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(10L)).thenReturn(Optional.of(dispensing));
        PharmacyRefundItemEntity refundItem = buildRefundItem(100L, "DRG001", "  ", new BigDecimal("2"));
        when(refundItemRepository.findByRefundId(1L)).thenReturn(List.of(refundItem));
        // 空白批次号应跳过 findByDrugCodeAndBatchNo，直接走 findByDrugCode
        PharmacyStockEntity stock = buildStock("DRG001", "B2", new BigDecimal("3"));
        when(stockRepository.findByDrugCode("DRG001")).thenReturn(List.of(stock));
        when(stockRepository.save(stock)).thenReturn(stock);
        when(refundRepository.save(refund)).thenReturn(refund);
        when(dispensingRepository.save(dispensing)).thenReturn(dispensing);
        when(converter.toResponse(refund, List.of(refundItem))).thenReturn(new PharmacyRefundResponse());

        Result<PharmacyRefundResponse> result = service.approve(1L);

        assertEquals("SUCCESS", result.getCode());
        verify(stockRepository, never()).findByDrugCodeAndBatchNo(any(), any());
        assertEquals(new BigDecimal("5"), stock.getQuantity());
    }

    @Test
    void approveShouldFailOnOptimisticLockWhenRestoringStock() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        refund.setDispensingId(10L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        when(dispensingRepository.findById(10L)).thenReturn(Optional.of(buildDispensing(10L, DispensingStatus.DISPENSED.getCode())));
        PharmacyRefundItemEntity refundItem = buildRefundItem(100L, "DRG001", "B1", new BigDecimal("2"));
        when(refundItemRepository.findByRefundId(1L)).thenReturn(List.of(refundItem));
        PharmacyStockEntity stock = buildStock("DRG001", "B1", new BigDecimal("3"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<PharmacyRefundResponse> result = service.approve(1L);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void approveShouldFailOnOptimisticLockWhenSavingRefund() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        refund.setDispensingId(10L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        when(dispensingRepository.findById(10L)).thenReturn(Optional.of(buildDispensing(10L, DispensingStatus.DISPENSED.getCode())));
        PharmacyRefundItemEntity refundItem = buildRefundItem(100L, "DRG001", "B1", new BigDecimal("2"));
        when(refundItemRepository.findByRefundId(1L)).thenReturn(List.of(refundItem));
        PharmacyStockEntity stock = buildStock("DRG001", "B1", new BigDecimal("3"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenReturn(stock);
        when(refundRepository.save(refund)).thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<PharmacyRefundResponse> result = service.approve(1L);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void approveShouldFailOnOptimisticLockWhenSavingDispensing() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        refund.setDispensingId(10L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        DispensingRecordEntity dispensing = buildDispensing(10L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(10L)).thenReturn(Optional.of(dispensing));
        PharmacyRefundItemEntity refundItem = buildRefundItem(100L, "DRG001", "B1", new BigDecimal("2"));
        when(refundItemRepository.findByRefundId(1L)).thenReturn(List.of(refundItem));
        PharmacyStockEntity stock = buildStock("DRG001", "B1", new BigDecimal("3"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenReturn(stock);
        when(refundRepository.save(refund)).thenReturn(refund);
        when(dispensingRepository.save(dispensing)).thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<PharmacyRefundResponse> result = service.approve(1L);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void approveShouldRestoreMultipleItems() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        refund.setDispensingId(10L);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        DispensingRecordEntity dispensing = buildDispensing(10L, DispensingStatus.DISPENSED.getCode());
        when(dispensingRepository.findById(10L)).thenReturn(Optional.of(dispensing));
        PharmacyRefundItemEntity ri1 = buildRefundItem(100L, "DRG001", "B1", new BigDecimal("2"));
        PharmacyRefundItemEntity ri2 = buildRefundItem(101L, "DRG002", "B2", new BigDecimal("1"));
        when(refundItemRepository.findByRefundId(1L)).thenReturn(List.of(ri1, ri2));
        PharmacyStockEntity s1 = buildStock("DRG001", "B1", new BigDecimal("3"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG001", "B1")).thenReturn(Optional.of(s1));
        PharmacyStockEntity s2 = buildStock("DRG002", "B2", new BigDecimal("4"));
        when(stockRepository.findByDrugCodeAndBatchNo("DRG002", "B2")).thenReturn(Optional.of(s2));
        when(stockRepository.save(any(PharmacyStockEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(refundRepository.save(refund)).thenReturn(refund);
        when(dispensingRepository.save(dispensing)).thenReturn(dispensing);
        when(converter.toResponse(eq(refund), anyList())).thenReturn(new PharmacyRefundResponse());

        Result<PharmacyRefundResponse> result = service.approve(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(new BigDecimal("5"), s1.getQuantity());
        assertEquals(new BigDecimal("5"), s2.getQuantity());
    }

    // ==================== reject ====================

    @Test
    void rejectShouldFailWhenIdNull() {
        Result<PharmacyRefundResponse> result = service.reject(null, "原因");
        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void rejectShouldFailWhenReasonNull() {
        Result<PharmacyRefundResponse> result = service.reject(1L, null);
        assertEquals(PharmacyErrorCode.REFUND_REASON_EMPTY.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void rejectShouldFailWhenReasonBlank() {
        Result<PharmacyRefundResponse> result = service.reject(1L, "   ");
        assertEquals(PharmacyErrorCode.REFUND_REASON_EMPTY.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void rejectShouldFailWhenNotFound() {
        when(refundRepository.findById(1L)).thenReturn(Optional.empty());

        Result<PharmacyRefundResponse> result = service.reject(1L, "原因");

        assertEquals(PharmacyErrorCode.REFUND_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void rejectShouldFailWhenNotPending() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.REJECTED.getCode());
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));

        Result<PharmacyRefundResponse> result = service.reject(1L, "原因");

        assertEquals(PharmacyErrorCode.REFUND_INVALID_STATE.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void rejectShouldSucceedAndAppendRemarkWhenNoExistingRemark() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        refund.setRemark(null);
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        when(refundRepository.save(refund)).thenReturn(refund);
        when(refundItemRepository.findByRefundId(1L)).thenReturn(Collections.emptyList());
        when(converter.toResponse(refund, Collections.emptyList())).thenReturn(new PharmacyRefundResponse());

        Result<PharmacyRefundResponse> result = service.reject(1L, "药品无问题");

        assertEquals("SUCCESS", result.getCode());
        assertEquals(PharmacyRefundStatus.REJECTED.getCode(), refund.getStatus());
        assertEquals("驳回原因: 药品无问题", refund.getRemark());
    }

    @Test
    void rejectShouldSucceedAndAppendRemarkWhenExistingRemarkPresent() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        refund.setRemark("原备注");
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        when(refundRepository.save(refund)).thenReturn(refund);
        when(refundItemRepository.findByRefundId(1L)).thenReturn(Collections.emptyList());
        when(converter.toResponse(refund, Collections.emptyList())).thenReturn(new PharmacyRefundResponse());

        Result<PharmacyRefundResponse> result = service.reject(1L, "药品无问题");

        assertEquals("SUCCESS", result.getCode());
        assertEquals("原备注 | 驳回原因: 药品无问题", refund.getRemark());
    }

    @Test
    void rejectShouldFailOnOptimisticLock() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        when(refundRepository.save(refund)).thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<PharmacyRefundResponse> result = service.reject(1L, "原因");

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    // ==================== getById ====================

    @Test
    void getByIdShouldFailWhenIdNull() {
        Result<PharmacyRefundResponse> result = service.getById(null);
        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByIdShouldFailWhenNotFound() {
        when(refundRepository.findById(1L)).thenReturn(Optional.empty());

        Result<PharmacyRefundResponse> result = service.getById(1L);

        assertEquals(PharmacyErrorCode.REFUND_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByIdShouldReturnResponseWithItems() {
        PharmacyRefundRecordEntity refund = buildRefundRecord(1L, PharmacyRefundStatus.PENDING.getCode());
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        PharmacyRefundItemEntity item = buildRefundItem(100L, "DRG001", "B1", new BigDecimal("1"));
        when(refundItemRepository.findByRefundId(1L)).thenReturn(List.of(item));
        PharmacyRefundResponse expected = new PharmacyRefundResponse();
        expected.setId(1L);
        when(converter.toResponse(refund, List.of(item))).thenReturn(expected);

        Result<PharmacyRefundResponse> result = service.getById(1L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
    }

    // ==================== helpers ====================

    private PharmacyRefundCreateRequest buildRefundRequest(Long dispensingId) {
        PharmacyRefundCreateRequest req = new PharmacyRefundCreateRequest();
        req.setDispensingId(dispensingId);
        req.setItems(new java.util.ArrayList<>(List.of(buildRefundItemRequest(100L, "DRG001", new BigDecimal("1")))));
        return req;
    }

    private PharmacyRefundItemRequest buildRefundItemRequest(Long dispensingItemId, String drugCode, BigDecimal quantity) {
        PharmacyRefundItemRequest item = new PharmacyRefundItemRequest();
        item.setDispensingItemId(dispensingItemId);
        item.setDrugCode(drugCode);
        item.setDrugName("药品-" + drugCode);
        item.setQuantity(quantity);
        return item;
    }

    private DispensingRecordEntity buildDispensing(Long id, String status) {
        DispensingRecordEntity entity = new DispensingRecordEntity();
        entity.setId(id);
        entity.setDispensingNo("DISP" + id);
        entity.setStatus(status);
        return entity;
    }

    private DispensingItemEntity buildDispensingItem(Long id, String drugCode, BigDecimal quantity) {
        DispensingItemEntity entity = new DispensingItemEntity();
        entity.setId(id);
        entity.setDispensingId(1L);
        entity.setDrugCode(drugCode);
        entity.setDrugName("药品-" + drugCode);
        entity.setQuantity(quantity);
        return entity;
    }

    private PharmacyRefundRecordEntity buildRefundRecord(Long id, String status) {
        PharmacyRefundRecordEntity entity = new PharmacyRefundRecordEntity();
        entity.setId(id);
        entity.setRefundNo("RFD" + id);
        entity.setDispensingId(1L);
        entity.setStatus(status);
        return entity;
    }

    private PharmacyRefundItemEntity buildRefundItem(Long dispensingItemId, String drugCode, String batchNo, BigDecimal quantity) {
        PharmacyRefundItemEntity entity = new PharmacyRefundItemEntity();
        entity.setId(1L);
        entity.setRefundId(1L);
        entity.setDispensingItemId(dispensingItemId);
        entity.setDrugCode(drugCode);
        entity.setDrugName("药品-" + drugCode);
        entity.setBatchNo(batchNo);
        entity.setQuantity(quantity);
        return entity;
    }

    private PharmacyStockEntity buildStock(String drugCode, String batchNo, BigDecimal quantity) {
        PharmacyStockEntity entity = new PharmacyStockEntity();
        entity.setId(1L);
        entity.setDrugCode(drugCode);
        entity.setDrugName("药品-" + drugCode);
        entity.setBatchNo(batchNo);
        entity.setQuantity(quantity);
        return entity;
    }
}
