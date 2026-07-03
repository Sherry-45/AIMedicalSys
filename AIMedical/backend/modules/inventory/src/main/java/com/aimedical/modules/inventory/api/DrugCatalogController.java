package com.aimedical.modules.inventory.api;

import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.dto.request.DrugCatalogCreateRequest;
import com.aimedical.modules.inventory.dto.request.DrugCatalogQueryRequest;
import com.aimedical.modules.inventory.dto.response.DrugCatalogResponse;
import com.aimedical.modules.inventory.service.DrugCatalogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 药品目录控制器。
 *
 * <p>药库基础管理（D1）- 药品字典维护。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/inventory/drugs")
@PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
public class DrugCatalogController {

    private final DrugCatalogService drugCatalogService;

    public DrugCatalogController(DrugCatalogService drugCatalogService) {
        this.drugCatalogService = drugCatalogService;
    }

    /**
     * 创建药品目录。
     */
    @PostMapping
    public Result<DrugCatalogResponse> create(@Valid @RequestBody DrugCatalogCreateRequest request) {
        return drugCatalogService.create(request);
    }

    /**
     * 更新药品目录。
     */
    @PutMapping("/{id}")
    public Result<DrugCatalogResponse> update(@PathVariable Long id,
                                              @Valid @RequestBody DrugCatalogCreateRequest request) {
        return drugCatalogService.update(id, request);
    }

    /**
     * 删除药品目录（软删除）。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return drugCatalogService.delete(id);
    }

    /**
     * 查询药品目录详情。
     */
    @GetMapping("/{id}")
    public Result<DrugCatalogResponse> getById(@PathVariable Long id) {
        return drugCatalogService.getById(id);
    }

    /**
     * 分页查询药品目录。
     */
    @GetMapping
    public Result<Page<DrugCatalogResponse>> query(DrugCatalogQueryRequest request) {
        return drugCatalogService.query(request);
    }

    /**
     * 切换药品启用/停用状态。
     */
    @PutMapping("/{id}/toggle-enabled")
    public Result<DrugCatalogResponse> toggleEnabled(@PathVariable Long id) {
        return drugCatalogService.toggleEnabled(id);
    }
}
