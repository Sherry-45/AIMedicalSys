package com.aimedical.modules.window.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.window.WindowErrorCode;
import com.aimedical.modules.window.converter.WindowConverter;
import com.aimedical.modules.window.dto.PaymentRecordResponse;
import com.aimedical.modules.window.dto.ReconcileRequest;
import com.aimedical.modules.window.dto.ReconcileResponse;
import com.aimedical.modules.window.entity.PaymentItemEntity;
import com.aimedical.modules.window.entity.PaymentRecordEntity;
import com.aimedical.modules.window.repository.PaymentItemRepository;
import com.aimedical.modules.window.repository.PaymentRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link ReconciliationServiceImpl} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ReconciliationServiceImplTest {

    @Mock private PaymentRecordRepository paymentRecordRepository;
    @Mock private PaymentItemRepository paymentItemRepository;
    @Mock private WindowConverter converter;

    private ReconciliationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReconciliationServiceImpl(paymentRecordRepository, paymentItemRepository, converter);
    }

    // ==================== reconcile ====================

    @Test
    void reconcileShouldFailWhenPaymentIdsNull() {
        ReconcileRequest req = new ReconcileRequest();
        req.setPaymentIds(null);

        Result<ReconcileResponse> result = service.reconcile(req, 300L, "操作员");

        assertEquals(WindowErrorCode.RECONCILE_NO_RECORDS.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).findById(any());
        verify(paymentRecordRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void reconcileShouldFailWhenPaymentIdsEmpty() {
        ReconcileRequest req = new ReconcileRequest();
        req.setPaymentIds(List.of());

        Result<ReconcileResponse> result = service.reconcile(req, 300L, "操作员");

        assertEquals(WindowErrorCode.RECONCILE_NO_RECORDS.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).findById(any());
        verify(paymentRecordRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void reconcileShouldFailWhenOnePaymentNotFound() {
        ReconcileRequest req = new ReconcileRequest();
        req.setPaymentIds(List.of(1L, 2L));

        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(buildPayment(1L, "PAID")));
        when(paymentRecordRepository.findById(2L)).thenReturn(Optional.empty());

        Result<ReconcileResponse> result = service.reconcile(req, 300L, "操作员");

        assertEquals(WindowErrorCode.PAYMENT_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void reconcileShouldFailWhenStateNotPaid() {
        ReconcileRequest req = new ReconcileRequest();
        req.setPaymentIds(List.of(1L, 2L));

        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(buildPayment(1L, "PAID")));
        when(paymentRecordRepository.findById(2L)).thenReturn(Optional.of(buildPayment(2L, "PENDING")));

        Result<ReconcileResponse> result = service.reconcile(req, 300L, "操作员");

        assertEquals(WindowErrorCode.RECONCILE_INVALID_STATE.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void reconcileShouldUseProvidedBatchNoAndSumAmounts() {
        ReconcileRequest req = new ReconcileRequest();
        req.setPaymentIds(List.of(1L, 2L));
        req.setReconcileBatchNo("BATCH001");

        PaymentRecordEntity e1 = buildPayment(1L, "PAID");
        e1.setPaidAmount(new BigDecimal("100.00"));
        PaymentRecordEntity e2 = buildPayment(2L, "PAID");
        e2.setPaidAmount(new BigDecimal("200.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(e1));
        when(paymentRecordRepository.findById(2L)).thenReturn(Optional.of(e2));
        when(paymentRecordRepository.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));

        Result<ReconcileResponse> result = service.reconcile(req, 300L, "操作员");

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals("BATCH001", result.getData().getReconcileBatchNo());
        assertEquals(2, result.getData().getReconciledCount());
        assertEquals(new BigDecimal("300.00"), result.getData().getTotalAmount());

        // 验证保存的记录
        ArgumentCaptor<List<PaymentRecordEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(paymentRecordRepository).saveAllAndFlush(captor.capture());
        List<PaymentRecordEntity> saved = captor.getValue();
        assertEquals(2, saved.size());
        for (PaymentRecordEntity record : saved) {
            assertEquals("RECONCILED", record.getStatus());
            assertEquals("BATCH001", record.getReconcileBatchNo());
            assertNotNull(record.getReconciledAt());
        }
    }

    @Test
    void reconcileShouldGenerateBatchNoWhenBlank() {
        ReconcileRequest req = new ReconcileRequest();
        req.setPaymentIds(List.of(1L));
        req.setReconcileBatchNo("");  // blank → 自动生成

        PaymentRecordEntity e1 = buildPayment(1L, "PAID");
        e1.setPaidAmount(new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(e1));
        when(paymentRecordRepository.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));

        Result<ReconcileResponse> result = service.reconcile(req, 300L, "操作员");

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData().getReconcileBatchNo());
        assertTrue(result.getData().getReconcileBatchNo().startsWith("RCN"));
    }

    @Test
    void reconcileShouldGenerateBatchNoWhenNull() {
        ReconcileRequest req = new ReconcileRequest();
        req.setPaymentIds(List.of(1L));
        req.setReconcileBatchNo(null);

        PaymentRecordEntity e1 = buildPayment(1L, "PAID");
        e1.setPaidAmount(new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(e1));
        when(paymentRecordRepository.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));

        Result<ReconcileResponse> result = service.reconcile(req, 300L, "操作员");

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().getReconcileBatchNo().startsWith("RCN"));
    }

    @Test
    void reconcileShouldHandleNullPaidAmountWhenSumming() {
        ReconcileRequest req = new ReconcileRequest();
        req.setPaymentIds(List.of(1L));
        req.setReconcileBatchNo("BATCH002");

        PaymentRecordEntity e1 = buildPayment(1L, "PAID");
        e1.setPaidAmount(null);  // null → ZERO
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(e1));
        when(paymentRecordRepository.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));

        Result<ReconcileResponse> result = service.reconcile(req, 300L, "操作员");

        assertEquals("SUCCESS", result.getCode());
        assertEquals(BigDecimal.ZERO, result.getData().getTotalAmount());
        assertEquals(1, result.getData().getReconciledCount());
    }

    @Test
    void reconcileShouldReturnConflictWhenOptimisticLockOccurs() {
        ReconcileRequest req = new ReconcileRequest();
        req.setPaymentIds(List.of(1L));
        req.setReconcileBatchNo("BATCH003");

        PaymentRecordEntity e1 = buildPayment(1L, "PAID");
        e1.setPaidAmount(new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(e1));
        when(paymentRecordRepository.saveAllAndFlush(anyList()))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<ReconcileResponse> result = service.reconcile(req, 300L, "操作员");

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    // ==================== listByBatchNo ====================

    @Test
    void listByBatchNoShouldReturnEmptyListWhenNoRecords() {
        when(paymentRecordRepository.findByReconcileBatchNo("BATCH001")).thenReturn(List.of());

        Result<List<PaymentRecordResponse>> result = service.listByBatchNo("BATCH001");

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
        verify(paymentItemRepository, never()).findByPaymentIdIn(any());
    }

    @Test
    void listByBatchNoShouldGroupItemsAndReturnResponses() {
        PaymentRecordEntity e1 = buildPayment(1L, "RECONCILED");
        PaymentRecordEntity e2 = buildPayment(2L, "RECONCILED");
        when(paymentRecordRepository.findByReconcileBatchNo("BATCH001"))
                .thenReturn(List.of(e1, e2));

        PaymentItemEntity item1 = new PaymentItemEntity();
        item1.setPaymentId(1L);
        PaymentItemEntity item2 = new PaymentItemEntity();
        item2.setPaymentId(2L);
        when(paymentItemRepository.findByPaymentIdIn(any())).thenReturn(List.of(item1, item2));

        PaymentRecordResponse r1 = new PaymentRecordResponse();
        r1.setId(1L);
        PaymentRecordResponse r2 = new PaymentRecordResponse();
        r2.setId(2L);
        when(converter.toPaymentResponse(eq(e1), anyList())).thenReturn(r1);
        when(converter.toPaymentResponse(eq(e2), anyList())).thenReturn(r2);

        Result<List<PaymentRecordResponse>> result = service.listByBatchNo("BATCH001");

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
        assertEquals(1L, result.getData().get(0).getId());
        assertEquals(2L, result.getData().get(1).getId());
    }

    // ==================== helpers ====================

    private PaymentRecordEntity buildPayment(Long id, String status) {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setId(id);
        entity.setPaymentNo("PAY" + id);
        entity.setPatientId(100L);
        entity.setPatientName("张三");
        entity.setStatus(status);
        return entity;
    }
}
