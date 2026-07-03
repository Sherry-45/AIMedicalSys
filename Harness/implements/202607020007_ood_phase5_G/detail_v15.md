# 详细设计（v15）

## 概述

在 `ai-impl/orchestrator/impl/` 包新增 6 个薄适配器型 CapabilityExecutor，每个通过继承 `AbstractCapabilityExecutor<T,R>` 实现，使用 `@Autowired(required=false) Object` 注入 Phase 4 服务（编译期不存在，运行时反射调用）；重写 `doExtractDepartmentId/doExtractVisitId/doExtractPatientId/doExtractSessionId` 从 `RequestContextUtils` 提取上下文；新增 `RequestContextUtils` 工具类；修改 `AbstractCapabilityExecutor.execute()` 中 `llmCallExecutor` 为 null 时的 NPE 问题。

## 文件规划

基路径（源码）：`AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/`
基路径（测试）：`AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/`

### 生产代码（8 个文件）

| # | 文件路径 | 操作 | 职责 |
|---|---------|------|------|
| 1 | `orchestrator/impl/DiagnosisCapabilityExecutor.java` | 新建 | DIAGNOSIS 薄适配器，委托 Phase 4 诊断服务 |
| 2 | `orchestrator/impl/AnalysisReportForInspectionCapabilityExecutor.java` | 新建 | ANALYSIS_REPORT_INSPECTION 薄适配器 |
| 3 | `orchestrator/impl/AnalysisReportForLabTestCapabilityExecutor.java` | 新建 | ANALYSIS_REPORT_LABTEST 薄适配器 |
| 4 | `orchestrator/impl/ImageAnalysisCapabilityExecutor.java` | 新建 | IMAGE_ANALYSIS 薄适配器（45s 超时） |
| 5 | `orchestrator/impl/RecommendExaminationCapabilityExecutor.java` | 新建 | RECOMMEND_EXAM 薄适配器（20s 超时） |
| 6 | `orchestrator/impl/RecommendExecutionOrderCapabilityExecutor.java` | 新建 | RECOMMEND_EXEC_ORDER 薄适配器（20s 超时） |
| 7 | `util/RequestContextUtils.java` | 新建 | Spring RequestContextHolder 请求头提取工具类 |
| 8 | `orchestrator/AbstractCapabilityExecutor.java` | 修改 | `execute()` 中 `llmCallExecutor==null` 时回退 `ForkJoinPool.commonPool()` |

### 测试代码（6 个文件）

| # | 文件路径 | 操作 | 主要测试维度 |
|---|---------|------|-------------|
| 9 | `orchestrator/impl/DiagnosisCapabilityExecutorTest.java` | 新建 | 空 DTO 降级、服务 null 降级、模拟成功委托、超时降级、Phase4BusinessException 失败 |
| 10 | `orchestrator/impl/AnalysisReportForInspectionCapabilityExecutorTest.java` | 新建 | 同上模式 |
| 11 | `orchestrator/impl/AnalysisReportForLabTestCapabilityExecutorTest.java` | 新建 | 同上模式 |
| 12 | `orchestrator/impl/ImageAnalysisCapabilityExecutorTest.java` | 新建 | 同上模式 |
| 13 | `orchestrator/impl/RecommendExaminationCapabilityExecutorTest.java` | 新建 | 同上模式 |
| 14 | `orchestrator/impl/RecommendExecutionOrderCapabilityExecutorTest.java` | 新建 | 同上模式 |

## 类型定义

### 1. RequestContextUtils

**形态**：class（工具类）
**包路径**：`com.aimedical.modules.ai.impl.util`
**职责**：使用 Spring `RequestContextHolder` 从当前请求头提取上下文字段

```java
package com.aimedical.modules.ai.impl.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestContextUtils {

    public static String extractFromRequestContext(String headerName) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attrs).getRequest().getHeader(headerName);
        }
        return null;
    }
}
```

**公开接口**：
- `static String extractFromRequestContext(String headerName)` — 从当前请求头提取指定名称的值

**构造方式**：不可实例化（private 隐式构造器）
**类型关系**：无继承/实现

---

### 2. AbstractCapabilityExecutor 修改

**形态**：abstract class（已有，修改）
**包路径**：`com.aimedical.modules.ai.impl.orchestrator`

**修改点**：

| # | 位置 | 原代码 | 修改后 |
|---|------|--------|--------|
| 1 | `execute()` 第 147-156 行 `supplyAsync()` | `CompletableFuture.supplyAsync(supplier, llmCallExecutor)` | `Executor executor = llmCallExecutor != null ? llmCallExecutor : ForkJoinPool.commonPool();` 后使用 `executor` |
| 2 | import 区 | 无 `ForkJoinPool` | 新增 `import java.util.concurrent.ForkJoinPool;` |

```java
// 修改后的 supplyAsync 调用（execute() 方法内）
Executor executor = llmCallExecutor != null ? llmCallExecutor : ForkJoinPool.commonPool();
CompletableFuture<AiResult<R>> future = CompletableFuture.supplyAsync(() -> {
    elapsedInDoExecuteInternal = System.currentTimeMillis() - capturedStartTime;
    try {
        return doExecuteInternal(capturedStartTime, capturedRequestCopy, capturedCapabilityId,
            capturedDepartmentId, capturedUserId, capturedSessionId, capturedCallerRole, capturedCallerId,
            capturedVisitId, capturedPatientId, capturedInputSummary);
    } finally {
        elapsedInDoExecuteInternal = System.currentTimeMillis() - capturedStartTime;
    }
}, executor);
```

---

### 3. 通用薄适配器行为模式（6 个执行器一致）

**形态**：class（薄适配器）
**包路径**：`com.aimedical.modules.ai.impl.orchestrator.impl`
**继承链**：`extends AbstractCapabilityExecutor<XXXRequest, XXXResponse>`

#### 3.1 注解

```java
@Service("DIAGNOSIS")  // 对应各执行器的能力标识
```

#### 3.2 构造器模式（以 DiagnosisCapabilityExecutor 为例）

```java
@Autowired
public DiagnosisCapabilityExecutor(
    @Autowired(required = false) Object diagnosisService,
    AiMetricsCollector metricsCollector,
    SlidingWindowMetricsStore metricsStore,
    @Qualifier("capabilityTimeoutConfig") Map<String, Duration> capabilityTimeoutConfig,
    @Qualifier("parseTimeoutConfig") Map<String, Duration> parseTimeoutConfig,
    Duration parseTimeoutDefault,
    @Value("${ai.execution.timeout.thin-adapter-default:30s}") Duration thinAdapterTimeout,
    @Qualifier("thinAdapterPerCapabilityConfig") Map<String, Duration> thinAdapterPerCapabilityConfig,
    ObjectMapper objectMapper
) {
    super(null, null, null, null, null,
          metricsCollector, metricsStore, null, null, null,
          capabilityTimeoutConfig, parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout,
          thinAdapterPerCapabilityConfig, null, objectMapper);
    this.diagnosisService = diagnosisService;
}
```

**super() 参数对照**：

| super 参数位置 | 参数名 | 薄适配器值 | 理由 |
|---------------|--------|-----------|------|
| 1 | inputType | null | 薄适配器不进行防御性拷贝转换（可空） |
| 2 | promptTemplateManager | null | 薄适配器不渲染 Prompt 模板 |
| 3 | modelRouter | null | 薄适配器不经过模型路由 |
| 4 | llmChatService | null | 薄适配器不直接调用 LLM |
| 5 | structuredOutputParser | null | 薄适配器不解析结构化输出 |
| 6 | metricsCollector | 构造传入 | 指标采集（必须） |
| 7 | metricsStore | 构造传入 | 滑动窗口存储（必须） |
| 8 | endpointHealthManager | null | 薄适配器不检查端点健康 |
| 9 | degradationStrategyMapRef | null | 薄适配器不参与降级策略检查 |
| 10 | localRuleFallback | null | 薄适配器暂不接入本地规则降级 |
| 11 | capabilityTimeoutConfig | 构造传入 | 超时配置（必须） |
| 12 | parseTimeoutConfig | 构造传入 | 解析超时配置（必须） |
| 13 | parseTimeoutDefault | 构造传入 | 默认解析超时（必须） |
| 14 | thinAdapterTimeout | 构造传入 | 薄适配器超时（必须） |
| 15 | thinAdapterPerCapabilityConfig | 构造传入 | 每能力超时覆盖（必须） |
| 16 | llmCallExecutor | null | 薄适配器不使用 LLM 线程池 |
| 17 | objectMapper | 构造传入 | 防御性拷贝（必须） |

#### 3.3 各执行器特有字段

| 执行器 | 能力标识 | @Service 值 | Phase 4 服务字段名（Object 类型） |
|-------|---------|------------|----------------------------------|
| DiagnosisCapabilityExecutor | DIAGNOSIS | "DIAGNOSIS" | diagnosisService |
| AnalysisReportForInspectionCapabilityExecutor | ANALYSIS_REPORT_INSPECTION | "ANALYSIS_REPORT_INSPECTION" | inspectionService |
| AnalysisReportForLabTestCapabilityExecutor | ANALYSIS_REPORT_LABTEST | "ANALYSIS_REPORT_LABTEST" | labTestService |
| ImageAnalysisCapabilityExecutor | IMAGE_ANALYSIS | "IMAGE_ANALYSIS" | imageAnalysisService |
| RecommendExaminationCapabilityExecutor | RECOMMEND_EXAM | "RECOMMEND_EXAM" | examinationService |
| RecommendExecutionOrderCapabilityExecutor | RECOMMEND_EXEC_ORDER | "RECOMMEND_EXEC_ORDER" | executionOrderService |

#### 3.4 DTO 类型参数

| 执行器 | T（输入） | R（输出） |
|-------|----------|----------|
| DiagnosisCapabilityExecutor | `DiagnosisRequest` | `DiagnosisResponse` |
| AnalysisReportForInspectionCapabilityExecutor | `InspectionReportRequest` | `InspectionReportResponse` |
| AnalysisReportForLabTestCapabilityExecutor | `LabTestReportRequest` | `LabTestReportResponse` |
| ImageAnalysisCapabilityExecutor | `ImageAnalysisRequest` | `ImageAnalysisResponse` |
| RecommendExaminationCapabilityExecutor | `ExaminationRecommendRequest` | `ExaminationRecommendResponse` |
| RecommendExecutionOrderCapabilityExecutor | `ExecutionOrderRequest` | `ExecutionOrderResponse` |

所有输入 DTO 位于 `com.aimedical.modules.ai.api.dto.{subpkg}.*`，当前均为空类（仅含无参构造器），`package` 前缀以 `com.aimedical.modules.ai.api.dto.` 开头。

#### 3.5 doExtract* 方法重写

每个执行器重写 4 个上下文提取方法，优先从 `RequestContextUtils` 提取请求头，回退到父类默认实现：

```java
@Override
protected String doExtractDepartmentId(T request) {
    String deptId = RequestContextUtils.extractFromRequestContext("X-Department-ID");
    return deptId != null ? deptId : super.doExtractDepartmentId(request);
}
@Override
protected String doExtractVisitId(T request) {
    String visitId = RequestContextUtils.extractFromRequestContext("X-Visit-ID");
    return visitId != null ? visitId : super.doExtractVisitId(request);
}
@Override
protected String doExtractPatientId(T request) {
    String patientId = RequestContextUtils.extractFromRequestContext("X-Patient-ID");
    return patientId != null ? patientId : super.doExtractPatientId(request);
}
@Override
protected String doExtractSessionId(T request) {
    String sessionId = RequestContextUtils.extractFromRequestContext("X-Session-ID");
    return sessionId != null ? sessionId : super.doExtractSessionId(request);
}
```

#### 3.6 doExecuteInternal 模板方法

以 `DiagnosisCapabilityExecutor` 为例，其余 5 个模式相同仅类型参数和字段名不同：

```java
@Override
protected AiResult<DiagnosisResponse> doExecuteInternal(
    long startTime, DiagnosisRequest request, String capabilityId,
    String departmentId, String userId, String sessionId,
    String callerRole, String callerId, String visitId, String patientId,
    String inputSummary
) {
    // @TODO Phase5: 使用真实 Phase 4 服务接口替换 @Autowired(required=false) Object
    // 底座切流初期：Phase 4 DTO 为空类，跳过委托直接降级
    if (isDtoEmpty(request)) {
        return doDegrade(startTime,
            DegradationReason.INFRASTRUCTURE_ERROR.getCode() + ":Phase4DtoEmpty",
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, null, null, null);
    }
    // Phase 4 服务未就绪（null）
    if (diagnosisService == null) {
        return doDegrade(startTime,
            DegradationReason.INFRASTRUCTURE_ERROR.getCode() + ":Phase4ServiceUnavailable",
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, null, null, null);
    }

    try {
        long resolvedTimeout = resolveThinAdapterTimeout(capabilityId);
        final Object service = diagnosisService;
        CompletableFuture<DiagnosisResponse> delegateFuture = CompletableFuture.supplyAsync(() -> {
            try {
                @SuppressWarnings("unchecked")
                DiagnosisResponse result = (DiagnosisResponse) service.getClass()
                    .getMethod("execute", request.getClass())
                    .invoke(service, request);
                return result;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        DiagnosisResponse result = delegateFuture.get(resolvedTimeout, TimeUnit.MILLISECONDS);

        // 成功路径：指标采集
        long elapsedMs = System.currentTimeMillis() - startTime;
        String outputSummary = extractOutputSummary(result);
        metricsCollector.record(new AiCallRecord(
            capabilityId, null, null, userId, departmentId, sessionId,
            visitId, patientId, callerRole, callerId,
            elapsedMs, false, null, 0, 0));
        metricsStore.recordSuccess(capabilityId, elapsedMs);
        return AiResult.success(result);
    } catch (TimeoutException e) {
        log.warn("ThinAdapter 委托超时: capabilityId={}, thinAdapterTimeout={}ms",
            capabilityId, resolveThinAdapterTimeout(capabilityId));
        return doDegrade(startTime,
            DegradationReason.TIMEOUT.getCode() + ":ThinAdapterTimeout",
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, null, null, null);
    } catch (ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof Phase4BusinessException || isKnownPhase4BusinessException(cause)) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            String errorCode = "PHASE4_" + cause.getClass().getSimpleName();
            metricsCollector.record(new AiCallRecord(
                capabilityId, null, null, userId, departmentId, sessionId,
                visitId, patientId, callerRole, callerId,
                elapsedMs, false, errorCode, 0, 0));
            metricsStore.recordFailure(capabilityId);
            return AiResult.failure(errorCode, cause.getMessage());
        }
        return doDegrade(startTime,
            DegradationReason.INFRASTRUCTURE_ERROR.getCode() + ":" + cause.getClass().getSimpleName(),
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, null, null, null);
    } catch (Exception e) {
        return doDegrade(startTime,
            DegradationReason.INFRASTRUCTURE_ERROR.getCode() + ":" + e.getClass().getSimpleName(),
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, null, null, null);
    }
}
```

#### 3.7 辅助方法

**isDtoEmpty**（6 个执行器共享，定义在各自类中）：

```java
// @TODO Phase5: 回归后移除该降级路径——Phase 4 DTO 就绪后 package 匹配不再需要
private boolean isDtoEmpty(T request) {
    String pkg = request.getClass().getPackage().getName();
    return pkg.startsWith("com.aimedical.modules.ai.api.dto.");
}
```

**resolveThinAdapterTimeout**（6 个执行器共享）：

```java
private long resolveThinAdapterTimeout(String capabilityId) {
    if (thinAdapterPerCapabilityConfig != null && thinAdapterPerCapabilityConfig.containsKey(capabilityId)) {
        return thinAdapterPerCapabilityConfig.get(capabilityId).toMillis();
    }
    return thinAdapterTimeout.toMillis();
}
```

#### 3.8 getCapabilityId / getInputType / getOutputType

每个执行器实现 CapabilityExecutor 接口的三个方法：

```java
@Override public String getCapabilityId() { return "DIAGNOSIS"; }
@Override public Class<DiagnosisRequest> getInputType() { return DiagnosisRequest.class; }
@Override public Class<DiagnosisResponse> getOutputType() { return DiagnosisResponse.class; }
```

## 各执行器类注释和 @TODO 标记

所有 6 个薄适配器类注释和 `doExecuteInternal()` 方法注释中添加以下 `@TODO Phase5:` 标记：

```java
/**
 * @TODO Phase5: 使用真实 Phase 4 服务接口替换 @Autowired(required=false) Object。
 * 当前使用 Object 类型避免编译期依赖，Phase 4 服务上线后应改为具体接口 + @Qualifier。
 */
```

## 错误处理

### 降级路径

| 条件 | 降级原因 | 返回形式 |
|------|---------|---------|
| `isDtoEmpty(request) == true` | `INFRASTRUCTURE_ERROR:Phase4DtoEmpty` | `AiResult.degraded(reason)` |
| `diagnosisService == null` | `INFRASTRUCTURE_ERROR:Phase4ServiceUnavailable` | `AiResult.degraded(reason)` |
| `delegateFuture.get()` 超时 → `TimeoutException` | `TIMEOUT:ThinAdapterTimeout` | `AiResult.degraded(reason)` |
| Phase 4 服务抛出 `RuntimeException(Phase4BusinessException)` → `ExecutionException` | `PHASE4_{ExceptionSimpleName}`（非降级，业务失败） | `AiResult.failure(errorCode, message)` |
| Phase 4 服务抛出其他异常 → `ExecutionException` | `INFRASTRUCTURE_ERROR:{CauseSimpleName}` | `AiResult.degraded(reason)` |
| 其他意外异常 | `INFRASTRUCTURE_ERROR:{ExceptionSimpleName}` | `AiResult.degraded(reason)` |

### 定制超时配置

通过 YAML `ai.execution.timeout.thin-adapter.per-capability` 覆盖：

```yaml
ai:
  execution:
    timeout:
      thin-adapter:
        per-capability:
          IMAGE_ANALYSIS: 45s
          RECOMMEND_EXAM: 20s
          RECOMMEND_EXEC_ORDER: 20s
```

- IMAGE_ANALYSIS 默认 45s（影像分析耗时高）
- RECOMMEND_EXAM 默认 20s（轻量推理）
- RECOMMEND_EXEC_ORDER 默认 20s（轻量推理）
- 其余 3 个使用 `thin-adapter-default`（30s）

## 行为契约

### 方法调用顺序
`execute()` (父类 final) → `doExecuteInternal()` (子类实现) → isDtoEmpty 预检 → Phase 4 服务 null 预检 → `CompletableFuture.supplyAsync` 反射委托 → `Future.get(timeout)` 超时检测 → 指标采集 / 降级

### 降级决策权
- 薄适配器不参与 `degradationStrategyMapRef` 预检管线（`checkPreDegradation` 因 degradationStrategyMapRef=null 跳过）
- 唯一自主降级来自 `thinAdapterTimeout` 超时检测（`CompletableFuture.get(timeout, TimeUnit)`）
- Phase 4 服务的业务异常（`Phase4BusinessException`）不上报告警，返回 `AiResult.failure()`

### 反射调用约定
Phase 4 服务接口必须存在 `public R execute(T request)` 方法，其中 R=Response 类型、T=Request 类型。通过 `service.getClass().getMethod("execute", request.getClass())` 获取并调用。

### ForkJoinPool 回退
当 `AbstractCapabilityExecutor.llmCallExecutor == null`（薄适配器不注入 LLM 线程池），`execute()` 中回退到 `ForkJoinPool.commonPool()` 作为默认 Executor。

## 依赖关系

**依赖的已有类型**：

| 类型 | 所在包 | 用途 |
|------|--------|------|
| `AbstractCapabilityExecutor<T,R>` | `orchestrator/` | 基类 |
| `CapabilityExecutor<T,R>` | `orchestrator/` | 接口 |
| `AiResult<T>` | `ai.api` | 返回类型 |
| `DegradationReason` | `ai.api.degradation` | 降级原因枚举 |
| `Phase4BusinessException` | `ai.api.dto.base` | 业务异常 instanceof 检测 |
| `AiCallRecord` | `impl.metrics` | 调用记录值对象 |
| `AiMetricsCollector` | `impl.metrics` | 指标采集接口 |
| `SlidingWindowMetricsStore` | `impl.metrics` | 指标存储 |
| `RequestContextUtils` | `impl.util` | 请求头提取工具（本任务新建） |
| `ObjectMapper` | `jackson.databind` | 防御性拷贝 |
| `DiagnosisRequest/Response` | `ai.api.dto.diagnosis` | DTO（空类） |
| `InspectionReportRequest/Response` | `ai.api.dto.inspection` | DTO（空类） |
| `LabTestReportRequest/Response` | `ai.api.dto.labtest` | DTO（空类） |
| `ImageAnalysisRequest/Response` | `ai.api.dto.image` | DTO（空类） |
| `ExaminationRecommendRequest/Response` | `ai.api.dto.examination` | DTO（空类） |
| `ExecutionOrderRequest/Response` | `ai.api.dto.execution` | DTO（空类） |

**外部依赖**：Spring `RequestContextHolder` / `ServletRequestAttributes`（ai-impl 中已包含 spring-web 依赖）

## 测试设计

### 测试替身策略

**禁止使用 `mock(Object.class)`**：Mockito 创建的代理类 `getClass()` 返回 CGLIB/ByteBuddy 代理类，其 `getMethod("execute", ...)` 会抛出 `NoSuchMethodException`。

**推荐方案——匿名类反射桩**：

```java
// 正常返回
Object service = new Object() {
    @SuppressWarnings("unused")
    public DiagnosisResponse execute(DiagnosisRequest req) {
        return new DiagnosisResponse();
    }
};

// 抛出 Phase4BusinessException
Object service = new Object() {
    @SuppressWarnings("unused")
    public DiagnosisResponse execute(DiagnosisRequest req) {
        throw new RuntimeException(new Phase4BusinessException("业务异常") {});
    }
};
```

**超时测试辅助**：使用 `ExecutorService`（`Executors.newSingleThreadExecutor()`）提交异步任务，在委托执行前 `Thread.sleep(delay)` 使 `CompletableFuture.get(timeout, TimeUnit)` 触发 `TimeoutException`。

### 测试维度（每个执行器 5 个测试方法）

以 `DiagnosisCapabilityExecutorTest` 为例：

| # | 测试方法 | 覆盖场景 | 验证要点 |
|---|---------|---------|---------|
| 1 | `shouldDegradeWhenDtoIsEmpty` | Phase 4 DTO 为空类（`new DiagnosisRequest()`） | `doExecuteInternal` 返回 `AiResult.isDegraded()=true`，`fallbackReason` 含 `:Phase4DtoEmpty` |
| 2 | `shouldDegradeWhenServiceIsNull` | Phase 4 服务未注入（`diagnosisService=null`） | 返回降级结果，`fallbackReason` 含 `:Phase4ServiceUnavailable` |
| 3 | `shouldSucceedWithValidDelegation` | 匿名类模拟 Phase 4 服务返回有效结果 | `AiResult.isSuccess()=true`，`data` 为模拟响应实例 |
| 4 | `shouldDegradeOnTimeout` | 委托执行延迟超过 `thinAdapterTimeout` | 返回降级结果，`fallbackReason` 含 `:ThinAdapterTimeout` |
| 5 | `shouldReturnFailureOnPhase4BusinessException` | Phase 4 服务抛出 `Phase4BusinessException` | `AiResult.isSuccess()=false`，`errorCode` 含 `PHASE4_` |

其余 5 个测试类（AnalysisReportForInspectionCapabilityExecutorTest 等）采用完全相同模式，仅替换具体的 Request/Response 类型。
