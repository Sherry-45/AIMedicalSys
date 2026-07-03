package com.aimedical.modules.pharmacy.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.PageResponse;
import com.aimedical.common.result.Result;
import com.aimedical.modules.pharmacy.PharmacyErrorCode;
import com.aimedical.modules.pharmacy.converter.PharmacyConverter;
import com.aimedical.modules.pharmacy.dto.PharmacyStockQueryRequest;
import com.aimedical.modules.pharmacy.dto.PharmacyStockResponse;
import com.aimedical.modules.pharmacy.entity.PharmacyStockEntity;
import com.aimedical.modules.pharmacy.repository.PharmacyStockRepository;
import com.aimedical.modules.pharmacy.service.PharmacyStockService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 药房库存服务实现。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class PharmacyStockServiceImpl implements PharmacyStockService {

    private final PharmacyStockRepository stockRepository;
    private final PharmacyConverter converter;

    public PharmacyStockServiceImpl(PharmacyStockRepository stockRepository, PharmacyConverter converter) {
        this.stockRepository = stockRepository;
        this.converter = converter;
    }

    @Override
    public Result<PageResponse<PharmacyStockResponse>> query(PharmacyStockQueryRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        String drugCode = request.getDrugCode();
        String drugName = request.getDrugName();

        Page<PharmacyStockEntity> page;
        boolean hasCode = drugCode != null && !drugCode.trim().isEmpty();
        boolean hasName = drugName != null && !drugName.trim().isEmpty();
        if (hasCode && hasName) {
            page = stockRepository.findByDrugCodeContainingAndDrugNameContaining(
                    drugCode.trim(), drugName.trim(), pageable);
        } else if (hasCode) {
            page = stockRepository.findByDrugCodeContaining(drugCode.trim(), pageable);
        } else if (hasName) {
            page = stockRepository.findByDrugNameContaining(drugName.trim(), pageable);
        } else {
            page = stockRepository.findAll(pageable);
        }

        List<PharmacyStockResponse> content = converter.toStockResponseList(page.getContent());
        PageResponse<PharmacyStockResponse> pageResponse = PageResponse.of(content, page.getTotalElements(),
                request.getPage(), request.getSize());
        return Result.success(pageResponse);
    }

    @Override
    public Result<PharmacyStockResponse> getByDrugCode(String drugCode) {
        if (drugCode == null || drugCode.trim().isEmpty()) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID, "药品编码不能为空");
        }
        List<PharmacyStockEntity> stocks = stockRepository.findByDrugCode(drugCode.trim());
        if (stocks.isEmpty()) {
            return Result.fail(PharmacyErrorCode.STOCK_NOT_FOUND);
        }
        // 返回首个批次作为代表
        return Result.success(converter.toResponse(stocks.get(0)));
    }

    @Override
    @Transactional
    public Result<PharmacyStockResponse> adjustStock(String drugCode, String batchNo,
                                                      BigDecimal quantity, String remark) {
        if (drugCode == null || drugCode.trim().isEmpty()) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID, "药品编码不能为空");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return Result.fail(PharmacyErrorCode.STOCK_ADJUST_INVALID, "调整数量不能为空或为零");
        }
        if (batchNo == null || batchNo.trim().isEmpty()) {
            return Result.fail(GlobalErrorCode.PARAM_INVALID, "批次号不能为空");
        }

        Optional<PharmacyStockEntity> stockOpt = stockRepository.findByDrugCodeAndBatchNo(drugCode.trim(), batchNo.trim());
        if (stockOpt.isEmpty()) {
            return Result.fail(PharmacyErrorCode.STOCK_BATCH_NOT_FOUND);
        }

        PharmacyStockEntity stock = stockOpt.get();
        BigDecimal newQuantity = stock.getQuantity().add(quantity);
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            return Result.fail(PharmacyErrorCode.STOCK_INSUFFICIENT,
                    "调整后库存为负数，当前库存: " + stock.getQuantity() + ", 调整数量: " + quantity);
        }
        stock.setQuantity(newQuantity);
        if (remark != null && !remark.trim().isEmpty()) {
            stock.setRemark(remark);
        }

        try {
            stock = stockRepository.save(stock);
            return Result.success(converter.toResponse(stock));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    public Result<List<PharmacyStockResponse>> listLowStock() {
        List<PharmacyStockEntity> lowStocks = stockRepository.findLowStock();
        return Result.success(converter.toStockResponseList(lowStocks));
    }
}
