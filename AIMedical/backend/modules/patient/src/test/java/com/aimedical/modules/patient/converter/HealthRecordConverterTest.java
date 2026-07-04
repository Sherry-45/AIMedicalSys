package com.aimedical.modules.patient.converter;

import com.aimedical.modules.patient.dto.health.HealthRecordResponse;
import com.aimedical.modules.patient.entity.HealthRecordCategory;
import com.aimedical.modules.patient.entity.HealthRecordEntity;
import com.aimedical.modules.patient.entity.HealthRecordType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link HealthRecordConverter} 单元测试。
 */
class HealthRecordConverterTest {

    private final HealthRecordConverter converter = new HealthRecordConverter();

    @Test
    void toResponseShouldReturnNullWhenEntityIsNull() {
        assertNull(converter.toResponse(null));
    }

    @Test
    void toResponseShouldMapAllFields() {
        HealthRecordEntity entity = new HealthRecordEntity();
        entity.setId(1L);
        entity.setPatientId(10L);
        entity.setRecordType(HealthRecordType.MEDICAL_RECORD.getCode());
        entity.setRecordCategory(HealthRecordCategory.OUTPATIENT.getCode());
        entity.setTitle("门诊记录");
        entity.setContent("患者主诉头痛");
        entity.setOrganization("市一医院");
        entity.setDepartment("神经内科");
        entity.setDoctorName("张医生");
        entity.setSourceId(100L);
        entity.setSourceTable("medical_record");
        entity.setReportData("{\"bp\":\"120/80\"}");
        entity.setRecordDate(LocalDate.of(2024, 6, 15));
        entity.setRemark("注意休息");
        LocalDateTime createdAt = LocalDateTime.of(2024, 6, 15, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 6, 15, 11, 0);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        HealthRecordResponse r = converter.toResponse(entity);

        assertEquals(1L, r.getId());
        assertEquals(10L, r.getPatientId());
        assertEquals(HealthRecordType.MEDICAL_RECORD.getCode(), r.getRecordType());
        assertEquals(HealthRecordType.MEDICAL_RECORD.getDesc(), r.getRecordTypeDesc());
        assertEquals(HealthRecordCategory.OUTPATIENT.getCode(), r.getRecordCategory());
        assertEquals(HealthRecordCategory.OUTPATIENT.getDesc(), r.getRecordCategoryDesc());
        assertEquals("门诊记录", r.getTitle());
        assertEquals("患者主诉头痛", r.getContent());
        assertEquals("市一医院", r.getOrganization());
        assertEquals("神经内科", r.getDepartment());
        assertEquals("张医生", r.getDoctorName());
        assertEquals(100L, r.getSourceId());
        assertEquals("medical_record", r.getSourceTable());
        assertEquals("{\"bp\":\"120/80\"}", r.getReportData());
        assertEquals(LocalDate.of(2024, 6, 15), r.getRecordDate());
        assertEquals("注意休息", r.getRemark());
        assertEquals(createdAt, r.getCreatedAt());
        assertEquals(updatedAt, r.getUpdatedAt());
    }

    @Test
    void toResponseShouldHandleUnknownTypeAndCategory() {
        HealthRecordEntity entity = new HealthRecordEntity();
        entity.setId(1L);
        entity.setRecordType("UNKNOWN_TYPE");
        entity.setRecordCategory("UNKNOWN_CATEGORY");
        entity.setTitle("t");

        HealthRecordResponse r = converter.toResponse(entity);

        assertEquals("UNKNOWN_TYPE", r.getRecordType());
        assertNull(r.getRecordTypeDesc());
        assertEquals("UNKNOWN_CATEGORY", r.getRecordCategory());
        assertNull(r.getRecordCategoryDesc());
    }

    @Test
    void toResponseShouldHandleNullTypeAndCategory() {
        HealthRecordEntity entity = new HealthRecordEntity();
        entity.setId(1L);
        entity.setRecordType(null);
        entity.setRecordCategory(null);
        entity.setTitle("t");

        HealthRecordResponse r = converter.toResponse(entity);

        assertNull(r.getRecordType());
        assertNull(r.getRecordTypeDesc());
        assertNull(r.getRecordCategory());
        assertNull(r.getRecordCategoryDesc());
    }

    @Test
    void toResponseListShouldReturnEmptyForNullInput() {
        assertTrue(converter.toResponseList(null).isEmpty());
    }

    @Test
    void toResponseListShouldReturnEmptyForEmptyInput() {
        assertTrue(converter.toResponseList(Collections.emptyList()).isEmpty());
    }

    @Test
    void toResponseListShouldMapAllEntities() {
        HealthRecordEntity e1 = new HealthRecordEntity();
        e1.setId(1L);
        e1.setRecordType(HealthRecordType.MEDICAL_RECORD.getCode());
        e1.setTitle("t1");

        HealthRecordEntity e2 = new HealthRecordEntity();
        e2.setId(2L);
        e2.setRecordType(HealthRecordType.LAB_TEST.getCode());
        e2.setTitle("t2");

        List<HealthRecordResponse> result = converter.toResponseList(List.of(e1, e2));

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("t1", result.get(0).getTitle());
        assertEquals(2L, result.get(1).getId());
        assertEquals("t2", result.get(1).getTitle());
    }
}
