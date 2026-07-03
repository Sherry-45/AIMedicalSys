package com.aimedical.modules.pharmacy.api;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.auth.CurrentUser;
import com.aimedical.modules.pharmacy.dto.DispensingCreateRequest;
import com.aimedical.modules.pharmacy.dto.DispensingQueryRequest;
import com.aimedical.modules.pharmacy.dto.DispensingResponse;
import com.aimedical.modules.pharmacy.service.DispensingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发药控制器。
 * <p>
 * 提供发药记录创建、执行发药、取消发药、查询等接口。
 * 全部需要 ADMIN 角色（药房工作人员使用管理员角色）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/pharmacy/dispensing")
@PreAuthorize("hasRole('ADMIN')")
public class DispensingController {

    private final DispensingService dispensingService;
    private final CurrentUser currentUser;

    public DispensingController(DispensingService dispensingService, CurrentUser currentUser) {
        this.dispensingService = dispensingService;
        this.currentUser = currentUser;
    }

    /**
     * 创建发药记录（待发药状态）。
     */
    @PostMapping
    public Result<DispensingResponse> create(@Valid @RequestBody DispensingCreateRequest request) {
        Long pharmacistId = currentUser.getUserId();
        String pharmacistName = currentUser.getUsername();
        if (pharmacistId == null) {
            return Result.fail(GlobalErrorCode.UNAUTHORIZED, "无法获取当前登录药师ID");
        }
        return dispensingService.create(request, pharmacistId, pharmacistName);
    }

    /**
     * 执行发药（PENDING -> DISPENSED，扣减库存）。
     */
    @PostMapping("/{id}/dispense")
    public Result<DispensingResponse> dispense(@PathVariable Long id) {
        Long pharmacistId = currentUser.getUserId();
        String pharmacistName = currentUser.getUsername();
        return dispensingService.dispense(id, pharmacistId, pharmacistName);
    }

    /**
     * 取消发药（仅 PENDING 状态可取消）。
     */
    @PostMapping("/{id}/cancel")
    public Result<DispensingResponse> cancel(@PathVariable Long id) {
        return dispensingService.cancel(id);
    }

    /**
     * 查询发药记录详情。
     */
    @GetMapping("/{id}")
    public Result<DispensingResponse> getById(@PathVariable Long id) {
        return dispensingService.getById(id);
    }

    /**
     * 分页查询发药记录。
     */
    @GetMapping
    public Result<com.aimedical.common.result.PageResponse<DispensingResponse>> query(@Valid @ModelAttribute DispensingQueryRequest request) {
        return dispensingService.query(request);
    }
}
