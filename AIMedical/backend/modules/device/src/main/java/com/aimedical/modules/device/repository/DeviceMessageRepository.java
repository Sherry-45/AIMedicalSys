package com.aimedical.modules.device.repository;

import com.aimedical.modules.device.entity.DeviceMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceMessageRepository extends JpaRepository<DeviceMessage, Long> {

    Page<DeviceMessage> findByDeviceId(Long deviceId, Pageable pageable);

    List<DeviceMessage> findByDeviceIdAndProcessedFalse(Long deviceId);
}
