# 任务指令（v25）

## 动作
RETRY

## 任务描述
修复 Task 18 (AiPlatformConfig + AiPlatformEnvironmentPostProcessor) 验证失败导致的 14 个测试编译错误。**生产代码零变更**，全部修改仅限 10 个测试文件中的 14 处类型不匹配。

### 根因
`AbstractCapabilityExecutor` 构造器 4 个参数类型从 `Map<String, Duration>` / `Duration` 改为 `AtomicReference<Map<String, Duration>>` / `AtomicReference<Duration>`（详见 detail_v24.md 修订说明 "v24 r2"），但测试文件在创建 executor 实例时仍传递原始 `Map.of(...)` 和 `Duration.ofSeconds(...)` 字面量，导致 test-compile 失败。同时 `AiRouterProperties.setRoutes()` 参数类型从 `Map<String, List<ModelRoute>>` 改为 `Map<String, List<ModelRouteConfig>>`，`DefaultModelRouterTest` 未同步。

## 选择理由
Task 18 是 Batch6 P3 首项，且 Task 19 (FallbackAiService 构造器迁移) 依赖 AiPlatformConfig 的属性概念。修复 test-compile 错误后即可重新验证。

## 任务上下文

### 问题文件及修复对照表

| 文件 | 错误行 | 修复方式 |
|------|--------|---------|
| `AbstractCapabilityExecutorTest.java` | 1471 | 匿名类 `TestExecutor` 构造器第 11/12/15 参数类型从 `Map<String, Duration>` 改为 `AtomicReference<Map<String, Duration>>`，第 13 参数从 `Duration` 改为 `AtomicReference<Duration>`；`super()` 调用同步 |
| `TriageCapabilityExecutorTest.java` | 89, 108 | `Map.of(..., Duration.ofSeconds(...))` → `new AtomicReference<>(Map.of(..., Duration.ofSeconds(...)))` |
| `TriageCapabilityExecutorTest.java` | 91, 110 | `Duration.ofSeconds(3)` → `new AtomicReference<>(Duration.ofSeconds(3))`（parseTimeoutDefault） |
| `DiscussionConclusionCapabilityExecutorTest.java` | 277, 340, 379, 419 | `Map.of(...)` → `new AtomicReference<>(Map.of(...))` |
| `DiscussionConclusionCapabilityExecutorTest.java` | 279, 342, 421 | `Duration.ofSeconds(3)` → `new AtomicReference<>(Duration.ofSeconds(3))` |
| `DiagnosisCapabilityExecutorTest.java` | 97 | `Map.of("DIAGNOSIS", Duration.ofSeconds(30))` → `new AtomicReference<>(Map.of(...))` |
| `DiagnosisCapabilityExecutorTest.java` | 98 | `Duration.ofSeconds(5)` → `new AtomicReference<>(Duration.ofSeconds(5))` |
| `AnalysisReportForInspectionCapabilityExecutorTest.java` | 97 | 同上 |
| `AnalysisReportForLabTestCapabilityExecutorTest.java` | 97 | 同上 |
| `ImageAnalysisCapabilityExecutorTest.java` | 97 | 同上 |
| `RecommendExaminationCapabilityExecutorTest.java` | 97 | 同上 |
| `RecommendExecutionOrderCapabilityExecutorTest.java` | 97 | 同上 |
| `DefaultModelRouterTest.java` | 13-16, 20 | 字段类型从 `ModelRoute` 改为 `ModelRouteConfig`；`props.setRoutes()` 参数类型同步为 `Map<String, List<ModelRouteConfig>>` |

### 详细修复说明

#### 1. 完整管线 Executor 测试（Triage、DiscussionConclusion）

**TriageCapabilityExecutorTest.java** — 2 处构造器调用需更新：

```java
// Line 89-92 (inline executor in test method):
new AtomicReference<>(Map.of("TRIAGE", Duration.ofSeconds(30))),  // capabilityTimeoutConfig
new AtomicReference<>(Map.of("TRIAGE", Duration.ofSeconds(5))),   // parseTimeoutConfig
new AtomicReference<>(Duration.ofSeconds(3)),                     // parseTimeoutDefault
null,                                                              // thinAdapterTimeout
new AtomicReference<>(Map.of()),                                  // thinAdapterPerCapabilityConfig

// Line 108-110 (createMinimalExecutor):
new AtomicReference<>(Map.of("TRIAGE", Duration.ofSeconds(30))),  // capabilityTimeoutConfig
null,                                                              // parseTimeoutConfig
null,                                                              // parseTimeoutDefault
null,                                                              // thinAdapterTimeout
null,                                                              // thinAdapterPerCapabilityConfig
```

**DiscussionConclusionCapabilityExecutorTest.java** — 4 处构造器调用，同上模式。

#### 2. AbstractCapabilityExecutorTest.java — 匿名类代理

第 1470-1472 行 4 个参数类型和传递方式需同步：

```java
// 参数声明（line 1460-1464 方法签名）：
AtomicReference<Map<String, Duration>> capabilityTimeoutConfig,
AtomicReference<Map<String, Duration>> parseTimeoutConfig,
AtomicReference<Duration> parseTimeoutDefault,
Duration thinAdapterTimeout,
AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig,

// super() 调用（line 1471-1472）：
capabilityTimeoutConfig, parseTimeoutConfig, parseTimeoutDefault,
thinAdapterTimeout, thinAdapterPerCapabilityConfig,
```

#### 3. 薄适配器 Executor 测试（6 个文件，相同模式）

```java
// current (line 97-98):
Map.of("CAPABILITY_NAME", Duration.ofSeconds(30)),   // capabilityTimeoutConfig
null, Duration.ofSeconds(5), thinAdapterTimeout,      // parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout

// fixed:
new AtomicReference<>(Map.of("CAPABILITY_NAME", Duration.ofSeconds(30))),
null, new AtomicReference<>(Duration.ofSeconds(5)), thinAdapterTimeout,
null,  // thinAdapterPerCapabilityConfig — need to add if constructor expects it
```

**注意**：薄适配器构造器顺序为（参照 DiagnosisCapabilityExecutor line 42-52）：
```
1. Object service
2. AiMetricsCollector metricsCollector
3. SlidingWindowMetricsStore metricsStore
4. AtomicReference<Map<String, Duration>> capabilityTimeoutConfig      ← 原为 Map
5. AtomicReference<Map<String, Duration>> parseTimeoutConfig           ← 原为 Map
6. AtomicReference<Duration> parseTimeoutDefault                       ← 原为 Duration
7. Duration thinAdapterTimeout
8. AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig  ← 原为 Map
9. ObjectMapper objectMapper
```

#### 4. DefaultModelRouterTest.java

```java
// 字段定义改为 ModelRouteConfig（而非 ModelRoute）：
private ModelRouteConfig routeA = new ModelRouteConfig();
// ... set endpointId, modelId, weight, timeoutMs via setters

// createRouter 参数类型同步：
private DefaultModelRouter createRouter(Map<String, List<ModelRouteConfig>> routes) {
    AiRouterProperties props = new AiRouterProperties();
    props.setRoutes(routes);  // 现在接受 Map<String, List<ModelRouteConfig>>
    ...
}

// 调用处同步：
createRouter(Map.of("KNOWN", List.of(routeA)));  // routeA 现在是 ModelRouteConfig
```

## 涉及文件清单（全部为修改，10 个测试文件）

| 操作 | 文件路径 |
|------|---------|
| 修改 | `modules/ai/ai-impl/src/test/java/.../orchestrator/AbstractCapabilityExecutorTest.java` |
| 修改 | `modules/ai/ai-impl/src/test/java/.../orchestrator/impl/TriageCapabilityExecutorTest.java` |
| 修改 | `modules/ai/ai-impl/src/test/java/.../orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` |
| 修改 | `modules/ai/ai-impl/src/test/java/.../orchestrator/impl/DiagnosisCapabilityExecutorTest.java` |
| 修改 | `modules/ai/ai-impl/src/test/java/.../orchestrator/impl/AnalysisReportForInspectionCapabilityExecutorTest.java` |
| 修改 | `modules/ai/ai-impl/src/test/java/.../orchestrator/impl/AnalysisReportForLabTestCapabilityExecutorTest.java` |
| 修改 | `modules/ai/ai-impl/src/test/java/.../orchestrator/impl/ImageAnalysisCapabilityExecutorTest.java` |
| 修改 | `modules/ai/ai-impl/src/test/java/.../orchestrator/impl/RecommendExaminationCapabilityExecutorTest.java` |
| 修改 | `modules/ai/ai-impl/src/test/java/.../orchestrator/impl/RecommendExecutionOrderCapabilityExecutorTest.java` |
| 修改 | `modules/ai/ai-impl/src/test/java/.../router/DefaultModelRouterTest.java` |

## RETRY 说明

**失败摘要**：Task 18 生产代码已全部实现并通过 compile，但因 AbstractCapabilityExecutor 构造器 4 个参数从 `Map/Duration` 改为 `AtomicReference` 后，10 个已有测试文件未同步更新参数类型，导致 14 个 test-compile 错误（429 已通过测试全部通过，0 运行期失败）。

**修正方向**：仅修改测试文件中的类型包装，将 `Map.of(...)` 包裹为 `new AtomicReference<>(Map.of(...))`，将 `Duration.ofSeconds(n)` 包裹为 `new AtomicReference<>(Duration.ofSeconds(n))`。DefaultModelRouterTest 中 `ModelRoute` 改为 `ModelRouteConfig`。生产代码零变更。

**验证方式**：修改后运行 `mvn test -pl modules/ai/ai-impl -am`，预期全部 test-compile 通过且测试通过数 ≥ 425。
