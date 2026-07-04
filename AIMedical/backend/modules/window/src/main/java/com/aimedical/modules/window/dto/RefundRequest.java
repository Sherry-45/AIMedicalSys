package com.aimedical.modules.window.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 退费请求（PAID -> REFUNDED）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class RefundRequest {

    @NotBlank(message = "退费原因不能为空")
    @Size(max = 500)
    private String refundReason;
}
