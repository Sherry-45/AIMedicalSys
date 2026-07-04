package com.aimedical.modules.pharmacy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 发药记录响应。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class DispensingResponse {

    private Long id;
    private String dispensingNo;
    private Long prescriptionId;
    private Long medicalOrderId;
    private Long patientId;
    private String patientName;
    private Long pharmacistId;
    private String pharmacistName;
    private String status;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    private LocalDateTime dispensedAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 发药明细列表 */
    private List<DispensingItemResponse> items;
}
