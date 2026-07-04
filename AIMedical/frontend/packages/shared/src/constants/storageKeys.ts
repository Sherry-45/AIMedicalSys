/**
 * sessionStorage 键名常量。
 *
 * 用于跨页面在 sessionStorage 中传递临时载荷（如长文本草稿、AI 请求快照），
 * 避免在 URL query 中静默截断。约定：目标页读取后立即 removeItem 清除。
 *
 * 注意：与 localStorage 中存储的 Token（见 utils/index.ts）区分：
 * - localStorage：持久化数据（access/refresh token）
 * - sessionStorage：单次会话内的临时载荷
 */

/** 病情录入草稿快照键。ConditionEntry 写入，AiDiagnosis / AiMedicalRecordGen / MedicalRecordForm 读取后清除。 */
export const STORAGE_KEY_CONDITION_ENTRY_DRAFT = 'condition_entry_draft'

/** AI 病历生成请求载荷键。AiMedicalRecordGen 写入，MedicalRecordForm 读取后清除。 */
export const STORAGE_KEY_AI_MEDICAL_RECORD_GEN = 'ai_medical_record_gen'

/**
 * 与病情录入流转相关的全部 sessionStorage 键，便于在批量清理场景中遍历。
 */
export const CONDITION_FLOW_STORAGE_KEYS: readonly string[] = [
  STORAGE_KEY_CONDITION_ENTRY_DRAFT,
  STORAGE_KEY_AI_MEDICAL_RECORD_GEN,
] as const
