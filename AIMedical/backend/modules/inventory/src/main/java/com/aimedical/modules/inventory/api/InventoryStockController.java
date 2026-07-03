package com.aimedical.modules.inventory.api;

import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.dto.request.InventoryStockQueryRequest;
import com.aimedical.modules.inventory.dto.request.StockAdjustRequest;
import com.aimedical.modules.inventory.dto.response.InventoryStockResponse;
import com.aimedical.modules.inventory.service.InventoryStockService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 库存控制器。
 *
 * <p>药库基础管理（D1）- 库存查询与调整、近效期预警、低库存预警。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/inventory/stock")
@PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
public class InventoryStockController {

    private final InventoryStockService inventoryStockService;

    public InventoryStockController(InventoryStockService inventoryStockService) {
        this.inventoryStockService = inventoryStockService;
    }

    /**
     * 分页查询库存批次。
     */
    @GetMapping
    public Result<Page<InventoryStockResponse>> query(InventoryStockQueryRequest request) {
        return inventoryStockService.query(request);
    }

    /**
     * 按药品编码查询全部批次。
     */
    @GetMapping("/{drugCode}")
    public Result<List<InventoryStockResponse>> getByDrugCode(@PathVariable String drugCode) {
        return inventoryStockService.getByDrugCode(drugCode);
    }

    /**
     * 库存调整（入库/出库）。
     */
    @PostMapping("/adjust")
    public Result<InventoryStockResponse> adjust(@Valid @RequestBody StockAdjustRequest request) {
        return inventoryStockService.adjustStock(request);
    }

    /**
     * 近效期预警：查询指定天数内即将过期的批次。
     */
    @GetMapping("/expiring")
    public Result<List<InventoryStockResponse>> listExpiringSoon(
            @RequestParam(defaultValue = "30") int days) {
        return inventoryStockService.listExpiringSoon(days);
    }

    /**
     * 低库存预警。
     */
    @GetMapping("/low-stock")
    public Result<List<InventoryStockResponse>> listLowStock() {
        return inventoryStockService.listLowStock();
    }
}
