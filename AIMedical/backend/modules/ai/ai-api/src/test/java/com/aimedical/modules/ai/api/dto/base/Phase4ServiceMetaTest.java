package com.aimedical.modules.ai.api.dto.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Phase4ServiceMetaTest {

    @Test
    void shouldConstructWithAllFields() {
        Phase4ServiceMeta meta = new Phase4ServiceMeta("gpt4", 1, 3);
        assertEquals("gpt4", meta.getModelId());
        assertEquals(1, meta.getPromptVersion());
        assertEquals(3, meta.getRetryCount());
    }

    @Test
    void shouldSupportNullFields() {
        Phase4ServiceMeta meta = new Phase4ServiceMeta(null, null, 0);
        assertNull(meta.getModelId());
        assertNull(meta.getPromptVersion());
        assertEquals(0, meta.getRetryCount());
    }

    @Test
    void shouldSupportJacksonSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        Phase4ServiceMeta original = new Phase4ServiceMeta("gpt4", 2, 1);
        String json = mapper.writeValueAsString(original);
        Phase4ServiceMeta deserialized = mapper.readValue(json, Phase4ServiceMeta.class);
        assertEquals(original, deserialized);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Phase4ServiceMeta meta1 = new Phase4ServiceMeta("gpt4", 1, 3);
        Phase4ServiceMeta meta2 = new Phase4ServiceMeta("gpt4", 1, 3);
        assertEquals(meta1, meta2);
        assertEquals(meta1.hashCode(), meta2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentModelId() {
        Phase4ServiceMeta meta1 = new Phase4ServiceMeta("gpt4", 1, 3);
        Phase4ServiceMeta meta2 = new Phase4ServiceMeta("gpt3", 1, 3);
        assertNotEquals(meta1, meta2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentPromptVersion() {
        Phase4ServiceMeta meta1 = new Phase4ServiceMeta("gpt4", 1, 3);
        Phase4ServiceMeta meta2 = new Phase4ServiceMeta("gpt4", 2, 3);
        assertNotEquals(meta1, meta2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentRetryCount() {
        Phase4ServiceMeta meta1 = new Phase4ServiceMeta("gpt4", 1, 3);
        Phase4ServiceMeta meta2 = new Phase4ServiceMeta("gpt4", 1, 5);
        assertNotEquals(meta1, meta2);
    }

    @Test
    void shouldNotBeEqualWhenComparedToDifferentType() {
        Phase4ServiceMeta meta = new Phase4ServiceMeta("gpt4", 1, 3);
        assertNotEquals(meta, "some string");
    }
}
