package com.aimedical.modules.ai.api.dto.inspection;

import lombok.Data;

import java.util.List;

/**
 * AI 检查报告生成响应。
 * 严格对齐需求文档 §3.4.5 输出契约：
 * report_draft / findings / impression / auxiliary_interpretation /
 * abnormal_items / comparison_summary / confidence / image_recognition。
 */
@Data
public class InspectionReportResponse {

    /** 报告草稿文本。 */
    private String reportDraft;

    /** 关键所见。 */
    private List<String> findings;

    /** 印象。 */
    private String impression;

    /** 辅助判读结论。 */
    private String auxiliaryInterpretation;

    /** 异常项列表，每项含 item_name/value/unit/status。 */
    private List<AbnormalItem> abnormalItems;

    /** 与历史同项目结果对比的趋势摘要。 */
    private String comparisonSummary;

    /** 置信度，取值范围 0–100。 */
    private Double confidence;

    /**
     * 影像识别子对象（可选），仅在影像类检查且 model_id 已识别时由 3.4.5 内部调用 3.4.7 填充。
     * 任何原因导致内部调用失败时缺省。
     */
    private ImageRecognition imageRecognition;

    public InspectionReportResponse() {
    }

    /**
     * 异常项明细。对齐 §3.4.5 abnormal_items 子字段。
     */
    @Data
    public static class AbnormalItem {
        private String itemName;
        private String value;
        private String unit;
        /** 状态：NORMAL / ABNORMAL / CRITICAL_HIGH / CRITICAL_LOW 等，影响 UI 异常标记。 */
        private String status;

        public AbnormalItem() {
        }
    }

    /**
     * 影像识别结果（由 3.4.5 内部调用 3.4.7 组装，非业务层显式调用）。
     */
    @Data
    public static class ImageRecognition {
        /** 模型标识。 */
        private String modelId;
        /** 识别结果摘要文本。 */
        private String recognitionSummary;
        /** 置信度。 */
        private Double confidence;

        public ImageRecognition() {
        }
    }
}
