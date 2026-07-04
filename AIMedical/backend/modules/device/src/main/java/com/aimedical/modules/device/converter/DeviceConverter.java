package com.aimedical.modules.device.converter;

import com.aimedical.modules.device.dto.DeviceDTO;
import com.aimedical.modules.device.dto.DeviceMessageDTO;
import com.aimedical.modules.device.entity.DeviceInfo;
import com.aimedical.modules.device.entity.DeviceMessage;

/**
 * 设备相关实体与 DTO 转换器。
 */
public final class DeviceConverter {

    private DeviceConverter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static DeviceDTO toDTO(DeviceInfo entity) {
        if (entity == null) {
            return null;
        }
        DeviceDTO dto = new DeviceDTO();
        dto.setId(entity.getId());
        dto.setDeviceCode(entity.getDeviceCode());
        dto.setDeviceName(entity.getDeviceName());
        dto.setDeviceType(entity.getDeviceType());
        dto.setProtocol(entity.getProtocol());
        dto.setStatus(entity.getStatus());
        dto.setManufacturer(entity.getManufacturer());
        dto.setModel(entity.getModel());
        dto.setLocation(entity.getLocation());
        dto.setConnectionConfig(entity.getConnectionConfig());
        dto.setLastHeartbeatAt(entity.getLastHeartbeatAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public static DeviceMessageDTO toMessageDTO(DeviceMessage entity) {
        if (entity == null) {
            return null;
        }
        DeviceMessageDTO dto = new DeviceMessageDTO();
        dto.setId(entity.getId());
        dto.setDeviceId(entity.getDeviceId());
        dto.setMessageType(entity.getMessageType());
        dto.setProtocol(entity.getProtocol());
        dto.setRawContent(entity.getRawContent());
        dto.setParsedContent(entity.getParsedContent());
        dto.setProcessed(entity.getProcessed());
        dto.setReceivedAt(entity.getReceivedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
