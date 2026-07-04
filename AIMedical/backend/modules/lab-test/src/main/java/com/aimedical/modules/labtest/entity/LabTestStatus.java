package com.aimedical.modules.labtest.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 检验状态枚举。
 */
@Getter
@AllArgsConstructor
public enum LabTestStatus implements BaseEnum {

    PENDING("PENDING", "待采样"),
    COLLECTED("COLLECTED", "已采样"),
    IN_PROGRESS("IN_PROGRESS", "检验中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;
}
