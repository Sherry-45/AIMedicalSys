# 详细设计（v8 r5）

## 概述

在 `ai-impl/orchestrator/` 包中实现 `executeStandardPipeline()` 管线方法，在 `orchestrator/impl/` 子包中创建 7 项底座能力 CapabilityExecutor 具体实现类，同步给 5 个存根类型添加管线所需的最小方法签名，并在 `client/exception/` 下新建 `StructuredOutputNotSupportedException` 供编译期 `instanceof` 判断。同时清理 `doDegrade()` 中的 TODO，并在 metrics 包创建 `AiCallRecord` DTO。在 `ai-api/dto/discussion/` 下新增 `DiscussionTranscript` DTO 并修改 `DiscussionConclusionRequest` 追加 transcripts 字段。

## 文件规划

### 新建（9 个）

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `.../client/exception/StructuredOutputNotSupportedException.java` | 新建 | 表示 LLM 不支持结构化输出的运行期异常 |
| `.../metrics/AiCallRecord.java` | 新建 | 指标采集 DTO，供 `AiMetricsCollector.record()` 使用 |
| `.../api/dto/discussion/DiscussionTranscript.java` | 新建 | 讨论转录记录 DTO，含 speakerRole/speakerName/timestamp/content |
| `.../orchestrator/impl/TriageCapabilityExecutor.java` | 新建 | 分诊执行器（TRIAGE） |
| `.../orchestrator/impl/PrescriptionCheckCapabilityExecutor.java` | 新建 | 处方审核执行器（RX_AUDIT） |
| `.../orchestrator/impl/MedicalRecordGenCapabilityExecutor.java` | 新建 | 病历生成执行器（MEDICAL_RECORD_GEN） |
| `.../orchestrator/impl/PrescriptionAssistCapabilityExecutor.java` | 新建 | 辅助开方执行器（RX_ASSIST） |
| `.../orchestrator/impl/KbQueryCapabilityExecutor.java` | 新建 | 知识库问答执行器（KB_QUERY） |
| `.../orchestrator/impl/ScheduleCapabilityExecutor.java` | 新建 | 排班执行器（SCHEDULE） |
| `.../orchestrator/impl/DiscussionConclusionCapabilityExecutor.java` | 新建 | 讨论结论执行器（DISCUSSION_CONCLUSION），带特有压缩前置阶段 |

### 修改（8 个）

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `.../orchestrator/AbstractCapabilityExecutor.java` | 修改 | 实现 `executeStandardPipeline()` 方法体；`doDegrade()` 中 TODO → `metricsCollector.record()`；`refineTimeoutReason()` 返回类型改为 `String`；`resolveTimeout()` 改为 `protected`；提取 `handleSuccess()` 方法 |
| `.../template/PromptTemplateManager.java` | 修改 | 新增 `String render(String templateKey, Map<String, Object> variables)` 方法签名 |
| `.../router/ModelRouter.java` | 修改 | 新增 `Object route(String capabilityId, Object request)` 方法签名 |
| `.../metrics/ModelEndpointHealthManager.java` | 修改 | 新增 `String getState(String routeResult)` 方法签名 |
| `.../parser/StructuredOutputParser.java` | 修改 | 新增 `<T> T parse(String rawContent, Class<T> targetClass)` 方法签名 |
| `.../metrics/AiMetricsCollector.java` | 修改 | 新增 `void record(AiCallRecord record)` 方法签名 |
| `.../api/dto/discussion/DiscussionConclusionRequest.java` | 修改 | 追加 `List<DiscussionTranscript> transcripts` 字段及 Getter/Setter |
| `.../fallback/LocalRuleFallback.java` | 核查 | 确认已有 `R fallback(T request)` 方法（无需修改） |

## 测试修改计划

`AbstractCapabilityExecutorTest.java` 因父类变更产生 3 项已知断裂，需同步修改：

| 测试方法 | 断裂原因 | 修改措施 |
|---------|---------|---------|
| `executeStandardPipelineShouldThrowUnsupportedOperation()` (L419) | `executeStandardPipeline()` 从 `UnsupportedOperationException` 替换为实际管线实现 | 创建带 mock 依赖的测试实例，验证管线返回合法 `AiResult<R>`（mock `PromptTemplateManager`、`ModelRouter` 等，验证成功路径返回 `AiResult.success`） |
| `refineTimeoutReasonShouldReturnTimeout()` (L451) | `refineTimeoutReason()` 返回类型从 `DegradationReason` 变为 `String` | `assertEquals(DegradationReason.TIMEOUT, ...)` → `assertEquals(DegradationReason.TIMEOUT.getCode(), ...)` |
| `doDegradeShouldHandleNonNullMetricsCollector()` (L556) | `AiMetricsCollector` 接口新增 `record()` 抽象方法，匿名类 `new AiMetricsCollector() {}` 无法编译 | 匿名类增加 `public void record(AiCallRecord r) {}` 方法体 |

## 类型定义

---

### StructuredOutputNotSupportedException

**形态**：class extends RuntimeException
**包路径**：`com.aimedical.modules.ai.impl.client.exception`
**职责**：表示 LLM 服务端不支持结构化输出（非 JSON 模式），供 `executeStandardPipeline()` 中 `instanceof` 判断使用

```java
package com.aimedical.modules.ai.impl.client.exception;

public class StructuredOutputNotSupportedException extends RuntimeException {
    public StructuredOutputNotSupportedException(String message) {
        super(message);
    }
    public StructuredOutputNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**公开接口**：继承 `RuntimeException` 的所有构造器
**构造方式**：`new StructuredOutputNotSupportedException(msg)` 或 `new StructuredOutputNotSupportedException(msg, cause)`
**类型关系**：继承 `RuntimeException`

---

### AiCallRecord

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.metrics`
**职责**：指标采集记录 DTO，供 `AiMetricsCollector.record()` 使用，由管线成功处理完或降级时构造

```java
package com.aimedical.modules.ai.impl.metrics;

import java.time.Duration;

public class AiCallRecord {
    private final String capabilityId;
    private final String modelId;
    private final String promptVersion;
    private final String userId;
    private final String departmentId;
    private final String sessionId;
    private final String visitId;
    private final String patientId;
    private final String callerRole;
    private final String callerId;
    private final long elapsedMs;
    private final boolean degraded;
    private final String degradeReason;
    private final int promptTokens;
    private final int completionTokens;

    public AiCallRecord(String capabilityId, String modelId, String promptVersion,
                        String userId, String departmentId, String sessionId,
                        String visitId, String patientId, String callerRole, String callerId,
                        long elapsedMs, boolean degraded, String degradeReason,
                        int promptTokens, int completionTokens) {
        this.capabilityId = capabilityId;
        this.modelId = modelId;
        this.promptVersion = promptVersion;
        this.userId = userId;
        this.departmentId = departmentId;
        this.sessionId = sessionId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.callerRole = callerRole;
        this.callerId = callerId;
        this.elapsedMs = elapsedMs;
        this.degraded = degraded;
        this.degradeReason = degradeReason;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
    }

    // Getters for all 15 fields
    public String getCapabilityId() { return capabilityId; }
    public String getModelId() { return modelId; }
    public String getPromptVersion() { return promptVersion; }
    public String getUserId() { return userId; }
    public String getDepartmentId() { return departmentId; }
    public String getSessionId() { return sessionId; }
    public String getVisitId() { return visitId; }
    public String getPatientId() { return patientId; }
    public String getCallerRole() { return callerRole; }
    public String getCallerId() { return callerId; }
    public long getElapsedMs() { return elapsedMs; }
    public boolean isDegraded() { return degraded; }
    public String getDegradeReason() { return degradeReason; }
    public int getPromptTokens() { return promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
}
```

**公开接口**：15 个 Getter（无 Setter，全量构造后只读）
**构造方式**：全参构造器一次性构造
**类型关系**：独立类

---

### DiscussionTranscript

**形态**：class
**包路径**：`com.aimedical.modules.ai.api.dto.discussion`
**职责**：讨论转录记录值对象，含发言者角色/姓名/时间戳/发言内容

```java
package com.aimedical.modules.ai.api.dto.discussion;

public class DiscussionTranscript {
    private String speakerRole;
    private String speakerName;
    private String timestamp;
    private String content;

    public DiscussionTranscript() {}

    // Getters and Setters for all 4 fields
    public String getSpeakerRole() { return speakerRole; }
    public void setSpeakerRole(String speakerRole) { this.speakerRole = speakerRole; }
    public String getSpeakerName() { return speakerName; }
    public void setSpeakerName(String speakerName) { this.speakerName = speakerName; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
```

**公开接口**：Getter/Setter 各 4 个
**构造方式**：无参构造器 + Setter

---

### DiscussionConclusionRequest（修改）

在原 DTO 基础上追加 transcripts 字段：

```java
// 追加字段
private List<DiscussionTranscript> transcripts;

// 追加方法
public List<DiscussionTranscript> getTranscripts() { return transcripts; }
public void setTranscripts(List<DiscussionTranscript> transcripts) { this.transcripts = transcripts; }
```

---

### PromptTemplateManager（修改）

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.template`

新增方法签名：

```java
public interface PromptTemplateManager {
    String render(String templateKey, Map<String, Object> variables);
}
```

- `render()`：接收模板键和变量 Map，返回渲染后的字符串
- 返回 null 由管线调用方做 null-safe 检查

---

### ModelRouter（修改）

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.router`

新增方法签名：

```java
public interface ModelRouter {
    Object route(String capabilityId, Object request);
}
```

- `route()`：返回路由结果（Object），null 表示无可用路由

---

### ModelEndpointHealthManager（修改）

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.metrics`

新增方法签名：

```java
public class ModelEndpointHealthManager {
    public ModelEndpointHealthManager() {}

    public String getState(String routeResult) {
        return null; // 存根实现
    }
}
```

- `getState()`：返回 `"HEALTHY"` 表示健康；null 或其它值均表示不可用，管线中统一触发 `doDegrade(ENDPOINT_UNAVAILABLE)`

---

### StructuredOutputParser（修改）

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.parser`

新增方法签名：

```java
public interface StructuredOutputParser {
    <T> T parse(String rawContent, Class<T> targetClass);
}
```

- `parse()`：将 LLM 原始文本反序列化为目标类型；失败时抛 RuntimeException

---

### AiMetricsCollector（修改）

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.metrics`

新增方法签名：

```java
public interface AiMetricsCollector {
    void record(AiCallRecord record);
}
```

---

### TriageCapabilityExecutor

**形态**：class extends `AbstractCapabilityExecutor<TriageRequest, TriageResponse>`
**包路径**：`com.aimedical.modules.ai.impl.orchestrator.impl`

```java
@Service("TRIAGE")
public class TriageCapabilityExecutor extends AbstractCapabilityExecutor<TriageRequest, TriageResponse> {

    @Value("${ai.prompt.version.TRIAGE:TRIAGE}")
    private String promptVersion;

    @Autowired
    public TriageCapabilityExecutor(
        Class<TriageRequest> inputType, PromptTemplateManager promptTemplateManager,
        ModelRouter modelRouter, LlmChatService llmChatService,
        StructuredOutputParser structuredOutputParser, AiMetricsCollector metricsCollector,
        SlidingWindowMetricsStore metricsStore, ModelEndpointHealthManager endpointHealthManager,
        AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef,
        LocalRuleFallback<TriageRequest, TriageResponse> localRuleFallback,
        Map<String, Duration> capabilityTimeoutConfig, Map<String, Duration> parseTimeoutConfig,
        Duration parseTimeoutDefault, Duration thinAdapterTimeout,
        Map<String, Duration> thinAdapterPerCapabilityConfig,
        Executor llmCallExecutor, ObjectMapper objectMapper
    ) {
        super(inputType, promptTemplateManager, modelRouter, llmChatService,
              structuredOutputParser, metricsCollector, metricsStore, endpointHealthManager,
              degradationStrategyMapRef, localRuleFallback, capabilityTimeoutConfig,
              parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout,
              thinAdapterPerCapabilityConfig, llmCallExecutor, objectMapper);
    }

    @Override public String getCapabilityId() { return "TRIAGE"; }
    @Override public Class<TriageRequest> getInputType() { return TriageRequest.class; }
    @Override public Class<TriageResponse> getOutputType() { return TriageResponse.class; }

    @Override
    protected AiResult<TriageResponse> doExecuteInternal(
        long startTime, TriageRequest request, String capabilityId,
        String departmentId, String userId, String sessionId,
        String callerRole, String callerId, String visitId, String patientId,
        String inputSummary
    ) {
        Map<String, Object> variables = extractVariables(request);
        return executeStandardPipeline(startTime, request, capabilityId,
            departmentId, userId, sessionId, callerRole, callerId, visitId, patientId,
            inputSummary, variables, this.promptVersion, null);
    }
}
```

**构造方式**：`@Autowired` 注入全部 17 参数，`super(...)` 传递
**类型关系**：继承 `AbstractCapabilityExecutor<TriageRequest, TriageResponse>`

---

### PrescriptionCheckCapabilityExecutor

**形态**：class extends `AbstractCapabilityExecutor<PrescriptionCheckRequest, PrescriptionCheckResponse>`
**包路径**：`com.aimedical.modules.ai.impl.orchestrator.impl`

```java
@Service("RX_AUDIT")
public class PrescriptionCheckCapabilityExecutor
    extends AbstractCapabilityExecutor<PrescriptionCheckRequest, PrescriptionCheckResponse> {

    @Value("${ai.prompt.version.RX_AUDIT:RX_AUDIT}")
    private String promptVersion;

    @Autowired
    public PrescriptionCheckCapabilityExecutor(
        Class<PrescriptionCheckRequest> inputType, PromptTemplateManager promptTemplateManager,
        ModelRouter modelRouter, LlmChatService llmChatService,
        StructuredOutputParser structuredOutputParser, AiMetricsCollector metricsCollector,
        SlidingWindowMetricsStore metricsStore, ModelEndpointHealthManager endpointHealthManager,
        AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef,
        LocalRuleFallback<PrescriptionCheckRequest, PrescriptionCheckResponse> localRuleFallback,
        Map<String, Duration> capabilityTimeoutConfig, Map<String, Duration> parseTimeoutConfig,
        Duration parseTimeoutDefault, Duration thinAdapterTimeout,
        Map<String, Duration> thinAdapterPerCapabilityConfig,
        Executor llmCallExecutor, ObjectMapper objectMapper
    ) {
        super(inputType, promptTemplateManager, modelRouter, llmChatService,
              structuredOutputParser, metricsCollector, metricsStore, endpointHealthManager,
              degradationStrategyMapRef, localRuleFallback, capabilityTimeoutConfig,
              parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout,
              thinAdapterPerCapabilityConfig, llmCallExecutor, objectMapper);
    }

    @Override public String getCapabilityId() { return "RX_AUDIT"; }
    @Override public Class<PrescriptionCheckRequest> getInputType() { return PrescriptionCheckRequest.class; }
    @Override public Class<PrescriptionCheckResponse> getOutputType() { return PrescriptionCheckResponse.class; }

    @Override
    protected AiResult<PrescriptionCheckResponse> doExecuteInternal(...) {
        Map<String, Object> variables = extractVariables(request);
        return executeStandardPipeline(startTime, request, capabilityId,
            departmentId, userId, sessionId, callerRole, callerId, visitId, patientId,
            inputSummary, variables, this.promptVersion, null);
    }
}
```

---

### MedicalRecordGenCapabilityExecutor

**形态**：class extends `AbstractCapabilityExecutor<MedicalRecordGenRequest, MedicalRecordGenResponse>`
**包路径**：`com.aimedical.modules.ai.impl.orchestrator.impl`

```java
@Service("MEDICAL_RECORD_GEN")
public class MedicalRecordGenCapabilityExecutor
    extends AbstractCapabilityExecutor<MedicalRecordGenRequest, MedicalRecordGenResponse> {

    @Value("${ai.prompt.version.MEDICAL_RECORD_GEN:MEDICAL_RECORD_GEN}")
    private String promptVersion;

    @Autowired
    public MedicalRecordGenCapabilityExecutor(... /* 17 参数 */) {
        super(...);
    }

    @Override public String getCapabilityId() { return "MEDICAL_RECORD_GEN"; }
    @Override public Class<MedicalRecordGenRequest> getInputType() { return MedicalRecordGenRequest.class; }
    @Override public Class<MedicalRecordGenResponse> getOutputType() { return MedicalRecordGenResponse.class; }

    @Override
    protected AiResult<MedicalRecordGenResponse> doExecuteInternal(...) {
        Map<String, Object> variables = extractVariables(request);
        return executeStandardPipeline(startTime, request, capabilityId,
            departmentId, userId, sessionId, callerRole, callerId, visitId, patientId,
            inputSummary, variables, this.promptVersion, null);
    }
}
```

---

### PrescriptionAssistCapabilityExecutor

**形态**：class extends `AbstractCapabilityExecutor<PrescriptionAssistRequest, PrescriptionAssistResponse>`
**包路径**：`com.aimedical.modules.ai.impl.orchestrator.impl`

```java
@Service("RX_ASSIST")
public class PrescriptionAssistCapabilityExecutor
    extends AbstractCapabilityExecutor<PrescriptionAssistRequest, PrescriptionAssistResponse> {

    @Value("${ai.prompt.version.RX_ASSIST:RX_ASSIST}")
    private String promptVersion;

    @Autowired
    public PrescriptionAssistCapabilityExecutor(... /* 17 参数 */) {
        super(...);
    }

    @Override public String getCapabilityId() { return "RX_ASSIST"; }
    @Override public Class<PrescriptionAssistRequest> getInputType() { return PrescriptionAssistRequest.class; }
    @Override public Class<PrescriptionAssistResponse> getOutputType() { return PrescriptionAssistResponse.class; }

    @Override
    protected AiResult<PrescriptionAssistResponse> doExecuteInternal(...) {
        Map<String, Object> variables = extractVariables(request);
        return executeStandardPipeline(startTime, request, capabilityId,
            departmentId, userId, sessionId, callerRole, callerId, visitId, patientId,
            inputSummary, variables, this.promptVersion, null);
    }
}
```

---

### KbQueryCapabilityExecutor

**形态**：class extends `AbstractCapabilityExecutor<KbQueryRequest, KbQueryResponse>`
**包路径**：`com.aimedical.modules.ai.impl.orchestrator.impl`

```java
@Service("KB_QUERY")
public class KbQueryCapabilityExecutor
    extends AbstractCapabilityExecutor<KbQueryRequest, KbQueryResponse> {

    @Value("${ai.prompt.version.KB_QUERY:KB_QUERY}")
    private String promptVersion;

    @Autowired
    public KbQueryCapabilityExecutor(... /* 17 参数 */) {
        super(...);
    }

    @Override public String getCapabilityId() { return "KB_QUERY"; }
    @Override public Class<KbQueryRequest> getInputType() { return KbQueryRequest.class; }
    @Override public Class<KbQueryResponse> getOutputType() { return KbQueryResponse.class; }

    @Override
    protected AiResult<KbQueryResponse> doExecuteInternal(...) {
        Map<String, Object> variables = extractVariables(request);
        return executeStandardPipeline(startTime, request, capabilityId,
            departmentId, userId, sessionId, callerRole, callerId, visitId, patientId,
            inputSummary, variables, this.promptVersion, null);
    }
}
```

---

### ScheduleCapabilityExecutor

**形态**：class extends `AbstractCapabilityExecutor<ScheduleRequest, ScheduleResponse>`
**包路径**：`com.aimedical.modules.ai.impl.orchestrator.impl`

```java
@Service("SCHEDULE")
public class ScheduleCapabilityExecutor
    extends AbstractCapabilityExecutor<ScheduleRequest, ScheduleResponse> {

    @Value("${ai.prompt.version.SCHEDULE:SCHEDULE}")
    private String promptVersion;

    @Autowired
    public ScheduleCapabilityExecutor(... /* 17 参数 */) {
        super(...);
    }

    @Override public String getCapabilityId() { return "SCHEDULE"; }
    @Override public Class<ScheduleRequest> getInputType() { return ScheduleRequest.class; }
    @Override public Class<ScheduleResponse> getOutputType() { return ScheduleResponse.class; }

    @Override
    protected AiResult<ScheduleResponse> doExecuteInternal(...) {
        Map<String, Object> variables = extractVariables(request);
        return executeStandardPipeline(startTime, request, capabilityId,
            departmentId, userId, sessionId, callerRole, callerId, visitId, patientId,
            inputSummary, variables, this.promptVersion, null);
    }
}
```

---

### DiscussionConclusionCapabilityExecutor

**形态**：class extends `AbstractCapabilityExecutor<DiscussionConclusionRequest, DiscussionConclusionResponse>`
**包路径**：`com.aimedical.modules.ai.impl.orchestrator.impl`

**特有字段**（3 个）：
| 字段 | 类型 | 注入源 | 默认值 |
|------|------|--------|--------|
| `compressionLightweightEndpoint` | `String` | `@Value("${ai.compression.lightweight-endpoint:compress-default}")` | `"compress-default"` |
| `compressionLightweightClientType` | `ClientType` | `@Value("${ai.compression.lightweight-client-type:HTTP_API}")` | `ClientType.HTTP_API` |
| `transcriptSummaryTimeout` | `Duration` | `@Value("${ai.execution.timeout.transcript-summary:15s}")` | `Duration.ofSeconds(15)` |

**构造器参数**：20 个（父类 17 + 特有 3）

```java
@Service("DISCUSSION_CONCLUSION")
public class DiscussionConclusionCapabilityExecutor
    extends AbstractCapabilityExecutor<DiscussionConclusionRequest, DiscussionConclusionResponse> {

    private static final Logger log = LoggerFactory.getLogger(DiscussionConclusionCapabilityExecutor.class);

    private static final int TOKEN_THRESHOLD = 4000;
    private static final double TRANSCRIPT_SUMMARY_CROWDING_RATIO = 0.2;

    @Value("${ai.prompt.version.DISCUSSION_CONCLUSION:DISCUSSION_CONCLUSION}")
    private String promptVersion;

    @Value("${ai.sentinel.reason.discussion-conclusion:}")
    private String sentinelReason;

    private final String compressionLightweightEndpoint;
    private final ClientType compressionLightweightClientType;
    private final Duration transcriptSummaryTimeout;

    // 前置压缩耗时跟踪，供 refineTimeoutReason() 读取
    private volatile long transcriptSummaryElapsedMs;

    @Autowired
    public DiscussionConclusionCapabilityExecutor(
        Class<DiscussionConclusionRequest> inputType, PromptTemplateManager promptTemplateManager,
        ModelRouter modelRouter, LlmChatService llmChatService,
        StructuredOutputParser structuredOutputParser, AiMetricsCollector metricsCollector,
        SlidingWindowMetricsStore metricsStore, ModelEndpointHealthManager endpointHealthManager,
        AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef,
        LocalRuleFallback<DiscussionConclusionRequest, DiscussionConclusionResponse> localRuleFallback,
        Map<String, Duration> capabilityTimeoutConfig, Map<String, Duration> parseTimeoutConfig,
        Duration parseTimeoutDefault, Duration thinAdapterTimeout,
        Map<String, Duration> thinAdapterPerCapabilityConfig,
        Executor llmCallExecutor, ObjectMapper objectMapper,
        @Value("${ai.compression.lightweight-endpoint:compress-default}") String compressionLightweightEndpoint,
        @Value("${ai.compression.lightweight-client-type:HTTP_API}") ClientType compressionLightweightClientType,
        @Value("${ai.execution.timeout.transcript-summary:15s}") Duration transcriptSummaryTimeout
    ) {
        super(inputType, promptTemplateManager, modelRouter, llmChatService,
              structuredOutputParser, metricsCollector, metricsStore, endpointHealthManager,
              degradationStrategyMapRef, localRuleFallback, capabilityTimeoutConfig,
              parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout,
              thinAdapterPerCapabilityConfig, llmCallExecutor, objectMapper);
        this.compressionLightweightEndpoint = compressionLightweightEndpoint;
        this.compressionLightweightClientType = compressionLightweightClientType;
        this.transcriptSummaryTimeout = transcriptSummaryTimeout;
    }

    @Override public String getCapabilityId() { return "DISCUSSION_CONCLUSION"; }
    @Override public Class<DiscussionConclusionRequest> getInputType() { return DiscussionConclusionRequest.class; }
    @Override public Class<DiscussionConclusionResponse> getOutputType() { return DiscussionConclusionResponse.class; }

    @Override
    protected AiResult<DiscussionConclusionResponse> doExecuteInternal(
        long startTime, DiscussionConclusionRequest request, String capabilityId,
        String departmentId, String userId, String sessionId,
        String callerRole, String callerId, String visitId, String patientId,
        String inputSummary
    ) {
        // 1. 前置压缩阶段
        List<DiscussionTranscript> transcripts = request.getTranscripts();
        transcriptSummaryElapsedMs = 0;
        if (transcripts != null && !transcripts.isEmpty()) {
            int estimatedTokens = estimateTokenCount(transcripts);
            if (estimatedTokens > TOKEN_THRESHOLD) {
                long compressStart = System.currentTimeMillis();
                try {
                    String compressed = compressTranscripts(transcripts, estimatedTokens);
                    // 压缩成功：将压缩摘要作为单条转录写回 request
                    DiscussionTranscript compressedTranscript = new DiscussionTranscript();
                    compressedTranscript.setSpeakerRole("SYSTEM");
                    compressedTranscript.setSpeakerName("compressor");
                    compressedTranscript.setTimestamp(String.valueOf(System.currentTimeMillis()));
                    compressedTranscript.setContent(compressed);
                    request.setTranscripts(List.of(compressedTranscript));
                } catch (Exception e) {
                    log.warn("transcript 压缩失败，回退截断: {}", e.toString());
                    request.setTranscripts(truncateTranscripts(transcripts, 2000));
                }
                transcriptSummaryElapsedMs = System.currentTimeMillis() - compressStart;
            }
        }

        // 2. 标准管线
        Map<String, Object> variables = extractVariables(request);
        return executeStandardPipeline(startTime, request, capabilityId,
            departmentId, userId, sessionId, callerRole, callerId, visitId, patientId,
            inputSummary, variables, this.promptVersion, this.sentinelReason);
    }

    @Override
    protected String refineTimeoutReason(String capabilityId, long elapsedInDoExecuteInternal, T request) {
        Duration capabilityTimeout = resolveTimeout(capabilityId);
        if (elapsedInDoExecuteInternal - transcriptSummaryElapsedMs < capabilityTimeout.toMillis() * TRANSCRIPT_SUMMARY_CROWDING_RATIO) {
            return DegradationReason.TIMEOUT.getCode() + ":transcriptSummaryCrowding";
        }
        return DegradationReason.TIMEOUT.getCode() + ":primaryLlmTimeout";
    }

    private int estimateTokenCount(List<DiscussionTranscript> transcripts) {
        int totalChars = 0;
        for (DiscussionTranscript t : transcripts) {
            totalChars += t.getContent() != null ? t.getContent().length() : 0;
        }
        return totalChars / 4 + 1;
    }

    private String formatTranscripts(List<DiscussionTranscript> transcripts) {
        StringBuilder sb = new StringBuilder();
        for (DiscussionTranscript t : transcripts) {
            sb.append("[").append(t.getSpeakerRole()).append("] ")
              .append(t.getSpeakerName()).append(" (").append(t.getTimestamp()).append("):\n")
              .append(t.getContent()).append("\n");
        }
        return sb.toString();
    }

    private List<DiscussionTranscript> truncateTranscripts(List<DiscussionTranscript> transcripts, int maxTokens) {
        int accumulated = 0;
        int cutoffIndex = transcripts.size();
        for (int i = 0; i < transcripts.size(); i++) {
            int len = transcripts.get(i).getContent() != null ? transcripts.get(i).getContent().length() : 0;
            accumulated += len;
            if (accumulated > maxTokens * 4) {
                cutoffIndex = i;
                break;
            }
        }
        return new ArrayList<>(transcripts.subList(0, cutoffIndex));
    }

    private String compressTranscripts(List<DiscussionTranscript> transcripts, int estimatedTokens) throws Exception {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            String formatted = formatTranscripts(transcripts);
            LlmChatRequest req = buildCompressionRequest(formatted);
            try {
                AiResult<LlmChatResponse> result = llmChatService.chat(req).get(
                    transcriptSummaryTimeout.toMillis(), TimeUnit.MILLISECONDS);
                if (result.isSuccess() && result.getData() != null) {
                    return result.getData().getContent();
                }
                throw new RuntimeException("Compression failed: "
                    + (result.isDegraded() ? result.getFallbackReason() : "unknown"));
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, llmCallExecutor);
        return future.get(transcriptSummaryTimeout.toMillis() + 5000, TimeUnit.MILLISECONDS);
    }

    private LlmChatRequest buildCompressionRequest(String formattedTranscripts) {
        LlmChatOptions options = new LlmChatOptions();
        options.setModelId(compressionLightweightEndpoint);
        options.setMaxTokens(1024);
        options.setTemperature(0.3);

        List<LlmChatMessage> messages = new ArrayList<>();
        messages.add(new LlmChatMessage(LlmChatMessageRole.USER,
            "请对以下讨论记录进行简要摘要压缩，保留关键诊疗信息:\n\n" + formattedTranscripts));

        return new LlmChatRequest(messages, options, compressionLightweightClientType, null);
    }
}
```

**说明**：
- 压缩结果（成功时的压缩摘要 / 失败时的截断列表）通过 `request.setTranscripts()` 写回 request 对象，再传入 `executeStandardPipeline()`。管线中 `request.toString()` 将包含压缩后的内容。
- `transcriptSummaryElapsedMs` 为 volatile 字段，供 `refineTimeoutReason()` 判断超时原因。
- 压缩任务复用 `llmCallExecutor` 线程池（任务规约回退方案）。**死锁风险**：`doExecuteInternal()` 本身已在 `llmCallExecutor` 上执行，若为单线程池则内部子任务永远无法启动。**约束**：`llmCallExecutor` 必须为多线程池（最小线程数 ≥ 2），否则单线程场景下 `compressTranscripts()` 的 `supplyAsync` 子任务无执行线程，导致死锁。

---

### AbstractCapabilityExecutor 变更

#### `resolveTimeout()` 改为 protected

当前为 `private`，子类 `refineTimeoutReason()` 中需要访问，改为 `protected`：

```java
protected Duration resolveTimeout(String capabilityId) {
    // 内容不变
}
```

#### `refineTimeoutReason()` 返回类型改为 String

父类默认实现：

```java
protected String refineTimeoutReason(String capabilityId, long elapsedInDoExecuteInternal, T request) {
    return DegradationReason.TIMEOUT.getCode();
}
```

#### `execute()` 中调用处适配

```java
// 原代码：
DegradationReason reason = refineTimeoutReason(finalCapabilityId, elapsedInDoExecuteInternal, finalRequestCopy);
return doDegrade(finalStartTime, reason.getCode(), ...);

// 改为：
String reason = refineTimeoutReason(finalCapabilityId, elapsedInDoExecuteInternal, finalRequestCopy);
return doDegrade(finalStartTime, reason, ...);
```

#### `executeStandardPipeline()` 实现

方法签名不变：

```java
protected final AiResult<R> executeStandardPipeline(
    long startTime, T request, String capabilityId,
    String departmentId, String userId, String sessionId,
    String callerRole, String callerId, String visitId, String patientId,
    String inputSummary, Map<String, Object> variables,
    String promptVersion, String sentinelReason
)
```

管线实现（替换 `throw new UnsupportedOperationException`）：

```
1. templateKey = capabilityId + ":" + promptVersion
   String renderedPrompt;
   try { renderedPrompt = promptTemplateManager.render(templateKey, variables); }
   catch (Exception e) {
       log.warn("render failed for key: {}, fallback to default prompt", templateKey, e);
       renderedPrompt = null;
   }
   if (renderedPrompt == null) {
       renderedPrompt = "You are a helpful medical AI assistant. Reply concisely.";
   }

2. Object routeResult = modelRouter.route(capabilityId, request);
   if (routeResult == null) {
       return doDegrade(startTime, DegradationReason.NO_AVAILABLE_ROUTE.getCode(),
           request, capabilityId, departmentId, callerRole, callerId,
           visitId, patientId, sessionId, inputSummary, null, promptVersion, null, sentinelReason);
   }

3. String healthState = endpointHealthManager.getState(String.valueOf(routeResult));
    if (healthState == null || !"HEALTHY".equals(healthState)) {
        return doDegrade(startTime, DegradationReason.ENDPOINT_UNAVAILABLE.getCode(),
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, promptVersion, null, sentinelReason);
    }

4. 构造 LlmChatOptions:
    - modelId: `String.valueOf(routeResult)`（routeResult 为 Object 类型，通过 toString() 获取 modelId 标识）
    - temperature: 0.7（默认）
    - maxTokens: 2048（默认）
    - 其余字段：null

5. 构造 LlmChatRequest:
   messages = List.of(
       new LlmChatMessage(LlmChatMessageRole.SYSTEM, renderedPrompt),
       new LlmChatMessage(LlmChatMessageRole.USER, request.toString())
   )
   options = 上一步的 options
    clientType = ClientType.HTTP_API（固定默认值，routeResult 为 Object 类型不含 clientType 字段）
   tools = null

6. 计算剩余超时窗口：
   long totalTimeoutMs = resolveTimeout(capabilityId).toMillis();
   long consumedMs = System.currentTimeMillis() - startTime;
   long remainingMs = totalTimeoutMs - consumedMs;
    if (remainingMs <= 0) {
        return doDegrade(startTime, DegradationReason.TIMEOUT.getCode(),
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, promptVersion, String.valueOf(routeResult), sentinelReason);
    }

   解析超时分配：
   long parseTimeoutMs;
   if (parseTimeoutConfig != null && parseTimeoutConfig.containsKey(capabilityId)) {
       parseTimeoutMs = parseTimeoutConfig.get(capabilityId).toMillis();
   } else if (parseTimeoutDefault != null) {
       parseTimeoutMs = parseTimeoutDefault.toMillis();
   } else {
       parseTimeoutMs = remainingMs * 40 / 100;
   }
   long structuredChatTimeoutMs = remainingMs - parseTimeoutMs;
   if (structuredChatTimeoutMs <= 0) structuredChatTimeoutMs = remainingMs / 2;

7. structuredChat() 优先:
   Class<R> outputType = getOutputType();  // 通过虚方法获取
   CompletableFuture<AiResult<StructuredChatResult<R>>> structuredFuture =
       llmChatService.structuredChat(llmChatRequest, outputType);
   try {
       AiResult<StructuredChatResult<R>> result = structuredFuture.get(
           structuredChatTimeoutMs, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return doDegrade(startTime, DegradationReason.PARSE_FAILURE.getCode(),
                request, capabilityId, departmentId, callerRole, callerId,
                visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
        }
         R parsedResult = result.getData().getData();  // StructuredChatResult<R>.data
        LlmChatResponse.LlmChatUsage usage = result.getData().getUsage();
        long elapsedMs = System.currentTimeMillis() - startTime;
        return handleSuccess(parsedResult, usage, elapsedMs,
            capabilityId, options.getModelId(), promptVersion,
            userId, departmentId, sessionId, visitId, patientId,
            callerRole, callerId);
   }
    catch (TimeoutException e) {
        return doDegrade(startTime, DegradationReason.TIMEOUT.getCode(),
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
    }
    catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return doDegrade(startTime, DegradationReason.TIMEOUT.getCode(),
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
    }
   catch (ExecutionException e) {
       Throwable cause = e.getCause();
       if (cause instanceof StructuredOutputNotSupportedException) {
           // 8. chat() 回退
           try {
               AiResult<LlmChatResponse> rawResult = llmChatService.chat(llmChatRequest)
                   .get(parseTimeoutMs, TimeUnit.MILLISECONDS);
            if (!rawResult.isSuccess()) {
                return doDegrade(startTime, DegradationReason.PARSE_FAILURE.getCode(),
                    request, capabilityId, departmentId, callerRole, callerId,
                    visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
            }
            String rawContent = rawResult.getData().getContent();
                R parsedResult = structuredOutputParser.parse(rawContent, outputType);
                return handleSuccess(parsedResult, null, System.currentTimeMillis() - startTime,
                    capabilityId, options.getModelId(), promptVersion,
                    userId, departmentId, sessionId, visitId, patientId,
                    callerRole, callerId);
           }
            catch (TimeoutException ex) {
                return doDegrade(startTime, DegradationReason.TIMEOUT.getCode(),
                    request, capabilityId, departmentId, callerRole, callerId,
                    visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return doDegrade(startTime, DegradationReason.TIMEOUT.getCode(),
                    request, capabilityId, departmentId, callerRole, callerId,
                    visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
            }
            catch (ExecutionException ex) {
                Throwable causeInner = ex.getCause();
                if (isKnownPhase4BusinessException(causeInner)
                    || causeInner.getClass().getName().contains("LlmInfrastructureException")) {
                    return doDegrade(startTime, DegradationReason.INFRASTRUCTURE_ERROR.getCode(),
                        request, capabilityId, departmentId, callerRole, callerId,
                        visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
                }
                throw new CompletionException(causeInner);
            }
            catch (Exception ex) {
                // parse() 异常
                return doDegrade(startTime, DegradationReason.PARSE_FAILURE.getCode(),
                    request, capabilityId, departmentId, callerRole, callerId,
                    visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
            }
       }
    else if (isKnownPhase4BusinessException(cause)
            || cause.getClass().getName().contains("LlmInfrastructureException")) {
        // 9.
        return doDegrade(startTime, DegradationReason.INFRASTRUCTURE_ERROR.getCode(),
            request, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
    }
    else {
           throw new CompletionException(cause);
       }
   }
```

**不新增 outputType 字段**：`executeStandardPipeline()` 通过 `getOutputType()` 虚方法获取输出类型，父类构造器保持 17 参数不变。

#### `handleSuccess()` 提取（消除 shared_success_handler 重复）

```java
private AiResult<R> handleSuccess(R parsedResult, LlmChatResponse.LlmChatUsage usage,
    long elapsedMs, String capabilityId, String modelId, String promptVersion,
    String userId, String departmentId, String sessionId, String visitId,
    String patientId, String callerRole, String callerId) {
    if (metricsCollector != null) {
        metricsCollector.record(new AiCallRecord(
            capabilityId, modelId, promptVersion, userId, departmentId,
            sessionId, visitId, patientId, callerRole, callerId, elapsedMs, false, null,
            usage != null ? usage.getPromptTokens() : 0,
            usage != null ? usage.getCompletionTokens() : 0));
    }
    metricsStore.recordSuccess(capabilityId, elapsedMs);
    return AiResult.success(parsedResult);
}
```

由 structuredChat 成功路径和 chat() 回退成功路径统一调用，消除重复代码。

#### doDegrade() TODO 清理

原 TODO（第 258 行）：

```java
// TODO: record AiCallRecord when AiMetricsCollector methods are defined
```

替换为：

```java
if (metricsCollector != null) {
    String userId = extractUserId();
    long elapsedMs = System.currentTimeMillis() - startTime;
    metricsCollector.record(new AiCallRecord(
        capabilityId, modelId, promptVersion,
        userId, departmentId, sessionId,
        visitId, patientId, callerRole, callerId,
        elapsedMs, true, degradeReason,
        0, 0
    ));
}
```

当前 `doDegrade()` 签名已包含 `promptVersion`、`modelId` 参数，无需追加；`userId` 通过 `extractUserId()` 方法体内获取。

#### 整理 AbstractCapabilityExecutor 变更汇总

| 变更项 | 说明 |
|--------|------|
| `refineTimeoutReason()` 返回 `DegradationReason` → `String` | 默认返回 `DegradationReason.TIMEOUT.getCode()` |
| `execute().exceptionally` 获取 timeout reason | `reason.getCode()` → 直接使用 `reason`（已为 String） |
| `resolveTimeout()` `private` → `protected` | 供子类 `refineTimeoutReason()` 调用 |
| `executeStandardPipeline()` 实现 | 从 UnsupportedOperationException 替换为完整管线 |
| `doDegrade()` TODO 替换 | 改为 `metricsCollector.record(new AiCallRecord(...))` |
| （无新增字段） | `transcriptSummaryElapsedMs` 仅属子类 `DiscussionConclusionCapabilityExecutor`（`private volatile long`），父类不变 |

## 错误处理

| 场景 | 处理方式 |
|------|---------|
| `promptTemplateManager.render()` 异常 | catch 异常并记录 warn 日志，回退使用固定兜底提示词 `"You are a helpful medical AI assistant. Reply concisely."` |
| `modelRouter.route()` 返回 null | `doDegrade(NO_AVAILABLE_ROUTE)` |
| `endpointHealthManager.getState()` 返回 null 或非 HEALTHY | `doDegrade(ENDPOINT_UNAVAILABLE)` |
| `structuredChat().get()` 超时 (TimeoutException) | `doDegrade(TIMEOUT)` |
| `structuredChat().get()` 中断 (InterruptedException) | 恢复中断标记 + `doDegrade(TIMEOUT)` |
| ExecutionException(cause instanceof StructuredOutputNotSupportedException) | 回退到 `chat()` + `structuredOutputParser.parse()` |
| chat() 回退阶段 TimeoutException | `doDegrade(TIMEOUT)` |
| chat() 回退阶段 LlmInfrastructureException | `doDegrade(INFRASTRUCTURE_ERROR)` |
| chat() 回退阶段 AiResult.isSuccess() 为 false | `doDegrade(PARSE_FAILURE)` |
| structuredOutputParser.parse() 异常 | `doDegrade(PARSE_FAILURE)` |
| 装配或注入失败 | 容器启动时报错，应用不启动 |

## 行为契约

1. **null-safe**：所有 stub 方法调用前均判断非 null（`if (xxx != null) xxx.method()`）
2. **阻塞语义**：`CompletableFuture.get(remainingTimeout, TimeUnit.MILLISECONDS)` 同步阻塞等待 LLM 结果
3. **内外双层超时保护**：外层 `orTimeout` 作为总超时兜底；内层 `get()` 超时按 `resolveTimeout() - 管线前置消耗` 计算
4. **中断恢复**：`InterruptedException` 捕获后调用 `Thread.currentThread().interrupt()` 恢复中断标记再走降级
5. **`AiResult.isSuccess()` 前置检查**：structuredChat 成功路径和 chat 回退路径均先检查 `isSuccess()`，避免 null `getData()` 导致 NPE
6. **`instanceof` 优先**：`StructuredOutputNotSupportedException` 使用编译期 `instanceof`；`LlmInfrastructureException` 使用 `contains("LlmInfrastructureException")` 字符串匹配
7. **DiscussionConclusion 压缩死锁风险**：`compressTranscripts()` 通过 `CompletableFuture.supplyAsync(..., llmCallExecutor)` 向 `llmCallExecutor` 提交压缩子任务，但 `doExecuteInternal()` 本身已在同一线程池上执行（见父类 `execute()` L137）。**若 `llmCallExecutor` 为单线程池（如 `Executors.newSingleThreadExecutor()`），内部子任务将永远无法获取执行线程，导致死锁**。**约束**：`llmCallExecutor` 必须为多线程池，最小线程数 ≥ 2。若实际 Spring 容器中注入的是单线程 Executor，则必须在 `DiscussionConclusionCapabilityExecutor` 中改用独立 `Executor`，或确保容器级 Executor 为多线程。
8. **`getOutputType()` 替代 `outputType` 字段**：`executeStandardPipeline()` 通过虚方法获取 outputType，父类构造器不新增参数（保持 17 参）
9. **压缩结果写回 request**：`DiscussionConclusionCapabilityExecutor.doExecuteInternal()` 中将压缩/截断后的 transcripts 通过 `request.setTranscripts()` 写回，再传入管线

## 依赖关系

| 组件 | 依赖 |
|------|------|
| 7 个 CapabilityExecutor | `AbstractCapabilityExecutor`, `@Service`, `@Value`, `@Autowired`, 各自 `Request`/`Response` DTO |
| AbstractCapabilityExecutor | `PromptTemplateManager`, `ModelRouter`, `LlmChatService`, `StructuredOutputParser`, `AiMetricsCollector`, `SlidingWindowMetricsStore`, `ModelEndpointHealthManager`, `LocalRuleFallback`, `ObjectMapper`, `DegradationStrategy`, `AiResult` |
| executeStandardPipeline() | 新增依赖：`StructuredOutputNotSupportedException` (client/exception), `AiCallRecord` (metrics) |
| DiscussionConclusionCapabilityExecutor | 额外依赖：`DiscussionTranscript`, `ClientType` |
| DiscussionConclusionRequest | 新增字段 `List<DiscussionTranscript> transcripts` |
| AiCallRecord | 无外部依赖 |
| StructuredOutputNotSupportedException | 无外部依赖（继承 RuntimeException） |

## 修订说明（v8 r5）

| 审查意见 | 修改措施 |
|---------|---------|
| **[严重]** DiscussionConclusionCapabilityExecutor 构造器为 21 参数（17+4），引入未定义的 `transcriptSummaryExecutor` Spring Bean | 删除 `transcriptSummaryExecutor` 字段/参数/赋值；`compressTranscripts()` 中改用父类注入的 `llmCallExecutor` 回退；参数计数恢复为 20（17+3），与任务规约一致 |
| **[一般]** `transcriptSummaryElapsedMs` 字段归属矛盾（父类变更表列为 protected，子类声明为 private） | 父类变更汇总表移除该条目；字段仅属子类 `DiscussionConclusionCapabilityExecutor`（`private volatile long`），消除遮蔽风险 |
| **[轻微]** `promptTemplateManager.render()` 异常时静默吞没，无日志 | catch 块增加 `log.warn(...)` 调用（`log` 字段已在父类第 37 行存在，无需新增） |
| **[轻微]** `render()` 失败/返回 null 时回退使用 `promptVersion` 字符串，语义错误 | 回退值改用固定兜底提示词 `"You are a helpful medical AI assistant. Reply concisely."` 替代模板键字符串 |
| **[轻微]** "shared_success_handler" 用注释标记，存在重复代码风险 | 提取为私有方法 `handleSuccess(R, LlmChatUsage, long, String...)`，structuredChat 成功路径和 chat() 回退成功路径统一调用 |

## 修订说明（v8 r6）

| 审查意见 | 修改措施 |
|---------|---------|
| **[严重]** `AbstractCapabilityExecutor` 中 `log` 字段声明冲突（设计说"新增"但源码已存在） | 移除整个"Logger 新增"变更条目；从文件规划表中移除"新增 `log` 字段"描述；v8 r5 修订说明中"父类新增 `log` 字段"修正为"已在父类第 37 行存在，无需新增" |
| **[严重]** 现有 `AbstractCapabilityExecutorTest.java` 兼容性断裂未处理（3 项测试） | 新增"测试修改计划"一节，逐项说明 3 项断裂的原因及修改措施 |
| **[一般]** `handleSuccess()` 违反 null-safe 行为契约 | `handleSuccess()` 中 `metricsCollector.record()` 调用前增加 `if (metricsCollector != null)` 保护，与 `doDegrade()` 保持一致 |
| **[轻微]** routeResult → LlmChatOptions 映射描述不足 | 明确 modelId 取值方式为 `String.valueOf(routeResult)`；clientType 固定默认值为 `ClientType.HTTP_API` |

## 修订说明（v8 r4）

| 审查意见 | 修改措施 |
|---------|---------|
| **[严重]** `refineTimeoutReason()` 返回 `DegradationReason` 但拼接结果为 `String` | 父类 `refineTimeoutReason()` 返回类型改为 `String`（默认返回 `DegradationReason.TIMEOUT.getCode()`）；`execute().exceptionally` 中直接使用 String 而非 `reason.getCode()`；子类 `DiscussionConclusionCapabilityExecutor` 的 override 返回 `DegradationReason.TIMEOUT.getCode() + ":transcriptSummaryCrowding"` |
| **[严重]** `StructuredChatResult` 拆包错误 | 修正为 `result.getData().getData()` 获取 `R`（`result.getData()` 是 `StructuredChatResult<R>`）；usage 通过 `result.getData().getUsage()` 获取；新增 `AiResult.isSuccess()` 前置检查 |
| **[严重]** `TOKEN_THRESHOLD` 常量、`extractTranscriptSummaryElapsed()`、`buildCompressionRequest()`、`log` 字段缺失 | 补充 `private static final int TOKEN_THRESHOLD = 4000`；`transcriptSummaryElapsedMs` 改为 `volatile` 字段（父类），由子类 `doExecuteInternal()` 设置、`refineTimeoutReason()` 读取；新增 `buildCompressionRequest()` 方法；子类声明 `private static final Logger log` |
| **[严重]** 前置压缩结果未写回 request | 压缩成功时将压缩摘要作为单条 `DiscussionTranscript` 通过 `request.setTranscripts()` 写回；截断回退时同样写回截断后的列表。管线中 `request.toString()` 包含压缩后的内容 |
| **[一般]** 构造器参数数量前后矛盾 | 移除上一版设计的 `outputType` 额外字段和构造器参数（改为 `getOutputType()` 虚方法获取）。父类构造器保持 17 参数，子类全部为 17 参数（`DiscussionConclusionCapabilityExecutor` 为 21 = 17+4，特有参数为 `compressionLightweightEndpoint`、`compressionLightweightClientType`、`transcriptSummaryTimeout`、`transcriptSummaryExecutor`） |
| **[一般]** `compressionLightweightEndpoint` 和 `compressionLightweightClientType` 未使用 | `buildCompressionRequest()` 中已引用 `compressionLightweightEndpoint`（设为 `LlmChatOptions.modelId`）和 `compressionLightweightClientType`（设为 `LlmChatRequest.clientType`），确保注入字段被使用 |
| **[一般]** `sentinelReason` 在管线中无使用路径 | 保持作为 `executeStandardPipeline()` 参数传递，在 `doDegrade()` 调用中传入，备未来记录降级日志用途。当前 `doDegrade()` 签名已包含 `sentinelReason` 参数 |
| **[轻微]** 管线和回退路径中未检查 `AiResult.isSuccess()` | 在 `structuredChat()` 和 `chat()` 回退路径中，在 `getData()` 前均增加 `isSuccess()` 前置检查，失败时走 `doDegrade(PARSE_FAILURE)` 降级 |

## 修订说明（v8 r8）

| 审查意见 | 修改措施 |
|---------|---------|
| **[严重]** `ModelEndpointHealthManager.getState()` 返回 null 时管线条件 `healthState != null && !"HEALTHY".equals(healthState)` 不进入降级，与规格"null 表示不可用"矛盾 | 管线条件修正为 `healthState == null \|\| !"HEALTHY".equals(healthState)`（选项 A），null 触发 `doDegrade(ENDPOINT_UNAVAILABLE)`；同步更新 `ModelEndpointHealthManager.getState()` 规格描述为"null 或其它值均表示不可用"；错误处理表对应修改 |
| **[一般]** `DiscussionConclusionCapabilityExecutor` 压缩阶段复用 `llmCallExecutor` 存在死锁风险（`doExecuteInternal` 本身已在同一线程池执行，单线程场景下内部子任务永远无法启动） | 行为契约第 7 条重写为"DiscussionConclusion 压缩死锁风险"文档，明确指出 `llmCallExecutor` 必须为多线程池（最小线程数 ≥ 2），并给出备选方案（改用独立 Executor）；DiscussionConclusion 设计说明中补充死锁风险警告和约束；compressTranscripts() 注释补充风险说明 |

## 修订说明（v8 r7）

| 审查意见 | 修改措施 |
|---------|---------|
| **[一般]** `compressTranscripts()` 内外两层 `future.get()` 使用完全相同超时值 `transcriptSummaryTimeout`，未考虑线程池排队等待时间 | 外层 `future.get()` 超时改为 `transcriptSummaryTimeout.toMillis() + 5000`（增加 5 秒排队缓冲），确保内部 lambda 有足够时间启动执行 |
| **[一般]** `truncateTranscripts()` 返回 `transcripts.subList(0, cutoffIndex)`（视图），持有原大列表全部元素引用，GC 不可回收 | 改为 `new ArrayList<>(transcripts.subList(0, cutoffIndex))` 创建独立副本，确保原大列表可被及时回收 |
| **[轻微]** `preciseTokenCount()` 方法名与行为不符——实际为字符数除以 4 的粗略估算，并非精确 token 计数 | 更名为 `estimateTokenCount()` 避免歧义，同步更新方法定义和所有调用点 |
| **[轻微]** `refineTimeoutReason()` 中 `capabilityTimeout.toMillis() * 0.2` 的字面量 `0.2` 未抽取为命名常量 | 提取为 `private static final double TRANSCRIPT_SUMMARY_CROWDING_RATIO = 0.2` 命名常量，增强可维护性 |
| **[轻微]** 管线伪代码中使用缩略 `...` 标记 `doDegrade()` 调用参数，关键路径未明确列出参数来源，存在实现时参数错序风险 | 在管线伪代码中为每条 `doDegrade()` 调用列出完整实参列表（共 15 参数），明确 `modelId`（未获取前为 null，获取后为 `String.valueOf(routeResult)`/`options.getModelId()`）、`outputSummary`（全部为 null）、`promptVersion`、`sentinelReason` 等参数来源 |
