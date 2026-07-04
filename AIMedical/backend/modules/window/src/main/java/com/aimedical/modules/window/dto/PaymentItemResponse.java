package com.aimedical.modules.window.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 缴费明细响应 DTO。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class PaymentItemResponse {

    private Long id;
    private Long paymentId;
    private String itemType;
    private String itemName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
