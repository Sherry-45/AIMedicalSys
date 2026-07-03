package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentItemTypeTest {

    @Test
    void shouldDefineFiveTypes() {
        assertEquals(5, PaymentItemType.values().length);
        assertNotNull(PaymentItemType.valueOf("REGISTRATION_FEE"));
        assertNotNull(PaymentItemType.valueOf("DRUG_FEE"));
        assertNotNull(PaymentItemType.valueOf("EXAMINATION_FEE"));
        assertNotNull(PaymentItemType.valueOf("LAB_TEST_FEE"));
        assertNotNull(PaymentItemType.valueOf("OTHER"));
    }

    @Test
    void shouldExposeCodeAndDescForRegistrationFee() {
        assertEquals("REGISTRATION_FEE", PaymentItemType.REGISTRATION_FEE.getCode());
        assertEquals("挂号费", PaymentItemType.REGISTRATION_FEE.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForDrugFee() {
        assertEquals("DRUG_FEE", PaymentItemType.DRUG_FEE.getCode());
        assertEquals("药品费", PaymentItemType.DRUG_FEE.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForExaminationFee() {
        assertEquals("EXAMINATION_FEE", PaymentItemType.EXAMINATION_FEE.getCode());
        assertEquals("检查费", PaymentItemType.EXAMINATION_FEE.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForLabTestFee() {
        assertEquals("LAB_TEST_FEE", PaymentItemType.LAB_TEST_FEE.getCode());
        assertEquals("化验费", PaymentItemType.LAB_TEST_FEE.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForOther() {
        assertEquals("OTHER", PaymentItemType.OTHER.getCode());
        assertEquals("其他", PaymentItemType.OTHER.getDesc());
    }

    @Test
    void shouldImplementBaseEnum() {
        for (PaymentItemType type : PaymentItemType.values()) {
            assertInstanceOf(BaseEnum.class, type);
            assertNotNull(type.getCode());
            assertNotNull(type.getDesc());
        }
    }
}
