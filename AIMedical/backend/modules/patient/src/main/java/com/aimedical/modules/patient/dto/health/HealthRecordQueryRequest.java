package com.aimedical.modules.patient.dto.health;

import lombok.Data;

import java.time.LocalDate;

/**
 * 健康档案查询请求：支持按类型/类别/机构/时间范围筛选，并支持分页。
 */
@Data
public class HealthRecordQueryRequest {

    private String recordType;

    private String recordCategory;

    private String organization;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer page = 0;

    private Integer size = 20;
}
