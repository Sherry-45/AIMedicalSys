package com.aimedical.modules.patient.dto.health;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 长期健康趋势响应：按类型/类别聚合计数、最近记录、时间跨度、就诊机构列表。
 */
@Data
public class HealthTrendResponse {

    private Long patientId;
    private Integer totalRecords;
    private Map<String, Integer> recordsByType;
    private Map<String, Integer> recordsByCategory;
    private List<HealthRecordResponse> recentRecords;
    private LocalDate earliestRecordDate;
    private LocalDate latestRecordDate;
    private List<String> organizations;
}
