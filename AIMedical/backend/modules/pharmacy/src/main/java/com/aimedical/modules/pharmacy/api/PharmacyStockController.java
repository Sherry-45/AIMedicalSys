package com.aimedical.modules.pharmacy.api;

import com.aimedical.common.result.Result;
import com.aimedical.modules.pharmacy.dto.PharmacyStockQueryRequest;
import com.aimedical.modules.pharmacy.dto.PharmacyStockResponse;
import com.aimedical.modules.pharmacy.service.PharmacyStockService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 药房库存控制器。
 * <p>
 * 提供库存查询、库存调整、低库存预警等接口。
 * 全部需要 ADMIN 角色（药房工作人员使用管理员角色）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/pharmacy/stock")
@PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
public class PharmacyStockController {

    private final PharmacyStockService stockService;

    public PharmacyStockController(PharmacyStockService stockService) {
        this.stockService = stockService;
    }

    /**
     * 分页查询药房库存。
     */
    @GetMapping
    public Result<com.aimedical.common.result.PageResponse<PharmacyStockResponse>> query(@Valid @ModelAttribute PharmacyStockQueryRequest request) {
        return stockService.query(request);
    }

    /**
     * 按药品编码查询库存。
     */
    @GetMapping("/{drugCode}")
    public Result<PharmacyStockResponse> getByDrugCode(@PathVariable String drugCode) {
        return stockService.getByDrugCode(drugCode);
    }

    /**
     * 调整库存数量（正数增加，负数减少）。
     */
    @PostMapping("/adjust")
    public Result<PharmacyStockResponse> adjustStock(@RequestParam String drugCode,
                                                      @RequestParam String batchNo,
                                                      @RequestParam BigDecimal quantity,
                                                      @RequestParam(required = false) String remark) {
        return stockService.adjustStock(drugCode, batchNo, quantity, remark);
    }

    /**
     * 查询低库存预警列表。
     */
    @GetMapping("/low-stock")
    public Result<List<PharmacyStockResponse>> listLowStock() {
        return stockService.listLowStock();
    }
}
