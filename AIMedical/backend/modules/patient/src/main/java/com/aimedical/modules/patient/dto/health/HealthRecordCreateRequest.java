package com.aimedical.modules.patient.dto.health;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HealthRecordCreateRequest {

    @NotNull
    private Long patientId;

    @NotBlank
    private String recordType;

    private String recordCategory;

    @NotBlank
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
}
