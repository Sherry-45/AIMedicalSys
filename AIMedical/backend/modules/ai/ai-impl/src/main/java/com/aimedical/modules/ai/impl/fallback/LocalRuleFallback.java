package com.aimedical.modules.ai.impl.fallback;

public interface LocalRuleFallback<T, R> {
    R fallback(T request);
}
