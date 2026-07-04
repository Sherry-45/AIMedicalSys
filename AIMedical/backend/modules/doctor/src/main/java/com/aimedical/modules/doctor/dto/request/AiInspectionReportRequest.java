package com.aimedical.modules.doctor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * AI 检查报告生成请求。
 *
 * <p>医生端调用 AI 根据检查原始数据生成结构化报告草稿；AI 不可用时降级为模板填充。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public record AiInspectionReportRequest(
    @NotNull Long examinationId,
    @NotBlank @Size(max = 50) String examinationType,
    @Size(max = 500) String rawDataRef,
    @NotNull Long patientId,
    List<Item> items,
    @Size(max = 100) String bodyPart,
    @Size(max = 500) String clinicalDiagnosis
) {

    /**
     * 检查项目测量数据。
     */
    public record Item(
        @Size(max = 100) String itemName,
        @Size(max = 100) String value,
        @Size(max = 50) String unit,
        @Size(max = 100) String referenceRange,
        @Size(max = 50) String status
    ) {
    }
}
