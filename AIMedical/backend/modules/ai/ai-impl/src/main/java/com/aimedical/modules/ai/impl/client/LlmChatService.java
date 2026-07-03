package com.aimedical.modules.ai.impl.client;

import com.aimedical.modules.ai.api.AiResult;
import java.util.concurrent.CompletableFuture;

public interface LlmChatService {

    CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request);

    <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
            LlmChatRequest request, Class<T> targetClass);

    ClientType getClientType();
}
