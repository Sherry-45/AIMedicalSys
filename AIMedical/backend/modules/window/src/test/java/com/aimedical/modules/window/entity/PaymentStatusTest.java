package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentStatusTest {

    @Test
    void shouldDefineFiveStatuses() {
        assertEquals(5, PaymentStatus.values().length);
        assertNotNull(PaymentStatus.valueOf("PENDING"));
        assertNotNull(PaymentStatus.valueOf("PAID"));
        assertNotNull(PaymentStatus.valueOf("REFUNDED"));
        assertNotNull(PaymentStatus.valueOf("RECONCILED"));
        assertNotNull(PaymentStatus.valueOf("CANCELLED"));
    }

    @Test
    void shouldExposeCodeAndDescForPending() {
        assertEquals("PENDING", PaymentStatus.PENDING.getCode());
        assertEquals("待支付", PaymentStatus.PENDING.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForPaid() {
        assertEquals("PAID", PaymentStatus.PAID.getCode());
        assertEquals("已支付", PaymentStatus.PAID.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForRefunded() {
        assertEquals("REFUNDED", PaymentStatus.REFUNDED.getCode());
        assertEquals("已退款", PaymentStatus.REFUNDED.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForReconciled() {
        assertEquals("RECONCILED", PaymentStatus.RECONCILED.getCode());
        assertEquals("已对账", PaymentStatus.RECONCILED.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForCancelled() {
        assertEquals("CANCELLED", PaymentStatus.CANCELLED.getCode());
        assertEquals("已取消", PaymentStatus.CANCELLED.getDesc());
    }

    @Test
    void shouldImplementBaseEnum() {
        for (PaymentStatus status : PaymentStatus.values()) {
            assertInstanceOf(BaseEnum.class, status);
            assertNotNull(status.getCode());
            assertNotNull(status.getDesc());
        }
    }
}
