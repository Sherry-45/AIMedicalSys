package com.aimedical.modules.ai.impl.router;

import com.aimedical.modules.ai.impl.experiment.ExperimentAssignment;

public interface ModelRouter {
    ModelRoute route(String capabilityId, ExperimentAssignment assignment);
}
