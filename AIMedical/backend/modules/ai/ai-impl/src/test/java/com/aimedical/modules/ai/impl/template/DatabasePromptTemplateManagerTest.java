package com.aimedical.modules.ai.impl.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.aimedical.modules.ai.impl.template.TemplateStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabasePromptTemplateManagerTest {

    @Mock
    private PromptTemplateRepository repository;

    @Mock
    private Environment environment;

    @InjectMocks
    private DatabasePromptTemplateManager manager;

    private PromptTemplate activeDept1;
    private PromptTemplate activeGlobal;

    @BeforeEach
    void setUp() {
        activeDept1 = new PromptTemplate(1L, "diag", "dept1", "You are a diagnostic AI for {{department}}", 1, ACTIVE);
        activeGlobal = new PromptTemplate(2L, "diag", null, "You are a diagnostic AI", 1, ACTIVE);
    }

    // --- Base paths ---

    @Test
    void shouldReturnCachedContentOnCacheHit() {
        when(repository.findByStatus(ACTIVE)).thenReturn(List.of(activeDept1));
        manager.warmup();
        String result = manager.render("diag", "dept1", Map.of("department", "cardiology"), null);
        assertEquals("You are a diagnostic AI for cardiology", result);
        verify(repository, never()).findByCapabilityIdAndDepartmentIdAndStatus(any(), any(), any());
    }

    @Test
    void shouldQueryDbOnCacheMiss() {
        when(repository.findByCapabilityIdAndDepartmentIdAndStatus("diag", "dept1", ACTIVE))
                .thenReturn(List.of(activeDept1));
        String result = manager.render("diag", "dept1", Map.of("department", "cardiology"), null);
        assertEquals("You are a diagnostic AI for cardiology", result);
    }

    @Test
    void warmupShouldPrepopulateCache() {
        when(repository.findByStatus(ACTIVE)).thenReturn(List.of(activeDept1, activeGlobal));
        manager.warmup();
        verify(repository, never()).findByCapabilityIdAndDepartmentIdAndStatus(any(), any(), any());
        assertEquals("You are a diagnostic AI for cardiology",
                manager.render("diag", "dept1", Map.of("department", "cardiology"), null));
        assertEquals("You are a diagnostic AI",
                manager.render("diag", null, Map.of(), null));
    }

    @Test
    void eventShouldInvalidateCacheEntry() {
        when(repository.findByStatus(ACTIVE)).thenReturn(List.of(activeDept1));
        manager.warmup();
        manager.onTemplateChanged(new TemplateChangedEvent(this, "diag", "dept1", null,
                TemplateChangedEvent.ChangeType.UPDATED, Instant.now()));
        when(repository.findByCapabilityIdAndDepartmentIdAndStatus("diag", "dept1", ACTIVE))
                .thenReturn(List.of(activeDept1));
        manager.render("diag", "dept1", Map.of("department", "neurology"), null);
        verify(repository).findByCapabilityIdAndDepartmentIdAndStatus("diag", "dept1", ACTIVE);
    }

    @Test
    void fallbackShouldReturnConfiguredValue() {
        when(environment.getProperty("ai.template.fallback.diag")).thenReturn("Custom fallback");
        assertEquals("Custom fallback", manager.getFallbackPrompt("diag"));
    }

    @Test
    void fallbackShouldReturnDefaultWhenNoConfig() {
        when(environment.getProperty("ai.template.fallback.triage")).thenReturn(null);
        assertEquals("You are a helpful medical AI assistant. Reply concisely.",
                manager.getFallbackPrompt("triage"));
    }

    @Test
    void concurrentRenderShouldNotThrow() throws InterruptedException {
        when(repository.findByStatus(ACTIVE)).thenReturn(List.of(activeDept1));
        manager.warmup();
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    manager.render("diag", "dept1", Map.of("department", "cardio"), null);
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        assertEquals(0, errors.get());
    }

    @Test
    void nullVariableValueShouldRetainPlaceholder() {
        when(repository.findByStatus(ACTIVE)).thenReturn(List.of(activeDept1));
        manager.warmup();
        Map<String, Object> vars = new java.util.HashMap<>();
        vars.put("department", null);
        String result = manager.render("diag", "dept1", vars, null);
        assertTrue(result.contains("{{department}}"));
    }

    // --- promptVersion branches ---

    @Test
    void exactVersionActiveShouldReturnVersionContent() {
        PromptTemplate versioned = new PromptTemplate(3L, "diag", "dept1", "Version 2 template", 2, ACTIVE);
        when(repository.findByCapabilityIdAndDepartmentIdAndVersion("diag", "dept1", 2))
                .thenReturn(Optional.of(versioned));
        String result = manager.render("diag", "dept1", Map.of(), 2);
        assertEquals("Version 2 template", result);
        verify(repository, never()).findByCapabilityIdAndDepartmentIdAndStatus(any(), any(), any());
    }

    @Test
    void nonActiveVersionShouldFallbackToActive() {
        PromptTemplate deprecated = new PromptTemplate(3L, "diag", "dept1", "Old version", 2, DEPRECATED);
        when(repository.findByCapabilityIdAndDepartmentIdAndVersion("diag", "dept1", 2))
                .thenReturn(Optional.of(deprecated));
        when(repository.findByCapabilityIdAndDepartmentIdAndStatus("diag", "dept1", ACTIVE))
                .thenReturn(List.of(activeDept1));
        String result = manager.render("diag", "dept1", Map.of("department", "cardio"), 2);
        assertEquals("You are a diagnostic AI for cardio", result);
    }

    @Test
    void versionNotFoundInDepartmentShouldFallbackToGlobal() {
        when(repository.findByCapabilityIdAndDepartmentIdAndVersion("diag", "dept1", 2))
                .thenReturn(Optional.empty());
        when(repository.findByCapabilityIdAndDepartmentIdAndVersion("diag", null, 2))
                .thenReturn(Optional.of(activeGlobal));
        String result = manager.render("diag", "dept1", Map.of(), 2);
        assertEquals("You are a diagnostic AI", result);
    }

    @Test
    void nullVersionShouldFallbackToActive() {
        when(repository.findByCapabilityIdAndDepartmentIdAndStatus("diag", "dept1", ACTIVE))
                .thenReturn(List.of(activeDept1));
        String result = manager.render("diag", "dept1", Map.of("department", "cardio"), null);
        assertEquals("You are a diagnostic AI for cardio", result);
    }

    @Test
    void allQueriesReturnNullShouldReturnNull() {
        when(repository.findByCapabilityIdAndDepartmentIdAndStatus("diag", "dept1", ACTIVE))
                .thenReturn(List.of());
        when(repository.findByCapabilityIdAndDepartmentIdAndStatus("diag", null, ACTIVE))
                .thenReturn(List.of());
        String result = manager.render("diag", "dept1", Map.of(), null);
        assertNull(result);
    }

    @Test
    void eventWithNullDepartmentIdShouldInvalidateAllAndRewarm() {
        when(repository.findByStatus(ACTIVE)).thenReturn(List.of(activeDept1, activeGlobal));
        manager.warmup();
        // cache populated, render from cache
        assertEquals("You are a diagnostic AI for cardiology",
                manager.render("diag", "dept1", Map.of("department", "cardiology"), null));
        // fire event with null departmentId → invalidateAll + warmup
        manager.onTemplateChanged(new TemplateChangedEvent(this, "diag", null, null,
                TemplateChangedEvent.ChangeType.UPDATED, Instant.now()));
        verify(repository, times(2)).findByStatus(ACTIVE);
        // cache should work after rewarming
        assertEquals("You are a diagnostic AI for cardiology",
                manager.render("diag", "dept1", Map.of("department", "cardiology"), null));
    }

    @Test
    void dbExceptionShouldReturnNull() {
        when(repository.findByCapabilityIdAndDepartmentIdAndStatus("diag", "dept1", ACTIVE))
                .thenThrow(new RuntimeException("DB error"));
        assertNull(manager.render("diag", "dept1", Map.of(), null));
    }

    // --- T55: warmup version key caching ---

    @Test
    void warmupShouldCreateVersionCacheKeys() {
        when(repository.findByStatus(ACTIVE)).thenReturn(List.of(activeDept1));
        manager.warmup();
        String result = manager.render("diag", "dept1", Map.of("department", "cardiology"), 1);
        assertEquals("You are a diagnostic AI for cardiology", result);
        verify(repository, never()).findByCapabilityIdAndDepartmentIdAndVersion(anyString(), any(), anyInt());
    }

    @Test
    void warmupWithDefaultVersionZeroShouldNotCreateVersionKey() {
        PromptTemplate defaultVer = new PromptTemplate(3L, "triage", null, "Default", 0, ACTIVE);
        when(repository.findByStatus(ACTIVE)).thenReturn(List.of(defaultVer));
        manager.warmup();
        // base key should be cached
        assertEquals("Default", manager.render("triage", null, Map.of(), null));
        verify(repository, never()).findByCapabilityIdAndDepartmentIdAndStatus(any(), any(), any());
    }

    // --- T54: resolveExactVersion cache-first ---

    @Test
    void exactVersionShouldHitCacheOnSecondCall() {
        PromptTemplate versioned = new PromptTemplate(3L, "diag", "dept1", "Version 2 template", 2, ACTIVE);
        when(repository.findByCapabilityIdAndDepartmentIdAndVersion("diag", "dept1", 2))
                .thenReturn(Optional.of(versioned));
        assertEquals("Version 2 template", manager.render("diag", "dept1", Map.of(), 2));
        assertEquals("Version 2 template", manager.render("diag", "dept1", Map.of(), 2));
        verify(repository, times(1)).findByCapabilityIdAndDepartmentIdAndVersion(anyString(), any(), anyInt());
    }
}
