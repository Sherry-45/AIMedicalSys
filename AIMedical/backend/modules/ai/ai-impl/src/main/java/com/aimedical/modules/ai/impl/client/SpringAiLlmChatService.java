package com.aimedical.modules.ai.impl.client;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;
import java.util.concurrent.CompletableFuture;

public class SpringAiLlmChatService implements LlmChatService {

    public SpringAiLlmChatService() {
    }

    @Override
    public ClientType getClientType() {
        return ClientType.SPRING_AI;
    }

    @Override
    public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request) {
        throw new LlmInfrastructureException("Spring AI not available");
    }

    @Override
    public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
            LlmChatRequest request, Class<T> targetClass) {
        throw new LlmInfrastructureException("Spring AI not available");
    }
}
