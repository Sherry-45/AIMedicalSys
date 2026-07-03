package com.aimedical.modules.ai.impl.orchestrator;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.degradation.DegradationContext;
import com.aimedical.modules.ai.api.degradation.DegradationReason;
import com.aimedical.modules.ai.api.degradation.DegradationStrategy;
import com.aimedical.modules.ai.impl.degradation.CircuitBreakerDegradationStrategy;
import com.aimedical.modules.ai.api.dto.base.AiRequestBase;
import com.aimedical.modules.ai.impl.client.LlmChatRequest;
import com.aimedical.modules.ai.impl.client.LlmChatResponse;
import com.aimedical.modules.ai.impl.client.ClientType;
import com.aimedical.modules.ai.impl.client.LlmChatService;
import com.aimedical.modules.ai.impl.client.StructuredChatResult;
import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;
import com.aimedical.modules.ai.impl.client.exception.StructuredOutputNotSupportedException;
import com.aimedical.modules.ai.impl.fallback.LocalRuleFallback;
import com.aimedical.modules.ai.impl.metrics.AiCallRecord;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.EndpointHealthState;
import com.aimedical.modules.ai.impl.metrics.ModelEndpointHealthManager;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.aimedical.modules.ai.impl.parser.StructuredOutputParser;
import com.aimedical.modules.ai.impl.router.ModelRoute;
import com.aimedical.modules.ai.impl.router.ModelRouter;
import com.aimedical.modules.ai.impl.template.PromptTemplateManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractCapabilityExecutorTest {

    private ObjectMapper objectMapper;
    private TestableExecutor<Object, Object> executor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );
    }

    @Test
    void executeShouldReturnSuccessResult() {
        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        AiResult<Object> result = future.join();
        assertTrue(result.isSuccess());
        assertEquals("ok", result.getData());
    }

    @Test
    void executorShouldReturnCapabilityId() {
        assertEquals("TEST", executor.getCapabilityId());
    }

    @Test
    void executorShouldReturnInputType() {
        assertEquals(Object.class, executor.getInputType());
    }

    @Test
    void executorShouldReturnOutputType() {
        assertEquals(Object.class, executor.getOutputType());
    }

    @Test
    void executeShouldHandleDefensiveCopyFailureGracefully() {
        ObjectMapper failingMapper = new ObjectMapper() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T convertValue(Object fromValue, Class<T> toValueType) {
                if (toValueType == Object.class) {
                    throw new RuntimeException("copy failed");
                }
                return (T) Map.of();
            }
        };

        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of("TEST", List.of())), null,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), failingMapper
        );

        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        AiResult<Object> result = future.join();
        assertTrue(result.isSuccess());
    }

    @Test
    void executeShouldDegradeWhenStrategyTriggered() {
        DegradationStrategy triggeringStrategy = new DegradationStrategy() {
            @Override
            public boolean shouldDegrade(DegradationContext context) {
                return true;
            }

            @Override
            public int getOrder() {
                return 0;
            }
        };

        SlidingWindowMetricsStore realStore = new SlidingWindowMetricsStore();
        Map<String, List<DegradationStrategy>> strategyMap = Map.of("TEST", List.of(triggeringStrategy));
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, realStore, null,
            new AtomicReference<>(strategyMap), null,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        AiResult<Object> result = future.join();
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.STRATEGY_TRIGGERED.getCode(), result.getFallbackReason());
    }

    @Test
    void executeShouldHandleTimeout() {
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofMillis(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        ) {
            @Override
            protected AiResult<Object> doExecuteInternal(long startTime, Object request, String capabilityId,
                    String departmentId, String userId, String sessionId, String callerRole, String callerId,
                    String visitId, String patientId, String inputSummary) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return AiResult.success("done");
            }
        };

        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        AiResult<Object> result = future.join();
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.TIMEOUT.getCode(), result.getFallbackReason());
    }

    @Test
    void executeShouldPropagateUnknownException() {
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        ) {
            @Override
            protected AiResult<Object> doExecuteInternal(long startTime, Object request, String capabilityId,
                    String departmentId, String userId, String sessionId, String callerRole, String callerId,
                    String visitId, String patientId, String inputSummary) {
                throw new RuntimeException("unknown");
            }
        };

        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        assertThrows(CompletionException.class, future::join);
    }

    @Test
    void doDegradeShouldUseFallbackWhenLocalRuleReturnsResult() {
        LocalRuleFallback<Object, Object> fallback = request -> "fallbackResult";

        DegradationStrategy triggeringStrategy = new DegradationStrategy() {
            @Override
            public boolean shouldDegrade(DegradationContext context) {
                return true;
            }

            @Override
            public int getOrder() {
                return 0;
            }
        };

        SlidingWindowMetricsStore realStore = new SlidingWindowMetricsStore();
        Map<String, List<DegradationStrategy>> strategyMap = Map.of("TEST", List.of(triggeringStrategy));
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, realStore, null,
            new AtomicReference<>(strategyMap), fallback,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        AiResult<Object> result = future.join();
        assertTrue(result.isSuccess());
        assertEquals("fallbackResult", result.getData());
    }

    @Test
    void isKnownPhase4BusinessExceptionShouldReturnTrueForKnownPackage() {
        Throwable cause = new com.aimedical.modules.diagnosis.TestPhase4Exception();
        assertTrue(executor.isKnownPhase4BusinessException(cause));
    }

    @Test
    void executeShouldPropagatePhase4BusinessException() {
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        ) {
            @Override
            protected AiResult<Object> doExecuteInternal(long startTime, Object request, String capabilityId,
                    String departmentId, String userId, String sessionId, String callerRole, String callerId,
                    String visitId, String patientId, String inputSummary) {
                throw new com.aimedical.modules.diagnosis.TestPhase4Exception();
            }
        };

        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        assertThrows(CompletionException.class, future::join);
    }

    @Test
    void executeShouldTimeoutUsingThinAdapterPerCapabilityConfig() {
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of(),
            null, null, null,
            Map.of("TEST", Duration.ofMillis(10)),
            Executors.newSingleThreadExecutor(), objectMapper
        ) {
            @Override
            protected AiResult<Object> doExecuteInternal(long startTime, Object request, String capabilityId,
                    String departmentId, String userId, String sessionId, String callerRole, String callerId,
                    String visitId, String patientId, String inputSummary) {
                try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return AiResult.success("done");
            }
        };

        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        AiResult<Object> result = future.join();
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
    }

    @Test
    void executeShouldTimeoutUsingThinAdapterTimeoutDefault() {
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            null, null, null,
            Duration.ofMillis(10), null,
            Executors.newSingleThreadExecutor(), objectMapper
        ) {
            @Override
            protected AiResult<Object> doExecuteInternal(long startTime, Object request, String capabilityId,
                    String departmentId, String userId, String sessionId, String callerRole, String callerId,
                    String visitId, String patientId, String inputSummary) {
                try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return AiResult.success("done");
            }
        };

        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        AiResult<Object> result = future.join();
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
    }

    @Test
    void executeShouldSortStrategiesByOrder() {
        HighOrderStrategy highOrder = new HighOrderStrategy();
        LowOrderStrategy lowOrder = new LowOrderStrategy();

        final String[] capturedSentinelReason = new String[1];
        SlidingWindowMetricsStore realStore = new SlidingWindowMetricsStore();
        Map<String, List<DegradationStrategy>> strategyMap = Map.of("TEST", List.of(highOrder, lowOrder));
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, realStore, null,
            new AtomicReference<>(strategyMap), null,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        ) {
            @Override
            protected AiResult<Object> doDegrade(String userId, long startTime, String degradeReason,
                    Object request, String capabilityId, String departmentId, String callerRole,
                    String callerId, String visitId, String patientId, String sessionId,
                    String inputSummary, String outputSummary, Integer promptVersion,
                    String modelId, String sentinelReason) {
                capturedSentinelReason[0] = sentinelReason;
                return super.doDegrade(userId, startTime, degradeReason, request, capabilityId,
                    departmentId, callerRole, callerId, visitId, patientId, sessionId, inputSummary,
                    outputSummary, promptVersion, modelId, sentinelReason);
            }
        };

        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        AiResult<Object> result = future.join();
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.STRATEGY_TRIGGERED.getCode(), result.getFallbackReason());
        assertEquals("LowOrderStrategy", capturedSentinelReason[0]);
    }

    @Test
    void extractVariablesShouldReturnMap() {
        Object request = new Object();
        Map<String, Object> vars = executor.extractVariables(request);
        assertNotNull(vars);
        assertTrue(vars instanceof Map);
    }

    @Test
    void doDegradeShouldReturnDegradedWhenFallbackReturnsNull() {
        LocalRuleFallback<Object, Object> fallback = request -> null;

        DegradationStrategy triggeringStrategy = new DegradationStrategy() {
            @Override
            public boolean shouldDegrade(DegradationContext context) {
                return true;
            }

            @Override
            public int getOrder() {
                return 0;
            }
        };

        SlidingWindowMetricsStore realStore = new SlidingWindowMetricsStore();
        Map<String, List<DegradationStrategy>> strategyMap = Map.of("TEST", List.of(triggeringStrategy));
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, realStore, null,
            new AtomicReference<>(strategyMap), fallback,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        AiResult<Object> result = future.join();
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
    }

    @Test
    void doExtractDepartmentIdShouldReturnNullForNonAiRequestBase() {
        assertNull(executor.doExtractDepartmentId(new Object()));
    }

    @Test
    void doExtractDepartmentIdShouldReturnValueForAiRequestBase() {
        TestAiRequest request = new TestAiRequest();
        assertEquals("dept1", executor.doExtractDepartmentId(request));
    }

    @Test
    void doExtractVisitIdShouldReturnValueForAiRequestBase() {
        TestAiRequest request = new TestAiRequest();
        assertEquals("visit1", executor.doExtractVisitId(request));
    }

    @Test
    void doExtractPatientIdShouldReturnValueForAiRequestBase() {
        TestAiRequest request = new TestAiRequest();
        assertEquals("pat1", executor.doExtractPatientId(request));
    }

    @Test
    void doExtractSessionIdShouldReturnValueForAiRequestBase() {
        TestAiRequest request = new TestAiRequest();
        assertEquals("session1", executor.doExtractSessionId(request));
    }

    @Test
    void executeStandardPipelineShouldReturnDegradedWhenNoRoute() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("renderedPrompt");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, assignment) -> null;
        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );
        AiResult<Object> result = executor.executeStandardPipeline(0L, new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.NO_AVAILABLE_ROUTE.getCode(), result.getFallbackReason());
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeStandardPipelineShouldReturnSuccessWithMetrics() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, assignment) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        final AiCallRecord[] capturedRecord = new AiCallRecord[1];
        AiMetricsCollector collector = new AiMetricsCollector() {
            @Override public void record(AiCallRecord r) { capturedRecord[0] = r; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.success(new LlmChatResponse("ok", null, null, 0)));
            }
            @Override
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                StructuredChatResult<T> result = new StructuredChatResult<>(
                    (T) "expectedResult", 0, new LlmChatResponse.LlmChatUsage(10, 20, 30));
                return CompletableFuture.completedFuture(AiResult.success(result));
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        final boolean[] recordSuccessCalled = {false};
        SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore() {
            @Override
            public void recordSuccess(String capabilityId, long elapsedMs) {
                recordSuccessCalled[0] = true;
                super.recordSuccess(capabilityId, elapsedMs);
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, null,
            collector, metricsStore, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            "dept1", "user1", "session1", "doc", "doc1", "visit1", "pat1",
            "inputSummary", Map.of(), 1, null);
        assertTrue(result.isSuccess());
        assertEquals("expectedResult", result.getData());
        assertNotNull(capturedRecord[0]);
        assertFalse(capturedRecord[0].isDegraded());
        assertNull(capturedRecord[0].getDegradationReason());
        assertEquals(10, capturedRecord[0].getPromptTokens());
        assertEquals(20, capturedRecord[0].getCompletionTokens());
        assertTrue(recordSuccessCalled[0], "metricsStore.recordSuccess() should be called");
    }

    @Test
    void executeStandardPipelineShouldDegradeWhenCircuitBreakerTriggersAfterRouting() {
        // T14: after routing, CircuitBreakerDegradationStrategy is checked with endpointId as operationName
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, assignment) -> ModelRoute.of("endpoint1");

        SlidingWindowMetricsStore realStore = new SlidingWindowMetricsStore();
        // Register failures under the capabilityId so failureRate threshold triggers
        realStore.recordFailure("TEST");
        CircuitBreakerDegradationStrategy cbStrategy =
            new CircuitBreakerDegradationStrategy(realStore, 0.5, Duration.ofMillis(5000));

        Map<String, List<DegradationStrategy>> strategyMap = Map.of("TEST", List.of(cbStrategy));
        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, null, null,
            null, realStore, null,
            new AtomicReference<>(strategyMap), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.STRATEGY_TRIGGERED.getCode(), result.getFallbackReason());
    }

    @Test
    void executeStandardPipelineShouldDegradeWhenEndpointUnavailable() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.UNAVAILABLE; }
            @Override public boolean tryProbe(String endpointId) { return false; }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, null, null,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.ENDPOINT_UNAVAILABLE.getCode(), result.getFallbackReason());
    }

    @SuppressWarnings("unchecked")
    @Test
    void executeStandardPipelineShouldContinueWhenEndpointUnavailableWithProbe() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.UNAVAILABLE; }
            @Override public boolean tryProbe(String endpointId) { return true; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.success(new LlmChatResponse("ok", null, null, 0)));
            }
            @Override
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                StructuredChatResult<T> result = new StructuredChatResult<>(
                    (T) "result", 0, new LlmChatResponse.LlmChatUsage(10, 20, 30));
                return CompletableFuture.completedFuture(AiResult.success(result));
            }
            @Override
            public ClientType getClientType() { return null; }
        };
        SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, null,
            null, metricsStore, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertTrue(result.isSuccess());
        assertEquals("result", result.getData());
    }

    @Test
    void executeStandardPipelineShouldDegradeWhenStructuredChatTimeout() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.success(new LlmChatResponse("ok", null, null, 0)));
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                return new CompletableFuture() {
                    @Override
                    public Object get(long timeout, TimeUnit unit) throws TimeoutException {
                        throw new TimeoutException();
                    }
                };
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, null,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.TIMEOUT.getCode(), result.getFallbackReason());
    }

    @SuppressWarnings("unchecked")
    @Test
    void executeStandardPipelineShouldFallbackWhenStructuredOutputNotSupported() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                LlmChatResponse response = new LlmChatResponse("rawParsedContent", null, null, 0);
                return CompletableFuture.completedFuture(AiResult.success(response));
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new StructuredOutputNotSupportedException("not supported"));
                return future;
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        StructuredOutputParser mockParser = new StructuredOutputParser() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T parse(String rawContent, Class<T> targetClass) {
                return (T) "parsedFromChat";
            }
        };
        SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, mockParser,
            null, metricsStore, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            "dept1", "user1", "session1", "doc", "doc1", "visit1", "pat1",
            "inputSummary", Map.of(), 1, null);
        assertTrue(result.isSuccess());
        assertEquals("parsedFromChat", result.getData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void executeStandardPipelineShouldDegradeOnParseFailureInFallback() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                LlmChatResponse response = new LlmChatResponse("rawContent", null, null, 0);
                return CompletableFuture.completedFuture(AiResult.success(response));
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new StructuredOutputNotSupportedException("not supported"));
                return future;
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        StructuredOutputParser failingParser = new StructuredOutputParser() {
            @Override
            public <T> T parse(String rawContent, Class<T> targetClass) {
                throw new RuntimeException("parse error");
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, failingParser,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.PARSE_FAILURE.getCode(), result.getFallbackReason());
    }

    @Test
    void executeStandardPipelineShouldDegradeWhenTimeoutWindowExhausted() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, null, null,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        long oldStartTime = System.currentTimeMillis() - 60000;
        AiResult<Object> result = executor.executeStandardPipeline(
            oldStartTime, new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.TIMEOUT.getCode(), result.getFallbackReason());
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeStandardPipelineShouldHandleNullPromptTemplateRender() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn(null);
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.success(new LlmChatResponse("ok", null, null, 0)));
            }
            @Override
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                StructuredChatResult<T> result = new StructuredChatResult<>(
                    (T) "result", 0, new LlmChatResponse.LlmChatUsage(0, 0, 0));
                return CompletableFuture.completedFuture(AiResult.success(result));
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };

        SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();
        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, null,
            null, metricsStore, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertTrue(result.isSuccess());
        assertEquals("result", result.getData());
    }

    @Test
    void executeStandardPipelineShouldDegradeOnStructuredChatInterruptedException() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.success(new LlmChatResponse("ok", null, null, 0)));
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                return new CompletableFuture() {
                    @Override
                    public Object get(long timeout, TimeUnit unit) throws InterruptedException {
                        throw new InterruptedException();
                    }
                };
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, null,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.TIMEOUT.getCode(), result.getFallbackReason());
        assertTrue(Thread.interrupted());
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeStandardPipelineShouldPropagatePhase4BusinessException() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.success(new LlmChatResponse("ok", null, null, 0)));
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new com.aimedical.modules.diagnosis.TestPhase4Exception());
                return future;
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, null,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        assertThrows(CompletionException.class, () -> executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeStandardPipelineShouldThrowCompletionExceptionOnUnknownExecutionException() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.success(new LlmChatResponse("ok", null, null, 0)));
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new RuntimeException("unknown"));
                return future;
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, null,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        assertThrows(CompletionException.class, () -> executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null));
    }

    @SuppressWarnings("unchecked")
    @Test
    void executeStandardPipelineShouldDegradeWhenChatFallbackTimesOut() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return new CompletableFuture() {
                    @Override
                    public Object get(long timeout, TimeUnit unit) throws TimeoutException {
                        throw new TimeoutException();
                    }
                };
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new StructuredOutputNotSupportedException("test"));
                return future;
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        StructuredOutputParser mockParser = new StructuredOutputParser() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T parse(String rawContent, Class<T> targetClass) {
                return (T) "parsed";
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, mockParser,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.TIMEOUT.getCode(), result.getFallbackReason());
    }

    @SuppressWarnings("unchecked")
    @Test
    void executeStandardPipelineShouldDegradeWhenChatFallbackInterrupted() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return new CompletableFuture() {
                    @Override
                    public Object get(long timeout, TimeUnit unit) throws InterruptedException {
                        throw new InterruptedException();
                    }
                };
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new StructuredOutputNotSupportedException("test"));
                return future;
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        StructuredOutputParser mockParser = new StructuredOutputParser() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T parse(String rawContent, Class<T> targetClass) {
                return (T) "parsed";
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, mockParser,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.TIMEOUT.getCode(), result.getFallbackReason());
        assertTrue(Thread.interrupted());
    }

    @SuppressWarnings("unchecked")
    @Test
    void executeStandardPipelineShouldDegradeWhenChatFallbackReturnsDegraded() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.degraded("fallback_reason"));
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new StructuredOutputNotSupportedException("test"));
                return future;
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        StructuredOutputParser mockParser = new StructuredOutputParser() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T parse(String rawContent, Class<T> targetClass) {
                return (T) "parsed";
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, mockParser,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.PARSE_FAILURE.getCode(), result.getFallbackReason());
    }

    @SuppressWarnings("unchecked")
    @Test
    void executeStandardPipelineShouldPropagatePhase4BusinessExceptionInFallback() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new com.aimedical.modules.diagnosis.TestPhase4Exception());
                return future;
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new StructuredOutputNotSupportedException("test"));
                return future;
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        StructuredOutputParser mockParser = new StructuredOutputParser() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T parse(String rawContent, Class<T> targetClass) {
                return (T) "parsed";
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, mockParser,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        assertThrows(CompletionException.class, () -> executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null));
    }

    @SuppressWarnings("unchecked")
    @Test
    void executeStandardPipelineShouldThrowCompletionExceptionOnUnknownChatFallbackError() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new RuntimeException("unknown"));
                return future;
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new StructuredOutputNotSupportedException("test"));
                return future;
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        StructuredOutputParser mockParser = new StructuredOutputParser() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T parse(String rawContent, Class<T> targetClass) {
                return (T) "parsed";
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, mockParser,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        assertThrows(CompletionException.class, () -> executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null));
    }

    @Test
    void isKnownPhase4BusinessExceptionShouldReturnFalseForRuntimeException() {
        Throwable cause = new RuntimeException("test");
        assertFalse(executor.isKnownPhase4BusinessException(cause));
    }

    @Test
    void extractOutputSummaryShouldReturnNullForNullResult() {
        assertNull(executor.extractOutputSummary(null));
    }

    @Test
    void extractOutputSummaryShouldReturnToStringForNonNullResult() {
        assertEquals("testValue", executor.extractOutputSummary("testValue"));
    }

    @Test
    void extractCallerRoleShouldReturnNull() {
        assertNull(executor.extractCallerRole());
    }

    @Test
    void extractCallerIdShouldReturnSystemWhenNoAuth() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        assertEquals("SYSTEM", executor.extractCallerId());
    }

    @Test
    void refineTimeoutReasonShouldReturnTimeout() {
        assertEquals(DegradationReason.TIMEOUT.getCode(), executor.refineTimeoutReason("TEST", 0L, new Object()));
    }

    @Test
    void executeShouldUseDefaultTimeoutWhenAllConfigsNull() {
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(null), null,
            null, null, null,
            null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "UNKNOWN");
        AiResult<Object> result = future.join();
        assertTrue(result.isSuccess());
    }

    @Test
    void executeShouldUseSystemUserWhenAuthIsNull() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        final String[] capturedUserId = new String[1];
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        ) {
            @Override
            @SuppressWarnings("unchecked")
            protected AiResult<Object> doExecuteInternal(long startTime, Object request, String capabilityId,
                    String departmentId, String userId, String sessionId, String callerRole, String callerId,
                    String visitId, String patientId, String inputSummary) {
                capturedUserId[0] = userId;
                return (AiResult<Object>) (AiResult<?>) AiResult.success("ok");
            }
        };

        Object request = new Object();
        executor.execute(request, "TEST").join();
        assertEquals("SYSTEM", capturedUserId[0]);
    }

    @Test
    void executeShouldTruncateInputSummary() {
        StringBuilder sb = new StringBuilder(1000);
        for (int i = 0; i < 1000; i++) {
            sb.append('x');
        }
        String longKey = sb.toString();
        Map<String, String> request = new HashMap<>();
        request.put(longKey, "value");

        final int[] capturedLength = new int[1];
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        ) {
            @Override
            @SuppressWarnings("unchecked")
            protected AiResult<Object> doExecuteInternal(long startTime, Object request, String capabilityId,
                    String departmentId, String userId, String sessionId, String callerRole, String callerId,
                    String visitId, String patientId, String inputSummary) {
                capturedLength[0] = inputSummary != null ? inputSummary.length() : 0;
                return (AiResult<Object>) (AiResult<?>) AiResult.success("ok");
            }
        };

        executor.execute(request, "TEST").join();
        assertTrue(capturedLength[0] <= 500);
    }

    @Test
    void executeShouldSetElapsedInDoExecuteInternal() {
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        ) {
            @Override
            @SuppressWarnings("unchecked")
            protected AiResult<Object> doExecuteInternal(long startTime, Object request, String capabilityId,
                    String departmentId, String userId, String sessionId, String callerRole, String callerId,
                    String visitId, String patientId, String inputSummary) {
                try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return (AiResult<Object>) (AiResult<?>) AiResult.success("ok");
            }
        };

        Object request = new Object();
        executor.execute(request, "TEST").join();
        assertTrue(executor.elapsedInDoExecuteInternal > 0);
    }

    @Test
    void resolveTimeoutShouldFallbackToThinAdapterTimeoutWhenFieldIsNull() {
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of(), null, null,
            Duration.ofSeconds(15), null,
            Executors.newSingleThreadExecutor(), objectMapper
        );
        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "UNKNOWN");
        AiResult<Object> result = future.join();
        assertTrue(result.isSuccess());
    }

    @Test
    void resolveTimeoutShouldFallbackToDefault30sWhenFieldAndThinAdapterTimeoutAreNull() {
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of(), null, null,
            null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );
        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "UNKNOWN");
        AiResult<Object> result = future.join();
        assertTrue(result.isSuccess());
    }

    @Test
    void resolveTimeoutShouldUseCapabilityTimeoutWhenThinAdapterPerCapabilityConfigIsNull() {
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null,
            null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );
        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        AiResult<Object> result = future.join();
        assertTrue(result.isSuccess());
    }

    @Test
    void doDegradeShouldHandleNonNullMetricsCollector() {
        AiMetricsCollector collector = new AiMetricsCollector() {
            @Override
            public void record(AiCallRecord r) {}
        };
        DegradationStrategy triggeringStrategy = new DegradationStrategy() {
            @Override public boolean shouldDegrade(DegradationContext ctx) { return true; }
            @Override public int getOrder() { return 0; }
        };

        SlidingWindowMetricsStore realStore = new SlidingWindowMetricsStore();
        Map<String, List<DegradationStrategy>> strategyMap = Map.of("TEST", List.of(triggeringStrategy));
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            collector, realStore, null,
            new AtomicReference<>(strategyMap), null,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        Object request = new Object();
        CompletableFuture<AiResult<Object>> future = executor.execute(request, "TEST");
        AiResult<Object> result = future.join();
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.STRATEGY_TRIGGERED.getCode(), result.getFallbackReason());
    }

    @Test
    void executeShouldPassUserIdThroughDegradePath() {
        DegradationStrategy triggeringStrategy = new DegradationStrategy() {
            @Override
            public boolean shouldDegrade(DegradationContext ctx) {
                return true;
            }
            @Override
            public int getOrder() {
                return 0;
            }
        };

        final String[] capturedUserId = new String[1];
        SlidingWindowMetricsStore realStore = new SlidingWindowMetricsStore();
        Map<String, List<DegradationStrategy>> strategyMap = Map.of("TEST", List.of(triggeringStrategy));
        executor = new TestableExecutor<>(
            Object.class, null, null, null, null,
            null, realStore, null,
            new AtomicReference<>(strategyMap), null,
            Map.of("TEST", Duration.ofSeconds(10)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        ) {
            @Override
            protected AiResult<Object> doDegrade(String userId, long startTime, String degradeReason,
                    Object request, String capabilityId, String departmentId, String callerRole,
                    String callerId, String visitId, String patientId, String sessionId,
                    String inputSummary, String outputSummary, Integer promptVersion,
                    String modelId, String sentinelReason) {
                capturedUserId[0] = userId;
                return AiResult.degraded(degradeReason);
            }
        };

        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        Object request = new Object();
        executor.execute(request, "TEST").join();
        assertEquals("SYSTEM", capturedUserId[0]);
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeStandardPipelineShouldDegradeOnLlmInfrastructureException() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.success(new LlmChatResponse("ok", null, null, 0)));
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new LlmInfrastructureException("infra error"));
                return future;
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, null,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.INFRASTRUCTURE_ERROR.getCode(), result.getFallbackReason());
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeStandardPipelineShouldDegradeOnLlmInfrastructureExceptionInFallback() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new LlmInfrastructureException("infra fallback error"));
                return future;
            }
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new StructuredOutputNotSupportedException("not supported"));
                return future;
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        StructuredOutputParser mockParser = new StructuredOutputParser() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T parse(String rawContent, Class<T> targetClass) {
                return (T) "parsed";
            }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, mockParser,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals(DegradationReason.INFRASTRUCTURE_ERROR.getCode(), result.getFallbackReason());
    }

    @Test
    void executeStandardPipelineShouldSkipCircuitBreakerCheckWhenMetricsStoreIsNull() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.success(new LlmChatResponse("ok", null, null, 0)));
            }
            @Override
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                StructuredChatResult<T> result = new StructuredChatResult<>(
                    (T) "result", 0, new LlmChatResponse.LlmChatUsage(0, 0, 0));
                return CompletableFuture.completedFuture(AiResult.success(result));
            }
            @Override
            public ClientType getClientType() { return null; }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, null,
            null, null, null,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertTrue(result.isSuccess());
    }

    @Test
    void executeStandardPipelineShouldHandleNullMetricsStoreOnSuccess() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.success(new LlmChatResponse("ok", null, null, 0)));
            }
            @Override
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                StructuredChatResult<T> result = new StructuredChatResult<>(
                    (T) "result", 0, new LlmChatResponse.LlmChatUsage(10, 20, 30));
                return CompletableFuture.completedFuture(AiResult.success(result));
            }
            @Override
            public ClientType getClientType() { return null; }
        };

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, null,
            null, null, mockHealth,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertTrue(result.isSuccess());
        assertEquals("result", result.getData());
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeStandardPipelineShouldContinueWhenEndpointHealthManagerIsNull() {
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        LlmChatService mockChat = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.success(new LlmChatResponse("ok", null, null, 0)));
            }
            @Override
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                StructuredChatResult<T> result = new StructuredChatResult<>(
                    (T) "result", 0, new LlmChatResponse.LlmChatUsage(10, 20, 30));
                return CompletableFuture.completedFuture(AiResult.success(result));
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();

        executor = new TestableExecutor<>(
            Object.class, mockTemplate, mockRouter, mockChat, null,
            null, metricsStore, null,
            new AtomicReference<>(Map.of()), null,
            Map.of("TEST", Duration.ofSeconds(30)),
            Map.of("TEST", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        AiResult<Object> result = executor.executeStandardPipeline(
            System.currentTimeMillis(), new Object(), "TEST",
            null, null, null, null, null, null, null, null,
            Map.of(), 1, null);
        assertTrue(result.isSuccess());
        assertEquals("result", result.getData());
    }

    static class HighOrderStrategy implements DegradationStrategy {
        @Override public boolean shouldDegrade(DegradationContext ctx) { return true; }
        @Override public int getOrder() { return 10; }
    }

    static class LowOrderStrategy implements DegradationStrategy {
        @Override public boolean shouldDegrade(DegradationContext ctx) { return true; }
        @Override public int getOrder() { return 0; }
    }

    static class TestAiRequest extends AiRequestBase {
        @Override
        public String getDepartmentId() { return "dept1"; }
        @Override
        public String getVisitId() { return "visit1"; }
        @Override
        public String getPatientId() { return "pat1"; }
        @Override
        public String getSessionId() { return "session1"; }
    }

    static class TestableExecutor<T, R> extends AbstractCapabilityExecutor<T, R> {

        private final Class<T> inputType;

        TestableExecutor(
            Class<T> inputType,
            PromptTemplateManager promptTemplateManager,
            ModelRouter modelRouter,
            LlmChatService llmChatService,
            StructuredOutputParser structuredOutputParser,
            AiMetricsCollector metricsCollector,
            SlidingWindowMetricsStore metricsStore,
            ModelEndpointHealthManager endpointHealthManager,
            AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef,
            LocalRuleFallback<T, R> localRuleFallback,
            Map<String, Duration> capabilityTimeoutConfig,
            Map<String, Duration> parseTimeoutConfig,
            Duration parseTimeoutDefault,
            Duration thinAdapterTimeout,
            Map<String, Duration> thinAdapterPerCapabilityConfig,
            java.util.concurrent.Executor llmCallExecutor,
            ObjectMapper objectMapper
        ) {
            super(promptTemplateManager, modelRouter, llmChatService,
                structuredOutputParser, metricsCollector, metricsStore,
                endpointHealthManager, degradationStrategyMapRef, localRuleFallback,
                capabilityTimeoutConfig, parseTimeoutConfig, parseTimeoutDefault,
                thinAdapterTimeout, thinAdapterPerCapabilityConfig,
                llmCallExecutor, objectMapper);
            this.inputType = inputType;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected AiResult<R> doExecuteInternal(long startTime, T request, String capabilityId,
                String departmentId, String userId, String sessionId, String callerRole, String callerId,
                String visitId, String patientId, String inputSummary) {
            return (AiResult<R>) AiResult.success("ok");
        }

        @Override
        public String getCapabilityId() {
            return "TEST";
        }

        @Override
        public Class<T> getInputType() {
            return inputType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<R> getOutputType() {
            return (Class<R>) Object.class;
        }
    }
}
