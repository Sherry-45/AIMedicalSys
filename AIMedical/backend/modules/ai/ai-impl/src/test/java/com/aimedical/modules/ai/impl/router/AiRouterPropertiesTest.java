package com.aimedical.modules.ai.impl.router;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.aimedical.modules.ai.impl.config.ModelRouteConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AiRouterPropertiesTest {

    private Logger logbackLogger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        logbackLogger = (Logger) LoggerFactory.getLogger(AiRouterProperties.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logbackLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logbackLogger.detachAppender(listAppender);
    }

    @Test
    void shouldConvertValidClientType() {
        AiRouterProperties props = new AiRouterProperties();
        ModelRouteConfig config = new ModelRouteConfig();
        config.setEndpointId("ep1");
        config.setClientType("http_api");
        config.setAuthType("none");
        config.setModelId("model-1");
        config.setWeight(100);
        config.setTimeoutMs(30000L);
        props.setRoutes(Map.of("CAP", List.of(config)));

        Map<String, List<ModelRoute>> result = props.toModelRouteMap();
        assertNotNull(result.get("CAP"));
        assertEquals(1, result.get("CAP").size());
        assertEquals("model-1", result.get("CAP").get(0).getModelId());
        assertEquals(0, listAppender.list.size());
    }

    @Test
    void shouldLogWarningForInvalidClientType() {
        AiRouterProperties props = new AiRouterProperties();
        ModelRouteConfig config = new ModelRouteConfig();
        config.setEndpointId("ep1");
        config.setClientType("INVALID_CLIENT");
        config.setAuthType("none");
        config.setModelId("model-1");
        config.setWeight(100);
        config.setTimeoutMs(30000L);
        props.setRoutes(Map.of("CAP", List.of(config)));

        Map<String, List<ModelRoute>> result = props.toModelRouteMap();

        assertEquals(1, listAppender.list.size());
        assertEquals(Level.WARN, listAppender.list.get(0).getLevel());
        assertTrue(listAppender.list.get(0).getFormattedMessage().contains("Invalid clientType value"));
        assertTrue(listAppender.list.get(0).getFormattedMessage().contains("INVALID_CLIENT"));
        assertEquals(com.aimedical.modules.ai.impl.client.ClientType.HTTP_API,
                result.get("CAP").get(0).getClientType());
    }

    @Test
    void shouldLogWarningForInvalidAuthType() {
        AiRouterProperties props = new AiRouterProperties();
        ModelRouteConfig config = new ModelRouteConfig();
        config.setEndpointId("ep1");
        config.setAuthType("INVALID_AUTH");
        config.setClientType("http_api");
        config.setModelId("model-1");
        config.setWeight(100);
        config.setTimeoutMs(30000L);
        props.setRoutes(Map.of("CAP", List.of(config)));

        Map<String, List<ModelRoute>> result = props.toModelRouteMap();

        assertEquals(1, listAppender.list.size());
        assertEquals(Level.WARN, listAppender.list.get(0).getLevel());
        assertTrue(listAppender.list.get(0).getFormattedMessage().contains("Invalid authType value"));
        assertTrue(listAppender.list.get(0).getFormattedMessage().contains("INVALID_AUTH"));
        assertEquals(com.aimedical.modules.ai.impl.client.AuthType.NONE,
                result.get("CAP").get(0).getAuthType());
    }

    @Test
    void shouldHandleBothInvalidEnums() {
        AiRouterProperties props = new AiRouterProperties();
        ModelRouteConfig config = new ModelRouteConfig();
        config.setEndpointId("ep1");
        config.setClientType("BAD_CLIENT");
        config.setAuthType("BAD_AUTH");
        config.setModelId("model-1");
        config.setWeight(100);
        config.setTimeoutMs(30000L);
        props.setRoutes(Map.of("CAP", List.of(config)));

        Map<String, List<ModelRoute>> result = props.toModelRouteMap();

        assertEquals(2, listAppender.list.size());
        assertTrue(listAppender.list.get(0).getFormattedMessage().contains("clientType"));
        assertTrue(listAppender.list.get(1).getFormattedMessage().contains("authType"));
        assertEquals(com.aimedical.modules.ai.impl.client.ClientType.HTTP_API,
                result.get("CAP").get(0).getClientType());
        assertEquals(com.aimedical.modules.ai.impl.client.AuthType.NONE,
                result.get("CAP").get(0).getAuthType());
    }

    @Test
    void shouldNotLogWarningWhenClientTypeIsNull() {
        AiRouterProperties props = new AiRouterProperties();
        ModelRouteConfig config = new ModelRouteConfig();
        config.setEndpointId("ep1");
        config.setClientType(null);
        config.setAuthType("none");
        config.setModelId("model-1");
        config.setWeight(100);
        config.setTimeoutMs(30000L);
        props.setRoutes(Map.of("CAP", List.of(config)));

        props.toModelRouteMap();

        assertEquals(0, listAppender.list.size());
    }

    @Test
    void shouldNotLogWarningWhenAuthTypeIsNull() {
        AiRouterProperties props = new AiRouterProperties();
        ModelRouteConfig config = new ModelRouteConfig();
        config.setEndpointId("ep1");
        config.setClientType("http_api");
        config.setAuthType(null);
        config.setModelId("model-1");
        config.setWeight(100);
        config.setTimeoutMs(30000L);
        props.setRoutes(Map.of("CAP", List.of(config)));

        props.toModelRouteMap();

        assertEquals(0, listAppender.list.size());
    }

    @Test
    void shouldHandleNullConfigGracefully() {
        AiRouterProperties props = new AiRouterProperties();
        props.setRoutes(Map.of("CAP", Collections.singletonList(null)));

        Map<String, List<ModelRoute>> result = props.toModelRouteMap();

        assertNotNull(result.get("CAP"));
        assertTrue(result.get("CAP").isEmpty());
        assertEquals(0, listAppender.list.size());
    }

    @Test
    void shouldHandleEmptyRoutes() {
        AiRouterProperties props = new AiRouterProperties();

        Map<String, List<ModelRoute>> result = props.toModelRouteMap();

        assertTrue(result.isEmpty());
        assertEquals(0, listAppender.list.size());
    }
}
