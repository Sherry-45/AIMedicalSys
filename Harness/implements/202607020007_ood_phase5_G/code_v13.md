# 实现报告（v13）

## 概述

实现 4 个 LLM 调用实现类 + 1 个临时装配配置类；修改 LlmChatRequest 新增 endpointId 字段；重构 DelegatingLlmChatService 为 Map 构造器注入；新增 AiResult.failure(String, String) 工厂方法；新增/修改对应测试；同步修复其他源文件中 4 参数构造器调用。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `ai-api/src/main/java/.../api/AiResult.java` | 新增 `failure(String errorCode, String message)` 工厂方法 |
| 修改 | `ai-impl/src/main/java/.../client/LlmChatRequest.java` | 新增 `endpointId` 字段 + 构造器参数 + getter |
| 修改 | `ai-impl/src/main/java/.../client/DelegatingLlmChatService.java` | 重构为 `Map<ClientType, LlmChatService>` 构造器；移除 `@Service`/`@Primary`/`@PostConstruct`/`initDelegates()` |
| 新建 | `ai-impl/src/main/java/.../client/HttpApiLlmChatService.java` | `LlmChatService` HTTP API 直连同步实现 |
| 新建 | `ai-impl/src/main/java/.../client/HttpApiLlmChatStreamService.java` | `LlmChatStreamService` HTTP API 流式占位（Flux.empty()） |
| 新建 | `ai-impl/src/main/java/.../client/SpringAiLlmChatService.java` | `LlmChatService` Spring AI 存根 |
| 新建 | `ai-impl/src/main/java/.../client/SpringAiLlmChatStreamService.java` | `LlmChatStreamService` Spring AI 流式存根 |
| 新建 | `ai-impl/src/main/java/.../client/AiClientConfig.java` | 临时 `@Configuration`，注册全部实现 |
| 修改 | `ai-impl/src/main/java/.../orchestrator/AbstractCapabilityExecutor.java` | LlmChatRequest 4 参数 → 5 参数 |
| 修改 | `ai-impl/src/main/java/.../orchestrator/impl/DiscussionConclusionCapabilityExecutor.java` | LlmChatRequest 4 参数 → 5 参数 |
| 修改 | `ai-impl/src/test/java/.../client/LlmChatRequestTest.java` | 更新构造器调用；新增 endpointId 断言 |
| 修改 | `ai-impl/src/test/java/.../client/DelegatingLlmChatServiceTest.java` | 适配 Map 构造器；移除 initDelegates/self-skip/missing-type 测试；新增 fallback-null 测试 |
| 新建 | `ai-impl/src/test/java/.../client/HttpApiLlmChatServiceTest.java` | 7 测试：getClientType/null endpointId/credential missing/rate limited/HTTP error/success/structuredChat not supported |
| 新建 | `ai-impl/src/test/java/.../client/HttpApiLlmChatStreamServiceTest.java` | 1 测试：StepVerifier 验证 Flux.empty() |
| 新建 | `ai-impl/src/test/java/.../client/SpringAiLlmChatServiceTest.java` | 3 测试：getClientType/chat throws/structuredChat throws |
| 新建 | `ai-impl/src/test/java/.../client/SpringAiLlmChatStreamServiceTest.java` | 1 测试：chatStream throws UnsupportedOperationException |
| 修改 | `ai-impl/pom.xml` | 新增 reactor-test test 依赖 |
| 修改 | `ai-impl/src/test/java/.../pom/AiImplPomCleanDependencyTest.java` | 依赖计数 8→9 |

## 编译验证

`mvn test -pl modules/ai/ai-impl -am` — **BUILD SUCCESS**, 334 tests, 0 failures, 0 errors.

## 设计偏差说明

无偏差。设计规格与实际实现一致。
