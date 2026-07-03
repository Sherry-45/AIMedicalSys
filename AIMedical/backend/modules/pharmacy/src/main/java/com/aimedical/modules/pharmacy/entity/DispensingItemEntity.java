package com.aimedical.modules.pharmacy.entity;

import com.aimedical.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 发药明细实体，属于 {@link DispensingRecordEntity} 聚合根的内部实体。
 * <p>
 * 通过 dispensingId 外键关联到发药记录，不使用 JPA {@code @ManyToOne} 关系映射，
 * 由聚合根 Service 层负责加载与持久化。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "dispensing_item")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DispensingItemEntity extends BaseEntity {

    /** 发药记录ID（外键，非关系映射） */
    @Column(name = "dispensing_id", nullable = false)
    private Long dispensingId;

    /** 药品编码 */
    @Column(name = "drug_code", length = 64, nullable = false)
    private String drugCode;

    /** 药品名称 */
    @Column(name = "drug_name", length = 255, nullable = false)
    private String drugName;

    /** 规格 */
    @Column(name = "specification", length = 255)
    private String specification;

    /** 批次号 */
    @Column(name = "batch_no", length = 64)
    private String batchNo;

    /** 数量 */
    @Column(name = "quantity", precision = 12, scale = 2, nullable = false)
    private BigDecimal quantity;

    /** 单位 */
    @Column(name = "unit", length = 20)
    private String unit;

    /** 单价 */
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** 金额 */
    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    /** 用法用量 */
    @Column(name = "dosage", length = 100)
    private String dosage;

    /** 用药途径 */
    @Column(name = "usage_method", length = 100)
    private String usageMethod;

    /** 频次 */
    @Column(name = "frequency", length = 50)
    private String frequency;

    /** 天数 */
    @Column(name = "days")
    private Integer days;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;
}
