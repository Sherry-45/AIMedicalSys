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
import com.aimedical.modules.patient.entity.PatientEntity;
import com.aimedical.modules.patient.repository.HealthRecordRepository;
import com.aimedical.modules.patient.repository.PatientAllergyRepository;
import com.aimedical.modules.patient.repository.PatientChronicDiseaseRepository;
import com.aimedical.modules.patient.repository.PatientRepository;
import com.aimedical.modules.patient.service.HealthRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordRepository repository;
    private final PatientRepository patientRepository;
    private final PatientAllergyRepository allergyRepository;
    private final PatientChronicDiseaseRepository chronicDiseaseRepository;
    private final HealthRecordConverter converter;

    public HealthRecordServiceImpl(HealthRecordRepository repository,
                                    PatientRepository patientRepository,
                                    PatientAllergyRepository allergyRepository,
                                    PatientChronicDiseaseRepository chronicDiseaseRepository,
                                    HealthRecordConverter converter) {
        this.repository = repository;
        this.patientRepository = patientRepository;
        this.allergyRepository = allergyRepository;
        this.chronicDiseaseRepository = chronicDiseaseRepository;
        this.converter = converter;
    }

    @Override
    @Transactional
    public Result<HealthRecordResponse> create(HealthRecordCreateRequest request) {
        Long patientId = request.getPatientId();
        if (patientId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID.getCode(), "patientId不能为空");
        }
        if (!patientRepository.existsById(patientId)) {
            return Result.fail(GlobalErrorCode.NOT_FOUND.getCode(), "患者不存在");
        }
        HealthRecordEntity entity = new HealthRecordEntity();
        entity.setPatientId(patientId);
        entity.setRecordType(request.getRecordType());
        entity.setRecordCategory(request.getRecordCategory());
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        entity.setOrganization(request.getOrganization());
        entity.setDepartment(request.getDepartment());
        entity.setDoctorName(request.getDoctorName());
        entity.setSourceId(request.getSourceId());
        entity.setSourceTable(request.getSourceTable());
        entity.setReportData(request.getReportData());
        entity.setRecordDate(request.getRecordDate());
        entity.setRemark(request.getRemark());
        entity = repository.save(entity);
        return Result.success(converter.toResponse(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<HealthRecordResponse> getById(Long id) {
        if (id == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID.getCode(), "id不能为空");
        }
        return repository.findById(id)
                .map(converter::toResponse)
                .map(Result::success)
                .orElseGet(() -> Result.fail(GlobalErrorCode.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<HealthRecordResponse>> query(Long patientId, HealthRecordQueryRequest request) {
        if (patientId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID.getCode(), "patientId不能为空");
        }
        if (request == null) {
            request = new HealthRecordQueryRequest();
        }
        String type = request.getRecordType();
        String category = request.getRecordCategory();
        String org = request.getOrganization();
        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();
        boolean hasFullDateRange = start != null && end != null;

        List<HealthRecordEntity> records;
        // 选择最精确的可用派生查询，未覆盖的条件在内存中再过滤
        if (type != null && hasFullDateRange) {
            records = repository.findByPatientIdAndRecordTypeAndRecordDateBetweenOrderByRecordDateDesc(patientId, type, start, end);
        } else if (org != null && hasFullDateRange) {
            records = repository.findByPatientIdAndOrganizationAndRecordDateBetweenOrderByRecordDateDesc(patientId, org, start, end);
        } else if (type != null) {
            records = repository.findByPatientIdAndRecordTypeOrderByRecordDateDesc(patientId, type);
        } else if (category != null) {
            records = repository.findByPatientIdAndRecordCategoryOrderByRecordDateDesc(patientId, category);
        } else if (org != null) {
            records = repository.findByPatientIdAndOrganizationOrderByRecordDateDesc(patientId, org);
        } else if (hasFullDateRange) {
            records = repository.findByPatientIdAndRecordDateBetweenOrderByRecordDateDesc(patientId, start, end);
        } else {
            records = repository.findByPatientIdOrderByRecordDateDesc(patientId);
        }

        List<HealthRecordEntity> filtered = records.stream()
                .filter(e -> type == null || type.equals(e.getRecordType()))
                .filter(e -> category == null || category.equals(e.getRecordCategory()))
                .filter(e -> org == null || org.equalsIgnoreCase(e.getOrganization()))
                .filter(e -> start == null || (e.getRecordDate() != null && !e.getRecordDate().isBefore(start)))
                .filter(e -> end == null || (e.getRecordDate() != null && !e.getRecordDate().isAfter(end)))
                .collect(Collectors.toList());

        List<HealthRecordEntity> paged = paginate(filtered, request.getPage(), request.getSize());
        return Result.success(converter.toResponseList(paged));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<HealthTrendResponse> getHealthTrend(Long patientId) {
        if (patientId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID.getCode(), "patientId不能为空");
        }
        List<HealthRecordEntity> all = repository.findByPatientIdOrderByRecordDateDesc(patientId);

        HealthTrendResponse trend = new HealthTrendResponse();
        trend.setPatientId(patientId);
        trend.setTotalRecords(all.size());

        Map<String, Integer> byType = new LinkedHashMap<>();
        Map<String, Integer> byCategory = new LinkedHashMap<>();
        List<String> organizations = new ArrayList<>();
        LocalDate earliest = null;
        LocalDate latest = null;
        for (HealthRecordEntity e : all) {
            if (e.getRecordType() != null) {
                byType.merge(e.getRecordType(), 1, Integer::sum);
            }
            if (e.getRecordCategory() != null) {
                byCategory.merge(e.getRecordCategory(), 1, Integer::sum);
            }
            if (e.getOrganization() != null && !organizations.contains(e.getOrganization())) {
                organizations.add(e.getOrganization());
            }
            if (e.getRecordDate() != null) {
                if (earliest == null || e.getRecordDate().isBefore(earliest)) {
                    earliest = e.getRecordDate();
                }
                if (latest == null || e.getRecordDate().isAfter(latest)) {
                    latest = e.getRecordDate();
                }
            }
        }
        trend.setRecordsByType(byType);
        trend.setRecordsByCategory(byCategory);
        trend.setOrganizations(organizations);
        trend.setEarliestRecordDate(earliest);
        trend.setLatestRecordDate(latest);

        int recentSize = Math.min(10, all.size());
        trend.setRecentRecords(converter.toResponseList(all.subList(0, recentSize)));
        return Result.success(trend);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<HealthSummaryResponse> getHealthSummary(Long patientId) {
        if (patientId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID.getCode(), "patientId不能为空");
        }
        PatientEntity patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            return Result.fail(GlobalErrorCode.NOT_FOUND.getCode(), "患者不存在");
        }

        HealthSummaryResponse summary = new HealthSummaryResponse();
        summary.setPatientId(patientId);
        summary.setPatientName(patient.getRealName());

        int allergyCount = allergyRepository.findByPatientId(patientId).size();
        int chronicCount = chronicDiseaseRepository.findByPatientId(patientId).size();
        summary.setAllergyCount(allergyCount);
        summary.setChronicDiseaseCount(chronicCount);

        HealthTrendResponse trend = getHealthTrend(patientId).getData();
        summary.setHealthTrend(trend);
        summary.setTotalRecords(trend != null ? trend.getTotalRecords() : 0);

        // 最近就诊：优先取病历类型，其次取任意有日期的记录（列表已按 recordDate 倒序）
        List<HealthRecordEntity> all = repository.findByPatientIdOrderByRecordDateDesc(patientId);
        LocalDate lastVisitDate = null;
        String lastVisitOrg = null;
        for (HealthRecordEntity e : all) {
            if (HealthRecordType.MEDICAL_RECORD.getCode().equals(e.getRecordType()) && e.getRecordDate() != null) {
                lastVisitDate = e.getRecordDate();
                lastVisitOrg = e.getOrganization();
                break;
            }
        }
        if (lastVisitDate == null) {
            for (HealthRecordEntity e : all) {
                if (e.getRecordDate() != null) {
                    lastVisitDate = e.getRecordDate();
                    lastVisitOrg = e.getOrganization();
                    break;
                }
            }
        }
        summary.setLastVisitDate(lastVisitDate);
        summary.setLastVisitOrganization(lastVisitOrg);
        return Result.success(summary);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<HealthRecordResponse>> queryByPatient(Long patientId) {
        if (patientId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID.getCode(), "patientId不能为空");
        }
        List<HealthRecordEntity> records = repository.findByPatientIdOrderByRecordDateDesc(patientId);
        return Result.success(converter.toResponseList(records));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<HealthRecordResponse>> queryPaymentRecords(Long patientId) {
        if (patientId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID.getCode(), "patientId不能为空");
        }
        List<HealthRecordEntity> records = repository.findByPatientIdAndRecordTypeOrderByRecordDateDesc(
                patientId, HealthRecordType.PAYMENT.getCode());
        return Result.success(converter.toResponseList(records));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<HealthRecordResponse>> queryDispensingRecords(Long patientId) {
        if (patientId == null) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID.getCode(), "patientId不能为空");
        }
        List<HealthRecordEntity> records = repository.findByPatientIdAndRecordTypeOrderByRecordDateDesc(
                patientId, HealthRecordType.DISPENSING.getCode());
        return Result.success(converter.toResponseList(records));
    }

    private List<HealthRecordEntity> paginate(List<HealthRecordEntity> source, Integer page, Integer size) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        int p = page == null ? 0 : page;
        int s = size == null ? 20 : size;
        if (p < 0) {
            p = 0;
        }
        if (s <= 0) {
            s = 20;
        }
        int fromIndex = p * s;
        if (fromIndex >= source.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + s, source.size());
        return source.subList(fromIndex, toIndex);
    }
}
