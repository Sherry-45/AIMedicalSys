package com.aimedical.modules.pharmacy.enums;

import com.aimedical.common.base.BaseEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DispensingStatusTest {

    @Test
    void shouldDefineFourStatuses() {
        assertEquals(4, DispensingStatus.values().length);
        assertNotNull(DispensingStatus.valueOf("PENDING"));
        assertNotNull(DispensingStatus.valueOf("DISPENSED"));
        assertNotNull(DispensingStatus.valueOf("REFUNDED"));
        assertNotNull(DispensingStatus.valueOf("CANCELLED"));
    }

    @Test
    void shouldExposeCodeAndDescForPending() {
        assertEquals("PENDING", DispensingStatus.PENDING.getCode());
        assertEquals("待发药", DispensingStatus.PENDING.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForDispensed() {
        assertEquals("DISPENSED", DispensingStatus.DISPENSED.getCode());
        assertEquals("已发药", DispensingStatus.DISPENSED.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForRefunded() {
        assertEquals("REFUNDED", DispensingStatus.REFUNDED.getCode());
        assertEquals("已退药", DispensingStatus.REFUNDED.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForCancelled() {
        assertEquals("CANCELLED", DispensingStatus.CANCELLED.getCode());
        assertEquals("已取消", DispensingStatus.CANCELLED.getDesc());
    }

    @Test
    void fromCodeShouldResolveAllKnownCodes() {
        assertEquals(DispensingStatus.PENDING, fromCode("PENDING"));
        assertEquals(DispensingStatus.DISPENSED, fromCode("DISPENSED"));
        assertEquals(DispensingStatus.REFUNDED, fromCode("REFUNDED"));
        assertEquals(DispensingStatus.CANCELLED, fromCode("CANCELLED"));
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
        for (DispensingStatus status : DispensingStatus.values()) {
            assertInstanceOf(BaseEnum.class, status);
            assertNotNull(status.getCode());
            assertNotNull(status.getDesc());
        }
    }

    /** Helper mirroring a typical BaseEnum.fromCode lookup, exercising getCode() for every value. */
    private DispensingStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (DispensingStatus status : DispensingStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
