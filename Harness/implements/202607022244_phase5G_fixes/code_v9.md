# 实现报告（v9）

## 概述

实现 Phase5G 修复：LLM 客户端基础设施 10 项问题（T6/T7/T36/T37/T38/T11/T12/T50/T39/T40），涉及 7 个源文件。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `LlmChatRequest.java` | T12：新增 endpointUrl 字段、6 参构造器、无参构造器初始化 null、getter |
| 修改 | `DelegatingLlmChatService.java` | T6：新增 @PostConstruct 枚举校验方法；T7：回退日志改进（WARN 级别、包含 endpointId、健康检查提示）；T36：getClientType() 改为抛 UnsupportedOperationException |
| 修改 | `EndpointRateLimiter.java` | T37：移除未使用的 maxBurstSeconds 字段 |
| 修改 | `HttpApiLlmChatService.java` | T11：HttpClient 池化（类字段 + 双构造器）；T12：URI 优先使用 endpointUrl；T38：限流拒绝改为抛 LlmInfrastructureException；T50：structuredChat 返回 AiResult.failure 而非抛异常；移除 StructuredOutputNotSupportedException 导入 |
| 修改 | `SpringAiLlmChatService.java` | T39：UnsupportedOperationException → LlmInfrastructureException |
| 修改 | `SpringAiLlmChatStreamService.java` | T39：UnsupportedOperationException → LlmInfrastructureException |
| 修改 | `LlmChatStreamService.java` | T40：移除未使用导入 AiAbilityInputInvalidException |

## 编译验证

通过（mvn compile 无错误）

## 设计偏差说明

无偏差
