# 测试审查报告（v8 r2）

## 审查结果
REJECTED

## 发现

### 管线覆盖缺口

- **[一般]** `AbstractCapabilityExecutorTest.java` — `structuredChat().get()` 的 `InterruptedException` 处理路径无测试覆盖。管线明确捕获 `InterruptedException` 并恢复中断标记后走 `doDegrade(TIMEOUT)`（设计 L854-859），但所有测试均未模拟此路径。该缺陷可能导致中断恢复行为在当前或未来变更中退化而无法被发现。

- **[一般]** `AbstractCapabilityExecutorTest.java` — `structuredChat().get()` 的 `ExecutionException` 中 `isKnownPhase4BusinessException()` / `LlmInfrastructureException` 路径无测试覆盖（设计 L907-913）。此路径触发 `doDegrade(INFRASTRUCTURE_ERROR)`，属于定义的错误处理行为，但无任何测试验证。

- **[一般]** `AbstractCapabilityExecutorTest.java` — `structuredChat().get()` 的 `ExecutionException` 中未知异常导致 `throw new CompletionException(cause)` 重新抛出路径无测试覆盖（设计 L915-916）。若该路径被错误地改为静默降级，测试无法捕获。

- **[一般]** `AbstractCapabilityExecutorTest.java` — `chat()` 回退阶段的 `InterruptedException`、`TimeoutException`、`isSuccess()==false` 分支均无测试覆盖（设计 L867-889）。当前仅覆盖了 `parse()` 异常分支，其余 3 条回退阶段错误路径无测试。

- **[一般]** `AbstractCapabilityExecutorTest.java` — `chat()` 回退阶段的 `ExecutionException` 中基础设施错误分支（设计 L890-897）和未知异常重新抛出分支（设计 L898）无测试覆盖。

### 断言缺口

- **[一般]** `AbstractCapabilityExecutorTest.java` — `executeStandardPipelineShouldReturnSuccessWithMetrics()` 验证了 `AiMetricsCollector.record()` 的指标参数，但未验证 `metricsStore.recordSuccess()` 已被调用。`handleSuccess()` 方法（设计 L936）包含此调用，测试未做任何形式的断言确认，若未来该调用被误删除或条件遗漏，测试仍通过。

### DiscussionConclusion 测试

- **[轻微]** `DiscussionConclusionCapabilityExecutorTest.java` — 测试文件实际包含 19 个测试方法（基本标识、refineTimeoutReason 3 条、skipCompression 3 条、estimateTokenCount 3 条、formatTranscripts 1 条、truncateTranscripts 3 条、compress 1 条、压缩成功/失败 2 条），测试报告仅提及 2 个"新增"测试，描述偏保守，但实际覆盖好于报告陈述。

- **[轻微]** `DiscussionConclusionCapabilityExecutorTest.java` — DiscussionConclusion 场景中 `structuredChat()` 可能抛出 `StructuredOutputNotSupportedException` 触发 chat() 回退，但无对应测试覆盖。这是管线标准错误处理路径在 DiscussionConclusion 上下文中的缺失覆盖。

## 修改要求

1. **`AbstractCapabilityExecutorTest.java`** — 为 `structuredChat()` 路径新增 `InterruptedException` 测试：使 `CompletableFuture.get()` 抛出 `InterruptedException`，验证降级为 `TIMEOUT` 且中断标记已恢复。期望：测试应在管线 catch 块中捕获 `InterruptedException` 并使 `result.isDegraded()=true && getFallbackReason()=TIMEOUT.getCode()`。

2. **`AbstractCapabilityExecutorTest.java`** — 为 `structuredChat()` 路径新增基础设施错误测试：使 `ExecutionException.getCause()` 返回 `isKnownPhase4BusinessException()=true` 的异常类型（如 `com.aimedical.modules.diagnosis.TestPhase4Exception`），验证降级为 `INFRASTRUCTURE_ERROR`。

3. **`AbstractCapabilityExecutorTest.java`** — 为 `structuredChat()` 路径新增未知 `ExecutionException` 重新抛出测试：使 `ExecutionException.getCause()` 返回非基础设施、非 `StructuredOutputNotSupportedException` 的普通 `RuntimeException`，验证 `executeStandardPipeline()` 抛出 `CompletionException`（不做降级）。

4. **`AbstractCapabilityExecutorTest.java`** — 为 `chat()` 回退路径新增 `InterruptedException`、`TimeoutException`、`!isSuccess()` 三项分支的独立测试，验证对应降级原因。

5. **`AbstractCapabilityExecutorTest.java`** — 为 `chat()` 回退路径新增基础设施错误和未知异常分支测试，验证降级或重新抛出行为。

6. **`AbstractCapabilityExecutorTest.java`** — 在 `executeStandardPipelineShouldReturnSuccessWithMetrics` 中增加对 `metricsStore.recordSuccess()` 的验证（例如通过自定义 spy 或状态检查），确认 `handleSuccess()` 完整执行了指标记录。

7. **`DiscussionConclusionCapabilityExecutorTest.java`** — 新增 `structuredChat()` 抛出 `StructuredOutputNotSupportedException` 场景下 DiscussionConclusion 走 chat() 回退的测试。
