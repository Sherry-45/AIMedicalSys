# 实现报告（v11）

## 概述

实现 R10 RETRY（2 处 AiPlatformConfigTest 断言修复）+ R11（6 项数据模型/值对象不可变性与设计一致性缺陷修复），波及 3 个生产文件 + 5 个测试文件。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `ai-impl/.../config/AiPlatformConfigTest.java` | RETRY：assertEquals→assertArrayEquals，getMethod→getDeclaredMethod |
| 修改 | `ai-api/.../AiResult.java` | T29：字段 final + 移除 setter + 添加工厂方法 degradedWithErrorCode |
| 修改 | `ai-api/.../degradation/DegradationContext.java` | T30：Integer→int + serialVersionUID→2L + isInitialized 新语义；T31：builder() 静态工厂 |
| 修改 | `ai-api/.../dto/base/Phase4BusinessException.java` | T32：添加 errorCode 字段 + getErrorCode() + 新构造器 |
| 修改 | `ai-impl/.../client/ChatToolDefinition.java` | T46：移除 setStrict() |
| 修改 | `ai-impl/.../client/LlmChatOptions.java` | T47：字段 final + 移除 setter + 无参构造器链式调用 + @JsonProperty 全参构造器 |
| 修改 | `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` | T47 波及：setter→全参构造器 |
| 修改 | `ai-impl/.../orchestrator/impl/DiscussionConclusionCapabilityExecutor.java` | T47 波及：setter→全参构造器 |
| 修改 | `ai-impl/.../degradation/TimeoutDegradationStrategy.java` | T30 波及：Integer→int 空值检查调整 |
| 修改 | `ai-api/.../AiResultTest.java` | T29：删 5 个 setter 测试 + 新增 degradedWithErrorCode 测试 |
| 修改 | `ai-api/.../degradation/DegradationContextTest.java` | T30：assertNull→assertEquals(0, ...) + isInitialized 测试重写 |
| 修改 | `ai-impl/.../client/ChatToolDefinitionTest.java` | T46：删 setStrict 测试 + 改写序列化测试 |
| 修改 | `ai-impl/.../client/LlmChatOptionsTest.java` | T47：删 shouldSetAndGetFields |
| 修改 | `ai-impl/.../metrics/SlidingWindowMetricsStoreTest.java` | T30 波及：.intValue() 调用移除（int→原始类型） |

## 编译验证

mvn test-compile -pl ai-api,ai-impl -am 通过，无编译错误。

## 设计偏差说明

无偏差。设计规格中 DegradationContextTest 的 `isInitializedShouldReturnFalseWhenAllDefaults` 测试改为直接验证 `new DegradationContext()` 的 isInitialized() 返回 false；额外新增 2 个涵盖性测试（elapsedTime 和 failureCount 单独设置时 isInitialized 为 true），确保新语义完全覆盖。查找到的超出设计范围的波及测试文件 `SlidingWindowMetricsStoreTest.java`（2 处 `.intValue()` 调用）一并修复。

## 修订说明（v11 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| SlidingWindowMetricsStoreTest.java:38-39 中 assertNull(ctx.getInvocationCount()) 和 assertNull(ctx.getFailureCount()) 因 int 默认值 0 无法为 null，应改为 assertEquals(0, ...) | SlidingWindowMetricsStoreTest.java:38-39 中 assertNull → assertEquals(0, ...) |
