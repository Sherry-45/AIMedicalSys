package com.aimedical.modules.pharmacy.dto;

import com.aimedical.common.result.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发药记录查询请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DispensingQueryRequest extends PageQuery {

    /** 患者ID */
    private Long patientId;

    /** 状态（DispensingStatus 的 code） */
    private String status;
}
