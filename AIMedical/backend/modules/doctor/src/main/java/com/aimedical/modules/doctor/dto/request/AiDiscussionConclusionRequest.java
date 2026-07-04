package com.aimedical.modules.doctor.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * AI 讨论结论生成请求。
 *
 * <p>医生端调用 AI 对疑难病例讨论的发言记录生成结论摘要；AI 不可用时降级提示人工归纳。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public record AiDiscussionConclusionRequest(
    @NotEmpty List<Transcript> transcripts
) {

    /**
     * 讨论发言记录。
     */
    public record Transcript(
        @Size(max = 50) String speakerRole,
        @Size(max = 100) String speakerName,
        @Size(max = 50) String timestamp,
        @Size(max = 2000) String content
    ) {
    }
}
