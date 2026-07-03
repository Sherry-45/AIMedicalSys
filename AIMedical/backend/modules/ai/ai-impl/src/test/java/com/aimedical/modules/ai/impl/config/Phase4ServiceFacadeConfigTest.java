package com.aimedical.modules.ai.impl.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import com.aimedical.modules.ai.api.dto.base.Phase4ServiceFacade;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisResponse;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisRequest;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisResponse;

@SuppressWarnings("unchecked")
class Phase4ServiceFacadeConfigTest {

    private Phase4ServiceFacadeConfig config;

    @BeforeEach
    void setUp() {
        config = new Phase4ServiceFacadeConfig();
    }

    private ObjectProvider<Object> providerOf(Object value) {
        ObjectProvider<Object> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    @Test
    void diagnosisPhase4ServiceShouldInvokeExecuteOnService() {
        DiagnosisResponse expected = new DiagnosisResponse();
        Object service = new Object() {
            @SuppressWarnings("unused")
            public DiagnosisResponse execute(DiagnosisRequest req) {
                return expected;
            }
        };
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade =
            config.diagnosisPhase4Service(providerOf(service));
        DiagnosisResponse result = facade.execute(new DiagnosisRequest());
        assertSame(expected, result);
    }

    @Test
    void imageAnalysisPhase4ServiceShouldInvokeExecuteOnService() {
        ImageAnalysisResponse expected = new ImageAnalysisResponse();
        Object service = new Object() {
            @SuppressWarnings("unused")
            public ImageAnalysisResponse execute(ImageAnalysisRequest req) {
                return expected;
            }
        };
        Phase4ServiceFacade<ImageAnalysisRequest, ImageAnalysisResponse> facade =
            config.imageAnalysisPhase4Service(providerOf(service));
        ImageAnalysisResponse result = facade.execute(new ImageAnalysisRequest());
        assertSame(expected, result);
    }

    @Test
    void executeShouldThrowIllegalStateExceptionWhenServiceIsNull() {
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade =
            config.diagnosisPhase4Service(providerOf(null));
        assertThrows(IllegalStateException.class, () -> facade.execute(new DiagnosisRequest()));
    }

    @Test
    void executeShouldUnwrapRuntimeExceptionFromInvocationTargetException() {
        Object service = new Object() {
            @SuppressWarnings("unused")
            public DiagnosisResponse execute(DiagnosisRequest req) {
                throw new IllegalArgumentException("业务参数错误");
            }
        };
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade =
            config.diagnosisPhase4Service(providerOf(service));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> facade.execute(new DiagnosisRequest()));
        assertEquals("业务参数错误", ex.getMessage());
    }

    @Test
    void executeShouldWrapNonRuntimeExceptionInRuntimeException() {
        Object service = new Object() {
            @SuppressWarnings("unused")
            public DiagnosisResponse execute(DiagnosisRequest req) throws Exception {
                throw new Exception("checked exception");
            }
        };
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade =
            config.diagnosisPhase4Service(providerOf(service));
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> facade.execute(new DiagnosisRequest()));
        assertTrue(ex.getCause() instanceof Exception);
        assertEquals("checked exception", ex.getCause().getMessage());
    }

    @Test
    void executeShouldThrowWhenNoMatchingExecuteMethod() {
        Object service = new Object();
        Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> facade =
            config.diagnosisPhase4Service(providerOf(service));
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> facade.execute(new DiagnosisRequest()));
        assertTrue(ex.getCause() instanceof NoSuchMethodException);
    }
}
