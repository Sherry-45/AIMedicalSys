# 任务指令（v3）

## 动作
NEW

## 任务描述
修复 v2 验证后残留的 9 个测试编译错误，分布在 4 个测试文件和 1 个测试辅助类：

| # | 文件 | 行号 | 错误类型 | 根因 |
|---|------|------|---------|------|
| 1 | `thinadapter/DiagnosisCapabilityExecutorTest.java` | 32 | cannot find symbol | 引用不存在的 `com.aimedical.modules.diagnosis.DiagnosisRequest` |
| 2-5 | `orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | 273,336,375,415 | constructor mismatch | 4 处调用首参传 `DiscussionConclusionRequest.class`（旧 inputType），当前构造器已无此参数 |
| 6 | `orchestrator/AbstractCapabilityExecutorTest.java` | 1519 | constructor mismatch | `TestableExecutor` 将 `inputType` 传入 `super()`，父类构造器已移除该参数 |
| 7 | `orchestrator/AbstractCapabilityExecutorTest.java` | 1542 | cannot find symbol | `getInputType()` 引用字段 `inputType`，该字段已不存在 |
| 8-9 | `orchestrator/impl/TriageCapabilityExecutorTest.java` | 85,104 | constructor mismatch | 2 处调用首参传 `TriageRequest.class`（旧 inputType），当前构造器已无此参数 |

## 选择理由
R1 主源码编译修复完成，R2 薄适配器测试适配完成（删除旧测试文件、保留 thinadapter 子包测试副本）。当前 9 个错误为仅剩的编译阻塞，修复后 `mvn test` 可全部通过编译阶段，为后续核心执行器业务逻辑修复（T3-T7）扫清障碍。

## 任务上下文

### 根因：R1 (T22) 移除了 AbstractCapabilityExecutor 构造器的 `Class<T> inputType` 参数
R1 将 `Class<T> inputType` 从 `AbstractCapabilityExecutor` 及其所有子类的构造器中移除，改由 `getInputType()` 虚方法返回。但以下测试文件未同步更新：

### 修复 1：`AbstractCapabilityExecutorTest.java` — `TestableExecutor` 内部类

**问题**：`TestableExecutor` 构造器（line 1500-1518）将首参 `Class<T> inputType` 传给 `super(inputType, ...)`；`getInputType()`（line 1541-1543）引用 `inputType` 字段但字段未声明。

**修正**：
- 在 `TestableExecutor` 类中声明 `private final Class<T> inputType;` 字段
- 保留构造器签名不变（保持所有调用代码兼容）
- 修改 `super()` 调用：去掉 `inputType` 首参，改为 `super(promptTemplateManager, ...)`
- 构造器体内增加 `this.inputType = inputType;`

```java
static class TestableExecutor<T, R> extends AbstractCapabilityExecutor<T, R> {
    private final Class<T> inputType;

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
    // ... rest unchanged
}
```

所有 `new TestableExecutor<>(Object.class, ...)` 调用代码**无需修改**。

### 修复 2：`TriageCapabilityExecutorTest.java` — 2 处构造参数移除

**问题**：构造器调用首参传 `TriageRequest.class`，当前构造器首参为 `PromptTemplateManager`。

**修正**：各替换 1 处 `TriageRequest.class` → `null`，参数数量从 17 降为 16：

- line 85-93: `new TriageCapabilityExecutor(TriageRequest.class, mockTemplate, ...)` → `new TriageCapabilityExecutor(null, mockTemplate, ...)`
- line 104-111: `new TriageCapabilityExecutor(TriageRequest.class, null, ...)` → `new TriageCapabilityExecutor(null, null, ...)`

### 修复 3：`DiscussionConclusionCapabilityExecutorTest.java` — 4 处构造参数移除

**问题**：同 Triage，4 处构造器调用首参传 `DiscussionConclusionRequest.class`。

**修正**：各替换 1 处 `DiscussionConclusionRequest.class` → `null`：

- line 273-282: `createMinimalExecutor`（line 375-383）— 替换首参
- line 336-345: `createExecutorWithFullPipelineMocks`（line 415-424）— 替换首参
- line 273: 内联构造（line 273-282）— 替换首参
- line 336: 内联构造（line 336-345）— 替换首参

### 修复 4：`thinadapter/DiagnosisCapabilityExecutorTest.java` — 跨包匿名类修复

**问题**：`line 32` 中 `new com.aimedical.modules.diagnosis.DiagnosisRequest() {}` 引用的类不存在。

**根因**：测试 `shouldDegradeWhenDtoFromPhase4Package` 需要创建一个运行时类在 `com.aimedical.modules.diagnosis` 包中的 `DiagnosisRequest` 实例，以触发 `isDtoEmpty()` 的 phase4 包检测逻辑。

**修正方案**：（选择方案A，最小侵入）

**方案A（推荐）**：新建测试辅助类 `Phase4DiagnosisRequest`
在 `com.aimedical.modules.diagnosis` 包（已有 `TestPhase4Exception.java`）中创建：

**新建文件**：`ai-impl/src/test/java/com/aimedical/modules/diagnosis/Phase4DiagnosisRequest.java`
```java
package com.aimedical.modules.diagnosis;

import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest;

public class Phase4DiagnosisRequest extends DiagnosisRequest {
}
```

在 `DiagnosisCapabilityExecutorTest.java:32` 修改为：
```java
DiagnosisRequest phase4Request = new com.aimedical.modules.diagnosis.Phase4DiagnosisRequest();
```

## 已有代码上下文

### 当前 AbstractCapabilityExecutor 构造器（R1 修改后 16 参数）
```java
protected AbstractCapabilityExecutor(
    PromptTemplateManager promptTemplateManager,   // 0
    ModelRouter modelRouter,                       // 1
    LlmChatService llmChatService,                 // 2
    StructuredOutputParser structuredOutputParser, // 3
    AiMetricsCollector metricsCollector,           // 4
    SlidingWindowMetricsStore metricsStore,        // 5
    ModelEndpointHealthManager endpointHealthManager,   // 6
    AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef, // 7
    LocalRuleFallback<T, R> localRuleFallback,     // 8
    AtomicReference<Map<String, Duration>> capabilityTimeoutConfig,  // 9
    AtomicReference<Map<String, Duration>> parseTimeoutConfig,       // 10
    AtomicReference<Duration> parseTimeoutDefault, // 11
    Duration thinAdapterTimeout,                   // 12
    AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig, // 13
    Executor llmCallExecutor,                      // 14
    ObjectMapper objectMapper                      // 15
)
```

### 已有测试辅助类
- `com.aimedical.modules.diagnosis.TestPhase4Exception` — 在 `ai-impl/src/test/` 下，扩展 `RuntimeException`

## 依赖关系
- 依赖：R1 主源码修改后的 `AbstractCapabilityExecutor` 及其子类构造器签名
- 依赖：R2 删除 6 个旧测试文件后残留的测试文件
- 修改不涉及 main 源码，全部为 test 目录下的修复

## 涉及文件清单

| 操作 | 文件路径 |
|------|---------|
| 修改 | `ai-impl/src/test/java/.../orchestrator/AbstractCapabilityExecutorTest.java`（TestableExecutor 内部类） |
| 修改 | `ai-impl/src/test/java/.../orchestrator/impl/TriageCapabilityExecutorTest.java`（2 处构造调用） |
| 修改 | `ai-impl/src/test/java/.../orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java`（4 处构造调用） |
| 修改 | `ai-impl/src/test/java/.../thinadapter/DiagnosisCapabilityExecutorTest.java`（line 32 类型引用） |
| 新建 | `ai-impl/src/test/java/.../diagnosis/Phase4DiagnosisRequest.java`（测试辅助类） |

---

## 修订说明（v3 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] 路线表与实际进度不同步：R2之后缺少「修复残留9个测试编译错误」行 | 路线表 R2 后插入新 R3 行（修复残留9个测试编译错误），原 R3-R12 顺延为 R4-R13；轮次详情同步插入 R3 小节并顺延编号；R1/R2 已标记 ✅ |
