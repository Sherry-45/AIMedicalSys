package com.aimedical.modules.ai.impl.experiment;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.aimedical.modules.ai.impl.experiment.ExperimentStatus.ACTIVE;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@Service
public class HashBucketExperimentManager implements ExperimentManager {

    private static final Logger log = LoggerFactory.getLogger(HashBucketExperimentManager.class);

    private static final int BUCKET_COUNT = 1000;

    private final ExperimentRepository repository;
    private final Cache<String, List<Experiment>> cache;

    @Autowired
    public HashBucketExperimentManager(ExperimentRepository repository) {
        this(repository, Ticker.systemTicker());
    }

    HashBucketExperimentManager(ExperimentRepository repository, Ticker ticker) {
        this.repository = repository;
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .ticker(ticker)
            .build();
    }

    @PostConstruct
    public void warmup() {
        try {
            List<Experiment> activeExperiments = repository.findByStatus(ACTIVE);
            java.util.Map<String, List<Experiment>> grouped = activeExperiments.stream()
                .collect(Collectors.groupingBy(Experiment::getCapabilityId));
            grouped.forEach((capabilityId, experiments) -> {
                List<Experiment> ordered = experiments.stream()
                    .sorted(Comparator.comparing(Experiment::getStartTime).reversed())
                    .collect(Collectors.toList());
                cache.put(capabilityId, ordered);
            });
            log.info("warmup completed: {} capability groups cached", grouped.size());
        } catch (Exception e) {
            log.warn("warmup failed: {}", e.toString());
        }
    }

    @EventListener
    public void onExperimentChanged(ExperimentChangedEvent event) {
        String capabilityId = event.getCapabilityId();
        if (capabilityId != null) {
            cache.invalidate(capabilityId);
        } else {
            cache.invalidateAll();
        }
    }

    @Override
    public ExperimentAssignment assign(String capabilityId, String userId, String sessionId) {
        try {
            String hashInput = sessionId != null ? sessionId : (userId != null ? userId : "");
            int hash = Math.floorMod(hashInput.hashCode(), BUCKET_COUNT);

            List<Experiment> experiments = cache.get(capabilityId, k ->
                repository.findByCapabilityIdAndStatusWithGroups(k, ACTIVE).stream()
                    .sorted(Comparator.comparing(Experiment::getStartTime).reversed())
                    .collect(Collectors.toList()));

            if (experiments == null || experiments.isEmpty()) {
                return ExperimentAssignment.createDefault();
            }

            Experiment effective = experiments.size() == 1
                ? experiments.get(0)
                : experiments.stream()
                    .max(Comparator.comparing(Experiment::getStartTime))
                    .orElseThrow();

            List<ExperimentGroup> groups = effective.getGroups();
            if (groups == null || groups.isEmpty()) {
                return ExperimentAssignment.createDefault();
            }

            int cumulative = 0;
            for (ExperimentGroup group : groups) {
                cumulative += group.getPercentage();
                if (cumulative > hash) {
                    return new ExperimentAssignment(
                        String.valueOf(effective.getId()),
                        group.getGroupId(),
                        group.getTargetModelId(),
                        group.getTargetPromptVersion()
                    );
                }
            }

            return ExperimentAssignment.createDefault();
        } catch (Exception e) {
            log.warn("assign failed for capabilityId={}: {}", capabilityId, e.toString());
            return ExperimentAssignment.createDefault();
        }
    }
}
