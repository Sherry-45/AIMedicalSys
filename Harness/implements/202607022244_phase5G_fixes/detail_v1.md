# 详细设计（v1）

## 概述

修复启动期阻塞与薄适配器基础问题（T22 + T58 + T1 + T2 + T18 + T34），使应用可正常启动。涉及 6 个薄适配器、7 个底座执行器、1 个父类、1 个健康管理器、2 个新建文件。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-api/src/main/java/.../dto/base/Phase4ServiceFacade.java` | **新建** | Phase 4 服务调用泛型门面接口 |
| `ai-impl/src/main/java/.../config/Phase4ServiceFacadeConfig.java` | **新建** | Spring 配置：为每个薄适配器创建 Phase4ServiceFacade Bean |
| `ai-impl/src/main/java/.../orchestrator/AbstractCapabilityExecutor.java` | **修改** | 移除 Class\<T\> inputType 字段/构造参数；defensiveCopy() 改用 getInputType() |
| `ai-impl/src/main/java/.../metrics/ModelEndpointHealthManager.java` | **修改** | 添加 @Service 注解注册为 Spring Bean |
| `ai-impl/src/main/java/.../orchestrator/impl/TriageCapabilityExecutor.java` | **修改** | 移除 inputType 构造参数 |
| `ai-impl/src/main/java/.../orchestrator/impl/ScheduleCapabilityExecutor.java` | **修改** | 移除 inputType 构造参数 |
| `ai-impl/src/main/java/.../orchestrator/impl/PrescriptionCheckCapabilityExecutor.java` | **修改** | 移除 inputType 构造参数 |
| `ai-impl/src/main/java/.../orchestrator/impl/PrescriptionAssistCapabilityExecutor.java` | **修改** | 移除 inputType 构造参数 |
| `ai-impl/src/main/java/.../orchestrator/impl/MedicalRecordGenCapabilityExecutor.java` | **修改** | 移除 inputType 构造参数 |
| `ai-impl/src/main/java/.../orchestrator/impl/KbQueryCapabilityExecutor.java` | **修改** | 移除 inputType 构造参数 |
| `ai-impl/src/main/java/.../orchestrator/impl/DiscussionConclusionCapabilityExecutor.java` | **修改** | 移除 inputType 构造参数 |
| `ai-impl/src/main/java/.../orchestrator/impl/DiagnosisCapabilityExecutor.java` | **移动+修改** | 移入 thinadapter 子包；Phase4ServiceFacade 注入；llmCallExecutor 注入；isDtoEmpty() 修复 |
| `ai-impl/src/main/java/.../orchestrator/impl/ImageAnalysisCapabilityExecutor.java` | **移动+修改** | 同上 |
| `ai-impl/src/main/java/.../orchestrator/impl/AnalysisReportForLabTestCapabilityExecutor.java` | **移动+修改** | 同上 |
| `ai-impl/src/main/java/.../orchestrator/impl/AnalysisReportForInspectionCapabilityExecutor.java` | **移动+修改** | 同上 |
| `ai-impl/src/main/java/.../orchestrator/impl/RecommendExecutionOrderCapabilityExecutor.java` | **移动+修改** | 同上 |
| `ai-impl/src/main/java/.../orchestrator/impl/RecommendExaminationCapabilityExecutor.java` | **移动+修改** | 同上 |
| `ai-impl/src/main/java/.../orchestrator/impl/AiOrchestrator.java` | **修改**（看情况） | 若包移动后 IDE 自动导入失效，更新导入路径 |

## 类型定义

### Phase4ServiceFacade\<RQ, RS\>
**形态**：interface（泛型）
**包路径**：`com.aimedical.modules.ai.api.dto.base`
**职责**：统一封装 Phase 4 服务调用，替代直接反射

```java
public interface Phase4ServiceFacade<RQ, RS> {
    RS execute(RQ request);
}
```

**公开接口**：
- `RS execute(RQ request)` — 执行 Phase 4 服务调用
**构造方式**：由 `Phase4ServiceFacadeConfig` 通过 lambda 创建
**类型关系**：无继承/实现

### Phase4ServiceFacadeConfig
**形态**：class（@Configuration）
**包路径**：`com.aimedical.modules.ai.impl.config`
**职责**：为 6 个薄适配器各创建一个带类型的 Phase4ServiceFacade Bean，内部封装反射调用

```java
@Configuration
public class Phase4ServiceFacadeConfig {

    @Bean("diagnosisPhase4Service")
    public Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> diagnosisPhase4Service(
            @Autowired(required = false) Object diagnosisService) {
        return request -> invokePhase4Service(diagnosisService, request, DiagnosisResponse.class);
    }

    @Bean("imageAnalysisPhase4Service")
    public Phase4ServiceFacade<ImageAnalysisRequest, ImageAnalysisResponse> imageAnalysisPhase4Service(
            @Autowired(required = false) Object imageAnalysisService) {
        return request -> invokePhase4Service(imageAnalysisService, request, ImageAnalysisResponse.class);
    }

    @Bean("labTestPhase4Service")
    public Phase4ServiceFacade<LabTestReportRequest, LabTestReportResponse> labTestPhase4Service(
            @Autowired(required = false) Object labTestService) {
        return request -> invokePhase4Service(labTestService, request, LabTestReportResponse.class);
    }

    @Bean("inspectionPhase4Service")
    public Phase4ServiceFacade<InspectionReportRequest, InspectionReportResponse> inspectionPhase4Service(
            @Autowired(required = false) Object inspectionService) {
        return request -> invokePhase4Service(inspectionService, request, InspectionReportResponse.class);
    }

    @Bean("executionOrderPhase4Service")
    public Phase4ServiceFacade<ExecutionOrderRequest, ExecutionOrderResponse> executionOrderPhase4Service(
            @Autowired(required = false) Object executionOrderService) {
        return request -> invokePhase4Service(executionOrderService, request, ExecutionOrderResponse.class);
    }

    @Bean("examinationPhase4Service")
    public Phase4ServiceFacade<ExaminationRecommendRequest, ExaminationRecommendResponse> examinationPhase4Service(
            @Autowired(required = false) Object examinationService) {
        return request -> invokePhase4Service(examinationService, request, ExaminationRecommendResponse.class);
    }

    @SuppressWarnings("unchecked")
    private <RQ, RS> RS invokePhase4Service(Object service, RQ request, Class<RS> responseType) {
        if (service == null) {
            throw new IllegalStateException("Phase4Service unavailable");
        }
        try {
            Class<?> requestClass = request.getClass();
            return (RS) service.getClass()
                .getMethod("execute", requestClass)
                .invoke(service, request);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

**公开接口**：6 个 @Bean 方法 + 1 个 private 辅助方法
**构造方式**：Spring 自动扫描 @Configuration
**类型关系**：依赖 `ai-api` DTO 类型 + Phase 4 服务 Object

### AbstractCapabilityExecutor<T, R> 修改
**形态**：abstract class（修改）
**包路径**：`com.aimedical.modules.ai.impl.orchestrator`

**变更**：
1. 移除第 61 行 `protected final Class<T> inputType;` 字段
2. 构造参数从 17 个缩减为 16 个：删除 `Class<T> inputType` 参数及对应赋值（第 80-116 行）
3. `defensiveCopy()` 第 197-199 行：`objectMapper.convertValue(request, inputType)` 改为 `objectMapper.convertValue(request, getInputType())`

### ModelEndpointHealthManager 修改
**形态**：class（修改）
**包路径**：`com.aimedical.modules.ai.impl.metrics`

**变更**：第 8 行 `public class ModelEndpointHealthManager` 添加 `@Service` 注解

### 底座执行器（7 个）修改
**形态**：class（修改）
**包路径**：`com.aimedical.modules.ai.impl.orchestrator.impl`

**变更**（以 TriageCapabilityExecutor 为例）：
- 移除构造参数中第 1 个 `Class<TriageRequest> inputType` 及对应的 `super()` 实参
- 其余参数不变，`super()` 对应位置取消

**涉及**：TriageCapabilityExecutor、ScheduleCapabilityExecutor、PrescriptionCheckCapabilityExecutor、PrescriptionAssistCapabilityExecutor、MedicalRecordGenCapabilityExecutor、KbQueryCapabilityExecutor、DiscussionConclusionCapabilityExecutor

### 薄适配器（6 个）修改
**形态**：class（移动+修改）
**原包路径**：`com.aimedical.modules.ai.impl.orchestrator.impl`
**新包路径**：`com.aimedical.modules.ai.impl.thinadapter`

**以 DiagnosisCapabilityExecutor 为例的最终形态**：

```java
@Service("DIAGNOSIS")
public class DiagnosisCapabilityExecutor extends AbstractCapabilityExecutor<DiagnosisRequest, DiagnosisResponse> {

    private static final Logger log = LoggerFactory.getLogger(DiagnosisCapabilityExecutor.class);

    private final Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service;

    @Autowired
    public DiagnosisCapabilityExecutor(
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service,
        AiMetricsCollector metricsCollector,
        SlidingWindowMetricsStore metricsStore,
        @Qualifier("capabilityTimeoutConfig") AtomicReference<Map<String, Duration>> capabilityTimeoutConfig,
        @Qualifier("parseTimeoutConfig") AtomicReference<Map<String, Duration>> parseTimeoutConfig,
        AtomicReference<Duration> parseTimeoutDefault,
        @Value("${ai.execution.timeout.thin-adapter-default:30s}") Duration thinAdapterTimeout,
        @Qualifier("thinAdapterPerCapabilityConfig") AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig,
        Executor llmCallExecutor,
        ObjectMapper objectMapper
    ) {
        super(null, null, null, null,
              metricsCollector, metricsStore, null, null, null,
              capabilityTimeoutConfig, parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout,
              thinAdapterPerCapabilityConfig, llmCallExecutor, objectMapper);
        this.phase4Service = phase4Service;
    }

    @Override public String getCapabilityId() { return "DIAGNOSIS"; }
    @Override public Class<DiagnosisRequest> getInputType() { return DiagnosisRequest.class; }
    @Override public Class<DiagnosisResponse> getOutputType() { return DiagnosisResponse.class; }
    // ... doExtractDepartmentId/VisitId/PatientId/SessionId 保持不变 ...

    @Override
    protected AiResult<DiagnosisResponse> doExecuteInternal(...) {
        if (isDtoEmpty(request)) {
            return doDegrade(...);
        }
        // Phase 4 service check removed — Phase4ServiceFacade handle null internally
        try {
            long resolvedTimeout = resolveThinAdapterTimeout(capabilityId);
            CompletableFuture<DiagnosisResponse> delegateFuture = CompletableFuture.supplyAsync(
                () -> phase4Service.execute(request), llmCallExecutor);
            DiagnosisResponse result = delegateFuture.get(resolvedTimeout, TimeUnit.MILLISECONDS);
            // ... metrics recording and return ...
        } catch (TimeoutException e) { ... }
          catch (ExecutionException e) { ... }
          catch (Exception e) { ... }
    }

    private boolean isDtoEmpty(DiagnosisRequest request) {
        String pkg = request.getClass().getPackage().getName();
        return knownPhase4Packages.stream().anyMatch(pkg::startsWith);
    }
}
```

**各薄适配器 CapabilityId 与 Bean 名称对应**：

| 薄适配器 | CapabilityId | 原 service 字段 | Phase4ServiceFacade 字段 | @Bean 名称 |
|---------|-------------|-----------------|-------------------------|-----------|
| DiagnosisCapabilityExecutor | DIAGNOSIS | diagnosisService | phase4Service | diagnosisPhase4Service |
| ImageAnalysisCapabilityExecutor | IMAGE_ANALYSIS | imageAnalysisService | phase4Service | imageAnalysisPhase4Service |
| AnalysisReportForLabTestCapabilityExecutor | ANALYSIS_REPORT_LABTEST | labTestService | phase4Service | labTestPhase4Service |
| AnalysisReportForInspectionCapabilityExecutor | ANALYSIS_REPORT_INSPECTION | inspectionService | phase4Service | inspectionPhase4Service |
| RecommendExecutionOrderCapabilityExecutor | RECOMMEND_EXEC_ORDER | executionOrderService | phase4Service | executionOrderPhase4Service |
| RecommendExaminationCapabilityExecutor | RECOMMEND_EXAM | examinationService | phase4Service | examinationPhase4Service |

## 错误处理

- `Phase4ServiceFacadeConfig.invokePhase4Service()` 中 `service == null` 抛出 `IllegalStateException`，由调用方 `doExecuteInternal` 的 catch 捕获后转为降级
- 反射调用异常通过 `InvocationTargetException → RuntimeException` 传播，由 `doExecuteInternal` 的 `ExecutionException` 分支处理
- `ModelEndpointHealthManager` 无特殊错误处理变化，仅添加 `@Service` 注解

## 行为契约

1. **T22**：`AbstractCapabilityExecutor` 移除 `inputType` 字段后，子类必须覆盖 `getInputType()` 返回具体类型字面量（已有 13 个子类均覆盖），`defensiveCopy()` 会通过 `getInputType()` 获取
2. **T58**：`ModelEndpointHealthManager` 添加 `@Service` 后，Spring 自动扫描创建单例，`AbstractCapabilityExecutor` 构造注入正常解析；不影响已有构造器注入点（底座 7 个 + 讨论结论 1 个已注入）
3. **T1**：`isDtoEmpty()` 逻辑翻转后，ai-api DTO 不再被错误判定为空，仅当实际 Phase 4 原生 DTO 进入时才触发降级
4. **T2**：所有反射调用集中到 `Phase4ServiceFacadeConfig.invokePhase4Service()`，薄适配器仅通过类型安全的 `Phase4ServiceFacade.execute(request)` 调用
5. **T18**：包移动后 `@SpringBootApplication(scanBasePackages = "com.aimedical")` 自动覆盖新包，无需额外组件扫描配置
6. **T34**：`CompletableFuture.supplyAsync(task, llmCallExecutor)` 确保薄适配器委托调用使用底座专用线程池而非 `ForkJoinPool.commonPool()`

## 依赖关系

- `Phase4ServiceFacade` 放在 `ai-api` 模块的 `dto/base/` 包中，与 `Phase4BusinessException` 同级（`ai-api` 无 `ai-impl` 依赖）
- `Phase4ServiceFacadeConfig` 放在 `ai-impl` 模块的 `config/` 包中，导入 `ai-api` 的 DTO 类型和 `Phase4ServiceFacade`
- 6 个薄适配器从 `orchestrator/impl/` 移至 `thinadapter/`，保持 `extends AbstractCapabilityExecutor<Req, Res>` 和 `@Service("CAPABILITY_ID")` 不变
- `AiOrchestrator` 通过 `List<CapabilityExecutor<?, ?>> executorList` 自动收集所有执行器，包移动后 Spring 扫描新包自动注入，无需修改 `AiOrchestrator` 导入（其导入仅为 DTO 类型和 CapabilityExecutor 接口，不涉及执行器具体类）
