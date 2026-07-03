package com.aimedical.modules.window.converter;

import com.aimedical.modules.window.dto.OfflineRegistrationResponse;
import com.aimedical.modules.window.dto.PaymentItemResponse;
import com.aimedical.modules.window.dto.PaymentRecordResponse;
import com.aimedical.modules.window.entity.OfflineRegistrationEntity;
import com.aimedical.modules.window.entity.PaymentItemEntity;
import com.aimedical.modules.window.entity.PaymentRecordEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 窗口模块实体与响应 DTO 转换器。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Component
public class WindowConverter {

    public OfflineRegistrationResponse toRegistrationResponse(OfflineRegistrationEntity entity) {
        if (entity == null) {
            return null;
        }
        OfflineRegistrationResponse resp = new OfflineRegistrationResponse();
        resp.setId(entity.getId());
        resp.setRegistrationNo(entity.getRegistrationNo());
        resp.setPatientId(entity.getPatientId());
        resp.setPatientName(entity.getPatientName());
        resp.setPatientPhone(entity.getPatientPhone());
        resp.setIdCard(entity.getIdCard());
        resp.setDoctorId(entity.getDoctorId());
        resp.setDoctorName(entity.getDoctorName());
        resp.setDepartment(entity.getDepartment());
        resp.setRegistrationType(entity.getRegistrationType());
        resp.setStatus(entity.getStatus());
        resp.setRegistrationFee(entity.getRegistrationFee());
        resp.setOperatorId(entity.getOperatorId());
        resp.setOperatorName(entity.getOperatorName());
        resp.setCancelReason(entity.getCancelReason());
        resp.setCancelTime(entity.getCancelTime());
        resp.setRemark(entity.getRemark());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());
        return resp;
    }

    public PaymentRecordResponse toPaymentResponse(PaymentRecordEntity entity, List<PaymentItemEntity> items) {
        if (entity == null) {
            return null;
        }
        PaymentRecordResponse resp = new PaymentRecordResponse();
        resp.setId(entity.getId());
        resp.setPaymentNo(entity.getPaymentNo());
        resp.setPatientId(entity.getPatientId());
        resp.setPatientName(entity.getPatientName());
        resp.setSourceId(entity.getSourceId());
        resp.setSourceType(entity.getSourceType());
        resp.setSourceNo(entity.getSourceNo());
        resp.setTotalAmount(entity.getTotalAmount());
        resp.setPaidAmount(entity.getPaidAmount());
        resp.setRefundAmount(entity.getRefundAmount());
        resp.setStatus(entity.getStatus());
        resp.setPaymentMethod(entity.getPaymentMethod());
        resp.setPayerName(entity.getPayerName());
        resp.setOperatorId(entity.getOperatorId());
        resp.setOperatorName(entity.getOperatorName());
        resp.setPaidAt(entity.getPaidAt());
        resp.setRefundedAt(entity.getRefundedAt());
        resp.setReconciledAt(entity.getReconciledAt());
        resp.setRefundReason(entity.getRefundReason());
        resp.setReconcileBatchNo(entity.getReconcileBatchNo());
        resp.setRemark(entity.getRemark());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());
        resp.setItems(Optional.ofNullable(items).orElse(Collections.emptyList())
                .stream()
                .map(this::toItemResponse)
                .toList());
        return resp;
    }

    public PaymentItemResponse toItemResponse(PaymentItemEntity entity) {
        if (entity == null) {
            return null;
        }
        PaymentItemResponse resp = new PaymentItemResponse();
        resp.setId(entity.getId());
        resp.setPaymentId(entity.getPaymentId());
        resp.setItemType(entity.getItemType());
        resp.setItemName(entity.getItemName());
        resp.setQuantity(entity.getQuantity());
        resp.setUnitPrice(entity.getUnitPrice());
        resp.setAmount(entity.getAmount());
        resp.setRemark(entity.getRemark());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());
        return resp;
    }

    /**
     * 批量转换缴费记录为响应（按 paymentId 分组明细，避免 N+1）。
     */
    public List<PaymentRecordResponse> toPaymentResponses(List<PaymentRecordEntity> entities,
                                                          List<PaymentItemEntity> allItems) {
        Map<Long, List<PaymentItemEntity>> itemMap = Optional.ofNullable(allItems)
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.groupingBy(PaymentItemEntity::getPaymentId));
        return entities.stream()
                .map(e -> toPaymentResponse(e, itemMap.getOrDefault(e.getId(), Collections.emptyList())))
                .toList();
    }
}
