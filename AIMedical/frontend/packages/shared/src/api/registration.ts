import type { BusinessError, PageResponse } from '../types'
import { apiGet, apiPost } from './client'

import type {
  CancelRegistrationRequest,
  RegistrationResponse,
} from '../types/registration'

/**
 * 挂号管理 API（医生端）。
 *
 * <p>对应后端 RegistrationController (/api/registration) 系列接口。
 * 注意：路径以 /registration 开头（不是 /patient/registration），路径不含 /api 前缀
 * （由 axios 客户端 baseURL 补齐）。
 *
 * <p>所有方法返回 Promise<T | BusinessError>，调用方需用 isBusinessError() 判断结果。
 * 查询参数使用 camelCase（与 Spring MVC 请求参数绑定一致），响应体字段为 snake_case
 * （JacksonConfig 全局 SNAKE_CASE 约定）。
 */
export const registrationApi = {
  /** 按医生分页查询挂号列表。GET /api/registration/doctor/{doctorId} */
  listByDoctor: (
    doctorId: number,
    page: number,
    size: number,
  ): Promise<PageResponse<RegistrationResponse> | BusinessError> => {
    return apiGet<PageResponse<RegistrationResponse>>(`/registration/doctor/${doctorId}`, {
      params: { page, size },
    })
  },

  /** 获取挂号详情。GET /api/registration/{id} */
  getById: (id: number): Promise<RegistrationResponse | BusinessError> => {
    return apiGet<RegistrationResponse>(`/registration/${id}`)
  },

  /** 确认挂号（PENDING -> CONFIRMED）。POST /api/registration/{id}/confirm */
  confirm: (id: number): Promise<RegistrationResponse | BusinessError> => {
    return apiPost<RegistrationResponse>(`/registration/${id}/confirm`)
  },

  /** 完成挂号（CONFIRMED -> COMPLETED）。POST /api/registration/{id}/complete */
  complete: (id: number): Promise<RegistrationResponse | BusinessError> => {
    return apiPost<RegistrationResponse>(`/registration/${id}/complete`)
  },

  /** 取消挂号（PENDING/CONFIRMED -> CANCELLED）。POST /api/registration/{id}/cancel */
  cancel: (
    id: number,
    reason: string,
  ): Promise<RegistrationResponse | BusinessError> => {
    const request: CancelRegistrationRequest = { cancel_reason: reason }
    return apiPost<RegistrationResponse>(`/registration/${id}/cancel`, request)
  },

  /** 标记爽约（PENDING/CONFIRMED -> NO_SHOW）。POST /api/registration/{id}/noshow */
  markNoShow: (id: number): Promise<RegistrationResponse | BusinessError> => {
    return apiPost<RegistrationResponse>(`/registration/${id}/noshow`)
  },
}
