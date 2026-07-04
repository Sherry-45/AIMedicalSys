package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 线下挂号实体（窗口挂号）。
 *
 * <p>记录窗口现场挂号信息，创建时同步生成一笔挂号费待支付缴费记录。
 * 状态字段以 String 存储枚举 code，不使用 JPA @Enumerated，便于跨模块解耦。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "offline_registration")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class OfflineRegistrationEntity extends BaseEntity {

    @Column(name = "registration_no", length = 32, unique = true)
    private String registrationNo;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "patient_name", length = 64)
    private String patientName;

    @Column(name = "patient_phone", length = 20)
    private String patientPhone;

    @Column(name = "id_card", length = 32)
    private String idCard;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "doctor_name", length = 64)
    private String doctorName;

    @Column(name = "department", length = 64)
    private String department;

    @Column(name = "registration_type", length = 20)
    private String registrationType = "OUTPATIENT";

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    @Column(name = "registration_fee", precision = 10, scale = 2)
    private BigDecimal registrationFee;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 64)
    private String operatorName;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "cancel_time")
    private LocalDateTime cancelTime;

    @Column(name = "remark", length = 500)
    private String remark;
}
