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
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisResponse;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosisCapabilityExecutorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();
    private final AiMetricsCollector metricsCollector = record -> {};

    @Test
    void shouldDegradeWhenDtoFromPhase4Package() {
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade = req -> new DiagnosisResponse();
        DiagnosisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        DiagnosisRequest phase4Request = new com.aimedical.modules.diagnosis.Phase4DiagnosisRequest();
        AiResult<DiagnosisResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), phase4Request, "DIAGNOSIS",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isDegraded());
        assertTrue(result.getFallbackReason().contains("Phase4DtoEmpty"));
    }

    @Test
    void shouldNotDegradeWhenDtoFromAiApiPackage() {
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade = req -> new DiagnosisResponse();
        DiagnosisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        DiagnosisRequest apiRequest = new DiagnosisRequest();
        AiResult<DiagnosisResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), apiRequest, "DIAGNOSIS",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void shouldDegradeWhenPhase4ServiceThrowsIllegalStateException() {
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade = req -> {
            throw new IllegalStateException("Phase4Service unavailable");
        };
        DiagnosisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<DiagnosisResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new DiagnosisRequest(), "DIAGNOSIS",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isDegraded());
    }

    @Test
    void shouldSucceedWithValidDelegation() {
        DiagnosisResponse expected = new DiagnosisResponse();
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade = req -> expected;
        DiagnosisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<DiagnosisResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new DiagnosisRequest(), "DIAGNOSIS",
            null, "user1", null, null, null, null, null, null);
        assertTrue(result.isSuccess());
        assertSame(expected, result.getData());
    }

    @Test
    void shouldDegradeOnTimeout() {
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade = req -> {
            try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return new DiagnosisResponse();
        };
        DiagnosisCapabilityExecutor executor = createExecutor(facade, Duration.ofMillis(50));
        AiResult<DiagnosisResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new DiagnosisRequest(), "DIAGNOSIS",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isDegraded());
        assertTrue(result.getFallbackReason().contains("ThinAdapterTimeout"));
    }

    @Test
    void shouldReturnFailureOnPhase4BusinessException() {
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade = req -> {
            throw new Phase4BusinessException("业务异常") {};
        };
        DiagnosisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<DiagnosisResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new DiagnosisRequest(), "DIAGNOSIS",
            null, "user1", null, null, null, null, null, null);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorCode().contains("PHASE4_"));
    }

    @Test
    void shouldSucceedWhenThinAdapterPerCapabilityConfigIsNull() {
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade = req -> new DiagnosisResponse();
        DiagnosisCapabilityExecutor executor = new DiagnosisCapabilityExecutor(
            facade, metricsCollector, metricsStore,
            Map.of("DIAGNOSIS", Duration.ofSeconds(30)),
            null, Duration.ofSeconds(5), Duration.ofSeconds(30),
            null, ForkJoinPool.commonPool(), objectMapper);
        AiResult<DiagnosisResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new DiagnosisRequest(), "DIAGNOSIS",
            null, "user1", null, null, null, null, null, null);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void shouldDegradeOnTimeoutWhenThinAdapterPerCapabilityConfigIsNull() {
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade = req -> {
            try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return new DiagnosisResponse();
        };
        DiagnosisCapabilityExecutor executor = new DiagnosisCapabilityExecutor(
            facade, metricsCollector, metricsStore,
            Map.of("DIAGNOSIS", Duration.ofSeconds(30)),
            null, Duration.ofSeconds(5), Duration.ofMillis(50),
            null, ForkJoinPool.commonPool(), objectMapper);
        AiResult<DiagnosisResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new DiagnosisRequest(), "DIAGNOSIS",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isDegraded());
        assertTrue(result.getFallbackReason().contains("ThinAdapterTimeout"));
    }

    @Test
    void getCapabilityIdShouldReturnDIAGNOSIS() {
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade = req -> new DiagnosisResponse();
        DiagnosisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals("DIAGNOSIS", executor.getCapabilityId());
    }

    @Test
    void getInputTypeShouldReturnDiagnosisRequestClass() {
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade = req -> new DiagnosisResponse();
        DiagnosisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals(DiagnosisRequest.class, executor.getInputType());
    }

    @Test
    void getOutputTypeShouldReturnDiagnosisResponseClass() {
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade = req -> new DiagnosisResponse();
        DiagnosisCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals(DiagnosisResponse.class, executor.getOutputType());
    }

    private DiagnosisCapabilityExecutor createExecutor(
            Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade, Duration thinAdapterTimeout) {
        return new DiagnosisCapabilityExecutor(
            facade, metricsCollector, metricsStore,
            Map.of("DIAGNOSIS", Duration.ofSeconds(30)),
            null, Duration.ofSeconds(5), thinAdapterTimeout,
            new ConcurrentHashMap<>(), ForkJoinPool.commonPool(), objectMapper);
    }
}
