package com.aimedical.modules.ai.impl.client.exception;

public class StructuredOutputNotSupportedException extends RuntimeException {
    public StructuredOutputNotSupportedException(String message) {
        super(message);
    }
    public StructuredOutputNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
