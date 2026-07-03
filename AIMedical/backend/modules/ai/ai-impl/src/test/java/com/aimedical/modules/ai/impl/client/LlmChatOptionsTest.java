package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class LlmChatOptionsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldConstructWithAllParameters() {
        List<String> stop = List.of("stop1", "stop2");
        LlmChatOptions opts = new LlmChatOptions("gpt-4", 0.7, 100, stop, 0.9, 0.5, 0.3);
        assertEquals("gpt-4", opts.getModelId());
        assertEquals(0.7, opts.getTemperature());
        assertEquals(100, opts.getMaxTokens());
        assertEquals(stop, opts.getStopSequences());
        assertEquals(0.9, opts.getTopP());
        assertEquals(0.5, opts.getFrequencyPenalty());
        assertEquals(0.3, opts.getPresencePenalty());
    }

    @Test
    void shouldDefaultToNullViaNoArgConstructor() {
        LlmChatOptions opts = new LlmChatOptions();
        assertNull(opts.getModelId());
        assertNull(opts.getTemperature());
        assertNull(opts.getMaxTokens());
        assertNull(opts.getStopSequences());
        assertNull(opts.getTopP());
        assertNull(opts.getFrequencyPenalty());
        assertNull(opts.getPresencePenalty());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        LlmChatOptions opts = new LlmChatOptions("gpt-4", 0.7, 100, null, null, null, null);
        String json = mapper.writeValueAsString(opts);
        assertTrue(json.contains("\"modelId\":\"gpt-4\""));
        assertTrue(json.contains("\"temperature\":0.7"));
        assertTrue(json.contains("\"maxTokens\":100"));
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String json = "{\"modelId\":\"gpt-4\",\"temperature\":0.7,\"maxTokens\":100}";
        LlmChatOptions opts = mapper.readValue(json, LlmChatOptions.class);
        assertEquals("gpt-4", opts.getModelId());
        assertEquals(0.7, opts.getTemperature());
        assertEquals(100, opts.getMaxTokens());
        assertNull(opts.getStopSequences());
        assertNull(opts.getTopP());
    }

    @Test
    void shouldRoundTripThroughJson() throws Exception {
        List<String> stop = List.of("stop1");
        LlmChatOptions original = new LlmChatOptions("gpt-4", 0.7, 100, stop, 0.9, 0.5, 0.3);
        String json = mapper.writeValueAsString(original);
        LlmChatOptions restored = mapper.readValue(json, LlmChatOptions.class);
        assertEquals(original.getModelId(), restored.getModelId());
        assertEquals(original.getTemperature(), restored.getTemperature());
        assertEquals(original.getMaxTokens(), restored.getMaxTokens());
        assertEquals(original.getStopSequences(), restored.getStopSequences());
        assertEquals(original.getTopP(), restored.getTopP());
        assertEquals(original.getFrequencyPenalty(), restored.getFrequencyPenalty());
        assertEquals(original.getPresencePenalty(), restored.getPresencePenalty());
    }

    @Test
    void shouldHandleEmptyJsonObject() throws Exception {
        LlmChatOptions opts = mapper.readValue("{}", LlmChatOptions.class);
        assertNull(opts.getModelId());
        assertNull(opts.getTemperature());
    }
}
