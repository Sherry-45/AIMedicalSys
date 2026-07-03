package com.aimedical.modules.patient.entity;

import com.aimedical.common.base.BaseEnum;

/**
 * 健康档案记录类型。
 */
public enum HealthRecordType implements BaseEnum {

    MEDICAL_RECORD("MEDICAL_RECORD", "病历"),
    PRESCRIPTION("PRESCRIPTION", "处方"),
    EXAM_REPORT("EXAM_REPORT", "检查报告"),
    LAB_TEST("LAB_TEST", "检验报告"),
    DISPENSING("DISPENSING", "发药记录"),
    PAYMENT("PAYMENT", "缴费记录"),
    ALLERGY("ALLERGY", "过敏史"),
    CHRONIC_DISEASE("CHRONIC_DISEASE", "慢病记录");

    private final String code;
    private final String desc;

    HealthRecordType(String code, String desc) {
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

    public static HealthRecordType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (HealthRecordType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
