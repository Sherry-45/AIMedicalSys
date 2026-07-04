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
 * 发药记录实体（聚合根）。
 * <p>
 * 采用 DDD 聚合根设计：{@link DispensingItemEntity} 通过 dispensingId 外键关联，
 * 不使用 JPA {@code @OneToMany} 关系映射，由聚合根 Service 层负责加载与持久化，
 * 避免双向关系映射的复杂性与 N+1 查询问题。
 * <p>
 * status 字段以 String 存储 {@link com.aimedical.modules.pharmacy.enums.DispensingStatus} 的 code，
 * 不使用 {@code @Enumerated}，便于跨模块序列化与历史数据兼容。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "dispensing_record")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DispensingRecordEntity extends BaseEntity {

    /** 发药单号（业务唯一） */
    @Column(name = "dispensing_no", length = 32, unique = true, nullable = false)
    private String dispensingNo;

    /** 处方ID */
    @Column(name = "prescription_id")
    private Long prescriptionId;

    /** 医嘱ID */
    @Column(name = "medical_order_id")
    private Long medicalOrderId;

    /** 患者ID */
    @Column(name = "patient_id")
    private Long patientId;

    /** 患者姓名 */
    @Column(name = "patient_name", length = 64)
    private String patientName;

    /** 发药药师ID */
    @Column(name = "pharmacist_id")
    private Long pharmacistId;

    /** 发药药师姓名 */
    @Column(name = "pharmacist_name", length = 64)
    private String pharmacistName;

    /** 状态：存储 DispensingStatus 的 code */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING";

    /** 总数量 */
    @Column(name = "total_quantity", precision = 12, scale = 2)
    private BigDecimal totalQuantity;

    /** 总金额 */
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /** 实际发药时间 */
    @Column(name = "dispensed_at")
    private LocalDateTime dispensedAt;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;
}
