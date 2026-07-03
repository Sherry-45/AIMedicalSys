package com.aimedical.modules.ai.api.degradation;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DegradationContext implements Serializable {

    private static final long serialVersionUID = 2L;
    public static final long DEFAULT_TTL_MILLIS = 60_000L;

    private String serviceName;
    private String operationName;

    private int invocationCount;
    private long lastFailureTime;
    private long elapsedTime;
    private String requestType;
    private int failureCount;
    private String departmentId;
    private long serializedTimestamp;

    public DegradationContext() {
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public int getInvocationCount() {
        return invocationCount;
    }

    public void setInvocationCount(int invocationCount) {
        this.invocationCount = invocationCount;
    }

    public long getLastFailureTime() {
        return lastFailureTime;
    }

    public void setLastFailureTime(long lastFailureTime) {
        this.lastFailureTime = lastFailureTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public long getSerializedTimestamp() {
        return serializedTimestamp;
    }

    public void setSerializedTimestamp(long serializedTimestamp) {
        this.serializedTimestamp = serializedTimestamp;
    }

    public void postDeserializationValidate() {
        boolean allDefault = (invocationCount == 0)
                && (failureCount == 0)
                && elapsedTime == 0L;
        if (allDefault) {
            this.requestType = null;
        }
    }

    public boolean isFresh() {
        if (serializedTimestamp == 0) {
            return false;
        }
        return System.currentTimeMillis() - serializedTimestamp <= DEFAULT_TTL_MILLIS;
    }

    public boolean isInitialized() {
        return invocationCount > 0 || failureCount > 0 || elapsedTime > 0L;
    }

    public static class Builder {

        public static Builder builder() {
            return new Builder();
        }

        private String serviceName;
        private String operationName;
        private int invocationCount;
        private long lastFailureTime;
        private long elapsedTime;
        private String requestType;
        private int failureCount;
        private String departmentId;
        private long serializedTimestamp;

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public Builder invocationCount(int invocationCount) {
            this.invocationCount = invocationCount;
            return this;
        }

        public Builder lastFailureTime(long lastFailureTime) {
            this.lastFailureTime = lastFailureTime;
            return this;
        }

        public Builder elapsedTime(long elapsedTime) {
            this.elapsedTime = elapsedTime;
            return this;
        }

        public Builder requestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        public Builder failureCount(int failureCount) {
            this.failureCount = failureCount;
            return this;
        }

        public Builder departmentId(String departmentId) {
            this.departmentId = departmentId;
            return this;
        }

        public Builder serializedTimestamp(long serializedTimestamp) {
            this.serializedTimestamp = serializedTimestamp;
            return this;
        }

        public DegradationContext build() {
            DegradationContext context = new DegradationContext();
            context.serviceName = this.serviceName;
            context.operationName = this.operationName;
            context.invocationCount = this.invocationCount;
            context.lastFailureTime = this.lastFailureTime;
            context.elapsedTime = this.elapsedTime;
            context.requestType = this.requestType;
            context.failureCount = this.failureCount;
            context.departmentId = this.departmentId;
            context.serializedTimestamp = this.serializedTimestamp;
            return context;
        }
    }
}
