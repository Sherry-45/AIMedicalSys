package com.aimedical.modules.examination.dto;

import com.aimedical.modules.examination.entity.ExaminationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExaminationCreateRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorId;

    @NotNull
    private ExaminationType examinationType;

    @Size(max = 200, message = "检查部位长度不能超过 200")
    private String bodyPart;

    @Size(max = 500, message = "临床诊断长度不能超过 500")
    private String clinicalDiagnosis;

    @Future
    private LocalDateTime scheduledAt;

    private Boolean emergencyFlag;

    @Size(max = 500, message = "影像URL长度不能超过 500")
    private String imageUrl;

    @Size(max = 50, message = "影像类型长度不能超过 50")
    private String imageType;

    @Valid
    private List<ExaminationItemRequest> items;
}
