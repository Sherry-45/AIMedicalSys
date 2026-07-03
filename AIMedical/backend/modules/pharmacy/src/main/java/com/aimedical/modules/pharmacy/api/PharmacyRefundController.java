package com.aimedical.modules.pharmacy.api;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.auth.CurrentUser;
import com.aimedical.modules.pharmacy.dto.PharmacyRefundCreateRequest;
import com.aimedical.modules.pharmacy.dto.PharmacyRefundResponse;
import com.aimedical.modules.pharmacy.service.PharmacyRefundService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 退药控制器。
 * <p>
 * 提供退药申请创建、审批通过、驳回、查询等接口。
 * 全部需要 ADMIN 角色（药房工作人员使用管理员角色）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/pharmacy/refund")
@PreAuthorize("hasRole('ADMIN')")
public class PharmacyRefundController {

    private final PharmacyRefundService refundService;
    private final CurrentUser currentUser;

    public PharmacyRefundController(PharmacyRefundService refundService, CurrentUser currentUser) {
        this.refundService = refundService;
        this.currentUser = currentUser;
    }

    /**
     * 创建退药申请（待处理状态）。
     */
    @PostMapping
    public Result<PharmacyRefundResponse> create(@Valid @RequestBody PharmacyRefundCreateRequest request) {
        Long pharmacistId = currentUser.getUserId();
        String pharmacistName = currentUser.getUsername();
        if (pharmacistId == null) {
            return Result.fail(GlobalErrorCode.UNAUTHORIZED, "无法获取当前登录药师ID");
        }
        return refundService.create(request, pharmacistId, pharmacistName);
    }

    /**
     * 审批通过退药（回补库存，发药记录变更为 REFUNDED）。
     */
    @PostMapping("/{id}/approve")
    public Result<PharmacyRefundResponse> approve(@PathVariable Long id) {
        return refundService.approve(id);
    }

    /**
     * 驳回退药申请。
     */
    @PostMapping("/{id}/reject")
    public Result<PharmacyRefundResponse> reject(@PathVariable Long id, @RequestParam String reason) {
        return refundService.reject(id, reason);
    }

    /**
     * 查询退药记录详情。
     */
    @GetMapping("/{id}")
    public Result<PharmacyRefundResponse> getById(@PathVariable Long id) {
        return refundService.getById(id);
    }
}
