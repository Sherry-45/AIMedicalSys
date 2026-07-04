package com.aimedical.modules.window.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 线下挂号创建请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class OfflineRegistrationCreateRequest {

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @NotBlank(message = "患者姓名不能为空")
    @Size(max = 64)
    private String patientName;

    @Size(max = 20)
    private String patientPhone;

    @Size(max = 32)
    private String idCard;

    private Long doctorId;

    @Size(max = 64)
    private String doctorName;

    @Size(max = 64)
    private String department;

    @Size(max = 20)
    private String registrationType;

    private BigDecimal registrationFee;

    @Size(max = 500)
    private String remark;
}
