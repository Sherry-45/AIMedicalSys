package com.aimedical.modules.ai.impl.parser;

public interface StructuredOutputParser {
    <T> T parse(String rawContent, Class<T> targetClass);
}
