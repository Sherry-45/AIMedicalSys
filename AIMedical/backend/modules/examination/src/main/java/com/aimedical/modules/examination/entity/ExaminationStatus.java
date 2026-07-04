package com.aimedical.modules.examination.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExaminationStatus implements BaseEnum {

    PENDING("PENDING", "待检查"),
    SCHEDULED("SCHEDULED", "已预约"),
    IN_PROGRESS("IN_PROGRESS", "检查中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;
}
