// ===========================================================================
// 药房模块类型定义（pharmacy）
//
// <p>独立类型文件，对应后端 pharmacy 模块的 record DTO。
// PageResponse<T> 已在 types/index.ts 中定义，此处不重复定义，使用时直接从
// '../types' 或 '@aimedical/shared' 导入即可。
// ===========================================================================

// ==================== 发药（Dispensing） ====================

/** 发药明细（创建请求）。对应后端 DispensingItemRequest。 */
export interface DispensingItemRequest {
  drugCode: string
  drugName: string
  specification?: string
  batchNo?: string
  quantity: number
  unit?: string
  unitPrice?: number
  dosage?: string
  usageMethod?: string
  frequency?: string
  days?: number
}

/** 创建发药请求。对应后端 DispensingCreateRequest。 */
export interface DispensingCreateRequest {
  prescriptionId?: number
  medicalOrderId?: number
  patientId: number
  patientName?: string
  items: DispensingItemRequest[]
  remark?: string
}

/** 发药查询请求。对应后端 DispensingQueryRequest。 */
export interface DispensingQueryRequest {
  patientId?: number
  status?: string
  page?: number
  size?: number
}

/** 发药响应。对应后端 DispensingResponse。 */
export interface DispensingResponse {
  id: number
  dispensingNo?: string
  prescriptionId?: number
  patientId: number
  patientName?: string
  status: string
  items?: any[]
  totalAmount?: number
  operatorId?: number
  operatorName?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
  dispensedAt?: string
}

// ==================== 退药（PharmacyRefund） ====================

/** 退药明细（创建请求）。对应后端 PharmacyRefundItemRequest。 */
export interface PharmacyRefundItemRequest {
  dispensingItemId: number
  drugCode: string
  drugName: string
  batchNo?: string
  quantity: number
  unit?: string
  unitPrice?: number
}

/** 创建退药请求。对应后端 PharmacyRefundCreateRequest。 */
export interface PharmacyRefundCreateRequest {
  dispensingId: number
  refundReason?: string
  items: PharmacyRefundItemRequest[]
  remark?: string
}

/** 退药查询请求。对应后端 PharmacyRefundQueryRequest。 */
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
  refundNo?: string
  dispensingId: number
  patientId?: number
  patientName?: string
  status: string
  refundReason?: string
  items?: any[]
  totalRefundAmount?: number
  operatorId?: number
  operatorName?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

// ==================== 药房库存（PharmacyStock） ====================

/** 库存查询请求。对应后端 PharmacyStockQueryRequest。 */
export interface PharmacyStockQueryRequest {
  drugCode?: string
  drugName?: string
  page?: number
  size?: number
}

/** 药房库存响应。对应后端 PharmacyStockResponse。 */
export interface PharmacyStockResponse {
  id: number
  drugCode: string
  drugName?: string
  specification?: string
  batchNo?: string
  quantity: number
  unit?: string
  unitPrice?: number
  expiryDate?: string
  warningThreshold?: number
  createdAt?: string
  updatedAt?: string
}
