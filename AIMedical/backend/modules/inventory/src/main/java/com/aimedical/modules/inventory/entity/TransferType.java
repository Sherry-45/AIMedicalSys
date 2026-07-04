package com.aimedical.modules.inventory.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 调拨类型枚举。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TransferType implements BaseEnum {

    INVENTORY_TO_PHARMACY("INVENTORY_TO_PHARMACY", "药库到药房"),
    PHARMACY_TO_INVENTORY("PHARMACY_TO_INVENTORY", "药房到药库"),
    PHARMACY_TO_PHARMACY("PHARMACY_TO_PHARMACY", "药房到药房");

    private final String code;
    private final String desc;
}
