# 详细设计（v7）

## 概述

修复 R6 验证失败的 13 个测试（11 个 CircuitBreakerDegradationStrategyTest failure + 2 个 AbstractCapabilityExecutorTest error），涉及 2 个源文件，2 处独立修复。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/.../degradation/CircuitBreakerDegradationStrategy.java` | 修改 | 修复 A — CLOSED case 恢复 computeIfAbsent + failureCount 递增 |
| `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` | 修改 | 修复 B — handleSuccess() metricsStore null 守卫 |

## 类型定义

### CircuitBreakerDegradationStrategy（修复 A）

**形态**：class（已有）
**包路径**：`com.aimedical.modules.ai.impl.degradation`

**shouldDegrade() CLOSED case 变更（line 58-72）**：

```java
case CLOSED: {
    CircuitData data = circuitDataMap.computeIfAbsent(key, k -> new CircuitData());
    data.failureCount++;
    double failureRate = metricsStore.getFailureRate(serviceName);
    if (failureRate >= failureRateThreshold) {
        long now = System.currentTimeMillis();
        data.circuitOpenedAt = now;
        data.lastFailureTime = now;
        data.failureCount = 1;
        stateRef.set(CircuitBreakerState.OPEN);
        return true;
    }
    return false;
}
```

**变更要点**：

1. `circuitDataMap.get(key)` → `circuitDataMap.computeIfAbsent(key, k -> new CircuitData())`：确保首次调用时创建 CircuitData 条目，消除 chicken-and-egg 问题
2. 新增 `data.failureCount++`：每次 shouldDegrade 调用时递增（连续失败累积），T60 只在 OPEN 转换时重置为 1
3. 移除 `if (data != null && data.failureCount > 0)` 外层守卫：递增后恒 >0，无需此条件。该条件正是 R6 阻止 CLOSED→OPEN 路径的根本原因

**行为契约**：

- 每次 `shouldDegrade()` 调用递增 failureCount，即使未触发 OPEN
- CLOSED→OPEN 转换时将 failureCount 重置为 1（符合 T60 要求）
- endpoint 级隔离通过 key（operationName 优先）自动维持，无需额外逻辑
- OPEN→HALF_OPEN、HALF_OPEN case 不变

### AbstractCapabilityExecutor.handleSuccess()（修复 B）

**形态**：class（已有）
**包路径**：`com.aimedical.modules.ai.impl.orchestrator`

**handleSuccess() 变更（line 515）**：

```java
// 变更前：
metricsStore.recordSuccess(capabilityId, elapsedMs);

// 变更后：
if (metricsStore != null) {
    metricsStore.recordSuccess(capabilityId, elapsedMs);
}
```

**行为契约**：

- 当 `metricsStore == null` 时静默跳过 recordSuccess，不降级、不抛 NPE
- 与 `checkPreDegradation()`（line 205）和 `doDegrade()`（line 276）中 metricsStore null 守卫模式一致
- `metricsCollector.record()` 已在 line 508 处有 null 守卫，但 metricsStore 此前没有

## 错误处理

- **修复 A**：无新错误类型。circuitDataMap 条目不存在时自动创建，不再静默返回 false
- **修复 B**：metricsStore == null 时跳过 recordSuccess，延续已有守卫模式

## 行为契约

1. **修复 A**：CLOSED case 每次调用 `shouldDegrade()` 时创建/获取 CircuitData、递增 failureCount，再根据 capability 级 failureRate 决定是否 OPEN。避免 R6 中因条目缺失/failureCount=0 导致的路径死锁
2. **修复 B**：`handleSuccess()` 中 `metricsStore.recordSuccess()` 仅在 `metricsStore != null` 时执行

## 依赖关系

- `CircuitBreakerDegradationStrategy`：无新增依赖
- `AbstractCapabilityExecutor`：无新增依赖
- 不涉及 pom.xml、配置类或新建文件
