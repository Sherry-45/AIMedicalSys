package com.aimedical.modules.device.repository;

import com.aimedical.modules.device.entity.DeviceInfo;
import com.aimedical.modules.device.entity.DeviceStatus;
import com.aimedical.modules.device.entity.DeviceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, Long> {

    Optional<DeviceInfo> findByDeviceCode(String code);

    Page<DeviceInfo> findByDeviceType(DeviceType deviceType, Pageable pageable);

    Page<DeviceInfo> findByStatus(DeviceStatus status, Pageable pageable);

    Page<DeviceInfo> findByDeviceTypeAndStatus(DeviceType deviceType, DeviceStatus status, Pageable pageable);
}
