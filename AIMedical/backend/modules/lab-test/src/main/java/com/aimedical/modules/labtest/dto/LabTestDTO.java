package com.aimedical.modules.labtest.dto;

import com.aimedical.modules.labtest.entity.LabTestStatus;
import com.aimedical.modules.labtest.entity.SampleType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 检验记录 DTO。
 */
@Data
public class LabTestDTO {

    private Long id;
    private Long patientId;
    private Long doctorId;
    private String testType;
    private SampleType sampleType;
    private LocalDateTime collectedAt;
    private LabTestStatus status;
    private String reportConclusion;
    private String aiInterpretation;
    private LocalDateTime reportedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LabTestItemDTO> items;
}
