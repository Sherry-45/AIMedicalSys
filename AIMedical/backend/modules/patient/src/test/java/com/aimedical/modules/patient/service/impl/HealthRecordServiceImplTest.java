package com.aimedical.modules.patient.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.patient.converter.HealthRecordConverter;
import com.aimedical.modules.patient.dto.health.HealthRecordCreateRequest;
import com.aimedical.modules.patient.dto.health.HealthRecordQueryRequest;
import com.aimedical.modules.patient.dto.health.HealthRecordResponse;
import com.aimedical.modules.patient.dto.health.HealthSummaryResponse;
import com.aimedical.modules.patient.dto.health.HealthTrendResponse;
import com.aimedical.modules.patient.entity.HealthRecordEntity;
import com.aimedical.modules.patient.entity.HealthRecordType;
import com.aimedical.modules.patient.entity.PatientAllergy;
import com.aimedical.modules.patient.entity.PatientChronicDisease;
import com.aimedical.modules.patient.entity.PatientEntity;
import com.aimedical.modules.patient.repository.HealthRecordRepository;
import com.aimedical.modules.patient.repository.PatientAllergyRepository;
import com.aimedical.modules.patient.repository.PatientChronicDiseaseRepository;
import com.aimedical.modules.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HealthRecordServiceImpl} 单元测试。
 *
 * <p>覆盖创建、按ID查询、多条件检索、健康趋势、合并摘要、缴费/发药记录查询等全部方法，
 * 以及分页、空值、不存在患者等边界场景。
 */
@ExtendWith(MockitoExtension.class)
class HealthRecordServiceImplTest {

    @Mock private HealthRecordRepository repository;
    @Mock private PatientRepository patientRepository;
    @Mock private PatientAllergyRepository allergyRepository;
    @Mock private PatientChronicDiseaseRepository chronicDiseaseRepository;
    @Mock private HealthRecordConverter converter;

    private HealthRecordServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new HealthRecordServiceImpl(repository, patientRepository,
                allergyRepository, chronicDiseaseRepository, converter);
    }

    // ==================== create ====================

    @Test
    void createShouldFailWhenPatientIdIsNull() {
        HealthRecordCreateRequest req = new HealthRecordCreateRequest();
        req.setPatientId(null);
        req.setRecordType(HealthRecordType.MEDICAL_RECORD.getCode());
        req.setTitle("t");

        Result<HealthRecordResponse> result = service.create(req);

        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
        verify(repository, never()).save(any());
    }

    @Test
    void createShouldFailWhenPatientNotExists() {
        HealthRecordCreateRequest req = buildCreateRequest(1L, HealthRecordType.MEDICAL_RECORD);
        when(patientRepository.existsById(1L)).thenReturn(false);

        Result<HealthRecordResponse> result = service.create(req);

        assertEquals(GlobalErrorCode.NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
        verify(repository, never()).save(any());
    }

    @Test
    void createShouldSaveAndReturnResponse() {
        HealthRecordCreateRequest req = buildCreateRequest(1L, HealthRecordType.LAB_TEST);
        req.setRecordCategory("OUTPATIENT");
        req.setOrganization("市一医院");
        req.setDepartment("内科");
        req.setDoctorName("张医生");
        req.setSourceId(100L);
        req.setSourceTable("lab_test");
        req.setReportData("{\"key\":\"value\"}");
        req.setRecordDate(LocalDate.of(2024, 6, 1));
        req.setRemark("复查");

        when(patientRepository.existsById(1L)).thenReturn(true);
        when(repository.save(any(HealthRecordEntity.class))).thenAnswer(inv -> {
            HealthRecordEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });
        HealthRecordResponse expected = new HealthRecordResponse();
        expected.setId(10L);
        when(converter.toResponse(any(HealthRecordEntity.class))).thenReturn(expected);

        Result<HealthRecordResponse> result = service.create(req);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(10L, result.getData().getId());

        ArgumentCaptor<HealthRecordEntity> captor = ArgumentCaptor.forClass(HealthRecordEntity.class);
        verify(repository).save(captor.capture());
        HealthRecordEntity saved = captor.getValue();
        assertEquals(1L, saved.getPatientId());
        assertEquals(HealthRecordType.LAB_TEST.getCode(), saved.getRecordType());
        assertEquals("OUTPATIENT", saved.getRecordCategory());
        assertEquals("市一医院", saved.getOrganization());
        assertEquals("内科", saved.getDepartment());
        assertEquals("张医生", saved.getDoctorName());
        assertEquals(100L, saved.getSourceId());
        assertEquals("lab_test", saved.getSourceTable());
        assertEquals("{\"key\":\"value\"}", saved.getReportData());
        assertEquals(LocalDate.of(2024, 6, 1), saved.getRecordDate());
        assertEquals("复查", saved.getRemark());
    }

    // ==================== getById ====================

    @Test
    void getByIdShouldFailWhenIdIsNull() {
        Result<HealthRecordResponse> result = service.getById(null);

        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByIdShouldReturnNotFoundWhenAbsent() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        Result<HealthRecordResponse> result = service.getById(5L);

        assertEquals(GlobalErrorCode.NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByIdShouldReturnResponseWhenFound() {
        HealthRecordEntity entity = buildEntity(5L, 1L, HealthRecordType.EXAM_REPORT);
        when(repository.findById(5L)).thenReturn(Optional.of(entity));
        HealthRecordResponse expected = new HealthRecordResponse();
        expected.setId(5L);
        when(converter.toResponse(entity)).thenReturn(expected);

        Result<HealthRecordResponse> result = service.getById(5L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(5L, result.getData().getId());
    }

    // ==================== query ====================

    @Test
    void queryShouldFailWhenPatientIdIsNull() {
        Result<List<HealthRecordResponse>> result = service.query(null, new HealthRecordQueryRequest());

        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void queryShouldUseDefaultRequestWhenNull() {
        HealthRecordEntity e = buildEntity(1L, 1L, HealthRecordType.MEDICAL_RECORD);
        when(repository.findByPatientIdOrderByRecordDateDesc(1L)).thenReturn(List.of(e));
        HealthRecordResponse resp = new HealthRecordResponse();
        resp.setId(1L);
        when(converter.toResponseList(any())).thenReturn(List.of(resp));

        Result<List<HealthRecordResponse>> result = service.query(1L, null);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        verify(repository).findByPatientIdOrderByRecordDateDesc(1L);
    }

    @Test
    void queryShouldUseTypeAndDateRangeWhenBothProvided() {
        HealthRecordQueryRequest req = new HealthRecordQueryRequest();
        req.setRecordType(HealthRecordType.LAB_TEST.getCode());
        req.setStartDate(LocalDate.of(2024, 1, 1));
        req.setEndDate(LocalDate.of(2024, 12, 31));

        when(repository.findByPatientIdAndRecordTypeAndRecordDateBetweenOrderByRecordDateDesc(
                eq(1L), eq(HealthRecordType.LAB_TEST.getCode()),
                eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 12, 31))))
                .thenReturn(Collections.emptyList());
        when(converter.toResponseList(any())).thenReturn(Collections.emptyList());

        Result<List<HealthRecordResponse>> result = service.query(1L, req);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void queryShouldUseOrgAndDateRangeWhenOrgAndDatesProvided() {
        HealthRecordQueryRequest req = new HealthRecordQueryRequest();
        req.setOrganization("市一医院");
        req.setStartDate(LocalDate.of(2024, 1, 1));
        req.setEndDate(LocalDate.of(2024, 12, 31));

        when(repository.findByPatientIdAndOrganizationAndRecordDateBetweenOrderByRecordDateDesc(
                eq(1L), eq("市一医院"),
                eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 12, 31))))
                .thenReturn(Collections.emptyList());
        when(converter.toResponseList(any())).thenReturn(Collections.emptyList());

        Result<List<HealthRecordResponse>> result = service.query(1L, req);

        assertEquals("SUCCESS", result.getCode());
    }

    @Test
    void queryShouldUseTypeOnlyWhenTypeProvidedWithoutFullDateRange() {
        HealthRecordQueryRequest req = new HealthRecordQueryRequest();
        req.setRecordType(HealthRecordType.PRESCRIPTION.getCode());

        when(repository.findByPatientIdAndRecordTypeOrderByRecordDateDesc(
                1L, HealthRecordType.PRESCRIPTION.getCode()))
                .thenReturn(Collections.emptyList());
        when(converter.toResponseList(any())).thenReturn(Collections.emptyList());

        Result<List<HealthRecordResponse>> result = service.query(1L, req);

        assertEquals("SUCCESS", result.getCode());
    }

    @Test
    void queryShouldUseCategoryWhenCategoryProvided() {
        HealthRecordQueryRequest req = new HealthRecordQueryRequest();
        req.setRecordCategory("OUTPATIENT");

        when(repository.findByPatientIdAndRecordCategoryOrderByRecordDateDesc(1L, "OUTPATIENT"))
                .thenReturn(Collections.emptyList());
        when(converter.toResponseList(any())).thenReturn(Collections.emptyList());

        Result<List<HealthRecordResponse>> result = service.query(1L, req);

        assertEquals("SUCCESS", result.getCode());
    }

    @Test
    void queryShouldUseOrgOnlyWhenOrgProvidedWithoutDateRange() {
        HealthRecordQueryRequest req = new HealthRecordQueryRequest();
        req.setOrganization("市二医院");

        when(repository.findByPatientIdAndOrganizationOrderByRecordDateDesc(1L, "市二医院"))
                .thenReturn(Collections.emptyList());
        when(converter.toResponseList(any())).thenReturn(Collections.emptyList());

        Result<List<HealthRecordResponse>> result = service.query(1L, req);

        assertEquals("SUCCESS", result.getCode());
    }

    @Test
    void queryShouldUseDateRangeOnlyWhenDatesProvidedWithoutTypeOrOrg() {
        HealthRecordQueryRequest req = new HealthRecordQueryRequest();
        req.setStartDate(LocalDate.of(2024, 1, 1));
        req.setEndDate(LocalDate.of(2024, 12, 31));

        when(repository.findByPatientIdAndRecordDateBetweenOrderByRecordDateDesc(
                1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)))
                .thenReturn(Collections.emptyList());
        when(converter.toResponseList(any())).thenReturn(Collections.emptyList());

        Result<List<HealthRecordResponse>> result = service.query(1L, req);

        assertEquals("SUCCESS", result.getCode());
    }

    @Test
    void queryShouldApplyPagination() {
        // 构造 25 条记录，page=1, size=10 → 应返回第 11~20 条
        List<HealthRecordEntity> entities = new java.util.ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            entities.add(buildEntity((long) i, 1L, HealthRecordType.MEDICAL_RECORD));
        }
        when(repository.findByPatientIdOrderByRecordDateDesc(1L)).thenReturn(entities);
        when(converter.toResponseList(any())).thenAnswer(inv -> {
            List<HealthRecordEntity> list = inv.getArgument(0);
            return list.stream().map(e -> {
                HealthRecordResponse r = new HealthRecordResponse();
                r.setId(e.getId());
                return r;
            }).toList();
        });

        HealthRecordQueryRequest req = new HealthRecordQueryRequest();
        req.setPage(1);
        req.setSize(10);

        Result<List<HealthRecordResponse>> result = service.query(1L, req);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(10, result.getData().size());
        // 第 11~20 条（索引 10~19）
        assertEquals(11L, result.getData().get(0).getId());
        assertEquals(20L, result.getData().get(9).getId());
    }

    @Test
    void queryShouldReturnEmptyWhenPageOutOfBounds() {
        List<HealthRecordEntity> entities = List.of(buildEntity(1L, 1L, HealthRecordType.MEDICAL_RECORD));
        when(repository.findByPatientIdOrderByRecordDateDesc(1L)).thenReturn(entities);
        when(converter.toResponseList(any())).thenReturn(Collections.emptyList());

        HealthRecordQueryRequest req = new HealthRecordQueryRequest();
        req.setPage(99);
        req.setSize(10);

        Result<List<HealthRecordResponse>> result = service.query(1L, req);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void queryShouldHandleNegativePageAndNonPositiveSize() {
        List<HealthRecordEntity> entities = List.of(buildEntity(1L, 1L, HealthRecordType.MEDICAL_RECORD));
        when(repository.findByPatientIdOrderByRecordDateDesc(1L)).thenReturn(entities);
        when(converter.toResponseList(any())).thenReturn(Collections.emptyList());

        HealthRecordQueryRequest req = new HealthRecordQueryRequest();
        req.setPage(-1);
        req.setSize(0);

        Result<List<HealthRecordResponse>> result = service.query(1L, req);

        assertEquals("SUCCESS", result.getCode());
    }

    @Test
    void queryShouldFilterByCategoryInMemoryWhenTypeQueryUsed() {
        // type 查询走 findByPatientIdAndRecordType...，category 在内存中过滤
        HealthRecordEntity e1 = buildEntity(1L, 1L, HealthRecordType.MEDICAL_RECORD);
        e1.setRecordCategory("OUTPATIENT");
        HealthRecordEntity e2 = buildEntity(2L, 1L, HealthRecordType.MEDICAL_RECORD);
        e2.setRecordCategory("INPATIENT");

        when(repository.findByPatientIdAndRecordTypeOrderByRecordDateDesc(1L, HealthRecordType.MEDICAL_RECORD.getCode()))
                .thenReturn(List.of(e1, e2));
        when(converter.toResponseList(any())).thenAnswer(inv -> {
            List<HealthRecordEntity> list = inv.getArgument(0);
            return list.stream().map(e -> {
                HealthRecordResponse r = new HealthRecordResponse();
                r.setId(e.getId());
                return r;
            }).toList();
        });

        HealthRecordQueryRequest req = new HealthRecordQueryRequest();
        req.setRecordType(HealthRecordType.MEDICAL_RECORD.getCode());
        req.setRecordCategory("INPATIENT");

        Result<List<HealthRecordResponse>> result = service.query(1L, req);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals(2L, result.getData().get(0).getId());
    }

    // ==================== getHealthTrend ====================

    @Test
    void getHealthTrendShouldFailWhenPatientIdIsNull() {
        Result<HealthTrendResponse> result = service.getHealthTrend(null);

        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getHealthTrendShouldReturnEmptyTrendWhenNoRecords() {
        when(repository.findByPatientIdOrderByRecordDateDesc(1L)).thenReturn(Collections.emptyList());

        Result<HealthTrendResponse> result = service.getHealthTrend(1L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getTotalRecords());
        assertTrue(result.getData().getRecordsByType().isEmpty());
        assertTrue(result.getData().getRecordsByCategory().isEmpty());
        assertTrue(result.getData().getOrganizations().isEmpty());
        assertNull(result.getData().getEarliestRecordDate());
        assertNull(result.getData().getLatestRecordDate());
        assertTrue(result.getData().getRecentRecords().isEmpty());
    }

    @Test
    void getHealthTrendShouldAggregateCorrectly() {
        HealthRecordEntity e1 = buildEntity(1L, 1L, HealthRecordType.MEDICAL_RECORD);
        e1.setRecordCategory("OUTPATIENT");
        e1.setOrganization("市一医院");
        e1.setRecordDate(LocalDate.of(2024, 6, 1));

        HealthRecordEntity e2 = buildEntity(2L, 1L, HealthRecordType.LAB_TEST);
        e2.setRecordCategory("OUTPATIENT");
        e2.setOrganization("市二医院");
        e2.setRecordDate(LocalDate.of(2024, 3, 15));

        HealthRecordEntity e3 = buildEntity(3L, 1L, HealthRecordType.MEDICAL_RECORD);
        e3.setRecordCategory("INPATIENT");
        e3.setOrganization("市一医院");
        e3.setRecordDate(LocalDate.of(2024, 9, 10));

        when(repository.findByPatientIdOrderByRecordDateDesc(1L)).thenReturn(List.of(e1, e2, e3));
        when(converter.toResponseList(any())).thenReturn(List.of(
                new HealthRecordResponse(), new HealthRecordResponse(), new HealthRecordResponse()));

        Result<HealthTrendResponse> result = service.getHealthTrend(1L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(3, result.getData().getTotalRecords());
        assertEquals(2, result.getData().getRecordsByType().get(HealthRecordType.MEDICAL_RECORD.getCode()));
        assertEquals(1, result.getData().getRecordsByType().get(HealthRecordType.LAB_TEST.getCode()));
        assertEquals(2, result.getData().getRecordsByCategory().get("OUTPATIENT"));
        assertEquals(1, result.getData().getRecordsByCategory().get("INPATIENT"));
        assertEquals(2, result.getData().getOrganizations().size());
        assertTrue(result.getData().getOrganizations().contains("市一医院"));
        assertTrue(result.getData().getOrganizations().contains("市二医院"));
        assertEquals(LocalDate.of(2024, 3, 15), result.getData().getEarliestRecordDate());
        assertEquals(LocalDate.of(2024, 9, 10), result.getData().getLatestRecordDate());
    }

    @Test
    void getHealthTrendShouldCapRecentRecordsAt10() {
        List<HealthRecordEntity> entities = new java.util.ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            entities.add(buildEntity((long) i, 1L, HealthRecordType.MEDICAL_RECORD));
        }
        when(repository.findByPatientIdOrderByRecordDateDesc(1L)).thenReturn(entities);
        when(converter.toResponseList(any())).thenAnswer(inv -> {
            List<HealthRecordEntity> list = inv.getArgument(0);
            return list.stream().map(e -> new HealthRecordResponse()).toList();
        });

        Result<HealthTrendResponse> result = service.getHealthTrend(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(15, result.getData().getTotalRecords());
        assertEquals(10, result.getData().getRecentRecords().size());
    }

    // ==================== getHealthSummary ====================

    @Test
    void getHealthSummaryShouldFailWhenPatientIdIsNull() {
        Result<HealthSummaryResponse> result = service.getHealthSummary(null);

        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getHealthSummaryShouldFailWhenPatientNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        Result<HealthSummaryResponse> result = service.getHealthSummary(1L);

        assertEquals(GlobalErrorCode.NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getHealthSummaryShouldAggregateAllInfo() {
        PatientEntity patient = new PatientEntity();
        patient.setId(1L);
        patient.setRealName("张三");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        PatientAllergy allergy = new PatientAllergy();
        allergy.setId(1L);
        when(allergyRepository.findByPatientId(1L)).thenReturn(List.of(allergy));

        PatientChronicDisease cd = new PatientChronicDisease();
        cd.setId(1L);
        when(chronicDiseaseRepository.findByPatientId(1L)).thenReturn(List.of(cd));

        HealthRecordEntity visit = buildEntity(1L, 1L, HealthRecordType.MEDICAL_RECORD);
        visit.setOrganization("市一医院");
        visit.setRecordDate(LocalDate.of(2024, 6, 1));

        // getHealthTrend 内部也调用 findByPatientIdOrderByRecordDateDesc
        // getHealthSummary 末尾再次调用以确定最近就诊
        when(repository.findByPatientIdOrderByRecordDateDesc(1L)).thenReturn(List.of(visit));
        when(converter.toResponseList(any())).thenReturn(List.of(new HealthRecordResponse()));

        Result<HealthSummaryResponse> result = service.getHealthSummary(1L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getPatientId());
        assertEquals("张三", result.getData().getPatientName());
        assertEquals(1, result.getData().getAllergyCount());
        assertEquals(1, result.getData().getChronicDiseaseCount());
        assertEquals(1, result.getData().getTotalRecords());
        assertEquals(LocalDate.of(2024, 6, 1), result.getData().getLastVisitDate());
        assertEquals("市一医院", result.getData().getLastVisitOrganization());
        assertNotNull(result.getData().getHealthTrend());
    }

    @Test
    void getHealthSummaryShouldFallbackToAnyRecordWhenNoMedicalRecordType() {
        PatientEntity patient = new PatientEntity();
        patient.setId(1L);
        patient.setRealName("李四");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(allergyRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());
        when(chronicDiseaseRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());

        // 没有 MEDICAL_RECORD 类型，只有 PAYMENT 类型
        HealthRecordEntity payment = buildEntity(1L, 1L, HealthRecordType.PAYMENT);
        payment.setOrganization("市三医院");
        payment.setRecordDate(LocalDate.of(2024, 5, 1));

        when(repository.findByPatientIdOrderByRecordDateDesc(1L)).thenReturn(List.of(payment));
        when(converter.toResponseList(any())).thenReturn(List.of(new HealthRecordResponse()));

        Result<HealthSummaryResponse> result = service.getHealthSummary(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(LocalDate.of(2024, 5, 1), result.getData().getLastVisitDate());
        assertEquals("市三医院", result.getData().getLastVisitOrganization());
    }

    @Test
    void getHealthSummaryShouldHandleNoVisitDates() {
        PatientEntity patient = new PatientEntity();
        patient.setId(1L);
        patient.setRealName("王五");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(allergyRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());
        when(chronicDiseaseRepository.findByPatientId(1L)).thenReturn(Collections.emptyList());

        // 记录没有日期
        HealthRecordEntity noDate = buildEntity(1L, 1L, HealthRecordType.MEDICAL_RECORD);
        noDate.setRecordDate(null);

        when(repository.findByPatientIdOrderByRecordDateDesc(1L)).thenReturn(List.of(noDate));
        when(converter.toResponseList(any())).thenReturn(List.of(new HealthRecordResponse()));

        Result<HealthSummaryResponse> result = service.getHealthSummary(1L);

        assertEquals("SUCCESS", result.getCode());
        assertNull(result.getData().getLastVisitDate());
        assertNull(result.getData().getLastVisitOrganization());
    }

    // ==================== queryByPatient ====================

    @Test
    void queryByPatientShouldFailWhenPatientIdIsNull() {
        Result<List<HealthRecordResponse>> result = service.queryByPatient(null);

        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void queryByPatientShouldReturnAllRecords() {
        HealthRecordEntity e = buildEntity(1L, 1L, HealthRecordType.MEDICAL_RECORD);
        when(repository.findByPatientIdOrderByRecordDateDesc(1L)).thenReturn(List.of(e));
        when(converter.toResponseList(any())).thenReturn(List.of(new HealthRecordResponse()));

        Result<List<HealthRecordResponse>> result = service.queryByPatient(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData().size());
    }

    // ==================== queryPaymentRecords ====================

    @Test
    void queryPaymentRecordsShouldFailWhenPatientIdIsNull() {
        Result<List<HealthRecordResponse>> result = service.queryPaymentRecords(null);

        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void queryPaymentRecordsShouldReturnOnlyPaymentType() {
        HealthRecordEntity e = buildEntity(1L, 1L, HealthRecordType.PAYMENT);
        when(repository.findByPatientIdAndRecordTypeOrderByRecordDateDesc(1L, HealthRecordType.PAYMENT.getCode()))
                .thenReturn(List.of(e));
        when(converter.toResponseList(any())).thenReturn(List.of(new HealthRecordResponse()));

        Result<List<HealthRecordResponse>> result = service.queryPaymentRecords(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData().size());
        verify(repository).findByPatientIdAndRecordTypeOrderByRecordDateDesc(1L, HealthRecordType.PAYMENT.getCode());
    }

    // ==================== queryDispensingRecords ====================

    @Test
    void queryDispensingRecordsShouldFailWhenPatientIdIsNull() {
        Result<List<HealthRecordResponse>> result = service.queryDispensingRecords(null);

        assertEquals(GlobalErrorCode.PARAM_INVALID.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void queryDispensingRecordsShouldReturnOnlyDispensingType() {
        HealthRecordEntity e = buildEntity(1L, 1L, HealthRecordType.DISPENSING);
        when(repository.findByPatientIdAndRecordTypeOrderByRecordDateDesc(1L, HealthRecordType.DISPENSING.getCode()))
                .thenReturn(List.of(e));
        when(converter.toResponseList(any())).thenReturn(List.of(new HealthRecordResponse()));

        Result<List<HealthRecordResponse>> result = service.queryDispensingRecords(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData().size());
        verify(repository).findByPatientIdAndRecordTypeOrderByRecordDateDesc(1L, HealthRecordType.DISPENSING.getCode());
    }

    // ==================== helpers ====================

    private HealthRecordCreateRequest buildCreateRequest(Long patientId, HealthRecordType type) {
        HealthRecordCreateRequest req = new HealthRecordCreateRequest();
        req.setPatientId(patientId);
        req.setRecordType(type.getCode());
        req.setTitle("测试记录");
        req.setContent("内容");
        return req;
    }

    private HealthRecordEntity buildEntity(Long id, Long patientId, HealthRecordType type) {
        HealthRecordEntity e = new HealthRecordEntity();
        e.setId(id);
        e.setPatientId(patientId);
        e.setRecordType(type.getCode());
        e.setTitle("记录-" + id);
        return e;
    }
}
