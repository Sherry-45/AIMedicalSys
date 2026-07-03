# 详细设计（v15）

## 概述

R15 最终轮：RETRY 修复 v14 引入的 3 个 AiRouterPropertiesTest 失败 + 3 项遗留问题（T49/T64/T65/T66）。完成后 Phase 5G 全部 64 项问题修复完毕。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `router/AiRouterPropertiesTest.java` | 修改 | A：4行 `getMessage()`→`getFormattedMessage()` + 1行 `List.of`→`Collections.singletonList` |
| `client/DefaultCredentialProvider.java` | 修改 | B-T49：添加语义注释 |
| `fallback/PrescriptionLocalRuleFallback.java` | 修改 | C-T64：try-catch 包裹 + CHECK_SKIPPED；C-T65：过敏检查 drugName 回退 |
| `parser/StructuredOutputParseException.java` | **新建** | D-T66：专用异常类 |
| `parser/JsonStructuredOutputParser.java` | 修改 | D-T66：RuntimeException→StructuredOutputParseException |
| `parser/JsonStructuredOutputParserTest.java` | 修改 | D-T66：2处 `assertThrows` 类型更换 |

## 类型定义

### 子任务A — AiRouterPropertiesTest 修复

**文件**：`AiRouterPropertiesTest.java`

**变更**：

| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 72 | `.getMessage().contains("Invalid clientType value")` | `.getFormattedMessage().contains("Invalid clientType value")` |
| 73 | `.getMessage().contains("INVALID_CLIENT")` | `.getFormattedMessage().contains("INVALID_CLIENT")` |
| 94 | `.getMessage().contains("Invalid authType value")` | `.getFormattedMessage().contains("Invalid authType value")` |
| 95 | `.getMessage().contains("INVALID_AUTH")` | `.getFormattedMessage().contains("INVALID_AUTH")` |
| 115 | `.getMessage().contains("clientType")` | `.getFormattedMessage().contains("clientType")` |
| 116 | `.getMessage().contains("authType")` | `.getFormattedMessage().contains("authType")` |
| 160 | `List.of((ModelRouteConfig) null)` | `Collections.singletonList(null)` |

**说明**：`ILoggingEvent.getMessage()` 返回 SLF4J 原始消息模板（含 `{}` 占位符），不包含格式化后的参数值。`getFormattedMessage()` 返回最终格式化消息，包含实际参数值。

**import 新增**：
```java
import java.util.Collections;
```

**import 影响**：文件中已有 `import java.util.List;`、`import java.util.Map;`，新增 `Collections`。

### 子任务B — T49 DefaultCredentialProvider 语义注释

**文件**：`DefaultCredentialProvider.java`

**位置**：line 93，`consecutiveFailures.set(0);` 前插入注释

```java
// Cache hit: reset failure tracking since credential was served successfully
consecutiveFailures.set(0);
```

**说明**：仅添加注释，不修改任何行为。`consecutiveFailures` 跟踪「获取凭证的连续失败次数」，credentialStore 命中意味着当前请求成功，应重置计数器。

### 子任务C — PrescriptionLocalRuleFallback 健壮性

#### T64: fallback() 异常保护

**文件**：`PrescriptionLocalRuleFallback.java`

**变更**：`fallback()` 方法体原有逻辑（line 71-97）整体包裹 try-catch：

```java
public PrescriptionCheckResponse fallback(PrescriptionCheckRequest request) {
    PrescriptionCheckResponse result = new PrescriptionCheckResponse();
    result.setFromFallback(true);
    try {
        // existing body line 71-96 unchanged
        List<AlertItem> alerts = new ArrayList<>();
        // ... 4 check* calls and risk level assignment ...
        result.setAlerts(alerts);
    } catch (Exception e) {
        log.warn("Prescription local rule fallback failed, marking as CHECK_SKIPPED", e);
        result.setRiskLevel("CHECK_SKIPPED");
        result.setAlerts(Collections.emptyList());
    }
    return result;
}
```

**新增 riskLevel 可取值**：`"CHECK_SKIPPED"` — 表示降级检查因异常无法完成。

**import 影响**：`java.util.Collections` 已存在（line 4），无需新增。

#### T65: checkAllergy() 映射扩展

**文件**：`PrescriptionLocalRuleFallback.java`

**变更**：`checkAllergy()` 中 line 178 后插入 null 守卫及 drugName 回退：

```java
String ingredient = DRUG_INGREDIENTS.get(item.getDrugId());
if (ingredient == null && item.getDrugName() != null) {
    ingredient = item.getDrugName().toLowerCase();
}
```

**说明**：当 `DRUG_INGREDIENTS` 映射中不存在 `item.getDrugId()` 的记录时，回退使用 `item.getDrugName()` 的小写形式直接匹配过敏原。`item.getDrugName() != null` 守卫防止 NPE。

### 子任务D — JsonStructuredOutputParser 专用异常

#### StructuredOutputParseException

**形态**：`class extends RuntimeException`
**包路径**：`com.aimedical.modules.ai.impl.parser`
**职责**：表示结构化输出解析失败（JSON 解析异常等）

```java
package com.aimedical.modules.ai.impl.parser;

public class StructuredOutputParseException extends RuntimeException {
    public StructuredOutputParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**构造方式**：`new StructuredOutputParseException(String message, Throwable cause)`
**类型关系**：继承 `java.lang.RuntimeException`

#### JsonStructuredOutputParser 变更

**文件**：`JsonStructuredOutputParser.java`

```java
// line 30 变更前
throw new RuntimeException("Failed to parse JSON content to " + targetClass.getSimpleName(), e);
// 变更后
throw new StructuredOutputParseException("Failed to parse JSON content to " + targetClass.getSimpleName(), e);
```

**import 新增**：无需手动新增（同包引用）。

#### JsonStructuredOutputParserTest 变更

**文件**：`JsonStructuredOutputParserTest.java`

| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 54 | `assertThrows(RuntimeException.class, ...)` | `assertThrows(StructuredOutputParseException.class, ...)` |
| 61 | `assertThrows(RuntimeException.class, ...)` | `assertThrows(StructuredOutputParseException.class, ...)` |

**import 新增**：无需手动新增（同包引用）。

## 错误处理

| 场景 | 处理方式 |
|------|---------|
| PrescriptionLocalRuleFallback.fallback() 中任一 check* 方法抛异常 | catch Exception → log.warn + setRiskLevel("CHECK_SKIPPED") + 空 alerts 列表 |
| JsonStructuredOutputParser.parse() 中 JSON 解析失败 | throw StructuredOutputParseException（替代泛化 RuntimeException） |

## 行为契约

| 组件 | 契约 |
|------|------|
| `PrescriptionLocalRuleFallback.fallback()` | 任何异常均被捕获并标记 CHECK_SKIPPED，不向外传播 |
| `PrescriptionLocalRuleFallback.checkAllergy()` | 当 DRUG_INGREDIENTS 中无匹配且 drugName 非空时，以 drugName.toLowerCase() 作为成分名继续匹配 |
| `StructuredOutputParseException` | 仅由 `JsonStructuredOutputParser.parse()` 抛出 |
| `JsonStructuredOutputParser.parse()` | 异常类型由 `RuntimeException` 变更为 `StructuredOutputParseException`，行为语义不变 |

## 依赖关系

| 类型 | 依赖变更 |
|------|---------|
| `AiRouterPropertiesTest.java` | 新增 `import java.util.Collections;` |
| `JsonStructuredOutputParser.java` | 无需新增 import（同包） |
| `JsonStructuredOutputParserTest.java` | 无需新增 import（同包） |
| `PrescriptionLocalRuleFallback.java` | 无新增 import（`Collections` 已存在） |
| `DefaultCredentialProvider.java` | 无变更 |
