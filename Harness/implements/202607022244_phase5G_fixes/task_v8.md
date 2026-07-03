# 任务指令（v8）

## 动作
NEW

## 任务描述
修复指标与健康管理 7 项问题（T15/T16/T17/T25/T51/T52/T59），涉及 4 个源文件：

| 任务 | 严重度 | 描述 | 文件 |
|------|--------|------|------|
| T15 | 严重 | `LoggingMetricsCollector.record()` 的 `@Async` 未绑定专用线程池 `metricsAsyncExecutor` | `LoggingMetricsCollector.java` |
| T16 | 严重 | `LoggingMetricsCollector` 中 `AiCallRecord→AiCallLogEntity` 字段映射 7 处硬编码 null/0 | `LoggingMetricsCollector.java` |
| T17 | 严重 | `AiCallRecord` 缺少 7 个关键字段（与 `AiCallLogEntity` 不对等）及 `success()/failure()/degraded()` 工厂方法 | `AiCallRecord.java` |
| T25 | 一般 | `AiCallRecord` 字段顺序和定义与设计文档不一致 | `AiCallRecord.java` |
| T51 | 一般 | `SlidingWindowMetricsStore.windowSeconds` 使用 `volatile long` 而非 `AtomicLong`；`setWindowSeconds()` 缺参数校验 | `SlidingWindowMetricsStore.java` |
| T52 | 一般 | `SlidingWindowMetricsStore` 读取方法未执行快照复制 | `SlidingWindowMetricsStore.java` |
| T59 | 一般 | `ModelEndpointHealthManager` UNAVAILABLE→成功探测后直接恢复 CONNECTED，跳过 DEGRADED 中间态 | `ModelEndpointHealthManager.java` |

## 选择理由
R7（熔断测试修复，2503 pass / 0 fail）已验证通过。R8 按计划推进指标与健康管理——这些任务构成完整的可观测性管线（数据模型→采集→存储→健康判定），4 个源文件、7 项修复高度耦合（T16/T17/T25 均围绕 AiCallRecord 字段对齐与 LoggingMetricsCollector 映射），合并一轮处理。

## 任务上下文

### 设计文档依据
- `Docs/06_ood_phase5_G.md` §3.5「AiCallRecord — AI 调用记录值对象」— 字段定义表（24 字段）和工厂方法签名
- `Docs/06_ood_phase5_G.md` §3.5「SlidingWindowMetricsStore — 调用指标滑动窗口存储」— 快照复制策略和线程安全协议
- `Docs/06_ood_phase5_G.md` §3.5「ModelEndpointHealthManager — 模型端点健康状态管理器」— 状态跃迁定义

### T15: @Async 线程池绑定
当前 `LoggingMetricsCollector.record()`:
```java
@Override
@Async
public void record(AiCallRecord record) {
```
改为:
```java
@Override
@Async("metricsAsyncExecutor")
public void record(AiCallRecord record) {
```

### T17 + T25: AiCallRecord 字段补齐与对齐
**设计文档要求的 AiCallRecord 字段**（24 个，与 `AiCallLogEntity` 对等，不包含 JPA 主键 `id`）：
| # | 字段 | 类型 | 当前状态 |
|---|------|------|---------|
| 1 | callTime | LocalDateTime | 缺失 → 新增 |
| 2 | capabilityId | String | 已有 |
| 3 | capabilityName | String | 缺失 → 新增（工厂方法内部从 `capabilityNameMapping` 解析，见设计文档 §3.5 字段填充策略） |
| 4 | visitId | String | 已有 |
| 5 | patientId | String | 已有 |
| 6 | departmentId | String | 已有 |
| 7 | callerRole | String | 已有 |
| 8 | callerId | String | 已有 |
| 9 | inputSummary | String | 缺失 → 新增 |
| 10 | outputSummary | String | 缺失 → 新增 |
| 11 | degraded | boolean | 已有 |
| 12 | degradationReason | String | 已有 |
| 13 | elapsedMs | long | 已有 |
| 14 | errorCode | String | 缺失 → 新增 |
| 15 | errorMessage | String | 缺失 → 新增 |
| 16 | modelId | String | 已有 |
| 17 | retryCount | int | 缺失 → 新增 |
| 18 | sessionId | String | 已有 |
| 19 | promptVersion | Integer | 已有 |
| 20 | promptTokens | int | 已有 |
| 21 | completionTokens | int | 已有 |
| 22 | totalTokens | Integer | 缺失 → 新增 |

**注意**: `callTime` 字段类型为 `LocalDateTime`，需导入 `java.time.LocalDateTime`。
**注意**: `capabilityName` 由工厂方法内部通过 `capabilityNameMapping` 解析——但当前设计要求仅保持字段对等，`capabilityNameMapping` 注入暂不在此轮范围（`capabilityName` 保留为构造函数参数或 setter，由调用方传入）。

**静态工厂方法签名**（按设计文档 §3.5）:
```java
static AiCallRecord success(String capabilityId, LocalDateTime callTime, long elapsedMs,
    String departmentId, String modelId, int retryCount,
    Integer promptTokens, Integer completionTokens,
    String inputSummary, String outputSummary,
    String visitId, String patientId, String sessionId,
    String callerRole, String callerId,
    Integer promptVersion, String sentinelReason)

static AiCallRecord failure(String capabilityId, LocalDateTime callTime, long elapsedMs,
    String errorCode, String errorMessage,
    String departmentId, String inputSummary,
    String visitId, String patientId, String sessionId,
    String callerRole, String callerId,
    Integer promptVersion, String sentinelReason)

static AiCallRecord degraded(String capabilityId, LocalDateTime callTime, long elapsedMs,
    String degradationReason, String modelId,
    String departmentId, String inputSummary,
    String visitId, String patientId, String sessionId,
    String callerRole, String callerId,
    String outputSummary, Integer promptVersion, String sentinelReason)
```

**sentinelReason 处理**: 当 `sentinelReason="EXPERIMENT_ASSIGN_ERROR"` 时，内部将 `promptVersion` 覆写为 `-1` 哨兵值。

### T16: 修复 LoggingMetricsCollector 字段映射
当前硬编码（配合 T17 新增字段后改为从 AiCallRecord 读取）:
- `entity.setInputSummary(null)` → `entity.setInputSummary(record.getInputSummary())`
- `entity.setOutputSummary(null)` → `entity.setOutputSummary(record.getOutputSummary())`
- `entity.setErrorCode(null)` → `entity.setErrorCode(record.getErrorCode())`
- `entity.setErrorMessage(null)` → `entity.setErrorMessage(record.getErrorMessage())`
- `entity.setRetryCount(0)` → `entity.setRetryCount(record.getRetryCount())`
- `entity.setTotalTokens(null)` → `entity.setTotalTokens(record.getTotalTokens())`
- `entity.setCallTime(LocalDateTime.now())` → `entity.setCallTime(record.getCallTime())`（工厂方法调用处已记录 callTime，不应覆盖为 now）

### T51: SlidingWindowMetricsStore.windowSeconds → AtomicLong
当前:
```java
private volatile long windowSeconds = 60;
```
改为:
```java
private final AtomicLong windowSeconds = new AtomicLong(60);
```
所有使用 `windowSeconds` 的地方改为 `windowSeconds.get()` / `windowSeconds.set()`。

`setWindowSeconds()`:
```java
public void setWindowSeconds(long windowSeconds) {
    if (windowSeconds <= 0) {
        throw new IllegalArgumentException("windowSeconds must be > 0, but got: " + windowSeconds);
    }
    this.windowSeconds.set(windowSeconds);
}
```

### T52: SlidingWindowMetricsStore 快照复制
在读取方法（`getFailureRate()`/`getEffectiveFailureRate()`/`getAverageElapsed()`/`buildDegradationContext()`）的 `synchronized (deque)` 块中，惰性淘汰后通过 `deque.toArray(new WindowedEvent[0])` 复制数组，然后在 `synchronized` 块外遍历副本进行计算。模板：

```java
public double getFailureRate(String capabilityId) {
    Deque<WindowedEvent> deque = windows.get(capabilityId);
    if (deque == null) {
        return 0.0;
    }
    WindowedEvent[] snapshot;
    synchronized (deque) {
        long cutoff = System.currentTimeMillis() - windowSeconds.get() * 1000;
        while (!deque.isEmpty() && deque.peekFirst().timestamp < cutoff) {
            deque.removeFirst();
        }
        snapshot = deque.toArray(new WindowedEvent[0]);
    }
    long successCount = 0;
    long failureCount = 0;
    for (WindowedEvent event : snapshot) {
        if (event.type == SlidingWindowMetricsStore.EventType.NORMAL_SUCCESS) {
            successCount++;
        } else if (event.type == SlidingWindowMetricsStore.EventType.FAILURE) {
            failureCount++;
        }
    }
    long denominator = successCount + failureCount;
    return denominator == 0 ? 0.0 : (double) failureCount / denominator;
}
```

对其他读取方法（`getEffectiveFailureRate()`/`getAverageElapsed()`/`buildDegradationContext()`）应用相同模式。

### T59: ModelEndpointHealthManager UNAVAILABLE→DEGRADED 中间态
当前 UNAVAILABLE case（line 95-104）:
```java
} else if (current == EndpointHealthState.UNAVAILABLE) {
    if (success) {
        state.healthState.set(EndpointHealthState.CONNECTED);  // 跳过DEGRADED
        // ... 计数器重置
    } else {
        state.lastProbeTime.set(System.currentTimeMillis());
    }
}
```
改为:
```java
} else if (current == EndpointHealthState.UNAVAILABLE) {
    if (success) {
        state.healthState.set(EndpointHealthState.DEGRADED);  // 先到DEGRADED
        state.consecutiveSlowCalls.set(0);
        state.consecutiveFailures.set(0);
        state.consecutiveSuccesses.set(0);
        state.cumulativeFailures.set(0);
    } else {
        state.lastProbeTime.set(System.currentTimeMillis());
    }
}
```
DEGRADED 下连续成功 3 次回到 CONNECTED（已有逻辑，不变）。DEGRADED 下再次失败 5 次回到 UNAVAILABLE（已有逻辑，不变）。

## 已有代码上下文

### AiCallRecord.java (ai-impl/metrics/)
当前为不可变值对象（所有字段 `final`），15 个字段通过全参构造器初始化，15 个 getter 方法。位于 `com.aimedical.modules.ai.impl.metrics` 包。

### LoggingMetricsCollector.java (ai-impl/metrics/)
实现 `AiMetricsCollector` 接口，`@Service` + `@Async` 注解。`record()` 方法将 AiCallRecord 映射到 AiCallLogEntity 后通过 repository 持久化。当前 7 处硬编码 null/0。

### SlidingWindowMetricsStore.java (ai-impl/metrics/)
`@Component`，内部 `ConcurrentHashMap<String, Deque<WindowedEvent>> windows`。提供 `recordSuccess/Degraded/Failure` 写入方法和 `getFailureRate/getEffectiveFailureRate/getAverageElapsed/buildDegradationContext` 读取方法。`windowSeconds` 使用 `volatile long`。读取方法在 `synchronized (deque)` 块内直接遍历 deque。

### ModelEndpointHealthManager.java (ai-impl/metrics/)
`@Service`，内部静态类 `EndpointState` 管理每个 endpoint 的状态。`recordCallResult()` 实现 CONNECTED/DEGRADED/UNAVAILABLE 三态跃迁。UNAVAILABLE→CONNECTED 跳过 DEGRADED。

### AiCallLogEntity.java (ai-impl/metrics/)
已有完整字段（24 个 + JPA 主键 `id`），全参构造器和 getter/setter。不需要修改。

### 涉及测试文件
- `AiCallRecordTest.java` — 需更新验证新增字段和工厂方法
- `LoggingMetricsCollectorTest.java` — 需更新验证新增字段映射和 `@Async("metricsAsyncExecutor")`
- `SlidingWindowMetricsStoreTest.java` — 需更新验证 AtomicLong 行为和快照复制
- `ModelEndpointHealthManagerTest.java` — 需更新 `probeSuccessShouldTransitionFromUNAVAILABLEToCONNECTED` 测试（期望从 UNAVAILABLE→DEGRADED 而非 CONNECTED）
