package com.aimedical.modules.ai.api.dto.base;

public abstract class Phase4BusinessException extends RuntimeException {

    private final String errorCode;

    protected Phase4BusinessException(String message) {
        super(message);
        this.errorCode = null;
    }

    protected Phase4BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    protected Phase4BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
