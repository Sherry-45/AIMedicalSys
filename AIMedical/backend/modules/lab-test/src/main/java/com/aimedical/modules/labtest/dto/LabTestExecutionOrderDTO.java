package com.aimedical.modules.labtest.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 执行顺序推荐结果 DTO（检验域）。
 * 对齐需求 §3.4.11 输出契约：execution_order / summary / disclaimer_required。
 */
@Data
public class LabTestExecutionOrderDTO {

    /** 执行顺序，每项含 taskId / priority / recommendedTime / reason。 */
    private List<OrderItem> executionOrder;

    /** 整体顺序说明。 */
    private String summary;

    /** 是否必须显示免责声明。 */
    private Boolean disclaimerRequired;

    /** 是否为降级结果（AI 不可用时回退到 FIFO 手动排序）。 */
    private boolean degraded;

    /**
     * 执行顺序项。
     */
    @Data
    public static class OrderItem {
        /** 任务标识。 */
        private Long taskId;
        /** 优先级：P1 / P2 / P3。 */
        private String priority;
        /** 建议执行时间。 */
        private String recommendedTime;
        /** 排序理由。 */
        private String reason;
    }
}
