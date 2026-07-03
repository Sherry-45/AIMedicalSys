package com.aimedical.modules.ai.impl.metrics;

import com.aimedical.common.config.JpaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaConfig.class)
class AiCallLogStatsRepositoryTest {

    @SpringBootApplication
    static class TestConfig {}

    @Autowired
    private TestEntityManager em;

    @Autowired
    private AiCallLogStatsRepository repository;

    @Test
    void shouldExtendJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(AiCallLogStatsRepository.class));
    }

    @Test
    void shouldHaveFindByCapabilityIdAndStatMonthBetweenMethod() throws Exception {
        Method method = AiCallLogStatsRepository.class.getMethod(
            "findByCapabilityIdAndStatMonthBetween", String.class, String.class, String.class);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void shouldReturnEmptyListWhenNoMatch() {
        List<AiCallLogStats> result = repository.findByCapabilityIdAndStatMonthBetween("NONEXIST", "2026-01", "2026-12");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindByCapabilityIdAndStatMonthBetween() {
        AiCallLogStats stats = new AiCallLogStats();
        stats.setCapabilityId("TRIAGE");
        stats.setStatMonth("2026-07");
        stats.setTotalCalls(100L);
        em.persistAndFlush(stats);

        List<AiCallLogStats> result = repository.findByCapabilityIdAndStatMonthBetween("TRIAGE", "2026-01", "2026-12");
        assertEquals(1, result.size());
        assertEquals("TRIAGE", result.get(0).getCapabilityId());
        assertEquals("2026-07", result.get(0).getStatMonth());
    }

    @Test
    void shouldFilterByMonthRange() {
        AiCallLogStats stats1 = new AiCallLogStats();
        stats1.setCapabilityId("TRIAGE");
        stats1.setStatMonth("2026-06");
        em.persistAndFlush(stats1);

        AiCallLogStats stats2 = new AiCallLogStats();
        stats2.setCapabilityId("TRIAGE");
        stats2.setStatMonth("2026-08");
        em.persistAndFlush(stats2);

        List<AiCallLogStats> result = repository.findByCapabilityIdAndStatMonthBetween("TRIAGE", "2026-07", "2026-12");
        assertEquals(1, result.size());
        assertEquals("2026-08", result.get(0).getStatMonth());
    }
}
