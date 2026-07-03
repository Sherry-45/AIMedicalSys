package com.aimedical.modules.ai.impl.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.aimedical.modules.ai.api.degradation.DegradationStrategy;
import com.aimedical.modules.ai.impl.client.CredentialProvider;
import com.aimedical.modules.ai.impl.client.DelegatingLlmChatService;
import com.aimedical.modules.ai.impl.client.EndpointRateLimiter;
import com.aimedical.modules.ai.impl.client.HttpApiLlmChatService;
import com.aimedical.modules.ai.impl.client.SpringAiLlmChatService;
import com.aimedical.modules.ai.impl.metrics.ModelEndpointHealthManager;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.aimedical.modules.ai.impl.router.AiRouterProperties;
import com.aimedical.modules.ai.impl.router.ModelRoute;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiPlatformConfigTest {

    private AiPlatformConfig config;
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        config = new AiPlatformConfig();
        applicationContext = mock(ApplicationContext.class);
        config.setApplicationContext(applicationContext);
    }

    private void mockDegradationBeans() {
        AiDegradationProperties degradationProps = new AiDegradationProperties();
        when(applicationContext.getBean(AiDegradationProperties.class)).thenReturn(degradationProps);
        when(applicationContext.getBeansOfType(DegradationStrategy.class)).thenReturn(Map.of());
    }

    @Test
    void classShouldBeAnnotatedWithConditionalOnProperty() {
        ConditionalOnProperty ann = AiPlatformConfig.class.getAnnotation(ConditionalOnProperty.class);
        assertNotNull(ann);
        assertArrayEquals(new String[]{"ai.platform.enabled"}, ann.name());
        assertEquals("true", ann.havingValue());
        assertFalse(ann.matchIfMissing());
    }

    @Test
    void delegatingLlmChatServiceShouldUseObjectProvider() {
        ObjectProvider<HttpApiLlmChatService> httpApiProvider = mock(ObjectProvider.class);
        ObjectProvider<SpringAiLlmChatService> springAiProvider = mock(ObjectProvider.class);
        ObjectProvider<ModelEndpointHealthManager> healthProvider = mock(ObjectProvider.class);
        when(httpApiProvider.getIfAvailable()).thenReturn(null);
        when(springAiProvider.getIfAvailable()).thenReturn(null);
        when(healthProvider.getIfAvailable()).thenReturn(null);

        DelegatingLlmChatService result = config.delegatingLlmChatService(httpApiProvider, springAiProvider, healthProvider);
        assertNotNull(result);
        verify(httpApiProvider).getIfAvailable();
        verify(springAiProvider).getIfAvailable();
    }

    @Test
    void delegatingLlmChatServiceShouldRegisterAvailableDelegates() {
        ObjectProvider<HttpApiLlmChatService> httpApiProvider = mock(ObjectProvider.class);
        ObjectProvider<SpringAiLlmChatService> springAiProvider = mock(ObjectProvider.class);
        ObjectProvider<ModelEndpointHealthManager> healthProvider = mock(ObjectProvider.class);
        HttpApiLlmChatService httpApi = mock(HttpApiLlmChatService.class);
        SpringAiLlmChatService springAi = mock(SpringAiLlmChatService.class);
        when(httpApiProvider.getIfAvailable()).thenReturn(httpApi);
        when(springAiProvider.getIfAvailable()).thenReturn(springAi);
        when(healthProvider.getIfAvailable()).thenReturn(null);

        DelegatingLlmChatService result = config.delegatingLlmChatService(httpApiProvider, springAiProvider, healthProvider);
        assertNotNull(result);
    }

    @Test
    void httpApiLlmChatServiceShouldUseObjectProvider() {
        ObjectProvider<CredentialProvider> credentialProvider = mock(ObjectProvider.class);
        ObjectProvider<EndpointRateLimiter> endpointRateLimiter = mock(ObjectProvider.class);
        when(credentialProvider.getIfAvailable()).thenReturn(null);
        when(endpointRateLimiter.getIfAvailable()).thenReturn(null);

        HttpApiLlmChatService result = config.httpApiLlmChatService(credentialProvider, endpointRateLimiter);
        assertNotNull(result);
        verify(credentialProvider).getIfAvailable();
        verify(endpointRateLimiter).getIfAvailable();
    }

    @Test
    void springAiLlmChatServiceShouldBeAnnotatedWithConditionalOnClass() throws Exception {
        ConditionalOnClass ann = AiPlatformConfig.class
            .getDeclaredMethod("springAiLlmChatService")
            .getAnnotation(ConditionalOnClass.class);
        assertNotNull(ann);
        assertEquals("org.springframework.ai.chat.ChatModel", ann.name()[0]);
    }

    @Test
    void initShouldSucceedWithValidConfiguration() {
        AiExecutionProperties execProps = new AiExecutionProperties();
        execProps.getPerCapability().put("cap1", Duration.ofSeconds(40));
        execProps.getThinAdapter().setDefaultTimeout(Duration.ofSeconds(30));
        execProps.getParse().setDefaultTimeout(Duration.ofSeconds(5));
        when(applicationContext.getBean(AiExecutionProperties.class)).thenReturn(execProps);
        mockDegradationBeans();

        assertDoesNotThrow(() -> config.init());

        assertEquals(Duration.ofSeconds(40), config.capabilityTimeoutConfig().get("cap1"));
        assertEquals(0, config.thinAdapterPerCapabilityConfig().size());
        assertNull(config.parseTimeoutConfig().get("cap1"));
        assertEquals(Duration.ofSeconds(5), config.parseTimeoutDefault());
    }

    @Test
    void initShouldThrowWhenPerCapabilityBelowThinAdapterPlus5s() {
        AiExecutionProperties execProps = new AiExecutionProperties();
        execProps.getPerCapability().put("cap1", Duration.ofSeconds(30));
        execProps.getThinAdapter().getPerCapability().put("cap1", Duration.ofSeconds(30));
        execProps.getThinAdapter().setDefaultTimeout(Duration.ofSeconds(30));
        when(applicationContext.getBean(AiExecutionProperties.class)).thenReturn(execProps);
        mockDegradationBeans();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> config.init());
        assertTrue(ex.getMessage().contains("cap1"));
    }

    @Test
    void initShouldThrowWhenParseTimeoutExceedsChatFallbackTimeout() {
        AiExecutionProperties execProps = new AiExecutionProperties();
        execProps.getPerCapability().put("cap1", Duration.ofSeconds(40));
        execProps.getThinAdapter().setDefaultTimeout(Duration.ofSeconds(30));
        execProps.getParse().getPerCapability().put("cap1", Duration.ofSeconds(50));
        execProps.getParse().setDefaultTimeout(Duration.ofSeconds(5));
        when(applicationContext.getBean(AiExecutionProperties.class)).thenReturn(execProps);
        mockDegradationBeans();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> config.init());
        assertTrue(ex.getMessage().contains("parseTimeout"));
    }

    @Test
    void initShouldCacheConfigValuesInAtomicReferences() {
        AiExecutionProperties execProps = new AiExecutionProperties();
        execProps.getPerCapability().put("cap1", Duration.ofSeconds(45));
        execProps.getThinAdapter().getPerCapability().put("cap1", Duration.ofSeconds(35));
        execProps.getThinAdapter().setDefaultTimeout(Duration.ofSeconds(30));
        execProps.getParse().getPerCapability().put("cap1", Duration.ofSeconds(10));
        execProps.getParse().setDefaultTimeout(Duration.ofSeconds(5));
        when(applicationContext.getBean(AiExecutionProperties.class)).thenReturn(execProps);
        mockDegradationBeans();

        config.init();

        assertEquals(Duration.ofSeconds(45), config.capabilityTimeoutConfig().get("cap1"));
        assertEquals(Duration.ofSeconds(35), config.thinAdapterPerCapabilityConfig().get("cap1"));
        assertEquals(Duration.ofSeconds(10), config.parseTimeoutConfig().get("cap1"));
        assertEquals(Duration.ofSeconds(5), config.parseTimeoutDefault());
    }

    @Test
    void modelRouteMapShouldDelegateToRouterProperties() {
        AiRouterProperties routerProps = new AiRouterProperties();
        ModelRouteConfig routeConfig = new ModelRouteConfig();
        routeConfig.setEndpointId("ep-1");
        routeConfig.setClientType("HTTP_API");
        routeConfig.setWeight(100);
        routeConfig.setTimeoutMs(30000L);
        routerProps.setRoutes(Map.of("cap1", List.of(routeConfig)));

        Map<String, List<ModelRoute>> result = config.modelRouteMap(routerProps);

        assertNotNull(result);
        assertTrue(result.containsKey("cap1"));
        assertEquals(1, result.get("cap1").size());
        assertEquals("ep-1", result.get("cap1").get(0).getEndpointId());
    }

    @Test
    void refreshWindowSecondsShouldUpdateMetricsStore() {
        Environment env = mock(Environment.class);
        when(env.getProperty("ai.sliding-window.window-seconds", "60")).thenReturn("120");
        when(applicationContext.getEnvironment()).thenReturn(env);
        SlidingWindowMetricsStore store = mock(SlidingWindowMetricsStore.class);
        when(applicationContext.getBean(SlidingWindowMetricsStore.class)).thenReturn(store);

        config.refreshWindowSeconds();

        verify(store).setWindowSeconds(120);
    }

    @Test
    void refreshCapabilityTimeoutConfigShouldRefreshFromBean() {
        AiExecutionProperties execProps = new AiExecutionProperties();
        execProps.getPerCapability().put("cap1", Duration.ofSeconds(45));
        execProps.getThinAdapter().getPerCapability().put("cap1", Duration.ofSeconds(35));
        execProps.getThinAdapter().setDefaultTimeout(Duration.ofSeconds(30));
        execProps.getParse().getPerCapability().put("cap1", Duration.ofSeconds(10));
        execProps.getParse().setDefaultTimeout(Duration.ofSeconds(5));
        when(applicationContext.getBean(AiExecutionProperties.class)).thenReturn(execProps);

        config.refreshCapabilityTimeoutConfig();

        assertEquals(Duration.ofSeconds(45), config.capabilityTimeoutConfig().get("cap1"));
        assertEquals(Duration.ofSeconds(35), config.thinAdapterPerCapabilityConfig().get("cap1"));
        assertEquals(Duration.ofSeconds(10), config.parseTimeoutConfig().get("cap1"));
        assertEquals(Duration.ofSeconds(5), config.parseTimeoutDefault());
    }

    @Test
    void refreshDegradationStrategiesShouldUpdateStrategyMapRef() {
        AiDegradationProperties degradationProps = new AiDegradationProperties();
        degradationProps.getStrategies().put("cap1", List.of("timeout", "circuit-breaker"));
        when(applicationContext.getBean(AiDegradationProperties.class)).thenReturn(degradationProps);

        DegradationStrategy timeout = mock(DegradationStrategy.class);
        DegradationStrategy cb = mock(DegradationStrategy.class);
        Map<String, DegradationStrategy> strategyBeans = new HashMap<>();
        strategyBeans.put("timeout", timeout);
        strategyBeans.put("circuit-breaker", cb);
        when(applicationContext.getBeansOfType(DegradationStrategy.class)).thenReturn(strategyBeans);

        config.refreshDegradationStrategies();

        Map<String, List<DegradationStrategy>> result = config.degradationStrategyMapRef().get();
        assertTrue(result.containsKey("cap1"));
        assertEquals(2, result.get("cap1").size());
        assertSame(timeout, result.get("cap1").get(0));
        assertSame(cb, result.get("cap1").get(1));
    }
}
