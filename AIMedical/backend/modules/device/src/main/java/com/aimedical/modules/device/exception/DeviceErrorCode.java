package com.aimedical.modules.device.exception;

import com.aimedical.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum DeviceErrorCode implements ErrorCode {

    DEVICE_NOT_FOUND("DEVICE_NOT_FOUND", "设备不存在"),
    DEVICE_CODE_DUPLICATE("DEVICE_CODE_DUPLICATE", "设备编码已存在"),
    DEVICE_OFFLINE("DEVICE_OFFLINE", "设备离线或不可用，无法接收报文"),
    MESSAGE_PARSE_FAILED("MESSAGE_PARSE_FAILED", "报文解析失败"),
    PROTOCOL_NOT_SUPPORTED("PROTOCOL_NOT_SUPPORTED", "不支持的协议");

    private final String code;
    private final String message;

    DeviceErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
