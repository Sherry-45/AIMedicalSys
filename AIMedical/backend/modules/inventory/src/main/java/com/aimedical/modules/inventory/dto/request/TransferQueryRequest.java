package com.aimedical.modules.inventory.dto.request;

import lombok.Data;

/**
 * 调拨单查询请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class TransferQueryRequest {

    private String status;

    private String transferType;

    private Integer page = 0;

    private Integer size = 20;
}
