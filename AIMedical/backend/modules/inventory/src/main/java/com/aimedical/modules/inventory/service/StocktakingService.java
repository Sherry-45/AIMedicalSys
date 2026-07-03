package com.aimedical.modules.inventory.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.dto.request.StocktakingCreateRequest;
import com.aimedical.modules.inventory.dto.request.StocktakingItemRequest;
import com.aimedical.modules.inventory.dto.request.StocktakingQueryRequest;
import com.aimedical.modules.inventory.dto.response.StocktakingResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 盘点服务。
 *
 * <p>状态机：DRAFT -> IN_PROGRESS -> PENDING_APPROVAL -> APPROVED -> COMPLETED。
 * 可从 PENDING_APPROVAL -> REJECTED，可从 DRAFT/IN_PROGRESS -> CANCELLED。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface StocktakingService {

    Result<StocktakingResponse> create(StocktakingCreateRequest request, Long operatorId, String operatorName);

    Result<StocktakingResponse> start(Long id);

    Result<StocktakingResponse> submitActual(Long id, List<StocktakingItemRequest> items);

    Result<StocktakingResponse> submitForApproval(Long id, Long approverId, String approverName);

    Result<StocktakingResponse> approve(Long id, Long approverId, String approverName);

    Result<StocktakingResponse> reject(Long id, Long approverId, String approverName, String rejectReason);

    Result<StocktakingResponse> complete(Long id);

    Result<StocktakingResponse> cancel(Long id);

    Result<StocktakingResponse> getById(Long id);

    Result<Page<StocktakingResponse>> query(StocktakingQueryRequest request);
}
