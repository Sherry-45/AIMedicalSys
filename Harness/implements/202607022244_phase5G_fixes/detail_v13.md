# 详细设计（v13）

## 概述

RETRY（R12）：修复 DiscussionConclusionCapabilityExecutorTest.java 末尾多余 `}` 导致编译失败。NEW（R13）：模板管理三项修复——T62 将 promptVersion 类型从 String 改为 Integer 并下溯至全部调用链；T54 使 DatabasePromptTemplateManager 缓存键包含 promptVersion 并在 resolveExactVersion 中先查缓存；T55 使 warmup 同时缓存 version 键。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | 修改 | R12 RETRY：删行 439 |
| `template/PromptTemplateManager.java` | 修改 | T62：接口 `String promptVersion` → `Integer` |
| `template/DatabasePromptTemplateManager.java` | 修改 | T62+T54+T55：类型变更 + 缓存键含版本 + warmup 双键 |
| `orchestrator/AbstractCapabilityExecutor.java` | 修改 | T62：3 方法签名 + 2 处 AiCallRecord 构造参数转换 |
| `orchestrator/impl/TriageCapabilityExecutor.java` | 修改 | T62：`String promptVersion` → `Integer`, `@Value` 默认 `0` |
| `orchestrator/impl/KbQueryCapabilityExecutor.java` | 修改 | T62：同上 |
| `orchestrator/impl/MedicalRecordGenCapabilityExecutor.java` | 修改 | T62：同上 |
| `orchestrator/impl/PrescriptionAssistCapabilityExecutor.java` | 修改 | T62：同上 |
| `orchestrator/impl/PrescriptionCheckCapabilityExecutor.java` | 修改 | T62：同上 |
| `orchestrator/impl/ScheduleCapabilityExecutor.java` | 修改 | T62：同上 |
| `orchestrator/impl/DiscussionConclusionCapabilityExecutor.java` | 修改 | T62：同上 |
| `template/DatabasePromptTemplateManagerTest.java` | 修改 | T62：`"2"`→`2`（3 处）, 测试改名 |
| `orchestrator/AbstractCapabilityExecutorTest.java` | 修改 | T62：23 处 `"v1"`→`1`, 2 处 doDegrade 签名 |

## 类型定义

### PromptTemplateManager
**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.template`

**变更前**：
```java
String render(String capabilityId, String departmentId, Map<String, Object> variables, String promptVersion);
```

**变更后**：
```java
String render(String capabilityId, String departmentId, Map<String, Object> variables, Integer promptVersion);
```

### DatabasePromptTemplateManager
**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.template`

**render() 签名变更**：`String promptVersion` → `Integer promptVersion`

**resolveExactVersion() 签名变更**：
```java
// 变更前
private PromptTemplate resolveExactVersion(String capabilityId, String departmentId, String promptVersion)
// 变更后
private PromptTemplate resolveExactVersion(String capabilityId, String departmentId, Integer promptVersion)
```

**resolveExactVersion 内部变更**（T54）：
- 移除 `Integer.parseInt()` 解析过程
- 新增：先通过 `buildCacheKey(capabilityId, departmentId, promptVersion)` 查缓存，命中则直接返回
- 新增：DB 查得 ACTIVE 模板后 `cache.put(key, pt)`

**新增 buildCacheKey 重载**（T54）：
```java
private static String buildCacheKey(String capabilityId, String departmentId, Integer promptVersion) {
    String base = capabilityId + ":" + (departmentId != null ? departmentId : "");
    if (promptVersion != null) {
        return base + ":v" + promptVersion;
    }
    return base;
}
```
现有 `buildCacheKey(capabilityId, departmentId)` 保留不变。

**warmup() 改造**（T55）：
```java
for (PromptTemplate pt : activeTemplates) {
    String key = buildCacheKey(pt.getCapabilityId(), pt.getDepartmentId());
    cache.put(key, pt);
    if (pt.getVersion() != null) {
        String versionKey = buildCacheKey(pt.getCapabilityId(), pt.getDepartmentId(), pt.getVersion());
        cache.put(versionKey, pt);
    }
}
```

### AbstractCapabilityExecutor
**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.orchestrator`

**方法签名变更**（3 处）：

1. `executeStandardPipeline()` 参数 `String promptVersion` → `Integer promptVersion`（line 336）
2. `doDegrade()` 参数 `String promptVersion` → `Integer promptVersion`（line 260）
3. `handleSuccess()` 参数 `String promptVersion` → `Integer promptVersion`（line 505）

**AiCallRecord 构造参数转换**（2 处）：

- `doDegrade()` line 267-268：
  ```java
  metricsCollector.record(new AiCallRecord(
      capabilityId, modelId, promptVersion != null ? String.valueOf(promptVersion) : null,
      userId, departmentId, sessionId, visitId, patientId, callerRole, callerId,
      elapsedMs, true, degradeReason, 0, 0));
  ```

- `handleSuccess()` line 509-513：
  ```java
  metricsCollector.record(new AiCallRecord(
      capabilityId, modelId, promptVersion != null ? String.valueOf(promptVersion) : null,
      userId, departmentId, sessionId, visitId, patientId, callerRole, callerId,
      elapsedMs, false, null,
      usage != null ? usage.getPromptTokens() : 0,
      usage != null ? usage.getCompletionTokens() : 0));
  ```

### 7 个子类字段变更
**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.orchestrator.impl`

所有 7 个 Executor 统一修改（TriageCapabilityExecutor, KbQueryCapabilityExecutor, MedicalRecordGenCapabilityExecutor, PrescriptionAssistCapabilityExecutor, PrescriptionCheckCapabilityExecutor, ScheduleCapabilityExecutor, DiscussionConclusionCapabilityExecutor）：

```java
// 变更前
@Value("${ai.prompt.version.XXX:XXX}")
private String promptVersion;

// 变更后
@Value("${ai.prompt.version.XXX:0}")
private Integer promptVersion;
```

具体映射：

| 类 | @Value key | 默认值变更 |
|----|-----------|----------|
| TriageCapabilityExecutor | `${ai.prompt.version.TRIAGE:TRIAGE}` | → `:0` |
| KbQueryCapabilityExecutor | `${ai.prompt.version.KB_QUERY:KB_QUERY}` | → `:0` |
| MedicalRecordGenCapabilityExecutor | `${ai.prompt.version.MEDICAL_RECORD_GEN:MEDICAL_RECORD_GEN}` | → `:0` |
| PrescriptionAssistCapabilityExecutor | `${ai.prompt.version.RX_ASSIST:RX_ASSIST}` | → `:0` |
| PrescriptionCheckCapabilityExecutor | `${ai.prompt.version.RX_AUDIT:RX_AUDIT}` | → `:0` |
| ScheduleCapabilityExecutor | `${ai.prompt.version.SCHEDULE:SCHEDULE}` | → `:0` |
| DiscussionConclusionCapabilityExecutor | `${ai.prompt.version.DISCUSSION_CONCLUSION:DISCUSSION_CONCLUSION}` | → `:0` |

## 测试变更

### DiscussionConclusionCapabilityExecutorTest.java（R12 RETRY）
- 删除 line 439 多余 `}`（line 438 已有关闭类的 `}`，line 439 为重复）

### DatabasePromptTemplateManagerTest.java（T62）

| 测试方法 | 变更前 | 变更后 |
|---------|--------|--------|
| `exactVersionActiveShouldReturnVersionContent` | `render(..., "2")` | `render(..., 2)` |
| `nonActiveVersionShouldFallbackToActive` | `render(..., "2")` | `render(..., 2)` |
| `versionNotFoundInDepartmentShouldFallbackToGlobal` | `render(..., "2")` | `render(..., 2)` |
| `invalidVersionStringShouldFallbackToActive` | `render(..., "not-a-number")` | 方法改名 `nullVersionShouldFallbackToActive`，`render(..., null)` |

### AbstractCapabilityExecutorTest.java（T62）

**23 处 executeStandardPipeline 调用**：所有 `Map.of(), "v1", null)` 改为 `Map.of(), 1, null)`

| 行号 | 变更前 | 变更后 |
|-----|--------|--------|
| 448, 507, 545, 574, 621, 670, 727, 781, 810, 858, 907, 956, 1001, 1059, 1120, 1176, 1234, 1289, 1588, 1646, 1687, 1729, 1773 | `"v1"` | `1` |

**2 处 doDegrade 覆盖**：

1. Lines 340-349：
   ```java
   // 参数 String promptVersion → Integer promptVersion
   protected AiResult<Object> doDegrade(String userId, long startTime, String degradeReason,
           Object request, String capabilityId, String departmentId, String callerRole,
           String callerId, String visitId, String patientId, String sessionId,
           String inputSummary, String outputSummary, Integer promptVersion,
           String modelId, String sentinelReason) {
   ```
   方法体不变（`promptVersion` 透传给 `super.doDegrade()`）。

2. Lines 1530-1537：
   ```java
   protected AiResult<Object> doDegrade(String userId, long startTime, String degradeReason,
           Object request, String capabilityId, String departmentId, String callerRole,
           String callerId, String visitId, String patientId, String sessionId,
           String inputSummary, String outputSummary, Integer promptVersion,
           String modelId, String sentinelReason) {
   ```
   方法体不变（不使用 `promptVersion`）。

## 错误处理

| 场景 | 处理方式 |
|------|---------|
| `@Value("${ai.prompt.version.XXX:0}")` 未配置 | Spring 注入 `0` 作为默认值，`resolveExactVersion(..., 0)` 返回 null → fallback 到 ACTIVE |
| 测试中 `render(..., 2)` | Integer 字面量，编译器自动装箱，无 NumberFormatException 风险 |

## 行为契约

| 组件 | 契约 |
|------|------|
| PromptTemplateManager.render() | 第 4 参数类型 `Integer`，null 表示使用 active 模板 |
| DatabasePromptTemplateManager.resolveExactVersion() | 先查缓存后查 DB；查得后写入缓存（仅 ACTIVE 状态） |
| DatabasePromptTemplateManager.warmup() | 每个 active 模板写入两个缓存键：active 键 + version 键 |
| AbstractCapabilityExecutor | AiCallRecord 构造时 `Integer → String` 转换 |

## 依赖关系

| 类型 | 依赖变更 |
|------|---------|
| AbstractCapabilityExecutor | 无新增/移除 import |
| 7 个子类 Executor | 无新增/移除 import |
| 测试文件 | 无新增/移除 import |
