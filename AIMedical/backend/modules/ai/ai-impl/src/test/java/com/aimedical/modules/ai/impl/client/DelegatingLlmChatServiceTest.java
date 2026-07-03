package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class DelegatingLlmChatServiceTest {

    private LlmChatService createService(ClientType clientType, AtomicReference<LlmChatRequest> capture) {
        return new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request) {
                if (capture != null) capture.set(request);
                return CompletableFuture.completedFuture(null);
            }
            @Override
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                if (capture != null) capture.set(request);
                return CompletableFuture.completedFuture(null);
            }
            @Override
            public ClientType getClientType() {
                return clientType;
            }
        };
    }

    @Test
    void shouldConstructWithServiceMap() {
        Map<ClientType, LlmChatService> services = new HashMap<>();
        DelegatingLlmChatService delegating = new DelegatingLlmChatService(services);
        assertNotNull(delegating);
    }

    @Test
    void shouldThrowOnGetClientType() {
        Map<ClientType, LlmChatService> services = new HashMap<>();
        DelegatingLlmChatService delegating = new DelegatingLlmChatService(services);
        assertThrows(UnsupportedOperationException.class, delegating::getClientType);
    }

    @Test
    void shouldFallbackToHttpApiWhenClientTypeIsNull() {
        LlmChatService httpApi = createService(ClientType.HTTP_API, null);
        Map<ClientType, LlmChatService> map = new HashMap<>();
        map.put(ClientType.HTTP_API, httpApi);
        DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);

        LlmChatRequest request = new LlmChatRequest();
        assertDoesNotThrow(() -> delegating.chat(request));
    }

    @Test
    void shouldFallbackForUnmappedClientType() {
        LlmChatService httpApi = createService(ClientType.HTTP_API, null);
        Map<ClientType, LlmChatService> map = new HashMap<>();
        map.put(ClientType.HTTP_API, httpApi);
        DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);

        LlmChatRequest request = new LlmChatRequest(null, null, ClientType.SPRING_AI, null, null);
        assertDoesNotThrow(() -> delegating.chat(request));
    }

    @Test
    void shouldFallbackToHttpApiWhenClientTypeIsNullForStructuredChat() {
        LlmChatService httpApi = createService(ClientType.HTTP_API, null);
        Map<ClientType, LlmChatService> map = new HashMap<>();
        map.put(ClientType.HTTP_API, httpApi);
        DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);

        LlmChatRequest request = new LlmChatRequest();
        assertDoesNotThrow(() -> delegating.structuredChat(request, String.class));
    }

    @Test
    void shouldFallbackForUnmappedClientTypeForStructuredChat() {
        LlmChatService httpApi = createService(ClientType.HTTP_API, null);
        Map<ClientType, LlmChatService> map = new HashMap<>();
        map.put(ClientType.HTTP_API, httpApi);
        DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);

        LlmChatRequest request = new LlmChatRequest(null, null, ClientType.SPRING_AI, null, null);
        assertDoesNotThrow(() -> delegating.structuredChat(request, String.class));
    }

    @Test
    void shouldDispatchToCorrectClientType() {
        AtomicReference<LlmChatRequest> captured = new AtomicReference<>();
        LlmChatService httpApi = createService(ClientType.HTTP_API, null);
        LlmChatService springAi = createService(ClientType.SPRING_AI, captured);
        Map<ClientType, LlmChatService> map = new HashMap<>();
        map.put(ClientType.HTTP_API, httpApi);
        map.put(ClientType.SPRING_AI, springAi);
        DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);

        LlmChatRequest request = new LlmChatRequest(null, null, ClientType.SPRING_AI, null, null);
        delegating.chat(request);
        assertSame(request, captured.get());
    }

    @Test
    void shouldDelegateStructuredChat() {
        AtomicReference<LlmChatRequest> captured = new AtomicReference<>();
        LlmChatService httpApi = createService(ClientType.HTTP_API, null);
        LlmChatService springAi = createService(ClientType.SPRING_AI, captured);
        Map<ClientType, LlmChatService> map = new HashMap<>();
        map.put(ClientType.HTTP_API, httpApi);
        map.put(ClientType.SPRING_AI, springAi);
        DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);

        LlmChatRequest request = new LlmChatRequest(null, null, ClientType.SPRING_AI, null, null);
        delegating.structuredChat(request, String.class);
        assertSame(request, captured.get());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldProduceUnmodifiableDelegates() throws Exception {
        LlmChatService httpApi = createService(ClientType.HTTP_API, null);
        Map<ClientType, LlmChatService> map = new HashMap<>();
        map.put(ClientType.HTTP_API, httpApi);
        DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);

        Field delegatesField = DelegatingLlmChatService.class.getDeclaredField("delegates");
        delegatesField.setAccessible(true);
        Map<ClientType, LlmChatService> delegates = (Map<ClientType, LlmChatService>) delegatesField.get(delegating);

        assertThrows(UnsupportedOperationException.class,
            () -> delegates.put(ClientType.SPRING_AI, httpApi));
    }

    @Test
    void shouldNotFailOnEmptyMap() {
        Map<ClientType, LlmChatService> map = new HashMap<>();
        DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);
        assertNotNull(delegating);
    }

    @Test
    void shouldPropagateExceptionFromDelegate() {
        LlmChatService failingService = new LlmChatService() {
            @Override
            public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request) {
                return CompletableFuture.failedFuture(new RuntimeException("delegate error"));
            }
            @Override
            public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
                    LlmChatRequest request, Class<T> targetClass) {
                return CompletableFuture.failedFuture(new RuntimeException("delegate error"));
            }
            @Override
            public ClientType getClientType() {
                return ClientType.HTTP_API;
            }
        };

        Map<ClientType, LlmChatService> map = new HashMap<>();
        map.put(ClientType.HTTP_API, failingService);
        DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);

        LlmChatRequest request = new LlmChatRequest(null, null, ClientType.HTTP_API, null, null);
        CompletableFuture<AiResult<LlmChatResponse>> future = delegating.chat(request);
        ExecutionException ex = assertThrows(ExecutionException.class, () -> future.get());
        assertEquals("delegate error", ex.getCause().getMessage());

        CompletableFuture<AiResult<StructuredChatResult<String>>> structuredFuture =
                delegating.structuredChat(request, String.class);
        ExecutionException structuredEx = assertThrows(ExecutionException.class, () -> structuredFuture.get());
        assertEquals("delegate error", structuredEx.getCause().getMessage());
    }

    @Test
    void shouldReturnFailedFutureWhenFallbackIsNull() {
        LlmChatService springAi = createService(ClientType.SPRING_AI, null);
        Map<ClientType, LlmChatService> map = new HashMap<>();
        map.put(ClientType.SPRING_AI, springAi);
        DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);

        LlmChatRequest request = new LlmChatRequest();
        CompletableFuture<AiResult<LlmChatResponse>> future = delegating.chat(request);
        ExecutionException ex = assertThrows(ExecutionException.class, () -> future.get());
        assertInstanceOf(LlmInfrastructureException.class, ex.getCause());
        assertEquals("no fallback available", ex.getCause().getMessage());
    }

    @Test
    void shouldLogWarnForMissingClientTypesOnPostConstruct() {
        Logger logger = (Logger) LoggerFactory.getLogger(DelegatingLlmChatService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            Map<ClientType, LlmChatService> map = new HashMap<>();
            map.put(ClientType.HTTP_API, createService(ClientType.HTTP_API, null));
            DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);
            delegating.checkDelegatesCompleteness();

            long missingCount = ClientType.values().length - 1;
            assertEquals(missingCount, appender.list.size());
            for (ILoggingEvent event : appender.list) {
                assertEquals(Level.WARN, event.getLevel());
                assertTrue(event.getFormattedMessage().contains("没有对应的 LlmChatService 实现"));
            }
        } finally {
            appender.stop();
            logger.detachAppender(appender);
        }
    }

    @Test
    void shouldNotLogWarnWhenAllClientTypesMappedOnPostConstruct() {
        Logger logger = (Logger) LoggerFactory.getLogger(DelegatingLlmChatService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            Map<ClientType, LlmChatService> map = new HashMap<>();
            for (ClientType ct : ClientType.values()) {
                map.put(ct, createService(ct, null));
            }
            DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);
            delegating.checkDelegatesCompleteness();

            assertEquals(0, appender.list.size());
        } finally {
            appender.stop();
            logger.detachAppender(appender);
        }
    }

    @Test
    void shouldLogWarnWithEndpointIdAndHealthCheckOnChatFallback() {
        Logger logger = (Logger) LoggerFactory.getLogger(DelegatingLlmChatService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            LlmChatService httpApi = createService(ClientType.HTTP_API, null);
            Map<ClientType, LlmChatService> map = new HashMap<>();
            map.put(ClientType.HTTP_API, httpApi);
            DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);

            LlmChatRequest request = new LlmChatRequest(null, null, ClientType.SPRING_AI, null, "test-ep-id");
            delegating.chat(request);

            assertEquals(1, appender.list.size());
            ILoggingEvent event = appender.list.get(0);
            assertEquals(Level.WARN, event.getLevel());
            String msg = event.getFormattedMessage();
            assertTrue(msg.contains("test-ep-id"));
            assertTrue(msg.contains("健康检查"));
        } finally {
            appender.stop();
            logger.detachAppender(appender);
        }
    }

    @Test
    void shouldLogWarnWithEndpointIdAndHealthCheckOnStructuredChatFallback() {
        Logger logger = (Logger) LoggerFactory.getLogger(DelegatingLlmChatService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            LlmChatService httpApi = createService(ClientType.HTTP_API, null);
            Map<ClientType, LlmChatService> map = new HashMap<>();
            map.put(ClientType.HTTP_API, httpApi);
            DelegatingLlmChatService delegating = new DelegatingLlmChatService(map);

            LlmChatRequest request = new LlmChatRequest(null, null, ClientType.SPRING_AI, null, "test-ep-id");
            delegating.structuredChat(request, String.class);

            assertEquals(1, appender.list.size());
            ILoggingEvent event = appender.list.get(0);
            assertEquals(Level.WARN, event.getLevel());
            String msg = event.getFormattedMessage();
            assertTrue(msg.contains("test-ep-id"));
            assertTrue(msg.contains("健康检查"));
        } finally {
            appender.stop();
            logger.detachAppender(appender);
        }
    }
}
