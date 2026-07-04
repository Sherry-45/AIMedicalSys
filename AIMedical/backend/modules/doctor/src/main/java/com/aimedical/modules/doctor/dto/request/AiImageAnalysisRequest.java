package com.aimedical.modules.doctor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * AI 影像分析请求。
 *
 * <p>医生端调用 AI 对影像数据进行识别与判读辅助；AI 不可用时降级提示人工判读。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public record AiImageAnalysisRequest(
    @NotBlank @Size(max = 500) String imageRef,
    @NotBlank @Size(max = 100) String modelId,
    @NotNull Long patientId,
    Long examinationId,
    @Size(max = 50) String examinationType,
    @Size(max = 100) String bodyPart,
    @Size(max = 500) String clinicalDiagnosis,
    @Size(max = 50) String imageType
) {
}
