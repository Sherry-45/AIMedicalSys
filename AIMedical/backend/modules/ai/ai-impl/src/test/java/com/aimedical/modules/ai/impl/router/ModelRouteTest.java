package com.aimedical.modules.ai.impl.router;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.aimedical.modules.ai.impl.client.AuthType;
import com.aimedical.modules.ai.impl.client.ClientType;

import static org.junit.jupiter.api.Assertions.*;

class ModelRouteTest {

    @Test
    void shouldCreateWithAllFields() {
        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");
        ModelRoute route = new ModelRoute("ep1", ClientType.HTTP_API, AuthType.API_KEY,
            "gpt-4", "https://api.example.com", 5, 30000L, params);
        assertEquals("ep1", route.getEndpointId());
        assertEquals(ClientType.HTTP_API, route.getClientType());
        assertEquals(AuthType.API_KEY, route.getAuthType());
        assertEquals("gpt-4", route.getModelId());
        assertEquals("https://api.example.com", route.getEndpointUrl());
        assertEquals(5, route.getWeight());
        assertEquals(30000L, route.getTimeoutMs());
        assertEquals(Map.of("key", "value"), route.getParameters());
    }

    @Test
    void shouldCreateViaOfFactoryWithMinimalFields() {
        ModelRoute route = ModelRoute.of("model-1");
        assertEquals("model-1", route.getModelId());
        assertNull(route.getEndpointId());
        assertNull(route.getClientType());
        assertNull(route.getAuthType());
        assertNull(route.getEndpointUrl());
        assertEquals(1, route.getWeight());
        assertEquals(30000L, route.getTimeoutMs());
        assertTrue(route.getParameters().isEmpty());
    }

    @Test
    void shouldDefensivelyCopyParametersInConstructor() {
        Map<String, Object> original = new HashMap<>();
        original.put("key", "value");
        ModelRoute route = new ModelRoute(null, null, null, "m", null, 1, 1000L, original);
        original.put("key", "modified");
        assertEquals("value", route.getParameters().get("key"));
    }

    @Test
    void shouldReturnUnmodifiableParameters() {
        ModelRoute route = new ModelRoute(null, null, null, "m", null, 1, 1000L, Map.of("k", "v"));
        assertThrows(UnsupportedOperationException.class, () -> route.getParameters().put("x", "y"));
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Map<String, Object> params = Map.of("k", "v");
        ModelRoute r1 = new ModelRoute("ep1", ClientType.HTTP_API, AuthType.API_KEY, "gpt-4", "url", 5, 30000L, params);
        ModelRoute r2 = new ModelRoute("ep1", ClientType.HTTP_API, AuthType.API_KEY, "gpt-4", "url", 5, 30000L, params);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        ModelRoute r3 = new ModelRoute("ep2", ClientType.HTTP_API, AuthType.API_KEY, "gpt-4", "url", 5, 30000L, params);
        assertNotEquals(r1, r3);
    }

    @Test
    void shouldHandleNullParameters() {
        ModelRoute route = new ModelRoute(null, null, null, "m", null, 1, 1000L, null);
        assertNotNull(route.getParameters());
        assertTrue(route.getParameters().isEmpty());
    }
}
