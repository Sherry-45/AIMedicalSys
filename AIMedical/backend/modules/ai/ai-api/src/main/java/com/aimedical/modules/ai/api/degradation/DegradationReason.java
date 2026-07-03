package com.aimedical.modules.ai.api.degradation;

public enum DegradationReason {

    NO_AVAILABLE_ROUTE("NoAvailableRoute", "No available model route"),
    ENDPOINT_UNAVAILABLE("EndpointUnavailable", "Endpoint is unavailable"),
    CIRCUIT_BREAKER_OPEN("CircuitBreakerOpen", "Circuit breaker is open"),
    PARSE_FAILURE("ParseFailure", "LLM output parse failure"),
    TIMEOUT("Timeout", "Request timeout"),
    STRATEGY_TRIGGERED("StrategyTriggered", "Degradation strategy triggered"),
    INTERNAL_ERROR("InternalError", "Internal error"),
    INFRASTRUCTURE_ERROR("InfrastructureError", "Infrastructure error");

    private final String code;
    private final String message;

    DegradationReason(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static DegradationReason fromCode(String code) {
        for (DegradationReason reason : values()) {
            if (reason.code.equals(code)) {
                return reason;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return code;
    }
}
