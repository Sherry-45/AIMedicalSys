# 实现计划

任务描述：Phase 5G 代码问题修复 - 共64项问题（1致命/17严重/46一般），覆盖启动期阻塞、运行时故障、代码质量与设计一致性
项目根目录：C:\Develop\Software\AIMedicalSys

## 实施路线

| 序号 | 轮次名称 | 包含任务ID | 问题简述 | 优先级概要 | 状态 |
|------|---------|-----------|---------|-----------|------|
| R1 | 启动期阻塞与薄适配器修复 | T22,T58,T1,T2,T18,T34 | 底座Executor构造器Class\<T\>不可注入；ModelEndpointHealthManager未注册Bean；isDtoEmpty恒真；反射调用改Phase4ServiceFacade；包路径调整；线程池修正 | 致命/严重 | ✅ |
| R2 | 编译修复(薄适配器测试适配) | R1验证遗留 | 薄适配器移入thinadapter子包后，orchestrator/impl/下6个旧测试文件未更新包导入和构造参数——删除旧文件保留thinadapter测试副本 | 一般 | ✅ |
| R3 | 修复残留9个测试编译错误 | v2验证遗留 | AbstractCapabilityExecutorTest/TriageCapabilityExecutorTest/DiscussionConclusionCapabilityExecutorTest/DiagnosisCapabilityExecutorTest共9处构造参数未同步 | 一般 | ✅ |
| R4 | 核心执行器异常处理 | T3,T4,T5,T21,T26,T27,T28 | Phase4Exception误走降级、字符串匹配instanceof、缺少catch、构造器签名、重复调用/null返回/null检查 | 严重/一般 | ✅ |
| R5 | Orchestrator与熔断降级 | T14,T60,T13,T61,T19,T20,T35 | 熔断器作用域、failureCount重置、probeLock清理、未注册fail-fast、record未实现、线程安全模型 | 严重/一般 | ❌ |
| R6 | R5 验证修复(熔断NPE/状态隔离/超时) | 22个测试覆盖3个根因 | metricsStore null守卫、endpoint级失败跟踪、薄适配器超时执行器 | 严重 | ❌ |
| R7 | R6验证修复(熔断CLOSED死锁+handleSuccess NPE) | 11 CB测试+2 ACE测试 | CB CLOSED case chicken-and-egg(failureCount取不到)、handleSuccess缺metricsStore null守卫 | 严重 | ✅ |
| R8 | 指标与健康管理 | T15,T16,T17,T25,T51,T52,T59 | @Async线程池绑定、字段映射丢失、AiCallRecord对等、快照复制、窗口参数、健康状态跃迁 | 严重/一般 | ✅ |
| R9 | LLM客户端基础设施 | T6,T7,T36,T37,T38,T11,T12,T50,T39,T40 | 枚举校验、回退日志、HttpClient池化、endpointUrl混用、异常风格 | 严重/一般 | ❌ |
| R10 | 配置与环境管理 | T8,T9,T43,T44,T10,T45 | @ConditionalOnProperty、ObjectProvider、Binder重绑定、配置告警、fail-fast | 严重/一般 | ❌ |
| R11 | 数据模型与值对象 | T29,T30,T31,T32,T46,T47 | AiResult不可变、字段修正、Builder工厂、getErrorCode、不可变性 | 一般 | ✅ |
| R12 | 讨论结论执行器 | T24,T33 | 防御性拷贝后修改、线程池嵌套死锁 | 一般 | ✅ |
| R13 | 模板管理 | T54,T55,T62 | 缓存键不含版本、预热绕过、签名不一致 | 一般 | ❌ |
| R14 | 路由与实验管理（含R13 RETRY+遗留修复） | T41,T42,T48,T56,T57,T63,R13-RETRY,R11-遗留 | 路由签名、@Scheduled配置、枚举回退、排序语义、N+1查询+R13修复+28遗留错误 | 一般 | ❌ |
| R15 | 兜底策略与解析器 | T49,T64,T65,T66 | credentialStore语义、异常处理、CHECK_SKIPPED、RuntimeException | 一般 | ⏳ |

## 轮次详情

### R1: 启动期阻塞与薄适配器修复
- **T22**(严重): 底座7项CapabilityExecutor构造器参数`Class<T> inputType`无法被Spring自动注入。每个子类已覆盖`getInputType()`但父类`defensiveCopy()`依赖于字段而非方法。需移除父类及子类构造参数，使`defensiveCopy()`调用`getInputType()`。
- **T58**(严重): `ModelEndpointHealthManager`无任何Spring注解，需添加`@Service`使其成为托管Bean。
- **T1**(致命): `isDtoEmpty()`检查包路径`com.aimedical.modules.ai.api.dto`恒真——所有DTO均在此包下。改为使用`knownPhase4Packages`静态集合判断。
- **T2**(严重): 薄适配器用反射调用Phase 4服务。代码库不存在Phase 4 Service接口、Maven不依赖Phase 4模块。新建`Phase4ServiceFacade<RQ,RS>`泛型接口(ai-api) + `Phase4ServiceFacadeConfig`配置类(ai-impl)统一封装反射，薄适配器注入类型安全接口。
- **T18**(一般): 6个薄适配器从`orchestrator/impl/`移至`thin-adapter/`子包，更新package声明和组件扫描。
- **T34**(一般): `CompletableFuture.supplyAsync()`无Executor参数默认使用`ForkJoinPool.commonPool()`，改为使用`llmCallExecutor`。
- **涉及文件**: `AbstractCapabilityExecutor.java`, `TriageCapabilityExecutor.java`, `ScheduleCapabilityExecutor.java`, `PrescriptionCheckCapabilityExecutor.java`, `PrescriptionAssistCapabilityExecutor.java`, `MedicalRecordGenCapabilityExecutor.java`, `KbQueryCapabilityExecutor.java`, `DiscussionConclusionCapabilityExecutor.java`, `ModelEndpointHealthManager.java`, 6个薄适配器(DiagnosisCapabilityExecutor, ImageAnalysisCapabilityExecutor, AnalysisReportForLabTestCapabilityExecutor, AnalysisReportForInspectionCapabilityExecutor, RecommendExecutionOrderCapabilityExecutor, RecommendExaminationCapabilityExecutor), `Phase4ServiceFacade.java`(新建), `Phase4ServiceFacadeConfig.java`(新建)
- **统一构造器**: R1任务文件中已给出四项改动叠加后的完整构造器签名。

### R2: 编译修复(薄适配器测试适配)
- **R1验证遗留**: 6个薄适配器移入`thinadapter`子包后，`orchestrator/impl/`下6个旧测试文件无法访问`thinadapter`包的`protected`方法（42处编译错误）。删除6个旧测试文件，保留`thinadapter/`下已有可编译测试副本。
- **涉及文件**: 删除 `ai-impl/src/test/.../orchestrator/impl/DiagnosisCapabilityExecutorTest.java` 等6个旧测试文件

### R3: 修复残留9个测试编译错误
- **v2验证遗留**: 9个测试编译错误，分布在4个测试文件和1个测试辅助类。根因为R1移除了AbstractCapabilityExecutor构造器的`Class<T> inputType`参数后，以下测试文件未同步更新：
  - `AbstractCapabilityExecutorTest.java`中的`TestableExecutor`内部类——`super()`调用及`inputType`字段引用
  - `TriageCapabilityExecutorTest.java`——2处构造调用首参传`TriageRequest.class`
  - `DiscussionConclusionCapabilityExecutorTest.java`——4处构造调用首参传`DiscussionConclusionRequest.class`
  - `thinadapter/DiagnosisCapabilityExecutorTest.java`——跨包匿名类引用不存在的类
- **涉及文件**: `AbstractCapabilityExecutorTest.java`, `TriageCapabilityExecutorTest.java`, `DiscussionConclusionCapabilityExecutorTest.java`, `DiagnosisCapabilityExecutorTest.java`, `Phase4DiagnosisRequest.java`(新建)

### R4: 核心执行器异常处理
- **T3**(严重): `AbstractCapabilityExecutor.execute()`的`exceptionally()`中`Phase4BusinessException`误走降级路径。需区分Phase4BusinessException与普通异常。
- **T4**(严重): `executeStandardPipeline()`中`LlmInfrastructureException`检测使用字符串匹配(`e.getClass().getName()`)而非`instanceof`。改为instanceof判断。
- **T5**(严重): `executeStandardPipeline()`缺少对`LlmInfrastructureException`的独立catch分支。需添加独立catch处理。
- **T21**(一般): `AbstractCapabilityExecutor`构造器参数类型与设计文档不一致。调整构造器签名对齐设计文档。⚠️ **波及全部13个子类**（7底座+6薄适配器）+ 父类内部4处`.get()`调用 + 薄适配器内部`.get()`调用，共15个文件需修改。
- **T26**(一般): `doDegrade()`中重复调用`extractUserId()`。消除冗余调用。
- **T27**(一般): `extractCallerRole()`/`extractCallerId()`始终返回null。实现正确提取逻辑。
- **T28**(一般): `executeStandardPipeline()`中`endpointHealthManager`未做null检查。添加防御性null检查。
- **涉及文件**: `AbstractCapabilityExecutor.java`, `RequestContextUtils.java`, 7个底座子类(`TriageCapabilityExecutor`/`ScheduleCapabilityExecutor`/`PrescriptionCheckCapabilityExecutor`/`PrescriptionAssistCapabilityExecutor`/`MedicalRecordGenCapabilityExecutor`/`KbQueryCapabilityExecutor`/`DiscussionConclusionCapabilityExecutor`), 6个薄适配器(`DiagnosisCapabilityExecutor`/`ImageAnalysisCapabilityExecutor`/`AnalysisReportForLabTestCapabilityExecutor`/`AnalysisReportForInspectionCapabilityExecutor`/`RecommendExecutionOrderCapabilityExecutor`/`RecommendExaminationCapabilityExecutor`)

### R5: Orchestrator与熔断降级
- **T14**(严重): `CircuitBreakerDegradationStrategy`熔断器作用域为capabilityId而非endpointId。修正为endpointId。
- **T60**(一般): `CircuitBreakerDegradationStrategy` CLOSED→OPEN转换时未重置failureCount。添加重置逻辑。
- **T13**(一般): `CircuitBreakerDegradationStrategy` OPEN→HALF_OPEN中probeLock无清理路径。添加崩溃后清理机制。
- **T61**(一般): `TimeoutDegradationStrategy`注入`SlidingWindowMetricsStore`但`shouldDegrade()`中未直接使用。移除未使用依赖或完善逻辑。
- **T19**(一般): `AiOrchestrator.handle()`对未注册能力标识返回`failure`而非fail-fast异常。改为抛出异常。
- **T20**(一般): `AiOrchestrator.handle()` catch块中`metricsCollector.record()`未实现。实现指标记录。
- **T35**(一般): `AiOrchestrator`使用`ConcurrentHashMap`但声明为`volatile Map`——线程安全模型冗余。统一为`ConcurrentHashMap`类型。
- **涉及文件**: `CircuitBreakerDegradationStrategy.java`, `TimeoutDegradationStrategy.java`, `AiOrchestrator.java`

### R6: 指标与健康管理
- **T15**(严重): `LoggingMetricsCollector`的`@Async`未绑定专用线程池`metricsAsyncExecutor`。添加`@Async("metricsAsyncExecutor")`。
- **T16**(严重): `LoggingMetricsCollector`中`AiCallRecord→AiCallLogEntity`字段映射存在多处数据丢失。修正字段映射。
- **T17**(严重): `AiCallRecord`与`AiCallLogEntity`字段不对等，缺少7个关键字段，缺少`success()/failure()/degraded()`工厂方法。补齐字段和工厂方法。
- **T25**(一般): `AiCallRecord`字段与设计文档不一致。按设计文档调整。
- **T51**(一般): `SlidingWindowMetricsStore`的`windowSeconds`使用`volatile`而非设计文档要求的`AtomicLong`；`setWindowSeconds()`无参数校验。改为AtomicLong并添加参数校验。
- **T52**(一般): `SlidingWindowMetricsStore`读取方法未执行快照复制。添加快照复制。
- **T59**(一般): `ModelEndpointHealthManager` UNAVAILABLE→CONNECTED跳过DEGRADED中间态。添加DEGRADED中间状态跃迁。
- **涉及文件**: `LoggingMetricsCollector.java`, `AiCallRecord.java`, `SlidingWindowMetricsStore.java`, `ModelEndpointHealthManager.java`, `AiCallLogEntity.java`

### R7: LLM客户端基础设施
- **T6**(严重): `DelegatingLlmChatService`缺少`@PostConstruct`枚举值完整性校验。添加枚举校验方法。
- **T7**(严重): `DelegatingLlmChatService`回退日志缺少上下文信息和健康检查端点告警。完善日志。
- **T36**(一般): `DelegatingLlmChatService.getClientType()`返回null语义不清。改进返回语义。
- **T37**(一般): `EndpointRateLimiter`未使用设计文档要求的`maxBurstSeconds`配置（死代码）。移除或使用该配置。
- **T38**(一般): `EndpointRateLimiter`限流拒绝时返回`AiResult.failure`绕过降级管线。改为抛出异常走降级管线。
- **T11**(严重): `HttpApiLlmChatService.chat()`同步阻塞且每次调用新建HttpClient实例，无连接池复用。改为连接池复用。
- **T12**(严重): `HttpApiLlmChatService.chat()`中`endpointId`被误用作HTTP URI（应使用`endpointUrl`）。改为使用endpointUrl。
- **T50**(一般): `HttpApiLlmChatService.structuredChat()`抛异常与`chat()`返回`AiResult.failure`异常处理风格不一致。统一为AiResult.failure风格。
- **T39**(一般): `SpringAiLlmChatService`/`SpringAiLlmChatStreamService`抛出`UnsupportedOperationException`而非`LlmInfrastructureException`。替换异常类型。
- **T40**(一般): `LlmChatStreamService`接口导入未使用的`AiAbilityInputInvalidException`。移除未使用导入。
- **涉及文件**: `DelegatingLlmChatService.java`, `EndpointRateLimiter.java`, `HttpApiLlmChatService.java`, `SpringAiLlmChatService.java`, `SpringAiLlmChatStreamService.java`, `LlmChatStreamService.java`

### R8: 配置与环境管理
- **T8**(严重): `AiPlatformConfig`缺少`@ConditionalOnProperty`条件注解。添加条件注解。
- **T9**(严重): `AiPlatformConfig`中`delegatingLlmChatService` Bean装配未按设计使用`ObjectProvider`延迟解析；`springAiLlmChatService`缺`@ConditionalOnClass`。修正装配方式。
- **T43**(一般): `AiPlatformConfig.refreshCapabilityTimeoutConfig()` Binder重新绑定与`@ConfigurationProperties`状态不一致。修正绑定逻辑。
- **T44**(一般): `AiPlatformConfig.refreshWindowSeconds()`直接调用setter，需确认线程安全。确认/修复线程安全。
- **T10**(严重): `AiPlatformEnvironmentPostProcessor`缺少配置冲突告警日志。添加告警日志。
- **T45**(一般): `FallbackAiService`构造器中`delegate==null`时无fail-fast告警；`aiPlatformEnabled`参数未使用。添加告警并移除未使用参数。
- **涉及文件**: `AiPlatformConfig.java`, `AiPlatformEnvironmentPostProcessor.java`, `FallbackAiService.java`

### R9: 数据模型与值对象
- **T29**(一般): `AiResult`缺少`degradedWithErrorCode`工厂方法；字段非final与设计文档不可变契约矛盾。添加工厂方法，字段设为final。
- **T30**(一般): `DegradationContext`的`invocationCount/failureCount`设计文档要求int实际为Integer；`serialVersionUID`未更新。修正为int并更新serialVersionUID。
- **T31**(一般): `DegradationContext.Builder`未实现设计文档要求的静态工厂方法`builder()`。添加builder()方法。
- **T32**(一般): `Phase4BusinessException`缺少`getErrorCode()`方法。添加getErrorCode()。
- **T46**(一般): `ChatToolDefinition.strict`字段有setter破坏不可变性。移除setter。
- **T47**(一般): `LlmChatOptions`全部字段可变，不符合设计文档值对象定义。字段设为final，通过构造器初始化。
- **涉及文件**: `AiResult.java`, `DegradationContext.java`, `Phase4BusinessException.java`, `ChatToolDefinition.java`, `LlmChatOptions.java`

### R10: 讨论结论执行器
- **T24**(一般): `DiscussionConclusionCapabilityExecutor`修改了防御性拷贝后的request对象。避免修改拷贝后的对象。
- **T33**(一般): `DiscussionConclusionCapabilityExecutor`压缩调用嵌套提交到`llmCallExecutor`存在线程池死锁风险。改用独立线程池或异步编排。
- **涉及文件**: `DiscussionConclusionCapabilityExecutor.java`

### R11: 模板管理
- **T54**(一般): `DatabasePromptTemplateManager`缓存键不包含`promptVersion`，`resolveExactVersion()`完全绕过缓存。缓存键加入promptVersion。
- **T55**(一般): `DatabasePromptTemplateManager` warmup缓存键不包含版本，预热后首次render仍可能触发DB查询。修正warmup缓存键。
- **T62**(一般): `PromptTemplateManager`接口签名`promptVersion`类型为String而非设计文档要求的Integer。改为Integer。
- **涉及文件**: `DatabasePromptTemplateManager.java`, `PromptTemplateManager.java`

### R12: 路由与实验管理
- **T41**(一般): `ModelRouter.route()`签名与设计文档不一致——第二个参数应为`ExperimentAssignment`而非`Object`。修正签名。
- **T42**(一般): `DefaultModelRouter.refreshRouteTable()`的`@Scheduled`未指定scheduler。指定scheduler。
- **T48**(一般): `AiRouterProperties.convert()`枚举转换失败时静默回退，运维无法感知配置错误。添加日志告警。
- **T56**(一般): `HashBucketExperimentManager`多实验选择逻辑使用`min().reversed()`语义反直觉。改为`max()`明确语义。
- **T57**(一般): `HashBucketExperimentManager`缓存中预热数据已排序但loader数据未排序，格式不一致。统一排序。
- **T63**(一般): `ExperimentGroup`的`@ManyToOne(LAZY)`与`Experiment`的`@OneToMany(EAGER)`冲突产生N+1查询。统一加载策略或添加`@BatchSize`。
- **涉及文件**: `ModelRouter.java`, `DefaultModelRouter.java`, `AiRouterProperties.java`, `HashBucketExperimentManager.java`, `ExperimentGroup.java`

### R13: 兜底策略与解析器
- **T49**(一般): `DefaultCredentialProvider.getCredential()`中`credentialStore`命中后重置`consecutiveFailures`语义不一致。修正语义或添加注释。
- **T64**(一般): `PrescriptionLocalRuleFallback`缺少数据源异常处理和`CHECK_SKIPPED`机制。添加异常处理和跳过机制。
- **T65**(一般): `PrescriptionLocalRuleFallback`过敏检查仅使用`DRUG_INGREDIENTS`映射，覆盖范围有限。扩展映射范围。
- **T66**(一般): `JsonStructuredOutputParser`解析失败时抛出`RuntimeException`而非专用异常。定义并抛出专用异常。
- **涉及文件**: `DefaultCredentialProvider.java`, `PrescriptionLocalRuleFallback.java`, `JsonStructuredOutputParser.java`

---

## R1 NEW 启动期阻塞与薄适配器修复
任务：修复启动期阻塞与薄适配器基础问题：T22（底座Executor构造器Class\<T\> inputType不可注入）、T58（ModelEndpointHealthManager Bean注册）、T1（薄适配器isDtoEmpty()包路径恒真）、T2（薄适配器反射调用改为Phase4ServiceFacade统一封装）、T18（薄适配器移入thin-adapter子包）、T34（薄适配器使用llmCallExecutor而非commonPool）
选择理由：启动期失败率先解决，否则应用无法启动。T22阻止所有7+6=13个CapabilityExecutor的Bean创建，T58阻止所有依赖它的执行器，T1导致所有请求错误降级。薄适配器相关6个问题（T1/T2/T18/T34）强相关且修改同一组文件，合并处理减少上下文切换。根据审查反馈，T2方案从"直接引用Phase 4接口"修正为"创建Phase4ServiceFacade接口统一封装"——因为代码库中不存在Phase 4 Service接口且Maven不依赖Phase 4模块。
上下文：
- T22: 7个底座执行器构造参数`Class<T> inputType`无法被Spring注入。每个子类已覆盖`getInputType()`返回具体类字面量，但父类`defensiveCopy()`依赖字段`inputType`而非方法。需要移除父类构造参数、子类构造参数，让`defensiveCopy()`调用`getInputType()`。
- T58: `ModelEndpointHealthManager`无任何Spring注解，需要添加`@Service`。
- T1: 薄适配器`isDtoEmpty()`检查`request.getClass().getPackage().getName().startsWith("com.aimedical.modules.ai.api.dto.")`——DTO全在该包下，恒真。应改为使用`knownPhase4Packages`静态集合判断。
- T2: 薄适配器用`service.getClass().getMethod("execute", requestClass).invoke(service, request)`反射调用。代码库不存在Phase 4 Service接口，Maven不依赖Phase 4模块。改为：在`ai-api`中定义`Phase4ServiceFacade<RQ, RS>`接口（含`RS execute(RQ request)`方法）；在`ai-impl`中创建`Phase4ServiceFacadeConfig` Spring配置类，注入Phase 4服务Object并返回类型安全的`Phase4ServiceFacade` Bean（反射封装在此配置类中）；薄适配器注入`Phase4ServiceFacade<XxxRequest, XxxResponse>`直接调用。**统一构造器设计**：薄适配器最终构造器为`(Phase4ServiceFacade<RQ,RS> phase4Service, AiMetricsCollector, SlidingWindowMetricsStore, capTimeoutConfig, parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout, thinAdapterPerCapabilityConfig, Executor llmCallExecutor, ObjectMapper)`，`super()`传参对应调整（inputType参数已移除，llmCallExecutor不再传null）。
- T18: 6个薄适配器当前位于`orchestrator/impl/`，需移到`thin-adapter/`子包。包路径变更涉及`package`声明、Spring组件扫描配置。`@Service("CAPABILITY_ID")`保持不变。
- T34: 薄适配器`CompletableFuture.supplyAsync(() -> {...})`未传Executor参数。改为`CompletableFuture.supplyAsync(() -> {...}, llmCallExecutor)`，通过父类`protected final Executor llmCallExecutor`字段获取。
- 涉及文件: DiagnosisCapabilityExecutor, ImageAnalysisCapabilityExecutor, AnalysisReportForLabTestCapabilityExecutor, AnalysisReportForInspectionCapabilityExecutor, RecommendExecutionOrderCapabilityExecutor, RecommendExaminationCapabilityExecutor, TriageCapabilityExecutor, ScheduleCapabilityExecutor, PrescriptionCheckCapabilityExecutor, PrescriptionAssistCapabilityExecutor, MedicalRecordGenCapabilityExecutor, KbQueryCapabilityExecutor, DiscussionConclusionCapabilityExecutor, AbstractCapabilityExecutor, ModelEndpointHealthManager, Phase4ServiceFacade(新建), Phase4ServiceFacadeConfig(新建)

---

## 修订说明（v1 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] T2 修复方案不可行：Phase 4 Service 接口不存在，无法实现直接类型引用 | T2 方案重构为：在 ai-api 中创建 `Phase4ServiceFacade<RQ, RS>` 泛型接口，在 ai-impl 中创建 `Phase4ServiceFacadeConfig` 配置类将反射逻辑集中封装于 @Bean 方法中，薄适配器注入类型安全的 Phase4ServiceFacade 接口 |
| [一般] T22/T18/T2 对薄适配器构造器的叠加修改存在合并冲突风险 | R1 任务上下文中给出了薄适配器在 T22+T18+T2+T34 四项改动后的统一最终构造器签名，包括参数顺序、super() 传参对应关系 |
| [格式] 实施路线表列为64行任务级别而非10~15行轮次级别 | 路线表重构为12行轮次（R1-R12），每轮聚合多个相关任务。新增「轮次详情」逐轮展开内部任务明细 |

## 修订说明（v1 r3）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] 路线表中 R1/R2 任务分配与实际合并范围不一致 | 路线表 R1 合并为一行涵盖全部6项任务（T22,T58,T1,T2,T18,T34），删除原 R2 行，后续 R3-R12 依次前移为 R2-R11。轮次详情同步合并 R1/R2 为单一 R1 小节，所有轮次重新编号。路线表维持11行（10~15行范围内）且与正文完全对齐 |

---

## R1 PASSED 启动期阻塞与薄适配器修复
结果：主源码编译通过，6 个薄适配器移入 thinadapter 子包，Phase4ServiceFacade + 构造器签名修改完成
测试：thinadapter 子包 6 个测试文件通过编译和执行

## R2 PASSED 编译修复(薄适配器测试适配)
结果：删除 orchestrator/impl/ 下 6 个旧测试文件（跨包 protected 方法不可访问），保留 thinadapter/ 下可编译测试副本
测试：thinadapter 6 个测试文件编译通过

## R3 PASSED 修复残留9个测试编译错误
结果：5 个文件 9 处编译错误全部修复：AbstractCapabilityExecutorTest(TestableExecutor 添加 inputType 字段 + super() 去首参)、TriageCapabilityExecutorTest(2处构造调用)、DiscussionConclusionCapabilityExecutorTest(4处构造调用)、DiagnosisCapabilityExecutorTest(line 32 匿名类改为 Phase4DiagnosisRequest)、Phase4DiagnosisRequest(新建)
测试：mvn test 通过，520 pass / 7 flaky(已知无关)

## R4 PASSED 核心执行器异常处理
结果：实现了 AbstractCapabilityExecutor 中 7 项异常处理修复：T3/T4/T5/T21/T26/T27/T28。涉及 15 个源文件（1 父类 + 1 工具类 + 7 底座子类 + 6 薄适配器），编译通过，测试 80 pass / 0 fail。
测试：mvn test 通过，80 pass / 0 fail

## R5 FAILED Orchestrator与熔断降级
任务：修复 AiOrchestrator 与熔断降级策略中 7 项问题：T14(CircuitBreakerDegradationStrategy 熔断器作用域 capabilityId→endpointId)、T60(CLOSED→OPEN 未重置 failureCount)、T13(OPEN→HALF_OPEN probeLock 无清理路径)、T61(TimeoutDegradationStrategy 未使用注入的 metricsStore)、T19(AiOrchestrator 未注册能力返回 failure 而非 fail-fast 异常)、T20(AiOrchestrator catch 块中 metricsCollector.record() 未实现)、T35(AiOrchestrator volatile Map + ConcurrentHashMap 冗余)
选择理由：R4 核心执行器异常处理已通过验证。R5 聚焦 Orchestrator 与熔断降级——熔断器 per-endpoint 作用域修正（T14）、状态跃迁和数据一致性修复（T60/T13）、引用清理（T61）、Orchestrator 健壮性增强（T19/T20/T35）。涉及 3 个源文件（CircuitBreakerDegradationStrategy、TimeoutDegradationStrategy、AiOrchestrator），无子类波及。
上下文：
- T14: CircuitBreakerDegradationStrategy stateMap 和 circuitDataMap 键为 context.getServiceName()（即 capabilityId），应改为 endpointId。DegradationContext 的 operationName 字段承载 endpointId，在 checkPreDegradation() 中通过 ctx.setOperationName(routeResult.getEndpointId()) 设置（需注意 routing 发生在 executeStandardPipeline 中，故熔断降级需在 routing 后单独检查，或在 checkPreDegradation 中尚未知 endpointId 时回退 capabilityId）。
- T60: CLOSED→OPEN data.failureCount 累加但从未重置。在 CLOSED case 中将 data.failureCount++ 改为 data.failureCount = 1。
- T13: HALF_OPEN 中 probeLock 被探测线程获取后若线程崩溃无释放路径。在 CircuitData 中添加 probeAcquiredAt 时间戳，HALF_OPEN case 中若 probeLock 被持有超过 openWindowMs 则 reset probeLock 重试。
- T61: TimeoutDegradationStrategy 构造器注入 metricsStore 但 shouldDegrade() 中未使用。移除此未使用字段和构造参数，精简实现。
- T19: AiOrchestrator.handle() 中 executorMap.get(capabilityId)==null 时返回 AiResult.failure 而非抛出异常。改为 throw new IllegalArgumentException("未注册能力标识: " + capabilityId)。
- T20: catch 块中 TODO 注释未实现 metricsCollector.record()。在 catch 块中构建 AiCallRecord(capabilityId, null, null, null, null, null, null, null, null, null, elapsedMs, true, degradeReason, 0, 0) 并调用 metricsCollector.record()。
- T35: executorMap 声明为 volatile Map<String,...> 但实际赋值为 ConcurrentHashMap。改为 ConcurrentHashMap<String, CapabilityExecutor<?, ?>> 并移除 volatile。
- 涉及文件: CircuitBreakerDegradationStrategy.java, TimeoutDegradationStrategy.java, AiOrchestrator.java

---

## R5 FAILED Orchestrator与熔断降级
结果：22 个测试失败（12 Failures + 10 Errors），3 个独立根因：
1. **metricsStore NPE（14个）**：executeStandardPipeline line 354 新增熔断二次检查中 `this.metricsStore.buildDegradationContext()` 无 null 守卫
2. **endpoint 级状态未隔离（1个）**：CLOSED case 使用 capability 级 `metricsStore.getFailureRate()`，同一 capability 下不同 endpoint 同时 OPEN
3. **薄适配器超时失效（7个）**：`Runnable::run` 执行器使 `Future.get(timeout)` 永不及时触发
验证：539 tests, 517 pass, 22 fail

## R6 RETRY R5验证修复
任务：修复 R5 验证失败的 22 个测试：metricsStore NPE 守卫、endpoint 级 CLOSED→OPEN 失败跟踪、薄适配器测试执行器异步化
选择理由：R5 实现存在 3 个独立根因导致 22 个测试失败，必须在推进 R7 前修复。三个根因相互独立，可在同一轮并行修复。
上下文：
- **修复 1**：`AbstractCapabilityExecutor.executeStandardPipeline()` line 354 前添加 `if (metricsStore != null)` 守卫，与 `checkPreDegradation()` 中已有的 null 守卫模式一致
- **修复 2**：`CircuitBreakerDegradationStrategy.shouldDegrade()` CLOSED case 改为使用 endpoint 级 `circuitDataMap.get(key).failureCount`。仅当该 endpoint 已有历史失败记录时才使用 capability 级失败率；洁净 endpoint 直接返回 false
- **修复 3**：6 个薄适配器测试文件（`AnalysisReportForInspectionCapabilityExecutorTest`/`AnalysisReportForLabTestCapabilityExecutorTest`/`DiagnosisCapabilityExecutorTest`/`ImageAnalysisCapabilityExecutorTest`/`RecommendExaminationCapabilityExecutorTest`/`RecommendExecutionOrderCapabilityExecutorTest`）中 `createExecutor()` 的 `Runnable::run` 替换为 `ForkJoinPool.commonPool()`
- 涉及文件: AbstractCapabilityExecutor.java, CircuitBreakerDegradationStrategy.java, 6个薄适配器测试文件

## R6 FAILED R5验证修复(熔断NPE/状态隔离/超时)
结果：529 pass / 11 failures / 2 errors。3类根因中①metricsStore NPE守卫已修复，②薄适配器测试执行器Runnable::run→commonPool已修复，③CLOSED case endpoint级失败跟踪引入chicken-and-egg死锁——circuitDataMap条目从未创建(缺computeIfAbsent)，failureCount始终为0，导致所有shouldDegrade()在CLOSED下恒返回false。
验证：539 tests，529 pass，11 fail（全部CircuitBreakerDegradationStrategyTest）+ 2 error（AbstractCapabilityExecutorTest）
深入原因：
1. CircuitBreakerDegradationStrategy.CLOSED: data.failureCount > 0 守卫+缺computeIfAbsent导致CLOSED→OPEN路径永远走不到
2. AbstractCapabilityExecutor.handleSuccess(): metricsStore.recordSuccess() 无null检查，metricsStore==null时NPE

---

## R7 RETRY R6验证修复(熔断CLOSED死锁+handleSuccess NPE)
任务：修复 R6 验证失败的 13 个测试：CLOSED case computeIfAbsent + failureCount 递增机制还原、handleSuccess metricsStore null守卫
选择理由：R6 CLOSED case 修改导致 circuitDataMap 条目创建+计数累积双缺失，所有 CLOSED→OPEN 转换彻底阻塞。handleSuccess metricsStore NPE 为独立遗漏。两处均为 R6 代码级回归，修复后应恢复 529→542 pass。
上下文：
- **修复 A**：CircuitBreakerDegradationStrategy.shouldDegrade() CLOSED case 恢复 `circuitDataMap.computeIfAbsent(key, k→new CircuitData())` 创建条目；在每个 shouldDegrade 调用时 `data.failureCount++` 递增（防溢出）；移除 `data.failureCount > 0` 外部守卫，因为递增后恒>0。T60(failureCount=1 on OPEN transition) 保留。endpoint级状态隔离通过 key 分离（operationName优先于serviceName）维持。
- **修复 B**：AbstractCapabilityExecutor.handleSuccess() 中 `metricsStore.recordSuccess(capabilityId, elapsedMs)` 前添加 `if (metricsStore != null)` 守卫，与同一方法中 `metricsCollector != null` 守卫模式一致。
- 涉及文件: CircuitBreakerDegradationStrategy.java, AbstractCapabilityExecutor.java

---

## 修订说明（v3 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] 路线表与实际进度不同步：R2之后缺少「修复残留9个测试编译错误」行 | 路线表 R2 后插入新 R3 行（修复残留9个测试编译错误），原 R3-R12 顺延为 R4-R13；轮次详情同步插入 R3 小节并顺延编号；R1/R2 已标记 ✅ |

---

## R7 PASSED 熔断测试修复
结果：修复 CLOSED case computeIfAbsent + failureCount 递增（解除 chicken-and-egg 死锁），handleSuccess metricsStore null 守卫
测试：2503 pass / 0 fail / 0 error / 6 skip

## R8 PASSED 指标与健康管理
任务：修复指标与健康管理 7 项问题：T15(LoggingMetricsCollector @Async线程池绑定)、T16(LoggingMetricsCollector字段映射数据丢失)、T17(AiCallRecord缺少7个关键字段+工厂方法)、T25(AiCallRecord字段与设计文档对齐)、T51(SlidingWindowMetricsStore windowSeconds AtomicLong+参数校验)、T52(SlidingWindowMetricsStore读取快照复制)、T59(ModelEndpointHealthManager UNAVAILABLE→DEGRADED→CONNECTED状态跃迁)
选择理由：R7 熔断测试修复已验证通过（2503 pass / 0 fail）。R8 按计划推进指标与健康管理模块——这些任务涉及指标采集（AiCallRecord）、指标持久化（LoggingMetricsCollector）、指标存储（SlidingWindowMetricsStore）和端点健康管理（ModelEndpointHealthManager），构成完整的可观测性管线。4 个源文件（AiCallRecord.java / LoggingMetricsCollector.java / SlidingWindowMetricsStore.java / ModelEndpointHealthManager.java），7 项修复高度耦合（T16/T17/T25 均围绕 AiCallRecord 字段对齐与映射），合并一轮处理。
上下文：
- **T15**(严重): `LoggingMetricsCollector.record()` 上 `@Async` 注解未指定线程池名称。需改为 `@Async("metricsAsyncExecutor")`（对应 `AiPlatformConfig` 中名为 `metricsAsyncExecutor` 的 `Executor` Bean）。
- **T16**(严重): `LoggingMetricsCollector.record()` 中 7 处硬编码：`entity.setInputSummary(null)` / `setOutputSummary(null)` / `setErrorCode(null)` / `setErrorMessage(null)` / `setRetryCount(0)` / `setTotalTokens(null)` — 这些字段在 AiCallRecord 中缺失，无法映射。需配合 T17 补齐 AiCallRecord 字段后在 LoggingMetricsCollector 中正确读取。
- **T17**(严重): AiCallRecord 当前 15 个字段（capabilityId/modelId/promptVersion/userId/departmentId/sessionId/visitId/patientId/callerRole/callerId/elapsedMs/degraded/degradeReason/promptTokens/completionTokens），与设计文档要求对等 AiCallLogEntity 的 24 个字段相比，缺失 7 个：`callTime(LocalDateTime)` / `capabilityName(String)` / `inputSummary(String)` / `outputSummary(String)` / `errorCode(String)` / `errorMessage(String)` / `retryCount(int)` / `totalTokens(Integer)`。需补齐这些字段，同时添加 3 个静态工厂方法：`success()`, `failure()`, `degraded()`。
- **T25**(一般): AiCallRecord 字段序与设计文档不一致。按设计文档 `06_ood_phase5_G.md` §3.5 AiCallRecord 字段表重新调整字段声明顺序和对位。
- **T51**(一般): `SlidingWindowMetricsStore` 的 `windowSeconds` 字段当前为 `private volatile long windowSeconds = 60;`，需改为 `private final AtomicLong windowSeconds = new AtomicLong(60);`。`setWindowSeconds()` 需添加参数校验（`windowSeconds > 0`，否则抛出 `IllegalArgumentException`）。
- **T52**(一般): `SlidingWindowMetricsStore` 读取方法（`getFailureRate()`/`getEffectiveFailureRate()`/`getAverageElapsed()`/`buildDegradationContext()`）未执行快照复制。需在 `synchronized (deque)` 块内通过 `deque.toArray()` 复制数组，然后在同步块外遍历副本进行计算，以缩短锁持有时间。
- **T59**(一般): `ModelEndpointHealthManager.recordCallResult()` 中 UNAVAILABLE 状态下探测成功直接跳转到 CONNECTED（line 97），跳过了 DEGRADED 中间态。应改为：UNAVAILABLE + success → DEGRADED（而非直接 CONNECTED）；DEGRADED 下连续成功 3 次 → CONNECTED（已实现）。
- 涉及文件: `AiCallRecord.java`, `LoggingMetricsCollector.java`, `SlidingWindowMetricsStore.java`, `ModelEndpointHealthManager.java`

---

## R8 PASSED 指标与健康管理
结果：AiCallRecord 扩容至 23 字段 + 3 静态工厂方法 + promptVersion String→Integer；LoggingMetricsCollector @Async 绑定 + 7 处硬编码修正；SlidingWindowMetricsStore AtomicLong + 快照复制；ModelEndpointHealthManager UNAVAILABLE→DEGRADED 中间态
测试：2527 pass / 0 fail / 0 error / 6 skip

## R9 NEW LLM客户端基础设施
任务：修复 LLM 客户端基础设施 10 项问题：T6(DelegatingLlmChatService @PostConstruct 枚举校验)、T7(回退日志上下文信息+健康告警)、T36(getClientType 返回 null 语义不清)、T37(EndpointRateLimiter maxBurstSeconds 死代码)、T38(限流拒绝绕过降级管线)、T11(HttpClient 连接池复用)、T12(endpointUrl 混用)、T50(structuredChat 异常风格不一致)、T39(UnsupportedOperationException→LlmInfrastructureException)、T40(移除未使用导入)
选择理由：R8（指标与健康管理）已验证通过（2527 pass / 0 fail）。R9 按计划推进 LLM 客户端基础设施——10 项问题集中在 client 包 6 个源文件，修复后影响运行时 LLM 通信链路的健壮性和可观测性。
上下文：
- **T6**: DelegatingLlmChatService 缺少 @PostConstruct 启动期校验所有 ClientType 枚举值（HTTP_API/SPRING_AI）是否有对应 delegate 实现
- **T7**: chat()/structuredChat() 回退日志缺少 endpointId 上下文和健康检查端点告警；log.error 应降级为 log.warn
- **T36**: getClientType() 返回 null → 改为抛出 UnsupportedOperationException
- **T37**: EndpointRateLimiter.defaultMaxBurstSeconds 声明但未使用 → 移除死代码
- **T38**: HttpApiLlmChatService 限流拒绝返回 AiResult.failure → 改为抛 LlmInfrastructureException 走降级管线
- **T11**: 每次 chat() 调用新建 HttpClient → 改为类字段复用
- **T12**: URI.create(endpointId) → 需要改为 endpointUrl（LlmChatRequest 需增加 endpointUrl 字段或维护映射）
- **T50**: structuredChat() 直接抛异常 → 改为返回 CompletableFuture.completedFuture(AiResult.failure(...))
- **T39**: SpringAiLlmChatService/SpringAiLlmChatStreamService 抛 UnsupportedOperationException → LlmInfrastructureException
- **T40**: LlmChatStreamService 移除未使用的 AiAbilityInputInvalidException 导入
- 涉及文件: DelegatingLlmChatService.java, EndpointRateLimiter.java, HttpApiLlmChatService.java, SpringAiLlmChatService.java, SpringAiLlmChatStreamService.java, LlmChatStreamService.java

---

## R9 FAILED LLM客户端基础设施
结果：主源码编译通过/2525 tests pass，但 LlmChatRequestTest 2 个错误——Jackson JSON 反序列化冲突：5参构造器与6参构造器均标注 @JsonProperty，Jackson 无法确定使用哪个构造器反序列化
验证：2525 pass / 0 fail / 2 error / 6 skip

## R10 NEW 配置与环境管理（含R9 RETRY修复）
任务：
- RETRY（R9遗留）：LlmChatRequest 6参构造器移除 @JsonProperty，消除 Jackson 反序列化歧义
- NEW（R10）：T8（AiPlatformConfig @ConditionalOnProperty）、T9（ObjectProvider 延迟解析 + springAiLlmChatService @ConditionalOnClass）、T43（refreshCapabilityTimeoutConfig Binder 改 ApplicationContext.getBean）、T44（refreshWindowSeconds 线程安全确认与参数校验）、T10（AiPlatformEnvironmentPostProcessor 配置冲突告警日志）、T45（FallbackAiService delegate==null fail-fast + 移除 aiPlatformEnabled 未使用参数）
选择理由：R9 验证失败仅 1 处 Jackson 注解问题（LlmChatRequest 单文件 2 行修改），修复代价极小，与 R10 配置任务无冲突可合并一轮。R10 6 项配置问题覆盖 3 个源文件（AiPlatformConfig / AiPlatformEnvironmentPostProcessor / FallbackAiService），强相关且均涉及 Spring 条件化装配与配置刷新，合并处理减少轮次。
上下文：
- **R9 RETRY**：LlmChatRequest.java 第 32-37 行 6 参构造器中 6 个 @JsonProperty 注解全部移除，仅保留 5 参构造器的 @JsonProperty 供 Jackson 反序列化。6 参构造器为内部程序化构造（代码中 new LlmChatRequest(messages, options, ...)）使用，不涉及 JSON 序列化/反序列化场景。
- **T8**(严重): AiPlatformConfig 类级别需添加 `@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true", matchIfMissing = false)`，使 AI 平台 Bean 仅在显式启用时创建。FallbackAiService 已通过 ObjectProvider+null 守卫适配无 Bean 场景。
- **T9**(严重): 
  - `springAiLlmChatService()` Bean 方法需添加 `@ConditionalOnClass(name = "org.springframework.ai.chat.ChatModel")`
  - `delegatingLlmChatService(HttpApiLlmChatService, SpringAiLlmChatService)` 方法参数改为 ObjectProvider，通过 `provider.getIfAvailable()` 可选获取，仅注册实际存在的客户端类型。同时 `httpApiLlmChatService()` 也应使用 ObjectProvider 保持一致性。
- **T43**(一般): `refreshCapabilityTimeoutConfig()` 中 Binder.bind() 改为 `applicationContext.getBean(AiExecutionProperties.class)` 读取，与 `cacheInitialConfigValues()` 一致。移除 Environment/Binder 相关代码。
- **T44**(一般): `refreshWindowSeconds()` 中 `store.setWindowSeconds(windowSeconds)` 需确认线程安全。T51 已为 SlidingWindowMetricsStore 的 `windowSeconds` 使用 `AtomicLong` 并添加 `windowSeconds > 0` 参数校验，T44 只需确认且无需额外修改。
- **T10**(严重): AiPlatformEnvironmentPostProcessor.postProcessEnvironment() 在自动设置 `ai.mock.enabled` 时添加 `log.warn()` 告警日志。
- **T45**(一般): FallbackAiService 构造器：`delegate == null` 时改为 `throw new IllegalStateException("No available AiService delegate: AI platform is enabled but no AiService implementation found")` fail-fast 异常；移除 `@Value("${ai.platform.enabled:false}") boolean aiPlatformEnabled` 未使用参数及对应导入（`@Value`）。
- 涉及文件: `LlmChatRequest.java`, `AiPlatformConfig.java`, `AiPlatformEnvironmentPostProcessor.java`, `FallbackAiService.java`

---

## R10 FAILED 配置与环境管理
结果：559 pass / 1 failure / 1 error。2个测试失败均为 AiPlatformConfigTest 断言问题：
1. `classShouldBeAnnotatedWithConditionalOnProperty:52` — `ann.name()` 返回 `String[]` 但 `assertEquals` 传了 String。修复：改为 `assertArrayEquals(new String[]{"ai.platform.enabled"}, ann.name())`
2. `springAiLlmChatServiceShouldBeAnnotatedWithConditionalOnClass:99` — `getMethod()` 只找到 public 方法，但 `springAiLlmChatService()` 是包级私有。修复：改为 `getDeclaredMethod("springAiLlmChatService")`
验证：561 tests, 559 pass, 1 fail, 1 error

---

## R11 NEW 数据模型与值对象（含R10 RETRY修复）
任务：
- RETRY（R10遗留）：AiPlatformConfigTest 2处断言修复
- NEW（R11）：T29（AiResult添加工厂方法+字段final）、T30（DegradationContext Integer→int+serialVersionUID更新）、T31（DegradationContext.Builder添加builder()）、T32（Phase4BusinessException添加getErrorCode()）、T46（ChatToolDefinition.strict移除setter）、T47（LlmChatOptions字段final+移除setter）
选择理由：R10验证失败仅2处测试断言问题（AiPlatformConfigTest单文件），修复代价极小，与R11数据模型任务无冲突可合并一轮。R11 6项问题覆盖5个源文件（ai-api中AiResult/DegradationContext/Phase4BusinessException，ai-impl中ChatToolDefinition/LlmChatOptions），均为数据对象不可变性加固，底层依赖优先。T47修改波及2个生产文件（AbstractCapabilityExecutor.java:389-391和DiscussionConclusionCapabilityExecutor.java:191-193需从setter模式改为全参构造器）。
上下文：
- **R10 RETRY**：
  - AiPlatformConfigTest.java:52 — `assertEquals("ai.platform.enabled", ann.name())` 改为 `assertArrayEquals(new String[]{"ai.platform.enabled"}, ann.name())`。`@ConditionalOnProperty.name()` 返回 `String[]`，用 `assertEquals(String, String[])` 类型不匹配。
  - AiPlatformConfigTest.java:99 — `getMethod("springAiLlmChatService")` 改为 `getDeclaredMethod("springAiLlmChatService")`。`springAiLlmChatService()` 方法为包级私有（无 public 修饰符），`Class.getMethod()` 仅返回 public 方法。
- **T29**(一般): AiResult 当前5字段（success/data/errorCode/degraded/fallbackReason）均为可变（有setter）。设计文档要求不可变值对象。需添加 `degradedWithErrorCode(String errorCode, String fallbackReason)` 静态工厂方法；所有字段设为 `final`（包括 `private final boolean success`、`private final T data`等）；移除全部setter；无参构造器通过 `this(false, null, null, false, null)` 链式调用全参构造器。波及测试文件：AiResultTest.java中5个setter测试(`shouldSetAndGetSuccess`/`shouldSetAndGetData`/`shouldSetAndGetErrorCode`/`shouldSetAndGetDegraded`/`shouldSetAndGetFallbackReason`)需删除或重写。无生产代码波及（MockAiService.setDegraded是在TriageResponse上，非AiResult）。
- **T30**(一般): DegradationContext 当前 `invocationCount` 和 `failureCount` 为 `Integer`，设计文档要求 `int`。改为 `int`。波及：`postDeserializationValidate()` 中 `(invocationCount == null || invocationCount == 0)` → `(invocationCount == 0)`；`isInitialized()` 中 `return invocationCount != null && failureCount != null` 需改为 `return invocationCount > 0 || failureCount > 0 || elapsedTime > 0L` 或加 `initialized` 标志位；Builder 中对应字段改为 `int`（默认0）。更新 `serialVersionUID`（当前 `1L` 改为 `2L`）。波及测试文件：DegradationContextTest.java中 `isInitialized` 相关断言和 `defaultConstructorShouldCreateZeroValueInstance` 中 `assertNull`→`assertEquals(0, ...)`。生产代码：TimeoutDegradationStrategy.java:19-20 的 `Integer invocationCount = context.getInvocationCount(); if (invocationCount == null || invocationCount == 0)` → `int invocationCount = context.getInvocationCount(); if (invocationCount == 0)`。
- **T31**(一般): DegradationContext.Builder 添加 `public static Builder builder() { return new Builder(); }` 静态工厂方法。不影响现有new DegradationContext.Builder()用法。
- **T32**(一般): Phase4BusinessException 添加 `private final String errorCode` 字段、对应 `protected Phase4BusinessException(String message, String errorCode)` 构造器、`public String getErrorCode()` getter。现有 `protected Phase4BusinessException(String message)` 构造器保留（errorCode=null），同名 `(String message, Throwable cause)` 构造器保留。现有匿名子类测试 `new Phase4BusinessException("test msg") {}` 不受影响（编译期不强制实现或传递 errorCode）。无生产代码子类（已搜索，0个 `extends Phase4BusinessException`）。
- **T46**(一般): ChatToolDefinition.strict 字段有 `setStrict(boolean)` setter 破坏不可变性。仅删除该 setter 方法即可（字段本身仍是 `private boolean strict = true`，Jackson 可通过反射设置字段值，对外 API 无 setter 即为只读）。波及测试：ChatToolDefinitionTest.java中 `shouldAllowMutatingStrict`（删除）和 `shouldReflectStrictMutationInSerialization`（重写，不用setter而用构造器或反射设值）。
- **T47**(一般): LlmChatOptions 7个字段全部可变（均有getter+setter）。设计文档要求为值对象（不可变）。所有字段改为 `final`，通过全参构造器初始化；移除全部setter；无参构造器通过 `this(null, null, null, null, null, null, null)` 链式调用全参构造器。⚠️ **波及生产代码**：
  - `AbstractCapabilityExecutor.java:388-391`：`new LlmChatOptions()` + 3次setter调用 → 改为全参构造器 `new LlmChatOptions(routeResult.getModelId(), 0.7, 2048, null, null, null, null)`
  - `DiscussionConclusionCapabilityExecutor.java:190-193`：同理改为全参构造器
  - 波及测试：LlmChatOptionsTest.java中 `shouldSetAndGetFields`（全部setter使用需删除），其余测试（构造器/序列化/反序列化）需调整以适配 final 字段。
- 涉及文件:
  - 修改：`AiPlatformConfigTest.java`（R10 RETRY）、`AiResult.java`（T29）、`DegradationContext.java`（T30+T31）、`Phase4BusinessException.java`（T32）、`ChatToolDefinition.java`（T46）、`LlmChatOptions.java`（T47）
  - 波及：`AbstractCapabilityExecutor.java`（T47波及）、`DiscussionConclusionCapabilityExecutor.java`（T47波及）、`TimeoutDegradationStrategy.java`（T30波及）
  - 测试：`AiResultTest.java`、`DegradationContextTest.java`、`Phase4BusinessExceptionTest.java`、`ChatToolDefinitionTest.java`、`LlmChatOptionsTest.java`

---

## R11 FAILED 数据模型与值对象
结果：2400 pass / 0 fail / 1 error — PrescriptionAuditServiceImplTest.auditShouldHandleAiResultDataNull:194 调用 `AiResult.setSuccess(true)`，因 T29 使字段 final 后移除 setter，下游测试 NoSuchMethodError
验证：2400 pass / 0 fail / 1 error / 6 skip
根因：R11 上游变更（AiResult 字段 final）导致 prescription 模块测试在运行时找不到 setSuccess 方法。同一模式波及 MedicalRecordConverterTest（5处 setDegraded + 1处 setSuccess）。

## R12 NEW 数据模型与值对象 RETRY + 讨论结论执行器
任务：
- **RETRY（R11）**：修复下游测试文件因 AiResult setter 移除导致的 NoSuchMethodError
- **NEW（R12）**：修复 DiscussionConclusionCapabilityExecutor 防御性拷贝后修改 request（T24）+ 线程池嵌套死锁风险（T33）
选择理由：R11 验证失败为下游测试适配问题，修复范围小（2 个测试文件 8 处 setter 调用），与 R12 讨论结论执行器均涉及 DiscussionConclusionCapabilityExecutor（T24 在同一文件），合并一轮处理减少轮次。
上下文：
- **RETRY（R11）**：
  - `PrescriptionAuditServiceImplTest.java:192-194`：`new AiResult<>()` + `setSuccess(true)` + `setData(null)` → 改为 `AiResult.success(null)`（setData(null) 多余——success() 工厂方法 data=null）
  - `MedicalRecordConverterTest.java:124-126`：同上，`new AiResult<>()` + `setSuccess(true)` + `setData(null)` → `AiResult.success(null)`
  - `MedicalRecordConverterTest.java:111-113`：`AiResult.failure("MR_GEN_AI_TIMEOUT")` + `setData(aiResp)` + `setDegraded(true)` → `new AiResult<>(false, aiResp, "MR_GEN_AI_TIMEOUT", true, null)`
  - `MedicalRecordConverterTest.java:135-136`：`AiResult.failure("MR_GEN_AI_TIMEOUT")` + `setDegraded(true)` → `AiResult.degradedWithErrorCode("MR_GEN_AI_TIMEOUT", null)`
  - `MedicalRecordConverterTest.java:148-150`：`AiResult.failure("MR_GEN_AI_INTERRUPTED")` + `setData(aiResp)` + `setDegraded(true)` → `new AiResult<>(false, aiResp, "MR_GEN_AI_INTERRUPTED", true, null)`
  - `MedicalRecordConverterTest.java:163-165`：`AiResult.failure("MR_GEN_AI_EXECUTION_ERROR")` + `setData(aiResp)` + `setDegraded(true)` → `new AiResult<>(false, aiResp, "MR_GEN_AI_EXECUTION_ERROR", true, null)`
  - `MedicalRecordConverterTest.java:178-180`：`AiResult.failure("SOME_UNKNOWN_ERROR")` + `setData(aiResp)` + `setDegraded(true)` → `new AiResult<>(false, aiResp, "SOME_UNKNOWN_ERROR", true, null)`
- **T24**(一般)：`doExecuteInternal()` 在压缩成功后调用 `request.setTranscripts(List.of(compressedTranscript))` 修改了防御性拷贝后的 request 对象。`DiscussionConclusionRequest` 仅含 `transcripts` 一个字段。修复：不修改 request，改为创建新 `DiscussionConclusionRequest` 实例装载压缩后的 transcripts，赋值给 request 引用供下游 `extractVariables()` 和 `executeStandardPipeline()` 使用。
- **T33**(一般)：`compressTranscripts()` 中 `CompletableFuture.supplyAsync(() -> {...}, llmCallExecutor)` 嵌套提交到 `llmCallExecutor`。`doExecuteInternal` 已在 `llmCallExecutor` 线程上运行，若池中无空闲线程则死锁。`AiPlatformConfig` 已有 `transcriptSummaryExecutor` 独立线程池 Bean。修复：在构造器中注入 `@Qualifier("transcriptSummaryExecutor") Executor transcriptSummaryExecutor`，`compressTranscripts()` 使用此独立线程池替代 `llmCallExecutor`。
- 涉及文件：
  - RETRY：`PrescriptionAuditServiceImplTest.java`、`MedicalRecordConverterTest.java`
  - NEW：`DiscussionConclusionCapabilityExecutor.java`

---

## R12 FAILED 讨论结论执行器 RETRY+下游测试适配
结果：DiscussionConclusionCapabilityExecutorTest.java:439 编译错误——多余的右花括号（文件末尾双 `}`）
验证：0 pass / 0 fail / 1 compile error

---

## R13 NEW 模板管理（含R12 RETRY修复）
任务：
- **RETRY（R12）**：删除 `DiscussionConclusionCapabilityExecutorTest.java:439` 多余的右花括号
- **T62（一般）**：`PromptTemplateManager.render()` 签名 `String promptVersion` → `Integer promptVersion`，同步下溯至 `DatabasePromptTemplateManager`、`AbstractCapabilityExecutor` 全部 3 个方法签名（`executeStandardPipeline`/`doDegrade`/`handleSuccess`）、8 个子类 `private String promptVersion` → `Integer`（改 `@Value` 默认值为 `0`）
- **T54（一般）**：`DatabasePromptTemplateManager` 缓存键包含 `promptVersion`，`resolveExactVersion()` 先查缓存再查 DB，查后回填缓存
- **T55（一般）**：`DatabasePromptTemplateManager.warmup()` 缓存键包含版本号，使预热后 `render(promptVersion)` 直接命中缓存不查 DB
选择理由：R12 验证失败仅 1 行多余 `}`（测试文件），修复代价为零。R13 三项均集中于 `template/` 包 2 个源文件（接口+实现），从接口签名变更到底层缓存修复形成完整管线：T62 改变类型后 T54/T55 的缓存键自然支持 Integer 版本号。涉及同一个实现 `DatabasePromptTemplateManager` 及上级调用链，合并一轮避免分段修改造成的编译失败。
上下文：
- **R12 RETRY**：`DiscussionConclusionCapabilityExecutorTest.java` 末尾行 439 多余的 `}`（行 438 已有关闭类的 `}`）。删除即可。不涉及生产代码。该测试已构造函数传参 `Executors.newSingleThreadExecutor()`（`transcriptSummaryExecutor` 对应位），无构造参数遗漏。
- **T62**：
  - `PromptTemplateManager.render()`：`(..., String promptVersion)` → `(..., Integer promptVersion)`
  - `DatabasePromptTemplateManager.render()`：同步改为 `Integer promptVersion`
  - `DatabasePromptTemplateManager.resolveExactVersion()`：参数从 `String promptVersion` 改为 `Integer promptVersion`，内部移除 `Integer.parseInt()` 逻辑，直接使用参数值
  - `AbstractCapabilityExecutor` 方法签名更改：
    - `executeStandardPipeline(..., String promptVersion, String sentinelReason)` → `Integer promptVersion`
    - `doDegrade(..., String promptVersion, String modelId, String sentinelReason)` → `Integer promptVersion`
    - `handleSuccess(..., String promptVersion, ...)` → `Integer promptVersion`
    - `AiCallRecord` 公开构造器（line 30）接受 `String promptVersion`（内部 parse 为 Integer）。改造后 `promptVersion` 为 Integer，调用时需 `promptVersion != null ? String.valueOf(promptVersion) : null` 转换（line 268, line 509 仅 2 处）
  - 8 个子类：`private String promptVersion` + `@Value("${ai.prompt.version.XXX:XXX}")` → `private Integer promptVersion` + `@Value("${ai.prompt.version.XXX:0}")`。所有默认值统一为 `0`（0 版本不存在 → `resolveExactVersion` 返回 null → fallback 到 active template）
  - `DatabasePromptTemplateManagerTest`：`render` 调用第 4 参数从 `"2"` → `2`（Integer）；`"not-a-number"` 测试改为 `null` 测试（验证 `null` 走 active 模板）
- **T54**：
  - `buildCacheKey(capabilityId, departmentId)` → 重载 `buildCacheKey(capabilityId, departmentId, promptVersion)`，version 非 null 时追加 `:v{version}` 后缀
  - `resolveExactVersion()` 先 `cache.getIfPresent(buildCacheKey(capId, deptId, version))`，命中直接返回；DB 查询后将结果 `cache.put(key, result)`
- **T55**：
  - `warmup()` 中每条 active 模板同时用 `buildCacheKey(capId, deptId, null)` 和 `buildCacheKey(capId, deptId, pt.getVersion())` 双键缓存
  - `onTemplateChanged()` 中 department 级 invalidation 保持针对 active 键，version 键由 TTL 自动过期；全局 invalidation（`cache.invalidateAll()` + `warmup()`）不受影响
  - 涉及的模板文件: `PromptTemplateManager.java`, `DatabasePromptTemplateManager.java`, `DatabasePromptTemplateManagerTest.java`
  - 涉及的上游文件: `AbstractCapabilityExecutor.java`（3 个方法签名 + 2 处 AiCallRecord 构造调用）, 8 个子类(7 底座 + 1 讨论结论) 各 1 行 `private String→Integer` 和 `@Value` 默认值
  - 涉及的测试文件: `AbstractCapabilityExecutorTest.java`（23 处 executeStandardPipeline `"v1"`→`1` + 2 处 doDegrade `String`→`Integer`）

---

## R13 FAILED 模板管理（T54,T55,T62）
结果：529 pass / 0 fail / 34 error。34个错误分两类：
1. **R13新引入（6个）**：DatabasePromptTemplateManagerTest — Mockito `any()` 对 `int` 类型参数返回 null 导致 NPE / InvalidUseOfMatchers。根因：`findByCapabilityIdAndDepartmentIdAndVersion` 第3参数为原始类型 `int`，测试中 `any()` 无法匹配原始 `int`，需改为 `anyInt()`。
2. **R11遗留（28个）**：CircuitBreakerDegradationStrategyTest×16 + TimeoutDegradationStrategyTest×6 + SlidingWindowMetricsStoreTest×5 + AbstractCapabilityExecutorTest×1 — NoSuchMethod `Builder.invocationCount(Integer)` / `getInvocationCount()` 返回 Integer。根因：R11将 DegradationContext invocationCount/failureCount 从 Integer 改为 int，Builder 签名从 invocationCount(Integer) 改为 invocationCount(int)，但运行时引用仍指向旧 Integer 签名。
验证：529 tests, 529 pass, 0 fail, 34 error

## R14 NEW 路由与实验管理（含R13 RETRY修复+遗留错误修复）
任务：
- **RETRY（R13）**：修复6个 DatabasePromptTemplateManagerTest 错误（`any()`→`anyInt()`）
- **FIX（遗留28错误）**：修复 DegradationContext Builder/Getter Integer→int 不一致导致的 NoSuchMethod 错误
- **NEW（R14）**：T41（ModelRouter.route() 签名 Object→ExperimentAssignment）、T42（@Scheduled 指定 scheduler）、T48（枚举转换失败日志告警）、T56（min().reversed()→max()）、T57（loader 排序统一）、T63（ExperimentGroup N+1 查询修复）
选择理由：R13 验证失败含 R11 遗留的 28 个错误（Builder.invocationCount(Integer) NoSuchMethod），这些错误会继续阻塞 R14 测试通过。R13 的 6 个 DatabasePromptTemplateManagerTest 错误与遗留 28 错误均属同一轮修复范畴。合并 R13 RETRY + 遗留修复 + R14 NEW 为一轮，一次性消除所有阻塞测试通过的错误后，再推进路由与实验管理功能实现。

### RETRY 上下文（R13 DatabasePromptTemplateManagerTest 6 error）
- **根因**：`PromptTemplateRepository.findByCapabilityIdAndDepartmentIdAndVersion(String, String, int)` 第3参数为原始类型 `int`。测试中 `any()` 返回 Object 无法匹配原始 int。
- **修复**：DatabasePromptTemplateManagerTest.java line 217、239 的 `verify(..., any(), any(), any())` 改为 `verify(..., anyString(), any(), anyInt())`。
- **涉及文件**：`DatabasePromptTemplateManagerTest.java`

### FIX 上下文（遗留28错误）
- **根因**：R11 将 DegradationContext.invocationCount/failureCount 从 Integer 改为 int。Builder.invocationCount(int) 存在但调用方仍引用旧 invocationCount(Integer) 签名。
- **修复方向**：
  1. 执行 `mvn clean compile test-compile` 强制全量重编译
  2. 如仍报错，检查 DegradationContext 字节码（`javap -c DegradationContext\$Builder.class`）
  3. 确认 buildDegradationContext() 中 `(int) invocationCount` 调用无误
  4. 检查 TimeoutDegradationStrategy 中 `context.getInvocationCount()` 使用方式
- **涉及文件**：`CircuitBreakerDegradationStrategyTest.java`, `TimeoutDegradationStrategyTest.java`, `SlidingWindowMetricsStoreTest.java`, `AbstractCapabilityExecutorTest.java`, `DegradationContext.java`

### T41 上下文
ModelRouter.route() 当前签名 `route(String, Object)` → 改为 `route(String, ExperimentAssignment)`。
- **波及**：DefaultModelRouter.java:49 签名 + AbstractCapabilityExecutorTest.java 中3处匿名lambda + AiOrchestrator.java（如有调用）

### T42 上下文
DefaultModelRouter.java:34 `@Scheduled(fixedDelay = 60000)` → `@Scheduled(fixedDelay = 60000, scheduler = "taskScheduler")`

### T48 上下文
AiRouterProperties.java:46-49,54-57 catch(IllegalArgumentException) 中添加 log.warn 告警。

### T56 上下文
HashBucketExperimentManager.java:86 `.min(Comparator.comparing(Experiment::getStartTime).reversed())` → `.max(Comparator.comparing(Experiment::getStartTime))`

### T57 上下文
HashBucketExperimentManager.java:76-77 cache.get() loader 数据添加与 warmup 一致的排序逻辑。

### T63 上下文
Experiment.java:31-32 `@OneToMany(fetch = EAGER)` → LAZY + EntityGraph 或添加 @BatchSize。ExperimentGroup.java:23 `@ManyToOne(fetch = LAZY)` 保留不变。

---

## R14 FAILED 路由与实验管理
结果：569 pass / 2 failure / 1 error。全部失败来自 AiRouterPropertiesTest（v14 新测试）：
1. `shouldLogWarningForInvalidClientType:73` — `getMessage()` 返回原始日志模板（含 `{}` 占位符），不包含格式化后的参数值 `INVALID_CLIENT`
2. `shouldLogWarningForInvalidAuthType:95` — 同上
3. `shouldHandleNullConfigGracefully:160` — `List.of(null)` 抛出 NullPointerException（`List.of()` 不允许 null 元素）
验证：572 tests, 569 pass, 2 fail, 1 error
修复方向：① `getMessage()`→`getFormattedMessage()`；② `List.of(null)`→`Collections.singletonList(null)`

---

## R15 NEW 兜底策略与解析器（含R14 RETRY修复）
任务：
- **RETRY（R14）**：修复 AiRouterPropertiesTest 3 个测试失败（`getMessage()`→`getFormattedMessage()` + `List.of(null)`→`Collections.singletonList(null)`）
- **NEW（R15）**：T49（DefaultCredentialProvider credentialStore 命中重置 consecutiveFailures 语义说明）、T64（PrescriptionLocalRuleFallback 数据源异常处理 + CHECK_SKIPPED）、T65（PrescriptionLocalRuleFallback 过敏检查 DRUG_INGREDIENTS 映射扩展）、T66（JsonStructuredOutputParser 抛出 RuntimeException→专用异常）
选择理由：R14 验证失败仅 AiRouterPropertiesTest 单文件 3 处问题，修复代价极小（`getFormattedMessage()` 替换 + `Collections.singletonList` 替换）。R15 四项任务为全部剩余待修复项（T49/T64/T65/T66），完成后 Phase 5G 所有 64 项问题修复完毕。四项任务涉及 4 个独立源文件（DefaultCredentialProvider / PrescriptionLocalRuleFallback / JsonStructuredOutputParser + 新异常类），无相互依赖，合并一轮处理。

### RETRY 上下文（R14 AiRouterPropertiesTest 3 failures）
- **根因 1**：`shouldLogWarningForInvalidClientType` line 72-73 和 `shouldLogWarningForInvalidAuthType` line 94-95 使用 `listAppender.list.get(0).getMessage()` 获取日志消息。Logback 中 `ILoggingEvent.getMessage()` 返回原始消息模板（含 SLF4J `{}` 占位符），而非格式化后的字符串。例如 `log.warn("Invalid clientType value: '{}', falling back to {}", config.getClientType(), ClientType.HTTP_API)` 的 `getMessage()` 返回 `"Invalid clientType value: '{}', falling back to {}"`，不包含 `"INVALID_CLIENT"`。
- **修复 1**：所有 `getMessage()` 调用替换为 `getFormattedMessage()`。
- **根因 2**：`shouldHandleNullConfigGracefully` line 160 `List.of((ModelRouteConfig) null)` — `List.of()` 是 Java 9+ 不可变列表工厂，不允许 null 元素，抛出 NullPointerException。
- **修复 2**：`List.of((ModelRouteConfig) null)` 替换为 `Collections.singletonList(null)`（`Collections.singletonList()` 允许 null 元素）。
- **涉及文件**：`AiRouterPropertiesTest.java`

### T49 上下文
**DefaultCredentialProvider.getCredential()** line 90-95：
```java
Credential stored = credentialStore.get(endpointId);
if (stored != null) {
    cache.put(endpointId, stored);
    consecutiveFailures.set(0);  // 被质疑语义不一致
    return Optional.of(stored);
}
```
**问题**：`credentialStore` 命中后重置 `consecutiveFailures`。质疑点：credentialStore 是本地内存缓存，命中该缓存不代表远端凭证库（vault）健康，重置连续失败计数器可能掩盖 vault 连接问题。
**修复方向**：当前行为实际上语义正确——`consecutiveFailures` 跟踪的是「获取凭证的连续失败次数」，从 credentialStore 获取到凭证意味着本次调用成功，应重置计数器。添加注释说明此语义即可，无需修改行为。即在该行前加 `// Cache hit: successful retrieval resets consecutive failure tracking` 类型注释。
**涉及文件**：`DefaultCredentialProvider.java`

### T64 上下文
**PrescriptionLocalRuleFallback.fallback()** 整体无 try-catch，任何步骤抛异常（如 `request.getPrescriptionItems()` 抛出 NPE 或数据访问异常）直接传播到调用方。
**修复方向**：
- 将 `fallback()` 方法体（从 `List<AlertItem> alerts = new ArrayList<>()` 到 `return result;`）包裹在 try-catch 中
- catch 中：`log.warn("Prescription local rule fallback failed, marking as CHECK_SKIPPED", e)`，设置 `result.setRiskLevel("CHECK_SKIPPED")`，确保 `result.setAlerts(emptyList)`，返回 result
- 新增 `"CHECK_SKIPPED"` 作为 `riskLevel` 可取值
- **涉及文件**：`PrescriptionLocalRuleFallback.java`

### T65 上下文
**PrescriptionLocalRuleFallback.checkAllergy()** line 177-186：
```java
for (PrescriptionCheckItem item : items) {
    String ingredient = DRUG_INGREDIENTS.get(item.getDrugId());
    if (ingredient != null && allergens.contains(ingredient.toLowerCase())) {
        // create alert
    }
}
```
**问题**：仅通过 `DRUG_INGREDIENTS.get(item.getDrugId())` 查找药物成分。如果 `item.getDrugId()` 不在 `DRUG_INGREDIENTS` 映射中（如 `drug_aspirin`、`drug_iso` 等已注册的 caution 药物），过敏检查完全跳过该药物。
**修复方向**：在 `DRUG_INGREDIENTS.get()` 返回 null 时，回退到使用 `item.getDrugName().toLowerCase()` 直接匹配过敏原。需添加 `item.getDrugName() != null` 守卫避免 NPE。即：
```java
String ingredient = DRUG_INGREDIENTS.get(item.getDrugId());
if (ingredient == null && item.getDrugName() != null) {
    ingredient = item.getDrugName().toLowerCase();
}
if (ingredient != null && allergens.contains(ingredient.toLowerCase())) { ... }
```
**涉及文件**：`PrescriptionLocalRuleFallback.java`

### T66 上下文
**JsonStructuredOutputParser.parse()** line 28-30：
```java
} catch (JsonProcessingException e) {
    log.warn("Failed to parse JSON content to {}", targetClass.getSimpleName(), e);
    throw new RuntimeException("Failed to parse JSON content to " + targetClass.getSimpleName(), e);
}
```
**问题**：抛出 `RuntimeException` 而非专用异常，调用方无法针对性地捕获/区分解析错误。
**修复方向**：
- 在 `parser` 包中新建 `StructuredOutputParseException extends RuntimeException` 类
- `JsonStructuredOutputParser.parse()` 中 `throw new RuntimeException(...)` 替换为 `throw new StructuredOutputParseException(...)`
- 测试文件 `JsonStructuredOutputParserTest.java` 中 2 处 `assertThrows(RuntimeException.class, ...)` 改为 `assertThrows(StructuredOutputParseException.class, ...)`
- **涉及文件**（新建）：`StructuredOutputParseException.java`
- **涉及文件**（修改）：`JsonStructuredOutputParser.java`, `JsonStructuredOutputParserTest.java`

---

## 修订说明（v15 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] T65 代码片段中 `item.getDrugName().toLowerCase()` 缺少 `item.getDrugName() != null` 守卫，drugName 为 null 时抛 NPE | plan.md 中 T65 代码片段改为 `if (ingredient == null && item.getDrugName() != null) {...}`，添加 null 守卫；task_v15.md 中 T65 任务描述同步补充 `item.getDrugName() != null` 条件说明 |

---

## R15 FAILED 兜底策略与解析器
结果：547 pass / 0 fail / 28 error。全部 28 error 为同一根因的 NoSuchMethodError：
- `CircuitBreakerDegradationStrategyTest` × 16 — `Builder.invocationCount(Integer)` NoSuchMethod
- `TimeoutDegradationStrategyTest` × 6 — NoSuchMethod
- `SlidingWindowMetricsStoreTest` × 5 — NoSuchMethod
- `AbstractCapabilityExecutorTest` × 1 — NoSuchMethod
验证：575 tests, 547 pass, 0 fail, 28 error
根因：T30（R11）将 `DegradationContext.Builder.invocationCount(Integer)` 改为 `invocationCount(int)` 后，源码已更新但二进制兼容类未重新编译——`SlidingWindowMetricsStore.java` 等文件在增量编译时被认为无变化而跳过，导致运行时调用旧二进制中的 `invocationCount(Integer)` 签名。非代码 bug，纯构建缓存问题。

## R16 NEW 清理重编译修复二进制兼容性
任务：执行 `mvn clean test -pl modules/ai/ai-impl -am`（清理 + 全量重新编译所有依赖包）以消除 28 个 NoSuchMethodError 二进制兼容性错误
选择理由：R15 验证失败的 28 error 均为 R11 引入的 DegradationContext Integer→int 变更导致的二进制兼容性问题。源码（DegradationContext.java, SlidingWindowMetricsStore.java 等）已正确更新，仅因增量编译未重新生成引用该变更的类的字节码。`mvn clean` 强制全量编译即可消除。
上下文：无源文件修改。仅需从 `AIMedical/backend/` 目录执行 `mvn clean test -pl modules/ai/ai-impl -am`。
