# 待办事项

---

- [ ] T1: [致命] 薄适配器 isDtoEmpty() 包路径匹配恒真——所有请求永远走 Phase4DtoEmpty 降级 — 来源：R1，位置：`DiagnosisCapabilityExecutor.java:167-170`（6个薄适配器均存在）
- [ ] T2: [严重] 薄适配器使用反射调用 Phase 4 服务而非直接类型引用——丧失编译期类型安全 — 来源：R1，位置：`ai-impl/orchestrator/impl/DiagnosisCapabilityExecutor.java:107-123`（6个薄适配器均存在）
- [ ] T3: [严重] AbstractCapabilityExecutor.execute() 的 exceptionally() 中 Phase4BusinessException 误走降级路径 — 来源：R1，位置：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:186-189`
- [ ] T4: [严重] executeStandardPipeline() 中 LlmInfrastructureException 检测使用字符串匹配而非 instanceof — 来源：R1，位置：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:465,477`
- [ ] T5: [严重] executeStandardPipeline() 缺少对 LlmInfrastructureException 的独立 catch 分支 — 来源：R1，位置：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:436-484`
- [ ] T6: [严重] DelegatingLlmChatService 缺少 @PostConstruct 枚举值完整性校验 — 来源：R2，位置：`ai-impl/client/DelegatingLlmChatService.java:12-55`
- [ ] T7: [严重] DelegatingLlmChatService 回退日志缺少上下文信息和健康检查端点告警 — 来源：R2，位置：`ai-impl/client/DelegatingLlmChatService.java:27-28,42-43`
- [ ] T8: [严重] AiPlatformConfig 缺少 @ConditionalOnProperty 条件注解 — 来源：R2，位置：`ai-impl/config/AiPlatformConfig.java:51-66`
- [ ] T9: [严重] AiPlatformConfig delegatingLlmChatService Bean 装配未按设计使用 ObjectProvider 延迟解析；springAiLlmChatService 缺 @ConditionalOnClass — 来源：R2，位置：`ai-impl/config/AiPlatformConfig.java:127-136`
- [ ] T10: [严重] AiPlatformEnvironmentPostProcessor 缺少配置冲突告警日志 — 来源：R2，位置：`ai-impl/config/AiPlatformEnvironmentPostProcessor.java:17`
- [ ] T11: [严重] HttpApiLlmChatService.chat() 同步阻塞且每次调用新建 HttpClient 实例，无连接池复用 — 来源：R2，位置：`ai-impl/client/HttpApiLlmChatService.java:34-73`
- [ ] T12: [严重] HttpApiLlmChatService.chat() 中 endpointId 被误用作 HTTP URI（应使用 endpointUrl） — 来源：R2，位置：`ai-impl/client/HttpApiLlmChatService.java:56-57`
- [ ] T13: [一般] CircuitBreakerDegradationStrategy OPEN→HALF_OPEN 转换已显式 CAS 锁，但探测线程崩溃后 probeLock 无清理路径 — 来源：R3，位置：`CircuitBreakerDegradationStrategy.java:70-86`
- [ ] T14: [严重] CircuitBreakerDegradationStrategy 熔断器作用域为 capabilityId 而非 endpointId（设计文档第 3.5 节对作用域描述前后不一致，需统一确认） — 来源：R3，位置：`CircuitBreakerDegradationStrategy.java:29-30`
- [ ] T15: [严重] LoggingMetricsCollector @Async 未绑定专用线程池 metricsAsyncExecutor — 来源：R3，位置：`ai-impl/metrics/LoggingMetricsCollector.java:22`
- [ ] T16: [严重] LoggingMetricsCollector AiCallRecord→AiCallLogEntity 字段映射存在多处数据丢失（capabilityName/inputSummary/outputSummary/errorCode/errorMessage/retryCount/totalTokens 硬编码） — 来源：R3，位置：`ai-impl/metrics/LoggingMetricsCollector.java:29-53`
- [ ] T17: [严重] AiCallRecord 与 AiCallLogEntity 字段不对等，缺少7个关键字段（callTime/capabilityName/inputSummary/outputSummary/errorCode/errorMessage/totalTokens/retryCount），缺少 success()/failure()/degraded() 工厂方法 — 来源：R3，位置：`AiCallRecord.java:1-59`
- [ ] T18: [一般] 薄适配器目录结构与设计文档不一致——应归属 thin-adapter/ 子包而非 orchestrator/impl/ — 来源：R1，位置：`ai-impl/orchestrator/impl/DiagnosisCapabilityExecutor.java` 等6个薄适配器
- [ ] T19: [一般] AiOrchestrator.handle() 对未注册能力标识返回 failure 而非 fail-fast 异常 — 来源：R1，位置：`ai-impl/orchestrator/AiOrchestrator.java:149-151`
- [ ] T20: [一般] AiOrchestrator.handle() catch 块中 metricsCollector.record() 未实现 — 来源：R1，位置：`ai-impl/orchestrator/AiOrchestrator.java:158`
- [ ] T21: [一般] AbstractCapabilityExecutor 构造器参数类型与设计文档不一致——capabilityTimeoutConfig/parseTimeoutConfig 应为 Map 而非 AtomicReference<Map>（用 AtomicReference 支持热加载但偏离设计契约） — 来源：R1，位置：`AbstractCapabilityExecutor.java:71-75`
- [ ] T22: [严重] 底座7项 CapabilityExecutor 构造器参数 Class<T> inputType 无法被 Spring 自动注入——启动期报错 — 来源：R1，位置：`TriageCapabilityExecutor.java:35-36`（7个底座执行器均存在）
- [ ] T24: [一般] DiscussionConclusionCapabilityExecutor 修改了防御性拷贝后的 request 对象 — 来源：R1，位置：`ai-impl/orchestrator/impl/DiscussionConclusionCapabilityExecutor.java:113,116`
- [ ] T25: [一般] AiCallRecord 字段与设计文档不一致——缺少 callTime/capabilityName/inputSummary/outputSummary/errorCode/errorMessage/totalTokens/retryCount — 来源：R1，位置：`AiCallRecord.java:1-59`
- [ ] T26: [一般] AbstractCapabilityExecutor.doDegrade() 中重复调用 extractUserId()——线程池线程中 SecurityContextHolder 不可用 — 来源：R1，位置：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:272`
- [ ] T27: [一般] AbstractCapabilityExecutor.extractCallerRole()/extractCallerId() 始终返回 null — 来源：R1，位置：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:323-329`
- [ ] T28: [一般] AbstractCapabilityExecutor.executeStandardPipeline() 中 endpointHealthManager 未做 null 检查 — 来源：R1，位置：`ai-impl/orchestrator/AbstractCapabilityExecutor.java:360`
- [ ] T29: [一般] AiResult 缺少 degradedWithErrorCode 工厂方法；字段非 final 与设计文档不可变契约矛盾 — 来源：R1，位置：`ai-api/AiResult.java:1-79`
- [ ] T30: [一般] DegradationContext invocationCount/failureCount 设计文档要求 int 实际为 Integer；serialVersionUID 未更新 — 来源：R1，位置：`DegradationContext.java:16,20`（注：serviceName/operationName 是 Phase 0 已有字段，非本阶段新增）
- [ ] T31: [一般] DegradationContext.Builder 未实现设计文档要求的静态工厂方法 builder() — 来源：R1，位置：`ai-api/degradation/DegradationContext.java:119-189`
- [ ] T32: [一般] Phase4BusinessException 缺少 getErrorCode() 方法 — 来源：R1，位置：`ai-api/dto/base/Phase4BusinessException.java:1-12`
- [ ] T33: [一般] DiscussionConclusionCapabilityExecutor 压缩调用嵌套提交到 llmCallExecutor 存在线程池死锁风险 — 来源：R1，位置：`ai-impl/orchestrator/impl/DiscussionConclusionCapabilityExecutor.java:170-185`
- [ ] T34: [一般] 薄适配器 CompletableFuture.supplyAsync() 使用默认 ForkJoinPool.commonPool() 而非底座专用 llmCallExecutor — 来源：R1，位置：`DiagnosisCapabilityExecutor.java:107`（6个薄适配器均存在）
- [ ] T35: [一般] AiOrchestrator 使用 ConcurrentHashMap 但声明为 volatile Map——线程安全模型冗余 — 来源：R1，位置：`ai-impl/orchestrator/AiOrchestrator.java:55,69,78`
- [ ] T36: [一般] DelegatingLlmChatService.getClientType() 返回 null 语义不清 — 来源：R2，位置：`ai-impl/client/DelegatingLlmChatService.java:52-54`
- [ ] T37: [一般] EndpointRateLimiter 未使用设计文档要求的 maxBurstSeconds 配置（死代码） — 来源：R2，位置：`ai-impl/client/EndpointRateLimiter.java:17-18,39-45`
- [ ] T38: [一般] EndpointRateLimiter 限流拒绝时返回 AiResult.failure 绕过降级管线，指标不可见 — 来源：R2，位置：`ai-impl/client/HttpApiLlmChatService.java:46-49`
- [ ] T39: [一般] SpringAiLlmChatService/SpringAiLlmChatStreamService 抛出 UnsupportedOperationException 而非 LlmInfrastructureException — 来源：R2，位置：`ai-impl/client/SpringAiLlmChatService.java:18-19,23-24`
- [ ] T40: [一般] LlmChatStreamService 接口导入未使用的 AiAbilityInputInvalidException — 来源：R2，位置：`ai-impl/client/LlmChatStreamService.java:3`
- [ ] T41: [一般] ModelRouter.route() 签名与设计文档不一致——第二个参数应为 ExperimentAssignment 而非 Object — 来源：R2，位置：`ai-impl/router/ModelRouter.java:4`
- [ ] T42: [一般] DefaultModelRouter.refreshRouteTable() @Scheduled 未指定 scheduler — 来源：R2，位置：`ai-impl/router/DefaultModelRouter.java:34`
- [ ] T43: [一般] AiPlatformConfig.refreshCapabilityTimeoutConfig() Binder 重新绑定与 @ConfigurationProperties 状态不一致 — 来源：R2，位置：`ai-impl/config/AiPlatformConfig.java:283-294`
- [ ] T44: [一般] AiPlatformConfig.refreshWindowSeconds() 直接调用 setter，需确认 SlidingWindowMetricsStore.setWindowSeconds() 线程安全 — 来源：R2，位置：`ai-impl/config/AiPlatformConfig.java:297-310`
- [ ] T45: [一般] FallbackAiService 构造器中 delegate==null 时无 fail-fast 告警；aiPlatformEnabled 参数未使用 — 来源：R2，位置：`ai-impl/fallback/FallbackAiService.java:49-55`
- [ ] T46: [一般] ChatToolDefinition.strict 字段有 setter 破坏不可变性 — 来源：R2，位置：`ai-impl/client/ChatToolDefinition.java:11,31`
- [ ] T47: [一般] LlmChatOptions 全部字段可变，不符合设计文档值对象定义 — 来源：R2，位置：`ai-impl/client/LlmChatOptions.java:5-49`
- [ ] T48: [一般] AiRouterProperties.convert() 枚举转换失败时静默回退，运维无法感知配置错误 — 来源：R2，位置：`ai-impl/router/AiRouterProperties.java:44-49`
- [ ] T49: [一般] DefaultCredentialProvider.getCredential() credentialStore 命中后重置 consecutiveFailures，被质疑语义不一致（但连续失败已在此处被打断，重置合理） — 来源：R2，位置：`DefaultCredentialProvider.java:90-95`
- [ ] T50: [一般] HttpApiLlmChatService.structuredChat() 抛异常与 chat() 返回 AiResult.failure 异常处理风格不一致 — 来源：R2，位置：`HttpApiLlmChatService.java:76-79`
- [ ] T51: [一般] SlidingWindowMetricsStore windowSeconds 使用 volatile 而非设计文档要求的 AtomicLong；setWindowSeconds() 无参数校验 — 来源：R3，位置：`ai-impl/metrics/SlidingWindowMetricsStore.java:15`
- [ ] T52: [一般] SlidingWindowMetricsStore 读取方法未执行快照复制，与设计文档不一致 — 来源：R3，位置：`ai-impl/metrics/SlidingWindowMetricsStore.java:49-65`
- [ ] T54: [一般] DatabasePromptTemplateManager 缓存键不包含 promptVersion，resolveExactVersion() 完全绕过缓存 — 来源：R3，位置：`ai-impl/template/DatabasePromptTemplateManager.java:184-186`
- [ ] T55: [一般] DatabasePromptTemplateManager warmup 缓存键不包含版本，预热后首次 render 仍可能触发 DB 查询 — 来源：R3，位置：`ai-impl/template/DatabasePromptTemplateManager.java:37-48`
- [ ] T56: [一般] HashBucketExperimentManager 多实验选择逻辑使用 min().reversed() 语义反直觉 — 来源：R3，位置：`ai-impl/experiment/HashBucketExperimentManager.java:83-87`
- [ ] T57: [一般] HashBucketExperimentManager 缓存中预热数据已排序但 loader 数据未排序，格式不一致 — 来源：R3，位置：`ai-impl/experiment/HashBucketExperimentManager.java:76-77`
- [ ] T58: [严重] ModelEndpointHealthManager 未注册 Spring Bean，CapabilityExecutor 构造注入均依赖此 Bean——启动期即报错 — 来源：R3，位置：`ModelEndpointHealthManager.java:8`
- [ ] T59: [一般] ModelEndpointHealthManager UNAVAILABLE 状态下探测成功直接恢复为 CONNECTED，跳过 DEGRADED 中间态 — 来源：R3，位置：`ai-impl/metrics/ModelEndpointHealthManager.java:92-101`
- [ ] T60: [一般] CircuitBreakerDegradationStrategy CLOSED→OPEN 转换时未重置 failureCount（本类无 consecutiveFailures 字段） — 来源：R3，位置：`CircuitBreakerDegradationStrategy.java:57-65`
- [ ] T61: [一般] TimeoutDegradationStrategy 注入 SlidingWindowMetricsStore 但 shouldDegrade 中未直接使用，数据通过 DegradationContext 间接流入 — 来源：R3，位置：`TimeoutDegradationStrategy.java:12-27`
- [ ] T62: [一般] PromptTemplateManager 接口签名 promptVersion 类型为 String 而非设计文档要求的 Integer — 来源：R3，位置：`ai-impl/template/PromptTemplateManager.java:6`
- [ ] T63: [一般] ExperimentGroup @ManyToOne(LAZY) 与 Experiment @OneToMany(EAGER) 冲突，EAGER 加载产生 N+1 查询问题 — 来源：R3，位置：`ai-impl/experiment/ExperimentGroup.java:23`
- [ ] T64: [一般] PrescriptionLocalRuleFallback 缺少数据源异常处理和 CHECK_SKIPPED 机制 — 来源：R3，位置：`ai-impl/fallback/PrescriptionLocalRuleFallback.java:68-98`
- [ ] T65: [一般] PrescriptionLocalRuleFallback 过敏检查仅使用 DRUG_INGREDIENTS 映射，覆盖范围有限 — 来源：R3，位置：`ai-impl/fallback/PrescriptionLocalRuleFallback.java:155-186`
- [ ] T66: [一般] JsonStructuredOutputParser 解析失败时抛出 RuntimeException 而非专用异常 — 来源：R3，位置：`ai-impl/parser/JsonStructuredOutputParser.java:30`

---

## 验证状态

> 经双 agent 独立交叉验证（2026-07-02），原 66 条，修正后 64 条：
> - **✅ 真实：51 条**（T1 由「严重」升级为「致命」；T22/T58 由「一般」升级为「严重」）
> - **⚠️ 部分真实/需澄清：11 条**（T13/T14/T24/T29/T30/T35/T44/T51/T52/T56/T60 — 附注已标注说明）
> - **❌ 移除：2 条**（T23：逻辑与设计一致，未反转；T53：cutoff 与事件时间戳均为 System.currentTimeMillis()，时间源相同）
> - **严重等级调整：4 条**（T1↑致命 / T13↓一般 / T22↑严重 / T58↑严重）
> - **描述修正：7 条**（T17/T21/T30/T34/T49/T50/T61）
