package com.aimedical.modules.ai.impl.thinadapter;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.degradation.DegradationReason;
import com.aimedical.modules.ai.api.dto.base.Phase4BusinessException;
import com.aimedical.modules.ai.api.dto.base.Phase4ServiceFacade;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportRequest;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportResponse;
import com.aimedical.modules.ai.impl.metrics.AiCallRecord;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutor;
import com.aimedical.modules.ai.impl.util.RequestContextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@Service("ANALYSIS_REPORT_INSPECTION")
public class AnalysisReportForInspectionCapabilityExecutor extends AbstractCapabilityExecutor<InspectionReportRequest, InspectionReportResponse> {

    private static final Logger log = LoggerFactory.getLogger(AnalysisReportForInspectionCapabilityExecutor.class);

    private final Phase4ServiceFacade<InspectionReportRequest, InspectionReportResponse> phase4Service;

    @Autowired
    public AnalysisReportForInspectionCapabilityExecutor(
        Phase4ServiceFacade<InspectionReportRequest, InspectionReportResponse> phase4Service,
        AiMetricsCollector metricsCollector,
        SlidingWindowMetricsStore metricsStore,
        @Qualifier("capabilityTimeoutConfig") Map<String, Duration> capabilityTimeoutConfig,
        @Qualifier("parseTimeoutConfig") Map<String, Duration> parseTimeoutConfig,
        Duration parseTimeoutDefault,
        @Value("${ai.execution.timeout.thin-adapter-default:30s}") Duration thinAdapterTimeout,
        @Qualifier("thinAdapterPerCapabilityConfig") Map<String, Duration> thinAdapterPerCapabilityConfig,
        Executor llmCallExecutor,
        ObjectMapper objectMapper
    ) {
        super(null, null, null, null,
              metricsCollector, metricsStore, null, null, null,
              capabilityTimeoutConfig, parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout,
              thinAdapterPerCapabilityConfig, llmCallExecutor, objectMapper);
        this.phase4Service = phase4Service;
    }

    @Override public String getCapabilityId() { return "ANALYSIS_REPORT_INSPECTION"; }
    @Override public Class<InspectionReportRequest> getInputType() { return InspectionReportRequest.class; }
    @Override public Class<InspectionReportResponse> getOutputType() { return InspectionReportResponse.class; }

    @Override
    protected String doExtractDepartmentId(InspectionReportRequest request) {
        String deptId = RequestContextUtils.extractFromRequestContext("X-Department-ID");
        return deptId != null ? deptId : super.doExtractDepartmentId(request);
    }

    @Override
    protected String doExtractVisitId(InspectionReportRequest request) {
        String visitId = RequestContextUtils.extractFromRequestContext("X-Visit-ID");
        return visitId != null ? visitId : super.doExtractVisitId(request);
    }

    @Override
    protected String doExtractPatientId(InspectionReportRequest request) {
        String patientId = RequestContextUtils.extractFromRequestContext("X-Patient-ID");
        return patientId != null ? patientId : super.doExtractPatientId(request);
    }

    @Override
    protected String doExtractSessionId(InspectionReportRequest request) {
        String sessionId = RequestContextUtils.extractFromRequestContext("X-Session-ID");
        return sessionId != null ? sessionId : super.doExtractSessionId(request);
    }

    @Override
    protected AiResult<InspectionReportResponse> doExecuteInternal(
        long startTime, InspectionReportRequest request, String capabilityId,
        String departmentId, String userId, String sessionId,
        String callerRole, String callerId, String visitId, String patientId,
        String inputSummary
    ) {
        if (isDtoEmpty(request)) {
            return doDegrade(userId, startTime,
                DegradationReason.INFRASTRUCTURE_ERROR.getCode() + ":Phase4DtoEmpty",
                request, capabilityId, departmentId, callerRole, callerId,
                visitId, patientId, sessionId, inputSummary, null, null, null, null);
        }

        try {
            long resolvedTimeout = resolveThinAdapterTimeout(capabilityId);
            CompletableFuture<InspectionReportResponse> delegateFuture = CompletableFuture.supplyAsync(
                () -> phase4Service.execute(request), llmCallExecutor);
            InspectionReportResponse result = delegateFuture.get(resolvedTimeout, TimeUnit.MILLISECONDS);

            long elapsedMs = System.currentTimeMillis() - startTime;
            String outputSummary = extractOutputSummary(result);
            metricsCollector.record(new AiCallRecord(
                capabilityId, null, null, userId, departmentId, sessionId,
                visitId, patientId, callerRole, callerId,
                elapsedMs, false, null, 0, 0));
            metricsStore.recordSuccess(capabilityId, elapsedMs);
            return AiResult.success(result);
        } catch (TimeoutException e) {
            log.warn("ThinAdapter 委托超时: capabilityId={}, thinAdapterTimeout={}ms",
                capabilityId, resolveThinAdapterTimeout(capabilityId));
            return doDegrade(userId, startTime,
                DegradationReason.TIMEOUT.getCode() + ":ThinAdapterTimeout",
                request, capabilityId, departmentId, callerRole, callerId,
                visitId, patientId, sessionId, inputSummary, null, null, null, null);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Phase4BusinessException || isKnownPhase4BusinessException(cause)) {
                long elapsedMs = System.currentTimeMillis() - startTime;
                String errorCode = "PHASE4_" + cause.getClass().getSimpleName();
                metricsCollector.record(new AiCallRecord(
                    capabilityId, null, null, userId, departmentId, sessionId,
                    visitId, patientId, callerRole, callerId,
                    elapsedMs, false, errorCode, 0, 0));
                metricsStore.recordFailure(capabilityId);
                return AiResult.failure(errorCode, cause.getMessage());
            }
            return doDegrade(userId, startTime,
                DegradationReason.INFRASTRUCTURE_ERROR.getCode() + ":" + cause.getClass().getSimpleName(),
                request, capabilityId, departmentId, callerRole, callerId,
                visitId, patientId, sessionId, inputSummary, null, null, null, null);
        } catch (Exception e) {
            return doDegrade(userId, startTime,
                DegradationReason.INFRASTRUCTURE_ERROR.getCode() + ":" + e.getClass().getSimpleName(),
                request, capabilityId, departmentId, callerRole, callerId,
                visitId, patientId, sessionId, inputSummary, null, null, null, null);
        }
    }

    private boolean isDtoEmpty(InspectionReportRequest request) {
        String pkg = request.getClass().getPackage().getName();
        return knownPhase4Packages.stream().anyMatch(pkg::startsWith);
    }

    private long resolveThinAdapterTimeout(String capabilityId) {
        if (thinAdapterPerCapabilityConfig == null) {
            return thinAdapterTimeout.toMillis();
        }
        if (thinAdapterPerCapabilityConfig.containsKey(capabilityId)) {
            return thinAdapterPerCapabilityConfig.get(capabilityId).toMillis();
        }
        return thinAdapterTimeout.toMillis();
    }
}
