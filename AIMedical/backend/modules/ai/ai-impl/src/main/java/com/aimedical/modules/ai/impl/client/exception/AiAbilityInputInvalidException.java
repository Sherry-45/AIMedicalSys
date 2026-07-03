package com.aimedical.modules.ai.impl.client.exception;

public class AiAbilityInputInvalidException extends RuntimeException {
    public AiAbilityInputInvalidException(String message) {
        super(message);
    }
    public AiAbilityInputInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
