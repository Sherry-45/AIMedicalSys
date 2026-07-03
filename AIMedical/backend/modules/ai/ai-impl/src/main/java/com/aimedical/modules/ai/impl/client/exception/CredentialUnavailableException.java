package com.aimedical.modules.ai.impl.client.exception;

public class CredentialUnavailableException extends RuntimeException {
    public CredentialUnavailableException(String message) {
        super(message);
    }
    public CredentialUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
