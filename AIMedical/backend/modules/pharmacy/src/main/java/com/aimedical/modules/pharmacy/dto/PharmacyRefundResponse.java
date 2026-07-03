package com.aimedical.modules.pharmacy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退药记录响应。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class PharmacyRefundResponse {

    private Long id;
    private String refundNo;
    private Long dispensingId;
    private Long patientId;
    private String patientName;
    private Long pharmacistId;
    private String pharmacistName;
    private String status;
    private String refundReason;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    private LocalDateTime refundedAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 退药明细列表 */
    private List<PharmacyRefundItemResponse> items;
}
