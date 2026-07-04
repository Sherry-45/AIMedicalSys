package com.aimedical.modules.labtest.api;

import com.aimedical.common.result.PageResponse;
import com.aimedical.common.result.Result;
import com.aimedical.modules.labtest.dto.LabTestCompleteRequest;
import com.aimedical.modules.labtest.dto.LabTestCreateRequest;
import com.aimedical.modules.labtest.dto.LabTestDTO;
import com.aimedical.modules.labtest.dto.LabTestExecutionOrderDTO;
import com.aimedical.modules.labtest.dto.LabTestQueryRequest;
import com.aimedical.modules.labtest.dto.LabTestTrendDTO;
import com.aimedical.modules.labtest.service.LabTestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 检验域 REST 控制器。
 */
@RestController
@RequestMapping("/api/lab-tests")
public class LabTestController {

    private final LabTestService labTestService;

    public LabTestController(LabTestService labTestService) {
        this.labTestService = labTestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<LabTestDTO> createLabTest(@Valid @RequestBody LabTestCreateRequest request) {
        return Result.success(labTestService.createLabTest(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<LabTestDTO> getLabTest(@PathVariable Long id) {
        return Result.success(labTestService.getLabTest(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<PageResponse<LabTestDTO>> getLabTests(@Valid LabTestQueryRequest query) {
        return Result.success(labTestService.getLabTests(query));
    }

    @PutMapping("/{id}/collect")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<LabTestDTO> collectSample(@PathVariable Long id) {
        return Result.success(labTestService.collectSample(id));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<LabTestDTO> startLabTest(@PathVariable Long id) {
        return Result.success(labTestService.startLabTest(id));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<LabTestDTO> completeLabTest(@PathVariable Long id,
                                              @Valid @RequestBody LabTestCompleteRequest request) {
        return Result.success(labTestService.completeLabTest(id, request));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<LabTestDTO> cancelLabTest(@PathVariable Long id) {
        return Result.success(labTestService.cancelLabTest(id));
    }

    @PostMapping("/{id}/ai-report")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<LabTestDTO> generateAiReport(@PathVariable Long id) {
        return Result.success(labTestService.generateAiReport(id));
    }

    @GetMapping("/trend/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<LabTestTrendDTO> getTrend(@PathVariable Long patientId,
                                            @RequestParam @NotBlank String itemName) {
        return Result.success(labTestService.getTrend(patientId, itemName));
    }

    @GetMapping("/execution-order/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<LabTestExecutionOrderDTO> recommendExecutionOrder(@PathVariable Long patientId) {
        return Result.success(labTestService.recommendExecutionOrder(patientId));
    }
}
