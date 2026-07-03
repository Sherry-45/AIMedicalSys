package com.aimedical.modules.inventory.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 盘点差异类型枚举。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum StocktakingDifferenceType implements BaseEnum {

    SURPLUS("SURPLUS", "盘盈"),
    LOSS("LOSS", "盘亏"),
    NONE("NONE", "无差异");

    private final String code;
    private final String desc;
}
