package com.aimedical.modules.device.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeviceStatus implements BaseEnum {

    ONLINE("ONLINE", "在线"),
    OFFLINE("OFFLINE", "离线"),
    ERROR("ERROR", "故障"),
    MAINTENANCE("MAINTENANCE", "维护中");

    private final String code;
    private final String desc;
}
