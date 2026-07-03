package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class LlmChatResponseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldConstructWithAllParameters() {
        LlmChatResponse.LlmChatUsage usage = new LlmChatResponse.LlmChatUsage(10, 20, 30);
        LlmChatResponse resp = new LlmChatResponse("Hello", usage, "gpt-4", 1);
        assertEquals("Hello", resp.getContent());
        assertSame(usage, resp.getUsage());
        assertEquals("gpt-4", resp.getModelId());
        assertEquals(1, resp.getRetryCount());
    }

    @Test
    void shouldDefaultToNullAndZeroViaNoArgConstructor() {
        LlmChatResponse resp = new LlmChatResponse();
        assertNull(resp.getContent());
        assertNull(resp.getUsage());
        assertNull(resp.getModelId());
        assertEquals(0, resp.getRetryCount());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        LlmChatResponse.LlmChatUsage usage = new LlmChatResponse.LlmChatUsage(10, 20, 30);
        LlmChatResponse resp = new LlmChatResponse("Hello", usage, "gpt-4", 1);
        String json = mapper.writeValueAsString(resp);
        assertTrue(json.contains("\"content\":\"Hello\""));
        assertTrue(json.contains("\"modelId\":\"gpt-4\""));
        assertTrue(json.contains("\"retryCount\":1"));
        assertTrue(json.contains("\"promptTokens\":10"));
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String json = "{\"content\":\"Hello\",\"usage\":{\"promptTokens\":10,\"completionTokens\":20,\"totalTokens\":30},\"modelId\":\"gpt-4\",\"retryCount\":1}";
        LlmChatResponse resp = mapper.readValue(json, LlmChatResponse.class);
        assertEquals("Hello", resp.getContent());
        assertEquals("gpt-4", resp.getModelId());
        assertEquals(1, resp.getRetryCount());
        assertNotNull(resp.getUsage());
        assertEquals(10, resp.getUsage().getPromptTokens());
        assertEquals(20, resp.getUsage().getCompletionTokens());
        assertEquals(30, resp.getUsage().getTotalTokens());
    }

    @Test
    void shouldRoundTripThroughJson() throws Exception {
        LlmChatResponse.LlmChatUsage usage = new LlmChatResponse.LlmChatUsage(5, 15, 20);
        LlmChatResponse original = new LlmChatResponse("Hi", usage, "gpt-3.5", 0);
        String json = mapper.writeValueAsString(original);
        LlmChatResponse restored = mapper.readValue(json, LlmChatResponse.class);
        assertEquals(original.getContent(), restored.getContent());
        assertEquals(original.getModelId(), restored.getModelId());
        assertEquals(original.getRetryCount(), restored.getRetryCount());
        assertEquals(original.getUsage().getPromptTokens(), restored.getUsage().getPromptTokens());
        assertEquals(original.getUsage().getCompletionTokens(), restored.getUsage().getCompletionTokens());
        assertEquals(original.getUsage().getTotalTokens(), restored.getUsage().getTotalTokens());
    }

    @Test
    void shouldHandleNullUsageInJson() throws Exception {
        String json = "{\"content\":\"Hello\",\"usage\":null,\"modelId\":\"gpt-4\",\"retryCount\":0}";
        LlmChatResponse resp = mapper.readValue(json, LlmChatResponse.class);
        assertNull(resp.getUsage());
    }
}
