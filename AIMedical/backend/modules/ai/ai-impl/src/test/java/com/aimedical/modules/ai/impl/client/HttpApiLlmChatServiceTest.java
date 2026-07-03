package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.impl.client.CredentialProvider.Credential;
import com.aimedical.modules.ai.impl.client.exception.AiAbilityInputInvalidException;
import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class HttpApiLlmChatServiceTest {

    private CredentialProvider credentialProvider;
    private EndpointRateLimiter endpointRateLimiter;
    private HttpApiLlmChatService service;

    @BeforeEach
    void setUp() {
        credentialProvider = mock(CredentialProvider.class);
        endpointRateLimiter = mock(EndpointRateLimiter.class);
        service = new HttpApiLlmChatService(credentialProvider, endpointRateLimiter);
    }

    @Test
    void shouldReturnHttpApiClientType() {
        assertEquals(ClientType.HTTP_API, service.getClientType());
    }

    @Test
    void shouldThrowWhenEndpointIdIsNull() {
        LlmChatRequest request = new LlmChatRequest();
        AiAbilityInputInvalidException ex = assertThrows(
                AiAbilityInputInvalidException.class,
                () -> service.chat(request));
        assertEquals("endpointId must not be null", ex.getMessage());
    }

    @Test
    void shouldThrowWhenCredentialMissing() {
        when(credentialProvider.getCredential("test-endpoint")).thenReturn(Optional.empty());
        LlmChatRequest request = new LlmChatRequest(null, null, null, null, "test-endpoint");
        LlmInfrastructureException ex = assertThrows(
                LlmInfrastructureException.class,
                () -> service.chat(request));
        assertTrue(ex.getMessage().contains("test-endpoint"));
    }

    @Test
    void shouldThrowWhenRateLimited() {
        when(credentialProvider.getCredential("test-endpoint"))
                .thenReturn(Optional.of(new Credential(AuthType.API_KEY, "token", Instant.MAX, "/vault")));
        when(endpointRateLimiter.tryAcquire("test-endpoint")).thenReturn(false);
        LlmChatRequest request = new LlmChatRequest(null, null, null, null, "test-endpoint");
        LlmInfrastructureException ex = assertThrows(
                LlmInfrastructureException.class,
                () -> service.chat(request));
        assertTrue(ex.getMessage().contains("RATE_LIMITED"));
    }

    @Test
    void shouldReturnHttpErrorResult() {
        when(credentialProvider.getCredential("http://localhost:1"))
                .thenReturn(Optional.of(new Credential(AuthType.API_KEY, "token", Instant.MAX, "/vault")));
        when(endpointRateLimiter.tryAcquire("http://localhost:1")).thenReturn(true);
        LlmChatRequest request = new LlmChatRequest(null, null, null, null, "http://localhost:1");
        CompletableFuture<AiResult<LlmChatResponse>> future = service.chat(request);
        AiResult<LlmChatResponse> result = future.join();
        assertFalse(result.isSuccess());
        assertEquals("HTTP_ERROR", result.getErrorCode());
    }

    @Test
    @Timeout(10)
    void shouldReturnChatSuccessResult() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat", exchange -> {
            byte[] resp = "{\"content\":\"hi\",\"usage\":{\"promptTokens\":1,\"completionTokens\":1,\"totalTokens\":2},\"modelId\":\"m\",\"retryCount\":0}".getBytes();
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            String url = "http://localhost:" + port + "/chat";
            when(credentialProvider.getCredential(url))
                    .thenReturn(Optional.of(new Credential(AuthType.API_KEY, "token", Instant.MAX, "/vault")));
            when(endpointRateLimiter.tryAcquire(url)).thenReturn(true);
            LlmChatRequest request = new LlmChatRequest(null, null, null, null, url);
            CompletableFuture<AiResult<LlmChatResponse>> future = service.chat(request);
            AiResult<LlmChatResponse> result = future.join();
            assertTrue(result.isSuccess());
            assertEquals("hi", result.getData().getContent());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldReturnFailureForStructuredChat() throws Exception {
        LlmChatRequest request = new LlmChatRequest();
        CompletableFuture<AiResult<StructuredChatResult<String>>> future =
                service.structuredChat(request, String.class);
        AiResult<StructuredChatResult<String>> result = future.get();
        assertFalse(result.isSuccess());
        assertEquals("NOT_SUPPORTED", result.getErrorCode());
    }

    @Test
    void shouldUseEndpointUrlWhenProvided() {
        when(credentialProvider.getCredential("test-endpoint"))
                .thenReturn(Optional.of(new Credential(AuthType.API_KEY, "token", Instant.MAX, "/vault")));
        when(endpointRateLimiter.tryAcquire("test-endpoint")).thenReturn(false);
        LlmChatRequest request = new LlmChatRequest(null, null, null, null, "test-endpoint", "http://custom-url");
        LlmInfrastructureException ex = assertThrows(
                LlmInfrastructureException.class,
                () -> service.chat(request));
        assertTrue(ex.getMessage().contains("test-endpoint"));
    }
}
