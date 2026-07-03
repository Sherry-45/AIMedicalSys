# 任务指令（v5）

## 动作
NEW

## 任务描述
在 `ai-impl/orchestrator/` 包新增 `AiOrchestrator` 类，实现 `com.aimedical.modules.ai.api.AiService` 接口全部 13 个方法，作为底座统一编排路由层。

**预期文件路径**：
- `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/AiOrchestrator.java`

**行为概要**：
1. 实现 `AiService` 全部 13 个方法，每个方法通过硬编码 capabilityId 委托给统一 `handle(capabilityId, request)` 方法
2. `handle()` 从 `executorMap` 查找对应的 `CapabilityExecutor` 并调用其 `execute()` 方法
3. `@PostConstruct` 阶段通过 `List<CapabilityExecutor>` 自动注入构建 `Map<String, CapabilityExecutor>`
4. `handle()` 中 `executor.execute()` 返回的 CompletableFuture 直接透传，不包装
5. `handle()` 中捕获意外异常（executorMap 中 null、execute() 抛出的同步异常），记录错误日志，写入 `SlidingWindowMetricsStore.recordFailure()`，返回 `AiResult.failure()`

**能力标识映射表**：

| AiService 方法 | capabilityId |
|---------------|-------------|
| triage | "TRIAGE" |
| prescriptionCheck | "RX_AUDIT" |
| generateMedicalRecord | "MEDICAL_RECORD_GEN" |
| prescriptionAssist | "RX_ASSIST" |
| knowledgeBaseQuery | "KB_QUERY" |
| schedule | "SCHEDULE" |
| discussionConclusion | "DISCUSSION_CONCLUSION" |
| diagnosis | "DIAGNOSIS" |
| analysisReportForInspection | "ANALYSIS_REPORT_INSPECTION" |
| analysisReportForLabTest | "ANALYSIS_REPORT_LABTEST" |
| imageAnalysis | "IMAGE_ANALYSIS" |
| recommendExamination | "RECOMMEND_EXAM" |
| recommendExecutionOrder | "RECOMMEND_EXEC_ORDER" |

**异常兜底行为**：
- `executorMap.get(capabilityId)` 返回 null 时：log.warn + 返回 `CompletableFuture.completedFuture(AiResult.failure("未注册能力标识: " + capabilityId))`
- `executor.execute()` 抛出同步异常时：log.error + `metricsStore.recordFailure(capabilityId)` + 返回 `CompletableFuture.completedFuture(AiResult.failure("AI服务暂时不可用，请稍后重试"))`
- 注意：`AiMetricsCollector` 当前为空接口（存根），catch 块中暂不调用 `metricsCollector.record()`，留 TODO 标记

## 选择理由
AiOrchestrator 是底座架构的入口编排层，P0 最高优先级。全部前置依赖均已就绪：
- CapabilityExecutor 接口 ✓（R4 PASSED）
- AbstractCapabilityExecutor 抽象骨架 ✓（R4 PASSED）
- SlidingWindowMetricsStore ✓（R2 PASSED）
- TimeoutDegradationStrategy ✓（R5 PASSED）
- CircuitBreakerDegradationStrategy ✓（R5 PASSED）
- AiService 接口（ai-api 已有）
- AiResult（ai-api 已有）

完成后即可将所有能力能力的执行纳入底座统一编排，是 Batch2（7 项底座 CapabilityExecutor 实现）的前置条件。

## 任务上下文

### 设计文档关键摘录（来自 Docs/06_ood_phase5_G.md）

**§3.1 编排层 — AiOrchestrator**：
- 替代原 MockAiService 成为 FallbackAiService 的实际委托对象（`ai.platform.enabled=true` 时激活）
- 每个方法的执行流程：查找执行器 → 委托执行 → 返回结果
- 不介入管线内部步骤（模板渲染、实验分流、模型路由、LLM 调用等）
- `@PostConstruct` 阶段扫描 `List<CapabilityExecutor>` 自动注入构建 Map
- 未注册执行器返回失败结果（非异常传播）

**§4.1 关键行为契约 — AiOrchestrator.handle() 伪代码**：
```python
handle(capabilityId, request):
  1. try:
  2.   executor = executorMap.get(capabilityId)
  3.   if executor == null:
  4.     log.warn("未注册能力标识: {}, 返回失败结果", capabilityId)
  5.     return CompletableFuture.completedFuture(AiResult.failure("未注册能力标识: " + capabilityId))
  6.   result = executor.execute(request, capabilityId)
  7.   return result
  8. catch (Exception e):
  9.   log.error("CapabilityExecutor 执行异常: capabilityId={}, error={}", capabilityId, e.getMessage(), e)
  10.  // 提取上下文信息（当前简化：跳过 HTTP Header 提取，仅记录）
  11.  metricsStore.recordFailure(capabilityId)
  12.  return CompletableFuture.completedFuture(AiResult.failure("AI服务暂时不可用，请稍后重试"))
```

### 已有代码上下文

**AiService 接口**（ai-api）：
- 13 个 CompletableFuture<AiResult<T>> 方法，各方法入参/返回值 DTO 不同
- 每个方法对应一个 distinct capabilityId

**CapabilityExecutor 接口**（已实现）：
- `CompletableFuture<AiResult<R>> execute(T request, String capabilityId)`
- `String getCapabilityId()`
- `Class<T> getInputType()`
- `Class<R> getOutputType()`

**SlidingWindowMetricsStore**（已实现）：
- `void recordFailure(String capabilityId)` — 记录失败次数
- `void recordSuccess(String capabilityId, long elapsedMs)`
- `void recordDegraded(String capabilityId, long elapsedMs)`

**AiMetricsCollector**（存根，空接口）：
- 当前无方法，catch 块中不调用，留 TODO

**@ConditionalOnProperty 与 Bean 装配**：
- AiOrchestrator 标注 `@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")`
- 与 MockAiService（`@ConditionalOnProperty(name = "ai.mock.enabled", ...)`）互斥
- 组件注解使用 `@Component` 或 `@Service`

## 测试规划

**测试文件路径**：
`AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/AiOrchestratorTest.java`

**覆盖场景**：
1. **正常路由**：Mock CapabilityExecutor 返回成功结果，验证 AiOrchestrator.triage() 等调用正确委托
2. **未知能力标识**：路由到未注册的能力标识，验证返回 AiResult.failure()
3. **CapabilityExecutor 异常**：模拟 executor.execute() 抛出异常，验证 catch 块捕获并返回 failure()
4. **executorMap 初始化**：验证 @PostConstruct 后 executorMap 包含了所有注入的 executor
5. **所有 13 个方法**：至少测试 2-3 个代表性方法（triage、diagnosis、prescriptionCheck），其余验证委托模式一致
