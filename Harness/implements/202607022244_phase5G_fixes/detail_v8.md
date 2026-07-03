# 详细设计（v8）

## 概述

修复指标与健康管理 7 项问题（T15/T16/T17/T25/T51/T52/T59），涉及 4 个源文件：`AiCallRecord.java`、`LoggingMetricsCollector.java`、`SlidingWindowMetricsStore.java`、`ModelEndpointHealthManager.java`。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/.../metrics/AiCallRecord.java` | 修改 | T17+T25：补齐至 23 字段 + 3 个静态工厂方法 + promptVersion 类型改为 Integer |
| `ai-impl/.../metrics/LoggingMetricsCollector.java` | 修改 | T15：`@Async` 绑定 `metricsAsyncExecutor`；T16：7 处硬编码改为从 record 读取 |
| `ai-impl/.../metrics/SlidingWindowMetricsStore.java` | 修改 | T51：`windowSeconds` 改为 `AtomicLong` + 参数校验；T52：4 个读取方法执行快照复制 |
| `ai-impl/.../metrics/ModelEndpointHealthManager.java` | 修改 | T59：UNAVAILABLE 下探测成功先到 DEGRADED 再经 3 次成功到 CONNECTED |

## 类型定义

### AiCallRecord

**形态**：class（不可变值对象，所有字段 final）
**包路径**：`com.aimedical.modules.ai.impl.metrics`

**字段定义**（23 个，与 AiCallLogEntity 对等，不含 JPA 主键 id + 保留上游使用的 userId）：

| # | 字段 | 类型 | 当前状态 | 变更 |
|---|------|------|---------|------|
| 1 | callTime | LocalDateTime | 缺失 | 新增 |
| 2 | capabilityId | String | 已有 | 不变 |
| 3 | capabilityName | String | 缺失 | 新增 |
| 4 | visitId | String | 已有 | 不变 |
| 5 | patientId | String | 已有 | 不变 |
| 6 | departmentId | String | 已有 | 不变 |
| 7 | callerRole | String | 已有 | 不变 |
| 8 | callerId | String | 已有 | 不变 |
| 9 | userId | String | 已有 | 加入三个工厂方法签名，由调用方传入 |
| 10 | inputSummary | String | 缺失 | 新增 |
| 11 | outputSummary | String | 缺失 | 新增 |
| 12 | degraded | boolean | 已有 | 不变 |
| 13 | degradationReason | String | 已有 | 由 degradeReason 改名为 degradationReason |
| 14 | elapsedMs | long | 已有 | 不变 |
| 15 | errorCode | String | 缺失 | 新增 |
| 16 | errorMessage | String | 缺失 | 新增 |
| 17 | modelId | String | 已有 | 不变 |
| 18 | retryCount | int | 缺失 | 新增 |
| 19 | sessionId | String | 已有 | 不变 |
| 20 | promptVersion | Integer | String | 类型变更 String→Integer |
| 21 | promptTokens | int | 已有 | 不变 |
| 22 | completionTokens | int | 已有 | 不变 |
| 23 | totalTokens | Integer | 缺失 | 新增 |

**构造方式**：
- 保留全参构造器（23 参数，类型按上表）
- 新增 3 个静态工厂方法（见下方签名），全参构造器标记为 `private`，仅工厂方法使用

**静态工厂方法**：

```java
static AiCallRecord success(String capabilityId, String capabilityName,
    LocalDateTime callTime, long elapsedMs,
    String departmentId, String modelId, int retryCount,
    Integer promptTokens, Integer completionTokens,
    String inputSummary, String outputSummary,
    String visitId, String patientId, String sessionId,
    String callerRole, String callerId, String userId,
    Integer promptVersion, String sentinelReason)

static AiCallRecord failure(String capabilityId, String capabilityName,
    LocalDateTime callTime, long elapsedMs,
    String errorCode, String errorMessage,
    String departmentId, String inputSummary,
    String visitId, String patientId, String sessionId,
    String callerRole, String callerId, String userId,
    Integer promptVersion, String sentinelReason)

static AiCallRecord degraded(String capabilityId, String capabilityName,
    LocalDateTime callTime, long elapsedMs,
    String degradationReason, String modelId,
    String departmentId, String inputSummary,
    String visitId, String patientId, String sessionId,
    String callerRole, String callerId, String userId,
    String outputSummary, Integer promptVersion, String sentinelReason)
```

**sentinelReason 处理**：当 `sentinelReason` 等于 `"EXPERIMENT_ASSIGN_ERROR"` 时，工厂方法内部将 `promptVersion` 覆写为 `-1`。

**公开接口**：23 个 getter（`getCallTime()` / `getCapabilityId()` / ... / `getTotalTokens()`）

### LoggingMetricsCollector

**形态**：class（已有）
**包路径**：`com.aimedical.modules.ai.impl.metrics`

**T15 变更**：
- `@Async` → `@Async("metricsAsyncExecutor")`

**T16 变更**（`record()` 方法内 7 处映射修复）：

| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 30 | `entity.setCallTime(LocalDateTime.now())` | `entity.setCallTime(record.getCallTime())` |
| 39 | `entity.setInputSummary(null)` | `entity.setInputSummary(record.getInputSummary())` |
| 40 | `entity.setOutputSummary(null)` | `entity.setOutputSummary(record.getOutputSummary())` |
| 44 | `entity.setErrorCode(null)` | `entity.setErrorCode(record.getErrorCode())` |
| 45 | `entity.setErrorMessage(null)` | `entity.setErrorMessage(record.getErrorMessage())` |
| 47 | `entity.setRetryCount(0)` | `entity.setRetryCount(record.getRetryCount())` |
| 52 | `entity.setTotalTokens(null)` | `entity.setTotalTokens(record.getTotalTokens())` |

**额外调整**：
- `record.getDegradeReason()` → `record.getDegradationReason()`（字段已由 degradeReason 统一重命名为 degradationReason）
- `parsePromptVersion()` 去除（AiCallRecord.promptVersion 已是 Integer，无需 String→Integer 解析）

### SlidingWindowMetricsStore

**形态**：class（已有）
**包路径**：`com.aimedical.modules.ai.impl.metrics`

**T51 变更**：

```java
// 变更前：
private volatile long windowSeconds = 60;

// 变更后：
private final AtomicLong windowSeconds = new AtomicLong(60);
```

- `record()` 方法中 `windowSeconds` → `windowSeconds.get()`
- 所有读取方法（`getFailureRate`/`getEffectiveFailureRate`/`getAverageElapsed`/`buildDegradationContext`）中 `windowSeconds` → `windowSeconds.get()`

`setWindowSeconds()` 方法：

```java
public void setWindowSeconds(long windowSeconds) {
    if (windowSeconds <= 0) {
        throw new IllegalArgumentException("windowSeconds must be > 0, but got: " + windowSeconds);
    }
    this.windowSeconds.set(windowSeconds);
}
```

**需要导入**：`java.util.concurrent.atomic.AtomicLong`

**T52 变更**：4 个读取方法从 synchronized 块内直接遍历 deque 改为：synchronized 块内惰性淘汰后通过 `deque.toArray(new WindowedEvent[0])` 复制快照，在 synchronized 块外遍历快照计算。

**模板（4 个方法统一模式）**：

```java
public double getFailureRate(String capabilityId) {
    Deque<WindowedEvent> deque = windows.get(capabilityId);
    if (deque == null) return 0.0;
    WindowedEvent[] snapshot;
    synchronized (deque) {
        long cutoff = System.currentTimeMillis() - windowSeconds.get() * 1000;
        while (!deque.isEmpty() && deque.peekFirst().timestamp < cutoff) {
            deque.removeFirst();
        }
        snapshot = deque.toArray(new WindowedEvent[0]);
    }
    // 在 synchronized 块外遍历 snapshot 计算
    long successCount = 0, failureCount = 0;
    for (WindowedEvent event : snapshot) {
        if (event.type == EventType.NORMAL_SUCCESS) successCount++;
        else if (event.type == EventType.FAILURE) failureCount++;
    }
    long denominator = successCount + failureCount;
    return denominator == 0 ? 0.0 : (double) failureCount / denominator;
}
```

同样模式应用于：
- `getEffectiveFailureRate()` — 统计 NORMAL_SUCCESS / DEGRADED / FAILURE 计数
- `getAverageElapsed()` — 累加所有事件 elapsedMs
- `buildDegradationContext()` — 全量统计（success/degraded/failure 计数、lastFailureTime、平均耗时）

**涉及导入变更**：`java.util.concurrent.atomic.AtomicLong`

### ModelEndpointHealthManager

**形态**：class（已有）
**包路径**：`com.aimedical.modules.ai.impl.metrics`

**T59 变更**：`recordCallResult()` 中 UNAVAILABLE case 的成功分支：

```java
// 变更前：
} else if (current == EndpointHealthState.UNAVAILABLE) {
    if (success) {
        state.healthState.set(EndpointHealthState.CONNECTED);
        state.consecutiveSlowCalls.set(0);
        state.consecutiveFailures.set(0);
        state.consecutiveSuccesses.set(0);
        state.cumulativeFailures.set(0);
    } else {
        state.lastProbeTime.set(System.currentTimeMillis());
    }
}

// 变更后：
} else if (current == EndpointHealthState.UNAVAILABLE) {
    if (success) {
        state.healthState.set(EndpointHealthState.DEGRADED);
        state.consecutiveSlowCalls.set(0);
        state.consecutiveFailures.set(0);
        state.consecutiveSuccesses.set(0);
        state.cumulativeFailures.set(0);
    } else {
        state.lastProbeTime.set(System.currentTimeMillis());
    }
}
```

**状态跃迁完整路径**：UNAVAILABLE →（探测成功）→ DEGRADED →（连续 3 次成功）→ CONNECTED

DEGRADED 分支已有逻辑（不变）：
- 成功且耗时 < threshold：递增 `consecutiveSuccesses`，>= 3 回到 CONNECTED
- 失败：递增 `cumulativeFailures`，>= 5 回到 UNAVAILABLE
- 成功但耗时 >= threshold：重置 `consecutiveSuccesses` 为 0

## 错误处理

| 文件 | 变更 |
|------|------|
| AiCallRecord | 无新增错误。全参构造器 private，仅工厂方法调用。sentinelReason 为 `"EXPERIMENT_ASSIGN_ERROR"` 时静默覆写 promptVersion |
| LoggingMetricsCollector | 无新错误类型。catch (Exception e) 日志恢复不变 |
| SlidingWindowMetricsStore | `setWindowSeconds(<=0)` 抛出 `IllegalArgumentException` |
| ModelEndpointHealthManager | 无新错误类型 |

## 行为契约

| 组件 | 契约 |
|------|------|
| AiCallRecord | 所有字段构造后不可变。工厂方法返回 fully-initialized 实例。`capabilityName` 由调用方直接传入（非内部解析）。`userId` 由所有三个工厂方法的调用方直接传入。success() 中 `degraded=false`，`degradationReason=null`，`errorCode=null`，`errorMessage=null`，`modelId=传入值`，`retryCount=传入值`，`totalTokens` 内部计算为 `promptTokens + completionTokens`（两者均非 null 时求和，任一为 null 则 `totalTokens=null`）。failure() 中 `degraded=false`，`modelId=null`，`retryCount=0`。degraded() 中 `degraded=true`，`errorCode=null`，`errorMessage=null`，`retryCount=0` |
| LoggingMetricsCollector.record() | record == null 时静默跳过。新字段全部从 record getter 读取。callTime 使用 record 中记录的时间而非重新获取 LocalDateTime.now() |
| SlidingWindowMetricsStore | windowSeconds > 0 恒成立。读取方法在 synchronized 块外遍历快照，不持有 deque 锁进行算术计算。写入方法（record*）持有锁进行淘汰+追加，不受读取计算影响 |
| ModelEndpointHealthManager | UNAVAILABLE→DEGRADED→CONNECTED 经过完整两阶段恢复，不再跳跃 |

## 依赖关系

| 类型 | 依赖 |
|------|------|
| AiCallRecord | `java.time.LocalDateTime`（新增）|
| LoggingMetricsCollector | AiCallRecord（字段变更后 getter 签名跟随变化）、AiCallLogRepository（不变）|
| SlidingWindowMetricsStore | DegradationContext（不变）、`java.util.concurrent.atomic.AtomicLong`（新增）|
| ModelEndpointHealthManager | EndpointHealthState、`java.util.concurrent.atomic.*`（不变）|

## 修订说明（v8 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| capabilityName 字段无法通过工厂方法设置 | 三个静态工厂方法（success/failure/degraded）签名均添加 `String capabilityName` 作为首个参数；行为契约中明确 capabilityName 由调用方直接传入 |
| totalTokens 在 success() 中无赋值路径 | success() 工厂方法内部将 totalTokens 计算为 promptTokens + completionTokens（两者均非 null 时求和，任一为 null 则 totalTokens=null），行为契约中记录此推算规则 |

## 修订说明（v8 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| userId 未加入三个工厂方法签名 | 三个工厂方法（success/failure/degraded）签名均添加 `String userId` 参数；行为契约明确 userId 由调用方直接传入 |
| degradationReason 命名不一致 | 字段由 `degradeReason` 统一重命名为 `degradationReason`；字段表改为"由 degradeReason 改名为 degradationReason"；LoggingMetricsCollector 调整中删除"若"条件，明确重命名已完成 |
