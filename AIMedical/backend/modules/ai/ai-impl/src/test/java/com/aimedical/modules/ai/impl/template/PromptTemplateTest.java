package com.aimedical.modules.ai.impl.template;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PromptTemplateTest {

    @Autowired
    private PromptTemplateRepository repository;

    @Test
    void shouldConstructAndGetProperties() {
        PromptTemplate pt = new PromptTemplate(null, "diag", "dept1", "template content {{key}}", 1, TemplateStatus.DRAFT);
        assertNull(pt.getId());
        assertEquals("diag", pt.getCapabilityId());
        assertEquals("dept1", pt.getDepartmentId());
        assertEquals("template content {{key}}", pt.getContent());
        assertEquals(1, pt.getVersion());
        assertEquals(TemplateStatus.DRAFT, pt.getStatus());
    }

    @Test
    void shouldDefaultStatusToDraft() {
        PromptTemplate pt = new PromptTemplate();
        assertEquals(TemplateStatus.DRAFT, pt.getStatus());
    }

    @Test
    void shouldSetPropertiesViaSetters() {
        PromptTemplate pt = new PromptTemplate();
        pt.setId(1L);
        pt.setCapabilityId("triage");
        pt.setDepartmentId(null);
        pt.setContent("You are a triage AI");
        pt.setVersion(2);
        pt.setStatus(TemplateStatus.ACTIVE);

        assertEquals(1L, pt.getId());
        assertEquals("triage", pt.getCapabilityId());
        assertNull(pt.getDepartmentId());
        assertEquals("You are a triage AI", pt.getContent());
        assertEquals(2, pt.getVersion());
        assertEquals(TemplateStatus.ACTIVE, pt.getStatus());
    }

    @Test
    void equalsAndHashCodeShouldBeBasedOnId() {
        PromptTemplate pt1 = new PromptTemplate(1L, "diag", null, "content", 1, TemplateStatus.DRAFT);
        PromptTemplate pt2 = new PromptTemplate(1L, "diag", "dept1", "other", 2, TemplateStatus.ACTIVE);
        assertEquals(pt1, pt2);
        assertEquals(pt1.hashCode(), pt2.hashCode());
    }

    @Test
    void equalsShouldReturnFalseForDifferentIds() {
        PromptTemplate pt1 = new PromptTemplate(1L, "diag", null, "content", 1, TemplateStatus.DRAFT);
        PromptTemplate pt2 = new PromptTemplate(2L, "diag", null, "content", 1, TemplateStatus.DRAFT);
        assertNotEquals(pt1, pt2);
    }

    @Test
    void uniqueConstraintShouldPreventDuplicateTriple() {
        repository.saveAndFlush(new PromptTemplate(null, "diag", "dept1", "content1", 1, TemplateStatus.DRAFT));
        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.saveAndFlush(new PromptTemplate(null, "diag", "dept1", "content2", 1, TemplateStatus.ACTIVE));
        });
    }

    @Test
    void nullableDepartmentIdShouldBeAllowed() {
        PromptTemplate pt = new PromptTemplate(null, "diag", null, "global content", 1, TemplateStatus.ACTIVE);
        PromptTemplate saved = repository.saveAndFlush(pt);
        assertNotNull(saved.getId());
        assertNull(saved.getDepartmentId());
    }

    @Test
    void shouldPersistAndRetrieveEnumAsString() {
        PromptTemplate pt = new PromptTemplate(null, "triage", "dept1", "triage content", 1, TemplateStatus.DEPRECATED);
        PromptTemplate saved = repository.saveAndFlush(pt);
        PromptTemplate found = repository.findById(saved.getId()).orElseThrow();
        assertEquals(TemplateStatus.DEPRECATED, found.getStatus());
    }
}
