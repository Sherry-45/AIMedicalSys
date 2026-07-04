package com.aimedical.modules.commonmodule.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.dto.request.UserCreateRequest;
import com.aimedical.modules.commonmodule.dto.request.UserPasswordResetRequest;
import com.aimedical.modules.commonmodule.dto.request.UserQueryRequest;
import com.aimedical.modules.commonmodule.dto.request.UserUpdateRequest;
import com.aimedical.modules.commonmodule.dto.response.UserResponse;
import org.springframework.data.domain.Page;

/**
 * 用户管理服务（管理员端）。
 *
 * <p>提供用户的 CRUD、启用/停用、重置密码、分配角色与岗位等能力。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface UserManagementService {

    Result<UserResponse> create(UserCreateRequest request);

    Result<UserResponse> update(Long id, UserUpdateRequest request);

    Result<Void> delete(Long id);

    Result<UserResponse> getById(Long id);

    Result<Page<UserResponse>> query(UserQueryRequest request);

    Result<UserResponse> toggleEnabled(Long id);

    Result<Void> resetPassword(Long id, UserPasswordResetRequest request);
}
