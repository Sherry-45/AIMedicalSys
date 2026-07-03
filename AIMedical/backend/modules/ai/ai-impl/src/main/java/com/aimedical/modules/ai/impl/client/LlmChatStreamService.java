package com.aimedical.modules.ai.impl.client;

import reactor.core.publisher.Flux;

public interface LlmChatStreamService {
    Flux<LlmChatResponse> chatStream(LlmChatRequest request);
}
