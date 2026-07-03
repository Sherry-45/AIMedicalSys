# 计划审查报告（v13 r1）

## 审查结果
REJECTED

## 发现

- **[严重] endpointId 来源缺失**：`HttpApiLlmChatService.chat()` 实现流程要求调用 `credentialProvider.getCredential(endpointId)` 和 `endpointRateLimiter.tryAcquire(endpointId)`，但 `LlmChatRequest` 不存在 `endpointId` 字段（仅含 `messages`/`options`/`clientType`/`tools`）。设计文档 §3.2.2 (line 1605, 1911) 明确指出凭据查询和限流均在 `LlmChatService` 实现类中按 `endpointId` 执行，但该字段无传入途径。任务未说明 `endpointId` 的来源。

- **[严重] DelegatingLlmChatService 改造未纳入任务范围**：plan.md (line 283) 将 DelegatingLlmChatService 构造器从 `List<LlmChatService>` 改为 `Map<ClientType, LlmChatService>` 注入列为 Task 10 的同步修改项，但 task_v13.md 任务描述仅列出 4 个新建文件，未包含 DelegatingLlmChatService 改造。

- **[一般] 新实现无法被 DelegatingLlmChatService 发现**：HttpApiLlmChatService/SpringAiLlmChatService 不标注 `@Service`（符合设计），而当前 DelegatingLlmChatService 使用 `List<LlmChatService>` 自动注入 + `@PostConstruct` 构建 Map。在不改造 DelegatingLlmChatService 的前提下，4 个新实现不会被自动装配。需在 Task 10 中同步改造 DelegatingLlmChatService，或明确依赖 Task 18 (AiPlatformConfig) 完成的过渡方案。

- **[一般] endpointId 字段缺失阻塞测试编写**：由于 endpointId 来源未定义，`HttpApiLlmChatService` 的 chat() 完整实现无法编写，对应的单元测试也无法覆盖凭据获取/限流/HTTP 调用三大核心路径。

## 修改要求

1. **[严重]** 明确 `endpointId` 的来源方案：选项 A — 在 `LlmChatRequest` 新增 `endpointId` 字段（修改现有 LlmChatRequest.java），由 CapabilityExecutor 在构造 request 时从 `ModelRoute` 设置；选项 B — 凭据获取和限流检查移至 CapabilityExecutor 层，不在 `HttpApiLlmChatService` 中执行。推荐选项 A，与设计文档 §3.2.2 一致。

2. **[严重]** 将 DelegatingLlmChatService 的构造器改造纳入 task_v13 涉及文件清单。按照 plan.md 既定方案：构造器改为 `Map<ClientType, LlmChatService>`，移除 `@Service`/`@Primary` 注解（由 AiPlatformConfig @Bean 注册），新增 `ObjectProvider` 模式处理选择性 Bean。

3. **[一般]** 补充 endpointId 缺失下的 `structuredChat()` 实现路径说明：`LlmChatRequest` 新增 `endpointId` 后，`structuredChat()` 同样需要它来完成凭据获取和限流。

4. **[一般]** 补充测试规划：至少覆盖 `HttpApiLlmChatService` 的 chat() 凭证获取失败/限流拒绝/HTTP 异常三路径，以及 SpringAi 两个存根的 UnsupportedOperationException 验证。
