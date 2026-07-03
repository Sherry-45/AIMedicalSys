package com.aimedical.modules.commonmodule.controller;

import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.dto.request.RoleCreateRequest;
import com.aimedical.modules.commonmodule.dto.request.RoleQueryRequest;
import com.aimedical.modules.commonmodule.dto.request.RoleUpdateRequest;
import com.aimedical.modules.commonmodule.dto.response.RoleResponse;
import com.aimedical.modules.commonmodule.service.RoleManagementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理控制器（管理员端）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    public RoleManagementController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    @PostMapping
    public Result<RoleResponse> create(@Valid @RequestBody RoleCreateRequest request) {
        return roleManagementService.create(request);
    }

    @PatchMapping("/{id}")
    public Result<RoleResponse> update(@PathVariable Long id,
                                       @Valid @RequestBody RoleUpdateRequest request) {
        return roleManagementService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return roleManagementService.delete(id);
    }

    @GetMapping("/{id}")
    public Result<RoleResponse> get(@PathVariable Long id) {
        return roleManagementService.getById(id);
    }

    @GetMapping
    public Result<Page<RoleResponse>> query(RoleQueryRequest request) {
        return roleManagementService.query(request);
    }

    @GetMapping("/all-enabled")
    public Result<List<RoleResponse>> listAllEnabled() {
        return roleManagementService.listAllEnabled();
    }
}
