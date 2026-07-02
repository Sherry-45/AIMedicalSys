package com.aimedical.modules.doctor.event;

import com.aimedical.modules.commonmodule.event.RegistrationEvent;
import com.aimedical.modules.doctor.dto.response.ConsultationQueueResponse;
import com.aimedical.modules.doctor.service.ConsultationQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听挂号确认事件，自动创建接诊队列项，实现挂号与接诊队列的联动。
 *
 * <p>与 consultation 模块的 {@code RegistrationEventListener} 独立监听同一事件，
 * 各自负责不同的副作用：本类负责入队，consultation 模块负责回填分诊记录。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Component
public class RegistrationEventListener {

    private static final Logger log = LoggerFactory.getLogger(RegistrationEventListener.class);

    private final ConsultationQueueService queueService;

    public RegistrationEventListener(ConsultationQueueService queueService) {
        this.queueService = queueService;
    }

    @EventListener
    public void onRegistrationConfirmed(RegistrationEvent event) {
        if (event.getDoctorId() == null) {
            log.debug("Skipping queue creation: no doctor assigned for registration {}", event.getRegistrationId());
            return;
        }
        try {
            Long patientId = Long.parseLong(event.getPatientId());
            String queueNo = event.getQueueNumber() != null
                    ? String.format("%03d", event.getQueueNumber())
                    : String.valueOf(System.currentTimeMillis() % 1000);
            ConsultationQueueResponse resp = queueService.create(
                    event.getRegistrationId(),
                    patientId,
                    event.getPatientName() != null ? event.getPatientName() : "患者" + patientId,
                    event.getDoctorId(),
                    event.getDepartmentName() != null ? event.getDepartmentName() : "未分诊",
                    queueNo
            );
            if (resp != null) {
                log.info("Created queue entry for registration {}, patient {}", event.getRegistrationId(), patientId);
            } else {
                log.debug("Queue entry already exists for registration {}, skipped", event.getRegistrationId());
            }
        } catch (NumberFormatException e) {
            log.warn("Cannot parse patientId '{}' for registration {}", event.getPatientId(), event.getRegistrationId());
        } catch (Exception e) {
            log.error("Failed to create queue entry for registration {}", event.getRegistrationId(), e);
        }
    }
}
