package com.aimedical.modules.device.dto;

import com.aimedical.modules.device.entity.DeviceProtocol;
import com.aimedical.modules.device.entity.DeviceStatus;
import com.aimedical.modules.device.entity.DeviceType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceDTO {

    private Long id;
    private String deviceCode;
    private String deviceName;
    private DeviceType deviceType;
    private DeviceProtocol protocol;
    private DeviceStatus status;
    private String manufacturer;
    private String model;
    private String location;
    private String connectionConfig;
    private LocalDateTime lastHeartbeatAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
