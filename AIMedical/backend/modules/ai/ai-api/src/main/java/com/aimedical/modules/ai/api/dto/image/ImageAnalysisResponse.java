package com.aimedical.modules.ai.api.dto.image;

import lombok.Data;

import java.util.List;

/**
 * AI 影像分析推理响应。
 * 严格对齐需求文档 §3.4.7 输出契约：
 * model_id / recognition_result(regions/labels/scores) / segmentation_mask_ref / confidence / auxiliary_advice。
 */
@Data
public class ImageAnalysisResponse {

    /** 模型标识。 */
    private String modelId;

    /** 识别结果，含 regions / labels / scores。 */
    private RecognitionResult recognitionResult;

    /** 分割掩码引用（仅肿瘤模型返回）。 */
    private String segmentationMaskRef;

    /** 置信度，取值范围 0–100。 */
    private Double confidence;

    /** 辅助判读建议。 */
    private String auxiliaryAdvice;

    public ImageAnalysisResponse() {
    }

    /**
     * 识别结果。对齐 §3.4.7 recognition_result 子字段。
     */
    @Data
    public static class RecognitionResult {
        /** 检出区域描述列表。 */
        private List<String> regions;
        /** 标签列表。 */
        private List<String> labels;
        /** 置信度分数列表，与 labels 一一对应。 */
        private List<Double> scores;

        public RecognitionResult() {
        }
    }
}
