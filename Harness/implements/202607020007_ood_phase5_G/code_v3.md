# 实现报告（v3）

## 概述

实现了 CapabilityExecutor 泛型接口与 AbstractCapabilityExecutor 抽象骨架类，同时创建 8 个编译期依赖的存根类型（其中 LocalRuleFallback 增加了 fallback 方法签名以保证可编译）。为 AbstractCapabilityExecutor 新增了 22 个单元测试。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | `ai-impl/.../template/PromptTemplateManager.java` | 存根接口 |
| 新建 | `ai-impl/.../router/ModelRouter.java` | 存根接口 |
| 新建 | `ai-impl/.../client/LlmChatService.java` | 存根接口 |
| 新建 | `ai-impl/.../parser/StructuredOutputParser.java` | 存根接口 |
| 新建 | `ai-impl/.../metrics/AiMetricsCollector.java` | 存根接口 |
| 新建 | `ai-impl/.../metrics/ModelEndpointHealthManager.java` | 存根类（无参构造器 + 空方法体） |
| 新建 | `ai-impl/.../fallback/LocalRuleFallback.java` | 存根泛型接口（增加了 `R fallback(T)` 方法签名） |
| 新建 | `ai-api/.../dto/base/AiRequestBase.java` | 存根抽象类（含 4 个 getter 方法） |
| 新建 | `ai-impl/.../orchestrator/CapabilityExecutor.java` | 能力执行泛型接口 |
| 新建 | `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` | 能力执行器抽象骨架类 |
| 新建 | `ai-impl/.../orchestrator/AbstractCapabilityExecutorTest.java` | 单元测试（22 个用例） |
| 修改 | `ai-impl/pom.xml` | 新增 `spring-security-core` 依赖 |
| 修改 | `ai-impl/.../pom/AiImplPomCleanDependencyTest.java` | 依赖计数从 4 更新为 5 |

## 编译验证

通过。`mvn compile -pl modules/ai/ai-impl -am` 成功。
全部测试通过：`mvn test -pl modules/ai/ai-impl -am` → 107 tests, 0 failures, 0 errors。

## 设计偏差说明

| 设计规格 | 偏差原因 | 实际处理 |
|---------|---------|---------|
| `LocalRuleFallback<T,R>` 为空接口 | 设计中的 doDegrade() 行为契约要求调用 `localRuleFallback.fallback(request)`，空接口导致编译错误 | 增加了 `R fallback(T request)` 方法签名。此为编译必要条件 |
| `AiMetricsCollector` 为空接口 | 设计中的 doDegrade() 行为契约要求 `metricsCollector != null` 时记录降级指标 | 因 AiMetricsCollector 和 AiCallRecord 均为未来类型，当前在 doDegrade 中放入 TODO 注释占位，待后续批次补充 |
| 设计使用 `cause.getClassName()`（不存在的方法） | `Throwable` 无 `getClassName()` 方法 | 改为 `cause.getClass().getName()` 实现包路径前缀匹配 |
| `ai-impl/pom.xml` 无 spring-security 依赖 | 设计中的 `SecurityContextHolder.getContext().getAuthentication()` 需要 `spring-security-core` | 在 pom.xml 中添加了 `spring-security-core` 依赖 |
