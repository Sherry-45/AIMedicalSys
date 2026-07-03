# 设计审查报告（v8 r4）

## 审查结果
REJECTED

## 发现

- **[严重]** `refineTimeoutReason()`（第 497-499 行）返回 `DegradationReason.TIMEOUT + ":transcriptSummaryCrowding"`。Java 中 `enum + String` 是字符串拼接，结果为 `String`，但父类方法签名（`AbstractCapabilityExecutor.java:310`）声明返回 `DegradationReason`，无法编译。该问题自 r1 以来持续存在。

- **[严重]** `executeStandardPipeline()` 第 644-646 行对 `StructuredChatResult` 的拆包错误：`result.getData()` 返回 `StructuredChatResult<R>` 而非 `R`，需要 `result.getData().getData()` 才能获取实际结果。且 `result.getUsage()` 不存在于 `AiResult` 类（`AiResult.java` 无 `getUsage()`），应改为 `result.getData().getUsage()`。

- **[严重]** `DiscussionConclusionCapabilityExecutor.doExecuteInternal()` 第 466 行引用 `TOKEN_THRESHOLD` 常量，但该类中未定义该常量。

- **[严重]** `refineTimeoutReason()` 第 495 行调用 `extractTranscriptSummaryElapsed(request)` 但该方法未定义。

- **[严重]** `compressTranscripts()` 第 545 行调用 `buildCompressionRequest(formatted)` 但该方法未定义。

- **[严重]** `DiscussionConclusionCapabilityExecutor` 第 474 行使用 `log.warn(...)` 但 `log` 是父类 `AbstractCapabilityExecutor` 的 `private static final` 字段（第 37 行），子类不可见，编译报错。

- **[严重]** 前置压缩阶段（第 462-479 行）计算的 `compressed`（成功）和截断后 `transcripts`（失败时局部变量重赋值）均未写回 `request` 或以任何方式传递给 `executeStandardPipeline()`。第 486 行仍传入原始 `request`，使得压缩/截断逻辑无实际效果。

- **[一般]** 构造器参数数量前后矛盾：第 403 行写"21 个（父类 17 + 特有 4）"，但第 717 行写"18+4=22 参数"。新增 `outputType`（第 18 参）后父类应为 18 参，21 和 22 均不正确。

- **[一般]** `DiscussionConclusionCapabilityExecutor` 注入的 `compressionLightweightEndpoint` 和 `compressionLightweightClientType` 在 `compressTranscripts()`（第 541-558 行）中未使用，该方法仅调用 `llmChatService.chat()`。

- **[一般]** `sentinelReason`（`executeStandardPipeline()` 第 14 参数）在管线步骤中未被引用，无使用路径。

- **[轻微]** 管线步骤 7 和 chat 回退路径中未检查 `AiResult.isSuccess()`，若 `LlmChatService` 返回 failure 状态的 `AiResult`（无异常抛出），后续 `getData()` 将返回 null 导致 NPE。

## 修改要求（仅 REJECTED 时）

1. **[严重]** `refineTimeoutReason()` 要么改为返回 `String` 并同步修改父类 / `execute()` 调用处，要么将拼接逻辑移至 `execute()` 的 `exceptionally` 处理器中，保持返回 `DegradationReason`。

2. **[严重]** Pipeline 中修正 `StructuredChatResult` 拆包：`result.getData()` → `result.getData().getData()`（`R`），usage 通过 `result.getData().getUsage()` 获取。

3. **[严重]** `DiscussionConclusionCapabilityExecutor` 中补充以下缺失元素：`TOKEN_THRESHOLD` 常量定义、`extractTranscriptSummaryElapsed()` 方法定义、`buildCompressionRequest()` 方法定义、`log` 字段声明。

4. **[严重]** 前置压缩阶段的结果需写回 `request`（若 `request` 允许修改）或构造新的 `request` 传入管线。当前设计未实现压缩的实际效果，需补全。

5. **[一般]** 统一 DiscussionConclusion 构造器参数计数方式，消除前后矛盾。

6. **[一般]** 明确 `compressionLightweightEndpoint` 和 `compressionLightweightClientType` 在压缩请求中的使用路径，或在 `buildCompressionRequest()` 中引用。
