package com.aimedical.modules.labtest.entity;

import com.aimedical.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 检验记录主实体。
 * <p>
 * 检验明细 LabTestItem 作为独立实体通过 labTestId 外键关联，
 * 不使用 JPA {@code @OneToMany} 关系映射，避免 N+1 查询与级联删除等隐式行为。
 */
@Entity
@Table(name = "lab_test")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LabTest extends BaseEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "test_type", nullable = false, length = 200)
    private String testType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sample_type", nullable = false, length = 20)
    private SampleType sampleType;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LabTestStatus status;

    @Column(name = "report_conclusion", length = 1000)
    private String reportConclusion;

    @Column(name = "ai_interpretation", columnDefinition = "TEXT")
    private String aiInterpretation;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt;

    @PrePersist
    void prePersist() {
        if (status == null) {
            status = LabTestStatus.PENDING;
        }
    }
}
