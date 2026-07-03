package com.aimedical.modules.ai.api;

import java.util.Objects;

public class AiResult<T> {

    private final boolean success;
    private final T data;
    private final String errorCode;
    private final boolean degraded;
    private final String fallbackReason;

    public AiResult() {
        this(false, null, null, false, null);
    }

    public AiResult(boolean success, T data, String errorCode, boolean degraded, String fallbackReason) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.degraded = degraded;
        this.fallbackReason = fallbackReason;
    }

    public static <T> AiResult<T> success(T data) {
        return new AiResult<>(true, Objects.requireNonNull(data), null, false, null);
    }

    public static <T> AiResult<T> failure(String errorCode) {
        return new AiResult<>(false, null, errorCode, false, null);
    }

    public static <T> AiResult<T> failure(String errorCode, String message) {
        return new AiResult<>(false, null, errorCode, false, message);
    }

    public static <T> AiResult<T> degraded(String fallbackReason) {
        return new AiResult<>(false, null, null, true, fallbackReason);
    }

    public static <T> AiResult<T> degradedWithErrorCode(String errorCode, String fallbackReason) {
        return new AiResult<>(false, null, errorCode, true, fallbackReason);
    }

    public static <T> AiResult<T> degraded(T data, String fallbackReason) {
        return new AiResult<>(false, data, null, true, fallbackReason);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isDegraded() {
        return degraded;
    }

    public String getFallbackReason() {
        return fallbackReason;
    }
}