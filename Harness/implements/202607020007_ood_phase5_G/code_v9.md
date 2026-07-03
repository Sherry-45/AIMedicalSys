# 实现报告（v9）

## 概述

修复了 `AbstractCapabilityExecutorTest` 和 `DiscussionConclusionCapabilityExecutorTest` 中 8 处由 `StructuredOutputParser` 泛型 lambda 引起的 test-compile 错误，以及 1 处 `metricsStore` 为 null 导致的隐藏运行时 NPE。生产代码无修改。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutorTest.java` | 7 处 lambda→匿名内部类；1 处 NPE 修复 |
| 修改 | `modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | 1 处 lambda→匿名内部类 |

## 编译验证

`mvn test -pl modules/ai/ai-impl -am` — test-compile 通过，258 个测试全部通过（Failures: 0, Errors: 0, Skipped: 0）。

## 设计偏差说明

无偏差。全部改动严格按照详细设计 v9 执行：

| 模式 | 数量 | 原始 lambda | 替换为 |
|------|------|------------|--------|
| A 返回字符串字面量 | 6 处 | `(rawContent, targetClass) -> "parsed"` 或 `"parsedFromChat"` | 匿名内部类，`@SuppressWarnings("unchecked")`，`return (T) "..."` |
| B 抛异常 | 1 处 | `(rawContent, targetClass) -> { throw new RuntimeException("parse error"); }` | 匿名内部类，`public <T> T parse(...) { throw ... }` |
| C 返回复杂类型 | 1 处 | `(rawContent, targetClass) -> new DiscussionConclusionResponse()` | 匿名内部类，`@SuppressWarnings("unchecked")`，`return (T) new DiscussionConclusionResponse()` |

NPE 修复：`executeStandardPipelineShouldHandleNullPromptTemplateRender` 中 `metricsStore` 参数从 `null` 改为 `new SlidingWindowMetricsStore()` 实例。
