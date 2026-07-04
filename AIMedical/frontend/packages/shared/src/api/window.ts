import type { BusinessError, PageResponse } from '../types'
import { apiGet, apiPost } from './client'

import type {
  OfflineRegistrationCancelRequest,
  OfflineRegistrationCreateRequest,
  OfflineRegistrationQueryRequest,
  OfflineRegistrationResponse,
  PaymentCreateRequest,
  PaymentQueryRequest,
  PaymentRecordResponse,
  PayRequest,
  ReconcileRequest,
  ReconcileResponse,
  RefundRequest,
} from '../types/window'

/**
 * 窗口模块 API
 *
 * <p>对应后端 /api/window/* 系列接口（OfflineRegistrationController /
 * PaymentController / ReconciliationController）。所有方法返回
 * Promise<T | BusinessError>，调用方需用 isBusinessError() 判断结果。
 *
 * <p>注意：路径不含 /api 前缀（client.ts 的 baseURL 已配置 /api）。
 * 查询接口返回 PageResponse<T>（content/totalElements/totalPages/page/size）。
 */
export const windowApi = {
  // ---- 线下挂号（OfflineRegistrationController: /window/registration） ----

  /** 创建线下挂号。POST /api/window/registration */
  createRegistration: (
    request: OfflineRegistrationCreateRequest,
  ): Promise<OfflineRegistrationResponse | BusinessError> => {
    return apiPost<OfflineRegistrationResponse>('/window/registration', request)
  },

  /** 取消挂号。POST /api/window/registration/{id}/cancel */
  cancelRegistration: (
    id: number,
    request: OfflineRegistrationCancelRequest,
  ): Promise<OfflineRegistrationResponse | BusinessError> => {
    return apiPost<OfflineRegistrationResponse>(`/window/registration/${id}/cancel`, request)
  },

  /** 查询挂号详情。GET /api/window/registration/{id} */
  getRegistration: (id: number): Promise<OfflineRegistrationResponse | BusinessError> => {
    return apiGet<OfflineRegistrationResponse>(`/window/registration/${id}`)
  },

  /** 分页查询挂号记录。GET /api/window/registration */
  queryRegistrations: (
    request: OfflineRegistrationQueryRequest,
  ): Promise<PageResponse<OfflineRegistrationResponse> | BusinessError> => {
    return apiGet<PageResponse<OfflineRegistrationResponse>>('/window/registration', {
      params: request,
    })
  },

  // ---- 收费退费（PaymentController: /window/payment） ----

  /** 创建缴费单。POST /api/window/payment */
  createPayment: (
    request: PaymentCreateRequest,
  ): Promise<PaymentRecordResponse | BusinessError> => {
    return apiPost<PaymentRecordResponse>('/window/payment', request)
  },

  /** 支付。POST /api/window/payment/{id}/pay */
  payPayment: (
    id: number,
    request: PayRequest,
  ): Promise<PaymentRecordResponse | BusinessError> => {
    return apiPost<PaymentRecordResponse>(`/window/payment/${id}/pay`, request)
  },

  /** 退费。POST /api/window/payment/{id}/refund */
  refundPayment: (
    id: number,
    request: RefundRequest,
  ): Promise<PaymentRecordResponse | BusinessError> => {
    return apiPost<PaymentRecordResponse>(`/window/payment/${id}/refund`, request)
  },

  /** 查询缴费详情。GET /api/window/payment/{id} */
  getPayment: (id: number): Promise<PaymentRecordResponse | BusinessError> => {
    return apiGet<PaymentRecordResponse>(`/window/payment/${id}`)
  },

  /** 分页查询缴费记录。GET /api/window/payment */
  queryPayments: (
    request: PaymentQueryRequest,
  ): Promise<PageResponse<PaymentRecordResponse> | BusinessError> => {
    return apiGet<PageResponse<PaymentRecordResponse>>('/window/payment', {
      params: request,
    })
  },

  /** 按患者查询缴费记录列表。GET /api/window/payment/patient/{patientId} */
  queryPaymentsByPatient: (
    patientId: number,
  ): Promise<PaymentRecordResponse[] | BusinessError> => {
    return apiGet<PaymentRecordResponse[]>(`/window/payment/patient/${patientId}`)
  },

  // ---- 对账（ReconciliationController: /window/reconciliation） ----

  /** 提交对账。POST /api/window/reconciliation/reconcile */
  reconcile: (request: ReconcileRequest): Promise<ReconcileResponse | BusinessError> => {
    return apiPost<ReconcileResponse>('/window/reconciliation/reconcile', request)
  },

  /** 按批次号查询对账记录列表。GET /api/window/reconciliation/batch/{batchNo} */
  listByBatchNo: (batchNo: string): Promise<PaymentRecordResponse[] | BusinessError> => {
    return apiGet<PaymentRecordResponse[]>(`/window/reconciliation/batch/${batchNo}`)
  },
}
