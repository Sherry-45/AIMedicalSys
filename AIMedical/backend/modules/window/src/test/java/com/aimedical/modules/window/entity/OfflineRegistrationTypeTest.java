package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfflineRegistrationTypeTest {

    @Test
    void shouldDefineThreeTypes() {
        assertEquals(3, OfflineRegistrationType.values().length);
        assertNotNull(OfflineRegistrationType.valueOf("OUTPATIENT"));
        assertNotNull(OfflineRegistrationType.valueOf("EXAMINATION"));
        assertNotNull(OfflineRegistrationType.valueOf("EMERGENCY"));
    }

    @Test
    void shouldExposeCodeAndDescForOutpatient() {
        assertEquals("OUTPATIENT", OfflineRegistrationType.OUTPATIENT.getCode());
        assertEquals("门诊", OfflineRegistrationType.OUTPATIENT.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForExamination() {
        assertEquals("EXAMINATION", OfflineRegistrationType.EXAMINATION.getCode());
        assertEquals("检查", OfflineRegistrationType.EXAMINATION.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForEmergency() {
        assertEquals("EMERGENCY", OfflineRegistrationType.EMERGENCY.getCode());
        assertEquals("急诊", OfflineRegistrationType.EMERGENCY.getDesc());
    }

    @Test
    void shouldImplementBaseEnum() {
        for (OfflineRegistrationType type : OfflineRegistrationType.values()) {
            assertInstanceOf(BaseEnum.class, type);
            assertNotNull(type.getCode());
            assertNotNull(type.getDesc());
        }
    }
}
