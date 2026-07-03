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
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendRequest;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendResponse;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class RecommendExaminationCapabilityExecutorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();
    private final AiMetricsCollector metricsCollector = record -> {};

    @Test
    void shouldSucceedWithValidDelegation() {
        ExaminationRecommendResponse expected = new ExaminationRecommendResponse();
        Phase4ServiceFacade<ExaminationRecommendRequest, ExaminationRecommendResponse> facade = req -> expected;
        RecommendExaminationCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<ExaminationRecommendResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new ExaminationRecommendRequest(), "RECOMMEND_EXAM",
            null, "user1", null, null, null, null, null, null);
        assertTrue(result.isSuccess());
        assertSame(expected, result.getData());
    }

    @Test
    void shouldDegradeOnTimeout() {
        Phase4ServiceFacade<ExaminationRecommendRequest, ExaminationRecommendResponse> facade = req -> {
            try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return new ExaminationRecommendResponse();
        };
        RecommendExaminationCapabilityExecutor executor = createExecutor(facade, Duration.ofMillis(50));
        AiResult<ExaminationRecommendResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new ExaminationRecommendRequest(), "RECOMMEND_EXAM",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isDegraded());
        assertTrue(result.getFallbackReason().contains("ThinAdapterTimeout"));
    }

    @Test
    void shouldReturnFailureOnPhase4BusinessException() {
        Phase4ServiceFacade<ExaminationRecommendRequest, ExaminationRecommendResponse> facade = req -> {
            throw new Phase4BusinessException("业务异常") {};
        };
        RecommendExaminationCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<ExaminationRecommendResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new ExaminationRecommendRequest(), "RECOMMEND_EXAM",
            null, "user1", null, null, null, null, null, null);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorCode().contains("PHASE4_"));
    }

    @Test
    void getCapabilityIdShouldReturnRECOMMEND_EXAM() {
        Phase4ServiceFacade<ExaminationRecommendRequest, ExaminationRecommendResponse> facade = req -> new ExaminationRecommendResponse();
        RecommendExaminationCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals("RECOMMEND_EXAM", executor.getCapabilityId());
    }

    @Test
    void getInputTypeShouldReturnExaminationRecommendRequestClass() {
        Phase4ServiceFacade<ExaminationRecommendRequest, ExaminationRecommendResponse> facade = req -> new ExaminationRecommendResponse();
        RecommendExaminationCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals(ExaminationRecommendRequest.class, executor.getInputType());
    }

    @Test
    void getOutputTypeShouldReturnExaminationRecommendResponseClass() {
        Phase4ServiceFacade<ExaminationRecommendRequest, ExaminationRecommendResponse> facade = req -> new ExaminationRecommendResponse();
        RecommendExaminationCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals(ExaminationRecommendResponse.class, executor.getOutputType());
    }

    private RecommendExaminationCapabilityExecutor createExecutor(
            Phase4ServiceFacade<ExaminationRecommendRequest, ExaminationRecommendResponse> facade, Duration thinAdapterTimeout) {
        return new RecommendExaminationCapabilityExecutor(
            facade, metricsCollector, metricsStore,
            Map.of("RECOMMEND_EXAM", Duration.ofSeconds(30)),
            null, Duration.ofSeconds(5), thinAdapterTimeout,
            new ConcurrentHashMap<>(), ForkJoinPool.commonPool(), objectMapper);
    }
}
