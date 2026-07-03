package com.aimedical.modules.commonmodule.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.dto.request.RoleCreateRequest;
import com.aimedical.modules.commonmodule.dto.request.RoleQueryRequest;
import com.aimedical.modules.commonmodule.dto.request.RoleUpdateRequest;
import com.aimedical.modules.commonmodule.dto.response.RoleResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 角色管理服务（管理员端）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface RoleManagementService {

    Result<RoleResponse> create(RoleCreateRequest request);

    Result<RoleResponse> update(Long id, RoleUpdateRequest request);

    Result<Void> delete(Long id);

    Result<RoleResponse> getById(Long id);

    Result<Page<RoleResponse>> query(RoleQueryRequest request);

    /**
     * 查询全部启用角色（供下拉框使用）。
     */
    Result<List<RoleResponse>> listAllEnabled();
}
