package com.aimedical.modules.ai.api.dto.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class CallContextTest {

    private static ObjectMapper createSnakeCaseMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    @Test
    void shouldConstructWithAllFields() {
        CallContext ctx = new CallContext("dept1", "doctor", "u001",
                "visit1", "p001", "sess1", "input", "output", 3);
        assertEquals("dept1", ctx.getDepartmentId());
        assertEquals("doctor", ctx.getCallerRole());
        assertEquals("u001", ctx.getCallerId());
        assertEquals("visit1", ctx.getVisitId());
        assertEquals("p001", ctx.getPatientId());
        assertEquals("sess1", ctx.getSessionId());
        assertEquals("input", ctx.getInputSummary());
        assertEquals("output", ctx.getOutputSummary());
        assertEquals(3, ctx.getPromptVersion());
    }

    @Test
    void shouldBeImmutable() {
        CallContext ctx = new CallContext("d", "r", "c", "v", "p", "s", "i", "o", 1);
        for (java.lang.reflect.Field field : CallContext.class.getDeclaredFields()) {
            assertTrue(Modifier.isFinal(field.getModifiers()),
                    "Field " + field.getName() + " should be final");
        }
    }

    @Test
    void shouldCreateCopyWithOutputSummary() {
        CallContext original = new CallContext("dept1", "doctor", "u001",
                "visit1", "p001", "sess1", "input", "originalOutput", 2);
        CallContext copy = original.withOutputSummary("newOutput");

        assertEquals("newOutput", copy.getOutputSummary());
        assertEquals("originalOutput", original.getOutputSummary());

        assertEquals(original.getDepartmentId(), copy.getDepartmentId());
        assertEquals(original.getCallerRole(), copy.getCallerRole());
        assertEquals(original.getCallerId(), copy.getCallerId());
        assertEquals(original.getVisitId(), copy.getVisitId());
        assertEquals(original.getPatientId(), copy.getPatientId());
        assertEquals(original.getSessionId(), copy.getSessionId());
        assertEquals(original.getInputSummary(), copy.getInputSummary());
        assertEquals(original.getPromptVersion(), copy.getPromptVersion());
    }

    @Test
    void shouldCreateCopyWithPromptVersion() {
        CallContext original = new CallContext("dept1", "doctor", "u001",
                "visit1", "p001", "sess1", "input", "output", 2);
        CallContext copy = original.withPromptVersion(5);

        assertEquals(5, copy.getPromptVersion());
        assertEquals(2, original.getPromptVersion());

        assertEquals(original.getDepartmentId(), copy.getDepartmentId());
        assertEquals(original.getCallerRole(), copy.getCallerRole());
        assertEquals(original.getCallerId(), copy.getCallerId());
        assertEquals(original.getVisitId(), copy.getVisitId());
        assertEquals(original.getPatientId(), copy.getPatientId());
        assertEquals(original.getSessionId(), copy.getSessionId());
        assertEquals(original.getInputSummary(), copy.getInputSummary());
        assertEquals(original.getOutputSummary(), copy.getOutputSummary());
    }

    @Test
    void shouldSupportNullOutputSummaryAndPromptVersion() {
        CallContext ctx = new CallContext("dept1", "doctor", "u001",
                "visit1", "p001", "sess1", "input", null, null);
        assertNull(ctx.getOutputSummary());
        assertNull(ctx.getPromptVersion());

        CallContext copyOutput = ctx.withOutputSummary("newOutput");
        assertEquals("newOutput", copyOutput.getOutputSummary());
        assertNull(copyOutput.getPromptVersion());

        CallContext copyVersion = ctx.withPromptVersion(3);
        assertEquals(3, copyVersion.getPromptVersion());
        assertNull(copyVersion.getOutputSummary());
    }

    @Test
    void shouldSupportJacksonSerialization() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();

        CallContext original = new CallContext("dept1", "doctor", "u001",
                "visit1", "p001", "sess1", "input", "output", 2);
        String json = mapper.writeValueAsString(original);
        CallContext deserialized = mapper.readValue(json, CallContext.class);
        assertEquals(original, deserialized);
    }
}
