package com.aimedical.modules.ai.impl.thinadapter;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.junit.jupiter.api.Test;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.dto.base.Phase4BusinessException;
import com.aimedical.modules.ai.api.dto.base.Phase4ServiceFacade;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderRequest;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderResponse;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class RecommendExecutionOrderCapabilityExecutorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();
    private final AiMetricsCollector metricsCollector = record -> {};

    @Test
    void shouldSucceedWithValidDelegation() {
        ExecutionOrderResponse expected = new ExecutionOrderResponse();
        Phase4ServiceFacade<ExecutionOrderRequest, ExecutionOrderResponse> facade = req -> expected;
        RecommendExecutionOrderCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<ExecutionOrderResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new ExecutionOrderRequest(), "RECOMMEND_EXEC_ORDER",
            null, "user1", null, null, null, null, null, null);
        assertTrue(result.isSuccess());
        assertSame(expected, result.getData());
    }

    @Test
    void shouldDegradeOnTimeout() {
        Phase4ServiceFacade<ExecutionOrderRequest, ExecutionOrderResponse> facade = req -> {
            try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return new ExecutionOrderResponse();
        };
        RecommendExecutionOrderCapabilityExecutor executor = createExecutor(facade, Duration.ofMillis(50));
        AiResult<ExecutionOrderResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new ExecutionOrderRequest(), "RECOMMEND_EXEC_ORDER",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isDegraded());
        assertTrue(result.getFallbackReason().contains("ThinAdapterTimeout"));
    }

    @Test
    void shouldReturnFailureOnPhase4BusinessException() {
        Phase4ServiceFacade<ExecutionOrderRequest, ExecutionOrderResponse> facade = req -> {
            throw new Phase4BusinessException("业务异常") {};
        };
        RecommendExecutionOrderCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<ExecutionOrderResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new ExecutionOrderRequest(), "RECOMMEND_EXEC_ORDER",
            null, "user1", null, null, null, null, null, null);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorCode().contains("PHASE4_"));
    }

    @Test
    void getCapabilityIdShouldReturnRECOMMEND_EXEC_ORDER() {
        Phase4ServiceFacade<ExecutionOrderRequest, ExecutionOrderResponse> facade = req -> new ExecutionOrderResponse();
        RecommendExecutionOrderCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals("RECOMMEND_EXEC_ORDER", executor.getCapabilityId());
    }

    @Test
    void getInputTypeShouldReturnExecutionOrderRequestClass() {
        Phase4ServiceFacade<ExecutionOrderRequest, ExecutionOrderResponse> facade = req -> new ExecutionOrderResponse();
        RecommendExecutionOrderCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals(ExecutionOrderRequest.class, executor.getInputType());
    }

    @Test
    void getOutputTypeShouldReturnExecutionOrderResponseClass() {
        Phase4ServiceFacade<ExecutionOrderRequest, ExecutionOrderResponse> facade = req -> new ExecutionOrderResponse();
        RecommendExecutionOrderCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals(ExecutionOrderResponse.class, executor.getOutputType());
    }

    private RecommendExecutionOrderCapabilityExecutor createExecutor(
            Phase4ServiceFacade<ExecutionOrderRequest, ExecutionOrderResponse> facade, Duration thinAdapterTimeout) {
        return new RecommendExecutionOrderCapabilityExecutor(
            facade, metricsCollector, metricsStore,
            Map.of("RECOMMEND_EXEC_ORDER", Duration.ofSeconds(30)),
            null, Duration.ofSeconds(5), thinAdapterTimeout,
            new ConcurrentHashMap<>(), ForkJoinPool.commonPool(), objectMapper);
    }
}
