package com.aimedical.modules.ai.api.dto.execution;

import lombok.Data;

import java.util.List;

/**
 * AI 执行顺序推荐请求。
 * 严格对齐需求文档 §3.4.11 输入契约：
 * task_items(task_id/task_type/item_name/urgency_hint/patient_id) /
 * context(encounter_id/department_id/available_resources) / task_role。
 */
@Data
public class ExecutionOrderRequest {

    /** 任务列表（必填）。 */
    private List<TaskItem> taskItems;

    /** 上下文信息（可选）。 */
    private Context context;

    /** 调用方岗位（必填）：IMAGING_DOCTOR / LAB_DOCTOR。 */
    private String taskRole;

    public ExecutionOrderRequest() {
    }

    /**
     * 任务项。对齐 §3.4.11 task_items 子字段。
     */
    @Data
    public static class TaskItem {
        /** 任务标识。 */
        private Long taskId;
        /** 任务类型：IMAGING / LAB。 */
        private String taskType;
        /** 检查/检验项目名称。 */
        private String itemName;
        /** 紧急度提示：LOW / MEDIUM / HIGH。 */
        private String urgencyHint;
        /** 患者标识。 */
        private Long patientId;

        public TaskItem() {
        }
    }

    /**
     * 上下文信息。对齐 §3.4.11 context 子字段。
     */
    @Data
    public static class Context {
        private String encounterId;
        private String departmentId;
        /** 可用资源列表。 */
        private List<String> availableResources;

        public Context() {
        }
    }
}
