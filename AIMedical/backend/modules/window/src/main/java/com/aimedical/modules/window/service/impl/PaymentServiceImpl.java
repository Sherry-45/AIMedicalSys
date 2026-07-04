package com.aimedical.modules.window.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.event.HealthRecordArchiveEvent;
import com.aimedical.modules.window.WindowErrorCode;
import com.aimedical.modules.window.converter.WindowConverter;
import com.aimedical.modules.window.dto.PayRequest;
import com.aimedical.modules.window.dto.PaymentCreateRequest;
import com.aimedical.modules.window.dto.PaymentItemRequest;
import com.aimedical.modules.window.dto.PaymentQueryRequest;
import com.aimedical.modules.window.dto.PaymentRecordResponse;
import com.aimedical.modules.window.dto.RefundRequest;
import com.aimedical.modules.window.entity.PaymentItemEntity;
import com.aimedical.modules.window.entity.PaymentRecordEntity;
import com.aimedical.modules.window.entity.PaymentStatus;
import com.aimedical.modules.window.repository.PaymentItemRepository;
import com.aimedical.modules.window.repository.PaymentRecordRepository;
import com.aimedical.modules.window.service.PaymentService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 缴费服务实现。
 *
 * <p>状态机：PENDING -> PAID -> REFUNDED。
 * 仅 PENDING 可支付；仅 PAID 可退款。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WindowConverter converter;

    public PaymentServiceImpl(PaymentRecordRepository paymentRecordRepository,
                              PaymentItemRepository paymentItemRepository,
                              ApplicationEventPublisher eventPublisher,
                              WindowConverter converter) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.paymentItemRepository = paymentItemRepository;
        this.eventPublisher = eventPublisher;
        this.converter = converter;
    }

    @Override
    @Transactional
    public Result<PaymentRecordResponse> create(PaymentCreateRequest request,
                                                 Long operatorId,
                                                 String operatorName) {
        PaymentRecordEntity payment = new PaymentRecordEntity();
        payment.setPaymentNo(generatePaymentNo());
        payment.setPatientId(request.getPatientId());
        payment.setPatientName(request.getPatientName());
        payment.setSourceId(request.getSourceId());
        payment.setSourceType(request.getSourceType());
        payment.setSourceNo(request.getSourceNo());
        payment.setTotalAmount(request.getTotalAmount());
        payment.setPaidAmount(BigDecimal.ZERO);
        payment.setRefundAmount(BigDecimal.ZERO);
        payment.setStatus(PaymentStatus.PENDING.getCode());
        payment.setOperatorId(operatorId);
        payment.setOperatorName(operatorName);
        payment.setRemark(request.getRemark());
        PaymentRecordEntity savedPayment = paymentRecordRepository.save(payment);

        // 生成明细并计算金额
        List<PaymentItemEntity> items = new ArrayList<>();
        for (PaymentItemRequest itemReq : request.getItems()) {
            PaymentItemEntity item = new PaymentItemEntity();
            item.setPaymentId(savedPayment.getId());
            item.setItemType(itemReq.getItemType());
            item.setItemName(itemReq.getItemName());
            BigDecimal qty = Optional.ofNullable(itemReq.getQuantity()).orElse(BigDecimal.ONE);
            item.setQuantity(qty);
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setAmount(itemReq.getUnitPrice().multiply(qty));
            item.setRemark(itemReq.getRemark());
            items.add(item);
        }
        List<PaymentItemEntity> savedItems = paymentItemRepository.saveAll(items);

        return Result.success(converter.toPaymentResponse(savedPayment, savedItems));
    }

    @Override
    @Transactional
    public Result<PaymentRecordResponse> pay(Long id, PayRequest request) {
        Optional<PaymentRecordEntity> opt = paymentRecordRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(WindowErrorCode.PAYMENT_NOT_FOUND);
        }
        PaymentRecordEntity entity = opt.get();
        if (PaymentStatus.PAID.getCode().equals(entity.getStatus())) {
            return Result.fail(WindowErrorCode.PAYMENT_ALREADY_PAID);
        }
        if (!PaymentStatus.PENDING.getCode().equals(entity.getStatus())) {
            return Result.fail(WindowErrorCode.PAYMENT_INVALID_STATE);
        }
        // 校验支付金额与应缴金额一致
        BigDecimal total = Optional.ofNullable(entity.getTotalAmount()).orElse(BigDecimal.ZERO);
        if (request.getPaidAmount() == null
                || request.getPaidAmount().compareTo(total) != 0) {
            return Result.fail(WindowErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        entity.setStatus(PaymentStatus.PAID.getCode());
        entity.setPaidAmount(request.getPaidAmount());
        entity.setPaymentMethod(request.getPaymentMethod());
        entity.setPayerName(request.getPayerName());
        entity.setPaidAt(LocalDateTime.now());
        try {
            PaymentRecordEntity saved = paymentRecordRepository.saveAndFlush(entity);
            List<PaymentItemEntity> items = paymentItemRepository.findByPaymentId(saved.getId());
            publishPaymentArchiveEvent(saved, HealthRecordArchiveEvent.Type.PAYMENT_PAID, "线下缴费");
            return Result.success(converter.toPaymentResponse(saved, items));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional
    public Result<PaymentRecordResponse> refund(Long id, RefundRequest request) {
        Optional<PaymentRecordEntity> opt = paymentRecordRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.fail(WindowErrorCode.PAYMENT_NOT_FOUND);
        }
        PaymentRecordEntity entity = opt.get();
        if (PaymentStatus.REFUNDED.getCode().equals(entity.getStatus())) {
            return Result.fail(WindowErrorCode.PAYMENT_ALREADY_REFUNDED);
        }
        if (!PaymentStatus.PAID.getCode().equals(entity.getStatus())) {
            return Result.fail(WindowErrorCode.REFUND_NOT_PAID);
        }
        // 全额退款：退费金额 = 已支付金额
        BigDecimal refundAmount = Optional.ofNullable(entity.getPaidAmount()).orElse(BigDecimal.ZERO);
        entity.setStatus(PaymentStatus.REFUNDED.getCode());
        entity.setRefundAmount(refundAmount);
        entity.setRefundReason(request.getRefundReason());
        entity.setRefundedAt(LocalDateTime.now());
        try {
            PaymentRecordEntity saved = paymentRecordRepository.saveAndFlush(entity);
            List<PaymentItemEntity> items = paymentItemRepository.findByPaymentId(saved.getId());
            publishPaymentArchiveEvent(saved, HealthRecordArchiveEvent.Type.PAYMENT_REFUNDED, "线下退费");
            return Result.success(converter.toPaymentResponse(saved, items));
        } catch (OptimisticLockingFailureException e) {
            return Result.fail(GlobalErrorCode.CONFLICT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Result<PaymentRecordResponse> getById(Long id) {
        return paymentRecordRepository.findById(id)
                .map(entity -> {
                    List<PaymentItemEntity> items = paymentItemRepository.findByPaymentId(entity.getId());
                    return Result.success(converter.toPaymentResponse(entity, items));
                })
                .orElseGet(() -> Result.fail(WindowErrorCode.PAYMENT_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Page<PaymentRecordResponse>> query(PaymentQueryRequest request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 20 : request.getSize();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<PaymentRecordEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getPatientId() != null) {
                predicates.add(cb.equal(root.get("patientId"), request.getPatientId()));
            }
            if (StringUtils.hasText(request.getStatus())) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (StringUtils.hasText(request.getSourceType())) {
                predicates.add(cb.equal(root.get("sourceType"), request.getSourceType()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<PaymentRecordEntity> entityPage = paymentRecordRepository.findAll(spec, pageRequest);
        // 批量加载明细，避免 N+1
        List<Long> paymentIds = entityPage.getContent().stream()
                .map(PaymentRecordEntity::getId)
                .toList();
        List<PaymentItemEntity> allItems = paymentIds.isEmpty()
                ? List.of()
                : paymentItemRepository.findByPaymentIdIn(paymentIds);
        Page<PaymentRecordResponse> result = entityPage.map(e ->
                converter.toPaymentResponse(e, filterItems(allItems, e.getId())));
        return Result.success(result);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<PaymentRecordResponse>> queryByPatient(Long patientId) {
        List<PaymentRecordEntity> entities = paymentRecordRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        if (entities.isEmpty()) {
            return Result.success(List.of());
        }
        List<Long> paymentIds = entities.stream().map(PaymentRecordEntity::getId).toList();
        List<PaymentItemEntity> allItems = paymentItemRepository.findByPaymentIdIn(paymentIds);
        Map<Long, List<PaymentItemEntity>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(PaymentItemEntity::getPaymentId));
        List<PaymentRecordResponse> responses = entities.stream()
                .map(e -> converter.toPaymentResponse(e, itemMap.getOrDefault(e.getId(), List.of())))
                .toList();
        return Result.success(responses);
    }

    private List<PaymentItemEntity> filterItems(List<PaymentItemEntity> allItems, Long paymentId) {
        return allItems.stream()
                .filter(i -> paymentId.equals(i.getPaymentId()))
                .toList();
    }

    /**
     * 发布健康档案归档事件，通知 patient 模块归档缴费/退费记录。
     *
     * <p>事件在事务内发布，监听端可通过 {@code @TransactionalEventListener(AFTER_COMMIT)}
     * 在事务提交后处理，保证只在业务成功落库后才归档。
     *
     * @param payment       缴费记录（已落库，含 id 与 paymentNo）
     * @param type          事件类型（PAYMENT_PAID / PAYMENT_REFUNDED）
     * @param summaryPrefix 摘要前缀（如 "线下缴费" / "线下退费"）
     */
    private void publishPaymentArchiveEvent(PaymentRecordEntity payment,
                                             HealthRecordArchiveEvent.Type type,
                                             String summaryPrefix) {
        HealthRecordArchiveEvent event = new HealthRecordArchiveEvent();
        event.setPatientId(payment.getPatientId());
        event.setPatientName(payment.getPatientName());
        event.setType(type);
        event.setRecordId(payment.getId());
        event.setRecordNo(payment.getPaymentNo());
        event.setOrganizationName("线下窗口");
        BigDecimal amountYuan = HealthRecordArchiveEvent.Type.PAYMENT_PAID.equals(type)
                ? payment.getPaidAmount()
                : payment.getRefundAmount();
        event.setAmount(toFen(amountYuan));
        event.setSummary(summaryPrefix + (payment.getPaymentNo() != null ? "：" + payment.getPaymentNo() : ""));
        event.setOccurredAt(LocalDateTime.now());
        eventPublisher.publishEvent(event);
    }

    /**
     * 将元（BigDecimal，精度 2）转换为分（Long）。
     */
    private Long toFen(BigDecimal yuan) {
        if (yuan == null) {
            return null;
        }
        return yuan.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis()
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
