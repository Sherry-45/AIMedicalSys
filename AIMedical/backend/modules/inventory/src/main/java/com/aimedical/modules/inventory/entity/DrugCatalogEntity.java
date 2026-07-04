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
 * 药品目录实体。
 *
 * <p>药库基础管理的核心字典，维护药品编码、名称、规格、价格等基础信息。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Entity
@Table(name = "drug_catalog")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DrugCatalogEntity extends BaseEntity {

    /** 药品编码（唯一） */
    @Column(name = "drug_code", nullable = false, length = 32, unique = true)
    private String drugCode;

    /** 药品名称 */
    @Column(name = "drug_name", nullable = false, length = 128)
    private String drugName;

    /** 通用名 */
    @Column(name = "generic_name", length = 128)
    private String genericName;

    /** 规格 */
    @Column(name = "specification", length = 128)
    private String specification;

    /** 生产厂家 */
    @Column(name = "manufacturer", length = 128)
    private String manufacturer;

    /** 剂型 */
    @Column(name = "drug_form", length = 32)
    private String drugForm;

    /** 药品分类 WESTERN_MEDICINE/CHINESE_MEDICINE/BIOLOGICAL/DEVICE */
    @Column(name = "drug_category", nullable = false, length = 32)
    private String drugCategory;

    /** 单位 */
    @Column(name = "unit", length = 32)
    private String unit;

    /** 零售价 */
    @Column(name = "retail_price", precision = 12, scale = 2)
    private BigDecimal retailPrice;

    /** 采购价 */
    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    /** 是否非处方药 */
    @Column(name = "otc_flag")
    private Boolean otcFlag;

    /** 是否启用 */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;
}
