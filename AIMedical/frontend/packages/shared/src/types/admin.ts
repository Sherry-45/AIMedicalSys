// ===========================================================================
// 管理员端类型定义（admin）
//
// <p>独立类型文件，对应后端 common-module 的用户/角色/岗位管理 DTO。
// - 响应接口（Response）：snake_case，匹配后端 Jackson SNAKE_CASE 序列化
// - 创建请求接口（CreateRequest）：snake_case，匹配 @RequestBody
// - 查询请求接口（QueryRequest）：camelCase，匹配 @ModelAttribute
// PageResponse<T> 已在 types/index.ts 中定义，使用时直接从 './index' 导入。
// ===========================================================================

import type { PageResponse } from './index'

// ==================== 用户管理 ====================

/** 用户类型枚举值（与后端 UserType.code 一致）。 */
export type UserTypeEnum = 'DOCTOR' | 'PATIENT' | 'ADMIN'

/** 用户创建请求。对应后端 UserCreateRequest（@RequestBody，snake_case）。 */
export interface UserCreateRequest {
  username: string
  password: string
  nickname: string
  phone?: string
  email?: string
  user_type: UserTypeEnum
  gender?: string
  age?: number
  role_ids?: number[]
  post_ids?: number[]
  password_change_required?: boolean
  remark?: string
}

/** 用户更新请求。对应后端 UserUpdateRequest（@RequestBody，snake_case，部分更新）。 */
export interface UserUpdateRequest {
  nickname?: string
  phone?: string
  email?: string
  user_type?: UserTypeEnum
  gender?: string
  age?: number
  role_ids?: number[]
  post_ids?: number[]
  enabled?: boolean
  password_change_required?: boolean
  remark?: string
}

/** 用户分页查询请求。对应后端 UserQueryRequest（@ModelAttribute，camelCase）。 */
export interface UserQueryRequest {
  keyword?: string
  userType?: UserTypeEnum
  enabled?: boolean
  page?: number
  size?: number
}

/** 重置密码请求。对应后端 UserPasswordResetRequest。 */
export interface UserPasswordResetRequest {
  new_password: string
  password_change_required?: boolean
}

/** 角色简表（嵌套在用户响应中）。 */
export interface RoleBrief {
  id: number
  code: string
  name: string
}

/** 岗位简表（嵌套在用户响应中）。 */
export interface PostBrief {
  id: number
  code: string
  name: string
}

/** 用户响应。对应后端 UserResponse（snake_case）。 */
export interface UserResponse {
  id: number
  username: string
  nickname: string
  phone?: string
  email?: string
  enabled: boolean
  password_change_required: boolean
  token_version: number
  user_type: UserTypeEnum
  gender?: string
  age?: number
  remark?: string
  created_at?: string
  updated_at?: string
  roles?: RoleBrief[]
  posts?: PostBrief[]
}

// ==================== 角色管理 ====================

/** 角色创建请求。对应后端 RoleCreateRequest。 */
export interface RoleCreateRequest {
  code: string
  name: string
  description?: string
  enabled?: boolean
  sort?: number
  remark?: string
}

/** 角色更新请求。对应后端 RoleUpdateRequest（部分更新）。 */
export interface RoleUpdateRequest {
  name?: string
  description?: string
  enabled?: boolean
  sort?: number
  remark?: string
}

/** 角色分页查询请求。对应后端 RoleQueryRequest（@ModelAttribute，camelCase）。 */
export interface RoleQueryRequest {
  keyword?: string
  enabled?: boolean
  page?: number
  size?: number
}

/** 角色响应。对应后端 RoleResponse。 */
export interface RoleResponse {
  id: number
  code: string
  name: string
  description?: string
  enabled: boolean
  sort: number
  remark?: string
  created_at?: string
  updated_at?: string
}

// ==================== 岗位管理 ====================

/** 岗位响应。对应后端 PostResponse。 */
export interface PostResponse {
  id: number
  code: string
  name: string
  description?: string
  enabled: boolean
  sort?: number
  role_id?: number
  role_name?: string
  remark?: string
  created_at?: string
  updated_at?: string
}

// Re-export PageResponse for convenient access within admin views
export type { PageResponse }
