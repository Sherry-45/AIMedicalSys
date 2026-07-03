import type { BusinessError, PageResponse } from '../types'
import { apiGet, apiPost, apiPut, apiDelete, apiPatch } from './client'

import type {
  UserCreateRequest,
  UserUpdateRequest,
  UserQueryRequest,
  UserPasswordResetRequest,
  UserResponse,
  RoleCreateRequest,
  RoleUpdateRequest,
  RoleQueryRequest,
  RoleResponse,
  PostResponse,
} from '../types/admin'

/**
 * 管理员端 API：用户管理。
 *
 * <p>对应后端 /api/admin/users/* 系列接口。所有方法返回 Promise<T | BusinessError>，
 * 调用方需用 isBusinessError() 判断结果。
 *
 * <p>注意：路径不含 /api 前缀，client 会自动添加。
 */
export const userManagementApi = {
  /** 创建用户。POST /api/admin/users */
  create: (request: UserCreateRequest): Promise<UserResponse | BusinessError> => {
    return apiPost<UserResponse>('/admin/users', request)
  },

  /** 更新用户。PUT /api/admin/users/{id} */
  update: (id: number, request: UserUpdateRequest): Promise<UserResponse | BusinessError> => {
    return apiPut<UserResponse>(`/admin/users/${id}`, request)
  },

  /** 删除用户（软删除）。DELETE /api/admin/users/{id} */
  delete: (id: number): Promise<void | BusinessError> => {
    return apiDelete<void>(`/admin/users/${id}`)
  },

  /** 查询用户详情。GET /api/admin/users/{id} */
  get: (id: number): Promise<UserResponse | BusinessError> => {
    return apiGet<UserResponse>(`/admin/users/${id}`)
  },

  /** 分页查询用户。GET /api/admin/users */
  query: (
    params: UserQueryRequest,
  ): Promise<PageResponse<UserResponse> | BusinessError> => {
    return apiGet<PageResponse<UserResponse>>('/admin/users', { params })
  },

  /** 切换用户启用/停用状态。PUT /api/admin/users/{id}/toggle-enabled */
  toggleEnabled: (id: number): Promise<UserResponse | BusinessError> => {
    return apiPut<UserResponse>(`/admin/users/${id}/toggle-enabled`)
  },

  /** 重置用户密码。PUT /api/admin/users/{id}/password */
  resetPassword: (
    id: number,
    request: UserPasswordResetRequest,
  ): Promise<void | BusinessError> => {
    return apiPut<void>(`/admin/users/${id}/password`, request)
  },
}

/**
 * 管理员端 API：角色管理。
 *
 * <p>对应后端 /api/admin/roles/* 系列接口。
 */
export const roleManagementApi = {
  /** 创建角色。POST /api/admin/roles */
  create: (request: RoleCreateRequest): Promise<RoleResponse | BusinessError> => {
    return apiPost<RoleResponse>('/admin/roles', request)
  },

  /** 更新角色。PATCH /api/admin/roles/{id} */
  update: (id: number, request: RoleUpdateRequest): Promise<RoleResponse | BusinessError> => {
    return apiPatch<RoleResponse>(`/admin/roles/${id}`, request)
  },

  /** 删除角色。DELETE /api/admin/roles/{id} */
  delete: (id: number): Promise<void | BusinessError> => {
    return apiDelete<void>(`/admin/roles/${id}`)
  },

  /** 查询角色详情。GET /api/admin/roles/{id} */
  get: (id: number): Promise<RoleResponse | BusinessError> => {
    return apiGet<RoleResponse>(`/admin/roles/${id}`)
  },

  /** 分页查询角色。GET /api/admin/roles */
  query: (
    params: RoleQueryRequest,
  ): Promise<PageResponse<RoleResponse> | BusinessError> => {
    return apiGet<PageResponse<RoleResponse>>('/admin/roles', { params })
  },

  /** 查询所有启用角色（下拉框使用）。GET /api/admin/roles/all-enabled */
  listAllEnabled: (): Promise<RoleResponse[] | BusinessError> => {
    return apiGet<RoleResponse[]>('/admin/roles/all-enabled')
  },
}

/**
 * 管理员端 API：岗位管理。
 *
 * <p>当前仅暴露查询所有启用岗位接口，用于用户管理表单中的岗位下拉选择。
 */
export const postManagementApi = {
  /** 查询所有启用岗位。GET /api/admin/posts/all-enabled */
  listAllEnabled: (): Promise<PostResponse[] | BusinessError> => {
    return apiGet<PostResponse[]>('/admin/posts/all-enabled')
  },
}
