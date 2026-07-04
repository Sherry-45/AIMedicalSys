package com.aimedical.modules.device.entity;

import com.aimedical.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_info")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DeviceInfo extends BaseEntity {

    @Column(name = "device_code", nullable = false, unique = true, length = 50)
    private String deviceCode;

    @Column(name = "device_name", nullable = false, length = 200)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 30)
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol", nullable = false, length = 20)
    private DeviceProtocol protocol;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeviceStatus status;

    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    @Column(name = "model", length = 200)
    private String model;

    @Column(name = "location", length = 500)
    private String location;

    @Column(name = "connection_config", columnDefinition = "TEXT")
    private String connectionConfig;

    @Column(name = "last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;

    @PrePersist
    void prePersist() {
        if (status == null) {
            status = DeviceStatus.OFFLINE;
        }
    }
}
