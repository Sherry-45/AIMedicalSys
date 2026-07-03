# 任务指令（v15）

## 动作
REVIEW_REVISED

## 任务描述
在 `ai-impl/orchestrator/impl/` 包新增 6 个薄适配器型 CapabilityExecutor，每个继承 `AbstractCapabilityExecutor`，通过 Phase 4 业务服务委托实现能力调用；重写 `doExtractDepartmentId/doExtractVisitId/doExtractPatientId/doExtractSessionId` 从 RequestContext 提取上下文；底座切流初期因 Phase 4 DTO 为空类，`doExecuteInternal()` 跳过委托直接降级（`DegradationReason.INFRASTRUCTURE_ERROR + ":Phase4DtoEmpty"`）。

### 涉及文件（全部新建）
基路径（源码）：`AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/`
基路径（测试）：`AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/`

#### 生产代码（7 个文件）
| # | 文件 | 能力标识 | Phase 4 服务接口（provided 依赖） | 输入/输出 DTO |
|---|------|---------|--------------------------------|--------------|
| 1 | `DiagnosisCapabilityExecutor.java` | `DIAGNOSIS` | `Object`（编译期不存在，运行时反射） | `DiagnosisRequest` / `DiagnosisResponse` |
| 2 | `AnalysisReportForInspectionCapabilityExecutor.java` | `ANALYSIS_REPORT_INSPECTION` | `Object`（编译期不存在，运行时反射） | `InspectionReportRequest` / `InspectionReportResponse` |
| 3 | `AnalysisReportForLabTestCapabilityExecutor.java` | `ANALYSIS_REPORT_LABTEST` | `Object`（编译期不存在，运行时反射） | `LabTestReportRequest` / `LabTestReportResponse` |
| 4 | `ImageAnalysisCapabilityExecutor.java` | `IMAGE_ANALYSIS` | `Object`（编译期不存在，运行时反射） | `ImageAnalysisRequest` / `ImageAnalysisResponse` |
| 5 | `RecommendExaminationCapabilityExecutor.java` | `RECOMMEND_EXAM` | `Object`（编译期不存在，运行时反射） | `ExaminationRecommendRequest` / `ExaminationRecommendResponse` |
| 6 | `RecommendExecutionOrderCapabilityExecutor.java` | `RECOMMEND_EXEC_ORDER` | `Object`（编译期不存在，运行时反射） | `ExecutionOrderRequest` / `ExecutionOrderResponse` |
| 7 | `RequestContextUtils.java`（提前创建） | — | — | — |
| 8 | `AbstractCapabilityExecutor.java`（修改） | — | — | — |

#### 测试代码（6 个文件）
| # | 文件 | 主要测试维度 |
|---|------|-------------|
| 7 | `DiagnosisCapabilityExecutorTest.java` | 空 DTO 降级、模拟成功委托、TimeoutException 降级、Phase4BusinessException 失败 |
| 8 | `AnalysisReportForInspectionCapabilityExecutorTest.java` | 同上模式 |
| 9 | `AnalysisReportForLabTestCapabilityExecutorTest.java` | 同上模式 |
| 10 | `ImageAnalysisCapabilityExecutorTest.java` | 同上模式 |
| 11 | `RecommendExaminationCapabilityExecutorTest.java` | 同上模式 |
| 12 | `RecommendExecutionOrderCapabilityExecutorTest.java` | 同上模式 |

### 类型详细要求

#### 公共行为模式（6 个执行器一致）

**注解**：
```java
@Service("DIAGNOSIS")  // 对应能力标识
```

**继承链**：`extends AbstractCapabilityExecutor<XXXRequest, XXXResponse>`

**构造器**（以 DiagnosisCapabilityExecutor 为例）：
```java
@Autowired
public DiagnosisCapabilityExecutor(
    // Phase 4 业务服务使用 Object 类型避免编译期类型依赖
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

**super() 参数对应关系**（AbstractCapabilityExecutor 17 参数构造器）：
| # | 参数 | 薄适配器值 | 理由 |
|---|------|-----------|------|
| 1 | inputType | null | 薄适配器不进行防御性拷贝转换（可空） |
| 2 | promptTemplateManager | null | 薄适配器不渲染 Prompt 模板 |
| 3 | modelRouter | null | 薄适配器不经过模型路由 |
| 4 | llmChatService | null | 薄适配器不直接调用 LLM |
| 5 | structuredOutputParser | null | 薄适配器不解析结构化输出 |
| 6 | metricsCollector | metricsCollector | 指标采集（必须） |
| 7 | metricsStore | metricsStore | 滑动窗口存储（必须） |
| 8 | endpointHealthManager | null | 薄适配器不检查端点健康 |
| 9 | degradationStrategyMapRef | null | 薄适配器不参与降级策略检查（降级全权委托 Phase 4 服务） |
| 10 | localRuleFallback | null | 薄适配器暂不接入本地规则降级 |
| 11 | capabilityTimeoutConfig | capabilityTimeoutConfig | 超时配置（必须） |
| 12 | parseTimeoutConfig | parseTimeoutConfig | 解析超时配置（必须） |
| 13 | parseTimeoutDefault | parseTimeoutDefault | 默认解析超时（必须） |
| 14 | thinAdapterTimeout | thinAdapterTimeout | 薄适配器超时（必须） |
| 15 | thinAdapterPerCapabilityConfig | thinAdapterPerCapabilityConfig | 每能力超时覆盖（必须） |
| 16 | llmCallExecutor | null | 薄适配器不使用 LLM 线程池 |
| 17 | objectMapper | objectMapper | 防御性拷贝（必须） |

**注意**：构造器参数 `AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef` 和 `LocalRuleFallback<T, R> localRuleFallback` 均传递 `null` 给 super()。这是因为薄适配器的降级判定全权委托给 Phase 4 服务，底座降级策略体系（超时/熔断）对薄适配器不可用。薄适配器的唯一自主降级来自 `thinAdapterTimeout` 超时检测——但此超时在 doExecuteInternal() 内通过 `CompletableFuture.get(timeout, TimeUnit)` 实现，不经过父类的 `degradationStrategyMapRef` 预检管线。

### AbstractCapabilityExecutor 修改（本任务范围）

由于薄适配器向 `super()` 传递 `llmCallExecutor=null`（薄适配器不使用 LLM 线程池），`AbstractCapabilityExecutor.execute()` 第 147-156 行的 `CompletableFuture.supplyAsync(supplier, llmCallExecutor)` 在 `llmCallExecutor` 为 null 时会立即抛出 NPE（JDK `screenExecutor()` 硬校验）。

**修改方案**：在 `execute()` 中为 null 回退到 `ForkJoinPool.commonPool()`：

```java
// 第 147 行改为：
Executor executor = llmCallExecutor != null ? llmCallExecutor : ForkJoinPool.commonPool();
CompletableFuture<AiResult<R>> future = CompletableFuture.supplyAsync(() -> {
    ...  // 原有 lambda 体不变
}, executor);
```

新增 import：`import java.util.concurrent.ForkJoinPool;`

### @TODO Phase5: 临时方案标记

所有 6 个薄适配器类的类注释和 `doExecuteInternal()` 方法注释中，添加以下 `@TODO` 标记，提示 Phase 5 重构：

```java
/**
 * @TODO Phase5: 使用真实 Phase 4 服务接口替换 @Autowired(required=false) Object。
 * 当前使用 Object 类型避免编译期依赖，Phase 4 服务上线后应改为具体接口 + @Qualifier。
 */
```

同时，`isDtoEmpty` 方法中的硬编码 package 前缀匹配也需添加 `@TODO Phase5:` 标记，提示回归后移除该降级路径。

**doExecuteInternal() 模板方法**（以 DiagnosisCapabilityExecutor 为例）：
```java
@Override
protected AiResult<DiagnosisResponse> doExecuteInternal(
    long startTime, DiagnosisRequest request, String capabilityId,
    String departmentId, String userId, String sessionId,
    String callerRole, String callerId, String visitId, String patientId,
    String inputSummary
) {
    // 底座切流初期：Phase 4 DTO 为空类，跳过委托直接降级
    // 检测策略：按 package 前缀判定是否属于已知空 DTO package
    if (isDtoEmpty(request)) {
        return doDegrade(startTime, DegradationReason.INFRASTRUCTURE_ERROR.getCode() + ":Phase4DtoEmpty",
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, null, null, null);
    }
    // Phase 4 服务未就绪（null）
    if (diagnosisService == null) {
        return doDegrade(startTime, DegradationReason.INFRASTRUCTURE_ERROR.getCode() + ":Phase4ServiceUnavailable",
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, null, null, null);
    }

    try {
        // 通过反射调用 Phase 4 服务（避免编译期类型依赖）
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
        return doDegrade(startTime, DegradationReason.TIMEOUT.getCode() + ":ThinAdapterTimeout",
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, null, null, null);
    } catch (ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof Phase4BusinessException || isKnownPhase4BusinessException(cause)) {
            // Phase 4 业务异常 → AiResult.failure()
            long elapsedMs = System.currentTimeMillis() - startTime;
            String errorCode = "PHASE4_" + cause.getClass().getSimpleName();
            metricsCollector.record(new AiCallRecord(
                capabilityId, null, null, userId, departmentId, sessionId,
                visitId, patientId, callerRole, callerId,
                elapsedMs, false, errorCode, 0, 0));
            metricsStore.recordFailure(capabilityId);
            return AiResult.failure(errorCode, cause.getMessage());
        }
        // 基础设施异常 → 降级
        return doDegrade(startTime, DegradationReason.INFRASTRUCTURE_ERROR.getCode() + ":" + cause.getClass().getSimpleName(),
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, null, null, null);
    } catch (Exception e) {
        return doDegrade(startTime, DegradationReason.INFRASTRUCTURE_ERROR.getCode() + ":" + e.getClass().getSimpleName(),
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, null, null, null);
    }
}
```

**辅助方法 isDtoEmpty**：
```java
private boolean isDtoEmpty(T request) {
    // 当前 Phase 4 DTO 均为空类，通过 package 前缀检测
    // 已知空 DTO package 列表：ai-api 模块 dto 子包
    String pkg = request.getClass().getPackage().getName();
    return pkg.startsWith("com.aimedical.modules.ai.api.dto.");
}
```

**RequestContextUtils 提前创建**：

前置创建 `RequestContextUtils.java`（ai-impl 工具类，Task 18 将扩展），位于 `ai-impl/.../util/` 包，使用 Spring `RequestContextHolder` 从当前请求头提取上下文：

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

**doExtractDepartmentId/doExtractVisitId/doExtractPatientId/doExtractSessionId 重写**：
```java
@Override
protected String doExtractDepartmentId(T request) {
    // Phase 4 DTO 尚未继承 AiRequestBase，从 RequestContext 提取
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

**resolveThinAdapterTimeout 辅助方法**：
```java
private long resolveThinAdapterTimeout(String capabilityId) {
    if (thinAdapterPerCapabilityConfig != null && thinAdapterPerCapabilityConfig.containsKey(capabilityId)) {
        return thinAdapterPerCapabilityConfig.get(capabilityId).toMillis();
    }
    return thinAdapterTimeout.toMillis();
}
```

#### 各执行器特有字段

| 执行器 | 能力标识 | Phase 4 服务字段名（Object 类型） | 默认超时 |
|--------|---------|----------------------------------|---------|
| DiagnosisCapabilityExecutor | DIAGNOSIS | diagnosisService | 30s |
| AnalysisReportForInspectionCapabilityExecutor | ANALYSIS_REPORT_INSPECTION | inspectionService | 30s |
| AnalysisReportForLabTestCapabilityExecutor | ANALYSIS_REPORT_LABTEST | labTestService | 30s |
| ImageAnalysisCapabilityExecutor | IMAGE_ANALYSIS | imageAnalysisService | 45s（影像分析耗时高） |
| RecommendExaminationCapabilityExecutor | RECOMMEND_EXAM | examinationService | 20s（轻量推理） |
| RecommendExecutionOrderCapabilityExecutor | RECOMMEND_EXEC_ORDER | executionOrderService | 20s（轻量推理） |

**各执行器超时配置**（通过 YAML `thin-adapter.per-capability` 覆盖）：
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

### 各能力对应的 DTO 来源

| 执行器 | 输入 DTO 包路径 | 输出 DTO 包路径 |
|--------|----------------|-----------------|
| DiagnosisCapabilityExecutor | `com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest` | `com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisResponse` |
| AnalysisReportForInspectionCapabilityExecutor | `com.aimedical.modules.ai.api.dto.inspection.InspectionReportRequest` | `com.aimedical.modules.ai.api.dto.inspection.InspectionReportResponse` |
| AnalysisReportForLabTestCapabilityExecutor | `com.aimedical.modules.ai.api.dto.labtest.LabTestReportRequest` | `com.aimedical.modules.ai.api.dto.labtest.LabTestReportResponse` |
| ImageAnalysisCapabilityExecutor | `com.aimedical.modules.ai.api.dto.image.ImageAnalysisRequest` | `com.aimedical.modules.ai.api.dto.image.ImageAnalysisResponse` |
| RecommendExaminationCapabilityExecutor | `com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendRequest` | `com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendResponse` |
| RecommendExecutionOrderCapabilityExecutor | `com.aimedical.modules.ai.api.dto.execution.ExecutionOrderRequest` | `com.aimedical.modules.ai.api.dto.execution.ExecutionOrderResponse` |

**注意**：DTO 均位于 `ai-api` 模块的 `dto/` 子包中（当前均为空类），Phase 4 模块的业务服务接口位于各模块自己的 `service/` 包中（当前可能不存在，`@Autowired(required = false)` 处理）。

## 选择理由
Batch5 P0。Task 17 是 6 项薄适配器能力的执行器实现，所有前置依赖均已就绪（AbstractCapabilityExecutor 骨架、Phase4BusinessException 异常基类、CallContext 值对象、Phase4ServiceMeta/Phase4ServiceMetaCapable 元数据契约）。Phase 4 DTO 为空类期间，所有薄适配器跳过委托直接降级，不阻塞底座上线。

## 任务上下文

### 设计文档对照
| 设计文档 § | 类型 | 关键约束 |
|-----------|------|---------|
| §3.12.1 | DiagnosisCapabilityExecutor | DIAGNOSIS; DiagnosisService.execute(); 降级预检→委托→指标采集 |
| §3.12.2 | AnalysisReportForInspectionCapabilityExecutor | ANALYSIS_REPORT_INSPECTION; InspectionService; 同上模式 |
| §3.12.3 | AnalysisReportForLabTestCapabilityExecutor | ANALYSIS_REPORT_LABTEST; LabTestService; 同上模式 |
| §3.12.4 | ImageAnalysisCapabilityExecutor | IMAGE_ANALYSIS; ImageAnalysisService; 45s 超时 |
| §3.12.5 | RecommendExaminationCapabilityExecutor | RECOMMEND_EXAM; ExaminationService; 20s 超时 |
| §3.12.6 | RecommendExecutionOrderCapabilityExecutor | RECOMMEND_EXEC_ORDER; ExecutionOrderService; 20s 超时 |
| §3.1 | DTO 空类初始期 | 底座切流初期跳过委托直接降级，原因 INFRASTRUCTURE_ERROR + ":Phase4DtoEmpty" |

### 已有代码上下文

已完成的依赖：
- `AbstractCapabilityExecutor`（ai-impl/orchestrator/）— 17 参数构造器、doDegrade()、doExtract* 默认实现、isKnownPhase4BusinessException()、executeStandardPipeline()、knownPhase4Packages 静态字段
- `Phase4BusinessException`（ai-api/dto/base/）— 抽象异常基类，薄适配器 catch 块通过 instanceof 检测
- `CallContext`（ai-api/dto/base/）— 9 字段不可变业务上下文值对象
- `Phase4ServiceMeta` / `Phase4ServiceMetaCapable`（ai-api/dto/base/）— 元数据契约
- `AiRequestBase`（ai-api/dto/base/）— 抽象骨架，4 个 getter 默认返回 null
- `SlidingWindowMetricsStore`（ai-impl/metrics/）— 指标存储
- `AiMetricsCollector`（ai-impl/metrics/）— 指标采集接口（存根）
- `AiCallRecord`（ai-impl/metrics/）— 调用记录值对象（15 参数构造器，不含 success/failure 工厂方法）
- `LocalRuleFallback<T,R>`（ai-impl/fallback/）— 本地规则降级接口（存根）
- `RequestContextUtils`（本任务提前创建最小实现，使用 Spring RequestContextHolder 提取请求头）

现有同目录下的参照实现：
- `TriageCapabilityExecutor` — 完整管线型执行器，使用 executeStandardPipeline()，17 参数全传
- `DiscussionConclusionCapabilityExecutor` — 完整管线型执行器，含压缩/截断前置逻辑

### 测试规划

每个测试类覆盖以下场景（以 DiagnosisCapabilityExecutorTest 为例，其余 5 个同理）：

| # | 测试方法 | 覆盖场景 | 验证要点 |
|---|---------|---------|---------|
| 1 | `shouldDegradeWhenDtoIsEmpty` | Phase 4 DTO 为空类 | doExecuteInternal 返回 AiResult.isDegraded()=true, reason 含 ":Phase4DtoEmpty" |
| 2 | `shouldDegradeWhenServiceIsNull` | Phase 4 服务未注入（null） | 返回降级结果，reason 含 ":Phase4ServiceUnavailable" |
| 3 | `shouldSucceedWithValidDelegation` | 模拟 Phase 4 服务返回有效结果 | 返回 AiResult.isSuccess()=true, data 为模拟响应 |
| 4 | `shouldDegradeOnTimeout` | 委托超时 | 返回降级结果，reason 含 ":ThinAdapterTimeout" |
| 5 | `shouldReturnFailureOnPhase4BusinessException` | Phase 4 服务抛出 Phase4BusinessException | 返回 AiResult.isSuccess()=false, errorCode 含 "PHASE4_" |

**测试辅助**：
- 生产代码的 Phase 4 服务字段为 `Object` 类型，通过反射调用 `execute()` 方法
- 测试中使用匿名类构造模拟服务（避免 Mockito 代理带来反射问题），见下方测试替身策略
- `isDtoEmpty(request)` 使用 package 前缀检测，测试中构造空 DTO 实例（`new DiagnosisRequest()`）触发降级路径

**测试替身策略（仅使用匿名类，避免 mock(Object) 反射问题）**：

⚠️ 不得使用 `mock(Object.class)` 方式：Mockito 创建的代理类 `getClass()` 返回 CGLIB/ByteBuddy 代理类，其 `getMethod("execute", ...)` 会抛出 `NoSuchMethodException`。

**推荐方案——匿名类反射桩**：
```java
// Phase 4 服务匿名声一（不继承/实现任何接口）
Object service = new Object() {
    @SuppressWarnings("unused")
    public DiagnosisResponse execute(DiagnosisRequest req) {
        return new DiagnosisResponse();
    }
};
// 通过反射调用 execute()
Method method = service.getClass().getMethod("execute", request.getClass());
DiagnosisResponse result = (DiagnosisResponse) method.invoke(service, request);
```

**超时测试辅助**：在测试中使用真实 `ExecutorService`（如 `Executors.newSingleThreadExecutor()`）控制 `delegateFuture.get(timeout, TimeUnit)` 的超时触发，在提交任务前 sleep 足够时间让 CompletableFuture 超时。

**Phase4BusinessException 抛出测试辅助**：
```java
Object service = new Object() {
    @SuppressWarnings("unused")
    public DiagnosisResponse execute(DiagnosisRequest req) {
        throw new RuntimeException(new Phase4BusinessException("业务异常") {});
    }
};
```

## 已有代码上下文

基路径（源码）：`AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/`
基路径（测试）：`AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/`

已有同目录文件：
- `TriageCapabilityExecutor.java`（完整管线型，17 参数全传 super）
- `PrescriptionCheckCapabilityExecutor.java`
- `MedicalRecordGenCapabilityExecutor.java`
- `PrescriptionAssistCapabilityExecutor.java`
- `KbQueryCapabilityExecutor.java`
- `ScheduleCapabilityExecutor.java`
- `DiscussionConclusionCapabilityExecutor.java`

## 修订说明（v15 r2）

### 涉及文件补充（较 R18 REVIEW_REVISED（r1）新增）

| # | 操作 | 文件 | 修改内容 |
|---|------|------|---------|
| 8 | 修改 | `AbstractCapabilityExecutor.java` | `execute()` 中 `llmCallExecutor==null` 时回退 `ForkJoinPool.commonPool()`；新增 `import java.util.concurrent.ForkJoinPool` |
| — | 修改 | 6 个薄适配器类注释 | 类注释和 doExecuteInternal 注释添加 `@TODO Phase5:` 标记，提示使用真实服务接口替换 Object 注入 |

### 修订说明

| 审查意见 | 修改措施 |
|---------|---------|
| [严重] llmCallExecutor=null 导致 supplyAsync NPE | 方案 1（最小改动）：修改 `AbstractCapabilityExecutor.execute()`，`llmCallExecutor == null` 时使用 `ForkJoinPool.commonPool()` 作为默认 Executor。将 AbstractCapabilityExecutor.java 加入本任务涉及文件（修改），见 §AbstractCapabilityExecutor 修改 |
| [一般] mock(Object.class) 反射桩不可用（Mockito 代理类不含 execute 方法） | 从测试策略中移除 `mock(Object.class)` 路径，仅保留匿名类方式；补充完整可编译的匿名类示例代码（正常返回 + 异常抛出 + 超时测试辅助） |
| [轻微] @Autowired Object 后续风险 | 在 6 个薄适配器类注释和 doExecuteInternal 注释中添加 `@TODO Phase5:` 标记，提示 Phase 5 回归时替换为具体服务接口 + @Qualifier；isDtoEmpty 方法同样添加标记 |

## 修订说明（v15 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] RequestContextUtils 不存在导致 doExtract* 编译错误 | 选项 A：本任务提前创建 `RequestContextUtils.java`（基于 Spring RequestContextHolder 的最小实现），将其加入涉及文件清单（#7），并补充完整代码模板 |
| [严重] AiCallRecord.success()/failure() 静态工厂方法不存在 | 将 doExecuteInternal 模板中 `AiCallRecord.success(...)` / `AiCallRecord.failure(...)` 替换为 `new AiCallRecord(...)` 直接构造，参数匹配 AbstractCapabilityExecutor 已有模式——成功路径 degraded=false/degradeReason=null，失败路径 degraded=false/degradeReason=errorCode，移除不存在的 LocalDateTime 参数 |
| [一般] Phase 4 服务类型引用矛盾（主模板用具体类型但不存在） | 构造器参数全部改为 `@Autowired(required = false) Object`；doExecuteInternal 中通过反射 `service.getClass().getMethod("execute", request.getClass()).invoke(service, request)` 调用；涉及文件表备注列改为"Object（编译期不存在，运行时反射）" |
| [轻微] isDtoEmpty 反射策略脆弱（非业务方法干扰） | 替换为 package 前缀检测：`request.getClass().getPackage().getName().startsWith("com.aimedical.modules.ai.api.dto.")`，移除反射方法计数策略 |
