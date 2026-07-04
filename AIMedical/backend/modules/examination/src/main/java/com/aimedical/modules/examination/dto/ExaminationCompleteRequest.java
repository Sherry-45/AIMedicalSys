package com.aimedical.modules.examination.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 检查完成请求 DTO。
 * 将印象、结论与明细列表统一通过请求体传递，避免 URL 参数编码问题。
 */
@Data
public class ExaminationCompleteRequest {

    private String impression;

    @Size(max = 1000, message = "检查结论长度不能超过 1000")
    private String conclusion;

    @NotEmpty
    @Valid
    private List<ExaminationItemRequest> items;
}
