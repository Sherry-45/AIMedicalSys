package com.aimedical.modules.patient.event;

import com.aimedical.modules.commonmodule.event.HealthRecordArchiveEvent;
import com.aimedical.modules.patient.entity.HealthRecordCategory;
import com.aimedical.modules.patient.entity.HealthRecordEntity;
import com.aimedical.modules.patient.entity.HealthRecordType;
import com.aimedical.modules.patient.repository.HealthRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 监听健康档案归档事件，将缴费/退款/发药/退药业务记录归档到患者健康档案。
 *
 * <p>window 模块和 pharmacy 模块在支付/退款/发药/退药时发布 {@link HealthRecordArchiveEvent}，
 * 本监听器将其转化为 {@link HealthRecordEntity} 持久化，打通数据接入管道。
 *
 * <p>使用 try-catch 包裹，监听失败仅记录日志，不影响主业务事务。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Component
public class HealthRecordArchiveEventListener {

    private static final Logger log = LoggerFactory.getLogger(HealthRecordArchiveEventListener.class);

    private final HealthRecordRepository healthRecordRepository;

    public HealthRecordArchiveEventListener(HealthRecordRepository healthRecordRepository) {
        this.healthRecordRepository = healthRecordRepository;
    }

    @EventListener
    public void onHealthRecordArchive(HealthRecordArchiveEvent event) {
        if (event == null || event.getType() == null) {
            log.warn("Skipping health record archive: event or type is null");
            return;
        }
        if (event.getPatientId() == null) {
            log.warn("Skipping health record archive: patientId is null, type={}, recordNo={}",
                    event.getType(), event.getRecordNo());
            return;
        }
        try {
            HealthRecordEntity entity = buildEntity(event);
            healthRecordRepository.save(entity);
            log.info("Archived health record for patient {}, type={}, recordNo={}",
                    event.getPatientId(), event.getType(), event.getRecordNo());
        } catch (Exception e) {
            log.error("Failed to archive health record for patient {}, type={}, recordNo={}",
                    event.getPatientId(), event.getType(), event.getRecordNo(), e);
        }
    }

    private HealthRecordEntity buildEntity(HealthRecordArchiveEvent event) {
        HealthRecordEntity entity = new HealthRecordEntity();
        entity.setPatientId(event.getPatientId());
        entity.setOrganization(event.getOrganizationName());
        entity.setRecordCategory(HealthRecordCategory.OUTPATIENT.getCode());
        entity.setSourceId(event.getRecordId());
        LocalDate recordDate = event.getOccurredAt() != null
                ? event.getOccurredAt().toLocalDate()
                : LocalDate.now();
        entity.setRecordDate(recordDate);

        switch (event.getType()) {
            case PAYMENT_PAID -> {
                entity.setRecordType(HealthRecordType.PAYMENT.getCode());
                entity.setTitle("线下缴费-" + safe(event.getRecordNo()));
                entity.setContent(buildPaymentContent(event));
            }
            case PAYMENT_REFUNDED -> {
                entity.setRecordType(HealthRecordType.PAYMENT.getCode());
                entity.setTitle("线下退费-" + safe(event.getRecordNo()));
                entity.setContent(buildPaymentContent(event));
            }
            case DISPENSED -> {
                entity.setRecordType(HealthRecordType.DISPENSING.getCode());
                entity.setTitle("药房发药-" + safe(event.getRecordNo()));
                entity.setContent(event.getSummary());
            }
            case REFUNDED -> {
                entity.setRecordType(HealthRecordType.DISPENSING.getCode());
                entity.setTitle("药房退药-" + safe(event.getRecordNo()));
                entity.setContent(event.getSummary());
            }
            default -> log.warn("Unknown health record archive event type: {}", event.getType());
        }
        return entity;
    }

    private String buildPaymentContent(HealthRecordArchiveEvent event) {
        StringBuilder sb = new StringBuilder();
        if (event.getSummary() != null) {
            sb.append(event.getSummary());
        }
        if (event.getAmount() != null) {
            if (sb.length() > 0) {
                sb.append("，");
            }
            sb.append("金额：").append(String.format("%.2f", event.getAmount() / 100.0)).append("元");
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private String safe(String s) {
        return s != null ? s : "";
    }
}
