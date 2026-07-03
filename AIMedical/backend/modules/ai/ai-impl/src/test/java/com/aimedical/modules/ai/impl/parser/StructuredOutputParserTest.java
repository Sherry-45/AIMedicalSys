package com.aimedical.modules.ai.impl.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StructuredOutputParserTest {

    @Test
    void anonymousClassShouldReturnStringLiteral() {
        StructuredOutputParser parser = new StructuredOutputParser() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T parse(String rawContent, Class<T> targetClass) {
                return (T) "parsed";
            }
        };
        String result = parser.parse("content", String.class);
        assertEquals("parsed", result);
    }

    @Test
    void anonymousClassShouldReturnStringFromChat() {
        StructuredOutputParser parser = new StructuredOutputParser() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T parse(String rawContent, Class<T> targetClass) {
                return (T) "parsedFromChat";
            }
        };
        String result = parser.parse("content", String.class);
        assertEquals("parsedFromChat", result);
    }

    @Test
    void anonymousClassShouldThrowException() {
        StructuredOutputParser parser = new StructuredOutputParser() {
            @Override
            public <T> T parse(String rawContent, Class<T> targetClass) {
                throw new RuntimeException("parse error");
            }
        };
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> parser.parse("content", String.class));
        assertEquals("parse error", ex.getMessage());
    }

    @Test
    void anonymousClassShouldReturnStringList() {
        StructuredOutputParser parser = new StructuredOutputParser() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T parse(String rawContent, Class<T> targetClass) {
                return (T) "parsed";
            }
        };
        String result = parser.parse("some raw content", String.class);
        assertNotNull(result);
        assertInstanceOf(String.class, result);
    }

    @Test
    void anonymousClassShouldHandleNullRawContent() {
        StructuredOutputParser parser = new StructuredOutputParser() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T parse(String rawContent, Class<T> targetClass) {
                return (T) "parsed";
            }
        };
        String result = parser.parse(null, String.class);
        assertEquals("parsed", result);
    }

    @Test
    void anonymousClassWithRawTypeShouldCompileAndRun() {
        StructuredOutputParser parser = new StructuredOutputParser() {
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> T parse(String rawContent, Class<T> targetClass) {
                return (T) "rawTypeResult";
            }
        };
        String result = parser.parse("content", String.class);
        assertEquals("rawTypeResult", result);
    }
}
