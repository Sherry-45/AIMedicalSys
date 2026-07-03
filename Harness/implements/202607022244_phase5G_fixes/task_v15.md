# 任务指令（v15）

## 动作
RETRY (R14) + NEW (R15)

## 任务描述

### PART A — RETRY: 修复 AiRouterPropertiesTest 3 个测试失败
修复 v14 新引入的 `AiRouterPropertiesTest` 3 个失败（2 failure + 1 error）：
- **根因 1**：`shouldLogWarningForInvalidClientType`(line 72-73) 和 `shouldLogWarningForInvalidAuthType`(line 94-95) 使用 `getMessage()` 获取日志消息。Logback 中 `ILoggingEvent.getMessage()` 返回原始消息模板（含 SLF4J `{}` 占位符），不包含格式化参数值。例如 `log.warn("Invalid clientType value: '{}', falling back to {}", ...)` 的 `getMessage()` 返回 `"Invalid clientType value: '{}', falling back to {}"`，不包含 `"INVALID_CLIENT"`。
- **修复**：所有 `getMessage()` → `getFormattedMessage()`
- **根因 2**：`shouldHandleNullConfigGracefully`(line 160) 使用 `List.of((ModelRouteConfig) null)` — `List.of()` 是 Java 9+ 不可变列表工厂，不允许 null 元素，抛出 NPE。
- **修复**：`List.of(null)` → `Collections.singletonList(null)`（`Collections.singletonList()` 允许 null 元素）
- **涉及文件**：`ai-impl/src/test/.../router/AiRouterPropertiesTest.java`

### PART B — NEW T49: DefaultCredentialProvider 语义注释
`DefaultCredentialProvider.getCredential()` 中 `credentialStore` 命中后 `consecutiveFailures.set(0)` 添加语义说明注释。
- **位置**：`DefaultCredentialProvider.java:93`
- **行为**：仅添加注释，不改行为。当前语义正确——`consecutiveFailures` 跟踪「获取凭证的连续失败次数」，从 credentialStore 获取到凭证即本次调用成功，应重置计数器。
- **涉及文件**：`ai-impl/src/main/.../client/DefaultCredentialProvider.java`

### PART C — NEW T64+T65: PrescriptionLocalRuleFallback 健壮性增强
**T64**（异常处理 + CHECK_SKIPPED）：
- 将 `fallback()` 方法体包裹在 try-catch 中
- catch 时 `log.warn("Prescription local rule fallback failed, marking as CHECK_SKIPPED", e)`
- 设置 `result.setRiskLevel("CHECK_SKIPPED")`，设置空 `alerts` 列表，返回 result
- 新增 `"CHECK_SKIPPED"` 作为 riskLevel 可取值

**T65**（过敏检查映射扩展）：
- `checkAllergy()` 中 `DRUG_INGREDIENTS.get(item.getDrugId())` 返回 null 时
- 且 `item.getDrugName() != null` 时，回退到 `item.getDrugName().toLowerCase()` 直接匹配过敏原

- **涉及文件**：`ai-impl/src/main/.../fallback/PrescriptionLocalRuleFallback.java`

### PART D — NEW T66: JsonStructuredOutputParser 专用异常
- 在 `parser` 包中新建 `StructuredOutputParseException extends RuntimeException`
- 构造器：`StructuredOutputParseException(String message, Throwable cause)`
- `JsonStructuredOutputParser.parse()` 中 `throw new RuntimeException(...)` → `throw new StructuredOutputParseException(...)`
- `JsonStructuredOutputParserTest` 中 2 处 `assertThrows(RuntimeException.class, ...)` → `assertThrows(StructuredOutputParseException.class, ...)`
- **涉及文件**（新建）：`ai-impl/src/main/.../parser/StructuredOutputParseException.java`
- **涉及文件**（修改）：`JsonStructuredOutputParser.java`, `JsonStructuredOutputParserTest.java`

## 选择理由

R14 验证失败仅 AiRouterPropertiesTest 单文件 3 处问题，修复代价极小（`getFormattedMessage()` 替换 + `Collections.singletonList` 替换）。R15 四项任务为全部剩余待修复项（T49/T64/T65/T66），完成后 Phase 5G 所有 64 项问题修复完毕。四项任务涉及 4 个独立源文件，无相互依赖，合并一轮处理。这是最终轮（R15）。

## 任务上下文

### RETRY 详细修复
**AiRouterPropertiesTest.java** 变更：
| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 72 | `assertTrue(listAppender.list.get(0).getMessage().contains("Invalid clientType value"));` | `assertTrue(listAppender.list.get(0).getFormattedMessage().contains("Invalid clientType value"));` |
| 73 | `assertTrue(listAppender.list.get(0).getMessage().contains("INVALID_CLIENT"));` | `assertTrue(listAppender.list.get(0).getFormattedMessage().contains("INVALID_CLIENT"));` |
| 94 | `assertTrue(listAppender.list.get(0).getMessage().contains("Invalid authType value"));` | `assertTrue(listAppender.list.get(0).getFormattedMessage().contains("Invalid authType value"));` |
| 95 | `assertTrue(listAppender.list.get(0).getMessage().contains("INVALID_AUTH"));` | `assertTrue(listAppender.list.get(0).getFormattedMessage().contains("INVALID_AUTH"));` |
| 160 | `props.setRoutes(Map.of("CAP", List.of((ModelRouteConfig) null)));` | `props.setRoutes(Map.of("CAP", Collections.singletonList(null)));` |

需要新增 import：`import java.util.Collections;`

### T49 DefaultCredentialProvider
**DefaultCredentialProvider.java:93** 当前行：
```java
consecutiveFailures.set(0);
```
改为：
```java
// Cache hit: reset failure tracking since credential was served successfully
consecutiveFailures.set(0);
```
仅添加注释，不改行为。

### T64 PrescriptionLocalRuleFallback 异常处理
**变更前**（fallback 方法体无异常保护）：
```java
public PrescriptionCheckResponse fallback(PrescriptionCheckRequest request) {
    PrescriptionCheckResponse result = new PrescriptionCheckResponse();
    result.setFromFallback(true);
    List<AlertItem> alerts = new ArrayList<>();
    // ... risk level logic ...
    result.setAlerts(alerts);
    return result;
}
```

**变更后**：
```java
public PrescriptionCheckResponse fallback(PrescriptionCheckRequest request) {
    PrescriptionCheckResponse result = new PrescriptionCheckResponse();
    result.setFromFallback(true);
    try {
        List<AlertItem> alerts = new ArrayList<>();
        List<PrescriptionCheckItem> items = request.getPrescriptionItems();
        if (items != null && !items.isEmpty()) {
            checkDrugInteractions(items, alerts);
            checkDoseRange(items, alerts);
            checkDuplicateDrugs(items, alerts);
        }
        PatientInfo patient = request.getPatientInfo();
        if (patient != null && items != null && !items.isEmpty()) {
            checkAllergy(patient, items, alerts);
        }
        if (patient != null && items != null && !items.isEmpty()) {
            checkSpecialPopulation(patient, items, alerts);
        }
        if (hasBlockAlert(alerts)) {
            result.setRiskLevel("BLOCK");
        } else if (hasWarnAlert(alerts)) {
            result.setRiskLevel("WARN");
        } else {
            result.setRiskLevel("PASS");
        }
        result.setAlerts(alerts);
    } catch (Exception e) {
        log.warn("Prescription local rule fallback failed, marking as CHECK_SKIPPED", e);
        result.setRiskLevel("CHECK_SKIPPED");
        result.setAlerts(Collections.emptyList());
    }
    return result;
}
```

需要新增 import：`import java.util.Collections;`（空列表用 `Collections.emptyList()` 或直接 `new ArrayList<>()`）

### T65 PrescriptionLocalRuleFallback 过敏检查扩展
**变更前**（checkAllergy line 178）：
```java
String ingredient = DRUG_INGREDIENTS.get(item.getDrugId());
```

**变更后**：
```java
String ingredient = DRUG_INGREDIENTS.get(item.getDrugId());
if (ingredient == null && item.getDrugName() != null) {
    ingredient = item.getDrugName().toLowerCase();
}
```

### T66 JsonStructuredOutputParser 专用异常
**新建 `StructuredOutputParseException.java`**（`ai-impl/src/main/.../parser/`）：
```java
package com.aimedical.modules.ai.impl.parser;

public class StructuredOutputParseException extends RuntimeException {
    public StructuredOutputParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**`JsonStructuredOutputParser.java` line 30**：
- `throw new RuntimeException(...)` → `throw new StructuredOutputParseException(...)`

**`JsonStructuredOutputParserTest.java`** 变更：
- line 54：`assertThrows(RuntimeException.class, ...)` → `assertThrows(StructuredOutputParseException.class, ...)`
- line 61：`assertThrows(RuntimeException.class, ...)` → `assertThrows(StructuredOutputParseException.class, ...)`

## 已有代码上下文

- `AiRouterProperties.java:52-62` — `log.warn("Invalid clientType/authType value: '{}', falling back to {}")` 使用 SLF4J `{}` 占位符
- `DefaultCredentialProvider.java:90-95` — `credentialStore.get()` 命中后 `consecutiveFailures.set(0)` + `cache.put()` + `return Optional.of(stored)`
- `PrescriptionLocalRuleFallback.java:68-98` — `fallback()` 无 try-catch，直接调用 4 个 check* 方法后设置 riskLevel
- `PrescriptionLocalRuleFallback.java:155-186` — `checkAllergy()` 仅通过 `DRUG_INGREDIENTS` 映射查找药物成分
- `JsonStructuredOutputParser.java:26-31` — `catch (JsonProcessingException e)` 内部 `throw new RuntimeException(...)`
- 项目已有自定义异常模式：`client/exception/` 包下有 `LlmInfrastructureException`, `CredentialUnavailableException`, `AiAbilityInputInvalidException`, `StructuredOutputNotSupportedException` 均继承 `RuntimeException`

## RETRY 说明（R14 AiRouterPropertiesTest）

| 问题 | 根因 | 修正 |
|------|------|------|
| `shouldLogWarningForInvalidClientType:73` 期望 true 得到 false | `getMessage()` 返回原始模板含 `{}`，不包含 `INVALID_CLIENT` | 改为 `getFormattedMessage()` |
| `shouldLogWarningForInvalidAuthType:95` 期望 true 得到 false | 同上 | 改为 `getFormattedMessage()` |
| `shouldHandleNullConfigGracefully:160` NullPointerException | `List.of()` 不允许 null 元素 | 改为 `Collections.singletonList(null)` |

---

## 修订说明（v15 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] T65 代码片段中 `item.getDrugName().toLowerCase()` 缺少 `item.getDrugName() != null` 守卫，drugName 为 null 时抛 NPE | task_v15.md 中 T65 任务描述补充 `item.getDrugName() != null` 条件说明；plan.md 中 T65 代码片段同步添加 null 守卫 |
