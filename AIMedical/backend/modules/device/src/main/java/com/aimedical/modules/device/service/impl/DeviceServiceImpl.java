package com.aimedical.modules.device.service.impl;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.common.result.PageResponse;
import com.aimedical.modules.device.converter.DeviceConverter;
import com.aimedical.modules.device.dto.DeviceCreateRequest;
import com.aimedical.modules.device.dto.DeviceDTO;
import com.aimedical.modules.device.dto.DeviceMessageDTO;
import com.aimedical.modules.device.dto.DeviceMessageRequest;
import com.aimedical.modules.device.dto.DeviceQueryRequest;
import com.aimedical.modules.device.dto.DeviceUpdateRequest;
import com.aimedical.modules.device.entity.DeviceInfo;
import com.aimedical.modules.device.entity.DeviceMessage;
import com.aimedical.modules.device.entity.DeviceStatus;
import com.aimedical.modules.device.exception.DeviceErrorCode;
import com.aimedical.modules.device.protocol.ProtocolParser;
import com.aimedical.modules.device.repository.DeviceInfoRepository;
import com.aimedical.modules.device.repository.DeviceMessageRepository;
import com.aimedical.modules.device.service.DeviceService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 设备接入服务实现。
 * 协议解析器通过 {@link List} 注入，并在构造时按 {@link ProtocolParser#getProtocol()} 建索引。
 */
@Service
public class DeviceServiceImpl implements DeviceService {

    /**
     * F1: 排序字段白名单，防止通过 sort 参数注入非法列名导致 SQL 异常或信息泄露。
     */
    private static final Set<String> SORT_WHITELIST = new HashSet<>(Arrays.asList(
            "id", "deviceCode", "deviceName", "deviceType", "protocol",
            "status", "manufacturer", "model", "location",
            "lastHeartbeatAt", "createdAt", "updatedAt"));

    private final DeviceInfoRepository deviceInfoRepository;
    private final DeviceMessageRepository deviceMessageRepository;
    private final Map<String, ProtocolParser> parserMap;

    public DeviceServiceImpl(DeviceInfoRepository deviceInfoRepository,
                             DeviceMessageRepository deviceMessageRepository,
                             List<ProtocolParser> parsers) {
        this.deviceInfoRepository = deviceInfoRepository;
        this.deviceMessageRepository = deviceMessageRepository;
        Map<String, ProtocolParser> map = new HashMap<>();
        if (parsers != null) {
            for (ProtocolParser parser : parsers) {
                map.put(parser.getProtocol(), parser);
            }
        }
        this.parserMap = map;
    }

    @Override
    @Transactional
    public DeviceDTO registerDevice(DeviceCreateRequest request) {
        if (deviceInfoRepository.findByDeviceCode(request.getDeviceCode()).isPresent()) {
            throw new BusinessException(DeviceErrorCode.DEVICE_CODE_DUPLICATE);
        }
        DeviceInfo device = new DeviceInfo();
        device.setDeviceCode(request.getDeviceCode());
        device.setDeviceName(request.getDeviceName());
        device.setDeviceType(request.getDeviceType());
        device.setProtocol(request.getProtocol());
        device.setManufacturer(request.getManufacturer());
        device.setModel(request.getModel());
        device.setLocation(request.getLocation());
        device.setConnectionConfig(request.getConnectionConfig());
        // status 由 @PrePersist 默认置为 OFFLINE
        DeviceInfo saved = deviceInfoRepository.saveAndFlush(device);
        return DeviceConverter.toDTO(saved);
    }

    @Override
    @Transactional
    public DeviceDTO updateDevice(Long id, DeviceUpdateRequest request) {
        DeviceInfo device = findDeviceOrThrow(id);
        if (request.getDeviceName() != null) {
            device.setDeviceName(request.getDeviceName());
        }
        if (request.getStatus() != null) {
            device.setStatus(request.getStatus());
        }
        if (request.getManufacturer() != null) {
            device.setManufacturer(request.getManufacturer());
        }
        if (request.getModel() != null) {
            device.setModel(request.getModel());
        }
        if (request.getLocation() != null) {
            device.setLocation(request.getLocation());
        }
        if (request.getConnectionConfig() != null) {
            device.setConnectionConfig(request.getConnectionConfig());
        }
        DeviceInfo saved = deviceInfoRepository.saveAndFlush(device);
        return DeviceConverter.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceDTO getDevice(Long id) {
        return DeviceConverter.toDTO(findDeviceOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeviceDTO> getDevices(DeviceQueryRequest query) {
        DeviceInfo probe = new DeviceInfo();
        if (query.getDeviceType() != null) {
            probe.setDeviceType(query.getDeviceType());
        }
        if (query.getProtocol() != null) {
            probe.setProtocol(query.getProtocol());
        }
        if (query.getStatus() != null) {
            probe.setStatus(query.getStatus());
        }
        Sort sort = parseSort(query.getSort());
        Page<DeviceInfo> page = deviceInfoRepository.findAll(Example.of(probe),
                PageRequest.of(query.getPage(), query.getSize(), sort));
        List<DeviceDTO> list = page.getContent().stream()
                .map(DeviceConverter::toDTO)
                .collect(Collectors.toList());
        return PageResponse.of(list, page.getTotalElements(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional
    public void deleteDevice(Long id) {
        DeviceInfo device = findDeviceOrThrow(id);
        // 触发 @SQLDelete 软删除，避免使用派生 deleteByXxx 导致的物理删除
        deviceInfoRepository.delete(device);
    }

    @Override
    @Transactional
    public DeviceDTO updateDeviceStatus(Long id, DeviceStatus status) {
        DeviceInfo device = findDeviceOrThrow(id);
        device.setStatus(status);
        DeviceInfo saved = deviceInfoRepository.saveAndFlush(device);
        return DeviceConverter.toDTO(saved);
    }

    @Override
    @Transactional
    public void heartbeat(Long id) {
        DeviceInfo device = findDeviceOrThrow(id);
        device.setLastHeartbeatAt(LocalDateTime.now());
        device.setStatus(DeviceStatus.ONLINE);
        deviceInfoRepository.saveAndFlush(device);
    }

    @Override
    @Transactional
    public DeviceMessageDTO receiveMessage(DeviceMessageRequest request) {
        DeviceInfo device = findDeviceOrThrow(request.getDeviceId());
        DeviceStatus status = device.getStatus();
        // ONLINE：正常接收业务报文；MAINTENANCE：维护态可接收测试报文以验证协议解析
        if (status != DeviceStatus.ONLINE && status != DeviceStatus.MAINTENANCE) {
            throw new BusinessException(DeviceErrorCode.DEVICE_OFFLINE);
        }
        ProtocolParser parser = parserMap.get(device.getProtocol().getCode());
        if (parser == null) {
            throw new BusinessException(DeviceErrorCode.PROTOCOL_NOT_SUPPORTED);
        }
        String parsed = parser.parse(request.getRawContent());

        DeviceMessage message = new DeviceMessage();
        message.setDeviceId(device.getId());
        message.setMessageType(request.getMessageType());
        message.setProtocol(device.getProtocol());
        message.setRawContent(request.getRawContent());
        message.setParsedContent(parsed);
        message.setProcessed(false);
        message.setReceivedAt(LocalDateTime.now());
        DeviceMessage saved = deviceMessageRepository.saveAndFlush(message);
        return DeviceConverter.toMessageDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeviceMessageDTO> getMessages(Long deviceId, int page, int size) {
        Page<DeviceMessage> pageResult = deviceMessageRepository.findByDeviceId(deviceId,
                PageRequest.of(page, size));
        List<DeviceMessageDTO> list = pageResult.getContent().stream()
                .map(DeviceConverter::toMessageDTO)
                .collect(Collectors.toList());
        return PageResponse.of(list, pageResult.getTotalElements(), page, size);
    }

    private DeviceInfo findDeviceOrThrow(Long id) {
        return deviceInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(DeviceErrorCode.DEVICE_NOT_FOUND));
    }

    /**
     * 解析分页查询的 sort 字段。每个元素格式为 {@code property} 或 {@code property,asc|desc}。
     * F1: 仅允许白名单内的属性名，非法元素将被忽略；全部非法或为空时返回 {@link Sort#unsorted()}。
     */
    private Sort parseSort(List<String> sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (String s : sort) {
            if (s == null || s.isBlank()) {
                continue;
            }
            String[] parts = s.split(",");
            String property = parts[0].trim();
            if (property.isEmpty() || !SORT_WHITELIST.contains(property)) {
                continue;
            }
            Sort.Direction direction = Sort.Direction.ASC;
            if (parts.length > 1) {
                String dir = parts[1].trim().toUpperCase();
                if ("DESC".equals(dir)) {
                    direction = Sort.Direction.DESC;
                }
            }
            orders.add(new Sort.Order(direction, property));
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }
}
