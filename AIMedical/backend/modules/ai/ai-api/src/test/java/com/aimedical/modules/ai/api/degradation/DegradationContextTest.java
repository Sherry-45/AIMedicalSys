package com.aimedical.modules.ai.api.degradation;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DegradationContextTest {

    @Test
    void shouldBuildWithAllFields() {
        DegradationContext ctx = new DegradationContext.Builder()
                .serviceName("testService")
                .operationName("testOp")
                .invocationCount(10)
                .lastFailureTime(1000L)
                .elapsedTime(500L)
                .requestType("sync")
                .failureCount(2)
                .departmentId("dept1")
                .serializedTimestamp(2000L)
                .build();
        assertEquals("testService", ctx.getServiceName());
        assertEquals("testOp", ctx.getOperationName());
        assertEquals(10, ctx.getInvocationCount());
        assertEquals(1000L, ctx.getLastFailureTime());
        assertEquals(500L, ctx.getElapsedTime());
        assertEquals("sync", ctx.getRequestType());
        assertEquals(2, ctx.getFailureCount());
        assertEquals("dept1", ctx.getDepartmentId());
        assertEquals(2000L, ctx.getSerializedTimestamp());
    }

    @Test
    void shouldBuildWithPartialFields() {
        DegradationContext ctx = new DegradationContext.Builder()
                .serviceName("s")
                .invocationCount(5)
                .build();
        assertEquals("s", ctx.getServiceName());
        assertEquals(5, ctx.getInvocationCount());
        assertNull(ctx.getOperationName());
        assertEquals(0, ctx.getFailureCount());
    }

    @Test
    void builderShouldReturnNewInstanceEachCall() {
        DegradationContext.Builder builder = new DegradationContext.Builder()
                .serviceName("s");
        DegradationContext ctx1 = builder.build();
        DegradationContext ctx2 = builder.build();
        assertNotSame(ctx1, ctx2);
        assertEquals("s", ctx1.getServiceName());
        assertEquals("s", ctx2.getServiceName());
    }

    @Test
    void postDeserializationValidateShouldClearRequestTypeWhenAllFieldsExplicitlyZero() {
        DegradationContext ctx = new DegradationContext.Builder()
                .invocationCount(0)
                .failureCount(0)
                .elapsedTime(0L)
                .requestType("sync")
                .build();
        ctx.postDeserializationValidate();
        assertNull(ctx.getRequestType());
    }

    @Test
    void postDeserializationValidateShouldClearRequestTypeWhenAllDefault() {
        DegradationContext ctx = new DegradationContext.Builder()
                .requestType("sync")
                .build();
        assertNotNull(ctx.getRequestType());
        ctx.postDeserializationValidate();
        assertNull(ctx.getRequestType());
    }

    @Test
    void postDeserializationValidateShouldNotClearRequestTypeWhenInvocationCountSet() {
        DegradationContext ctx = new DegradationContext.Builder()
                .invocationCount(5)
                .requestType("sync")
                .build();
        ctx.postDeserializationValidate();
        assertEquals("sync", ctx.getRequestType());
    }

    @Test
    void postDeserializationValidateShouldNotClearRequestTypeWhenFailureCountSet() {
        DegradationContext ctx = new DegradationContext.Builder()
                .failureCount(1)
                .requestType("sync")
                .build();
        ctx.postDeserializationValidate();
        assertEquals("sync", ctx.getRequestType());
    }

    @Test
    void postDeserializationValidateShouldNotClearRequestTypeWhenElapsedTimeSet() {
        DegradationContext ctx = new DegradationContext.Builder()
                .elapsedTime(100L)
                .requestType("sync")
                .build();
        ctx.postDeserializationValidate();
        assertEquals("sync", ctx.getRequestType());
    }

    @Test
    void postDeserializationValidateIsIdempotent() {
        DegradationContext ctx = new DegradationContext.Builder()
                .requestType("sync")
                .build();
        ctx.postDeserializationValidate();
        ctx.postDeserializationValidate();
        assertNull(ctx.getRequestType());
    }

    @Test
    void isFreshShouldReturnTrueWhenWithinTtl() {
        DegradationContext ctx = new DegradationContext.Builder()
                .serializedTimestamp(System.currentTimeMillis())
                .build();
        assertTrue(ctx.isFresh());
    }

    @Test
    void isFreshShouldReturnFalseWhenExpired() {
        DegradationContext ctx = new DegradationContext.Builder()
                .serializedTimestamp(System.currentTimeMillis() - 120_000L)
                .build();
        assertFalse(ctx.isFresh());
    }

    @Test
    void isFreshShouldReturnFalseWhenTimestampIsZero() {
        DegradationContext ctx = new DegradationContext();
        assertFalse(ctx.isFresh());
    }

    @Test
    void isInitializedShouldReturnTrueWhenBothCountsSet() {
        DegradationContext ctx = new DegradationContext.Builder()
                .invocationCount(10)
                .failureCount(2)
                .build();
        assertTrue(ctx.isInitialized());
    }

    @Test
    void isInitializedShouldReturnFalseWhenAllDefaults() {
        DegradationContext ctx = new DegradationContext();
        assertFalse(ctx.isInitialized());
    }

    @Test
    void isInitializedShouldReturnTrueWhenElapsedTimeSet() {
        DegradationContext ctx = new DegradationContext.Builder()
                .elapsedTime(100L)
                .build();
        assertTrue(ctx.isInitialized());
    }

    @Test
    void isInitializedShouldReturnTrueWhenFailureCountSet() {
        DegradationContext ctx = new DegradationContext.Builder()
                .failureCount(2)
                .build();
        assertTrue(ctx.isInitialized());
    }

    @Test
    void isInitializedShouldReturnFalseForDefaultContext() {
        DegradationContext ctx = new DegradationContext();
        assertFalse(ctx.isInitialized());
    }

    @Test
    void shouldCreateBuilderViaStaticFactory() {
        DegradationContext ctx = DegradationContext.Builder.builder()
                .serviceName("svc")
                .invocationCount(3)
                .build();
        assertEquals("svc", ctx.getServiceName());
        assertEquals(3, ctx.getInvocationCount());
    }

    @Test
    void shouldImplementSerializable() {
        assertTrue(new DegradationContext() instanceof Serializable);
    }

    @Test
    void defaultConstructorShouldCreateZeroValueInstance() {
        DegradationContext ctx = new DegradationContext();
        assertNull(ctx.getServiceName());
        assertNull(ctx.getOperationName());
        assertEquals(0, ctx.getInvocationCount());
        assertEquals(0L, ctx.getLastFailureTime());
        assertEquals(0L, ctx.getElapsedTime());
        assertNull(ctx.getRequestType());
        assertEquals(0, ctx.getFailureCount());
        assertNull(ctx.getDepartmentId());
        assertEquals(0L, ctx.getSerializedTimestamp());
    }
}
