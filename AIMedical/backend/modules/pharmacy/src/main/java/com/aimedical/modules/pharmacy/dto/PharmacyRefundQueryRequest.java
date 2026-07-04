package com.aimedical.modules.pharmacy.dto;

import com.aimedical.common.result.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 退药记录查询请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PharmacyRefundQueryRequest extends PageQuery {

    /** 患者ID */
    private Long patientId;

    /** 状态（PharmacyRefundStatus 的 code） */
    private String status;

    /** 起始时间（按 createdAt 过滤，包含） */
    private LocalDateTime startTime;

    /** 结束时间（按 createdAt 过滤，包含） */
    private LocalDateTime endTime;
}
