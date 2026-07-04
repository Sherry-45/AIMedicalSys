package com.aimedical.modules.commonmodule.controller;

import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.dto.request.UserCreateRequest;
import com.aimedical.modules.commonmodule.dto.request.UserPasswordResetRequest;
import com.aimedical.modules.commonmodule.dto.request.UserQueryRequest;
import com.aimedical.modules.commonmodule.dto.request.UserUpdateRequest;
import com.aimedical.modules.commonmodule.dto.response.UserResponse;
import com.aimedical.modules.commonmodule.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制器（管理员端）。
 *
 * <p>提供用户的 CRUD、启用/停用、重置密码等能力。
 * 安全策略：所有端点仅 ADMIN 角色可访问。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping
    public Result<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return userManagementService.create(request);
    }

    @PutMapping("/{id}")
    public Result<UserResponse> update(@PathVariable Long id,
                                       @Valid @RequestBody UserUpdateRequest request) {
        return userManagementService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return userManagementService.delete(id);
    }

    @GetMapping("/{id}")
    public Result<UserResponse> get(@PathVariable Long id) {
        return userManagementService.getById(id);
    }

    @GetMapping
    public Result<Page<UserResponse>> query(UserQueryRequest request) {
        return userManagementService.query(request);
    }

    @PutMapping("/{id}/toggle-enabled")
    public Result<UserResponse> toggleEnabled(@PathVariable Long id) {
        return userManagementService.toggleEnabled(id);
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @Valid @RequestBody UserPasswordResetRequest request) {
        return userManagementService.resetPassword(id, request);
    }
}
