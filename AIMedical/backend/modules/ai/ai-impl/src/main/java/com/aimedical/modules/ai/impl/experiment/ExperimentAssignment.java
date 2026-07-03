package com.aimedical.modules.ai.impl.experiment;

public final class ExperimentAssignment {

    private final String experimentId;
    private final String groupId;
    private final String targetModelId;
    private final Integer targetPromptVersion;

    public ExperimentAssignment(String experimentId, String groupId,
                                 String targetModelId, Integer targetPromptVersion) {
        this.experimentId = experimentId;
        this.groupId = groupId;
        this.targetModelId = targetModelId;
        this.targetPromptVersion = targetPromptVersion;
    }

    public static ExperimentAssignment createDefault() {
        return new ExperimentAssignment(null, "default", null, null);
    }

    public static ExperimentAssignment createErrorFallback() {
        return new ExperimentAssignment(null, "experiment-error", null, null);
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getTargetModelId() {
        return targetModelId;
    }

    public Integer getTargetPromptVersion() {
        return targetPromptVersion;
    }
}
