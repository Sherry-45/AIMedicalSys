package com.aimedical.modules.patient.entity;

import com.aimedical.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 健康档案记录实体。
 *
 * <p>统一存储患者各类健康相关记录（病历、处方、检查/检验报告、发药、缴费、过敏、慢病等），
 * 通过 {@link HealthRecordType} 区分类型，并保留来源记录引用（sourceId/sourceTable）
 * 以便与各业务模块（挂号、收费、药房等）数据打通。
 */
@Entity
@Table(name = "health_record")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class HealthRecordEntity extends BaseEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "record_type", length = 32, nullable = false)
    private String recordType;

    @Column(name = "record_category", length = 32)
    private String recordCategory;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 128)
    private String organization;

    @Column(length = 64)
    private String department;

    @Column(name = "doctor_name", length = 64)
    private String doctorName;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "source_table", length = 64)
    private String sourceTable;

    @Column(name = "report_data", columnDefinition = "TEXT")
    private String reportData;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(length = 500)
    private String remark;
}
