package com.aimedical.modules.inventory.entity;

import com.aimedical.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 盘点单实体。
 *
 * <p>状态机：DRAFT -> IN_PROGRESS -> PENDING_APPROVAL -> APPROVED -> COMPLETED。
 * 可从 PENDING_APPROVAL -> REJECTED，可从 DRAFT/IN_PROGRESS -> CANCELLED。
 * 明细通过 {@link StocktakingItemEntity} 以 stocktakingId 外键关联，
 * 不使用 JPA 关系映射。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "stocktaking")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class StocktakingEntity extends BaseEntity {

    /** 盘点单号（唯一） */
    @Column(name = "stocktaking_no", nullable = false, length = 32, unique = true)
    private String stocktakingNo;

    /** 盘点类型 FULL/PARTIAL/SPOT */
    @Column(name = "stocktaking_type", nullable = false, length = 20)
    private String stocktakingType = StocktakingType.FULL.getCode();

    /** 状态 DRAFT/IN_PROGRESS/PENDING_APPROVAL/APPROVED/REJECTED/COMPLETED/CANCELLED */
    @Column(name = "status", nullable = false, length = 20)
    private String status = StocktakingStatus.DRAFT.getCode();

    /** 操作人ID */
    @Column(name = "operator_id")
    private Long operatorId;

    /** 操作人姓名 */
    @Column(name = "operator_name", length = 64)
    private String operatorName;

    /** 开始时间 */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /** 结束时间 */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /** 总项数 */
    @Column(name = "total_items")
    private Integer totalItems;

    /** 盘盈项数 */
    @Column(name = "surplus_items")
    private Integer surplusItems;

    /** 盘亏项数 */
    @Column(name = "loss_items")
    private Integer lossItems;

    /** 审批人ID */
    @Column(name = "approver_id")
    private Long approverId;

    /** 审批人姓名 */
    @Column(name = "approver_name", length = 64)
    private String approverName;

    /** 审批时间 */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /** 驳回原因 */
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;
}
