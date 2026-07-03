/**
 * 挂号管理模块类型定义（医生端）。
 *
 * <p>对应后端 RegistrationController (/api/registration) 系列接口。
 *
 * <p>字段命名约定（与现有 doctor / health-record 模块保持一致）：
 * - 请求体 / 响应体字段遵循后端 JacksonConfig 全局 SNAKE_CASE 约定（如 patient_id、registration_no）；
 * - 查询参数字段使用 camelCase，与 Spring MVC 请求参数绑定一致。
 *
 * <p>注意：本模块与 patient 端线上挂号（/api/patient/registration，类型 RegistrationRequest /
 * RegistrationRecord）面向不同控制器，字段不同，故独立定义，不复用 patient 端类型。
 */

/**
 * 挂号状态枚举值（字符串字面量联合类型）。
 *
 * <p>对应后端 RegistrationStatus 枚举：
 * - PENDING：待确认
 * - CONFIRMED：已确认
 * - COMPLETED：已完成
 * - CANCELLED：已取消
 * - NO_SHOW：爽约
 */
export type RegistrationStatus = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW'

/**
 * 创建挂号请求（DTO）。对应后端 RegistrationDTO。
 *
 * <p>POST /api/registration，请求体由 Jackson SNAKE_CASE 反序列化。
 */
export interface RegistrationDTO {
  patient_id: number
  patient_name: string
  doctor_id: number
  doctor_name: string
  department: string
  registration_type: string
  scheduled_date: string
  scheduled_time_slot: string
  remark?: string
}

/**
 * 挂号响应。对应后端 RegistrationResponse。
 *
 * <p>由 Jackson SNAKE_CASE 序列化输出，字段名保持 snake_case。
 */
export interface RegistrationResponse {
  id: number
  registration_no: string
  patient_id: number
  patient_name: string
  patient_phone: string
  doctor_id: number
  doctor_name: string
  department: string
  registration_type: string
  status: RegistrationStatus
  scheduled_date: string
  scheduled_time_slot: string
  registration_fee: number
  remark: string | null
  created_at: string
  updated_at: string
}

/**
 * 取消挂号请求。对应后端 CancelRegistrationRequest。
 *
 * <p>POST /api/registration/{id}/cancel，请求体由 Jackson SNAKE_CASE 反序列化。
 */
export interface CancelRegistrationRequest {
  cancel_reason: string
}
