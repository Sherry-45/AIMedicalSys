import type { BusinessError } from '../types'
import { apiGet, apiPost, apiPut, apiDelete } from './client'

import type {
  DeviceCreateRequest,
  DeviceMessageResponse,
  DeviceResponse,
  DeviceStatus,
  DeviceUpdateRequest,
  PageResponse,
} from '../types'

/**
 * 管理员端 API
 *
 * <p>对应后端 /api/devices/* 系列接口。所有方法返回 Promise<T | BusinessError>，
 * 调用方需用 isBusinessError() 判断结果。
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
