package com.aimedical.modules.doctor.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.doctor.dto.response.ConsultationQueueResponse;

import java.util.List;

/**
 * 接诊/叫号队列服务。
 *
 * <p>状态流转：WAITING -> CALLED -> IN_CONSULTATION -> FINISHED；
 * WAITING/CALLED -> SKIPPED（过号）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface ConsultationQueueService {

    /**
     * 由挂号事件驱动创建队列项（幂等：同一 registrationId 重复入队将被跳过）。
     *
     * @param registrationId 关联挂号记录ID（可为 null，兼容手动创建场景）
     * @param patientId      患者档案ID
     * @param patientName    患者姓名（冗余展示）
     * @param doctorId       接诊医生用户ID
     * @param department     科室
     * @param queueNo        排队号
     * @return 新建的队列响应；若 registrationId 已存在则返回 null
     */
    ConsultationQueueResponse create(Long registrationId, Long patientId, String patientName,
                                     Long doctorId, String department, String queueNo);

    Result<List<ConsultationQueueResponse>> listMyQueue(Long doctorUserId);

    Result<List<ConsultationQueueResponse>> listWaiting(Long doctorUserId);

    Result<ConsultationQueueResponse> callNext(Long doctorUserId);

    Result<ConsultationQueueResponse> startConsultation(Long id, Long doctorUserId);

    Result<ConsultationQueueResponse> finishConsultation(Long id, Long doctorUserId);

    Result<ConsultationQueueResponse> skip(Long id, Long doctorUserId);
}
