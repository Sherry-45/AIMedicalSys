package com.aimedical.modules.pharmacy.enums;

import com.aimedical.common.base.BaseEnum;

/**
 * 退药记录状态。
 * <p>
 * 状态流转：
 * <ul>
 *     <li>PENDING -> REFUNDED（审批通过并完成退药）</li>
 *     <li>PENDING -> REJECTED（驳回退药申请）</li>
 * </ul>
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public enum PharmacyRefundStatus implements BaseEnum {

    PENDING("PENDING", "待处理"),
    REFUNDED("REFUNDED", "已退药"),
    REJECTED("REJECTED", "已驳回");

    private final String code;
    private final String desc;

    PharmacyRefundStatus(String code, String desc) {
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
}
