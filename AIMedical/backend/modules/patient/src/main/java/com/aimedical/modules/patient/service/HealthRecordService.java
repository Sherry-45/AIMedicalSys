package com.aimedical.modules.patient.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.patient.dto.health.HealthRecordCreateRequest;
import com.aimedical.modules.patient.dto.health.HealthRecordQueryRequest;
import com.aimedical.modules.patient.dto.health.HealthRecordResponse;
import com.aimedical.modules.patient.dto.health.HealthSummaryResponse;
import com.aimedical.modules.patient.dto.health.HealthTrendResponse;

import java.util.List;

/**
 * 健康档案服务：负责完整报告整合、缴费/发药记录查询、按时间/机构/类别检索，
 * 以及长期健康趋势与合并摘要展示。
 */
public interface HealthRecordService {

    Result<HealthRecordResponse> create(HealthRecordCreateRequest request);

    Result<HealthRecordResponse> getById(Long id);

    Result<List<HealthRecordResponse>> query(Long patientId, HealthRecordQueryRequest request);

    Result<HealthTrendResponse> getHealthTrend(Long patientId);

    Result<HealthSummaryResponse> getHealthSummary(Long patientId);

    Result<List<HealthRecordResponse>> queryByPatient(Long patientId);

    Result<List<HealthRecordResponse>> queryPaymentRecords(Long patientId);

    Result<List<HealthRecordResponse>> queryDispensingRecords(Long patientId);
}
