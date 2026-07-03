# 详细设计（v24 r2）

## 概述

在 `ai-impl/config/` 包新建底座统一配置装配体系，包含 AiPlatformConfig（统一 @Configuration 装配类）、AiPlatformEnvironmentPostProcessor（配置转发前置处理器）、7 个 @ConfigurationProperties 属性类、ModelRouteConfig 可变 DTO；同步修改 TimeoutDegradationStrategy 和 CircuitBreakerDegradationStrategy 添加 @Component 注解；修改 AiRouterProperties 和 DefaultModelRouter 适配可绑定配置类型；删除 AiClientConfig（被 AiPlatformConfig 吸收）。本次修订解决 refreshCapabilityTimeoutConfig() Environment 读取失效和配置缓存非原子问题。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformConfig.java` | 新建 | 底座统一配置装配类，@Configuration + @EnableConfigurationProperties + @EnableJpaRepositories + @EntityScan + @EnableAsync + ApplicationContextAware |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformEnvironmentPostProcessor.java` | 新建 | EnvironmentPostProcessor，启动早期 ai.platform.enabled → ai.mock.enabled 转发 |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiExecutionProperties.java` | 新建 | @ConfigurationProperties(prefix = "ai.execution") |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiDegradationProperties.java` | 新建 | @ConfigurationProperties(prefix = "ai.degradation") |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiRateLimitingProperties.java` | 新建 | @ConfigurationProperties(prefix = "ai.rate-limiting") |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiMetricsAsyncProperties.java` | 新建 | @ConfigurationProperties(prefix = "ai.metrics.async") |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformProperties.java` | 新建 | @ConfigurationProperties(prefix = "ai.platform") |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiSlidingWindowProperties.java` | 新建 | @ConfigurationProperties(prefix = "ai.sliding-window") |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiTemplateProperties.java` | 新建 | @ConfigurationProperties(prefix = "ai.template.fallback") |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/ModelRouteConfig.java` | 新建 | 可变 POJO，用于 Spring Boot @ConfigurationProperties setter 绑定 |
| `ai-impl/src/main/resources/META-INF/spring.factories` | 新建 | 注册 AiPlatformEnvironmentPostProcessor |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/degradation/TimeoutDegradationStrategy.java` | 修改 | 类签名添加 @Component("timeout") |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/degradation/CircuitBreakerDegradationStrategy.java` | 修改 | 类签名添加 @Component("circuit-breaker") |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/router/AiRouterProperties.java` | 修改 | routes 类型改为 Map<String, List<ModelRouteConfig>>，新增 toModelRouteMap() |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/router/DefaultModelRouter.java` | 修改 | refreshRouteTable() 适配新类型，先调用 toModelRouteMap() |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/AiClientConfig.java` | 删除 | 被 AiPlatformConfig 完全吸收 |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutor.java` | 修改 | capabilityTimeoutConfig/parseTimeoutConfig/thinAdapterPerCapabilityConfig 参数类型从 Map 改为 AtomicReference |
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/orchestrator/impl/（全部13个CapabilityExecutor）` | 修改 | 构造器签名中对应参数类型同步修改 |
| `ai-impl/src/test/java/com/aimedical/modules/ai/impl/config/AiPlatformConfigTest.java` | 新建 | 6 个测试方法 |
| `ai-impl/src/test/java/com/aimedical/modules/ai/impl/config/AiPlatformEnvironmentPostProcessorTest.java` | 新建 | 4 个测试方法 |

## 类型定义

### AiPlatformConfig

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.config`
**职责**：底座统一配置装配类，吸收 AiClientConfig 的全部 @Bean 定义；通过 @EnableConfigurationProperties 引入全部 7 个属性类；管理 4 个线程池 Bean；在 @PostConstruct 阶段构建降级策略 Map、执行启动期配置校验；提供 @Scheduled 定时刷新方法

```java
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
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;
import com.aimedical.modules.ai.impl.router.ModelRoute;

import jakarta.annotation.PostConstruct;

@Configuration
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
@EnableJpaRepositories(basePackages = "com.aimedical.modules.ai.impl")
@EntityScan(basePackages = "com.aimedical.modules.ai.impl")
@EnableAsync
@EnableScheduling
public class AiPlatformConfig implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(AiPlatformConfig.class);

    private ApplicationContext applicationContext;
    private final AtomicReference<Map<String, List<DegradationStrategy>>> strategyMapRef =
        new AtomicReference<>(Map.of());

    // 可刷新配置缓存：AtomicReference 保证 @Scheduled 原子替换
    private final AtomicReference<Map<String, Duration>> capabilityTimeoutConfigRef =
        new AtomicReference<>(new ConcurrentHashMap<>());
    private final AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfigRef =
        new AtomicReference<>(new ConcurrentHashMap<>());
    private final AtomicReference<Map<String, Duration>> parseTimeoutConfigRef =
        new AtomicReference<>(new ConcurrentHashMap<>());
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
        capabilityTimeoutConfigRef.set(new ConcurrentHashMap<>(execProps.getPerCapability()));
        thinAdapterPerCapabilityConfigRef.set(new ConcurrentHashMap<>(execProps.getThinAdapter().getPerCapability()));
        parseTimeoutConfigRef.set(new ConcurrentHashMap<>(execProps.getParse().getPerCapability()));
        parseTimeoutDefaultRef.set(execProps.getParse().getDefaultTimeout());
    }

    // --- @Bean 方法（从 AiClientConfig 吸收） ---

    @Bean
    HttpApiLlmChatService httpApiLlmChatService(
            CredentialProvider credentialProvider,
            EndpointRateLimiter endpointRateLimiter) {
        return new HttpApiLlmChatService(credentialProvider, endpointRateLimiter);
    }

    @Bean
    HttpApiLlmChatStreamService httpApiLlmChatStreamService(
            CredentialProvider credentialProvider,
            EndpointRateLimiter endpointRateLimiter) {
        return new HttpApiLlmChatStreamService(credentialProvider, endpointRateLimiter);
    }

    @Bean
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
            HttpApiLlmChatService httpApi,
            SpringAiLlmChatService springAi) {
        Map<ClientType, LlmChatService> delegates = new HashMap<>();
        delegates.put(ClientType.HTTP_API, httpApi);
        delegates.put(ClientType.SPRING_AI, springAi);
        return new DelegatingLlmChatService(delegates);
    }

    // --- 线程池 @Bean ---

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

    // --- 配置属性 @Bean（返回 AtomicReference，消费者调用 get() 读取最新值） ---

    @Bean("capabilityTimeoutConfig")
    public AtomicReference<Map<String, Duration>> capabilityTimeoutConfig() {
        return capabilityTimeoutConfigRef;
    }

    @Bean("thinAdapterPerCapabilityConfig")
    public AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig() {
        return thinAdapterPerCapabilityConfigRef;
    }

    @Bean("parseTimeoutConfig")
    public AtomicReference<Map<String, Duration>> parseTimeoutConfig() {
        return parseTimeoutConfigRef;
    }

    @Bean
    public AtomicReference<Duration> parseTimeoutDefault() {
        return parseTimeoutDefaultRef;
    }

    @Bean("degradationStrategyMapRef")
    public AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef() {
        return this.strategyMapRef;
    }

    @Bean("modelRouteMap")
    public Map<String, List<ModelRoute>> modelRouteMap(AiRouterProperties routerProperties) {
        return routerProperties.toModelRouteMap();
    }

    // --- @PostConstruct 初始化方法 ---

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

        // 校验 1：per-capability >= thin-adapter.per-capability + 5s
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

        // 校验 2：parseTimeout <= chatFallbackTimeout
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

    // --- @Scheduled 定时刷新（通过 Binder 从 Environment 重新读取，AtomicReference 原子替换） ---

    @Scheduled(fixedDelay = 60000, scheduler = "scheduledTaskExecutor")
    public void refreshDegradationStrategies() {
        log.debug("定时刷新降级策略配置");
        rebuildStrategyMap(true);
    }

    @Scheduled(fixedDelay = 60000, scheduler = "scheduledTaskExecutor")
    public void refreshCapabilityTimeoutConfig() {
        log.debug("定时刷新超时配置");
        Environment env = applicationContext.getEnvironment();
        Binder binder = Binder.get(env);
        AiExecutionProperties refreshedProps = binder
            .bind("ai.execution", Bindable.of(AiExecutionProperties.class))
            .orElseGet(AiExecutionProperties::new);
        // 原子替换，消除 clear+putAll 非原子窗口
        capabilityTimeoutConfigRef.set(new ConcurrentHashMap<>(refreshedProps.getPerCapability()));
        thinAdapterPerCapabilityConfigRef.set(new ConcurrentHashMap<>(refreshedProps.getThinAdapter().getPerCapability()));
        parseTimeoutConfigRef.set(new ConcurrentHashMap<>(refreshedProps.getParse().getPerCapability()));
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
```

**公开接口**：
- 5 个 LLM 服务 @Bean（httpApiLlmChatService、httpApiLlmChatStreamService、springAiLlmChatService、springAiLlmChatStreamService、delegatingLlmChatService）
- 4 个线程池 @Bean（llmCallExecutor 返回 Executor、metricsAsyncExecutor 返回 Executor、transcriptSummaryExecutor 返回 Executor、scheduledTaskExecutor 返回 ThreadPoolTaskScheduler）
- 6 个配置属性 @Bean（capabilityTimeoutConfig 返回 AtomicReference<Map<>>、thinAdapterPerCapabilityConfig 返回 AtomicReference<Map<>>、parseTimeoutConfig 返回 AtomicReference<Map<>>、parseTimeoutDefault 返回 AtomicReference<Duration>、degradationStrategyMapRef、modelRouteMap）
- 3 个 @Scheduled 刷新方法（refreshDegradationStrategies、refreshCapabilityTimeoutConfig、refreshWindowSeconds）

**构造方式**：Spring 容器自动管理（@Configuration）
**类型关系**：实现 `ApplicationContextAware`

### AiPlatformEnvironmentPostProcessor

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.config`
**职责**：Spring 启动早期将 `ai.platform.enabled` → `ai.mock.enabled` 反向转发，两开关互斥

```java
package com.aimedical.modules.ai.impl.config;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class AiPlatformEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        String platformEnabled = env.getProperty("ai.platform.enabled");
        if (platformEnabled != null && env.getProperty("ai.mock.enabled") == null) {
            boolean mockDisabled = !Boolean.parseBoolean(platformEnabled);
            env.getPropertySources().addLast(
                new MapPropertySource("aiPlatformForwarding",
                    Collections.singletonMap("ai.mock.enabled", String.valueOf(mockDisabled))));
        }
    }
}
```

**公开接口**：`postProcessEnvironment(ConfigurableEnvironment, SpringApplication)` — 实现 EnvironmentPostProcessor 接口
**构造方式**：由 SpringFactoriesLoader 在容器外创建实例，非 Spring 管理
**类型关系**：实现 `EnvironmentPostProcessor`

### META-INF/spring.factories

**文件路径**：`ai-impl/src/main/resources/META-INF/spring.factories`
**内容**：
```
org.springframework.boot.env.EnvironmentPostProcessor=\
com.aimedical.modules.ai.impl.config.AiPlatformEnvironmentPostProcessor
```

### AiExecutionProperties

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.config`
**职责**：`@ConfigurationProperties(prefix = "ai.execution")`，承载超时配置

```java
package com.aimedical.modules.ai.impl.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.execution")
public class AiExecutionProperties {

    private Map<String, Duration> perCapability = new HashMap<>();
    private ThinAdapterConfig thinAdapter = new ThinAdapterConfig();
    private ParseConfig parse = new ParseConfig();

    public Map<String, Duration> getPerCapability() { return perCapability; }
    public void setPerCapability(Map<String, Duration> perCapability) { this.perCapability = perCapability; }
    public ThinAdapterConfig getThinAdapter() { return thinAdapter; }
    public void setThinAdapter(ThinAdapterConfig thinAdapter) { this.thinAdapter = thinAdapter; }
    public ParseConfig getParse() { return parse; }
    public void setParse(ParseConfig parse) { this.parse = parse; }

    public static class ThinAdapterConfig {
        private Map<String, Duration> perCapability = new HashMap<>();
        private Duration defaultTimeout = Duration.ofSeconds(30);
        public Map<String, Duration> getPerCapability() { return perCapability; }
        public void setPerCapability(Map<String, Duration> perCapability) { this.perCapability = perCapability; }
        public Duration getDefaultTimeout() { return defaultTimeout; }
        public void setDefaultTimeout(Duration defaultTimeout) { this.defaultTimeout = defaultTimeout; }
    }

    public static class ParseConfig {
        private Map<String, Duration> perCapability = new HashMap<>();
        private Duration defaultTimeout = Duration.ofSeconds(5);
        public Map<String, Duration> getPerCapability() { return perCapability; }
        public void setPerCapability(Map<String, Duration> perCapability) { this.perCapability = perCapability; }
        public Duration getDefaultTimeout() { return defaultTimeout; }
        public void setDefaultTimeout(Duration defaultTimeout) { this.defaultTimeout = defaultTimeout; }
    }
}
```

**关键字段**：
- `perCapability: Map<String, Duration>` — 各能力的整体超时阈值
- `thinAdapter.perCapability: Map<String, Duration>` — 薄适配器按能力超时
- `thinAdapter.defaultTimeout: Duration` — 薄适配器默认超时，默认 30s
- `parse.perCapability: Map<String, Duration>` — 解析按能力超时
- `parse.defaultTimeout: Duration` — 解析默认超时，默认 5s

### AiDegradationProperties

```java
package com.aimedical.modules.ai.impl.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.degradation")
public class AiDegradationProperties {

    private Map<String, List<String>> strategies = new HashMap<>();
    private int contextTtlSeconds = 60;
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();

    public Map<String, List<String>> getStrategies() { return strategies; }
    public void setStrategies(Map<String, List<String>> strategies) { this.strategies = strategies; }
    public int getContextTtlSeconds() { return contextTtlSeconds; }
    public void setContextTtlSeconds(int contextTtlSeconds) { this.contextTtlSeconds = contextTtlSeconds; }
    public CircuitBreakerConfig getCircuitBreaker() { return circuitBreaker; }
    public void setCircuitBreaker(CircuitBreakerConfig circuitBreaker) { this.circuitBreaker = circuitBreaker; }

    public static class CircuitBreakerConfig {
        private double failureRateThreshold = 0.5;
        private long openWindowMs = 30000;
        public double getFailureRateThreshold() { return failureRateThreshold; }
        public void setFailureRateThreshold(double failureRateThreshold) { this.failureRateThreshold = failureRateThreshold; }
        public long getOpenWindowMs() { return openWindowMs; }
        public void setOpenWindowMs(long openWindowMs) { this.openWindowMs = openWindowMs; }
    }
}
```

**关键字段**：
- `strategies: Map<String, List<String>>` — 各能力的降级策略白名单（策略 Bean name 列表）
- `contextTtlSeconds: int` — 降级上下文 TTL，默认 60
- `circuitBreaker.failureRateThreshold: double` — 熔断失败率阈值，默认 0.5
- `circuitBreaker.openWindowMs: long` — 熔断开启窗口毫秒数，默认 30000

### AiRateLimitingProperties

```java
package com.aimedical.modules.ai.impl.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.rate-limiting")
public class AiRateLimitingProperties {

    private boolean enabled = false;
    private Map<String, EndpointRateLimitConfig> endpoints = new HashMap<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Map<String, EndpointRateLimitConfig> getEndpoints() { return endpoints; }
    public void setEndpoints(Map<String, EndpointRateLimitConfig> endpoints) { this.endpoints = endpoints; }

    public static class EndpointRateLimitConfig {
        private double permitsPerSecond = 10.0;
        private int warmupPeriodSeconds = 1;
        public double getPermitsPerSecond() { return permitsPerSecond; }
        public void setPermitsPerSecond(double permitsPerSecond) { this.permitsPerSecond = permitsPerSecond; }
        public int getWarmupPeriodSeconds() { return warmupPeriodSeconds; }
        public void setWarmupPeriodSeconds(int warmupPeriodSeconds) { this.warmupPeriodSeconds = warmupPeriodSeconds; }
    }
}
```

**关键字段**：
- `enabled: boolean` — 是否启用限流，默认 false
- `endpoints: Map<String, EndpointRateLimitConfig>` — 各 endpoint 的限流配置

### AiMetricsAsyncProperties

```java
package com.aimedical.modules.ai.impl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.metrics.async")
public class AiMetricsAsyncProperties {

    private int corePoolSize = 1;
    private int maxPoolSize = 2;
    private int queueCapacity = 1000;

    public int getCorePoolSize() { return corePoolSize; }
    public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
    public int getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
}
```

### AiPlatformProperties

```java
package com.aimedical.modules.ai.impl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.platform")
public class AiPlatformProperties {

    private boolean enabled = false;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
```

### AiSlidingWindowProperties

```java
package com.aimedical.modules.ai.impl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.sliding-window")
public class AiSlidingWindowProperties {

    private int windowSeconds = 60;
    private int maxEventsPerCapability = 10000;

    public int getWindowSeconds() { return windowSeconds; }
    public void setWindowSeconds(int windowSeconds) { this.windowSeconds = windowSeconds; }
    public int getMaxEventsPerCapability() { return maxEventsPerCapability; }
    public void setMaxEventsPerCapability(int maxEventsPerCapability) { this.maxEventsPerCapability = maxEventsPerCapability; }
}
```

### AiTemplateProperties

```java
package com.aimedical.modules.ai.impl.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.template.fallback")
public class AiTemplateProperties {

    private Map<String, String> capabilityFallback = new HashMap<>();

    public Map<String, String> getCapabilityFallback() { return capabilityFallback; }
    public void setCapabilityFallback(Map<String, String> capabilityFallback) { this.capabilityFallback = capabilityFallback; }
}
```

### ModelRouteConfig

**形态**：class（可变 POJO，含无参构造器 + setters）
**包路径**：`com.aimedical.modules.ai.impl.config`
**职责**：解决 AiRouterProperties 的 YAML 绑定限制，与 ModelRoute 字段对等但全部字段可变

```java
package com.aimedical.modules.ai.impl.config;

import java.util.HashMap;
import java.util.Map;

public class ModelRouteConfig {

    private String endpointId;
    private String clientType;
    private String authType;
    private String modelId;
    private String endpointUrl;
    private int weight;
    private long timeoutMs;
    private Map<String, Object> parameters = new HashMap<>();

    public ModelRouteConfig() {}

    public String getEndpointId() { return endpointId; }
    public void setEndpointId(String endpointId) { this.endpointId = endpointId; }
    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public long getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}
```

### AiRouterProperties（修改后）

```java
package com.aimedical.modules.ai.impl.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.aimedical.modules.ai.impl.client.AuthType;
import com.aimedical.modules.ai.impl.client.ClientType;
import com.aimedical.modules.ai.impl.config.ModelRouteConfig;

@ConfigurationProperties(prefix = "ai.router")
public class AiRouterProperties {

    private Map<String, List<ModelRouteConfig>> routes = new HashMap<>();

    public Map<String, List<ModelRouteConfig>> getRoutes() { return routes; }
    public void setRoutes(Map<String, List<ModelRouteConfig>> routes) { this.routes = routes; }

    public Map<String, List<ModelRoute>> toModelRouteMap() {
        Map<String, List<ModelRoute>> result = new HashMap<>();
        for (Map.Entry<String, List<ModelRouteConfig>> entry : routes.entrySet()) {
            String capabilityId = entry.getKey();
            List<ModelRouteConfig> configs = entry.getValue();
            List<ModelRoute> modelRoutes = new ArrayList<>();
            if (configs != null) {
                for (ModelRouteConfig config : configs) {
                    ModelRoute route = convert(config);
                    if (route != null) {
                        modelRoutes.add(route);
                    }
                }
            }
            result.put(capabilityId, modelRoutes);
        }
        return result;
    }

    private static ModelRoute convert(ModelRouteConfig config) {
        if (config == null) return null;
        ClientType clientType = null;
        if (config.getClientType() != null) {
            try {
                clientType = ClientType.valueOf(config.getClientType().toUpperCase());
            } catch (IllegalArgumentException e) {
                clientType = ClientType.HTTP_API;
            }
        }
        AuthType authType = null;
        if (config.getAuthType() != null) {
            try {
                authType = AuthType.valueOf(config.getAuthType().toUpperCase());
            } catch (IllegalArgumentException e) {
                authType = AuthType.NONE;
            }
        }
        return new ModelRoute(
            config.getEndpointId(),
            clientType,
            authType,
            config.getModelId(),
            config.getEndpointUrl(),
            config.getWeight(),
            config.getTimeoutMs(),
            config.getParameters()
        );
    }
}
```

### DefaultModelRouter（修改后）

`refreshRouteTable()` 中调用 `routerProperties.toModelRouteMap()` 先转换：

```java
@Scheduled(fixedDelay = 60000)
public void refreshRouteTable() {
    Map<String, List<ModelRoute>> rawRoutes = routerProperties.toModelRouteMap();
    Map<String, ModelRoute[]> newTable = buildRouteTable(rawRoutes);
    routeTableRef.set(newTable);
    log.debug("路由表已刷新，包含 {} 项能力路由", newTable.size());
}
```

`buildRouteTable` 的签名不变（仍接受 `Map<String, List<ModelRoute>>`）。`init()` 中的 `refreshRouteTable()` 调用不变。

### TimeoutDegradationStrategy（修改后）

类签名添加 `@Component("timeout")`，已有实现不变：

```java
@Component("timeout")
public class TimeoutDegradationStrategy implements DegradationStrategy {
    // 已有实现不变
}
```

### CircuitBreakerDegradationStrategy（修改后）

类签名添加 `@Component("circuit-breaker")`，已有实现不变：

```java
@Component("circuit-breaker")
public class CircuitBreakerDegradationStrategy implements DegradationStrategy {
    // 已有实现不变
}
```

### AbstractCapabilityExecutor（构造器参数修改）

构造器参数第11/12/15项类型变更：

```
变更前：
  Map<String, Duration> capabilityTimeoutConfig,       // 第11参
  Map<String, Duration> parseTimeoutConfig,             // 第12参
  Duration parseTimeoutDefault,                         // 第13参
  Map<String, Duration> thinAdapterPerCapabilityConfig, // 第15参

变更后：
  AtomicReference<Map<String, Duration>> capabilityTimeoutConfig,
  AtomicReference<Map<String, Duration>> parseTimeoutConfig,
  AtomicReference<Duration> parseTimeoutDefault,
  AtomicReference<Map<String, Duration>> thinAdapterPerCapabilityConfig,
```

对应字段类型同步变更，内部使用 `capabilityTimeoutConfig.get().get(key)` 读取。

### 全部 13 个 CapabilityExecutor 子类（构造器参数同步修改）

每个子类构造器中对应第11/12/13/15参数类型从 `Map<String, Duration>` / `Duration` 改为 `AtomicReference<Map<String, Duration>>` / `AtomicReference<Duration>`。

## 错误处理

| 条件 | 处理方式 |
|------|---------|
| `per-capability < thin-adapter.per-capability + 5s` | @PostConstruct 抛出 `IllegalStateException`，启动期 fail-fast |
| `parseTimeout > chatFallbackTimeout` | @PostConstruct 抛出 `IllegalStateException`，启动期 fail-fast |
| YAML 策略简名在容器中找不到对应 `@Component` Bean | 启动期和定时刷新中 WARN 日志 + 跳过该策略，不阻塞启动 |
| `Environment.getProperty()` 解析数值失败 | refreshWindowSeconds() 中 WARN 日志，使用旧值 |
| `ClientType.valueOf()` / `AuthType.valueOf()` 转换失败 | toModelRouteMap() 中静默回退默认值（HTTP_API / NONE），不抛出异常 |
| `Binder.bind()` 解析失败 | `orElseGet(AiExecutionProperties::new)` 使用全默认值，不阻塞刷新 |

## 行为契约

1. **@Component 注册**：TimeoutDegradationStrategy 和 CircuitBreakerDegradationStrategy 标注 `@Component` 的 Bean name 必须与 YAML 策略白名单中的 key 一致（`"timeout"` / `"circuit-breaker"`）
2. **AiPlatformEnvironmentPostProcessor 顺序**：必须在 Spring 容器初始化前执行，通过 `META-INF/spring.factories` 注册
3. **配置校验优先级**：`per-capability >= thin-adapter + 5s` 和 `parseTimeout <= chatFallbackTimeout` 两个校验均在 @PostConstruct 执行，校验不通过阻止应用启动
4. **Map 配置注入**：禁止使用 `@Value` 注入 Map 类型，全部通过 `@ConfigurationProperties` + `@Bean` 方法注入
5. **AiClientConfig 删除**：其 5 个 @Bean 方法和 @EnableAsync 注解已迁移至 AiPlatformConfig，删除前需确认所有引用已更新
6. **线程安全**：degradationStrategyMapRef 使用 `AtomicReference` 确保读取端无锁安全；@Scheduled 刷新方法通过 `AtomicReference.set()` 全量替换 Map 引用，消除 clear+putAll 非原子窗口；超时配置三层缓存均使用 `AtomicReference<Map<String, Duration>>`，消费者通过 `ref.get()` 获取最新快照
7. **可刷新配置模式**：`refreshCapabilityTimeoutConfig()` 通过 `Binder.get(env).bind("ai.execution", Bindable.of(AiExecutionProperties.class))` 从 Environment 逐次重新绑定，而非从已缓存的 ConfigurationProperties Bean 读取（该 Bean 不会随 Environment 运行时变更而自动重绑定）；绑定结果通过 AtomicReference.set() 原子发布
8. **refreshWindowSeconds 传播**：从 Environment 解析 `ai.sliding-window.window-seconds` 后调用 `SlidingWindowMetricsStore.setWindowSeconds(long)`，利用该字段已有的 `volatile` 保证线程间可见

## 依赖关系

### 依赖的已有类型
- `DegradationStrategy` (ai-api/degradation/)
- `ModelRoute`, `ModelRouter` (ai-impl/router/)
- `ClientType`, `AuthType` (ai-impl/client/)
- `HttpApiLlmChatService`, `HttpApiLlmChatStreamService`, `SpringAiLlmChatService`, `SpringAiLlmChatStreamService`, `DelegatingLlmChatService`, `CredentialProvider`, `EndpointRateLimiter` (ai-impl/client/)
- `SlidingWindowMetricsStore` (ai-impl/metrics/)
- `AbstractCapabilityExecutor` (ai-impl/orchestrator/) — 构造器参数类型变更为 AtomicReference
- `Binder`, `Bindable` (org.springframework.boot.context.properties.bind) — Spring Boot 内置，无新增依赖

### 未修改但存在关联的已有文件
- `LoggingMetricsCollector.java`（metrics/）— `@Async("metricsAsyncExecutor")` 引用 metricsAsyncExecutor 池
- `NoOpDegradationStrategy.java`（degradation/）— 已有 `@Component` + `@ConditionalOnMissingBean`
- `SlidingWindowMetricsStore`（metrics/）— `windowSeconds` 为 `volatile`

### 暴露给后续任务的公开接口
- `@Bean("llmCallExecutor")` — 被各 CapabilityExecutor 构造器第 16 参数注入
- `@Bean("metricsAsyncExecutor")` — 被 `LoggingMetricsCollector.record()` 上的 `@Async("metricsAsyncExecutor")` 引用
- `@Bean("transcriptSummaryExecutor")` — 被 DiscussionConclusionCapabilityExecutor 使用
- `@Bean("scheduledTaskExecutor")` — 底座内多个 @Scheduled 任务共享
- `@Bean("degradationStrategyMapRef")` — 被各 CapabilityExecutor 构造器第 9 参数注入
- `@Bean("capabilityTimeoutConfig")` 返回 `AtomicReference<Map<String, Duration>>` — 被各 CapabilityExecutor 构造器第 11 参数注入，消费者调用 `.get()` 获取最新 Map
- `@Bean("parseTimeoutConfig")` 返回 `AtomicReference<Map<String, Duration>>` — 被各 CapabilityExecutor 构造器第 12 参数注入
- `@Bean("thinAdapterPerCapabilityConfig")` 返回 `AtomicReference<Map<String, Duration>>` — 被各 CapabilityExecutor 构造器第 15 参数注入
- `@Bean("parseTimeoutDefault")` 返回 `AtomicReference<Duration>` — 被各 CapabilityExecutor 构造器第 13 参数注入

## 修订说明（v24 r2）

| 审查意见 | 修改措施 |
|---------|---------|
| [严重] `refreshCapabilityTimeoutConfig()` 通过 `getBean(AiExecutionProperties.class)` 读取配置，但 ConfigurationProperties 单例 Bean 不会随 Environment 运行时变更自动重绑定，导致定时刷新实际无操作；`Environment env` 变量未被使用（死代码） | 废弃 `getBean()` 方案，改用 `Binder.get(env).bind("ai.execution", Bindable.of(AiExecutionProperties.class))` 从 Environment 逐次重新绑定获取最新值；移除了方法体内未使用的 `Environment env` 变量 |
| [一般] `refreshCapabilityTimeoutConfig()` 中 `clear()+putAll()` 非原子操作，`clear()` 后 `putAll()` 前并发读返回 `null` | 缓存字段从 `ConcurrentHashMap` 改为 `AtomicReference<Map<String, Duration>>`，`@Scheduled` 方法创建新 `ConcurrentHashMap` 后通过 `set()` 一次性原子替换；`@Bean` 返回 `AtomicReference` 而非直接返回 Map，消费者通过 `get()` 获取最新快照；`parseTimeoutDefault` 同步改为 `AtomicReference<Duration>` |
| [轻微] `refreshCapabilityTimeoutConfig()` 中 `Environment env` 变量未被使用 | 已修复（见上），`env` 现在被 `Binder.get(env)` 使用 |
| [轻微] `@Bean("degradationStrategyMapRef")` 直接暴露 `AtomicReference` 原始引用，消费者可意外调用 `set()` | 保留当前方案（后续可优化），与已有 `degradationStrategyMapRef` 模式一致 |
