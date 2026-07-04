package com.aimedical.modules.ai.api.dto.inspection;

import lombok.Data;

import java.util.List;

/**
 * AI 检查报告生成请求。
 * 对齐需求文档 §3.4.5 输入契约：
 * exam_id / exam_type / raw_data_ref / patient_id / exam_items(item_name/value/unit/reference_range/status)。
 */
@Data
public class InspectionReportRequest {

    /** 检查任务标识。 */
    private Long examinationId;
    /** 检查类型，如 CT / MRI / X-RAY。 */
    private String examinationType;
    /** 原始数据引用（影像类检查同时作为 3.4.7 的 image_ref）。 */
    private String rawDataRef;
    /** 患者标识。 */
    private Long patientId;
    /** 检查项目测量数据。 */
    private List<Item> items;

    /** 以下为业务上下文字段，供 AI 参考但不在 §3.4.5 契约必填范围内。 */
    private Long doctorId;
    private String bodyPart;
    private String clinicalDiagnosis;
    private String impression;
    private String conclusion;

    public InspectionReportRequest() {
    }

    /**
     * 检查项目测量数据。对齐 §3.4.5 exam_items 子字段。
     */
    @Data
    public static class Item {
        private String itemName;
        private String value;
        private String unit;
        private String referenceRange;
        /** 状态：NORMAL / ABNORMAL / CRITICAL_HIGH / CRITICAL_LOW 等。 */
        private String status;
    }
}
