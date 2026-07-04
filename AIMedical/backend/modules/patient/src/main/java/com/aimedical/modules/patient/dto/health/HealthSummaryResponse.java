package com.aimedical.modules.patient.dto.health;

import lombok.Data;

import java.time.LocalDate;

/**
 * 健康档案合并展示摘要：整合过敏、慢病计数与健康趋势，供前端合并展示。
 */
@Data
public class HealthSummaryResponse {

    private Long patientId;
    private String patientName;
    private Integer totalRecords;
    private Integer allergyCount;
    private Integer chronicDiseaseCount;
    private LocalDate lastVisitDate;
    private String lastVisitOrganization;
    private HealthTrendResponse healthTrend;
}
