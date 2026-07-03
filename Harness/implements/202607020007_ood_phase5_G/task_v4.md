# 任务指令（v4）

## 动作
NEW

## 任务描述
在 `ai-impl/degradation/` 包新增两个具体降级策略实现类，并编写对应的单元测试：

### 1. TimeoutDegradationStrategy
- **路径**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/degradation/TimeoutDegradationStrategy.java`
- **类型**：class implements DegradationStrategy
- **职责**：基于 `DegradationContext` 中的最近调用平均耗时（`elapsedTime`）判定是否触发降级。若平均耗时超过硬超时阈值的 80%，触发降级
- **关键行为**：
  - 通过构造器注入 `SlidingWindowMetricsStore`（获取 `getAverageElapsed()`）和超时阈值配置 `Duration`（默认 30 秒）
  - `shouldDegrade(context)`：取 `context.getElapsedTime()` 与 `timeoutThreshold * 0.8` 比较，超过则返回 true
  - `getOrder()`：返回 20（高于 CircuitBreaker 的 10，使熔断器优先判定）

### 2. CircuitBreakerDegradationStrategy
- **路径**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/degradation/CircuitBreakerDegradationStrategy.java`
- **类型**：class implements DegradationStrategy
- **职责**：当某能力的最近调用失败率超过阈值（默认 50%）时，触发熔断——在熔断窗口（默认 30 秒）内所有对该能力的调用直接降级
- **关键行为**：
  - 内部定义 `CircuitBreakerState` 枚举：`CLOSED`, `OPEN`, `HALF_OPEN`
  - 使用 `ConcurrentHashMap<String, AtomicReference<CircuitBreakerState>>` 维护每个 `capabilityId` 的独立熔断状态
  - 使用 `ConcurrentHashMap<String, CircuitData>` 维护失败计数和最近失败时间
  - `shouldDegrade(context)`：
    - CLOSED 状态：检查 `getFailureRate(capabilityId)` >= threshold → OPEN
    - OPEN 状态：检查熔断窗口是否到期（`currentTime - lastFailureTime >= openWindowMs`）→ HALF_OPEN
    - HALF_OPEN 状态：使用 `AtomicBoolean probeLock` 确保仅一个探测线程通过 → CLOSED；其余返回 true（降级）
  - `getState(String capabilityId)`：返回当前熔断器状态
  - `getOrder()`：返回 10（优先执行）
  - 通过构造器注入 `SlidingWindowMetricsStore`（用于 `getFailureRate()`）、失败率阈值（默认 0.5）、熔断窗口（默认 30 秒）

### 测试文件
- `ai-impl/src/test/java/com/aimedical/modules/ai/impl/degradation/TimeoutDegradationStrategyTest.java`
- `ai-impl/src/test/java/com/aimedical/modules/ai/impl/degradation/CircuitBreakerDegradationStrategyTest.java`

### 测试覆盖要求

**TimeoutDegradationStrategyTest**：
1. `shouldDegradeWhenElapsedExceedsThreshold` — 平均耗时超过 80% 阈值时返回 true
2. `shouldNotDegradeWhenElapsedBelowThreshold` — 平均耗时低于阈值时返回 false
3. `shouldNotDegradeWhenNoContextData` — context 中 invocationCount 为 0 时返回 false
4. `getOrderShouldReturnDefault` — 验证 getOrder() 返回 20

**CircuitBreakerDegradationStrategyTest**：
1. `shouldBeClosedInitially` — 初始状态为 CLOSED
2. `shouldOpenWhenFailureRateExceedsThreshold` — 失败率超过阈值后状态变为 OPEN
3. `shouldDegradeWhenOpen` — OPEN 状态时 shouldDegrade 返回 true
4. `shouldTransitionToHalfOpenAfterWindow` — 熔断窗口到期后转为 HALF_OPEN
5. `halfOpenProbeShouldPassWhenAllowed` — HALF_OPEN 状态下首次探测通过（返回 false，允许调用）
6. `halfOpenProbeShouldDegradeWhenInProgress` — HALF_OPEN 状态下已有探测进行中时返回 true
7. `shouldCloseAfterSuccessfulProbe` — HALF_OPEN 探测成功后回到 CLOSED
8. `shouldReopenAfterFailedProbe` — HALF_OPEN 探测失败后回到 OPEN
9. `getStateShouldReturnCurrentState` — getState() 返回正确状态
10. `shouldIsolatePerCapabilityState` — 不同能力标识独立维护熔断状态
11. `getOrderShouldReturnDefault` — 验证 getOrder() 返回 10

## 选择理由
TimeoutDegradationStrategy 和 CircuitBreakerDegradationStrategy 是 CapabilityExecutor 降级预检管线的核心策略组件。前置依赖（DegradationContext、SlidingWindowMetricsStore、DegradationStrategy 接口）均已在前序任务中实现并验证通过。当前为 P0 优先级，是 Batch1 的最后两个类型，完成后 Batch1 全部完成。

## 任务上下文
- `DegradationStrategy` 接口已定义 `shouldDegrade(DegradationContext)` 和 `default getOrder()`
- `DegradationContext` 已包含 `invocationCount`、`failureCount`、`elapsedTime`、`lastFailureTime`、`requestType`、`departmentId` 字段
- `SlidingWindowMetricsStore` 已提供 `getFailureRate(capabilityId)`、`getAverageElapsed(capabilityId)`、`getEffectiveFailureRate(capabilityId)` 方法
- `NoOpDegradationStrategy` 是现有唯一实现，始终返回 false

## 已有代码上下文
- 现有文件：`ai-impl/.../degradation/NoOpDegradationStrategy.java`（`@Component @ConditionalOnMissingBean`）
- 现有文件：`ai-impl/.../metrics/SlidingWindowMetricsStore.java`（`@Component`，线程安全滑动窗口存储）
- 已有测试风格：JUnit 5，无 Spring 注解（纯 POJO 测试），静态内部辅助类替代 Mockito
