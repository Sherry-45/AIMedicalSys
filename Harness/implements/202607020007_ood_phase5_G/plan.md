# 实现计划

任务描述：完成 OOD Phase5_G AI 进阶底座全部代码实现
项目根目录：C:\Develop\Software\AIMedicalSys

---

## 实施路线表格

| # | 批次 | 任务名称 | 涉及文件 | 状态 |
|--|------|---------|---------|------|
| 1 | Batch1 (P0) | ai-api: DegradationReason 枚举 + DegradationStrategy getOrder() + DegradationContext 扩展 | DegradationReason.java, DegradationStrategy.java, DegradationContext.java | ☑ |
| 2 | Batch1 (P0) | SlidingWindowMetricsStore (滑动窗口指标存储) | SlidingWindowMetricsStore.java | ☑ |
| 3 | Batch1 (P0) | CapabilityExecutor 接口 + AbstractCapabilityExecutor 抽象骨架（v3 r1 修订） | CapabilityExecutor.java, AbstractCapabilityExecutor.java + 8 个存根类型 | ☑ |
| 4 | Batch1 (P0) | AiOrchestrator 统一编排层 (实现 AiService 13 方法) | AiOrchestrator.java | ☑ |
| 5 | Batch1 (P0) | TimeoutDegradationStrategy + CircuitBreakerDegradationStrategy | TimeoutDegradationStrategy.java, CircuitBreakerDegradationStrategy.java | ☑ |
| 6 | Batch2 (P0) | 7 项底座能力 CapabilityExecutor (完整管线) | TriageCapabilityExecutor.java, PrescriptionCheckCapabilityExecutor.java, MedicalRecordGenCapabilityExecutor.java, PrescriptionAssistCapabilityExecutor.java, KbQueryCapabilityExecutor.java, ScheduleCapabilityExecutor.java, DiscussionConclusionCapabilityExecutor.java | ☑ |
| 7 | Batch3 (P1) | LLM 调用层 DTO 与枚举 (LlmChatRequest, LlmChatMessage, LlmChatMessageRole, LlmChatOptions, LlmChatResponse, LlmChatUsage, StructuredChatResult, ChatToolDefinition, ClientType, AuthType) | client/ 下全部 DTO 类型 | ☑ |
| 8 | Batch3 (P1) | CredentialProvider + DefaultCredentialProvider + EndpointRateLimiter + LlmInfrastructureException | CredentialProvider.java, DefaultCredentialProvider.java, EndpointRateLimiter.java, LlmInfrastructureException.java, pom.xml | ☑ |
| 9 | Batch3 (P1) | LlmChatService + LlmChatStreamService 接口 + DelegatingLlmChatService | LlmChatService.java, LlmChatStreamService.java, DelegatingLlmChatService.java | ☑ |
| 10 | Batch3 (P1) | HttpApiLlmChatService + HttpApiLlmChatStreamService + SpringAiLlmChatService + SpringAiLlmChatStreamService | HttpApiLlmChatService.java, HttpApiLlmChatStreamService.java, SpringAiLlmChatService.java, SpringAiLlmChatStreamService.java, LlmChatRequest.java, DelegatingLlmChatService.java, AiClientConfig.java | ☑ |
| 11 | Batch4 (P2) | ModelRouter + DefaultModelRouter + ModelRoute + 测试 mock 适配 | ModelRouter.java, DefaultModelRouter.java, ModelRoute.java, AbstractCapabilityExecutor.java, AbstractCapabilityExecutorTest.java, DiscussionConclusionCapabilityExecutorTest.java, TriageCapabilityExecutorTest.java | ☑ |
| 12 | Batch4 (P2) | PromptTemplateManager + DatabasePromptTemplateManager + PromptTemplate + PromptTemplateRepository（v17 r2 修订：含 AbstractCapabilityExecutor 适配 + 3 测试文件 lambda→Mockito mock + H2 + 测试断言同步） | PromptTemplateManager.java, DatabasePromptTemplateManager.java, PromptTemplate.java, TemplateStatus.java, PromptTemplateRepository.java, TemplateChangedEvent.java, AbstractCapabilityExecutor.java, AbstractCapabilityExecutorTest.java, DiscussionConclusionCapabilityExecutorTest.java, TriageCapabilityExecutorTest.java, PromptTemplateTest.java, DatabasePromptTemplateManagerTest.java, ai-impl/pom.xml, AiImplPomCleanDependencyTest.java | ☑ |
| 13 | Batch4 (P2) | ExperimentManager + HashBucketExperimentManager + Experiment + ExperimentGroup + ExperimentRepository + ExperimentAssignment + ExperimentStatus + ExperimentChangedEvent | ExperimentManager.java, HashBucketExperimentManager.java, Experiment.java, ExperimentGroup.java, ExperimentRepository.java, ExperimentAssignment.java, ExperimentStatus.java, ExperimentChangedEvent.java, ExperimentAssignmentTest.java, HashBucketExperimentManagerTest.java | ☑ |
| 14 | Batch4 (P2) | AiMetricsCollector + LoggingMetricsCollector + AiCallLogEntity + AiCallLogRepository + AiCallLogStats + AiCallLogStatsRepository | LoggingMetricsCollector.java, AiCallLogEntity.java, AiCallLogRepository.java, AiCallLogStats.java, AiCallLogStatsRepository.java, AiClientConfig.java | ☑ |
| 15 | Batch4 (P2) | ModelEndpointHealthManager | ModelEndpointHealthManager.java, EndpointHealthState.java, AbstractCapabilityExecutor.java, AbstractCapabilityExecutorTest.java, DiscussionConclusionCapabilityExecutorTest.java, TriageCapabilityExecutorTest.java, EndpointHealthStateTest.java, ModelEndpointHealthManagerTest.java | ☑ |
| 16 | Batch5 (P0) | ai-api dto/base/ 子包: AiRequestBase + Phase4ServiceMeta + Phase4ServiceMetaCapable + Phase4BusinessException + CallContext | dto/base/AiRequestBase.java, Phase4ServiceMeta.java, Phase4ServiceMetaCapable.java, Phase4BusinessException.java, CallContext.java | ☑ |
| 17 | Batch5 (P0) | 6 项薄适配器 CapabilityExecutor (Phase4 委托) | DiagnosisCapabilityExecutor.java, AnalysisReportForInspectionCapabilityExecutor.java, AnalysisReportForLabTestCapabilityExecutor.java, ImageAnalysisCapabilityExecutor.java, RecommendExaminationCapabilityExecutor.java, RecommendExecutionOrderCapabilityExecutor.java | ☑ |
| 18 | Batch6 (P3) | AiPlatformConfig + AiPlatformEnvironmentPostProcessor + 7 @ConfigurationProperties + 4 线程池 + DegradationStrategy @Component + ModelRouteConfig | AiPlatformConfig.java, AiPlatformEnvironmentPostProcessor.java, AiExecutionProperties.java, AiDegradationProperties.java, AiRateLimitingProperties.java, AiMetricsAsyncProperties.java, AiPlatformProperties.java, AiSlidingWindowProperties.java, AiTemplateProperties.java, ModelRouteConfig.java, spring.factories, TimeoutDegradationStrategy.java, CircuitBreakerDegradationStrategy.java, AiRouterProperties.java, DefaultModelRouter.java, AiClientConfig.java(删除) | ☑ |
| 19 | Batch6 (P3) | FallbackAiService 构造器迁移 (ObjectProvider + @Primary) | FallbackAiService.java (refactor) | ☑ |
| 20 | Batch7 (P3) | StructuredOutputParser + JsonStructuredOutputParser | StructuredOutputParser.java, JsonStructuredOutputParser.java | ☑ |
| 21 | Batch7 (P3) | PrescriptionLocalRuleFallback（新建） | PrescriptionLocalRuleFallback.java, PrescriptionLocalRuleFallbackTest.java | ☑ |

---

## R26 PASSED Task 15: ModelEndpointHealthManager
结果：实现 EndpointHealthState 枚举（CONNECTED/DEGRADED/UNAVAILABLE）、ModelEndpointHealthManager 完整状态机（ConcurrentHashMap + Atomic* 线程安全、tryProbe 30s 探测窗口、recordCallResult 状态转换）；修改 AbstractCapabilityExecutor 行 359-363 String → EndpointHealthState 枚举比较，新增 tryProbe 分支；适配 3 个测试文件 ~20 处 mock（String→枚举）
测试：verify_v21.md PASSED — 901 测试通过，0 失败

## R26 NEW Task 20: StructuredOutputParser + JsonStructuredOutputParser
任务：在 ai-impl/parser/ 包创建 JsonStructuredOutputParser 实现类（Jackson ObjectMapper 驱动），实现 StructuredOutputParser 接口的 parse() 方法；保留现有 StructuredOutputParser 接口不变
选择理由：Batch7 P3 首项。StructuredOutputParser 是 LLM 结构化输出解析的接口（被 AbstractCapabilityExecutor 标准管线引用），JsonStructuredOutputParser 是唯一实现，零外部代码依赖（仅需已有 Jackson + ObjectMapper），可独立测试。底层依赖优先原则，先实现解析层再推动上层回退逻辑
上下文：依赖 StructuredOutputParser 接口（已有存根，无需修改）、Jackson ObjectMapper（已有依赖，被 ai-impl/pom.xml 和父 POM 管理）；无新增外部依赖

---

## R1 REVIEW_REVISED Task 1: ai-api Degradation 基础设施 + SlidingWindowMetricsStore
任务：在 ai-api 模块新增 DegradationReason 枚举、扩展 DegradationStrategy 接口(DegradationReason.java 新增枚举，DegradationStrategy.java 新增 default int getOrder() 方法)、扩展 DegradationContext 字段；在 ai-impl/metrics 模块新增 SlidingWindowMetricsStore
选择理由：所有降级判定和指标采集的基石，无前置依赖，底层优先
上下文：ai-api 已有 DegradationStrategy.java 和 DegradationContext.java 骨架，ai-impl 已有 NoOpDegradationStrategy.java
审查反馈：[plan_review_v1_r1.md] REJECTED - 3 个问题（DegradationContext 字段类型 Integer/int、DegradationReason 枚举值不完整、EventType 命名），均已修订，详见 task_v1.md 修订说明

## R1 RETRY Task 1 (r2 修订)
原因：[plan_review_v1_r2.md] REJECTED - 1 个问题（THIN_ADAPTER_DELEGATE_ERROR 与设计文档 §3.8 定义不一致）
修订：移除 THIN_ADAPTER_DELEGATE_ERROR 枚举常量，恢复设计文档 8 个标准常量；薄适配器异常统一使用 INFRASTRUCTURE_ERROR + ":subType" 模式

## R1 REVIEW_REVISED (r3 计划修订)
原因：[plan_review_v1_r3.md] REJECTED - 1 个问题（Batch 3 缺失 SpringAiLlmChatService 和 SpringAiLlmChatStreamService）
修订：扩展 road map 表 task 10，将 SpringAiLlmChatService 和 SpringAiLlmChatStreamService 与 HttpApi 实现并列列出，涉及文件补充 SpringAiLlmChatService.java 和 SpringAiLlmChatStreamService.java

## R1 FAILED Task 1+2: Degradation + SlidingWindowMetricsStore
原因：[verify_v1.md] FAILED - 1 测试失败
- 失败用例：`buildDegradationContextShouldReturnMaxLastFailureTimeForMultipleFailures`
- 根因：测试断言 `ctx2.getLastFailureTime() > first`（严格大于），但连续两次 `recordFailure` 在同一毫秒内发生时，`System.currentTimeMillis()` 返回相同值，导致 `lastFailureTime` 相等而非严格大于
- 修正方向：将断言从 `>` 改为 `>=`，消除时间精度依赖

---

## R2 PASSED Task 1+2: 修复测试断言 + 变量命名清理
任务：修复 `SlidingWindowMetricsStoreTest.java` 中时间精度相关断言，同步修复 `SlidingWindowMetricsStore.java` 中变量命名（code review 建议）
结果：`SlidingWindowMetricsStoreTest.java` 断言 `>` → `>=`；`SlidingWindowMetricsStore.java` 变量 `totalElapsedDegraded`/`degradedEventCount` → `totalElapsedNonFailure`/`nonFailureEventCount`
测试：verify_v2.md PASSED — 全部 484 测试通过，0 失败

---

## R3 NEW Task 3: CapabilityExecutor 接口 + AbstractCapabilityExecutor 抽象骨架
任务：在 ai-impl/orchestrator/ 包新增 CapabilityExecutor<T,R> 泛型接口定义 execute()/getCapabilityId()/getInputType()/getOutputType() 方法签名，以及 AbstractCapabilityExecutor<T,R> 抽象骨架类封装降级预检模板方法、超时兜底、doExecuteInternal() 抽象方法、doDegrade() 辅助方法、doExtractDepartmentId/VisitId/PatientId/SessionId 提取方法、extractVariables/extractOutputSummary 默认实现
选择理由：CapabilityExecutor 是 AiOrchestrator 路由的核心接口，AbstractCapabilityExecutor 是所有 13 项能力执行器的公共基类，Pipeline 依赖的基石。无前置依赖（仅依赖 ai-api 现有类型 AiResult/DegradationContext/DegradationReason）
上下文：依赖 SlidingWindowMetricsStore（已完成）、AiResult.java（现有）、DegradationReason.java（已完成）、DegradationContext.java（已完成）

## R3 REVIEW_REVISED Task 3 (v3 r1 修订)
审查：[plan_review_v3_r1.md] REJECTED — 3 个问题
- [严重] 8 个类型编译期不存在（PromptTemplateManager、ModelRouter、LlmChatService、StructuredOutputParser、AiMetricsCollector、ModelEndpointHealthManager、LocalRuleFallback、AiRequestBase）
- [一般] 计划摘要遗漏 executeStandardPipeline、isKnownPhase4BusinessException、refineTimeoutReason、knownPhase4Packages
- [一般] 计划未提及测试规划

修订内容：
1. 编译修复（Option A）：在前置步骤创建 8 个最小存根类型（7 个在 ai-impl 子包，1 个在 ai-api/dto/base/），确保编译通过
2. 补充 missing 成员说明（executeStandardPipeline() 占位方法、isKnownPhase4BusinessException() 方法、refineTimeoutReason() 方法、knownPhase4Packages 静态字段）
3. 补充测试规划（Mockito 框架、降级链 Mock、超时测试方法、防御性拷贝测试、7 类覆盖说明）

涉及文件：CapabilityExecutor.java、AbstractCapabilityExecutor.java + 8 个存根类型文件：
- ai-impl: template/PromptTemplateManager.java, router/ModelRouter.java, client/LlmChatService.java, parser/StructuredOutputParser.java, metrics/AiMetricsCollector.java, metrics/ModelEndpointHealthManager.java, fallback/LocalRuleFallback.java
- ai-api: dto/base/AiRequestBase.java

---

## R4 PASSED Task 3: CapabilityExecutor + AbstractCapabilityExecutor
结果：实现 CapabilityExecutor<T,R> 泛型接口、AbstractCapabilityExecutor<T,R> 抽象骨架（含 execute() 模板方法、降级预检、超时兜底、防御性拷贝、doExecuteInternal/doDegrade/doExtract* 等完整行为契约）、7 个 ai-impl 存根 + 1 个 ai-api 存根
测试：[verify_v3.md](verify_v3.md) PASSED — 全部 518 测试通过，0 失败

## R4 NEW Task 5: TimeoutDegradationStrategy + CircuitBreakerDegradationStrategy
任务：在 ai-impl/degradation/ 包新增 TimeoutDegradationStrategy 和 CircuitBreakerDegradationStrategy 两个具体降级策略实现
选择理由：具体降级策略是 CapabilityExecutor 降级预检管线的核心组件，P0 优先级，前置依赖（DegradationContext、SlidingWindowMetricsStore）均已完成
上下文：依赖 DegradationContext（已完成）、SlidingWindowMetricsStore（已完成）、DegradationStrategy接口（已完成）

---

## R5 PASSED Task 5: TimeoutDegradationStrategy + CircuitBreakerDegradationStrategy
结果：实现 TimeoutDegradationStrategy（基于平均耗时触发降级）和 CircuitBreakerDegradationStrategy（基于失败率触发熔断降级，含 CLOSED/OPEN/HALF_OPEN 状态机、per-capability probeLock、recordProbeResult）；同步修复 SlidingWindowMetricsStore.buildDegradationContext 中 serviceName 赋值
测试：verify_v4.md PASSED — 全部 22 测试通过，0 失败（ai-api 6 + ai-impl 43 用例）

## R5 NEW Task 4: AiOrchestrator 统一编排层
任务：在 ai-impl/orchestrator/ 包新增 AiOrchestrator 类，实现 AiService 接口全部 13 个方法，通过 handle() 统一委托模式路由到 CapabilityExecutor；包含 executorMap 构建（@PostConstruct 扫描 List<CapabilityExecutor>）、异常兜底处理、SlidingWindowMetricsStore 指标记录
选择理由：AiOrchestrator 是底座入口编排层，P0 最高优先级，所有前置依赖均已就绪（CapabilityExecutor/AbstractCapabilityExecutor、SlidingWindowMetricsStore、Timeout/CircuitBreakerDegradationStrategy）
上下文：依赖 CapabilityExecutor<T,R>（已完成）、AbstractCapabilityExecutor（已完成）、SlidingWindowMetricsStore（已完成）、DegradationStrategy 体系（已完成）、AiService 接口（已有）、AiMetricsCollector（存根、空接口）

---

## R6 PROCEED (R5 验证通过)
验证报告：verify_v4.md PASSED — Task 5（TimeoutDegradationStrategy + CircuitBreakerDegradationStrategy）全部 22 测试通过，0 失败
推进至：Task 4 — AiOrchestrator 统一编排层（已在 R5 定义为 NEW，沿用 task_v5.md 指令）

---

## R6 PASSED Task 4: AiOrchestrator 统一编排层
结果：实现 AiOrchestrator 类，实现 AiService 13 方法，通过 executorMap 路由到 CapabilityExecutor，含同步异常保护与指标记录
测试：verify_v5.md PASSED — 全部 87 测试通过，0 失败

## R7 REVIEW_REVISED Task 7: LLM 调用层 DTO 与枚举
任务：在 ai-impl/client/ 包创建 LLM 客户端层 9 个 DTO/枚举文件（含 LlmChatUsage 内嵌静态类），同步修改 LlmChatService.java 新增 chat() 和 structuredChat() 方法签名；所有 DTO 支持 Jackson 序列化
选择理由：Task 7 零外部代码依赖（纯数据类），是 Batch3 P1 LLM 调用层的基础。Task 6（7 项底座能力执行器）依赖 LLM 客户端接口及 DTO，底层优先原则先实现 DTO 层
上下文：无代码依赖；设计文档 §3.2 完整定义了每个类型的字段级契约
审查反馈：[plan_review_v7_r1.md] REJECTED — 3 个问题（缺失 LlmChatService.java 同步更新、缺少 Jackson 序列化约束、文件计数不精确），均已修订，详见 task_v7.md 修订说明

---

## R8 PASSED Task 7: LLM 调用层 DTO 与枚举
结果：创建 9 个 DTO/枚举类型（含内嵌 LlmChatUsage），修改 LlmChatService.java 新增 chat()/structuredChat() 方法签名；所有类型支持 Jackson 序列化
测试：verify_v7.md PASSED — 607 测试通过，0 失败（common 225 + ai-api 175 + ai-impl 211）

## R8 NEW Task 6: 7 项底座能力 CapabilityExecutor（完整管线）
任务：在 ai-impl/orchestrator/ 包新增 7 个 CapabilityExecutor 具体实现类，每个继承 AbstractCapabilityExecutor，实现 doExecuteInternal() 管线逻辑
选择理由：Task 6 是 Batch2 P0 核心业务执行器，所有前置依赖均已就绪（AbstractCapabilityExecutor、降级策略、SlidingWindowMetricsStore、LLM DTO 类型 + 全部 8 个基础设施存根）
上下文：依赖 AbstractCapabilityExecutor（已完成所有模板方法和 7 个存根类型）、LlmChatService 存根、ModelRouter 存根、PromptTemplateManager 存根、StructuredOutputParser 存根、AiMetricsCollector 存根、ModelEndpointHealthManager 存根、LocalRuleFallback 存根、AiRequestBase 存根

---

## R8 REVIEW_REVISED Task 6 (v8 r1 修订)
审查：[plan_review_v8_r1.md] REJECTED — 2 个问题
- [严重] 存根接口缺失管线所需方法签名（5 个存根均缺少管线调用方法），导致编译失败
- [一般] 管线伪代码步骤 1-2（实验分流）与类设计不一致：方法签名已接收 promptVersion/sentinelReason 作为参数，但伪代码要求管线内部重新提取

修订内容：
1. 给 5 个存根类型添加最小方法签名：PromptTemplateManager.render()、ModelRouter.route()、ModelEndpointHealthManager.getState()、StructuredOutputParser.parse()、AiMetricsCollector.record()
2. 从 executeStandardPipeline() 伪代码中移除步骤 1-2（实验分流），明确由 doExecuteInternal() 在调用前准备 promptVersion/sentinelReason 值后传入
3. 更新 task_v8.md 涉及文件清单（修改 1→6 个）及任务描述和上下文说明
4. 移除对 ExperimentManager 的依赖要求（不属于本管线职责）

涉及文件：同 R8 NEW + 5 个存根类型文件修改

---

## R9 REVIEW_REVISED Task 6 (v8 r2 修订)
审查：[plan_review_v8_r2.md](plan_review_v8_r2.md) REJECTED — 6 个问题
- [严重] AbstractCapabilityExecutor 构造器参数数量不匹配（计划 16，实际 17 — 多一个 ObjectMapper）
- [严重] executeStandardPipeline() 中 CompletableFuture 阻塞语义未指定（get/join/异常策略未澄清）
- [一般] promptVersion/sentinelReason 来源未按执行器逐一定义
- [一般] doDegrade() 中 TODO 未纳入本任务范围
- [轻微] DiscussionConclusion "多 2 参数" 应为"多 3 参数"
- [轻微] 管线步骤 6 60/40 分配未考虑 parseTimeoutConfig

修订内容：
1. 所有"16 参数"更正为"17 参数"（含 ObjectMapper）；DiscussionConclusion 参数计数更正为 20（17+3）
2. 明确使用 `.get(remainingTimeout, TimeUnit)` 阻塞获取，补充所有受检异常处理路径，澄清外层 orTimeout 为兜底
3. 为每个执行器在表格中新增 promptVersion 来源列（@Value 注入 + 默认值）和 sentinelReason 来源列
4. 将清理 TODO 纳入涉及文件清单，要求在 doDegrade() 中将 TODO 替换为 metricsCollector.record() 实际调用
5. "多 2 参数" → "多 3 参数"
6. 优先使用 parseTimeoutConfig/parseTimeoutDefault，仅当两者均不可用时才回退到 60/40 固定比例

涉及文件：同 R8 NEW + 5 个存根类型修改 + AbstractCapabilityExecutor.doDegrade() TODO 清理

---

## R10 FAILED Task 6 (v8 验证失败)
验证报告：[verify_v8.md](verify_v8.md) FAILED — 8 个 test-compile 错误
原因：`StructuredOutputParser` 接口包含泛型方法 `<T> T parse(String, Class<T>)`，测试代码中使用 lambda 表达式（如 `(rawContent, targetClass) -> "parsed"`）时，Java 编译器无法从 lambda 上下文中推断泛型类型参数 `<T>`，导致编译失败。
影响文件（全部在 ai-impl test 目录）：
1. `AbstractCapabilityExecutorTest.java` — 7 处 lambda（lines 597, 641, 885, 934, 978, 1024, 1070）
2. `DiscussionConclusionCapabilityExecutorTest.java` — 1 处 lambda（line 310）

## R10 RETRY Task 6 (修复 test-compile 错误)
任务：将上述 8 处 `StructuredOutputParser` 的 lambda 表达式替换为匿名内部类实现，确保编译器可以正确解析泛型方法 `<T> T parse(String, Class<T>)`
修正方向摘要：
- 对于返回值的 lambda：`(rawContent, targetClass) -> "parsed"` → `new StructuredOutputParser() { @Override @SuppressWarnings("unchecked") public <T> T parse(String rawContent, Class<T> targetClass) { return (T) "parsed"; } }`
- 对于抛异常的 lambda：`(rawContent, targetClass) -> { throw new RuntimeException("parse error"); }` → 匿名类中 throw
- 对于返回复杂类型的 lambda：`(rawContent, targetClass) -> new DiscussionConclusionResponse()` → 匿名类中 `(T) new DiscussionConclusionResponse()`

---

## R11 PASSED Task 6: 7 项底座能力 CapabilityExecutor (完整管线)
结果：修复 8 处 StructuredOutputParser 泛型 lambda test-compile 错误 + 1 处 metricsStore null NPE；生产代码无需修改
测试：[verify_v9.md](verify_v9.md) PASSED — 264 测试通过，0 失败

## R11 NEW Task 8: CredentialProvider + EndpointRateLimiter + LlmInfrastructureException
任务：在 ai-impl/client/ 包新增 CredentialProvider 接口（含内嵌 Credential 值对象 + CredentialProviderState 枚举）、EndpointRateLimiter 类（基于 Guava 令牌桶）、LlmInfrastructureException 异常类（extends RuntimeException）
选择理由：Task 8 是 Batch3 P1 LLM 调用层的基础设施组件，前置依赖（DTO 类型 ClientType/AuthType）均已完成。CredentialProvider 提供端点认证凭据查询，EndpointRateLimiter 提供令牌桶限流，LlmInfrastructureException 作为基础设施异常分类——三者均是 LlmChatService 实现类和 DelegatingLlmChatService 的必要依赖
上下文：依赖 ClientType.java（已完成）、AuthType.java（已完成）、LlmChatService.java 存根（已完成）

---

## R12 PROCEED (验证通过，推进 Task 8)
验证报告：[verify_v9.md](verify_v9.md) PASSED — Task 6（7 项底座能力 CapabilityExecutor）全部 264 测试通过，0 失败
推进至：Task 8 — CredentialProvider + EndpointRateLimiter + LlmInfrastructureException（沿用 task_v10.md 指令）

---

## R12 REVIEW_REVISED Task 8: CredentialProvider + EndpointRateLimiter + LlmInfrastructureException
审查：[plan_review_v10_r2.md](plan_review_v10_r2.md) REJECTED — 4 个问题
- [严重] Caffeine 依赖缺失
- [严重] Guava 依赖缺失
- [一般] AiPlatformConfig 尚未就绪
- [一般] Vault 查询抽象缺失

修订内容：
1. ai-impl/pom.xml 补充 Caffeine + Guava 依赖声明
2. EndpointRateLimiter 改用 @Value 直接注入，AiPlatformConfig 统一装配推迟至 Task 18
3. 新增 DefaultCredentialProvider 默认内存实现（含完整状态机 + 缓存 + 测试辅助方法）
4. 更新 task_v10.md 涉及文件表（新增 DefaultCredentialProvider.java + DefaultCredentialProviderTest.java + pom.xml 修改）
5. 末尾追加修订说明表格

涉及文件：同 R11 NEW + DefaultCredentialProvider.java + DefaultCredentialProviderTest.java + ai-impl/pom.xml（修改）

---

## R13 PASSED Task 8: CredentialProvider + EndpointRateLimiter + LlmInfrastructureException
结果：实现 CredentialProvider 接口（含内嵌 Credential 值对象 + CredentialProviderState 枚举）、DefaultCredentialProvider（ConcurrentHashMap + Caffeine Expiry + 完整状态机）、EndpointRateLimiter（Guava RateLimiter + @Value + Environment 多维配置）、LlmInfrastructureException + CredentialUnavailableException；修改 pom.xml 添加 Caffeine + Guava 依赖
测试：verify_v10.md PASSED — 304 测试通过，0 失败（common 225 + ai-impl 79）

## R13 REVIEW_REVISED Task 9: LlmChatService + LlmChatStreamService 接口 + DelegatingLlmChatService
审查：[plan_review_v11_r1.md](plan_review_v11_r1.md) REJECTED — 4 个问题
- [严重] AiAbilityInputInvalidException 不存在 — Task v11 要求 LlmChatStreamService "违反约束时返回 Flux.error(AiAbilityInputInvalidException)"，但该异常类不存在
- [严重] reactor-core 编译期依赖缺失 — ai-impl/pom.xml 未声明 reactor-core
- [一般] 未提及测试规划
- [轻微] DelegatingLlmChatService 关键契约未在计划中体现（unmodifiableMap、@PostConstruct 校验、回退）

修订内容：
1. 新增 AiAbilityInputInvalidException 异常类到 ai-impl/client/exception/ 包，沿用现有 RuntimeException 子类模式
2. 在 ai-impl/pom.xml 添加 reactor-core 依赖（scope compile，参考设计文档 §8.2 的 optional=true 说明，当前任务阶段声明为 compile 确保编译通过）
3. 补充测试规划：LlmChatStreamServiceTest（反射验证接口契约 + Flux 返回类型）和 DelegatingLlmChatServiceTest（分发/回退/防御性封装/启动校验）
4. 补充 DelegatingLlmChatService 关键行为约束：Collections.unmodifiableMap 防御性封装、@PostConstruct 校验收发验证、默认回退机制及日志级别

涉及文件补充：
- 新建 ai-impl/.../client/exception/AiAbilityInputInvalidException.java
- 修改 ai-impl/pom.xml（reactor-core 依赖）

---

## R13 REVIEW_REVISED Task 9 (v11 r2 修订)
审查：[plan_review_v11_r2.md](plan_review_v11_r2.md) REJECTED — 3 个问题
- [严重] DelegatingLlmChatService 构造器签名导致 Spring 无法自动装配（Map<ClientType, LlmChatService> 不支持自动注入）
- [一般] DelegatingLlmChatServiceTest 遗漏 structuredChat 分发验证
- [轻微] 防御性拷贝未明确（未指定先 new HashMap<>() 再 unmodifiableMap）

修订内容：
1. 参照 AiOrchestrator 模式，DelegatingLlmChatService 构造器改为接收 `List<LlmChatService> allServices`；新增 `LlmChatService.getClientType()` 接口方法，在 `@PostConstruct` 中构建 `Map<ClientType, LlmChatService>`；跳过自身（`service == this`）
2. 测试规划补充 structuredChat 分发验证（targetClass 参数透传的 Mockito verify 断言）
3. 明确防御性拷贝：先 `new HashMap<>(delegates)` 再 `Collections.unmodifiableMap()`

涉及文件补充：
- 修改 `LlmChatService.java`（新增 `ClientType getClientType()` 方法签名，使 DelegatingLlmChatService 可在 @PostConstruct 中识别各实现的客户端类型）

## R13 NEW Task 9: LlmChatService + LlmChatStreamService 接口 + DelegatingLlmChatService
任务：在 ai-impl/client/ 包创建 LlmChatStreamService 接口（chatStream → Flux<LlmChatResponse>）和 DelegatingLlmChatService 类（@Primary，按 clientType 分发至 delegates）；同步修改 LlmChatService 接口新增 getClientType() 方法
选择理由：Task 9 是 Batch3 P1 LLM 调用层的接口与分发层，是 Task 10（HttpApi + SpringAi 实现类）的直接前置依赖。所有前置依赖均已就绪（DTO 类型、CredentialProvider、EndpointRateLimiter、LlmChatService 存根）
上下文：依赖 LlmChatService 接口（已有存根，需新增 getClientType() 方法）、LlmChatRequest 的 clientType 字段、ClientType 枚举、CredentialProvider（已完成）、EndpointRateLimiter（已完成）

---

## R14 FAILED Task 9: 实现 + 测试已通过，pom 依赖计数测试失败
验证报告：[verify_v11.md](verify_v11.md) FAILED — 1 测试失败
- 失败用例：`AiImplPomCleanDependencyTest.totalDependenciesCountShouldBeSeven`
- 根因：Task 9 向 `ai-impl/pom.xml` 添加了 `reactor-core` 依赖，总依赖数从 7 增至 8，但该测试硬编码断言 `assertEquals(7, ...)` 未同步更新
- 修正方向：将 `AiImplPomCleanDependencyTest.java:63` 断言从 `assertEquals(7, ...)` 改为 `assertEquals(8, ...)`
- 涉及文件：仅 `AiImplPomCleanDependencyTest.java`（测试文件，修改 1 行断言）

---

## R15 PASSED Task 9: LlmChatService + LlmChatStreamService + DelegatingLlmChatService (RETRY)
结果：Assertion 修正 `assertEquals(7,...)` → `assertEquals(8,...)`，同时保持生产代码零变更；其他 Module 725 用例全部通过，0 失败，5 跳过
测试：[verify_v12.md](verify_v12.md) PASSED — 725 测试通过，0 失败

## R15 NEW Task 10: HttpApiLlmChatService + HttpApiLlmChatStreamService + SpringAiLlmChatService + SpringAiLlmChatStreamService
任务：在 ai-impl/client/ 包新增 4 个 LLM 调用实现类——HttpApiLlmChatService（LlmChatService 实现，HTTP 直连同步）、SpringAiLlmChatService（LlmChatService 实现，Spring AI 存根）、HttpApiLlmChatStreamService（LlmChatStreamService 实现，HTTP 流式同步存根）、SpringAiLlmChatStreamService（LlmChatStreamService 实现，Spring AI 流式存根）；同步修改 DelegatingLlmChatService 构造器（从 List 注入改为 Map 注入以匹配 AiPlatformConfig @Bean 装配模式）
选择理由：Task 10 是 Batch3 P1 LLM 调用层的最终实现层，所有前置依赖均已就绪（LlmChatService 接口、LlmChatStreamService 接口、ClientType 枚举、CredentialProvider、EndpointRateLimiter）
上下文：依赖 LlmChatService 接口（已完成）、LlmChatStreamService 接口（已完成）、CredentialProvider（已完成）、EndpointRateLimiter（已完成）、ClientType（已完成）

---

## R16 REVIEW_REVISED Task 10: v13 r1 审议修订
审查：[plan_review_v13_r1.md](plan_review_v13_r1.md) REJECTED — 4 个问题
- [严重] endpointId 来源缺失 — LlmChatRequest 不存在 endpointId 字段，阻塞凭据获取和限流
- [严重] DelegatingLlmChatService 改造未纳入任务范围 — plan.md 已列出但 task_v13.md 遗漏
- [一般] 新实现无法被 DelegatingLlmChatService 发现 — 无 @Service 导致无法自动注入
- [一般] endpointId 缺失阻塞测试编写

修订内容：
1. **endpointId 来源**（选项 A）：在 LlmChatRequest 新增 `endpointId` 字段 + 构造器参数 + getter，由 CapabilityExecutor 在构造 request 时从 ModelRoute 设置
2. **DelegatingLlmChatService 重构**：构造器改为 `Map<ClientType, LlmChatService>`，移除 `@Service`/`@Primary`，移除 `initDelegates()` 方法
3. **AiClientConfig 临时配置**：新增 `@Configuration` 类，@Bean 注册 4 个实现 + DelegatingLlmChatService（标记 @Primary）；Task 18 AiPlatformConfig 将吸收该配置
4. **测试规划**：HttpApiLlmChatServiceTest 覆盖 7 条路径（clientType/credential缺失/限流/HTTP异常/success/structuredChat抛异常），SpringAi 存根验证 UnsupportedOperationException

涉及文件补充（较 R15 NEW 新增）：
- 新建 `LlmChatRequest.java`（修改）
- 新建 `AiClientConfig.java`（临时 @Configuration）
- 修改 `DelegatingLlmChatService.java`（重构）
- 4 个测试文件（含 HttpApiLlmChatServiceTest 等）

---

## R16 REVIEW_REVISED Task 10 (v13 r2 修订)
审查：[plan_review_v13_r2.md](plan_review_v13_r2.md) REJECTED — 2 个问题
- [严重] DelegatingLlmChatServiceTest.java 未纳入涉及文件，重构后将编译失败
- [一般] 测试规划未覆盖重构后 DelegatingLlmChatService 行为契约

修订内容：
1. 在"全部涉及文件"表增加第 12 行：DelegatingLlmChatServiceTest.java（修改）
2. 在测试规划中新增 Test 5: DelegatingLlmChatServiceTest，覆盖 9 个测试方法（构造/分发/回退/防御性拷贝/异常传播）

涉及文件补充（较 R16 NEW 新增）：
- 修改 `DelegatingLlmChatServiceTest.java`（适配 Map 构造器、移除 initDelegates 测试）

---

## R17 PASSED Task 10: HttpApiLlmChatService + SpringAiLlmChatService implementations
结果：实现 4 个 LLM 实现类、LlmChatRequest endpointId 字段、DelegatingLlmChatService Map 重构、AiClientConfig @Configuration、AiResult.failure() 工厂方法；同步修复 AbstractCapabilityExecutor/DiscussionConclusionCapabilityExecutor 中 LlmChatRequest 4→5 参数构造器调用
测试：verify_v13.md PASSED — 2251 测试通过，0 失败

## R17 NEW Task 16: ai-api dto/base/ subpackage（Phase4ServiceMeta + Phase4ServiceMetaCapable + Phase4BusinessException + CallContext）
任务：在 ai-api/dto/base/ 包新增 4 个类型（Phase4ServiceMeta 值对象、Phase4ServiceMetaCapable 接口、Phase4BusinessException 抽象异常基类、CallContext 不可变值对象），确认 AiRequestBase 已有骨架满足当前需求
选择理由：Batch5 P0 优先于 Batch4 P2。这些类型是 Phase 4 服务集成契约的基础设施，Task 17（6 项薄适配器 CapabilityExecutor）的直接前置依赖
上下文：依赖 AiRequestBase（已有骨架）；4 个新类型零外部依赖（纯数据/接口/异常类型），均在 ai-api 模块内。设计文档 §3.1 详细定义了每个类型的字段级契约和约束

---

## R18 PASSED Task 16: ai-api dto/base/ subpackage
结果：实现 Phase4ServiceMeta（元数据值对象）、Phase4ServiceMetaCapable（可选接口）、Phase4BusinessException（抽象异常基类）、CallContext（不可变业务上下文值对象）；确认 AiRequestBase 无需修改；新增 jackson-databind test 依赖
测试：verify_v14.md PASSED — 2269 测试通过，0 失败；test_v14.md 18 测试方法覆盖全部契约

## R18 NEW Task 17: 6 项薄适配器 CapabilityExecutor（Phase 4 委托）
任务：在 ai-impl/orchestrator/impl/ 包新增 6 个 CapabilityExecutor 实现（DiagnosisCapabilityExecutor、AnalysisReportForInspectionCapabilityExecutor、AnalysisReportForLabTestCapabilityExecutor、ImageAnalysisCapabilityExecutor、RecommendExaminationCapabilityExecutor、RecommendExecutionOrderCapabilityExecutor），每个继承 AbstractCapabilityExecutor，实现降级预检→委托 Phase 4 服务→指标采集的薄适配器管线；重写 doExtractDepartmentId/doExtractVisitId/doExtractPatientId/doExtractSessionId 从 RequestContext 提取
选择理由：Batch5 P0。所有前置依赖均已就绪（AbstractCapabilityExecutor、Phase4BusinessException、CallContext、Phase4ServiceMeta/Phase4ServiceMetaCapable）
上下文：依赖 AbstractCapabilityExecutor（已完成所有模板方法和 17 参数构造器）、Phase4BusinessException（已完成）、AiMetricsCollector 存根、SlidingWindowMetricsStore（已完成）、LocalRuleFallback 存根

---

## R18 REVIEW_REVISED Task 17 (v15 r1 审议修订)
审查：[plan_review_v15_r1.md](plan_review_v15_r1.md) REJECTED — 4 个问题
- [严重] RequestContextUtils 不存在导致 doExtract* 编译错误
- [严重] AiCallRecord.success()/failure() 静态工厂方法不存在
- [一般] Phase 4 服务类型引用矛盾（主模板用具体类型但 type 不存在）
- [轻微] isDtoEmpty 反射策略脆弱（非业务方法干扰）

修订内容：
1. 本任务提前创建 RequestContextUtils.java（基于 Spring RequestContextHolder 的最小实现），加入涉及文件清单
2. AiCallRecord.success/failure 替换为 new AiCallRecord(...) 直接构造，移除 LocalDateTime 参数
3. 构造器参数从具体类型改为 @Autowired(required = false) Object，doExecuteInternal 通过反射调用 Phase 4 服务
4. isDtoEmpty 从反射方法计数改为 package 前缀检测

涉及文件（较 R18 NEW 新增）：
- 新建 RequestContextUtils.java（ai-impl/util/）

---

## R18 REVIEW_REVISED Task 17 (v15 r2 修订)
审查：[plan_review_v15_r2.md](plan_review_v15_r2.md) REJECTED — 3 个问题
- [严重] llmCallExecutor=null 导致 supplyAsync NPE（JDK screenExecutor 硬校验）
- [一般] mock(Object.class) 反射桩不可用（Mockito 代理类不含 execute 方法）
- [轻微] @Autowired Object 临时方案的后续风险需记录

修订内容：
1. **llmCallExecutor null 处理**（方案 1，最小改动）：修改 AbstractCapabilityExecutor.execute()，`llmCallExecutor == null` 时使用 `ForkJoinPool.commonPool()` 作为默认 Executor；将 AbstractCapabilityExecutor.java 纳入本任务涉及文件
2. **测试策略修正**：移除 mock(Object.class) 路径，仅保留匿名类反射桩方式，补充完整可编译的匿名类示例代码（正常返回 + 异常抛出 + 超时测试辅助）
3. **@TODO Phase5: 标记**：6 个薄适配器类注释和 doExecuteInternal 注释添加 `@TODO Phase5:` 标记，提示回归时替换为具体服务接口 + @Qualifier；isDtoEmpty 方法同样添加标记

涉及文件（较 R18 REVIEW_REVISED（r1）新增）：
- 修改 AbstractCapabilityExecutor.java（execute() 中 llmCallExecutor==null 回退 commonPool）
- 修改 6 个薄适配器类注释（添加 @TODO Phase5: 标记）

---

## R19 PASSED Task 17: 6 项薄适配器 CapabilityExecutor（Phase4 委托）
结果：实现 6 个薄适配器 CapabilityExecutor、RequestContextUtils 工具类；修改 AbstractCapabilityExecutor.execute() llmCallExecutor==null 回退 commonPool；8 处 Test StructuredOutputParser 泛型 lambda → 匿名内部类
测试：[verify_v15.md](verify_v15.md) PASSED — 2305 测试通过，0 失败

## R19 NEW Task 11: ModelRouter + DefaultModelRouter + ModelRoute
任务：在 ai-impl/router/ 包创建 ModelRoute 值对象，修改 ModelRouter 接口返回类型为 ModelRoute，实现 DefaultModelRouter 默认路由实现（AtomicReference 路由表、@PostConstruct 初始化、权重随机选择、@Scheduled 热刷新）；同步修改 AbstractCapabilityExecutor 使用 ModelRoute 字段
选择理由：Batch4 P2 首项，ModelRouter 是底座管线路由核心组件（被 AbstractCapabilityExecutor 和 7 个完整管线执行器直接依赖），无前置外部依赖（仅依赖已有 ClientType/AuthType 枚举），实现后可解锁后续 ModelEndpointHealthManager 集成
上下文：依赖 ModelRouter.java（现有存根接口，需修改返回类型）、ClientType.java（已有）、AuthType.java（已有）、AbstractCapabilityExecutor.java（需修改 routeResult 处理逻辑）

## R19 REVIEW_REVISED Task 11: ModelRouter + DefaultModelRouter + ModelRoute
审查：[plan_review_v16_r1.md](plan_review_v16_r1.md) REJECTED — 3 个问题
- [严重] 测试文件修改未纳入计划 — 3 个测试文件 ~18+ 处 ModelRouter mock 需从返回 String 改为返回 ModelRoute.of("model-1")
- [一般] ModelRoute.of(String modelId) 便捷工厂方法未纳入
- [轻微] 路线表涉及文件列仅列出 3 个文件，未包含 AbstractCapabilityExecutor.java 及 3 个测试文件

修订内容：
1. 路线表 row 11 涉及文件列扩展为 7 个（ModelRouter.java, DefaultModelRouter.java, ModelRoute.java, AbstractCapabilityExecutor.java, AbstractCapabilityExecutorTest.java, DiscussionConclusionCapabilityExecutorTest.java, TriageCapabilityExecutorTest.java）
2. ModelRoute 值对象定义中明确标注需提供 `public static ModelRoute of(String modelId)` 便捷工厂方法
3. 任务描述补充 18+ mock 更新范围说明：测试文件 mock 全部从 `(capId, req) -> "model-1"` 改为 `(capId, req) -> ModelRoute.of("model-1")`

涉及文件（较 R19 NEW 新增）：
- 修改 `AbstractCapabilityExecutorTest.java`（~16 处 mock 适配）
- 修改 `DiscussionConclusionCapabilityExecutorTest.java`（~3 处 mock 适配）
- 修改 `TriageCapabilityExecutorTest.java`（~1 处 mock 适配）

---

## R19 REVIEW_REVISED Task 11: ModelRouter + DefaultModelRouter + ModelRoute (v16 r2 修订)
审查：[plan_review_v16_r2.md](plan_review_v16_r2.md) REJECTED — 6 个问题
- [严重] `options.setClientType()` 编译错误 — LlmChatOptions 类无此方法
- [严重] 新建生产代码缺少测试文件规划（ModelRouteTest、DefaultModelRouterTest）
- [一般] AbstractCapabilityExecutor L383 `String.valueOf(routeResult)` 遗漏
- [一般] DefaultModelRouter 配置源未明确
- [一般] 测试 mock 计数不精确（~16 → 15，共 19 处）
- [轻微] routeResult.getClientType() 空值安全未提及

修订内容：
1. 所有代码伪代码中的 `options.setClientType()` 改为修改 `LlmChatRequest` 构造器第 3 参数 + null 安全回退（`routeResult.getClientType() != null ? ... : ClientType.HTTP_API`）
2. 涉及文件表新增 ModelRouteTest.java（值对象契约）、DefaultModelRouterTest.java（权重/并发/生命周期）、AiRouterProperties.java（@ConfigurationProperties 存根）
3. AbstractCapabilityExecutor 修改范围从 L352-L375 扩展到 L352-L387，新增"修改点 3"覆盖超时降级路径 L381-L383 的 `String.valueOf(routeResult)` → `routeResult.getModelId()`
4. 实施要点明确 AiRouterProperties 存根方案：`@ConfigurationProperties(prefix = "ai.router")` 持有 `Map<String, List<ModelRoute>> routes`
5. 修正 mock 计数为 15（AbstractCapabilityExecutorTest）+ 3 + 1 = 19
6. 补充 clientType null 安全回退策略

涉及文件（较 R19 REVIEW_REVISED（r1）新增）：
- 新建 `router/AiRouterProperties.java`（@ConfigurationProperties 存根）
- 新建 `router/ModelRouteTest.java`（测试）
- 新建 `router/DefaultModelRouterTest.java`（测试）

---

## R20 PASSED Task 11: ModelRouter + DefaultModelRouter + ModelRoute
结果：实现 ModelRouter 接口（返回 ModelRoute）、ModelRoute 值对象（含 of() 工厂、防御性拷贝、equals/hashCode）、DefaultModelRouter（AtomicReference 路由表、@PostConstruct 初始化、权重随机选择、@Scheduled 定时刷新）、AiRouterProperties 存根；修改 AbstractCapabilityExecutor 3 处适配 ModelRoute 字段访问（getEndpointId/getModelId/getClientType）；适配 19 处测试 mock（String → ModelRoute.of()）
测试：[verify_v16.md](verify_v16.md) PASSED — 2318 测试通过，0 失败

## R20 NEW Task 12: PromptTemplateManager + DatabasePromptTemplateManager + PromptTemplate + PromptTemplateRepository
任务：在 ai-impl/template/ 包实现 Prompt 模板管理层（5 个类型 + 1 个事件类 + 2 个测试文件），同步修改 PromptTemplateManager 接口签名适配 4 参数契约，更新所有调用方 mock（lambda→Mockito mock）；修改 AbstractCapabilityExecutor.executeStandardPipeline() 中 render 调用；修改 ai-impl/pom.xml 添加 spring-boot-starter-data-jpa 依赖
选择理由：Batch4 P2 次序第二项。PromptTemplateManager 被 7 项底座能力 CapabilityExecutor 直接依赖（管线模板渲染步骤引用），且是本批次首个需要 JPA 持久化的组件，先打通 JPA 依赖可解锁后续 ExperimentManager 等同样依赖 JPA 的任务
上下文：依赖 PromptTemplateManager 存根（已完成，需修改接口签名）、JPA（需新增依赖）、Caffeine（已有依赖）

---

## R20 REVIEW_REVISED Task 12 (v17 r1 审议修订)
审查：[plan_review_v17_r1.md](plan_review_v17_r1.md) REJECTED — 2 个问题
- [严重] AbstractCapabilityExecutor 生产代码调用未更新，导致编译失败
- [严重] 接口新增 getFallbackPrompt() 后无法使用 Lambda 模拟

修订内容：
1. 将 AbstractCapabilityExecutor.java 纳入涉及文件，executeStandardPipeline() 中 render(templateKey, variables) → render(capabilityId, departmentId, variables, promptVersion)，移除 templateKey 局部变量
2. 3 个测试文件 ~20 处 lambda 替换为 Mockito mock(PromptTemplateManager.class) + when().thenReturn() 模式；新增 import static org.mockito.*
3. 同步更新 task_v17.md 中"已有代码上下文"说明、"测试 mock 更新范围"、实施要点及涉及文件表

涉及文件（较 R20 NEW 新增）：
- 修改 orchestrator/AbstractCapabilityExecutor.java（1 处 render 调用适配）
- 修改 AbstractCapabilityExecutorTest.java（~16 处 lambda→Mockito mock）
- 修改 DiscussionConclusionCapabilityExecutorTest.java（3 处 lambda→Mockito mock）
- 修改 TriageCapabilityExecutorTest.java（1 处 lambda→Mockito mock）

---

## R20 REVIEW_REVISED Task 12 (v17 r2 修订)
审查：[plan_review_v17_r2.md](plan_review_v17_r2.md) REJECTED — 3 个问题
- [严重] AiImplPomCleanDependencyTest 断言未同步更新（当前 `assertEquals(9,...)`，新增 spring-boot-starter-data-jpa + h2 后总依赖数变 11）
- [一般] 路线表 row 12 涉及文件列不完整（遗漏 TemplateStatus.java、TemplateChangedEvent.java、PromptTemplateTest.java、DatabasePromptTemplateManagerTest.java、ai-impl/pom.xml）
- [轻微] 未考虑 H2 测试依赖

修订内容：
1. **测试断言同步**：`AiImplPomCleanDependencyTest.java:63` 从 `assertEquals(9,...)` → `assertEquals(11,...)`；纳入涉及文件清单
2. **路线表补全**：row 12 涉及文件列补充 TemplateStatus.java、TemplateChangedEvent.java、PromptTemplateTest.java、DatabasePromptTemplateManagerTest.java、ai-impl/pom.xml、AiImplPomCleanDependencyTest.java
3. **H2 依赖**：ai-impl/pom.xml test scope 添加 `com.h2database:h2`（版本由父 POM `<h2.version>2.2.224</h2.version>` 管理）

涉及文件（较 R20 REVIEW_REVISED (r1) 新增）：
- 修改 `pom/AiImplPomCleanDependencyTest.java`（断言 9→11）
- 修改 `ai-impl/pom.xml`（补充 h2 test 依赖）

---

## R21 PASSED Task 12: PromptTemplateManager + DatabasePromptTemplateManager
结果：实现 TemplateStatus 枚举、PromptTemplate JPA 实体、PromptTemplateRepository、TemplateChangedEvent、DatabasePromptTemplateManager（Caffeine 缓存 + @PostConstruct 预热 + @EventListener 失效）；修改 PromptTemplateManager 接口签名及所有调用方 mock（16+3+1 处 lambda→Mockito mock）；修改 AbstractCapabilityExecutor 中 render 调用适配 4 参数；修改 pom.xml 添加 spring-boot-starter-data-jpa + h2；修改 AiImplPomCleanDependencyTest 断言 9→11
测试：[verify_v17.md](verify_v17.md) PASSED — 402 测试通过，0 失败

## R21 NEW Task 13: ExperimentManager + HashBucketExperimentManager + Experiment + ExperimentGroup + ExperimentRepository + ExperimentAssignment + ExperimentStatus + ExperimentChangedEvent
任务：在 ai-impl/experiment/ 包创建 A/B 实验管理层（8 个类型（含 1 个事件类）+ 2 个测试文件）
选择理由：Batch4 P2 次序第三项。ExperimentManager 是底座管线实验分流组件（被 7 项底座能力 CapabilityExecutor 在 doExecuteInternal() 管线步骤中引用），且已具备 JPA 依赖（Task 12 已引入 spring-boot-starter-data-jpa + h2），无新增外部依赖
上下文：依赖 JPA（task 12 已完成）、Caffeine（已有依赖），不依赖其他实验组件外的底座类型

---

## R22 REVIEW_REVISED Task 13: ExperimentManager + HashBucketExperimentManager
审查：[plan_review_v18_r1.md](plan_review_v18_r1.md) REJECTED — 3 个问题
- [一般] 路线表 row 13 "涉及文件"列不完整（缺少 ExperimentStatus.java、ExperimentChangedEvent.java、ExperimentAssignmentTest.java、HashBucketExperimentManagerTest.java）
- [一般] Experiment.groups @OneToMany LAZY 加载策略未明确，存在 LazyInitializationException 运行时风险
- [轻微] "8 个类型 + 1 个事件类" 计数描述可优化

修订内容：
1. 路线表 row 13 "涉及文件"列补齐为 10 个文件（含 ExperimentStatus.java、ExperimentChangedEvent.java、ExperimentAssignmentTest.java、HashBucketExperimentManagerTest.java）
2. Experiment.groups 获取策略明确为 `FetchType.EAGER`（因 Experiment 始终与 groups 一起使用；Caffeine 缓存无 @Transactional 上下文，LAZY 会导致 LazyInitializationException）
3. 任务描述计数改为"8 个类型（含 1 个事件类）+ 2 个测试文件"消除歧义

涉及文件（较 R21 NEW 不变，补充完整清单）：
- 新建（10 文件）：ExperimentManager.java, ExperimentAssignment.java, ExperimentStatus.java, Experiment.java, ExperimentGroup.java, ExperimentRepository.java, ExperimentChangedEvent.java, HashBucketExperimentManager.java, ExperimentAssignmentTest.java, HashBucketExperimentManagerTest.java

---

## R22 REVIEW_REVISED Task 13: ExperimentManager + HashBucketExperimentManager (v18 r2 修订)
审查：[plan_review_v18_r2.md](plan_review_v18_r2.md) REJECTED — 2 个问题
- [一般] HashBucketExperimentManager.assign() 对同一 capabilityId 存在多个 ACTIVE 实验时的选择策略未定义
- [轻微] ExperimentGroup.@ManyToOne experiment 未指定 fetch 策略，默认 EAGER 与 Experiment.groups EAGER 形成双向 EAGER

修订内容：
1. **多个 ACTIVE 实验选择策略**：补充约定"同一 capability 同时最多一个 ACTIVE 实验（管理端保证）"；实现侧若出现多个则按 startTime 降序取第一条，确保选择确定性。已在 task_v18.md HashBucketExperimentManager 小节添加该策略
2. **@ManyToOne fetch 策略**：ExperimentGroup.experiment 字段明确为 `@ManyToOne(fetch = FetchType.LAZY)`，避免双向 EAGER 导致不必要的联表查询。已在 task_v18.md 预期文件清单 Item 5 及 ExperimentGroup 字段定义中同步更新

涉及文件：
- 无新增文件，仅 task_v18.md 内容修订（3 处修改 + 追加修订说明）

---

## R22 REVIEW_REVISED Task 13: ExperimentManager + HashBucketExperimentManager (v18 r3 修订)
审查：[plan_review_v18_r3.md](plan_review_v18_r3.md) REJECTED — 2 个一般问题 + 1 个轻微问题
- [一般] 路线表 row 13 "涉及文件"列仍不完整（缺少 4 个文件）
- [一般] hash 计算可能产生负值，违反 [0,1000) 区间约定
- [轻微] dbExceptionShouldReturnDefault 测试执行路径未明确

修订内容：
1. 路线表 row 13 "涉及文件"列已补齐为 10 个文件（此前 R22 r1 修订已更新，无需重复修改）
2. hash 计算改为 `Math.floorMod(hashCode(), 1000)`，结果恒为非负；测试规划新增 `shouldHandleNegativeHashValue` 覆盖负边界
3. 测试规划中补充 dbExceptionShouldReturnDefault 触发方式说明：@PostConstruct 前 mock Repository 抛异常，或预热后 @EventListener 清空缓存制造 cache miss

涉及文件：
- 无新增文件，仅 task_v18.md 内容修订（3 处修改 + 追加修订说明）

---

## R22 REVIEW_REVISED Task 13: ExperimentManager + HashBucketExperimentManager (v18 r4 修订)
审查：[plan_review_v18_r4.md](plan_review_v18_r4.md) REJECTED — 1 个严重问题
- [严重] ExperimentRepository 缺少 `findByStatus(ExperimentStatus)` 查询方法。HashBucketExperimentManager.warmup() 需要"查询全部 ACTIVE 实验（含 ExperimentGroup），预填充缓存"，但当前 3 个查询方法均需 capabilityId 或时间范围作为过滤条件，无法直接返回全部 ACTIVE 实验

修订内容：
1. ExperimentRepository 新增 `List<Experiment> findByStatus(ExperimentStatus status);` 方法，与 PromptTemplateRepository.findByStatus(TemplateStatus) 模式一致，用于 warmup() 查询全部 ACTIVE 实验

涉及文件：
- 修改 task_v18.md ExperimentRepository 查询方法定义（新增第 1 行 + 追加修订说明 v18 r4）

---

## R23 PASSED Task 13: ExperimentManager + HashBucketExperimentManager
结果：实现 ExperimentManager 接口、ExperimentAssignment 不可变值对象、ExperimentStatus 枚举、Experiment/ExperimentGroup JPA @Entity、ExperimentRepository（4 个查询方法）、ExperimentChangedEvent 事件类、HashBucketExperimentManager（Caffeine 缓存 + Math.floorMod 哈希分桶 + @PostConstruct warmup + @EventListener 缓存失效）
测试：verify_v18.md PASSED — 425 测试通过，0 失败

## R23 NEW Task 14: AiMetricsCollector + LoggingMetricsCollector + AiCallLogEntity + AiCallLogRepository + AiCallLogStats + AiCallLogStatsRepository
任务：在 ai-impl/metrics/ 包创建指标采集体系：LoggingMetricsCollector 实现（@Async 异步写入 + JPA 持久化）、AiCallLogEntity JPA @Entity（与现有 AiCallRecord 字段对等）、AiCallLogRepository、AiCallLogStats JPA @Entity（月度聚合统计）、AiCallLogStatsRepository
选择理由：Batch4 P2 次序末项，AiMetricsCollector 是底座管线指标采集核心组件（被 AbstractCapabilityExecutor 和全部 13 项能力执行器直接依赖），JPA 依赖已就绪（Task 12 已引入 spring-boot-starter-data-jpa + h2）
上下文：依赖 AiMetricsCollector 存根接口（已完成，void record(AiCallRecord) 方法签名）、AiCallRecord 值对象（已完成，15 字段构造器）、JPA（Task 12 已完成）、@Async Spring 异步支持（已有默认配置）

## R23 REVIEW_REVISED Task 14 (v19 r1 修订)
审查：[plan_review_v19_r1.md](plan_review_v19_r1.md) REJECTED — 2 个问题
- [严重] @Async("metricsAsyncExecutor") 无对应 Bean 导致启动失败
- [一般] AiCallLogEntity 字段映射策略未定义

修订内容：
1. @Async("metricsAsyncExecutor") → @Async（无 qualifier），使用 Spring 默认 applicationTaskExecutor；专用线程池命名推迟至 Task 18 AiPlatformConfig
2. 新增 AiCallRecord → AiCallLogEntity 字段映射表，逐字段标注直接复制/null/派生策略（含 promptVersion 的 Integer.parseInt null 安全转换说明）
3. plan.md 路线表 row 14 涉及文件列从笼统的"metrics/ 下全部类型"改为具体 5 个新建文件路径

涉及文件：无新增/修改，仅 task_v19.md 内容修订（3 处修改 + 追加修订说明）

---

## R23 REVIEW_REVISED Task 14 (v19 r2 修订)
审查：[plan_review_v19_r2.md](plan_review_v19_r2.md) REJECTED — 2 个问题
- [严重] @EnableAsync 缺失导致 @Async 无法生效 — Spring Boot 不会自动启用 @EnableAsync，整个代码库无任何 @EnableAsync 声明
- [一般] AiCallLogEntity 遗漏 userId 字段 — AiCallRecord 包含 userId（第 9 行 getter），但 AiCallLogEntity 字段列表和映射表均未涵盖

修订内容：
1. **@EnableAsync 方案 A**：在已有 `AiClientConfig.java` 类级别添加 `@EnableAsync` 注解，使 LoggingMetricsCollector.record() 上的 @Async 真正生效。路线表 row 14 "涉及文件"列补充 AiClientConfig.java；不变约定补充 @EnableAsync 配置说明；实施要点第 4 条写明修改方式
2. **AiCallLogEntity 补充 userId**：字段列表 + 映射表补充 userId（String，可空，值来源 `record.getUserId()`），实施要点第 5 条说明；同步更新 AiCallLogEntityTest 断言

涉及文件：
- 修改 `task_v19.md`（4 处修改 + 追加修订说明 v19 r2）
- 生产文件新增 `AiClientConfig.java`（修改，添加 @EnableAsync）

---

## R24 FAILED Task 14: AiMetricsCollector + LoggingMetricsCollector + AiCallLogEntity (v19 验证失败)
验证报告：[verify_v19.md](verify_v19.md) FAILED — 455 测试运行，1 失败
- 失败用例：`LoggingMetricsCollectorTest.shouldSaveEntityWhenRecordCalled:57`
- 根因：测试在第 43 行使用 `"v2"` 作为 promptVersion 参数，但 `parsePromptVersion("v2")` 执行 `Integer.parseInt("v2")` 抛出 NumberFormatException，返回 null。`assertEquals(Integer.valueOf(2), entity.getPromptVersion())` 因此得到 `<null>` 而非 `<2>`。
- 涉及文件：仅 `LoggingMetricsCollectorTest.java`（第 43 行参数 `"v2"` → `"2"`）

## R24 RETRY Task 14: 修复 LoggingMetricsCollectorTest 测试数据
任务：将 LoggingMetricsCollectorTest.shouldSaveEntityWhenRecordCalled() 中 promptVersion 参数从 `"v2"` 改为 `"2"`，确保 parseInt 正常解析
修正方向摘要：
- LoggingMetricsCollectorTest.java:43: `"v2"` → `"2"`

---

## R25 PASSED Task 14: AiMetricsCollector + LoggingMetricsCollector + AiCallLogEntity
结果：修复 LoggingMetricsCollectorTest 测试数据 `"v2"` → `"2"`，消除 v19 验证唯一失败用例；生产代码零变更
测试：[verify_v20.md](verify_v20.md) PASSED — 455 测试通过，0 失败

## R25 NEW Task 15: ModelEndpointHealthManager
任务：在 ai-impl/metrics/ 包实现 ModelEndpointHealthManager（端点健康管理），含完整状态机（CONNECTED/DEGRADED/UNAVAILABLE）及三个具体方法
选择理由：Batch4 P2 最后一项，ModelEndpointHealthManager 是底座管线的端点健康监控组件（被 AbstractCapabilityExecutor 和 ModelRouter 引用），所有前置依赖均已就绪（SlidingWindowMetricsStore、DegradationContext）
上下文：依赖 ModelRouter（已完成）、SlidingWindowMetricsStore（已完成）、DegradationContext（已完成）；无新增外部依赖

## R25 REVIEW_REVISED Task 15: ModelEndpointHealthManager (v21 r1 修订)
审查：[plan_review_v21_r1.md] REJECTED — 4 个严重问题 + 1 个一般问题
- [严重] 状态模型错误：plan.md R25 NEW 使用 UP/DOWN/RECOVERING，task_v21.md §1 要求 CONNECTED/DEGRADED/UNAVAILABLE
- [严重] 方法签名缺失：计划仅提"健康探测抽象方法"，task_v21.md §2 定义了 getState()/tryProbe()/recordCallResult() 三个具体方法及完整状态转换规则
- [严重] AbstractCapabilityExecutor 适配未纳入计划：task_v21.md §3 要求修改行 359-363 String→枚举比较 + tryProbe 分支
- [严重] 测试文件完全缺失：task_v21.md §4 要求 5 个测试文件新建/修改（EndpointHealthStateTest 3 类、ModelEndpointHealthManagerTest 9 场景含并发、3 个已有测试 mock 适配）
- [一般] 详细计划过于笼统

修订内容：
1. 状态模型修正：UP/DOWN/RECOVERING → CONNECTED/DEGRADED/UNAVAILABLE（与 task_v21.md §1 一致）
2. 方法定义明确：补充 getState(String)→EndpointHealthState、tryProbe(String)→boolean、recordCallResult(String,boolean,long)→void 三个方法签名，附完整状态转换表
3. AbstractCapabilityExecutor 适配纳入：涉及文件补充 AbstractCapabilityExecutor.java（行 359-363 从 String 比较改为枚举比较 + tryProbe 分支），"已有代码上下文"同步补充说明
4. 测试规划完整：明确 EndpointHealthStateTest.java（枚举常量 / ordinal / null 安全）、ModelEndpointHealthManagerTest.java（9 用例含 20 线程并发）、3 个已有测试文件 mock 适配范围
5. 详细计划扩展为完整的实施要点章节（见下方补充内容）

涉及文件：
- 修改 plan.md（R25 NEW + R25 REVIEW_REVISED 追加）
- task_v21.md 追加修订说明（保留前序内容不变）

### 实施要点补充

**EndpointHealthState 枚举**：
- 三个常量：CONNECTED（正常）、DEGRADED（性能退化）、UNAVAILABLE（不可用）
- 位置：ai-impl/metrics/EndpointHealthState.java

**ModelEndpointHealthManager**：
- 每个 endpointId 维护独立状态，内部数据结构使用 ConcurrentHashMap<String, EndpointState>
- EndpointState 包含：AtomicReference<EndpointHealthState> healthState、AtomicInteger consecutiveSlowCalls/consecutiveFailures/cumulativeFailures/consecutiveSuccesses、AtomicLong lastProbeTime、AtomicLong lastSlowCallThresholdMs（默认 5000ms）
- 方法 getState(String)：首次访问自动注册为 CONNECTED，返回当前状态
- 方法 tryProbe(String)：UNAVAILABLE 时距上次探测 >= 30 秒返回 true 并更新 lastProbeTime，否则 false
- 方法 recordCallResult(String, boolean, long)：触发状态转换，使用 synchronized 或 CAS 保证原子性

**状态转换规则**：
- CONNECTED → DEGRADED：连续 3 次耗时 > 阈值（5000ms）
- CONNECTED → UNAVAILABLE：连续 5 次失败（success=false）
- DEGRADED → CONNECTED：连续 3 次正常（耗时 < 阈值）
- DEGRADED → UNAVAILABLE：累积失败 >= 5
- UNAVAILABLE → CONNECTED：tryProbe=true 且探测成功
- UNAVAILABLE → UNAVAILABLE：探测失败（重置 30 秒计时器）

**AbstractCapabilityExecutor 适配**（行 359-363）：
- getState 返回类型从 String → EndpointHealthState
- !"HEALTHY".equals(healthState) → healthState == EndpointHealthState.UNAVAILABLE
- UNAVAILABLE 时先调 tryProbe，能探测则超时阈值减半后放行，不能探测则 doDegrade(ENDPOINT_UNAVAILABLE)

**文件操作**：
| 操作 | 文件路径 |
|------|---------|
| 新建 | ai-impl/metrics/EndpointHealthState.java |
| 重写 | ai-impl/metrics/ModelEndpointHealthManager.java |
| 修改 | ai-impl/orchestrator/AbstractCapabilityExecutor.java（行 359-363） |
| 新建 | ai-impl/metrics/EndpointHealthStateTest.java |
| 新建 | ai-impl/metrics/ModelEndpointHealthManagerTest.java |
| 修改 | ai-impl/orchestrator/AbstractCapabilityExecutorTest.java（~16 处 mock） |
| 修改 | ai-impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java（~3 处） |
| 修改 | ai-impl/orchestrator/impl/TriageCapabilityExecutorTest.java（~1 处） |

---

## R27 PASSED Task 20: StructuredOutputParser + JsonStructuredOutputParser
结果：实现 JsonStructuredOutputParser（Jackson ObjectMapper 驱动），8 个测试方法全部通过
验证：verify_v22.md 显示 FAILED（775 通过，1 失败），但唯一失败为 `LoginAttemptTrackerTest.shouldResetUsernameFailuresWhenWindowExpires:240`（common 模块预先存在的 flaky 测试，与 AI 底座任务无关），Task 20 自身 8 个测试全部通过，生产代码零缺陷

## R27 NEW Task 21: PrescriptionLocalRuleFallback（新建）
任务：在 ai-impl/fallback/ 包实现 PrescriptionLocalRuleFallback（LocalRuleFallback 接口的具体实现），执行 5 项本地规则检查（配伍禁忌、剂量范围、重复用药、过敏史、儿童/孕妇用药警示）；白名单模式处理数据源异常
选择理由：Batch7 P3 第二项。Task 20（StructuredOutputParser）已完成，PrescriptionLocalRuleFallback 仅依赖已有的 LocalRuleFallback 接口和 PrescriptionCheckRequest/Response DTO，零外部新增依赖；基于 hardcode 内存数据实现 5 项预设规则检查，不涉及 JSON 解析
上下文：依赖 LocalRuleFallback 接口（现有，不修改）、PrescriptionCheckRequest/PrescriptionCheckResponse（现有 ai-api DTO 类型）、PrescriptionCheckCapabilityExecutor（合成管线已在 doDegrade() 中调用 localRuleFallback.fallback(request)）

## R27 REVIEW_REVISED Task 21: PrescriptionLocalRuleFallback（v23 r2 修订）
审查：[plan_review_v23_r2.md] REJECTED — 1 个问题
- [一般] PrescriptionLocalRuleFallback 缺少 Spring 注解声明（`@Service`），PrescriptionCheckCapabilityExecutor 的 `@Autowired` 构造器期望 Spring 容器注入此实现，但无 `@Service`/`@Component` 时不会被组件扫描发现，抛出 `NoSuchBeanDefinitionException`

修订内容：
1. 类签名添加 `@Service` 注解（遵循项目已有 CapabilityExecutor 模式），确保 Spring 容器自动注册该 Bean，使 PrescriptionCheckCapabilityExecutor 的构造器注入成功
2. task_v23.md 类签名更新为 `@Service public class PrescriptionLocalRuleFallback`，末尾追加修订说明表格

---

## R28 PASSED Task 21: PrescriptionLocalRuleFallback
结果：实现 PrescriptionLocalRuleFallback（5 项本地规则检查：配伍禁忌、剂量范围、重复用药、过敏史、特殊人群）；通过 @Service 注册为 Spring Bean
测试：verify_v23.md PASSED — 20 测试通过，0 失败

## R28 REVIEW_REVISED Task 18: AiPlatformConfig + AiPlatformEnvironmentPostProcessor (v24 r1 修订)
审查：[plan_review_v24_r1.md](plan_review_v24_r1.md) REJECTED — 9 个问题（5 严重 + 3 一般 + 1 轻微）
- [严重] DegradationStrategy Bean 未注册（Timeout/CircuitBreaker 缺 @Component）
- [严重] @Value("${...}") 无法注入 Map<String, Duration>
- [严重] 缺失 7 个 @ConfigurationProperties 属性类
- [严重] 缺失 4 个线程池 @Bean（llmCallExecutor/metricsAsyncExecutor/transcriptSummaryExecutor/scheduledTaskExecutor）
- [严重] 缺失 @EnableJpaRepositories + @EntityScan
- [一般] @Value 在 @Bean 方法上语法不正确
- [一般] chatFallbackTimeout 未定义
- [一般] 缺失测试规划
- [轻微] AiRouterProperties @TODO 未处理

修订内容：
1. TimeoutDegradationStrategy/CircuitBreakerDegradationStrategy 添加 @Component 注解（纳入涉及文件）
2. 废弃 @Value 注入 Map 方案，改为 @ConfigurationProperties 体系：新建 7 个属性类（AiExecutionProperties/AiDegradationProperties/AiRateLimitingProperties/AiMetricsAsyncProperties/AiPlatformProperties/AiSlidingWindowProperties/AiTemplateProperties），AiPlatformConfig 标注 @EnableConfigurationProperties 引入
3. 新增 4 个线程池 @Bean 定义（llmCallExecutor/metricsAsyncExecutor/transcriptSummaryExecutor/scheduledTaskExecutor）
4. AiPlatformConfig 类形态补充 @EnableJpaRepositories + @EntityScan(basePackages = "com.aimedical.modules.ai.impl")
5. 明确 chatFallbackTimeout = capabilityTimeoutConfig.get(capabilityId)
6. 新建 ModelRouteConfig 可变 POJO 解决 AiRouterProperties YAML 绑定限制；更新 DefaultModelRouter 适配
7. 补充测试规划（AiPlatformConfigTest 6 方法 + AiPlatformEnvironmentPostProcessorTest 4 方法）
8. 删除 AiClientConfig.java（被 AiPlatformConfig 完全吸收）
9. 无新增 Maven 依赖，AiImplPomCleanDependencyTest 断言 11 不变

涉及文件：详见 task_v24.md 涉及文件清单（11 新建 + 4 修改 + 1 删除）

## R28 NEW Task 18: AiPlatformConfig + AiPlatformEnvironmentPostProcessor (原始计划)
任务：在 ai-impl/config/ 包创建 AiPlatformConfig（@Configuration 底座配置装配类）和 AiPlatformEnvironmentPostProcessor（配置转发前置处理器）；通过 META-INF/spring.factories 注册 EnvironmentPostProcessor
选择理由：Batch6 P3 剩余首项。AiPlatformConfig 是底座统一配置装配类，负责 Bean 装配、配置校验、定时刷新；AiPlatformEnvironmentPostProcessor 负责启动期 ai.platform.enabled → ai.mock.enabled 转发。Task 19（FallbackAiService 构造器迁移）依赖 AiPlatformConfig 的 ai.platform.enabled 属性概念
上下文：依赖全部已完成的基础设施组件（SlidingWindowMetricsStore、AbstractCapabilityExecutor、AiOrchestrator、HttpApiLlmChatService、ModelRouter、PromptTemplateManager、ExperimentManager、AiMetricsCollector、ModelEndpointHealthManager、StructuredOutputParser 等）；将吸收现有的 AiClientConfig @Configuration（Task 10 临时配置）和 AiRouterProperties 存根（Task 11）

---

## R29 FAILED Task 18: AiPlatformConfig + AiPlatformEnvironmentPostProcessor (v24 验证失败)
验证报告：[verify_v24.md](verify_v24.md) FAILED — ai-impl test-compile 14 个错误，425 测试通过（common 225 + ai-api 200），0 失败
原因：AbstractCapabilityExecutor 构造器参数从 `Map<String, Duration>` / `Duration` 改为 `AtomicReference<Map<String, Duration>>` / `AtomicReference<Duration>` 后，10 个测试文件（13 处调用）仍传递原始 Map/Duration 类型导致编译错误；AiRouterProperties.setRoutes() 参数类型从 `Map<String, List<ModelRoute>>` 改为 `Map<String, List<ModelRouteConfig>>` 后，DefaultModelRouterTest 1 处传递 ModelRoute 类型不匹配
涉及修复文件（全部为测试文件，生产代码零变更）：
| 文件 | 错误行 | 修复方式 |
|------|--------|---------|
| AbstractCapabilityExecutorTest.java | 1471 | 匿名类构造器代理参数类型同步 |
| TriageCapabilityExecutorTest.java | 85, 104 | 2 处 `Map.of()` → `new AtomicReference<>(Map.of())` |
| DiscussionConclusionCapabilityExecutorTest.java | 273, 336, 375, 415 | 4 处 `Map.of()` → `new AtomicReference<>(Map.of())` |
| DiagnosisCapabilityExecutorTest.java | 95 | `Map.of()` → `new AtomicReference<>(Map.of())` |
| AnalysisReportForInspectionCapabilityExecutorTest.java | 95 | 同上 |
| AnalysisReportForLabTestCapabilityExecutorTest.java | 95 | 同上 |
| ImageAnalysisCapabilityExecutorTest.java | 95 | 同上 |
| RecommendExaminationCapabilityExecutorTest.java | 95 | 同上 |
| RecommendExecutionOrderCapabilityExecutorTest.java | 95 | 同上 |
| DefaultModelRouterTest.java | 20 | ModelRoute → ModelRouteConfig |
修正方向：10 个测试文件共 14 处类型不匹配，全部使用 `new AtomicReference<>(...)` 包装或替换测试数据类型即可修复，不涉及生产代码修改

## R29 FAILED Task 18 (v25 RETRY 验证失败)
验证报告：[verify_v25.md](verify_v25.md) FAILED — 516 测试运行，13 失败，6 错误，0 跳过
**分类根因分析：**
1. **[生产代码] AiPlatformConfigTest.initShouldSucceedWithValidConfiguration:54** — `config.thinAdapterPerCapabilityConfig().get().get("cap1")` 返回 null 而非 PT30S。根因：`thinAdapterPerCapabilityConfigRef` 仅存储 per-capability 值（`execProps.getThinAdapter().getPerCapability()`），不包含默认值。测试期望"cap1"获取默认 PT30S 但实现未填充默认值。涉及文件：`AiPlatformConfigTest.java` lines 54-55（错误期望）
2. **[生产代码] 6 个薄适配器 Executor 测试 NPE** — `resolveThinAdapterTimeout()` 在 `DiagnosisCapabilityExecutor.java:173` 调用 `thinAdapterPerCapabilityConfig.get()` 时，字段为 null 导致 NPE。根因：v24 将字段类型从 `Map<String, Duration>` 改为 `AtomicReference<Map<String, Duration>>` 后，`resolveThinAdapterTimeout()` 改为调用 `.get()` 但未添加字段级别的 null 检查。薄适配器测试代码构造器传入 `null` 作为第 8 参，v25 未修改此 null。
   涉及文件：`AbstractCapabilityExecutor.java:525`（`resolveTimeout` 中 `thinAdapterPerCapabilityConfig.get()` 同样缺少 null 安全）、`DiagnosisCapabilityExecutor.java:173` 及 6 个相似薄适配器测试文件
修正方向：
- **Fix A (测试 1 文件)**：AiPlatformConfigTest.java lines 54-55 期望修正——`thinAdapterPerCapabilityConfig` 和 `parseTimeoutConfig` 不包含默认值回填，改为 `assertNull` 或删除
- **Fix B (生产代码 2 方法)**：AbstractCapabilityExecutor.java `resolveTimeout()` lines 525 添加 `thinAdapterPerCapabilityConfig == null` 前置检查；DiagnosisCapabilityExecutor.java `resolveThinAdapterTimeout()` lines 173 添加同样 null 安全
- **Fix C (测试 6 文件)**：6 个薄适配器测试文件 `createExecutor()` 中第 8 参从 `null` 改为 `new AtomicReference<>(new ConcurrentHashMap<>())`

## R29 PASSED Task 18: AiPlatformConfig + AiPlatformEnvironmentPostProcessor (v26 修复验证通过)
结果：实现 2 处生产代码 null 安全检查 + 5 个薄适配器 Executor null 守卫 + 1 处测试断言修正 + 6 个测试构造器参数修正（null→ConcurrentHashMap）；所有 9 个涉及文件均编译通过
测试：verify_v26.md PASSED — 956 测试通过（common 225 + ai-api 200 + ai-impl 531），0 失败，0 错误，5 跳过

## R29 RETRY Task 18 (v26): 修复 v25 验证暴露的 AiPlatformConfig 空值和 null 安全问题

---

## R30 PROCEED (R29 v26 验证通过)
验证报告：verify_v26.md PASSED — Task 18（AiPlatformConfig v26 修复）956 测试全部通过，0 失败
推进至：Task 19 — FallbackAiService 构造器迁移（ObjectProvider + @Primary）

## R30 NEW Task 19: FallbackAiService 构造器迁移 (ObjectProvider + @Primary)
任务：在 ai-impl/fallback/ 包重构 FallbackAiService 构造器，从 `(List<AiService>, List<DegradationStrategy>)` 迁移为 `(ObjectProvider<AiService>, @Value boolean aiPlatformEnabled)`；标注 `@Primary` 确保业务模块注入优先；移除 `selectDelegate()`/`applyStrategies()` 方法；简化 13 个委托方法为直接 delegate 调用；同步更新 FallbackAiServiceTest.java
选择理由：最终剩余任务。Task 18（AiPlatformConfig + AiPlatformEnvironmentPostProcessor）已验证通过，AiPlatformConfig 已完成 ai.platform.enabled 属性绑定装配。FallbackAiService 构造器迁移依赖 ai.platform.enabled 配置概念已就绪；ObjectProvider 延迟解析依赖的 AiService Bean @ConditionalOnProperty 互斥机制已由 AiPlatformEnvironmentPostProcessor 保证（Task 18 已完成）
上下文：依赖 AiPlatformConfig（已完成，定义 ai.platform.enabled 配置）、AiPlatformEnvironmentPostProcessor（已完成，保证 AiService 实现互斥）、AiOrchestrator/@ConditionalOnProperty 体系（已完成）；涉及文件：FallbackAiService.java（重写构造器+方法简化）+ FallbackAiServiceTest.java（~40 处构造器调用适配+删除 7 个策略依赖测试）

---

## R31 PASSED Task 19: FallbackAiService 构造器迁移 (ObjectProvider + @Primary)
结果：重构 FallbackAiService 构造器从 `(List<AiService>, List<DegradationStrategy>)` 迁移为 `(ObjectProvider<AiService>, @Value boolean aiPlatformEnabled)`；标注 `@Primary`；移除 `selectDelegate()`/`applyStrategies()` 方法；简化 13 个委托方法为直接 delegate 调用；同步修改 FallbackAiServiceTest.java 适配新构造器并删除 7 个策略依赖测试
验证：verify_v27.md PASSED — 2465 测试通过（common 225 + common-module-api 152 + common-module-impl 399 + ai-api 200 + ai-impl 524 + 业务模块 965），0 失败，0 错误，6 跳过
