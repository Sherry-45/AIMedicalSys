package com.aimedical.modules.pharmacy.entity;

import com.aimedical.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 药房库存实体（聚合根）。
 * <p>
 * 记录药品在药房中的实时库存信息，按药品编码 + 批次号维度管理。
 * 发药时扣减库存，退药时回补库存，库存调整用于人工校正。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "pharmacy_stock")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PharmacyStockEntity extends BaseEntity {

    /** 药品编码 */
    @Column(name = "drug_code", length = 64, nullable = false)
    private String drugCode;

    /** 药品名称 */
    @Column(name = "drug_name", length = 255, nullable = false)
    private String drugName;

    /** 批次号 */
    @Column(name = "batch_no", length = 64)
    private String batchNo;

    /** 库存数量 */
    @Column(name = "quantity", precision = 12, scale = 2, nullable = false)
    private BigDecimal quantity;

    /** 单位（如：盒、瓶、支） */
    @Column(name = "unit", length = 20)
    private String unit;

    /** 零售价 */
    @Column(name = "retail_price", precision = 10, scale = 2)
    private BigDecimal retailPrice;

    /** 有效期至 */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /** 货位 */
    @Column(name = "shelf_location", length = 64)
    private String shelfLocation;

    /** 安全库存阈值（低于此值触发低库存预警） */
    @Column(name = "safety_stock", precision = 12, scale = 2)
    private BigDecimal safetyStock;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;
}
