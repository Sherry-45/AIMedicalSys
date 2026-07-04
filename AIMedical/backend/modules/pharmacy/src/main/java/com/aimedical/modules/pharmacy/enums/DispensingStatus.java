package com.aimedical.modules.pharmacy.enums;

import com.aimedical.common.base.BaseEnum;

/**
 * 发药记录状态。
 * <p>
 * 状态流转：
 * <ul>
 *     <li>PENDING -> DISPENSED（执行发药）</li>
 *     <li>PENDING -> CANCELLED（取消发药）</li>
 *     <li>DISPENSED -> REFUNDED（完成退药）</li>
 * </ul>
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public enum DispensingStatus implements BaseEnum {

    PENDING("PENDING", "待发药"),
    DISPENSED("DISPENSED", "已发药"),
    REFUNDED("REFUNDED", "已退药"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    DispensingStatus(String code, String desc) {
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
