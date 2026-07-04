package com.aimedical.modules.device.entity;

import com.aimedical.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_message")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DeviceMessage extends BaseEntity {

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "message_type", nullable = false, length = 50)
    private String messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol", nullable = false, length = 20)
    private DeviceProtocol protocol;

    @Column(name = "raw_content", columnDefinition = "TEXT")
    private String rawContent;

    @Column(name = "parsed_content", columnDefinition = "TEXT")
    private String parsedContent;

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;
}
