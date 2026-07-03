package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentSourceTypeTest {

    @Test
    void shouldDefineThreeTypes() {
        assertEquals(3, PaymentSourceType.values().length);
        assertNotNull(PaymentSourceType.valueOf("REGISTRATION"));
        assertNotNull(PaymentSourceType.valueOf("MEDICAL_ORDER"));
        assertNotNull(PaymentSourceType.valueOf("DISPENSING"));
    }

    @Test
    void shouldExposeCodeAndDescForRegistration() {
        assertEquals("REGISTRATION", PaymentSourceType.REGISTRATION.getCode());
        assertEquals("挂号", PaymentSourceType.REGISTRATION.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForMedicalOrder() {
        assertEquals("MEDICAL_ORDER", PaymentSourceType.MEDICAL_ORDER.getCode());
        assertEquals("医嘱", PaymentSourceType.MEDICAL_ORDER.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForDispensing() {
        assertEquals("DISPENSING", PaymentSourceType.DISPENSING.getCode());
        assertEquals("发药", PaymentSourceType.DISPENSING.getDesc());
    }

    @Test
    void shouldImplementBaseEnum() {
        for (PaymentSourceType type : PaymentSourceType.values()) {
            assertInstanceOf(BaseEnum.class, type);
            assertNotNull(type.getCode());
            assertNotNull(type.getDesc());
        }
    }
}
