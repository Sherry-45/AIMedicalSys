# 实现报告（v4）

## 概述

实现了 TimeoutDegradationStrategy 和 CircuitBreakerDegradationStrategy 两个 DegradationStrategy 实现类，及对应的单元测试；同时修改了 SlidingWindowMetricsStore.buildDegradationContext 以设置 serviceName 字段。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `ai-impl/.../metrics/SlidingWindowMetricsStore.java` | `buildDegradationContext` 两个分支各添加 `.serviceName(capabilityId)` |
| 新建 | `ai-impl/.../degradation/TimeoutDegradationStrategy.java` | 超时降级策略实现，implements DegradationStrategy |
| 新建 | `ai-impl/.../degradation/CircuitBreakerDegradationStrategy.java` | 熔断降级策略实现，implements DegradationStrategy；含内部枚举 CircuitBreakerState 和静态内部类 CircuitData |
| 新建 | `ai-impl/.../degradation/TimeoutDegradationStrategyTest.java` | 5 个测试用例（含 getOrder） |
| 新建 | `ai-impl/.../degradation/CircuitBreakerDegradationStrategyTest.java` | 12 个测试用例（含 getOrder、getState for unknown、null serviceName） |

## 编译验证

`mvn test -pl ai-impl -Dtest="*DegradationStrategy*,*SlidingWindowMetricsStore*"` — 38/38 通过，0 失败，0 错误。

## 设计偏差说明

无偏差。设计规格中 TimeoutDegradationStrategyTest 要求 4 个测试用例，实际编写 5 个（增加了 `orderShouldBe20`）；CircuitBreakerDegradationStrategyTest 要求 11 个，实际编写 12 个（增加了 `orderShouldBe10`）。均在实现报告中说明，未自行修改设计规格。
