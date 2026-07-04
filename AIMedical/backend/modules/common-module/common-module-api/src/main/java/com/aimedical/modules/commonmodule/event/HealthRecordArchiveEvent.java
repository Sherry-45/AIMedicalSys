package com.aimedical.modules.commonmodule.event;

import java.time.LocalDateTime;

/**
 * 健康档案归档事件。
 *
 * <p>当产生与患者健康档案相关的业务事件时（缴费、退款、取药、退药等），
 * 由业务模块（window / pharmacy）发布此事件，由 patient 模块监听并归档到患者健康档案。
 *
 * <p>该事件采用 Port-Adapter 思想，避免 patient 模块对 window / pharmacy 模块的
 * 编译期依赖，仅依赖 common-module-api。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public class HealthRecordArchiveEvent {

    /** 事件类型 */
    public enum Type {
        /** 缴费已支付 */
        PAYMENT_PAID,
        /** 缴费已退款 */
        PAYMENT_REFUNDED,
        /** 药房已发药 */
        DISPENSED,
        /** 药房已退药 */
        REFUNDED
    }

    /** 患者ID（patient_profile.id） */
    private Long patientId;
    /** 患者姓名 */
    private String patientName;
    /** 事件类型 */
    private Type type;
    /** 业务记录ID（如 payment_record.id, dispensing.id） */
    private Long recordId;
    /** 业务单号（如 payment_no, dispensing_no） */
    private String recordNo;
    /** 机构ID */
    private String organizationId;
    /** 机构名称 */
    private String organizationName;
    /** 金额（分），仅用于缴费类事件；其他类型可为 null */
    private Long amount;
    /** 摘要描述 */
    private String summary;
    /** 事件发生时间 */
    private LocalDateTime occurredAt;

    public HealthRecordArchiveEvent() {}

    public HealthRecordArchiveEvent(Long patientId, String patientName, Type type, Long recordId,
                                    String recordNo, String organizationId, String organizationName,
                                    Long amount, String summary, LocalDateTime occurredAt) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.type = type;
        this.recordId = recordId;
        this.recordNo = recordNo;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.amount = amount;
        this.summary = summary;
        this.occurredAt = occurredAt;
    }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public String getRecordNo() { return recordNo; }
    public void setRecordNo(String recordNo) { this.recordNo = recordNo; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
