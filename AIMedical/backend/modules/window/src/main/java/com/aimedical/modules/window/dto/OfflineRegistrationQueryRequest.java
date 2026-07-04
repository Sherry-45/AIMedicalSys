package com.aimedical.modules.window.dto;

import lombok.Data;

/**
 * 线下挂号分页查询请求。
 *
 * <p>所有过滤字段均为可选，按条件组合查询。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class OfflineRegistrationQueryRequest {

    private Long patientId;

    private String patientName;

    private String status;

    private Integer page = 0;

    private Integer size = 20;
}
