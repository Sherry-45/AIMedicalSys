package com.aimedical.modules.ai.impl.orchestrator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.degradation.DegradationContext;
import com.aimedical.modules.ai.api.degradation.DegradationReason;
import com.aimedical.modules.ai.api.degradation.DegradationStrategy;
import com.aimedical.modules.ai.api.dto.base.AiRequestBase;
import com.aimedical.modules.ai.impl.client.ClientType;
import com.aimedical.modules.ai.impl.client.LlmChatMessage;
import com.aimedical.modules.ai.impl.client.LlmChatMessageRole;
import com.aimedical.modules.ai.impl.client.LlmChatOptions;
import com.aimedical.modules.ai.impl.client.LlmChatRequest;
import com.aimedical.modules.ai.impl.client.LlmChatResponse;
import com.aimedical.modules.ai.impl.client.LlmChatService;
import com.aimedical.modules.ai.impl.client.StructuredChatResult;
import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;
import com.aimedical.modules.ai.impl.client.exception.StructuredOutputNotSupportedException;
import com.aimedical.modules.ai.impl.degradation.CircuitBreakerDegradationStrategy;
import com.aimedical.modules.ai.impl.fallback.LocalRuleFallback;
import com.aimedical.modules.ai.impl.metrics.AiCallRecord;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.EndpointHealthState;
import com.aimedical.modules.ai.impl.metrics.ModelEndpointHealthManager;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.aimedical.modules.ai.impl.parser.StructuredOutputParser;
import com.aimedical.modules.ai.impl.experiment.ExperimentAssignment;
import com.aimedical.modules.ai.impl.router.ModelRoute;
import com.aimedical.modules.ai.impl.router.ModelRouter;
import com.aimedical.modules.ai.impl.template.PromptTemplateManager;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractCapabilityExecutor<T, R> implements CapabilityExecutor<T, R> {

    private static final Logger log = LoggerFactory.getLogger(AbstractCapabilityExecutor.class);

    protected static final Set<String> knownPhase4Packages = Set.of(
        "com.aimedical.modules.diagnosis",
        "com.aimedical.modules.inspection",
        "com.aimedical.modules.labtest",
        "com.aimedical.modules.image",
        "com.aimedical.modules.examination",
        "com.aimedical.modules.execution"
    );

    protected final PromptTemplateManager promptTemplateManager;
    protected final ModelRouter modelRouter;
    protected final LlmChatService llmChatService;
    protected final StructuredOutputParser structuredOutputParser;
    protected final AiMetricsCollector metricsCollector;
    protected final SlidingWindowMetricsStore metricsStore;
    protected final ModelEndpointHealthManager endpointHealthManager;
    protected final AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef;
    protected final LocalRuleFallback<T, R> localRuleFallback;
    protected final Map<String, Duration> capabilityTimeoutConfig;
    protected final Map<String, Duration> parseTimeoutConfig;
    protected final Duration parseTimeoutDefault;
    protected final Duration thinAdapterTimeout;
    protected final Map<String, Duration> thinAdapterPerCapabilityConfig;
    protected final Executor llmCallExecutor;
    protected final ObjectMapper objectMapper;
    protected volatile long elapsedInDoExecuteInternal;

    protected AbstractCapabilityExecutor(
        PromptTemplateManager promptTemplateManager,
        ModelRouter modelRouter,
        LlmChatService llmChatService,
        StructuredOutputParser structuredOutputParser,
        AiMetricsCollector metricsCollector,
        SlidingWindowMetricsStore metricsStore,
        ModelEndpointHealthManager endpointHealthManager,
        AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef,
        @Nullable LocalRuleFallback<T, R> localRuleFallback,
        Map<String, Duration> capabilityTimeoutConfig,
        Map<String, Duration> parseTimeoutConfig,
        Duration parseTimeoutDefault,
        Duration thinAdapterTimeout,
        Map<String, Duration> thinAdapterPerCapabilityConfig,
        Executor llmCallExecutor,
        ObjectMapper objectMapper
    ) {
        this.promptTemplateManager = promptTemplateManager;
        this.modelRouter = modelRouter;
        this.llmChatService = llmChatService;
        this.structuredOutputParser = structuredOutputParser;
        this.metricsCollector = metricsCollector;
        this.metricsStore = metricsStore;
        this.endpointHealthManager = endpointHealthManager;
        this.degradationStrategyMapRef = degradationStrategyMapRef;
        this.localRuleFallback = localRuleFallback;
        this.capabilityTimeoutConfig = capabilityTimeoutConfig;
        this.parseTimeoutConfig = parseTimeoutConfig;
        this.parseTimeoutDefault = parseTimeoutDefault;
        this.thinAdapterTimeout = thinAdapterTimeout;
        this.thinAdapterPerCapabilityConfig = thinAdapterPerCapabilityConfig;
        this.llmCallExecutor = llmCallExecutor;
        this.objectMapper = objectMapper;
    }

    @Override
    public final CompletableFuture<AiResult<R>> execute(T request, String capabilityId) {
        long startTime = System.currentTimeMillis();
        String userId = extractUserId();
        String departmentId = doExtractDepartmentId(request);
        String visitId = doExtractVisitId(request);
        String patientId = doExtractPatientId(request);
        String sessionId = doExtractSessionId(request);
        String callerRole = extractCallerRole();
        String callerId = extractCallerId();
        T requestCopy = defensiveCopy(request, capabilityId);
        String inputSummary = computeInputSummary(requestCopy);

        CompletableFuture<AiResult<R>> preDegraded = checkPreDegradation(
            startTime, userId, requestCopy, capabilityId, departmentId, callerRole, callerId,
            visitId, patientId, sessionId, inputSummary);
        if (preDegraded != null) {
            return preDegraded;
        }

        long capturedStartTime = startTime;
        String capturedUserId = userId;
        String capturedDepartmentId = departmentId;
        String capturedVisitId = visitId;
        String capturedPatientId = patientId;
        String capturedSessionId = sessionId;
        String capturedCallerRole = callerRole;
        String capturedCallerId = callerId;
        String capturedInputSummary = inputSummary;
        T capturedRequestCopy = requestCopy;
        String capturedCapabilityId = capabilityId;

        Executor executor = llmCallExecutor != null ? llmCallExecutor : ForkJoinPool.commonPool();
        CompletableFuture<AiResult<R>> future = CompletableFuture.supplyAsync(() -> {
            elapsedInDoExecuteInternal = System.currentTimeMillis() - capturedStartTime;
            try {
                return doExecuteInternal(capturedStartTime, capturedRequestCopy, capturedCapabilityId,
                    capturedDepartmentId, capturedUserId, capturedSessionId, capturedCallerRole, capturedCallerId,
                    capturedVisitId, capturedPatientId, capturedInputSummary);
            } finally {
                elapsedInDoExecuteInternal = System.currentTimeMillis() - capturedStartTime;
            }
        }, executor);

        Duration timeout = resolveTimeout(capabilityId);
        future = future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);

        long finalStartTime = startTime;
        String finalCapabilityId = capabilityId;
        String finalDepartmentId = departmentId;
        String finalCallerRole = callerRole;
        String finalCallerId = callerId;
        String finalVisitId = visitId;
        String finalPatientId = patientId;
        String finalSessionId = sessionId;
        String finalInputSummary = inputSummary;
        T finalRequestCopy = requestCopy;

        return future.exceptionally(throwable -> {
            Throwable cause = throwable instanceof CompletionException
                ? (throwable.getCause() != null ? throwable.getCause() : throwable)
                : throwable;
            if (cause instanceof TimeoutException) {
                String reason = refineTimeoutReason(finalCapabilityId, elapsedInDoExecuteInternal, finalRequestCopy);
                return doDegrade(capturedUserId, finalStartTime, reason, finalRequestCopy, finalCapabilityId,
                    finalDepartmentId, finalCallerRole, finalCallerId, finalVisitId, finalPatientId, finalSessionId,
                    finalInputSummary, null, null, null, null);
            }
            log.error("CapabilityExecutor 意外异常: capabilityId={}, cause={}", finalCapabilityId, cause.toString());
            throw new CompletionException(cause);
        });
    }

    @SuppressWarnings("unchecked")
    private T defensiveCopy(T request, String capabilityId) {
        try {
            return objectMapper.convertValue(request, getInputType());
        } catch (Exception e) {
            log.warn("防御性拷贝失败: capabilityId={}, inputType={}, 回退到原始 request", capabilityId, getInputType());
            return request;
        }
    }

    private CompletableFuture<AiResult<R>> checkPreDegradation(
        long startTime, String userId, T requestCopy, String capabilityId,
        String departmentId, String callerRole, String callerId,
        String visitId, String patientId, String sessionId, String inputSummary
    ) {
        if (metricsStore == null || degradationStrategyMapRef == null) {
            return null;
        }
        String requestType = requestCopy.getClass().getSimpleName();
        DegradationContext ctx = metricsStore.buildDegradationContext(capabilityId, requestType);
        ctx.setDepartmentId(departmentId);
        Map<String, List<DegradationStrategy>> currentStrategyMap = degradationStrategyMapRef.get();
        if (currentStrategyMap == null) {
            return null;
        }
        List<DegradationStrategy> strategies = currentStrategyMap.get(capabilityId);
        if (strategies == null) {
            return null;
        }
        List<DegradationStrategy> sorted = new ArrayList<>(strategies);
        sorted.sort(Comparator.comparingInt(DegradationStrategy::getOrder));
        for (DegradationStrategy strategy : sorted) {
            if (strategy.shouldDegrade(ctx)) {
                return CompletableFuture.completedFuture(doDegrade(
                    userId, startTime, DegradationReason.STRATEGY_TRIGGERED.getCode(), requestCopy,
                    capabilityId, departmentId, callerRole, callerId, visitId, patientId, sessionId,
                    inputSummary, null, null, null, strategy.getClass().getSimpleName()));
            }
        }
        return null;
    }

    protected abstract AiResult<R> doExecuteInternal(
        long startTime,
        T request,
        String capabilityId,
        String departmentId,
        String userId,
        String sessionId,
        String callerRole,
        String callerId,
        String visitId,
        String patientId,
        String inputSummary
    );

    protected AiResult<R> doDegrade(
        String userId,
        long startTime,
        String degradeReason,
        T request,
        String capabilityId,
        String departmentId,
        String callerRole,
        String callerId,
        String visitId,
        String patientId,
        String sessionId,
        String inputSummary,
        String outputSummary,
        Integer promptVersion,
        String modelId,
        String sentinelReason
    ) {
        long elapsedMs = System.currentTimeMillis() - startTime;

        if (metricsCollector != null) {
            metricsCollector.record(new AiCallRecord(
                capabilityId, modelId, promptVersion != null ? String.valueOf(promptVersion) : null,
                userId, departmentId, sessionId,
                visitId, patientId, callerRole, callerId,
                elapsedMs, true, degradeReason,
                0, 0
            ));
        }

        if (metricsStore != null) {
            metricsStore.recordDegraded(capabilityId, elapsedMs);
        }

        if (localRuleFallback != null) {
            R fallbackResult = localRuleFallback.fallback(request);
            if (fallbackResult != null) {
                if (metricsStore != null) {
        if (metricsStore != null) {
            metricsStore.recordSuccess(capabilityId, elapsedMs);
        }
                }
                return AiResult.success(fallbackResult);
            }
        }

        return AiResult.degraded(degradeReason);
    }

    protected Map<String, Object> extractVariables(T request) {
        return objectMapper.convertValue(request, Map.class);
    }

    protected String doExtractDepartmentId(T request) {
        return request instanceof AiRequestBase ? ((AiRequestBase) request).getDepartmentId() : null;
    }

    protected String doExtractVisitId(T request) {
        return request instanceof AiRequestBase ? ((AiRequestBase) request).getVisitId() : null;
    }

    protected String doExtractPatientId(T request) {
        return request instanceof AiRequestBase ? ((AiRequestBase) request).getPatientId() : null;
    }

    protected String doExtractSessionId(T request) {
        return request instanceof AiRequestBase ? ((AiRequestBase) request).getSessionId() : null;
    }

    protected String extractOutputSummary(Object result) {
        return result != null ? result.toString() : null;
    }

    protected String extractCallerRole() {
        return com.aimedical.modules.ai.impl.util.RequestContextUtils.extractCallerRole();
    }

    protected String extractCallerId() {
        return com.aimedical.modules.ai.impl.util.RequestContextUtils.extractCallerId();
    }

    protected String refineTimeoutReason(String capabilityId, long elapsedInDoExecuteInternal, T request) {
        return DegradationReason.TIMEOUT.getCode();
    }

    protected final AiResult<R> executeStandardPipeline(
        long startTime, T request, String capabilityId,
        String departmentId, String userId, String sessionId,
        String callerRole, String callerId, String visitId, String patientId,
        String inputSummary, Map<String, Object> variables,
        Integer promptVersion, String sentinelReason
    ) {
        String renderedPrompt;
        try {
            renderedPrompt = promptTemplateManager.render(capabilityId, departmentId, variables, promptVersion);
        } catch (Exception e) {
            log.warn("render failed for capabilityId: {}, fallback to default prompt", capabilityId, e);
            renderedPrompt = null;
        }
        if (renderedPrompt == null) {
            renderedPrompt = "You are a helpful medical AI assistant. Reply concisely.";
        }

        ModelRoute routeResult = modelRouter.route(capabilityId, ExperimentAssignment.createDefault());
        if (routeResult == null) {
            return doDegrade(userId, startTime, DegradationReason.NO_AVAILABLE_ROUTE.getCode(),
                request, capabilityId, departmentId, callerRole, callerId,
                visitId, patientId, sessionId, inputSummary, null, promptVersion, null, sentinelReason);
        }

        DegradationContext cbCtx = null;
        if (metricsStore != null) {
            cbCtx = metricsStore.buildDegradationContext(capabilityId, request.getClass().getSimpleName());
            cbCtx.setOperationName(routeResult.getEndpointId());
        }
        Map<String, List<DegradationStrategy>> strategyMap = degradationStrategyMapRef.get();
        if (cbCtx != null && strategyMap != null) {
            List<DegradationStrategy> strategies = strategyMap.get(capabilityId);
            if (strategies != null) {
                for (DegradationStrategy s : strategies) {
                    if (s instanceof CircuitBreakerDegradationStrategy && s.shouldDegrade(cbCtx)) {
                        return doDegrade(userId, startTime, DegradationReason.STRATEGY_TRIGGERED.getCode(),
                            request, capabilityId, departmentId, callerRole, callerId,
                            visitId, patientId, sessionId, inputSummary, null, promptVersion, null, sentinelReason);
                    }
                }
            }
        }

        boolean canProbe = false;
        if (endpointHealthManager != null) {
            EndpointHealthState healthState = endpointHealthManager.getState(routeResult.getEndpointId());
            if (healthState == EndpointHealthState.UNAVAILABLE) {
                canProbe = endpointHealthManager.tryProbe(routeResult.getEndpointId());
                if (!canProbe) {
                    return doDegrade(userId, startTime, DegradationReason.ENDPOINT_UNAVAILABLE.getCode(),
                        request, capabilityId, departmentId, callerRole, callerId,
                        visitId, patientId, sessionId, inputSummary, null, promptVersion, null, sentinelReason);
                }
            }
        }

        LlmChatOptions options = new LlmChatOptions(
            routeResult.getModelId(), 0.7, 2048, null, null, null, null);

        List<LlmChatMessage> messages = new ArrayList<>();
        messages.add(new LlmChatMessage(LlmChatMessageRole.SYSTEM, renderedPrompt));
        messages.add(new LlmChatMessage(LlmChatMessageRole.USER, request.toString()));

        ClientType clientType = routeResult.getClientType() != null ? routeResult.getClientType() : ClientType.HTTP_API;
        LlmChatRequest llmChatRequest = new LlmChatRequest(messages, options, clientType, null, null);

        long totalTimeoutMs = resolveTimeout(capabilityId).toMillis();
        if (canProbe) {
            totalTimeoutMs = totalTimeoutMs / 2;
        }
        long consumedMs = System.currentTimeMillis() - startTime;
        long remainingMs = totalTimeoutMs - consumedMs;
        if (remainingMs <= 0) {
            return doDegrade(userId, startTime, DegradationReason.TIMEOUT.getCode(),
                request, capabilityId, departmentId, callerRole, callerId,
                visitId, patientId, sessionId, inputSummary, null, promptVersion, routeResult.getModelId(), sentinelReason);
        }

        long parseTimeoutMs;
        if (parseTimeoutConfig != null && parseTimeoutConfig.containsKey(capabilityId)) {
            parseTimeoutMs = parseTimeoutConfig.get(capabilityId).toMillis();
        } else if (parseTimeoutDefault != null) {
            parseTimeoutMs = parseTimeoutDefault.toMillis();
        } else {
            parseTimeoutMs = remainingMs * 40 / 100;
        }
        long structuredChatTimeoutMs = remainingMs - parseTimeoutMs;
        if (structuredChatTimeoutMs <= 0) {
            structuredChatTimeoutMs = remainingMs / 2;
        }

        Class<R> outputType = getOutputType();
        CompletableFuture<AiResult<StructuredChatResult<R>>> structuredFuture =
            llmChatService.structuredChat(llmChatRequest, outputType);
        try {
            AiResult<StructuredChatResult<R>> result = structuredFuture.get(
                structuredChatTimeoutMs, TimeUnit.MILLISECONDS);
            if (!result.isSuccess()) {
                return doDegrade(userId, startTime, DegradationReason.PARSE_FAILURE.getCode(),
                    request, capabilityId, departmentId, callerRole, callerId,
                    visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
            }
            R parsedResult = result.getData().getData();
            LlmChatResponse.LlmChatUsage usage = result.getData().getUsage();
            long elapsedMs = System.currentTimeMillis() - startTime;
            return handleSuccess(parsedResult, usage, elapsedMs,
                capabilityId, options.getModelId(), promptVersion,
                userId, departmentId, sessionId, visitId, patientId,
                callerRole, callerId);
        } catch (TimeoutException e) {
            return doDegrade(userId, startTime, DegradationReason.TIMEOUT.getCode(),
                request, capabilityId, departmentId, callerRole, callerId,
                visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return doDegrade(userId, startTime, DegradationReason.TIMEOUT.getCode(),
                request, capabilityId, departmentId, callerRole, callerId,
                visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof StructuredOutputNotSupportedException) {
                try {
                    AiResult<LlmChatResponse> rawResult = llmChatService.chat(llmChatRequest)
                        .get(parseTimeoutMs, TimeUnit.MILLISECONDS);
                    if (!rawResult.isSuccess()) {
                        return doDegrade(userId, startTime, DegradationReason.PARSE_FAILURE.getCode(),
                            request, capabilityId, departmentId, callerRole, callerId,
                            visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
                    }
                    String rawContent = rawResult.getData().getContent();
                    R parsedResult = structuredOutputParser.parse(rawContent, outputType);
                    return handleSuccess(parsedResult, null, System.currentTimeMillis() - startTime,
                        capabilityId, options.getModelId(), promptVersion,
                        userId, departmentId, sessionId, visitId, patientId,
                        callerRole, callerId);
                } catch (TimeoutException ex) {
                    return doDegrade(userId, startTime, DegradationReason.TIMEOUT.getCode(),
                        request, capabilityId, departmentId, callerRole, callerId,
                        visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return doDegrade(userId, startTime, DegradationReason.TIMEOUT.getCode(),
                        request, capabilityId, departmentId, callerRole, callerId,
                        visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
                } catch (ExecutionException ex) {
                    Throwable causeInner = ex.getCause();
                    if (causeInner instanceof LlmInfrastructureException) {
                        return doDegrade(userId, startTime, DegradationReason.INFRASTRUCTURE_ERROR.getCode(),
                            request, capabilityId, departmentId, callerRole, callerId,
                            visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
                    } else if (isKnownPhase4BusinessException(causeInner)) {
                        throw new CompletionException(causeInner);
                    } else {
                        throw new CompletionException(causeInner);
                    }
                } catch (Exception ex) {
                    return doDegrade(userId, startTime, DegradationReason.PARSE_FAILURE.getCode(),
                        request, capabilityId, departmentId, callerRole, callerId,
                        visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
                }
            } else if (cause instanceof LlmInfrastructureException) {
                return doDegrade(userId, startTime, DegradationReason.INFRASTRUCTURE_ERROR.getCode(),
                    request, capabilityId, departmentId, callerRole, callerId,
                    visitId, patientId, sessionId, inputSummary, null, promptVersion, options.getModelId(), sentinelReason);
            } else if (isKnownPhase4BusinessException(cause)) {
                throw new CompletionException(cause);
            } else {
                throw new CompletionException(cause);
            }
        }
    }

    private AiResult<R> handleSuccess(R parsedResult, LlmChatResponse.LlmChatUsage usage,
        long elapsedMs, String capabilityId, String modelId, Integer promptVersion,
        String userId, String departmentId, String sessionId, String visitId,
        String patientId, String callerRole, String callerId) {
        if (metricsCollector != null) {
            metricsCollector.record(new AiCallRecord(
                capabilityId, modelId, promptVersion != null ? String.valueOf(promptVersion) : null, userId, departmentId,
                sessionId, visitId, patientId, callerRole, callerId, elapsedMs, false, null,
                usage != null ? usage.getPromptTokens() : 0,
                usage != null ? usage.getCompletionTokens() : 0));
        }
        if (metricsStore != null) {
            metricsStore.recordSuccess(capabilityId, elapsedMs);
        }
        return AiResult.success(parsedResult);
    }

    protected boolean isKnownPhase4BusinessException(Throwable cause) {
        String className = cause.getClass().getName();
        for (String pkg : knownPhase4Packages) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private String extractUserId() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    protected Duration resolveTimeout(String capabilityId) {
        if (capabilityTimeoutConfig != null && capabilityTimeoutConfig.containsKey(capabilityId)) {
            return capabilityTimeoutConfig.get(capabilityId);
        }
        if (thinAdapterPerCapabilityConfig != null && thinAdapterPerCapabilityConfig.containsKey(capabilityId)) {
            return thinAdapterPerCapabilityConfig.get(capabilityId);
        }
        if (thinAdapterTimeout != null) {
            return thinAdapterTimeout;
        }
        return Duration.ofSeconds(30);
    }

    private String computeInputSummary(T request) {
        Map<String, Object> vars = extractVariables(request);
        String summary = vars != null ? vars.toString() : String.valueOf(request);
        return summary.length() > 500 ? summary.substring(0, 500) : summary;
    }
}
