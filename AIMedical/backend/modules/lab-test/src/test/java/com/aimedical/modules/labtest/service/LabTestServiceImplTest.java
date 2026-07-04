package com.aimedical.modules.labtest.service;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.common.result.PageResponse;
import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.AiService;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderResponse;
import com.aimedical.modules.ai.api.dto.labtest.LabTestReportResponse;
import com.aimedical.modules.labtest.dto.LabTestCompleteRequest;
import com.aimedical.modules.labtest.dto.LabTestCreateRequest;
import com.aimedical.modules.labtest.dto.LabTestDTO;
import com.aimedical.modules.labtest.dto.LabTestExecutionOrderDTO;
import com.aimedical.modules.labtest.dto.LabTestItemRequest;
import com.aimedical.modules.labtest.dto.LabTestQueryRequest;
import com.aimedical.modules.labtest.dto.LabTestTrendDTO;
import com.aimedical.modules.labtest.entity.AbnormalFlag;
import com.aimedical.modules.labtest.entity.LabTest;
import com.aimedical.modules.labtest.entity.LabTestItem;
import com.aimedical.modules.labtest.entity.LabTestStatus;
import com.aimedical.modules.labtest.entity.SampleType;
import com.aimedical.modules.labtest.exception.LabTestErrorCode;
import com.aimedical.modules.labtest.repository.LabTestItemRepository;
import com.aimedical.modules.labtest.repository.LabTestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * LabTestServiceImpl 单元测试。
 * 通过 mock 仓储与 AI 服务验证全部业务方法。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LabTestServiceImpl 检验服务")
class LabTestServiceImplTest {

    @Mock
    private LabTestRepository labTestRepository;

    @Mock
    private LabTestItemRepository labTestItemRepository;

    @Mock
    private AiService aiService;

    @InjectMocks
    private LabTestServiceImpl service;

    @Test
    @DisplayName("createLabTest 应以 PENDING 状态创建检验记录")
    void createLabTest_shouldCreateWithPendingStatus() {
        LabTestCreateRequest request = new LabTestCreateRequest();
        request.setPatientId(10L);
        request.setDoctorId(20L);
        request.setTestType("血常规");
        request.setSampleType(SampleType.BLOOD);
        when(labTestRepository.saveAndFlush(any(LabTest.class))).thenAnswer(inv -> {
            LabTest e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        LabTestDTO dto = service.createLabTest(request);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPatientId()).isEqualTo(10L);
        assertThat(dto.getDoctorId()).isEqualTo(20L);
        assertThat(dto.getTestType()).isEqualTo("血常规");
        assertThat(dto.getSampleType()).isEqualTo(SampleType.BLOOD);
        assertThat(dto.getStatus()).isEqualTo(LabTestStatus.PENDING);
        verify(labTestRepository).saveAndFlush(any(LabTest.class));
    }

    @Test
    @DisplayName("collectSample 应将 PENDING 状态转为 COLLECTED 并设置采样时间")
    void collectSample_shouldTransitionToCollected() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.PENDING);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));
        when(labTestRepository.saveAndFlush(any(LabTest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(labTestItemRepository.findByLabTestId(1L)).thenReturn(Collections.emptyList());

        LabTestDTO dto = service.collectSample(1L);

        assertThat(dto.getStatus()).isEqualTo(LabTestStatus.COLLECTED);
        assertThat(dto.getCollectedAt()).isNotNull();
        verify(labTestRepository).saveAndFlush(any(LabTest.class));
    }

    @Test
    @DisplayName("collectSample 当非 PENDING 状态时抛出状态异常")
    void collectSample_shouldThrowWhenNotPending() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.COMPLETED);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));

        assertThatThrownBy(() -> service.collectSample(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(LabTestErrorCode.LAB_TEST_STATUS_INVALID);
    }

    @Test
    @DisplayName("startLabTest 应将 COLLECTED 状态转为 IN_PROGRESS")
    void startLabTest_shouldTransitionToInProgress() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.COLLECTED);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));
        when(labTestRepository.saveAndFlush(any(LabTest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(labTestItemRepository.findByLabTestId(1L)).thenReturn(Collections.emptyList());

        LabTestDTO dto = service.startLabTest(1L);

        assertThat(dto.getStatus()).isEqualTo(LabTestStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("startLabTest 当非 COLLECTED 状态时抛出状态异常")
    void startLabTest_shouldThrowWhenNotCollected() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.PENDING);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));

        assertThatThrownBy(() -> service.startLabTest(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(LabTestErrorCode.LAB_TEST_STATUS_INVALID);
    }

    @Test
    @DisplayName("completeLabTest 应将 IN_PROGRESS 转为 COMPLETED 并保存明细")
    void completeLabTest_shouldTransitionToCompletedAndSaveItems() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.IN_PROGRESS);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));
        when(labTestRepository.saveAndFlush(any(LabTest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(labTestItemRepository.findByLabTestId(1L)).thenReturn(Collections.emptyList());
        when(labTestItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        LabTestItemRequest item = new LabTestItemRequest();
        item.setItemName("白细胞计数");
        item.setResult("6.5");
        item.setUnit("10^9/L");
        item.setReferenceRange("4-10");
        item.setAbnormalFlag(AbnormalFlag.NORMAL);

        LabTestCompleteRequest request = new LabTestCompleteRequest();
        request.setReportConclusion("正常");
        request.setItems(List.of(item));

        LabTestDTO dto = service.completeLabTest(1L, request);

        assertThat(dto.getStatus()).isEqualTo(LabTestStatus.COMPLETED);
        assertThat(dto.getReportConclusion()).isEqualTo("正常");
        assertThat(dto.getReportedAt()).isNotNull();
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getItemName()).isEqualTo("白细胞计数");
        verify(labTestItemRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("completeLabTest 当明细为空时抛出明细空异常")
    void completeLabTest_shouldThrowWhenItemsEmpty() {
        LabTestCompleteRequest request = new LabTestCompleteRequest();
        request.setReportConclusion("结论");
        request.setItems(Collections.emptyList());

        assertThatThrownBy(() -> service.completeLabTest(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(LabTestErrorCode.LAB_TEST_ITEM_EMPTY);
    }

    @Test
    @DisplayName("completeLabTest 当非 IN_PROGRESS 状态时抛出状态异常")
    void completeLabTest_shouldThrowWhenNotInProgress() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.PENDING);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));

        LabTestItemRequest item = new LabTestItemRequest();
        item.setItemName("白细胞计数");

        LabTestCompleteRequest request = new LabTestCompleteRequest();
        request.setItems(List.of(item));

        assertThatThrownBy(() -> service.completeLabTest(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(LabTestErrorCode.LAB_TEST_STATUS_INVALID);
    }

    @Test
    @DisplayName("cancelLabTest 应将 PENDING 状态转为 CANCELLED")
    void cancelLabTest_shouldTransitionFromPendingToCancelled() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.PENDING);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));
        when(labTestRepository.saveAndFlush(any(LabTest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(labTestItemRepository.findByLabTestId(1L)).thenReturn(Collections.emptyList());

        LabTestDTO dto = service.cancelLabTest(1L);

        assertThat(dto.getStatus()).isEqualTo(LabTestStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelLabTest 应将 COLLECTED 状态转为 CANCELLED")
    void cancelLabTest_shouldTransitionFromCollectedToCancelled() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.COLLECTED);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));
        when(labTestRepository.saveAndFlush(any(LabTest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(labTestItemRepository.findByLabTestId(1L)).thenReturn(Collections.emptyList());

        LabTestDTO dto = service.cancelLabTest(1L);

        assertThat(dto.getStatus()).isEqualTo(LabTestStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelLabTest 当 COMPLETED 状态时抛出状态异常")
    void cancelLabTest_shouldThrowWhenCompleted() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.COMPLETED);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));

        assertThatThrownBy(() -> service.cancelLabTest(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(LabTestErrorCode.LAB_TEST_STATUS_INVALID);
    }

    @Test
    @DisplayName("getLabTest 应返回含明细的 DTO")
    void getLabTest_shouldReturnDtoWithItems() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.COMPLETED);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));
        LabTestItem item = new LabTestItem();
        item.setId(100L);
        item.setLabTestId(1L);
        item.setItemName("白细胞计数");
        item.setAbnormalFlag(AbnormalFlag.NORMAL);
        when(labTestItemRepository.findByLabTestId(1L)).thenReturn(List.of(item));

        LabTestDTO dto = service.getLabTest(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getItemName()).isEqualTo("白细胞计数");
    }

    @Test
    @DisplayName("getLabTest 当记录不存在时抛出 NOT_FOUND")
    void getLabTest_shouldThrowWhenNotFound() {
        when(labTestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLabTest(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(LabTestErrorCode.LAB_TEST_NOT_FOUND);
    }

    @Test
    @DisplayName("getLabTests 应返回分页结果")
    void getLabTests_shouldReturnPagedResults() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.COMPLETED);
        Page<LabTest> page = new PageImpl<>(List.of(labTest), PageRequest.of(0, 20), 1);
        when(labTestRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(page);
        when(labTestItemRepository.findByLabTestIdIn(anyList())).thenReturn(Collections.emptyList());

        LabTestQueryRequest query = new LabTestQueryRequest();
        query.setPatientId(10L);

        PageResponse<LabTestDTO> response = service.getLabTests(query);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1L);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("getLabTests 当无结果时返回空页")
    void getLabTests_shouldReturnEmptyPage() {
        Page<LabTest> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(labTestRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(page);

        LabTestQueryRequest query = new LabTestQueryRequest();

        PageResponse<LabTestDTO> response = service.getLabTests(query);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();
    }

    // ==================== AI 检验报告 §3.4.6 ====================

    @Test
    @DisplayName("generateAiReport 成功时更新 AI 解读")
    void generateAiReport_shouldUpdateInterpretationOnSuccess() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.COMPLETED);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));
        LabTestReportResponse response = new LabTestReportResponse();
        response.setReportDraft("AI报告草稿");
        response.setInterpretation("AI 检验解读内容");
        response.setSuggestions(List.of("建议复查"));
        response.setConfidence(0.92);
        when(aiService.analysisReportForLabTest(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));
        when(labTestRepository.saveAndFlush(any(LabTest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(labTestItemRepository.findByLabTestId(1L)).thenReturn(Collections.emptyList());

        LabTestDTO dto = service.generateAiReport(1L);

        assertThat(dto.getAiInterpretation()).isNotBlank();
        assertThat(dto.getAiInterpretation()).contains("AI报告草稿");
        assertThat(dto.getAiInterpretation()).contains("AI 检验解读内容");
        assertThat(dto.getAiInterpretation()).contains("建议复查");
        verify(labTestRepository).saveAndFlush(any(LabTest.class));
    }

    @Test
    @DisplayName("generateAiReport 当 AI 返回失败时降级到医生手动判读，不抛出")
    void generateAiReport_shouldDegradeWhenAiFails() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.COMPLETED);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));
        when(aiService.analysisReportForLabTest(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.failure("AI_ERROR")));
        when(labTestItemRepository.findByLabTestId(1L)).thenReturn(Collections.emptyList());

        LabTestDTO dto = service.generateAiReport(1L);

        // §3.4.6 降级路径：不设置 aiInterpretation，不抛出
        assertThat(dto.getAiInterpretation()).isNull();
        verify(labTestRepository, never()).saveAndFlush(any(LabTest.class));
    }

    @Test
    @DisplayName("generateAiReport 当 AI 返回降级结果时也降级到医生手动判读")
    void generateAiReport_shouldDegradeWhenAiReturnsDegraded() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.COMPLETED);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));
        when(aiService.analysisReportForLabTest(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.degraded("降级")));
        when(labTestItemRepository.findByLabTestId(1L)).thenReturn(Collections.emptyList());

        LabTestDTO dto = service.generateAiReport(1L);

        assertThat(dto.getAiInterpretation()).isNull();
        verify(labTestRepository, never()).saveAndFlush(any(LabTest.class));
    }

    @Test
    @DisplayName("generateAiReport 当 AI 调用异常时降级到医生手动判读，不抛出")
    void generateAiReport_shouldDegradeWhenAiThrows() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.COMPLETED);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));
        when(aiService.analysisReportForLabTest(any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("连接超时")));
        when(labTestItemRepository.findByLabTestId(1L)).thenReturn(Collections.emptyList());

        LabTestDTO dto = service.generateAiReport(1L);

        // §3.4.6 降级路径：不设置 aiInterpretation，不抛出
        assertThat(dto.getAiInterpretation()).isNull();
        verify(labTestRepository, never()).saveAndFlush(any(LabTest.class));
    }

    @Test
    @DisplayName("generateAiReport 当非 COMPLETED 状态时抛出状态异常")
    void generateAiReport_shouldThrowWhenNotCompleted() {
        LabTest labTest = buildLabTest(1L, LabTestStatus.IN_PROGRESS);
        when(labTestRepository.findById(1L)).thenReturn(Optional.of(labTest));

        assertThatThrownBy(() -> service.generateAiReport(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(LabTestErrorCode.LAB_TEST_STATUS_INVALID);
    }

    // ==================== AI 执行顺序推荐 §3.4.11 ====================

    @Test
    @DisplayName("recommendExecutionOrder 成功时返回 AI 排序结果")
    void recommendExecutionOrder_shouldReturnDto_whenSuccess() {
        LabTest test1 = buildLabTest(1L, LabTestStatus.PENDING);
        LabTest test2 = buildLabTest(2L, LabTestStatus.PENDING);
        when(labTestRepository.findByPatientIdAndStatus(eq(10L), eq(LabTestStatus.PENDING)))
                .thenReturn(List.of(test1, test2));

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
        item2.setReason("常规");
        response.setExecutionOrder(List.of(item1, item2));
        response.setSummary("按紧急度排序");
        response.setDisclaimerRequired(true);
        when(aiService.recommendExecutionOrder(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        LabTestExecutionOrderDTO result = service.recommendExecutionOrder(10L);

        assertThat(result).isNotNull();
        assertThat(result.getExecutionOrder()).hasSize(2);
        assertThat(result.getExecutionOrder().get(0).getTaskId()).isEqualTo(2L);
        assertThat(result.getExecutionOrder().get(0).getPriority()).isEqualTo("P1");
        assertThat(result.getSummary()).isEqualTo("按紧急度排序");
        assertThat(result.getDisclaimerRequired()).isTrue();
        assertThat(result.isDegraded()).isFalse();
        verify(aiService).recommendExecutionOrder(any());
    }

    @Test
    @DisplayName("recommendExecutionOrder 当无待执行检验时不调用 AI 并返回空列表")
    void recommendExecutionOrder_shouldReturnEmptyAndNotCallAi_whenNoPending() {
        when(labTestRepository.findByPatientIdAndStatus(eq(10L), eq(LabTestStatus.PENDING)))
                .thenReturn(Collections.emptyList());

        LabTestExecutionOrderDTO result = service.recommendExecutionOrder(10L);

        assertThat(result).isNotNull();
        assertThat(result.getExecutionOrder()).isEmpty();
        verify(aiService, never()).recommendExecutionOrder(any());
    }

    @Test
    @DisplayName("recommendExecutionOrder 当 AI 返回失败时降级到 FIFO 排序")
    void recommendExecutionOrder_shouldDegrade_whenAiReturnsFailure() {
        LabTest test1 = buildLabTest(1L, LabTestStatus.PENDING);
        when(labTestRepository.findByPatientIdAndStatus(eq(10L), eq(LabTestStatus.PENDING)))
                .thenReturn(List.of(test1));
        when(aiService.recommendExecutionOrder(any()))
                .thenReturn(CompletableFuture.completedFuture(AiResult.failure("AI_ERROR")));

        LabTestExecutionOrderDTO result = service.recommendExecutionOrder(10L);

        // §3.4.11 降级路径：返回 FIFO 排序结果，不抛异常
        assertThat(result).isNotNull();
        assertThat(result.isDegraded()).isTrue();
        assertThat(result.getExecutionOrder()).isNotEmpty();
        assertThat(result.getDisclaimerRequired()).isTrue();
    }

    // ==================== 检验趋势图 ====================

    @Test
    @DisplayName("getTrend 应按时间排序构建检验趋势")
    void getTrend_shouldBuildTrendPointsSortedByDate() {
        LocalDateTime date1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2024, 2, 1, 10, 0);

        LabTest test1 = buildLabTest(1L, LabTestStatus.COMPLETED);
        test1.setCreatedAt(date1);
        LabTest test2 = buildLabTest(2L, LabTestStatus.COMPLETED);
        test2.setCreatedAt(date2);
        when(labTestRepository.findByPatientIdAndStatus(eq(10L), eq(LabTestStatus.COMPLETED)))
                .thenReturn(List.of(test1, test2));

        LabTestItem item1 = new LabTestItem();
        item1.setLabTestId(1L);
        item1.setItemName("白细胞计数");
        item1.setResult("6.5");
        item1.setUnit("10^9/L");
        item1.setAbnormalFlag(AbnormalFlag.NORMAL);

        LabTestItem item2 = new LabTestItem();
        item2.setLabTestId(2L);
        item2.setItemName("白细胞计数");
        item2.setResult("3.5");
        item2.setUnit("10^9/L");
        item2.setAbnormalFlag(AbnormalFlag.LOW);

        when(labTestItemRepository.findByLabTestIdIn(anyList())).thenReturn(List.of(item1, item2));

        LabTestTrendDTO trend = service.getTrend(10L, "白细胞计数");

        assertThat(trend.getItemName()).isEqualTo("白细胞计数");
        assertThat(trend.getUnit()).isEqualTo("10^9/L");
        assertThat(trend.getPoints()).hasSize(2);
        assertThat(trend.getPoints().get(0).getResult()).isEqualTo("6.5");
        assertThat(trend.getPoints().get(0).getTestDate()).isEqualTo(date1);
        assertThat(trend.getPoints().get(0).getAbnormalFlag()).isEqualTo(AbnormalFlag.NORMAL);
        assertThat(trend.getPoints().get(1).getResult()).isEqualTo("3.5");
        assertThat(trend.getPoints().get(1).getTestDate()).isEqualTo(date2);
        assertThat(trend.getPoints().get(1).getAbnormalFlag()).isEqualTo(AbnormalFlag.LOW);
    }

    @Test
    @DisplayName("getTrend 当无已完成检验时返回空趋势")
    void getTrend_shouldReturnEmptyWhenNoCompletedTests() {
        when(labTestRepository.findByPatientIdAndStatus(eq(10L), eq(LabTestStatus.COMPLETED)))
                .thenReturn(Collections.emptyList());

        LabTestTrendDTO trend = service.getTrend(10L, "白细胞计数");

        assertThat(trend.getItemName()).isEqualTo("白细胞计数");
        assertThat(trend.getPoints()).isEmpty();
        assertThat(trend.getUnit()).isNull();
    }

    @Test
    @DisplayName("getTrend 应过滤不匹配的明细项")
    void getTrend_shouldFilterNonMatchingItems() {
        LocalDateTime date1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LabTest test1 = buildLabTest(1L, LabTestStatus.COMPLETED);
        test1.setCreatedAt(date1);
        when(labTestRepository.findByPatientIdAndStatus(eq(10L), eq(LabTestStatus.COMPLETED)))
                .thenReturn(List.of(test1));

        LabTestItem matchingItem = new LabTestItem();
        matchingItem.setLabTestId(1L);
        matchingItem.setItemName("白细胞计数");
        matchingItem.setResult("6.5");
        matchingItem.setUnit("10^9/L");
        matchingItem.setAbnormalFlag(AbnormalFlag.NORMAL);

        LabTestItem otherItem = new LabTestItem();
        otherItem.setLabTestId(1L);
        otherItem.setItemName("红细胞计数");
        otherItem.setResult("4.5");
        otherItem.setUnit("10^12/L");
        otherItem.setAbnormalFlag(AbnormalFlag.NORMAL);

        when(labTestItemRepository.findByLabTestIdIn(anyList())).thenReturn(List.of(matchingItem, otherItem));

        LabTestTrendDTO trend = service.getTrend(10L, "白细胞计数");

        assertThat(trend.getPoints()).hasSize(1);
        assertThat(trend.getPoints().get(0).getResult()).isEqualTo("6.5");
    }

    @Test
    @DisplayName("getTrend 当结果超过 N 次时只返回最近 N 次（P0 修复验证）")
    void getTrend_shouldLimitToLatestNPoints_whenExceedingN() {
        // 默认 N=5，构造 7 次检验，应只返回最近 5 次
        List<LabTest> tests = new ArrayList<>();
        List<LabTestItem> items = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            LabTest test = buildLabTest((long) i, LabTestStatus.COMPLETED);
            test.setCreatedAt(LocalDateTime.of(2024, 1, i, 10, 0));
            tests.add(test);

            LabTestItem item = new LabTestItem();
            item.setLabTestId((long) i);
            item.setItemName("白细胞计数");
            item.setResult(String.valueOf(i));
            item.setUnit("10^9/L");
            item.setAbnormalFlag(AbnormalFlag.NORMAL);
            items.add(item);
        }
        when(labTestRepository.findByPatientIdAndStatus(eq(10L), eq(LabTestStatus.COMPLETED)))
                .thenReturn(tests);
        when(labTestItemRepository.findByLabTestIdIn(anyList())).thenReturn(items);

        LabTestTrendDTO trend = service.getTrend(10L, "白细胞计数");

        // 应只返回最近 5 次（日期最大的 5 个：3,4,5,6,7）
        assertThat(trend.getPoints()).hasSize(5);
        // 验证按时间正序排列
        assertThat(trend.getPoints().get(0).getResult()).isEqualTo("3");
        assertThat(trend.getPoints().get(4).getResult()).isEqualTo("7");
    }

    private LabTest buildLabTest(Long id, LabTestStatus status) {
        LabTest labTest = new LabTest();
        labTest.setId(id);
        labTest.setPatientId(10L);
        labTest.setDoctorId(20L);
        labTest.setTestType("血常规");
        labTest.setSampleType(SampleType.BLOOD);
        labTest.setStatus(status);
        return labTest;
    }
}
