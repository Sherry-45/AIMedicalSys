package com.aimedical.modules.ai.impl.client;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;
import com.aimedical.modules.ai.impl.metrics.ModelEndpointHealthManager;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DelegatingLlmChatService implements LlmChatService {

    private static final Logger log = LoggerFactory.getLogger(DelegatingLlmChatService.class);

    private final Map<ClientType, LlmChatService> delegates;
    private final ObjectProvider<ModelEndpointHealthManager> endpointHealthManagerProvider;

    public DelegatingLlmChatService(Map<ClientType, LlmChatService> delegates) {
        this(delegates, null);
    }

    public DelegatingLlmChatService(Map<ClientType, LlmChatService> delegates,
                                    ObjectProvider<ModelEndpointHealthManager> endpointHealthManagerProvider) {
        this.delegates = Collections.unmodifiableMap(new HashMap<>(delegates));
        this.endpointHealthManagerProvider = endpointHealthManagerProvider;
    }

    @PostConstruct
    void checkDelegatesCompleteness() {
        for (ClientType ct : ClientType.values()) {
            if (!delegates.containsKey(ct)) {
                log.warn("ClientType={} 没有对应的 LlmChatService 实现，请在配置中注册", ct);
            }
        }
    }

    @Override
    public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request) {
        ClientType ct = request.getClientType();
        LlmChatService delegate = delegates.get(ct);
        if (ct == null || delegate == null) {
            String endpointId = request.getEndpointId();
            log.warn("未找到 ClientType={} 的实现（endpointId={}），回退到 HTTP_API；建议检查健康检查端点状态",
                ct, endpointId);
            reportFallbackHealth(endpointId);
            delegate = delegates.get(ClientType.HTTP_API);
        }
        if (delegate == null) {
            return CompletableFuture.failedFuture(new LlmInfrastructureException("no fallback available"));
        }
        return delegate.chat(request);
    }

    @Override
    public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
            LlmChatRequest request, Class<T> targetClass) {
        ClientType ct = request.getClientType();
        LlmChatService delegate = delegates.get(ct);
        if (ct == null || delegate == null) {
            String endpointId = request.getEndpointId();
            log.warn("未找到 ClientType={} 的实现（endpointId={}），回退到 HTTP_API；建议检查健康检查端点状态",
                ct, endpointId);
            reportFallbackHealth(endpointId);
            delegate = delegates.get(ClientType.HTTP_API);
        }
        if (delegate == null) {
            return CompletableFuture.failedFuture(new LlmInfrastructureException("no fallback available"));
        }
        return delegate.structuredChat(request, targetClass);
    }

    private void reportFallbackHealth(String endpointId) {
        if (endpointId == null || endpointHealthManagerProvider == null) {
            return;
        }
        ModelEndpointHealthManager healthManager = endpointHealthManagerProvider.getIfAvailable();
        if (healthManager == null) {
            return;
        }
        try {
            healthManager.recordCallResult(endpointId, false, 0L);
            log.info("已上报健康检查告警: endpointId={} 标记为失败，触发 fallback", endpointId);
        } catch (Exception e) {
            log.warn("上报健康检查告警失败: endpointId={}, error={}", endpointId, e.toString());
        }
    }

    @Override
    public ClientType getClientType() {
        throw new UnsupportedOperationException("DelegatingLlmChatService does not have its own ClientType");
    }
}
