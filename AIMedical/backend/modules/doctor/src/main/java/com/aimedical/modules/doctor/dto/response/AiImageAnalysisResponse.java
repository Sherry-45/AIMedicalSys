package com.aimedical.modules.doctor.dto.response;

import java.util.List;

/**
 * AI 影像分析响应。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public record AiImageAnalysisResponse(
    String modelId,
    RecognitionResult recognitionResult,
    String segmentationMaskRef,
    Double confidence,
    String auxiliaryAdvice
) {

    /**
     * 识别结果。
     */
    public record RecognitionResult(
        List<String> regions,
        List<String> labels,
        List<Double> scores
    ) {
    }
}
