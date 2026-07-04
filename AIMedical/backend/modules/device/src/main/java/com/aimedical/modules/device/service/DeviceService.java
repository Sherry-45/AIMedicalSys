package com.aimedical.modules.device.service;

import com.aimedical.common.result.PageResponse;
import com.aimedical.modules.device.dto.DeviceCreateRequest;
import com.aimedical.modules.device.dto.DeviceDTO;
import com.aimedical.modules.device.dto.DeviceMessageDTO;
import com.aimedical.modules.device.dto.DeviceMessageRequest;
import com.aimedical.modules.device.dto.DeviceQueryRequest;
import com.aimedical.modules.device.dto.DeviceUpdateRequest;
import com.aimedical.modules.device.entity.DeviceStatus;

/**
 * 设备接入服务。负责设备注册、状态维护、心跳以及报文接收与解析。
 */
public interface DeviceService {

    DeviceDTO registerDevice(DeviceCreateRequest request);

    DeviceDTO updateDevice(Long id, DeviceUpdateRequest request);

    DeviceDTO getDevice(Long id);

    PageResponse<DeviceDTO> getDevices(DeviceQueryRequest query);

    /**
     * 删除设备（软删除，由 {@code @SQLDelete} 触发）。
     *
     * @param id 设备 ID
     */
    void deleteDevice(Long id);

    DeviceDTO updateDeviceStatus(Long id, DeviceStatus status);

    /**
     * 设备心跳：更新 lastHeartbeatAt 为当前时间，并将状态置为 ONLINE。
     *
     * @param id 设备 ID
     */
    void heartbeat(Long id);

    /**
     * 接收设备上报报文，按设备协议选择解析器解析，保存原始与解析后内容。
     *
     * @param request 报文请求
     * @return 解析后的报文 DTO
     */
    DeviceMessageDTO receiveMessage(DeviceMessageRequest request);

    PageResponse<DeviceMessageDTO> getMessages(Long deviceId, int page, int size);
}
