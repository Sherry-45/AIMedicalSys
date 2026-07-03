package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class LlmChatMessageTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldConstructWithRoleAndContent() {
        LlmChatMessage msg = new LlmChatMessage(LlmChatMessageRole.USER, "Hello");
        assertEquals(LlmChatMessageRole.USER, msg.getRole());
        assertEquals("Hello", msg.getContent());
    }

    @Test
    void shouldDefaultToNullViaNoArgConstructor() {
        LlmChatMessage msg = new LlmChatMessage();
        assertNull(msg.getRole());
        assertNull(msg.getContent());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        LlmChatMessage msg = new LlmChatMessage(LlmChatMessageRole.ASSISTANT, "Hi");
        String json = mapper.writeValueAsString(msg);
        assertTrue(json.contains("\"role\":\"ASSISTANT\""));
        assertTrue(json.contains("\"content\":\"Hi\""));
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String json = "{\"role\":\"USER\",\"content\":\"Hello\"}";
        LlmChatMessage msg = mapper.readValue(json, LlmChatMessage.class);
        assertEquals(LlmChatMessageRole.USER, msg.getRole());
        assertEquals("Hello", msg.getContent());
    }

    @Test
    void shouldRoundTripThroughJson() throws Exception {
        LlmChatMessage original = new LlmChatMessage(LlmChatMessageRole.SYSTEM, "You are a helpful assistant.");
        String json = mapper.writeValueAsString(original);
        LlmChatMessage restored = mapper.readValue(json, LlmChatMessage.class);
        assertEquals(original.getRole(), restored.getRole());
        assertEquals(original.getContent(), restored.getContent());
    }

    @Test
    void shouldRejectInvalidRoleInJson() {
        String json = "{\"role\":\"INVALID\",\"content\":\"Hello\"}";
        assertThrows(Exception.class, () -> mapper.readValue(json, LlmChatMessage.class));
    }

    @Test
    void shouldHandleNullContentInJson() throws Exception {
        String json = "{\"role\":\"USER\",\"content\":null}";
        LlmChatMessage msg = mapper.readValue(json, LlmChatMessage.class);
        assertEquals(LlmChatMessageRole.USER, msg.getRole());
        assertNull(msg.getContent());
    }
}
