package com.aimedical.modules.window.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 对账请求（批量将 PAID 记录置为 RECONCILED）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class ReconcileRequest {

    @NotEmpty(message = "对账缴费记录ID列表不能为空")
    private List<Long> paymentIds;

    @Size(max = 32)
    private String reconcileBatchNo;
}
