package com.aimedical.modules.ai.impl.router;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import com.aimedical.modules.ai.impl.experiment.ExperimentAssignment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@Service
public class DefaultModelRouter implements ModelRouter {

    private static final Logger log = LoggerFactory.getLogger(DefaultModelRouter.class);

    private final AiRouterProperties routerProperties;
    private final AtomicReference<Map<String, ModelRoute[]>> routeTableRef;

    public DefaultModelRouter(AiRouterProperties routerProperties) {
        this.routerProperties = routerProperties;
        this.routeTableRef = new AtomicReference<>(Collections.emptyMap());
    }

    @PostConstruct
    public void init() {
        refreshRouteTable();
    }

    @Scheduled(fixedDelay = 60000, scheduler = "scheduledTaskExecutor")
    public void refreshRouteTable() {
        Map<String, List<ModelRoute>> rawRoutes = routerProperties.toModelRouteMap();
        Map<String, ModelRoute[]> newTable = buildRouteTable(rawRoutes);
        routeTableRef.set(newTable);
        log.debug("路由表已刷新，包含 {} 项能力路由", newTable.size());
    }

    // @TODO Phase5: 后续任务引入 RouteConfigChangedEvent 事件驱动刷新
    // @EventListener(RouteConfigChangedEvent.class)
    // public void onRouteConfigChanged(RouteConfigChangedEvent event) {
    //     refreshRouteTable();
    // }

    @Override
    public ModelRoute route(String capabilityId, ExperimentAssignment assignment) {
        ModelRoute[] routes = routeTableRef.get().get(capabilityId);
        if (routes == null || routes.length == 0) {
            return null;
        }
        if (routes.length == 1) {
            return routes[0];
        }
        return selectWeighted(routes);
    }

    static ModelRoute selectWeighted(ModelRoute[] routes) {
        int totalWeight = 0;
        for (ModelRoute r : routes) {
            totalWeight += r.getWeight();
        }
        if (totalWeight <= 0) {
            return routes[ThreadLocalRandom.current().nextInt(routes.length)];
        }
        int target = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;
        for (ModelRoute r : routes) {
            cumulative += r.getWeight();
            if (target < cumulative) {
                return r;
            }
        }
        return routes[routes.length - 1];
    }

    private Map<String, ModelRoute[]> buildRouteTable(Map<String, List<ModelRoute>> rawRoutes) {
        if (rawRoutes == null || rawRoutes.isEmpty()) {
            return Collections.emptyMap();
        }
        java.util.HashMap<String, ModelRoute[]> table = new java.util.HashMap<>();
        for (Map.Entry<String, List<ModelRoute>> entry : rawRoutes.entrySet()) {
            List<ModelRoute> list = entry.getValue();
            if (list == null || list.isEmpty()) {
                continue;
            }
            table.put(entry.getKey(), list.toArray(new ModelRoute[0]));
        }
        return Collections.unmodifiableMap(table);
    }
}
