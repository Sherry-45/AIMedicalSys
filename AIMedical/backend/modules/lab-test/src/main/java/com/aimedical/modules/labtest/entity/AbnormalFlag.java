package com.aimedical.modules.labtest.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 异常标志枚举。
 */
@Getter
@AllArgsConstructor
public enum AbnormalFlag implements BaseEnum {

    NORMAL("NORMAL", "正常"),
    LOW("LOW", "偏低"),
    HIGH("HIGH", "偏高"),
    CRITICAL_LOW("CRITICAL_LOW", "危急值低"),
    CRITICAL_HIGH("CRITICAL_HIGH", "危急值高");

    private final String code;
    private final String desc;
}
