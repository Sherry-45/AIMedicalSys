package com.aimedical.modules.ai.api.dto.execution;

import lombok.Data;

import java.util.List;

/**
 * AI 执行顺序推荐响应。
 * 严格对齐需求文档 §3.4.11 输出契约：
 * execution_order(task_id/priority/recommended_time/reason) / summary / disclaimer_required。
 */
@Data
public class ExecutionOrderResponse {

    /** 执行顺序，每项含 task_id / priority / recommended_time / reason。 */
    private List<OrderItem> executionOrder;

    /** 整体顺序说明。 */
    private String summary;

    /** 是否必须显示免责声明，固定 true。 */
    private Boolean disclaimerRequired;

    public ExecutionOrderResponse() {
    }

    /**
     * 执行顺序项。对齐 §3.4.11 execution_order 子字段。
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

        public OrderItem() {
        }
    }
}
