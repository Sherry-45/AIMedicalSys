package com.aimedical.modules.ai.impl.orchestrator.impl;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.degradation.DegradationStrategy;
import com.aimedical.modules.ai.api.dto.triage.TriageRequest;
import com.aimedical.modules.ai.api.dto.triage.TriageResponse;
import com.aimedical.modules.ai.impl.client.LlmChatService;
import com.aimedical.modules.ai.impl.fallback.LocalRuleFallback;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.ModelEndpointHealthManager;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.aimedical.modules.ai.impl.orchestrator.AbstractCapabilityExecutor;
import com.aimedical.modules.ai.impl.parser.StructuredOutputParser;
import com.aimedical.modules.ai.impl.router.ModelRouter;
import com.aimedical.modules.ai.impl.template.PromptTemplateManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@Service("TRIAGE")
public class TriageCapabilityExecutor extends AbstractCapabilityExecutor<TriageRequest, TriageResponse> {

    @Value("${ai.prompt.version.TRIAGE:0}")
    private Integer promptVersion;

    @Autowired
    public TriageCapabilityExecutor(
        PromptTemplateManager promptTemplateManager,
        ModelRouter modelRouter, LlmChatService llmChatService,
        StructuredOutputParser structuredOutputParser, AiMetricsCollector metricsCollector,
        SlidingWindowMetricsStore metricsStore, ModelEndpointHealthManager endpointHealthManager,
        AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef,
        @Autowired(required = false)
        LocalRuleFallback<TriageRequest, TriageResponse> localRuleFallback,
        Map<String, Duration> capabilityTimeoutConfig, Map<String, Duration> parseTimeoutConfig,
        Duration parseTimeoutDefault, Duration thinAdapterTimeout,
        Map<String, Duration> thinAdapterPerCapabilityConfig,
        Executor llmCallExecutor, ObjectMapper objectMapper
    ) {
        super(promptTemplateManager, modelRouter, llmChatService,
              structuredOutputParser, metricsCollector, metricsStore, endpointHealthManager,
              degradationStrategyMapRef, localRuleFallback, capabilityTimeoutConfig,
              parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout,
              thinAdapterPerCapabilityConfig, llmCallExecutor, objectMapper);
    }

    @Override public String getCapabilityId() { return "TRIAGE"; }
    @Override public Class<TriageRequest> getInputType() { return TriageRequest.class; }
    @Override public Class<TriageResponse> getOutputType() { return TriageResponse.class; }

    @Override
    protected AiResult<TriageResponse> doExecuteInternal(
        long startTime, TriageRequest request, String capabilityId,
        String departmentId, String userId, String sessionId,
        String callerRole, String callerId, String visitId, String patientId,
        String inputSummary
    ) {
        Map<String, Object> variables = extractVariables(request);
        return executeStandardPipeline(startTime, request, capabilityId,
            departmentId, userId, sessionId, callerRole, callerId, visitId, patientId,
            inputSummary, variables, this.promptVersion, null);
    }
}
