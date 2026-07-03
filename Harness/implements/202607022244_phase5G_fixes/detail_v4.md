# 详细设计（v4）

## 概述

修复 AbstractCapabilityExecutor 及其子类中 7 项问题（T3/T4/T5/T21/T26/T27/T28）。涉及 15 个源文件（1 父类 + 1 工具类 + 7 底座子类 + 6 薄适配器），修改构造器签名、异常处理逻辑、字段类型及方法签名。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` | 修改 | T3/T4/T5/T21/T26/T27/T28 — 异常处理重构、字段类型变更、方法签名变更 |
| `ai-impl/.../util/RequestContextUtils.java` | 修改 | T27 — 新增 extractCallerRole()/extractCallerId() 静态方法 |
| `ai-impl/.../orchestrator/impl/TriageCapabilityExecutor.java` | 修改 | T21 波及 — 构造器参数 AtomicReference→直接类型 |
| `ai-impl/.../orchestrator/impl/ScheduleCapabilityExecutor.java` | 修改 | T21 波及 — 同上 |
| `ai-impl/.../orchestrator/impl/PrescriptionCheckCapabilityExecutor.java` | 修改 | T21 波及 — 同上 |
| `ai-impl/.../orchestrator/impl/PrescriptionAssistCapabilityExecutor.java` | 修改 | T21 波及 — 同上 |
| `ai-impl/.../orchestrator/impl/MedicalRecordGenCapabilityExecutor.java` | 修改 | T21 波及 — 同上 |
| `ai-impl/.../orchestrator/impl/KbQueryCapabilityExecutor.java` | 修改 | T21 波及 — 同上 |
| `ai-impl/.../orchestrator/impl/DiscussionConclusionCapabilityExecutor.java` | 修改 | T21 波及 — 同上 |
| `ai-impl/.../thinadapter/DiagnosisCapabilityExecutor.java` | 修改 | T21 波及 — 构造器参数变更 + resolveThinAdapterTimeout() .get() 清理；T26 波及 — doDegrade() 调用传入 userId |
| `ai-impl/.../thinadapter/ImageAnalysisCapabilityExecutor.java` | 修改 | 同上 |
| `ai-impl/.../thinadapter/AnalysisReportForLabTestCapabilityExecutor.java` | 修改 | 同上 |
| `ai-impl/.../thinadapter/AnalysisReportForInspectionCapabilityExecutor.java` | 修改 | 同上 |
| `ai-impl/.../thinadapter/RecommendExecutionOrderCapabilityExecutor.java` | 修改 | 同上 |
| `ai-impl/.../thinadapter/RecommendExaminationCapabilityExecutor.java` | 修改 | 同上 |

## 类型定义

### AbstractCapabilityExecutor<T, R>

**形态**：abstract class
**包路径**：`com.aimedical.modules.ai.impl.orchestrator`
**职责**：能力执行器基类，封装 LLM 调用管线与降级逻辑

**字段变更（T21，4 处）**：

| 字段名 | 变更前 | 变更后 |
|-------|--------|--------|
| `capabilityTimeoutConfig` | `AtomicReference<Map<String, Duration>>` | `Map<String, Duration>` (final) |
| `parseTimeoutConfig` | `AtomicReference<Map<String, Duration>>` | `Map<String, Duration>` (final) |
| `parseTimeoutDefault` | `AtomicReference<Duration>` | `Duration` (final) |
| `thinAdapterPerCapabilityConfig` | `AtomicReference<Map<String, Duration>>` | `Map<String, Duration>` (final) |

**构造器签名变更（T21，16 参数）**：

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
    Map<String, Duration> capabilityTimeoutConfig,             // 变更
    Map<String, Duration> parseTimeoutConfig,                   // 变更
    Duration parseTimeoutDefault,                               // 变更
    Duration thinAdapterTimeout,
    Map<String, Duration> thinAdapterPerCapabilityConfig,       // 变更
    Executor llmCallExecutor,
    ObjectMapper objectMapper
)
```

**公开方法**：

| 方法 | 变更说明 |
|------|---------|
| `execute(T, String) → CompletableFuture<AiResult<R>>` | T3: exceptionally() 移除 Phase4BusinessException 分支 |
| `doDegrade(long, String, T, String, String, String, String, String, String, String, String, String, String, String, String) → AiResult<R>` | **不变**（但内部删除 `extractUserId()` 调用） |
| `doDegrade(long, String, T, String, String, String, String, String, String, String, String, String, String, String, String, String) → AiResult<R>` | **T26: 新增重载** — 首参后插入 `String userId` |
| `checkPreDegradation(long, T, String, String, String, String, String, String, String, String) → CompletableFuture<AiResult<R>>` | **T26: 新增 `String userId` 参数**（第 2 位） |
| `executeStandardPipeline(long, T, String, String, String, String, String, String, String, String, String, Map<String,Object>, String, String) → AiResult<R>` | T4/T5: 异常检测逻辑重构；T28: endpointHealthManager null 保护；所有 doDegrade 调用传入 `userId` |
| `extractCallerRole() → String` | T27: 委托至 `RequestContextUtils.extractCallerRole()` |
| `extractCallerId() → String` | T27: 委托至 `RequestContextUtils.extractCallerId()` |
| `resolveTimeout(String) → Duration` | T21 波及：字段 `.get()` 调用清理 — 4 处变更 |

**构造方式**：子类通过 `super(...)` 调用
**类型关系**：7 个底座子类和 6 个薄适配器直接继承

### doDegrade() 方法重载（T26）

**形态**：protected 方法，新增重载（保留原方法向后兼容，但原方法内部删除 `extractUserId()` 改为从参数获取）

**新增签名**：
```java
protected AiResult<R> doDegrade(
    String userId,           // 新增首位参数
    long startTime,
    String degradeReason,
    T request,
    String capabilityId,
    String departmentId,
    String callerRole,
    String callerId,
    String visitId,
    String patientId,
    String sessionId,
    String inputSummary,
    String outputSummary,
    String promptVersion,
    String modelId,
    String sentinelReason
)
```

**内部变更**：删除 `String userId = extractUserId();` 语句（原 line 269），直接使用参数 `userId`

**原方法处理**：保持签名不变（不新增参数），将内部 `extractUserId()` 调用替换为参数中的 `userId`。调用者应优先使用新增重载，但原方法仍允许基类内部非 userId 感知路径使用（通过内联提取 userId）。

**决策说明**：采用新增重载而非修改原签名，是为了：
1. 不需要逐一修改所有子类 `doDegrade()` 的实际调用处——子类通过 `super.doDegrade(...)` 调用父类受保护方法时使用原签名
2. 外层 `execute()`/`checkPreDegradation()`/`executeStandardPipeline()` 等 38 处调用点全部使用新增重载

**实际方案澄清**：由于所有调用点均在 AbstractCapabilityExecutor 内部或子类的 doExecuteInternal() 中（这些方法已拥有 `userId` 参数），且无外部包调用 doDegrade()，因此实际修改为**在 doDegrade() 原方法签名首位增加 `String userId` 参数**。内部删除 `extractUserId()` 调用，使用参数值。所有 38 处调用点同步更新。

### T3 exceptionally() 重构

**当前逻辑**（line 173-191）：
```
TimeoutException → doDegrade(TIMEOUT)
isKnownPhase4BusinessException(cause) → doDegrade(INTERNAL_ERROR)  ← 删除此分支
其他 → throw CompletionException(cause)
```

**变更后逻辑**：
```
TimeoutException → doDegrade(TIMEOUT, capturedUserId)
其他 → throw CompletionException(cause)
```

**变更说明**：删除 line 183-187 整个 `isKnownPhase4BusinessException(cause)` if 分支。Phase4BusinessException 与其他非超时异常一样走 `throw new CompletionException(cause)` 向上传播至 AiOrchestrator。

### T4+T5 executeStandardPipeline() 异常检测重构

**当前异常处理结构**（line 433-481）：
```
catch (ExecutionException e):
    cause = e.getCause()
    if (cause instanceof StructuredOutputNotSupportedException):
        // inner try-catch with raw chat fallback
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

**变更后逻辑**（三叉结构）：
```
catch (ExecutionException e):
    cause = e.getCause()
    if (cause instanceof StructuredOutputNotSupportedException):
        // inner try-catch with raw chat fallback
        catch (ExecutionException ex):
            causeInner = ex.getCause()
            if causeInner instanceof LlmInfrastructureException:           // 独立分支
                → doDegrade(INFRASTRUCTURE_ERROR, userId)
            else if isKnownPhase4BusinessException(causeInner):            // 非降级，直接传播
                throw new CompletionException(causeInner)
            else:                                                          // 其他异常
                throw new CompletionException(causeInner)
    else if cause instanceof LlmInfrastructureException:                   // 独立分支
        → doDegrade(INFRASTRUCTURE_ERROR, userId)
    else if isKnownPhase4BusinessException(cause):                         // 非降级，直接传播
        throw new CompletionException(cause)
    else:                                                                  // 其他异常
        throw new CompletionException(cause)
```

**关键变更**：
1. `causeInner.getClass().getName().contains("LlmInfrastructureException")` → `causeInner instanceof LlmInfrastructureException`（T4）
2. `cause.getClass().getName().contains("LlmInfrastructureException")` → `cause instanceof LlmInfrastructureException`（T4）
3. LlmInfrastructureException 独立走降级，Phase4BusinessException 直接传播不降级（T5）
4. 降级路径调用新增重载 `doDegrade(userId, ...)`（T26 波及）

**⚠️ Phase4BusinessException 在两处的新行为**：
- 内层 catch（StructuredOutputNotSupportedException fallback 内）：`else if isKnownPhase4BusinessException(causeInner)` → `throw new CompletionException(causeInner)`
- 外层 catch：`else if isKnownPhase4BusinessException(cause)` → `throw new CompletionException(cause)`
- 两处均不再调用 `doDegrade()`

### T28 endpointHealthManager null 保护

**当前代码**（line 357-366）：
```java
EndpointHealthState healthState = endpointHealthManager.getState(routeResult.getEndpointId());
boolean canProbe = false;
if (healthState == EndpointHealthState.UNAVAILABLE) {
    canProbe = endpointHealthManager.tryProbe(routeResult.getEndpointId());
    if (!canProbe) {
        return doDegrade(...);
    }
}
```

**变更后**：
```java
if (endpointHealthManager != null) {
    EndpointHealthState healthState = endpointHealthManager.getState(routeResult.getEndpointId());
    boolean canProbe = false;
    if (healthState == EndpointHealthState.UNAVAILABLE) {
        canProbe = endpointHealthManager.tryProbe(routeResult.getEndpointId());
        if (!canProbe) {
            return doDegrade(userId, startTime, DegradationReason.ENDPOINT_UNAVAILABLE.getCode(),
                request, capabilityId, departmentId, callerRole, callerId,
                visitId, patientId, sessionId, inputSummary, null, promptVersion, null, sentinelReason);
        }
    }
}
```

**后续使用 `canProbe` 的 line 381**：`if (healthState == EndpointHealthState.UNAVAILABLE && canProbe)` 仅在 `endpointHealthManager != null` 区域内有效，需将其包裹在同一条件块内，或将 `canProbe` 声明移出并在 `endpointHealthManager == null` 时设为 `false`。

**实际方案**：将 `canProbe` 声明移出 null 块作用域，默认为 `false`：

```java
boolean canProbe = false;
if (endpointHealthManager != null) {
    EndpointHealthState healthState = endpointHealthManager.getState(routeResult.getEndpointId());
    if (healthState == EndpointHealthState.UNAVAILABLE) {
        canProbe = endpointHealthManager.tryProbe(routeResult.getEndpointId());
        if (!canProbe) {
            return doDegrade(...);
        }
    }
}
```

### T21 resolvedTimeout() 字段引用清理

`resolveTimeout()` 方法（line 517-531）中 4 处 `.get()` 调用清理：

| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 518 | `capabilityTimeoutConfig.get()` | `capabilityTimeoutConfig` |
| 522-523 | `thinAdapterPerCapabilityConfig.get()` | `thinAdapterPerCapabilityConfig` |
| 393 | `parseTimeoutConfig.get()` | `parseTimeoutConfig` |
| 397 | `parseTimeoutDefault.get()` | `parseTimeoutDefault` |

**注意**：line 393 的 `parseTimeoutConfig.get()` 原意为 `Map#get(capabilityId)`（"获取某能力的解析超时配置"），字段类型变更为 `Map<String, Duration>` 后，因其为 `Map` 不是 `AtomicReference`，语法变为 `parseTimeoutConfig.get(capabilityId)` 保持不变——实际为 `Map.get(Object key)`，无需修改调用语法，仅去除 `AtomicReference` 层。line 397 同理：`parseTimeoutDefault.get()` 原为 `AtomicReference.get()`，变更为 `Duration` 后直接用变量即可。

**⚠️ 注意**：line 522-523 当前为：
```java
Map<String, Duration> thinConfig = thinAdapterPerCapabilityConfig != null
    ? thinAdapterPerCapabilityConfig.get() : null;
```
变更为：
```java
Map<String, Duration> thinConfig = thinAdapterPerCapabilityConfig;
```
因字段不再为 null（构造器强制赋值），从 `AtomicReference<Map>` 变更为 `Map<String, Duration>` 后，`thinAdapterPerCapabilityConfig` 本身即为 `Map<String, Duration>` 类型，无需解引用。但不排除构造器传入 null 的可能性，因此保留 null 安全判断：
```java
Map<String, Duration> thinConfig = thinAdapterPerCapabilityConfig;
```

### T26 doDegrade() 调用点 userId 参数注入

**AbstractCapabilityExecutor 内 13 处调用点**（T3 移除 1 处后剩余 13 处）：

| 所在方法 | 行号（原） | 降级原因 | userId 值 |
|---------|-----------|---------|-----------|
| `exceptionally()` | 179 | TIMEOUT | `capturedUserId`（方法局部捕获变量） |
| `checkPreDegradation()` | 226-229 | STRATEGY_TRIGGERED | `userId`（方法参数） |
| `executeStandardPipeline()` | 352-355 | NO_AVAILABLE_ROUTE | `userId`（方法参数） |
| `executeStandardPipeline()` | 362-365 | ENDPOINT_UNAVAILABLE | `userId` |
| `executeStandardPipeline()` | 387-390 | TIMEOUT | `userId` |
| `executeStandardPipeline()` | 413-416 | PARSE_FAILURE | `userId` |
| `executeStandardPipeline()` | 425-428 | TIMEOUT | `userId` |
| `executeStandardPipeline()` | 430-433 | TIMEOUT | `userId` |
| `executeStandardPipeline()` | 440-443 | PARSE_FAILURE | `userId` |
| `executeStandardPipeline()` | 451-454 | TIMEOUT | `userId` |
| `executeStandardPipeline()` | 456-459 | TIMEOUT | `userId` |
| `executeStandardPipeline()` | 463-466 | INFRASTRUCTURE_ERROR | `userId` |
| `executeStandardPipeline()` | 469-472 | PARSE_FAILURE | `userId` |
| `executeStandardPipeline()` | 475-478 | INFRASTRUCTURE_ERROR | `userId` |

**薄适配器 24 处调用点**（6 个适配器 × 4 处/个）：

| 所在方法 | 降级原因 | userId 值 |
|---------|---------|-----------|
| `doExecuteInternal()` | Phase4DtoEmpty | `userId`（方法参数） |
| `doExecuteInternal()` | ThinAdapterTimeout | `userId` |
| `doExecuteInternal()` | ExecutionException generic | `userId` |
| `doExecuteInternal()` | Exception generic | `userId` |

### RequestContextUtils

**形态**：utility class
**包路径**：`com.aimedical.modules.ai.impl.util`
**职责**：请求上下文提取工具

**新增方法（T27）**：

```java
/**
 * 从 SecurityContextHolder 提取 callerRole。
 * 遍历 Authentication.getAuthorities()，优先返回含 "ROLE_" 前缀的 authority，
 * 无匹配时返回 null。
 */
public static String extractCallerRole()

/**
 * 从 SecurityContextHolder 提取 callerId。
 * 返回 Authentication.getName()，null 时回退 "SYSTEM"。
 */
public static String extractCallerId()
```

### 7 个底座子类构造器（T21 波及）

**变更模式**：每个子类构造器中 4 个 AtomicReference 类型参数 → 直接类型，`super()` 对应变更。import 中删除 `AtomicReference`（若不再使用）。

**7 个子类**：
- `TriageCapabilityExecutor`
- `ScheduleCapabilityExecutor`
- `PrescriptionCheckCapabilityExecutor`
- `PrescriptionAssistCapabilityExecutor`
- `MedicalRecordGenCapabilityExecutor`
- `KbQueryCapabilityExecutor`
- `DiscussionConclusionCapabilityExecutor`

**以 TriageCapabilityExecutor 为例的变更**：

```java
// 变更前
public TriageCapabilityExecutor(
    PromptTemplateManager promptTemplateManager,
    ...
    AtomicReference<Map<String, Duration>> capabilityTimeoutConfig,
    AtomicReference<Map<String, Duration>> parseTimeoutConfig,
    AtomicReference<Duration> parseTimeoutDefault,
    Duration thinAdapterTimeout,
    AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig,
    ...
) {
    super(..., capabilityTimeoutConfig, parseTimeoutConfig,
          parseTimeoutDefault, thinAdapterTimeout,
          thinAdapterPerCapabilityConfig, ...);
}

// 变更后
public TriageCapabilityExecutor(
    PromptTemplateManager promptTemplateManager,
    ...
    Map<String, Duration> capabilityTimeoutConfig,
    Map<String, Duration> parseTimeoutConfig,
    Duration parseTimeoutDefault,
    Duration thinAdapterTimeout,
    Map<String, Duration> thinAdapterPerCapabilityConfig,
    ...
) {
    super(..., capabilityTimeoutConfig, parseTimeoutConfig,
          parseTimeoutDefault, thinAdapterTimeout,
          thinAdapterPerCapabilityConfig, ...);
}
```

**DiscussionConclusionCapabilityExecutor 额外注意**：该类有额外 3 个构造参数（`compressionLightweightEndpoint`, `compressionLightweightClientType`, `transcriptSummaryTimeout`），仅变更父类相关的 4 个参数类型，额外参数不受影响。

### 6 个薄适配器构造器（T21 波及）

**变更模式**：构造器参数中 4 个 `AtomicReference` → 直接类型，`super()` 对应变更。

**以 DiagnosisCapabilityExecutor 为例的变更**：

```java
// 变更前
public DiagnosisCapabilityExecutor(
    Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service,
    AiMetricsCollector metricsCollector,
    SlidingWindowMetricsStore metricsStore,
    @Qualifier("capabilityTimeoutConfig") AtomicReference<Map<String, Duration>> capabilityTimeoutConfig,
    @Qualifier("parseTimeoutConfig") AtomicReference<Map<String, Duration>> parseTimeoutConfig,
    AtomicReference<Duration> parseTimeoutDefault,
    @Value("${ai.execution.timeout.thin-adapter-default:30s}") Duration thinAdapterTimeout,
    @Qualifier("thinAdapterPerCapabilityConfig") AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig,
    Executor llmCallExecutor, ObjectMapper objectMapper
) {
    super(null, null, null, null, metricsCollector, metricsStore, null, null, null,
          capabilityTimeoutConfig, parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout,
          thinAdapterPerCapabilityConfig, llmCallExecutor, objectMapper);
}

// 变更后
public DiagnosisCapabilityExecutor(
    Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service,
    AiMetricsCollector metricsCollector,
    SlidingWindowMetricsStore metricsStore,
    @Qualifier("capabilityTimeoutConfig") Map<String, Duration> capabilityTimeoutConfig,
    @Qualifier("parseTimeoutConfig") Map<String, Duration> parseTimeoutConfig,
    Duration parseTimeoutDefault,
    @Value("${ai.execution.timeout.thin-adapter-default:30s}") Duration thinAdapterTimeout,
    @Qualifier("thinAdapterPerCapabilityConfig") Map<String, Duration> thinAdapterPerCapabilityConfig,
    Executor llmCallExecutor, ObjectMapper objectMapper
) {
    super(null, null, null, null, metricsCollector, metricsStore, null, null, null,
          capabilityTimeoutConfig, parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout,
          thinAdapterPerCapabilityConfig, llmCallExecutor, objectMapper);
}
```

**薄适配器 `resolveThinAdapterTimeout()` 中 .get() 清理**（T21 波及）：

```java
// 变更前
private long resolveThinAdapterTimeout(String capabilityId) {
    if (thinAdapterPerCapabilityConfig == null) {
        return thinAdapterTimeout.toMillis();
    }
    Map<String, Duration> config = thinAdapterPerCapabilityConfig.get();
    if (config != null && config.containsKey(capabilityId)) {
        return config.get(capabilityId).toMillis();
    }
    return thinAdapterTimeout.toMillis();
}

// 变更后（thinAdapterPerCapabilityConfig 类型已为 Map<String, Duration>）
private long resolveThinAdapterTimeout(String capabilityId) {
    if (thinAdapterPerCapabilityConfig == null) {
        return thinAdapterTimeout.toMillis();
    }
    if (thinAdapterPerCapabilityConfig.containsKey(capabilityId)) {
        return thinAdapterPerCapabilityConfig.get(capabilityId).toMillis();
    }
    return thinAdapterTimeout.toMillis();
}
```

## 错误处理

- LlmInfrastructureException：继承自 RuntimeException，在 executeStandardPipeline() 中被 `instanceof` 捕获并走 INFRASTRUCTURE_ERROR 降级
- Phase4BusinessException：继承自 RuntimeException，不再被降级处理，两处（exceptionally + executeStandardPipeline 内外）均包装为 CompletionException 向上传播
- endpointHealthManager null：防御性 null 检查，跳过健康检查逻辑，不降级
- TimeoutException：保持不变，继续走 TIMEOUT 降级
- 所有异常路径均已确保 userId 通过参数传入而非 SecurityContextHolder 提取

## 行为契约

1. **T3**：`exceptionally()` 中 Phase4BusinessException 不再触发降级，改为 `throw new CompletionException(cause)`
2. **T4**：LlmInfrastructureException 检测使用 `instanceof` 而非字符串包含匹配
3. **T5**：executeStandardPipeline() 异常处理三叉结构：LlmInfrastructureException→降级 / Phase4BusinessException→传播 / Others→传播
4. **T21**：4 个 AtomicReference 字段变更为直接类型，13 个子类构造器签名对应变更
5. **T26**：`doDegrade()` 首参新增 `String userId`，删除内部 `extractUserId()`；`checkPreDegradation()` 第二位新增 `String userId`；所有 38 处调用点同步传入 userId
6. **T27**：`extractCallerRole()`/`extractCallerId()` 委托至 `RequestContextUtils` 静态方法，不再返回 null
7. **T28**：`executeStandardPipeline()` 中 `endpointHealthManager` null 时跳过健康检查，视为端点可用

## 依赖关系

- `AbstractCapabilityExecutor` 依赖 `LlmInfrastructureException`（ai-impl/client/exception 包）
- `RequestContextUtils.extractCallerRole()/extractCallerId()` 依赖 `org.springframework.security.core.context.SecurityContextHolder`
- 所有 13 个子类依赖更新后的父类构造器签名
- 6 个薄适配器额外依赖 `resolveThinAdapterTimeout()` 中 `thinAdapterPerCapabilityConfig` 类型变更
- 不涉及 pom.xml、配置类或新建文件

## 修订说明（v4 R1）
| 审查意见 | 修改措施 |
|---------|---------|
| 无（首轮设计） | — |
