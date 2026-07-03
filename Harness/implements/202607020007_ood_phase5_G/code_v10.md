# 实现报告（v10）

## 概述

为 `ai-impl/client/` 包新增 4 个类型——`CredentialProvider` 接口（含内嵌 `Credential` 值对象 + `CredentialProviderState` 枚举）、`DefaultCredentialProvider` 默认内存实现、`EndpointRateLimiter` 端点限流器、`LlmInfrastructureException` 和 `CredentialUnavailableException` 基础设施异常——为 LLM 调用层提供凭据查询和限流基础设施。同步修改 `ai-impl/pom.xml` 添加 Caffeine 和 Guava 依赖。新增 5 个测试文件覆盖各类型。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/CredentialProvider.java` | 凭据查询接口，含内嵌 Credential 值对象和 package-private CredentialProviderState 枚举 |
| 新建 | `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/DefaultCredentialProvider.java` | CredentialProvider 接口的默认内存实现，基于 ConcurrentHashMap + Caffeine 缓存 + 完整状态机 |
| 新建 | `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/EndpointRateLimiter.java` | 端点限流器，基于 Guava RateLimiter 令牌桶算法，@Value + Environment 多维度配置 |
| 新建 | `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/exception/LlmInfrastructureException.java` | LLM 基础设施异常，extends RuntimeException |
| 新建 | `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/exception/CredentialUnavailableException.java` | 凭据不可用异常，extends RuntimeException |
| 修改 | `AIMedical/backend/modules/ai/ai-impl/pom.xml` | 添加 caffeine + guava:32.1.3-jre 依赖 |
| 新建 | `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/CredentialProviderTest.java` | CredentialProvider 接口契约测试 |
| 新建 | `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/DefaultCredentialProviderTest.java` | DefaultCredentialProvider 状态机、缓存、凭据管理测试 |
| 新建 | `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/EndpointRateLimiterTest.java` | EndpointRateLimiter tryAcquire 测试 |
| 新建 | `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/exception/LlmInfrastructureExceptionTest.java` | LlmInfrastructureException 构造测试 |
| 新建 | `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/exception/CredentialUnavailableExceptionTest.java` | CredentialUnavailableException 构造测试 |
| 修改 | `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/pom/AiImplPomCleanDependencyTest.java` | 更新依赖计数（5→7）及新增 caffeine/guava 断言 |

## 编译验证

编译和测试全部通过，共 297 测试用例，0 失败 0 错误。

```
[INFO] Tests run: 297, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## 设计偏差说明

无偏差，实现严格遵循 detail_v10.md 设计规格。
