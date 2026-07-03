# 测试编写报告（v3）

## 概述

基于详细设计（detail_v3.md）的行为契约，在已有 22 个测试用例基础上新增 7 个用例，覆盖原测试未触及的关键行为契约维度。

## 测试文件

**路径**：`ai-impl/src/test/java/.../orchestrator/AbstractCapabilityExecutorTest.java`
**总用例数**：29（原有 22 + 新增 7）
**新增辅助文件**：`ai-impl/src/test/java/.../diagnosis/TestPhase4Exception.java`

## 新增用例

| # | 测试方法 | 覆盖契约 | 行为契约依据（detail_v3.md） |
|---|---------|---------|---------------------------|
| 1 | `isKnownPhase4BusinessExceptionShouldReturnTrueForKnownPackage` | isKnownPhase4BusinessException 对已知 Phase 4 包返回 true | 第 472~473 行 + 第 39~46 行 knownPhase4Packages |
| 2 | `executeShouldDegradeOnPhase4BusinessException` | execute() 中 Phase 4 业务异常 → doDegrade(INTERNAL_ERROR) 降级而非传播 | 第 344~351 行 exceptionally 处理 |
| 3 | `executeShouldTimeoutUsingThinAdapterPerCapabilityConfig` | 超时三级回退第二层：capabilityTimeoutConfig 不包含该 capability 时回退到 thinAdapterPerCapabilityConfig | 第 323~332 行 resolveTimeout 三级回退 |
| 4 | `executeShouldTimeoutUsingThinAdapterTimeoutDefault` | 超时三级回退第三层：两 map 均不包含时回退到 thinAdapterTimeout | 第 323~332 行 resolveTimeout 三级回退 |
| 5 | `executeShouldSortStrategiesByOrder` | 降级策略按 getOrder() 升序执行，order 低的策略先判断 | 第 306 行 "按 getOrder() 升序遍历策略" |
| 6 | `extractVariablesShouldReturnMap` | extractVariables 默认行为返回 ObjectMapper.convertValue(request, Map.class) | 第 405~407 行 "ObjectMapper.convertValue(request, Map.class)" |

## 设计偏差记录

无。所有新增测试均严格遵循 detail_v3.md 的行为契约。

## 风格一致性

- JUnit 5（无 Spring 注解），与项目其他测试风格一致
- 使用静态内部辅助类（TestableExecutor）避免 Mockito 依赖
- 使用 fully-qualified class name 引用 TestPhase4Exception（无需 import）
- 新增 TestPhase4Exception 为测试辅助类，位于 `com.aimedical.modules.diagnosis` 包以触发 knownPhase4Packages 匹配
