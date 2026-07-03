package com.aimedical.modules.ai.impl.experiment;

import com.github.benmanes.caffeine.cache.Ticker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.aimedical.modules.ai.impl.experiment.ExperimentStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HashBucketExperimentManagerTest {

    @Mock
    private ExperimentRepository repository;

    private HashBucketExperimentManager manager;

    private Experiment activeExperiment;
    private ExperimentGroup groupA;
    private ExperimentGroup groupB;

    @BeforeEach
    void setUp() {
        manager = new HashBucketExperimentManager(repository);
        groupA = new ExperimentGroup(null, null, "groupA", 500, "model-a", 1);
        groupB = new ExperimentGroup(null, null, "groupB", 500, "model-b", 2);
        activeExperiment = new Experiment(1L, "cap1", List.of(groupA, groupB),
                ACTIVE, LocalDateTime.now(), null);
    }

    @Test
    void shouldReturnDefaultWhenNoExperiments() {
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE)).thenReturn(List.of());
        ExperimentAssignment result = manager.assign("cap1", "user1", "session1");
        assertEquals("default", result.getGroupId());
        assertNull(result.getExperimentId());
        assertNull(result.getTargetModelId());
        assertNull(result.getTargetPromptVersion());
    }

    @Test
    void shouldAssignToBucketByHash() {
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(activeExperiment));
        // single-char strings guarantee hashCode == char value
        // hash=200 falls into groupA (cumulative 500 > 200)
        String sessionForGroupA = String.valueOf((char) 200);
        ExperimentAssignment resultA = manager.assign("cap1", "user1", sessionForGroupA);
        assertEquals("groupA", resultA.getGroupId());
        assertEquals("1", resultA.getExperimentId());
        assertEquals("model-a", resultA.getTargetModelId());
        assertEquals(Integer.valueOf(1), resultA.getTargetPromptVersion());

        // hash=750 falls into groupB (cumulative 500 > 750 is false, cumulative 1000 > 750 is true)
        String sessionForGroupB = String.valueOf((char) 750);
        ExperimentAssignment resultB = manager.assign("cap1", "user1", sessionForGroupB);
        assertEquals("groupB", resultB.getGroupId());
        assertEquals("1", resultB.getExperimentId());
        assertEquals("model-b", resultB.getTargetModelId());
        assertEquals(Integer.valueOf(2), resultB.getTargetPromptVersion());
    }

    @Test
    void shouldMatchCumulativePercentage() {
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(activeExperiment));
        // hash=499 → groupA (cumulative 500 > 499)
        // hash=500 → groupB (cumulative 500 > 500 is false, cumulative 1000 > 500 is true)
        // single-char strings guarantee hashCode == char value
        String sessionFor499 = String.valueOf((char) 499);
        String sessionFor500 = String.valueOf((char) 500);

        ExperimentAssignment resultA = manager.assign("cap1", "user", sessionFor499);
        assertEquals("groupA", resultA.getGroupId());

        ExperimentAssignment resultB = manager.assign("cap1", "user", sessionFor500);
        assertEquals("groupB", resultB.getGroupId());
    }

    @Test
    void shouldReturnDefaultWhenHashExceedsAllPercentages() {
        ExperimentGroup partial = new ExperimentGroup(null, null, "partial", 300, null, null);
        Experiment exp = new Experiment(2L, "cap1", List.of(partial),
                ACTIVE, LocalDateTime.now(), null);
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(exp));
        // hash=500 → cumulative 300 is not > 500 → default
        ExperimentAssignment result = manager.assign("cap1", "user", String.valueOf((char) 500));
        assertEquals("default", result.getGroupId());
    }

    @Test
    void shouldHandleNegativeHashValue() {
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(activeExperiment));
        // floorMod guarantees [0, 1000) for any negative hashCode
        assertEquals(999, Math.floorMod(-1, 1000));
        assertEquals(0, Math.floorMod(-1000, 1000));
        int intMinFloorMod = Math.floorMod(Integer.MIN_VALUE, 1000);
        assertTrue(intMinFloorMod >= 0 && intMinFloorMod < 1000);

        // assign() with actual string producing negative hashCode
        // "zzzzzzzz" overflows the int-based hashCode computation
        String negativeHashInput = "zzzzzzzz";
        assertTrue(negativeHashInput.hashCode() < 0);
        ExperimentAssignment result = manager.assign("cap1", "user", negativeHashInput);
        assertNotNull(result);
        assertNotNull(result.getGroupId());
    }

    @Test
    void cacheShouldExpireAfterWrite() {
        ManualTicker ticker = new ManualTicker();
        HashBucketExperimentManager managerWithTicker =
                new HashBucketExperimentManager(repository, ticker);

        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(activeExperiment));

        // First call populates cache
        managerWithTicker.assign("cap1", "user1", "s1");
        verify(repository, times(1)).findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE);

        // Second call uses cache (not expired yet)
        managerWithTicker.assign("cap1", "user1", "s1");
        verify(repository, times(1)).findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE);

        // Advance time past expireAfterWrite (5 min)
        ticker.advance(5, TimeUnit.MINUTES);
        ticker.advance(1, TimeUnit.NANOSECONDS);

        // Cache expired, should call DB again
        managerWithTicker.assign("cap1", "user1", "s1");
        verify(repository, times(2)).findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE);
    }

    @Test
    void warmupShouldPrepopulateCache() {
        when(repository.findByStatus(ACTIVE)).thenReturn(List.of(activeExperiment));
        manager.warmup();
        ExperimentAssignment result = manager.assign("cap1", "user1", "s1");
        assertNotNull(result);
        assertTrue("groupA".equals(result.getGroupId()) || "groupB".equals(result.getGroupId()));
        verify(repository, never()).findByCapabilityIdAndStatusWithGroups(anyString(), any());
    }

    @Test
    void eventShouldInvalidateCacheEntry() {
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(activeExperiment));
        manager.assign("cap1", "user1", "s1");
        verify(repository, times(1)).findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE);

        // Fire event to invalidate cache for cap1
        manager.onExperimentChanged(new ExperimentChangedEvent(this, "cap1", 1L,
                ExperimentChangedEvent.ChangeType.UPDATED, Instant.now()));

        // Next call should hit DB again
        manager.assign("cap1", "user1", "s1");
        verify(repository, times(2)).findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE);
    }

    @Test
    void eventWithNullCapabilityIdShouldInvalidateAll() {
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(activeExperiment));
        manager.assign("cap1", "user1", "s1");
        verify(repository, times(1)).findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE);

        // Fire event with null capabilityId → invalidate all
        manager.onExperimentChanged(new ExperimentChangedEvent(this, null, null,
                ExperimentChangedEvent.ChangeType.UPDATED, Instant.now()));

        manager.assign("cap1", "user1", "s1");
        verify(repository, times(2)).findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE);
    }

    @Test
    void assignShouldNotThrowOnNullUserIdOrSessionId() {
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(activeExperiment));
        assertDoesNotThrow(() -> manager.assign("cap1", null, null));
        assertDoesNotThrow(() -> manager.assign("cap1", "user1", null));
        assertDoesNotThrow(() -> manager.assign("cap1", null, "session1"));
    }

    @Test
    void concurrentAssignShouldNotThrow() throws InterruptedException {
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(activeExperiment));
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);
        for (int i = 0; i < threadCount; i++) {
            final String uid = "user" + i;
            new Thread(() -> {
                try {
                    manager.assign("cap1", uid, "session");
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
    void dbExceptionShouldReturnDefault() {
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenThrow(new RuntimeException("DB connection failed"));
        ExperimentAssignment result = manager.assign("cap1", "user1", "session1");
        assertEquals("default", result.getGroupId());
    }

    @Test
    void sessionIdNullButUserIdNonNullShouldInvokeHash() {
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(activeExperiment));
        // When sessionId is null, userId should be used as hash input
        ExperimentAssignment result = manager.assign("cap1", "unique-user-id", null);
        assertNotNull(result.getGroupId());
        assertTrue("groupA".equals(result.getGroupId()) || "groupB".equals(result.getGroupId()));
    }

    @Test
    void shouldUseLatestExperimentWhenMultipleActive() {
        Experiment olderExp = new Experiment(1L, "cap1", List.of(groupA),
                ACTIVE, LocalDateTime.now().minusDays(1), null);
        Experiment newerExp = new Experiment(2L, "cap1", List.of(groupB),
                ACTIVE, LocalDateTime.now(), null);
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(olderExp, newerExp));

        String session = String.valueOf((char) 200);
        ExperimentAssignment result = manager.assign("cap1", "user1", session);
        // newerExp has groups=[groupB], hash=200 falls into groupB (500 > 200)
        assertEquals("groupB", result.getGroupId());
        assertEquals("2", result.getExperimentId());
        assertEquals("model-b", result.getTargetModelId());
        assertEquals(Integer.valueOf(2), result.getTargetPromptVersion());
    }

    @Test
    void loaderShouldSortByStartTimeDescAndMaxSelectsCorrectExperiment() {
        ExperimentGroup groupFromFirst = new ExperimentGroup(null, null, "firstGroup", 500, "model-first", 1);
        ExperimentGroup groupFromSecond = new ExperimentGroup(null, null, "secondGroup", 500, "model-second", 2);
        Experiment older = new Experiment(1L, "cap1", List.of(groupFromFirst),
                ACTIVE, LocalDateTime.now().minusDays(2), null);
        Experiment newer = new Experiment(2L, "cap1", List.of(groupFromSecond),
                ACTIVE, LocalDateTime.now(), null);
        // Repository returns unsorted (older first)
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(older, newer));

        // hash=200 → whichever experiment is selected must have cumulative 500 > 200
        // The correct selection is the one with max startTime = newer experiment
        ExperimentAssignment result = manager.assign("cap1", "user1", String.valueOf((char) 200));
        assertEquals("2", result.getExperimentId());
        assertEquals("secondGroup", result.getGroupId());
        assertEquals("model-second", result.getTargetModelId());
    }

    @Test
    void warmupExceptionShouldNotBlockStartup() {
        when(repository.findByStatus(ACTIVE)).thenThrow(new RuntimeException("DB error"));
        assertDoesNotThrow(() -> manager.warmup());
        // After failed warmup, cache is empty, assign() should query DB
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(activeExperiment));
        ExperimentAssignment result = manager.assign("cap1", "user1", String.valueOf((char) 200));
        assertEquals("groupA", result.getGroupId());
    }

    @Test
    void shouldReturnDefaultWhenExperimentHasNullGroups() {
        Experiment expWithNullGroups = new Experiment(1L, "cap1", null,
                ACTIVE, LocalDateTime.now(), null);
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(expWithNullGroups));
        ExperimentAssignment result = manager.assign("cap1", "user1", "s1");
        assertEquals("default", result.getGroupId());
    }

    @Test
    void shouldReturnDefaultWhenExperimentHasEmptyGroups() {
        Experiment expWithEmptyGroups = new Experiment(1L, "cap1", List.of(),
                ACTIVE, LocalDateTime.now(), null);
        when(repository.findByCapabilityIdAndStatusWithGroups("cap1", ACTIVE))
                .thenReturn(List.of(expWithEmptyGroups));
        ExperimentAssignment result = manager.assign("cap1", "user1", "s1");
        assertEquals("default", result.getGroupId());
    }

    private static class ManualTicker implements Ticker {
        private long nanos;

        @Override
        public long read() {
            return nanos;
        }

        void advance(long duration, TimeUnit unit) {
            nanos += unit.toNanos(duration);
        }
    }
}
