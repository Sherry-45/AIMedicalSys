# 测试审查报告（v8 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** `AbstractCapabilityExecutorTest.java` — `executeStandardPipeline()` 管线方法存在约 15 条独立代码路径（成功路径、structuredChat 超时/中断/回退、健康检查失败、超时窗口耗尽、parse 失败、基础设施异常等），但仅有 `routeResult=null`（`NO_AVAILABLE_ROUTE`）一条降级路径被覆盖。设计明确要求 "验证成功路径返回 `AiResult.success`"，但实际测试仅验证了 routeResult=null 场景。管线核心逻辑（`structuredChat().get()` 成功路径拆包、`StructuredOutputNotSupportedException` → `chat()` 回退、`handleSuccess()` 调用、`TimeoutException` 降级、`InterruptedException` 中断恢复等）全部未经测试，测试无法证明管线按契约正常工作。

- **[一般]** `DiscussionConclusionCapabilityExecutor` — 该执行器（含压缩前置阶段、`compressTranscripts()`/`truncateTranscripts()`/`estimateTokenCount()`/`formatTranscripts()` 辅助方法及 `refineTimeoutReason()` 覆写）完全无测试覆盖。设计行为契约第 7、9 条（死锁风险、压缩写回）未经验证。

- **[一般]** `handleSuccess()` (`AbstractCapabilityExecutor.java:475`) — 提取为独立方法后，其 metrics 记录行为（`metricsCollector.record()` + `metricsStore.recordSuccess()`）无直接测试。设计行为契约第 5 条与空安全约束（`if (metricsCollector != null)`）未经验证。

- **[轻微]** `doDegradeShouldHandleNonNullMetricsCollector` (`AbstractCapabilityExecutorTest.java:571`) — 验证了匿名类编译通过但未断言 `metricsCollector.record()` 实际被调用或记录了正确的内容。

## 修改要求（仅 REJECTED 时）

1. **[严重]** `AbstractCapabilityExecutorTest.java` — 为 `executeStandardPipeline()` 补充至少以下测试场景：
   - **成功路径**：mock `PromptTemplateManager.render()` 返回有效提示词 → mock `ModelRouter.route()` 返回非 null → mock `ModelEndpointHealthManager.getState()` 返回 `"HEALTHY"` → mock `LlmChatService.structuredChat()` 返回已完成的 `CompletableFuture<AiResult<StructuredChatResult<R>>>`（其中 `result.isSuccess()=true`, `getData().getData()` 返回有效结果, `getData().getUsage()` 返回 usage）→ 验证返回 `AiResult.success()` 且 `result.getData()` 等于预期值
   - **EndPoint 不可用降级**：`getState()` 返回 `null` 或 `"UNHEALTHY"` → 验证 `ENDPOINT_UNAVAILABLE` 降级
   - **structuredChat 超时**：`structuredFuture.get()` 抛 `TimeoutException` → 验证 `TIMEOUT` 降级
   - **structuredChat 中断**：`structuredFuture.get()` 抛 `InterruptedException` → 验证 `TIMEOUT` 降级且中断标记恢复
   - **不支持结构化输出回退**：`structuredFuture.get()` 抛 `ExecutionException(cause instanceof StructuredOutputNotSupportedException)` → mock `chat()` 返回有效原始内容 + `structuredOutputParser.parse()` 成功 → 验证最终返回 `AiResult.success()`
   - **parse 失败回退**：`chat()` 返回成功但 `structuredOutputParser.parse()` 抛异常 → 验证 `PARSE_FAILURE` 降级
   - **超时窗口耗尽**：mock `System.currentTimeMillis()` 使 `consumedMs >= totalTimeoutMs` → 验证 `TIMEOUT` 降级
   - 其中成功路径是必须项，其余推荐至少覆盖 2-3 个主要异常路径

2. **[一般]** 新建 `DiscussionConclusionCapabilityExecutorTest.java` — 测试以下场景：
   - `doExecuteInternal()` 中 transcripts 未超过阈值不触发压缩 → 验证 `request.setTranscripts()` 未被替换
   - `doExecuteInternal()` 中 transcripts 超过阈值 → mock `llmChatService.chat()` 返回压缩结果 → 验证 transcripts 被替换为单条 SYSTEM 压缩摘要
   - `doExecuteInternal()` 中压缩异常 → 验证回退截断逻辑
   - `refineTimeoutReason()` 超时原因拼接（`:transcriptSummaryCrowding` 与 `:primaryLlmTimeout` 分支）
   - `estimateTokenCount()` / `formatTranscripts()` / `truncateTranscripts()` 辅助方法边界条件

3. **[一般]** `AbstractCapabilityExecutorTest.java` — 为 `handleSuccess()` 增加验证：
   - 当 `metricsCollector != null` 时验证 `record()` 被调用且 `degraded=false`、`degradeReason=null`、`promptTokens`/`completionTokens` 映射正确
   - 当 `metricsStore != null` 时验证 `recordSuccess()` 被调用
