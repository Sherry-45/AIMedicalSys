package com.aimedical.modules.ai.impl.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class JsonStructuredOutputParser implements StructuredOutputParser {

    private static final Logger log = LoggerFactory.getLogger(JsonStructuredOutputParser.class);

    private final ObjectMapper objectMapper;

    public JsonStructuredOutputParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T parse(String rawContent, Class<T> targetClass) {
        if (rawContent == null || rawContent.isBlank()) {
            throw new IllegalArgumentException("rawContent must not be null or blank");
        }
        Objects.requireNonNull(targetClass, "targetClass must not be null");
        try {
            return objectMapper.readValue(rawContent, targetClass);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON content to {}", targetClass.getSimpleName(), e);
            throw new StructuredOutputParseException("Failed to parse JSON content to " + targetClass.getSimpleName(), e);
        }
    }
}
