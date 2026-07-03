// ===========================================================================
// 窗口模块类型（线下挂号 / 收费退费 / 对账）
//
// <p>命名约定（与现有 doctor 模块保持一致）：
// - 响应体（Response）字段使用 snake_case：后端 JacksonConfig 全局 SNAKE_CASE 序列化。
// - 请求体（POST/PUT Body，CreateRequest/PayRequest 等）字段使用 snake_case：
//   后端 Jackson SNAKE_CASE 反序列化，axios 直接序列化对象为 JSON。
// - 查询参数（GET QueryRequest）字段使用 camelCase：Spring 直接按 Java 字段名绑定
//   query param（不经过 Jackson），参考 doctor.ts 的 `params: { patientId }` 用法。
// - 时间字段为 ISO-8601 字符串（后端 LocalDateTime 序列化）。
// ===========================================================================

// ==================== 线下挂号 ====================

/** 线下挂号状态枚举值（与后端状态机一致）。 */
export type OfflineRegistrationStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED'

/** 创建线下挂号请求。对应后端 OfflineRegistrationCreateRequest（POST body, snake_case）。 */
export interface OfflineRegistrationCreateRequest {
  patient_id: number
  patient_name: string
  patient_phone?: string
  id_card?: string
  doctor_id?: number
  doctor_name?: string
  department?: string
  registration_type?: string
  registration_fee?: number
  remark?: string
}

/** 取消挂号请求。对应后端 OfflineRegistrationCancelRequest（POST body, snake_case）。 */
export interface OfflineRegistrationCancelRequest {
  cancel_reason: string
}

/** 挂号记录查询请求。对应后端 OfflineRegistrationQueryRequest（GET query, camelCase）。 */
export interface OfflineRegistrationQueryRequest {
  patientId?: number
  patientName?: string
  status?: OfflineRegistrationStatus
  page?: number
  size?: number
}

/** 挂号记录响应。对应后端 OfflineRegistrationResponse（snake_case）。 */
export interface OfflineRegistrationResponse {
  id: number
  registration_no: string | null
  patient_id: number
  patient_name: string | null
  patient_phone: string | null
  id_card: string | null
  doctor_id: number | null
  doctor_name: string | null
  department: string | null
  registration_type: string | null
  registration_fee: number | null
  status: OfflineRegistrationStatus
  cancel_reason: string | null
  remark: string | null
  created_at: string | null
  updated_at: string | null
  cancelled_at: string | null
}

// ==================== 收费退费 ====================

/** 缴费记录状态枚举值。 */
export type PaymentStatus = 'PENDING' | 'PAID' | 'REFUNDED' | 'RECONCILED'

/** 缴费明细项（创建请求）。对应后端 PaymentItemRequest（POST body, snake_case）。 */
export interface PaymentItemRequest {
  item_type: string
  item_name: string
  quantity?: number
  unit_price: number
  remark?: string
}

/** 缴费明细项（响应）。对应后端 PaymentItemResponse（snake_case）。 */
export interface PaymentItemResponse {
  id: number | null
  item_type: string
  item_name: string
  quantity: number | null
  unit_price: number
  remark: string | null
}

/** 创建缴费单请求。对应后端 PaymentCreateRequest（POST body, snake_case）。 */
export interface PaymentCreateRequest {
  patient_id: number
  patient_name?: string
  source_id?: number
  source_type?: string
  source_no?: string
  total_amount: number
  items: PaymentItemRequest[]
  remark?: string
}

/** 支付请求。对应后端 PayRequest（POST body, snake_case）。 */
export interface PayRequest {
  payment_method: string
  payer_name?: string
  paid_amount: number
}

/** 退费请求。对应后端 RefundRequest（POST body, snake_case）。 */
export interface RefundRequest {
  refund_reason: string
}

/** 缴费记录查询请求。对应后端 PaymentQueryRequest（GET query, camelCase）。 */
export interface PaymentQueryRequest {
  patientId?: number
  status?: PaymentStatus
  sourceType?: string
  page?: number
  size?: number
}

/** 缴费记录响应。对应后端 PaymentRecordResponse（snake_case）。 */
export interface PaymentRecordResponse {
  id: number
  payment_no: string | null
  patient_id: number
  patient_name: string | null
  source_id: number | null
  source_type: string | null
  source_no: string | null
  total_amount: number
  paid_amount: number | null
  payment_method: string | null
  payer_name: string | null
  status: PaymentStatus
  refund_reason: string | null
  refund_amount: number | null
  refund_time: string | null
  reconciled: boolean | null
  reconcile_batch_no: string | null
  reconcile_time: string | null
  items: PaymentItemResponse[] | null
  remark: string | null
  created_at: string | null
  updated_at: string | null
  paid_at: string | null
}

// ==================== 对账 ====================

/** 对账请求。对应后端 ReconcileRequest（POST body, snake_case）。 */
export interface ReconcileRequest {
  payment_ids: number[]
  reconcile_batch_no?: string
}

/** 对账响应。对应后端 ReconcileResponse（snake_case）。 */
export interface ReconcileResponse {
  batch_no: string | null
  reconciled_count: number | null
  total_amount: number | null
  records: PaymentRecordResponse[] | null
}
