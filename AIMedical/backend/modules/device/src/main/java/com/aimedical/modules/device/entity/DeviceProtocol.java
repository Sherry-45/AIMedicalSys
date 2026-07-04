package com.aimedical.modules.device.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeviceProtocol implements BaseEnum {

    HL7("HL7", "HL7协议"),
    DICOM("DICOM", "DICOM协议"),
    ASTM("ASTM", "ASTM协议"),
    SERIAL("SERIAL", "串口协议"),
    TCP("TCP", "TCP协议"),
    HTTP("HTTP", "HTTP协议");

    private final String code;
    private final String desc;
}
