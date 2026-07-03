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
 * <p>状态机：DRAFT -> IN_PROGRESS -> COMPLETED (或 CANCELLED)。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface StocktakingService {

    Result<StocktakingResponse> create(StocktakingCreateRequest request, Long operatorId, String operatorName);

    Result<StocktakingResponse> start(Long id);

    Result<StocktakingResponse> submitActual(Long id, List<StocktakingItemRequest> items);

    Result<StocktakingResponse> complete(Long id);

    Result<StocktakingResponse> cancel(Long id);

    Result<StocktakingResponse> getById(Long id);

    Result<Page<StocktakingResponse>> query(StocktakingQueryRequest request);
}
