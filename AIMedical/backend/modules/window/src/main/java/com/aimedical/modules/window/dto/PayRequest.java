package com.aimedical.modules.window.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付请求（PENDING -> PAID）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class PayRequest {

    @NotBlank(message = "支付方式不能为空")
    @Size(max = 20)
    private String paymentMethod;

    @Size(max = 64)
    private String payerName;

    @NotNull(message = "支付金额不能为空")
    private BigDecimal paidAmount;
}
