package com.aimedical.modules.window.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.window.converter.WindowConverter;
import com.aimedical.modules.window.dto.OfflineRegistrationCancelRequest;
import com.aimedical.modules.window.dto.OfflineRegistrationCreateRequest;
import com.aimedical.modules.window.dto.OfflineRegistrationQueryRequest;
import com.aimedical.modules.window.dto.OfflineRegistrationResponse;
import com.aimedical.modules.window.dto.PaymentRecordResponse;
import com.aimedical.modules.window.dto.RefundRequest;
import com.aimedical.modules.window.entity.OfflineRegistrationEntity;
import com.aimedical.modules.window.entity.OfflineRegistrationStatus;
import com.aimedical.modules.window.entity.OfflineRegistrationType;
import com.aimedical.modules.window.entity.PaymentItemEntity;
import com.aimedical.modules.window.entity.PaymentItemType;
import com.aimedical.modules.window.entity.PaymentRecordEntity;
import com.aimedical.modules.window.entity.PaymentSourceType;
import com.aimedical.modules.window.entity.PaymentStatus;
import com.aimedical.modules.window.repository.OfflineRegistrationRepository;
import com.aimedical.modules.window.repository.PaymentItemRepository;
import com.aimedical.modules.window.repository.PaymentRecordRepository;
import com.aimedical.modules.window.service.OfflineRegistrationService;
import com.aimedical.modules.window.service.PaymentService;
import com.aimedical.modules.window.WindowErrorCode;
import jakarta.persistence.criteria.Predicate;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 线下挂号服务实现。
 *
 * <p>创建挂号时在同一事务内同步生成挂号费待支付缴费记录，保证挂号与收费前置单的原子性。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class OfflineRegistrationServiceImpl implements OfflineRegistrationService {

    private final OfflineRegistrationRepository registrationRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final PaymentService paymentService;
    private final WindowConverter converter;

    public OfflineRegistrationServiceImpl(OfflineRegistrationRepository registrationRepository,
                                          PaymentRecordRepository paymentRecordRepository,
                                          PaymentItemRepository paymentItemRepository,
                                          PaymentService paymentService,
                                          WindowConverter converter) {
        this.registrationRepository = registrationRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.paymentItemRepository = paymentItemRepository;
        this.paymentService = paymentService;
        this.converter = converter;
    }

    @Override
    @Transactional
    public Result<OfflineRegistrationResponse> create(OfflineRegistrationCreateRequest request,
                                                       Long operatorId,
                                                       String operatorName) {
        OfflineRegistrationEntity entity = new OfflineRegistrationEntity();
        entity.setRegistrationNo(generateRegistrationNo());
        entity.setPatientId(request.getPatientId());
        entity.setPatientName(request.getPatientName());
        entity.setPatientPhone(request.getPatientPhone());
        entity.setIdCard(request.getIdCard());
        entity.setDoctorId(request.getDoctorId());
        entity.setDoctorName(request.getDoctorName());
        entity.setDepartment(request.getDepartment());
        // 挂号类型默认门诊
        entity.setRegistrationType(StringUtils.hasText(request.getRegistrationType())
                ? request.getRegistrationType()
                : OfflineRegistrationType.OUTPATIENT.getCode());
        BigDecimal fee = Optional.ofNullable(request.getRegistrationFee()).orElse(BigDecimal.ZERO);
        entity.setRegistrationFee(fee);
        entity.setStatus(OfflineRegistrationStatus.ACTIVE.getCode());
        entity.setOperatorId(operatorId);
        entity.setOperatorName(operatorName);
        entity.setRemark(request.getRemark());

        OfflineRegistrationEntity saved;
        try {
            saved = registrationRepository.saveAndFlush(entity);
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }

        // 同步生成挂号费待支付缴费记录（同一事务，失败则整体回滚）
        createRegistrationPayment(saved, operatorId, operatorName);

        return Result.success(converter.toRegistrationResponse(saved));
    }

    /**
     * 为挂号创建一笔待支付缴费记录及一条挂号费明细。
     */
    private void createRegistrationPayment(OfflineRegistrationEntity registration,
                                           Long operatorId,
                                           String operatorName) {
        PaymentRecordEntity payment = new PaymentRecordEntity();
        payment.setPaymentNo(generatePaymentNo());
        payment.setPatientId(registration.getPatientId());
        payment.setPatientName(registration.getPatientName());
        payment.setSourceId(registration.getId());
        payment.setSourceType(PaymentSourceType.REGISTRATION.getCode());
        payment.setSourceNo(registration.getRegistrationNo());
        BigDecimal fee = Optional.ofNullable(registration.getRegistrationFee()).orElse(BigDecimal.ZERO);
        payment.setTotalAmount(fee);
        payment.setPaidAmount(BigDecimal.ZERO);
        payment.setRefundAmount(BigDecimal.ZERO);
        payment.setStatus(PaymentStatus.PENDING.getCode());
        payment.setOperatorId(operatorId);
        payment.setOperatorName(operatorName);
        PaymentRecordEntity savedPayment = paymentRecordRepository.save(payment);

        PaymentItemEntity item = new PaymentItemEntity();
        item.setPaymentId(savedPayment.getId());
        item.setItemType(PaymentItemType.REGISTRATION_FEE.getCode());
        item.setItemName("挂号费");
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(fee);
        item.setAmount(fee);
        paymentItemRepository.save(item);
    }

    @Override
    @Transactional
    public Result<OfflineRegistrationResponse> cancel(Long id, OfflineRegistrationCancelRequest request) {
        Optional<OfflineRegistrationEntity> opt = registrationRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(WindowErrorCode.REGISTRATION_NOT_FOUND);
        }
        OfflineRegistrationEntity entity = opt.get();
        if (OfflineRegistrationStatus.CANCELLED.getCode().equals(entity.getStatus())) {
            return Result.fail(WindowErrorCode.REGISTRATION_ALREADY_CANCELLED);
        }
        if (!OfflineRegistrationStatus.ACTIVE.getCode().equals(entity.getStatus())) {
            return Result.fail(WindowErrorCode.REGISTRATION_INVALID_STATE);
        }

        // 退号前处理关联的缴费记录：已支付触发退费，待支付直接取消，已退款则拒绝退号
        List<PaymentRecordEntity> payments = paymentRecordRepository.findBySourceTypeAndSourceId(
                PaymentSourceType.REGISTRATION.getCode(), id);
        for (PaymentRecordEntity payment : payments) {
            String payStatus = payment.getStatus();
            if (PaymentStatus.PAID.getCode().equals(payStatus)) {
                // 已支付：调用退费流程触发退款
                RefundRequest refundRequest = new RefundRequest();
                String cancelReason = request.getCancelReason();
                refundRequest.setRefundReason(StringUtils.hasText(cancelReason)
                        ? "退号同步退费：" + cancelReason
                        : "退号同步退费");
                Result<PaymentRecordResponse> refundResult = paymentService.refund(payment.getId(), refundRequest);
                if (!GlobalErrorCode.SUCCESS.getCode().equals(refundResult.getCode())) {
                    return Result.fail(refundResult.getCode(), refundResult.getMessage());
                }
            } else if (PaymentStatus.PENDING.getCode().equals(payStatus)) {
                // 待支付：直接取消缴费记录
                payment.setStatus(PaymentStatus.CANCELLED.getCode());
                paymentRecordRepository.save(payment);
            } else if (PaymentStatus.REFUNDED.getCode().equals(payStatus)) {
                // 已退款：拒绝退号，避免重复处理
                return Result.fail(WindowErrorCode.REGISTRATION_PAYMENT_ALREADY_REFUNDED);
            }
            // 其他状态（如 RECONCILED、CANCELLED）跳过
        }

        entity.setStatus(OfflineRegistrationStatus.CANCELLED.getCode());
        entity.setCancelReason(request.getCancelReason());
        entity.setCancelTime(LocalDateTime.now());
        try {
            OfflineRegistrationEntity saved = registrationRepository.saveAndFlush(entity);
            return Result.success(converter.toRegistrationResponse(saved));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Result<OfflineRegistrationResponse> getById(Long id) {
        return registrationRepository.findById(id)
                .map(entity -> Result.success(converter.toRegistrationResponse(entity)))
                .orElseGet(() -> Result.fail(WindowErrorCode.REGISTRATION_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Page<OfflineRegistrationResponse>> query(OfflineRegistrationQueryRequest request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 20 : request.getSize();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<OfflineRegistrationEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getPatientId() != null) {
                predicates.add(cb.equal(root.get("patientId"), request.getPatientId()));
            }
            if (StringUtils.hasText(request.getPatientName())) {
                predicates.add(cb.like(root.get("patientName"), "%" + request.getPatientName() + "%"));
            }
            if (StringUtils.hasText(request.getStatus())) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<OfflineRegistrationResponse> result = registrationRepository.findAll(spec, pageRequest)
                .map(converter::toRegistrationResponse);
        return Result.success(result);
    }

    private String generateRegistrationNo() {
        return "OFR" + System.currentTimeMillis()
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }

    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis()
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
