package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PaymentMethod implements BaseEnum {

    CASH("CASH", "现金"),
    WECHAT("WECHAT", "微信"),
    ALIPAY("ALIPAY", "支付宝"),
    BANK_CARD("BANK_CARD", "银行卡"),
    INSURANCE("INSURANCE", "医保");

    private final String code;
    private final String desc;
}
