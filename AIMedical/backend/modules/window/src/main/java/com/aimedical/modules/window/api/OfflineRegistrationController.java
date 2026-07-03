package com.aimedical.modules.window.api;

import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.auth.CurrentUser;
import com.aimedical.modules.window.dto.OfflineRegistrationCancelRequest;
import com.aimedical.modules.window.dto.OfflineRegistrationCreateRequest;
import com.aimedical.modules.window.dto.OfflineRegistrationQueryRequest;
import com.aimedical.modules.window.dto.OfflineRegistrationResponse;
import com.aimedical.modules.window.service.OfflineRegistrationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 线下窗口挂号控制器。
 *
 * <p>窗口工作人员（ADMIN 角色）进行现场挂号、取消与查询。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/window/registration")
@PreAuthorize("hasRole('ADMIN')")
public class OfflineRegistrationController {

    private final OfflineRegistrationService registrationService;
    private final CurrentUser currentUser;

    public OfflineRegistrationController(OfflineRegistrationService registrationService,
                                         CurrentUser currentUser) {
        this.registrationService = registrationService;
        this.currentUser = currentUser;
    }

    /**
     * 创建线下挂号（同步生成挂号费待支付缴费记录）。
     */
    @PostMapping
    public Result<OfflineRegistrationResponse> create(@Valid @RequestBody OfflineRegistrationCreateRequest request) {
        return registrationService.create(request, currentUser.getUserId(), currentUser.getUsername());
    }

    /**
     * 取消挂号。
     */
    @PostMapping("/{id}/cancel")
    public Result<OfflineRegistrationResponse> cancel(@PathVariable Long id,
                                                       @Valid @RequestBody OfflineRegistrationCancelRequest request) {
        return registrationService.cancel(id, request);
    }

    /**
     * 查询挂号详情。
     */
    @GetMapping("/{id}")
    public Result<OfflineRegistrationResponse> getById(@PathVariable Long id) {
        return registrationService.getById(id);
    }

    /**
     * 分页查询挂号记录（按条件组合）。
     */
    @GetMapping
    public Result<Page<OfflineRegistrationResponse>> query(OfflineRegistrationQueryRequest request) {
        return registrationService.query(request);
    }
}
