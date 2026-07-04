package com.aimedical.modules.inventory.entity;

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
 * 调拨单实体。
 *
 * <p>状态机：DRAFT -> PENDING_APPROVAL -> APPROVED -> IN_TRANSIT -> RECEIVED。
 * 可从 PENDING_APPROVAL -> REJECTED，可从 DRAFT/PENDING_APPROVAL -> CANCELLED。
 * 明细通过 {@link TransferItemEntity} 以 transferId 外键关联，不使用 JPA 关系映射。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "transfer_order")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class TransferOrderEntity extends BaseEntity {

    /** 调拨单号（唯一） */
    @Column(name = "transfer_no", nullable = false, length = 32, unique = true)
    private String transferNo;

    /** 调拨类型 INVENTORY_TO_PHARMACY/PHARMACY_TO_INVENTORY/PHARMACY_TO_PHARMACY */
    @Column(name = "transfer_type", nullable = false, length = 32)
    private String transferType;

    /** 状态 DRAFT/PENDING_APPROVAL/APPROVED/IN_TRANSIT/RECEIVED/REJECTED/CANCELLED */
    @Column(name = "status", nullable = false, length = 20)
    private String status = TransferStatus.DRAFT.getCode();

    /** 调出部门 */
    @Column(name = "source_dept", length = 64)
    private String sourceDept;

    /** 调入部门 */
    @Column(name = "target_dept", length = 64)
    private String targetDept;

    /** 申请人ID */
    @Column(name = "applicant_id")
    private Long applicantId;

    /** 申请人姓名 */
    @Column(name = "applicant_name", length = 64)
    private String applicantName;

    /** 审批人ID */
    @Column(name = "approver_id")
    private Long approverId;

    /** 审批人姓名 */
    @Column(name = "approver_name", length = 64)
    private String approverName;

    /** 审批时间 */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /** 发货时间 */
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    /** 接收时间 */
    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    /** 总项数 */
    @Column(name = "total_items")
    private Integer totalItems;

    /** 总金额 */
    @Column(name = "total_amount", precision = 14, scale = 2)
    private BigDecimal totalAmount;

    /** 驳回原因 */
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;
}
