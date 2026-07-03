# 实现报告（v11）

## 概述

实现了 `LlmChatStreamService` 接口、`DelegatingLlmChatService` 分发实现（`@Primary`）、`AiAbilityInputInvalidException` 异常类；修改了 `LlmChatService` 接口（新增 `getClientType()` 方法签名）和 `pom.xml`（添加 reactor-core 依赖）；新增 2 个测试文件覆盖接口契约和分发行为。同步修复了因接口变更导致编译失败的 17 处现有测试匿名类。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | ai-impl/src/main/java/.../client/LlmChatStreamService.java | 流式对话客户端接口 |
| 新建 | ai-impl/src/main/java/.../client/DelegatingLlmChatService.java | `@Primary` 分发实现 |
| 新建 | ai-impl/src/main/java/.../client/exception/AiAbilityInputInvalidException.java | AI 能力输入校验异常 |
| 修改 | ai-impl/src/main/java/.../client/LlmChatService.java | 新增 `ClientType getClientType()` 方法签名 |
| 修改 | ai-impl/pom.xml | 添加 `io.projectreactor:reactor-core` 依赖 |
| 新建 | ai-impl/src/test/java/.../client/LlmChatStreamServiceTest.java | 反射契约测试 |
| 新建 | ai-impl/src/test/java/.../client/DelegatingLlmChatServiceTest.java | 分发行为测试 |
| 修改 | ai-impl/src/test/java/.../orchestrator/AbstractCapabilityExecutorTest.java | 13 处匿名类添加 `getClientType()` 实现及 import |
| 修改 | ai-impl/src/test/java/.../orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java | 3 处匿名类添加 `getClientType()` 实现 |
| 修改 | ai-impl/src/test/java/.../orchestrator/impl/TriageCapabilityExecutorTest.java | 1 处匿名类添加 `getClientType()` 实现及 import |

## 编译验证

Maven 编译通过，99 个 client 包测试、92 个 orchestrator+exception 测试全部通过（0 failure, 0 error）。

## 设计偏差说明

无偏差。所有类型形态、方法签名、错误处理方式、行为契约与详细设计 v11 一致。
