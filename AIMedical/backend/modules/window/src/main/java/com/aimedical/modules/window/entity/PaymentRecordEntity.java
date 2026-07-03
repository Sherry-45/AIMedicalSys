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
 * 缴费记录实体（窗口收费/退费/对账）。
 *
 * <p>状态机：PENDING -> PAID -> REFUNDED / RECONCILED。
 * 明细通过 {@link PaymentItemEntity} 的 paymentId 外键关联，不使用 JPA 关系映射。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "payment_record")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PaymentRecordEntity extends BaseEntity {

    @Column(name = "payment_no", length = 32, unique = true)
    private String paymentNo;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "patient_name", length = 64)
    private String patientName;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "source_type", length = 20)
    private String sourceType;

    @Column(name = "source_no", length = 32)
    private String sourceNo;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "status", length = 20)
    private String status = "PENDING";

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "payer_name", length = 64)
    private String payerName;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 64)
    private String operatorName;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Column(name = "reconcile_batch_no", length = 32)
    private String reconcileBatchNo;

    @Column(name = "remark", length = 500)
    private String remark;
}
