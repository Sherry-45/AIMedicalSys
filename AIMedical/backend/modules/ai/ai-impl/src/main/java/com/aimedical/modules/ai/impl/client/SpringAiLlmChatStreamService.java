package com.aimedical.modules.ai.impl.client;

import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;
import reactor.core.publisher.Flux;

public class SpringAiLlmChatStreamService implements LlmChatStreamService {

    public SpringAiLlmChatStreamService() {
    }

    @Override
    public Flux<LlmChatResponse> chatStream(LlmChatRequest request) {
        throw new LlmInfrastructureException("Spring AI not available");
    }
}
