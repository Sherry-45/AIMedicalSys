package com.aimedical.modules.ai.impl.orchestrator.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.degradation.DegradationReason;
import com.aimedical.modules.ai.api.degradation.DegradationStrategy;
import com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionRequest;
import com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionResponse;
import com.aimedical.modules.ai.api.dto.discussion.DiscussionTranscript;
import com.aimedical.modules.ai.impl.client.ClientType;
import com.aimedical.modules.ai.impl.client.LlmChatMessage;
import com.aimedical.modules.ai.impl.client.LlmChatMessageRole;
import com.aimedical.modules.ai.impl.client.LlmChatOptions;
import com.aimedical.modules.ai.impl.client.LlmChatRequest;
import com.aimedical.modules.ai.impl.client.LlmChatResponse;
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
@Service("DISCUSSION_CONCLUSION")
public class DiscussionConclusionCapabilityExecutor
    extends AbstractCapabilityExecutor<DiscussionConclusionRequest, DiscussionConclusionResponse> {

    private static final Logger log = LoggerFactory.getLogger(DiscussionConclusionCapabilityExecutor.class);

    private static final int TOKEN_THRESHOLD = 4000;
    private static final double TRANSCRIPT_SUMMARY_CROWDING_RATIO = 0.2;

    @Value("${ai.prompt.version.DISCUSSION_CONCLUSION:0}")
    private Integer promptVersion;

    @Value("${ai.sentinel.reason.discussion-conclusion:}")
    private String sentinelReason;

    private final String compressionLightweightEndpoint;
    private final ClientType compressionLightweightClientType;
    private final Duration transcriptSummaryTimeout;

    private final Executor transcriptSummaryExecutor;

    private volatile long transcriptSummaryElapsedMs;

    @Autowired
    public DiscussionConclusionCapabilityExecutor(
        PromptTemplateManager promptTemplateManager,
        ModelRouter modelRouter, LlmChatService llmChatService,
        StructuredOutputParser structuredOutputParser, AiMetricsCollector metricsCollector,
        SlidingWindowMetricsStore metricsStore, ModelEndpointHealthManager endpointHealthManager,
        AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef,
        @Autowired(required = false)
        LocalRuleFallback<DiscussionConclusionRequest, DiscussionConclusionResponse> localRuleFallback,
        Map<String, Duration> capabilityTimeoutConfig, Map<String, Duration> parseTimeoutConfig,
        Duration parseTimeoutDefault, Duration thinAdapterTimeout,
        Map<String, Duration> thinAdapterPerCapabilityConfig,
        Executor llmCallExecutor, ObjectMapper objectMapper,
        @Value("${ai.compression.lightweight-endpoint:compress-default}") String compressionLightweightEndpoint,
        @Value("${ai.compression.lightweight-client-type:HTTP_API}") ClientType compressionLightweightClientType,
        @Value("${ai.execution.timeout.transcript-summary:15s}") Duration transcriptSummaryTimeout,
        @Qualifier("transcriptSummaryExecutor") Executor transcriptSummaryExecutor
    ) {
        super(promptTemplateManager, modelRouter, llmChatService,
              structuredOutputParser, metricsCollector, metricsStore, endpointHealthManager,
              degradationStrategyMapRef, localRuleFallback, capabilityTimeoutConfig,
              parseTimeoutConfig, parseTimeoutDefault, thinAdapterTimeout,
              thinAdapterPerCapabilityConfig, llmCallExecutor, objectMapper);
        this.compressionLightweightEndpoint = compressionLightweightEndpoint;
        this.compressionLightweightClientType = compressionLightweightClientType;
        this.transcriptSummaryTimeout = transcriptSummaryTimeout;
        this.transcriptSummaryExecutor = transcriptSummaryExecutor;
    }

    @Override public String getCapabilityId() { return "DISCUSSION_CONCLUSION"; }
    @Override public Class<DiscussionConclusionRequest> getInputType() { return DiscussionConclusionRequest.class; }
    @Override public Class<DiscussionConclusionResponse> getOutputType() { return DiscussionConclusionResponse.class; }

    @Override
    protected AiResult<DiscussionConclusionResponse> doExecuteInternal(
        long startTime, DiscussionConclusionRequest request, String capabilityId,
        String departmentId, String userId, String sessionId,
        String callerRole, String callerId, String visitId, String patientId,
        String inputSummary
    ) {
        List<DiscussionTranscript> transcripts = request.getTranscripts();
        transcriptSummaryElapsedMs = 0;
        if (transcripts != null && !transcripts.isEmpty()) {
            int estimatedTokens = estimateTokenCount(transcripts);
            if (estimatedTokens > TOKEN_THRESHOLD) {
                long compressStart = System.currentTimeMillis();
                try {
                    String compressed = compressTranscripts(transcripts, estimatedTokens);
                    DiscussionTranscript compressedTranscript = new DiscussionTranscript();
                    compressedTranscript.setSpeakerRole("SYSTEM");
                    compressedTranscript.setSpeakerName("compressor");
                    compressedTranscript.setTimestamp(String.valueOf(System.currentTimeMillis()));
                    compressedTranscript.setContent(compressed);
                    DiscussionConclusionRequest newRequest = new DiscussionConclusionRequest();
                    newRequest.setTranscripts(List.of(compressedTranscript));
                    request = newRequest;
                } catch (Exception e) {
                    log.warn("transcript 压缩失败，回退截断: {}", e.toString());
                    DiscussionConclusionRequest newRequest = new DiscussionConclusionRequest();
                    newRequest.setTranscripts(truncateTranscripts(transcripts, 2000));
                    request = newRequest;
                }
                transcriptSummaryElapsedMs = System.currentTimeMillis() - compressStart;
            }
        }

        Map<String, Object> variables = extractVariables(request);
        return executeStandardPipeline(startTime, request, capabilityId,
            departmentId, userId, sessionId, callerRole, callerId, visitId, patientId,
            inputSummary, variables, this.promptVersion, this.sentinelReason);
    }

    @Override
    protected String refineTimeoutReason(String capabilityId, long elapsedInDoExecuteInternal, DiscussionConclusionRequest request) {
        Duration capabilityTimeout = resolveTimeout(capabilityId);
        if (elapsedInDoExecuteInternal - transcriptSummaryElapsedMs < capabilityTimeout.toMillis() * TRANSCRIPT_SUMMARY_CROWDING_RATIO) {
            return DegradationReason.TIMEOUT.getCode() + ":transcriptSummaryCrowding";
        }
        return DegradationReason.TIMEOUT.getCode() + ":primaryLlmTimeout";
    }

    private int estimateTokenCount(List<DiscussionTranscript> transcripts) {
        int totalChars = 0;
        for (DiscussionTranscript t : transcripts) {
            totalChars += t.getContent() != null ? t.getContent().length() : 0;
        }
        return totalChars / 4 + 1;
    }

    private String formatTranscripts(List<DiscussionTranscript> transcripts) {
        StringBuilder sb = new StringBuilder();
        for (DiscussionTranscript t : transcripts) {
            sb.append("[").append(t.getSpeakerRole()).append("] ")
              .append(t.getSpeakerName()).append(" (").append(t.getTimestamp()).append("):\n")
              .append(t.getContent()).append("\n");
        }
        return sb.toString();
    }

    private List<DiscussionTranscript> truncateTranscripts(List<DiscussionTranscript> transcripts, int maxTokens) {
        int accumulated = 0;
        int cutoffIndex = transcripts.size();
        for (int i = 0; i < transcripts.size(); i++) {
            int len = transcripts.get(i).getContent() != null ? transcripts.get(i).getContent().length() : 0;
            accumulated += len;
            if (accumulated > maxTokens * 4) {
                cutoffIndex = i;
                break;
            }
        }
        return new ArrayList<>(transcripts.subList(0, cutoffIndex));
    }

    private String compressTranscripts(List<DiscussionTranscript> transcripts, int estimatedTokens) throws Exception {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            String formatted = formatTranscripts(transcripts);
            LlmChatRequest req = buildCompressionRequest(formatted);
            try {
                AiResult<LlmChatResponse> result = llmChatService.chat(req).get(
                    transcriptSummaryTimeout.toMillis(), TimeUnit.MILLISECONDS);
                if (result.isSuccess() && result.getData() != null) {
                    return result.getData().getContent();
                }
                throw new RuntimeException("Compression failed: "
                    + (result.isDegraded() ? result.getFallbackReason() : "unknown"));
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, transcriptSummaryExecutor);
        return future.get(transcriptSummaryTimeout.toMillis() + 5000, TimeUnit.MILLISECONDS);
    }

    private LlmChatRequest buildCompressionRequest(String formattedTranscripts) {
        LlmChatOptions options = new LlmChatOptions(
            compressionLightweightEndpoint, 0.3, 1024, null, null, null, null);

        List<LlmChatMessage> messages = new ArrayList<>();
        messages.add(new LlmChatMessage(LlmChatMessageRole.USER,
            "请对以下讨论记录进行简要摘要压缩，保留关键诊疗信息:\n\n" + formattedTranscripts));

        return new LlmChatRequest(messages, options, compressionLightweightClientType, null, null);
    }
}
