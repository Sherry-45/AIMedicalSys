package com.aimedical.modules.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 调拨明细请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class TransferItemRequest {

    @NotBlank(message = "药品编码不能为空")
    @Size(max = 32)
    private String drugCode;

    @Size(max = 128)
    private String drugName;

    @Size(max = 128)
    private String specification;

    @Size(max = 64)
    private String batchNo;

    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    @Size(max = 32)
    private String unit;

    private BigDecimal unitPrice;
}
