package com.aimedical.modules.patient.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link HealthRecordType} 单元测试。
 */
class HealthRecordTypeTest {

    @Test
    void shouldDefineEightTypes() {
        assertEquals(8, HealthRecordType.values().length);
    }

    @Test
    void shouldExposeCodeAndDescForMedicalRecord() {
        assertEquals("MEDICAL_RECORD", HealthRecordType.MEDICAL_RECORD.getCode());
        assertEquals("病历", HealthRecordType.MEDICAL_RECORD.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForPrescription() {
        assertEquals("PRESCRIPTION", HealthRecordType.PRESCRIPTION.getCode());
        assertEquals("处方", HealthRecordType.PRESCRIPTION.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForExamReport() {
        assertEquals("EXAM_REPORT", HealthRecordType.EXAM_REPORT.getCode());
        assertEquals("检查报告", HealthRecordType.EXAM_REPORT.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForLabTest() {
        assertEquals("LAB_TEST", HealthRecordType.LAB_TEST.getCode());
        assertEquals("检验报告", HealthRecordType.LAB_TEST.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForDispensing() {
        assertEquals("DISPENSING", HealthRecordType.DISPENSING.getCode());
        assertEquals("发药记录", HealthRecordType.DISPENSING.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForPayment() {
        assertEquals("PAYMENT", HealthRecordType.PAYMENT.getCode());
        assertEquals("缴费记录", HealthRecordType.PAYMENT.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForAllergy() {
        assertEquals("ALLERGY", HealthRecordType.ALLERGY.getCode());
        assertEquals("过敏史", HealthRecordType.ALLERGY.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForChronicDisease() {
        assertEquals("CHRONIC_DISEASE", HealthRecordType.CHRONIC_DISEASE.getCode());
        assertEquals("慢病记录", HealthRecordType.CHRONIC_DISEASE.getDesc());
    }

    @Test
    void fromCodeShouldReturnMatchingType() {
        assertEquals(HealthRecordType.MEDICAL_RECORD, HealthRecordType.fromCode("MEDICAL_RECORD"));
        assertEquals(HealthRecordType.PAYMENT, HealthRecordType.fromCode("PAYMENT"));
        assertEquals(HealthRecordType.DISPENSING, HealthRecordType.fromCode("DISPENSING"));
    }

    @Test
    void fromCodeShouldReturnNullForNullCode() {
        assertNull(HealthRecordType.fromCode(null));
    }

    @Test
    void fromCodeShouldReturnNullForUnknownCode() {
        assertNull(HealthRecordType.fromCode("NOT_EXIST"));
    }
}
