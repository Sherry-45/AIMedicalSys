package com.aimedical.modules.ai.impl.template;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.aimedical.modules.ai.impl.template.TemplateStatus.ACTIVE;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@Service
public class DatabasePromptTemplateManager implements PromptTemplateManager {

    private static final Logger log = LoggerFactory.getLogger(DatabasePromptTemplateManager.class);

    private final PromptTemplateRepository repository;
    private final Environment environment;
    private final Cache<String, PromptTemplate> cache;

    public DatabasePromptTemplateManager(PromptTemplateRepository repository, Environment environment) {
        this.repository = repository;
        this.environment = environment;
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();
    }

    @PostConstruct
    public void warmup() {
        try {
            java.util.List<PromptTemplate> activeTemplates = repository.findByStatus(ACTIVE);
            for (PromptTemplate pt : activeTemplates) {
                String key = buildCacheKey(pt.getCapabilityId(), pt.getDepartmentId());
                cache.put(key, pt);
                if (pt.getVersion() > 0) {
                    String versionKey = buildCacheKey(pt.getCapabilityId(), pt.getDepartmentId(), pt.getVersion());
                    cache.put(versionKey, pt);
                }
            }
            log.info("warmup completed: {} active templates cached", activeTemplates.size());
        } catch (Exception e) {
            log.warn("warmup failed: {}", e.toString());
        }
    }

    @EventListener
    public void onTemplateChanged(TemplateChangedEvent event) {
        String capabilityId = event.getCapabilityId();
        String departmentId = event.getDepartmentId();
        if (departmentId != null) {
            cache.invalidate(buildCacheKey(capabilityId, departmentId));
        } else {
            cache.invalidateAll();
            warmup();
        }
    }

    @Override
    public String render(String capabilityId, String departmentId,
                          Map<String, Object> variables, Integer promptVersion) {
        try {
            PromptTemplate template = null;

            if (promptVersion != null) {
                template = resolveExactVersion(capabilityId, departmentId, promptVersion);
            }

            if (template == null) {
                template = resolveActiveTemplate(capabilityId, departmentId);
            }

            if (template == null) {
                return null;
            }

            return replacePlaceholders(template.getContent(), variables);
        } catch (Exception e) {
            log.warn("render failed for capabilityId={}: {}", capabilityId, e.toString());
            return null;
        }
    }

    private PromptTemplate resolveExactVersion(String capabilityId, String departmentId, Integer promptVersion) {
        String cacheKey = buildCacheKey(capabilityId, departmentId, promptVersion);
        PromptTemplate cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        Optional<PromptTemplate> opt = repository.findByCapabilityIdAndDepartmentIdAndVersion(
                capabilityId, departmentId, promptVersion);
        if (opt.isPresent()) {
            PromptTemplate pt = opt.get();
            if (pt.getStatus() == ACTIVE) {
                log.debug("exact version {} found and ACTIVE for capabilityId={}, departmentId={}",
                        promptVersion, capabilityId, departmentId);
                cache.put(cacheKey, pt);
                return pt;
            } else {
                log.warn("exact version {} found but status={} for capabilityId={}, fallback to ACTIVE",
                        promptVersion, pt.getStatus(), capabilityId);
            }
        } else if (departmentId != null) {
            Optional<PromptTemplate> globalOpt = repository.findByCapabilityIdAndDepartmentIdAndVersion(
                    capabilityId, null, promptVersion);
            if (globalOpt.isPresent()) {
                PromptTemplate pt = globalOpt.get();
                if (pt.getStatus() == ACTIVE) {
                    log.debug("exact version {} found in global template for capabilityId={}", promptVersion, capabilityId);
                    cache.put(cacheKey, pt);
                    return pt;
                } else {
                    log.warn("exact version {} found in global template but status={} for capabilityId={}, fallback to ACTIVE",
                            promptVersion, pt.getStatus(), capabilityId);
                }
            } else {
                log.warn("exact version {} not found for capabilityId={} (department or global), fallback to ACTIVE",
                        promptVersion, capabilityId);
            }
        } else {
            log.warn("exact version {} not found for capabilityId={}, fallback to ACTIVE", promptVersion, capabilityId);
        }
        return null;
    }

    private PromptTemplate resolveActiveTemplate(String capabilityId, String departmentId) {
        String key = buildCacheKey(capabilityId, departmentId);
        PromptTemplate cached = cache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }

        java.util.List<PromptTemplate> results = repository.findByCapabilityIdAndDepartmentIdAndStatus(
                capabilityId, departmentId, ACTIVE);
        if (!results.isEmpty()) {
            PromptTemplate pt = results.get(0);
            cache.put(key, pt);
            return pt;
        }

        if (departmentId != null) {
            java.util.List<PromptTemplate> globalResults = repository.findByCapabilityIdAndDepartmentIdAndStatus(
                    capabilityId, null, ACTIVE);
            if (!globalResults.isEmpty()) {
                PromptTemplate pt = globalResults.get(0);
                cache.put(key, pt);
                return pt;
            }
        }

        return null;
    }

    private String replacePlaceholders(String content, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return content;
        }
        String result = content;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            Object value = entry.getValue();
            if (value != null) {
                result = result.replace(placeholder, value.toString());
            } else {
                log.warn("variable '{}' is null, placeholder '{}' retained", entry.getKey(), placeholder);
            }
        }
        return result;
    }

    @Override
    public String getFallbackPrompt(String capabilityId) {
        String configured = environment.getProperty("ai.template.fallback." + capabilityId);
        if (configured != null && !configured.isEmpty()) {
            return configured;
        }
        return "You are a helpful medical AI assistant. Reply concisely.";
    }

    private static String buildCacheKey(String capabilityId, String departmentId) {
        return capabilityId + ":" + (departmentId != null ? departmentId : "");
    }

    private static String buildCacheKey(String capabilityId, String departmentId, Integer promptVersion) {
        String base = capabilityId + ":" + (departmentId != null ? departmentId : "");
        if (promptVersion != null) {
            return base + ":v" + promptVersion;
        }
        return base;
    }
}
