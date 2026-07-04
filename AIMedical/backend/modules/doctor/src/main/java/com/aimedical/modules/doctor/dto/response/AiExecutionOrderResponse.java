package com.aimedical.modules.doctor.dto.response;

import java.util.List;

/**
 * AI 执行顺序推荐响应。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public record AiExecutionOrderResponse(
    List<OrderItem> executionOrder,
    String summary,
    Boolean disclaimerRequired
) {

    /**
     * 执行顺序项。
     */
    public record OrderItem(
        Long taskId,
        String priority,
        String recommendedTime,
        String reason
    ) {
    }
}
