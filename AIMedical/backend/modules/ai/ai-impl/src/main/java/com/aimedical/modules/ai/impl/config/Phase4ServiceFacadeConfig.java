package com.aimedical.modules.ai.impl.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aimedical.modules.ai.api.dto.base.Phase4ServiceFacade;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisResponse;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisRequest;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisResponse;
import com.aimedical.modules.ai.api.dto.labtest.LabTestReportRequest;
import com.aimedical.modules.ai.api.dto.labtest.LabTestReportResponse;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportRequest;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportResponse;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderRequest;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderResponse;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendRequest;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendResponse;

@Configuration
@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
public class Phase4ServiceFacadeConfig {

    @Bean("diagnosisPhase4Service")
    public Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> diagnosisPhase4Service(
            @Qualifier("diagnosisService") ObjectProvider<Object> diagnosisServiceProvider) {
        return request -> invokePhase4Service(diagnosisServiceProvider.getIfAvailable(), request, DiagnosisResponse.class);
    }

    @Bean("imageAnalysisPhase4Service")
    public Phase4ServiceFacade<ImageAnalysisRequest, ImageAnalysisResponse> imageAnalysisPhase4Service(
            @Qualifier("imageAnalysisService") ObjectProvider<Object> imageAnalysisServiceProvider) {
        return request -> invokePhase4Service(imageAnalysisServiceProvider.getIfAvailable(), request, ImageAnalysisResponse.class);
    }

    @Bean("labTestPhase4Service")
    public Phase4ServiceFacade<LabTestReportRequest, LabTestReportResponse> labTestPhase4Service(
            @Qualifier("labTestService") ObjectProvider<Object> labTestServiceProvider) {
        return request -> invokePhase4Service(labTestServiceProvider.getIfAvailable(), request, LabTestReportResponse.class);
    }

    @Bean("inspectionPhase4Service")
    public Phase4ServiceFacade<InspectionReportRequest, InspectionReportResponse> inspectionPhase4Service(
            @Qualifier("inspectionService") ObjectProvider<Object> inspectionServiceProvider) {
        return request -> invokePhase4Service(inspectionServiceProvider.getIfAvailable(), request, InspectionReportResponse.class);
    }

    @Bean("executionOrderPhase4Service")
    public Phase4ServiceFacade<ExecutionOrderRequest, ExecutionOrderResponse> executionOrderPhase4Service(
            @Qualifier("executionOrderService") ObjectProvider<Object> executionOrderServiceProvider) {
        return request -> invokePhase4Service(executionOrderServiceProvider.getIfAvailable(), request, ExecutionOrderResponse.class);
    }

    @Bean("examinationPhase4Service")
    public Phase4ServiceFacade<ExaminationRecommendRequest, ExaminationRecommendResponse> examinationPhase4Service(
            @Qualifier("examinationService") ObjectProvider<Object> examinationServiceProvider) {
        return request -> invokePhase4Service(examinationServiceProvider.getIfAvailable(), request, ExaminationRecommendResponse.class);
    }

    @SuppressWarnings("unchecked")
    private <RQ, RS> RS invokePhase4Service(Object service, RQ request, Class<RS> responseType) {
        if (service == null) {
            throw new IllegalStateException("Phase4Service unavailable");
        }
        try {
            Class<?> requestClass = request.getClass();
            return (RS) service.getClass()
                .getMethod("execute", requestClass)
                .invoke(service, request);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
