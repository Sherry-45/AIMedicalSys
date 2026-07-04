package com.aimedical.modules.doctor.dto.response;

import java.util.List;

/**
 * AI 检查报告生成响应。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public record AiInspectionReportResponse(
    String reportDraft,
    List<String> findings,
    String impression,
    String auxiliaryInterpretation,
    List<AbnormalItem> abnormalItems,
    String comparisonSummary,
    Double confidence,
    ImageRecognition imageRecognition
) {

    /**
     * 异常项明细。
     */
    public record AbnormalItem(
        String itemName,
        String value,
        String unit,
        String status
    ) {
    }

    /**
     * 影像识别结果（由检查报告生成内部调用影像分析填充）。
     */
    public record ImageRecognition(
        String modelId,
        String recognitionSummary,
        Double confidence
    ) {
    }
}
