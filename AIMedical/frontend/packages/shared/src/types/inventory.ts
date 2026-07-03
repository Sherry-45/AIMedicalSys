// ===========================================================================
// 药库模块（inventory）类型定义
//
// <p>对应后端 /api/inventory/* 系列接口的 DTO。所有字段遵循后端 JacksonConfig
// 全局 SNAKE_CASE 约定，与 doctor 模块保持一致。时间字段为 ISO-8601 字符串。
// ===========================================================================

import type { PageResponse } from './index'

// ==================== 盘点（Stocktaking） ====================

/** 盘点状态枚举值（与后端 StocktakingStatus.code 一致）。 */
export type StocktakingStatus =
  | 'DRAFT'
  | 'IN_PROGRESS'
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'REJECTED'
  | 'COMPLETED'
  | 'CANCELLED'

/** 盘点明细请求。对应后端 StocktakingItemRequest。 */
export interface StocktakingItemRequest {
  drug_code: string
  drug_name?: string
  batch_no?: string
  book_quantity?: number
  actual_quantity?: number
  unit?: string
  remark?: string
}

/** 创建盘点单请求。对应后端 StocktakingCreateRequest。 */
export interface StocktakingCreateRequest {
  stocktaking_type?: string
  remark?: string
  items: StocktakingItemRequest[]
}

/** 盘点查询请求。对应后端 StocktakingQueryRequest。 */
export interface StocktakingQueryRequest {
  status?: string
  page?: number
  size?: number
}

/** 盘点驳回请求。对应后端 StocktakingRejectRequest。 */
export interface StocktakingRejectRequest {
  reject_reason?: string
}

/** 盘点明细响应。对应后端 StocktakingItemDto。 */
export interface StocktakingItem {
  id: number | null
  drug_code: string
  drug_name?: string
  batch_no?: string
  book_quantity?: number
  actual_quantity?: number
  unit?: string
  remark?: string
}

/** 盘点单响应。对应后端 StocktakingResponse。 */
export interface StocktakingResponse {
  id: number
  stocktaking_no?: string
  stocktaking_type?: string
  status: StocktakingStatus
  operator_id?: number
  operator_name?: string
  start_time?: string
  end_time?: string
  total_items?: number
  surplus_items?: number
  loss_items?: number
  approver_id?: number
  approver_name?: string
  approved_at?: string
  reject_reason?: string
  remark?: string
  items?: StocktakingItem[]
  created_at?: string
  updated_at?: string
}

// ==================== 调拨（Transfer） ====================

/** 调拨状态枚举值（与后端 TransferStatus.code 一致）。 */
export type TransferStatus =
  | 'DRAFT'
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'REJECTED'
  | 'IN_TRANSIT'
  | 'RECEIVED'
  | 'CANCELLED'

/** 调拨明细请求。对应后端 TransferItemRequest。 */
export interface TransferItemRequest {
  drug_code: string
  drug_name?: string
  specification?: string
  batch_no?: string
  quantity: number
  unit?: string
  unit_price?: number
}

/** 创建调拨单请求。对应后端 TransferCreateRequest。 */
export interface TransferCreateRequest {
  transfer_type: string
  source_dept?: string
  target_dept?: string
  remark?: string
  items: TransferItemRequest[]
}

/** 调拨审批请求。对应后端 TransferApproveRequest。 */
export interface TransferApproveRequest {
  approved: boolean
  reject_reason?: string
}

/** 调拨查询请求。对应后端 TransferQueryRequest。 */
export interface TransferQueryRequest {
  status?: string
  transferType?: string
  page?: number
  size?: number
}

/** 调拨明细响应。对应后端 TransferItemDto。 */
export interface TransferItem {
  id: number | null
  drug_code: string
  drug_name?: string
  specification?: string
  batch_no?: string
  quantity: number
  unit?: string
  unit_price?: number
}

/** 调拨单响应。对应后端 TransferOrderResponse。 */
export interface TransferOrderResponse {
  id: number
  transfer_no?: string
  transfer_type?: string
  status: TransferStatus
  source_dept?: string
  target_dept?: string
  applicant_id?: number
  applicant_name?: string
  approver_id?: number
  approver_name?: string
  approved_at?: string
  shipped_at?: string
  received_at?: string
  total_items?: number
  total_amount?: number
  reject_reason?: string
  remark?: string
  created_at?: string
  updated_at?: string
  items?: TransferItem[]
}

// ==================== 库存（Inventory Stock） ====================

/** 库存查询请求。对应后端 InventoryStockQueryRequest。 */
export interface InventoryStockQueryRequest {
  drugCode?: string
  batchNo?: string
  page?: number
  size?: number
}

/** 库存调整请求。对应后端 StockAdjustRequest。 */
export interface StockAdjustRequest {
  drug_code: string
  batch_no: string
  quantity: number
  remark?: string
}

/** 库存响应。对应后端 InventoryStockResponse。 */
export interface InventoryStockResponse {
  id: number
  drug_code: string
  drug_name?: string
  specification?: string
  batch_no?: string
  quantity: number
  unit?: string
  unit_price?: number
  expiry_date?: string
  manufacturer?: string
  drug_category?: string
  enabled?: boolean
  created_at?: string
  updated_at?: string
}

// ==================== 药品目录（Drug Catalog） ====================

/** 药品目录创建请求。对应后端 DrugCatalogCreateRequest。 */
export interface DrugCatalogCreateRequest {
  drug_code: string
  drug_name: string
  generic_name?: string
  specification?: string
  manufacturer?: string
  drug_form?: string
  drug_category: string
  unit?: string
  retail_price?: number
  purchase_price?: number
  otc_flag?: boolean
  remark?: string
}

/** 药品目录查询请求。对应后端 DrugCatalogQueryRequest。 */
export interface DrugCatalogQueryRequest {
  drug_code?: string
  drug_name?: string
  drug_category?: string
  enabled?: boolean
  page?: number
  size?: number
}

/** 药品目录响应。对应后端 DrugCatalogResponse。 */
export interface DrugCatalogResponse {
  id: number
  drug_code: string
  drug_name: string
  generic_name?: string
  specification?: string
  manufacturer?: string
  drug_form?: string
  drug_category: string
  unit?: string
  retail_price?: number
  purchase_price?: number
  otc_flag?: boolean
  enabled?: boolean
  remark?: string
  created_at?: string
  updated_at?: string
}

// ==================== 分页响应别名 ====================

export type StocktakingPage = PageResponse<StocktakingResponse>
export type TransferPage = PageResponse<TransferOrderResponse>
export type InventoryStockPage = PageResponse<InventoryStockResponse>
export type DrugCatalogPage = PageResponse<DrugCatalogResponse>
