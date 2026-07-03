package com.aimedical.modules.ai.impl.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonStructuredOutputParserTest {

    private JsonStructuredOutputParser parser;

    @BeforeEach
    void setUp() {
        parser = new JsonStructuredOutputParser(new ObjectMapper());
    }

    @Test
    void shouldParseStringLiteral() {
        String result = parser.parse("\"hello\"", String.class);
        assertEquals("hello", result);
    }

    @Test
    void shouldParseDto() {
        String json = "{\"name\":\"test\",\"value\":42}";
        TestDto result = parser.parse(json, TestDto.class);
        assertEquals("test", result.name);
        assertEquals(42, result.value);
    }

    @Test
    void shouldThrowExceptionForNullRawContent() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> parser.parse(null, String.class));
        assertTrue(ex.getMessage().contains("rawContent"));
    }

    @Test
    void shouldThrowExceptionForBlankRawContent() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> parser.parse("   ", String.class));
        assertTrue(ex.getMessage().contains("rawContent"));
    }

    @Test
    void shouldThrowExceptionForNullTargetClass() {
        assertThrows(NullPointerException.class,
            () -> parser.parse("\"hello\"", null));
    }

    @Test
    void shouldThrowExceptionForInvalidJson() {
        StructuredOutputParseException ex = assertThrows(StructuredOutputParseException.class,
            () -> parser.parse("{invalid}", String.class));
        assertTrue(ex.getMessage().contains("Failed to parse JSON"));
    }

    @Test
    void shouldThrowExceptionForTypeMismatch() {
        StructuredOutputParseException ex = assertThrows(StructuredOutputParseException.class,
            () -> parser.parse("[1,2,3]", String.class));
        assertTrue(ex.getMessage().contains("Failed to parse JSON"));
    }

    @Test
    void shouldParseWithWhitespaceSurrounding() {
        String json = "  {\"name\":\"ws\",\"value\":7}  ";
        TestDto result = parser.parse(json, TestDto.class);
        assertEquals("ws", result.name);
        assertEquals(7, result.value);
    }

    private static class TestDto {
        public String name;
        public int value;
    }
}
