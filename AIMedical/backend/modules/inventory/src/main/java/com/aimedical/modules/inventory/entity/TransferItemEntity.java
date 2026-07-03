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
 * 调拨明细实体。
 *
 * <p>通过 transferId 外键关联调拨单，不使用 JPA 关系映射。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "transfer_item")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class TransferItemEntity extends BaseEntity {

    /** 调拨单ID */
    @Column(name = "transfer_id", nullable = false)
    private Long transferId;

    /** 药品编码 */
    @Column(name = "drug_code", nullable = false, length = 32)
    private String drugCode;

    /** 药品名称 */
    @Column(name = "drug_name", length = 128)
    private String drugName;

    /** 规格 */
    @Column(name = "specification", length = 128)
    private String specification;

    /** 批次号 */
    @Column(name = "batch_no", length = 64)
    private String batchNo;

    /** 数量 */
    @Column(name = "quantity", nullable = false, precision = 14, scale = 2)
    private BigDecimal quantity;

    /** 单位 */
    @Column(name = "unit", length = 32)
    private String unit;

    /** 单价 */
    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /** 金额 */
    @Column(name = "amount", precision = 14, scale = 2)
    private BigDecimal amount;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;
}
