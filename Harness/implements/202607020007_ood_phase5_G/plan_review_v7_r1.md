# 计划审查报告（v7 r1）

## 审查结果
REJECTED

## 发现

### [严重] 计划缺失 LlmChatService.java 同步更新

task_v7.md §额外要求（第 103-105 行）明确要求：**同步更新 `LlmChatService.java` 接口，新增 2 个方法签名**：
- `CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request)`
- `<T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(LlmChatRequest request, Class<T> targetClass)`

当前计划仅提及 9 个 DTO/枚举文件的创建，未包含此修改。当前 `LlmChatService.java` 为空接口存根（4 行），此更新是 Task 7 的必要产出，遗漏将导致 Task 9 衔接失败。

### [一般] 计划未提及 Jackson 序列化支持

task_v7.md §额外要求（第 108 行）要求所有 DTO 类支持 Jackson 序列化（通过 `@JsonProperty` 或默认无参构造器实现）。计划中未提及此约束，可能出现类型设计不支持序列化的问题。

### [轻微] 文件计数表述不精确

计划称"10 个 DTO 与枚举类型"，但实际为 9 个独立文件 + LlmChatUsage 内嵌静态类（位于 LlmChatResponse.java 内部）。建议修正为"9 个 DTO/枚举文件（含 1 个内嵌静态类）"以消除歧义。

## 修改要求

1. **[严重]** 在计划中补充：Task 7 需同步修改 `LlmChatService.java`，新增 `chat()` 和 `structuredChat()` 方法签名，并列出其完整方法签名和 `@SuppressWarnings("unchecked")` 标注（因 `StructuredChatResult<T>` 泛型擦除）。将涉及文件补充为 `LlmChatService.java`（修改）+ 9 个 DTO 文件（新建）。

2. **[一般]** 在计划中明确每个 DTO 类需支持 Jackson 序列化的约束（`@JsonProperty` 字段标注 + 无参构造器或 Jackson 兼容设计），无 Lombok 约定已在上下文中体现，可复用。

3. **[轻微]** 将文件计数统一调整为"9 个 DTO/枚举文件"，明确说明 LlmChatUsage 为内嵌静态类。
