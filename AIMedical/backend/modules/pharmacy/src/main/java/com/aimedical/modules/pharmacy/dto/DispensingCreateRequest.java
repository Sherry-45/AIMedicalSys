package com.aimedical.modules.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 发药创建请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class DispensingCreateRequest {

    /** 处方ID */
    private Long prescriptionId;

    /** 医嘱ID */
    private Long medicalOrderId;

    /** 患者ID */
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /** 患者姓名 */
    private String patientName;

    /** 发药明细列表 */
    @Valid
    @NotEmpty(message = "发药明细不能为空")
    private List<DispensingItemRequest> items;

    /** 备注 */
    private String remark;
}
