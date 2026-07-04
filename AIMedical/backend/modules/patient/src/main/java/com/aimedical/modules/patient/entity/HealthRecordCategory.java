package com.aimedical.modules.patient.entity;

import com.aimedical.common.base.BaseEnum;

/**
 * 健康档案记录类别。
 */
public enum HealthRecordCategory implements BaseEnum {

    OUTPATIENT("OUTPATIENT", "门诊"),
    INPATIENT("INPATIENT", "住院"),
    PHYSICAL_EXAM("PHYSICAL_EXAM", "体检"),
    FOLLOW_UP("FOLLOW_UP", "随访");

    private final String code;
    private final String desc;

    HealthRecordCategory(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    public static HealthRecordCategory fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (HealthRecordCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        return null;
    }
}
