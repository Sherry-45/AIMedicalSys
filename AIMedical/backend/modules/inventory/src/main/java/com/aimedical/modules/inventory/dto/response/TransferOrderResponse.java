package com.aimedical.modules.inventory.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 调拨单响应 DTO。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class TransferOrderResponse {

    private Long id;
    private String transferNo;
    private String transferType;
    private String status;
    private String sourceDept;
    private String targetDept;
    private Long applicantId;
    private String applicantName;
    private Long approverId;
    private String approverName;
    private LocalDateTime approvedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private String rejectReason;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TransferItemResponse> items;
}
