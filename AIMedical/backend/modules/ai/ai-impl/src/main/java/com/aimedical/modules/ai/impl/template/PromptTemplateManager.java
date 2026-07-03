package com.aimedical.modules.ai.impl.template;

import java.util.Map;

public interface PromptTemplateManager {
    String render(String capabilityId, String departmentId, Map<String, Object> variables, Integer promptVersion);
    String getFallbackPrompt(String capabilityId);
}
