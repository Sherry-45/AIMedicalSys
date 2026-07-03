# 详细设计（v3 r4）

## 概述

实现 CapabilityExecutor 泛型接口与 AbstractCapabilityExecutor 抽象骨架类，同时创建 8 个编译期依赖的存根类型。本设计是 Phase5 包 G 批次 1 核心骨架的一部分，为后续 13 个 CapabilityExecutor 子类提供基类。

## 文件规划

### 前置步骤：存根类型

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/template/PromptTemplateManager.java` | 新建 | 存根接口 |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/router/ModelRouter.java` | 新建 | 存根接口 |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/LlmChatService.java` | 新建 | 存根接口 |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/parser/StructuredOutputParser.java` | 新建 | 存根接口 |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/metrics/AiMetricsCollector.java` | 新建 | 存根接口 |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/metrics/ModelEndpointHealthManager.java` | 新建 | 存根类（无参构造器 + 空方法体） |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/fallback/LocalRuleFallback.java` | 新建 | 存根泛型接口 |
| `AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/dto/base/AiRequestBase.java` | 新建 | 存根抽象类（含 getDepartmentId/getVisitId/getPatientId/getSessionId 空方法） |

### 核心类型

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/CapabilityExecutor.java` | 新建 | 能力执行泛型接口 |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutor.java` | 新建 | 能力执行器抽象骨架 |
| `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutorTest.java` | 新建 | 单元测试 |

## 类型定义

### 前置步骤：存根类型（8个）

#### PromptTemplateManager

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.template`
**职责**：编译期存根，仅确保 AbstractCapabilityExecutor 可引用该类型
**签名**：
```java
package com.aimedical.modules.ai.impl.template;

public interface PromptTemplateManager {
}
```

#### ModelRouter

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.router`
**职责**：编译期存根
**签名**：
```java
package com.aimedical.modules.ai.impl.router;

public interface ModelRouter {
}
```

#### LlmChatService

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：编译期存根
**签名**：
```java
package com.aimedical.modules.ai.impl.client;

public interface LlmChatService {
}
```

#### StructuredOutputParser

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.parser`
**职责**：编译期存根
**签名**：
```java
package com.aimedical.modules.ai.impl.parser;

public interface StructuredOutputParser {
}
```

#### AiMetricsCollector

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.metrics`
**职责**：编译期存根
**签名**：
```java
package com.aimedical.modules.ai.impl.metrics;

public interface AiMetricsCollector {
}
```

#### ModelEndpointHealthManager

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.metrics`
**职责**：编译期存根，确保构造器引用通过
**签名**：
```java
package com.aimedical.modules.ai.impl.metrics;

public class ModelEndpointHealthManager {
    public ModelEndpointHealthManager() {
    }
}
```

#### LocalRuleFallback<T, R>

**形态**：interface（泛型）
**包路径**：`com.aimedical.modules.ai.impl.fallback`
**职责**：编译期存根
**签名**：
```java
package com.aimedical.modules.ai.impl.fallback;

public interface LocalRuleFallback<T, R> {
}
```

#### AiRequestBase

**形态**：abstract class
**包路径**：`com.aimedical.modules.ai.api.dto.base`
**职责**：编译期存根，底座能力请求 DTO 的公共基类。当前为带 protected 构造器和 4 个空 getter 方法的抽象类，使 AbstractCapabilityExecutor 的 doExtract* 方法可按 instanceof + cast 模式编译通过。后续批次补充业务字段并替换 getter 为真实字段读取
**签名**：
```java
package com.aimedical.modules.ai.api.dto.base;

public abstract class AiRequestBase {
    protected AiRequestBase() {
    }

    public String getDepartmentId() { return null; }
    public String getVisitId() { return null; }
    public String getPatientId() { return null; }
    public String getSessionId() { return null; }
}
```

---

### 1. CapabilityExecutor<T, R> 泛型接口

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.orchestrator`
**职责**：单项 AI 能力完整执行管线的泛型契约。每个 AI 能力（如 TRIAGE、RX_AUDIT 等）对应一个实现，封装该能力的完整执行流程
**类型签名**：
```java
public interface CapabilityExecutor<T, R> {
```
**泛型参数**：
- `T` — 能力对应的业务请求 DTO 类型
- `R` — 能力对应的业务响应 DTO 类型

**公开接口**：

| 方法签名 | 返回类型 | 职责 |
|---------|---------|------|
| `execute(T request, String capabilityId)` | `CompletableFuture<AiResult<R>>` | 执行单项 AI 能力的完整管线。不抛出业务异常，失败/降级均通过 CompletableFuture 完成（非异常路径） |
| `getCapabilityId()` | `String` | 返回该执行器对应的能力标识（如 "TRIAGE"、"RX_AUDIT"） |
| `getInputType()` | `Class<T>` | 输入 DTO 的 Class 对象 |
| `getOutputType()` | `Class<R>` | 输出 DTO 的 Class 对象 |

**Javadoc 约定**：
- `@implNote` request DTO 约定为只读对象，任何下游组件不得修改 request 或其嵌套字段
- `@apiNote` 不可变 DTO 与防御性拷贝兼容性说明：
  - DTO 为不可变且标注 Jackson 注解（@JsonCreator + @ConstructorProperties 或 @Jacksonized）→ 防御性拷贝正常工作
  - DTO 为可变（有 setter / 无参构造器）→ 防御性拷贝正常工作
  - DTO 为不可变但未标注 Jackson 注解 → 实现者须在子类中自行完成防御性拷贝，或补全 Jackson 注解后依赖默认拷贝

**构造方式**：无（接口）
**类型关系**：被 AbstractCapabilityExecutor 实现；被 AiOrchestrator 的 Map<String, CapabilityExecutor> 引用

---

### 2. AbstractCapabilityExecutor<T, R> 抽象骨架类

**形态**：abstract class
**包路径**：`com.aimedical.modules.ai.impl.orchestrator`
**职责**：CapabilityExecutor 的抽象骨架实现，封装降级预检、超时兜底、指标采集等所有子类共用的公共逻辑。13 个子类（7 底座 + 6 薄适配器）均继承此类，仅需特化 doExecuteInternal()

**类型签名**：
```java
public abstract class AbstractCapabilityExecutor<T, R> implements CapabilityExecutor<T, R> {
```

#### 字段

| 字段名 | 类型 | 修饰符 | 用途 |
|-------|------|--------|------|
| `inputType` | `Class<T>` | `protected final` | 输入 DTO 的 Class 对象 |
| `promptTemplateManager` | `PromptTemplateManager` | `protected final` | Prompt 模板管理器（薄适配器场景传 null） |
| `modelRouter` | `ModelRouter` | `protected final` | 模型路由器（薄适配器场景传 null） |
| `llmChatService` | `LlmChatService` | `protected final` | LLM 对话客户端（薄适配器场景传 null） |
| `structuredOutputParser` | `StructuredOutputParser` | `protected final` | 结构化输出解析器（薄适配器场景传 null） |
| `metricsCollector` | `AiMetricsCollector` | `protected final` | AI 指标采集器 |
| `metricsStore` | `SlidingWindowMetricsStore` | `protected final` | 滑动窗口指标存储 |
| `endpointHealthManager` | `ModelEndpointHealthManager` | `protected final` | 端点健康状态管理器（薄适配器场景传 null） |
| `degradationStrategyMapRef` | `AtomicReference<Map<String, List<DegradationStrategy>>>` | `protected final` | 降级策略 Map 的 AtomicReference，支持运行时热加载 |
| `localRuleFallback` | `LocalRuleFallback<T, R>` | `protected final` | 本地规则降级（可选，@Autowired(required=false)） |
| `capabilityTimeoutConfig` | `Map<String, Duration>` | `protected final` | 按能力标识配置的整体超时阈值 Map |
| `thinAdapterPerCapabilityConfig` | `Map<String, Duration>` | `protected final` | 按能力标识配置的薄适配器超时覆盖 Map |
| `parseTimeoutConfig` | `Map<String, Duration>` | `protected final` | 按能力标识配置的解析超时阈值 Map，预留用于 executeStandardPipeline() |
| `parseTimeoutDefault` | `Duration` | `protected final` | 解析超时默认值，预留用于 executeStandardPipeline() |
| `thinAdapterTimeout` | `Duration` | `protected final` | 薄适配器默认超时阈值 |
| `llmCallExecutor` | `Executor` | `protected final` | LLM 调用线程池，用于 supplyAsync 异步执行 |
| `objectMapper` | `ObjectMapper` | `protected final` | Jackson ObjectMapper，用于 execute() 入口处的防御性拷贝 |
| `elapsedInDoExecuteInternal` | `long` | `protected volatile` | doExecuteInternal() 的已消耗时间（毫秒），声明为 volatile 保证线程间可见性 |

```java
protected final Class<T> inputType;
protected final PromptTemplateManager promptTemplateManager;
protected final ModelRouter modelRouter;
protected final LlmChatService llmChatService;
protected final StructuredOutputParser structuredOutputParser;
protected final AiMetricsCollector metricsCollector;
protected final SlidingWindowMetricsStore metricsStore;
protected final ModelEndpointHealthManager endpointHealthManager;
protected final AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef;
protected final LocalRuleFallback<T, R> localRuleFallback;
protected final Map<String, Duration> capabilityTimeoutConfig;
protected final Map<String, Duration> thinAdapterPerCapabilityConfig;
protected final Map<String, Duration> parseTimeoutConfig;
protected final Duration parseTimeoutDefault;
protected final Duration thinAdapterTimeout;
protected final Executor llmCallExecutor;
protected final ObjectMapper objectMapper;
protected volatile long elapsedInDoExecuteInternal;

protected static final Set<String> knownPhase4Packages = Set.of(
    "com.aimedical.modules.diagnosis",
    "com.aimedical.modules.inspection",
    "com.aimedical.modules.labtest",
    "com.aimedical.modules.image",
    "com.aimedical.modules.examination",
    "com.aimedical.modules.execution"
);
```

#### 构造器

**形态**：17 参数全量构造器，`protected`
**约定**：所有参数均可为 null（薄适配器场景前 5 个传 null），构造器仅保存参数到字段，不做 null 校验

```java
protected AbstractCapabilityExecutor(
    Class<T> inputType,
    PromptTemplateManager promptTemplateManager,
    ModelRouter modelRouter,
    LlmChatService llmChatService,
    StructuredOutputParser structuredOutputParser,
    AiMetricsCollector metricsCollector,
    SlidingWindowMetricsStore metricsStore,
    ModelEndpointHealthManager endpointHealthManager,
    AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef,
    LocalRuleFallback<T, R> localRuleFallback,
    Map<String, Duration> capabilityTimeoutConfig,
    Map<String, Duration> parseTimeoutConfig,
    Duration parseTimeoutDefault,
    Duration thinAdapterTimeout,
    Map<String, Duration> thinAdapterPerCapabilityConfig,
    Executor llmCallExecutor,
    ObjectMapper objectMapper
)
```

#### execute() 模板方法（final）

```java
public final CompletableFuture<AiResult<R>> execute(T request, String capabilityId)
```

**行为契约**：

1. **ThreadLocal 上下文提取**（supplyAsync 之前，容器线程）：
   - `startTime = System.currentTimeMillis()`
   - `userId` 从 `SecurityContextHolder.getContext().getAuthentication().getName()` 提取，getAuthentication() 为 null 时回退 "SYSTEM"
   - `departmentId = doExtractDepartmentId(request)`
   - `visitId = doExtractVisitId(request)`
   - `patientId = doExtractPatientId(request)`
   - `sessionId = doExtractSessionId(request)`
   - `callerRole = extractCallerRole()` — 暂返回 null（RequestContextUtils 未实现）
   - `callerId = extractCallerId()` — 暂返回 null（RequestContextUtils 未实现）

2. **防御性拷贝**：
   - `T requestCopy = objectMapper.convertValue(request, inputType)`
   - 转换失败时 WARN 日志（"防御性拷贝失败: capabilityId={}, inputType={}, 回退到原始 request"），回退使用原始 request

3. **预计算 inputSummary**：
   - 调用 `extractVariables(requestCopy).toString()`，截取前 500 字符
   - 若 extractVariables 返回 null，使用 `String.valueOf(requestCopy)` 截取 500 字符

4. **降级预检**（supplyAsync 之前，容器线程；metricsStore 为 null 或 degradationStrategyMapRef 为 null 时跳过）：
   - `String requestType = requestCopy.getClass().getSimpleName()`
   - `DegradationContext ctx = metricsStore.buildDegradationContext(capabilityId, requestType)`
   - `ctx.setDepartmentId(departmentId)`
   - `Map<String, List<DegradationStrategy>> currentStrategyMap = degradationStrategyMapRef.get()`（若返回 null 则跳过）
   - `List<DegradationStrategy> strategies = currentStrategyMap.get(capabilityId)`（若为 null 则跳过）
   - 按 `getOrder()` 升序遍历策略：任一策略 `shouldDegrade(ctx)` 返回 true → `CompletableFuture.completedFuture(doDegrade(...))`

5. **正常请求入线程池**：
   ```java
   CompletableFuture<AiResult<R>> future = CompletableFuture.supplyAsync(() -> {
       elapsedInDoExecuteInternal = System.currentTimeMillis() - startTime;
       try {
           return doExecuteInternal(startTime, requestCopy, capabilityId,
               departmentId, userId, sessionId, callerRole, callerId,
               visitId, patientId, inputSummary);
       } finally {
           elapsedInDoExecuteInternal = System.currentTimeMillis() - startTime;
       }
   }, llmCallExecutor);
   ```

6. **超时兜底**（三级回退 + null 安全兜底）：
   ```
   timeout = resolveTimeout(capabilityId):
       if capabilityTimeoutConfig != null && capabilityTimeoutConfig.containsKey(capabilityId):
           return capabilityTimeoutConfig.get(capabilityId)
       if thinAdapterPerCapabilityConfig != null && thinAdapterPerCapabilityConfig.containsKey(capabilityId):
           return thinAdapterPerCapabilityConfig.get(capabilityId)
       if thinAdapterTimeout != null:
           return thinAdapterTimeout
       return Duration.ofSeconds(30)
   ```
   `future = future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)`

7. **exceptionally 处理**：
   ```java
   return future.exceptionally(throwable -> {
       Throwable cause = throwable instanceof CompletionException
           ? (throwable.getCause() != null ? throwable.getCause() : throwable)
           : throwable;
       if (cause instanceof TimeoutException) {
           DegradationReason reason = refineTimeoutReason(capabilityId, elapsedInDoExecuteInternal, requestCopy);
           return doDegrade(startTime, reason.getCode(), requestCopy, capabilityId, ...);
       }
       if (isKnownPhase4BusinessException(cause)) {
           return doDegrade(startTime, DegradationReason.INTERNAL_ERROR.getCode(), requestCopy, capabilityId, ...);
       }
       log.error("CapabilityExecutor 意外异常: capabilityId={}, cause={}", capabilityId, cause.toString());
       throw new CompletionException(cause);
   });
   ```

#### doExecuteInternal() 抽象方法

```java
protected abstract AiResult<R> doExecuteInternal(
    long startTime,
    T request,
    String capabilityId,
    String departmentId,
    String userId,
    String sessionId,
    String callerRole,
    String callerId,
    String visitId,
    String patientId,
    String inputSummary
);
```

#### doDegrade() 辅助方法

```java
protected AiResult<R> doDegrade(
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

**职责**：统一降级兜底入口。
1. 计算 `elapsedMs = System.currentTimeMillis() - startTime`
2. 若 `metricsCollector != null`：记录降级指标（AiCallRecord）
3. 若 `metricsStore != null`：调用 `metricsStore.recordDegraded(capabilityId, elapsedMs)`
4. 尝试本地规则降级：
   - 若 `localRuleFallback != null`：调用 `localRuleFallback.fallback(request)`，若返回非 null 且未降级，则使用该结果 + 记录成功指标
   - 否则返回 `AiResult.degraded(degradeReason)`
5. 不抛出异常

#### 变量提取方法

```java
protected Map<String, Object> extractVariables(T request)
```
- **默认行为**：`ObjectMapper.convertValue(request, Map.class)`

```java
protected String doExtractDepartmentId(T request)
```
- **默认行为**：`request instanceof AiRequestBase ? ((AiRequestBase) request).getDepartmentId() : null`
- **说明**：AiRequestBase 存根已包含返回 null 的 getDepartmentId() 方法，子类可重写

```java
protected String doExtractVisitId(T request)
```
- **默认行为**：`request instanceof AiRequestBase ? ((AiRequestBase) request).getVisitId() : null`

```java
protected String doExtractPatientId(T request)
```
- **默认行为**：`request instanceof AiRequestBase ? ((AiRequestBase) request).getPatientId() : null`

```java
protected String doExtractSessionId(T request)
```
- **默认行为**：`request instanceof AiRequestBase ? ((AiRequestBase) request).getSessionId() : null`

```java
protected String extractOutputSummary(Object result)
```
- **默认行为**：`result != null ? result.toString() : null`

```java
protected String extractCallerRole()
```
- **默认行为**：返回 null（待 RequestContextUtils 实现后委托调用）

```java
protected String extractCallerId()
```
- **默认行为**：返回 null（待 RequestContextUtils 实现后委托调用）

```java
protected DegradationReason refineTimeoutReason(
    String capabilityId,
    long elapsedInDoExecuteInternal,
    T request
)
```
- **默认行为**：返回 `DegradationReason.TIMEOUT`

#### executeStandardPipeline() 占位方法

```java
protected final AiResult<R> executeStandardPipeline(
    long startTime, T request, String capabilityId,
    String departmentId, String userId, String sessionId,
    String callerRole, String callerId, String visitId, String patientId,
    String inputSummary, Map<String, Object> variables,
    String promptVersion, String sentinelReason
)
```
- **形态**：`protected final`
- **当前实现**：`throw new UnsupportedOperationException("待后续实现")`

#### isKnownPhase4BusinessException() 辅助方法

```java
protected boolean isKnownPhase4BusinessException(Throwable cause)
```
- **行为**：从 cause.getClassName() 提取包路径前缀，检查是否在 `knownPhase4Packages` 中

#### 私有辅助方法

```java
private Duration resolveTimeout(String capabilityId)
```
- 三级回退 + null 安全兜底（见 execute() 步骤 6）

```java
private String computeInputSummary(T request)
```
- extractVariables(request).toString() 截取 500 字符

## 错误处理

| 场景 | 处理方式 | 降级原因 |
|------|---------|---------|
| 降级预检命中 | supplyAsync 之前，CompletableFuture.completedFuture(doDegrade(...)) | STRATEGY_TRIGGERED + 策略类名 |
| 整体超时 | orTimeout 触发，exceptionally 捕获 TimeoutException | TIMEOUT（子类可通过 refineTimeoutReason 细化） |
| Phase 4 业务异常 | exceptionally 中 isKnownPhase4BusinessException 检测通过 → doDegrade | INTERNAL_ERROR |
| 未知异常 | exceptionally 中非 TimeoutException 且非已知业务异常 → 日志 ERROR + 重新抛出 | 向上传播 |
| 防御性拷贝失败 | WARN 日志 + 回退使用原始 request | — |
| metricsStore / degradationStrategyMapRef 为 null | 跳过降级预检，直接走正常路径 | — |

## 行为契约

### 方法调用顺序

```
execute(request, capabilityId)
  ├── 提取 ThreadLocal 上下文（容器线程）
  ├── 防御性拷贝
  ├── 预计算 inputSummary
  ├── 降级预检（容器线程）
  │     └── 命中 → CompletableFuture.completedFuture(doDegrade(...))
  ├── supplyAsync(doExecuteInternal, llmCallExecutor)  ← 设置 elapsedInDoExecuteInternal
  ├── orTimeout（三级回退确定超时值）
  └── exceptionally
        ├── TimeoutException → refineTimeoutReason → doDegrade
        ├── 已知 Phase 4 业务异常 → doDegrade
        └── 未知异常 → ERROR 日志 + 重新抛出
```

### 状态变化规则

- `elapsedInDoExecuteInternal` 在 supplyAsync lambda 入口处设置，出口处（finally 块）更新
- `degradationStrategyMapRef.get()` 在每次 execute() 调用时读取最新快照
- execute() 为 final 方法，禁止子类重写

## 依赖关系

### 本任务创建的依赖

| 类型 | 包路径 | 消费者 |
|------|--------|--------|
| `CapabilityExecutor<T,R>` | `ai-impl/orchestrator/` | 被 AiOrchestrator 引用 |
| `AbstractCapabilityExecutor<T,R>` | `ai-impl/orchestrator/` | 被 13 个子类继承 |
| 8 个存根类型 | 见文件规划 | 被 AbstractCapabilityExecutor 构造器引用 |

### 依赖的已有类型

| 类型 | 包路径 | 来源 |
|------|--------|------|
| `AiResult<T>` | `ai-api` | 已有 |
| `DegradationContext` | `ai-api/degradation/` | 已有 |
| `DegradationReason` | `ai-api/degradation/` | 已有 |
| `DegradationStrategy` | `ai-api/degradation/` | 已有 |
| `SlidingWindowMetricsStore` | `ai-impl/metrics/` | 已有 |
| `ObjectMapper` | jackson-databind | spring-boot-starter-web 传递引入 |

## 修订说明（v3 r4）

| 审查意见 | 修改措施 |
|---------|---------|
| **[严重]** `llmCallExecutor` 字段缺失，supplyAsync 调用中引用导致编译错误 | 在 AbstractCapabilityExecutor 中添加 `protected final Executor llmCallExecutor` 字段和对应构造器参数（17 参数构造器），execute() 模板方法中使用 `llmCallExecutor` 作为 supplyAsync 的第二个参数 |
| **[一般]** 四个 `doExtract*` 方法（DepartmentId/VisitId/PatientId/SessionId）默认行为与任务描述冲突——任务描述要求 instanceof + cast 模式，但 AiRequestBase 存根为空抽象类无 getter 方法 | 采用**选项 A**：为 AiRequestBase 存根添加 4 个 getter 方法（getDepartmentId/getVisitId/getPatientId/getSessionId），均返回 null 作为默认实现。四个 doExtract* 方法改为 `request instanceof AiRequestBase ? ((AiRequestBase) request).getXxx() : null` 模式，既可按任务描述编译通过，返回值又与 AiRequestBase 存根当前状态一致（均为 null）。后续批次 AiRequestBase 补充真实字段时，doExtract* 方法无需修改自动生效 |
