package com.aimedical.modules.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退药明细请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class PharmacyRefundItemRequest {

    /** 原发药明细ID */
    @NotNull(message = "发药明细ID不能为空")
    private Long dispensingItemId;

    /** 药品编码 */
    @NotBlank(message = "药品编码不能为空")
    private String drugCode;

    /** 药品名称 */
    @NotBlank(message = "药品名称不能为空")
    private String drugName;

    /** 批次号 */
    private String batchNo;

    /** 退药数量 */
    @NotNull(message = "退药数量不能为空")
    private BigDecimal quantity;

    /** 单位 */
    private String unit;

    /** 单价 */
    private BigDecimal unitPrice;
}
