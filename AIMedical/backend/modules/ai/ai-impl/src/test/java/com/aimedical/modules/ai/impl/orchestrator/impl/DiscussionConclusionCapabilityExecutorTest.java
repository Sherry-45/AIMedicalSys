package com.aimedical.modules.ai.impl.orchestrator.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.degradation.DegradationReason;
import com.aimedical.modules.ai.api.degradation.DegradationStrategy;
import com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionRequest;
import com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionResponse;
import com.aimedical.modules.ai.api.dto.discussion.DiscussionTranscript;
import com.aimedical.modules.ai.impl.client.ClientType;
import com.aimedical.modules.ai.impl.client.LlmChatRequest;
import com.aimedical.modules.ai.impl.client.LlmChatResponse;
import com.aimedical.modules.ai.impl.client.LlmChatService;
import com.aimedical.modules.ai.impl.client.StructuredChatResult;
import com.aimedical.modules.ai.impl.client.exception.StructuredOutputNotSupportedException;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.EndpointHealthState;
import com.aimedical.modules.ai.impl.metrics.ModelEndpointHealthManager;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.aimedical.modules.ai.impl.parser.StructuredOutputParser;
import com.aimedical.modules.ai.impl.router.ModelRoute;
import com.aimedical.modules.ai.impl.router.ModelRouter;
import com.aimedical.modules.ai.impl.template.PromptTemplateManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscussionConclusionCapabilityExecutorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnCapabilityId() {
        assertEquals("DISCUSSION_CONCLUSION", createMinimalExecutor().getCapabilityId());
    }

    @Test
    void shouldReturnInputType() {
        assertEquals(DiscussionConclusionRequest.class, createMinimalExecutor().getInputType());
    }

    @Test
    void shouldReturnOutputType() {
        assertEquals(DiscussionConclusionResponse.class, createMinimalExecutor().getOutputType());
    }

    @Test
    void refineTimeoutReasonShouldReturnTranscriptSummaryCrowdingWhenWithinRatio() throws Exception {
        DiscussionConclusionCapabilityExecutor executor = createMinimalExecutor();
        setPrivateField(executor, executor.getClass(), "transcriptSummaryElapsedMs", 500L);
        String reason = executor.refineTimeoutReason("DISCUSSION_CONCLUSION", 600L, new DiscussionConclusionRequest());
        assertTrue(reason.contains("transcriptSummaryCrowding"));
    }

    @Test
    void refineTimeoutReasonShouldReturnPrimaryLlmTimeoutWhenBeyondRatio() throws Exception {
        DiscussionConclusionCapabilityExecutor executor = createMinimalExecutor();
        setPrivateField(executor, executor.getClass(), "transcriptSummaryElapsedMs", 500L);
        String reason = executor.refineTimeoutReason("DISCUSSION_CONCLUSION", 10000L, new DiscussionConclusionRequest());
        assertTrue(reason.contains("primaryLlmTimeout"));
    }

    @Test
    void refineTimeoutReasonShouldReturnPrimaryLlmTimeoutWhenNoTranscriptSummary() throws Exception {
        DiscussionConclusionCapabilityExecutor executor = createMinimalExecutor();
        setPrivateField(executor, executor.getClass(), "transcriptSummaryElapsedMs", 0L);
        String reason = executor.refineTimeoutReason("DISCUSSION_CONCLUSION", 10000L, new DiscussionConclusionRequest());
        assertTrue(reason.contains("primaryLlmTimeout"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void doExecuteInternalShouldSkipCompressionWhenTranscriptsNull() {
        DiscussionConclusionCapabilityExecutor executor = createExecutorWithFullPipelineMocks();
        DiscussionConclusionRequest request = new DiscussionConclusionRequest();
        long startTime = System.currentTimeMillis();
        AiResult<DiscussionConclusionResponse> result = executor.doExecuteInternal(
            startTime, request, "DISCUSSION_CONCLUSION",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isSuccess());
    }

    @SuppressWarnings("unchecked")
    @Test
    void doExecuteInternalShouldSkipCompressionWhenTranscriptsEmpty() {
        DiscussionConclusionCapabilityExecutor executor = createExecutorWithFullPipelineMocks();
        DiscussionConclusionRequest request = new DiscussionConclusionRequest();
        request.setTranscripts(new ArrayList<>());
        long startTime = System.currentTimeMillis();
        AiResult<DiscussionConclusionResponse> result = executor.doExecuteInternal(
            startTime, request, "DISCUSSION_CONCLUSION",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isSuccess());
    }

    @SuppressWarnings("unchecked")
    @Test
    void doExecuteInternalShouldSkipCompressionWhenUnderTokenThreshold() {
        DiscussionConclusionCapabilityExecutor executor = createExecutorWithFullPipelineMocks();
        DiscussionConclusionRequest request = new DiscussionConclusionRequest();
        DiscussionTranscript t = new DiscussionTranscript();
        t.setContent("short");
        request.setTranscripts(List.of(t));
        long startTime = System.currentTimeMillis();
        AiResult<DiscussionConclusionResponse> result = executor.doExecuteInternal(
            startTime, request, "DISCUSSION_CONCLUSION",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isSuccess());
    }

    @Test
    void estimateTokenCountShouldReturnOneForEmptyContent() throws Exception {
        DiscussionConclusionCapabilityExecutor executor = createMinimalExecutor();
        int count = invokePrivateMethod(executor, executor.getClass(),
            "estimateTokenCount", new Class<?>[]{List.class}, List.of(new DiscussionTranscript()));
        assertEquals(1, count);
    }

    @Test
    void estimateTokenCountShouldCalculateCorrectly() throws Exception {
        DiscussionConclusionCapabilityExecutor executor = createMinimalExecutor();
        DiscussionTranscript t = new DiscussionTranscript();
        t.setContent("abcd");
        int count = invokePrivateMethod(executor, executor.getClass(),
            "estimateTokenCount", new Class<?>[]{List.class}, List.of(t));
        assertEquals(2, count);
    }

    @Test
    void estimateTokenCountShouldHandleNullContent() throws Exception {
        DiscussionConclusionCapabilityExecutor executor = createMinimalExecutor();
        DiscussionTranscript t = new DiscussionTranscript();
        t.setContent(null);
        int count = invokePrivateMethod(executor, executor.getClass(),
            "estimateTokenCount", new Class<?>[]{List.class}, List.of(t));
        assertEquals(1, count);
    }

    @Test
    void formatTranscriptsShouldFormatCorrectly() throws Exception {
        DiscussionConclusionCapabilityExecutor executor = createMinimalExecutor();
        DiscussionTranscript t = new DiscussionTranscript();
        t.setSpeakerRole("doctor");
        t.setSpeakerName("张医生");
        t.setTimestamp("2026-07-02T10:00:00");
        t.setContent("患者血压偏高");
        String formatted = invokePrivateMethod(executor, executor.getClass(),
            "formatTranscripts", new Class<?>[]{List.class}, List.of(t));
        assertTrue(formatted.contains("[doctor]"));
        assertTrue(formatted.contains("张医生"));
        assertTrue(formatted.contains("2026-07-02T10:00:00"));
        assertTrue(formatted.contains("患者血压偏高"));
    }

    @Test
    void truncateTranscriptsShouldReturnAllWhenUnderLimit() throws Exception {
        DiscussionConclusionCapabilityExecutor executor = createMinimalExecutor();
        List<DiscussionTranscript> transcripts = new ArrayList<>();
        DiscussionTranscript t = new DiscussionTranscript();
        t.setContent("hello");
        transcripts.add(t);
        List<DiscussionTranscript> result = invokePrivateMethod(executor, executor.getClass(),
            "truncateTranscripts", new Class<?>[]{List.class, int.class}, transcripts, 10000);
        assertEquals(1, result.size());
    }

    @Test
    void truncateTranscriptsShouldTruncateWhenOverLimit() throws Exception {
        DiscussionConclusionCapabilityExecutor executor = createMinimalExecutor();
        List<DiscussionTranscript> transcripts = new ArrayList<>();
        DiscussionTranscript t1 = new DiscussionTranscript();
        t1.setContent("a".repeat(10000));
        transcripts.add(t1);
        DiscussionTranscript t2 = new DiscussionTranscript();
        t2.setContent("should be cut");
        transcripts.add(t2);
        List<DiscussionTranscript> result = invokePrivateMethod(executor, executor.getClass(),
            "truncateTranscripts", new Class<?>[]{List.class, int.class}, transcripts, 1000);
        assertTrue(result.size() < 2);
    }

    @Test
    void truncateTranscriptsShouldReturnIndependentCopy() throws Exception {
        DiscussionConclusionCapabilityExecutor executor = createMinimalExecutor();
        List<DiscussionTranscript> original = new ArrayList<>();
        DiscussionTranscript t = new DiscussionTranscript();
        t.setContent("test");
        original.add(t);
        List<DiscussionTranscript> result = invokePrivateMethod(executor, executor.getClass(),
            "truncateTranscripts", new Class<?>[]{List.class, int.class}, original, 10000);
        original.clear();
        assertEquals(1, result.size(), "should be independent copy");
    }

    @SuppressWarnings("unchecked")
    @Test
    void doExecuteInternalShouldCompressWhenOverTokenThreshold() {
        DiscussionConclusionCapabilityExecutor executor = createExecutorWithFullPipelineMocks();
        DiscussionConclusionRequest request = new DiscussionConclusionRequest();
        DiscussionTranscript t = new DiscussionTranscript();
        t.setContent("a".repeat(20000));
        request.setTranscripts(List.of(t));
        long startTime = System.currentTimeMillis();
        AiResult<DiscussionConclusionResponse> result = executor.doExecuteInternal(
            startTime, request, "DISCUSSION_CONCLUSION",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isSuccess());
    }

    @SuppressWarnings("unchecked")
    @Test
    void doExecuteInternalShouldNotModifyOriginalRequestWhenCompressing() {
        DiscussionConclusionCapabilityExecutor executor = createExecutorWithFullPipelineMocks();
        DiscussionConclusionRequest request = new DiscussionConclusionRequest();
        DiscussionTranscript t = new DiscussionTranscript();
        t.setContent("a".repeat(20000));
        List<DiscussionTranscript> originalTranscripts = List.of(t);
        request.setTranscripts(originalTranscripts);
        long startTime = System.currentTimeMillis();
        executor.doExecuteInternal(
            startTime, request, "DISCUSSION_CONCLUSION",
            null, null, null, null, null, null, null, null);
        assertSame(originalTranscripts, request.getTranscripts(),
            "doExecuteInternal should NOT modify original request (T24)");
    }

    @SuppressWarnings("unchecked")
    @Test
    void doExecuteInternalShouldFallbackToTruncationWhenCompressionFails() {
        LlmChatService mockChatWithFailure = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest req) {
                return CompletableFuture.completedFuture(AiResult.degraded("compress_error"));
            }
            @Override
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                StructuredChatResult<T> result = new StructuredChatResult<>(
                    (T) new DiscussionConclusionResponse(), 0,
                    new LlmChatResponse.LlmChatUsage(10, 20, 30));
                return CompletableFuture.completedFuture(AiResult.success(result));
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
        when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
        when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
        ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
        ModelEndpointHealthManager mockHealth = new ModelEndpointHealthManager() {
            @Override public EndpointHealthState getState(String routeResult) { return EndpointHealthState.CONNECTED; }
        };
        SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();

        DiscussionConclusionCapabilityExecutor executor = new DiscussionConclusionCapabilityExecutor(
            mockTemplate, mockRouter, mockChatWithFailure, null,
            null, metricsStore, mockHealth,
            new AtomicReference<>(Map.<String, List<DegradationStrategy>>of()), null,
            Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30)),
            Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newFixedThreadPool(2), objectMapper,
            "compress-default", ClientType.HTTP_API, Duration.ofSeconds(15),
            Executors.newSingleThreadExecutor()
        );

        DiscussionConclusionRequest request = new DiscussionConclusionRequest();
        DiscussionTranscript t = new DiscussionTranscript();
        t.setContent("a".repeat(20000));
        List<DiscussionTranscript> originalTranscripts = List.of(t);
        request.setTranscripts(originalTranscripts);
        long startTime = System.currentTimeMillis();
        AiResult<DiscussionConclusionResponse> result = executor.doExecuteInternal(
            startTime, request, "DISCUSSION_CONCLUSION",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isSuccess());
        assertSame(originalTranscripts, request.getTranscripts(),
            "T24: doExecuteInternal must not modify original request");
    }

    @SuppressWarnings("unchecked")
    @Test
    void doExecuteInternalShouldFallbackToChatWhenStructuredOutputNotSupported() {
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
                LlmChatResponse response = new LlmChatResponse("parsedContent", null, null, 0);
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
                return (T) new DiscussionConclusionResponse();
            }
        };
        SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();

        DiscussionConclusionCapabilityExecutor executor = new DiscussionConclusionCapabilityExecutor(
            mockTemplate, mockRouter, mockChat, mockParser,
            null, metricsStore, mockHealth,
            new AtomicReference<>(Map.<String, List<DegradationStrategy>>of()), null,
            Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30)),
            Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newFixedThreadPool(2), objectMapper,
            "compress-default", ClientType.HTTP_API, Duration.ofSeconds(15),
            Executors.newSingleThreadExecutor()
        );

        DiscussionConclusionRequest request = new DiscussionConclusionRequest();
        DiscussionTranscript t = new DiscussionTranscript();
        t.setContent("short");
        request.setTranscripts(List.of(t));
        long startTime = System.currentTimeMillis();
        AiResult<DiscussionConclusionResponse> result = executor.doExecuteInternal(
            startTime, request, "DISCUSSION_CONCLUSION",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof DiscussionConclusionResponse);
    }

    private static void setPrivateField(Object target, Class<?> clazz, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivateMethod(Object target, Class<?> clazz, String methodName,
                                              Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = clazz.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return (T) method.invoke(target, args);
    }

    private DiscussionConclusionCapabilityExecutor createMinimalExecutor() {
        return new DiscussionConclusionCapabilityExecutor(
            null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.<String, List<DegradationStrategy>>of()), null,
            Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30)),
            null, null, null, null,
            Executors.newFixedThreadPool(2), objectMapper,
            "compress-default", ClientType.HTTP_API, Duration.ofSeconds(15),
            Executors.newSingleThreadExecutor()
        );
    }

    @SuppressWarnings("unchecked")
    private DiscussionConclusionCapabilityExecutor createExecutorWithFullPipelineMocks() {
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
                    (T) new DiscussionConclusionResponse(), 0,
                    new LlmChatResponse.LlmChatUsage(10, 20, 30));
                return CompletableFuture.completedFuture(AiResult.success(result));
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();

        return new DiscussionConclusionCapabilityExecutor(
            mockTemplate, mockRouter, mockChat, null,
            null, metricsStore, mockHealth,
            new AtomicReference<>(Map.<String, List<DegradationStrategy>>of()), null,
            Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(30)),
            Map.of("DISCUSSION_CONCLUSION", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newFixedThreadPool(2), objectMapper,
            "compress-default", ClientType.HTTP_API, Duration.ofSeconds(15),
            Executors.newSingleThreadExecutor()
        );
    }

    @Test
    void transcriptSummaryExecutorShouldBeInjected() throws Exception {
        DiscussionConclusionCapabilityExecutor executor = createMinimalExecutor();
        Field field = DiscussionConclusionCapabilityExecutor.class.getDeclaredField("transcriptSummaryExecutor");
        field.setAccessible(true);
        Executor injected = (Executor) field.get(executor);
        assertNotNull(injected, "transcriptSummaryExecutor should be injected (T33)");
    }
}
