# 详细设计（v3）

## 概述

修复 v2 验证后残留的 9 个测试编译错误，分布在 4 个测试文件和 1 个测试辅助类。全部为 test 目录下的修复，不涉及 main 源码。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/src/test/java/.../orchestrator/AbstractCapabilityExecutorTest.java` | 修改 | TestableExecutor 内部类：声明 inputType 字段，super() 去掉首参 |
| `ai-impl/src/test/java/.../orchestrator/impl/TriageCapabilityExecutorTest.java` | 修改 | 2 处构造调用去掉 TriageRequest.class 首参 |
| `ai-impl/src/test/java/.../orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | 修改 | 4 处构造调用去掉 DiscussionConclusionRequest.class 首参 |
| `ai-impl/src/test/java/.../thinadapter/DiagnosisCapabilityExecutorTest.java` | 修改 | line 32 匿名类改为引用 Phase4DiagnosisRequest |
| `ai-impl/src/test/java/.../diagnosis/Phase4DiagnosisRequest.java` | 新建 | 测试辅助类，继承 DiagnosisRequest |

## 类型定义

### Phase4DiagnosisRequest

**形态**：class
**包路径**：`com.aimedical.modules.diagnosis`
**职责**：为 `shouldDegradeWhenDtoFromPhase4Package` 测试提供属于 `com.aimedical.modules.diagnosis` 包的 `DiagnosisRequest` 子类实例，以触发 `isDtoEmpty()` 的包路径检测逻辑
**签名**：

```java
package com.aimedical.modules.diagnosis;

import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest;

public class Phase4DiagnosisRequest extends DiagnosisRequest {
}
```

**构造方式**：`new Phase4DiagnosisRequest()` 无参构造（继承自父类）
**类型关系**：继承 `com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest`

## 修改详述

### 修改 1：AbstractCapabilityExecutorTest.java — TestableExecutor 内部类

**文件路径**：`ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutorTest.java`

**变更 a：声明 inputType 字段**（在 `TestableExecutor` 类中，`getInputType()` 方法上方）

```java
private final Class<T> inputType;
```

**变更 b：修改构造器**（line 1500-1525），保持签名不变（保留 `Class<T> inputType` 作为首参），修改 `super()` 去掉首参，添加字段赋值：

```java
TestableExecutor(
    Class<T> inputType,
    PromptTemplateManager promptTemplateManager,
    ModelRouter modelRouter,
    LlmChatService llmChatService,
    StructuredOutputParser structuredOutputParser,
    AiMetricsCollector metricsCollector,
    SlidingWindowMetricsStore metricsStore,
    ModelEndpointHealthManager endpointHealthManager,
    AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef,
    LocalRuleFallback<T, R> localRuleFallback,
    AtomicReference<Map<String, Duration>> capabilityTimeoutConfig,
    AtomicReference<Map<String, Duration>> parseTimeoutConfig,
    AtomicReference<Duration> parseTimeoutDefault,
    Duration thinAdapterTimeout,
    AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig,
    java.util.concurrent.Executor llmCallExecutor,
    ObjectMapper objectMapper
) {
    super(promptTemplateManager, modelRouter, llmChatService,
          structuredOutputParser, metricsCollector, metricsStore,
          endpointHealthManager, degradationStrategyMapRef, localRuleFallback,
          capabilityTimeoutConfig, parseTimeoutConfig, parseTimeoutDefault,
          thinAdapterTimeout, thinAdapterPerCapabilityConfig,
          llmCallExecutor, objectMapper);
    this.inputType = inputType;
}
```

**不变**：`getInputType()` 方法 body（line 1541-1543）不变，因其引用的 `inputType` 字段已声明。

**引用影响**：36 处 `new TestableExecutor<>(Object.class, ...)` 调用无需修改。

### 修改 2：TriageCapabilityExecutorTest.java — 2 处构造参数移除

**文件路径**：`ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/TriageCapabilityExecutorTest.java`

**变更 a：line 85-93** — 首参 `TriageRequest.class` → `null`，参数数量 17→16：

```java
TriageCapabilityExecutor executor = new TriageCapabilityExecutor(
    null, mockTemplate, mockRouter, mockChat, null,
    null, metricsStore, mockHealth,
    new AtomicReference<>(Map.<String, List<DegradationStrategy>>of()), null,
    new AtomicReference<>(Map.of("TRIAGE", Duration.ofSeconds(30))),
    new AtomicReference<>(Map.of("TRIAGE", Duration.ofSeconds(5))),
    new AtomicReference<>(Duration.ofSeconds(3)), null, null,
    Executors.newSingleThreadExecutor(), objectMapper
);
```

**变更 b：line 104-111（createMinimalExecutor）** — 首参 `TriageRequest.class` → `null`：

```java
return new TriageCapabilityExecutor(
    null, null, null, null, null,
    null, null, null,
    new AtomicReference<>(Map.<String, List<DegradationStrategy>>of()), null,
    new AtomicReference<>(Map.of("TRIAGE", Duration.ofSeconds(30))),
    null, null, null, null,
    Executors.newSingleThreadExecutor(), objectMapper
);
```

### 修改 3：DiscussionConclusionCapabilityExecutorTest.java — 4 处构造参数移除

**文件路径**：`ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java`

**变更 a：line 273-282** — 首参 `DiscussionConclusionRequest.class` → `null`：

```java
DiscussionConclusionCapabilityExecutor executor = new DiscussionConclusionCapabilityExecutor(
    null, mockTemplate, mockRouter, mockChatWithFailure, null,
    null, metricsStore, mockHealth,
    new AtomicReference<>(Map.<String, List<DegradationStrategy>>of()), null,
    new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30))),
    new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(5))),
    new AtomicReference<>(Duration.ofSeconds(3)), null, null,
    Executors.newFixedThreadPool(2), objectMapper,
    "compress-default", ClientType.HTTP_API, Duration.ofSeconds(15)
);
```

**变更 b：line 336-345** — 首参 `DiscussionConclusionRequest.class` → `null`：

```java
DiscussionConclusionCapabilityExecutor executor = new DiscussionConclusionCapabilityExecutor(
    null, mockTemplate, mockRouter, mockChat, mockParser,
    null, metricsStore, mockHealth,
    new AtomicReference<>(Map.<String, List<DegradationStrategy>>of()), null,
    new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30))),
    new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(5))),
    new AtomicReference<>(Duration.ofSeconds(3)), null, null,
    Executors.newFixedThreadPool(2), objectMapper,
    "compress-default", ClientType.HTTP_API, Duration.ofSeconds(15)
);
```

**变更 c：line 374-383（createMinimalExecutor）** — 首参 `DiscussionConclusionRequest.class` → `null`：

```java
return new DiscussionConclusionCapabilityExecutor(
    null, null, null, null, null,
    null, null, null,
    new AtomicReference<>(Map.<String, List<DegradationStrategy>>of()), null,
    new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30))),
    null, null, null, null,
    Executors.newFixedThreadPool(2), objectMapper,
    "compress-default", ClientType.HTTP_API, Duration.ofSeconds(15)
);
```

**变更 d：line 415-424（createExecutorWithFullPipelineMocks）** — 首参 `DiscussionConclusionRequest.class` → `null`：

```java
return new DiscussionConclusionCapabilityExecutor(
    null, mockTemplate, mockRouter, mockChat, null,
    null, metricsStore, mockHealth,
    new AtomicReference<>(Map.<String, List<DegradationStrategy>>of()), null,
    new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30))),
    new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(5))),
    new AtomicReference<>(Duration.ofSeconds(3)), null, null,
    Executors.newFixedThreadPool(2), objectMapper,
    "compress-default", ClientType.HTTP_API, Duration.ofSeconds(15)
);
```

### 修改 4：DiagnosisCapabilityExecutorTest.java line 32 — 跨包匿名类修复

**文件路径**：`ai-impl/src/test/java/com/aimedical/modules/ai/impl/thinadapter/DiagnosisCapabilityExecutorTest.java`

**变更 a：line 32** — 引用新建的 `Phase4DiagnosisRequest` 替代不存在的匿名类：

```java
DiagnosisRequest phase4Request = new com.aimedical.modules.diagnosis.Phase4DiagnosisRequest();
```

无需新增 import 语句（使用全限定名，风格与 line 32 原写法一致）。

## 错误处理

- 新建的 `Phase4DiagnosisRequest` 无额外错误处理逻辑，继承自 `DiagnosisRequest` 的无参构造
- 所有修改不涉及异常抛出或捕获逻辑变更

## 行为契约

1. `TestableExecutor` 构造器签名保持不变（17 参数，首参 `Class<T> inputType`），所有 36 处调用代码不受影响
2. `TriageCapabilityExecutorTest` 2 处构造参数数从 17 降为 16（去掉 `TriageRequest.class` 首参）
3. `DiscussionConclusionCapabilityExecutorTest` 4 处构造参数数从 20 降为 19（去掉 `DiscussionConclusionRequest.class` 首参）
4. `Phase4DiagnosisRequest` 位于 `com.aimedical.modules.diagnosis` 包下，通过类加载的 `Class.getName()` 返回 `"com.aimedical.modules.diagnosis.Phase4DiagnosisRequest"`，以 `startsWith("com.aimedical.modules.diagnosis")` 匹配触发降级路径

## 依赖关系

- `Phase4DiagnosisRequest` 依赖 `ai-api` 模块的 `DiagnosisRequest`（编译期依赖，test scope）
- 修改 1-3 依赖 R1 修改后 `AbstractCapabilityExecutor` 及其子类的构造器签名
- 修改不涉及 main 源码、pom.xml 或构建配置

## 修订说明（v3 R1）

| 审查意见 | 修改措施 |
|---------|---------|
| v2 验证 FAILED：9 个测试编译错误 | 新增 v3 设计：TestableExecutor 添加 inputType 字段 + 修正 super() 调用；4 个子类测试构造器调用去除 inputType 首参；新建 Phase4DiagnosisRequest 测试辅助类 |
