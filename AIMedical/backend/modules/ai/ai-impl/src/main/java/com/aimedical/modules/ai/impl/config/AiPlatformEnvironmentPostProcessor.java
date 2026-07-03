package com.aimedical.modules.ai.impl.config;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiPlatformEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(AiPlatformEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        String platformEnabled = env.getProperty("ai.platform.enabled");
        if (platformEnabled != null && env.getProperty("ai.mock.enabled") == null) {
            boolean mockDisabled = !Boolean.parseBoolean(platformEnabled);
            log.warn("ai.platform.enabled={} 且 ai.mock.enabled 未设置，自动设置 ai.mock.enabled={}",
                platformEnabled, String.valueOf(mockDisabled));
            env.getPropertySources().addLast(
                new MapPropertySource("aiPlatformForwarding",
                    Collections.singletonMap("ai.mock.enabled", String.valueOf(mockDisabled))));
        }
    }
}
