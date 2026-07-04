import type {
  ConsultationStatus,
  DeviceStatus,
  ExaminationStatus,
  ExaminationType,
  LabTestStatus,
  MedicalOrderStatus,
  MedicalOrderType,
  PrescriptionStatus,
} from '../types'

/**
 * Element Plus Tag 组件支持的类型。
 */
export type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'

// ==================== 处方状态 ====================

export const PRESCRIPTION_STATUS_LABELS: Record<PrescriptionStatus, string> = {
  DRAFT: '草稿',
  PENDING_REVIEW: '待审',
  APPROVED: '已通过',
  REJECTED: '已驳回',
}

export const PRESCRIPTION_STATUS_TAG_TYPES: Record<PrescriptionStatus, TagType> = {
  DRAFT: 'info',
  PENDING_REVIEW: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
}

// ==================== 检查状态与类型 ====================

export const EXAMINATION_STATUS_LABELS: Record<ExaminationStatus, string> = {
  PENDING: '待处理',
  SCHEDULED: '已预约',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

export const EXAMINATION_STATUS_TAG_TYPES: Record<ExaminationStatus, TagType> = {
  PENDING: 'info',
  SCHEDULED: 'warning',
  IN_PROGRESS: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'danger',
}

export const EXAMINATION_TYPE_LABELS: Record<ExaminationType, string> = {
  CT: 'CT',
  MRI: '核磁共振',
  X_RAY: 'X光',
  ULTRASOUND: '超声',
  MAMMOGRAPHY: '乳腺钼靶',
  ENDOSCOPY: '内镜',
  OTHER: '其他',
}

// ==================== 检验状态 ====================

export const LAB_TEST_STATUS_LABELS: Record<LabTestStatus, string> = {
  PENDING: '待采样',
  COLLECTED: '已采样',
  IN_PROGRESS: '检验中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

export const LAB_TEST_STATUS_TAG_TYPES: Record<LabTestStatus, TagType> = {
  PENDING: 'info',
  COLLECTED: 'warning',
  IN_PROGRESS: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'danger',
}

// ==================== 医嘱状态与类型 ====================

export const MEDICAL_ORDER_STATUS_LABELS: Record<MedicalOrderStatus, string> = {
  DRAFT: '草稿',
  SUBMITTED: '已提交',
  CHARGED: '已收费',
  DISPENSED: '已发药',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

export const MEDICAL_ORDER_STATUS_TAG_TYPES: Record<MedicalOrderStatus, TagType> = {
  DRAFT: 'info',
  SUBMITTED: 'primary',
  CHARGED: 'warning',
  DISPENSED: 'warning',
  COMPLETED: 'success',
  CANCELLED: 'info',
}

export const MEDICAL_ORDER_TYPE_LABELS: Record<MedicalOrderType, string> = {
  DRUG: '药品',
  EXAMINATION: '检查',
  LAB_TEST: '检验',
}

export const MEDICAL_ORDER_TYPE_TAG_TYPES: Record<MedicalOrderType, TagType> = {
  DRUG: 'success',
  EXAMINATION: 'primary',
  LAB_TEST: 'warning',
}

// ==================== 叫号状态 ====================

export const CONSULTATION_STATUS_LABELS: Record<ConsultationStatus, string> = {
  WAITING: '候诊',
  CALLED: '已叫号',
  IN_CONSULTATION: '接诊中',
  FINISHED: '已完成',
  SKIPPED: '已过号',
}

export const CONSULTATION_STATUS_TAG_TYPES: Record<ConsultationStatus, TagType> = {
  WAITING: 'info',
  CALLED: 'warning',
  IN_CONSULTATION: 'danger',
  FINISHED: 'success',
  SKIPPED: 'info',
}

// ==================== 设备状态 ====================

export const DEVICE_STATUS_LABELS: Record<DeviceStatus, string> = {
  ONLINE: '在线',
  OFFLINE: '离线',
  ERROR: '故障',
  MAINTENANCE: '维护中',
}

export const DEVICE_STATUS_TAG_TYPES: Record<DeviceStatus, TagType> = {
  ONLINE: 'success',
  OFFLINE: 'info',
  ERROR: 'danger',
  MAINTENANCE: 'warning',
}

// ==================== 通用工具函数 ====================

/**
 * 通用状态文案查找函数。给定标签映射表和状态值，返回对应中文文案；未命中时回退到原值。
 */
export function labelOf<T extends string>(
  map: Record<T, string>,
  value: T | string,
): string {
  return (map as Record<string, string>)[value] ?? value
}

/**
 * 通用 Tag 类型查找函数。给定 Tag 类型映射表和状态值，返回对应 Element Plus Tag 类型；未命中时回退到 'info'。
 */
export function tagTypeOf<T extends string>(
  map: Record<T, TagType>,
  value: T | string,
): TagType {
  return (map as Record<string, TagType>)[value] ?? 'info'
}
