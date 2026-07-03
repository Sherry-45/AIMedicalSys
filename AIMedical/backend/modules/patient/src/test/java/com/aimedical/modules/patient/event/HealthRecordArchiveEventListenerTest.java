package com.aimedical.modules.patient.event;

import com.aimedical.modules.commonmodule.event.HealthRecordArchiveEvent;
import com.aimedical.modules.commonmodule.event.HealthRecordArchiveEvent.Type;
import com.aimedical.modules.patient.entity.HealthRecordCategory;
import com.aimedical.modules.patient.entity.HealthRecordEntity;
import com.aimedical.modules.patient.entity.HealthRecordType;
import com.aimedical.modules.patient.repository.HealthRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HealthRecordArchiveEventListener} 单元测试。
 *
 * <p>覆盖 4 种事件类型归档、null 跳过、异常捕获、边界字段处理等场景，
 * 确保 window/pharmacy 模块发布的健康档案事件能被正确归档。
 */
@ExtendWith(MockitoExtension.class)
class HealthRecordArchiveEventListenerTest {

    @Mock private HealthRecordRepository healthRecordRepository;

    private HealthRecordArchiveEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new HealthRecordArchiveEventListener(healthRecordRepository);
    }

    @Test
    void shouldArchivePaymentPaidEvent() {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 7, 4, 10, 30, 0);
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                1L, "李明", Type.PAYMENT_PAID, 100L, "PAY20260704001",
                "ORG001", "线下窗口", 15000L, "线下缴费", occurredAt);

        listener.onHealthRecordArchive(event);

        ArgumentCaptor<HealthRecordEntity> captor = ArgumentCaptor.forClass(HealthRecordEntity.class);
        verify(healthRecordRepository).save(captor.capture());
        HealthRecordEntity saved = captor.getValue();
        assertEquals(1L, saved.getPatientId());
        assertEquals(HealthRecordType.PAYMENT.getCode(), saved.getRecordType());
        assertEquals(HealthRecordCategory.OUTPATIENT.getCode(), saved.getRecordCategory());
        assertEquals("线下缴费-PAY20260704001", saved.getTitle());
        assertEquals("线下窗口", saved.getOrganization());
        assertEquals(100L, saved.getSourceId());
        assertEquals(occurredAt.toLocalDate(), saved.getRecordDate());
        assertNotNull(saved.getContent());
        assertEquals("线下缴费，金额：150.00元", saved.getContent());
    }

    @Test
    void shouldArchivePaymentRefundedEvent() {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                2L, "张三", Type.PAYMENT_REFUNDED, 200L, "REF20260704002",
                "ORG001", "线下窗口", 8000L, "线下退费", LocalDateTime.now());

        listener.onHealthRecordArchive(event);

        ArgumentCaptor<HealthRecordEntity> captor = ArgumentCaptor.forClass(HealthRecordEntity.class);
        verify(healthRecordRepository).save(captor.capture());
        HealthRecordEntity saved = captor.getValue();
        assertEquals(HealthRecordType.PAYMENT.getCode(), saved.getRecordType());
        assertEquals("线下退费-REF20260704002", saved.getTitle());
        assertEquals("线下退费，金额：80.00元", saved.getContent());
    }

    @Test
    void shouldArchiveDispensedEvent() {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                3L, "王五", Type.DISPENSED, 300L, "DSP20260704003",
                "ORG002", "门诊药房", null, "阿莫西林 x2", LocalDateTime.now());

        listener.onHealthRecordArchive(event);

        ArgumentCaptor<HealthRecordEntity> captor = ArgumentCaptor.forClass(HealthRecordEntity.class);
        verify(healthRecordRepository).save(captor.capture());
        HealthRecordEntity saved = captor.getValue();
        assertEquals(HealthRecordType.DISPENSING.getCode(), saved.getRecordType());
        assertEquals("药房发药-DSP20260704003", saved.getTitle());
        assertEquals("阿莫西林 x2", saved.getContent());
        assertEquals("门诊药房", saved.getOrganization());
    }

    @Test
    void shouldArchiveRefundedEvent() {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                4L, "赵六", Type.REFUNDED, 400L, "RFD20260704004",
                "ORG002", "门诊药房", null, "退药：布洛芬 x1", LocalDateTime.now());

        listener.onHealthRecordArchive(event);

        ArgumentCaptor<HealthRecordEntity> captor = ArgumentCaptor.forClass(HealthRecordEntity.class);
        verify(healthRecordRepository).save(captor.capture());
        HealthRecordEntity saved = captor.getValue();
        assertEquals(HealthRecordType.DISPENSING.getCode(), saved.getRecordType());
        assertEquals("药房退药-RFD20260704004", saved.getTitle());
        assertEquals("退药：布洛芬 x1", saved.getContent());
    }

    @Test
    void shouldSkipWhenEventIsNull() {
        listener.onHealthRecordArchive(null);
        verify(healthRecordRepository, never()).save(any());
    }

    @Test
    void shouldSkipWhenTypeIsNull() {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                1L, "李明", null, 100L, "PAY001",
                "ORG001", "线下窗口", 1000L, "摘要", LocalDateTime.now());

        listener.onHealthRecordArchive(event);
        verify(healthRecordRepository, never()).save(any());
    }

    @Test
    void shouldSkipWhenPatientIdIsNull() {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                null, "李明", Type.PAYMENT_PAID, 100L, "PAY001",
                "ORG001", "线下窗口", 1000L, "摘要", LocalDateTime.now());

        listener.onHealthRecordArchive(event);
        verify(healthRecordRepository, never()).save(any());
    }

    @Test
    void shouldCatchExceptionAndNotPropagate() {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                1L, "李明", Type.PAYMENT_PAID, 100L, "PAY001",
                "ORG001", "线下窗口", 1000L, "摘要", LocalDateTime.now());
        when(healthRecordRepository.save(any(HealthRecordEntity.class)))
                .thenThrow(new RuntimeException("DB connection lost"));

        // 不应抛出异常
        listener.onHealthRecordArchive(event);
        verify(healthRecordRepository).save(any(HealthRecordEntity.class));
    }

    @Test
    void shouldHandleNullAmountForPaymentEvent() {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                1L, "李明", Type.PAYMENT_PAID, 100L, "PAY001",
                "ORG001", "线下窗口", null, "线下缴费", LocalDateTime.now());

        listener.onHealthRecordArchive(event);

        ArgumentCaptor<HealthRecordEntity> captor = ArgumentCaptor.forClass(HealthRecordEntity.class);
        verify(healthRecordRepository).save(captor.capture());
        HealthRecordEntity saved = captor.getValue();
        // amount 为 null 时，content 只含 summary
        assertEquals("线下缴费", saved.getContent());
    }

    @Test
    void shouldHandleNullSummaryForPaymentEvent() {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                1L, "李明", Type.PAYMENT_PAID, 100L, "PAY001",
                "ORG001", "线下窗口", 2000L, null, LocalDateTime.now());

        listener.onHealthRecordArchive(event);

        ArgumentCaptor<HealthRecordEntity> captor = ArgumentCaptor.forClass(HealthRecordEntity.class);
        verify(healthRecordRepository).save(captor.capture());
        HealthRecordEntity saved = captor.getValue();
        // summary 为 null 时，content 只含金额
        assertEquals("金额：20.00元", saved.getContent());
    }

    @Test
    void shouldHandleNullSummaryAndNullAmountForPaymentEvent() {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                1L, "李明", Type.PAYMENT_PAID, 100L, "PAY001",
                "ORG001", "线下窗口", null, null, LocalDateTime.now());

        listener.onHealthRecordArchive(event);

        ArgumentCaptor<HealthRecordEntity> captor = ArgumentCaptor.forClass(HealthRecordEntity.class);
        verify(healthRecordRepository).save(captor.capture());
        HealthRecordEntity saved = captor.getValue();
        assertNull(saved.getContent());
    }

    @Test
    void shouldUseNowWhenOccurredAtIsNull() {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                1L, "李明", Type.DISPENSED, 100L, "DSP001",
                "ORG001", "药房", null, "发药", null);

        listener.onHealthRecordArchive(event);

        ArgumentCaptor<HealthRecordEntity> captor = ArgumentCaptor.forClass(HealthRecordEntity.class);
        verify(healthRecordRepository).save(captor.capture());
        HealthRecordEntity saved = captor.getValue();
        // occurredAt 为 null 时，recordDate 用 LocalDate.now()
        assertNotNull(saved.getRecordDate());
        assertEquals(java.time.LocalDate.now(), saved.getRecordDate());
    }

    @Test
    void shouldHandleNullRecordNoInTitle() {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                1L, "李明", Type.DISPENSED, 100L, null,
                "ORG001", "药房", null, "发药摘要", LocalDateTime.now());

        listener.onHealthRecordArchive(event);

        ArgumentCaptor<HealthRecordEntity> captor = ArgumentCaptor.forClass(HealthRecordEntity.class);
        verify(healthRecordRepository).save(captor.capture());
        HealthRecordEntity saved = captor.getValue();
        // recordNo 为 null 时，title 后缀为空字符串
        assertEquals("药房发药-", saved.getTitle());
    }

    @Test
    void shouldHandleNullSummaryForDispensingEvent() {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent(
                1L, "李明", Type.DISPENSED, 100L, "DSP001",
                "ORG001", "药房", null, null, LocalDateTime.now());

        listener.onHealthRecordArchive(event);

        ArgumentCaptor<HealthRecordEntity> captor = ArgumentCaptor.forClass(HealthRecordEntity.class);
        verify(healthRecordRepository).save(captor.capture());
        HealthRecordEntity saved = captor.getValue();
        assertNull(saved.getContent());
    }
}
