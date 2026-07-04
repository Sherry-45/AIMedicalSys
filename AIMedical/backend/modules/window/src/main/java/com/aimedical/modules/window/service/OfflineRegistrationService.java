package com.aimedical.modules.window.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.window.dto.OfflineRegistrationCancelRequest;
import com.aimedical.modules.window.dto.OfflineRegistrationCreateRequest;
import com.aimedical.modules.window.dto.OfflineRegistrationQueryRequest;
import com.aimedical.modules.window.dto.OfflineRegistrationResponse;
import org.springframework.data.domain.Page;

/**
 * 线下挂号服务。
 *
 * <p>负责窗口现场挂号创建、取消与查询。创建挂号时同步生成一笔挂号费待支付缴费记录。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface OfflineRegistrationService {

    /**
     * 创建线下挂号，并同步生成挂号费待支付缴费记录。
     *
     * @param request       挂号请求
     * @param operatorId    操作员用户ID
     * @param operatorName  操作员姓名
     */
    Result<OfflineRegistrationResponse> create(OfflineRegistrationCreateRequest request,
                                               Long operatorId,
                                               String operatorName);

    /**
     * 取消挂号（仅 ACTIVE 可取消）。
     */
    Result<OfflineRegistrationResponse> cancel(Long id, OfflineRegistrationCancelRequest request);

    /**
     * 按主键查询挂号详情。
     */
    Result<OfflineRegistrationResponse> getById(Long id);

    /**
     * 分页查询挂号记录（按条件组合）。
     */
    Result<Page<OfflineRegistrationResponse>> query(OfflineRegistrationQueryRequest request);
}
