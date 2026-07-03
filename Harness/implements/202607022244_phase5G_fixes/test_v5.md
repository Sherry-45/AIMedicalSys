# 测试报告（v5）

## 概述

针对 phase5G 7 项修复（T14/T60/T13/T61/T19/T20/T35）编写单元测试，覆盖 4 个被测类型，共 4 个测试文件。

## 文件变更清单

| 操作 | 测试文件 | 对应任务 |
|------|---------|---------|
| 修改 | `ai-impl/.../degradation/CircuitBreakerDegradationStrategyTest.java` | T14/T60/T13 |
| 修改 | `ai-impl/.../degradation/TimeoutDegradationStrategyTest.java` | T61 |
| 修改 | `ai-impl/.../orchestrator/AiOrchestratorTest.java` | T19/T20/T35 |
| 修改 | `ai-impl/.../orchestrator/AbstractCapabilityExecutorTest.java` | T14 |

## 新增/修改测试用例

### CircuitBreakerDegradationStrategyTest（T14/T60/T13）

| 测试方法 | 契约验证 | 维度 |
|---------|---------|------|
| `shouldUseOperationNameAsKeyWhenPresent` | T14: 优先以 operationName（endpointId）作为 stateMap 键 | 正常路径 |
| `shouldFallbackToServiceNameWhenOperationNameIsNull` | T14: operationName 为 null 时回退 serviceName | 边界条件 |
| `shouldSetFailureCountToOneWhenTransitionToOpen` | T60: CLOSED→OPEN 时 failureCount = 1 | 正常路径 |
| `shouldTimeoutProbeLockAndRetry` | T13: probeLock 超时自动清零并重试探测 | 状态交互 |
| `shouldMaintainIndependentStateByEndpointId` | T14: 不同 endpointId 在同 capability 下的状态隔离 | 状态交互 |

### TimeoutDegradationStrategyTest（T61）

- 移除 `metricsStore` 依赖（字段/构造参数/import），构造器签名改为 `(Duration)`

### AiOrchestratorTest（T19/T20/T35）

| 测试方法 | 契约验证 | 维度 |
|---------|---------|------|
| `shouldThrowIllegalArgumentExceptionForUnregisteredCapability`（重写） | T19: 未注册 capabilityId 抛出 `IllegalArgumentException` | 错误路径 |
| `shouldRecordAiCallRecordOnSyncException`（新增） | T20: catch 块中 `metricsCollector.record()` 调用正确 | 正常路径 |
| `shouldHandleConcurrentAccessToExecutorMap`（新增） | T35: `ConcurrentHashMap` 支持并发访问 | 状态交互 |

### AbstractCapabilityExecutorTest（T14）

| 测试方法 | 契约验证 | 维度 |
|---------|---------|------|
| `executeStandardPipelineShouldDegradeWhenCircuitBreakerTriggersAfterRouting` | T14: routing 后 endpointId 粒度熔断二次检查 | 正常路径 |

## 设计偏差说明

无偏差。
