# 详细设计（v1）

## 概述

本设计覆盖 Phase5_G 底座核心骨架的基础类型：ai-api 模块的降级基础设施扩展（DegradationReason 枚举、DegradationStrategy 接口扩展、DegradationContext 字段扩展）和 ai-impl 模块的滑动窗口指标存储（SlidingWindowMetricsStore）。四个交付物无前置依赖，可独立开发并验证。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/degradation/DegradationReason.java` | 新建 | 8 个枚举常量的降级原因/错误码集中管理 |
| `AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/degradation/DegradationStrategy.java` | 修改 | 新增 default getOrder() 方法（二进制兼容） |
| `AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/degradation/DegradationContext.java` | 修改 | 扩展 7 个字段 + Builder 模式 + 校验方法 |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/metrics/SlidingWindowMetricsStore.java` | 新建 | 滑动窗口指标存储，线程安全 @Component |
| `AIMedical/backend/modules/ai/ai-api/src/test/java/com/aimedical/modules/ai/api/degradation/DegradationReasonTest.java` | 新建 | 枚举常量、getCode/getMessage、toString 格式测试 |
| `AIMedical/backend/modules/ai/ai-api/src/test/java/com/aimedical/modules/ai/api/degradation/DegradationContextTest.java` | 新建 | Builder 模式、序列化、三元方法测试 |
| `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/metrics/SlidingWindowMetricsStoreTest.java` | 新建 | 记录/查询/惰性淘汰/并发安全测试 |

## 类型定义

### DegradationReason

**形态**：enum
**包路径**：`com.aimedical.modules.ai.api.degradation`
**职责**：集中管理所有降级原因和错误码常量，消除字符串字面量

```
public enum DegradationReason {
    NO_AVAILABLE_ROUTE("NoAvailableRoute", "No available model route"),
    ENDPOINT_UNAVAILABLE("EndpointUnavailable", "Endpoint is unavailable"),
    CIRCUIT_BREAKER_OPEN("CircuitBreakerOpen", "Circuit breaker is open"),
    PARSE_FAILURE("ParseFailure", "LLM output parse failure"),
    TIMEOUT("Timeout", "Request timeout"),
    STRATEGY_TRIGGERED("StrategyTriggered", "Degradation strategy triggered"),
    INTERNAL_ERROR("InternalError", "Internal error"),
    INFRASTRUCTURE_ERROR("InfrastructureError", "Infrastructure error");

    private final String code;
    private final String message;
}
```

**公开接口**：
- `String getCode()` — 返回 code 值
- `String getMessage()` — 返回 message 值
- `static DegradationReason fromCode(String code)` — 按 code 查找枚举，未匹配返回 null
- `String toString()` — 返回 `code` 值（与拼接 ":subType" 模式兼容）

**构造方式**：JVM 类加载时初始化 8 个预定义常量
**类型关系**：隐式继承 `java.lang.Enum<DegradationReason>`

### DegradationStrategy

**形态**：interface
**包路径**：`com.aimedical.modules.ai.api.degradation`
**职责**：降级策略契约，新增排序支持以支持策略链有序执行

```
public interface DegradationStrategy {
    boolean shouldDegrade(DegradationContext context);
    default int getOrder() { return 0; }
}
```

**公开接口**：
- `boolean shouldDegrade(DegradationContext context)` — 判定是否触发降级（已有，不变）
- `default int getOrder()` — 返回策略排序优先级，升序执行。默认 0 确保二进制兼容

**类型关系**：NoOpDegradationStrategy、TimeoutDegradationStrategy、CircuitBreakerDegradationStrategy 等实现类

### DegradationContext

**形态**：class implements Serializable
**包路径**：`com.aimedical.modules.ai.api.degradation`
**职责**：降级判定上下文，扩展实时窗口指标字段，支持 Jackson 新旧 JSON 互读

**类型签名**：
```
@JsonIgnoreProperties(ignoreUnknown = true)
public class DegradationContext implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final long DEFAULT_TTL_MILLIS = 60_000L;

    // Phase 0 已有字段
    private String serviceName;
    private String operationName;

    // Phase5_G 新增字段
    private Integer invocationCount;       // 默认 null，包装类型确保 Jackson 反序列化旧 JSON 安全
    private long lastFailureTime;          // 默认 0L
    private long elapsedTime;              // 默认 0L
    private String requestType;            // 可空
    private Integer failureCount;          // 默认 null，包装类型
    private String departmentId;           // 可空
    private long serializedTimestamp;      // 默认 0L，秒级时间戳
}
```

**公开接口**：
- 无参构造器（保留，仅初始化 Phase 0 字段，新增字段取语言默认值）
- 全字段 getter/setter（serviceName/operationName 已有，新增 7 个字段各一对）
- `void postDeserializationValidate()` — 检测所有统计字段（invocationCount、failureCount、elapsedTime）均为默认值（null/0L）时，将 requestType 置空标记为"未初始化"
- `boolean isFresh()` — 若 serializedTimestamp > 0 则比较 `System.currentTimeMillis() - serializedTimestamp <= DEFAULT_TTL_MILLIS`；若 serializedTimestamp == 0，直接返回 false（表示数据未构建）
- `boolean isInitialized()` — 返回 `invocationCount != null && failureCount != null`（即 Builder 或 SlidingWindowMetricsStore 至少填充了这两个统计字段）

**构造方式**：
- 无参构造器（零值实例，Phase 0 兼容）
- Builder 模式：静态内部类 `DegradationContext.Builder`，所有字段通过 Builder 方法赋值，`build()` 方法返回 DegradationContext 实例

**Builder 接口**：
```
public static class Builder {
    public Builder serviceName(String)
    public Builder operationName(String)
    public Builder invocationCount(Integer)
    public Builder lastFailureTime(long)
    public Builder elapsedTime(long)
    public Builder requestType(String)
    public Builder failureCount(Integer)
    public Builder departmentId(String)
    public Builder serializedTimestamp(long)
    public DegradationContext build()
}
```

**类型关系**：被 `SlidingWindowMetricsStore.buildDegradationContext()` 构建，被 `DegradationStrategy.shouldDegrade()` 消费

### SlidingWindowMetricsStore

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.metrics`
**注解**：`@Component`
**职责**：为每个能力标识维护独立的调用指标滑动窗口，为降级策略提供实时数据源

**内部数据结构**：
```
private final ConcurrentHashMap<String, Deque<WindowedEvent>> windows;
private volatile long windowSeconds;                  // 默认 60
private final int maxEventsPerCapability;             // 默认 10000
```

**公开接口**：
```
void recordSuccess(String capabilityId, long elapsedMs)
void recordDegraded(String capabilityId, long elapsedMs)
void recordFailure(String capabilityId)
double getFailureRate(String capabilityId)
double getEffectiveFailureRate(String capabilityId)
double getAverageElapsed(String capabilityId)
DegradationContext buildDegradationContext(String capabilityId, String requestType)
```

各方法行为：

**recordSuccess/recordDegraded/recordFailure**：
1. `windows.computeIfAbsent(capabilityId, k -> new LinkedList<>())` 获取或创建 Deque
2. `synchronized (deque)` 块内执行：
   - 惰性淘汰：从头部移除 `timestamp < cutoff` 的事件（`cutoff = System.currentTimeMillis() - windowSeconds * 1000`）
   - 上限淘汰：若队列长度 >= maxEventsPerCapability，从头部移除过剩事件
   - 尾部追加新 `WindowedEvent(type, System.currentTimeMillis(), elapsedMs)`
3. recordFailure 的 elapsedMs 传入 0

**getFailureRate**：
1. `windows.get(capabilityId)` 获取 Deque，若 null 返回 0.0
2. `synchronized (deque)` 块内：
   - 惰性淘汰
   - 快照复制（`toArray(new WindowedEvent[0])` 或流式收集）
   - 统计 NORMAL_SUCCESS 和 FAILURE 计数
   - 返回 `failureCount / (successCount + failureCount)`，分母为 0 时返回 0.0

**getEffectiveFailureRate**：
1. 同上获取 Deque
2. `synchronized (deque)` 块内：
   - 惰性淘汰 + 快照复制
   - 统计三类事件计数
   - 返回 `failureCount / (successCount + failureCount + degradedCount)`，分母为 0 时返回 0.0
   - **公式说明**：根据 OOD 设计文档 §3.5 第 2515 行定义，有效失败率 = recordFailure / (recordSuccess + recordDegraded + recordFailure)。降级事件仅计入分母反映"整体系统退化程度"，不计入分子——因为降级兜底成功不等于 LLM 调用失败。此公式区别于 `getFailureRate()`（分母不含 degraded）

**getAverageElapsed**：
1. 同上获取 Deque
2. `synchronized (deque)` 块内：
   - 惰性淘汰 + 快照复制
   - 统计所有事件（含 FAILURE，其 elapsedMs=0）的 elapsedMs 总和和数量
   - 返回 `totalElapsed / count`，count 为 0 时返回 0.0

**buildDegradationContext**：
1. 获取 Deque
2. `synchronized (deque)` 块内：
   - 惰性淘汰 + 快照复制
   - 统计 successCount、failureCount、degradedCount
   - 计算 invocationCount = successCount + failureCount + degradedCount
   - 计算 lastFailureTime = 最近 FAILURE 事件的 timestamp（若无 FAILURE 则 = 0）
   - 计算 elapsedTime = 所有 NORMAL_SUCCESS/DEGRADED 事件的 elapsedMs 平均值
   - 构建 DegradationContext：使用 Builder 填充所有统计字段 + requestType + serializedTimestamp = System.currentTimeMillis()
3. 返回构建的 DegradationContext 实例

**线程安全协议**：
- `ConcurrentHashMap` 保证 `computeIfAbsent` 的原子创建
- 每个能力标识的 `Deque<WindowedEvent>` 使用 `synchronized (deque)` 块保护所有读/写操作，同一 Deque 的淘汰和追加互斥
- 写入方法（recordXxx）和读取方法（getXxx/buildDegradationContext）在 Deque 级别互斥
- `maxEventsPerCapability` 和 `windowSeconds` 使用 `volatile` 保证可见性

### WindowedEvent

**形态**：static inner class
**归属**：`SlidingWindowMetricsStore`
**职责**：滑动窗口中的单次事件值对象

```
static class WindowedEvent {
    final EventType type;
    final long timestamp;
    final long elapsedMs;    // 0 for FAILURE
}
```

**构造方式**：仅通过构造器 `WindowedEvent(EventType type, long timestamp, long elapsedMs)` 创建，字段公开 final 无需 getter

### EventType

**形态**：static inner enum
**归属**：`SlidingWindowMetricsStore`
**职责**：标记事件类型

```
enum EventType {
    NORMAL_SUCCESS,
    DEGRADED,
    FAILURE
}
```

## 错误处理

- **DegradationReason**：纯枚举，无运行时错误。
- **DegradationStrategy.getOrder()**：default 方法，无异常。
- **DegradationContext**：Builder 的 build() 方法不做校验（允许零值构造）；postDeserializationValidate() 为幂等方法，无异常抛出。
- **SlidingWindowMetricsStore**：所有公有方法入参 `capabilityId` 不为 null（传入 null 时 ConcurrentHashMap 抛出 NPE 属合理崩溃，不在方法内捕获）；`buildDegradationContext()` 对不存在的 capabilityId 返回零值 DegradationContext（invocationCount=null, failureCount=null）。

## 行为契约

| 方法 | 前置条件 | 后置条件 | 特殊规则 |
|------|---------|---------|---------|
| DegradationReason.fromCode() | code 不为 null | 返回匹配枚举或 null | null-safe，不抛异常 |
| DegradationContext.postDeserializationValidate() | 无 | 若 invocCount/failureCount/elapsedTime 全默认值，则 requestType=null | 幂等，可重复调用 |
| DegradationContext.isFresh() | 无 | 返回 boolean | serializedTimestamp=0 时返回 false |
| DegradationContext.Builder.build() | 无 | 返回 DegradationContext | 未显式赋值的字段取默认值（null/0/0L） |
| SlidingWindowMetricsStore.recordXxx | capabilityId 不为 null | 事件已追加到窗口 | 惰性淘汰同步执行 |
| SlidingWindowMetricsStore.getXxxRate | capabilityId 不为 null | 返回 0.0~1.0 的 double | 无事件时返回 0.0 |
| SlidingWindowMetricsStore.buildDegradationContext | capabilityId 不为 null | 返回 DegradationContext（非 null） | 无窗口时返回零值 context |

## 依赖关系

| 类型 | 依赖的已有类型 | 被哪些后续类型依赖 |
|------|---------------|-----------------|
| DegradationReason | 无 | AbstractCapabilityExecutor（降级路径）、AiResult（降级原因参数）、DegradationStrategy 实现 |
| DegradationStrategy | DegradationContext | AbstractCapabilityExecutor（降级预检）、FallbackAiService（过渡期） |
| DegradationContext | 无 | DegradationStrategy 实现、SlidingWindowMetricsStore.buildDegradationContext() |
| SlidingWindowMetricsStore | DegradationContext (ai-api) | AbstractCapabilityExecutor、CircuitBreakerDegradationStrategy、TimeoutDegradationStrategy、AiOrchestrator |

**暴露给后续任务的公开接口**：
- `DegradationReason.getCode()` / `getMessage()` — 在所有降级路径中引用枚举常量
- `DegradationContext.Builder` — AiOrchestrator 构建降级上下文使用
- `SlidingWindowMetricsStore` 全部公有方法 — 被 AbstractCapabilityExecutor 的执行模块调用

## 修订说明（v1 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] getEffectiveFailureRate 公式与 OOD 设计文档不符：当前设计为 `(failureCount + degradedCount) / (successCount + failureCount + degradedCount)`，但 OOD 文档 §3.5 第 2515 行定义为 `recordFailure / (recordSuccess + recordDegraded + recordFailure)`。降级事件仅计入分母不计入分子，因为降级兜底成功不等于 LLM 调用失败 | 将 `getEffectiveFailureRate` 公式修正为 `failureCount / (successCount + failureCount + degradedCount)`，新增公式说明引用 OOD 设计文档依据。分子仅含 failureCount，分母含三类事件 |
| [轻微] 文件路径格式与任务约定不一致：设计使用相对路径 `ai-api/src/main/java/...` 而任务使用完整模块路径 `AIMedical/backend/modules/ai/ai-api/src/main/java/...` | 统一文件规划表中所有路径为任务约定的完整格式 `AIMedical/backend/modules/ai/...` |
