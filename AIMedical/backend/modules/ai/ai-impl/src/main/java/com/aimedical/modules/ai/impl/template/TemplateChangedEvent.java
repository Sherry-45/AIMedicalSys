package com.aimedical.modules.ai.impl.template;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;

public class TemplateChangedEvent extends ApplicationEvent {

    public enum ChangeType {
        CREATED, UPDATED, DELETED, STATUS_CHANGED
    }

    private final String capabilityId;
    private final String departmentId;
    private final Integer promptVersion;
    private final ChangeType changeType;
    private final Instant changedAt;

    public TemplateChangedEvent(Object source, String capabilityId, String departmentId,
                                 Integer promptVersion, ChangeType changeType, Instant changedAt) {
        super(source);
        this.capabilityId = capabilityId;
        this.departmentId = departmentId;
        this.promptVersion = promptVersion;
        this.changeType = changeType;
        this.changedAt = changedAt;
    }

    public String getCapabilityId() {
        return capabilityId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public Integer getPromptVersion() {
        return promptVersion;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
