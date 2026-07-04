package com.aimedical.modules.device.dto;

import com.aimedical.modules.device.entity.DeviceStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeviceUpdateRequest {

    @Size(max = 200, message = "设备名称长度不能超过 200")
    private String deviceName;

    private DeviceStatus status;

    @Size(max = 200, message = "厂商长度不能超过 200")
    private String manufacturer;

    @Size(max = 200, message = "型号长度不能超过 200")
    private String model;

    @Size(max = 500, message = "位置长度不能超过 500")
    private String location;

    @Size(max = 10000, message = "连接配置长度不能超过 10000")
    private String connectionConfig;
}
