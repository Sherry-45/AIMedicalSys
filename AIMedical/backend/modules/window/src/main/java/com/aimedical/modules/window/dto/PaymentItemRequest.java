package com.aimedical.modules.window.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 缴费明细请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class PaymentItemRequest {

    @NotBlank(message = "项目类型不能为空")
    @Size(max = 20)
    private String itemType;

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 128)
    private String itemName;

    private BigDecimal quantity;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    @Size(max = 500)
    private String remark;
}
