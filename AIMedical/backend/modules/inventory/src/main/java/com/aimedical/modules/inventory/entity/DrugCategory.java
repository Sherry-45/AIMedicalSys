package com.aimedical.modules.inventory.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 药品分类枚举。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum DrugCategory implements BaseEnum {

    WESTERN_MEDICINE("WESTERN_MEDICINE", "西药"),
    CHINESE_MEDICINE("CHINESE_MEDICINE", "中药"),
    BIOLOGICAL("BIOLOGICAL", "生物制品"),
    DEVICE("DEVICE", "器械");

    private final String code;
    private final String desc;
}
