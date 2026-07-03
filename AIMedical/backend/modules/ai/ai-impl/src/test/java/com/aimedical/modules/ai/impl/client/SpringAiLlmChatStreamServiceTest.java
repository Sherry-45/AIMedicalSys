package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;
import org.junit.jupiter.api.Test;

class SpringAiLlmChatStreamServiceTest {

    private final SpringAiLlmChatStreamService service = new SpringAiLlmChatStreamService();

    @Test
    void shouldThrowOnChatStream() {
        LlmChatRequest request = new LlmChatRequest();
        LlmInfrastructureException ex = assertThrows(
                LlmInfrastructureException.class,
                () -> service.chatStream(request));
        assertEquals("Spring AI not available", ex.getMessage());
    }
}
