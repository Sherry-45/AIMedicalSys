package com.aimedical.modules.ai.impl.client.exception;

public class LlmInfrastructureException extends RuntimeException {
    public LlmInfrastructureException(String message) {
        super(message);
    }
    public LlmInfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
