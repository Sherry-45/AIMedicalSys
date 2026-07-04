package com.aimedical.modules.patient.dto.health;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HealthRecordResponse {

    private Long id;
    private Long patientId;
    private String recordType;
    private String recordTypeDesc;
    private String recordCategory;
    private String recordCategoryDesc;
    private String title;
    private String content;
    private String organization;
    private String department;
    private String doctorName;
    private Long sourceId;
    private String sourceTable;
    private String reportData;
    private LocalDate recordDate;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
