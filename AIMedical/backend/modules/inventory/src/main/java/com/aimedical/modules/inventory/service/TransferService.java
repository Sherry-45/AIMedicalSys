package com.aimedical.modules.inventory.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.dto.request.TransferApproveRequest;
import com.aimedical.modules.inventory.dto.request.TransferCreateRequest;
import com.aimedical.modules.inventory.dto.request.TransferQueryRequest;
import com.aimedical.modules.inventory.dto.response.TransferOrderResponse;
import org.springframework.data.domain.Page;

/**
 * 调拨服务。
 *
 * <p>状态机：DRAFT -> PENDING_APPROVAL -> APPROVED -> IN_TRANSIT -> RECEIVED。
 * 可从 PENDING_APPROVAL -> REJECTED，可从 DRAFT/PENDING_APPROVAL -> CANCELLED。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface TransferService {

    Result<TransferOrderResponse> create(TransferCreateRequest request, Long applicantId, String applicantName);

    Result<TransferOrderResponse> submit(Long id);

    Result<TransferOrderResponse> approve(Long id, TransferApproveRequest request, Long approverId, String approverName);

    Result<TransferOrderResponse> ship(Long id);

    Result<TransferOrderResponse> receive(Long id);

    Result<TransferOrderResponse> cancel(Long id);

    Result<TransferOrderResponse> getById(Long id);

    Result<Page<TransferOrderResponse>> query(TransferQueryRequest request);
}
