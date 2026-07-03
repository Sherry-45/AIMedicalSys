# 任务指令（v4）

## 动作
NEW

## 任务描述
R4: 核心执行器异常处理 — 修复 AbstractCapabilityExecutor 中 7 项问题（T3,T4,T5,T21,T26,T27,T28）。

**核心涉及文件**: `AbstractCapabilityExecutor.java`、`RequestContextUtils.java`
**T21 波及文件**: 7 个底座子类、6 个薄适配器（见 T21 小节和涉及文件清单）

---

### T3 [严重] — exceptionally() 中 Phase4BusinessException 误走降级路径
- **位置**: `AbstractCapabilityExecutor.execute()` 的 exceptionally() 回调（line 183-187）
- **问题**: `isKnownPhase4BusinessException(cause)` 匹配到 Phase 4 业务异常后调用 `doDegrade()`。但 Phase4BusinessException 代表 Phase 4 侧的业务错误（如校验失败），应作为普通 failure 传播而非降解处理
- **设计依据**: `Docs/06_ood_phase5_G.md:3471-3483` — exceptionally 伪代码仅处理 TimeoutException，其他异常均 `throw new CompletionException(cause)` 传播到 AiOrchestrator 的 catch 块，由 AiOrchestrator 转为 `AiResult.failure()`
- **修正**: 移除 exceptionally() 中 `isKnownPhase4BusinessException(cause)` 分支，让 Phase4BusinessException 像其他非超时异常一样重新包装为 `CompletionException` 向上传播

### T4 [严重] — executeStandardPipeline() 中字符串匹配替代 instanceof
- **位置**: `executeStandardPipeline()` 行 462, 474
- **问题**: `causeInner.getClass().getName().contains("LlmInfrastructureException")` 和 `cause.getClass().getName().contains("LlmInfrastructureException")` 使用类名字符串匹配检测 LlmInfrastructureException，而非类型安全的 instanceof
- **修正**: 两处均改为 `cause instanceof LlmInfrastructureException`

### T5 [严重] — executeStandardPipeline() 缺少对 LlmInfrastructureException 的独立 catch 分支
- **位置**: `executeStandardPipeline()` 的 ExecutionException 处理器（line 433-480）
- **问题**: LlmInfrastructureException 的检测混杂在 `isKnownPhase4BusinessException` 的 else-if 中，无独立可读的 catch 语义块
- **修正**: 在 ExecutionException 处理器中，先通过 `cause instanceof LlmInfrastructureException` 检测并走独立降级路径（`DegradationReason.INFRASTRUCTURE_ERROR`）；再处理 Phase4BusinessException（直接传播）；最后处理其他异常。内外两处（line 459-466 的 inner catch、line 473-480 的 outer catch）均按此三叉逻辑重构
- **⚠️ 与 T28 叠加重合区域**: T5 和 T28 均修改 `executeStandardPipeline()` 同一方法体（line 332-482）。T28 修改前半段（line 357-366 health check null 保护），T5 修改后半段（line 433-481 异常处理结构）。两者虽不直接冲突，但 `doDegrade()` 调用在该方法内出现 14 处，修改时需注意所有调用点的 `userId` 参数传入（T26 引入的新参数）。

### T21 [一般] — 构造器参数类型与设计文档不一致
- **位置**: 
  - 父类: `AbstractCapabilityExecutor` 构造器签名（line 79-113）及字段声明（line 70-74）
  - ⚠️ **波及 7 个底座子类构造器**: TriageCapabilityExecutor, ScheduleCapabilityExecutor, PrescriptionCheckCapabilityExecutor, PrescriptionAssistCapabilityExecutor, MedicalRecordGenCapabilityExecutor, KbQueryCapabilityExecutor, DiscussionConclusionCapabilityExecutor（每个都携带 `AtomicReference<Map<String, Duration>>` 参数并传入 `super()`）
  - ⚠️ **波及 6 个薄适配器构造器**: DiagnosisCapabilityExecutor, ImageAnalysisCapabilityExecutor, AnalysisReportForLabTestCapabilityExecutor, AnalysisReportForInspectionCapabilityExecutor, RecommendExecutionOrderCapabilityExecutor, RecommendExaminationCapabilityExecutor（同上模式）
- **问题**: 设计文档 `Docs/06_ood_phase5_G.md:1528-1544` 规定的构造器中 `capabilityTimeoutConfig`/`parseTimeoutConfig`/`parseTimeoutDefault`/`thinAdapterPerCapabilityConfig` 参数类型分别为 `Map<String, Duration>`/`Map<String, Duration>`/`Duration`/`Map<String, Duration>`（非 AtomicReference 包装），当前代码使用了 `AtomicReference<Map<String, Duration>>`/`AtomicReference<Duration>`
- **修正**:
  1. **父类字段声明**（4 处变更）:
     - `capabilityTimeoutConfig`: `AtomicReference<Map<String, Duration>>` → `Map<String, Duration>`
     - `parseTimeoutConfig`: `AtomicReference<Map<String, Duration>>` → `Map<String, Duration>`
     - `parseTimeoutDefault`: `AtomicReference<Duration>` → `Duration`
     - `thinAdapterPerCapabilityConfig`: `AtomicReference<Map<String, Duration>>` → `Map<String, Duration>`
  2. **父类构造器签名**: 对应 4 个参数类型同步变更
  3. **父类内部 `.get()` 调用清理**（4 处）:
     - line 393: `parseTimeoutConfig.get()` → `parseTimeoutConfig`
     - line 397: `parseTimeoutDefault.get()` → `parseTimeoutDefault`
     - line 518: `capabilityTimeoutConfig.get()` → `capabilityTimeoutConfig`
     - line 522-523: `thinAdapterPerCapabilityConfig.get()` → `thinAdapterPerCapabilityConfig`（需做 null 判断后直接使用）
     - ⚠️ 注意 line 393 的 `parseTimeoutConfig` 在字段类型变更后为 `Map<String, Duration>`，其 `.get()` 调用需改为直接变量引用（`Map` 接口无 `.get()` 方法）
  4. **7 个底座子类**: 构造器参数中 4 个 AtomicReference 类型改为直接类型，`super()` 调用对应调整。import 中删除 `AtomicReference`（如不再有其他 AtomicReference 字段引用时）
  5. **6 个薄适配器**: 
     - 构造器参数类型和 `super()` 传参同步变更
     - `resolveThinAdapterTimeout()` 方法内 `thinAdapterPerCapabilityConfig.get()` → `thinAdapterPerCapabilityConfig`（line 155）
     - 修复后 `thinAdapterPerCapabilityConfig` 为 `Map<String, Duration>`，直接 `config.containsKey()` 判断
  6. ⚠️ 不修改 `degradationStrategyMapRef`（设计文档和代码均保持 AtomicReference）

### T26 [一般] — doDegrade() 中重复调用 extractUserId()
- **位置**: `doDegrade()`（line 268-269）
- **问题**: doDegrade() 在方法体内部调用 `extractUserId()` 从 SecurityContextHolder 提取 userId，但 execute() 入口处（line 118）已在容器线程中提取过 userId。在降级路径中（尤其是 Exceptionally 回调和线程池内），SecurityContextHolder 不可用，导致 userId 可能错误提取为 "SYSTEM"
- **修正**:
  - 在 `doDegrade()` 参数列表首部**新增** `String userId` 参数
  - 删除内部 `String userId = extractUserId();` 语句（line 269）
  - 更新所有 `doDegrade()` 调用点：execute() 中传 `capturedUserId`，checkPreDegradation() 中传 `userId`（需为 checkPreDegradation 增加 `String userId` 参数），executeStandardPipeline() 中传其已有 `userId` 形参
- **`checkPreDegradation()` 可见性说明**: 该方法当前为 `private`（line 203），无子类重写，因此增加 `userId` 参数不涉及任何子类变更，仅需同步更新父类中的调用点即可。

### T27 [一般] — extractCallerRole()/extractCallerId() 始终返回 null
- **位置**: `AbstractCapabilityExecutor`（line 320-326）
- **问题**: 两个方法均为空实现，返回 `null`，导致 AiCallRecord 中 callerRole/callerId 始终为空
- **设计依据**: `Docs/06_ood_phase5_G.md:3058-3065` — 应委托至 `RequestContextUtils.extractCallerRole()`/`extractCallerId()`
- **修正**:
  - 在 `RequestContextUtils.java` 中新增 `extractCallerRole()` 和 `extractCallerId()` 静态方法（实现见设计文档 line 3043-3056：从 SecurityContextHolder.getContext().getAuthentication() 提取 authorities，过滤 ROLE_ 前缀优先匹配，次选首条 authority；getName() 提取 callerId，null 时回退 "SYSTEM"）
  - `AbstractCapabilityExecutor.extractCallerRole()` → `return RequestContextUtils.extractCallerRole();`
  - `AbstractCapabilityExecutor.extractCallerId()` → `return RequestContextUtils.extractCallerId();`

### T28 [一般] — executeStandardPipeline() 中 endpointHealthManager 未做 null 检查
- **位置**: `executeStandardPipeline()` line 357,359-360
- **问题**: 直接调用 `endpointHealthManager.getState()` 和 `endpointHealthManager.tryProbe()`，当 endpointHealthManager 为 null 时 NPE
- **修正**: 在使用 endpointHealthManager 前添加防御性 null 检查。若为 null，跳过健康检查步骤（认为端点可用，直接尝试 LLM 调用）
- **⚠️ 与 T5 叠加重合区域**: T28 修改 `executeStandardPipeline()` 前半段（line 357-366），T5 修改后半段（line 433-481）。两者在同一方法体内、各自独立段落，可独立实施。但需注意该方法内共 14 处 `doDegrade()` 调用，T26 引入的 `userId` 参数需同步更新所有调用点，包括 T28 新增的和 T5 重构后的调用。

## 选择理由
R3 已修复全部测试编译阻塞，当前 mvn test 通过编译阶段。R4 聚焦 AbstractCapabilityExecutor 核心执行器的异常处理逻辑修复——这是运行时正确性的关键路径。T3/T4/T5 修复异常分类（Phase4 业务异常 vs LlmInfrastructureException vs 通用异常），T21 对齐设计文档构造器签名（⚠️ 波及全部 13 个子类），T26/T27/T28 修复 doDegrade 重复提取、caller 字段空值、防御性 null 检查。

## 任务上下文

### 当前 AbstractCapabilityExecutor 构造函数（16 参数，不含 inputType）
```java
protected AbstractCapabilityExecutor(
    PromptTemplateManager promptTemplateManager,
    ModelRouter modelRouter,
    LlmChatService llmChatService,
    StructuredOutputParser structuredOutputParser,
    AiMetricsCollector metricsCollector,
    SlidingWindowMetricsStore metricsStore,
    ModelEndpointHealthManager endpointHealthManager,
    AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef,
    LocalRuleFallback<T, R> localRuleFallback,
    AtomicReference<Map<String, Duration>> capabilityTimeoutConfig,
    AtomicReference<Map<String, Duration>> parseTimeoutConfig,
    AtomicReference<Duration> parseTimeoutDefault,
    Duration thinAdapterTimeout,
    AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig,
    Executor llmCallExecutor,
    ObjectMapper objectMapper
)
```

### 当前 exceptionally() 处理逻辑（line 173-191）
```
TimeoutException → doDegrade(TIMEOUT)
isKnownPhase4BusinessException(cause) → doDegrade(INTERNAL_ERROR)
其他 → throw CompletionException(cause)
```

### 当前 executeStandardPipeline() 异常处理结构（line 433-481）
```
catch (ExecutionException e):
    cause = e.getCause()
    if (cause instanceof StructuredOutputNotSupportedException):
        // fallback to chat() + parse()
        catch (ExecutionException ex):
            causeInner = ex.getCause()
            if isKnownPhase4BusinessException(causeInner) || stringMatch(LlmInfrastructureException):
                → doDegrade(INFRASTRUCTURE_ERROR)
            throw CompletionException(causeInner)
    else if isKnownPhase4BusinessException(cause) || stringMatch(LlmInfrastructureException):
        → doDegrade(INFRASTRUCTURE_ERROR)
    else:
        throw CompletionException(cause)
```

### 设计文档构造函数要求（Docs/06_ood_phase5_G.md:1528-1544）
```java
AbstractCapabilityExecutor(
    // inputType removed by R1, design not yet updated for this
    PromptTemplateManager promptTemplateManager,
    ModelRouter modelRouter,
    LlmChatService llmChatService,
    StructuredOutputParser structuredOutputParser,
    AiMetricsCollector metricsCollector,
    SlidingWindowMetricsStore metricsStore,
    ModelEndpointHealthManager endpointHealthManager,
    AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef,
    @Autowired(required = false) LocalRuleFallback<T, R> localRuleFallback,
    @Qualifier("capabilityTimeoutConfig") Map<String, Duration> capabilityTimeoutConfig,      // 非 AtomicReference
    @Qualifier("parseTimeoutConfig") Map<String, Duration> parseTimeoutConfig,                 // 非 AtomicReference
    @Value("${ai.execution.timeout.parse.default:5s}") Duration parseTimeoutDefault,           // 非 AtomicReference
    @Value("${ai.execution.timeout.thin-adapter-default:30s}") Duration thinAdapterTimeout,
    @Qualifier("thinAdapterPerCapabilityConfig") Map<String, Duration> thinAdapterPerCapabilityConfig, // 非 AtomicReference
    @Autowired ObjectMapper objectMapper
)
```

### 底座子类构造器模式（7 个子类，以 TriageCapabilityExecutor 为例）
```java
public TriageCapabilityExecutor(
    PromptTemplateManager promptTemplateManager,
    ModelRouter modelRouter, LlmChatService llmChatService,
    StructuredOutputParser structuredOutputParser, AiMetricsCollector metricsCollector,
    SlidingWindowMetricsStore metricsStore, ModelEndpointHealthManager endpointHealthManager,
    AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef,
    LocalRuleFallback<TriageRequest, TriageResponse> localRuleFallback,
    AtomicReference<Map<String, Duration>> capabilityTimeoutConfig,
    AtomicReference<Map<String, Duration>> parseTimeoutConfig,
    AtomicReference<Duration> parseTimeoutDefault, Duration thinAdapterTimeout,
    AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig,
    Executor llmCallExecutor, ObjectMapper objectMapper
) {
    super(promptTemplateManager, modelRouter, llmChatService,
          structuredOutputParser, metricsCollector, metricsStore, endpointHealthManager,
          degradationStrategyMapRef, localRuleFallback, capabilityTimeoutConfig,
          parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout,
          thinAdapterPerCapabilityConfig, llmCallExecutor, objectMapper);
}
```
所有 7 个底座子类（Triage、Schedule、PrescriptionCheck、PrescriptionAssist、MedicalRecordGen、KbQuery、DiscussionConclusion）构造器均遵循同一模式，`AtomicReference<Map<String, Duration>>` 4 个参数传入 `super()`。

### 薄适配器构造器模式（6 个适配器，以 DiagnosisCapabilityExecutor 为例）
```java
public DiagnosisCapabilityExecutor(
    Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service,
    AiMetricsCollector metricsCollector,
    SlidingWindowMetricsStore metricsStore,
    @Qualifier("capabilityTimeoutConfig") AtomicReference<Map<String, Duration>> capabilityTimeoutConfig,
    @Qualifier("parseTimeoutConfig") AtomicReference<Map<String, Duration>> parseTimeoutConfig,
    AtomicReference<Duration> parseTimeoutDefault,
    @Value("${ai.execution.timeout.thin-adapter-default:30s}") Duration thinAdapterTimeout,
    @Qualifier("thinAdapterPerCapabilityConfig") AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig,
    Executor llmCallExecutor,
    ObjectMapper objectMapper
) {
    super(null, null, null, null,
          metricsCollector, metricsStore, null, null, null,
          capabilityTimeoutConfig, parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout,
          thinAdapterPerCapabilityConfig, llmCallExecutor, objectMapper);
}
```
所有 6 个薄适配器（Diagnosis、ImageAnalysis、AnalysisReportForLabTest、AnalysisReportForInspection、RecommendExecutionOrder、RecommendExamination）构造器均遵循同一模式。

## 已有代码上下文
- `AbstractCapabilityExecutor.java` — 538 行，ai-impl/orchestrator/ 包
- `RequestContextUtils.java` — ai-impl/util/ 包，已有 `extractFromRequestContext()` 方法
- `LlmInfrastructureException` — ai-impl/client/exception/ 包，继承 RuntimeException
- `Phase4BusinessException` — ai-api/dto/base/ 包，抽象类继承 RuntimeException
- 所有 7 项底座 CapabilityExecutor 子类通过 super() 调用父类构造器；R1 已移除 inputType 参数
- 所有 6 项薄适配器子类同样通过 super() 调用父类构造器，并携带 4 个 AtomicReference 参数
- 设计文档 line 3471-3483: exceptionally 仅处理 TimeoutException，其他一律传播
- 设计文档 line 3634: LlmInfrastructureException 应被独立 catch
- 设计文档 line 3058-3065: extractCallerRole/extractCallerId 委托 RequestContextUtils
- 设计文档 line 3082: extractCallerRole 实现（ROLE_ 前缀过滤），extractCallerId 实现（getName → "SYSTEM"）

## 涉及文件清单

| 操作 | 类别 | 文件路径 |
|------|------|---------|
| 修改 | 父类（T3/T4/T5/T21/T26/T27/T28） | `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` |
| 修改 | 工具类（T27） | `ai-impl/.../util/RequestContextUtils.java` |
| 修改 | 底座子类 1/7（T21 波及） | `ai-impl/.../orchestrator/impl/TriageCapabilityExecutor.java` |
| 修改 | 底座子类 2/7（T21 波及） | `ai-impl/.../orchestrator/impl/ScheduleCapabilityExecutor.java` |
| 修改 | 底座子类 3/7（T21 波及） | `ai-impl/.../orchestrator/impl/PrescriptionCheckCapabilityExecutor.java` |
| 修改 | 底座子类 4/7（T21 波及） | `ai-impl/.../orchestrator/impl/PrescriptionAssistCapabilityExecutor.java` |
| 修改 | 底座子类 5/7（T21 波及） | `ai-impl/.../orchestrator/impl/MedicalRecordGenCapabilityExecutor.java` |
| 修改 | 底座子类 6/7（T21 波及） | `ai-impl/.../orchestrator/impl/KbQueryCapabilityExecutor.java` |
| 修改 | 底座子类 7/7（T21 波及） | `ai-impl/.../orchestrator/impl/DiscussionConclusionCapabilityExecutor.java` |
| 修改 | 薄适配器 1/6（T21 波及） | `ai-impl/.../thinadapter/DiagnosisCapabilityExecutor.java` |
| 修改 | 薄适配器 2/6（T21 波及） | `ai-impl/.../thinadapter/ImageAnalysisCapabilityExecutor.java` |
| 修改 | 薄适配器 3/6（T21 波及） | `ai-impl/.../thinadapter/AnalysisReportForLabTestCapabilityExecutor.java` |
| 修改 | 薄适配器 4/6（T21 波及） | `ai-impl/.../thinadapter/AnalysisReportForInspectionCapabilityExecutor.java` |
| 修改 | 薄适配器 5/6（T21 波及） | `ai-impl/.../thinadapter/RecommendExecutionOrderCapabilityExecutor.java` |
| 修改 | 薄适配器 6/6（T21 波及） | `ai-impl/.../thinadapter/RecommendExaminationCapabilityExecutor.java` |

**共 15 个文件**: 1 父类 + 1 工具类 + 7 底座子类 + 6 薄适配器

## 修订说明（v4 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] T21 构造器类型变更范围严重遗漏 — 涉及文件清单仅 2 个，遗漏 13 个子类 + 父类内部 .get() 调用 + 薄适配器内部 .get() 调用 | T21 小节已扩展为完整覆盖：7 个底座子类构造器、6 个薄适配器构造器、父类内部 4 处 .get() 调用（line 393/397/518/522-523）、薄适配器 resolveThinAdapterTimeout() 中 .get() 调用。涉及文件清单从 2 个扩展为 15 个。 |
| [一般] T26 checkPreDegradation() 私有方法需标注可见性 | T26 小节末尾已添加 `checkPreDegradation()` 可见性说明：标注该方法为 `private`（line 203）、无子类重写，增加 `userId` 参数不涉及子类变更。 |
| [一般] T5 和 T28 在 executeStandardPipeline() 中的叠加重合区域需标注 | T5 小节末尾和 T28 小节末尾均已添加 ⚠️ 叠加重合区域标注：说明两者在同一方法体不同段落，以及方法内共 14 处 `doDegrade()` 调用需同步适配 T26 引入的 `userId` 参数。 |
