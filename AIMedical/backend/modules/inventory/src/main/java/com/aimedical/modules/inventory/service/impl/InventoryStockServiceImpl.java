package com.aimedical.modules.inventory.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.InventoryErrorCode;
import com.aimedical.modules.inventory.converter.InventoryConverter;
import com.aimedical.modules.inventory.dto.request.InventoryStockQueryRequest;
import com.aimedical.modules.inventory.dto.request.StockAdjustRequest;
import com.aimedical.modules.inventory.dto.response.InventoryStockResponse;
import com.aimedical.modules.inventory.entity.InventoryStockEntity;
import com.aimedical.modules.inventory.repository.InventoryStockRepository;
import com.aimedical.modules.inventory.service.InventoryStockService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 库存服务实现。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class InventoryStockServiceImpl implements InventoryStockService {

    private final InventoryStockRepository stockRepository;
    private final InventoryConverter converter;

    public InventoryStockServiceImpl(InventoryStockRepository stockRepository, InventoryConverter converter) {
        this.stockRepository = stockRepository;
        this.converter = converter;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Page<InventoryStockResponse>> query(InventoryStockQueryRequest request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 20 : request.getSize();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<InventoryStockEntity> entityPage;
        boolean hasDrugCode = request.getDrugCode() != null && !request.getDrugCode().isBlank();
        boolean hasBatchNo = request.getBatchNo() != null && !request.getBatchNo().isBlank();

        if (hasDrugCode && hasBatchNo) {
            entityPage = stockRepository.findByDrugCodeAndBatchNo(
                    request.getDrugCode(), request.getBatchNo(), pageable);
        } else if (hasDrugCode) {
            entityPage = stockRepository.findByDrugCode(request.getDrugCode(), pageable);
        } else {
            entityPage = stockRepository.findAll(pageable);
        }

        return Result.success(entityPage.map(converter::toStockResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<InventoryStockResponse>> getByDrugCode(String drugCode) {
        List<InventoryStockResponse> list = stockRepository.findByDrugCode(drugCode).stream()
                .map(converter::toStockResponse)
                .toList();
        return Result.success(list);
    }

    @Override
    @Transactional
    public Result<InventoryStockResponse> adjustStock(StockAdjustRequest request) {
        Optional<InventoryStockEntity> opt = stockRepository
                .findByDrugCodeAndBatchNo(request.getDrugCode(), request.getBatchNo());
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.STOCK_BATCH_NOT_FOUND);
        }
        InventoryStockEntity entity = opt.get();
        BigDecimal newQuantity = entity.getQuantity().add(request.getQuantity());
        // 调整后不允许为负
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            return Result.fail(InventoryErrorCode.STOCK_INSUFFICIENT);
        }
        entity.setQuantity(newQuantity);
        if (request.getRemark() != null && !request.getRemark().isBlank()) {
            entity.setRemark(request.getRemark());
        }
        try {
            InventoryStockEntity saved = stockRepository.save(entity);
            return Result.success(converter.toStockResponse(saved));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<InventoryStockResponse>> listExpiringSoon(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        List<InventoryStockResponse> list = stockRepository.findByExpiryDateBefore(threshold).stream()
                .map(converter::toStockResponse)
                .toList();
        return Result.success(list);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<InventoryStockResponse>> listLowStock() {
        // 低库存定义：库存数量 <= 0 的批次（含已耗尽批次）
        // 由于无独立 low_stock_threshold 字段，此处返回数量为零或低于安全值(10)的批次
        List<InventoryStockResponse> list = stockRepository.findAll().stream()
                .filter(entity -> entity.getQuantity() != null
                        && entity.getQuantity().compareTo(new BigDecimal("10")) <= 0)
                .map(converter::toStockResponse)
                .toList();
        return Result.success(list);
    }
}
