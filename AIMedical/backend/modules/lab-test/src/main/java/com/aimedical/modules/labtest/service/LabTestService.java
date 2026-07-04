package com.aimedical.modules.labtest.service;

import com.aimedical.common.result.PageResponse;
import com.aimedical.modules.labtest.dto.LabTestCompleteRequest;
import com.aimedical.modules.labtest.dto.LabTestCreateRequest;
import com.aimedical.modules.labtest.dto.LabTestDTO;
import com.aimedical.modules.labtest.dto.LabTestExecutionOrderDTO;
import com.aimedical.modules.labtest.dto.LabTestQueryRequest;
import com.aimedical.modules.labtest.dto.LabTestTrendDTO;

/**
 * 检验域服务接口。
 */
public interface LabTestService {

    LabTestDTO createLabTest(LabTestCreateRequest request);

    LabTestDTO collectSample(Long id);

    LabTestDTO startLabTest(Long id);

    LabTestDTO completeLabTest(Long id, LabTestCompleteRequest request);

    LabTestDTO cancelLabTest(Long id);

    LabTestDTO getLabTest(Long id);

    PageResponse<LabTestDTO> getLabTests(LabTestQueryRequest query);

    LabTestDTO generateAiReport(Long id);

    LabTestTrendDTO getTrend(Long patientId, String itemName);

    /**
     * AI 执行顺序推荐（检验域）。对齐需求 §3.4.11。
     * AI 不可用时降级到 FIFO 手动排序（degraded=true）。
     */
    LabTestExecutionOrderDTO recommendExecutionOrder(Long patientId);
}
