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
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisRequest;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisResponse;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class ImageAnalysisCapabilityExecutorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();
    private final AiMetricsCollector metricsCollector = record -> {};

    @Test
    void shouldSucceedWithValidDelegation() {
        ImageAnalysisResponse expected = new ImageAnalysisResponse();
        Phase4ServiceFacade<ImageAnalysisRequest, ImageAnalysisResponse> facade = req -> expected;
        ImageAnalysisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<ImageAnalysisResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new ImageAnalysisRequest(), "IMAGE_ANALYSIS",
            null, "user1", null, null, null, null, null, null);
        assertTrue(result.isSuccess());
        assertSame(expected, result.getData());
    }

    @Test
    void shouldDegradeOnTimeout() {
        Phase4ServiceFacade<ImageAnalysisRequest, ImageAnalysisResponse> facade = req -> {
            try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return new ImageAnalysisResponse();
        };
        ImageAnalysisCapabilityExecutor executor = createExecutor(facade, Duration.ofMillis(50));
        AiResult<ImageAnalysisResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new ImageAnalysisRequest(), "IMAGE_ANALYSIS",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isDegraded());
        assertTrue(result.getFallbackReason().contains("ThinAdapterTimeout"));
    }

    @Test
    void shouldReturnFailureOnPhase4BusinessException() {
        Phase4ServiceFacade<ImageAnalysisRequest, ImageAnalysisResponse> facade = req -> {
            throw new Phase4BusinessException("业务异常") {};
        };
        ImageAnalysisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<ImageAnalysisResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new ImageAnalysisRequest(), "IMAGE_ANALYSIS",
            null, "user1", null, null, null, null, null, null);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorCode().contains("PHASE4_"));
    }

    @Test
    void getCapabilityIdShouldReturnIMAGE_ANALYSIS() {
        Phase4ServiceFacade<ImageAnalysisRequest, ImageAnalysisResponse> facade = req -> new ImageAnalysisResponse();
        ImageAnalysisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals("IMAGE_ANALYSIS", executor.getCapabilityId());
    }

    @Test
    void getInputTypeShouldReturnImageAnalysisRequestClass() {
        Phase4ServiceFacade<ImageAnalysisRequest, ImageAnalysisResponse> facade = req -> new ImageAnalysisResponse();
        ImageAnalysisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals(ImageAnalysisRequest.class, executor.getInputType());
    }

    @Test
    void getOutputTypeShouldReturnImageAnalysisResponseClass() {
        Phase4ServiceFacade<ImageAnalysisRequest, ImageAnalysisResponse> facade = req -> new ImageAnalysisResponse();
        ImageAnalysisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals(ImageAnalysisResponse.class, executor.getOutputType());
    }

    private ImageAnalysisCapabilityExecutor createExecutor(
            Phase4ServiceFacade<ImageAnalysisRequest, ImageAnalysisResponse> facade, Duration thinAdapterTimeout) {
        return new ImageAnalysisCapabilityExecutor(
            facade, metricsCollector, metricsStore,
            Map.of("IMAGE_ANALYSIS", Duration.ofSeconds(30)),
            null, Duration.ofSeconds(5), thinAdapterTimeout,
            new ConcurrentHashMap<>(), ForkJoinPool.commonPool(), objectMapper);
    }
}
