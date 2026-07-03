package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfflineRegistrationStatusTest {

    @Test
    void shouldDefineTwoStatuses() {
        assertEquals(2, OfflineRegistrationStatus.values().length);
        assertNotNull(OfflineRegistrationStatus.valueOf("ACTIVE"));
        assertNotNull(OfflineRegistrationStatus.valueOf("CANCELLED"));
    }

    @Test
    void shouldExposeCodeAndDescForActive() {
        assertEquals("ACTIVE", OfflineRegistrationStatus.ACTIVE.getCode());
        assertEquals("有效", OfflineRegistrationStatus.ACTIVE.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForCancelled() {
        assertEquals("CANCELLED", OfflineRegistrationStatus.CANCELLED.getCode());
        assertEquals("已取消", OfflineRegistrationStatus.CANCELLED.getDesc());
    }

    @Test
    void shouldImplementBaseEnum() {
        for (OfflineRegistrationStatus status : OfflineRegistrationStatus.values()) {
            assertInstanceOf(BaseEnum.class, status);
            assertNotNull(status.getCode());
            assertNotNull(status.getDesc());
        }
    }
}
