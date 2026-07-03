package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class HttpApiLlmChatStreamServiceTest {

    @Test
    void shouldReturnEmptyFlux() {
        HttpApiLlmChatStreamService service = new HttpApiLlmChatStreamService(null, null);
        Flux<LlmChatResponse> flux = service.chatStream(new LlmChatRequest());
        StepVerifier.create(flux)
                .expectNextCount(0)
                .verifyComplete();
    }
}
