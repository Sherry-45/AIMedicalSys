package com.aimedical.modules.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘点明细请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class StocktakingItemRequest {

    @NotBlank(message = "药品编码不能为空")
    @Size(max = 32)
    private String drugCode;

    @Size(max = 128)
    private String drugName;

    @Size(max = 64)
    private String batchNo;

    private BigDecimal bookQuantity;

    private BigDecimal actualQuantity;

    @Size(max = 32)
    private String unit;

    @Size(max = 500)
    private String remark;
}
