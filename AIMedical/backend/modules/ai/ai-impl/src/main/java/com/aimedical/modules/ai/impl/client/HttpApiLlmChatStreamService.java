package com.aimedical.modules.ai.impl.client;

import reactor.core.publisher.Flux;

public class HttpApiLlmChatStreamService implements LlmChatStreamService {

    private final CredentialProvider credentialProvider;
    private final EndpointRateLimiter endpointRateLimiter;

    public HttpApiLlmChatStreamService(CredentialProvider credentialProvider,
                                       EndpointRateLimiter endpointRateLimiter) {
        this.credentialProvider = credentialProvider;
        this.endpointRateLimiter = endpointRateLimiter;
    }

    @Override
    public Flux<LlmChatResponse> chatStream(LlmChatRequest request) {
        return Flux.empty();
    }
}
