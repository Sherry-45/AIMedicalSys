package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;
import org.junit.jupiter.api.Test;

class SpringAiLlmChatServiceTest {

    private final SpringAiLlmChatService service = new SpringAiLlmChatService();

    @Test
    void shouldReturnSpringAiClientType() {
        assertEquals(ClientType.SPRING_AI, service.getClientType());
    }

    @Test
    void shouldThrowOnChat() {
        LlmChatRequest request = new LlmChatRequest();
        LlmInfrastructureException ex = assertThrows(
                LlmInfrastructureException.class,
                () -> service.chat(request));
        assertEquals("Spring AI not available", ex.getMessage());
    }

    @Test
    void shouldThrowOnStructuredChat() {
        LlmChatRequest request = new LlmChatRequest();
        assertThrows(LlmInfrastructureException.class,
                () -> service.structuredChat(request, String.class));
    }
}
