package com.aimedical.modules.device.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeviceType implements BaseEnum {

    LAB_EQUIPMENT("LAB_EQUIPMENT", "检验设备"),
    IMAGING_EQUIPMENT("IMAGING_EQUIPMENT", "影像设备"),
    MONITOR("MONITOR", "监护设备"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;
}
