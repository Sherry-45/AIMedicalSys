package com.aimedical.modules.ai.api.dto.labtest;

import lombok.Data;

import java.util.List;

/**
 * AI 检验报告生成响应。
 * 严格对齐需求文档 §3.4.6 输出契约：
 * report_draft / abnormal_items(含 delta) / interpretation / suggestions / confidence。
 */
@Data
public class LabTestReportResponse {

    /** 报告草稿文本。 */
    private String reportDraft;

    /** 异常项列表，每项含 item_name/value/unit/reference_range/status/delta。 */
    private List<AbnormalItem> abnormalItems;

    /** 结果智能解读。 */
    private String interpretation;

    /** 进一步建议。 */
    private List<String> suggestions;

    /** 置信度，取值范围 0–100。 */
    private Double confidence;

    public LabTestReportResponse() {
    }

    /**
     * 异常项明细。对齐 §3.4.6 abnormal_items 子字段。
     */
    @Data
    public static class AbnormalItem {
        private String itemName;
        private String value;
        private String unit;
        private String referenceRange;
        private String status;
        /** 与上一次同项目结果的差值（趋势分析）。 */
        private String delta;

        public AbnormalItem() {
        }
    }
}
