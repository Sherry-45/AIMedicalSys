package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缴费项目类型枚举。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PaymentItemType implements BaseEnum {

    REGISTRATION_FEE("REGISTRATION_FEE", "挂号费"),
    DRUG_FEE("DRUG_FEE", "药品费"),
    EXAMINATION_FEE("EXAMINATION_FEE", "检查费"),
    LAB_TEST_FEE("LAB_TEST_FEE", "化验费"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;
}
