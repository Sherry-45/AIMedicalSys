package com.aimedical.modules.device.dto;

import com.aimedical.common.result.PageQuery;
import com.aimedical.modules.device.entity.DeviceProtocol;
import com.aimedical.modules.device.entity.DeviceStatus;
import com.aimedical.modules.device.entity.DeviceType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceQueryRequest extends PageQuery {

    private DeviceType deviceType;

    private DeviceProtocol protocol;

    private DeviceStatus status;
}
