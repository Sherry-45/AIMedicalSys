package com.aimedical.modules.window.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.window.dto.PayRequest;
import com.aimedical.modules.window.dto.PaymentCreateRequest;
import com.aimedical.modules.window.dto.PaymentQueryRequest;
import com.aimedical.modules.window.dto.PaymentRecordResponse;
import com.aimedical.modules.window.dto.RefundRequest;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 缴费服务。
 *
 * <p>缴费状态机：PENDING -> PAID -> REFUNDED。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface PaymentService {

    /**
     * 创建缴费记录（含明细，状态 PENDING）。
     *
     * @param request       缴费请求
     * @param operatorId    操作员用户ID
     * @param operatorName  操作员姓名
     */
    Result<PaymentRecordResponse> create(PaymentCreateRequest request,
                                         Long operatorId,
                                         String operatorName);

    /**
     * 支付（PENDING -> PAID）。
     */
    Result<PaymentRecordResponse> pay(Long id, PayRequest request);

    /**
     * 退费（PAID -> REFUNDED）。
     */
    Result<PaymentRecordResponse> refund(Long id, RefundRequest request);

    /**
     * 按主键查询缴费详情（含明细）。
     */
    Result<PaymentRecordResponse> getById(Long id);

    /**
     * 分页查询缴费记录（按条件组合）。
     */
    Result<Page<PaymentRecordResponse>> query(PaymentQueryRequest request);

    /**
     * 按患者查询缴费记录列表。
     */
    Result<List<PaymentRecordResponse>> queryByPatient(Long patientId);
}
