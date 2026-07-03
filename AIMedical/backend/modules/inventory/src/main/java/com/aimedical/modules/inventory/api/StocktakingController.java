package com.aimedical.modules.inventory.api;

import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.auth.CurrentUser;
import com.aimedical.modules.inventory.dto.request.StocktakingCreateRequest;
import com.aimedical.modules.inventory.dto.request.StocktakingItemRequest;
import com.aimedical.modules.inventory.dto.request.StocktakingQueryRequest;
import com.aimedical.modules.inventory.dto.response.StocktakingResponse;
import com.aimedical.modules.inventory.service.StocktakingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 盘点控制器。
 *
 * <p>药库基础管理（D1）- 盘点流程管理。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/inventory/stocktaking")
@PreAuthorize("hasRole('ADMIN')")
public class StocktakingController {

    private final StocktakingService stocktakingService;
    private final CurrentUser currentUser;

    public StocktakingController(StocktakingService stocktakingService, CurrentUser currentUser) {
        this.stocktakingService = stocktakingService;
        this.currentUser = currentUser;
    }

    /**
     * 创建盘点单。
     */
    @PostMapping
    public Result<StocktakingResponse> create(@Valid @RequestBody StocktakingCreateRequest request) {
        Long operatorId = currentUser.getUserId();
        String operatorName = currentUser.getUsername();
        return stocktakingService.create(request, operatorId, operatorName);
    }

    /**
     * 开始盘点（DRAFT -> IN_PROGRESS）。
     */
    @PostMapping("/{id}/start")
    public Result<StocktakingResponse> start(@PathVariable Long id) {
        return stocktakingService.start(id);
    }

    /**
     * 提交实际盘点数量。
     */
    @PutMapping("/{id}/items")
    public Result<StocktakingResponse> submitActual(@PathVariable Long id,
                                                    @Valid @RequestBody List<StocktakingItemRequest> items) {
        return stocktakingService.submitActual(id, items);
    }

    /**
     * 完成盘点（IN_PROGRESS -> COMPLETED），计算差异并回写库存。
     */
    @PostMapping("/{id}/complete")
    public Result<StocktakingResponse> complete(@PathVariable Long id) {
        return stocktakingService.complete(id);
    }

    /**
     * 取消盘点。
     */
    @PostMapping("/{id}/cancel")
    public Result<StocktakingResponse> cancel(@PathVariable Long id) {
        return stocktakingService.cancel(id);
    }

    /**
     * 查询盘点单详情。
     */
    @GetMapping("/{id}")
    public Result<StocktakingResponse> getById(@PathVariable Long id) {
        return stocktakingService.getById(id);
    }

    /**
     * 分页查询盘点单。
     */
    @GetMapping
    public Result<Page<StocktakingResponse>> query(StocktakingQueryRequest request) {
        return stocktakingService.query(request);
    }
}
