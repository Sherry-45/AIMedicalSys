/**
 * 健康档案模块类型定义。
 *
 * <p>对应后端 HealthRecordController (/api/patient/health-records) 系列接口。
 *
 * <p>字段命名约定（与现有 doctor 模块保持一致）：
 * - 请求体 / 响应体字段遵循后端 JacksonConfig 全局 SNAKE_CASE 约定（如 patient_id、record_type）；
 * - 查询参数字段使用 camelCase，与 Spring MVC 请求参数绑定一致（参考 doctor.ts 中 patientId 用法）。
 */

// ==================== 请求体（JSON body，snake_case） ====================

/**
 * 创建健康档案请求。对应后端 HealthRecordCreateRequest。
 *
 * <p>POST /api/patient/health-records，请求体由 Jackson SNAKE_CASE 反序列化。
 */
export interface HealthRecordCreateRequest {
  patient_id: number
  record_type: string
  record_category?: string
  title: string
  content?: string
  organization?: string
  department?: string
  doctor_name?: string
  source_id?: number
  source_table?: string
  report_data?: string
  record_date?: string
  remark?: string
}

// ==================== 查询参数（query params，camelCase） ====================

/**
 * 健康档案查询请求。对应后端 HealthRecordQueryRequest。
 *
 * <p>GET /api/patient/health-records，参数通过 query string 传递，
 * Spring MVC 按字段名绑定，故使用 camelCase。
 */
export interface HealthRecordQueryRequest {
  recordType?: string
  recordCategory?: string
  organization?: string
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

// ==================== 响应体（JSON，snake_case） ====================

/**
 * 健康档案响应。对应后端 HealthRecordResponse。
 *
 * <p>由 Jackson SNAKE_CASE 序列化输出，字段名保持 snake_case。
 */
export interface HealthRecordResponse {
  id: number
  patient_id: number
  record_type: string
  record_type_desc?: string
  record_category?: string
  record_category_desc?: string
  title: string
  content?: string
  organization?: string
  department?: string
  doctor_name?: string
  source_id?: number
  source_table?: string
  report_data?: string
  record_date?: string
  remark?: string
  created_at?: string
  updated_at?: string
}

/**
 * 健康趋势响应。对应后端 HealthTrendResponse。
 *
 * <p>聚合统计信息：按类型/类别分组的记录数、最近记录列表、就诊机构等。
 */
export interface HealthTrendResponse {
  patient_id: number
  total_records: number
  records_by_type: Record<string, number>
  records_by_category: Record<string, number>
  recent_records: HealthRecordResponse[]
  earliest_record_date?: string
  latest_record_date?: string
  organizations: string[]
}

/**
 * 健康摘要响应。对应后端 HealthSummaryResponse。
 *
 * <p>包含患者基本信息、过敏/慢病计数、最近就诊信息及嵌套的健康趋势。
 */
export interface HealthSummaryResponse {
  patient_id: number
  patient_name?: string
  total_records: number
  allergy_count?: number
  chronic_disease_count?: number
  last_visit_date?: string
  last_visit_organization?: string
  health_trend: HealthTrendResponse
}
