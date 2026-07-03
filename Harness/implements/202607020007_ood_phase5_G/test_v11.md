# 测试报告（v11）

## 概述

基于详细设计 v11 的行为契约编写单元测试。覆盖 3 个新增/修改类型：`LlmChatStreamService` 接口、`DelegatingLlmChatService` 分发实现、`AiAbilityInputInvalidException` 异常类。共 3 个测试文件，13 个测试用例。

## 测试文件清单

| 文件路径 | 操作 | 用例数 | 覆盖维度 |
|---------|------|--------|---------|
| `client/LlmChatStreamServiceTest.java` | 无变更 | 2 | 接口形态 + 方法签名 |
| `client/DelegatingLlmChatServiceTest.java` | 增强 | 8 | 构造、initDelegates、分发、回退、防御性封装 |
| `client/exception/AiAbilityInputInvalidExceptionTest.java` | 新增 | 3 | 构造器 + 异常层次 |

## LlmChatStreamServiceTest（2 用例）

| 用例 | 覆盖契约 |
|------|---------|
| `shouldDeclareChatStreamMethod` | chatStream 方法签名：返回 Flux，参数为 LlmChatRequest |
| `shouldBePublicInterface` | 类型形态为 interface |

## DelegatingLlmChatServiceTest（8 用例）

| 用例 | 覆盖契约 |
|------|---------|
| `shouldConstructWithServiceList` | 构造器接收 List 参数 |
| `shouldReturnNullClientType` | getClientType() 返回 null |
| `shouldFallbackToHttpApiWhenClientTypeIsNull` | clientType 为 null 时回退到 HTTP_API |
| `shouldFallbackForUnmappedClientType` | clientType 为有效枚举值但无对应实现时回退到 HTTP_API |
| `shouldDispatchToCorrectClientType` | chat() 按 ClientType 正确分发到目标实现 |
| `shouldDelegateStructuredChat` | structuredChat() 按 ClientType 正确分发到目标实现 |
| `shouldProduceUnmodifiableDelegates` | initDelegates() 后 delegates 为不可变 Map（反射验证） |
| `shouldNotFailOnEmptyServiceList` | initDelegates 不受空服务列表影响 |

## AiAbilityInputInvalidExceptionTest（3 用例）

| 用例 | 覆盖契约 |
|------|---------|
| `shouldConstructWithMessage` | 单参数构造器：message 正确，cause 为 null |
| `shouldConstructWithMessageAndCause` | 双参数构造器：message 和 cause 均正确 |
| `shouldBeRuntimeException` | 继承 RuntimeException |

## 设计偏差

无。所有测试基于行为契约编写，未修改编码 agent 的源码文件。
