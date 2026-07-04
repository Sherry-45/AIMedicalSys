package com.aimedical.modules.device.api;

import com.aimedical.common.result.PageResponse;
import com.aimedical.common.result.Result;
import com.aimedical.modules.device.dto.DeviceCreateRequest;
import com.aimedical.modules.device.dto.DeviceDTO;
import com.aimedical.modules.device.dto.DeviceMessageDTO;
import com.aimedical.modules.device.dto.DeviceMessageRequest;
import com.aimedical.modules.device.dto.DeviceQueryRequest;
import com.aimedical.modules.device.dto.DeviceUpdateRequest;
import com.aimedical.modules.device.entity.DeviceStatus;
import com.aimedical.modules.device.service.DeviceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DeviceDTO> registerDevice(@Valid @RequestBody DeviceCreateRequest request) {
        return Result.success(deviceService.registerDevice(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'DEVICE')")
    public Result<DeviceDTO> getDevice(@PathVariable Long id) {
        return Result.success(deviceService.getDevice(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'DEVICE')")
    public Result<PageResponse<DeviceDTO>> getDevices(@Valid DeviceQueryRequest query) {
        return Result.success(deviceService.getDevices(query));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DeviceDTO> updateDevice(@PathVariable Long id,
                                          @Valid @RequestBody DeviceUpdateRequest request) {
        return Result.success(deviceService.updateDevice(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DeviceDTO> updateDeviceStatus(@PathVariable Long id,
                                                @RequestParam @NotNull DeviceStatus status) {
        return Result.success(deviceService.updateDeviceStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return Result.success((Void) null);
    }

    @PostMapping("/{id}/heartbeat")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'DEVICE')")
    public Result<Void> heartbeat(@PathVariable Long id) {
        deviceService.heartbeat(id);
        return Result.success((Void) null);
    }

    @PostMapping("/messages")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'DEVICE')")
    public Result<DeviceMessageDTO> receiveMessage(@Valid @RequestBody DeviceMessageRequest request) {
        return Result.success(deviceService.receiveMessage(request));
    }

    @GetMapping("/{deviceId}/messages")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'DEVICE')")
    public Result<PageResponse<DeviceMessageDTO>> getMessages(@PathVariable Long deviceId,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        // F6: 限制单页最大 100 条，防止恶意大页查询拖垮数据库
        int cappedSize = Math.min(Math.max(size, 1), 100);
        return Result.success(deviceService.getMessages(deviceId, page, cappedSize));
    }
}
