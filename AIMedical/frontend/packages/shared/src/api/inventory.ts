import type { BusinessError } from '../types'
import type { PageResponse } from '../types'
import type {
  DrugCatalogCreateRequest,
  DrugCatalogQueryRequest,
  DrugCatalogResponse,
  InventoryStockQueryRequest,
  InventoryStockResponse,
  StockAdjustRequest,
  StocktakingCreateRequest,
  StocktakingItemRequest,
  StocktakingQueryRequest,
  StocktakingRejectRequest,
  StocktakingResponse,
  TransferApproveRequest,
  TransferCreateRequest,
  TransferOrderResponse,
  TransferQueryRequest,
} from '../types/inventory'
import { apiDelete, apiGet, apiPost, apiPut } from './client'

/**
 * 药库模块 API
 *
 * <p>对应后端 /api/inventory/* 系列接口。所有方法返回 Promise<T | BusinessError>，
 * 调用方需用 isBusinessError() 判断结果。
 *
 * <p>包含四组接口：盘点（stocktaking）、调拨（transfer）、库存（stock）、药品目录（drugs）。
 */
export const inventoryApi = {
  // ==================== 盘点（Stocktaking） ====================

  /** 创建盘点单。POST /api/inventory/stocktaking */
  createStocktaking: (
    request: StocktakingCreateRequest,
  ): Promise<StocktakingResponse | BusinessError> => {
    return apiPost<StocktakingResponse>('/inventory/stocktaking', request)
  },

  /** 开始盘点。POST /api/inventory/stocktaking/{id}/start */
  startStocktaking: (id: number): Promise<StocktakingResponse | BusinessError> => {
    return apiPost<StocktakingResponse>(`/inventory/stocktaking/${id}/start`)
  },

  /** 提交盘点明细（录入实际数量）。PUT /api/inventory/stocktaking/{id}/items */
  submitStocktakingItems: (
    id: number,
    items: StocktakingItemRequest[],
  ): Promise<StocktakingResponse | BusinessError> => {
    return apiPut<StocktakingResponse>(`/inventory/stocktaking/${id}/items`, items)
  },

  /** 提交盘点审批。POST /api/inventory/stocktaking/{id}/submit */
  submitStocktakingForApproval: (id: number): Promise<StocktakingResponse | BusinessError> => {
    return apiPost<StocktakingResponse>(`/inventory/stocktaking/${id}/submit`)
  },

  /** 审批通过盘点。POST /api/inventory/stocktaking/{id}/approve */
  approveStocktaking: (id: number): Promise<StocktakingResponse | BusinessError> => {
    return apiPost<StocktakingResponse>(`/inventory/stocktaking/${id}/approve`)
  },

  /** 驳回盘点。POST /api/inventory/stocktaking/{id}/reject */
  rejectStocktaking: (
    id: number,
    request: StocktakingRejectRequest,
  ): Promise<StocktakingResponse | BusinessError> => {
    return apiPost<StocktakingResponse>(`/inventory/stocktaking/${id}/reject`, request)
  },

  /** 完成盘点。POST /api/inventory/stocktaking/{id}/complete */
  completeStocktaking: (id: number): Promise<StocktakingResponse | BusinessError> => {
    return apiPost<StocktakingResponse>(`/inventory/stocktaking/${id}/complete`)
  },

  /** 取消盘点。POST /api/inventory/stocktaking/{id}/cancel */
  cancelStocktaking: (id: number): Promise<StocktakingResponse | BusinessError> => {
    return apiPost<StocktakingResponse>(`/inventory/stocktaking/${id}/cancel`)
  },

  /** 获取盘点单详情。GET /api/inventory/stocktaking/{id} */
  getStocktaking: (id: number): Promise<StocktakingResponse | BusinessError> => {
    return apiGet<StocktakingResponse>(`/inventory/stocktaking/${id}`)
  },

  /** 分页查询盘点单。GET /api/inventory/stocktaking */
  queryStocktaking: (
    request: StocktakingQueryRequest,
  ): Promise<PageResponse<StocktakingResponse> | BusinessError> => {
    return apiGet<PageResponse<StocktakingResponse>>('/inventory/stocktaking', {
      params: request,
    })
  },

  // ==================== 调拨（Transfer） ====================

  /** 创建调拨单。POST /api/inventory/transfer */
  createTransfer: (
    request: TransferCreateRequest,
  ): Promise<TransferOrderResponse | BusinessError> => {
    return apiPost<TransferOrderResponse>('/inventory/transfer', request)
  },

  /** 提交调拨单。POST /api/inventory/transfer/{id}/submit */
  submitTransfer: (id: number): Promise<TransferOrderResponse | BusinessError> => {
    return apiPost<TransferOrderResponse>(`/inventory/transfer/${id}/submit`)
  },

  /** 审批调拨单。POST /api/inventory/transfer/{id}/approve */
  approveTransfer: (
    id: number,
    request: TransferApproveRequest,
  ): Promise<TransferOrderResponse | BusinessError> => {
    return apiPost<TransferOrderResponse>(`/inventory/transfer/${id}/approve`, request)
  },

  /** 发货。POST /api/inventory/transfer/{id}/ship */
  shipTransfer: (id: number): Promise<TransferOrderResponse | BusinessError> => {
    return apiPost<TransferOrderResponse>(`/inventory/transfer/${id}/ship`)
  },

  /** 接收。POST /api/inventory/transfer/{id}/receive */
  receiveTransfer: (id: number): Promise<TransferOrderResponse | BusinessError> => {
    return apiPost<TransferOrderResponse>(`/inventory/transfer/${id}/receive`)
  },

  /** 取消调拨。POST /api/inventory/transfer/{id}/cancel */
  cancelTransfer: (id: number): Promise<TransferOrderResponse | BusinessError> => {
    return apiPost<TransferOrderResponse>(`/inventory/transfer/${id}/cancel`)
  },

  /** 获取调拨单详情。GET /api/inventory/transfer/{id} */
  getTransfer: (id: number): Promise<TransferOrderResponse | BusinessError> => {
    return apiGet<TransferOrderResponse>(`/inventory/transfer/${id}`)
  },

  /** 分页查询调拨单。GET /api/inventory/transfer */
  queryTransfer: (
    request: TransferQueryRequest,
  ): Promise<PageResponse<TransferOrderResponse> | BusinessError> => {
    return apiGet<PageResponse<TransferOrderResponse>>('/inventory/transfer', {
      params: request,
    })
  },

  // ==================== 库存（Inventory Stock） ====================

  /** 分页查询库存。GET /api/inventory/stock */
  queryStock: (
    request: InventoryStockQueryRequest,
  ): Promise<PageResponse<InventoryStockResponse> | BusinessError> => {
    return apiGet<PageResponse<InventoryStockResponse>>('/inventory/stock', {
      params: request,
    })
  },

  /** 按药品编码查询库存列表。GET /api/inventory/stock/{drugCode} */
  getStockByDrugCode: (
    drugCode: string,
  ): Promise<InventoryStockResponse[] | BusinessError> => {
    return apiGet<InventoryStockResponse[]>(`/inventory/stock/${drugCode}`)
  },

  /** 库存调整。POST /api/inventory/stock/adjust */
  adjustStock: (
    request: StockAdjustRequest,
  ): Promise<InventoryStockResponse | BusinessError> => {
    return apiPost<InventoryStockResponse>('/inventory/stock/adjust', request)
  },

  /** 查询近效期药品列表。GET /api/inventory/stock/expiring */
  listExpiringSoon: (
    days?: number,
  ): Promise<InventoryStockResponse[] | BusinessError> => {
    return apiGet<InventoryStockResponse[]>('/inventory/stock/expiring', {
      params: days !== undefined ? { days } : undefined,
    })
  },

  /** 查询低库存预警列表。GET /api/inventory/stock/low-stock */
  listLowStock: (): Promise<InventoryStockResponse[] | BusinessError> => {
    return apiGet<InventoryStockResponse[]>('/inventory/stock/low-stock')
  },

  // ==================== 药品目录（Drug Catalog） ====================

  /** 创建药品。POST /api/inventory/drugs */
  createDrug: (
    request: DrugCatalogCreateRequest,
  ): Promise<DrugCatalogResponse | BusinessError> => {
    return apiPost<DrugCatalogResponse>('/inventory/drugs', request)
  },

  /** 更新药品。PUT /api/inventory/drugs/{id} */
  updateDrug: (
    id: number,
    request: DrugCatalogCreateRequest,
  ): Promise<DrugCatalogResponse | BusinessError> => {
    return apiPut<DrugCatalogResponse>(`/inventory/drugs/${id}`, request)
  },

  /** 删除药品。DELETE /api/inventory/drugs/{id} */
  deleteDrug: (id: number): Promise<void | BusinessError> => {
    return apiDelete<void>(`/inventory/drugs/${id}`)
  },

  /** 获取药品详情。GET /api/inventory/drugs/{id} */
  getDrug: (id: number): Promise<DrugCatalogResponse | BusinessError> => {
    return apiGet<DrugCatalogResponse>(`/inventory/drugs/${id}`)
  },

  /** 分页查询药品目录。GET /api/inventory/drugs */
  queryDrugs: (
    request: DrugCatalogQueryRequest,
  ): Promise<PageResponse<DrugCatalogResponse> | BusinessError> => {
    return apiGet<PageResponse<DrugCatalogResponse>>('/inventory/drugs', {
      params: request,
    })
  },

  /** 启用/禁用药品。PUT /api/inventory/drugs/{id}/toggle-enabled */
  toggleDrugEnabled: (id: number): Promise<DrugCatalogResponse | BusinessError> => {
    return apiPut<DrugCatalogResponse>(`/inventory/drugs/${id}/toggle-enabled`)
  },
}
