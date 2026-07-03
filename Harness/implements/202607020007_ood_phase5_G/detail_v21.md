# 详细设计（v21）

## 概述

实现 `EndpointHealthState` 枚举和 `ModelEndpointHealthManager` 完整状态机；适配 `AbstractCapabilityExecutor` 中端点健康检查代码以使用枚举；新增/修改对应测试文件。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/metrics/EndpointHealthState.java` | 新建 | 定义 CONNECTED/DEGRADED/UNAVAILABLE 枚举 |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/metrics/ModelEndpointHealthManager.java` | 重写 | 从 String 存根升级为完整状态机实现 |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutor.java` | 修改 | 行 359-363 适配枚举 + tryProbe 逻辑 |
| `ai-impl/src/test/java/com/aimedical/modules/ai/impl/metrics/EndpointHealthStateTest.java` | 新建 | 枚举常量存在性/ordinal/name/null 安全 |
| `ai-impl/src/test/java/com/aimedical/modules/ai/impl/metrics/ModelEndpointHealthManagerTest.java` | 重写 | 9 个测试（8 场景 + 1 并发安全） |
| `ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutorTest.java` | 修改 | ~16 处 `getState` mock 从 `return "HEALTHY"` 改为 `return EndpointHealthState.CONNECTED` |
| `ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | 修改 | ~3 处 mock 适配 |
| `ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/TriageCapabilityExecutorTest.java` | 修改 | ~1 处 mock 适配 |

## 类型定义

### EndpointHealthState

**形态**：enum
**包路径**：`com.aimedical.modules.ai.impl.metrics`
**职责**：表示端点健康状态的枚举常量

```java
public enum EndpointHealthState {
    CONNECTED,
    DEGRADED,
    UNAVAILABLE
}
```

**公开接口**：无（标准枚举，继承 `Enum.name()` / `Enum.ordinal()` / `Enum.valueOf()`）
**构造方式**：直接引用常量
**类型关系**：隐式继承 `java.lang.Enum<EndpointHealthState>`

### ModelEndpointHealthManager

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.metrics`
**职责**：管理多个 LLM 端点的健康状态，支持状态转换、探测和调用结果记录

```java
public class ModelEndpointHealthManager {

    // 内部状态对象
    private static class EndpointState {
        final AtomicReference<EndpointHealthState> healthState = new AtomicReference<>(EndpointHealthState.CONNECTED);
        final AtomicInteger consecutiveSlowCalls = new AtomicInteger(0);
        final AtomicInteger consecutiveFailures = new AtomicInteger(0);
        final AtomicInteger cumulativeFailures = new AtomicInteger(0);
        final AtomicInteger consecutiveSuccesses = new AtomicInteger(0);
        final AtomicLong lastProbeTime = new AtomicLong(0);
        final AtomicLong slowCallThresholdMs = new AtomicLong(5000L);
    }

    private final ConcurrentHashMap<String, EndpointState> states = new ConcurrentHashMap<>();

    public ModelEndpointHealthManager() {}

    // 核心方法
    public EndpointHealthState getState(String endpointId);
    public boolean tryProbe(String endpointId);
    public void recordCallResult(String endpointId, boolean success, long elapsedMs);

    // 辅助方法
    public void setSlowCallThreshold(String endpointId, long thresholdMs);
    public void setLastProbeTime(String endpointId, long timestampMs);
    public long getSlowCallThreshold(String endpointId);
    public long getLastProbeTime(String endpointId);
}
```

**公开接口**：
- `EndpointHealthState getState(String endpointId)` — 返回端点当前健康状态
- `boolean tryProbe(String endpointId)` — 端点 UNAVAILABLE 时判定是否到探测窗口
- `void recordCallResult(String endpointId, boolean success, long elapsedMs)` — 记录调用结果并触发状态转换
- `void setSlowCallThreshold(String endpointId, long thresholdMs)` — 测试用：设置指定端点的慢调用阈值
- `void setLastProbeTime(String endpointId, long timestampMs)` — 测试用：设置指定端点上次探测时间
- `long getSlowCallThreshold(String endpointId)` — 测试用：获取慢调用阈值
- `long getLastProbeTime(String endpointId)` — 测试用：获取上次探测时间戳

**构造方式**：`new ModelEndpointHealthManager()`
**类型关系**：无继承，内部使用 `EndpointState` 内嵌静态类

### AbstractCapabilityExecutor 适配

**文件**：`AbstractCapabilityExecutor.java`，行 359-363（含周边行适配）
**变更内容**：

- `endpointHealthManager` 的 `getState` 返回类型从 `String` 改为 `EndpointHealthState`
- 将原行 359-364 的代码替换为以下逻辑：

```java
EndpointHealthState healthState = endpointHealthManager.getState(routeResult.getEndpointId());
boolean canProbe = false;
if (healthState == EndpointHealthState.UNAVAILABLE) {
    canProbe = endpointHealthManager.tryProbe(routeResult.getEndpointId());
    if (!canProbe) {
        return doDegrade(startTime, DegradationReason.ENDPOINT_UNAVAILABLE.getCode(),
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, promptVersion, null, sentinelReason);
    }
    // canProbe=true: 允许探测调用，管线继续进入 LLM 调用阶段
}
```

- 在行 378 的 `long totalTimeoutMs = resolveTimeout(capabilityId).toMillis();` 之后追加：

```java
if (healthState == EndpointHealthState.UNAVAILABLE && canProbe) {
    totalTimeoutMs = totalTimeoutMs / 2;
}
```

## 错误处理

无自定义异常。`recordCallResult` 和 `tryProbe` 通过内部 CAS + `synchronized` 保证状态原子性。`getState` 对新端点使用 `computeIfAbsent` 自动注册（永不返回 null）。传入 null `endpointId` 时 `computeIfAbsent` 会抛出 NPE，调用方需保证非 null。

## 行为契约

### 自动注册规则

`getState`、`tryProbe`、`recordCallResult` 首次接收到新 `endpointId` 时，使用 `ConcurrentHashMap.computeIfAbsent(endpointId, k -> new EndpointState())` 原子注册。`getState` 对新端点返回 `CONNECTED`。

### 状态转换规则（recordCallResult 触发）

前置条件：`endpointId` 已注册。

| 当前状态 | condition(success, elapsedMs) | 目标状态 | 副作用 |
|---------|-------------------------------|---------|--------|
| CONNECTED | success=false, ++consecutiveFailures >= 5 | UNAVAILABLE | 重置 consecutiveSlowCalls, consecutiveFailures, consecutiveSuccesses, cumulativeFailures |
| CONNECTED | success=true, elapsed > threshold, ++consecutiveSlowCalls >= 3 | DEGRADED | 重置 consecutiveSlowCalls |
| CONNECTED | 其他 | CONNECTED | 仅更新对应计数器（见计数器更新规则），不做状态转换 |
| DEGRADED | success=true, elapsed < threshold, ++consecutiveSuccesses >= 3 | CONNECTED | 重置 consecutiveSlowCalls, consecutiveFailures, consecutiveSuccesses, cumulativeFailures |
| DEGRADED | success=false, ++cumulativeFailures >= 5 | UNAVAILABLE | 重置 consecutiveSlowCalls, consecutiveFailures, consecutiveSuccesses, cumulativeFailures |
| DEGRADED | 其他 | DEGRADED | 仅更新对应计数器（见计数器更新规则），不做状态转换 |
| UNAVAILABLE | success=true（探测成功） | CONNECTED | 重置 consecutiveSlowCalls, consecutiveFailures, consecutiveSuccesses, cumulativeFailures |
| UNAVAILABLE | success=false（探测失败） | UNAVAILABLE | 重置 lastProbeTime = System.currentTimeMillis() |

### 计数器更新规则

- `consecutiveSlowCalls`：CONNECTED 且 success=true 且 elapsed > threshold 时递增；状态转换时重置
- `consecutiveFailures`：CONNECTED 且 success=false 时递增；进入 CONNECTED 或 UNAVAILABLE 时重置
- `cumulativeFailures`：DEGRADED 且 success=false 时递增；进入 CONNECTED 或 UNAVAILABLE 时重置
- `consecutiveSuccesses`：DEGRADED 且 success=true 且 elapsed < threshold 时递增；进入 CONNECTED 或 UNAVAILABLE 时重置

### DEGRADED 状态下慢调用（elapsed >= threshold）的计数器行为

当 `healthState == DEGRADED`：
- `success=true` 且 `elapsed >= threshold`：视为"慢成功"，**重置 `consecutiveSuccesses` 为 0**（慢调用打断恢复连续计数），`cumulativeFailures` 不变
- `success=false`：`cumulativeFailures` 递增，`consecutiveSuccesses` 重置为 0（失败也打断恢复连续计数）

### tryProbe 行为

- 若当前状态不为 UNAVAILABLE，返回 false（不做任何事）
- 若当前为 UNAVAILABLE：
  - `System.currentTimeMillis() - lastProbeTime >= 30_000`：返回 true（更新 lastProbeTime = System.currentTimeMillis()）
  - 否则返回 false

### concurrentCallResult 线程安全

`recordCallResult` 和 `tryProbe` 内部使用 `synchronized(endpointState)` 块保证同一端点的状态转换与计数器更新的原子性。跨端点的不同调用不受影响。

### AbstractCapabilityExecutor 适配

`executeStandardPipeline` 方法中：
1. `getState` 返回 `EndpointHealthState`，移除 `null` 检查（`computeIfAbsent` 保证非 null）
2. 当 `healthState == UNAVAILABLE` 时调用 `tryProbe`：
   - `tryProbe` 返回 false → 立即 `doDegrade(ENDPOINT_UNAVAILABLE)`
   - `tryProbe` 返回 true → 继续进入 LLM 调用，`totalTimeoutMs` 减半
3. 当 `healthState == CONNECTED` 或 `DEGRADED` 时，正常进入 LLM 调用（无额外变化）

### 辅助方法（测试用）

- `setSlowCallThreshold(endpointId, thresholdMs)`：设置端点的慢调用阈值，覆盖默认 5000ms
- `setLastProbeTime(endpointId, timestampMs)`：设置端点的上次探测时间，用于测试探测窗口
- `getSlowCallThreshold(endpointId)`：获取端点的当前慢调用阈值
- `getLastProbeTime(endpointId)`：获取端点的上次探测时间戳
- 这些方法同样使用 `computeIfAbsent` 自动注册

## 依赖关系

### ModelEndpointHealthManager

- **依赖的已有类型**：`java.util.concurrent.ConcurrentHashMap`、`java.util.concurrent.atomic.*`
- **被引用于**：`AbstractCapabilityExecutor`（构造函数注入 `ModelEndpointHealthManager`，`executeStandardPipeline` 中调用 `getState` + `tryProbe`）

### AbstractCapabilityExecutor 适配

- **新增导入**：`com.aimedical.modules.ai.impl.metrics.EndpointHealthState`
- **移除导入**：无（`ModelEndpointHealthManager` 已导入，原来的 String 用法的隐式导入不需变更）
- **对外一致性**：`endpointHealthManager.getState()` 返回类型改变（String → EndpointHealthState），但调用者在测试中做相应适配即可

## 修订说明（v21 r1）

| 审查意见 | 修改措施 |
|---------|---------|
| **[严重]** `canProbe` 变量作用域错误：在 if 块内声明但在块外引用导致编译失败 | 将 `boolean canProbe` 声明提升到第一个 if 块之前（初始化为 false），在 if 块内赋值；timeout 减半逻辑改为在 `totalTimeoutMs = resolveTimeout(...)` 之后用 `if (healthState == UNAVAILABLE && canProbe)` 条件判断 |
| **[一般]** 自动注册机制缺乏线程安全保障，未指定使用 `ConcurrentHashMap.computeIfAbsent` 原子方法 | 在 §行为契约「自动注册规则」中明确指定 `getState`、`tryProbe`、`recordCallResult` 统一使用 `ConcurrentHashMap.computeIfAbsent(endpointId, k -> new EndpointState())` 实现原子注册 |
| **[轻微]** `cumulativeFailures` 重置规则内部不一致（转换表写"不重置"，计数器规则写"重置"） | 统一为"重置 cumulativeFailures"（与计数器规则一致），删除转换表中"不重置 cumulativeFailures"的描述 |

## 修订说明（v21 r2）

| 审查意见 | 修改措施 |
|---------|---------|
| **[一般]** CONNECTED→UNAVAILABLE 副作用列缺少 consecutiveFailures | 补充至重置清单：重置 consecutiveSlowCalls, consecutiveFailures, consecutiveSuccesses, cumulativeFailures |
| **[一般]** DEGRADED→UNAVAILABLE 副作用列缺少 cumulativeFailures 和 consecutiveFailures | 补全重置清单为：consecutiveSlowCalls, consecutiveFailures, consecutiveSuccesses, cumulativeFailures |
| **[一般]** DEGRADED→CONNECTED 副作用列缺少 consecutiveSuccesses | 补全重置清单为：consecutiveSlowCalls, consecutiveFailures, consecutiveSuccesses, cumulativeFailures |
| **[轻微]** 字段名 `slowCallThresholdMs` 与任务规格 `lastSlowCallThresholdMs` 不一致 | 保持 `slowCallThresholdMs`。理由：该字段是慢调用阈值配置值，而非时间戳记录；`last` 前缀仅适用于 `lastProbeTime` 这类时间戳字段。语义上 `slowCallThresholdMs` 更准确。 |
| **[轻微]** DEGRADED 状态下 `elapsed >= threshold` 时 `consecutiveSuccesses` 行为未显式定义 | 新增「DEGRADED 状态下慢调用的计数器行为」小节，明确：`elapsed >= threshold` 时重置 `consecutiveSuccesses` 为 0。 |
| **[轻微]** `consecutiveSlowCalls` 重置规则未覆盖进入 CONNECTED 场景 | 计数器规则从"进入其他状态（非 CONNECTED）时重置"改为"状态转换时重置"，涵盖全部六种转换（含进入 CONNECTED）。 |
