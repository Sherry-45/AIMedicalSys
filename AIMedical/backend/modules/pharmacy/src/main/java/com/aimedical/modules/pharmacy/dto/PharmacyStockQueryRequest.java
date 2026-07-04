package com.aimedical.modules.pharmacy.dto;

import com.aimedical.common.result.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药房库存查询请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PharmacyStockQueryRequest extends PageQuery {

    /** 药品编码（模糊匹配） */
    private String drugCode;

    /** 药品名称（模糊匹配） */
    private String drugName;
}
