import type { BusinessError } from '../types'
import { apiGet, apiPost, apiPut } from './client'

import type {
  AiDiagnosisRequest,
  AiDiagnosisResponse,
  AiDiscussionConclusionRequest,
  AiDiscussionConclusionResponse,
  AiExaminationRequest,
  AiExaminationResponse,
  AiMedicalRecordGenRequest,
  AiMedicalRecordGenResponse,
  AiPrescriptionAssistRequest,
  AiPrescriptionAssistResponse,
  AiPrescriptionAuditRequest,
  AiPrescriptionAuditResponse,
  AiResultResponse,
  ChargePreOrderDTO,
  ConsultationQueueResponse,
  DoctorDto,
  DoctorProfileUpdateRequest,
  ExaminationCompleteRequest,
  ExaminationCreateRequest,
  ExaminationResponse,
  ExecutionOrderResponse,
  LabTestCompleteRequest,
  LabTestCreateRequest,
  LabTestResponse,
  LabTestTrendResponse,
  MedicalOrderCreateRequest,
  MedicalOrderDTO,
  MedicalRecordCreateRequest,
  MedicalRecordResponse,
  MedicalRecordTemplateResponse,
  MedicationOrderDTO,
  PageResponse,
  PrescriptionAuditRequest,
  PrescriptionCreateRequest,
  PrescriptionResponse,
} from '../types'

/**
 * 医生端 API
 *
 * <p>对应后端 /api/doctor/* 系列接口。所有方法返回 Promise<T | BusinessError>，
 * 调用方需用 isBusinessError() 判断结果。
 *
 * <p>注意：AI 相关接口返回的是 AiResultResponse<T>（已被响应拦截器解包外层 Result），
 * 其内部的 success/degraded 字段标识 AI 是否可用，前端据此展示降级 UI。
 */
export const doctorApi = {
  // ---- 挂号/叫号队列 ----

  /** 查询当前医生的活跃队列（候诊+已叫号+接诊中）。GET /api/doctor/queue */
  listMyQueue: (): Promise<ConsultationQueueResponse[] | BusinessError> => {
    return apiGet<ConsultationQueueResponse[]>('/doctor/queue')
  },

  /** 查询当前医生的候诊队列（仅 WAITING）。GET /api/doctor/queue/waiting */
  listWaiting: (): Promise<ConsultationQueueResponse[] | BusinessError> => {
    return apiGet<ConsultationQueueResponse[]>('/doctor/queue/waiting')
  },

  /** 叫下一位患者（WAITING -> CALLED）。POST /api/doctor/queue/call-next */
  callNext: (): Promise<ConsultationQueueResponse | BusinessError> => {
    return apiPost<ConsultationQueueResponse>('/doctor/queue/call-next')
  },

  /** 开始接诊（CALLED -> IN_CONSULTATION）。POST /api/doctor/queue/{id}/start */
  startConsultation: (id: number): Promise<ConsultationQueueResponse | BusinessError> => {
    return apiPost<ConsultationQueueResponse>(`/doctor/queue/${id}/start`)
  },

  /** 完成接诊（IN_CONSULTATION -> FINISHED）。POST /api/doctor/queue/{id}/finish */
  finishConsultation: (id: number): Promise<ConsultationQueueResponse | BusinessError> => {
    return apiPost<ConsultationQueueResponse>(`/doctor/queue/${id}/finish`)
  },

  /** 过号（WAITING/CALLED -> SKIPPED）。POST /api/doctor/queue/{id}/skip */
  skipQueue: (id: number): Promise<ConsultationQueueResponse | BusinessError> => {
    return apiPost<ConsultationQueueResponse>(`/doctor/queue/${id}/skip`)
  },

  // ---- 处方中心 ----

  /** 创建处方（草稿或直接提交审核）。POST /api/doctor/prescriptions */
  createPrescription: (
    request: PrescriptionCreateRequest,
  ): Promise<PrescriptionResponse | BusinessError> => {
    return apiPost<PrescriptionResponse>('/doctor/prescriptions', request)
  },

  /** 获取处方详情。GET /api/doctor/prescriptions/{id} */
  getPrescription: (id: number): Promise<PrescriptionResponse | BusinessError> => {
    return apiGet<PrescriptionResponse>(`/doctor/prescriptions/${id}`)
  },

  /** 按患者查询处方列表。GET /api/doctor/prescriptions?patientId= */
  listPrescriptionsByPatient: (
    patientId: number,
  ): Promise<PrescriptionResponse[] | BusinessError> => {
    return apiGet<PrescriptionResponse[]>('/doctor/prescriptions', {
      params: { patientId },
    })
  },

  /** 查询当前医生开立的所有处方（"我的处方"）。GET /api/doctor/prescriptions/by-doctor */
  listPrescriptionsByDoctor: (): Promise<PrescriptionResponse[] | BusinessError> => {
    return apiGet<PrescriptionResponse[]>('/doctor/prescriptions/by-doctor')
  },

  /** 提交处方审核（DRAFT/REJECTED -> PENDING_REVIEW）。POST /api/doctor/prescriptions/{id}/submit */
  submitPrescription: (id: number): Promise<PrescriptionResponse | BusinessError> => {
    return apiPost<PrescriptionResponse>(`/doctor/prescriptions/${id}/submit`)
  },

  /** 审核处方（PENDING_REVIEW -> APPROVED/REJECTED）。POST /api/doctor/prescriptions/{id}/audit */
  auditPrescription: (
    id: number,
    request: PrescriptionAuditRequest,
  ): Promise<PrescriptionResponse | BusinessError> => {
    return apiPost<PrescriptionResponse>(`/doctor/prescriptions/${id}/audit`, request)
  },

  // ---- 病历中心 ----

  /** 创建或更新草稿病历（publish=true 时同时发布为正式版本）。POST /api/doctor/medical-records */
  saveMedicalRecord: (
    request: MedicalRecordCreateRequest,
  ): Promise<MedicalRecordResponse | BusinessError> => {
    return apiPost<MedicalRecordResponse>('/doctor/medical-records', request)
  },

  /** 获取病历详情。GET /api/doctor/medical-records/{id} */
  getMedicalRecord: (id: number): Promise<MedicalRecordResponse | BusinessError> => {
    return apiGet<MedicalRecordResponse>(`/doctor/medical-records/${id}`)
  },

  /** 按患者查询病历列表（按版本号倒序）。GET /api/doctor/medical-records?patientId= */
  listMedicalRecordsByPatient: (
    patientId: number,
  ): Promise<MedicalRecordResponse[] | BusinessError> => {
    return apiGet<MedicalRecordResponse[]>('/doctor/medical-records', {
      params: { patientId },
    })
  },

  /** 将草稿病历发布为正式版本。POST /api/doctor/medical-records/{id}/publish */
  publishMedicalRecord: (id: number): Promise<MedicalRecordResponse | BusinessError> => {
    return apiPost<MedicalRecordResponse>(`/doctor/medical-records/${id}/publish`)
  },

  /** 按科室查询启用的病历模板列表。GET /api/doctor/medical-records/templates?department= */
  listMedicalRecordTemplates: (
    department?: string,
  ): Promise<MedicalRecordTemplateResponse[] | BusinessError> => {
    return apiGet<MedicalRecordTemplateResponse[]>('/doctor/medical-records/templates', {
      params: { department },
    })
  },

  // ---- AI 入口（全部返回 AiResultResponse<T>，degraded=true 时需展示降级 UI） ----

  /** 占位诊断入口。POST /api/doctor/ai/diagnosis */
  aiDiagnosis: (
    request: AiDiagnosisRequest,
  ): Promise<AiResultResponse<AiDiagnosisResponse> | BusinessError> => {
    return apiPost<AiResultResponse<AiDiagnosisResponse>>('/doctor/ai/diagnosis', request)
  },

  /** 开立检查推荐入口。POST /api/doctor/ai/examination */
  aiExamination: (
    request: AiExaminationRequest,
  ): Promise<AiResultResponse<AiExaminationResponse> | BusinessError> => {
    return apiPost<AiResultResponse<AiExaminationResponse>>('/doctor/ai/examination', request)
  },

  /** 辅助开方入口。POST /api/doctor/ai/prescription-assist */
  aiPrescriptionAssist: (
    request: AiPrescriptionAssistRequest,
  ): Promise<AiResultResponse<AiPrescriptionAssistResponse> | BusinessError> => {
    return apiPost<AiResultResponse<AiPrescriptionAssistResponse>>(
      '/doctor/ai/prescription-assist',
      request,
    )
  },

  /** 处方审核入口。POST /api/doctor/ai/prescription-audit */
  aiPrescriptionAudit: (
    request: AiPrescriptionAuditRequest,
  ): Promise<AiResultResponse<AiPrescriptionAuditResponse> | BusinessError> => {
    return apiPost<AiResultResponse<AiPrescriptionAuditResponse>>(
      '/doctor/ai/prescription-audit',
      request,
    )
  },

  /** 病历生成入口。POST /api/doctor/ai/medical-record-gen */
  aiMedicalRecordGen: (
    request: AiMedicalRecordGenRequest,
  ): Promise<AiResultResponse<AiMedicalRecordGenResponse> | BusinessError> => {
    return apiPost<AiResultResponse<AiMedicalRecordGenResponse>>(
      '/doctor/ai/medical-record-gen',
      request,
    )
  },

  /** 讨论结论生成入口。POST /api/doctor/ai/discussion-conclusion */
  aiDiscussionConclusion: (
    request: AiDiscussionConclusionRequest,
  ): Promise<AiResultResponse<AiDiscussionConclusionResponse> | BusinessError> => {
    return apiPost<AiResultResponse<AiDiscussionConclusionResponse>>(
      '/doctor/ai/discussion-conclusion',
      request,
    )
  },

  // ---- 检查域 (Examination) ----

  /** 创建检查单。POST /api/examinations */
  createExamination: (
    request: ExaminationCreateRequest,
  ): Promise<ExaminationResponse | BusinessError> => {
    return apiPost<ExaminationResponse>('/examinations', request)
  },

  /** 获取检查单详情。GET /api/examinations/{id} */
  getExamination: (id: number): Promise<ExaminationResponse | BusinessError> => {
    return apiGet<ExaminationResponse>(`/examinations/${id}`)
  },

  /** 分页查询检查单列表。GET /api/examinations */
  listExaminations: (
    params?: { patientId?: number; status?: string; page?: number; size?: number },
  ): Promise<PageResponse<ExaminationResponse> | BusinessError> => {
    return apiGet<PageResponse<ExaminationResponse>>('/examinations', { params })
  },

  /** 预约检查。PUT /api/examinations/{id}/schedule */
  scheduleExamination: (
    id: number,
    scheduledAt: string,
  ): Promise<ExaminationResponse | BusinessError> => {
    return apiPut<ExaminationResponse>(`/examinations/${id}/schedule`, null, {
      params: { scheduledAt },
    })
  },

  /** 开始检查。PUT /api/examinations/{id}/start */
  startExamination: (id: number): Promise<ExaminationResponse | BusinessError> => {
    return apiPut<ExaminationResponse>(`/examinations/${id}/start`)
  },

  /** 完成检查。PUT /api/examinations/{id}/complete */
  completeExamination: (
    id: number,
    request: ExaminationCompleteRequest,
  ): Promise<ExaminationResponse | BusinessError> => {
    return apiPut<ExaminationResponse>(`/examinations/${id}/complete`, request)
  },

  /** 取消检查。PUT /api/examinations/{id}/cancel */
  cancelExamination: (id: number): Promise<ExaminationResponse | BusinessError> => {
    return apiPut<ExaminationResponse>(`/examinations/${id}/cancel`)
  },

  /** 生成 AI 检查报告。POST /api/examinations/{id}/ai-report */
  generateExaminationAiReport: (
    id: number,
  ): Promise<ExaminationResponse | BusinessError> => {
    return apiPost<ExaminationResponse>(`/examinations/${id}/ai-report`)
  },

  /** AI 影像分析。POST /api/examinations/{id}/image-analysis */
  analyzeExaminationImage: (
    id: number,
  ): Promise<ExaminationResponse | BusinessError> => {
    return apiPost<ExaminationResponse>(`/examinations/${id}/image-analysis`)
  },

  /** AI 执行顺序推荐。GET /api/examinations/execution-order/{patientId} */
  recommendExaminationOrder: (
    patientId: number,
  ): Promise<ExecutionOrderResponse | BusinessError> => {
    return apiGet<ExecutionOrderResponse>(`/examinations/execution-order/${patientId}`)
  },

  // ---- 检验域 (LabTest) ----

  /** 创建检验单。POST /api/lab-tests */
  createLabTest: (
    request: LabTestCreateRequest,
  ): Promise<LabTestResponse | BusinessError> => {
    return apiPost<LabTestResponse>('/lab-tests', request)
  },

  /** 获取检验单详情。GET /api/lab-tests/{id} */
  getLabTest: (id: number): Promise<LabTestResponse | BusinessError> => {
    return apiGet<LabTestResponse>(`/lab-tests/${id}`)
  },

  /** 分页查询检验单列表。GET /api/lab-tests */
  listLabTests: (
    params?: { patientId?: number; status?: string; page?: number; size?: number },
  ): Promise<PageResponse<LabTestResponse> | BusinessError> => {
    return apiGet<PageResponse<LabTestResponse>>('/lab-tests', { params })
  },

  /** 采样。PUT /api/lab-tests/{id}/collect */
  collectSample: (id: number): Promise<LabTestResponse | BusinessError> => {
    return apiPut<LabTestResponse>(`/lab-tests/${id}/collect`)
  },

  /** 开始检验。PUT /api/lab-tests/{id}/start */
  startLabTest: (id: number): Promise<LabTestResponse | BusinessError> => {
    return apiPut<LabTestResponse>(`/lab-tests/${id}/start`)
  },

  /** 完成检验。PUT /api/lab-tests/{id}/complete */
  completeLabTest: (
    id: number,
    request: LabTestCompleteRequest,
  ): Promise<LabTestResponse | BusinessError> => {
    return apiPut<LabTestResponse>(`/lab-tests/${id}/complete`, request)
  },

  /** 取消检验。PUT /api/lab-tests/{id}/cancel */
  cancelLabTest: (id: number): Promise<LabTestResponse | BusinessError> => {
    return apiPut<LabTestResponse>(`/lab-tests/${id}/cancel`)
  },

  /** 生成 AI 检验报告。POST /api/lab-tests/{id}/ai-report */
  generateLabTestAiReport: (
    id: number,
  ): Promise<LabTestResponse | BusinessError> => {
    return apiPost<LabTestResponse>(`/lab-tests/${id}/ai-report`)
  },

  /** 查询检验趋势图。GET /api/lab-tests/trend/{patientId}?itemName= */
  getLabTestTrend: (
    patientId: number,
    itemName: string,
  ): Promise<LabTestTrendResponse | BusinessError> => {
    return apiGet<LabTestTrendResponse>(`/lab-tests/trend/${patientId}`, {
      params: { itemName },
    })
  },

  /** AI 检验执行顺序推荐。GET /api/lab-tests/execution-order/{patientId} */
  recommendLabTestOrder: (
    patientId: number,
  ): Promise<ExecutionOrderResponse | BusinessError> => {
    return apiGet<ExecutionOrderResponse>(`/lab-tests/execution-order/${patientId}`)
  },

  // ---- 医嘱域 (MedicalOrder) ----

  /** 创建医嘱。POST /api/medical-orders */
  createMedicalOrder: (
    request: MedicalOrderCreateRequest,
  ): Promise<MedicalOrderDTO | BusinessError> => {
    return apiPost<MedicalOrderDTO>('/medical-orders', request)
  },

  /** 获取医嘱详情。GET /api/medical-orders/{id} */
  getMedicalOrder: (id: number): Promise<MedicalOrderDTO | BusinessError> => {
    return apiGet<MedicalOrderDTO>(`/medical-orders/${id}`)
  },

  /** 提交医嘱（DRAFT -> SUBMITTED）。POST /api/medical-orders/{id}/submit */
  submitMedicalOrder: (id: number): Promise<MedicalOrderDTO | BusinessError> => {
    return apiPost<MedicalOrderDTO>(`/medical-orders/${id}/submit`)
  },

  /** 取消医嘱。POST /api/medical-orders/{id}/cancel */
  cancelMedicalOrder: (id: number): Promise<MedicalOrderDTO | BusinessError> => {
    return apiPost<MedicalOrderDTO>(`/medical-orders/${id}/cancel`)
  },

  /** 完成医嘱。POST /api/medical-orders/{id}/complete */
  completeMedicalOrder: (id: number): Promise<MedicalOrderDTO | BusinessError> => {
    return apiPost<MedicalOrderDTO>(`/medical-orders/${id}/complete`)
  },

  /** 生成预收费订单。POST /api/medical-orders/{id}/charge-pre */
  generateChargePreOrder: (
    id: number,
  ): Promise<ChargePreOrderDTO | BusinessError> => {
    return apiPost<ChargePreOrderDTO>(`/medical-orders/${id}/charge-pre`)
  },

  /** 按患者分页查询医嘱。GET /api/medical-orders/patient/{patientId} */
  listMedicalOrdersByPatient: (
    patientId: number,
    params?: { page?: number; size?: number },
  ): Promise<PageResponse<MedicalOrderDTO> | BusinessError> => {
    return apiGet<PageResponse<MedicalOrderDTO>>(
      `/medical-orders/patient/${patientId}`,
      { params },
    )
  },

  /** 按医生分页查询医嘱。GET /api/medical-orders/doctor/{doctorId} */
  listMedicalOrdersByDoctor: (
    doctorId: number,
    params?: { page?: number; size?: number },
  ): Promise<PageResponse<MedicalOrderDTO> | BusinessError> => {
    return apiGet<PageResponse<MedicalOrderDTO>>(
      `/medical-orders/doctor/${doctorId}`,
      { params },
    )
  },

  /** 获取发药合同。GET /api/medical-orders/{id}/medication-contract */
  getMedicationContract: (
    id: number,
  ): Promise<MedicationOrderDTO | BusinessError> => {
    return apiGet<MedicationOrderDTO>(`/medical-orders/${id}/medication-contract`)
  },

  // ---- 医生档案 (Doctor Profile) ----

  /** 查询当前登录医生的档案。GET /api/doctor/profile */
  getDoctorProfile: (): Promise<DoctorDto | BusinessError> => {
    return apiGet<DoctorDto>('/doctor/profile')
  },

  /** 更新当前登录医生的档案（仅可变字段）。PUT /api/doctor/profile */
  updateDoctorProfile: (
    request: DoctorProfileUpdateRequest,
  ): Promise<DoctorDto | BusinessError> => {
    return apiPut<DoctorDto>('/doctor/profile', request)
  },
}
