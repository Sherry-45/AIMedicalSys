package com.aimedical.modules.ai.impl.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.aimedical.modules.ai.api.degradation.DegradationStrategy;
import com.aimedical.modules.ai.impl.client.ClientType;
import com.aimedical.modules.ai.impl.client.CredentialProvider;
import com.aimedical.modules.ai.impl.client.DelegatingLlmChatService;
import com.aimedical.modules.ai.impl.client.EndpointRateLimiter;
import com.aimedical.modules.ai.impl.client.HttpApiLlmChatService;
import com.aimedical.modules.ai.impl.client.HttpApiLlmChatStreamService;
import com.aimedical.modules.ai.impl.client.LlmChatService;
import com.aimedical.modules.ai.impl.client.SpringAiLlmChatService;
import com.aimedical.modules.ai.impl.client.SpringAiLlmChatStreamService;
import com.aimedical.modules.ai.impl.degradation.CircuitBreakerDegradationStrategy;
import com.aimedical.modules.ai.impl.degradation.TimeoutDegradationStrategy;
import com.aimedical.modules.ai.impl.experiment.ExperimentRepository;
import com.aimedical.modules.ai.impl.metrics.AiCallLogRepository;
import com.aimedical.modules.ai.impl.metrics.AiCallLogStatsRepository;
import com.aimedical.modules.ai.impl.metrics.ModelEndpointHealthManager;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.aimedical.modules.ai.impl.parser.JsonStructuredOutputParser;
import com.aimedical.modules.ai.impl.parser.StructuredOutputParser;
import com.aimedical.modules.ai.impl.router.AiRouterProperties;
import com.aimedical.modules.ai.impl.router.ModelRoute;
import com.aimedical.modules.ai.impl.template.PromptTemplateRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;

@Configuration
@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties({
    AiRouterProperties.class,
    AiExecutionProperties.class,
    AiDegradationProperties.class,
    AiRateLimitingProperties.class,
    AiMetricsAsyncProperties.class,
    AiPlatformProperties.class,
    AiSlidingWindowProperties.class,
    AiTemplateProperties.class
})
@EntityScan(basePackages = "com.aimedical.modules.ai.impl")
@EnableAsync
@EnableScheduling
public class AiPlatformConfig implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(AiPlatformConfig.class);

    private ApplicationContext applicationContext;
    private final AtomicReference<Map<String, List<DegradationStrategy>>> strategyMapRef =
        new AtomicReference<>(Map.of());

    private final ConcurrentHashMap<String, Duration> capabilityTimeoutConfigMap =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Duration> thinAdapterPerCapabilityConfigMap =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Duration> parseTimeoutConfigMap =
        new ConcurrentHashMap<>();
    private final AtomicReference<Duration> parseTimeoutDefaultRef =
        new AtomicReference<>(Duration.ofSeconds(5));

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        cacheInitialConfigValues();
        rebuildStrategyMap(true);
        validateConfig();
    }

    private void cacheInitialConfigValues() {
        AiExecutionProperties execProps = applicationContext.getBean(AiExecutionProperties.class);
        capabilityTimeoutConfigMap.putAll(execProps.getPerCapability());
        thinAdapterPerCapabilityConfigMap.putAll(execProps.getThinAdapter().getPerCapability());
        parseTimeoutConfigMap.putAll(execProps.getParse().getPerCapability());
        parseTimeoutDefaultRef.set(execProps.getParse().getDefaultTimeout());
    }

    @Bean
    HttpApiLlmChatService httpApiLlmChatService(
            ObjectProvider<CredentialProvider> credentialProviderProvider,
            ObjectProvider<EndpointRateLimiter> endpointRateLimiterProvider) {
        return new HttpApiLlmChatService(
            credentialProviderProvider.getIfAvailable(),
            endpointRateLimiterProvider.getIfAvailable());
    }

    @Bean
    HttpApiLlmChatStreamService httpApiLlmChatStreamService(
            CredentialProvider credentialProvider,
            EndpointRateLimiter endpointRateLimiter) {
        return new HttpApiLlmChatStreamService(credentialProvider, endpointRateLimiter);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.ai.chat.ChatModel")
    SpringAiLlmChatService springAiLlmChatService() {
        return new SpringAiLlmChatService();
    }

    @Bean
    SpringAiLlmChatStreamService springAiLlmChatStreamService() {
        return new SpringAiLlmChatStreamService();
    }

    @Bean
    @Primary
    DelegatingLlmChatService delegatingLlmChatService(
            ObjectProvider<HttpApiLlmChatService> httpApiProvider,
            ObjectProvider<SpringAiLlmChatService> springAiProvider,
            ObjectProvider<ModelEndpointHealthManager> endpointHealthManagerProvider) {
        Map<ClientType, LlmChatService> delegates = new HashMap<>();
        HttpApiLlmChatService httpApi = httpApiProvider.getIfAvailable();
        if (httpApi != null) {
            delegates.put(ClientType.HTTP_API, httpApi);
        }
        SpringAiLlmChatService springAi = springAiProvider.getIfAvailable();
        if (springAi != null) {
            delegates.put(ClientType.SPRING_AI, springAi);
        }
        return new DelegatingLlmChatService(delegates, endpointHealthManagerProvider);
    }

    @Bean("llmCallExecutor")
    public Executor llmCallExecutor() {
        int coreSize = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(coreSize, 2 * coreSize, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean("metricsAsyncExecutor")
    public Executor metricsAsyncExecutor(AiMetricsAsyncProperties metricsAsyncProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(metricsAsyncProperties.getCorePoolSize());
        executor.setMaxPoolSize(metricsAsyncProperties.getMaxPoolSize());
        executor.setQueueCapacity(metricsAsyncProperties.getQueueCapacity());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy() {
            @Override public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                log.warn("指标采集任务被丢弃: queueSize={}, activeCount={}",
                    e.getQueue().size(), e.getActiveCount());
                super.rejectedExecution(r, e);
            }
        });
        executor.setThreadNamePrefix("metrics-async-");
        executor.initialize();
        return executor;
    }

    @Bean("transcriptSummaryExecutor")
    public Executor transcriptSummaryExecutor() {
        int coreSize = Math.min(Runtime.getRuntime().availableProcessors(), 4);
        return new ThreadPoolExecutor(coreSize, 2 * coreSize, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(20),
            new ThreadPoolExecutor.DiscardPolicy() {
                @Override public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                    log.warn("转录摘要压缩任务被丢弃: queueSize={}, activeCount={}",
                        e.getQueue().size(), e.getActiveCount());
                    super.rejectedExecution(r, e);
                }
            });
    }

    @Bean("scheduledTaskExecutor")
    public ThreadPoolTaskScheduler scheduledTaskExecutor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(3);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public StructuredOutputParser structuredOutputParser(ObjectMapper objectMapper) {
        return new JsonStructuredOutputParser(objectMapper);
    }

    @Bean("capabilityTimeoutConfig")
    public Map<String, Duration> capabilityTimeoutConfig() {
        return capabilityTimeoutConfigMap;
    }

    @Bean("circuit-breaker")
    public CircuitBreakerDegradationStrategy circuitBreakerDegradationStrategy(
            SlidingWindowMetricsStore metricsStore, AiDegradationProperties degradationProperties) {
        AiDegradationProperties.CircuitBreakerConfig cbConfig = degradationProperties.getCircuitBreaker();
        double threshold = cbConfig.getFailureRateThreshold();
        Duration openWindow = Duration.ofMillis(cbConfig.getOpenWindowMs());
        log.info("创建 CircuitBreakerDegradationStrategy: failureRateThreshold={}, openWindow={}",
            threshold, openWindow);
        return new CircuitBreakerDegradationStrategy(metricsStore, threshold, openWindow);
    }

    @Bean("timeout")
    public TimeoutDegradationStrategy timeoutDegradationStrategy(AiDegradationProperties degradationProperties) {
        AiDegradationProperties.TimeoutConfig timeoutConfig = degradationProperties.getTimeout();
        Duration threshold = Duration.ofMillis(timeoutConfig.getThresholdMs());
        log.info("创建 TimeoutDegradationStrategy: threshold={}", threshold);
        return new TimeoutDegradationStrategy(threshold);
    }

    @Bean("thinAdapterPerCapabilityConfig")
    public Map<String, Duration> thinAdapterPerCapabilityConfig() {
        return thinAdapterPerCapabilityConfigMap;
    }

    @Bean("parseTimeoutConfig")
    public Map<String, Duration> parseTimeoutConfig() {
        return parseTimeoutConfigMap;
    }

    @Bean
    public Duration parseTimeoutDefault() {
        return parseTimeoutDefaultRef.get();
    }

    @Bean("degradationStrategyMapRef")
    public AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef() {
        return this.strategyMapRef;
    }

    @Bean("modelRouteMap")
    public Map<String, List<ModelRoute>> modelRouteMap(AiRouterProperties routerProperties) {
        return routerProperties.toModelRouteMap();
    }

    private void rebuildStrategyMap(boolean warnOnMissing) {
        Map<String, DegradationStrategy> strategyBeans =
            applicationContext.getBeansOfType(DegradationStrategy.class);
        AiDegradationProperties degradationProps =
            applicationContext.getBean(AiDegradationProperties.class);
        Map<String, List<String>> strategyConfig = degradationProps.getStrategies();
        Map<String, List<DegradationStrategy>> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : strategyConfig.entrySet()) {
            String capabilityId = entry.getKey();
            List<String> strategyNames = entry.getValue();
            List<DegradationStrategy> capabilityStrategies = new ArrayList<>();
            if (strategyNames != null) {
                for (String name : strategyNames) {
                    DegradationStrategy strategy = strategyBeans.get(name);
                    if (strategy != null) {
                        capabilityStrategies.add(strategy);
                    } else if (warnOnMissing) {
                        log.warn("策略 {} 未在容器中找到对应的 Bean，跳过", name);
                    }
                }
            }
            result.put(capabilityId, capabilityStrategies);
        }
        this.strategyMapRef.set(result);
    }

    private void validateConfig() {
        AiExecutionProperties execProps = applicationContext.getBean(AiExecutionProperties.class);
        Map<String, Duration> perCap = execProps.getPerCapability();
        Map<String, Duration> thinAdapterPerCap = execProps.getThinAdapter().getPerCapability();
        Duration thinDefault = execProps.getThinAdapter().getDefaultTimeout();

        for (Map.Entry<String, Duration> entry : perCap.entrySet()) {
            String capId = entry.getKey();
            Duration mainTimeout = entry.getValue();
            Duration adapterTimeout = thinAdapterPerCap.getOrDefault(capId, thinDefault);
            if (mainTimeout.compareTo(adapterTimeout.plusSeconds(5)) < 0) {
                throw new IllegalStateException(
                    "配置校验失败: capability " + capId + " 的 per-capability (" + mainTimeout
                    + ") 必须 >= thin-adapter.per-capability (" + adapterTimeout + ") + 5s");
            }
        }

        Map<String, Duration> parsePerCap = execProps.getParse().getPerCapability();
        Duration parseDefault = execProps.getParse().getDefaultTimeout();
        for (Map.Entry<String, Duration> entry : perCap.entrySet()) {
            String capId = entry.getKey();
            Duration chatFallbackTimeout = entry.getValue();
            Duration parseTimeout = parsePerCap.getOrDefault(capId, parseDefault);
            if (parseTimeout.compareTo(chatFallbackTimeout) > 0) {
                throw new IllegalStateException(
                    "配置校验失败: capability " + capId + " 的 parseTimeout (" + parseTimeout
                    + ") 必须 <= chatFallbackTimeout (" + chatFallbackTimeout + ")");
            }
        }
    }

    @Scheduled(fixedDelay = 60000, scheduler = "scheduledTaskExecutor")
    public void refreshDegradationStrategies() {
        log.debug("定时刷新降级策略配置");
        rebuildStrategyMap(true);
    }

    @Scheduled(fixedDelay = 60000, scheduler = "scheduledTaskExecutor")
    public void refreshCapabilityTimeoutConfig() {
        log.debug("定时刷新超时配置");
        AiExecutionProperties refreshedProps = applicationContext.getBean(AiExecutionProperties.class);
        capabilityTimeoutConfigMap.clear();
        capabilityTimeoutConfigMap.putAll(refreshedProps.getPerCapability());
        thinAdapterPerCapabilityConfigMap.clear();
        thinAdapterPerCapabilityConfigMap.putAll(refreshedProps.getThinAdapter().getPerCapability());
        parseTimeoutConfigMap.clear();
        parseTimeoutConfigMap.putAll(refreshedProps.getParse().getPerCapability());
        parseTimeoutDefaultRef.set(refreshedProps.getParse().getDefaultTimeout());
        log.debug("超时配置已刷新，包含 {} 项能力", refreshedProps.getPerCapability().size());
    }

    @Scheduled(fixedDelay = 60000, scheduler = "scheduledTaskExecutor")
    public void refreshWindowSeconds() {
        log.debug("定时刷新滑动窗口配置");
        Environment env = applicationContext.getEnvironment();
        String windowStr = env.getProperty("ai.sliding-window.window-seconds", "60");
        try {
            long windowSeconds = Long.parseLong(windowStr);
            SlidingWindowMetricsStore store = applicationContext.getBean(SlidingWindowMetricsStore.class);
            store.setWindowSeconds(windowSeconds);
            log.debug("滑动窗口配置已刷新: windowSeconds={}", windowSeconds);
        } catch (NumberFormatException e) {
            log.warn("解析 ai.sliding-window.window-seconds 失败: {}", windowStr);
        }
    }
}
