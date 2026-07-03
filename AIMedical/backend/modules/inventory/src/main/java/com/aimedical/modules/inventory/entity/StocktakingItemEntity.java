package com.aimedical.modules.inventory.entity;

import com.aimedical.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 盘点明细实体。
 *
 * <p>通过 stocktakingId 外键关联盘点单，不使用 JPA 关系映射。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "stocktaking_item")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class StocktakingItemEntity extends BaseEntity {

    /** 盘点单ID */
    @Column(name = "stocktaking_id", nullable = false)
    private Long stocktakingId;

    /** 药品编码 */
    @Column(name = "drug_code", nullable = false, length = 32)
    private String drugCode;

    /** 药品名称 */
    @Column(name = "drug_name", length = 128)
    private String drugName;

    /** 批次号 */
    @Column(name = "batch_no", length = 64)
    private String batchNo;

    /** 账面数量 */
    @Column(name = "book_quantity", precision = 14, scale = 2)
    private BigDecimal bookQuantity;

    /** 实际数量 */
    @Column(name = "actual_quantity", precision = 14, scale = 2)
    private BigDecimal actualQuantity;

    /** 差异（实际 - 账面） */
    @Column(name = "difference", precision = 14, scale = 2)
    private BigDecimal difference;

    /** 差异类型 SURPLUS/LOSS/NONE */
    @Column(name = "difference_type", length = 20)
    private String differenceType;

    /** 单位 */
    @Column(name = "unit", length = 32)
    private String unit;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;
}
