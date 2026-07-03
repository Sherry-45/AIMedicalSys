# 计划审查报告（v10 r2）

## 审查结果
REJECTED

## 发现

### [严重] Caffeine 依赖未声明，编译将失败
`CredentialProvider` 的凭据缓存实现（基于 Caffeine `Expiry` 接口动态TTL）需要 `com.github.ben-manes.caffeine:caffeine` 依赖，但 `ai-impl/pom.xml` 中未声明此依赖。虽然 consultation、prescription、medical-record 等模块引用了 Caffeine，但 ai-impl 自身没有声明，也未通过 `spring-boot-starter-cache` 等传递依赖获取。编译 `CredentialProvider.java` 引用 `Caffeine.newBuilder().expireAfter(...)` 时将报错：`package com.github.benmanes.caffeine.cache does not exist`。

**修正方向**：在 `ai-impl/pom.xml` `<dependencies>` 中补充 Caffeine 依赖声明（作用域 `compile`）。

### [严重] Guava 依赖未声明，编译将失败
`EndpointRateLimiter` 使用 Guava `RateLimiter`（`com.google.common.util.concurrent.RateLimiter`），但 Guava 未在项目的任何 `pom.xml` 中声明。Spring Boot 3.x 不传递依赖 Guava。编译时将报错：`package com.google.common.util.concurrent does not exist`。

**修正方向**：在 `ai-impl/pom.xml`（或父 pom 的 dependencyManagement）中补充 Guava 依赖声明（作用域 `compile`）。

### [一般] EndpointRateLimiter 配置来源（AiPlatformConfig）尚未就绪
task_v10.md 明确声明 EndpointRateLimiter 的限流配置「从 `AiPlatformConfig` 注入」，但 `AiPlatformConfig` 属于 Task 18（Batch 6 P3），在当前 Task 8 之后至少 3 个批次才实现。这意味着 EndpointRateLimiter 实现时没有配置注入目标，也无法编译通过（构造器或 `@Autowired` 字段会引用不存在的类型）。

**修正方向**：（方案A）将 `AiPlatformConfig` 提前至 Task 8 同期实现（至少定义 `@ConfigurationProperties` 限流配置类）；（方案B）修改 task_v10.md，明确 EndpointRateLimiter 在 Task 8 中通过 `@Value` 直接注入属性值，待 Task 18 再迁移到 `AiPlatformConfig` 统一装配。

### [一般] CredentialProvider 的 Vault 查询抽象缺失
task_v10.md 定义了 `CredentialProvider` 接口及缓存/状态机行为，但未说明凭据从何处获取。设计文档提到 Vault/配置中心查询，但当前代码库中没有 Vault 客户端抽象。若 CredentialProvider 仅定义为接口（实现留待后续），其状态机（NORMAL→CACHE_ONLY→BACKOFF）依赖实际的 Vault 查询结果来驱动状态转换——无真实查询逻辑则状态机无法验证，测试也无法覆盖状态转换。

**修正方向**：在 task_v10.md 中明确 CredentialProvider 是否需要附带一个简单的默认实现（如 `MapCredentialProvider` 或 `ConfigFileCredentialProvider`），或添加一个测试专用的 `FailingVaultCredentialProvider` 用于验证状态机行为。

## 修改要求（REJECTED）
以上 4 个问题须修正后方可推进。其中 **[严重]** 级问题（Caffeine/Guava 依赖缺失）直接影响编译，**[一般]** 级问题（AiPlatformConfig 依赖、Vault 抽象）影响实现完整性和测试覆盖率。
