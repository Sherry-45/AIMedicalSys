package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

class LlmChatStreamServiceTest {

    @Test
    void shouldDeclareChatStreamMethod() throws Exception {
        Method method = LlmChatStreamService.class.getMethod("chatStream", LlmChatRequest.class);
        assertEquals(Flux.class, method.getReturnType());
        assertEquals(1, method.getParameterCount());
        assertEquals(LlmChatRequest.class, method.getParameterTypes()[0]);
    }

    @Test
    void shouldBePublicInterface() {
        assertTrue(LlmChatStreamService.class.isInterface());
        assertTrue(Modifier.isPublic(LlmChatStreamService.class.getModifiers()));
    }
}
