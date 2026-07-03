# 详细设计（v7）

## 概述

在 `ai-impl/client/` 包中创建 9 个 LLM 调用层 DTO/枚举类型（含 1 个内嵌静态类 `LlmChatUsage`），并修改 `LlmChatService.java` 接口新增 2 个方法签名。所有类型归属包 `com.aimedical.modules.ai.impl.client`，不依赖 Lombok，支持 Jackson 序列化/反序列化。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/src/main/java/.../client/ClientType.java` | 新建 | 模型客户端类型枚举 |
| `ai-impl/src/main/java/.../client/AuthType.java` | 新建 | 端点认证方式枚举 |
| `ai-impl/src/main/java/.../client/LlmChatMessageRole.java` | 新建 | 对话消息角色枚举 |
| `ai-impl/src/main/java/.../client/LlmChatMessage.java` | 新建 | 消息 DTO：role + content |
| `ai-impl/src/main/java/.../client/LlmChatOptions.java` | 新建 | LLM 参数 DTO：7 个可选字段 |
| `ai-impl/src/main/java/.../client/LlmChatRequest.java` | 新建 | LLM 请求 DTO |
| `ai-impl/src/main/java/.../client/LlmChatResponse.java` | 新建 | LLM 响应 DTO，内嵌 `LlmChatUsage` |
| `ai-impl/src/main/java/.../client/StructuredChatResult.java` | 新建 | 泛型结构化结果 DTO |
| `ai-impl/src/main/java/.../client/ChatToolDefinition.java` | 新建 | 工具定义 DTO |
| `ai-impl/src/main/java/.../client/LlmChatService.java` | 修改 | 新增 `chat()` 和 `structuredChat()` 方法签名 |

## 类型定义

---

### ClientType

**形态**：enum
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：标识模型客户端类型，用于 ModelRoute.clientType 和分发决策

```java
public enum ClientType {
    HTTP_API,
    SPRING_AI
}
```

---

### AuthType

**形态**：enum
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：标识端点认证方式，用于 ModelRoute.authType

```java
public enum AuthType {
    API_KEY,
    OAUTH2,
    NONE
}
```

---

### LlmChatMessageRole

**形态**：enum
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：对话消息角色

```java
public enum LlmChatMessageRole {
    SYSTEM,
    USER,
    ASSISTANT
}
```

---

### LlmChatMessage

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：单条对话消息

```java
public class LlmChatMessage {

    private final LlmChatMessageRole role;
    private final String content;

    public LlmChatMessage() {}

    public LlmChatMessage(LlmChatMessageRole role, String content) {
        this.role = role;
        this.content = content;
    }

    public LlmChatMessageRole getRole() { return role; }
    public String getContent() { return content; }
}
```

**公开接口**：
- `getRole()` → `LlmChatMessageRole`
- `getContent()` → `String`

**构造方式**：全参构造器 `(LlmChatMessageRole role, String content)`；Jackson 使用无参构造器 + `@JsonProperty` 字段注入

**类型关系**：独立类

---

### LlmChatOptions

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：LLM 调用参数配置（全部可空）

```java
public class LlmChatOptions {

    private String modelId;
    private Double temperature;
    private Integer maxTokens;
    private List<String> stopSequences;
    private Double topP;
    private Double frequencyPenalty;
    private Double presencePenalty;

    public LlmChatOptions() {}

    public LlmChatOptions(String modelId, Double temperature, Integer maxTokens,
                          List<String> stopSequences, Double topP,
                          Double frequencyPenalty, Double presencePenalty) {
        this.modelId = modelId;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.stopSequences = stopSequences;
        this.topP = topP;
        this.frequencyPenalty = frequencyPenalty;
        this.presencePenalty = presencePenalty;
    }

    // Getters and Setters for all 7 fields
}
```

**公开接口**：Getter/Setter 各 7 个
**构造方式**：无参构造器 + 全参构造器（7 参数，顺序与字段声明对齐）
**Jackson**：无参构造器 + Setter（无需 `@JsonProperty`）

---

### LlmChatRequest

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：LLM 调用请求

```java
public class LlmChatRequest {

    private final List<LlmChatMessage> messages;
    private final LlmChatOptions options;
    private final ClientType clientType;
    private final List<ChatToolDefinition> tools;

    public LlmChatRequest() {}

    public LlmChatRequest(List<LlmChatMessage> messages, LlmChatOptions options,
                          ClientType clientType, List<ChatToolDefinition> tools) {
        this.messages = messages;
        this.options = options;
        this.clientType = clientType;
        this.tools = tools;
    }

    public List<LlmChatMessage> getMessages() { return messages; }
    public LlmChatOptions getOptions() { return options; }
    public ClientType getClientType() { return clientType; }
    public List<ChatToolDefinition> getTools() { return tools; }
}
```

**公开接口**：各字段 Getter
**构造方式**：全参构造器 `(List<LlmChatMessage>, LlmChatOptions, ClientType, List<ChatToolDefinition>)`
**Jackson**：无参构造器 + 字段级 `@JsonProperty` 标注

---

### LlmChatResponse

**形态**：class，内嵌 `public static class LlmChatUsage`
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：LLM 调用响应

```java
public class LlmChatResponse {

    private final String content;
    private final LlmChatUsage usage;
    private final String modelId;
    private final int retryCount;

    public LlmChatResponse() {}

    public LlmChatResponse(String content, LlmChatUsage usage, String modelId, int retryCount) {
        this.content = content;
        this.usage = usage;
        this.modelId = modelId;
        this.retryCount = retryCount;
    }

    public String getContent() { return content; }
    public LlmChatUsage getUsage() { return usage; }
    public String getModelId() { return modelId; }
    public int getRetryCount() { return retryCount; }

    // --- 内嵌静态类 ---

    public static class LlmChatUsage {

        private final int promptTokens;
        private final int completionTokens;
        private final int totalTokens;

        public LlmChatUsage() {}

        public LlmChatUsage(int promptTokens, int completionTokens, int totalTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }

        public int getPromptTokens() { return promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
        public int getTotalTokens() { return totalTokens; }
    }
}
```

**公开接口**：
- `LlmChatResponse`: `getContent()`, `getUsage()`, `getModelId()`, `getRetryCount()`
- `LlmChatUsage`: `getPromptTokens()`, `getCompletionTokens()`, `getTotalTokens()`

**构造方式**：全参构造器；Jackson 使用无参构造器 + `@JsonProperty` 字段注入
**Jackson**：无参构造器 + 字段级 `@JsonProperty`

---

### StructuredChatResult\<T\>

**形态**：泛型 class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：结构化 LLM 返回结果（泛型封装）

```java
public class StructuredChatResult<T> {

    private final T data;
    private final int retryCount;
    private final LlmChatUsage usage;

    public StructuredChatResult() {}

    public StructuredChatResult(T data, int retryCount, LlmChatUsage usage) {
        this.data = data;
        this.retryCount = retryCount;
        this.usage = usage;
    }

    public T getData() { return data; }
    public int getRetryCount() { return retryCount; }
    public LlmChatUsage getUsage() { return usage; }
}
```

**公开接口**：`getData()`, `getRetryCount()`, `getUsage()`
**构造方式**：全参构造器；Jackson 使用无参构造器 + `@JsonProperty` 字段注入
**Jackson**：无参构造器 + 字段级 `@JsonProperty`

---

### ChatToolDefinition

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：工具/函数定义描述

```java
public class ChatToolDefinition {

    private final String name;
    private final String description;
    private final JsonNode parameters;
    private boolean strict = true;  // 非 final，默认 true；Jackson 缺失时保留默认值

    public ChatToolDefinition() {}

    public ChatToolDefinition(String name, String description, JsonNode parameters, boolean strict) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.strict = strict;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public JsonNode getParameters() { return parameters; }
    public boolean isStrict() { return strict; }
    public void setStrict(boolean strict) { this.strict = strict; }
}
```

**公开接口**：`getName()`, `getDescription()`, `getParameters()`, `isStrict()`, `setStrict(boolean)`
**构造方式**：全参构造器；Jackson 使用无参构造器 + `@JsonProperty` 字段注入（`strict` 非 final，通过 Setter 或字段注入，缺失时保留 `true`）
**Jackson**：无参构造器 + 字段级 `@JsonProperty`（仅 `name`、`description`、`parameters` 三个 final 字段）+ `strict` 字段通过初始化值 `true` 保证默认

---

### LlmChatService（修改）

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：LLM 聊天服务接口，新增调用方法

```java
public interface LlmChatService {

    CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request);

    <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
            LlmChatRequest request, Class<T> targetClass);
}
```

**说明**：
- `@SuppressWarnings("unchecked")` 不在接口方法声明上标注（对接口方法无效），由实现类在泛型转型处按需添加
- `chat()` — 基础对话，返回 `LlmChatResponse`
- `structuredChat()` — 结构化输出，返回 `StructuredChatResult<T>`，`targetClass` 用于 Jackson 反序列化目标类型

## 错误处理

无自定义错误类型。所有 DTO 构造器/Getter 不抛出受检异常（null 参数由调用方保证）。枚举解析等错误由 Jackson 默认行为处理（反序列化失败抛 `JsonMappingException`）。

## 行为契约

1. **不可变性**：`LlmChatMessage`、`LlmChatRequest`、`LlmChatResponse`、`LlmChatUsage`、`StructuredChatResult` 的字段为 `final`（构造后不可变）；`ChatToolDefinition` 的 `name`/`description`/`parameters` 为 `final`，`strict` 可变
2. **Jackson 兼容性**：所有含 `final` 字段的类均提供 `public` 无参构造器 + 字段级 `@JsonProperty` 标注；`LlmChatOptions` 使用标准 Java Bean 风格（无参构造器 + Setter）
3. **空安全**：`messages`（`LlmChatRequest`）、`content`（`LlmChatResponse`）、`data`（`StructuredChatResult`）、`name`/`description`/`parameters`（`ChatToolDefinition`）语义上非空，调用方应保证非 null 传入
4. **泛型擦除**：`StructuredChatResult<T>` 的 Jackson 反序列化需要调用方提供 `Class<T>`；`structuredChat()` 的实现类需处理 `(StructuredChatResult<T>) result` 的 unchecked 转型

## 依赖关系

| 类型 | 依赖 |
|------|------|
| `LlmChatMessage` | `LlmChatMessageRole` |
| `LlmChatRequest` | `LlmChatMessage`, `LlmChatOptions`, `ClientType`, `ChatToolDefinition` |
| `LlmChatResponse` | `LlmChatUsage`（内嵌） |
| `StructuredChatResult` | `LlmChatUsage` |
| `LlmChatService` | `AiResult`（`com.aimedical.modules.ai.api`）, `LlmChatRequest`, `LlmChatResponse`, `StructuredChatResult`, `CompletableFuture` |

暴露给后续任务的接口：
- `LlmChatService.chat(LlmChatRequest)` → `CompletableFuture<AiResult<LlmChatResponse>>`
- `LlmChatService.structuredChat(LlmChatRequest, Class<T>)` → `CompletableFuture<AiResult<StructuredChatResult<T>>>`

## 修订说明（v7 r1）

| 审查意见 | 修改措施 |
|---------|---------|
| [严重] ChatToolDefinition.strict 默认值在 Jackson 反序列化时无法保持 | 将 `strict` 改为非 final 字段，初始化 `private boolean strict = true;`，仅保留 getter `isStrict()` 和 setter `setStrict()`；其余 3 个字段保持 final + `@JsonProperty` |
| [一般] @SuppressWarnings("unchecked") 标注位置不当 | 从接口方法声明移除该注解，在 LlmChatService 设计说明中注明"实现类在泛型转型处按需添加" |
| [一般] 无参构造器可见性与项目已有约定不一致 | 所有含 final 字段类的无参构造器统一改为 `public`（与项目现有 `TriageResponse`、`RecommendedDoctor` 等 DTO 风格一致） |
| [轻微] LlmChatUsage 内嵌静态类误标 final 修饰符 | 移除 `final` 类修饰符，保持 `public static class LlmChatUsage` |
| [轻微] LlmChatOptions 全参构造器参数顺序未与字段声明顺序对齐 | 已对齐无需处理；确认字段声明与构造器参数顺序一致 |

## 硬性约束落实

- ✅ `strict` 默认值问题：非 final + 初始化值 `true`，Jackson 缺失字段时保留 `true`
- ✅ `@SuppressWarnings("unchecked")` 移至实现类（接口声明处不标注）
- ✅ 无参构造器均 `public`
- ✅ `LlmChatUsage` 为 `public static class`（非 `final`）
- ✅ 不使用 Lombok
- ✅ 所有 `AiResult` 引用通过 `com.aimedical.modules.ai.api.AiResult` 导入
