package com.aimedical.modules.window.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.window.WindowErrorCode;
import com.aimedical.modules.window.converter.WindowConverter;
import com.aimedical.modules.window.dto.OfflineRegistrationCancelRequest;
import com.aimedical.modules.window.dto.OfflineRegistrationCreateRequest;
import com.aimedical.modules.window.dto.OfflineRegistrationQueryRequest;
import com.aimedical.modules.window.dto.OfflineRegistrationResponse;
import com.aimedical.modules.window.dto.PaymentRecordResponse;
import com.aimedical.modules.window.dto.RefundRequest;
import com.aimedical.modules.window.entity.OfflineRegistrationEntity;
import com.aimedical.modules.window.entity.PaymentItemEntity;
import com.aimedical.modules.window.entity.PaymentRecordEntity;
import com.aimedical.modules.window.entity.PaymentSourceType;
import com.aimedical.modules.window.entity.PaymentStatus;
import com.aimedical.modules.window.repository.OfflineRegistrationRepository;
import com.aimedical.modules.window.repository.PaymentItemRepository;
import com.aimedical.modules.window.repository.PaymentRecordRepository;
import com.aimedical.modules.window.service.PaymentService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link OfflineRegistrationServiceImpl} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class OfflineRegistrationServiceImplTest {

    @Mock private OfflineRegistrationRepository registrationRepository;
    @Mock private PaymentRecordRepository paymentRecordRepository;
    @Mock private PaymentItemRepository paymentItemRepository;
    @Mock private PaymentService paymentService;
    @Mock private WindowConverter converter;

    private OfflineRegistrationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OfflineRegistrationServiceImpl(
                registrationRepository, paymentRecordRepository, paymentItemRepository,
                paymentService, converter);
    }

    // ==================== create ====================

    @Test
    void createShouldSaveRegistrationAndGeneratePayment() {
        OfflineRegistrationCreateRequest req = new OfflineRegistrationCreateRequest();
        req.setPatientId(100L);
        req.setPatientName("张三");
        req.setPatientPhone("13800000000");
        req.setIdCard("110101199001011234");
        req.setDoctorId(200L);
        req.setDoctorName("李医生");
        req.setDepartment("内科");
        req.setRegistrationType("EMERGENCY");
        req.setRegistrationFee(new BigDecimal("50.00"));
        req.setRemark("备注");

        when(registrationRepository.saveAndFlush(any(OfflineRegistrationEntity.class)))
                .thenAnswer(inv -> {
                    OfflineRegistrationEntity e = inv.getArgument(0);
                    e.setId(1L);
                    return e;
                });
        when(paymentRecordRepository.save(any(PaymentRecordEntity.class)))
                .thenAnswer(inv -> {
                    PaymentRecordEntity p = inv.getArgument(0);
                    p.setId(10L);
                    return p;
                });
        OfflineRegistrationResponse expected = new OfflineRegistrationResponse();
        expected.setId(1L);
        when(converter.toRegistrationResponse(any(OfflineRegistrationEntity.class))).thenReturn(expected);

        Result<OfflineRegistrationResponse> result = service.create(req, 300L, "操作员");

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());

        // 验证保存的挂号实体字段
        ArgumentCaptor<OfflineRegistrationEntity> regCaptor = ArgumentCaptor.forClass(OfflineRegistrationEntity.class);
        verify(registrationRepository).saveAndFlush(regCaptor.capture());
        OfflineRegistrationEntity savedReg = regCaptor.getValue();
        assertEquals(100L, savedReg.getPatientId());
        assertEquals("张三", savedReg.getPatientName());
        assertEquals("EMERGENCY", savedReg.getRegistrationType());
        assertEquals("ACTIVE", savedReg.getStatus());
        assertEquals(new BigDecimal("50.00"), savedReg.getRegistrationFee());
        assertEquals(300L, savedReg.getOperatorId());
        assertEquals("操作员", savedReg.getOperatorName());
        assertNotNull(savedReg.getRegistrationNo());
        assertTrue(savedReg.getRegistrationNo().startsWith("OFR"));

        // 验证同步生成的缴费记录
        ArgumentCaptor<PaymentRecordEntity> payCaptor = ArgumentCaptor.forClass(PaymentRecordEntity.class);
        verify(paymentRecordRepository).save(payCaptor.capture());
        PaymentRecordEntity savedPay = payCaptor.getValue();
        assertEquals(100L, savedPay.getPatientId());
        assertEquals(1L, savedPay.getSourceId());
        assertEquals(PaymentSourceType.REGISTRATION.getCode(), savedPay.getSourceType());
        assertEquals(new BigDecimal("50.00"), savedPay.getTotalAmount());
        assertEquals(BigDecimal.ZERO, savedPay.getPaidAmount());
        assertEquals("PENDING", savedPay.getStatus());

        // 验证生成的挂号费明细
        ArgumentCaptor<PaymentItemEntity> itemCaptor = ArgumentCaptor.forClass(PaymentItemEntity.class);
        verify(paymentItemRepository).save(itemCaptor.capture());
        PaymentItemEntity savedItem = itemCaptor.getValue();
        assertEquals(10L, savedItem.getPaymentId());
        assertEquals("REGISTRATION_FEE", savedItem.getItemType());
        assertEquals("挂号费", savedItem.getItemName());
        assertEquals(BigDecimal.ONE, savedItem.getQuantity());
        assertEquals(new BigDecimal("50.00"), savedItem.getUnitPrice());
        assertEquals(new BigDecimal("50.00"), savedItem.getAmount());
    }

    @Test
    void createShouldDefaultRegistrationTypeAndFeeWhenAbsent() {
        OfflineRegistrationCreateRequest req = new OfflineRegistrationCreateRequest();
        req.setPatientId(100L);
        req.setPatientName("张三");
        req.setRegistrationType("");  // blank
        req.setRegistrationFee(null); // null

        when(registrationRepository.saveAndFlush(any(OfflineRegistrationEntity.class)))
                .thenAnswer(inv -> {
                    OfflineRegistrationEntity e = inv.getArgument(0);
                    e.setId(1L);
                    return e;
                });
        when(paymentRecordRepository.save(any(PaymentRecordEntity.class)))
                .thenAnswer(inv -> {
                    PaymentRecordEntity p = inv.getArgument(0);
                    p.setId(10L);
                    return p;
                });
        when(converter.toRegistrationResponse(any(OfflineRegistrationEntity.class)))
                .thenReturn(new OfflineRegistrationResponse());

        service.create(req, 300L, "操作员");

        ArgumentCaptor<OfflineRegistrationEntity> regCaptor = ArgumentCaptor.forClass(OfflineRegistrationEntity.class);
        verify(registrationRepository).saveAndFlush(regCaptor.capture());
        OfflineRegistrationEntity savedReg = regCaptor.getValue();
        assertEquals("OUTPATIENT", savedReg.getRegistrationType());
        assertEquals(BigDecimal.ZERO, savedReg.getRegistrationFee());
    }

    @Test
    void createShouldReturnConflictWhenOptimisticLockOccurs() {
        OfflineRegistrationCreateRequest req = new OfflineRegistrationCreateRequest();
        req.setPatientId(100L);
        req.setPatientName("张三");

        when(registrationRepository.saveAndFlush(any(OfflineRegistrationEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<OfflineRegistrationResponse> result = service.create(req, 300L, "操作员");

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
        verify(paymentRecordRepository, never()).save(any());
        verify(paymentItemRepository, never()).save(any());
        verify(converter, never()).toRegistrationResponse(any());
    }

    // ==================== cancel ====================

    @Test
    void cancelShouldFailWhenRegistrationNotFound() {
        when(registrationRepository.findById(1L)).thenReturn(Optional.empty());

        OfflineRegistrationCancelRequest req = new OfflineRegistrationCancelRequest();
        req.setCancelReason("不想看了");

        Result<OfflineRegistrationResponse> result = service.cancel(1L, req);

        assertEquals(WindowErrorCode.REGISTRATION_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
        verify(registrationRepository, never()).saveAndFlush(any());
    }

    @Test
    void cancelShouldFailWhenAlreadyCancelled() {
        OfflineRegistrationEntity entity = buildRegistration(1L, "CANCELLED");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(entity));

        OfflineRegistrationCancelRequest req = new OfflineRegistrationCancelRequest();
        req.setCancelReason("重复取消");

        Result<OfflineRegistrationResponse> result = service.cancel(1L, req);

        assertEquals(WindowErrorCode.REGISTRATION_ALREADY_CANCELLED.getCode(), result.getCode());
        assertNull(result.getData());
        verify(registrationRepository, never()).saveAndFlush(any());
    }

    @Test
    void cancelShouldFailWhenStateNotActive() {
        OfflineRegistrationEntity entity = buildRegistration(1L, "UNKNOWN_STATE");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(entity));

        OfflineRegistrationCancelRequest req = new OfflineRegistrationCancelRequest();
        req.setCancelReason("取消");

        Result<OfflineRegistrationResponse> result = service.cancel(1L, req);

        assertEquals(WindowErrorCode.REGISTRATION_INVALID_STATE.getCode(), result.getCode());
        assertNull(result.getData());
        verify(registrationRepository, never()).saveAndFlush(any());
    }

    @Test
    void cancelShouldSucceedWhenActive() {
        OfflineRegistrationEntity entity = buildRegistration(1L, "ACTIVE");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(registrationRepository.saveAndFlush(any(OfflineRegistrationEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        OfflineRegistrationResponse expected = new OfflineRegistrationResponse();
        expected.setId(1L);
        when(converter.toRegistrationResponse(any(OfflineRegistrationEntity.class))).thenReturn(expected);

        OfflineRegistrationCancelRequest req = new OfflineRegistrationCancelRequest();
        req.setCancelReason("不想看了");

        Result<OfflineRegistrationResponse> result = service.cancel(1L, req);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());

        ArgumentCaptor<OfflineRegistrationEntity> captor = ArgumentCaptor.forClass(OfflineRegistrationEntity.class);
        verify(registrationRepository).saveAndFlush(captor.capture());
        OfflineRegistrationEntity saved = captor.getValue();
        assertEquals("CANCELLED", saved.getStatus());
        assertEquals("不想看了", saved.getCancelReason());
        assertNotNull(saved.getCancelTime());
    }

    @Test
    void cancelShouldReturnConflictWhenOptimisticLockOccurs() {
        OfflineRegistrationEntity entity = buildRegistration(1L, "ACTIVE");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(registrationRepository.saveAndFlush(any(OfflineRegistrationEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        OfflineRegistrationCancelRequest req = new OfflineRegistrationCancelRequest();
        req.setCancelReason("取消");

        Result<OfflineRegistrationResponse> result = service.cancel(1L, req);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void cancelShouldRefundWhenAssociatedPaymentPaid() {
        OfflineRegistrationEntity entity = buildRegistration(1L, "ACTIVE");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(entity));
        PaymentRecordEntity payment = buildPayment(10L, "PAID");
        payment.setPaidAmount(new BigDecimal("50.00"));
        when(paymentRecordRepository.findBySourceTypeAndSourceId(
                PaymentSourceType.REGISTRATION.getCode(), 1L)).thenReturn(List.of(payment));
        when(paymentService.refund(eq(10L), any(RefundRequest.class)))
                .thenReturn(Result.success(new PaymentRecordResponse()));
        when(registrationRepository.saveAndFlush(any(OfflineRegistrationEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(converter.toRegistrationResponse(any(OfflineRegistrationEntity.class)))
                .thenReturn(new OfflineRegistrationResponse());

        OfflineRegistrationCancelRequest req = new OfflineRegistrationCancelRequest();
        req.setCancelReason("不想看了");

        Result<OfflineRegistrationResponse> result = service.cancel(1L, req);

        assertEquals("SUCCESS", result.getCode());
        // 验证调用了退费且退费原因包含取消原因
        ArgumentCaptor<RefundRequest> refundCaptor = ArgumentCaptor.forClass(RefundRequest.class);
        verify(paymentService).refund(eq(10L), refundCaptor.capture());
        assertTrue(refundCaptor.getValue().getRefundReason().contains("退号同步退费"));
        assertTrue(refundCaptor.getValue().getRefundReason().contains("不想看了"));
        // 验证挂号状态已取消
        ArgumentCaptor<OfflineRegistrationEntity> regCaptor =
                ArgumentCaptor.forClass(OfflineRegistrationEntity.class);
        verify(registrationRepository).saveAndFlush(regCaptor.capture());
        assertEquals("CANCELLED", regCaptor.getValue().getStatus());
    }

    @Test
    void cancelShouldCancelPendingPayment() {
        OfflineRegistrationEntity entity = buildRegistration(1L, "ACTIVE");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(entity));
        PaymentRecordEntity payment = buildPayment(10L, "PENDING");
        when(paymentRecordRepository.findBySourceTypeAndSourceId(
                PaymentSourceType.REGISTRATION.getCode(), 1L)).thenReturn(List.of(payment));
        when(registrationRepository.saveAndFlush(any(OfflineRegistrationEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(converter.toRegistrationResponse(any(OfflineRegistrationEntity.class)))
                .thenReturn(new OfflineRegistrationResponse());

        OfflineRegistrationCancelRequest req = new OfflineRegistrationCancelRequest();
        req.setCancelReason("不想看了");

        Result<OfflineRegistrationResponse> result = service.cancel(1L, req);

        assertEquals("SUCCESS", result.getCode());
        // 验证待支付缴费记录被置为 CANCELLED
        ArgumentCaptor<PaymentRecordEntity> payCaptor = ArgumentCaptor.forClass(PaymentRecordEntity.class);
        verify(paymentRecordRepository).save(payCaptor.capture());
        assertEquals("CANCELLED", payCaptor.getValue().getStatus());
        // 验证未调用退费
        verify(paymentService, never()).refund(any(), any());
        // 验证挂号状态已取消
        ArgumentCaptor<OfflineRegistrationEntity> regCaptor =
                ArgumentCaptor.forClass(OfflineRegistrationEntity.class);
        verify(registrationRepository).saveAndFlush(regCaptor.capture());
        assertEquals("CANCELLED", regCaptor.getValue().getStatus());
    }

    @Test
    void cancelShouldFailWhenAssociatedPaymentAlreadyRefunded() {
        OfflineRegistrationEntity entity = buildRegistration(1L, "ACTIVE");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(entity));
        PaymentRecordEntity payment = buildPayment(10L, "REFUNDED");
        when(paymentRecordRepository.findBySourceTypeAndSourceId(
                PaymentSourceType.REGISTRATION.getCode(), 1L)).thenReturn(List.of(payment));

        OfflineRegistrationCancelRequest req = new OfflineRegistrationCancelRequest();
        req.setCancelReason("取消");

        Result<OfflineRegistrationResponse> result = service.cancel(1L, req);

        assertEquals(WindowErrorCode.REGISTRATION_PAYMENT_ALREADY_REFUNDED.getCode(), result.getCode());
        assertNull(result.getData());
        verify(registrationRepository, never()).saveAndFlush(any());
        verify(paymentService, never()).refund(any(), any());
    }

    @Test
    void cancelShouldPropagateFailureWhenRefundFails() {
        OfflineRegistrationEntity entity = buildRegistration(1L, "ACTIVE");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(entity));
        PaymentRecordEntity payment = buildPayment(10L, "PAID");
        payment.setPaidAmount(new BigDecimal("50.00"));
        when(paymentRecordRepository.findBySourceTypeAndSourceId(
                PaymentSourceType.REGISTRATION.getCode(), 1L)).thenReturn(List.of(payment));
        when(paymentService.refund(eq(10L), any(RefundRequest.class)))
                .thenReturn(Result.fail(WindowErrorCode.REFUND_NOT_PAID));

        OfflineRegistrationCancelRequest req = new OfflineRegistrationCancelRequest();
        req.setCancelReason("取消");

        Result<OfflineRegistrationResponse> result = service.cancel(1L, req);

        assertEquals(WindowErrorCode.REFUND_NOT_PAID.getCode(), result.getCode());
        assertNull(result.getData());
        verify(registrationRepository, never()).saveAndFlush(any());
    }

    // ==================== getById ====================

    @Test
    void getByIdShouldFailWhenNotFound() {
        when(registrationRepository.findById(5L)).thenReturn(Optional.empty());

        Result<OfflineRegistrationResponse> result = service.getById(5L);

        assertEquals(WindowErrorCode.REGISTRATION_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
        verify(converter, never()).toRegistrationResponse(any());
    }

    @Test
    void getByIdShouldReturnResponseWhenFound() {
        OfflineRegistrationEntity entity = buildRegistration(5L, "ACTIVE");
        when(registrationRepository.findById(5L)).thenReturn(Optional.of(entity));
        OfflineRegistrationResponse expected = new OfflineRegistrationResponse();
        expected.setId(5L);
        when(converter.toRegistrationResponse(entity)).thenReturn(expected);

        Result<OfflineRegistrationResponse> result = service.getById(5L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(5L, result.getData().getId());
    }

    // ==================== query ====================

    @Test
    void queryShouldApplyDefaultPageAndSizeWhenNull() {
        OfflineRegistrationQueryRequest req = new OfflineRegistrationQueryRequest();
        req.setPage(null);
        req.setSize(null);

        Page<OfflineRegistrationEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(registrationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        Result<Page<OfflineRegistrationResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(registrationRepository).findAll(any(Specification.class), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
    }

    @Test
    void queryShouldUseProvidedPageAndSizeAndMapResults() {
        OfflineRegistrationQueryRequest req = new OfflineRegistrationQueryRequest();
        req.setPatientId(100L);
        req.setPatientName("张三");
        req.setStatus("ACTIVE");
        req.setPage(1);
        req.setSize(5);

        OfflineRegistrationEntity entity = buildRegistration(1L, "ACTIVE");
        Page<OfflineRegistrationEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(1, 5), 1);
        when(registrationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        OfflineRegistrationResponse resp = new OfflineRegistrationResponse();
        resp.setId(1L);
        when(converter.toRegistrationResponse(entity)).thenReturn(resp);

        Result<Page<OfflineRegistrationResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getContent().size());
        assertEquals(1L, result.getData().getContent().get(0).getId());
    }

    // ==================== helpers ====================

    private OfflineRegistrationEntity buildRegistration(Long id, String status) {
        OfflineRegistrationEntity entity = new OfflineRegistrationEntity();
        entity.setId(id);
        entity.setRegistrationNo("OFR" + id);
        entity.setPatientId(100L);
        entity.setPatientName("张三");
        entity.setStatus(status);
        return entity;
    }

    private PaymentRecordEntity buildPayment(Long id, String status) {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.setId(id);
        entity.setPaymentNo("PAY" + id);
        entity.setPatientId(100L);
        entity.setPatientName("张三");
        entity.setSourceId(1L);
        entity.setSourceType(PaymentSourceType.REGISTRATION.getCode());
        entity.setStatus(status);
        return entity;
    }
}
