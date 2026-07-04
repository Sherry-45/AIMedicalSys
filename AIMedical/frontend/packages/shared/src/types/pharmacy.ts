// ===========================================================================
// 药房模块类型定义（pharmacy）
//
// <p>独立类型文件，对应后端 pharmacy 模块的 record DTO。
// - 响应接口（Response）：snake_case，匹配后端 Jackson SNAKE_CASE 序列化
// - 创建请求接口（CreateRequest/ItemRequest）：snake_case，匹配 @RequestBody
// - 查询请求接口（QueryRequest）：camelCase，匹配 @ModelAttribute
// PageResponse<T> 已在 types/index.ts 中定义，使用时直接从 './index' 导入。
// ===========================================================================

import type { PageResponse } from './index'

// ==================== 发药（Dispensing） ====================

/** 发药状态枚举值（与后端 DispensingStatus.code 一致）。 */
export type DispensingStatus = 'PENDING' | 'DISPENSED' | 'REFUNDED' | 'CANCELLED'

/** 发药明细（创建请求）。对应后端 DispensingItemRequest。 */
export interface DispensingItemRequest {
  drug_code: string
  drug_name: string
  specification?: string
  batch_no?: string
  quantity: number
  unit?: string
  unit_price?: number
  dosage?: string
  usage_method?: string
  frequency?: string
  days?: number
}

/** 创建发药请求。对应后端 DispensingCreateRequest。 */
export interface DispensingCreateRequest {
  prescription_id?: number
  medical_order_id?: number
  patient_id: number
  patient_name?: string
  items: DispensingItemRequest[]
  remark?: string
}

/** 发药查询请求。对应后端 DispensingQueryRequest（@ModelAttribute，保持 camelCase）。 */
export interface DispensingQueryRequest {
  patientId?: number
  status?: string
  page?: number
  size?: number
}

/** 发药响应。对应后端 DispensingResponse。 */
export interface DispensingResponse {
  id: number
  dispensing_no?: string
  prescription_id?: number
  medical_order_id?: number
  patient_id: number
  patient_name?: string
  pharmacist_id?: number
  pharmacist_name?: string
  status: string
  total_quantity?: number
  total_amount?: number
  dispensed_at?: string
  remark?: string
  created_at?: string
  updated_at?: string
  items?: any[]
}

// ==================== 退药（PharmacyRefund） ====================

/** 退药状态枚举值（与后端 PharmacyRefundStatus.code 一致，无 APPROVED）。 */
export type PharmacyRefundStatus = 'PENDING' | 'REFUNDED' | 'REJECTED'

/** 退药明细（创建请求）。对应后端 PharmacyRefundItemRequest。 */
export interface PharmacyRefundItemRequest {
  dispensing_item_id: number
  drug_code: string
  drug_name: string
  batch_no?: string
  quantity: number
  unit?: string
  unit_price?: number
}

/** 创建退药请求。对应后端 PharmacyRefundCreateRequest。 */
export interface PharmacyRefundCreateRequest {
  dispensing_id: number
  refund_reason?: string
  items: PharmacyRefundItemRequest[]
  remark?: string
}

/** 退药查询请求。对应后端 PharmacyRefundQueryRequest（@ModelAttribute，保持 camelCase）。 */
export interface PharmacyRefundQueryRequest {
  patientId?: number
  status?: string
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}

/** 退药响应。对应后端 PharmacyRefundResponse。 */
export interface PharmacyRefundResponse {
  id: number
  refund_no?: string
  dispensing_id: number
  patient_id?: number
  patient_name?: string
  pharmacist_id?: number
  pharmacist_name?: string
  status: string
  refund_reason?: string
  total_quantity?: number
  total_amount?: number
  refunded_at?: string
  remark?: string
  created_at?: string
  updated_at?: string
  items?: any[]
}

// ==================== 药房库存（PharmacyStock） ====================

/** 库存查询请求。对应后端 PharmacyStockQueryRequest（@ModelAttribute，保持 camelCase）。 */
export interface PharmacyStockQueryRequest {
  drugCode?: string
  drugName?: string
  page?: number
  size?: number
}

/** 药房库存响应。对应后端 PharmacyStockResponse。 */
export interface PharmacyStockResponse {
  id: number
  drug_code: string
  drug_name?: string
  specification?: string
  batch_no?: string
  quantity: number
  unit?: string
  retail_price?: number
  expiry_date?: string
  shelf_location?: string
  safety_stock?: number
  remark?: string
  created_at?: string
  updated_at?: string
}

// ==================== 分页响应别名 ====================

export type DispensingPage = PageResponse<DispensingResponse>
export type PharmacyRefundPage = PageResponse<PharmacyRefundResponse>
export type PharmacyStockPage = PageResponse<PharmacyStockResponse>
