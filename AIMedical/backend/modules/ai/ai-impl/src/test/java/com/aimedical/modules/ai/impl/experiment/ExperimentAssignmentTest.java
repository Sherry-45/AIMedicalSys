package com.aimedical.modules.ai.impl.experiment;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentAssignmentTest {

    @Test
    void shouldConstructWithAllFields() {
        ExperimentAssignment ea = new ExperimentAssignment("exp1", "groupA", "model-x", 3);
        assertEquals("exp1", ea.getExperimentId());
        assertEquals("groupA", ea.getGroupId());
        assertEquals("model-x", ea.getTargetModelId());
        assertEquals(3, ea.getTargetPromptVersion());
    }

    @Test
    void shouldCreateDefaultAssignment() {
        ExperimentAssignment ea = ExperimentAssignment.createDefault();
        assertNull(ea.getExperimentId());
        assertEquals("default", ea.getGroupId());
        assertNull(ea.getTargetModelId());
        assertNull(ea.getTargetPromptVersion());
    }

    @Test
    void shouldCreateErrorFallbackAssignment() {
        ExperimentAssignment ea = ExperimentAssignment.createErrorFallback();
        assertNull(ea.getExperimentId());
        assertEquals("experiment-error", ea.getGroupId());
        assertNull(ea.getTargetModelId());
        assertNull(ea.getTargetPromptVersion());
    }

    @Test
    void shouldBeImmutable() {
        assertTrue(Modifier.isFinal(ExperimentAssignment.class.getModifiers()));
        for (java.lang.reflect.Field field : ExperimentAssignment.class.getDeclaredFields()) {
            assertTrue(Modifier.isFinal(field.getModifiers()));
        }
    }

    @Test
    void shouldSupportNullTargetFields() {
        ExperimentAssignment ea = new ExperimentAssignment("exp1", "groupB", null, null);
        assertEquals("exp1", ea.getExperimentId());
        assertEquals("groupB", ea.getGroupId());
        assertNull(ea.getTargetModelId());
        assertNull(ea.getTargetPromptVersion());
    }

    @Test
    void equalsAndHashCodeShouldNotBeImplemented() {
        ExperimentAssignment ea1 = new ExperimentAssignment("exp1", "groupA", null, null);
        ExperimentAssignment ea2 = new ExperimentAssignment("exp1", "groupA", null, null);
        assertFalse(ea1.equals(ea2));
    }
}
