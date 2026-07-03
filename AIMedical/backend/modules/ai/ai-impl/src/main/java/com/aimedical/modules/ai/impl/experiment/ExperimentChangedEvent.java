package com.aimedical.modules.ai.impl.experiment;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;

public class ExperimentChangedEvent extends ApplicationEvent {

    public enum ChangeType {
        CREATED, UPDATED, DELETED, STATUS_CHANGED
    }

    private final String capabilityId;
    private final Long experimentId;
    private final ChangeType changeType;
    private final Instant changedAt;

    public ExperimentChangedEvent(Object source, String capabilityId, Long experimentId,
                                    ChangeType changeType, Instant changedAt) {
        super(source);
        this.capabilityId = capabilityId;
        this.experimentId = experimentId;
        this.changeType = changeType;
        this.changedAt = changedAt;
    }

    public String getCapabilityId() {
        return capabilityId;
    }

    public Long getExperimentId() {
        return experimentId;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
