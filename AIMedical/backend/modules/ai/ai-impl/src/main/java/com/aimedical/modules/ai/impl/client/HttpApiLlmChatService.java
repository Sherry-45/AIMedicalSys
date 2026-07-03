package com.aimedical.modules.ai.impl.client;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.impl.client.exception.AiAbilityInputInvalidException;
import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;
import com.aimedical.modules.ai.impl.client.CredentialProvider.Credential;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class HttpApiLlmChatService implements LlmChatService {

    private final CredentialProvider credentialProvider;
    private final EndpointRateLimiter endpointRateLimiter;
    private final HttpClient httpClient;

    public HttpApiLlmChatService(CredentialProvider credentialProvider,
                                  EndpointRateLimiter endpointRateLimiter,
                                  HttpClient httpClient) {
        this.credentialProvider = credentialProvider;
        this.endpointRateLimiter = endpointRateLimiter;
        this.httpClient = httpClient;
    }

    public HttpApiLlmChatService(CredentialProvider credentialProvider,
                                  EndpointRateLimiter endpointRateLimiter) {
        this(credentialProvider, endpointRateLimiter,
             HttpClient.newBuilder()
                 .connectTimeout(Duration.ofSeconds(10))
                 .build());
    }

    @Override
    public ClientType getClientType() {
        return ClientType.HTTP_API;
    }

    @Override
    public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request) {
        try {
            String endpointId = request.getEndpointId();
            if (endpointId == null) {
                throw new AiAbilityInputInvalidException("endpointId must not be null");
            }

            Optional<Credential> credentialOpt = credentialProvider.getCredential(endpointId);
            if (credentialOpt.isEmpty()) {
                throw new LlmInfrastructureException("No credential for endpoint: " + endpointId);
            }

            boolean acquired = endpointRateLimiter.tryAcquire(endpointId);
            if (!acquired) {
                throw new LlmInfrastructureException("RATE_LIMITED: " + endpointId);
            }

            Credential credential = credentialOpt.get();
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(request);

            String url = request.getEndpointUrl() != null ? request.getEndpointUrl() : endpointId;
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + credential.getCredentialValue())
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> httpResponse = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            LlmChatResponse chatResponse = mapper.readValue(httpResponse.body(), LlmChatResponse.class);
            return CompletableFuture.completedFuture(AiResult.success(chatResponse));

        } catch (AiAbilityInputInvalidException | LlmInfrastructureException e) {
            throw e;
        } catch (Exception e) {
            return CompletableFuture.completedFuture(AiResult.failure("HTTP_ERROR", e.getMessage()));
        }
    }

    @Override
    public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
            LlmChatRequest request, Class<T> targetClass) {
        return CompletableFuture.completedFuture(
            AiResult.failure("NOT_SUPPORTED", "structuredChat not implemented for HTTP API"));
    }
}
