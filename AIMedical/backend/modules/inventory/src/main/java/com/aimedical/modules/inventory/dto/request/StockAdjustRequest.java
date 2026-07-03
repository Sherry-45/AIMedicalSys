package com.aimedical.modules.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存调整请求。
 *
 * <p>调整可为正数（入库）或负数（出库）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class StockAdjustRequest {

    @NotBlank(message = "药品编码不能为空")
    @Size(max = 32)
    private String drugCode;

    @NotBlank(message = "批次号不能为空")
    @Size(max = 64)
    private String batchNo;

    @NotNull(message = "调整数量不能为空")
    private BigDecimal quantity;

    @Size(max = 500)
    private String remark;
}
