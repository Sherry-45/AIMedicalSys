package com.aimedical.modules.pharmacy.enums;

import com.aimedical.common.base.BaseEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PharmacyRefundStatusTest {

    @Test
    void shouldDefineThreeStatuses() {
        assertEquals(3, PharmacyRefundStatus.values().length);
        assertNotNull(PharmacyRefundStatus.valueOf("PENDING"));
        assertNotNull(PharmacyRefundStatus.valueOf("REFUNDED"));
        assertNotNull(PharmacyRefundStatus.valueOf("REJECTED"));
    }

    @Test
    void shouldExposeCodeAndDescForPending() {
        assertEquals("PENDING", PharmacyRefundStatus.PENDING.getCode());
        assertEquals("待处理", PharmacyRefundStatus.PENDING.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForRefunded() {
        assertEquals("REFUNDED", PharmacyRefundStatus.REFUNDED.getCode());
        assertEquals("已退药", PharmacyRefundStatus.REFUNDED.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForRejected() {
        assertEquals("REJECTED", PharmacyRefundStatus.REJECTED.getCode());
        assertEquals("已驳回", PharmacyRefundStatus.REJECTED.getDesc());
    }

    @Test
    void fromCodeShouldResolveAllKnownCodes() {
        assertEquals(PharmacyRefundStatus.PENDING, fromCode("PENDING"));
        assertEquals(PharmacyRefundStatus.REFUNDED, fromCode("REFUNDED"));
        assertEquals(PharmacyRefundStatus.REJECTED, fromCode("REJECTED"));
    }

    @Test
    void fromCodeShouldReturnNullForUnknownCode() {
        assertNull(fromCode("UNKNOWN"));
    }

    @Test
    void fromCodeShouldReturnNullForNullCode() {
        assertNull(fromCode(null));
    }

    @Test
    void allValuesShouldImplementBaseEnum() {
        for (PharmacyRefundStatus status : PharmacyRefundStatus.values()) {
            assertInstanceOf(BaseEnum.class, status);
            assertNotNull(status.getCode());
            assertNotNull(status.getDesc());
        }
    }

    /** Helper mirroring a typical BaseEnum.fromCode lookup, exercising getCode() for every value. */
    private PharmacyRefundStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PharmacyRefundStatus status : PharmacyRefundStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
