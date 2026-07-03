# 测试报告（v4）

## 概述

基于详细设计 v4 r1 的行为契约，为 TimeoutDegradationStrategy 和 CircuitBreakerDegradationStrategy 编写单元测试。每个被测类型对应一个测试文件，所有测试基于公开接口行为，不依赖实现细节。

## 文件清单

| 文件路径 | 用例数 | 说明 |
|---------|--------|------|
| `ai-impl/.../degradation/TimeoutDegradationStrategyTest.java` | 7 | 覆盖正常路径、边界条件、错误路径、getOrder |
| `ai-impl/.../degradation/CircuitBreakerDegradationStrategyTest.java` | 16 | 覆盖状态机全路径、边界条件、错误路径、多能力隔离、probeLock 生命周期 |

## 测试覆盖

### TimeoutDegradationStrategy（7 个用例）

| 测试方法 | 覆盖维度 | 验证点 |
|---------|---------|--------|
| `shouldNotDegradeWhenNoInvocationData` | 错误路径 | invocationCount 为 null 时返回 false |
| `shouldNotDegradeWhenInvocationCountIsZero` | 边界条件 | invocationCount 为 0 时返回 false |
| `shouldDegradeWhenElapsedExceedsThreshold` | 正常路径 | elapsed > threshold×0.8 返回 true |
| `shouldNotDegradeWhenElapsedBelowThreshold` | 正常路径 | elapsed < threshold×0.8 返回 false |
| `shouldNotDegradeWhenElapsedEqualsExactThreshold` | 边界条件 | elapsed == threshold×0.8（>`）返回 false |
| `shouldUseDefaultTimeoutWhenNull` | 边界条件 | 构造器 timeoutThreshold 为 null 时默认 30s |
| `orderShouldBe20` | 行为契约 | getOrder() 返回 20 |

### CircuitBreakerDegradationStrategy（16 个用例）

| 测试方法 | 覆盖维度 | 验证点 |
|---------|---------|--------|
| `shouldThrowWhenMetricsStoreIsNull` | 错误路径 | 构造器 metricsStore 为 null 时抛 IllegalArgumentException |
| `shouldNotDegradeWhenServiceNameIsNull` | 错误路径 | serviceName 为 null 时返回 false |
| `shouldNotDegradeWhenFailureRateBelowThreshold` | 正常路径 | CLOSED 状态，失败率 < 阈值 → false |
| `shouldDegradeWhenFailureRateExceedsThreshold` | 正常路径 | CLOSED 状态，失败率 ≥ 阈值 → true |
| `shouldTransitionToOpenAndReturnTrueOnHighFailureRate` | 状态交互 | CLOSED→OPEN，getState 返回 OPEN |
| `shouldRemainOpenAndAlwaysDegradeWithinWindow` | 状态交互 | OPEN 窗口内多次调用均返回 true |
| `shouldTransitionToHalfOpenAfterWindowExpires` | 状态交互 | 窗口到期 → HALF_OPEN，返回 false |
| `halfOpenShouldAllowSingleProbeAndDegradeOthers` | 状态交互 | HALF_OPEN 首次返回 false（放行探测），后续返回 true |
| `recordProbeResultSuccessShouldTransitionToClosedAndAllowNewProbe` | 状态交互 + probeLock | 探测成功→CLOSED，probeLock 释放后新探测可获取 |
| `recordProbeResultFailureShouldTransitionToOpen` | 状态交互 | 探测失败→OPEN |
| `recordProbeResultOnUnknownCapabilityShouldBeNoOp` | 错误路径 | 未知 capability 调用 recordProbeResult 无影响 |
| `shouldMaintainIndependentStateForMultipleCapabilities` | 状态交互 | 多能力各自独立维护熔断状态 |
| `getStateShouldReturnClosedForUnknownCapability` | 边界条件 | 未记录的能力返回 CLOSED |
| `shouldUseDefaultValuesWhenThresholdIsNaN` | 边界条件 | failureRateThreshold 为 NaN 时默认 0.5，openWindow 为 null 时默认 30s |
| `orderShouldBe10` | 行为契约 | getOrder() 返回 10 |

## 偏离说明

| 设计规格 | 实际 | 说明 |
|---------|------|------|
| Timeout: 4 个用例 | 7 个用例 | 增加：invocationCount=0 边界、精确边界（elapsed==threshold×0.8）、已有 shouldUseDefaultTimeoutWhenNull（设计未计入） |
| CircuitBreaker: 11 个用例 | 16 个用例 | 增加：null metricsStore 构造器校验、recordProbeResult 未知能力无操作、多能力独立状态、探测成功后再获取探测锁（probeLock 生命周期验证） |
