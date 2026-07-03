# 计划审查报告（v8 r2）

## 审查结果
REJECTED

## 发现

### **[严重]** AbstractCapabilityExecutor 构造器参数数量不匹配（计划 16，实际 17）

`task_v8.md` 第 85 行明确要求"接收 **16 参数**并 super(...) 传递"，但 `AbstractCapabilityExecutor.java:67-84` 实际的构造器签名有 **17 个参数**（比计划多一个 `ObjectMapper objectMapper` 参数）。7 个子类构造器若按计划写 16 个参数调用 `super(...)` 将导致编译错误。

同样，`DiscussionConclusionCapabilityExecutor` 的"共 19 参数（父类 16 + 特有 3）"应为 17+3=**20 参数**。

**期望修正方向**：将计划中所有"16 参数"修正为「17 参数（含 ObjectMapper）」，同步更新 DiscussionConclusion 的参数计数（17+3=20），确保实现者按正确签名编写构造器。

### **[严重]** `executeStandardPipeline()` 中 CompletableFuture 阻塞语义未指定

`executeStandardPipeline()` 是同步方法（返回 `AiResult<R>`），但管线伪代码步骤 7-8 调用 `llmChatService.structuredChat()` / `llmChatService.chat()`，二者均返回 `CompletableFuture<AiResult<...>>`。计划未指定如何将异步结果解析为同步值（`.get(timeout, TimeUnit)` / `.join()`），也未说明：

1. `InterruptedException` / `ExecutionException` / `CancellationException` 的处理策略
2. 嵌套超时语义：`execute()` 已通过 `.orTimeout()` 施加整体超时，管线内部又按 60/40 分配分段超时——内外两层超时的竞态条件未澄清（外层先触发 vs 内层先触发）
3. 当 stub 返回的 `CompletableFuture` 被调用时立即抛出异常（而非返回 incomplete future），管线异常捕获是否正确

**期望修正方向**：在管线伪代码中明确使用 `.get(remainingTimeout, TimeUnit)` 阻塞获取，补充所有受检异常的处理路径，并澄清外层 `orTimeout` 与内部分配超时的关系（建议：内部分配的 60% 超时应基于外层 `resolveTimeout()` 减去已消耗时间计算，外层 `orTimeout` 作为最后兜底）。

### **[一般]** `promptVersion` / `sentinelReason` 的来源未按执行器逐一定义

计划由各 `doExecuteInternal()` 在调用 `executeStandardPipeline()` 前确定这两个值，但 7 个底座执行器没有任何一个说明如何获取 `promptVersion`（来自固定配置？来自 `@Value` 属性？来自 YAML？）和 `sentinelReason`（来源是什么？）。实现者缺乏指导方向。

**期望修正方向**：在计划中为每个执行器（至少按模式分组）明确 `promptVersion`（如"使用 capabilityId 作为默认 promptVersion"，或"从配置 `ai.prompt.version.{capabilityId}` 注入"）和 `sentinelReason`（如"固定 null"或其他来源）。

### **[一般]** `doDegrade()` 中的 TODO 未纳入本任务范围

`AbstractCapabilityExecutor.java:258` 存在待办注释：
```java
// TODO: record AiCallRecord when AiMetricsCollector methods are defined
```
本任务恰好为 `AiMetricsCollector` 添加了 `record()` 方法签名，但计划未提及需同步更新 `doDegrade()` 中的 TODO，导致这个遗留在后续轮次仍不会被清理，形成技术债。

**期望修正方向**：在涉及文件清单中追加 `AbstractCapabilityExecutor.java` 的修改操作，将 `doDegrade()` 中的 TODO 替换为对 `metricsCollector.record()` 的实际调用。

### **[轻微]** DiscussionConclusion "多 2 参数" 表述与实际数量不一致

第 101 行写"构造器多 2 参数"，但紧接着列出 3 个参数（`compressionLightweightEndpoint`、`compressionLightweightClientType`、`transcriptSummaryTimeout`），总数应为"多 3 参数"。

**期望修正方向**：将"多 2 参数"改为"多 3 参数"。

### **[轻微]** 管线步骤 6 的 60/40 分配未考虑 `parseTimeoutConfig` 已有配置

`AbstractCapabilityExecutor` 已有 `parseTimeoutConfig: Map<String, Duration>` 和 `parseTimeoutDefault: Duration` 构造器参数（第 59-60 行），表明结构化解析的超时并非固定百分比分配，而是有独立配置。计划中的伪代码第 6 步若按固定 60/40 实现，将与现有设计冲突。

**期望修正方向**：管线超时分配至少应提及可选的 parseTimeout 配置覆盖机制，而非完全硬编码 60/40 比例。

## 修改要求

对上述 **[严重]** 和 **[一般]** 问题（共 5 项），需对应修正计划后方可推进。其中参数计数不匹配和 CompletableFuture 阻塞语义缺失是必须解决的阻塞性问题。
