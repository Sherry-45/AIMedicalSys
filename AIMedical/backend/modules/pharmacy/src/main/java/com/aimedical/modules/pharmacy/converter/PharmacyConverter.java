package com.aimedical.modules.pharmacy.converter;

import com.aimedical.modules.pharmacy.dto.DispensingItemResponse;
import com.aimedical.modules.pharmacy.dto.DispensingResponse;
import com.aimedical.modules.pharmacy.dto.PharmacyRefundItemResponse;
import com.aimedical.modules.pharmacy.dto.PharmacyRefundResponse;
import com.aimedical.modules.pharmacy.dto.PharmacyStockResponse;
import com.aimedical.modules.pharmacy.entity.DispensingItemEntity;
import com.aimedical.modules.pharmacy.entity.DispensingRecordEntity;
import com.aimedical.modules.pharmacy.entity.PharmacyRefundItemEntity;
import com.aimedical.modules.pharmacy.entity.PharmacyRefundRecordEntity;
import com.aimedical.modules.pharmacy.entity.PharmacyStockEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 药房域转换器。
 * <p>
 * 负责实体到响应 DTO 的转换，保持领域模型与传输模型解耦。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Component
public class PharmacyConverter {

    // ===== 库存 =====

    public PharmacyStockResponse toResponse(PharmacyStockEntity entity) {
        if (entity == null) {
            return null;
        }
        PharmacyStockResponse response = new PharmacyStockResponse();
        response.setId(entity.getId());
        response.setDrugCode(entity.getDrugCode());
        response.setDrugName(entity.getDrugName());
        response.setBatchNo(entity.getBatchNo());
        response.setQuantity(entity.getQuantity());
        response.setUnit(entity.getUnit());
        response.setRetailPrice(entity.getRetailPrice());
        response.setExpiryDate(entity.getExpiryDate());
        response.setShelfLocation(entity.getShelfLocation());
        response.setSafetyStock(entity.getSafetyStock());
        response.setRemark(entity.getRemark());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public List<PharmacyStockResponse> toStockResponseList(List<PharmacyStockEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ===== 发药 =====

    public DispensingResponse toResponse(DispensingRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        DispensingResponse response = new DispensingResponse();
        response.setId(entity.getId());
        response.setDispensingNo(entity.getDispensingNo());
        response.setPrescriptionId(entity.getPrescriptionId());
        response.setMedicalOrderId(entity.getMedicalOrderId());
        response.setPatientId(entity.getPatientId());
        response.setPatientName(entity.getPatientName());
        response.setPharmacistId(entity.getPharmacistId());
        response.setPharmacistName(entity.getPharmacistName());
        response.setStatus(entity.getStatus());
        response.setTotalQuantity(entity.getTotalQuantity());
        response.setTotalAmount(entity.getTotalAmount());
        response.setDispensedAt(entity.getDispensedAt());
        response.setRemark(entity.getRemark());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public DispensingItemResponse toResponse(DispensingItemEntity entity) {
        if (entity == null) {
            return null;
        }
        DispensingItemResponse response = new DispensingItemResponse();
        response.setId(entity.getId());
        response.setDispensingId(entity.getDispensingId());
        response.setDrugCode(entity.getDrugCode());
        response.setDrugName(entity.getDrugName());
        response.setSpecification(entity.getSpecification());
        response.setBatchNo(entity.getBatchNo());
        response.setQuantity(entity.getQuantity());
        response.setUnit(entity.getUnit());
        response.setUnitPrice(entity.getUnitPrice());
        response.setAmount(entity.getAmount());
        response.setDosage(entity.getDosage());
        response.setUsageMethod(entity.getUsageMethod());
        response.setFrequency(entity.getFrequency());
        response.setDays(entity.getDays());
        response.setRemark(entity.getRemark());
        return response;
    }

    public List<DispensingItemResponse> toDispensingItemResponseList(List<DispensingItemEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * 组装发药记录响应（含明细列表）。
     */
    public DispensingResponse toResponse(DispensingRecordEntity entity, List<DispensingItemEntity> items) {
        DispensingResponse response = toResponse(entity);
        if (response != null) {
            response.setItems(toDispensingItemResponseList(items));
        }
        return response;
    }

    // ===== 退药 =====

    public PharmacyRefundResponse toResponse(PharmacyRefundRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        PharmacyRefundResponse response = new PharmacyRefundResponse();
        response.setId(entity.getId());
        response.setRefundNo(entity.getRefundNo());
        response.setDispensingId(entity.getDispensingId());
        response.setPatientId(entity.getPatientId());
        response.setPatientName(entity.getPatientName());
        response.setPharmacistId(entity.getPharmacistId());
        response.setPharmacistName(entity.getPharmacistName());
        response.setStatus(entity.getStatus());
        response.setRefundReason(entity.getRefundReason());
        response.setTotalQuantity(entity.getTotalQuantity());
        response.setTotalAmount(entity.getTotalAmount());
        response.setRefundedAt(entity.getRefundedAt());
        response.setRemark(entity.getRemark());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public PharmacyRefundItemResponse toResponse(PharmacyRefundItemEntity entity) {
        if (entity == null) {
            return null;
        }
        PharmacyRefundItemResponse response = new PharmacyRefundItemResponse();
        response.setId(entity.getId());
        response.setRefundId(entity.getRefundId());
        response.setDispensingItemId(entity.getDispensingItemId());
        response.setDrugCode(entity.getDrugCode());
        response.setDrugName(entity.getDrugName());
        response.setBatchNo(entity.getBatchNo());
        response.setQuantity(entity.getQuantity());
        response.setUnit(entity.getUnit());
        response.setUnitPrice(entity.getUnitPrice());
        response.setAmount(entity.getAmount());
        response.setRemark(entity.getRemark());
        return response;
    }

    public List<PharmacyRefundItemResponse> toRefundItemResponseList(List<PharmacyRefundItemEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * 组装退药记录响应（含明细列表）。
     */
    public PharmacyRefundResponse toResponse(PharmacyRefundRecordEntity entity, List<PharmacyRefundItemEntity> items) {
        PharmacyRefundResponse response = toResponse(entity);
        if (response != null) {
            response.setItems(toRefundItemResponseList(items));
        }
        return response;
    }
}
