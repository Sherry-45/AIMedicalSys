package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.Test;

class StructuredChatResultTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldConstructWithAllParameters() {
        LlmChatResponse.LlmChatUsage usage = new LlmChatResponse.LlmChatUsage(10, 20, 30);
        StructuredChatResult<String> result = new StructuredChatResult<>("data", 2, usage);
        assertEquals("data", result.getData());
        assertEquals(2, result.getRetryCount());
        assertSame(usage, result.getUsage());
    }

    @Test
    void shouldDefaultToNullAndZeroViaNoArgConstructor() {
        StructuredChatResult<String> result = new StructuredChatResult<>();
        assertNull(result.getData());
        assertEquals(0, result.getRetryCount());
        assertNull(result.getUsage());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        LlmChatResponse.LlmChatUsage usage = new LlmChatResponse.LlmChatUsage(1, 2, 3);
        StructuredChatResult<String> result = new StructuredChatResult<>("output", 0, usage);
        String json = mapper.writeValueAsString(result);
        assertTrue(json.contains("\"data\":\"output\""));
        assertTrue(json.contains("\"retryCount\":0"));
        assertTrue(json.contains("\"promptTokens\":1"));
    }

    @Test
    void shouldDeserializeStringDataFromJson() throws Exception {
        String json = "{\"data\":\"output\",\"retryCount\":1,\"usage\":{\"promptTokens\":5,\"completionTokens\":10,\"totalTokens\":15}}";
        StructuredChatResult<String> result = mapper.readValue(json,
            TypeFactory.defaultInstance().constructParametricType(StructuredChatResult.class, String.class));
        assertEquals("output", result.getData());
        assertEquals(1, result.getRetryCount());
        assertEquals(5, result.getUsage().getPromptTokens());
        assertEquals(10, result.getUsage().getCompletionTokens());
        assertEquals(15, result.getUsage().getTotalTokens());
    }

    @Test
    void shouldDeserializeNumericDataFromJson() throws Exception {
        String json = "{\"data\":42,\"retryCount\":0,\"usage\":{\"promptTokens\":0,\"completionTokens\":0,\"totalTokens\":0}}";
        StructuredChatResult<Integer> result = mapper.readValue(json,
            TypeFactory.defaultInstance().constructParametricType(StructuredChatResult.class, Integer.class));
        assertEquals(42, result.getData());
    }

    @Test
    void shouldRoundTripThroughJson() throws Exception {
        LlmChatResponse.LlmChatUsage usage = new LlmChatResponse.LlmChatUsage(10, 20, 30);
        StructuredChatResult<String> original = new StructuredChatResult<>("hello", 1, usage);
        String json = mapper.writeValueAsString(original);
        StructuredChatResult<String> restored = mapper.readValue(json,
            TypeFactory.defaultInstance().constructParametricType(StructuredChatResult.class, String.class));
        assertEquals(original.getData(), restored.getData());
        assertEquals(original.getRetryCount(), restored.getRetryCount());
        assertEquals(original.getUsage().getPromptTokens(), restored.getUsage().getPromptTokens());
        assertEquals(original.getUsage().getCompletionTokens(), restored.getUsage().getCompletionTokens());
        assertEquals(original.getUsage().getTotalTokens(), restored.getUsage().getTotalTokens());
    }

    @Test
    void shouldHandleNullDataInJson() throws Exception {
        String json = "{\"data\":null,\"retryCount\":0,\"usage\":null}";
        StructuredChatResult<String> result = mapper.readValue(json,
            TypeFactory.defaultInstance().constructParametricType(StructuredChatResult.class, String.class));
        assertNull(result.getData());
        assertNull(result.getUsage());
    }
}
