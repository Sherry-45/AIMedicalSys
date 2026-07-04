package com.aimedical.modules.window.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 线下挂号响应 DTO。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class OfflineRegistrationResponse {

    private Long id;
    private String registrationNo;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private String idCard;
    private Long doctorId;
    private String doctorName;
    private String department;
    private String registrationType;
    private String status;
    private BigDecimal registrationFee;
    private Long operatorId;
    private String operatorName;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
