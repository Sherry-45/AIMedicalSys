package com.aimedical.modules.labtest.service;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.PageResponse;
import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.AiService;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderRequest;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderResponse;
import com.aimedical.modules.ai.api.dto.labtest.LabTestReportRequest;
import com.aimedical.modules.ai.api.dto.labtest.LabTestReportResponse;
import com.aimedical.modules.labtest.converter.LabTestConverter;
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
import com.aimedical.modules.labtest.exception.LabTestErrorCode;
import com.aimedical.modules.labtest.repository.LabTestItemRepository;
import com.aimedical.modules.labtest.repository.LabTestRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * 检验域服务实现。
 */
@Slf4j
@Service
public class LabTestServiceImpl implements LabTestService {

    /**
     * 检验趋势图默认取最近 N 次结果值。对齐需求 §3.3.4：N 默认 5 次。
     */
    @Value("${lab-test.trend.default-n:5}")
    private int trendDefaultN = 5;

    private final LabTestRepository labTestRepository;
    private final LabTestItemRepository labTestItemRepository;
    private final AiService aiService;

    public LabTestServiceImpl(LabTestRepository labTestRepository,
                              LabTestItemRepository labTestItemRepository,
                              AiService aiService) {
        this.labTestRepository = labTestRepository;
        this.labTestItemRepository = labTestItemRepository;
        this.aiService = aiService;
    }

    @Override
    @Transactional
    public LabTestDTO createLabTest(LabTestCreateRequest request) {
        LabTest entity = new LabTest();
        entity.setPatientId(request.getPatientId());
        entity.setDoctorId(request.getDoctorId());
        entity.setTestType(request.getTestType());
        entity.setSampleType(request.getSampleType());
        entity.setStatus(LabTestStatus.PENDING);
        entity = saveWithOptimisticLock(entity);
        return LabTestConverter.toDTO(entity, Collections.emptyList());
    }

    @Override
    @Transactional
    public LabTestDTO collectSample(Long id) {
        LabTest entity = requireLabTest(id);
        validateStatus(entity, LabTestStatus.PENDING);
        entity.setStatus(LabTestStatus.COLLECTED);
        entity.setCollectedAt(LocalDateTime.now());
        entity = saveWithOptimisticLock(entity);
        return toDtoWithItems(entity);
    }

    @Override
    @Transactional
    public LabTestDTO startLabTest(Long id) {
        LabTest entity = requireLabTest(id);
        validateStatus(entity, LabTestStatus.COLLECTED);
        entity.setStatus(LabTestStatus.IN_PROGRESS);
        entity = saveWithOptimisticLock(entity);
        return toDtoWithItems(entity);
    }

    @Override
    @Transactional
    public LabTestDTO completeLabTest(Long id, LabTestCompleteRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException(LabTestErrorCode.LAB_TEST_ITEM_EMPTY);
        }
        LabTest entity = requireLabTest(id);
        validateStatus(entity, LabTestStatus.IN_PROGRESS);
        entity.setStatus(LabTestStatus.COMPLETED);
        entity.setReportConclusion(request.getReportConclusion());
        entity.setReportedAt(LocalDateTime.now());
        entity = saveWithOptimisticLock(entity);

        // 软删除旧明细：先查询再调用 deleteAll，确保触发 @SQLDelete
        List<LabTestItem> oldItems = labTestItemRepository.findByLabTestId(entity.getId());
        if (!oldItems.isEmpty()) {
            labTestItemRepository.deleteAll(oldItems);
        }

        // 提取 effectively final 的 labTestId，供 lambda 使用
        Long labTestId = entity.getId();
        List<LabTestItemRequest> items = request.getItems();
        List<LabTestItem> itemEntities = items.stream().map(req -> {
            LabTestItem item = new LabTestItem();
            item.setLabTestId(labTestId);
            item.setItemName(req.getItemName());
            item.setResult(req.getResult());
            item.setUnit(req.getUnit());
            item.setReferenceRange(req.getReferenceRange());
            item.setAbnormalFlag(req.getAbnormalFlag() != null ? req.getAbnormalFlag() : AbnormalFlag.NORMAL);
            // B7: 危急值复核提示（对齐 §3.2.4 异常分支：结果中存在异常离谱值时给出复核提示）
            if (item.getAbnormalFlag() == AbnormalFlag.CRITICAL_LOW
                    || item.getAbnormalFlag() == AbnormalFlag.CRITICAL_HIGH) {
                log.warn("检验危急值需复核: labTestId={}, itemName={}, result={}, flag={}",
                        labTestId, item.getItemName(), item.getResult(), item.getAbnormalFlag());
            }
            return item;
        }).collect(Collectors.toList());
        labTestItemRepository.saveAll(itemEntities);

        return LabTestConverter.toDTO(entity, itemEntities);
    }

    @Override
    @Transactional
    public LabTestDTO cancelLabTest(Long id) {
        LabTest entity = requireLabTest(id);
        validateStatus(entity, LabTestStatus.PENDING, LabTestStatus.COLLECTED);
        entity.setStatus(LabTestStatus.CANCELLED);
        entity = saveWithOptimisticLock(entity);
        return toDtoWithItems(entity);
    }

    @Override
    public LabTestDTO getLabTest(Long id) {
        LabTest entity = requireLabTest(id);
        return toDtoWithItems(entity);
    }

    @Override
    public PageResponse<LabTestDTO> getLabTests(LabTestQueryRequest query) {
        Pageable pageable = PageRequest.of(query.getPage(), query.getSize());
        LabTest probe = new LabTest();
        if (query.getPatientId() != null) {
            probe.setPatientId(query.getPatientId());
        }
        if (query.getDoctorId() != null) {
            probe.setDoctorId(query.getDoctorId());
        }
        if (query.getStatus() != null) {
            probe.setStatus(query.getStatus());
        }
        if (query.getSampleType() != null) {
            probe.setSampleType(query.getSampleType());
        }
        Page<LabTest> page = labTestRepository.findAll(Example.of(probe), pageable);

        List<Long> testIds = page.getContent().stream().map(LabTest::getId).collect(Collectors.toList());
        List<LabTestItem> items = testIds.isEmpty()
                ? Collections.emptyList()
                : labTestItemRepository.findByLabTestIdIn(testIds);
        Map<Long, List<LabTestItem>> itemMap = items.stream()
                .collect(Collectors.groupingBy(LabTestItem::getLabTestId));

        List<LabTestDTO> dtos = page.getContent().stream()
                .map(lt -> LabTestConverter.toDTO(lt, itemMap.getOrDefault(lt.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        return PageResponse.of(dtos, page.getTotalElements(), query.getPage(), query.getSize());
    }

    @Override
    public LabTestDTO generateAiReport(Long id) {
        LabTest entity = requireLabTest(id);
        validateStatus(entity, LabTestStatus.COMPLETED);
        // B9: 只查询一次明细，后续复用（避免重复查询）
        List<LabTestItem> items = labTestItemRepository.findByLabTestId(entity.getId());

        LabTestReportRequest request = new LabTestReportRequest();
        request.setLabTestId(entity.getId());
        request.setPatientId(entity.getPatientId());
        request.setDoctorId(entity.getDoctorId());
        request.setTestType(entity.getTestType());
        request.setSampleType(entity.getSampleType() == null ? null : entity.getSampleType().getCode());
        request.setReportConclusion(entity.getReportConclusion());
        request.setItems(items.stream().map(LabTestServiceImpl::toLabTestItemDto).collect(Collectors.toList()));

        // AI 调用在事务外执行，避免长时间占用 DB 连接
        AiResult<LabTestReportResponse> aiResult;
        try {
            aiResult = aiService.analysisReportForLabTest(request).join();
        } catch (Exception e) {
            // B3: AI 调用异常时降级到医生手动判读（对齐 §3.4.6 / §3.2.4 降级路径），不抛出
            log.warn("AI 检验报告调用异常，降级到医生手动判读: labTestId={}", entity.getId(), e);
            return LabTestConverter.toDTO(entity, items);
        }
        if (aiResult == null || !aiResult.isSuccess() || aiResult.isDegraded()) {
            // B3: AI 返回失败/降级时回退到医生手动判读，不抛出
            log.warn("AI 检验报告返回失败/降级，回退到医生手动判读: labTestId={}, aiResult={}", entity.getId(), aiResult);
            return LabTestConverter.toDTO(entity, items);
        }
        LabTestReportResponse data = aiResult.getData();
        if (data != null) {
            // 完整消费 §3.4.6 输出契约字段，组装到 ai_interpretation（实体仅单列存储）
            StringBuilder interpretation = new StringBuilder();
            if (data.getReportDraft() != null) {
                interpretation.append(data.getReportDraft());
            }
            if (data.getInterpretation() != null) {
                if (interpretation.length() > 0) {
                    interpretation.append("\n");
                }
                interpretation.append("解读：").append(data.getInterpretation());
            }
            if (data.getAbnormalItems() != null && !data.getAbnormalItems().isEmpty()) {
                if (interpretation.length() > 0) {
                    interpretation.append("\n");
                }
                String abnormalSummary = data.getAbnormalItems().stream()
                        .map(a -> a.getItemName() + "=" + a.getValue()
                                + (a.getDelta() != null ? "(Δ" + a.getDelta() + ")" : "")
                                + "[" + a.getStatus() + "]")
                        .collect(Collectors.joining("，"));
                interpretation.append("异常项：").append(abnormalSummary);
            }
            if (data.getSuggestions() != null && !data.getSuggestions().isEmpty()) {
                if (interpretation.length() > 0) {
                    interpretation.append("\n");
                }
                interpretation.append("建议：").append(String.join("；", data.getSuggestions()));
            }
            if (interpretation.length() > 0) {
                entity.setAiInterpretation(interpretation.toString());
            }
        }
        entity = saveWithOptimisticLock(entity);
        // B9: 复用已查询的 items，避免 toDtoWithItems 再次查询
        return LabTestConverter.toDTO(entity, items);
    }

    @Override
    @Transactional(readOnly = true)
    public LabTestTrendDTO getTrend(Long patientId, String itemName) {
        List<LabTest> completedTests =
                labTestRepository.findByPatientIdAndStatus(patientId, LabTestStatus.COMPLETED);
        // 使用 reportedAt（报告时间）作为趋势图的检验日期
        Map<Long, LocalDateTime> testDateMap = completedTests.stream()
                .collect(Collectors.toMap(LabTest::getId, lt -> lt.getReportedAt() != null
                        ? lt.getReportedAt() : lt.getCreatedAt()));

        List<Long> testIds = completedTests.stream().map(LabTest::getId).collect(Collectors.toList());
        List<LabTestItem> allItems = testIds.isEmpty()
                ? Collections.emptyList()
                : labTestItemRepository.findByLabTestIdIn(testIds);

        // 精确匹配项目名，避免"白细胞"误命中"白细胞分类计数"
        List<LabTestItem> matchedItems = allItems.stream()
                .filter(item -> itemName.equals(item.getItemName()))
                .collect(Collectors.toList());

        List<LabTestTrendDTO.TrendPoint> points = matchedItems.stream()
                .map(item -> {
                    LabTestTrendDTO.TrendPoint point = new LabTestTrendDTO.TrendPoint();
                    point.setTestDate(testDateMap.get(item.getLabTestId()));
                    point.setResult(item.getResult());
                    point.setAbnormalFlag(item.getAbnormalFlag());
                    return point;
                })
                // B4: 限制为最近 N 次结果值（对齐 §3.3.4：N 默认 5 次，可由配置覆盖）
                // 先降序取最近 N 次，再升序排列以保证时间正序展示
                .sorted(Comparator.comparing(LabTestTrendDTO.TrendPoint::getTestDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(Math.max(1, trendDefaultN))
                .sorted(Comparator.comparing(LabTestTrendDTO.TrendPoint::getTestDate,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        LabTestTrendDTO dto = new LabTestTrendDTO();
        dto.setItemName(itemName);
        dto.setUnit(matchedItems.stream()
                .map(LabTestItem::getUnit)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null));
        dto.setPoints(points);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public LabTestExecutionOrderDTO recommendExecutionOrder(Long patientId) {
        List<LabTest> pending = labTestRepository
                .findByPatientIdAndStatus(patientId, LabTestStatus.PENDING);
        LabTestExecutionOrderDTO dto = new LabTestExecutionOrderDTO();
        if (pending.isEmpty()) {
            dto.setExecutionOrder(new ArrayList<>());
            dto.setDisclaimerRequired(true);
            return dto;
        }

        // 构建 §3.4.11 task_items 结构
        List<ExecutionOrderRequest.TaskItem> taskItems = pending.stream().map(lt -> {
            ExecutionOrderRequest.TaskItem item = new ExecutionOrderRequest.TaskItem();
            item.setTaskId(lt.getId());
            item.setTaskType("LAB");
            item.setItemName(lt.getTestType());
            item.setUrgencyHint("MEDIUM");
            item.setPatientId(lt.getPatientId());
            return item;
        }).collect(Collectors.toList());

        ExecutionOrderRequest request = new ExecutionOrderRequest();
        request.setTaskItems(taskItems);
        request.setTaskRole("LAB_DOCTOR");

        AiResult<ExecutionOrderResponse> result;
        try {
            result = aiService.recommendExecutionOrder(request).join();
        } catch (CompletionException e) {
            // B2: AI 调用异常时降级到 FIFO 手动排序（对齐 §3.4.11 降级路径）
            log.warn("AI 执行顺序推荐调用异常，降级到 FIFO: patientId={}", patientId, e);
            return buildDegradedExecutionOrder(pending);
        }
        if (result == null || !result.isSuccess() || result.isDegraded() || result.getData() == null) {
            log.warn("AI 执行顺序推荐返回失败/降级，回退到 FIFO: patientId={}, result={}", patientId, result);
            return buildDegradedExecutionOrder(pending);
        }
        ExecutionOrderResponse data = result.getData();
        if (data.getExecutionOrder() != null) {
            dto.setExecutionOrder(data.getExecutionOrder().stream()
                    .map(o -> {
                        LabTestExecutionOrderDTO.OrderItem item = new LabTestExecutionOrderDTO.OrderItem();
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
     * §3.4.11 降级路径：AI 不可用时按 FIFO 手动排序。
     */
    private LabTestExecutionOrderDTO buildDegradedExecutionOrder(List<LabTest> pending) {
        LabTestExecutionOrderDTO dto = new LabTestExecutionOrderDTO();
        List<LabTestExecutionOrderDTO.OrderItem> order = pending.stream()
                .map(lt -> {
                    LabTestExecutionOrderDTO.OrderItem item = new LabTestExecutionOrderDTO.OrderItem();
                    item.setTaskId(lt.getId());
                    item.setPriority("P2");
                    item.setReason("AI不可用，按FIFO降级排序");
                    return item;
                })
                .collect(Collectors.toList());
        dto.setExecutionOrder(order);
        dto.setSummary("AI不可用，按FIFO降级排序");
        dto.setDisclaimerRequired(true);
        dto.setDegraded(true);
        return dto;
    }

    private LabTest requireLabTest(Long id) {
        return labTestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(LabTestErrorCode.LAB_TEST_NOT_FOUND));
    }

    private static LabTestReportRequest.Item toLabTestItemDto(LabTestItem item) {
        LabTestReportRequest.Item dto = new LabTestReportRequest.Item();
        dto.setItemName(item.getItemName());
        dto.setValue(item.getResult());
        dto.setUnit(item.getUnit());
        dto.setReferenceRange(item.getReferenceRange());
        dto.setStatus(item.getAbnormalFlag() == null ? null : item.getAbnormalFlag().getCode());
        return dto;
    }

    private void validateStatus(LabTest labTest, LabTestStatus... allowed) {
        for (LabTestStatus status : allowed) {
            if (labTest.getStatus() == status) {
                return;
            }
        }
        throw new BusinessException(LabTestErrorCode.LAB_TEST_STATUS_INVALID);
    }

    private LabTest saveWithOptimisticLock(LabTest entity) {
        try {
            return labTestRepository.saveAndFlush(entity);
        } catch (OptimisticLockException e) {
            throw new BusinessException(GlobalErrorCode.SYSTEM_ERROR, e);
        }
    }

    private LabTestDTO toDtoWithItems(LabTest entity) {
        List<LabTestItem> items = labTestItemRepository.findByLabTestId(entity.getId());
        return LabTestConverter.toDTO(entity, items);
    }
}
