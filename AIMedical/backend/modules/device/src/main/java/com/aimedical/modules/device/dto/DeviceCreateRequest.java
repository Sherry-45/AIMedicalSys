package com.aimedical.modules.device.dto;

import com.aimedical.modules.device.entity.DeviceProtocol;
import com.aimedical.modules.device.entity.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeviceCreateRequest {

    @NotBlank
    @Size(max = 50, message = "设备编码长度不能超过 50")
    private String deviceCode;

    @NotBlank
    @Size(max = 200, message = "设备名称长度不能超过 200")
    private String deviceName;

    @NotNull
    private DeviceType deviceType;

    @NotNull
    private DeviceProtocol protocol;

    @Size(max = 200, message = "厂商长度不能超过 200")
    private String manufacturer;

    @Size(max = 200, message = "型号长度不能超过 200")
    private String model;

    @Size(max = 500, message = "位置长度不能超过 500")
    private String location;

    @Size(max = 10000, message = "连接配置长度不能超过 10000")
    private String connectionConfig;
}
