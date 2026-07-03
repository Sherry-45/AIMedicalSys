package com.aimedical.modules.ai.api.dto.base;

public interface Phase4ServiceFacade<RQ, RS> {
    RS execute(RQ request);
}
