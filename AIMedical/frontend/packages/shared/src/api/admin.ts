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

import type {
  DeviceCreateRequest,
  DeviceMessageResponse,
  DeviceResponse,
  DeviceStatus,
  DeviceUpdateRequest,
} from '../types'

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

/**
 * 管理员端 API：硬件接入（Device）。
 *
 * <p>对应后端 /api/devices/* 系列接口。
 */
export const adminApi = {
  // ---- 硬件接入 (Device) ----

  /** 注册设备。POST /api/devices */
  registerDevice: (
    request: DeviceCreateRequest,
  ): Promise<DeviceResponse | BusinessError> => {
    return apiPost<DeviceResponse>('/devices', request)
  },

  /** 获取设备详情。GET /api/devices/{id} */
  getDevice: (id: number): Promise<DeviceResponse | BusinessError> => {
    return apiGet<DeviceResponse>(`/devices/${id}`)
  },

  /** 分页查询设备列表。GET /api/devices */
  listDevices: (
    params?: { deviceType?: string; status?: string; page?: number; size?: number },
  ): Promise<PageResponse<DeviceResponse> | BusinessError> => {
    return apiGet<PageResponse<DeviceResponse>>('/devices', { params })
  },

  /** 更新设备信息。PUT /api/devices/{id} */
  updateDevice: (
    id: number,
    request: DeviceUpdateRequest,
  ): Promise<DeviceResponse | BusinessError> => {
    return apiPut<DeviceResponse>(`/devices/${id}`, request)
  },

  /** 更新设备状态。PUT /api/devices/{id}/status?status= */
  updateDeviceStatus: (
    id: number,
    status: DeviceStatus,
  ): Promise<DeviceResponse | BusinessError> => {
    return apiPut<DeviceResponse>(`/devices/${id}/status`, null, {
      params: { status },
    })
  },

  /** 删除设备。DELETE /api/devices/{id} */
  deleteDevice: (id: number): Promise<void | BusinessError> => {
    return apiDelete<void>(`/devices/${id}`)
  },

  /** 设备心跳。POST /api/devices/{id}/heartbeat */
  deviceHeartbeat: (id: number): Promise<void | BusinessError> => {
    return apiPost<void>(`/devices/${id}/heartbeat`)
  },

  /** 查询设备消息列表。GET /api/devices/{deviceId}/messages */
  listDeviceMessages: (
    deviceId: number,
    page?: number,
    size?: number,
  ): Promise<PageResponse<DeviceMessageResponse> | BusinessError> => {
    return apiGet<PageResponse<DeviceMessageResponse>>(
      `/devices/${deviceId}/messages`,
      { params: { page: page ?? 0, size: size ?? 20 } },
    )
  },
}
