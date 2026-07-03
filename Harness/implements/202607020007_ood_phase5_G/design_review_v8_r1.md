# 设计审查报告（v8 r1）

## 审查结果
REJECTED

## 发现

### **[严重] `executeStandardPipeline()` 管线未正确处理 `LlmChatService` 的 `AiResult` 包装层**

`LlmChatService.structuredChat()` 返回 `CompletableFuture<AiResult<StructuredChatResult<T>>>`，`chat()` 返回 `CompletableFuture<AiResult<LlmChatResponse>>`。设计稿对各步骤的处理方式未体现出两层解包：`.get()` 先得到 `AiResult<>`，再从中提取 `data` 才是实际结果。

- **结构化路径（步骤 7）**：`get()` 后需先检查 `aiResult.isSuccess()`，再取 `aiResult.getData()` 得 `StructuredChatResult<T>`，再取 `.getData()` 得 `T parsedResult`。设计稿直接将 `.get()` 的返回值视为 `T`，缺少两层 `getData()` 调用。
- **chat 回退路径（步骤 8）**：`rawContent` 需经过 `chatResult.getData().getContent()` 获取，而非 `.get()` 直接取 `getContent()`。
- **shared_success_handler（步骤 10）**：引用的 `parsedResult` 在结构化路径中来源不明确（未定义从何处取得），需在步骤 7 和 8 中明确定义。

**期望**：管线步骤补充 `AiResult` 解包；明确 `parsedResult` 的获取来源（结构化路径 vs. parse 回退路径）。

### **[严重] `doDegrade()` 中 TODO 替换为 `AiCallRecord` 的 `userId` 变量未定义**

设计稿（第 341-346 行）在 `doDegrade()` 方法内引用 `userId`，但 `doDegrade(long startTime, String degradeReason, T request, ...)` 的当前签名不包含 `userId` 参数，该方法内也无同名变量/字段。替换后的代码将无法编译。

**期望**：要么为 `doDegrade()` 添加 `userId` 参数并更新所有调用处，要么在方法内通过 `extractUserId()` 获取（需将 `extractUserId()` 从 `private` 改为 `protected` 或保留 `private` 后直接调用）。

### **[严重] `buildCompressRequest()` 的 `LlmChatOptions` 构造器参数数不匹配**

`com.aimedical.modules.ai.impl.client.LlmChatOptions` 的全参构造器为 7 参数（`modelId, temperature, maxTokens, stopSequences, topP, frequencyPenalty, presencePenalty`）。设计稿第 610 行传入了 7 个值（1 个 String + 6 个 null），参数数量正确。**此处无问题，已核验。**

（更正：此处无问题——`compressionLightweightEndpoint, null, null, null, null, null, null` 共 7 个参数，与构造器匹配。）

### **[严重] `refineTimeoutReason()` 中 `transcriptSummaryElapsedMs` 的获取方式不一致**

设计稿第 521 行代码片段使用了局部变量声明 `long transcriptSummaryElapsedMs = ...;`，但第 616 行说明该值应来自 **实例字段** `private volatile long transcriptSummaryElapsedMs`。代码与说明不一致。

**期望**：代码片段应改为读取字段（`this.transcriptSummaryElapsedMs`），而非声明局部变量。

### **[一般] `ModelEndpointHealthManager.getState()` 签名与任务描述不一致**

任务文件 `task_v8.md` 第 150 行要求新增 `getState(String)`（接收字符串参数），但设计稿第 249 行定义为 `getState(ModelRoute route)`。设计选择 `ModelRoute` 作为参数更合理（健康检查需要知道具体端点），但这是对任务规格的偏离且未注明理由。

**期望**：确认选择 `ModelRoute` 作为参数类型，并说明这是有意的设计细化。

### **[一般] `sentinelReason` 参数在管线中未使用**

`executeStandardPipeline()` 的第 14 个参数是 `sentinelReason`，但管线步骤中从未引用该参数。`doDegrade()` 的最后一个参数也是 `sentinelReason`，但降级路径中同样未使用。设计稿未说明该参数的预期用途或传递路径。

**期望**：明确 `sentinelReason` 在管线中的用途（是传入 `doDegrade()` 还是保留给后续扩展），或在参数说明中标注"保留"。

### **[轻微] `doDegrade()` 中 `metricsCollector.record()` 缺少 `modelId` 获取途径**

设计稿第 341-346 行的 `AiCallRecord` 构造中传入了 `modelId`、`promptVersion`、`outputSummary`，但这些变量在 `doDegrade()` 中作为参数存在（当前签名已有），**此处无问题。**

（更正：`modelId`、`promptVersion`、`outputSummary` 均已在 `doDegrade()` 参数列表中，构造调用正确。）

### **[轻微] 管线步骤 2 `modelRouter.route()` 返回类型在异常路径下的 null 安全处理未完整展开**

设计稿第 289 行：`routeResult = null` 后，步骤 3 的 null-safe 检查涵盖了 `routeResult` 为 null 的情况，但步骤 4-5 中未显式描述 `routeResult` 为 null 时 `ModelRoute` 字段提取的逻辑（仅说"使用默认值"）。建议在步骤 4 中补充：`routeResult` 为 null 时直接使用 `new LlmChatOptions()`。
