# 任务指令（v8 r2）

## 动作
RETRY

## 任务描述
在 `ai-impl/orchestrator/` 包中实现 `executeStandardPipeline()` 管线方法，并在 `orchestrator/impl/` 子包中创建 7 项底座能力 CapabilityExecutor 具体实现类，每个继承 `AbstractCapabilityExecutor<T,R>` 并完成 `doExecuteInternal()` 管线实现。作为前置条件，同步给 5 个存根类型（`PromptTemplateManager`、`ModelRouter`、`ModelEndpointHealthManager`、`StructuredOutputParser`、`AiMetricsCollector`）添加管线所需的最小方法签名，确保编译通过。同时清理 `AbstractCapabilityExecutor.doDegrade()` 中的 TODO 遗留。

## 选择理由
Task 6 是 Batch2 P0 核心业务执行器，所有前置依赖均已就绪。7 项底座能力各自对应 `AiService` 接口的一个方法，是 `AiOrchestrator` 路由分发的最终执行单元。实现 `executeStandardPipeline()` 可将标准管线逻辑集中一处避免 7 份重复代码。

## 任务上下文

### AbstractCapabilityExecutor 构造器参数（17 个）

当前 `AbstractCapabilityExecutor.java:67-84` 的实际构造器签名为 **17 个参数**（含 `ObjectMapper objectMapper`）：

| # | 参数 | 类型 |
|---|------|------|
| 1 | `inputType` | `Class<T>` |
| 2 | `promptTemplateManager` | `PromptTemplateManager` |
| 3 | `modelRouter` | `ModelRouter` |
| 4 | `llmChatService` | `LlmChatService` |
| 5 | `structuredOutputParser` | `StructuredOutputParser` |
| 6 | `metricsCollector` | `AiMetricsCollector` |
| 7 | `metricsStore` | `SlidingWindowMetricsStore` |
| 8 | `endpointHealthManager` | `ModelEndpointHealthManager` |
| 9 | `degradationStrategyMapRef` | `AtomicReference<Map<String, List<DegradationStrategy>>>` |
| 10 | `localRuleFallback` | `LocalRuleFallback<T, R>` |
| 11 | `capabilityTimeoutConfig` | `Map<String, Duration>` |
| 12 | `parseTimeoutConfig` | `Map<String, Duration>` |
| 13 | `parseTimeoutDefault` | `Duration` |
| 14 | `thinAdapterTimeout` | `Duration` |
| 15 | `thinAdapterPerCapabilityConfig` | `Map<String, Duration>` |
| 16 | `llmCallExecutor` | `Executor` |
| 17 | `objectMapper` | `ObjectMapper` |

**所有 7 个底座执行器构造器必须使用 17 参数调用 `super(...)`。**
`DiscussionConclusionCapabilityExecutor` 额外多 3 个特有参数，共 **20 参数**（17 + 3）。

### AbstractCapabilityExecutor.executeStandardPipeline() 实现

当前 `executeStandardPipeline()` 抛出 `UnsupportedOperationException`。需实现以下管线伪代码：

```java
protected final AiResult<R> executeStandardPipeline(
    long startTime, T request, String capabilityId,
    String departmentId, String userId, String sessionId,
    String callerRole, String callerId, String visitId, String patientId,
    String inputSummary, Map<String, Object> variables,
    String promptVersion, String sentinelReason
) {
    // 1. 构建 templateKey（capabilityId + ":" + promptVersion），模板渲染（try-catch，异常时回退到 promptVersion 默认值）
    //    promptTemplateManager.render(templateKey, variables)
    //    注意：render() 返回 String，null-safe 检查
    //
    // 2. 模型路由：modelRouter.route(capabilityId, request)，返回 Object，null-safe 检查
    //
    // 3. 健康检查：endpointHealthManager.getState(routeResult)，null-safe（若返回非 null 且非 HEALTHY 状态，回调降级）
    //
    // 4. 构造 LlmChatOptions（从路由结果提取参数，无模式下使用默认值）
    //    responseFormat = structuredOutputClass（根据 inputType 自动判断）
    //
    // 5. 构造 LlmChatRequest（SYSTEM=renderedPrompt, USER=request.toString()）
    //
    // 6. 计算剩余超时窗口：
    //    - 优先使用 parseTimeoutConfig.get(capabilityId)，若存在则用此值作为解析超时
    //    - 否则使用 parseTimeoutDefault（若存在）作为解析超时
    //    - 否则按 60%/40% 分配 structuredChat/chatFallback
    //    - 总超时 = resolveTimeout(capabilityId) - 已消耗时间
    //    - 外层 execute() 的 orTimeout 作为最后兜底（内外双层保护：内层为分配超时，外层为总超时）
    //
    // 7. structuredChat() 优先调用（使用 LlmChatRequest，附带解析超时）
    //    - 使用 future.get(remainingTimeout, TimeUnit.MILLISECONDS) 阻塞等待
    //    - 成功则走 shared_success_handler
    //    - 捕获 TimeoutException → doDegrade(TIMEOUT)
    //    - 捕获 InterruptedException → 恢复中断，doDegrade(TIMEOUT)
    //    - 捕获 ExecutionException → 检查 cause 类型
    //
    // 8. 若 StructuredOutputNotSupportedException:
    //    - 调用 chat() 回退
    //    - 使用 chatFallbackTimeout.get(remaining, TimeUnit) 阻塞等待
    //    - 成功后通过 structuredOutputParser.parse(rawContent, targetClass) 解析
    //    - 解析失败 → doDegrade(PARSE_FAILURE)
    //
    // 9. LlmInfrastructureException / 其他 ExecutionException → doDegrade(INFRASTRUCTURE_ERROR)
    //
    // 10. shared_success_handler:
    //     metricsCollector.record(callRecord) + metricsStore.recordSuccess(capabilityId, elapsedMs)
    //     + AiResult.success(parsedResult)
    //
    // 11. 解析失败 → doDegrade(PARSE_FAILURE)
}
```

**关键阻塞语义澄清：**
- 所有 `CompletableFuture` 结果使用 `.get(remainingTimeout, TimeUnit.MILLISECONDS)` 同步阻塞获取
- `get()` 的 3 种受检异常：`TimeoutException` → 降级、`InterruptedException` → 恢复中断标记后降级、`ExecutionException` → 检查 cause 类型
- 外层 `orTimeout`（见 `AbstractCapabilityExecutor.execute()` 第 149 行）作为总超时兜底，内部分配超时基于 `resolveTimeout()` 减去管线前置步骤已消耗时间计算，内外双层保护

所有 stub 调用的 null 返回值使用 null-safe 检查（`if (xxx != null) xxx.method()`），避免 NPE。

### 每个 CapabilityExecutor 实现要求

每个执行器：
1. 标注 `@Service("capabilityId")`，与 YAML 配置中 `degradation.strategies` 的 key 一致
2. `getCapabilityId()` 返回能力标识常量（全大写）
3. `getInputType()` 返回其 `Request` DTO 的 `Class`
4. `getOutputType()` 返回其 `Response` DTO 的 `Class`
5. 构造器标注 `@Autowired`，接收 **17 参数**并 `super(...)` 传递
6. `doExecuteInternal()` 调用 `executeStandardPipeline()`

| 能力 | 输入 DTO | 输出 DTO | 降级策略 | YAML modelId | promptVersion 来源 | sentinelReason |
|------|---------|---------|---------|-------------|-------------------|---------------|
| TRIAGE | `TriageRequest` | `TriageResponse` | `[timeout, circuit-breaker]` | qwen-plus | `@Value("${ai.prompt.version.TRIAGE:TRIAGE}")`注入 | 固定 null |
| RX_AUDIT | `PrescriptionCheckRequest` | `PrescriptionCheckResponse` | `[timeout, noop]` | qwen-turbo | `@Value("${ai.prompt.version.RX_AUDIT:RX_AUDIT}")`注入 | 固定 null |
| MEDICAL_RECORD_GEN | `MedicalRecordGenRequest` | `MedicalRecordGenResponse` | `[timeout, circuit-breaker]` | qwen-plus | `@Value("${ai.prompt.version.MEDICAL_RECORD_GEN:MEDICAL_RECORD_GEN}")`注入 | 固定 null |
| RX_ASSIST | `PrescriptionAssistRequest` | `PrescriptionAssistResponse` | `[timeout, circuit-breaker]` | qwen-plus | `@Value("${ai.prompt.version.RX_ASSIST:RX_ASSIST}")`注入 | 固定 null |
| KB_QUERY | `KbQueryRequest` | `KbQueryResponse` | `[timeout, circuit-breaker]` | qwen-plus | `@Value("${ai.prompt.version.KB_QUERY:KB_QUERY}")`注入 | 固定 null |
| SCHEDULE | `ScheduleRequest` | `ScheduleResponse` | `[timeout, noop]` | qwen-plus | `@Value("${ai.prompt.version.SCHEDULE:SCHEDULE}")`注入 | 固定 null |
| DISCUSSION_CONCLUSION | `DiscussionConclusionRequest` | `DiscussionConclusionResponse` | `[timeout, circuit-breaker]` | qwen-max | `@Value("${ai.prompt.version.DISCUSSION_CONCLUSION:DISCUSSION_CONCLUSION}")`注入 | `@Value("${ai.sentinel.reason.discussion-conclusion:}")`注入，可为空字串 |

所有 DTO 引用路径：`com.aimedical.modules.ai.api.dto.{capability}.*Request/*Response`

### DiscussionConclusionCapabilityExecutor 特有行为

1. **构造器多 3 参数**：`compressionLightweightEndpoint`, `compressionLightweightClientType`, `transcriptSummaryTimeout`，共 **20 参数**（父类 17 + 特有 3）
2. **特有字段**：
   - `compressionLightweightEndpoint: String` (`@Value("${ai.compression.lightweight-endpoint:compress-default}")`)
   - `compressionLightweightClientType: ClientType` (`@Value("${ai.compression.lightweight-client-type:HTTP_API}")`)
   - `transcriptSummaryTimeout: Duration` (`@Value("${ai.execution.timeout.transcript-summary:15s}")`)
3. **前置压缩阶段**（在 `doExecuteInternal()` 中调用 `executeStandardPipeline()` 之前执行）：
   - 使用 `preciseTokenCount(transcripts)` 估算 Token 数
   - 若超出阈值，提交 `CompletableFuture` 到独立线程池（暂用 `llmCallExecutor` 回退），调用 `LlmChatService.chat()` 做摘要压缩
   - 超时/失败时回退到 `truncateTranscripts(transcripts, 2000)`
4. **重写 `refineTimeoutReason()`**：检查 `elapsedInDoExecuteInternal - transcriptSummaryElapsedMs < capabilityTimeout * 0.2` 时返回 `DegradationReason.TIMEOUT + ":transcriptSummaryCrowding"`，否则 `DegradationReason.TIMEOUT + ":primaryLlmTimeout"`
5. **辅助方法**（private，本类实现）：
   - `preciseTokenCount(List<DiscussionTranscript>)`: 字符估算（jtokkit 不可用时）
   - `formatTranscripts(List<DiscussionTranscript>)`: 格式化为 `[{speakerRole}] {speakerName} ({timestamp}):\n{content}\n`
   - `truncateTranscripts(List<DiscussionTranscript>, int maxTokens)`: 末尾截断至 maxTokens

### 涉及文件

#### 修改（7 个）
| 文件路径 | 操作 | 说明 |
|---------|------|------|
| `ai-impl/src/main/java/.../orchestrator/AbstractCapabilityExecutor.java` | 修改 | 实现 `executeStandardPipeline()` 方法体；同时在 `doDegrade()` 中将 TODO 替换为 `metricsCollector.record()` 实际调用 |
| `ai-impl/src/main/java/.../template/PromptTemplateManager.java` | 修改 | 新增 `render(String, Map)` 方法签名 |
| `ai-impl/src/main/java/.../router/ModelRouter.java` | 修改 | 新增 `route(String, Object)` 方法签名 |
| `ai-impl/src/main/java/.../metrics/ModelEndpointHealthManager.java` | 修改 | 新增 `getState(String)` 方法 |
| `ai-impl/src/main/java/.../parser/StructuredOutputParser.java` | 修改 | 新增 `<T> T parse(String, Class<T>)` 方法签名 |
| `ai-impl/src/main/java/.../metrics/AiMetricsCollector.java` | 修改 | 新增 `record(Object)` 方法签名 |
| `ai-impl/src/main/java/.../fallback/LocalRuleFallback.java` | 核查 | 确认已有 `fallback(T)` 方法签名（早期存根创建时已包含） |

#### 新建（7 个）
| 文件路径 | 包 | CapabilityId |
|---------|---|-------------|
| `ai-impl/src/main/java/.../orchestrator/impl/TriageCapabilityExecutor.java` | `com.aimedical.modules.ai.impl.orchestrator.impl` | `TRIAGE` |
| `ai-impl/src/main/java/.../orchestrator/impl/PrescriptionCheckCapabilityExecutor.java` | 同上 | `RX_AUDIT` |
| `ai-impl/src/main/java/.../orchestrator/impl/MedicalRecordGenCapabilityExecutor.java` | 同上 | `MEDICAL_RECORD_GEN` |
| `ai-impl/src/main/java/.../orchestrator/impl/PrescriptionAssistCapabilityExecutor.java` | 同上 | `RX_ASSIST` |
| `ai-impl/src/main/java/.../orchestrator/impl/KbQueryCapabilityExecutor.java` | 同上 | `KB_QUERY` |
| `ai-impl/src/main/java/.../orchestrator/impl/ScheduleCapabilityExecutor.java` | 同上 | `SCHEDULE` |
| `ai-impl/src/main/java/.../orchestrator/impl/DiscussionConclusionCapabilityExecutor.java` | 同上 | `DISCUSSION_CONCLUSION` |

#### 测试（8 个）
| 文件路径 | 覆盖内容 |
|---------|---------|
| `ai-impl/src/test/java/.../orchestrator/impl/executeStandardPipelineTest.java` | 标准管线核心步骤：模板渲染→模型路由→LLM structuredChat→结构化解析→指标采集完整路径；验证 3 条降级路径（TimeoutException→degrade、StructuredOutputNotSupportedException→chat+parse 回退、LlmInfrastructureException→degrade）；验证 null route 安全 |
| `ai-impl/src/test/java/.../orchestrator/impl/TriageCapabilityExecutorTest.java` | 分诊执行器基本契约（构造/Id/类型/委托管线） |
| `ai-impl/src/test/java/.../orchestrator/impl/PrescriptionCheckCapabilityExecutorTest.java` | 处方审核执行器基本契约 |
| `ai-impl/src/test/java/.../orchestrator/impl/MedicalRecordGenCapabilityExecutorTest.java` | 病历生成执行器基本契约 |
| `ai-impl/src/test/java/.../orchestrator/impl/PrescriptionAssistCapabilityExecutorTest.java` | 辅助开方执行器基本契约 |
| `ai-impl/src/test/java/.../orchestrator/impl/KbQueryCapabilityExecutorTest.java` | 知识库问答执行器基本契约 |
| `ai-impl/src/test/java/.../orchestrator/impl/ScheduleCapabilityExecutorTest.java` | 排班执行器基本契约 |
| `ai-impl/src/test/java/.../orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | 讨论结论执行器基本契约 + 特有压缩配置 + refineTimeoutReason |

### 已有代码上下文

- `AbstractCapabilityExecutor.java`（完整实现含 17 参构造器、execute() 模板方法、doDegrade()、checkPreDegradation()、防御性拷贝等）
- `CapabilityExecutor.java`（泛型接口：execute/getCapabilityId/getInputType/getOutputType）
- `AiOrchestrator.java`（通过 `List<CapabilityExecutor>` 自动注入构建 executorMap，按 capabilityId 路由）
- 7 个存根类型位于各自子包（PromptTemplateManager、ModelRouter、LlmChatService、StructuredOutputParser、AiMetricsCollector、ModelEndpointHealthManager、LocalRuleFallback），**其中 5 个将在本任务中添加最小方法签名**（见上方涉及文件）
- LLM DTO 全部就绪（Task 7 完成，ai-impl/client/ 包下 9 个类型 + LlmChatService 方法签名）
- 实验分流（ExperimentManager）不在管线内处理，由各执行器 `doExecuteInternal()` 在调用 `executeStandardPipeline()` 前确定 `promptVersion`/`sentinelReason` 值

### doDegrade() TODO 清理

当前 `AbstractCapabilityExecutor.java:258` 存在：
```java
// TODO: record AiCallRecord when AiMetricsCollector methods are defined
```
需替换为实际调用：
```java
if (metricsCollector != null) {
    metricsCollector.record(new AiCallRecord(...));
}
```
其中 `AiCallRecord` 可使用已存在的 `com.aimedical.modules.ai.impl.metrics.AiCallRecord`（如不存在则在 metrics 包创建最小 DTO）。

### 测试要求

1. `executeStandardPipelineTest`：
   - Mock 全部 7 个 stub 依赖 + SlidingWindowMetricsStore
   - 验证全路径：模板渲染 → 模型路由 → structuredChat → 解析 → 指标采集
   - 验证 3 条降级路径：TimeoutException → degrade、StructuredOutputNotSupportedException → chat+parse 回退、LlmInfrastructureException → degrade
   - 验证 null route（modelRouter.route() 返回 null）
   - 验证 `.get()` 的 InterruptedException 恢复行为

2. 各 `XxxCapabilityExecutorTest`：
   - 构造器注入验证（通过 `@Service` 名称）
   - `getCapabilityId()` 返回值
   - `getInputType()` / `getOutputType()` 正确类型
   - `doExecuteInternal()` 委托至 `executeStandardPipeline()`

3. `DiscussionConclusionCapabilityExecutorTest`：
   - 特有构造器参数注入（compressionLightweightEndpoint 等）
   - `refineTimeoutReason()` 超时原因细化逻辑

### 包名统一规则
所有新类包路径：`com.aimedical.modules.ai.impl.orchestrator.impl`
均使用 `@Service` 注解注册为 Spring Bean，方便 `AiOrchestrator` 自动注入。
不使用 Lombok，所有构造器/Getter/Setter 手写。

## 硬性约束
- 不修改已完成的 CapabilityExecutor.java 接口
- 不修改已完成的 AiOrchestrator.java（仅需确保新实现按 getCapabilityId() 自动注册）
- 不修改已完成的 AiService 接口
- 所有 `@SuppressWarnings("unchecked")` 仅标注在实现类方法上，不标注在接口
- 所有 `AiResult` 引用通过 `com.aimedical.modules.ai.api.AiResult` 导入

## 修订说明（v8 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] AbstractCapabilityExecutor 构造器参数数量不匹配（计划 16，实际 17） | 所有"16 参数"更正为"17 参数"（含 ObjectMapper）；DiscussionConclusion 参数计数更正为 20（17+3） |
| [严重] executeStandardPipeline() 中 CompletableFuture 阻塞语义未指定 | 明确使用 `.get(remainingTimeout, TimeUnit)` 阻塞获取；补充所有受检异常（TimeoutException/InterruptedException/ExecutionException）的处理路径；澄清外层 orTimeout 为兜底、内部分配超时基于 resolveTimeout 减去已消耗时间计算 |
| [一般] promptVersion/sentinelReason 来源未按执行器逐一定义 | 为每个执行器在表格中新增 promptVersion 来源列（@Value 注入 + 默认值）和 sentinelReason 来源列 |
| [一般] doDegrade() 中的 TODO 未纳入本任务范围 | 将清理 TODO 纳入涉及文件清单，要求在 doDegrade() 中将 TODO 替换为 metricsCollector.record() 实际调用 |
| [轻微] DiscussionConclusion "多 2 参数" 表述与实际数量不一致 | "多 2 参数" → "多 3 参数" |
| [轻微] 管线步骤 6 的 60/40 分配未考虑 parseTimeoutConfig 已有配置 | 优先使用 parseTimeoutConfig 和 parseTimeoutDefault，仅当两者均不可用时才回退到 60/40 固定比例 |
