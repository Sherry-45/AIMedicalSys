package com.aimedical.modules.examination.service.impl;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.PageResponse;
import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.AiService;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderRequest;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderResponse;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisRequest;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisResponse;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportRequest;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportResponse;
import com.aimedical.modules.examination.converter.ExaminationConverter;
import com.aimedical.modules.examination.dto.ExaminationCompleteRequest;
import com.aimedical.modules.examination.dto.ExaminationCreateRequest;
import com.aimedical.modules.examination.dto.ExaminationDTO;
import com.aimedical.modules.examination.dto.ExaminationItemRequest;
import com.aimedical.modules.examination.dto.ExaminationQueryRequest;
import com.aimedical.modules.examination.dto.ExecutionOrderDTO;
import com.aimedical.modules.examination.entity.Examination;
import com.aimedical.modules.examination.entity.ExaminationItem;
import com.aimedical.modules.examination.entity.ExaminationStatus;
import com.aimedical.modules.examination.entity.ExaminationType;
import com.aimedical.modules.examination.exception.ExaminationErrorCode;
import com.aimedical.modules.examination.repository.ExaminationItemRepository;
import com.aimedical.modules.examination.repository.ExaminationRepository;
import com.aimedical.modules.examination.service.ExaminationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExaminationServiceImpl implements ExaminationService {

    private final ExaminationRepository examinationRepository;
    private final ExaminationItemRepository examinationItemRepository;
    private final AiService aiService;

    @Override
    @Transactional
    public ExaminationDTO createExamination(ExaminationCreateRequest request) {
        Examination examination = new Examination();
        examination.setPatientId(request.getPatientId());
        examination.setDoctorId(request.getDoctorId());
        examination.setExaminationType(request.getExaminationType());
        examination.setBodyPart(request.getBodyPart());
        examination.setClinicalDiagnosis(request.getClinicalDiagnosis());
        examination.setScheduledAt(request.getScheduledAt());
        examination.setEmergencyFlag(request.getEmergencyFlag() != null ? request.getEmergencyFlag() : false);
        examination.setImageUrl(request.getImageUrl());
        examination.setImageType(request.getImageType());
        examination.setStatus(ExaminationStatus.PENDING);

        Examination saved = saveWithOptimisticLock(examination);

        List<ExaminationItem> items = new ArrayList<>();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (ExaminationItemRequest itemReq : request.getItems()) {
                ExaminationItem item = new ExaminationItem();
                item.setExaminationId(saved.getId());
                item.setItemName(itemReq.getItemName());
                item.setFinding(itemReq.getFinding());
                item.setMeasurement(itemReq.getMeasurement());
                item.setAbnormalFlag(itemReq.getAbnormalFlag() != null ? itemReq.getAbnormalFlag() : false);
                items.add(examinationItemRepository.save(item));
            }
        }
        return ExaminationConverter.toDTO(saved, items);
    }

    @Override
    @Transactional
    public ExaminationDTO scheduleExamination(Long id, LocalDateTime scheduledAt) {
        Examination examination = requireExamination(id);
        validateStatus(examination.getStatus(), Arrays.asList(ExaminationStatus.PENDING), "schedule");
        if (scheduledAt == null || !scheduledAt.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ExaminationErrorCode.SCHEDULED_TIME_INVALID);
        }
        examination.setStatus(ExaminationStatus.SCHEDULED);
        examination.setScheduledAt(scheduledAt);
        Examination saved = saveWithOptimisticLock(examination);
        List<ExaminationItem> items = examinationItemRepository.findByExaminationId(saved.getId());
        return ExaminationConverter.toDTO(saved, items);
    }

    @Override
    @Transactional
    public ExaminationDTO startExamination(Long id) {
        Examination examination = requireExamination(id);
        validateStatus(examination.getStatus(),
                Arrays.asList(ExaminationStatus.PENDING, ExaminationStatus.SCHEDULED), "start");
        examination.setStatus(ExaminationStatus.IN_PROGRESS);
        Examination saved = saveWithOptimisticLock(examination);
        List<ExaminationItem> items = examinationItemRepository.findByExaminationId(saved.getId());
        return ExaminationConverter.toDTO(saved, items);
    }

    @Override
    @Transactional
    public ExaminationDTO completeExamination(Long id, ExaminationCompleteRequest request) {
        Examination examination = requireExamination(id);
        validateStatus(examination.getStatus(), Arrays.asList(ExaminationStatus.IN_PROGRESS), "complete");
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException(ExaminationErrorCode.EXAMINATION_ITEM_EMPTY);
        }
        examination.setStatus(ExaminationStatus.COMPLETED);
        examination.setImpression(request.getImpression());
        examination.setConclusion(request.getConclusion());
        examination.setReportedAt(LocalDateTime.now());

        // 批量软删除旧明细，等价于逐条 deleteAll 触发的 @SQLDelete，但仅一条 UPDATE
        examinationItemRepository.softDeleteByExaminationId(examination.getId());

        List<ExaminationItem> savedItems = new ArrayList<>();
        for (ExaminationItemRequest itemReq : request.getItems()) {
            ExaminationItem item = new ExaminationItem();
            item.setExaminationId(examination.getId());
            item.setItemName(itemReq.getItemName());
            item.setFinding(itemReq.getFinding());
            item.setMeasurement(itemReq.getMeasurement());
            item.setAbnormalFlag(itemReq.getAbnormalFlag() != null ? itemReq.getAbnormalFlag() : false);
            savedItems.add(examinationItemRepository.save(item));
        }

        Examination saved = saveWithOptimisticLock(examination);
        return ExaminationConverter.toDTO(saved, savedItems);
    }

    @Override
    @Transactional
    public ExaminationDTO cancelExamination(Long id) {
        Examination examination = requireExamination(id);
        validateStatus(examination.getStatus(),
                Arrays.asList(ExaminationStatus.PENDING, ExaminationStatus.SCHEDULED, ExaminationStatus.IN_PROGRESS),
                "cancel");
        examination.setStatus(ExaminationStatus.CANCELLED);
        Examination saved = saveWithOptimisticLock(examination);
        List<ExaminationItem> items = examinationItemRepository.findByExaminationId(saved.getId());
        return ExaminationConverter.toDTO(saved, items);
    }

    @Override
    @Transactional(readOnly = true)
    public ExaminationDTO getExamination(Long id) {
        Examination examination = requireExamination(id);
        List<ExaminationItem> items = examinationItemRepository.findByExaminationId(examination.getId());
        return ExaminationConverter.toDTO(examination, items);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExaminationDTO> getExaminations(ExaminationQueryRequest query) {
        Pageable pageable = PageRequest.of(query.getPage(), query.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Examination probe = new Examination();
        if (query.getPatientId() != null) {
            probe.setPatientId(query.getPatientId());
        }
        if (query.getDoctorId() != null) {
            probe.setDoctorId(query.getDoctorId());
        }
        if (query.getStatus() != null) {
            probe.setStatus(query.getStatus());
        }
        if (query.getExaminationType() != null) {
            probe.setExaminationType(query.getExaminationType());
        }
        Page<Examination> page = examinationRepository.findAll(Example.of(probe), pageable);

        List<Long> examinationIds = page.getContent().stream()
                .map(Examination::getId).collect(Collectors.toList());
        Map<Long, List<ExaminationItem>> itemMap = (examinationIds.isEmpty()
                ? Collections.<ExaminationItem>emptyList()
                : examinationItemRepository.findByExaminationIdIn(examinationIds)).stream()
                .collect(Collectors.groupingBy(ExaminationItem::getExaminationId));

        List<ExaminationDTO> content = page.getContent().stream()
                .map(e -> ExaminationConverter.toDTO(e,
                        itemMap.getOrDefault(e.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getTotalElements(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional
    public ExaminationDTO generateAiReport(Long id) {
        Examination examination = requireExamination(id);
        // 允许 IN_PROGRESS 和 COMPLETED 状态生成 AI 报告（对齐 §3.4.5：AI 辅助医生出具报告）
        validateStatus(examination.getStatus(),
                Arrays.asList(ExaminationStatus.IN_PROGRESS, ExaminationStatus.COMPLETED), "generateAiReport");
        List<ExaminationItem> items = examinationItemRepository.findByExaminationId(examination.getId());

        InspectionReportRequest request = new InspectionReportRequest();
        request.setExaminationId(examination.getId());
        request.setExaminationType(examination.getExaminationType().getCode());
        request.setRawDataRef(examination.getImageUrl());
        request.setPatientId(examination.getPatientId());
        request.setDoctorId(examination.getDoctorId());
        request.setBodyPart(examination.getBodyPart());
        request.setClinicalDiagnosis(examination.getClinicalDiagnosis());
        request.setImpression(examination.getImpression());
        request.setConclusion(examination.getConclusion());
        request.setItems(items.stream().map(ExaminationServiceImpl::toInspectionItemDto).collect(Collectors.toList()));

        AiResult<InspectionReportResponse> result;
        try {
            result = aiService.analysisReportForInspection(request).join();
        } catch (CompletionException e) {
            // §3.4.5 降级路径：AI 自身失败时回退到医生手动判读，不抛出
            log.warn("AI检查报告调用异常，降级到医生手动判读: examinationId={}", examination.getId(), e);
            return ExaminationConverter.toDTO(examination, items);
        }
        if (result == null || !result.isSuccess() || result.isDegraded()) {
            log.warn("AI检查报告返回失败/降级，回退到医生手动判读: examinationId={}, result={}", examination.getId(), result);
            return ExaminationConverter.toDTO(examination, items);
        }
        InspectionReportResponse data = result.getData();
        if (data != null) {
            // §3.4.5 内部组合 §3.4.7：影像类检查且 model_id 可推断时，内部调用 3.4.7 填充 image_recognition
            fillImageRecognition(data, examination, items);

            // 将 AI 报告字段组装到 ai_interpretation（实体仅单列存储）
            StringBuilder interpretation = new StringBuilder();
            if (data.getReportDraft() != null) {
                interpretation.append(data.getReportDraft());
            }
            if (data.getImpression() != null) {
                if (interpretation.length() > 0) {
                    interpretation.append("\n");
                }
                interpretation.append("印象：").append(data.getImpression());
            }
            if (data.getAuxiliaryInterpretation() != null) {
                if (interpretation.length() > 0) {
                    interpretation.append("\n");
                }
                interpretation.append("辅助判读：").append(data.getAuxiliaryInterpretation());
            }
            if (data.getFindings() != null && !data.getFindings().isEmpty()) {
                if (interpretation.length() > 0) {
                    interpretation.append("\n");
                }
                interpretation.append("发现：").append(String.join("；", data.getFindings()));
            }
            if (data.getComparisonSummary() != null) {
                if (interpretation.length() > 0) {
                    interpretation.append("\n");
                }
                interpretation.append("趋势对比：").append(data.getComparisonSummary());
            }
            if (interpretation.length() > 0) {
                examination.setAiInterpretation(interpretation.toString());
            }
            if (data.getConfidence() != null) {
                examination.setAiConfidence(data.getConfidence());
            }
        }
        Examination saved = saveWithOptimisticLock(examination);
        List<ExaminationItem> latestItems = examinationItemRepository.findByExaminationId(saved.getId());
        return ExaminationConverter.toDTO(saved, latestItems);
    }

    /**
     * §3.4.5 内部组合 §3.4.7：影像类检查时调用影像分析填充 image_recognition。
     * 任何原因导致的失败统一返回 EXAM_AI_INTERNAL_IMG_FAIL 并将 image_recognition 缺省，报告正常返回。
     */
    private void fillImageRecognition(InspectionReportResponse report, Examination examination,
                                      List<ExaminationItem> items) {
        String modelId = inferModelId(examination.getExaminationType(),
                items.isEmpty() ? null : items.get(0).getItemName());
        if (modelId == null || examination.getImageUrl() == null) {
            // 非影像类或无原始数据引用，跳过 3.4.7 调用
            return;
        }
        ImageAnalysisRequest imgRequest = new ImageAnalysisRequest();
        imgRequest.setImageRef(examination.getImageUrl());
        imgRequest.setModelId(modelId);
        imgRequest.setPatientId(examination.getPatientId());
        imgRequest.setExaminationId(examination.getId());
        imgRequest.setExaminationType(examination.getExaminationType().getCode());
        imgRequest.setBodyPart(examination.getBodyPart());
        imgRequest.setImageType(examination.getImageType());

        try {
            AiResult<ImageAnalysisResponse> imgResult = aiService.imageAnalysis(imgRequest).join();
            if (imgResult != null && imgResult.isSuccess() && imgResult.getData() != null) {
                ImageAnalysisResponse imgData = imgResult.getData();
                InspectionReportResponse.ImageRecognition recognition = new InspectionReportResponse.ImageRecognition();
                recognition.setModelId(imgData.getModelId());
                recognition.setConfidence(imgData.getConfidence());
                if (imgData.getRecognitionResult() != null && imgData.getRecognitionResult().getLabels() != null) {
                    recognition.setRecognitionSummary(String.join("；", imgData.getRecognitionResult().getLabels()));
                }
                if (imgData.getAuxiliaryAdvice() != null) {
                    if (recognition.getRecognitionSummary() == null) {
                        recognition.setRecognitionSummary(imgData.getAuxiliaryAdvice());
                    } else {
                        recognition.setRecognitionSummary(recognition.getRecognitionSummary()
                                + "；" + imgData.getAuxiliaryAdvice());
                    }
                }
                report.setImageRecognition(recognition);
            } else {
                log.warn("AI检查报告内部影像分析失败(EXAM_AI_INTERNAL_IMG_FAIL): examinationId={}, modelId={}, result={}",
                        examination.getId(), modelId, imgResult);
            }
        } catch (Exception e) {
            log.warn("AI检查报告内部影像分析异常(EXAM_AI_INTERNAL_IMG_FAIL): examinationId={}, modelId={}",
                    examination.getId(), modelId, e);
        }
    }

    /**
     * 根据 exam_type + item_name 推断影像分析模型标识。
     * 映射规则由 §3.3.4 综合管理维护，此处为简化实现。
     */
    private String inferModelId(ExaminationType examType, String itemName) {
        if (examType == null) {
            return null;
        }
        switch (examType) {
            case CT:
                return "HEAD_CT_ARTIFACT";
            case MRI:
                return "TUMOR_SEGMENTATION";
            case X_RAY:
                return "XRAY_ANALYSIS";
            default:
                return null;
        }
    }

    @Override
    @Transactional
    public ExaminationDTO analyzeImage(Long id) {
        Examination examination = requireExamination(id);
        // 允许 IN_PROGRESS 和 COMPLETED 状态进行影像分析
        validateStatus(examination.getStatus(),
                Arrays.asList(ExaminationStatus.IN_PROGRESS, ExaminationStatus.COMPLETED), "analyzeImage");

        String modelId = inferModelId(examination.getExaminationType(),
                examinationItemRepository.findByExaminationId(examination.getId()).stream()
                        .findFirst().map(ExaminationItem::getItemName).orElse(null));

        ImageAnalysisRequest request = new ImageAnalysisRequest();
        request.setImageRef(examination.getImageUrl());
        request.setModelId(modelId);
        request.setPatientId(examination.getPatientId());
        request.setExaminationId(examination.getId());
        request.setExaminationType(examination.getExaminationType().getCode());
        request.setBodyPart(examination.getBodyPart());
        request.setClinicalDiagnosis(examination.getClinicalDiagnosis());
        request.setImageType(examination.getImageType());

        AiResult<ImageAnalysisResponse> result;
        try {
            result = aiService.imageAnalysis(request).join();
        } catch (CompletionException e) {
            // §3.4.7 降级路径：失败时回退到医生手动判读，不抛出
            log.warn("AI影像分析调用异常，降级到医生手动判读: examinationId={}", examination.getId(), e);
            List<ExaminationItem> items = examinationItemRepository.findByExaminationId(examination.getId());
            return ExaminationConverter.toDTO(examination, items);
        }
        if (result == null || !result.isSuccess() || result.isDegraded()) {
            log.warn("AI影像分析返回失败/降级，回退到医生手动判读: examinationId={}, result={}", examination.getId(), result);
            List<ExaminationItem> items = examinationItemRepository.findByExaminationId(examination.getId());
            return ExaminationConverter.toDTO(examination, items);
        }
        ImageAnalysisResponse data = result.getData();
        if (data != null) {
            // 影像分析结果独立持久化，不覆盖医生原始印象/结论
            StringBuilder analysisResult = new StringBuilder();
            if (data.getModelId() != null) {
                analysisResult.append("模型：").append(data.getModelId());
            }
            if (data.getRecognitionResult() != null) {
                if (data.getRecognitionResult().getRegions() != null && !data.getRecognitionResult().getRegions().isEmpty()) {
                    if (analysisResult.length() > 0) {
                        analysisResult.append("\n");
                    }
                    analysisResult.append("识别区域：").append(String.join("；", data.getRecognitionResult().getRegions()));
                }
                if (data.getRecognitionResult().getLabels() != null && !data.getRecognitionResult().getLabels().isEmpty()) {
                    if (analysisResult.length() > 0) {
                        analysisResult.append("\n");
                    }
                    analysisResult.append("标签：").append(String.join("；", data.getRecognitionResult().getLabels()));
                }
            }
            if (data.getSegmentationMaskRef() != null) {
                if (analysisResult.length() > 0) {
                    analysisResult.append("\n");
                }
                analysisResult.append("分割掩码：").append(data.getSegmentationMaskRef());
            }
            if (data.getAuxiliaryAdvice() != null) {
                if (analysisResult.length() > 0) {
                    analysisResult.append("\n");
                }
                analysisResult.append("辅助建议：").append(data.getAuxiliaryAdvice());
            }
            if (analysisResult.length() > 0) {
                examination.setImageAnalysisResult(analysisResult.toString());
            }
            if (data.getConfidence() != null) {
                examination.setImageConfidence(data.getConfidence());
            }
        }
        Examination saved = saveWithOptimisticLock(examination);
        List<ExaminationItem> items = examinationItemRepository.findByExaminationId(saved.getId());
        return ExaminationConverter.toDTO(saved, items);
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionOrderDTO recommendExecutionOrder(Long patientId) {
        // 查询待执行检查任务（PENDING + SCHEDULED）
        List<Examination> pending = examinationRepository
                .findByPatientIdAndStatus(patientId, ExaminationStatus.PENDING);
        List<Examination> scheduled = examinationRepository
                .findByPatientIdAndStatus(patientId, ExaminationStatus.SCHEDULED);
        List<Examination> tasks = new ArrayList<>();
        tasks.addAll(pending);
        tasks.addAll(scheduled);

        ExecutionOrderDTO dto = new ExecutionOrderDTO();
        if (tasks.isEmpty()) {
            dto.setExecutionOrder(new ArrayList<>());
            dto.setDisclaimerRequired(true);
            return dto;
        }

        // 构建 §3.4.11 task_items 结构
        List<ExecutionOrderRequest.TaskItem> taskItems = tasks.stream().map(e -> {
            ExecutionOrderRequest.TaskItem item = new ExecutionOrderRequest.TaskItem();
            item.setTaskId(e.getId());
            item.setTaskType("IMAGING");
            item.setItemName(e.getExaminationType().getDesc());
            item.setUrgencyHint(e.getEmergencyFlag() != null && e.getEmergencyFlag() ? "HIGH" : "MEDIUM");
            item.setPatientId(e.getPatientId());
            return item;
        }).collect(Collectors.toList());

        ExecutionOrderRequest request = new ExecutionOrderRequest();
        request.setTaskItems(taskItems);
        request.setTaskRole("IMAGING_DOCTOR");

        AiResult<ExecutionOrderResponse> result;
        try {
            result = aiService.recommendExecutionOrder(request).join();
        } catch (CompletionException e) {
            // §3.4.11 降级路径：AI 异常时回退到 FIFO + 紧急度排序
            log.warn("AI执行顺序推荐调用异常，降级到 FIFO: patientId={}", patientId, e);
            return buildDegradedExecutionOrder(tasks);
        }
        if (result == null || !result.isSuccess() || result.isDegraded() || result.getData() == null) {
            log.warn("AI执行顺序推荐返回失败/降级，回退到 FIFO: patientId={}, result={}", patientId, result);
            return buildDegradedExecutionOrder(tasks);
        }
        ExecutionOrderResponse data = result.getData();
        if (data.getExecutionOrder() != null) {
            dto.setExecutionOrder(data.getExecutionOrder().stream()
                    .map(o -> {
                        ExecutionOrderDTO.OrderItem item = new ExecutionOrderDTO.OrderItem();
                        item.setTaskId(o.getTaskId());
                        item.setPriority(o.getPriority());
                        item.setRecommendedTime(o.getRecommendedTime());
                        item.setReason(o.getReason());
                        return item;
                    }).collect(Collectors.toList()));
        } else {
            dto.setExecutionOrder(new ArrayList<>());
        }
        dto.setSummary(data.getSummary());
        dto.setDisclaimerRequired(data.getDisclaimerRequired() != null ? data.getDisclaimerRequired() : true);
        dto.setDegraded(false);
        return dto;
    }

    /**
     * §3.4.11 降级路径：AI 不可用时按 FIFO + 紧急度手动排序。
     */
    private ExecutionOrderDTO buildDegradedExecutionOrder(List<Examination> tasks) {
        ExecutionOrderDTO dto = new ExecutionOrderDTO();
        // 紧急优先，同级别按创建时间 FIFO
        List<ExecutionOrderDTO.OrderItem> order = tasks.stream()
                .sorted((a, b) -> {
                    boolean aEmergency = a.getEmergencyFlag() != null && a.getEmergencyFlag();
                    boolean bEmergency = b.getEmergencyFlag() != null && b.getEmergencyFlag();
                    if (aEmergency != bEmergency) {
                        return aEmergency ? -1 : 1;
                    }
                    return a.getCreatedAt() != null && b.getCreatedAt() != null
                            ? a.getCreatedAt().compareTo(b.getCreatedAt()) : 0;
                })
                .map(e -> {
                    ExecutionOrderDTO.OrderItem item = new ExecutionOrderDTO.OrderItem();
                    item.setTaskId(e.getId());
                    item.setPriority(e.getEmergencyFlag() != null && e.getEmergencyFlag() ? "P1" : "P2");
                    item.setReason("AI不可用，按FIFO+紧急度降级排序");
                    return item;
                })
                .collect(Collectors.toList());
        dto.setExecutionOrder(order);
        dto.setSummary("AI不可用，按FIFO+紧急度降级排序");
        dto.setDisclaimerRequired(true);
        dto.setDegraded(true);
        return dto;
    }

    private Examination requireExamination(Long id) {
        return examinationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExaminationErrorCode.EXAMINATION_NOT_FOUND));
    }

    private static InspectionReportRequest.Item toInspectionItemDto(ExaminationItem item) {
        InspectionReportRequest.Item dto = new InspectionReportRequest.Item();
        dto.setItemName(item.getItemName());
        dto.setValue(item.getMeasurement());
        dto.setStatus(item.getAbnormalFlag() != null && item.getAbnormalFlag() ? "ABNORMAL" : "NORMAL");
        return dto;
    }

    private void validateStatus(ExaminationStatus current, List<ExaminationStatus> allowed, String action) {
        if (!allowed.contains(current)) {
            log.warn("检查状态校验失败: action={}, current={}, allowed={}", action, current, allowed);
            throw new BusinessException(ExaminationErrorCode.EXAMINATION_STATUS_INVALID);
        }
    }

    private Examination saveWithOptimisticLock(Examination examination) {
        try {
            return examinationRepository.saveAndFlush(examination);
        } catch (OptimisticLockingFailureException e) {
            throw new BusinessException(GlobalErrorCode.SYSTEM_ERROR, e);
        }
    }
}
