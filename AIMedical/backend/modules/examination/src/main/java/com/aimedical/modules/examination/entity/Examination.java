package com.aimedical.modules.examination.entity;

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

@Entity
@Table(name = "examination")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Examination extends BaseEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "examination_type", nullable = false, length = 20)
    private ExaminationType examinationType;

    @Column(name = "body_part", length = 200)
    private String bodyPart;

    @Column(name = "clinical_diagnosis", length = 500)
    private String clinicalDiagnosis;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ExaminationStatus status;

    @Column(name = "emergency_flag", nullable = false)
    private Boolean emergencyFlag = false;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "image_type", length = 50)
    private String imageType;

    @Column(name = "impression", columnDefinition = "TEXT")
    private String impression;

    @Column(name = "conclusion", length = 1000)
    private String conclusion;

    @Column(name = "ai_interpretation", columnDefinition = "TEXT")
    private String aiInterpretation;

    @Column(name = "ai_confidence")
    private Double aiConfidence;

    @Column(name = "image_analysis_result", columnDefinition = "TEXT")
    private String imageAnalysisResult;

    @Column(name = "image_confidence")
    private Double imageConfidence;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt;

    @PrePersist
    void prePersist() {
        if (status == null) {
            status = ExaminationStatus.PENDING;
        }
        if (emergencyFlag == null) {
            emergencyFlag = false;
        }
    }
}
