package com.aimedical.modules.pharmacy.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.pharmacy.dto.PharmacyRefundCreateRequest;
import com.aimedical.modules.pharmacy.dto.PharmacyRefundResponse;

/**
 * 退药服务。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface PharmacyRefundService {

    /**
     * 创建退药申请（待处理状态）。
     * <p>
     * 业务规则：仅 DISPENSED 状态的发药记录可发起退药；退药数量不可超过发药数量；
     * 同一发药记录不可存在重复的待处理退药申请。
     *
     * @param request         退药创建请求
     * @param pharmacistId    药师ID
     * @param pharmacistName  药师姓名
     * @return 退药记录
     */
    Result<PharmacyRefundResponse> create(PharmacyRefundCreateRequest request, Long pharmacistId, String pharmacistName);

    /**
     * 审批通过退药：状态变更为 REFUNDED，回补药房库存，发药记录状态变更为 REFUNDED。
     *
     * @param refundId 退药记录ID
     * @return 退药记录
     */
    Result<PharmacyRefundResponse> approve(Long refundId);

    /**
     * 驳回退药申请：状态变更为 REJECTED。
     *
     * @param refundId 退药记录ID
     * @param reason   驳回原因
     * @return 退药记录
     */
    Result<PharmacyRefundResponse> reject(Long refundId, String reason);

    /**
     * 查询退药记录详情（含明细）。
     *
     * @param refundId 退药记录ID
     * @return 退药记录
     */
    Result<PharmacyRefundResponse> getById(Long refundId);
}
