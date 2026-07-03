package com.aimedical.modules.pharmacy.service;

import com.aimedical.common.result.PageResponse;
import com.aimedical.common.result.Result;
import com.aimedical.modules.pharmacy.dto.DispensingCreateRequest;
import com.aimedical.modules.pharmacy.dto.DispensingQueryRequest;
import com.aimedical.modules.pharmacy.dto.DispensingResponse;

/**
 * 发药服务。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface DispensingService {

    /**
     * 创建发药记录（待发药状态），并校验库存是否充足。
     *
     * @param request         发药创建请求
     * @param pharmacistId    药师ID
     * @param pharmacistName  药师姓名
     * @return 发药记录
     */
    Result<DispensingResponse> create(DispensingCreateRequest request, Long pharmacistId, String pharmacistName);

    /**
     * 执行发药：将状态从 PENDING 变更为 DISPENSED，扣减药房库存。
     *
     * @param dispensingId    发药记录ID
     * @param pharmacistId    药师ID
     * @param pharmacistName  药师姓名
     * @return 发药记录
     */
    Result<DispensingResponse> dispense(Long dispensingId, Long pharmacistId, String pharmacistName);

    /**
     * 取消发药（仅 PENDING 状态可取消，无库存变动）。
     *
     * @param dispensingId 发药记录ID
     * @return 发药记录
     */
    Result<DispensingResponse> cancel(Long dispensingId);

    /**
     * 查询发药记录详情（含明细）。
     *
     * @param dispensingId 发药记录ID
     * @return 发药记录
     */
    Result<DispensingResponse> getById(Long dispensingId);

    /**
     * 分页查询发药记录。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    Result<PageResponse<DispensingResponse>> query(DispensingQueryRequest request);
}
