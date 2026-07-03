package com.aimedical.modules.inventory.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.dto.request.InventoryStockQueryRequest;
import com.aimedical.modules.inventory.dto.request.StockAdjustRequest;
import com.aimedical.modules.inventory.dto.response.InventoryStockResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 库存服务。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface InventoryStockService {

    Result<Page<InventoryStockResponse>> query(InventoryStockQueryRequest request);

    Result<List<InventoryStockResponse>> getByDrugCode(String drugCode);

    Result<InventoryStockResponse> adjustStock(StockAdjustRequest request);

    Result<List<InventoryStockResponse>> listExpiringSoon(int days);

    Result<List<InventoryStockResponse>> listLowStock();
}
