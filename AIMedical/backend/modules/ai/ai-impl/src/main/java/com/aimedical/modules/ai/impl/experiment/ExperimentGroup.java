package com.aimedical.modules.ai.impl.experiment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "ai_experiment_group")
public class ExperimentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    @Column(nullable = false, length = 50)
    private String groupId;

    @Column(nullable = false)
    private int percentage;

    @Column(length = 50)
    private String targetModelId;

    @Column
    private Integer targetPromptVersion;

    public ExperimentGroup() {
    }

    public ExperimentGroup(Long id, Experiment experiment, String groupId, int percentage,
                            String targetModelId, Integer targetPromptVersion) {
        this.id = id;
        this.experiment = experiment;
        this.groupId = groupId;
        this.percentage = percentage;
        this.targetModelId = targetModelId;
        this.targetPromptVersion = targetPromptVersion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public String getTargetModelId() {
        return targetModelId;
    }

    public void setTargetModelId(String targetModelId) {
        this.targetModelId = targetModelId;
    }

    public Integer getTargetPromptVersion() {
        return targetPromptVersion;
    }

    public void setTargetPromptVersion(Integer targetPromptVersion) {
        this.targetPromptVersion = targetPromptVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentGroup that = (ExperimentGroup) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
