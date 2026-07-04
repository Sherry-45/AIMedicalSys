package com.aimedical.modules.ai.api.dto.labtest;

import lombok.Data;

import java.util.List;

/**
 * AI 检验报告生成请求。
 * 对齐需求文档 §3.4.6 输入契约：
 * lab_id / lab_items(item_name/value/unit/reference_range/status) / patient_id。
 */
@Data
public class LabTestReportRequest {

    /** 检验任务标识。 */
    private Long labTestId;
    /** 患者标识。 */
    private Long patientId;
    /** 检验项目结果数据。 */
    private List<Item> items;

    /** 以下为业务上下文字段，供 AI 参考但不在 §3.4.6 契约必填范围内。 */
    private Long doctorId;
    private String testType;
    private String sampleType;
    private String reportConclusion;

    public LabTestReportRequest() {
    }

    /**
     * 检验项目结果数据。对齐 §3.4.6 lab_items 子字段。
     */
    @Data
    public static class Item {
        private String itemName;
        /** 结果值。 */
        private String value;
        private String unit;
        private String referenceRange;
        /** 状态：NORMAL / ABNORMAL / CRITICAL_HIGH / CRITICAL_LOW 等。 */
        private String status;
    }
}
