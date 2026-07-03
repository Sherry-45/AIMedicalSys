package com.aimedical.modules.ai.impl.orchestrator;

import java.util.concurrent.CompletableFuture;

import com.aimedical.modules.ai.api.AiResult;

public interface CapabilityExecutor<T, R> {

    CompletableFuture<AiResult<R>> execute(T request, String capabilityId);

    String getCapabilityId();

    Class<T> getInputType();

    Class<R> getOutputType();
}
