# 计划审查报告（v1 r3）

## 审查结果
REJECTED

## 发现

- **[严重]** — Batch 3（P1）遗漏 SpringAiLlmChatService 和 SpringAiLlmChatStreamService 两个必需实现。

  task 10 仅列出 `HttpApiLlmChatService + HttpApiLlmChatStreamService`，但设计文档 §1.3、§2.1 目录结构和 §2.3 类图明确将 `SpringAiLlmChatService` 和 `SpringAiLlmChatStreamService` 列为 `LlmChatService`/`LlmChatStreamService` 接口的并行实现。`DelegatingLlmChatService` 的 `Map<ClientType, LlmChatService>` 分发机制要求 `ClientType.SPRING_AI` 有对应实现（设计文档 §3.2 DelegatingLlmChatService 伪代码显式包含 `delegates.put(ClientType.SPRING_AI, springAi)`），且启动期 `@PostConstruct` 会检查所有 `ClientType` 枚举值均有对应实现。缺少 Spring AI 实现将导致：
  - `ClientType.SPRING_AI` 路由分发不可用
  - `DelegatingLlmChatService` 启动期自检失败（ERROR 日志）
  - 不符合设计文档要求的"全部代码实现"

  修正方向：在 Batch 3 中新增独立任务（或扩展 task 10），明确包含 `SpringAiLlmChatService` 和 `SpringAiLlmChatStreamService`，与 `HttpApiLlmChatService`/`HttpApiLlmChatStreamService` 并列实现。同时确认该类是否需要 Spring AI 相关依赖（pom.xml 目前未发现 spring-ai 依赖项，需补充）。
