package com.aimedical.modules.examination.dto;

import com.aimedical.modules.examination.entity.ExaminationStatus;
import com.aimedical.modules.examination.entity.ExaminationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExaminationDTO {

    private Long id;

    private Long patientId;

    private Long doctorId;

    private ExaminationType examinationType;

    private String bodyPart;

    private String clinicalDiagnosis;

    private LocalDateTime scheduledAt;

    private ExaminationStatus status;

    private Boolean emergencyFlag;

    private String imageUrl;

    private String imageType;

    private String impression;

    private String conclusion;

    private String aiInterpretation;

    private Double aiConfidence;

    private String imageAnalysisResult;

    private Double imageConfidence;

    private LocalDateTime reportedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ExaminationItemDTO> items;
}
