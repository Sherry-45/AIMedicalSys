package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import com.aimedical.modules.ai.api.AiResult;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class LlmChatServiceTest {

    @Test
    void shouldDeclareChatMethod() throws Exception {
        Method method = LlmChatService.class.getMethod("chat", LlmChatRequest.class);
        assertEquals(CompletableFuture.class, method.getReturnType());
        assertEquals(LlmChatRequest.class, method.getParameterTypes()[0]);
    }

    @Test
    void shouldDeclareStructuredChatMethod() throws Exception {
        Method method = LlmChatService.class.getMethod("structuredChat", LlmChatRequest.class, Class.class);
        assertEquals(CompletableFuture.class, method.getReturnType());
        assertEquals(2, method.getParameterCount());
        assertEquals(LlmChatRequest.class, method.getParameterTypes()[0]);
        assertEquals(Class.class, method.getParameterTypes()[1]);
    }

    @Test
    void chatMethodShouldReturnAiResultOfLlmChatResponse() throws Exception {
        Method method = LlmChatService.class.getMethod("chat", LlmChatRequest.class);
        var genericReturn = method.getGenericReturnType();
        assertTrue(genericReturn.getTypeName().contains("AiResult"),
            "Expected AiResult in return type but was: " + genericReturn.getTypeName());
        assertTrue(genericReturn.getTypeName().contains("LlmChatResponse"),
            "Expected LlmChatResponse in return type but was: " + genericReturn.getTypeName());
    }

    @Test
    void structuredChatMethodShouldReturnAiResultOfStructuredChatResult() throws Exception {
        Method method = LlmChatService.class.getMethod("structuredChat", LlmChatRequest.class, Class.class);
        var genericReturn = method.getGenericReturnType();
        assertTrue(genericReturn.getTypeName().contains("StructuredChatResult"),
            "Expected StructuredChatResult in return type but was: " + genericReturn.getTypeName());
    }

    @Test
    void shouldBePublicInterface() {
        assertTrue(LlmChatService.class.isInterface());
    }

    @Test
    void shouldDeclareGetClientTypeMethod() throws Exception {
        Method method = LlmChatService.class.getMethod("getClientType");
        assertEquals(ClientType.class, method.getReturnType());
        assertEquals(0, method.getParameterCount());
    }
}
