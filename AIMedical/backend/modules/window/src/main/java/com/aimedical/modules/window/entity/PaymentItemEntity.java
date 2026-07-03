package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 缴费明细实体。
 *
 * <p>通过 paymentId 外键关联到 {@link PaymentRecordEntity}，不使用 JPA 关系映射。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "payment_item")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PaymentItemEntity extends BaseEntity {

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "item_type", length = 20)
    private String itemType;

    @Column(name = "item_name", length = 128)
    private String itemName;

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "remark", length = 500)
    private String remark;
}
