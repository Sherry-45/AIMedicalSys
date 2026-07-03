# 任务指令（v1 r1）

## 动作
NEW

## 任务描述
修复启动期阻塞与薄适配器基础问题：T22 + T58 + T1 + T2 + T18 + T34，使应用可正常启动。

### T22: 底座7项CapabilityExecutor构造器参数Class\<T\> inputType无法被Spring自动注入
**涉及文件：**
- `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` — 父类构造器、defensiveCopy() 方法
- 以下7个底座执行器（各移除inputType构造参数）：
  - `ai-impl/.../orchestrator/impl/TriageCapabilityExecutor.java`
  - `ai-impl/.../orchestrator/impl/ScheduleCapabilityExecutor.java`
  - `ai-impl/.../orchestrator/impl/PrescriptionCheckCapabilityExecutor.java`
  - `ai-impl/.../orchestrator/impl/PrescriptionAssistCapabilityExecutor.java`
  - `ai-impl/.../orchestrator/impl/MedicalRecordGenCapabilityExecutor.java`
  - `ai-impl/.../orchestrator/impl/KbQueryCapabilityExecutor.java`
  - `ai-impl/.../orchestrator/impl/DiscussionConclusionCapabilityExecutor.java`

**修复方案：**
1. `AbstractCapabilityExecutor` 中移除 `Class<T> inputType` 构造参数和 `this.inputType = inputType` 赋值
2. `defensiveCopy()` 方法中将 `inputType` 引用改为调用 `getInputType()` 获取类型
3. 上述7个子类移除构造参数 `Class<T> inputType` 和对应的 `super()` 实参

**注意：** 6个薄适配器已经对 inputType 传 null，本修复后因 defensiveCopy() 改用 getInputType()，薄适配器也将正确获得具体类型。

### T58: ModelEndpointHealthManager未注册Spring Bean
**涉及文件：**
- `ai-impl/.../metrics/ModelEndpointHealthManager.java`

**修复方案：**
为 `ModelEndpointHealthManager` 添加 `@Service` 注解使其成为 Spring 托管 Bean，确保所有 CapabilityExecutor 的构造注入可解析。

### T1: 薄适配器 isDtoEmpty() 包路径匹配恒真
**涉及文件：**
- 6个薄适配器（DiagnosisCapabilityExecutor, ImageAnalysisCapabilityExecutor, AnalysisReportForLabTestCapabilityExecutor, AnalysisReportForInspectionCapabilityExecutor, RecommendExecutionOrderCapabilityExecutor, RecommendExaminationCapabilityExecutor）

**修复方案：**
修复 `isDtoEmpty()` 方法：当前逻辑检查 `pkg.startsWith("com.aimedical.modules.ai.api.dto.")` 但所有DTO均在此包下故恒真。应改为使用 `knownPhase4Packages` 静态集合（已在 `AbstractCapabilityExecutor` 中定义为 `Set.of("com.aimedical.modules.diagnosis", "com.aimedical.modules.inspection", ...)`）作为判断依据，检查请求对象的包路径是否属于 Phase 4 包。

### T2: 薄适配器使用反射调用 Phase 4 服务 —— 改为 Phase4ServiceFacade 统一封装
**涉及文件：**
- `ai-api/.../dto/base/Phase4ServiceFacade.java`（新建泛型接口）
- `ai-impl/.../config/Phase4ServiceFacadeConfig.java`（新建 Spring 配置类）
- 6个薄适配器（DiagnosisCapabilityExecutor 等）

**背景：** 审查发现代码库中不存在 Phase 4 Service 接口（如 `DiagnosisService`），Maven 也不依赖 Phase 4 模块，原"直接类型引用"方案不可行。

**修复方案：**
1. **新建 `Phase4ServiceFacade<RQ, RS>` 泛型接口**（放在 `ai-api` 模块 `dto/base/` 包中，与 Phase4BusinessException 同包）：
   ```java
   package com.aimedical.modules.ai.api.dto.base;

   public interface Phase4ServiceFacade<RQ, RS> {
       RS execute(RQ request);
   }
   ```

2. **新建 `Phase4ServiceFacadeConfig` Spring 配置类**（放在 `ai-impl` 模块 `config/` 包中），为每个薄适配器创建类型安全的 `Phase4ServiceFacade` Bean：
   ```java
   @Configuration
   public class Phase4ServiceFacadeConfig {
       @Bean("diagnosisPhase4Service")
       public Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> diagnosisPhase4Service(
               @Autowired(required = false) Object diagnosisService) {
           return request -> invokePhase4Service(diagnosisService, request, DiagnosisResponse.class);
       }
       // ... 其余5个薄适配器类似
   }
   ```
   `invokePhase4Service` 辅助方法封装当前反射逻辑（`getMethod("execute", requestClass).invoke(service, request)`），集中在一处。

3. **修改6个薄适配器**：将构造器参数从 `@Autowired(required = false) Object xxxService` 替换为 `Phase4ServiceFacade<XxxRequest, XxxResponse> xxxService`；`doExecuteInternal()` 中的反射调用替换为 `xxxService.execute(request)` 直接调用。

### T18: 薄适配器目录结构与设计文档不一致
**涉及文件：**
- 6个薄适配器（需从 `orchestrator/impl/` 移至 `thin-adapter/` 子包）

**修复方案：**
1. 新建 `thin-adapter/` 子包目录
2. 将6个薄适配器类移动至新包，更新 `package` 声明
3. `@Service("CAPABILITY_ID")` 的 capabilityId 值保持不变
4. 更新Spring组件扫描配置以包含新包路径
5. 更新 `AiOrchestrator` 及其他引用处的导入路径
6. 保持 `extends AbstractCapabilityExecutor<XxxRequest, XxxResponse>` 不变

### T34: 薄适配器 CompletableFuture.supplyAsync() 使用默认 ForkJoinPool.commonPool()
**涉及文件：**
- 6个薄适配器

**修复方案：**
将 `CompletableFuture.supplyAsync(() -> {...})` 改为 `CompletableFuture.supplyAsync(() -> {...}, llmCallExecutor)`，使用底座专用线程池。从父类 `AbstractCapabilityExecutor` 获取 `protected final Executor llmCallExecutor` 字段。构造器中不再对 super() 传 null，而是注入 `Executor llmCallExecutor` 参数。

### 统一构造器设计（T22 + T18 + T2 + T34 叠加）
6个薄适配器在以上四项改动后的最终统一构造器形态（以 DiagnosisCapabilityExecutor 为例）：
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

    @Override
    public String getCapabilityId() { return "DIAGNOSIS"; }
    @Override
    public Class<DiagnosisRequest> getInputType() { return DiagnosisRequest.class; }
    @Override
    public Class<DiagnosisResponse> getOutputType() { return DiagnosisResponse.class; }
}
```
- T22: `super()` 从17参数减为16参数（inputType 已移除）
- T18: `package` 从 `orchestrator/impl` 改为 `thin-adapter`，`@Service("DIAGNOSIS")` 保持不变
- T2: `Object diagnosisService` → `Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service`
- T34: `llmCallExecutor` 注入并传给 `super()`（不再传 null）
- 其余5个薄适配器同理，仅 `Phase4ServiceFacade` 泛型参数和 `getCapabilityId()` 返回值不同

## 选择理由
启动期阻塞问题与薄适配器层基础修复优先级最高——应用无法启动则其余所有修复均无意义。T22 阻止所有7+6=13个 CapabilityExecutor 的 Bean 创建，T58 阻止所有依赖它的执行器，T1 导致所有请求错误降级。薄适配器相关6个问题（T1/T2/T18/T34）强相关且修改同一组文件，合并处理减少上下文切换。根据审查反馈，T2 方案已从"直接引用 Phase 4 接口"修正为"创建 Phase4ServiceFacade 统一封装"方案以解决 Phase 4 接口不存在的问题。

## 任务上下文
### 需求约束
- T22: 每个子类已覆盖 `getInputType()` 返回具体类字面量（如 `TriageRequest.class`），但父类 `defensiveCopy()` 依赖于字段 `inputType` 而非方法 `getInputType()`。Spring 无法自动注入 `Class<T>` 类型的参数。
- T58: `ModelEndpointHealthManager` 是一个无任何注解的 POJO，类定义仅为 `public class ModelEndpointHealthManager { ... }`，没有 `@Service`/`@Component` 注解。
- T1: `isDtoEmpty()` 检查包路径 `com.aimedical.modules.ai.api.dto` 但所有请求DTO都在此包下，恒真返回true。应改为检查 Phase4 包路径。`AbstractCapabilityExecutor` 中已有 `knownPhase4Packages` 静态集合可直接复用。
- T2: 审查确认代码库中不存在 Phase 4 Service 接口（如 `DiagnosisService`），Maven 不依赖 Phase 4 模块。采用新建 `Phase4ServiceFacade` 接口+ `Phase4ServiceFacadeConfig` 配置类的方案，将反射封装在配置类中。
- T18: 6个薄适配器当前位于 `orchestrator/impl/`，设计文档要求归属 `thin-adapter/` 子包。
- T34: `CompletableFuture.supplyAsync()` 无Executor参数默认使用 `ForkJoinPool.commonPool()`，应与底座执行器一致使用 `llmCallExecutor`。

### 现有代码上下文
- `AbstractCapabilityExecutor` 第48-116行：类定义为抽象类，构造器接收17个参数，其中第一个为 `Class<T> inputType`。`defensiveCopy()` 第197-203行使用 `objectMapper.convertValue(request, inputType)` 做防御性拷贝。
- 7个底座子类（如 `TriageCapabilityExecutor` 第34-52行）构造器均以 `Class<XxxRequest> inputType` 作为第一个参数并传递给 `super()`。
- 6个薄适配器（如 `DiagnosisCapabilityExecutor` 第38-52行）构造器对 `inputType` 传 `null`，注入 `@Autowired(required = false) Object` 泛型服务引用。
- `ModelEndpointHealthManager` 第1-121行：纯 POJO，无任何 Spring 注解。
- `DiagnosisCapabilityExecutor` 第167-170行：`isDtoEmpty()` 方法检查 `request.getClass().getPackage().getName().startsWith("com.aimedical.modules.ai.api.dto.")`。
- `DiagnosisCapabilityExecutor` 第107-123行：反射调用 `service.getClass().getMethod("execute", requestClass).invoke(service, request)`。
- `DiagnosisCapabilityExecutor` 第107行：`CompletableFuture.supplyAsync(() -> {...})` 未传 Executor。

---

## 修订说明（v1 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] T2 修复方案不可行：Phase 4 Service 接口不存在，无法实现直接类型引用 | T2 方案重构：新建 `Phase4ServiceFacade<RQ, RS>` 泛型接口（ai-api）+ `Phase4ServiceFacadeConfig` 配置类（ai-impl）将反射封装其中，薄适配器注入类型安全的 `Phase4ServiceFacade`。已在任务描述和统一构造器设计中体现 |
| [一般] T22/T18/T2 对薄适配器构造器的叠加修改存在合并冲突风险 | 新增"统一构造器设计"章节，给出了薄适配器在 T22+T18+T2+T34 四项改动后的完整构造器签名、super() 参数映射、以及各字段说明 |
| [格式] 实施路线表列为64行任务级别而非10~15行轮次级别 | 路线表重构为12行轮次（plan.md）。本任务文件（R1）内容不变 |

## 修订说明（v1 r3）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] 路线表中 R1/R2 任务分配与实际合并范围不一致 | 路线表已于 plan.md 中修复：R1 合并为一行涵盖全部6项任务，删除原 R2 行，后续轮次重新编号。本任务文件（R1）内容本身已正确涵盖全部6项，无需修改 |
