package com.aimedical.modules.ai.impl.metrics;

import com.aimedical.common.config.JpaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaConfig.class)
class AiCallLogRepositoryTest {

    @SpringBootApplication
    static class TestConfig {}

    @Autowired
    private TestEntityManager em;

    @Autowired
    private AiCallLogRepository repository;

    @Test
    void shouldExtendJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(AiCallLogRepository.class));
    }

    @Test
    void shouldHaveCountByCallTimeBeforeMethod() throws Exception {
        Method method = AiCallLogRepository.class.getMethod("countByCallTimeBefore", LocalDateTime.class);
        assertEquals(long.class, method.getReturnType());
    }

    @Test
    void shouldCountByCallTimeBeforeReturnZeroWhenNoRecords() {
        long count = repository.countByCallTimeBefore(LocalDateTime.now());
        assertEquals(0, count);
    }

    @Test
    void shouldCountByCallTimeBeforeReturnCorrectCount() {
        AiCallLogEntity entity = new AiCallLogEntity();
        entity.setCallTime(LocalDateTime.now().minusDays(1));
        em.persistAndFlush(entity);

        long countBefore = repository.countByCallTimeBefore(LocalDateTime.now());
        assertEquals(1, countBefore);

        long countAfter = repository.countByCallTimeBefore(LocalDateTime.now().minusDays(2));
        assertEquals(0, countAfter);
    }
}
