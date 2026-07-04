package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缴费来源类型枚举。
 *
 * <p>标识一笔缴费的业务来源：挂号、医嘱或发药。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PaymentSourceType implements BaseEnum {

    REGISTRATION("REGISTRATION", "挂号"),
    MEDICAL_ORDER("MEDICAL_ORDER", "医嘱"),
    DISPENSING("DISPENSING", "发药");

    private final String code;
    private final String desc;
}
