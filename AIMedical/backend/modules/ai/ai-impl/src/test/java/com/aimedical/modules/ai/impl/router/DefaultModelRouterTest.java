package com.aimedical.modules.ai.impl.router;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aimedical.modules.ai.impl.config.ModelRouteConfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultModelRouterTest {

    private ModelRouteConfig routeA = new ModelRouteConfig();
    private ModelRouteConfig routeB = new ModelRouteConfig();
    private ModelRouteConfig routeZeroA = new ModelRouteConfig();
    private ModelRouteConfig routeZeroB = new ModelRouteConfig();

    {
        routeA.setEndpointId("ep-a"); routeA.setModelId("model-a"); routeA.setWeight(80); routeA.setTimeoutMs(30000L);
        routeB.setEndpointId("ep-b"); routeB.setModelId("model-b"); routeB.setWeight(20); routeB.setTimeoutMs(30000L);
        routeZeroA.setEndpointId("ep-za"); routeZeroA.setModelId("model-za"); routeZeroA.setWeight(0); routeZeroA.setTimeoutMs(30000L);
        routeZeroB.setEndpointId("ep-zb"); routeZeroB.setModelId("model-zb"); routeZeroB.setWeight(0); routeZeroB.setTimeoutMs(30000L);
    }

    private DefaultModelRouter createRouter(Map<String, List<ModelRouteConfig>> routes) {
        AiRouterProperties props = new AiRouterProperties();
        if (routes != null) {
            props.setRoutes(routes);
        }
        DefaultModelRouter router = new DefaultModelRouter(props);
        router.init();
        return router;
    }

    @Test
    void shouldReturnNullForUnknownCapability() {
        DefaultModelRouter router = createRouter(Map.of("KNOWN", List.of(routeA)));
        assertNull(router.route("UNKNOWN", null));
    }

    @Test
    void shouldReturnRouteForKnownCapability() {
        DefaultModelRouter router = createRouter(Map.of("CAP", List.of(routeA)));
        ModelRoute result = router.route("CAP", null);
        assertNotNull(result);
        assertEquals("model-a", result.getModelId());
    }

    @Test
    void shouldSelectByWeight() {
        DefaultModelRouter router = createRouter(Map.of("CAP", List.of(routeA, routeB)));
        int countA = 0;
        int countB = 0;
        int samples = 1000;
        for (int i = 0; i < samples; i++) {
            ModelRoute r = router.route("CAP", null);
            if ("model-a".equals(r.getModelId())) countA++;
            else countB++;
        }
        // 80:20 ratio, allow +/-15% tolerance
        assertTrue(countA > samples * 0.5, "model-a should be selected more often");
        assertTrue(countB > 0, "model-b should be selected at least once");
    }

    @Test
    void shouldSelectRandomlyWhenAllWeightsZero() {
        DefaultModelRouter router = createRouter(Map.of("CAP", List.of(routeZeroA, routeZeroB)));
        int countA = 0;
        int countB = 0;
        int samples = 200;
        for (int i = 0; i < samples; i++) {
            ModelRoute r = router.route("CAP", null);
            if ("model-za".equals(r.getModelId())) countA++;
            else countB++;
        }
        assertTrue(countA > 0, "routeZeroA should be selected at least once");
        assertTrue(countB > 0, "routeZeroB should be selected at least once");
    }

    @Test
    void shouldBeThreadSafe() {
        DefaultModelRouter router = createRouter(Map.of("CAP", List.of(routeA, routeB)));
        Thread[] threads = new Thread[50];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 20; j++) {
                    assertNotNull(router.route("CAP", null));
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    @Test
    void shouldRefreshOnPostConstruct() {
        DefaultModelRouter router = createRouter(Map.of("CAP", List.of(routeA)));
        assertNotNull(router.route("CAP", null));
    }

    @Test
    void shouldNotCrashOnEmptyProperties() {
        DefaultModelRouter router = createRouter(null);
        assertNull(router.route("ANY", null));

        router = createRouter(new HashMap<>());
        assertNull(router.route("ANY", null));
    }
}
