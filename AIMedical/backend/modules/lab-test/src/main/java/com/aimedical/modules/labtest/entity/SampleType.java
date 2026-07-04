package com.aimedical.modules.labtest.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标本类型枚举。
 */
@Getter
@AllArgsConstructor
public enum SampleType implements BaseEnum {

    BLOOD("BLOOD", "全血"),
    SERUM("SERUM", "血清"),
    PLASMA("PLASMA", "血浆"),
    URINE("URINE", "尿液"),
    STOOL("STOOL", "粪便"),
    SPUTUM("SPUTUM", "痰液"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;
}
