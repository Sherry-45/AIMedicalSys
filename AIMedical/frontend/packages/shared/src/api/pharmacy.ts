import type { BusinessError } from '../types'
import { apiGet, apiPost } from './client'

import type {
  DispensingCreateRequest,
  DispensingQueryRequest,
  DispensingResponse,
  PharmacyRefundCreateRequest,
  PharmacyRefundQueryRequest,
  PharmacyRefundResponse,
  PharmacyStockQueryRequest,
  PharmacyStockResponse,
} from '../types/pharmacy'

/**
 * 药房模块 API
 *
 * <p>对应后端 /api/pharmacy/* 系列接口。所有方法返回 Promise<T | BusinessError>，
 * 调用方需用 isBusinessError() 判断结果。
 *
 * <p>注意：路径不含 /api 前缀，client 会自动添加。
 */
export const pharmacyApi = {
  // ---- 发药（Dispensing） ----

  /** 创建发药单。POST /api/pharmacy/dispensing */
  createDispensing: (
    request: DispensingCreateRequest,
  ): Promise<DispensingResponse | BusinessError> => {
    return apiPost<DispensingResponse>('/pharmacy/dispensing', request)
  },

  /** 执行发药（待发药 -> 已发药）。POST /api/pharmacy/dispensing/{id}/dispense */
  dispense: (id: number): Promise<DispensingResponse | BusinessError> => {
    return apiPost<DispensingResponse>(`/pharmacy/dispensing/${id}/dispense`)
  },

  /** 取消发药。POST /api/pharmacy/dispensing/{id}/cancel */
  cancelDispensing: (id: number): Promise<DispensingResponse | BusinessError> => {
    return apiPost<DispensingResponse>(`/pharmacy/dispensing/${id}/cancel`)
  },

  /** 获取发药单详情。GET /api/pharmacy/dispensing/{id} */
  getDispensing: (id: number): Promise<DispensingResponse | BusinessError> => {
    return apiGet<DispensingResponse>(`/pharmacy/dispensing/${id}`)
  },

  /** 查询发药记录列表。GET /api/pharmacy/dispensing */
  queryDispensing: (
    params: DispensingQueryRequest,
  ): Promise<DispensingResponse[] | BusinessError> => {
    return apiGet<DispensingResponse[]>('/pharmacy/dispensing', { params })
  },

  // ---- 退药（PharmacyRefund） ----

  /** 创建退药单。POST /api/pharmacy/refund */
  createRefund: (
    request: PharmacyRefundCreateRequest,
  ): Promise<PharmacyRefundResponse | BusinessError> => {
    return apiPost<PharmacyRefundResponse>('/pharmacy/refund', request)
  },

  /** 审批退药（通过）。POST /api/pharmacy/refund/{id}/approve */
  approveRefund: (id: number): Promise<PharmacyRefundResponse | BusinessError> => {
    return apiPost<PharmacyRefundResponse>(`/pharmacy/refund/${id}/approve`)
  },

  /** 驳回退药。POST /api/pharmacy/refund/{id}/reject?reason= */
  rejectRefund: (
    id: number,
    reason: string,
  ): Promise<PharmacyRefundResponse | BusinessError> => {
    return apiPost<PharmacyRefundResponse>(`/pharmacy/refund/${id}/reject`, null, {
      params: { reason },
    })
  },

  /** 获取退药单详情。GET /api/pharmacy/refund/{id} */
  getRefund: (id: number): Promise<PharmacyRefundResponse | BusinessError> => {
    return apiGet<PharmacyRefundResponse>(`/pharmacy/refund/${id}`)
  },

  /** 查询退药记录列表。GET /api/pharmacy/refund */
  queryRefund: (
    params: PharmacyRefundQueryRequest,
  ): Promise<PharmacyRefundResponse[] | BusinessError> => {
    return apiGet<PharmacyRefundResponse[]>('/pharmacy/refund', { params })
  },

  // ---- 药房库存（PharmacyStock） ----

  /** 查询药房库存列表。GET /api/pharmacy/stock */
  queryStock: (
    params: PharmacyStockQueryRequest,
  ): Promise<PharmacyStockResponse[] | BusinessError> => {
    return apiGet<PharmacyStockResponse[]>('/pharmacy/stock', { params })
  },

  /** 按药品编码查询库存详情。GET /api/pharmacy/stock/{drugCode} */
  getStock: (drugCode: string): Promise<PharmacyStockResponse | BusinessError> => {
    return apiGet<PharmacyStockResponse>(`/pharmacy/stock/${drugCode}`)
  },

  /** 查询低库存预警列表。GET /api/pharmacy/stock/low-stock */
  listLowStock: (): Promise<PharmacyStockResponse[] | BusinessError> => {
    return apiGet<PharmacyStockResponse[]>('/pharmacy/stock/low-stock')
  },
}
