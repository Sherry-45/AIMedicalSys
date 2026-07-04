package com.aimedical.modules.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发药明细请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class DispensingItemRequest {

    /** 药品编码 */
    @NotBlank(message = "药品编码不能为空")
    private String drugCode;

    /** 药品名称 */
    @NotBlank(message = "药品名称不能为空")
    private String drugName;

    /** 规格 */
    private String specification;

    /** 批次号 */
    private String batchNo;

    /** 数量 */
    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    /** 单位 */
    private String unit;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 用法用量 */
    private String dosage;

    /** 用药途径 */
    private String usageMethod;

    /** 频次 */
    private String frequency;

    /** 天数 */
    private Integer days;
}
