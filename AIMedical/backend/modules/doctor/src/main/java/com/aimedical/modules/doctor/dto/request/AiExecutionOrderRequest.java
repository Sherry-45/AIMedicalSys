package com.aimedical.modules.doctor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * AI 执行顺序推荐请求。
 *
 * <p>医技端（影像/检验）调用 AI 对多个任务进行执行顺序排序；AI 不可用时降级为按紧急度+提交时间排序。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public record AiExecutionOrderRequest(
    @NotEmpty List<TaskItem> taskItems,
    Context context,
    @NotBlank @Size(max = 50) String taskRole
) {

    /**
     * 任务项。
     */
    public record TaskItem(
        Long taskId,
        @Size(max = 50) String taskType,
        @Size(max = 100) String itemName,
        @Size(max = 20) String urgencyHint,
        Long patientId
    ) {
    }

    /**
     * 上下文信息。
     */
    public record Context(
        @Size(max = 50) String encounterId,
        @Size(max = 50) String departmentId,
        List<String> availableResources
    ) {
    }
}
