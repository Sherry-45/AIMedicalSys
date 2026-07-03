package com.aimedical.modules.ai.impl.config;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import static org.junit.jupiter.api.Assertions.*;

class AiPlatformEnvironmentPostProcessorTest {

    private final AiPlatformEnvironmentPostProcessor processor = new AiPlatformEnvironmentPostProcessor();

    @Test
    void shouldForwardDisabledWhenPlatformEnabled() {
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(
            new MapPropertySource("test", Map.of("ai.platform.enabled", "true")));

        processor.postProcessEnvironment(env, null);

        assertEquals("false", env.getProperty("ai.mock.enabled"));
    }

    @Test
    void shouldForwardEnabledWhenPlatformDisabled() {
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(
            new MapPropertySource("test", Map.of("ai.platform.enabled", "false")));

        processor.postProcessEnvironment(env, null);

        assertEquals("true", env.getProperty("ai.mock.enabled"));
    }

    @Test
    void shouldNotForwardWhenNoPlatformEnabledProperty() {
        StandardEnvironment env = new StandardEnvironment();

        processor.postProcessEnvironment(env, null);

        assertNull(env.getProperty("ai.mock.enabled"));
    }

    @Test
    void shouldNotForwardWhenMockEnabledAlreadyExists() {
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(
            new MapPropertySource("test", Map.of(
                "ai.platform.enabled", "true",
                "ai.mock.enabled", "already_set")));

        processor.postProcessEnvironment(env, null);

        assertEquals("already_set", env.getProperty("ai.mock.enabled"));
    }

    @Test
    void shouldLogWarningWhenForwardingMockEnabled() {
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(
            new MapPropertySource("test", Map.of("ai.platform.enabled", "true")));

        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(AiPlatformEnvironmentPostProcessor.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            processor.postProcessEnvironment(env, null);
            assertEquals(1, appender.list.size());
            assertEquals(Level.WARN, appender.list.get(0).getLevel());
            assertTrue(appender.list.get(0).getFormattedMessage().contains("ai.mock.enabled"),
                "Log should mention ai.mock.enabled");
        } finally {
            appender.stop();
            logger.detachAppender(appender);
        }
    }
}
