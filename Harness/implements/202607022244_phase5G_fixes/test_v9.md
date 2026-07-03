# 测试报告（v9）

## 概述

Phase5G 修复对应的单元测试更新：覆盖 10 项问题（T6/T7/T36/T37/T38/T11/T12/T50/T39/T40），涉及 5 个测试文件变更。

## 测试文件变更清单

| 文件 | 变更 |
|------|------|
| `DelegatingLlmChatServiceTest.java` | T36：`shouldReturnNullClientType` → `shouldThrowOnGetClientType`，`assertNull` → `assertThrows(UnsupportedOperationException.class, ...)`；T6：新增 `shouldLogWarnForMissingClientTypesOnPostConstruct` / `shouldNotLogWarnWhenAllClientTypesMappedOnPostConstruct`；T7：新增 `shouldLogWarnWithEndpointIdAndHealthCheckOnChatFallback` / `shouldLogWarnWithEndpointIdAndHealthCheckOnStructuredChatFallback`；新增 logback + slf4j 导入 |
| `HttpApiLlmChatServiceTest.java` | T38：`shouldReturnRateLimitedResult` → `shouldThrowWhenRateLimited`，改为 `assertThrows(LlmInfrastructureException.class, ...)`；T50：`shouldThrowStructuredOutputNotSupportedException` → `shouldReturnFailureForStructuredChat`，改为断言 `AiResult.failure`；T12：新增 `shouldUseEndpointUrlWhenProvided` 用例；移除 `StructuredOutputNotSupportedException` 导入 |
| `SpringAiLlmChatServiceTest.java` | T39：`assertThrows(UnsupportedOperationException.class, ...)` → `assertThrows(LlmInfrastructureException.class, ...)`；新增 `LlmInfrastructureException` 导入 |
| `SpringAiLlmChatStreamServiceTest.java` | T39：`assertThrows(UnsupportedOperationException.class, ...)` → `assertThrows(LlmInfrastructureException.class, ...)`；新增 `LlmInfrastructureException` 导入 |
| `LlmChatRequestTest.java` | T12：`shouldDefaultToNullViaNoArgConstructor` 新增 `assertNull(req.getEndpointUrl())`；新增 `shouldSetEndpointUrlViaSixParamConstructor` |

## 未变更文件说明

| 文件 | 原因 |
|------|------|
| `EndpointRateLimiterTest.java` | T37 仅移除未使用字段，不影响测试行为 |
| `LlmChatStreamServiceTest.java` | T40 仅移除未使用导入，接口签名不变 |

## 测试覆盖矩阵

| 行为契约 | 测试用例 | 文件 |
|---------|---------|------|
| @PostConstruct 对缺失 ClientType 记录 WARN 日志 | `shouldLogWarnForMissingClientTypesOnPostConstruct` | DelegatingLlmChatServiceTest |
| @PostConstruct 全映射时不产生日志 | `shouldNotLogWarnWhenAllClientTypesMappedOnPostConstruct` | DelegatingLlmChatServiceTest |
| chat 回退日志级别 WARN、含 endpointId 和健康检查提示 | `shouldLogWarnWithEndpointIdAndHealthCheckOnChatFallback` | DelegatingLlmChatServiceTest |
| structuredChat 回退日志级别 WARN、含 endpointId 和健康检查提示 | `shouldLogWarnWithEndpointIdAndHealthCheckOnStructuredChatFallback` | DelegatingLlmChatServiceTest |
| DelegatingLlmChatService.getClientType() 抛 UnsupportedOperationException | `shouldThrowOnGetClientType` | DelegatingLlmChatServiceTest |
| 限流拒绝时抛 LlmInfrastructureException | `shouldThrowWhenRateLimited` | HttpApiLlmChatServiceTest |
| structuredChat 返回 AiResult.failure | `shouldReturnFailureForStructuredChat` | HttpApiLlmChatServiceTest |
| URI 优先使用 endpointUrl | `shouldUseEndpointUrlWhenProvided` | HttpApiLlmChatServiceTest |
| SpringAiLlmChatService 抛 LlmInfrastructureException | `shouldThrowOnChat` / `shouldThrowOnStructuredChat` | SpringAiLlmChatServiceTest |
| SpringAiLlmChatStreamService 抛 LlmInfrastructureException | `shouldThrowOnChatStream` | SpringAiLlmChatStreamServiceTest |
| 6参构造器正确设置 endpointUrl 和 getter | `shouldSetEndpointUrlViaSixParamConstructor` | LlmChatRequestTest |
| 无参构造器中 endpointUrl 为 null | `shouldDefaultToNullViaNoArgConstructor`（新增断言） | LlmChatRequestTest |
