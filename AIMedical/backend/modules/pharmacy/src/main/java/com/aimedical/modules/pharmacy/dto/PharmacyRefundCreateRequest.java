package com.aimedical.modules.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 退药创建请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class PharmacyRefundCreateRequest {

    /** 关联的发药记录ID */
    @NotNull(message = "发药记录ID不能为空")
    private Long dispensingId;

    /** 退药原因 */
    private String refundReason;

    /** 退药明细列表 */
    @Valid
    @NotEmpty(message = "退药明细不能为空")
    private List<PharmacyRefundItemRequest> items;

    /** 备注 */
    private String remark;
}
