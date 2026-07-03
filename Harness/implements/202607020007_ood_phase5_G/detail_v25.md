# 详细设计（v25）

## 概述

修复 Task 18（AiPlatformConfig + AiPlatformEnvironmentPostProcessor）验证失败导致的 14 个 test-compile 错误。根因：`AbstractCapabilityExecutor` 构造器 4 个参数类型从 `Map<String, Duration>` / `Duration` 改为 `AtomicReference<Map<String, Duration>>` / `AtomicReference<Duration>`（v24 r2 修订），但 10 个已有测试文件未同步更新参数类型，同时 `DefaultModelRouterTest` 中 `ModelRoute` 类型未同步为 `ModelRouteConfig`。**生产代码零变更**，全部修改仅限 10 个测试文件。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `orchestrator/AbstractCapabilityExecutorTest.java` | 修改 | TestableExecutor 匿名类构造器 5 个参数类型同步 |
| `orchestrator/impl/TriageCapabilityExecutorTest.java` | 修改 | 2 处构造器调用中 5 个参数包装为 AtomicReference |
| `orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | 修改 | 4 处构造器调用中 5 个参数包装为 AtomicReference |
| `orchestrator/impl/DiagnosisCapabilityExecutorTest.java` | 修改 | createExecutor() 中 2 个参数包装为 AtomicReference |
| `orchestrator/impl/AnalysisReportForInspectionCapabilityExecutorTest.java` | 修改 | createExecutor() 中 2 个参数包装为 AtomicReference |
| `orchestrator/impl/AnalysisReportForLabTestCapabilityExecutorTest.java` | 修改 | createExecutor() 中 2 个参数包装为 AtomicReference |
| `orchestrator/impl/ImageAnalysisCapabilityExecutorTest.java` | 修改 | createExecutor() 中 2 个参数包装为 AtomicReference |
| `orchestrator/impl/RecommendExaminationCapabilityExecutorTest.java` | 修改 | createExecutor() 中 2 个参数包装为 AtomicReference |
| `orchestrator/impl/RecommendExecutionOrderCapabilityExecutorTest.java` | 修改 | createExecutor() 中 2 个参数包装为 AtomicReference |
| `router/DefaultModelRouterTest.java` | 修改 | ModelRoute → ModelRouteConfig 类型同步 |

## 类型变更详表

### 变更模式

所有 AtomicReference 包装均遵循同一模式：

```
变更前:  Map.of(...)                     → 变更后: new AtomicReference<>(Map.of(...))
变更前:  Duration.ofSeconds(n)           → 变更后: new AtomicReference<>(Duration.ofSeconds(n))
变更前:  Map<String, List<DegradationStrategy>>  → 变更后: new AtomicReference<>(Map<String, List<DegradationStrategy>>)
变更前:  null (作为第4个Map参数)          → 变更后: null (不变, 类型从Map变为AtomicReference后null合法)
变更前:  null (作为Duration参数)          → 变更后: null (不变, 类型从Duration变为AtomicReference后null合法)
```

### AbstractCapabilityExecutorTest.java

**文件**：`ai-impl/src/test/java/.../orchestrator/AbstractCapabilityExecutorTest.java`

**位置**：TestableExecutor 构造器（line ~1449-1473）

**变更内容**：5 个参数类型声明 + super() 调用同步

| 参数 | 变更前类型 | 变更后类型 |
|------|-----------|-----------|
| 第 9 参 | `Map<String, List<DegradationStrategy>> degradationStrategyMapRef` | `AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef` |
| 第 11 参 | `Map<String, Duration> capabilityTimeoutConfig` | `AtomicReference<Map<String, Duration>> capabilityTimeoutConfig` |
| 第 12 参 | `Map<String, Duration> parseTimeoutConfig` | `AtomicReference<Map<String, Duration>> parseTimeoutConfig` |
| 第 13 参 | `Duration parseTimeoutDefault` | `AtomicReference<Duration> parseTimeoutDefault` |
| 第 15 参 | `Map<String, Duration> thinAdapterPerCapabilityConfig` | `AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig` |

**已有 import**：`java.util.concurrent.atomic.AtomicReference` 已存在（line 13），无需新增。

### TriageCapabilityExecutorTest.java

**文件**：`ai-impl/src/test/java/.../orchestrator/impl/TriageCapabilityExecutorTest.java`

**位置 1**：line 85-93（inline executor in `shouldExecuteSuccessfully`）

| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 88 | `Map.<String, List<DegradationStrategy>>of()` | `new AtomicReference<>(Map.<String, List<DegradationStrategy>>of())` |
| 89 | `Map.of("TRIAGE", Duration.ofSeconds(30))` | `new AtomicReference<>(Map.of("TRIAGE", Duration.ofSeconds(30)))` |
| 90 | `Map.of("TRIAGE", Duration.ofSeconds(5))` | `new AtomicReference<>(Map.of("TRIAGE", Duration.ofSeconds(5)))` |
| 91 | `Duration.ofSeconds(3), null, null` | `new AtomicReference<>(Duration.ofSeconds(3)), null, null` |

**位置 2**：line 103-112（`createMinimalExecutor`）

| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 107 | `Map.<String, List<DegradationStrategy>>of()` | `new AtomicReference<>(Map.<String, List<DegradationStrategy>>of())` |
| 108 | `Map.of("TRIAGE", Duration.ofSeconds(30))` | `new AtomicReference<>(Map.of("TRIAGE", Duration.ofSeconds(30)))` |
| 109 | `null, null, null, null` | `null, null, null, null`（不变，但第3个参数 `null` 现在匹配 `AtomicReference<Duration>` 而非 `Duration`） |

**已有 import**：`java.util.concurrent.atomic.AtomicReference` 已存在（line 8），无需新增。

### DiscussionConclusionCapabilityExecutorTest.java

**文件**：`ai-impl/src/test/java/.../orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java`

**位置 1**：line 273-282（inline executor in `shouldHandleLongTranscript`）

| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 276 | `Map.<String, List<DegradationStrategy>>of()` | `new AtomicReference<>(Map.<String, List<DegradationStrategy>>of())` |
| 277 | `Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30))` | `new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30)))` |
| 278 | `Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(5))` | `new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(5)))` |
| 279 | `Duration.ofSeconds(3), null, null` | `new AtomicReference<>(Duration.ofSeconds(3)), null, null` |

**位置 2**：line 336-345（inline executor in `shouldHandleShortTranscript`）

| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 339 | `Map.<String, List<DegradationStrategy>>of()` | `new AtomicReference<>(Map.<String, List<DegradationStrategy>>of())` |
| 340 | `Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30))` | `new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30)))` |
| 341 | `Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(5))` | `new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(5)))` |
| 342 | `Duration.ofSeconds(3), null, null` | `new AtomicReference<>(Duration.ofSeconds(3)), null, null` |

**位置 3**：line 374-383（`createMinimalExecutor`）

| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 378 | `Map.<String, List<DegradationStrategy>>of()` | `new AtomicReference<>(Map.<String, List<DegradationStrategy>>of())` |
| 379 | `Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30))` | `new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30)))` |
| 380 | `null, null, null, null` | `null, null, null, null`（不变） |

**位置 4**：line 415-424（`createExecutorWithFullPipelineMocks`）

| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 418 | `Map.<String, List<DegradationStrategy>>of()` | `new AtomicReference<>(Map.<String, List<DegradationStrategy>>of())` |
| 419 | `Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30))` | `new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30)))` |
| 420 | `Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(5))` | `new AtomicReference<>(Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(5)))` |
| 421 | `Duration.ofSeconds(3), null, null` | `new AtomicReference<>(Duration.ofSeconds(3)), null, null` |

**已有 import**：`java.util.concurrent.atomic.AtomicReference` 已存在（line 11），无需新增。

### 薄适配器 Executor 测试（6 个文件，相同模式）

**文件清单**：
- `DiagnosisCapabilityExecutorTest.java`
- `AnalysisReportForInspectionCapabilityExecutorTest.java`
- `AnalysisReportForLabTestCapabilityExecutorTest.java`
- `ImageAnalysisCapabilityExecutorTest.java`
- `RecommendExaminationCapabilityExecutorTest.java`
- `RecommendExecutionOrderCapabilityExecutorTest.java`

**模式**：每个文件的 `createExecutor()` 方法中构造器调用的第 4 个参数（capabilityTimeoutConfig）和第 6 个参数（parseTimeoutDefault）需要包装为 AtomicReference。

**注意**：`parseTimeoutConfig` 已经是 `null`（第 5 参），类型从 `Map<String, Duration>` 变为 `AtomicReference<Map<String, Duration>>` 后 `null` 仍然合法，无需修改。

以 `DiagnosisCapabilityExecutorTest.java` 的 `createExecutor`（line 94-100）为例：

```java
// 当前（变更前）：
new DiagnosisCapabilityExecutor(
    service, metricsCollector, metricsStore,
    Map.of("DIAGNOSIS", Duration.ofSeconds(30)),   // 第4参: capabilityTimeoutConfig
    null,                                            // 第5参: parseTimeoutConfig
    Duration.ofSeconds(5),                          // 第6参: parseTimeoutDefault
    thinAdapterTimeout,                              // 第7参: thinAdapterTimeout
    null,                                            // 第8参: thinAdapterPerCapabilityConfig
    objectMapper                                     // 第9参: objectMapper
);

// 变更后：
new DiagnosisCapabilityExecutor(
    service, metricsCollector, metricsStore,
    new AtomicReference<>(Map.of("DIAGNOSIS", Duration.ofSeconds(30))),
    null,
    new AtomicReference<>(Duration.ofSeconds(5)),
    thinAdapterTimeout,
    null,
    objectMapper
);
```

各文件对应的 capability name：

| 文件 | capability name |
|------|----------------|
| DiagnosisCapabilityExecutorTest.java | `DIAGNOSIS` |
| AnalysisReportForInspectionCapabilityExecutorTest.java | `ANALYSIS_REPORT_INSPECTION` |
| AnalysisReportForLabTestCapabilityExecutorTest.java | `ANALYSIS_REPORT_LABTEST` |
| ImageAnalysisCapabilityExecutorTest.java | `IMAGE_ANALYSIS` |
| RecommendExaminationCapabilityExecutorTest.java | `RECOMMEND_EXAM` |
| RecommendExecutionOrderCapabilityExecutorTest.java | `RECOMMEND_EXEC_ORDER` |

**import 变更**：每个文件需要新增 `import java.util.concurrent.atomic.AtomicReference;`。当前这些文件没有该 import。

### DefaultModelRouterTest.java

**文件**：`ai-impl/src/test/java/.../router/DefaultModelRouterTest.java`

**变更 1**：字段类型和构造方式（line 13-16）

```java
// 变更前：
private ModelRoute routeA = new ModelRoute("ep-a", null, null, "model-a", null, 80, 30000L, null);
private ModelRoute routeB = new ModelRoute("ep-b", null, null, "model-b", null, 20, 30000L, null);
private ModelRoute routeZeroA = new ModelRoute("ep-za", null, null, "model-za", null, 0, 30000L, null);
private ModelRoute routeZeroB = new ModelRoute("ep-zb", null, null, "model-zb", null, 0, 30000L, null);

// 变更后：
private ModelRouteConfig routeA = new ModelRouteConfig();
private ModelRouteConfig routeB = new ModelRouteConfig();
private ModelRouteConfig routeZeroA = new ModelRouteConfig();
private ModelRouteConfig routeZeroB = new ModelRouteConfig();
```

对应 setter 初始化（需在 `@BeforeEach` 或声明处同步初始化，建议在字段声明处用实例初始化器）：

```java
{
    routeA.setEndpointId("ep-a"); routeA.setModelId("model-a"); routeA.setWeight(80); routeA.setTimeoutMs(30000L);
    routeB.setEndpointId("ep-b"); routeB.setModelId("model-b"); routeB.setWeight(20); routeB.setTimeoutMs(30000L);
    routeZeroA.setEndpointId("ep-za"); routeZeroA.setModelId("model-za"); routeZeroA.setWeight(0); routeZeroA.setTimeoutMs(30000L);
    routeZeroB.setEndpointId("ep-zb"); routeZeroB.setModelId("model-zb"); routeZeroB.setWeight(0); routeZeroB.setTimeoutMs(30000L);
}
```

**变更 2**：`createRouter` 方法签名（line 18）

```java
// 变更前：
private DefaultModelRouter createRouter(Map<String, List<ModelRoute>> routes) {

// 变更后：
private DefaultModelRouter createRouter(Map<String, List<ModelRouteConfig>> routes) {
```

`props.setRoutes(routes)` 调用不变——因为 `setRoutes` 参数类型已同步为 `Map<String, List<ModelRouteConfig>>`。

**变更 3**：所有 `createRouter(Map.of(...))` 调用处的参数不变——因为 `routeA`/`routeB`/`routeZeroA`/`routeZeroB` 字段类型已变为 `ModelRouteConfig`，`Map.of("KNOWN", List.of(routeA))` 的类型自动推导为 `Map<String, List<ModelRouteConfig>>`。

**变更 4**：更新 import，添加 `ModelRouteConfig` 的 import：
```java
import com.aimedical.modules.ai.impl.config.ModelRouteConfig;
```
同时移除或保留 `ModelRoute` import（仍可能在其它测试中使用——检查后发现 `ModelRoute` 仅被用于字段声明和 `verifyRoute` 断言中的 `result.getModelId()` 等，可保留，但 `ModelRoute` 作为字段类型不再被使用，可能会被 IDE 标记为 unused import。建议将 `import ...ModelRoute` 替换为 `import ...config.ModelRouteConfig`）。

**注意**：测试方法内对 `ModelRoute` 的引用（如 `route("CAP", null)` 返回类型仍是 `ModelRoute`）不受影响——`DefaultModelRouter.route()` 返回 `ModelRoute` 不变，断言 `r.getModelId()` 等调用不变。

## 错误处理

所有修改均为编译期类型修正，无运行期逻辑变更。`null` 作为 `AtomicReference` 参数类型合法。`ModelRouteConfig` 均为有参构造后通过 setter 填充，无 NPE 风险。

## import 汇总

| 文件 | 需新增的 import |
|------|----------------|
| `AbstractCapabilityExecutorTest.java` | 已有 `AtomicReference`，无需变更 |
| `TriageCapabilityExecutorTest.java` | 已有 `AtomicReference`，无需变更 |
| `DiscussionConclusionCapabilityExecutorTest.java` | 已有 `AtomicReference`，无需变更 |
| `DiagnosisCapabilityExecutorTest.java` | 新增 `import java.util.concurrent.atomic.AtomicReference;` |
| `AnalysisReportForInspectionCapabilityExecutorTest.java` | 新增 `import java.util.concurrent.atomic.AtomicReference;` |
| `AnalysisReportForLabTestCapabilityExecutorTest.java` | 新增 `import java.util.concurrent.atomic.AtomicReference;` |
| `ImageAnalysisCapabilityExecutorTest.java` | 新增 `import java.util.concurrent.atomic.AtomicReference;` |
| `RecommendExaminationCapabilityExecutorTest.java` | 新增 `import java.util.concurrent.atomic.AtomicReference;` |
| `RecommendExecutionOrderCapabilityExecutorTest.java` | 新增 `import java.util.concurrent.atomic.AtomicReference;` |
| `DefaultModelRouterTest.java` | `import com.aimedical.modules.ai.impl.config.ModelRouteConfig;`（替换 `ModelRoute` import） |

## 行为契约

1. 所有修改仅限于测试文件，生产代码零变更
2. 修改后运行 `mvn test -pl modules/ai/ai-impl -am` 预期全部 test-compile 通过且测试通过数 ≥ 425（common 225 + ai-api 200 + ai-impl ≥ 0，v24 中 ai-impl 因编译失败未运行）
3. 每个文件的修改范围：仅包装现有字面量为 `new AtomicReference<>(...)`，不改变语义
4. `ModelRouteConfig` 使用无参构造 + setter，与 Spring Boot 绑定模式一致
