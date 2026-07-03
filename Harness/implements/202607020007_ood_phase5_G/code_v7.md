# 实现报告（v7）

## 概述

在 `com.aimedical.modules.ai.impl.client` 包中创建了 9 个新类型（3 枚举 + 6 DTO 类，含 1 个内嵌静态类 `LlmChatUsage`），并修改了 `LlmChatService.java` 接口新增 2 个方法签名。所有类型不依赖 Lombok，支持 Jackson 序列化/反序列化。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | `ai-impl/src/main/java/.../client/ClientType.java` | 模型客户端类型枚举（HTTP_API, SPRING_AI） |
| 新建 | `ai-impl/src/main/java/.../client/AuthType.java` | 端点认证方式枚举（API_KEY, OAUTH2, NONE） |
| 新建 | `ai-impl/src/main/java/.../client/LlmChatMessageRole.java` | 对话消息角色枚举（SYSTEM, USER, ASSISTANT） |
| 新建 | `ai-impl/src/main/java/.../client/LlmChatMessage.java` | 消息 DTO，final role + content，@JsonProperty 标注 |
| 新建 | `ai-impl/src/main/java/.../client/LlmChatOptions.java` | LLM 参数 DTO，Java Bean 风格（无参构造器 + Setter） |
| 新建 | `ai-impl/src/main/java/.../client/LlmChatRequest.java` | 请求 DTO，4 个 final 字段 + @JsonProperty |
| 新建 | `ai-impl/src/main/java/.../client/LlmChatResponse.java` | 响应 DTO，内嵌 `public static class LlmChatUsage`（3 个 final 字段） |
| 新建 | `ai-impl/src/main/java/.../client/StructuredChatResult.java` | 泛型结构化结果 DTO，引用 `LlmChatResponse.LlmChatUsage` |
| 新建 | `ai-impl/src/main/java/.../client/ChatToolDefinition.java` | 工具定义 DTO，name/description/parameters 为 final + @JsonProperty，strict 可变+初始化 true |
| 修改 | `ai-impl/src/main/java/.../client/LlmChatService.java` | 新增 `chat()` 和 `structuredChat()` 方法签名 |

## 编译验证

`mvn compile -pl ai-impl -am` — 编译通过（无输出 = 成功）。

## 设计偏差说明

| 设计规格 | 偏差说明 | 实际处理 |
|---------|---------|---------|
| `StructuredChatResult` 引用 `LlmChatUsage` | `LlmChatUsage` 是 `LlmChatResponse` 的内嵌静态类，设计未注明全限定路径 | 使用 `LlmChatResponse.LlmChatUsage` 在字段/构造器参数/Getter 中引用 |

## 修订说明（v7 r1）

| 审查意见 | 修改措施 |
|---------|---------|
| [严重] ChatToolDefinition.strict 的 `@JsonProperty("strict")` 标注在构造器参数上，导致 Jackson 使用参数化构造器注入，JSON 缺失 strict 时得到 false 而非 true | 从全参构造器中移除 `strict` 参数（采用审查推荐的方案 A），构造器改为 3 参数 `(name, description, parameters)`；`strict` 通过字段初始化 `true` 保证默认值，Jackson 通过 `setStrict()` 设置 JSON 中存在的值 |
