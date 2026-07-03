# 任务指令（v7）

## 动作
REVIEW_REVISED

## 任务描述
在 `ai-impl/client/` 包中创建 LLM 调用层 9 个 DTO/枚举类型文件（含 1 个内嵌静态类 LlmChatUsage），并同步修改 `LlmChatService.java` 接口新增 2 个方法签名：
- `ai-impl/.../client/ClientType.java` — enum, `HTTP_API / SPRING_AI`
- `ai-impl/.../client/AuthType.java` — enum, `API_KEY / OAUTH2 / NONE`
- `ai-impl/.../client/LlmChatMessageRole.java` — enum, `SYSTEM / USER / ASSISTANT`
- `ai-impl/.../client/LlmChatMessage.java` — class, `role + content`
- `ai-impl/.../client/LlmChatOptions.java` — class, 7 个强类型 LLM 参数字段
- `ai-impl/.../client/LlmChatRequest.java` — class, `messages + options + clientType + tools`
- `ai-impl/.../client/LlmChatResponse.java` — class, `content + usage + modelId + retryCount`，内嵌 `LlmChatUsage` 静态类
- `ai-impl/.../client/StructuredChatResult.java` — 泛型 class, `data + retryCount + usage`
- `ai-impl/.../client/ChatToolDefinition.java` — class, `name + description + parameters + strict`

- `ai-impl/.../client/LlmChatService.java` — **修改**（非新建），新增 `chat()` 和 `structuredChat()` 方法签名

修改 `LlmChatService.java` 方法签名：
- `CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request)`
- `<T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(LlmChatRequest request, Class<T> targetClass)`

注意 `structuredChat` 因 `StructuredChatResult<T>` 泛型擦除需标注 `@SuppressWarnings("unchecked")`。

编写对应单元测试文件 `*Test.java`，覆盖构造器/Getter/枚举值/序列化。

## 选择理由
LLM 客户端 DTO 是 Batch3 (P1) 的基础层，零外部代码依赖，底层优先。Task 6（7 项底座能力执行器）的 `doExecuteInternal()` 和 `executeStandardPipeline()` 均需引用这些 DTO 类型，必须先完成。

## 任务上下文
设计文档 `Docs/06_ood_phase5_G.md` §3.2 完整定义了每个类型的字段级契约。所有类型归属 `ai-impl/client/` 包。现有 `LlmChatService.java` 为空接口存根，需同步更新其方法签名以引用新 DTO 类型。

### 类型规格

#### ClientType (enum)
- 包: `com.aimedical.modules.ai.impl.client`
- 常量: `HTTP_API`, `SPRING_AI`
- 用途: 模型客户端类型枚举，用于 `ModelRoute.clientType` 和 `DelegatingLlmChatService` 分发决策

#### AuthType (enum)
- 包: `com.aimedical.modules.ai.impl.client`
- 常量: `API_KEY`, `OAUTH2`, `NONE`
- 用途: 端点认证方式，用于 `ModelRoute.authType`

#### LlmChatMessageRole (enum)
- 包: `com.aimedical.modules.ai.impl.client`
- 常量: `SYSTEM`, `USER`, `ASSISTANT`
- 用途: 对话消息角色枚举

#### LlmChatMessage (class)
- 包: `com.aimedical.modules.ai.impl.client`
- 字段: `role` (LlmChatMessageRole, final, 非空), `content` (String, final, 非空)
- 构造器: 全参构造器 `(LlmChatMessageRole role, String content)`
- Getter: `getRole()`, `getContent()`
- Jackson: 无参构造器 + 字段级 `@JsonProperty` 标注

#### LlmChatOptions (class)
- 包: `com.aimedical.modules.ai.impl.client`
- 字段 (全部可空):
  - `modelId` (String)
  - `temperature` (Double)
  - `maxTokens` (Integer)
  - `stopSequences` (List<String>)
  - `topP` (Double)
  - `frequencyPenalty` (Double)
  - `presencePenalty` (Double)
- 构造器: 无参构造器（全部 null） + 全参构造器（7 参数）
- Getter/Setter 各字段
- Jackson: 无参构造器 + Setter 方法（已满足 Jackson 反序列化要求）

#### LlmChatRequest (class)
- 包: `com.aimedical.modules.ai.impl.client`
- 字段:
  - `messages` (List<LlmChatMessage>, final, 非空)
  - `options` (LlmChatOptions, final, 可空)
  - `clientType` (ClientType, final, 可空)
  - `tools` (List<ChatToolDefinition>, final, 可空)
- 构造器: 全参构造器 `(List<LlmChatMessage> messages, LlmChatOptions options, ClientType clientType, @Nullable List<ChatToolDefinition> tools)`
- Getter: 各字段
- Jackson: 无参构造器 + 字段级 `@JsonProperty` 标注（因字段为 final，需标注构造器参数）

#### LlmChatResponse (class)
- 包: `com.aimedical.modules.ai.impl.client`
- 字段:
  - `content` (String, final, 非空)
  - `usage` (LlmChatUsage, final, 可空)
  - `modelId` (String, final, 可空)
  - `retryCount` (int, final, 默认 0)
- 内嵌静态类 `LlmChatUsage`:
  - 字段: `promptTokens` (int, final), `completionTokens` (int, final), `totalTokens` (int, final)
  - 构造器: 全参构造器 `(int promptTokens, int completionTokens, int totalTokens)`
  - Getter 各字段
- 构造器: 全参构造器 `(String content, LlmChatUsage usage, String modelId, int retryCount)`
- Getter: 各字段
- Jackson: 无参构造器 + 字段级 `@JsonProperty` 标注（因字段为 final）

#### StructuredChatResult<T> (class)
- 包: `com.aimedical.modules.ai.impl.client`
- 字段: `data` (T, final, 非空), `retryCount` (int, final, 默认 0), `usage` (LlmChatUsage, final, 可空)
- 构造器: 全参构造器 `(T data, int retryCount, LlmChatUsage usage)`
- Getter: 各字段
- Jackson: 无参构造器 + 字段级 `@JsonProperty` 标注（因字段为 final）

#### ChatToolDefinition (class)
- 包: `com.aimedical.modules.ai.impl.client`
- 字段:
  - `name` (String, final, 非空)
  - `description` (String, final, 非空)
  - `parameters` (com.fasterxml.jackson.databind.JsonNode, final, 非空)
  - `strict` (boolean, final, 默认 true)
- 构造器: 全参构造器 `(String name, String description, JsonNode parameters, boolean strict)`
- Getter: 各字段
- Jackson: 无参构造器 + 字段级 `@JsonProperty` 标注（因字段为 final）

### 额外要求
1. 所有 `AiResult` 引用通过已有的 `com.aimedical.modules.ai.api.AiResult` 导入
2. 不使用 Lombok（与项目现有约定一致）
3. 所有类需支持 Jackson 序列化：含 final 字段的类使用无参构造器 + `@JsonProperty` 字段标注（或构造器参数标注）；仅含 Setter 的类（如 LlmChatOptions）无参构造器 + Setter 即可满足

## 已有代码上下文
- `ai-impl/.../client/LlmChatService.java`: 当前为空接口，需新增 2 个方法签名
- `ai-impl/.../client/` 包: 现有目录，已有 `LlmChatService.java`
- `ai-api/.../AiResult.java`: 已有完整定义，可用于方法返回值
- 项目不使用 Lombok，所有 Getter/Setter/构造器需手写

## 修订说明（v7 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] 计划缺失 LlmChatService.java 同步更新 | 任务描述和涉及文件补充 LlmChatService.java（修改），新增 `chat()` 和 `structuredChat()` 方法签名及 `@SuppressWarnings("unchecked")` 标注 |
| [一般] 计划未提及 Jackson 序列化支持 | 每个 DTO 类规格补充 Jackson 序列化约束（无参构造器 + `@JsonProperty`），额外要求中集中说明 |
| [轻微] 文件计数表述不精确 | 统一表述为"9 个 DTO/枚举文件（含 1 个内嵌静态类 LlmChatUsage）" |
