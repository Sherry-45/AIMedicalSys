package com.aimedical.modules.inventory.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 盘点类型枚举。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum StocktakingType implements BaseEnum {

    FULL("FULL", "全盘"),
    PARTIAL("PARTIAL", "部分盘"),
    SPOT("SPOT", "抽盘");

    private final String code;
    private final String desc;
}
