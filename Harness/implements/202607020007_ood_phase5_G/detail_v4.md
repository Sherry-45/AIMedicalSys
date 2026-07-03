# 详细设计（v4 r1）

## 概述

在 `ai-impl/degradation/` 包新增两个 DegradationStrategy 实现类：TimeoutDegradationStrategy（基于调用平均耗时触发降级）和 CircuitBreakerDegradationStrategy（基于失败率触发熔断降级），以及对应的单元测试。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/.../degradation/TimeoutDegradationStrategy.java` | 新建 | 超时降级策略实现 |
| `ai-impl/.../degradation/CircuitBreakerDegradationStrategy.java` | 新建 | 熔断降级策略实现 |
| `ai-impl/.../degradation/TimeoutDegradationStrategyTest.java` | 新建 | 4 个测试用例 |
| `ai-impl/.../degradation/CircuitBreakerDegradationStrategyTest.java` | 新建 | 11 个测试用例 |
| `ai-impl/.../metrics/SlidingWindowMetricsStore.java` | 修改 | `buildDegradationContext` 中添加 `serviceName` 赋值 |

## 前置修正：SlidingWindowMetricsStore.buildDegradationContext

**问题**：`buildDegradationContext(String capabilityId, String requestType)` 未将 `capabilityId` 存入 `DegradationContext` 的 `serviceName` 字段，导致下游降级策略无法从 `context.getServiceName()` 获取 capabilityId。

**修改**：在两个 return 分支的 Builder 链中均添加 `.serviceName(capabilityId)`：

```java
// 分支 1（deque == null）：第 116~119 行
return new DegradationContext.Builder()
        .serviceName(capabilityId)   // 新增
        .requestType(requestType)
        .serializedTimestamp(System.currentTimeMillis())
        .build();

// 分支 2（deque != null）：第 154~161 行
return new DegradationContext.Builder()
        .serviceName(capabilityId)   // 新增
        .invocationCount((int) invocationCount)
        .lastFailureTime(lastFailureTime)
        .elapsedTime(elapsedTime)
        .failureCount((int) failureCount)
        .requestType(requestType)
        .serializedTimestamp(System.currentTimeMillis())
        .build();
```

## 类型定义

### 1. TimeoutDegradationStrategy

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.degradation`
**职责**：基于 DegradationContext 中的平均耗时（`elapsedTime`）判定是否触发降级。若平均耗时超过硬超时阈值的 80%，触发降级
**修饰**：`public`

**构造器**：
```java
public TimeoutDegradationStrategy(SlidingWindowMetricsStore metricsStore, Duration timeoutThreshold)
```
- `metricsStore` — 滑动窗口指标存储（注入备用，当前 `shouldDegrade` 使用 context 数据）
- `timeoutThreshold` — 硬超时阈值，为 null 时默认 `Duration.ofSeconds(30)`

**公开接口**：

| 方法签名 | 返回类型 | 职责 |
|---------|---------|------|
| `shouldDegrade(DegradationContext context)` | `boolean` | 若 `context.getElapsedTime() > timeoutThreshold.toMillis() * 0.8` 返回 true；当 context 未初始化（无调用数据）时返回 false |
| `getOrder()` | `int` | 返回 20 |

**行为契约**：
- `shouldDegrade` 中，`context.getElapsedTime()` 单位为毫秒（由 `SlidingWindowMetricsStore.buildDegradationContext` 计算的平均耗时）
- 比较公式：`context.getElapsedTime() > timeoutThreshold.toMillis() * 0.8`
- 当 `context.getInvocationCount()` 为 null 或 0 时直接返回 false（无数据不降级）
- 不依赖 `metricsStore` 进行 `shouldDegrade` 判定，仅通过构造器存储以供后续扩展

**构造方式**：`new TimeoutDegradationStrategy(metricsStore, Duration.ofSeconds(30))`
**类型关系**：implements DegradationStrategy

---

### 2. CircuitBreakerDegradationStrategy

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.degradation`
**职责**：当某能力的最近调用失败率超过阈值时触发熔断，在熔断窗口内所有对该能力的调用直接降级。每个 `capabilityId` 独立维护熔断状态
**修饰**：`public`

**内部类型**：

#### CircuitBreakerState（内部枚举）
**形态**：`public enum`
**包路径**：内部定义于 `CircuitBreakerDegradationStrategy`
**常量**：`CLOSED`, `OPEN`, `HALF_OPEN`

#### CircuitData（内部静态类）
**形态**：`private static class`
**包路径**：内部定义于 `CircuitBreakerDegradationStrategy`
**字段**：

| 字段名 | 类型 | 用途 |
|-------|------|------|
| `circuitOpenedAt` | `volatile long` | 最近一次电路打开的时刻（毫秒时间戳），用于判定 OPEN 窗口是否到期 |
| `lastFailureTime` | `volatile long` | 最近一次失败的时刻（毫秒时间戳），与任务描述对齐 |
| `failureCount` | `volatile int` | 累积失败计数，与任务描述对齐 |
| `probeLock` | `AtomicBoolean` | 按 capabilityId 独立的探测锁，确保同一能力同一时刻仅一个探测请求通过 |

**构造器**：
```java
public CircuitBreakerDegradationStrategy(
    SlidingWindowMetricsStore metricsStore,
    double failureRateThreshold,
    Duration openWindow
)
```
- `metricsStore` — 用于获取 `getFailureRate(capabilityId)`，必须非 null
- `failureRateThreshold` — 失败率阈值，为 NaN 时默认 0.5
- `openWindow` — 熔断窗口持续时间，为 null 时默认 `Duration.ofSeconds(30)`

**字段**：

| 字段名 | 类型 | 修饰符 | 用途 |
|-------|------|--------|------|
| `metricsStore` | `SlidingWindowMetricsStore` | `private final` | 用于查询失败率 |
| `failureRateThreshold` | `double` | `private final` | 失败率阈值，默认 0.5 |
| `openWindowMs` | `long` | `private final` | 熔断窗口毫秒数，默认 30_000 |
| `stateMap` | `ConcurrentHashMap<String, AtomicReference<CircuitBreakerState>>` | `private final` | 每个 capabilityId 的独立熔断状态 |
| `circuitDataMap` | `ConcurrentHashMap<String, CircuitData>` | `private final` | 每个 capabilityId 的电路数据（含 per-capability probeLock） |

**公开接口**：

| 方法签名 | 返回类型 | 职责 |
|---------|---------|------|
| `shouldDegrade(DegradationContext context)` | `boolean` | 状态机主逻辑（见下方行为契约） |
| `getState(String capabilityId)` | `CircuitBreakerState` | 返回指定能力的当前熔断器状态，未记录的能力返回 CLOSED |
| `recordProbeResult(String capabilityId, boolean success)` | `void` | HALF_OPEN 探测完成后由调用方报告结果。success=true → 转到 CLOSED；success=false → 转到 OPEN 并重置窗口 |
| `getOrder()` | `int` | 返回 10 |

**shouldDegrade 状态机行为契约**：

输入：`context.getServiceName()` 作为 capabilityId；若为 null 返回 false。

```
stateRef = stateMap.computeIfAbsent(capabilityId, k -> new AtomicReference<>(CLOSED))
state = stateRef.get()

switch state:
  CLOSED:
    failureRate = metricsStore.getFailureRate(capabilityId)
    if failureRate >= failureRateThreshold:
      data = circuitDataMap.computeIfAbsent(capabilityId, k -> new CircuitData())
      data.circuitOpenedAt = System.currentTimeMillis()
      data.lastFailureTime = System.currentTimeMillis()
      data.failureCount++
      stateRef.set(OPEN)
      return true   // 降级
    else:
      return false  // 正常

  OPEN:
    data = circuitDataMap.get(capabilityId)       // CLOSED→OPEN 时已确保存在
    if System.currentTimeMillis() - data.circuitOpenedAt >= openWindowMs:
      stateRef.set(HALF_OPEN)
      // fall through 到 HALF_OPEN
    else:
      return true   // 降级

  HALF_OPEN:
    data = circuitDataMap.get(capabilityId)
    if data != null && data.probeLock.compareAndSet(false, true):
      // 不改变状态！仅标记探测锁
      return false  // 放行探测请求
    else:
      return true   // 已有探测进行中，降级
```

**关键变更说明（vs v4 原始设计）**：
1. `stateMap` 初始化：统一使用 `computeIfAbsent`，首次访问自动创建 `AtomicReference<CLOSED>`
2. `circuitDataMap` 初始化：在 CLOSED→OPEN 转换时使用 `computeIfAbsent` 创建 CircuitData 实例，避免 NPE
3. HALF_OPEN 状态下 `shouldDegrade` **不再更改状态**，仅控制探测锁——状态变更全部交由 `recordProbeResult` 管理
4. `probeLock` 为 per-capability（位于 CircuitData 内部），而非全局共享

**recordProbeResult 行为契约**：

```java
recordProbeResult(capabilityId, success):
    stateRef = stateMap.get(capabilityId)
    if stateRef == null: return        // 无状态记录，忽略

    data = circuitDataMap.get(capabilityId)
    if success:
        stateRef.set(CLOSED)           // 探测成功 → 闭合电路
    else:
        if data != null:
            data.circuitOpenedAt = System.currentTimeMillis()
            data.lastFailureTime = System.currentTimeMillis()
            data.failureCount++
        stateRef.set(OPEN)             // 探测失败 → 重新打开

    // 无论成功与否，必须释放 probeLock
    if data != null:
        data.probeLock.set(false)
```

**调用方要求**：`recordProbeResult` 的调用方必须保证在 try-finally 块中调用，确保即使探测过程中抛出异常也能正确释放 probeLock：
```java
if (!strategy.shouldDegrade(ctx)) {
    try {
        // 执行探测（doExecuteInternal 等）
        boolean probeSuccess = executeProbe(...);
        circuitBreaker.recordProbeResult(capabilityId, probeSuccess);
    } catch (Exception e) {
        circuitBreaker.recordProbeResult(capabilityId, false);
    }
}
```

**getState 行为契约**：

```java
getState(capabilityId):
    AtomicReference<CircuitBreakerState> ref = stateMap.get(capabilityId)
    return ref != null ? ref.get() : CLOSED
```

**构造方式**：`new CircuitBreakerDegradationStrategy(metricsStore, 0.5, Duration.ofSeconds(30))`
**类型关系**：implements DegradationStrategy

## 错误处理

| 场景 | 处理方式 |
|------|---------|
| Timeout: `getInvocationCount()` 为 null/0 | 返回 false（不降级） |
| Timeout: `getElapsedTime()` ≤ 阈值 | 返回 false（不降级） |
| CircuitBreaker: `getServiceName()` 为 null | 返回 false（不降级） |
| CircuitBreaker: CLOSED 且失败率 < 阈值 | 返回 false（不降级） |
| CircuitBreaker: CLOSED 且失败率 ≥ 阈值 | 转到 OPEN，设置 circuitOpenedAt，返回 true |
| CircuitBreaker: OPEN 且在窗口内 | 返回 true |
| CircuitBreaker: OPEN 且窗口到期 | 转到 HALF_OPEN（fall through），检查探测锁 |
| CircuitBreaker: HALF_OPEN 且无探测进行中 | 返回 false（放行探测），状态不变 |
| CircuitBreaker: HALF_OPEN 且有探测进行中 | 返回 true |
| CircuitBreaker: 探测成功 | 调用 recordProbeResult(true) → CLOSED，释放 probeLock |
| CircuitBreaker: 探测失败 | 调用 recordProbeResult(false) → OPEN + 重置窗口，释放 probeLock |
| CircuitBreaker: 探测异常 | 调用方 catch 后 recordProbeResult(false) |

## 行为契约

### shouldDegrade 调用顺序

1. 两个策略均通过 `DegradationStrategy.shouldDegrade(DegradationContext)` 被 `AbstractCapabilityExecutor.execute()` 调用
2. 调用前 context 由 `metricsStore.buildDegradationContext(capabilityId, requestType)` 构建，且应已设置 `serviceName = capabilityId`
3. 调用方按 `getOrder()` 升序执行：CircuitBreaker（order=10）优先于 Timeout（order=20）

### CircuitBreaker 状态变化规则

```
CLOSED ──(failureRate >= threshold)──────▶ OPEN
OPEN   ──(window expired)────────────────▶ HALF_OPEN
OPEN   ──(within window)─────────────────▶ OPEN (stay, always degrade)
HALF_OPEN ──(probeLock acquired)─────────▶ HALF_OPEN (stay, return false, allow one probe)
HALF_OPEN ──(probeLock held)─────────────▶ HALF_OPEN (stay, return true)
HALF_OPEN ──(recordProbeResult=true)─────▶ CLOSED
HALF_OPEN ──(recordProbeResult=false)────▶ OPEN
```

### circuitOpenedAt 更新规则

- 仅当状态从 CLOSED→OPEN 或 recordProbeResult(false)→OPEN 时写入 `circuitOpenedAt = System.currentTimeMillis()`
- OPEN→HALF_OPEN 和 HALF_OPEN→CLOSED 时不修改 `circuitOpenedAt`

### probeLock 生命周期

```
acquire: shouldDegrade(capabilityId) 在 HALF_OPEN 状态下通过 compareAndSet(false, true) 获取
release: recordProbeResult(capabilityId, *) 中通过 set(false) 释放
约束:    acquire 与 release 之间必须成对出现；未 release 将导致该能力所有后续 HALF_OPEN 探测永久阻塞
```

## 依赖关系

| 类型 | 来源 | 被依赖方式 |
|------|------|-----------|
| `DegradationStrategy` | ai-api（已有） | 被两个新实现 implements |
| `DegradationContext` | ai-api（已有） | `shouldDegrade` 参数 |
| `SlidingWindowMetricsStore` | ai-impl/metrics（已有） | 构造器注入，用于 `getFailureRate()` 和 `buildDegradationContext()` |
| `DegradationReason` | ai-api（已有） | 枚举引用（外部队列使用） |

**对外暴露**：
- `CircuitBreakerDegradationStrategy.getState(String)` — 供监控/管理端查看熔断状态
- `CircuitBreakerDegradationStrategy.recordProbeResult(String, boolean)` — 供 AbstractCapabilityExecutor 在 HALF_OPEN 探测完成后调用

## 修订说明（v4 r1）

| 审查意见 | 修改措施 |
|---------|---------|
| **[严重]** CircuitBreakerDegradationStrategy NPE：CLOSED→OPEN 转换时 `circuitDataMap[capabilityId]` 未初始化，访问 `circuitOpenedAt` 导致 NPE | 在 CLOSED→OPEN 分支中使用 `circuitDataMap.computeIfAbsent(capabilityId, k -> new CircuitData())` 确保 CircuitData 实例存在后再写入 `circuitOpenedAt` |
| **[严重]** 全局 `probeLock` 破坏 "每个 capabilityId 独立维护熔断状态" 的设计要求 | 将 `probeLock` 从全局 `AtomicBoolean` 移至 `CircuitData` 内部，按 capabilityId 独立管理：`CircuitData.probeLock`（per-capability `AtomicBoolean`） |
| **[一般]** HALF_OPEN 状态下过早转入 CLOSED：`shouldDegrade` 在 HALF_OPEN 获取 probeLock 后立即将状态设为 CLOSED，产生竞争窗口 | `shouldDegrade` 在 HALF_OPEN 中**不再更改状态**，仅通过 `probeLock.compareAndSet(false, true)` 决定是否放行探测。状态变更（CLOSED 或 OPEN）全部交由 `recordProbeResult` 管理 |
| **[一般]** `stateMap.get(capabilityId)` 不会自动初始化，首次访问返回 null | 统一使用 `stateMap.computeIfAbsent(capabilityId, k -> new AtomicReference<>(CLOSED))` 替代 `get()`，确保首次访问自动初始化为 CLOSED |
| **[轻微]** `probeLock` 异常残留风险：若探测过程抛出异常，probeLock 永久卡在 true | 在行为契约中明确要求调用方使用 try-finally 模式确保 `recordProbeResult` 一定被调用；`recordProbeResult` 内部无论 success 值如何，均执行 `probeLock.set(false)` 释放锁 |
| **[轻微]** `CircuitData` 字段与任务描述不一致：任务要求 "维护失败计数和最近失败时间"，但原设计仅含 `circuitOpenedAt` | 在 `CircuitData` 中补充 `lastFailureTime`（`volatile long`）和 `failureCount`（`volatile int`）字段，在 CLOSED→OPEN 和 recordProbeResult(false) 时同步更新 |

DESIGN_WRITTEN:C:\Develop\Software\AIMedicalSys\Harness\implements\202607020007_ood_phase5_G\detail_v4.md
