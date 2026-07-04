package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodTest {

    @Test
    void shouldDefineFiveMethods() {
        assertEquals(5, PaymentMethod.values().length);
        assertNotNull(PaymentMethod.valueOf("CASH"));
        assertNotNull(PaymentMethod.valueOf("WECHAT"));
        assertNotNull(PaymentMethod.valueOf("ALIPAY"));
        assertNotNull(PaymentMethod.valueOf("BANK_CARD"));
        assertNotNull(PaymentMethod.valueOf("INSURANCE"));
    }

    @Test
    void shouldExposeCodeAndDescForCash() {
        assertEquals("CASH", PaymentMethod.CASH.getCode());
        assertEquals("现金", PaymentMethod.CASH.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForWechat() {
        assertEquals("WECHAT", PaymentMethod.WECHAT.getCode());
        assertEquals("微信", PaymentMethod.WECHAT.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForAlipay() {
        assertEquals("ALIPAY", PaymentMethod.ALIPAY.getCode());
        assertEquals("支付宝", PaymentMethod.ALIPAY.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForBankCard() {
        assertEquals("BANK_CARD", PaymentMethod.BANK_CARD.getCode());
        assertEquals("银行卡", PaymentMethod.BANK_CARD.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForInsurance() {
        assertEquals("INSURANCE", PaymentMethod.INSURANCE.getCode());
        assertEquals("医保", PaymentMethod.INSURANCE.getDesc());
    }

    @Test
    void shouldImplementBaseEnum() {
        for (PaymentMethod method : PaymentMethod.values()) {
            assertInstanceOf(BaseEnum.class, method);
            assertNotNull(method.getCode());
            assertNotNull(method.getDesc());
        }
    }
}
