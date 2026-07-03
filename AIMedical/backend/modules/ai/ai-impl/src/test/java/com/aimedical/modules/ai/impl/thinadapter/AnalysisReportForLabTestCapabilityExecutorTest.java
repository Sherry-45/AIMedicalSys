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
import com.aimedical.modules.ai.api.dto.labtest.LabTestReportRequest;
import com.aimedical.modules.ai.api.dto.labtest.LabTestReportResponse;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisReportForLabTestCapabilityExecutorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();
    private final AiMetricsCollector metricsCollector = record -> {};

    @Test
    void shouldSucceedWithValidDelegation() {
        LabTestReportResponse expected = new LabTestReportResponse();
        Phase4ServiceFacade<LabTestReportRequest, LabTestReportResponse> facade = req -> expected;
        AnalysisReportForLabTestCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<LabTestReportResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new LabTestReportRequest(), "ANALYSIS_REPORT_LABTEST",
            null, "user1", null, null, null, null, null, null);
        assertTrue(result.isSuccess());
        assertSame(expected, result.getData());
    }

    @Test
    void shouldDegradeOnTimeout() {
        Phase4ServiceFacade<LabTestReportRequest, LabTestReportResponse> facade = req -> {
            try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return new LabTestReportResponse();
        };
        AnalysisReportForLabTestCapabilityExecutor executor = createExecutor(facade, Duration.ofMillis(50));
        AiResult<LabTestReportResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new LabTestReportRequest(), "ANALYSIS_REPORT_LABTEST",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isDegraded());
        assertTrue(result.getFallbackReason().contains("ThinAdapterTimeout"));
    }

    @Test
    void shouldReturnFailureOnPhase4BusinessException() {
        Phase4ServiceFacade<LabTestReportRequest, LabTestReportResponse> facade = req -> {
            throw new Phase4BusinessException("业务异常") {};
        };
        AnalysisReportForLabTestCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<LabTestReportResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new LabTestReportRequest(), "ANALYSIS_REPORT_LABTEST",
            null, "user1", null, null, null, null, null, null);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorCode().contains("PHASE4_"));
    }

    @Test
    void getCapabilityIdShouldReturnANALYSIS_REPORT_LABTEST() {
        Phase4ServiceFacade<LabTestReportRequest, LabTestReportResponse> facade = req -> new LabTestReportResponse();
        AnalysisReportForLabTestCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals("ANALYSIS_REPORT_LABTEST", executor.getCapabilityId());
    }

    @Test
    void getInputTypeShouldReturnLabTestReportRequestClass() {
        Phase4ServiceFacade<LabTestReportRequest, LabTestReportResponse> facade = req -> new LabTestReportResponse();
        AnalysisReportForLabTestCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals(LabTestReportRequest.class, executor.getInputType());
    }

    @Test
    void getOutputTypeShouldReturnLabTestReportResponseClass() {
        Phase4ServiceFacade<LabTestReportRequest, LabTestReportResponse> facade = req -> new LabTestReportResponse();
        AnalysisReportForLabTestCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals(LabTestReportResponse.class, executor.getOutputType());
    }

    private AnalysisReportForLabTestCapabilityExecutor createExecutor(
            Phase4ServiceFacade<LabTestReportRequest, LabTestReportResponse> facade, Duration thinAdapterTimeout) {
        return new AnalysisReportForLabTestCapabilityExecutor(
            facade, metricsCollector, metricsStore,
            Map.of("ANALYSIS_REPORT_LABTEST", Duration.ofSeconds(30)),
            null, Duration.ofSeconds(5), thinAdapterTimeout,
            new ConcurrentHashMap<>(), ForkJoinPool.commonPool(), objectMapper);
    }
}
