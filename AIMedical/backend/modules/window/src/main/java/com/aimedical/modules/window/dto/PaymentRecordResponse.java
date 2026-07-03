package com.aimedical.modules.window.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 缴费记录响应 DTO（含明细列表）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class PaymentRecordResponse {

    private Long id;
    private String paymentNo;
    private Long patientId;
    private String patientName;
    private Long sourceId;
    private String sourceType;
    private String sourceNo;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal refundAmount;
    private String status;
    private String paymentMethod;
    private String payerName;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime reconciledAt;
    private String refundReason;
    private String reconcileBatchNo;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PaymentItemResponse> items;
}
