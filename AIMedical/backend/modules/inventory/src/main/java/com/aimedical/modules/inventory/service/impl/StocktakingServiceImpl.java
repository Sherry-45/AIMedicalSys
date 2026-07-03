package com.aimedical.modules.inventory.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.InventoryErrorCode;
import com.aimedical.modules.inventory.converter.InventoryConverter;
import com.aimedical.modules.inventory.dto.request.StocktakingCreateRequest;
import com.aimedical.modules.inventory.dto.request.StocktakingItemRequest;
import com.aimedical.modules.inventory.dto.request.StocktakingQueryRequest;
import com.aimedical.modules.inventory.dto.response.StocktakingResponse;
import com.aimedical.modules.inventory.entity.InventoryStockEntity;
import com.aimedical.modules.inventory.entity.StocktakingDifferenceType;
import com.aimedical.modules.inventory.entity.StocktakingEntity;
import com.aimedical.modules.inventory.entity.StocktakingItemEntity;
import com.aimedical.modules.inventory.entity.StocktakingStatus;
import com.aimedical.modules.inventory.entity.StocktakingType;
import com.aimedical.modules.inventory.repository.InventoryStockRepository;
import com.aimedical.modules.inventory.repository.StocktakingItemRepository;
import com.aimedical.modules.inventory.repository.StocktakingRepository;
import com.aimedical.modules.inventory.service.StocktakingService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 盘点服务实现。
 *
 * <p>状态机：DRAFT -> IN_PROGRESS -> COMPLETED (或 CANCELLED)。
 * <ul>
 *   <li>create：创建盘点单与明细，自动从库存批次填充账面数量</li>
 *   <li>start：DRAFT -> IN_PROGRESS，记录开始时间</li>
 *   <li>submitActual：提交实际数量，更新明细的实际数量</li>
 *   <li>complete：IN_PROGRESS -> COMPLETED，计算差异、盘盈/盘亏项数，回写库存</li>
 *   <li>cancel：DRAFT/IN_PROGRESS -> CANCELLED</li>
 * </ul>
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class StocktakingServiceImpl implements StocktakingService {

    private final StocktakingRepository stocktakingRepository;
    private final StocktakingItemRepository stocktakingItemRepository;
    private final InventoryStockRepository stockRepository;
    private final InventoryConverter converter;

    public StocktakingServiceImpl(StocktakingRepository stocktakingRepository,
                                  StocktakingItemRepository stocktakingItemRepository,
                                  InventoryStockRepository stockRepository,
                                  InventoryConverter converter) {
        this.stocktakingRepository = stocktakingRepository;
        this.stocktakingItemRepository = stocktakingItemRepository;
        this.stockRepository = stockRepository;
        this.converter = converter;
    }

    @Override
    @Transactional
    public Result<StocktakingResponse> create(StocktakingCreateRequest request, Long operatorId, String operatorName) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return Result.fail(InventoryErrorCode.STOCKTAKING_ITEM_EMPTY);
        }

        StocktakingEntity entity = new StocktakingEntity();
        entity.setStocktakingNo(generateStocktakingNo());
        entity.setStocktakingType(request.getStocktakingType() == null || request.getStocktakingType().isBlank()
                ? StocktakingType.FULL.getCode() : request.getStocktakingType());
        entity.setStatus(StocktakingStatus.DRAFT.getCode());
        entity.setOperatorId(operatorId);
        entity.setOperatorName(operatorName);
        entity.setRemark(request.getRemark());
        entity.setTotalItems(request.getItems().size());
        entity.setSurplusItems(0);
        entity.setLossItems(0);

        StocktakingEntity saved = stocktakingRepository.save(entity);

        // 创建明细，自动从库存批次填充账面数量
        List<StocktakingItemEntity> items = new ArrayList<>();
        for (StocktakingItemRequest itemReq : request.getItems()) {
            StocktakingItemEntity item = new StocktakingItemEntity();
            item.setStocktakingId(saved.getId());
            item.setDrugCode(itemReq.getDrugCode());
            item.setDrugName(itemReq.getDrugName());
            item.setBatchNo(itemReq.getBatchNo());
            item.setUnit(itemReq.getUnit());
            item.setRemark(itemReq.getRemark());

            // 自动填充账面数量：优先按药品编码+批次号精确匹配，否则汇总该药品全部批次
            BigDecimal bookQuantity = lookupBookQuantity(itemReq.getDrugCode(), itemReq.getBatchNo());
            item.setBookQuantity(bookQuantity);
            // 创建时实际数量留空，待 start 后由 submitActual 填充
            item.setActualQuantity(null);
            item.setDifference(null);
            item.setDifferenceType(StocktakingDifferenceType.NONE.getCode());

            items.add(item);
        }
        stocktakingItemRepository.saveAll(items);

        return Result.success(converter.toStocktakingResponse(saved, items));
    }

    @Override
    @Transactional
    public Result<StocktakingResponse> start(Long id) {
        Optional<StocktakingEntity> opt = stocktakingRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.STOCKTAKING_NOT_FOUND);
        }
        StocktakingEntity entity = opt.get();
        if (!StocktakingStatus.DRAFT.getCode().equals(entity.getStatus())) {
            return Result.fail(InventoryErrorCode.STOCKTAKING_INVALID_STATE);
        }
        entity.setStatus(StocktakingStatus.IN_PROGRESS.getCode());
        entity.setStartTime(LocalDateTime.now());
        try {
            StocktakingEntity saved = stocktakingRepository.save(entity);
            List<StocktakingItemEntity> items = stocktakingItemRepository.findByStocktakingId(id);
            return Result.success(converter.toStocktakingResponse(saved, items));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<StocktakingResponse> submitActual(Long id, List<StocktakingItemRequest> items) {
        Optional<StocktakingEntity> opt = stocktakingRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.STOCKTAKING_NOT_FOUND);
        }
        StocktakingEntity entity = opt.get();
        // 仅 IN_PROGRESS 状态可提交实际数量
        if (!StocktakingStatus.IN_PROGRESS.getCode().equals(entity.getStatus())) {
            return Result.fail(InventoryErrorCode.STOCKTAKING_INVALID_STATE);
        }
        if (items == null || items.isEmpty()) {
            return Result.fail(InventoryErrorCode.STOCKTAKING_ACTUAL_EMPTY);
        }

        List<StocktakingItemEntity> existingItems = stocktakingItemRepository.findByStocktakingId(id);
        // 按 drugCode + batchNo 建立索引，便于匹配实际数量
        for (StocktakingItemEntity existing : existingItems) {
            for (StocktakingItemRequest req : items) {
                if (sameItem(existing, req)) {
                    existing.setActualQuantity(req.getActualQuantity());
                    // 同步备注
                    if (req.getRemark() != null && !req.getRemark().isBlank()) {
                        existing.setRemark(req.getRemark());
                    }
                    break;
                }
            }
        }
        stocktakingItemRepository.saveAll(existingItems);

        return Result.success(converter.toStocktakingResponse(entity, existingItems));
    }

    @Override
    @Transactional
    public Result<StocktakingResponse> complete(Long id) {
        Optional<StocktakingEntity> opt = stocktakingRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.STOCKTAKING_NOT_FOUND);
        }
        StocktakingEntity entity = opt.get();
        if (!StocktakingStatus.IN_PROGRESS.getCode().equals(entity.getStatus())) {
            return Result.fail(InventoryErrorCode.STOCKTAKING_INVALID_STATE);
        }

        List<StocktakingItemEntity> items = stocktakingItemRepository.findByStocktakingId(id);
        int surplus = 0;
        int loss = 0;
        for (StocktakingItemEntity item : items) {
            BigDecimal book = item.getBookQuantity() == null ? BigDecimal.ZERO : item.getBookQuantity();
            BigDecimal actual = item.getActualQuantity() == null ? book : item.getActualQuantity();
            BigDecimal difference = actual.subtract(book);
            item.setDifference(difference);

            int cmp = difference.compareTo(BigDecimal.ZERO);
            if (cmp > 0) {
                item.setDifferenceType(StocktakingDifferenceType.SURPLUS.getCode());
                surplus++;
            } else if (cmp < 0) {
                item.setDifferenceType(StocktakingDifferenceType.LOSS.getCode());
                loss++;
            } else {
                item.setDifferenceType(StocktakingDifferenceType.NONE.getCode());
            }

            // 回写库存：以实际数量为准调整库存批次
            adjustStockByDifference(item.getDrugCode(), item.getBatchNo(), difference);
        }
        stocktakingItemRepository.saveAll(items);

        entity.setStatus(StocktakingStatus.COMPLETED.getCode());
        entity.setEndTime(LocalDateTime.now());
        entity.setTotalItems(items.size());
        entity.setSurplusItems(surplus);
        entity.setLossItems(loss);

        try {
            StocktakingEntity saved = stocktakingRepository.save(entity);
            return Result.success(converter.toStocktakingResponse(saved, items));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<StocktakingResponse> cancel(Long id) {
        Optional<StocktakingEntity> opt = stocktakingRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.STOCKTAKING_NOT_FOUND);
        }
        StocktakingEntity entity = opt.get();
        String status = entity.getStatus();
        // 仅 DRAFT / IN_PROGRESS 可取消
        if (!StocktakingStatus.DRAFT.getCode().equals(status)
                && !StocktakingStatus.IN_PROGRESS.getCode().equals(status)) {
            return Result.fail(InventoryErrorCode.STOCKTAKING_INVALID_STATE);
        }
        entity.setStatus(StocktakingStatus.CANCELLED.getCode());
        try {
            StocktakingEntity saved = stocktakingRepository.save(entity);
            List<StocktakingItemEntity> items = stocktakingItemRepository.findByStocktakingId(id);
            return Result.success(converter.toStocktakingResponse(saved, items));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Result<StocktakingResponse> getById(Long id) {
        return stocktakingRepository.findById(id)
                .map(entity -> {
                    List<StocktakingItemEntity> items = stocktakingItemRepository.findByStocktakingId(id);
                    return Result.success(converter.toStocktakingResponse(entity, items));
                })
                .orElseGet(() -> Result.fail(InventoryErrorCode.STOCKTAKING_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Page<StocktakingResponse>> query(StocktakingQueryRequest request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 20 : request.getSize();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<StocktakingEntity> entityPage;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            entityPage = stocktakingRepository.findByStatus(request.getStatus(), pageable);
        } else {
            entityPage = stocktakingRepository.findAll(pageable);
        }

        Page<StocktakingResponse> responsePage = entityPage.map(entity -> {
            List<StocktakingItemEntity> items = stocktakingItemRepository.findByStocktakingId(entity.getId());
            return converter.toStocktakingResponse(entity, items);
        });
        return Result.success(responsePage);
    }

    /**
     * 生成盘点单号：STK + 时间戳。
     */
    private String generateStocktakingNo() {
        return "STK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    /**
     * 从库存批次查询账面数量。
     * 优先按药品编码+批次号精确匹配；批次号为空时汇总该药品全部批次。
     */
    private BigDecimal lookupBookQuantity(String drugCode, String batchNo) {
        if (batchNo != null && !batchNo.isBlank()) {
            return stockRepository.findByDrugCodeAndBatchNo(drugCode, batchNo)
                    .map(InventoryStockEntity::getQuantity)
                    .orElse(BigDecimal.ZERO);
        }
        List<InventoryStockEntity> batches = stockRepository.findByDrugCode(drugCode);
        return batches.stream()
                .map(e -> e.getQuantity() == null ? BigDecimal.ZERO : e.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 判断明细是否匹配同一药品批次（批次号为空时按药品编码匹配）。
     */
    private boolean sameItem(StocktakingItemEntity existing, StocktakingItemRequest req) {
        if (!existing.getDrugCode().equals(req.getDrugCode())) {
            return false;
        }
        if (existing.getBatchNo() == null || existing.getBatchNo().isBlank()) {
            return req.getBatchNo() == null || req.getBatchNo().isBlank();
        }
        return existing.getBatchNo().equals(req.getBatchNo());
    }

    /**
     * 盘点完成时按差异回写库存：盘盈增加库存，盘亏减少库存。
     */
    private void adjustStockByDifference(String drugCode, String batchNo, BigDecimal difference) {
        if (difference == null || difference.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        if (batchNo == null || batchNo.isBlank()) {
            // 无批次号时跳过回写（无法定位具体批次）
            return;
        }
        Optional<InventoryStockEntity> opt = stockRepository.findByDrugCodeAndBatchNo(drugCode, batchNo);
        if (opt.isEmpty()) {
            return;
        }
        InventoryStockEntity stock = opt.get();
        BigDecimal newQuantity = stock.getQuantity().add(difference);
        // 不允许负库存
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            newQuantity = BigDecimal.ZERO;
        }
        stock.setQuantity(newQuantity);
        stockRepository.save(stock);
    }
}
