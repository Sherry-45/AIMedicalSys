# 任务指令（v3 r1 修订）

## 动作
NEW

## 任务描述
在 ai-impl/orchestrator/ 包中新增以下 2 个类型：

### 前置步骤：创建编译期依赖的存根类型（Stub）

AbstractCapabilityExecutor 构造器引用了 8 个当前不存在的类型。本任务首先在对应包路径下创建最小存根接口/类，确保编译通过。这些存根将在后续批次实现真实组件时被替换。

#### 存根类型清单

| # | 存根类型 | 形态 | 包路径 | 归属模块 |
|---|---------|------|--------|---------|
| 1 | `PromptTemplateManager` | interface（空） | `com.aimedical.modules.ai.impl.template` | ai-impl |
| 2 | `ModelRouter` | interface（空） | `com.aimedical.modules.ai.impl.router` | ai-impl |
| 3 | `LlmChatService` | interface（空） | `com.aimedical.modules.ai.impl.client` | ai-impl |
| 4 | `StructuredOutputParser` | interface（空） | `com.aimedical.modules.ai.impl.parser` | ai-impl |
| 5 | `AiMetricsCollector` | interface（空） | `com.aimedical.modules.ai.impl.metrics` | ai-impl |
| 6 | `ModelEndpointHealthManager` | class（含无参构造器，空方法体） | `com.aimedical.modules.ai.impl.metrics` | ai-impl |
| 7 | `LocalRuleFallback<T,R>` | interface（空） | `com.aimedical.modules.ai.impl.fallback` | ai-impl |
| 8 | `AiRequestBase` | abstract class（空，含 protected 无参构造器） | `com.aimedical.modules.ai.api.dto.base` | ai-api |

存根接口/类的具体要求：
- **interface 存根**：仅声明 `package` 和 `public interface Xxx { }`，无方法声明，无注解
- **class 存根**（ModelEndpointHealthManager）：`public class ModelEndpointHealthManager { public ModelEndpointHealthManager() {} }`
- **abstract class 存根**（AiRequestBase）：`public abstract class AiRequestBase { protected AiRequestBase() {} }`，归属 `com.aimedical.modules.ai.api.dto.base`（在 ai-api 模块创建 `dto/base/` 子包）
- 仅确保类型可被 import 引用，不添加任何业务逻辑

### 1. CapabilityExecutor<T, R> 泛型接口

**包**: `com.aimedical.modules.ai.impl.orchestrator`
**文件**: `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/CapabilityExecutor.java`

方法签名：
- `CompletableFuture<AiResult<R>> execute(T request, String capabilityId)` — 单项 AI 能力的完整执行管线
- `String getCapabilityId()` — 返回该执行器对应的能力标识（如 "TRIAGE"）
- `Class<T> getInputType()` — 输入 DTO 的 Class 对象
- `Class<R> getOutputType()` — 输出 DTO 的 Class 对象

Javadoc 需声明：request DTO 约定为只读对象（@implNote）；不可变 DTO 与防御性拷贝兼容性说明（@apiNote）。

### 2. AbstractCapabilityExecutor<T, R> 抽象骨架类

**包**: `com.aimedical.modules.ai.impl.orchestrator`
**文件**: `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutor.java`

#### 字段
```
protected final Class<T> inputType
protected final Map<String, Duration> capabilityTimeoutConfig
protected final Map<String, Duration> thinAdapterPerCapabilityConfig
protected final Map<String, Duration> parseTimeoutConfig
protected final Duration thinAdapterTimeout
protected final Duration parseTimeoutDefault
protected volatile long elapsedInDoExecuteInternal
protected static final Set<String> knownPhase4Packages
```

#### 构造器（16 参数全量构造器）
```
AbstractCapabilityExecutor(
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
    ObjectMapper objectMapper
)
```
各参数均可为 null（薄适配器场景前 5 个传 null），构造器仅保存参数到字段，不做 null 校验。

#### execute() 模板方法（final）
伪代码流程（与设计文档 §3.1 一致）：
1. 在 supplyAsync 之前提取 ThreadLocal 上下文（startTime、userId、departmentId、visitId、patientId、sessionId、callerRole、callerId）
2. 防御性拷贝 request（ObjectMapper.convertValue，失败时回退原始 request + WARN 日志）
3. 预计算 inputSummary（truncate 到 500 字符）
4. 降级预检：buildDegradationContext → 执行降级策略链 → 命中则 CompletableFuture.completedFuture(doDegrade(...))
5. 正常请求入 supplyAsync(llmCallExecutor)
6. orTimeout(capabilityTimeout)
7. exceptionally 处理 TimeoutException → doDegrade(...)
8. 返回 CompletableFuture

#### doExecuteInternal() 抽象方法
```
protected abstract AiResult<R> doExecuteInternal(
    long startTime, T request, String capabilityId,
    String departmentId, String userId, String sessionId,
    String callerRole, String callerId, String visitId, String patientId,
    String inputSummary)
```

#### doDegrade() 辅助方法
```
protected AiResult<R> doDegrade(
    long startTime, String degradeReason,
    T request, String capabilityId,
    String departmentId, String callerRole, String callerId,
    String visitId, String patientId, String sessionId,
    String inputSummary, String outputSummary, String promptVersion,
    String modelId, String sentinelReason)
```
职责：记录指标（metricsCollector + slidingWindowMetricsStore）、尝试 localRuleFallback、返回 AiResult.degraded()

#### 变量提取方法
- `protected Map<String, Object> extractVariables(T request)` — 默认使用 ObjectMapper.convertValue(request, Map.class)
- `protected String doExtractDepartmentId(T request)` — 默认：从 AiRequestBase.getDepartmentId() 获取（request instanceof AiRequestBase 时）；否则 null
- `protected String doExtractVisitId(T request)` — 默认：从 AiRequestBase.getVisitId() 获取
- `protected String doExtractPatientId(T request)` — 默认：从 AiRequestBase.getPatientId() 获取
- `protected String doExtractSessionId(T request)` — 默认：从 AiRequestBase.getSessionId() 获取
- `protected String extractOutputSummary(Object result)` — 默认 return result != null ? result.toString() : null
- `protected String extractCallerRole()` — 委托 RequestContextUtils（当前先返回 null，RequestContextUtils 未实现前可暂时返回 null）
- `protected String extractCallerId()` — 委托 RequestContextUtils（同上）
- `protected DegradationReason refineTimeoutReason(String capabilityId, long elapsedInDoExecuteInternal, T request)` — 默认 return DegradationReason.TIMEOUT

#### executeStandardPipeline() 方法（protected final）
占位方法，用于供子类在完成前置阶段后复用标准管线。当前抛出 UnsupportedOperationException("待后续实现")，待管线组件就绪后填充。

#### isKnownPhase4BusinessException() 辅助方法
```
protected boolean isKnownPhase4BusinessException(Throwable cause)
```
通过异常类全名的包路径前缀匹配检测是否属于已知 Phase 4 模块业务异常。knownPhase4Packages 静态集合包含：
```java
protected static final Set<String> knownPhase4Packages = Set.of(
    "com.aimedical.modules.diagnosis",
    "com.aimedical.modules.inspection",
    "com.aimedical.modules.labtest",
    "com.aimedical.modules.image",
    "com.aimedical.modules.examination",
    "com.aimedical.modules.execution"
);
```

### 测试要求

测试文件：`AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutorTest.java`

测试策略：
- 使用 Mockito 作为 Mock 框架
- 创建 AbstractCapabilityExecutor 的匿名子类（或测试专用子类）用于测试，该子类实现 doExecuteInternal() 返回固定 AiResult.success(data)
- 降级策略链 Mock：使用 MockDegradationStrategy（实现 DegradationStrategy 接口，通过构造函数控制 shouldDegrade() 返回值）
- 超时测试：设置极短 capabilityTimeout（如 Duration.ofMillis(1)）触发 TimeoutException
- 防御性拷贝测试：创建可变 DTO，验证拷贝后修改原始 DTO 不影响拷贝副本

测试覆盖：
1. **CapabilityExecutor 接口契约验证**：验证接口方法签名正确
2. **AbstractCapabilityExecutor 构造器测试**：验证 16 参构造器正确保存输入参数
3. **降级预检路径测试**（Mock 降级策略始终返回 true）：
   - 验证预检降级时 CompletableFuture 直接 complete（不入线程池）
   - 验证 doDegrade 被调用（可验证 metricsCollector.record 调用次数为 1）
4. **正常路径测试**（Mock 降级策略始终返回 false）：
   - 验证 doExecuteInternal 被调用
   - 验证超时兜底生效（设置极短超时，验证 TimeoutException 被捕获并触发降级）
5. **防御性拷贝测试**：
   - 验证拷贝成功时 request 不被下游修改
   - 验证拷贝失败时回退到原始 request（WARN 日志）
6. **字段提取方法测试**：
   - doExtractDepartmentId/doExtractVisitId/doExtractPatientId/doExtractSessionId
   - extractVariables、extractOutputSummary
   - refineTimeoutReason
7. **isKnownPhase4BusinessException** 测试：已知包名返回 true，未知返回 false

## 选择理由
CapabilityExecutor 是 AiOrchestrator 路由的核心接口，AbstractCapabilityExecutor 是所有 13 项能力执行器的公共基类。本任务依赖 8 个尚不存在的类型（通过创建存根解决），其余依赖（AiResult、DegradationContext、DegradationReason、SlidingWindowMetricsStore）已在之前任务中完成。不影响已有 MockAiService 和 FallbackAiService 的生效路径。

## 任务上下文
- CapabilityExecutor 接口定义执行管线契约，AiOrchestrator 通过其 Map<String, CapabilityExecutor> 映射表路由请求
- AbstractCapabilityExecutor 封装了所有子类共用的降级预检、超时兜底、指标采集逻辑
- 13 个具体子类（7 底座 + 6 薄适配器）均继承此类，只需特化 doExecuteInternal()
- 降级策略链通过 AtomicReference<Map<String, List<DegradationStrategy>>> 注入，支持运行时热加载
- 8 个存根类型（PromptTemplateManager、ModelRouter、LlmChatService、StructuredOutputParser、AiMetricsCollector、ModelEndpointHealthManager、LocalRuleFallback、AiRequestBase）在本任务中创建，后续批次实现真实组件时替换
- extractCallerRole/extractCallerId 暂返回 null（RequestContextUtils 未实现）
- executeStandardPipeline() 当前抛出 UnsupportedOperationException

## 已有代码上下文
- `AiResult.java` — ai-api 已有，含 success()/failure()/degraded() 工厂方法
- `DegradationContext.java` — ai-api/degradation/ 已有，含 Builder 模式
- `DegradationReason.java` — ai-api/degradation/ 已有，8 个枚举常量
- `SlidingWindowMetricsStore.java` — ai-impl/metrics/ 已有，含 buildDegradationContext()
- `NoOpDegradationStrategy.java` — ai-impl/degradation/ 已有，可作为策略 Mock
- 依赖汇总：ai-impl 当前 pom.xml 已包含 spring-boot-starter、spring-boot-starter-web、spring-boot-starter-test、ai-api；jackson-databind 通过 spring-boot-starter-web 传递引入，ObjectMapper 可直接 @Autowired
- 无 Guava 依赖，无 commons-lang3 依赖，字符串截断可手动实现或用 substring

---

## 修订说明（v3 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] AbstractCapabilityExecutor 构造器引用了 8 个当前不存在的类型，代码无法编译 | 采用 Option A：在前置步骤中创建 8 个最小存根类型（7 个在 ai-impl 子包，1 个在 ai-api/dto/base/）。存根仅声明类型骨架，无方法体，确保编译通过。后续批次实现真实组件时替换存根。 |
| [一般] 计划摘要遗漏 executeStandardPipeline、isKnownPhase4BusinessException、refineTimeoutReason、knownPhase4Packages | 已在任务描述中补充这些成员的完整说明（见 executeStandardPipeline() 占位方法、isKnownPhase4BusinessException() 辅助方法、refineTimeoutReason() 方法、knownPhase4Packages 静态字段四节） |
| [一般] 计划未提及测试规划 | 已补充测试策略（Mockito 框架、降级策略链 Mock 方法、超时测试方法、防御性拷贝测试方法）及 7 类测试覆盖的详细说明 |
