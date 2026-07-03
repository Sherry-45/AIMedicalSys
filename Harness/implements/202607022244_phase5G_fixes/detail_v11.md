# 详细设计（v11）

## 概述

RETRY（R10）修复 2 处 AiPlatformConfigTest 断言实现问题。NEW（R11）修复 6 项数据模型/值对象不可变性与设计一致性缺陷（T29/T30/T31/T32/T46/T47），波及 3 个生产文件 + 4 个测试文件。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/.../config/AiPlatformConfigTest.java` | 修改 | RETRY：assertEquals→assertArrayEquals，getMethod→getDeclaredMethod |
| `ai-api/.../AiResult.java` | 修改 | T29：final字段 + 移除setter + 添加工厂方法 |
| `ai-api/.../degradation/DegradationContext.java` | 修改 | T30：Integer→int + serialVersionUID→2L + isInitialized新语义；T31：builder()静态工厂 |
| `ai-api/.../dto/base/Phase4BusinessException.java` | 修改 | T32：添加 errorCode 字段 + getErrorCode() + 新构造器 |
| `ai-impl/.../client/ChatToolDefinition.java` | 修改 | T46：移除 setStrict() |
| `ai-impl/.../client/LlmChatOptions.java` | 修改 | T47：final字段 + 移除setter + 无参构造器链式调用 |
| `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` | 修改 | T47波及：setter→全参构造器 |
| `ai-impl/.../impl/DiscussionConclusionCapabilityExecutor.java` | 修改 | T47波及：setter→全参构造器 |
| `ai-impl/.../degradation/TimeoutDegradationStrategy.java` | 修改 | T30波及：Integer→int 空值检查调整 |
| `ai-api/.../AiResultTest.java` | 修改 | T29：删除5个setter测试 + 保留工厂/构造器测试 + 新增degradedWithErrorCode测试 |
| `ai-api/.../degradation/DegradationContextTest.java` | 修改 | T30：isInitialized/null→0断言调整 |
| `ai-impl/.../client/ChatToolDefinitionTest.java` | 修改 | T46：删除shouldAllowMutatingStrict + 改写shouldReflectStrictMutationInSerialization |
| `ai-impl/.../client/LlmChatOptionsTest.java` | 修改 | T47：删除shouldSetAndGetFields + 其余适配final字段 |

## 类型定义

### RETRY — AiPlatformConfigTest

**形态**：class（已有，2处断言修改）
**包路径**：`com.aimedical.modules.ai.impl.config`

**变更1 — Line 52**：
```java
// 变更前：
assertEquals("ai.platform.enabled", ann.name());
// 变更后：
assertArrayEquals(new String[]{"ai.platform.enabled"}, ann.name());
```
`@ConditionalOnProperty.name()` 返回 `String[]`，必须使用 `assertArrayEquals`。

**变更2 — Line 97-99**：
```java
// 变更前：
ConditionalOnClass ann = AiPlatformConfig.class
    .getMethod("springAiLlmChatService")
    .getAnnotation(ConditionalOnClass.class);
// 变更后：
ConditionalOnClass ann = AiPlatformConfig.class
    .getDeclaredMethod("springAiLlmChatService")
    .getAnnotation(ConditionalOnClass.class);
```
`springAiLlmChatService()` 方法无 `public` 修饰符（包级私有 Spring @Bean 方法），`getMethod()` 仅访问 public 方法。

### T29 — AiResult

**形态**：class（原有，字段/方法变更）
**包路径**：`com.aimedical.modules.ai.api`

**字段变更**（全部改为 `final`）：
```java
private final boolean success;
private final T data;
private final String errorCode;
private final boolean degraded;
private final String fallbackReason;
```

**构造器变更**：
```java
// 无参构造器链式调用：
public AiResult() {
    this(false, null, null, false, null);
}

// 全参构造器（不变）：
public AiResult(boolean success, T data, String errorCode, boolean degraded, String fallbackReason) {
    this.success = success;
    this.data = data;
    this.errorCode = errorCode;
    this.degraded = degraded;
    this.fallbackReason = fallbackReason;
}
```

**工厂方法新增**：
```java
public static <T> AiResult<T> degradedWithErrorCode(String errorCode, String fallbackReason) {
    return new AiResult<>(false, null, errorCode, true, fallbackReason);
}
```

**移除全部 setter**（共5个）：`setSuccess()`、`setData()`、`setErrorCode()`、`setDegraded()`、`setFallbackReason()`

**保留全部 getter**（共5个）：`isSuccess()`、`getData()`、`getErrorCode()`、`isDegraded()`、`getFallbackReason()`

**保留现有工厂方法**（不变）：`success(T data)`、`failure(String)`、`failure(String,String)`、`degraded(String)`

### T30 — DegradationContext（Integer→int）

**形态**：class（已有，字段类型/方法变更）
**包路径**：`com.aimedical.modules.ai.api.degradation`

**字段变更**：
```java
private static final long serialVersionUID = 2L;  // 1L→2L
private int invocationCount;                       // Integer→int
private int failureCount;                          // Integer→int
```

**getter/setter 签名变更**：
```java
public int getInvocationCount() { return invocationCount; }
public void setInvocationCount(int invocationCount) { this.invocationCount = invocationCount; }
public int getFailureCount() { return failureCount; }
public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
```

**postDeserializationValidate() 变更**：
```java
// 变更前：
boolean allDefault = (invocationCount == null || invocationCount == 0)
        && (failureCount == null || failureCount == 0)
        && elapsedTime == 0L;
// 变更后（int 原始类型无需 null 检查）：
boolean allDefault = (invocationCount == 0)
        && (failureCount == 0)
        && elapsedTime == 0L;
```

**isInitialized() 新语义**（int 原始类型无法区分"未设置"，改用正数值检测）：
```java
public boolean isInitialized() {
    return invocationCount > 0 || failureCount > 0 || elapsedTime > 0L;
}
```
语义变更：从"字段已赋值（非null）"变为"存在有效数据（至少一个计数器>0）"。

**Builder 字段类型变更**：
```java
private int invocationCount;  // Integer→int
private int failureCount;     // Integer→int
```

**Builder setter 参数类型变更**：
```java
public Builder invocationCount(int invocationCount) { ... }
public Builder failureCount(int failureCount) { ... }
```

**Builder.build() 方法体不变**（直接赋值，原用法 `this.invocationCount` 已为 int，赋值给 `context.invocationCount` 兼容）

### T31 — DegradationContext.Builder.builder()

在 `Builder` 内部类中添加：
```java
public static Builder builder() {
    return new Builder();
}
```
现有 `new DegradationContext.Builder()` 调用仍兼容。

### T32 — Phase4BusinessException

**形态**：abstract class（已有，新增字段+构造器+方法）
**包路径**：`com.aimedical.modules.ai.api.dto.base`

**新增字段**：
```java
private final String errorCode;
```

**新增构造器**：
```java
protected Phase4BusinessException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
}
```

**新增方法**：
```java
public String getErrorCode() {
    return errorCode;
}
```

**保留现有2个构造器**（不变）：
```java
protected Phase4BusinessException(String message)
protected Phase4BusinessException(String message, Throwable cause)
```

### T46 — ChatToolDefinition

**形态**：class（已有，移除方法）
**包路径**：`com.aimedical.modules.ai.impl.client`

**移除方法**（仅删除 `setStrict()`，其余不变）：
```java
// 删除以下整行：
public void setStrict(boolean strict) { this.strict = strict; }
```

`strict` 字段保持 `private boolean strict = true;` 非 final（Jackson 通过反射写字段需要可写）。

### T47 — LlmChatOptions

**形态**：class（已有，字段/方法变更）
**包路径**：`com.aimedical.modules.ai.impl.client`

**字段变更**（全部改为 `final`）：
```java
private final String modelId;
private final Double temperature;
private final Integer maxTokens;
private final List<String> stopSequences;
private final Double topP;
private final Double frequencyPenalty;
private final Double presencePenalty;
```

**构造器变更**：
```java
// 无参构造器链式调用：
public LlmChatOptions() {
    this(null, null, null, null, null, null, null);
}

// 全参构造器添加 @JsonProperty（确保 Jackson 基于构造器反序列化）：
public LlmChatOptions(
        @JsonProperty("modelId") String modelId,
        @JsonProperty("temperature") Double temperature,
        @JsonProperty("maxTokens") Integer maxTokens,
        @JsonProperty("stopSequences") List<String> stopSequences,
        @JsonProperty("topP") Double topP,
        @JsonProperty("frequencyPenalty") Double frequencyPenalty,
        @JsonProperty("presencePenalty") Double presencePenalty) {
    this.modelId = modelId;
    this.temperature = temperature;
    this.maxTokens = maxTokens;
    this.stopSequences = stopSequences;
    this.topP = topP;
    this.frequencyPenalty = frequencyPenalty;
    this.presencePenalty = presencePenalty;
}
```

**移除全部 setter**（共7个）：
`setModelId`、`setTemperature`、`setMaxTokens`、`setStopSequences`、`setTopP`、`setFrequencyPenalty`、`setPresencePenalty`

**新增导入**：`com.fasterxml.jackson.annotation.JsonProperty`

## 波及生产代码变更

### AbstractCapabilityExecutor（T47波及）

**位置**：第388-391行

```java
// 变更前：
LlmChatOptions options = new LlmChatOptions();
options.setModelId(routeResult.getModelId());
options.setTemperature(0.7);
options.setMaxTokens(2048);

// 变更后（stopSequences/topP/frequencyPenalty/presencePenalty 传 null）：
LlmChatOptions options = new LlmChatOptions(
    routeResult.getModelId(), 0.7, 2048, null, null, null, null);
```

### DiscussionConclusionCapabilityExecutor（T47波及）

**位置**：第190-193行

```java
// 变更前：
LlmChatOptions options = new LlmChatOptions();
options.setModelId(compressionLightweightEndpoint);
options.setMaxTokens(1024);
options.setTemperature(0.3);

// 变更后（stopSequences/topP/frequencyPenalty/presencePenalty 传 null）：
LlmChatOptions options = new LlmChatOptions(
    compressionLightweightEndpoint, 0.3, 1024, null, null, null, null);
```

### TimeoutDegradationStrategy（T30波及）

**位置**：第19-20行

```java
// 变更前：
Integer invocationCount = context.getInvocationCount();
if (invocationCount == null || invocationCount == 0) {

// 变更后（int 原始类型无需 null 检查）：
int invocationCount = context.getInvocationCount();
if (invocationCount == 0) {
```

## 测试文件变更

### AiResultTest（T29）

| 行 | 操作 | 说明 |
|----|------|------|
| 64-71 | 删除 | `shouldSetAndGetSuccess` — setter 已移除 |
| 73-80 | 删除 | `shouldSetAndGetData` — setter 已移除 |
| 82-87 | 删除 | `shouldSetAndGetErrorCode` — setter 已移除 |
| 89-94 | 删除 | `shouldSetAndGetDegraded` — setter 已移除 |
| 96-101 | 删除 | `shouldSetAndGetFallbackReason` — setter 已移除 |
| 新增 | 新建 | `shouldCreateDegradedWithErrorCodeViaFactory` — 验证 `degradedWithErrorCode("ERR", "reason")` 返回 degraded=true、errorCode="ERR"、fallbackReason="reason" |

保留 7 个测试：`shouldCreateWithDefaultConstructor`、`shouldCreateWithAllArgsConstructor`、`shouldCreateSuccessResultViaFactory`、`shouldThrowNpeWhenSuccessWithNullData`、`shouldCreateFailureResultViaFactory`、`shouldCreateDegradedResultViaFactory`、`shouldSupportDifferentGenericTypes`。

`shouldCreateWithDefaultConstructor` 中 `assertFalse(result.isSuccess())` 等断言不受影响（无参构造器仍将 success=false, degraded=false）。

### DegradationContextTest（T30）

| 行 | 操作 | 说明 |
|----|------|------|
| 169-171 | 修改断言 | `defaultConstructorShouldCreateZeroValueInstance`：`assertNull(ctx.getInvocationCount())` → `assertEquals(0, ctx.getInvocationCount())`；`assertNull(ctx.getFailureCount())` → `assertEquals(0, ctx.getFailureCount())` |
| 143-149 | 修改断言 | `isInitializedShouldReturnTrueWhenBothCountsNotNull`（重命名为 `isInitializedShouldReturnTrueWhenBothCountsSet`）：不变，invocationCount=10>0、failureCount=2>0 仍返回 true |
| 151-157 | 修改断言 | `isInitializedShouldReturnFalseWhenInvocationCountNull`（重命名为 `isInitializedShouldReturnFalseWhenAllCountsZero`）：`failureCount(2)` → assertTrue(isInitialized)（因 failureCount=2>0）。或用 `invocationCount(0).failureCount(0).elapsedTime(0L)` 验证为 false |
| 159-165 | 修改断言 | `isInitializedShouldReturnFalseWhenFailureCountNull`（重命名为同上合并）：同上处理 |
| 43-44 | 修改断言 | `shouldBuildWithPartialFields`：`assertNull(ctx.getFailureCount())` → `assertEquals(0, ctx.getFailureCount())` |
| 182-183 | 修改断言 | `defaultConstructorShouldCreateZeroValueInstance`：同上 |

**推荐合并方案**：将 `isInitializedShouldReturnFalseWhenInvocationCountNull` 和 `isInitializedShouldReturnFalseWhenFailureCountNull` 合并为一个测试 `isInitializedShouldReturnFalseWhenAllDefaults()`，验证 `new DegradationContext()` 的 isInitialized() 为 false。

### ChatToolDefinitionTest（T46）

| 行 | 操作 | 说明 |
|----|------|------|
| 32-40 | 删除 | `shouldAllowMutatingStrict` — setStrict() 已移除 |
| 82-90 | 改写 | `shouldReflectStrictMutationInSerialization` → 改为 `shouldSerializeWithDefaultStrictTrue`：移除 setStrict 调用，仅验证序列化后 strict:true；或直接删除此测试（已有 `shouldSerializeToJson` 覆盖 strict:true 场景） |

### LlmChatOptionsTest（T47）

| 行 | 操作 | 说明 |
|----|------|------|
| 38-55 | 删除 | `shouldSetAndGetFields` — setter 已移除 |
| 57-64 | 不变 | `shouldSerializeToJson` — 全参构造器+getter 不受影响 |
| 67-75 | 不变 | `shouldDeserializeFromJson` — Jackson 通过 @JsonProperty 构造器反序列化 |
| 77-90 | 不变 | `shouldRoundTripThroughJson` — 同上 |
| 92-97 | 不变 | `shouldHandleEmptyJsonObject` — 无参构造器链式调用后均为 null |

## 错误处理

| 文件 | 变更 |
|------|------|
| AiResult | 字段 final 后，setter 不再可用。所有对象创建必须通过构造器或工厂方法 |
| DegradationContext | `postDeserializationValidate()` 中 `invocationCount==0` 语义与原先 `invocationCount==null||invocationCount==0` 一致（int 默认值即 0） |
| TimeoutDegradationStrategy | `invocationCount==0` 短路条件与原先 `null||0` 一致 |

## 行为契约

| 组件 | 契约 |
|------|------|
| AiResult | 所有字段构造后不可变。无参构造器产生 `success=false, data=null, errorCode=null, degraded=false, fallbackReason=null`。`degradedWithErrorCode()` 返回 degraded=true 且 errorCode 非空 |
| DegradationContext | invocationCount/failureCount 原始类型 int。isInitialized() 语义：至少一个计数器 > 0 或 elapsedTime > 0 时返回 true。Builder 新提供 builder() 静态工厂 |
| Phase4BusinessException | 新增 errorCode 字段可选。3 种构造方式：仅 message、message+cause、message+errorCode |
| ChatToolDefinition | strict 字段对外只读（isStrict()），Jackson 仍可反射写入 |
| LlmChatOptions | 7 字段构造后不可变。Jackson 反序列化依赖 @JsonProperty 全参构造器。无参构造器产生全 null 实例 |
| TimeoutDegradationStrategy | shouldDegrade 中 invocationCount 以 int 处理，删除 null 检查 |

## 依赖关系

| 类型 | 依赖变更 |
|------|---------|
| LlmChatOptions | 新增导入：`com.fasterxml.jackson.annotation.JsonProperty` |
| 其余类型 | 无新增/移除依赖 |

## 修订说明

（首轮设计，无审查意见）
