# 详细设计（v5）

## 概述

在 `ai-impl/orchestrator/` 包新增 `AiOrchestrator` 类，实现 `AiService` 接口全部 13 个方法，作为底座统一编排路由层。每个方法通过硬编码 capabilityId 委托给 `handle(capabilityId, request)` 核心方法，由后者从预先构建的 `executorMap` 查找对应 `CapabilityExecutor` 并执行。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/.../orchestrator/AiOrchestrator.java` | 新建 | 底座编排路由层，实现 AiService 全部 13 个方法 |

## 类型定义

### AiOrchestrator

**形态**：class

**包路径**：`com.aimedical.modules.ai.impl.orchestrator`

**修饰**：`public`

**注解**：
- `@Service`
- `@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")`

**职责**：实现 `AiService` 接口，通过能力标识（capabilityId）将请求路由到对应的 `CapabilityExecutor`，统一处理异常和错误日志，作为底座架构的入口编排层。

**字段**：

| 字段 | 类型 | 修饰符 | 注入方式 | 用途 |
|------|------|--------|---------|------|
| `executorList` | `List<CapabilityExecutor<?, ?>>` | `private final` | 构造器注入 | Spring 自动收集所有 CapabilityExecutor Bean |
| `metricsStore` | `SlidingWindowMetricsStore` | `private final` | 构造器注入 | 用于 `recordFailure()` 记录失败指标 |
| `metricsCollector` | `AiMetricsCollector` | `private final` | 构造器注入 | 存根接口，当前仅占位供后续扩展 |
| `executorMap` | `Map<String, CapabilityExecutor<?, ?>>` | `private volatile` | @PostConstruct 构建 | capabilityId → CapabilityExecutor 映射 |

**构造器**：

```java
public AiOrchestrator(
    List<CapabilityExecutor<?, ?>> executorList,
    SlidingWindowMetricsStore metricsStore,
    AiMetricsCollector metricsCollector
)
```

**公开接口**（AiService 全部 13 个方法）：

| 方法签名 | 返回类型 | capabilityId |
|---------|---------|-------------|
| `triage(TriageRequest request)` | `CompletableFuture<AiResult<TriageResponse>>` | "TRIAGE" |
| `diagnosis(DiagnosisRequest request)` | `CompletableFuture<AiResult<DiagnosisResponse>>` | "DIAGNOSIS" |
| `prescriptionCheck(PrescriptionCheckRequest request)` | `CompletableFuture<AiResult<PrescriptionCheckResponse>>` | "RX_AUDIT" |
| `generateMedicalRecord(MedicalRecordGenRequest request)` | `CompletableFuture<AiResult<MedicalRecordGenResponse>>` | "MEDICAL_RECORD_GEN" |
| `analysisReportForInspection(InspectionReportRequest request)` | `CompletableFuture<AiResult<InspectionReportResponse>>` | "ANALYSIS_REPORT_INSPECTION" |
| `analysisReportForLabTest(LabTestReportRequest request)` | `CompletableFuture<AiResult<LabTestReportResponse>>` | "ANALYSIS_REPORT_LABTEST" |
| `imageAnalysis(ImageAnalysisRequest request)` | `CompletableFuture<AiResult<ImageAnalysisResponse>>` | "IMAGE_ANALYSIS" |
| `knowledgeBaseQuery(KbQueryRequest request)` | `CompletableFuture<AiResult<KbQueryResponse>>` | "KB_QUERY" |
| `recommendExamination(ExaminationRecommendRequest request)` | `CompletableFuture<AiResult<ExaminationRecommendResponse>>` | "RECOMMEND_EXAM" |
| `prescriptionAssist(PrescriptionAssistRequest request)` | `CompletableFuture<AiResult<PrescriptionAssistResponse>>` | "RX_ASSIST" |
| `recommendExecutionOrder(ExecutionOrderRequest request)` | `CompletableFuture<AiResult<ExecutionOrderResponse>>` | "RECOMMEND_EXEC_ORDER" |
| `schedule(ScheduleRequest request)` | `CompletableFuture<AiResult<ScheduleResponse>>` | "SCHEDULE" |
| `discussionConclusion(DiscussionConclusionRequest request)` | `CompletableFuture<AiResult<DiscussionConclusionResponse>>` | "DISCUSSION_CONCLUSION" |

每个公开方法签名完全一致的模式：
```java
@Override
public CompletableFuture<AiResult<TriageResponse>> triage(TriageRequest request) {
    return handle("TRIAGE", request);
}
```

**内部方法**：

```java
@PostConstruct
void initExecutorMap()
```
- 遍历 `executorList`，对每个 executor 调用 `executor.getCapabilityId()` 获取 capabilityId
- 以 capabilityId 为 key、executor 为 value 构建 `ConcurrentHashMap<String, CapabilityExecutor<?, ?>>`
- 若重复 capabilityId，后注册的覆盖先注册的，log.warn 记录覆盖事件

```java
@SuppressWarnings({"rawtypes", "unchecked"})
private <T> CompletableFuture<AiResult<T>> handle(String capabilityId, Object request)
```
- 局部变量声明为原始类型 `CapabilityExecutor executor = executorMap.get(capabilityId)` 以绕开通配符捕获问题（`Map<String, CapabilityExecutor<?, ?>>` 的 `get()` 返回 `CapabilityExecutor<?, ?>`，直接调用 `.execute(Object, String)` 因 wildcard capture 无法编译；使用原始类型后以 Object 参数调用可行）
- 若为 null：log.warn + 返回 `CompletableFuture.completedFuture(AiResult.failure("未注册能力标识: " + capabilityId))`
- 否则调用 `executor.execute(request, capabilityId)` 并直接返回其结果（透传）
- 若 `executor.execute()` 同步抛出异常：catch (Exception e) → log.error + `metricsStore.recordFailure(capabilityId)` + 返回 `CompletableFuture.completedFuture(AiResult.failure("AI服务暂时不可用，请稍后重试"))`
- catch 块中留 TODO 注释：`// TODO: metricsCollector.record() when AiMetricsCollector methods are defined`
- `@SuppressWarnings("rawtypes")`：消除原始类型局部变量的 rawtypes 警告
- `@SuppressWarnings("unchecked")`：消除 executor.execute() 返回 `CompletableFuture<AiResult<R>>` 到 `CompletableFuture<AiResult<T>>` 的未检查转型警告

**类型关系**：implements `AiService`

## 错误处理

| 场景 | 处理方式 |
|------|---------|
| `executorMap.get(capabilityId)` 返回 null | log.warn + `CompletableFuture.completedFuture(AiResult.failure("未注册能力标识: " + capabilityId))` |
| `executor.execute()` 同步抛出异常 | log.error(e) + `metricsStore.recordFailure(capabilityId)` + `CompletableFuture.completedFuture(AiResult.failure("AI服务暂时不可用，请稍后重试"))` |
| 异步异常（CompletableFuture 内部） | 不拦截，透传给调用方（MockAiService 风格的 future.failedFuture 等） |

**注意**：
- `handle()` 只捕获同步抛出的 `Exception`，不包装 executor.execute() 返回的 CompletableFuture 中的异步异常
- catch 块中暂不调用 `metricsCollector.record()`（空接口存根），仅留 TODO 标记

## 行为契约

1. **方法调用映射**：每个 AiService 方法使用硬编码 capabilityId（见上表），请求 request 对象直接传递，不做转换或包装
2. **executorMap 初始化顺序**：`@PostConstruct initExecutorMap()` 在构造器之后、任何对外服务调用之前执行
3. **executor.execute() 返回值透传**：`handle()` 直接将 executor 返回的 `CompletableFuture<AiResult<R>>` 向上转型为 `CompletableFuture<AiResult<T>>` 返回，不额外包装 `.thenApply()` 或 `.exceptionally()`
4. **同步异常保护**：executor.execute() 可能同步抛出异常（如空指针、类型转换），`handle()` 用 try-catch(Exception) 保护并返回 failure 结果
5. **异步异常不干预**：CompletableFuture 内部的异常（超时、业务异常）由 AbstractCapabilityExecutor 自身处理，AiOrchestrator 不做二次拦截

## 依赖关系

| 类型 | 来源 | 被依赖方式 |
|------|------|-----------|
| `AiService` | ai-api（已有） | implements |
| `AiResult` | ai-api（已有） | 返回值类型 + 静态工厂方法 failure() |
| `CapabilityExecutor` | ai-impl/orchestrator（已有） | executorMap 值类型 |
| `SlidingWindowMetricsStore` | ai-impl/metrics（已有） | 构造器注入，catch 块中调用 recordFailure() |
| `AiMetricsCollector` | ai-impl/metrics（已有） | 构造器注入（存根，catch 块中留 TODO） |
| 全部 DTO 类型（13 request + 13 response） | ai-api/dto/*（已有） | AiService 方法签名中引用 |

**对外暴露**：无额外暴露，仅作为 AiService 的一个实现由 Spring 容器管理

## 修订说明（v5 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] `handle()` 中 `executorMap.get()` 返回 `CapabilityExecutor<?, ?>` 无法直接以 Object 参数调用 `execute(T, String)`，Java wildcard capture 导致编译错误 | 将局部变量声明为原始类型 `CapabilityExecutor executor` 以绕开通配符捕获；`@SuppressWarnings` 扩展为 `{"rawtypes", "unchecked"}` 分别抑制原始类型警告和返回转型未检查警告；在方法描述中明确说明了该方案的原理和必要性 |
