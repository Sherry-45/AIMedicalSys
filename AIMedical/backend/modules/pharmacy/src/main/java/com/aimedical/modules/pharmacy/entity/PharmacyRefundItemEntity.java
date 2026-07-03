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
 * 退药明细实体，属于 {@link PharmacyRefundRecordEntity} 聚合根的内部实体。
 * <p>
 * 通过 refundId 外键关联到退药记录，通过 dispensingItemId 外键关联到原发药明细，
 * 不使用 JPA 关系映射。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "pharmacy_refund_item")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PharmacyRefundItemEntity extends BaseEntity {

    /** 退药记录ID（外键，非关系映射） */
    @Column(name = "refund_id", nullable = false)
    private Long refundId;

    /** 原发药明细ID（外键） */
    @Column(name = "dispensing_item_id")
    private Long dispensingItemId;

    /** 药品编码 */
    @Column(name = "drug_code", length = 64, nullable = false)
    private String drugCode;

    /** 药品名称 */
    @Column(name = "drug_name", length = 255, nullable = false)
    private String drugName;

    /** 批次号 */
    @Column(name = "batch_no", length = 64)
    private String batchNo;

    /** 退药数量 */
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

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;
}
