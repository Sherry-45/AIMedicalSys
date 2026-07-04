package com.aimedical.modules.inventory.converter;

import com.aimedical.modules.inventory.dto.response.DrugCatalogResponse;
import com.aimedical.modules.inventory.dto.response.InventoryStockResponse;
import com.aimedical.modules.inventory.dto.response.StocktakingItemResponse;
import com.aimedical.modules.inventory.dto.response.StocktakingResponse;
import com.aimedical.modules.inventory.dto.response.TransferItemResponse;
import com.aimedical.modules.inventory.dto.response.TransferOrderResponse;
import com.aimedical.modules.inventory.entity.DrugCatalogEntity;
import com.aimedical.modules.inventory.entity.InventoryStockEntity;
import com.aimedical.modules.inventory.entity.StocktakingEntity;
import com.aimedical.modules.inventory.entity.StocktakingItemEntity;
import com.aimedical.modules.inventory.entity.TransferItemEntity;
import com.aimedical.modules.inventory.entity.TransferOrderEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 库存模块实体与 DTO 转换器。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Component
public class InventoryConverter {

    /**
     * 药品目录实体转响应 DTO。
     */
    public DrugCatalogResponse toDrugCatalogResponse(DrugCatalogEntity entity) {
        if (entity == null) {
            return null;
        }
        DrugCatalogResponse response = new DrugCatalogResponse();
        response.setId(entity.getId());
        response.setDrugCode(entity.getDrugCode());
        response.setDrugName(entity.getDrugName());
        response.setGenericName(entity.getGenericName());
        response.setSpecification(entity.getSpecification());
        response.setManufacturer(entity.getManufacturer());
        response.setDrugForm(entity.getDrugForm());
        response.setDrugCategory(entity.getDrugCategory());
        response.setUnit(entity.getUnit());
        response.setRetailPrice(entity.getRetailPrice());
        response.setPurchasePrice(entity.getPurchasePrice());
        response.setOtcFlag(entity.getOtcFlag());
        response.setEnabled(entity.getEnabled());
        response.setRemark(entity.getRemark());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    /**
     * 库存批次实体转响应 DTO。
     */
    public InventoryStockResponse toStockResponse(InventoryStockEntity entity) {
        if (entity == null) {
            return null;
        }
        InventoryStockResponse response = new InventoryStockResponse();
        response.setId(entity.getId());
        response.setDrugCode(entity.getDrugCode());
        response.setBatchNo(entity.getBatchNo());
        response.setQuantity(entity.getQuantity());
        response.setUnit(entity.getUnit());
        response.setPurchasePrice(entity.getPurchasePrice());
        response.setRetailPrice(entity.getRetailPrice());
        response.setExpiryDate(entity.getExpiryDate());
        response.setProductionDate(entity.getProductionDate());
        response.setWarehouseLocation(entity.getWarehouseLocation());
        response.setRemark(entity.getRemark());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    /**
     * 盘点单实体转响应 DTO（含明细）。
     */
    public StocktakingResponse toStocktakingResponse(StocktakingEntity entity, List<StocktakingItemEntity> items) {
        if (entity == null) {
            return null;
        }
        StocktakingResponse response = new StocktakingResponse();
        response.setId(entity.getId());
        response.setStocktakingNo(entity.getStocktakingNo());
        response.setStocktakingType(entity.getStocktakingType());
        response.setStatus(entity.getStatus());
        response.setOperatorId(entity.getOperatorId());
        response.setOperatorName(entity.getOperatorName());
        response.setStartTime(entity.getStartTime());
        response.setEndTime(entity.getEndTime());
        response.setTotalItems(entity.getTotalItems());
        response.setSurplusItems(entity.getSurplusItems());
        response.setLossItems(entity.getLossItems());
        response.setApproverId(entity.getApproverId());
        response.setApproverName(entity.getApproverName());
        response.setApprovedAt(entity.getApprovedAt());
        response.setRejectReason(entity.getRejectReason());
        response.setRemark(entity.getRemark());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setItems(Optional.ofNullable(items).orElse(Collections.emptyList())
                .stream().map(this::toStocktakingItemResponse).toList());
        return response;
    }

    /**
     * 盘点明细实体转响应 DTO。
     */
    public StocktakingItemResponse toStocktakingItemResponse(StocktakingItemEntity entity) {
        if (entity == null) {
            return null;
        }
        StocktakingItemResponse response = new StocktakingItemResponse();
        response.setId(entity.getId());
        response.setStocktakingId(entity.getStocktakingId());
        response.setDrugCode(entity.getDrugCode());
        response.setDrugName(entity.getDrugName());
        response.setBatchNo(entity.getBatchNo());
        response.setBookQuantity(entity.getBookQuantity());
        response.setActualQuantity(entity.getActualQuantity());
        response.setDifference(entity.getDifference());
        response.setDifferenceType(entity.getDifferenceType());
        response.setUnit(entity.getUnit());
        response.setRemark(entity.getRemark());
        return response;
    }

    /**
     * 调拨单实体转响应 DTO（含明细）。
     */
    public TransferOrderResponse toTransferResponse(TransferOrderEntity entity, List<TransferItemEntity> items) {
        if (entity == null) {
            return null;
        }
        TransferOrderResponse response = new TransferOrderResponse();
        response.setId(entity.getId());
        response.setTransferNo(entity.getTransferNo());
        response.setTransferType(entity.getTransferType());
        response.setStatus(entity.getStatus());
        response.setSourceDept(entity.getSourceDept());
        response.setTargetDept(entity.getTargetDept());
        response.setApplicantId(entity.getApplicantId());
        response.setApplicantName(entity.getApplicantName());
        response.setApproverId(entity.getApproverId());
        response.setApproverName(entity.getApproverName());
        response.setApprovedAt(entity.getApprovedAt());
        response.setShippedAt(entity.getShippedAt());
        response.setReceivedAt(entity.getReceivedAt());
        response.setTotalItems(entity.getTotalItems());
        response.setTotalAmount(entity.getTotalAmount());
        response.setRejectReason(entity.getRejectReason());
        response.setRemark(entity.getRemark());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setItems(Optional.ofNullable(items).orElse(Collections.emptyList())
                .stream().map(this::toTransferItemResponse).toList());
        return response;
    }

    /**
     * 调拨明细实体转响应 DTO。
     */
    public TransferItemResponse toTransferItemResponse(TransferItemEntity entity) {
        if (entity == null) {
            return null;
        }
        TransferItemResponse response = new TransferItemResponse();
        response.setId(entity.getId());
        response.setTransferId(entity.getTransferId());
        response.setDrugCode(entity.getDrugCode());
        response.setDrugName(entity.getDrugName());
        response.setSpecification(entity.getSpecification());
        response.setBatchNo(entity.getBatchNo());
        response.setQuantity(entity.getQuantity());
        response.setUnit(entity.getUnit());
        response.setUnitPrice(entity.getUnitPrice());
        response.setAmount(entity.getAmount());
        response.setRemark(entity.getRemark());
        return response;
    }
}
