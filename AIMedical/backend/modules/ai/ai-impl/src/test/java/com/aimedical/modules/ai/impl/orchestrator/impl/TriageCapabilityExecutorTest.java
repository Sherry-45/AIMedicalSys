package com.aimedical.modules.ai.impl.orchestrator.impl;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.degradation.DegradationStrategy;
import com.aimedical.modules.ai.api.dto.triage.TriageRequest;
import com.aimedical.modules.ai.api.dto.triage.TriageResponse;
import com.aimedical.modules.ai.impl.client.LlmChatRequest;
import com.aimedical.modules.ai.impl.client.LlmChatResponse;
import com.aimedical.modules.ai.impl.client.ClientType;
import com.aimedical.modules.ai.impl.client.LlmChatService;
import com.aimedical.modules.ai.impl.client.StructuredChatResult;
import com.aimedical.modules.ai.impl.metrics.EndpointHealthState;
import com.aimedical.modules.ai.impl.metrics.ModelEndpointHealthManager;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.aimedical.modules.ai.impl.router.ModelRoute;
import com.aimedical.modules.ai.impl.router.ModelRouter;
import com.aimedical.modules.ai.impl.template.PromptTemplateManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TriageCapabilityExecutorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnTriageCapabilityId() {
        TriageCapabilityExecutor executor = createMinimalExecutor();
        assertEquals("TRIAGE", executor.getCapabilityId());
    }

    @Test
    void shouldReturnTriageInputType() {
        TriageCapabilityExecutor executor = createMinimalExecutor();
        assertEquals(TriageRequest.class, executor.getInputType());
    }

    @Test
    void shouldReturnTriageOutputType() {
        TriageCapabilityExecutor executor = createMinimalExecutor();
        assertEquals(TriageResponse.class, executor.getOutputType());
    }

    @SuppressWarnings("unchecked")
    @Test
    void doExecuteInternalShouldReturnPipelineResult() {
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
                    (T) new TriageResponse(), 0, new LlmChatResponse.LlmChatUsage(10, 20, 30));
                return CompletableFuture.completedFuture(AiResult.success(result));
            }
            @Override
            public ClientType getClientType() {
                return null;
            }
        };
        SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();

        TriageCapabilityExecutor executor = new TriageCapabilityExecutor(
            mockTemplate, mockRouter, mockChat, null,
            null, metricsStore, mockHealth,
            new AtomicReference<>(Map.<String, List<DegradationStrategy>>of()), null,
            Map.of("TRIAGE", Duration.ofSeconds(30)),
            Map.of("TRIAGE", Duration.ofSeconds(5)),
            Duration.ofSeconds(3), null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );

        long startTime = System.currentTimeMillis();
        AiResult<TriageResponse> result = executor.doExecuteInternal(
            startTime, new TriageRequest(), "TRIAGE",
            "dept1", "user1", "session1",
            "doctor", "doc1", "visit1", "pat1", "input summary");
        assertTrue(result.isSuccess());
    }

    private TriageCapabilityExecutor createMinimalExecutor() {
        return new TriageCapabilityExecutor(
            null, null, null, null,
            null, null, null,
            new AtomicReference<>(Map.<String, List<DegradationStrategy>>of()), null,
            Map.of("TRIAGE", Duration.ofSeconds(30)),
            null, null, null, null,
            Executors.newSingleThreadExecutor(), objectMapper
        );
    }
}
