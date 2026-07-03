package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class LlmChatRequestTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldConstructWithAllParameters() {
        List<LlmChatMessage> messages = List.of(new LlmChatMessage(LlmChatMessageRole.USER, "Hi"));
        LlmChatOptions options = new LlmChatOptions("gpt-4", 0.7, 100, null, null, null, null);
        List<ChatToolDefinition> tools = List.of(new ChatToolDefinition("tool1", "desc", null));
        LlmChatRequest req = new LlmChatRequest(messages, options, ClientType.HTTP_API, tools, null);
        assertSame(messages, req.getMessages());
        assertSame(options, req.getOptions());
        assertEquals(ClientType.HTTP_API, req.getClientType());
        assertSame(tools, req.getTools());
    }

    @Test
    void shouldDefaultToNullViaNoArgConstructor() {
        LlmChatRequest req = new LlmChatRequest();
        assertNull(req.getMessages());
        assertNull(req.getOptions());
        assertNull(req.getClientType());
        assertNull(req.getTools());
        assertNull(req.getEndpointId());
        assertNull(req.getEndpointUrl());
    }

    @Test
    void shouldSetEndpointUrlViaSixParamConstructor() {
        List<LlmChatMessage> messages = List.of(new LlmChatMessage(LlmChatMessageRole.USER, "Hi"));
        LlmChatOptions options = new LlmChatOptions("gpt-4", 0.7, 100, null, null, null, null);
        List<ChatToolDefinition> tools = List.of(new ChatToolDefinition("tool1", "desc", null));
        LlmChatRequest req = new LlmChatRequest(messages, options, ClientType.HTTP_API, tools, "ep-id", "http://custom-url");
        assertEquals("http://custom-url", req.getEndpointUrl());
        assertEquals("ep-id", req.getEndpointId());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        LlmChatRequest req = new LlmChatRequest(
            List.of(new LlmChatMessage(LlmChatMessageRole.USER, "Hi")),
            new LlmChatOptions("gpt-4", 0.7, 100, null, null, null, null),
            ClientType.HTTP_API,
            List.of(new ChatToolDefinition("tool1", "desc", null)),
            null);
        String json = mapper.writeValueAsString(req);
        assertTrue(json.contains("\"clientType\":\"HTTP_API\""));
        assertTrue(json.contains("\"modelId\":\"gpt-4\""));
        assertTrue(json.contains("\"messages\""));
        assertTrue(json.contains("\"tools\""));
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String json = "{\"messages\":[{\"role\":\"USER\",\"content\":\"Hi\"}],"
            + "\"options\":{\"modelId\":\"gpt-4\"},"
            + "\"clientType\":\"HTTP_API\","
            + "\"tools\":[{\"name\":\"tool1\",\"description\":\"desc\",\"parameters\":null}]}";
        LlmChatRequest req = mapper.readValue(json, LlmChatRequest.class);
        assertEquals(1, req.getMessages().size());
        assertEquals(LlmChatMessageRole.USER, req.getMessages().get(0).getRole());
        assertEquals("gpt-4", req.getOptions().getModelId());
        assertEquals(ClientType.HTTP_API, req.getClientType());
        assertEquals(1, req.getTools().size());
        assertEquals("tool1", req.getTools().get(0).getName());
    }

    @Test
    void shouldRoundTripThroughJson() throws Exception {
        LlmChatRequest original = new LlmChatRequest(
            List.of(new LlmChatMessage(LlmChatMessageRole.SYSTEM, "Be helpful.")),
            new LlmChatOptions("gpt-4", 0.7, 100, null, null, null, null),
            ClientType.SPRING_AI, null, null);
        String json = mapper.writeValueAsString(original);
        LlmChatRequest restored = mapper.readValue(json, LlmChatRequest.class);
        assertEquals(original.getClientType(), restored.getClientType());
        assertEquals(1, restored.getMessages().size());
        assertEquals(original.getMessages().get(0).getRole(), restored.getMessages().get(0).getRole());
        assertEquals(original.getMessages().get(0).getContent(), restored.getMessages().get(0).getContent());
    }

    @Test
    void shouldRejectInvalidClientTypeInJson() {
        String json = "{\"clientType\":\"INVALID\"}";
        assertThrows(Exception.class, () -> mapper.readValue(json, LlmChatRequest.class));
    }
}
