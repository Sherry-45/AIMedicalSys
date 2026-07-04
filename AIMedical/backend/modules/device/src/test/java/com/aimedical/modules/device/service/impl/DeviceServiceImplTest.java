package com.aimedical.modules.device.service.impl;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.common.result.PageResponse;
import com.aimedical.modules.device.dto.DeviceCreateRequest;
import com.aimedical.modules.device.dto.DeviceDTO;
import com.aimedical.modules.device.dto.DeviceMessageDTO;
import com.aimedical.modules.device.dto.DeviceMessageRequest;
import com.aimedical.modules.device.dto.DeviceQueryRequest;
import com.aimedical.modules.device.dto.DeviceUpdateRequest;
import com.aimedical.modules.device.entity.DeviceInfo;
import com.aimedical.modules.device.entity.DeviceMessage;
import com.aimedical.modules.device.entity.DeviceProtocol;
import com.aimedical.modules.device.entity.DeviceStatus;
import com.aimedical.modules.device.entity.DeviceType;
import com.aimedical.modules.device.exception.DeviceErrorCode;
import com.aimedical.modules.device.protocol.ProtocolParser;
import com.aimedical.modules.device.repository.DeviceInfoRepository;
import com.aimedical.modules.device.repository.DeviceMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DeviceServiceImpl 单元测试。mock repository 与 protocol parser。
 */
@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    private DeviceInfoRepository deviceInfoRepository;

    @Mock
    private DeviceMessageRepository deviceMessageRepository;

    @Mock
    private ProtocolParser hl7Parser;

    @Mock
    private ProtocolParser dicomParser;

    private DeviceServiceImpl service;

    @BeforeEach
    void setUp() {
        when(hl7Parser.getProtocol()).thenReturn("HL7");
        when(dicomParser.getProtocol()).thenReturn("DICOM");
        service = new DeviceServiceImpl(deviceInfoRepository, deviceMessageRepository,
                List.of(hl7Parser, dicomParser));
    }

    private DeviceInfo buildOnlineHl7Device() {
        DeviceInfo device = new DeviceInfo();
        device.setId(1L);
        device.setDeviceCode("LAB-001");
        device.setDeviceName("全自动生化分析仪");
        device.setDeviceType(DeviceType.LAB_EQUIPMENT);
        device.setProtocol(DeviceProtocol.HL7);
        device.setStatus(DeviceStatus.ONLINE);
        device.setManufacturer("罗氏");
        device.setModel("Cobas 8000");
        device.setLocation("检验科1楼");
        device.setConnectionConfig("{\"host\":\"192.168.1.100\",\"port\":5000}");
        return device;
    }

    @Test
    @DisplayName("registerDevice 新设备注册成功")
    void registerDevice_success() {
        DeviceCreateRequest request = new DeviceCreateRequest();
        request.setDeviceCode("LAB-001");
        request.setDeviceName("全自动生化分析仪");
        request.setDeviceType(DeviceType.LAB_EQUIPMENT);
        request.setProtocol(DeviceProtocol.HL7);
        request.setManufacturer("罗氏");
        when(deviceInfoRepository.findByDeviceCode("LAB-001")).thenReturn(Optional.empty());
        when(deviceInfoRepository.saveAndFlush(any(DeviceInfo.class))).thenAnswer(inv -> {
            DeviceInfo d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        DeviceDTO dto = service.registerDevice(request);

        assertEquals(1L, dto.getId());
        assertEquals("LAB-001", dto.getDeviceCode());
        assertEquals(DeviceType.LAB_EQUIPMENT, dto.getDeviceType());
        assertEquals(DeviceProtocol.HL7, dto.getProtocol());
        verify(deviceInfoRepository).saveAndFlush(any(DeviceInfo.class));
    }

    @Test
    @DisplayName("registerDevice 编码重复抛 DEVICE_CODE_DUPLICATE")
    void registerDevice_duplicateCode() {
        DeviceCreateRequest request = new DeviceCreateRequest();
        request.setDeviceCode("LAB-001");
        request.setDeviceName("name");
        request.setDeviceType(DeviceType.LAB_EQUIPMENT);
        request.setProtocol(DeviceProtocol.HL7);
        when(deviceInfoRepository.findByDeviceCode("LAB-001")).thenReturn(Optional.of(buildOnlineHl7Device()));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.registerDevice(request));
        assertEquals(DeviceErrorCode.DEVICE_CODE_DUPLICATE.getCode(), ex.getErrorCode().getCode());
        verify(deviceInfoRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("updateDevice 更新成功")
    void updateDevice_success() {
        DeviceInfo device = buildOnlineHl7Device();
        when(deviceInfoRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceInfoRepository.saveAndFlush(any(DeviceInfo.class))).thenAnswer(inv -> inv.getArgument(0));
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("新名称");
        request.setStatus(DeviceStatus.MAINTENANCE);

        DeviceDTO dto = service.updateDevice(1L, request);

        assertEquals("新名称", dto.getDeviceName());
        assertEquals(DeviceStatus.MAINTENANCE, dto.getStatus());
        verify(deviceInfoRepository).saveAndFlush(device);
    }

    @Test
    @DisplayName("updateDevice 设备不存在抛 DEVICE_NOT_FOUND")
    void updateDevice_notFound() {
        when(deviceInfoRepository.findById(2L)).thenReturn(Optional.empty());
        DeviceUpdateRequest request = new DeviceUpdateRequest();
        request.setDeviceName("x");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateDevice(2L, request));
        assertEquals(DeviceErrorCode.DEVICE_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    @DisplayName("getDevice 查询成功")
    void getDevice_success() {
        when(deviceInfoRepository.findById(1L)).thenReturn(Optional.of(buildOnlineHl7Device()));

        DeviceDTO dto = service.getDevice(1L);

        assertEquals(1L, dto.getId());
        assertEquals("LAB-001", dto.getDeviceCode());
    }

    @Test
    @DisplayName("getDevice 不存在抛 DEVICE_NOT_FOUND")
    void getDevice_notFound() {
        when(deviceInfoRepository.findById(9L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.getDevice(9L));
        assertEquals(DeviceErrorCode.DEVICE_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    @DisplayName("getDevices 返回分页结果")
    void getDevices() {
        DeviceQueryRequest query = new DeviceQueryRequest();
        query.setPage(0);
        query.setSize(10);
        Page<DeviceInfo> page = new PageImpl<>(Collections.singletonList(buildOnlineHl7Device()),
                PageRequest.of(0, 10), 1);
        when(deviceInfoRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(page);

        PageResponse<DeviceDTO> response = service.getDevices(query);

        assertEquals(1, response.getContent().size());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }

    @Test
    @DisplayName("updateDeviceStatus 更新成功")
    void updateDeviceStatus_success() {
        DeviceInfo device = buildOnlineHl7Device();
        when(deviceInfoRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceInfoRepository.saveAndFlush(any(DeviceInfo.class))).thenAnswer(inv -> inv.getArgument(0));

        DeviceDTO dto = service.updateDeviceStatus(1L, DeviceStatus.ERROR);

        assertEquals(DeviceStatus.ERROR, dto.getStatus());
        verify(deviceInfoRepository).saveAndFlush(device);
    }

    @Test
    @DisplayName("updateDeviceStatus 不存在抛 DEVICE_NOT_FOUND")
    void updateDeviceStatus_notFound() {
        when(deviceInfoRepository.findById(2L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateDeviceStatus(2L, DeviceStatus.ERROR));
        assertEquals(DeviceErrorCode.DEVICE_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    @DisplayName("heartbeat 更新心跳时间并置 ONLINE")
    void heartbeat_success() {
        DeviceInfo device = buildOnlineHl7Device();
        device.setStatus(DeviceStatus.OFFLINE);
        when(deviceInfoRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceInfoRepository.saveAndFlush(any(DeviceInfo.class))).thenAnswer(inv -> inv.getArgument(0));

        service.heartbeat(1L);

        assertEquals(DeviceStatus.ONLINE, device.getStatus());
        assertNotNull(device.getLastHeartbeatAt());
        verify(deviceInfoRepository).saveAndFlush(device);
    }

    @Test
    @DisplayName("heartbeat 不存在抛 DEVICE_NOT_FOUND")
    void heartbeat_notFound() {
        when(deviceInfoRepository.findById(2L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.heartbeat(2L));
        assertEquals(DeviceErrorCode.DEVICE_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    @DisplayName("receiveMessage 在线设备 + HL7 解析成功")
    void receiveMessage_success() {
        DeviceInfo device = buildOnlineHl7Device();
        when(deviceInfoRepository.findById(1L)).thenReturn(Optional.of(device));
        when(hl7Parser.parse("raw")).thenReturn("{\"segments\":[]}");
        when(deviceMessageRepository.saveAndFlush(any(DeviceMessage.class))).thenAnswer(inv -> {
            DeviceMessage m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });
        DeviceMessageRequest request = new DeviceMessageRequest();
        request.setDeviceId(1L);
        request.setMessageType("OBSERVATION_RESULT");
        request.setRawContent("raw");

        DeviceMessageDTO dto = service.receiveMessage(request);

        assertEquals(10L, dto.getId());
        assertEquals(1L, dto.getDeviceId());
        assertEquals("OBSERVATION_RESULT", dto.getMessageType());
        assertEquals(DeviceProtocol.HL7, dto.getProtocol());
        assertEquals("raw", dto.getRawContent());
        assertEquals("{\"segments\":[]}", dto.getParsedContent());
        assertNotNull(dto.getReceivedAt());
        verify(hl7Parser).parse("raw");
    }

    @Test
    @DisplayName("receiveMessage 设备不存在抛 DEVICE_NOT_FOUND")
    void receiveMessage_deviceNotFound() {
        when(deviceInfoRepository.findById(2L)).thenReturn(Optional.empty());
        DeviceMessageRequest request = new DeviceMessageRequest();
        request.setDeviceId(2L);
        request.setMessageType("OBSERVATION_RESULT");
        request.setRawContent("raw");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.receiveMessage(request));
        assertEquals(DeviceErrorCode.DEVICE_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    @DisplayName("receiveMessage 设备离线抛 DEVICE_OFFLINE")
    void receiveMessage_deviceOffline() {
        DeviceInfo device = buildOnlineHl7Device();
        device.setStatus(DeviceStatus.OFFLINE);
        when(deviceInfoRepository.findById(1L)).thenReturn(Optional.of(device));
        DeviceMessageRequest request = new DeviceMessageRequest();
        request.setDeviceId(1L);
        request.setMessageType("OBSERVATION_RESULT");
        request.setRawContent("raw");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.receiveMessage(request));
        assertEquals(DeviceErrorCode.DEVICE_OFFLINE.getCode(), ex.getErrorCode().getCode());
        verify(hl7Parser, never()).parse(any());
    }

    @Test
    @DisplayName("receiveMessage ERROR 状态也抛 DEVICE_OFFLINE")
    void receiveMessage_errorStatusThrowsOffline() {
        DeviceInfo device = buildOnlineHl7Device();
        device.setStatus(DeviceStatus.ERROR);
        when(deviceInfoRepository.findById(1L)).thenReturn(Optional.of(device));
        DeviceMessageRequest request = new DeviceMessageRequest();
        request.setDeviceId(1L);
        request.setMessageType("OBSERVATION_RESULT");
        request.setRawContent("raw");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.receiveMessage(request));
        assertEquals(DeviceErrorCode.DEVICE_OFFLINE.getCode(), ex.getErrorCode().getCode());
        verify(hl7Parser, never()).parse(any());
    }

    @Test
    @DisplayName("receiveMessage MAINTENANCE 状态允许接收测试报文")
    void receiveMessage_maintenanceAllowed() {
        DeviceInfo device = buildOnlineHl7Device();
        device.setStatus(DeviceStatus.MAINTENANCE);
        when(deviceInfoRepository.findById(1L)).thenReturn(Optional.of(device));
        when(hl7Parser.parse("raw")).thenReturn("{\"segments\":[]}");
        when(deviceMessageRepository.saveAndFlush(any(DeviceMessage.class))).thenAnswer(inv -> {
            DeviceMessage m = inv.getArgument(0);
            m.setId(11L);
            return m;
        });
        DeviceMessageRequest request = new DeviceMessageRequest();
        request.setDeviceId(1L);
        request.setMessageType("TEST_MESSAGE");
        request.setRawContent("raw");

        DeviceMessageDTO dto = service.receiveMessage(request);

        assertEquals(11L, dto.getId());
        assertEquals(DeviceProtocol.HL7, dto.getProtocol());
        assertEquals("{\"segments\":[]}", dto.getParsedContent());
        verify(hl7Parser).parse("raw");
    }

    @Test
    @DisplayName("receiveMessage 无对应协议解析器抛 PROTOCOL_NOT_SUPPORTED")
    void receiveMessage_protocolNotSupported() {
        DeviceInfo device = buildOnlineHl7Device();
        device.setProtocol(DeviceProtocol.ASTM);
        when(deviceInfoRepository.findById(1L)).thenReturn(Optional.of(device));
        DeviceMessageRequest request = new DeviceMessageRequest();
        request.setDeviceId(1L);
        request.setMessageType("OBSERVATION_RESULT");
        request.setRawContent("raw");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.receiveMessage(request));
        assertEquals(DeviceErrorCode.PROTOCOL_NOT_SUPPORTED.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    @DisplayName("getMessages 返回分页报文")
    void getMessages() {
        DeviceMessage message = new DeviceMessage();
        message.setId(10L);
        message.setDeviceId(1L);
        message.setMessageType("OBSERVATION_RESULT");
        message.setProtocol(DeviceProtocol.HL7);
        message.setRawContent("raw");
        message.setParsedContent("{}");
        message.setProcessed(false);
        message.setReceivedAt(LocalDateTime.now());
        Page<DeviceMessage> page = new PageImpl<>(Collections.singletonList(message),
                PageRequest.of(0, 20), 1);
        when(deviceMessageRepository.findByDeviceId(eq(1L), any(Pageable.class))).thenReturn(page);

        PageResponse<DeviceMessageDTO> response = service.getMessages(1L, 0, 20);

        assertEquals(1, response.getContent().size());
        assertEquals(10L, response.getContent().get(0).getId());
        assertEquals(1L, response.getTotalElements());
        assertTrue(response.getTotalPages() >= 1);
    }

    @Test
    @DisplayName("deleteDevice 软删除委派 repository.delete")
    void deleteDevice_success() {
        DeviceInfo device = buildOnlineHl7Device();
        when(deviceInfoRepository.findById(1L)).thenReturn(Optional.of(device));

        service.deleteDevice(1L);

        verify(deviceInfoRepository).delete(device);
    }

    @Test
    @DisplayName("deleteDevice 不存在抛 DEVICE_NOT_FOUND")
    void deleteDevice_notFound() {
        when(deviceInfoRepository.findById(2L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteDevice(2L));
        assertEquals(DeviceErrorCode.DEVICE_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
        verify(deviceInfoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getDevices 使用 sort 字段构造分页（不抛异常即通过）")
    void getDevices_withSort() {
        DeviceQueryRequest query = new DeviceQueryRequest();
        query.setPage(0);
        query.setSize(10);
        query.setSort(List.of("deviceCode,asc", "createdAt,desc"));
        Page<DeviceInfo> page = new PageImpl<>(Collections.singletonList(buildOnlineHl7Device()),
                PageRequest.of(0, 10), 1);
        when(deviceInfoRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(page);

        PageResponse<DeviceDTO> response = service.getDevices(query);

        assertEquals(1, response.getContent().size());
        assertEquals(1L, response.getTotalElements());
    }

    @Test
    @DisplayName("getDevices sort 字段含非法元素时应忽略并不抛异常")
    void getDevices_withInvalidSortIgnored() {
        DeviceQueryRequest query = new DeviceQueryRequest();
        query.setPage(0);
        query.setSize(10);
        query.setSort(List.of(",desc", "", "  ", "deviceCode"));
        Page<DeviceInfo> page = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        when(deviceInfoRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(page);

        PageResponse<DeviceDTO> response = service.getDevices(query);

        assertEquals(0, response.getContent().size());
        assertEquals(0L, response.getTotalElements());
    }
}
