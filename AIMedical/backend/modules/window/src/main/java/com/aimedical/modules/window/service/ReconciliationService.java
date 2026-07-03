package com.aimedical.modules.window.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.window.dto.PaymentRecordResponse;
import com.aimedical.modules.window.dto.ReconcileRequest;
import com.aimedical.modules.window.dto.ReconcileResponse;

import java.util.List;

/**
 * 对账服务。
 *
 * <p>将一批已支付(PAID)缴费记录置为已对账(RECONCILED)，并打上对账批次号。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface ReconciliationService {

    /**
     * 批量对账（PAID -> RECONCILED）。
     *
     * @param request       对账请求（含缴费记录ID列表与批次号）
     * @param operatorId    操作员用户ID
     * @param operatorName  操作员姓名
     */
    Result<ReconcileResponse> reconcile(ReconcileRequest request,
                                        Long operatorId,
                                        String operatorName);

    /**
     * 按对账批次号查询该批次全部缴费记录。
     */
    Result<List<PaymentRecordResponse>> listByBatchNo(String batchNo);
}
