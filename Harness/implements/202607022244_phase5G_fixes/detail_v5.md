# 详细设计（v5）

## 概述

修复 Orchestrator 与熔断降级相关 7 项问题（T14/T60/T13/T61/T19/T20/T35）。涉及 4 个源文件：
- `CircuitBreakerDegradationStrategy.java` — 熔断器作用域 endpointId、failureCount 重置、probeLock 清理
- `TimeoutDegradationStrategy.java` — 移除未使用的 metricsStore 依赖
- `AiOrchestrator.java` — fail-fast 异常、metricsCollector.record()、ConcurrentHashMap 类型修正
- `AbstractCapabilityExecutor.java` — routing 后添加 CircuitBreaker 重新检查

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/.../degradation/CircuitBreakerDegradationStrategy.java` | 修改 | T14/T60/T13 — 键优先取 endpointId、failureCount 重置、probeLock 超时清理 |
| `ai-impl/.../degradation/TimeoutDegradationStrategy.java` | 修改 | T61 — 移除 metricsStore 字段/构造参数/import |
| `ai-impl/.../orchestrator/AiOrchestrator.java` | 修改 | T19/T20/T35 — fail-fast、record() 实现、ConcurrentHashMap 声明 |
| `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` | 修改 | T14 — routing 后 endpointId 粒度熔断二次检查 |

## 类型定义

### CircuitBreakerDegradationStrategy

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.degradation`
**职责**：per-endpoint 熔断降级策略

**字段变更**：

| 字段 | 变更前 | 变更后 |
|------|--------|--------|
| `stateMap` 键 | `String` = capabilityId | `String` = endpointId（优先）/ capabilityId（回退） |
| `circuitDataMap` 键 | 同上 | 同上 |

**内部类 CircuitData 变更（T13）**：

```java
private static class CircuitData {
    volatile long circuitOpenedAt;
    volatile long lastFailureTime;
    volatile int failureCount;
    final AtomicBoolean probeLock = new AtomicBoolean(false);
    volatile long probeAcquiredAt;            // 新增：探测获取时间戳
}
```

**公开接口**：

| 方法 | 变更说明 |
|------|---------|
| `shouldDegrade(DegradationContext) → boolean` | T14: 键改为 `context.getOperationName() != null ? context.getOperationName() : context.getServiceName()`；T60: CLOSED→OPEN 时 `data.failureCount = 1` 而非 `++`；T13: HALF_OPEN case 中 `probeAcquiredAt` 超时清理 |
| `getState(String) → CircuitBreakerState` | 不变，参数名改为 `key`（兼容 capabilityId/endpointId 调用） |
| `recordProbeResult(String, boolean) → void` | 不变，参数名改为 `key` |
| `getOrder() → int` | 不变 |

**T14 shouldDegrade() 键变更细节**：

```java
// line 46 变更
String key = context.getOperationName() != null
    ? context.getOperationName()
    : context.getServiceName();
```

所有 `stateMap`/`circuitDataMap` 的 `computeIfAbsent`/`get` 调用使用 `key` 而非 `capabilityId`。

**T60 CLOSED→OPEN 变更细节**（line 63）：

```java
// 变更前
data.failureCount++;
// 变更后
data.failureCount = 1;
```

**T13 HALF_OPEN probeLock 超时清理变更细节**（line 79-85）：

```java
case HALF_OPEN: {
    CircuitData data = circuitDataMap.get(key);
    if (data != null && data.probeLock.compareAndSet(false, true)) {
        data.probeAcquiredAt = System.currentTimeMillis();
        return false;  // 允许探测
    } else if (data != null && data.probeAcquiredAt != 0
        && System.currentTimeMillis() - data.probeAcquiredAt > openWindowMs) {
        // probeLock 超时，重置并重试
        data.probeLock.set(false);
        data.probeAcquiredAt = 0;
        if (data.probeLock.compareAndSet(false, true)) {
            data.probeAcquiredAt = System.currentTimeMillis();
            return false;
        }
        return true;
    } else {
        return true;   // 其他请求降级
    }
}
```

### TimeoutDegradationStrategy

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.degradation`
**职责**：基于超时阈值的降级策略

**变更（T61）**：

| 变更项 | 变更前 | 变更后 |
|--------|--------|--------|
| 字段 `metricsStore` | `SlidingWindowMetricsStore` | 移除 |
| 构造器参数 | `(SlidingWindowMetricsStore, Duration)` | `(Duration)` |
| import `SlidingWindowMetricsStore` | 存在 | 移除 |

**构造器签名**：
```java
public TimeoutDegradationStrategy(Duration timeoutThreshold) {
    this.timeoutThreshold = timeoutThreshold != null ? timeoutThreshold : Duration.ofSeconds(30);
}
```

`shouldDegrade()` 和 `getOrder()` 保持不变。

### AiOrchestrator

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.orchestrator`
**职责**：AI 能力编排入口

**字段变更（T35）**：

```java
// 变更前
private volatile Map<String, CapabilityExecutor<?, ?>> executorMap;

// 变更后
private ConcurrentHashMap<String, CapabilityExecutor<?, ?>> executorMap;
```

移除 `volatile`（仅 @PostConstruct 写入一次，后续只读）。

**initExecutorMap() 变更（T35）**：
```java
// 变更前
Map<String, CapabilityExecutor<?, ?>> map = new ConcurrentHashMap<>();
// ...
this.executorMap = map;

// 变更后
ConcurrentHashMap<String, CapabilityExecutor<?, ?>> map = new ConcurrentHashMap<>();
// ...
this.executorMap = map;
```

**handle() 变更（T19 + T20）**：

```java
private <T> CompletableFuture<AiResult<T>> handle(String capabilityId, Object request) {
    long startTime = System.currentTimeMillis();                  // T20: 新增
    CapabilityExecutor executor = executorMap.get(capabilityId);
    if (executor == null) {                                       // T19: 改为异常
        throw new IllegalArgumentException("未注册能力标识: " + capabilityId);
    }
    try {
        return executor.execute(request, capabilityId);
    } catch (Exception e) {
        log.error("执行 capability 时发生异常: capabilityId={}", capabilityId, e);
        metricsStore.recordFailure(capabilityId);
        // T20: 实现 metricsCollector.record()
        metricsCollector.record(new AiCallRecord(
            capabilityId, null, null, null, null, null, null, null, null, null,
            System.currentTimeMillis() - startTime, true, e.getClass().getSimpleName(), 0, 0));
        return CompletableFuture.completedFuture(AiResult.failure("AI服务暂时不可用，请稍后重试"));
    }
}
```

**新增 import（T20）**：
- `com.aimedical.modules.ai.impl.metrics.AiCallRecord`

### AbstractCapabilityExecutor

**形态**：abstract class
**包路径**：`com.aimedical.modules.ai.impl.orchestrator`
**职责**：能力执行器基类

**executeStandardPipeline() 变更（T14）**：

在 routeResult 非空校验之后（line 351 之后）、endpointHealthManager 检查之前（line 353 之前）插入 endpointId 粒度的熔断降级二次检查：

```java
// routing 完成，routeResult 已验证非 null
// ==== T14: endpointId 粒度的熔断降级检查 ====
DegradationContext cbCtx = metricsStore.buildDegradationContext(capabilityId, request.getClass().getSimpleName());
cbCtx.setOperationName(routeResult.getEndpointId());
Map<String, List<DegradationStrategy>> strategyMap = degradationStrategyMapRef.get();
if (strategyMap != null) {
    List<DegradationStrategy> strategies = strategyMap.get(capabilityId);
    if (strategies != null) {
        for (DegradationStrategy s : strategies) {
            if (s instanceof CircuitBreakerDegradationStrategy && s.shouldDegrade(cbCtx)) {
                return doDegrade(userId, startTime, DegradationReason.STRATEGY_TRIGGERED.getCode(),
                    request, capabilityId, departmentId, callerRole, callerId,
                    visitId, patientId, sessionId, inputSummary, null, promptVersion, null, sentinelReason);
            }
        }
    }
}
// ==== 结束 T14 插入 ====

boolean canProbe = false;
if (endpointHealthManager != null) {
    // ... 原有健康检查逻辑不变
}
```

**新增 import（T14）**：
- `com.aimedical.modules.ai.impl.degradation.CircuitBreakerDegradationStrategy`

## 错误处理

- **T19**：未注册能力标识从返回 `AiResult.failure` 改为抛出 `IllegalArgumentException`——调用方需捕获或允许传播
- **T20**：catch 块中 `metricsCollector.record()` 使用局部 `startTime` 计算耗时，`degraded=true`，`degradeReason=e.getClass().getSimpleName()`
- 其余降级路径行为不变

## 行为契约

1. **T14**：`CircuitBreakerDegradationStrategy.shouldDegrade()` 优先以 `DegradationContext.operationName`（endpointId）为键，回退 `serviceName`（capabilityId）
2. **T14**：`AbstractCapabilityExecutor.executeStandardPipeline()` 中 routing 成功后对 `CircuitBreakerDegradationStrategy` 做二次检查——使用 endpointId 作为 operationName
3. **T60**：CLOSED→OPEN 转换时 `failureCount` 重置为 1（当前触发 OPEN 的这次失败）
4. **T13**：`CircuitData.probeAcquiredAt` 记录探测获取时间戳；`probeLock` 被持有超过 `openWindowMs` 时自动清零并重试
5. **T61**：`TimeoutDegradationStrategy` 不再依赖 `SlidingWindowMetricsStore`，构造器仅接受 `Duration timeoutThreshold`
6. **T19**：`AiOrchestrator.handle()` 中未注册 capabilityId 抛出 `IllegalArgumentException`
7. **T20**：`AiOrchestrator.handle()` catch 块实现 `metricsCollector.record(new AiCallRecord(...))`
8. **T35**：`AiOrchestrator.executorMap` 类型改为 `ConcurrentHashMap`，移除 `volatile`

## 依赖关系

- `CircuitBreakerDegradationStrategy`：无新增依赖
- `TimeoutDegradationStrategy`：移除对 `SlidingWindowMetricsStore` 的依赖
- `AiOrchestrator`：新增依赖 `AiCallRecord`（已在项目中）
- `AbstractCapabilityExecutor`：新增依赖 `CircuitBreakerDegradationStrategy`（已在项目中）
- 不涉及 pom.xml、配置类或新建文件
