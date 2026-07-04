package com.aimedical.modules.examination.service.impl;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.AiService;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderResponse;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisResponse;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportResponse;
import com.aimedical.modules.examination.dto.ExaminationCompleteRequest;
import com.aimedical.modules.examination.dto.ExaminationCreateRequest;
import com.aimedical.modules.examination.dto.ExaminationDTO;
import com.aimedical.modules.examination.dto.ExaminationItemRequest;
import com.aimedical.modules.examination.dto.ExecutionOrderDTO;
import com.aimedical.modules.examination.entity.Examination;
import com.aimedical.modules.examination.entity.ExaminationItem;
import com.aimedical.modules.examination.entity.ExaminationStatus;
import com.aimedical.modules.examination.entity.ExaminationType;
import com.aimedical.modules.examination.repository.ExaminationItemRepository;
import com.aimedical.modules.examination.repository.ExaminationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExaminationServiceImplTest {

    @Mock
    private ExaminationRepository examinationRepository;

    @Mock
    private ExaminationItemRepository examinationItemRepository;

    @Mock
    private AiService aiService;

    @InjectMocks
    private ExaminationServiceImpl service;

    @Test
    void createExamination_shouldCreateWithPendingStatusAndItems() {
        ExaminationCreateRequest request = new ExaminationCreateRequest();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setExaminationType(ExaminationType.CT);
        request.setBodyPart("胸部");
        request.setEmergencyFlag(true);
        request.setImageUrl("http://example.com/img.dcm");
        request.setImageType("DICOM");

        ExaminationItemRequest itemReq = new ExaminationItemRequest();
        itemReq.setItemName("肺窗");
        itemReq.setAbnormalFlag(true);
        request.setItems(Collections.singletonList(itemReq));

        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> {
            Examination e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });
        when(examinationItemRepository.save(any(ExaminationItem.class))).thenAnswer(inv -> {
            ExaminationItem i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        ExaminationDTO result = service.createExamination(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(ExaminationStatus.PENDING);
        assertThat(result.getPatientId()).isEqualTo(1L);
        assertThat(result.getDoctorId()).isEqualTo(2L);
        assertThat(result.getExaminationType()).isEqualTo(ExaminationType.CT);
        assertThat(result.getEmergencyFlag()).isTrue();
        assertThat(result.getImageUrl()).isEqualTo("http://example.com/img.dcm");
        assertThat(result.getImageType()).isEqualTo("DICOM");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getItemName()).isEqualTo("肺窗");
        assertThat(result.getItems().get(0).getAbnormalFlag()).isTrue();
    }

    @Test
    void createExamination_shouldCreateWithoutItems_whenItemsNull() {
        ExaminationCreateRequest request = new ExaminationCreateRequest();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setExaminationType(ExaminationType.MRI);

        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> {
            Examination e = inv.getArgument(0);
            e.setId(101L);
            return e;
        });

        ExaminationDTO result = service.createExamination(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getStatus()).isEqualTo(ExaminationStatus.PENDING);
        assertThat(result.getEmergencyFlag()).isFalse();
        assertThat(result.getItems()).isEmpty();
        verify(examinationItemRepository, never()).save(any(ExaminationItem.class));
    }

    @Test
    void scheduleExamination_shouldTransitionFromPendingToScheduled() {
        Examination examination = buildExamination(1L, ExaminationStatus.PENDING);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(1);
        ExaminationDTO result = service.scheduleExamination(1L, scheduledAt);

        assertThat(result.getStatus()).isEqualTo(ExaminationStatus.SCHEDULED);
        assertThat(result.getScheduledAt()).isEqualTo(scheduledAt);
    }

    @Test
    void scheduleExamination_shouldThrow_whenStatusNotPending() {
        Examination examination = buildExamination(1L, ExaminationStatus.COMPLETED);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));

        assertThatThrownBy(() -> service.scheduleExamination(1L, LocalDateTime.now()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void startExamination_shouldTransitionFromPendingToInProgress() {
        Examination examination = buildExamination(1L, ExaminationStatus.PENDING);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.startExamination(1L);

        assertThat(result.getStatus()).isEqualTo(ExaminationStatus.IN_PROGRESS);
    }

    @Test
    void startExamination_shouldTransitionFromScheduledToInProgress() {
        Examination examination = buildExamination(1L, ExaminationStatus.SCHEDULED);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.startExamination(1L);

        assertThat(result.getStatus()).isEqualTo(ExaminationStatus.IN_PROGRESS);
    }

    @Test
    void startExamination_shouldThrow_whenStatusCompleted() {
        Examination examination = buildExamination(1L, ExaminationStatus.COMPLETED);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));

        assertThatThrownBy(() -> service.startExamination(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void completeExamination_shouldTransitionFromInProgressToCompleted() {
        Examination examination = buildExamination(1L, ExaminationStatus.IN_PROGRESS);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));

        ExaminationCompleteRequest request = new ExaminationCompleteRequest();
        request.setImpression("未见异常");
        request.setConclusion("正常");
        ExaminationItemRequest itemReq = new ExaminationItemRequest();
        itemReq.setItemName("病灶");
        itemReq.setFinding("结节");
        request.setItems(Collections.singletonList(itemReq));

        when(examinationItemRepository.save(any(ExaminationItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> inv.getArgument(0));

        ExaminationDTO result = service.completeExamination(1L, request);

        assertThat(result.getStatus()).isEqualTo(ExaminationStatus.COMPLETED);
        assertThat(result.getImpression()).isEqualTo("未见异常");
        assertThat(result.getConclusion()).isEqualTo("正常");
        assertThat(result.getReportedAt()).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getItemName()).isEqualTo("病灶");
    }

    @Test
    void completeExamination_shouldThrow_whenItemsEmpty() {
        Examination examination = buildExamination(1L, ExaminationStatus.IN_PROGRESS);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));

        ExaminationCompleteRequest request = new ExaminationCompleteRequest();
        request.setItems(Collections.emptyList());

        assertThatThrownBy(() -> service.completeExamination(1L, request))
                .isInstanceOf(BusinessException.class);
        verify(examinationRepository, never()).saveAndFlush(any(Examination.class));
    }

    @Test
    void completeExamination_shouldThrow_whenStatusNotInProgress() {
        Examination examination = buildExamination(1L, ExaminationStatus.PENDING);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));

        ExaminationCompleteRequest request = new ExaminationCompleteRequest();
        ExaminationItemRequest itemReq = new ExaminationItemRequest();
        itemReq.setItemName("test");
        request.setItems(Collections.singletonList(itemReq));

        assertThatThrownBy(() -> service.completeExamination(1L, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void cancelExamination_shouldTransitionFromPendingToCancelled() {
        Examination examination = buildExamination(1L, ExaminationStatus.PENDING);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.cancelExamination(1L);

        assertThat(result.getStatus()).isEqualTo(ExaminationStatus.CANCELLED);
    }

    @Test
    void cancelExamination_shouldTransitionFromScheduledToCancelled() {
        Examination examination = buildExamination(1L, ExaminationStatus.SCHEDULED);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.cancelExamination(1L);

        assertThat(result.getStatus()).isEqualTo(ExaminationStatus.CANCELLED);
    }

    @Test
    void cancelExamination_shouldTransitionFromInProgressToCancelled() {
        Examination examination = buildExamination(1L, ExaminationStatus.IN_PROGRESS);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.cancelExamination(1L);

        assertThat(result.getStatus()).isEqualTo(ExaminationStatus.CANCELLED);
    }

    @Test
    void cancelExamination_shouldThrow_whenStatusCompleted() {
        Examination examination = buildExamination(1L, ExaminationStatus.COMPLETED);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));

        assertThatThrownBy(() -> service.cancelExamination(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getExamination_shouldReturnDtoWithItems() {
        Examination examination = buildExamination(1L, ExaminationStatus.COMPLETED);
        examination.setImpression("im");
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        ExaminationItem item = new ExaminationItem();
        item.setId(5L);
        item.setExaminationId(1L);
        item.setItemName("item");
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.singletonList(item));

        ExaminationDTO result = service.getExamination(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getImpression()).isEqualTo("im");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void getExaminations_shouldReturnPagedResults() {
        Examination exam1 = buildExamination(1L, ExaminationStatus.PENDING);
        Examination exam2 = buildExamination(2L, ExaminationStatus.COMPLETED);
        Page<Examination> page = new PageImpl<>(Arrays.asList(exam1, exam2), PageRequest.of(0, 20), 2);
        when(examinationRepository.findAll(any(Example.class), any(PageRequest.class))).thenReturn(page);
        when(examinationItemRepository.findByExaminationIdIn(any())).thenReturn(Collections.emptyList());

        com.aimedical.modules.examination.dto.ExaminationQueryRequest query =
                new com.aimedical.modules.examination.dto.ExaminationQueryRequest();
        query.setPage(0);
        query.setSize(20);
        com.aimedical.common.result.PageResponse<ExaminationDTO> result = service.getExaminations(query);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2L);
    }

    // ==================== AI 检查报告 §3.4.5 ====================

    @Test
    void generateAiReport_shouldSucceedAndPersistAiInterpretation_whenAiReturnsSuccess() {
        Examination examination = buildExamination(1L, ExaminationStatus.COMPLETED);
        examination.setImpression("原始印象");
        examination.setConclusion("原始结论");
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        InspectionReportResponse response = new InspectionReportResponse();
        response.setReportDraft("AI报告草稿");
        response.setImpression("AI impression");
        response.setAuxiliaryInterpretation("AI辅助判读");
        response.setFindings(List.of("发现1", "发现2"));
        response.setComparisonSummary("趋势对比摘要");
        response.setConfidence(0.95);
        when(aiService.analysisReportForInspection(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));
        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.generateAiReport(1L);

        assertThat(result).isNotNull();
        // AI 解读独立持久化，原始印象/结论不被覆盖
        assertThat(result.getImpression()).isEqualTo("原始印象");
        assertThat(result.getConclusion()).isEqualTo("原始结论");
        assertThat(result.getAiInterpretation()).contains("AI报告草稿");
        assertThat(result.getAiInterpretation()).contains("AI impression");
        assertThat(result.getAiInterpretation()).contains("AI辅助判读");
        assertThat(result.getAiInterpretation()).contains("发现1");
        assertThat(result.getAiInterpretation()).contains("趋势对比摘要");
        assertThat(result.getAiConfidence()).isEqualTo(0.95);
        verify(aiService).analysisReportForInspection(any());
    }

    @Test
    void generateAiReport_shouldAlsoAllowInProgressStatus() {
        Examination examination = buildExamination(1L, ExaminationStatus.IN_PROGRESS);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        InspectionReportResponse response = new InspectionReportResponse();
        response.setReportDraft("AI报告");
        when(aiService.analysisReportForInspection(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));
        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.generateAiReport(1L);

        assertThat(result).isNotNull();
        assertThat(result.getAiInterpretation()).contains("AI报告");
    }

    @Test
    void generateAiReport_shouldDegrade_whenAiReturnsFailure() {
        Examination examination = buildExamination(1L, ExaminationStatus.COMPLETED);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        when(aiService.analysisReportForInspection(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.failure("AI_ERROR")));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.generateAiReport(1L);

        // §3.4.5 降级路径：返回 DTO，不抛异常，不持久化 AI 字段
        assertThat(result).isNotNull();
        assertThat(result.getAiInterpretation()).isNull();
        verify(examinationRepository, never()).saveAndFlush(any(Examination.class));
    }

    @Test
    void generateAiReport_shouldDegrade_whenAiReturnsDegraded() {
        Examination examination = buildExamination(1L, ExaminationStatus.COMPLETED);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        when(aiService.analysisReportForInspection(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.degraded("降级")));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.generateAiReport(1L);

        assertThat(result).isNotNull();
        assertThat(result.getAiInterpretation()).isNull();
        verify(examinationRepository, never()).saveAndFlush(any(Examination.class));
    }

    @Test
    void generateAiReport_shouldThrow_whenStatusPending() {
        Examination examination = buildExamination(1L, ExaminationStatus.PENDING);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));

        assertThatThrownBy(() -> service.generateAiReport(1L))
                .isInstanceOf(BusinessException.class);
        verify(aiService, never()).analysisReportForInspection(any());
    }

    @Test
    void generateAiReport_shouldInternalCombineImageRecognition_whenImagingTypeWithImageUrl() {
        // §3.4.5 内部组合 §3.4.7：影像类检查且有 imageUrl 时，内部调用影像分析
        Examination examination = buildExamination(1L, ExaminationStatus.COMPLETED);
        examination.setImageUrl("http://example.com/img.dcm");
        examination.setImageType("DICOM");
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));

        InspectionReportResponse response = new InspectionReportResponse();
        response.setReportDraft("AI报告");
        response.setConfidence(0.9);
        when(aiService.analysisReportForInspection(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ImageAnalysisResponse imgResponse = new ImageAnalysisResponse();
        imgResponse.setModelId("HEAD_CT_ARTIFACT");
        imgResponse.setConfidence(0.85);
        ImageAnalysisResponse.RecognitionResult recognition = new ImageAnalysisResponse.RecognitionResult();
        recognition.setLabels(List.of("结节"));
        imgResponse.setRecognitionResult(recognition);
        when(aiService.imageAnalysis(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(imgResponse)));

        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.generateAiReport(1L);

        assertThat(result).isNotNull();
        assertThat(result.getAiInterpretation()).contains("AI报告");
        assertThat(result.getAiConfidence()).isEqualTo(0.9);
        // 验证内部调用了 §3.4.7 影像分析
        verify(aiService).analysisReportForInspection(any());
        verify(aiService).imageAnalysis(any());
    }

    // ==================== AI 影像分析推理 §3.4.7 ====================

    @Test
    void analyzeImage_shouldSucceedAndPersistImageAnalysisResult_whenAiReturnsSuccess() {
        Examination examination = buildExamination(1L, ExaminationStatus.COMPLETED);
        examination.setImageUrl("http://example.com/img.dcm");
        examination.setImageType("DICOM");
        examination.setImpression("原始印象");
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        ImageAnalysisResponse response = new ImageAnalysisResponse();
        response.setModelId("HEAD_CT_ARTIFACT");
        response.setConfidence(0.88);
        ImageAnalysisResponse.RecognitionResult recognition = new ImageAnalysisResponse.RecognitionResult();
        recognition.setRegions(List.of("右上肺"));
        recognition.setLabels(List.of("结节"));
        response.setRecognitionResult(recognition);
        response.setSegmentationMaskRef("mask://seg/123");
        response.setAuxiliaryAdvice("建议复查");
        when(aiService.imageAnalysis(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));
        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.analyzeImage(1L);

        assertThat(result).isNotNull();
        // 影像分析结果独立持久化，不覆盖原始印象
        assertThat(result.getImpression()).isEqualTo("原始印象");
        assertThat(result.getImageAnalysisResult()).contains("HEAD_CT_ARTIFACT");
        assertThat(result.getImageAnalysisResult()).contains("右上肺");
        assertThat(result.getImageAnalysisResult()).contains("结节");
        assertThat(result.getImageAnalysisResult()).contains("mask://seg/123");
        assertThat(result.getImageAnalysisResult()).contains("建议复查");
        assertThat(result.getImageConfidence()).isEqualTo(0.88);
        verify(aiService).imageAnalysis(any());
    }

    @Test
    void analyzeImage_shouldAlsoAllowInProgressStatus() {
        Examination examination = buildExamination(1L, ExaminationStatus.IN_PROGRESS);
        examination.setImageUrl("http://example.com/img.dcm");
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        ImageAnalysisResponse response = new ImageAnalysisResponse();
        response.setModelId("HEAD_CT_ARTIFACT");
        when(aiService.imageAnalysis(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));
        when(examinationRepository.saveAndFlush(any(Examination.class))).thenAnswer(inv -> inv.getArgument(0));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.analyzeImage(1L);

        assertThat(result).isNotNull();
        assertThat(result.getImageAnalysisResult()).contains("HEAD_CT_ARTIFACT");
    }

    @Test
    void analyzeImage_shouldDegrade_whenAiReturnsFailure() {
        Examination examination = buildExamination(1L, ExaminationStatus.COMPLETED);
        examination.setImageUrl("http://example.com/img.dcm");
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));
        when(aiService.imageAnalysis(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.failure("AI_ERROR")));
        when(examinationItemRepository.findByExaminationId(1L)).thenReturn(Collections.emptyList());

        ExaminationDTO result = service.analyzeImage(1L);

        // §3.4.7 降级路径：返回 DTO，不抛异常，不持久化
        assertThat(result).isNotNull();
        assertThat(result.getImageAnalysisResult()).isNull();
        verify(examinationRepository, never()).saveAndFlush(any(Examination.class));
    }

    @Test
    void analyzeImage_shouldThrow_whenStatusPending() {
        Examination examination = buildExamination(1L, ExaminationStatus.PENDING);
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(examination));

        assertThatThrownBy(() -> service.analyzeImage(1L))
                .isInstanceOf(BusinessException.class);
        verify(aiService, never()).imageAnalysis(any());
    }

    // ==================== AI 执行顺序推荐 §3.4.11 ====================

    @Test
    void recommendExecutionOrder_shouldReturnDto_whenSuccess() {
        Examination exam1 = buildExamination(1L, ExaminationStatus.PENDING);
        exam1.setEmergencyFlag(true);
        Examination exam2 = buildExamination(2L, ExaminationStatus.PENDING);
        exam2.setEmergencyFlag(false);
        when(examinationRepository.findByPatientIdAndStatus(any(), eq(ExaminationStatus.PENDING)))
                .thenReturn(Arrays.asList(exam1, exam2));
        when(examinationRepository.findByPatientIdAndStatus(any(), eq(ExaminationStatus.SCHEDULED)))
                .thenReturn(Collections.emptyList());

        ExecutionOrderResponse response = new ExecutionOrderResponse();
        ExecutionOrderResponse.OrderItem item1 = new ExecutionOrderResponse.OrderItem();
        item1.setTaskId(2L);
        item1.setPriority("P1");
        item1.setRecommendedTime("2026-07-04 09:00");
        item1.setReason("急诊优先");
        ExecutionOrderResponse.OrderItem item2 = new ExecutionOrderResponse.OrderItem();
        item2.setTaskId(1L);
        item2.setPriority("P2");
        item2.setRecommendedTime("2026-07-04 10:00");
        item2.setReason("常规检查");
        response.setExecutionOrder(Arrays.asList(item1, item2));
        response.setSummary("急诊优先");
        response.setDisclaimerRequired(true);
        when(aiService.recommendExecutionOrder(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ExecutionOrderDTO result = service.recommendExecutionOrder(1L);

        assertThat(result).isNotNull();
        assertThat(result.getExecutionOrder()).hasSize(2);
        assertThat(result.getExecutionOrder().get(0).getTaskId()).isEqualTo(2L);
        assertThat(result.getExecutionOrder().get(0).getPriority()).isEqualTo("P1");
        assertThat(result.getExecutionOrder().get(1).getTaskId()).isEqualTo(1L);
        assertThat(result.getSummary()).isEqualTo("急诊优先");
        assertThat(result.getDisclaimerRequired()).isTrue();
        assertThat(result.isDegraded()).isFalse();
        verify(aiService).recommendExecutionOrder(any());
    }

    @Test
    void recommendExecutionOrder_shouldReturnEmptyAndNotCallAi_whenNoPendingExaminations() {
        when(examinationRepository.findByPatientIdAndStatus(any(), eq(ExaminationStatus.PENDING)))
                .thenReturn(Collections.emptyList());
        when(examinationRepository.findByPatientIdAndStatus(any(), eq(ExaminationStatus.SCHEDULED)))
                .thenReturn(Collections.emptyList());

        ExecutionOrderDTO result = service.recommendExecutionOrder(1L);

        assertThat(result).isNotNull();
        assertThat(result.getExecutionOrder()).isEmpty();
        verify(aiService, never()).recommendExecutionOrder(any());
    }

    @Test
    void recommendExecutionOrder_shouldDegrade_whenAiReturnsFailure() {
        Examination exam1 = buildExamination(1L, ExaminationStatus.PENDING);
        when(examinationRepository.findByPatientIdAndStatus(any(), eq(ExaminationStatus.PENDING)))
                .thenReturn(Arrays.asList(exam1));
        when(examinationRepository.findByPatientIdAndStatus(any(), eq(ExaminationStatus.SCHEDULED)))
                .thenReturn(Collections.emptyList());
        when(aiService.recommendExecutionOrder(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.failure("AI_ERROR")));

        ExecutionOrderDTO result = service.recommendExecutionOrder(1L);

        // §3.4.11 降级路径：返回 FIFO 排序结果，不抛异常
        assertThat(result).isNotNull();
        assertThat(result.isDegraded()).isTrue();
        assertThat(result.getExecutionOrder()).isNotEmpty();
        assertThat(result.getDisclaimerRequired()).isTrue();
    }

    private Examination buildExamination(Long id, ExaminationStatus status) {
        Examination examination = new Examination();
        examination.setId(id);
        examination.setPatientId(1L);
        examination.setDoctorId(2L);
        examination.setExaminationType(ExaminationType.CT);
        examination.setStatus(status);
        return examination;
    }
}
