# 详细设计（v9）

## 概述

修复 Task 6（7 项底座能力 CapabilityExecutor）测试代码中的 8 处 test-compile 错误以及 1 处隐藏的运行时 NPE。生产代码无需修改。

## 问题分析

### 问题 1：8 处 test-compile 错误

`StructuredOutputParser` 的泛型方法 `<T> T parse(String, Class<T>)` 在 lambda 表达式中无法进行 Java 编译器类型推断。将全部 8 处 lambda 替换为匿名内部类。

**受影响文件**：
- `AbstractCapabilityExecutorTest.java` — 7 处（lines 597, 641, 885, 934, 978, 1024, 1070）
- `DiscussionConclusionCapabilityExecutorTest.java` — 1 处（line 310）

### 问题 2：1 处隐藏运行时 NPE

`executeStandardPipelineShouldHandleNullPromptTemplateRender` 测试方法（line 724）将 `metricsStore` 参数传为 `null`，导致管线成功路径中 `handleSuccess()` 调用 `metricsStore.recordSuccess()` 时触发 NPE。该错误因前序轮次 ai-impl test-compile 失败而被隐藏。

## 修改方案

### 8 处 compile 错误

全部采用「lambda → 匿名内部类」替换，分 3 种模式：

| 模式 | 数量 | 原始 lambda | 替换为 |
|------|------|------------|--------|
| A 返回字符串字面量 | 6 处 | `(rawContent, targetClass) -> "parsed"` 或 `"parsedFromChat"` | 匿名内部类，`@SuppressWarnings("unchecked")`，`return (T) "..."` |
| B 抛异常 | 1 处 | `(rawContent, targetClass) -> { throw new RuntimeException("parse error"); }` | 匿名内部类，`public <T> T parse(...) { throw ... }` |
| C 返回复杂类型 | 1 处 | `(rawContent, targetClass) -> new DiscussionConclusionResponse()` | 匿名内部类，`@SuppressWarnings("unchecked")`，`return (T) new DiscussionConclusionResponse()` |

### 1 处 NPE

将 `metricsStore` 参数从 `null` 改为 `new SlidingWindowMetricsStore()` 实例。

## 文件修改清单

| 文件 | 修改内容 |
|------|---------|
| `AbstractCapabilityExecutorTest.java` | 7 处 lambda → 匿名内部类；1 处 metricsStore null → new 实例 |
| `DiscussionConclusionCapabilityExecutorTest.java` | 1 处 lambda → 匿名内部类 |

## 验证方式

```bash
mvn test -pl modules/ai/ai-impl -am
```

预期：test-compile 通过，258 个测试全部通过。
