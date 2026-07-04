package com.aimedical.modules.inventory.entity;

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
 * 库存批次实体。
 *
 * <p>按药品编码 + 批次号维护实际库存，记录采购价、零售价、有效期等信息。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "inventory_stock")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InventoryStockEntity extends BaseEntity {

    /** 药品编码 */
    @Column(name = "drug_code", nullable = false, length = 32)
    private String drugCode;

    /** 批次号 */
    @Column(name = "batch_no", nullable = false, length = 64)
    private String batchNo;

    /** 库存数量 */
    @Column(name = "quantity", nullable = false, precision = 14, scale = 2)
    private BigDecimal quantity;

    /** 单位 */
    @Column(name = "unit", length = 32)
    private String unit;

    /** 采购价 */
    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    /** 零售价 */
    @Column(name = "retail_price", precision = 12, scale = 2)
    private BigDecimal retailPrice;

    /** 有效期至 */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /** 生产日期 */
    @Column(name = "production_date")
    private LocalDate productionDate;

    /** 库位 */
    @Column(name = "warehouse_location", length = 64)
    private String warehouseLocation;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;
}
