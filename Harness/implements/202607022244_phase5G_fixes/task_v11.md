# 任务指令（v11）

## 动作
RETRY + NEW

## 任务描述

### RETRY（R10遗留）
修复 AiPlatformConfigTest 中 2 个测试失败：
1. `classShouldBeAnnotatedWithConditionalOnProperty:52` — `@ConditionalOnProperty.name()` 返回 `String[]`，`assertEquals` 应改为 `assertArrayEquals`
2. `springAiLlmChatServiceShouldBeAnnotatedWithConditionalOnClass:99` — `getMethod()` 只能找到 public 方法，应改为 `getDeclaredMethod()`

### NEW（R11）
修复数据模型与值对象 6 项不可变性/设计一致性修复：
- T29: AiResult 添加工厂方法 + 字段设为 final（不可变契约）
- T30: DegradationContext invocationCount/failureCount Integer→int + serialVersionUID 更新
- T31: DegradationContext.Builder 添加 builder() 静态工厂方法
- T32: Phase4BusinessException 添加 getErrorCode()
- T46: ChatToolDefinition.strict 移除 setter
- T47: LlmChatOptions 字段 final + 移除 setter（波及 2 个生产文件）

## 选择理由

R10 失败仅 2 处测试断言问题（同文件 AiPlatformConfigTest），修复代价极小，与 R11 无冲突可合并一轮。R11 6 项数据模型问题覆盖 5 个源文件，均属底层值对象不可变性加固——从 ai-api (AiResult/DegradationContext/Phase4BusinessException) 到 ai-impl (ChatToolDefinition/LlmChatOptions)，符合底层依赖优先原则。

## 任务上下文

### R10 RETRY 细节

**AiPlatformConfigTest.java**:
- **Line 52**: `assertEquals("ai.platform.enabled", ann.name())` — `@ConditionalOnProperty.name()` 返回 `String[]`。修正：`assertArrayEquals(new String[]{"ai.platform.enabled"}, ann.name())`
- **Line 97-99**: `AiPlatformConfig.class.getMethod("springAiLlmChatService")` — `springAiLlmChatService()` 为包级私有（`@Bean` 方法无 `public` 修饰符），`getMethod()` 仅访问 public 方法。修正：`getDeclaredMethod("springAiLlmChatService")`

当前 AiPlatformConfig 源码（已含 R10 T8/T9 修改）：
- Line 53: `@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true", matchIfMissing = false)`
- Line 122: `@Bean @ConditionalOnClass(name = "org.springframework.ai.chat.ChatModel") SpringAiLlmChatService springAiLlmChatService() { ... }`

### T29 — AiResult 不可变+工厂方法

当前状态（`ai-api/src/main/java/.../AiResult.java`，79行）：
```java
public class AiResult<T> {
    private boolean success;      // 非final
    private T data;               // 非final
    private String errorCode;     // 非final
    private boolean degraded;     // 非final
    private String fallbackReason;// 非final
    // 无参构造器 + 5参构造器
    // success/failure/degraded 3个静态工厂方法
    // 5组 getter/setter
}
```

要求：
- 缺少 `degradedWithErrorCode(String errorCode, String fallbackReason)` 工厂方法（degraded=true + 带 errorCode）
- 所有字段改为 `final`
- 无参构造器通过 `this(false, null, null, false, null)` 链式调用保留
- 移除全部 setter（仅保留 getter 和 isSuccess()/isDegraded()）

### T30 — DegradationContext Integer→int

当前状态（`ai-api/src/main/java/.../degradation/DegradationContext.java`，190行）：
```java
private Integer invocationCount;  // → int
private Integer failureCount;     // → int
```
- `serialVersionUID = 1L` → 改为 `2L`
- `postDeserializationValidate()`: `invocationCount == null || invocationCount == 0` → `invocationCount == 0`
- `isInitialized()`: `return invocationCount != null && failureCount != null` → 需要新的语义（如 `return invocationCount > 0 || failureCount > 0 || elapsedTime > 0L` 或加 `initialized` 布尔标志）
- Builder 中对应字段改为 `int`

波及生产代码：
- `TimeoutDegradationStrategy.java:19`: `Integer invocationCount = context.getInvocationCount(); if (invocationCount == null || invocationCount == 0)` → `int invocationCount = context.getInvocationCount(); if (invocationCount == 0)`

### T31 — DegradationContext.Builder.builder()

在 Builder 内部类中添加：
```java
public static Builder builder() {
    return new Builder();
}
```
现有 `new DegradationContext.Builder()` 用法仍兼容。

### T32 — Phase4BusinessException.getErrorCode()

当前状态（`ai-api/src/main/java/.../dto/base/Phase4BusinessException.java`，12行）：
```java
public abstract class Phase4BusinessException extends RuntimeException {
    protected Phase4BusinessException(String message) { super(message); }
    protected Phase4BusinessException(String message, Throwable cause) { super(message, cause); }
}
```

要求：
- 添加 `private final String errorCode` 字段
- 添加 `protected Phase4BusinessException(String message, String errorCode)` 构造器
- 添加 `public String getErrorCode() { return errorCode; }`（具体方法，非抽象）
- 保留现有 2 个构造器

无生产代码子类（搜索 0 个 `extends Phase4BusinessException`）。测试中使用匿名子类 `new Phase4BusinessException("msg") {}` 不受影响。

### T46 — ChatToolDefinition.strict 移除 setter

当前（`ai-impl/src/main/java/.../client/ChatToolDefinition.java`，32行）：
```java
private boolean strict = true;
public void setStrict(boolean strict) { this.strict = strict; }  // 删除此行
```

仅删除 `setStrict()` 方法，字段不做 `final`（Jackson 通过反射设置字段需要字段可写）。对外 API 仅暴露 `isStrict()`，即为只读。

### T47 — LlmChatOptions 不可变

当前状态（`ai-impl/src/main/java/.../client/LlmChatOptions.java`，49行）：7个字段（modelId/temperature/maxTokens/stopSequences/topP/frequencyPenalty/presencePenalty）全部有 setter。

要求：
- 所有字段改为 `final`
- 移除全部 setter
- 无参构造器通过 `this(null, null, null, null, null, null, null)` 链式调用
- 全参构造器保留含 `@JsonProperty`（如需要保持JSON反序列化）

⚠️ **波及生产代码**（2个文件）：
1. `AbstractCapabilityExecutor.java:388-391`:
   ```java
   LlmChatOptions options = new LlmChatOptions();
   options.setModelId(routeResult.getModelId());
   options.setTemperature(0.7);
   options.setMaxTokens(2048);
   ```
   → 改为一行全参构造器（含 stopSequences/topP/frequencyPenalty/presencePenalty 传 null）

2. `DiscussionConclusionCapabilityExecutor.java:190-193`:
   ```java
   LlmChatOptions options = new LlmChatOptions();
   options.setModelId(compressionLightweightEndpoint);
   options.setMaxTokens(1024);
   options.setTemperature(0.3);
   ```
   → 同理改为全参构造器

## 已有代码上下文

### 源文件清单与当前行数

| 文件 | 路径 | 当前行数 | 操作 |
|------|------|---------|------|
| AiPlatformConfigTest.java | `ai-impl/src/test/java/.../config/AiPlatformConfigTest.java` | 238 | RETRY修改 |
| AiResult.java | `ai-api/src/main/java/.../AiResult.java` | 79 | T29修改 |
| DegradationContext.java | `ai-api/src/main/java/.../degradation/DegradationContext.java` | 190 | T30+T31修改 |
| Phase4BusinessException.java | `ai-api/src/main/java/.../dto/base/Phase4BusinessException.java` | 12 | T32修改 |
| ChatToolDefinition.java | `ai-impl/src/main/java/.../client/ChatToolDefinition.java` | 32 | T46修改 |
| LlmChatOptions.java | `ai-impl/src/main/java/.../client/LlmChatOptions.java` | 49 | T47修改 |
| AbstractCapabilityExecutor.java | `ai-impl/src/main/java/.../orchestrator/AbstractCapabilityExecutor.java` | 559 | T47波及 |
| DiscussionConclusionCapabilityExecutor.java | `ai-impl/src/main/java/.../impl/DiscussionConclusionCapabilityExecutor.java` | 201 | T47波及 |
| TimeoutDegradationStrategy.java | `ai-impl/src/main/java/.../degradation/TimeoutDegradationStrategy.java` | 30 | T30波及 |

### 测试文件清单

| 文件 | 路径 | 当前行数 | 影响 |
|------|------|---------|------|
| AiPlatformConfigTest.java | 同上 | 238 | RETRY: 2处断言 |
| AiResultTest.java | `ai-api/src/test/java/.../AiResultTest.java` | 111 | T29: 删除5个setter测试，保留6个工厂+构造器测试 |
| DegradationContextTest.java | `ai-api/src/test/java/.../degradation/DegradationContextTest.java` | 191 | T30: isInitialized/null→0断言调整 |
| Phase4BusinessExceptionTest.java | `ai-api/src/test/java/.../dto/base/Phase4BusinessExceptionTest.java` | 28 | T32: 可能无需变更 |
| ChatToolDefinitionTest.java | `ai-impl/src/test/java/.../client/ChatToolDefinitionTest.java` | 91 | T46: 删除shouldAllowMutatingStrict，改写shouldReflectStrictMutationInSerialization |
| LlmChatOptionsTest.java | `ai-impl/src/test/java/.../client/LlmChatOptionsTest.java` | 98 | T47: 删除shouldSetAndGetFields，其余适配final字段 |

## RETRY 说明

R10 验证 561 tests: 559 pass / 1 failure / 1 error。均属 AiPlatformConfigTest 断言实现问题（非代码逻辑错误）：
1. `ann.name()` 数组 vs 字符串类型不匹配
2. `getMethod()` 不能访问包级私有方法，需 `getDeclaredMethod()`

修复仅在 AiPlatformConfigTest.java 单文件中改动，无需修改生产代码。
