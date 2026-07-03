package com.aimedical.modules.patient.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link HealthRecordCategory} 单元测试。
 */
class HealthRecordCategoryTest {

    @Test
    void shouldDefineFourCategories() {
        assertEquals(4, HealthRecordCategory.values().length);
    }

    @Test
    void shouldExposeCodeAndDescForOutpatient() {
        assertEquals("OUTPATIENT", HealthRecordCategory.OUTPATIENT.getCode());
        assertEquals("门诊", HealthRecordCategory.OUTPATIENT.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForInpatient() {
        assertEquals("INPATIENT", HealthRecordCategory.INPATIENT.getCode());
        assertEquals("住院", HealthRecordCategory.INPATIENT.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForPhysicalExam() {
        assertEquals("PHYSICAL_EXAM", HealthRecordCategory.PHYSICAL_EXAM.getCode());
        assertEquals("体检", HealthRecordCategory.PHYSICAL_EXAM.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForFollowUp() {
        assertEquals("FOLLOW_UP", HealthRecordCategory.FOLLOW_UP.getCode());
        assertEquals("随访", HealthRecordCategory.FOLLOW_UP.getDesc());
    }

    @Test
    void fromCodeShouldReturnMatchingCategory() {
        assertEquals(HealthRecordCategory.OUTPATIENT, HealthRecordCategory.fromCode("OUTPATIENT"));
        assertEquals(HealthRecordCategory.INPATIENT, HealthRecordCategory.fromCode("INPATIENT"));
        assertEquals(HealthRecordCategory.PHYSICAL_EXAM, HealthRecordCategory.fromCode("PHYSICAL_EXAM"));
        assertEquals(HealthRecordCategory.FOLLOW_UP, HealthRecordCategory.fromCode("FOLLOW_UP"));
    }

    @Test
    void fromCodeShouldReturnNullForNullCode() {
        assertNull(HealthRecordCategory.fromCode(null));
    }

    @Test
    void fromCodeShouldReturnNullForUnknownCode() {
        assertNull(HealthRecordCategory.fromCode("NOT_EXIST"));
    }
}
