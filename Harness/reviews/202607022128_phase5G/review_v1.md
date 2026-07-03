# R1: ai-api 模块 + ai-impl/orchestrator/ 编排层

审查时间：2026-07-02T22:28

### 审查范围

- `ai-api/src/main/java/com/aimedical/modules/ai/api/AiService.java`
- `ai-api/src/main/java/com/aimedical/modules/ai/api/AiResult.java`
- `ai-api/src/main/java/com/aimedical/modules/ai/api/degradation/DegradationContext.java`
- `ai-api/src/main/java/com/aimedical/modules/ai/api/degradation/DegradationReason.java`
- `ai-api/src/main/java/com/aimedical/modules/ai/api/degradation/DegradationStrategy.java`
- `ai-api/src/main/java/com/aimedical/modules/ai/api/dto/base/AiRequestBase.java`
- `ai-api/src/main/java/com/aimedical/modules/ai/api/dto/base/CallContext.java`
- `ai-api/src/main/java/com/aimedical/modules/ai/api/dto/base/Phase4ServiceMeta.java`
- `ai-api/src/main/java/com/aimedical/modules/ai/api/dto/base/Phase4ServiceMetaCapable.java`
- `ai-api/src/main/java/com/aimedical/modules/ai/api/dto/base/Phase4BusinessException.java`
- `ai-api/src/main/java/com/aimedical/modules/ai/api/dto/discussion/DiscussionConclusionRequest.java`
- `ai-api/src/main/java/com/aimedical/modules/ai/api/dto/discussion/DiscussionConclusionResponse.java`
- `ai-api/src/main/java/com/aimedical/modules/ai/api/dto/discussion/DiscussionTranscript.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/AiOrchestrator.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/CapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/TriageCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/PrescriptionCheckCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/MedicalRecordGenCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/PrescriptionAssistCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/KbQueryCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/ScheduleCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiagnosisCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/AnalysisReportForInspectionCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/AnalysisReportForLabTestCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/ImageAnalysisCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/RecommendExaminationCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/RecommendExecutionOrderCapabilityExecutor.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/util/RequestContextUtils.java`
- `ai-impl/src/main/java/com/aimedical/modules/ai/impl/metrics/AiCallRecord.java`

### 发现

#### [严重] 薄适配器 isDtoEmpty() 判定逻辑错误——所有请求均被误判为空 DTO 导致永远降级

- **位置**：`ai-impl/orchestrator/impl/DiagnosisCapabilityExecutor.java:167-170`（6 个薄适配器均存在相同问题）
- **描述**：`isDtoEmpty()` 方法通过检查 `request.getClass().getPackage().getName().startsWith("com.aimedical.modules.ai.api.dto.")` 来判定 DTO 是否为空。然而所有 Phase 4 请求 DTO（`DiagnosisRequest`、`InspectionReportRequest` 等）的包名恰好就是 `com.aimedical.modules.ai.api.dto.*`，因此此条件**永远返回 true**，导致所有薄适配器请求均直接走降级路径（`INFRASTRUCTURE_ERROR:Phase4DtoEmpty`），Phase 4 服务委托调用永远不会被执行。这与设计文档 §3.1 的意图完全相反——设计文档明确说明 7 项底座能力在切流初期即完整可用，6 项薄适配器在 Phase 4 DTO 补齐前才走降级。
- **建议**：`isDtoEmpty()` 应检测 DTO 是否为"已知空类"（无业务字段），而非检测包路径。正确实现应通过反射检查 DTO 是否除默认构造器外无其他业务字段（或维护一个已知空 DTO 类名注册表），与设计文档 §3.1 中"通过 `className` 或注册表判定"的约定一致。

#### [严重] 薄适配器使用反射调用 Phase 4 服务而非直接类型引用——丧失编译期类型安全

- **位置**：`ai-impl/orchestrator/impl/DiagnosisCapabilityExecutor.java:107-123`（6 个薄适配器均存在相同问题）
- **描述**：设计文档 §3.1 明确要求薄适配器通过构造器注入对应的 Phase 4 业务服务接口（如 `DiagnosisService`），并直接调用 `diagnosisService.execute(request)`。但实现中：(1) 服务引用类型为 `Object`（`private final Object diagnosisService`），而非具体的 Phase 4 服务接口；(2) 通过反射 `service.getClass().getMethod("execute", requestClass).invoke(service, request)` 调用，丧失了编译期类型检查。反射调用在方法签名变更时不会产生编译错误，运行时将抛出 `NoSuchMethodException`，且 `inputType` 为 null 时 `requestClass` 回退到 `request.getClass().getSuperclass()`（对未继承基类的 DTO 将得到 `Object.class`），进一步增加方法查找失败风险。
- **建议**：按设计文档要求，薄适配器应声明具体 Phase 4 服务接口类型的构造器参数（如 `DiagnosisService diagnosisService`），直接调用 `diagnosisService.execute(request)`。Maven `provided` 作用域确保编译期类型可用、运行时由容器提供实现。

#### [严重] AbstractCapabilityExecutor.execute() 的 exceptionally() 中 Phase4BusinessException 误走降级路径

- **位置**：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:186-189`
- **描述**：`execute()` 方法的 `exceptionally()` 回调中，当 `isKnownPhase4BusinessException(cause)` 为 true 时，代码走 `doDegrade()` 降级路径并使用 `DegradationReason.INTERNAL_ERROR`。但根据设计文档 §3.1 薄适配器伪代码，Phase 4 业务异常应返回 `AiResult.failure()`（非降级），而非走降级路径。业务异常（如参数校验失败、数据不存在）语义上不是降级场景，不应消耗降级策略的配额或触发降级指标记录。此处 `isKnownPhase4BusinessException` 在 `execute()` 的 `exceptionally()` 中被调用也不合理——该回调处理的是 `supplyAsync` lambda 抛出的异常，而底座完整管线的 `doExecuteInternal()` 不应抛出 Phase 4 业务异常（那是薄适配器特有的异常类型）。
- **建议**：移除 `execute().exceptionally()` 中的 `isKnownPhase4BusinessException` 分支。Phase 4 业务异常的处理应仅在薄适配器的 `doExecuteInternal()` 中进行（当前薄适配器实现已正确处理）。`execute().exceptionally()` 应仅处理 `TimeoutException` 和其他未预期异常。

#### [严重] AbstractCapabilityExecutor.executeStandardPipeline() 中 LlmInfrastructureException 检测使用字符串匹配而非 instanceof

- **位置**：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:465,477`
- **描述**：`executeStandardPipeline()` 的 `ExecutionException` catch 块中，对 `LlmInfrastructureException` 的检测使用 `cause.getClass().getName().contains("LlmInfrastructureException")` 字符串匹配，而非 `cause instanceof LlmInfrastructureException`。`LlmInfrastructureException` 已在 `ai-impl/client/exception/` 包中定义且已 import，应使用类型安全的 `instanceof` 检测。字符串匹配在类重命名时静默失效，且可能误匹配其他包含相同子串的异常类名。
- **建议**：将 `cause.getClass().getName().contains("LlmInfrastructureException")` 替换为 `cause instanceof LlmInfrastructureException`，与同文件中对 `StructuredOutputNotSupportedException` 的 `instanceof` 检测方式保持一致。

#### [严重] AbstractCapabilityExecutor.executeStandardPipeline() 缺少对 LlmInfrastructureException 的独立 catch 分支

- **位置**：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:436-484`
- **描述**：设计文档 §3.2 明确要求 `CapabilityExecutor.doExecuteInternal()` 中对 `StructuredOutputNotSupportedException` 和 `LlmInfrastructureException` 使用两个独立 catch 分支处理：前者触发 chat() 回退路径，后者直接降级不尝试回退。但当前实现将两者合并到同一个 `ExecutionException` catch 块中，通过 `if-else` 链判断。更关键的是，`structuredChat()` 的 `ExecutionException` catch 块中，`LlmInfrastructureException` 的检测位于 `StructuredOutputNotSupportedException` 的 `if` 分支内部（第 465 行），这意味着只有当 `structuredChat()` 抛出 `StructuredOutputNotSupportedException` 后回退到 `chat()` 再次失败时，`LlmInfrastructureException` 才会被检测到。而 `structuredChat()` 直接抛出 `LlmInfrastructureException` 的场景（HTTP 5xx、连接超时等）走的是第 476-480 行的 else-if 分支，虽然也走了降级路径，但逻辑结构不清晰，且与设计文档的双 catch 分支约定不一致。
- **建议**：重构 `executeStandardPipeline()` 的异常处理，将 `structuredChat()` 调用包裹在独立的 try-catch 中，对 `StructuredOutputNotSupportedException` 和 `LlmInfrastructureException` 使用两个独立 catch 分支，与设计文档 §3.2 的异常分类契约一致。

#### [一般] 薄适配器目录结构与设计文档不一致——应归属 thin-adapter/ 子包而非 orchestrator/impl/

- **位置**：`ai-impl/orchestrator/impl/DiagnosisCapabilityExecutor.java` 等 6 个薄适配器
- **描述**：设计文档 §2.1 目录结构明确定义薄适配器归属 `ai-impl/thin-adapter/` 子包，与底座 7 项能力的 `ai-impl/orchestrator/impl/` 分离。但实现中 6 个薄适配器全部放在 `orchestrator/impl/` 包下，与底座能力混在一起。这导致：(1) 包结构无法区分底座完整管线与薄适配器委托两种不同实现模式；(2) 薄适配器对 Phase 4 模块的 Maven `provided` 依赖影响整个 `orchestrator/impl/` 包的编译期可见性；(3) 未来将某项能力从薄适配器升级为底座管线时，缺乏物理隔离的迁移边界。
- **建议**：将 6 个薄适配器类迁移至 `com.aimedical.modules.ai.impl.thin-adapter` 包，与设计文档 §2.1 目录结构一致。

#### [一般] AiOrchestrator.handle() 对未注册能力标识返回 failure 而非设计文档要求的 fail-fast 异常

- **位置**：`ai-impl/orchestrator/AiOrchestrator.java:149-151`
- **描述**：设计文档 §3.1 明确要求"未注册对应执行器的能力标识在被 AiOrchestrator 接收时将抛出明确的配置异常（启动期 fail-fast 而非运行时静默降级）"。但实现中 `handle()` 方法在 `executor == null` 时仅 log.warn 并返回 `AiResult.failure()`，属于运行时静默降级行为。未注册的能力标识通常意味着配置错误，静默返回 failure 会使问题难以发现和诊断。
- **建议**：将 `executor == null` 时的处理改为抛出 `IllegalStateException`（或至少在启动期 `@PostConstruct` 阶段校验 13 个能力标识是否全部注册），与设计文档 fail-fast 约定一致。

#### [一般] AiOrchestrator.handle() catch 块中 metricsCollector.record() 未实现

- **位置**：`ai-impl/orchestrator/AiOrchestrator.java:158`
- **描述**：`handle()` 方法的 catch 块中存在 `// TODO: metricsCollector.record() when AiMetricsCollector methods are defined` 注释，意味着 handle 层面的意外异常指标记录未实现。设计文档 §3.1 明确要求 AiOrchestrator 持有 `AiMetricsCollector` 用于"兜底记录 handle() 层面意外异常的指标"，当前实现注入了 `metricsCollector` 但未使用。
- **建议**：补充 `metricsCollector.record()` 调用，记录 handle 层面的意外异常指标（capabilityId、elapsedMs、degraded=true、degradeReason 等）。

#### [一般] AbstractCapabilityExecutor 构造器参数类型与设计文档不一致——capabilityTimeoutConfig/parseTimeoutConfig/thinAdapterPerCapabilityConfig 应为 Map 而非 AtomicReference<Map>

- **位置**：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:71-75`
- **描述**：设计文档 §3.1 构造器伪代码中，`capabilityTimeoutConfig`、`parseTimeoutConfig`、`thinAdapterPerCapabilityConfig` 的类型为 `Map<String, Duration>`（通过 `@Qualifier` 注入），`parseTimeoutDefault` 为 `Duration`（通过 `@Value` 注入）。但实现中将这四个字段全部改为 `AtomicReference` 包装（`AtomicReference<Map<String, Duration>>`、`AtomicReference<Duration>`），暗示支持运行时热替换。然而设计文档中仅 `degradationStrategyMapRef` 明确要求使用 `AtomicReference` 支持热加载，超时配置的热加载机制未在设计文档中定义。此变更虽增加了灵活性，但与设计文档不一致，且 `AtomicReference` 的全量替换语义在配置变更期间可能导致请求看到不一致的超时配置快照（如 `capabilityTimeoutConfig` 已更新但 `parseTimeoutConfig` 仍为旧值）。
- **建议**：如确需超时配置热加载，应在设计文档中补充此决策及一致性保障机制；否则恢复为设计文档定义的 `Map<String, Duration>` 直接注入。

#### [一般] 底座 7 项 CapabilityExecutor 构造器第一个参数 Class<T> inputType 不应由 Spring 自动注入

- **位置**：`ai-impl/orchestrator/impl/TriageCapabilityExecutor.java:35-36`（7 个底座执行器均存在相同问题）
- **描述**：底座 7 项 CapabilityExecutor 的构造器第一个参数声明为 `Class<TriageRequest> inputType`，标注 `@Autowired`。Spring 会尝试按类型注入 `Class<TriageRequest>` 对应的 Bean，但 `Class` 对象通常不是 Spring Bean，依赖 Spring 的自动注入机制不可靠。设计文档 §3.1 构造器伪代码中 `inputType` 是显式传入的参数（由子类在 `super()` 调用中直接传递 `TriageRequest.class`），不应由容器注入。
- **建议**：移除构造器中的 `Class<T> inputType` 参数，改为在 `super()` 调用中直接传递具体的 Class 字面量（如 `TriageRequest.class`），与设计文档一致。

#### [一般] DiscussionConclusionCapabilityExecutor.refineTimeoutReason() 逻辑与设计文档反转

- **位置**：`ai-impl/orchestrator/impl/DiscussionConclusionCapabilityExecutor.java:129-135`
- **描述**：设计文档 §3.1 定义 `refineTimeoutReason()` 的逻辑为：`transcriptSummaryElapsedMs > capabilityTimeout * 0.8 → ":transcriptSummaryCrowding"`，否则 `":primaryLlmTimeout"`。即当压缩摘要耗时占整体超时 80% 以上时判定为压缩拥挤。但实现中的条件为 `elapsedInDoExecuteInternal - transcriptSummaryElapsedMs < capabilityTimeout * 0.2`，等价于"主 LLM 耗时 < 超时的 20%"，即"主 LLM 耗时很短时判定为压缩拥挤"——这与设计意图相反。当压缩摘要耗时很长但主 LLM 耗时也很长时（两者均接近超时），实现会判定为 `:primaryLlmTimeout` 而非 `:transcriptSummaryCrowding`。
- **建议**：将条件改为 `transcriptSummaryElapsedMs > capabilityTimeout.toMillis() * 0.8`，与设计文档 §3.1 一致。

#### [一般] DiscussionConclusionCapabilityExecutor 修改了防御性拷贝后的 request 对象

- **位置**：`ai-impl/orchestrator/impl/DiscussionConclusionCapabilityExecutor.java:113,116`
- **描述**：`doExecuteInternal()` 中调用 `request.setTranscripts(...)` 修改了传入的 request 对象。虽然 `AbstractCapabilityExecutor.execute()` 已做防御性拷贝，但设计文档明确约定"request DTO 在整个执行管线中约定为只读对象"，子类不应修改 request。此处修改了拷贝后的对象虽不影响调用方，但违反了只读约定，且如果未来防御性拷贝机制变更（如因性能原因跳过拷贝），将导致调用方数据被污染。
- **建议**：不修改 request 对象，而是将压缩后的 transcripts 作为独立变量传递给 `executeStandardPipeline()` 或在 `extractVariables()` 中处理。

#### [一般] AiCallRecord 字段与设计文档 §2.3 类图不一致——缺少多个关键字段

- **位置**：`ai-impl/metrics/AiCallRecord.java:1-59`
- **描述**：设计文档 §2.3 类图定义 `AiCallRecord` 包含 21 个字段（callTime、capabilityName、inputSummary、outputSummary、errorCode、errorMessage、totalTokens 等），但实现仅包含 15 个字段，缺少以下关键字段：(1) `callTime`（LocalDateTime，调用时间戳）；(2) `capabilityName`（能力名称）；(3) `inputSummary`/`outputSummary`（输入/输出摘要）；(4) `errorCode`/`errorMessage`（错误码/错误消息）；(5) `totalTokens`（总 Token 数）；(6) `sessionId` 在 AiCallRecord 中有但设计文档的 AiCallLogEntity 中也有，字段对等性需确认。缺少 `callTime` 导致无法按时间维度查询调用记录；缺少 `errorCode`/`errorMessage` 导致失败/降级记录无法携带错误详情。
- **建议**：按设计文档 §2.3 补齐缺失字段，确保 `AiCallRecord` 与 `AiCallLogEntity` 字段对等。

#### [一般] AbstractCapabilityExecutor.doDegrade() 中重复调用 extractUserId()

- **位置**：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:272`
- **描述**：`doDegrade()` 方法内部调用 `extractUserId()` 获取 userId，但 `extractUserId()` 从 `SecurityContextHolder` 读取。`doDegrade()` 可能在 `supplyAsync` 的线程池线程中被调用（如超时降级路径），此时 `SecurityContextHolder` 的 ThreadLocal 上下文可能不可用（取决于 `DelegatingSecurityContextRunnable` 是否配置），导致 userId 回退为 "SYSTEM"。而 `execute()` 入口处已在容器线程提取了 userId 并传入 `doExecuteInternal()`，`doDegrade()` 应使用同样的 userId 而非重新提取。
- **建议**：将 userId 作为 `doDegrade()` 的参数传入（与 `doExecuteInternal()` 一致），避免在线程池线程中访问 SecurityContextHolder。

#### [一般] AbstractCapabilityExecutor.extractCallerRole()/extractCallerId() 始终返回 null

- **位置**：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:323-329`
- **描述**：设计文档 §3.1 明确定义 `extractCallerRole()` 委托至 `RequestContextUtils.extractCallerRole()`，`extractCallerId()` 委托至 `RequestContextUtils.extractCallerId()`。但实现中两个方法均硬编码返回 null，且 `RequestContextUtils` 仅提供 `extractFromRequestContext(String headerName)` 方法，未实现 `extractCallerRole()` 和 `extractCallerId()`。这导致所有 `AiCallRecord` 的 `callerRole` 和 `callerId` 字段永远为 null，丧失了调用方角色和标识的审计能力。
- **建议**：在 `RequestContextUtils` 中实现 `extractCallerRole()` 和 `extractCallerId()`（从 SecurityContext/RequestContext 提取），并在 `AbstractCapabilityExecutor` 中委托调用。

#### [一般] AbstractCapabilityExecutor.executeStandardPipeline() 中 endpointHealthManager 未做 null 检查

- **位置**：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:360`
- **描述**：`executeStandardPipeline()` 第 360 行直接调用 `endpointHealthManager.getState(routeResult.getEndpointId())`，但薄适配器子类在 `super()` 调用中传入 `endpointHealthManager=null`。虽然薄适配器不调用 `executeStandardPipeline()`，但如果未来有子类误调用或底座子类在构造时传入 null，将导致 NPE。设计文档 §3.1 提到 `AbstractCapabilityExecutor` 构造器对 null 引用做防御（跳过对应管线步骤而非 NPE）。
- **建议**：在 `executeStandardPipeline()` 入口处对 `endpointHealthManager` 做 null 检查，null 时跳过端点健康检查步骤。

#### [一般] AiResult 缺少设计文档要求的新增字段和工厂方法

- **位置**：`ai-api/AiResult.java:1-79`
- **描述**：设计文档 §1.6.1 标注 `AiResult<T>` 状态为"✓ 已有完整定义"，但当前 `AiResult` 的 `degraded()` 工厂方法签名与薄适配器使用场景不匹配——薄适配器业务异常路径使用 `AiResult.failure(errorCode, message)` 返回失败结果，但 `AiResult` 缺少同时携带 `errorCode` 和 `degraded=true` 的工厂方法（`AiResultFactory` 中有 `degraded(fallbackReason, errorCode, partialData)` 但 `AiResult` 本身未暴露）。此外，`AiResult` 的字段非 final（有 setter），与设计文档 §1.3 中"返回值 `CompletableFuture<AiResult<T>>` 完成后 `AiResult` 为不可变对象"的线程安全契约矛盾。
- **建议**：(1) 将 `AiResult` 字段改为 `final`，移除 setter，通过构造器赋值；(2) 补充 `AiResult.degradedWithErrorCode(fallbackReason, errorCode)` 工厂方法。

#### [一般] DegradationContext 扩展字段与设计文档不一致——新增 serviceName/operationName 但缺少设计文档定义的 invocationCount/failureCount 类型变更

- **位置**：`ai-api/degradation/DegradationContext.java:13-21`
- **描述**：设计文档 §2.3 类图定义 `DegradationContext` 字段为 `int invocationCount`（基本类型）和 `int failureCount`（基本类型），但实现中使用 `Integer invocationCount` 和 `Integer failureCount`（包装类型）。包装类型允许 null 值，导致 `isInitialized()` 方法需要 null 检查，且在序列化/反序列化时 null 值的处理与基本类型不同。此外，实现新增了 `serviceName` 和 `operationName` 两个字段，但设计文档 §2.3 类图中未定义这两个字段。`serialVersionUID` 为 `1L`，但字段已扩展（新增 serviceName/operationName），应更新 serialVersionUID 以确保序列化兼容性。
- **建议**：(1) 将 `invocationCount`/`failureCount` 改为基本类型 `int`，与设计文档一致；(2) 更新 `serialVersionUID`；(3) 确认 `serviceName`/`operationName` 是否为设计意图，如是则补充到设计文档。

#### [一般] DegradationContext.Builder 未实现设计文档要求的静态工厂方法 builder()

- **位置**：`ai-api/degradation/DegradationContext.java:119-189`
- **描述**：设计文档 §2.3 类图定义 `DegradationContext` 提供 `Builder builder()` 静态工厂方法，但实现中 `Builder` 是内部类且无 `builder()` 静态入口方法。调用方需直接 `new DegradationContext.Builder()` 创建，与设计文档的 `DegradationContext.builder()` 约定不一致。
- **建议**：在 `DegradationContext` 中添加 `public static Builder builder() { return new Builder(); }` 方法。

#### [一般] Phase4BusinessException 缺少 getErrorCode() 方法

- **位置**：`ai-api/dto/base/Phase4BusinessException.java:1-12`
- **描述**：设计文档 §3.1 异常处理规则要求薄适配器 catch 块优先使用 Phase 4 业务异常中自带的 `getErrorCode()` 方法获取错误码。但 `Phase4BusinessException` 抽象基类未定义 `getErrorCode()` 抽象方法或默认实现，子类无法通过统一接口暴露错误码。薄适配器当前回退到 `"PHASE4_" + cause.getClass().getSimpleName()` 作为错误码，这是设计文档中"getErrorCode() 不存在时的回退策略"，但基类应提供此方法以支持最终态。
- **建议**：在 `Phase4BusinessException` 中添加 `public String getErrorCode()` 方法（可提供默认实现返回 `getClass().getSimpleName()`），与设计文档最终态一致。

#### [一般] DiscussionConclusionCapabilityExecutor 压缩调用嵌套提交到 llmCallExecutor 存在线程池死锁风险

- **位置**：`ai-impl/orchestrator/impl/DiscussionConclusionCapabilityExecutor.java:170-185`
- **描述**：`compressTranscripts()` 方法在 `llmCallExecutor` 线程池的 Worker 线程中执行（因为 `doExecuteInternal()` 本身已运行在 `llmCallExecutor` 线程中），又通过 `CompletableFuture.supplyAsync(..., llmCallExecutor)` 向同一线程池提交压缩任务，然后通过 `future.get()` 阻塞等待结果。这构成嵌套提交+阻塞等待模式，当 `llmCallExecutor` 的所有 Worker 线程均执行到 `compressTranscripts()` 的 `future.get()` 阻塞点时，压缩任务因无线程可用而永远无法执行，形成线程池死锁。
- **建议**：压缩任务应提交到 `ForkJoinPool.commonPool()`（与设计文档薄适配器伪代码中委托调用使用 commonPool 的约定一致），或使用独立线程池，避免与主管线共享同一线程池。

#### [一般] 薄适配器 CompletableFuture.supplyAsync() 未指定线程池

- **位置**：`ai-impl/orchestrator/impl/DiagnosisCapabilityExecutor.java:107`（6 个薄适配器均存在相同问题）
- **描述**：设计文档 §3.1 薄适配器伪代码明确注释"使用公共 ForkJoinPool.commonPool()（默认线程池）而非 llmCallExecutor，避免嵌套提交到同一线程池产生的排队死锁风险"。但实现中 `CompletableFuture.supplyAsync(() -> ...)` 未指定 Executor，默认使用 `ForkJoinPool.commonPool()`，虽然结果与设计文档一致，但属于隐式依赖而非显式声明。更重要的是，薄适配器的 `doExecuteInternal()` 运行在 `llmCallExecutor` 线程中（由 `AbstractCapabilityExecutor.execute()` 的 `supplyAsync` 提交），在 `llmCallExecutor` Worker 线程中通过 `delegateFuture.get()` 阻塞等待 `commonPool` 任务完成，存在设计文档已分析的线程饥饿风险。
- **建议**：显式指定 `ForkJoinPool.commonPool()` 作为 Executor 参数，使线程池选择意图自文档化。

#### [一般] AiOrchestrator 使用 ConcurrentHashMap 但声明为 volatile Map——线程安全模型冗余

- **位置**：`ai-impl/orchestrator/AiOrchestrator.java:55,69,78`
- **描述**：`executorMap` 声明为 `volatile Map<String, CapabilityExecutor<?, ?>>`，但在 `@PostConstruct` 中赋值为 `ConcurrentHashMap` 实例。设计文档 §3.1 明确说明"Map<String, CapabilityExecutor> 在初始化后不再变更，读操作无竞争"。既然 Map 初始化后不变，`ConcurrentHashMap` 的并发安全特性无必要，使用普通 `HashMap` + `volatile` 引用即可（volatile 保证引用的可见性，HashMap 初始化后不变保证线程安全）。当前写法虽无功能错误，但暗示 Map 可能被并发修改，与设计文档的不可变语义矛盾。
- **建议**：将 `executorMap` 改为 `private volatile Map<String, CapabilityExecutor<?, ?>>` + 普通不可变 Map（`Map.of(...)` 或 `Collections.unmodifiableMap(new HashMap<>(...))`），与设计文档"初始化后不再变更"语义一致。

#### [轻微] RequestContextUtils 缺少 extractCallerRole()/extractCallerId() 方法

- **位置**：`ai-impl/util/RequestContextUtils.java:1-20`
- **描述**：设计文档 §3.1 定义 `extractCallerRole()` 和 `extractCallerId()` 统一委托至 `RequestContextUtils` 的规范实现，但 `RequestContextUtils` 仅提供通用的 `extractFromRequestContext(String headerName)` 方法，未提供 `extractCallerRole()` 和 `extractCallerId()` 专用方法。`AbstractCapabilityExecutor` 中的 `extractCallerRole()`/`extractCallerId()` 因此只能返回 null。
- **建议**：在 `RequestContextUtils` 中补充 `extractCallerRole()` 和 `extractCallerId()` 方法实现。

#### [轻微] DiscussionConclusionRequest 未继承 AiRequestBase

- **位置**：`ai-api/dto/discussion/DiscussionConclusionRequest.java:1-14`
- **描述**：`DiscussionConclusionRequest` 是底座 7 项能力之一，其 DTO 应继承 `AiRequestBase` 以支持 `doExtractDepartmentId()` 等方法从 DTO 直接提取上下文字段。但当前 `DiscussionConclusionRequest` 未继承 `AiRequestBase`，导致 `AbstractCapabilityExecutor.doExtractDepartmentId()` 的默认实现（`instanceof AiRequestBase` 检查）返回 null，departmentId 只能从 RequestContext 提取。其他底座 DTO（如 `TriageRequest`）需确认是否同样未继承 `AiRequestBase`。
- **建议**：确认底座 7 项能力的请求 DTO 是否应继承 `AiRequestBase`，如是则补齐继承关系。

#### [轻微] DiscussionConclusionResponse 为空类——缺少业务字段

- **位置**：`ai-api/dto/discussion/DiscussionConclusionResponse.java:1-7`
- **描述**：`DiscussionConclusionResponse` 仅有默认构造器，无任何业务字段（如结论文本、关键发现、建议等）。作为底座 7 项能力之一的响应 DTO，空类无法承载 LLM 结构化输出的解析结果，`StructuredOutputParser.parse()` 将无法将 LLM 输出映射到此空类。
- **建议**：补充 `DiscussionConclusionResponse` 的业务字段（如 `conclusion: String`、`keyFindings: List<String>`、`recommendations: List<String>` 等），与 LLM 结构化输出对齐。

#### [轻微] DiscussionTranscript.timestamp 字段类型为 String 而非设计友好的类型

- **位置**：`ai-api/dto/discussion/DiscussionTranscript.java:6`
- **描述**：`timestamp` 字段使用 `String` 类型存储时间戳，缺乏类型安全和格式约束。设计文档未明确指定此字段类型，但使用 `LocalDateTime` 或 `Instant` 可提供编译期类型安全和格式一致性。
- **建议**：考虑将 `timestamp` 改为 `LocalDateTime` 或 `Instant` 类型，或至少在 Javadoc 中标注期望的日期格式。

#### [轻微] 底座 7 项 CapabilityExecutor 的 promptVersion 使用 @Value 配置注入而非实验分流结果

- **位置**：`ai-impl/orchestrator/impl/TriageCapabilityExecutor.java:31-33`（7 个底座执行器均存在相同问题）
- **描述**：设计文档 §3.1 定义 `promptVersion` 应由 `ExperimentManager.assign()` 返回的 `assignment.getTargetPromptVersion()` 提供，在 `doExecuteInternal()` 中动态获取。但底座 7 项执行器使用 `@Value("${ai.prompt.version.TRIAGE:TRIAGE}")` 静态配置注入 promptVersion，绕过了实验分流机制。这意味着 A/B 实验的 Prompt 版本控制对这些能力不生效。
- **建议**：在 `doExecuteInternal()` 中通过 `ExperimentManager.assign()` 获取动态 promptVersion，替代 `@Value` 静态配置。

#### [轻微] AbstractCapabilityExecutor.extractOutputSummary() 未按设计文档截断至 500 字符

- **位置**：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:319-321`
- **描述**：设计文档 §3.1 定义 `extractOutputSummary()` 默认实现为 `StringUtils.truncate(result.toString(), 500)`，但实现中直接返回 `result.toString()` 无截断。长输出摘要可能导致 `AiCallRecord` 的 `outputSummary` 字段过大，影响日志和数据库存储。
- **建议**：添加 500 字符截断逻辑。

#### [轻微] AbstractCapabilityExecutor.computeInputSummary() 使用 extractVariables() 而非 request.toString()

- **位置**：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:536-540`
- **描述**：设计文档 §3.1 定义 `inputSummary` 为 `StringUtils.truncate(defensiveCopy.toString(), 500)`，但实现中先调用 `extractVariables(request)` 将 DTO 转为 Map 再取 `toString()`。这引入了额外的 Jackson 序列化开销，且 Map 的 toString 格式与 DTO 的 toString 格式不同，可能影响日志可读性。
- **建议**：直接使用 `request.toString()` 并截断至 500 字符，与设计文档一致。

### 本轮统计

| 严重程度 | 数量 |
|---------|------|
| 严重 | 5 |
| 一般 | 14 |
| 轻微 | 7 |

### 总评

本轮审查覆盖 ai-api 模块和 ai-impl/orchestrator/ 编排层共 31 个文件，对照设计文档 §1.3/§2.1/§2.3/§3.1 进行一致性验证。

**核心问题**：5 项严重问题均集中在编排层实现与设计文档的关键偏差上。最严重的是薄适配器 `isDtoEmpty()` 的判定逻辑错误（包路径匹配导致所有请求永远降级）和反射调用 Phase 4 服务（丧失编译期类型安全），这两个问题将直接导致薄适配器在运行时无法正常工作。`executeStandardPipeline()` 中 `LlmInfrastructureException` 的字符串匹配检测和缺失的双 catch 分支设计也构成运行时异常处理的风险。

**设计一致性问题**：薄适配器目录结构（`orchestrator/impl/` vs 设计要求的 `thin-adapter/`）、构造器参数类型（`AtomicReference` vs 设计要求的 `Map`）、`AiCallRecord` 字段缺失、`extractCallerRole()`/`extractCallerId()` 未实现等 14 项一般问题，反映了实现与设计文档之间存在系统性偏差，建议在后续迭代中逐步对齐。

**ai-api 模块质量**：`CallContext`、`Phase4ServiceMeta`、`Phase4ServiceMetaCapable`、`Phase4BusinessException`、`DegradationReason`、`DegradationStrategy` 的实现与设计文档基本一致，质量良好。`DegradationContext` 的 `serialVersionUID` 和字段类型需微调。`AiResult` 的可变性与设计文档的不可变契约矛盾需关注。
