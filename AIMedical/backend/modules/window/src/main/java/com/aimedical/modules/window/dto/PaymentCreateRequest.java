package com.aimedical.modules.window.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 缴费记录创建请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class PaymentCreateRequest {

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @Size(max = 64)
    private String patientName;

    private Long sourceId;

    @Size(max = 20)
    private String sourceType;

    @Size(max = 32)
    private String sourceNo;

    @NotNull(message = "应缴总额不能为空")
    private BigDecimal totalAmount;

    @Valid
    @NotEmpty(message = "缴费明细不能为空")
    private List<PaymentItemRequest> items;

    @Size(max = 500)
    private String remark;
}
