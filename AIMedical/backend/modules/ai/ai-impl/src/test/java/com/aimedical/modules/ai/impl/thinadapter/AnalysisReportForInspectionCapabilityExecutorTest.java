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
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportRequest;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportResponse;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisReportForInspectionCapabilityExecutorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SlidingWindowMetricsStore metricsStore = new SlidingWindowMetricsStore();
    private final AiMetricsCollector metricsCollector = record -> {};

    @Test
    void shouldSucceedWithValidDelegation() {
        InspectionReportResponse expected = new InspectionReportResponse();
        Phase4ServiceFacade<InspectionReportRequest, InspectionReportResponse> facade = req -> expected;
        AnalysisReportForInspectionCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<InspectionReportResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new InspectionReportRequest(), "ANALYSIS_REPORT_INSPECTION",
            null, "user1", null, null, null, null, null, null);
        assertTrue(result.isSuccess());
        assertSame(expected, result.getData());
    }

    @Test
    void shouldDegradeOnTimeout() {
        Phase4ServiceFacade<InspectionReportRequest, InspectionReportResponse> facade = req -> {
            try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return new InspectionReportResponse();
        };
        AnalysisReportForInspectionCapabilityExecutor executor = createExecutor(facade, Duration.ofMillis(50));
        AiResult<InspectionReportResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new InspectionReportRequest(), "ANALYSIS_REPORT_INSPECTION",
            null, null, null, null, null, null, null, null);
        assertTrue(result.isDegraded());
        assertTrue(result.getFallbackReason().contains("ThinAdapterTimeout"));
    }

    @Test
    void shouldReturnFailureOnPhase4BusinessException() {
        Phase4ServiceFacade<InspectionReportRequest, InspectionReportResponse> facade = req -> {
            throw new Phase4BusinessException("业务异常") {};
        };
        AnalysisReportForInspectionCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        AiResult<InspectionReportResponse> result = executor.doExecuteInternal(
            System.currentTimeMillis(), new InspectionReportRequest(), "ANALYSIS_REPORT_INSPECTION",
            null, "user1", null, null, null, null, null, null);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorCode().contains("PHASE4_"));
    }

    @Test
    void getCapabilityIdShouldReturnANALYSIS_REPORT_INSPECTION() {
        Phase4ServiceFacade<InspectionReportRequest, InspectionReportResponse> facade = req -> new InspectionReportResponse();
        AnalysisReportForInspectionCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals("ANALYSIS_REPORT_INSPECTION", executor.getCapabilityId());
    }

    @Test
    void getInputTypeShouldReturnInspectionReportRequestClass() {
        Phase4ServiceFacade<InspectionReportRequest, InspectionReportResponse> facade = req -> new InspectionReportResponse();
        AnalysisReportForInspectionCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals(InspectionReportRequest.class, executor.getInputType());
    }

    @Test
    void getOutputTypeShouldReturnInspectionReportResponseClass() {
        Phase4ServiceFacade<InspectionReportRequest, InspectionReportResponse> facade = req -> new InspectionReportResponse();
        AnalysisReportForInspectionCapabilityExecutor executor = createExecutor(facade, Duration.ofSeconds(30));
        assertEquals(InspectionReportResponse.class, executor.getOutputType());
    }

    private AnalysisReportForInspectionCapabilityExecutor createExecutor(
            Phase4ServiceFacade<InspectionReportRequest, InspectionReportResponse> facade, Duration thinAdapterTimeout) {
        return new AnalysisReportForInspectionCapabilityExecutor(
            facade, metricsCollector, metricsStore,
            Map.of("ANALYSIS_REPORT_INSPECTION", Duration.ofSeconds(30)),
            null, Duration.ofSeconds(5), thinAdapterTimeout,
            new ConcurrentHashMap<>(), ForkJoinPool.commonPool(), objectMapper);
    }
}
