package com.aimedical.modules.doctor.dto.response;

/**
 * AI 讨论结论生成响应。
 *
 * <p>注意：当前 AI 能力 {@code discussionConclusion} 的响应定义为空对象，
 * 此 DTO 预留结论摘要字段以便后续扩展；降级路径下使用兜底文本填充。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public record AiDiscussionConclusionResponse(
    String conclusionSummary,
    String keyPoints,
    String actionItems
) {
}
