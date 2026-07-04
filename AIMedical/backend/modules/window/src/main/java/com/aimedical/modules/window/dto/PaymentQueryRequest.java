package com.aimedical.modules.window.dto;

import lombok.Data;

/**
 * 缴费记录分页查询请求。
 *
 * <p>所有过滤字段均为可选，按条件组合查询。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class PaymentQueryRequest {

    private Long patientId;

    private String status;

    private String sourceType;

    private Integer page = 0;

    private Integer size = 20;
}
