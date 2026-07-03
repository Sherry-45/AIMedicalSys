# 实现报告（v5）

## 概述

修复 Orchestrator 与熔断降级相关 7 项问题（T14/T60/T13/T61/T19/T20/T35）。涉及 4 个源文件，全部为修改操作。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `ai-impl/.../degradation/CircuitBreakerDegradationStrategy.java` | T14/T60/T13 — 键优先取 endpointId、failureCount 重置为 1、probeLock 超时清理 |
| 修改 | `ai-impl/.../degradation/TimeoutDegradationStrategy.java` | T61 — 移除 metricsStore 字段/构造参数/import |
| 修改 | `ai-impl/.../orchestrator/AiOrchestrator.java` | T19/T20/T35 — fail-fast 异常、metricsCollector.record()、ConcurrentHashMap 类型 |
| 修改 | `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` | T14 — routing 后 endpointId 粒度熔断二次检查 |

## 编译验证

`mvn compile -pl ai-impl -am -q` — 编译通过，无错误无警告。

## 设计偏差说明

无偏差。
