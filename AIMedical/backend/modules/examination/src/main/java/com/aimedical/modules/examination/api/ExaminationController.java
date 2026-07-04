package com.aimedical.modules.examination.api;

import com.aimedical.common.result.PageResponse;
import com.aimedical.common.result.Result;
import com.aimedical.modules.examination.dto.ExaminationCompleteRequest;
import com.aimedical.modules.examination.dto.ExaminationCreateRequest;
import com.aimedical.modules.examination.dto.ExaminationDTO;
import com.aimedical.modules.examination.dto.ExaminationQueryRequest;
import com.aimedical.modules.examination.dto.ExecutionOrderDTO;
import com.aimedical.modules.examination.service.ExaminationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/examinations")
@RequiredArgsConstructor
public class ExaminationController {

    private final ExaminationService examinationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<ExaminationDTO> createExamination(@Valid @RequestBody ExaminationCreateRequest request) {
        return Result.success(examinationService.createExamination(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<ExaminationDTO> getExamination(@PathVariable Long id) {
        return Result.success(examinationService.getExamination(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<PageResponse<ExaminationDTO>> getExaminations(@Valid ExaminationQueryRequest query) {
        return Result.success(examinationService.getExaminations(query));
    }

    @PutMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<ExaminationDTO> scheduleExamination(@PathVariable Long id,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                       LocalDateTime scheduledAt) {
        return Result.success(examinationService.scheduleExamination(id, scheduledAt));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<ExaminationDTO> startExamination(@PathVariable Long id) {
        return Result.success(examinationService.startExamination(id));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<ExaminationDTO> completeExamination(@PathVariable Long id,
                                                      @Valid @RequestBody ExaminationCompleteRequest request) {
        return Result.success(examinationService.completeExamination(id, request));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<ExaminationDTO> cancelExamination(@PathVariable Long id) {
        return Result.success(examinationService.cancelExamination(id));
    }

    @PostMapping("/{id}/ai-report")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<ExaminationDTO> generateAiReport(@PathVariable Long id) {
        return Result.success(examinationService.generateAiReport(id));
    }

    @PostMapping("/{id}/image-analysis")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<ExaminationDTO> analyzeImage(@PathVariable Long id) {
        return Result.success(examinationService.analyzeImage(id));
    }

    @GetMapping("/execution-order/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result<ExecutionOrderDTO> recommendExecutionOrder(@PathVariable Long patientId) {
        return Result.success(examinationService.recommendExecutionOrder(patientId));
    }
}
