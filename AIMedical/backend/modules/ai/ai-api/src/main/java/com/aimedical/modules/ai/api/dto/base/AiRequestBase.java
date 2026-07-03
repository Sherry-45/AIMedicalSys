package com.aimedical.modules.ai.api.dto.base;

public abstract class AiRequestBase {
    protected AiRequestBase() {
    }

    public String getDepartmentId() { return null; }
    public String getVisitId() { return null; }
    public String getPatientId() { return null; }
    public String getSessionId() { return null; }
}
