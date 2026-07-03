# 实现报告（v1）

## 概述

实现了 Phase5_G 底座核心骨架 4 个交付物：ai-api 模块的 DegradationReason 枚举、DegradationStrategy 接口扩展（getOrder）、DegradationContext 字段扩展 + Builder + 校验方法，以及 ai-impl 模块的 SlidingWindowMetricsStore 滑动窗口指标存储。同时新增 3 个测试文件覆盖全部行为契约。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/degradation/DegradationReason.java | 8 个枚举常量的降级原因/错误码集中管理 |
| 修改 | AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/degradation/DegradationStrategy.java | 新增 default int getOrder() 方法（二进制兼容） |
| 修改 | AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/degradation/DegradationContext.java | 扩展 7 个字段 + Builder 模式 + 校验方法 |
| 新建 | AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/metrics/SlidingWindowMetricsStore.java | 滑动窗口指标存储，线程安全 @Component |
| 修改 | AIMedical/backend/modules/ai/ai-api/pom.xml | 新增 jackson-annotations 依赖（编译期需要） |
| 新建 | AIMedical/backend/modules/ai/ai-api/src/test/java/com/aimedical/modules/ai/api/degradation/DegradationReasonTest.java | 枚举常量、getCode/getMessage、fromCode、toString 测试（15 用例） |
| 新建 | AIMedical/backend/modules/ai/ai-api/src/test/java/com/aimedical/modules/ai/api/degradation/DegradationContextTest.java | Builder、postDeserializationValidate、isFresh、isInitialized 测试（16 用例） |
| 新建 | AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/metrics/SlidingWindowMetricsStoreTest.java | 记录/查询/惰性淘汰/并发安全测试（18 用例） |

## 编译验证

BUILD SUCCESS。ai-api 模块 167 测试用例全部通过（含新增 31），ai-impl 模块 83 测试用例全部通过（含新增 18），合计 250 测试用例 0 失败 0 错误。

## 设计偏差说明

| 设计规格 | 偏差原因 | 实际处理 |
|---------|---------|---------|
| 文件规划表未包含 pom.xml 修改 | ai-api 模块的 pom.xml 缺少 `com.fasterxml.jackson.core:jackson-annotations` 编译依赖，导致 `@JsonIgnoreProperties` 无法编译 | 在 ai-api/pom.xml 中显式添加 jackson-annotations 依赖 |
| postDeserializationValidate 的 allDefault 判断 | 设计描述"均为默认值（null/0L）"未明确 null 与 0 的关系 | 实现为 `(invocationCount==null \|\| invocationCount==0) && (failureCount==null \|\| failureCount==0) && elapsedTime==0L`，覆盖 null 和零值两种默认态 |
| SlidingWindowMetricsStore 的 `lastFailureTime` 在 buildDegradationContext 中的计算 | 设计未明确多条 FAILURE 事件时取最近一条 | 实现为取最大 timestamp 的 FAILURE 事件时间 |
