package com.aimedical.modules.doctor.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 医生档案 DTO。
 *
 * <p>对应 {@code doctor_profile} 表的核心字段，用于医生端查询/更新自己的档案信息。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class DoctorDto {

    /** 主键 ID（doctor_profile 主键，非 user_id）。 */
    private Long id;

    /** 关联的用户ID（auth 模块 user 表主键）。 */
    private Long userId;

    /** 真实姓名。 */
    private String realName;

    /** 性别。 */
    private String gender;

    /** 职称。 */
    private String title;

    /** 科室。 */
    private String department;

    /** 专长。 */
    private String specialty;

    /** 个人简介。 */
    private String introduction;

    /** 执业许可证号。 */
    private String licenseNo;

    /** 执业年限。 */
    private Integer practiceYears;

    /** 问诊费。 */
    private BigDecimal consultationFee;

    /** 备注。 */
    private String remark;
}
