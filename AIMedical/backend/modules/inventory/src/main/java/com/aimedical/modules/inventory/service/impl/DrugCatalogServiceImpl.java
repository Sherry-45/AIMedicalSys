package com.aimedical.modules.inventory.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.InventoryErrorCode;
import com.aimedical.modules.inventory.converter.InventoryConverter;
import com.aimedical.modules.inventory.dto.request.DrugCatalogCreateRequest;
import com.aimedical.modules.inventory.dto.request.DrugCatalogQueryRequest;
import com.aimedical.modules.inventory.dto.response.DrugCatalogResponse;
import com.aimedical.modules.inventory.entity.DrugCatalogEntity;
import com.aimedical.modules.inventory.entity.DrugCategory;
import com.aimedical.modules.inventory.repository.DrugCatalogRepository;
import com.aimedical.modules.inventory.service.DrugCatalogService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 药品目录服务实现。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class DrugCatalogServiceImpl implements DrugCatalogService {

    private final DrugCatalogRepository drugCatalogRepository;
    private final InventoryConverter converter;

    public DrugCatalogServiceImpl(DrugCatalogRepository drugCatalogRepository, InventoryConverter converter) {
        this.drugCatalogRepository = drugCatalogRepository;
        this.converter = converter;
    }

    @Override
    @Transactional
    public Result<DrugCatalogResponse> create(DrugCatalogCreateRequest request) {
        if (drugCatalogRepository.existsByDrugCode(request.getDrugCode())) {
            return Result.fail(InventoryErrorCode.DRUG_CODE_DUPLICATE);
        }
        validateDrugCategory(request.getDrugCategory());

        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setDrugCode(request.getDrugCode());
        entity.setDrugName(request.getDrugName());
        entity.setGenericName(request.getGenericName());
        entity.setSpecification(request.getSpecification());
        entity.setManufacturer(request.getManufacturer());
        entity.setDrugForm(request.getDrugForm());
        entity.setDrugCategory(request.getDrugCategory());
        entity.setUnit(request.getUnit());
        entity.setRetailPrice(request.getRetailPrice());
        entity.setPurchasePrice(request.getPurchasePrice());
        entity.setOtcFlag(request.getOtcFlag());
        entity.setEnabled(true);
        entity.setRemark(request.getRemark());

        DrugCatalogEntity saved = drugCatalogRepository.save(entity);
        return Result.success(converter.toDrugCatalogResponse(saved));
    }

    @Override
    @Transactional
    public Result<DrugCatalogResponse> update(Long id, DrugCatalogCreateRequest request) {
        Optional<DrugCatalogEntity> opt = drugCatalogRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.DRUG_NOT_FOUND);
        }
        validateDrugCategory(request.getDrugCategory());

        DrugCatalogEntity entity = opt.get();
        // 编码变更时校验唯一性
        if (!entity.getDrugCode().equals(request.getDrugCode())
                && drugCatalogRepository.existsByDrugCode(request.getDrugCode())) {
            return Result.fail(InventoryErrorCode.DRUG_CODE_DUPLICATE);
        }
        entity.setDrugCode(request.getDrugCode());
        entity.setDrugName(request.getDrugName());
        entity.setGenericName(request.getGenericName());
        entity.setSpecification(request.getSpecification());
        entity.setManufacturer(request.getManufacturer());
        entity.setDrugForm(request.getDrugForm());
        entity.setDrugCategory(request.getDrugCategory());
        entity.setUnit(request.getUnit());
        entity.setRetailPrice(request.getRetailPrice());
        entity.setPurchasePrice(request.getPurchasePrice());
        entity.setOtcFlag(request.getOtcFlag());
        entity.setRemark(request.getRemark());

        try {
            DrugCatalogEntity saved = drugCatalogRepository.save(entity);
            return Result.success(converter.toDrugCatalogResponse(saved));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<Void> delete(Long id) {
        Optional<DrugCatalogEntity> opt = drugCatalogRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.DRUG_NOT_FOUND);
        }
        // 软删除：BaseEntity 的 @SQLDelete 会将 deleted 置为 true
        drugCatalogRepository.delete(opt.get());
        return Result.success(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<DrugCatalogResponse> getById(Long id) {
        return drugCatalogRepository.findById(id)
                .map(entity -> Result.success(converter.toDrugCatalogResponse(entity)))
                .orElseGet(() -> Result.fail(InventoryErrorCode.DRUG_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<DrugCatalogResponse> getByDrugCode(String drugCode) {
        return drugCatalogRepository.findByDrugCode(drugCode)
                .map(entity -> Result.success(converter.toDrugCatalogResponse(entity)))
                .orElseGet(() -> Result.fail(InventoryErrorCode.DRUG_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Page<DrugCatalogResponse>> query(DrugCatalogQueryRequest request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 20 : request.getSize();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<DrugCatalogEntity> entityPage;
        boolean hasName = request.getDrugName() != null && !request.getDrugName().isBlank();
        boolean hasCategory = request.getDrugCategory() != null && !request.getDrugCategory().isBlank();

        if (hasName && hasCategory) {
            entityPage = drugCatalogRepository.findByDrugNameContainingAndDrugCategory(
                    request.getDrugName(), request.getDrugCategory(), pageable);
        } else if (hasName) {
            entityPage = drugCatalogRepository.findByDrugNameContaining(request.getDrugName(), pageable);
        } else if (hasCategory) {
            entityPage = drugCatalogRepository.findByDrugCategory(request.getDrugCategory(), pageable);
        } else {
            entityPage = drugCatalogRepository.findAll(pageable);
        }

        // enabled 过滤在内存中完成（避免组合查询方法爆炸）
        Page<DrugCatalogResponse> responsePage = entityPage.map(converter::toDrugCatalogResponse);
        if (request.getEnabled() != null) {
            List<DrugCatalogResponse> filtered = responsePage.getContent().stream()
                    .filter(r -> request.getEnabled().equals(r.getEnabled()))
                    .toList();
            // 简单返回过滤后的列表分页（保持 Page 包装）
            responsePage = new PageImpl<>(filtered, pageable, filtered.size());
        }
        return Result.success(responsePage);
    }

    @Override
    @Transactional
    public Result<DrugCatalogResponse> toggleEnabled(Long id) {
        Optional<DrugCatalogEntity> opt = drugCatalogRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(InventoryErrorCode.DRUG_NOT_FOUND);
        }
        DrugCatalogEntity entity = opt.get();
        entity.setEnabled(!Boolean.TRUE.equals(entity.getEnabled()));
        try {
            DrugCatalogEntity saved = drugCatalogRepository.save(entity);
            return Result.success(converter.toDrugCatalogResponse(saved));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    /**
     * 校验药品分类合法性。
     */
    private void validateDrugCategory(String category) {
        if (category == null || category.isBlank()) {
            return;
        }
        for (DrugCategory c : DrugCategory.values()) {
            if (c.getCode().equals(category)) {
                return;
            }
        }
        throw new IllegalArgumentException("无效的药品分类: " + category);
    }
}
