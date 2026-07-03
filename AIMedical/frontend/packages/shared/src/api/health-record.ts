import type { BusinessError } from '../types'
import { apiGet, apiPost } from './client'
import type {
  HealthRecordCreateRequest,
  HealthRecordQueryRequest,
  HealthRecordResponse,
  HealthSummaryResponse,
  HealthTrendResponse,
} from '../types/health-record'

/**
 * 健康档案 API
 *
 * <p>对应后端 HealthRecordController (/api/patient/health-records)。
 * 注意：路径以 /patient/health-records 开头（不是 /health-record），路径不含 /api 前缀
 * （由 axios 客户端 baseURL 补齐）。
 *
 * <p>patientId 为可选查询参数；管理员/医生查看患者档案时需传 patientId。
 * 所有方法返回 Promise<T | BusinessError>，调用方需用 isBusinessError() 判断结果。
 */
export const healthRecordApi = {
  /** 创建健康档案。POST /api/patient/health-records */
  create: (
    request: HealthRecordCreateRequest,
  ): Promise<HealthRecordResponse | BusinessError> => {
    return apiPost<HealthRecordResponse>('/patient/health-records', request)
  },

  /** 获取健康档案详情。GET /api/patient/health-records/{id} */
  getById: (id: number): Promise<HealthRecordResponse | BusinessError> => {
    return apiGet<HealthRecordResponse>(`/patient/health-records/${id}`)
  },

  /** 查询健康档案列表。GET /api/patient/health-records */
  query: (
    params: HealthRecordQueryRequest,
    patientId?: number,
  ): Promise<HealthRecordResponse[] | BusinessError> => {
    return apiGet<HealthRecordResponse[]>('/patient/health-records', {
      params: { ...params, patientId },
    })
  },

  /** 获取健康趋势。GET /api/patient/health-records/trend */
  getTrend: (patientId?: number): Promise<HealthTrendResponse | BusinessError> => {
    return apiGet<HealthTrendResponse>('/patient/health-records/trend', {
      params: { patientId },
    })
  },

  /** 获取健康摘要。GET /api/patient/health-records/summary */
  getSummary: (patientId?: number): Promise<HealthSummaryResponse | BusinessError> => {
    return apiGet<HealthSummaryResponse>('/patient/health-records/summary', {
      params: { patientId },
    })
  },

  /** 查询缴费记录。GET /api/patient/health-records/payment-records */
  queryPaymentRecords: (
    patientId?: number,
  ): Promise<HealthRecordResponse[] | BusinessError> => {
    return apiGet<HealthRecordResponse[]>('/patient/health-records/payment-records', {
      params: { patientId },
    })
  },

  /** 查询取药记录。GET /api/patient/health-records/dispensing-records */
  queryDispensingRecords: (
    patientId?: number,
  ): Promise<HealthRecordResponse[] | BusinessError> => {
    return apiGet<HealthRecordResponse[]>('/patient/health-records/dispensing-records', {
      params: { patientId },
    })
  },
}
