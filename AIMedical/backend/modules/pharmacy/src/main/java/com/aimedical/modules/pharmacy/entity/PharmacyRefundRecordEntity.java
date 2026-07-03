package com.aimedical.modules.pharmacy.entity;

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
 * 退药记录实体（聚合根）。
 * <p>
 * 采用 DDD 聚合根设计：{@link PharmacyRefundItemEntity} 通过 refundId 外键关联，
 * 不使用 JPA {@code @OneToMany} 关系映射。
 * <p>
 * status 字段以 String 存储 {@link com.aimedical.modules.pharmacy.enums.PharmacyRefundStatus} 的 code。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "pharmacy_refund_record")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PharmacyRefundRecordEntity extends BaseEntity {

    /** 退药单号（业务唯一） */
    @Column(name = "refund_no", length = 32, unique = true, nullable = false)
    private String refundNo;

    /** 关联的发药记录ID */
    @Column(name = "dispensing_id", nullable = false)
    private Long dispensingId;

    /** 患者ID */
    @Column(name = "patient_id")
    private Long patientId;

    /** 患者姓名 */
    @Column(name = "patient_name", length = 64)
    private String patientName;

    /** 操作药师ID */
    @Column(name = "pharmacist_id")
    private Long pharmacistId;

    /** 操作药师姓名 */
    @Column(name = "pharmacist_name", length = 64)
    private String pharmacistName;

    /** 状态：存储 PharmacyRefundStatus 的 code */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING";

    /** 退药原因 */
    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    /** 退药总数量 */
    @Column(name = "total_quantity", precision = 12, scale = 2)
    private BigDecimal totalQuantity;

    /** 退药总金额 */
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /** 实际退药时间 */
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;
}
