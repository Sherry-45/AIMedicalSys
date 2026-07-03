package com.aimedical.modules.pharmacy.service;

import com.aimedical.common.result.PageResponse;
import com.aimedical.common.result.Result;
import com.aimedical.modules.pharmacy.dto.PharmacyStockQueryRequest;
import com.aimedical.modules.pharmacy.dto.PharmacyStockResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * 药房库存服务。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface PharmacyStockService {

    /**
     * 分页查询药房库存。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    Result<PageResponse<PharmacyStockResponse>> query(PharmacyStockQueryRequest request);

    /**
     * 按药品编码查询库存（汇总所有批次）。
     *
     * @param drugCode 药品编码
     * @return 库存信息（取首个批次作为代表返回，或无库存时返回失败）
     */
    Result<PharmacyStockResponse> getByDrugCode(String drugCode);

    /**
     * 调整库存数量（人工校正，正数加库存，负数减库存）。
     *
     * @param drugCode 药品编码
     * @param batchNo  批次号
     * @param quantity 调整数量（正数为增加，负数为减少）
     * @param remark   备注
     * @return 调整后的库存信息
     */
    Result<PharmacyStockResponse> adjustStock(String drugCode, String batchNo, BigDecimal quantity, String remark);

    /**
     * 查询低库存预警列表（库存低于安全库存阈值）。
     *
     * @return 低库存列表
     */
    Result<List<PharmacyStockResponse>> listLowStock();
}
