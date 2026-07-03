package com.aimedical.modules.window.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.window.WindowErrorCode;
import com.aimedical.modules.window.converter.WindowConverter;
import com.aimedical.modules.window.dto.PayRequest;
import com.aimedical.modules.window.dto.PaymentCreateRequest;
import com.aimedical.modules.window.dto.PaymentItemRequest;
import com.aimedical.modules.window.dto.PaymentQueryRequest;
import com.aimedical.modules.window.dto.PaymentRecordResponse;
import com.aimedical.modules.window.dto.RefundRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link PaymentServiceImpl} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRecordRepository paymentRecordRepository;
    @Mock private PaymentItemRepository paymentItemRepository;
    @Mock private WindowConverter converter;

    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentServiceImpl(paymentRecordRepository, paymentItemRepository, converter);
    }

    // ==================== create ====================

    @Test
    void createShouldSavePaymentAndItemsAndReturnResponse() {
        PaymentCreateRequest req = new PaymentCreateRequest();
        req.setPatientId(100L);
        req.setPatientName("张三");
        req.setSourceId(500L);
        req.setSourceType("MEDICAL_ORDER");
        req.setSourceNo("ORDER001");
        req.setTotalAmount(new BigDecimal("200.00"));
        req.setRemark("备注");

        PaymentItemRequest itemReq1 = new PaymentItemRequest();
        itemReq1.setItemType("DRUG_FEE");
        itemReq1.setItemName("阿莫西林");
        itemReq1.setQuantity(new BigDecimal("2"));
        itemReq1.setUnitPrice(new BigDecimal("15.00"));
        itemReq1.setRemark("item1");

        PaymentItemRequest itemReq2 = new PaymentItemRequest();
        itemReq2.setItemType("EXAMINATION_FEE");
        itemReq2.setItemName("血常规");
        itemReq2.setQuantity(null); // 默认 1
        itemReq2.setUnitPrice(new BigDecimal("170.00"));

        req.setItems(List.of(itemReq1, itemReq2));

        when(paymentRecordRepository.save(any(PaymentRecordEntity.class)))
                .thenAnswer(inv -> {
                    PaymentRecordEntity p = inv.getArgument(0);
                    p.setId(1L);
                    return p;
                });
        when(paymentItemRepository.saveAll(anyList()))
                .thenAnswer(inv -> {
                    List<PaymentItemEntity> items = inv.getArgument(0);
                    for (int i = 0; i < items.size(); i++) {
                        items.get(i).setId((long) (i + 1));
                    }
                    return items;
                });
        PaymentRecordResponse expected = new PaymentRecordResponse();
        expected.setId(1L);
        when(converter.toPaymentResponse(any(PaymentRecordEntity.class), anyList())).thenReturn(expected);

        Result<PaymentRecordResponse> result = service.create(req, 300L, "操作员");

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());

        // 验证保存的缴费记录
        ArgumentCaptor<PaymentRecordEntity> payCaptor = ArgumentCaptor.forClass(PaymentRecordEntity.class);
        verify(paymentRecordRepository).save(payCaptor.capture());
        PaymentRecordEntity savedPay = payCaptor.getValue();
        assertEquals(100L, savedPay.getPatientId());
        assertEquals("张三", savedPay.getPatientName());
        assertEquals(500L, savedPay.getSourceId());
        assertEquals("MEDICAL_ORDER", savedPay.getSourceType());
        assertEquals("ORDER001", savedPay.getSourceNo());
        assertEquals(new BigDecimal("200.00"), savedPay.getTotalAmount());
        assertEquals(BigDecimal.ZERO, savedPay.getPaidAmount());
        assertEquals(BigDecimal.ZERO, savedPay.getRefundAmount());
        assertEquals("PENDING", savedPay.getStatus());
        assertEquals(300L, savedPay.getOperatorId());
        assertEquals("操作员", savedPay.getOperatorName());
        assertNotNull(savedPay.getPaymentNo());
        assertTrue(savedPay.getPaymentNo().startsWith("PAY"));

        // 验证保存的明细及金额计算
        ArgumentCaptor<List<PaymentItemEntity>> itemCaptor = ArgumentCaptor.forClass(List.class);
        verify(paymentItemRepository).saveAll(itemCaptor.capture());
        List<PaymentItemEntity> savedItems = itemCaptor.getValue();
        assertEquals(2, savedItems.size());

        PaymentItemEntity i1 = savedItems.get(0);
        assertEquals(1L, i1.getPaymentId());
        assertEquals("DRUG_FEE", i1.getItemType());
        assertEquals(new BigDecimal("2"), i1.getQuantity());
        assertEquals(new BigDecimal("15.00"), i1.getUnitPrice());
        assertEquals(new BigDecimal("30.00"), i1.getAmount());

        PaymentItemEntity i2 = savedItems.get(1);
        assertEquals(BigDecimal.ONE, i2.getQuantity()); // null -> ONE
        assertEquals(new BigDecimal("170.00"), i2.getUnitPrice());
        assertEquals(new BigDecimal("170.00"), i2.getAmount());
    }

    // ==================== pay ====================

    @Test
    void payShouldFailWhenPaymentNotFound() {
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.empty());

        PayRequest req = new PayRequest();
        req.setPaymentMethod("CASH");
        req.setPaidAmount(new BigDecimal("100.00"));

        Result<PaymentRecordResponse> result = service.pay(1L, req);

        assertEquals(WindowErrorCode.PAYMENT_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).saveAndFlush(any());
    }

    @Test
    void payShouldFailWhenAlreadyPaid() {
        PaymentRecordEntity entity = buildPayment(1L, "PAID", new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(entity));

        PayRequest req = new PayRequest();
        req.setPaymentMethod("CASH");
        req.setPaidAmount(new BigDecimal("100.00"));

        Result<PaymentRecordResponse> result = service.pay(1L, req);

        assertEquals(WindowErrorCode.PAYMENT_ALREADY_PAID.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).saveAndFlush(any());
    }

    @Test
    void payShouldFailWhenStateNotPending() {
        PaymentRecordEntity entity = buildPayment(1L, "REFUNDED", new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(entity));

        PayRequest req = new PayRequest();
        req.setPaymentMethod("CASH");
        req.setPaidAmount(new BigDecimal("100.00"));

        Result<PaymentRecordResponse> result = service.pay(1L, req);

        assertEquals(WindowErrorCode.PAYMENT_INVALID_STATE.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).saveAndFlush(any());
    }

    @Test
    void payShouldFailWhenPaidAmountIsNull() {
        PaymentRecordEntity entity = buildPayment(1L, "PENDING", new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(entity));

        PayRequest req = new PayRequest();
        req.setPaymentMethod("CASH");
        req.setPaidAmount(null);

        Result<PaymentRecordResponse> result = service.pay(1L, req);

        assertEquals(WindowErrorCode.PAYMENT_AMOUNT_MISMATCH.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).saveAndFlush(any());
    }

    @Test
    void payShouldFailWhenAmountMismatch() {
        PaymentRecordEntity entity = buildPayment(1L, "PENDING", new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(entity));

        PayRequest req = new PayRequest();
        req.setPaymentMethod("CASH");
        req.setPaidAmount(new BigDecimal("99.00"));

        Result<PaymentRecordResponse> result = service.pay(1L, req);

        assertEquals(WindowErrorCode.PAYMENT_AMOUNT_MISMATCH.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).saveAndFlush(any());
    }

    @Test
    void payShouldSucceedWhenAmountMatches() {
        PaymentRecordEntity entity = buildPayment(1L, "PENDING", new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentRecordRepository.saveAndFlush(any(PaymentRecordEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        List<PaymentItemEntity> items = List.of(new PaymentItemEntity());
        when(paymentItemRepository.findByPaymentId(1L)).thenReturn(items);
        PaymentRecordResponse expected = new PaymentRecordResponse();
        expected.setId(1L);
        when(converter.toPaymentResponse(any(PaymentRecordEntity.class), eq(items))).thenReturn(expected);

        PayRequest req = new PayRequest();
        req.setPaymentMethod("WECHAT");
        req.setPayerName("张三");
        req.setPaidAmount(new BigDecimal("100.00"));

        Result<PaymentRecordResponse> result = service.pay(1L, req);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());

        ArgumentCaptor<PaymentRecordEntity> captor = ArgumentCaptor.forClass(PaymentRecordEntity.class);
        verify(paymentRecordRepository).saveAndFlush(captor.capture());
        PaymentRecordEntity saved = captor.getValue();
        assertEquals("PAID", saved.getStatus());
        assertEquals(new BigDecimal("100.00"), saved.getPaidAmount());
        assertEquals("WECHAT", saved.getPaymentMethod());
        assertEquals("张三", saved.getPayerName());
        assertNotNull(saved.getPaidAt());
    }

    @Test
    void payShouldHandleNullTotalAmount() {
        PaymentRecordEntity entity = buildPayment(1L, "PENDING", null);
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentRecordRepository.saveAndFlush(any(PaymentRecordEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(paymentItemRepository.findByPaymentId(1L)).thenReturn(List.of());
        when(converter.toPaymentResponse(any(PaymentRecordEntity.class), anyList()))
                .thenReturn(new PaymentRecordResponse());

        PayRequest req = new PayRequest();
        req.setPaymentMethod("CASH");
        req.setPaidAmount(BigDecimal.ZERO); // 与默认 ZERO 一致

        Result<PaymentRecordResponse> result = service.pay(1L, req);

        assertEquals("SUCCESS", result.getCode());
    }

    @Test
    void payShouldReturnConflictWhenOptimisticLockOccurs() {
        PaymentRecordEntity entity = buildPayment(1L, "PENDING", new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentRecordRepository.saveAndFlush(any(PaymentRecordEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        PayRequest req = new PayRequest();
        req.setPaymentMethod("CASH");
        req.setPaidAmount(new BigDecimal("100.00"));

        Result<PaymentRecordResponse> result = service.pay(1L, req);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    // ==================== refund ====================

    @Test
    void refundShouldFailWhenPaymentNotFound() {
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.empty());

        RefundRequest req = new RefundRequest();
        req.setRefundReason("误缴");

        Result<PaymentRecordResponse> result = service.refund(1L, req);

        assertEquals(WindowErrorCode.PAYMENT_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).saveAndFlush(any());
    }

    @Test
    void refundShouldFailWhenAlreadyRefunded() {
        PaymentRecordEntity entity = buildPayment(1L, "REFUNDED", new BigDecimal("100.00"));
        entity.setPaidAmount(new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(entity));

        RefundRequest req = new RefundRequest();
        req.setRefundReason("重复退费");

        Result<PaymentRecordResponse> result = service.refund(1L, req);

        assertEquals(WindowErrorCode.PAYMENT_ALREADY_REFUNDED.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).saveAndFlush(any());
    }

    @Test
    void refundShouldFailWhenNotPaid() {
        PaymentRecordEntity entity = buildPayment(1L, "PENDING", new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(entity));

        RefundRequest req = new RefundRequest();
        req.setRefundReason("未支付退费");

        Result<PaymentRecordResponse> result = service.refund(1L, req);

        assertEquals(WindowErrorCode.REFUND_NOT_PAID.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).saveAndFlush(any());
    }

    @Test
    void refundShouldSucceedWhenPaid() {
        PaymentRecordEntity entity = buildPayment(1L, "PAID", new BigDecimal("100.00"));
        entity.setPaidAmount(new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentRecordRepository.saveAndFlush(any(PaymentRecordEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(paymentItemRepository.findByPaymentId(1L)).thenReturn(List.of());
        PaymentRecordResponse expected = new PaymentRecordResponse();
        expected.setId(1L);
        when(converter.toPaymentResponse(any(PaymentRecordEntity.class), anyList())).thenReturn(expected);

        RefundRequest req = new RefundRequest();
        req.setRefundReason("误缴");

        Result<PaymentRecordResponse> result = service.refund(1L, req);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());

        ArgumentCaptor<PaymentRecordEntity> captor = ArgumentCaptor.forClass(PaymentRecordEntity.class);
        verify(paymentRecordRepository).saveAndFlush(captor.capture());
        PaymentRecordEntity saved = captor.getValue();
        assertEquals("REFUNDED", saved.getStatus());
        assertEquals(new BigDecimal("100.00"), saved.getRefundAmount());
        assertEquals("误缴", saved.getRefundReason());
        assertNotNull(saved.getRefundedAt());
    }

    @Test
    void refundShouldHandleNullPaidAmount() {
        PaymentRecordEntity entity = buildPayment(1L, "PAID", new BigDecimal("100.00"));
        entity.setPaidAmount(null);
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentRecordRepository.saveAndFlush(any(PaymentRecordEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(paymentItemRepository.findByPaymentId(1L)).thenReturn(List.of());
        when(converter.toPaymentResponse(any(PaymentRecordEntity.class), anyList()))
                .thenReturn(new PaymentRecordResponse());

        RefundRequest req = new RefundRequest();
        req.setRefundReason("误缴");

        Result<PaymentRecordResponse> result = service.refund(1L, req);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<PaymentRecordEntity> captor = ArgumentCaptor.forClass(PaymentRecordEntity.class);
        verify(paymentRecordRepository).saveAndFlush(captor.capture());
        assertEquals(BigDecimal.ZERO, captor.getValue().getRefundAmount());
    }

    @Test
    void refundShouldReturnConflictWhenOptimisticLockOccurs() {
        PaymentRecordEntity entity = buildPayment(1L, "PAID", new BigDecimal("100.00"));
        entity.setPaidAmount(new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentRecordRepository.saveAndFlush(any(PaymentRecordEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        RefundRequest req = new RefundRequest();
        req.setRefundReason("误缴");

        Result<PaymentRecordResponse> result = service.refund(1L, req);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    // ==================== getById ====================

    @Test
    void getByIdShouldFailWhenNotFound() {
        when(paymentRecordRepository.findById(5L)).thenReturn(Optional.empty());

        Result<PaymentRecordResponse> result = service.getById(5L);

        assertEquals(WindowErrorCode.PAYMENT_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentItemRepository, never()).findByPaymentId(any());
    }

    @Test
    void getByIdShouldReturnResponseWithItemsWhenFound() {
        PaymentRecordEntity entity = buildPayment(5L, "PAID", new BigDecimal("100.00"));
        when(paymentRecordRepository.findById(5L)).thenReturn(Optional.of(entity));
        List<PaymentItemEntity> items = List.of(new PaymentItemEntity());
        when(paymentItemRepository.findByPaymentId(5L)).thenReturn(items);
        PaymentRecordResponse expected = new PaymentRecordResponse();
        expected.setId(5L);
        when(converter.toPaymentResponse(entity, items)).thenReturn(expected);

        Result<PaymentRecordResponse> result = service.getById(5L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(5L, result.getData().getId());
    }

    // ==================== query ====================

    @Test
    void queryShouldApplyDefaultsWhenPageAndSizeNull() {
        PaymentQueryRequest req = new PaymentQueryRequest();
        req.setPage(null);
        req.setSize(null);

        Page<PaymentRecordEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(paymentRecordRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        Result<Page<PaymentRecordResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
        // 空列表时不应调用 findByPaymentIdIn
        verify(paymentItemRepository, never()).findByPaymentIdIn(any());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(paymentRecordRepository).findAll(any(Specification.class), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
    }

    @Test
    void queryShouldLoadItemsAndMapResultsWhenNonEmpty() {
        PaymentQueryRequest req = new PaymentQueryRequest();
        req.setPatientId(100L);
        req.setStatus("PAID");
        req.setSourceType("REGISTRATION");
        req.setPage(0);
        req.setSize(10);

        PaymentRecordEntity entity = buildPayment(1L, "PAID", new BigDecimal("100.00"));
        Page<PaymentRecordEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        when(paymentRecordRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        PaymentItemEntity item = new PaymentItemEntity();
        item.setPaymentId(1L);
        when(paymentItemRepository.findByPaymentIdIn(any())).thenReturn(List.of(item));
        PaymentRecordResponse resp = new PaymentRecordResponse();
        resp.setId(1L);
        when(converter.toPaymentResponse(any(PaymentRecordEntity.class), anyList())).thenReturn(resp);

        Result<Page<PaymentRecordResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getContent().size());
        assertEquals(1L, result.getData().getContent().get(0).getId());
    }

    // ==================== queryByPatient ====================

    @Test
    void queryByPatientShouldReturnEmptyListWhenNoRecords() {
        when(paymentRecordRepository.findByPatientIdOrderByCreatedAtDesc(100L)).thenReturn(List.of());

        Result<List<PaymentRecordResponse>> result = service.queryByPatient(100L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
        verify(paymentItemRepository, never()).findByPaymentIdIn(any());
    }

    @Test
    void queryByPatientShouldGroupItemsAndReturnResponses() {
        PaymentRecordEntity e1 = buildPayment(1L, "PAID", new BigDecimal("100.00"));
        PaymentRecordEntity e2 = buildPayment(2L, "PENDING", new BigDecimal("50.00"));
        when(paymentRecordRepository.findByPatientIdOrderByCreatedAtDesc(100L))
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

        Result<List<PaymentRecordResponse>> result = service.queryByPatient(100L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
        assertEquals(1L, result.getData().get(0).getId());
        assertEquals(2L, result.getData().get(1).getId());
    }

    // ==================== helpers ====================

    private PaymentRecordEntity buildPayment(Long id, String status, BigDecimal totalAmount) {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setId(id);
        entity.setPaymentNo("PAY" + id);
        entity.setPatientId(100L);
        entity.setPatientName("张三");
        entity.setTotalAmount(totalAmount);
        entity.setStatus(status);
        return entity;
    }
}
