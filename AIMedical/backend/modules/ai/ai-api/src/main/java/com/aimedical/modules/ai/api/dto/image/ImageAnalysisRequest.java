package com.aimedical.modules.ai.api.dto.image;

import lombok.Data;

/**
 * AI 影像分析推理请求。
 * 对齐需求文档 §3.4.7 输入契约：
 * image_ref / model_id / patient_id。
 */
@Data
public class ImageAnalysisRequest {

    /** 影像数据引用（必填）。 */
    private String imageRef;
    /** 模型标识（必填，取值范围由岗位决定）。 */
    private String modelId;
    /** 患者标识。 */
    private Long patientId;

    /** 以下为业务上下文字段，供 AI 参考但不在 §3.4.7 契约必填范围内。 */
    private Long examinationId;
    private String examinationType;
    private String bodyPart;
    private String clinicalDiagnosis;
    private String imageType;

    public ImageAnalysisRequest() {
    }
}
