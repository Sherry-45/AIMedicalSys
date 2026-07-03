package com.aimedical.modules.window.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.window.WindowErrorCode;
import com.aimedical.modules.window.converter.WindowConverter;
import com.aimedical.modules.window.dto.PaymentRecordResponse;
import com.aimedical.modules.window.dto.ReconcileRequest;
import com.aimedical.modules.window.dto.ReconcileResponse;
import com.aimedical.modules.window.entity.PaymentItemEntity;
import com.aimedical.modules.window.entity.PaymentRecordEntity;
import com.aimedical.modules.window.entity.PaymentStatus;
import com.aimedical.modules.window.repository.PaymentItemRepository;
import com.aimedical.modules.window.repository.PaymentRecordRepository;
import com.aimedical.modules.window.service.ReconciliationService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 对账服务实现。
 *
 * <p>批量将已支付(PAID)缴费记录置为已对账(RECONCILED)，统一打上对账批次号。
 * 仅 PAID 可对账；任一记录状态不符则整批失败，保证对账批次的完整性。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class ReconciliationServiceImpl implements ReconciliationService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final WindowConverter converter;

    public ReconciliationServiceImpl(PaymentRecordRepository paymentRecordRepository,
                                     PaymentItemRepository paymentItemRepository,
                                     WindowConverter converter) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.paymentItemRepository = paymentItemRepository;
        this.converter = converter;
    }

    @Override
    @Transactional
    public Result<ReconcileResponse> reconcile(ReconcileRequest request,
                                                Long operatorId,
                                                String operatorName) {
        List<Long> paymentIds = request.getPaymentIds();
        if (paymentIds == null || paymentIds.isEmpty()) {
            return Result.fail(WindowErrorCode.RECONCILE_NO_RECORDS);
        }

        // 一次性取出全部待对账记录
        List<PaymentRecordEntity> records = new ArrayList<>();
        for (Long id : paymentIds) {
            Optional<PaymentRecordEntity> opt = paymentRecordRepository.findById(id);
            if (opt.isEmpty()) {
                return Result.fail(WindowErrorCode.PAYMENT_NOT_FOUND);
            }
            records.add(opt.get());
        }

        // 校验全部为 PAID，任一不符则整批拒绝
        for (PaymentRecordEntity record : records) {
            if (!PaymentStatus.PAID.getCode().equals(record.getStatus())) {
                return Result.fail(WindowErrorCode.RECONCILE_INVALID_STATE);
            }
        }

        String batchNo = StringUtils.hasText(request.getReconcileBatchNo())
                ? request.getReconcileBatchNo()
                : generateBatchNo();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PaymentRecordEntity record : records) {
            record.setStatus(PaymentStatus.RECONCILED.getCode());
            record.setReconcileBatchNo(batchNo);
            record.setReconciledAt(now);
            totalAmount = totalAmount.add(Optional.ofNullable(record.getPaidAmount()).orElse(BigDecimal.ZERO));
        }

        try {
            paymentRecordRepository.saveAllAndFlush(records);
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }

        ReconcileResponse response = new ReconcileResponse();
        response.setReconcileBatchNo(batchNo);
        response.setReconciledCount(records.size());
        response.setTotalAmount(totalAmount);
        return Result.success(response);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<PaymentRecordResponse>> listByBatchNo(String batchNo) {
        List<PaymentRecordEntity> records = paymentRecordRepository.findByReconcileBatchNo(batchNo);
        if (records.isEmpty()) {
            return Result.success(List.of());
        }
        List<Long> paymentIds = records.stream().map(PaymentRecordEntity::getId).toList();
        List<PaymentItemEntity> allItems = paymentItemRepository.findByPaymentIdIn(paymentIds);
        Map<Long, List<PaymentItemEntity>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(PaymentItemEntity::getPaymentId));
        List<PaymentRecordResponse> responses = records.stream()
                .map(e -> converter.toPaymentResponse(e, itemMap.getOrDefault(e.getId(), List.of())))
                .toList();
        return Result.success(responses);
    }

    private String generateBatchNo() {
        return "RCN" + System.currentTimeMillis()
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
