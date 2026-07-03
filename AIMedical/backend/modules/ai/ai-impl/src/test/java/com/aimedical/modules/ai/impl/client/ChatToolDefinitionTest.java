package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ChatToolDefinitionTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldConstructWithAllParameters() {
        JsonNode params = mapper.createObjectNode().put("type", "object");
        ChatToolDefinition def = new ChatToolDefinition("get_weather", "Get weather", params);
        assertEquals("get_weather", def.getName());
        assertEquals("Get weather", def.getDescription());
        assertSame(params, def.getParameters());
        assertTrue(def.isStrict());
    }

    @Test
    void shouldDefaultToNullAndStrictTrueViaNoArgConstructor() {
        ChatToolDefinition def = new ChatToolDefinition();
        assertNull(def.getName());
        assertNull(def.getDescription());
        assertNull(def.getParameters());
        assertTrue(def.isStrict());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        JsonNode params = mapper.createObjectNode().put("type", "object");
        ChatToolDefinition def = new ChatToolDefinition("get_weather", "Get weather", params);
        String json = mapper.writeValueAsString(def);
        assertTrue(json.contains("\"name\":\"get_weather\""));
        assertTrue(json.contains("\"description\":\"Get weather\""));
        assertTrue(json.contains("\"strict\":true"));
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String json = "{\"name\":\"get_weather\",\"description\":\"Get weather\",\"parameters\":{\"type\":\"object\"},\"strict\":false}";
        ChatToolDefinition def = mapper.readValue(json, ChatToolDefinition.class);
        assertEquals("get_weather", def.getName());
        assertEquals("Get weather", def.getDescription());
        assertNotNull(def.getParameters());
        assertFalse(def.isStrict());
    }

    @Test
    void shouldDefaultStrictToTrueWhenMissingInJson() throws Exception {
        String json = "{\"name\":\"get_weather\",\"description\":\"Get weather\",\"parameters\":null}";
        ChatToolDefinition def = mapper.readValue(json, ChatToolDefinition.class);
        assertEquals("get_weather", def.getName());
        assertTrue(def.isStrict());
    }

    @Test
    void shouldRoundTripThroughJson() throws Exception {
        JsonNode params = mapper.createObjectNode().put("type", "object");
        ChatToolDefinition original = new ChatToolDefinition("tool", "desc", params);
        String json = mapper.writeValueAsString(original);
        ChatToolDefinition restored = mapper.readValue(json, ChatToolDefinition.class);
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getDescription(), restored.getDescription());
        assertEquals(original.getParameters(), restored.getParameters());
        assertEquals(original.isStrict(), restored.isStrict());
    }

    @Test
    void shouldDefaultToStrictTrueInSerialization() throws Exception {
        ChatToolDefinition def = new ChatToolDefinition("t", "d", null);
        String json = mapper.writeValueAsString(def);
        assertTrue(json.contains("\"strict\":true"));
    }
}
