package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 线下挂号类型枚举。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum OfflineRegistrationType implements BaseEnum {

    OUTPATIENT("OUTPATIENT", "门诊"),
    EXAMINATION("EXAMINATION", "检查"),
    EMERGENCY("EMERGENCY", "急诊");

    private final String code;
    private final String desc;
}
