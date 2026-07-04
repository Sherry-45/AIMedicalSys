package com.aimedical.modules.labtest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 检验完成请求 DTO。
 * 将报告结论与明细列表统一通过请求体传递，并支持 List 元素的级联校验。
 */
@Data
public class LabTestCompleteRequest {

    @Size(max = 1000, message = "报告结论长度不能超过 1000")
    private String reportConclusion;

    @NotEmpty
    @Valid
    private List<LabTestItemRequest> items;
}
