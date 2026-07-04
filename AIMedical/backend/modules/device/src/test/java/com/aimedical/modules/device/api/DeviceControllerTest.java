package com.aimedical.modules.device.api;

import com.aimedical.common.result.PageResponse;
import com.aimedical.common.result.Result;
import com.aimedical.modules.device.dto.DeviceCreateRequest;
import com.aimedical.modules.device.dto.DeviceDTO;
import com.aimedical.modules.device.dto.DeviceMessageDTO;
import com.aimedical.modules.device.dto.DeviceMessageRequest;
import com.aimedical.modules.device.dto.DeviceQueryRequest;
import com.aimedical.modules.device.dto.DeviceUpdateRequest;
import com.aimedical.modules.device.entity.DeviceProtocol;
import com.aimedical.modules.device.entity.DeviceStatus;
import com.aimedical.modules.device.entity.DeviceType;
import com.aimedical.modules.device.service.DeviceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * DeviceController 单元测试。验证各端点正确委派 service 并返回 Result.success。
 */
@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private DeviceController controller;

    private DeviceDTO buildDto() {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(1L);
        dto.setDeviceCode("LAB-001");
        dto.setDeviceName("全自动生化分析仪");
        dto.setDeviceType(DeviceType.LAB_EQUIPMENT);
        dto.setProtocol(DeviceProtocol.HL7);
        dto.setStatus(DeviceStatus.ONLINE);
        return dto;
    }

    @Test
    @DisplayName("POST / 注册设备委派 service.registerDevice")
    void registerDevice() {
        DeviceCreateRequest request = new DeviceCreateRequest();
        request.setDeviceCode("LAB-001");
        request.setDeviceName("name");
        request.setDeviceType(DeviceType.LAB_EQUIPMENT);
        request.setProtocol(DeviceProtocol.HL7);
        DeviceDTO dto = buildDto();
        org.mockito.Mockito.when(deviceService.registerDevice(request)).thenReturn(dto);

        Result<DeviceDTO> result = controller.registerDevice(request);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(deviceService).registerDevice(request);
    }

    @Test
    @DisplayName("GET /{id} 委派 service.getDevice")
    void getDevice() {
        DeviceDTO dto = buildDto();
        org.mockito.Mockito.when(deviceService.getDevice(1L)).thenReturn(dto);

        Result<DeviceDTO> result = controller.getDevice(1L);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(deviceService).getDevice(1L);
    }

    @Test
    @DisplayName("GET / 委派 service.getDevices")
    void getDevices() {
        DeviceQueryRequest query = new DeviceQueryRequest();
        query.setPage(0);
        query.setSize(10);
        PageResponse<DeviceDTO> page = PageResponse.of(Collections.singletonList(buildDto()), 1L, 0, 10);
        org.mockito.Mockito.when(deviceService.getDevices(query)).thenReturn(page);

        Result<PageResponse<DeviceDTO>> result = controller.getDevices(query);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getTotalElements());
        verify(deviceService).getDevices(query);
    }

    @Test
    @DisplayName("PUT /{id} 委派 service.updateDevice")
    void updateDevice() {
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("新名称");
        request.setStatus(DeviceStatus.MAINTENANCE);
        DeviceDTO dto = buildDto();
        org.mockito.Mockito.when(deviceService.updateDevice(1L, request)).thenReturn(dto);

        Result<DeviceDTO> result = controller.updateDevice(1L, request);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(deviceService).updateDevice(1L, request);
    }

    @Test
    @DisplayName("PUT /{id}/status 委派 service.updateDeviceStatus")
    void updateDeviceStatus() {
        DeviceDTO dto = buildDto();
        org.mockito.Mockito.when(deviceService.updateDeviceStatus(1L, DeviceStatus.ERROR)).thenReturn(dto);

        Result<DeviceDTO> result = controller.updateDeviceStatus(1L, DeviceStatus.ERROR);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(deviceService).updateDeviceStatus(1L, DeviceStatus.ERROR);
    }

    @Test
    @DisplayName("POST /{id}/heartbeat 委派 service.heartbeat")
    void heartbeat() {
        Result<Void> result = controller.heartbeat(1L);

        assertEquals("SUCCESS", result.getCode());
        assertNull(result.getData());
        verify(deviceService).heartbeat(1L);
    }

    @Test
    @DisplayName("DELETE /{id} 委派 service.deleteDevice")
    void deleteDevice() {
        Result<Void> result = controller.deleteDevice(1L);

        assertEquals("SUCCESS", result.getCode());
        assertNull(result.getData());
        verify(deviceService).deleteDevice(1L);
    }

    @Test
    @DisplayName("POST /messages 委派 service.receiveMessage")
    void receiveMessage() {
        DeviceMessageRequest request = new DeviceMessageRequest();
        request.setDeviceId(1L);
        request.setMessageType("OBSERVATION_RESULT");
        request.setRawContent("raw");
        DeviceMessageDTO dto = new DeviceMessageDTO();
        dto.setId(10L);
        dto.setDeviceId(1L);
        org.mockito.Mockito.when(deviceService.receiveMessage(request)).thenReturn(dto);

        Result<DeviceMessageDTO> result = controller.receiveMessage(request);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(deviceService).receiveMessage(request);
    }

    @Test
    @DisplayName("GET /{deviceId}/messages 委派 service.getMessages")
    void getMessages() {
        PageResponse<DeviceMessageDTO> page = PageResponse.of(Collections.emptyList(), 0L, 0, 20);
        org.mockito.Mockito.when(deviceService.getMessages(eq(1L), eq(0), eq(20))).thenReturn(page);

        Result<PageResponse<DeviceMessageDTO>> result = controller.getMessages(1L, 0, 20);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        verify(deviceService).getMessages(1L, 0, 20);
    }
}
