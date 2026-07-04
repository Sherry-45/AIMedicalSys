package com.aimedical.modules.doctor.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医生档案更新请求。
 *
 * <p>仅允许医生更新可变字段（简介、专长、职称等），
 * 不允许通过此接口修改 user_id / real_name / license_no 等敏感字段。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class DoctorProfileUpdateRequest {

    @Size(max = 20, message = "性别长度不能超过20")
    private String gender;

    @Size(max = 64, message = "职称长度不能超过64")
    private String title;

    @Size(max = 64, message = "科室长度不能超过64")
    private String department;

    @Size(max = 255, message = "专长长度不能超过255")
    private String specialty;

    @Size(max = 65535, message = "简介长度超出限制")
    private String introduction;

    private Integer practiceYears;

    private BigDecimal consultationFee;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
