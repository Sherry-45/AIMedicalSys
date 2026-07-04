package com.aimedical.modules.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeviceMessageRequest {

    @NotNull
    private Long deviceId;

    @NotBlank
    @Size(max = 50, message = "消息类型长度不能超过 50")
    private String messageType;

    @NotBlank
    @Size(max = 65535, message = "原始报文长度不能超过 65535 个字符")
    private String rawContent;
}
