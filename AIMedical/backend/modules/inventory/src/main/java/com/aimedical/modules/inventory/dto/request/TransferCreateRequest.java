package com.aimedical.modules.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 调拨单创建请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class TransferCreateRequest {

    @NotBlank(message = "调拨类型不能为空")
    @Size(max = 32)
    private String transferType;

    @Size(max = 64)
    private String sourceDept;

    @Size(max = 64)
    private String targetDept;

    @Size(max = 500)
    private String remark;

    @Valid
    @NotEmpty(message = "调拨明细不能为空")
    private List<TransferItemRequest> items;
}
