package com.aimedical.modules.device.dto;

import com.aimedical.modules.device.entity.DeviceProtocol;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceMessageDTO {

    private Long id;
    private Long deviceId;
    private String messageType;
    private DeviceProtocol protocol;
    private String rawContent;
    private String parsedContent;
    private Boolean processed;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
