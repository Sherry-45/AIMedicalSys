package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LlmChatMessageRoleTest {

    @Test
    void shouldContainSystem() {
        assertNotNull(LlmChatMessageRole.valueOf("SYSTEM"));
    }

    @Test
    void shouldContainUser() {
        assertNotNull(LlmChatMessageRole.valueOf("USER"));
    }

    @Test
    void shouldContainAssistant() {
        assertNotNull(LlmChatMessageRole.valueOf("ASSISTANT"));
    }

    @Test
    void shouldHaveExactlyThreeConstants() {
        assertEquals(3, LlmChatMessageRole.values().length);
    }

    @Test
    void shouldThrowOnInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> LlmChatMessageRole.valueOf("INVALID"));
    }
}
