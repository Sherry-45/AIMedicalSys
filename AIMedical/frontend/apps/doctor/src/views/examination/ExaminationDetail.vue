<template>
  <div class="page-container" v-loading="loading">
    <div class="top-bar">
      <el-button text @click="router.back()">← 返回</el-button>
    </div>

    <el-card v-if="exam">
      <template #header>
        <div class="card-header">
          <h2>检查单 #{{ exam.id }}</h2>
          <div class="header-tags">
            <el-tag :type="statusTagType(exam.status)" size="large">
              {{ statusLabel(exam.status) }}
            </el-tag>
            <el-tag v-if="exam.emergency_flag" type="danger" size="large">急诊</el-tag>
            <el-tag type="info" size="large">{{ typeLabel(exam.examination_type) }}</el-tag>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="患者ID">
          <el-link type="primary" :underline="false" @click="goPatient(exam.patient_id)">
            {{ exam.patient_id }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="开单医生">{{ exam.doctor_id }}</el-descriptions-item>
        <el-descriptions-item label="检查部位">{{ exam.body_part || '—' }}</el-descriptions-item>
        <el-descriptions-item label="临床诊断">{{ exam.clinical_diagnosis || '—' }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">{{ formatDateTime(exam.scheduled_at) }}</el-descriptions-item>
        <el-descriptions-item label="报告时间">{{ formatDateTime(exam.reported_at) }}</el-descriptions-item>
        <el-descriptions-item v-if="exam.image_url" label="影像地址" :span="2">
          <el-link type="primary" :href="exam.image_url" target="_blank" :underline="false">
            {{ exam.image_url }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item v-if="exam.image_type" label="影像格式">{{ exam.image_type }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(exam.created_at) }}</el-descriptions-item>
        <el-descriptions-item v-if="exam.impression" label="印象" :span="2">{{ exam.impression }}</el-descriptions-item>
        <el-descriptions-item v-if="exam.conclusion" label="结论" :span="2">{{ exam.conclusion }}</el-descriptions-item>
      </el-descriptions>

      <!-- 检查明细 -->
      <div class="section">
        <h3 class="section-title">检查明细</h3>
        <el-table :data="exam.items" border style="width: 100%">
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column prop="item_name" label="项目名称" min-width="140" />
          <el-table-column label="所见" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ row.finding || '—' }}</template>
          </el-table-column>
          <el-table-column label="测量值" width="120">
            <template #default="{ row }">{{ row.measurement || '—' }}</template>
          </el-table-column>
          <el-table-column label="异常" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.abnormal_flag" type="danger" size="small">异常</el-tag>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无检查明细" :image-size="60" />
          </template>
        </el-table>
      </div>

      <!-- AI 检查报告解读 -->
      <div v-if="exam.ai_interpretation" class="section">
        <h3 class="section-title">AI 检查报告解读</h3>
        <el-alert type="info" :closable="false" class="ai-block">
          <template #title>
            <div class="ai-text">{{ exam.ai_interpretation }}</div>
            <div v-if="exam.ai_confidence != null" class="ai-confidence">
              置信度：{{ (exam.ai_confidence * 100).toFixed(1) }}%
            </div>
          </template>
        </el-alert>
      </div>

      <!-- AI 影像分析结果 -->
      <div v-if="exam.image_analysis_result" class="section">
        <h3 class="section-title">AI 影像分析结果</h3>
        <el-alert type="info" :closable="false" class="ai-block">
          <template #title>
            <div class="ai-text">{{ exam.image_analysis_result }}</div>
            <div v-if="exam.image_confidence != null" class="ai-confidence">
              置信度：{{ (exam.image_confidence * 100).toFixed(1) }}%
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 操作区 -->
      <div class="actions">
        <el-button
          v-if="exam.status === 'PENDING'"
          type="warning"
          :loading="scheduling"
          @click="openScheduleDialog"
        >预约</el-button>
        <el-button
          v-if="['PENDING', 'SCHEDULED'].includes(exam.status)"
          type="primary"
          :loading="starting"
          @click="handleStart"
        >开始检查</el-button>
        <el-button
          v-if="exam.status === 'IN_PROGRESS'"
          type="success"
          :loading="completing"
          @click="openCompleteDialog"
        >完成检查</el-button>
        <el-button
          v-if="['PENDING', 'SCHEDULED'].includes(exam.status)"
          type="danger"
          :loading="cancelling"
          @click="handleCancel"
        >取消</el-button>
        <el-button
          v-if="exam.status === 'COMPLETED'"
          type="primary"
          :loading="generatingReport"
          @click="handleGenerateReport"
        >生成 AI 报告</el-button>
        <el-button
          v-if="exam.status === 'COMPLETED' && canAnalyzeImage"
          type="primary"
          :loading="analyzingImage"
          @click="handleAnalyzeImage"
        >AI 影像分析</el-button>
      </div>
    </el-card>

    <!-- 预约对话框 -->
    <el-dialog
      v-model="scheduleDialogVisible"
      title="预约检查时间"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px">
        <el-form-item label="预约时间">
          <el-date-picker
            v-model="scheduleAt"
            type="datetime"
            placeholder="选择预约时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="scheduling" @click="handleSchedule">确认预约</el-button>
      </template>
    </el-dialog>

    <!-- 完成检查对话框 -->
    <el-dialog
      v-model="completeDialogVisible"
      title="完成检查"
      width="720px"
      :close-on-click-modal="false"
    >
      <el-form label-position="top">
        <el-form-item label="印象 (Impression)">
          <el-input
            v-model="completeForm.impression"
            type="textarea"
            :rows="2"
            placeholder="检查印象"
          />
        </el-form-item>
        <el-form-item label="结论 (Conclusion)">
          <el-input
            v-model="completeForm.conclusion"
            type="textarea"
            :rows="2"
            placeholder="检查结论"
          />
        </el-form-item>
        <el-form-item label="检查明细">
          <div class="items-editor">
            <div
              v-for="(item, idx) in completeForm.items"
              :key="idx"
              class="item-row"
            >
              <el-input v-model="item.item_name" placeholder="项目名称" style="width: 160px" />
              <el-input v-model="item.finding" placeholder="所见" style="flex: 1" />
              <el-input v-model="item.measurement" placeholder="测量值" style="width: 120px" />
              <el-checkbox v-model="item.abnormal_flag">异常</el-checkbox>
              <el-button type="danger" link @click="removeItem(idx)">删除</el-button>
            </div>
            <el-button type="primary" link @click="addItem">+ 添加项目</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="completing" @click="handleComplete">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type {
  ExaminationCompleteRequest,
  ExaminationItemRequest,
  ExaminationResponse,
  ExaminationStatus,
  ExaminationType,
} from '@aimedical/shared'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const exam = ref<ExaminationResponse | null>(null)

const scheduling = ref(false)
const starting = ref(false)
const completing = ref(false)
const cancelling = ref(false)
const generatingReport = ref(false)
const analyzingImage = ref(false)

const scheduleDialogVisible = ref(false)
const scheduleAt = ref<string>('')

const completeDialogVisible = ref(false)
const completeForm = reactive<{
  impression: string
  conclusion: string
  items: ExaminationItemRequest[]
}>({
  impression: '',
  conclusion: '',
  items: [],
})

const examId = computed(() => Number(route.params.id))

// 仅影像类型（非 OTHER）且有 imageUrl 时允许 AI 影像分析
const canAnalyzeImage = computed(
  () =>
    !!exam.value &&
    exam.value.examination_type !== 'OTHER' &&
    !!exam.value.image_url,
)

const formatDateTime = (iso: string | null): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

const typeLabel = (type: ExaminationType): string => {
  const map: Record<ExaminationType, string> = {
    CT: 'CT',
    MRI: '核磁共振',
    X_RAY: 'X光',
    ULTRASOUND: '超声',
    MAMMOGRAPHY: '乳腺钼靶',
    ENDOSCOPY: '内镜',
    OTHER: '其他',
  }
  return map[type] || type
}

const statusLabel = (status: ExaminationStatus): string => {
  const map: Record<ExaminationStatus, string> = {
    PENDING: '待处理',
    SCHEDULED: '已预约',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  return map[status] || status
}

const statusTagType = (
  status: ExaminationStatus,
): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<
    ExaminationStatus,
    'primary' | 'success' | 'info' | 'warning' | 'danger'
  > = {
    PENDING: 'info',
    SCHEDULED: 'warning',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'danger',
  }
  return map[status] || 'info'
}

function goPatient(patientId: number) {
  router.push(`/patient/${patientId}`)
}

async function loadExam() {
  loading.value = true
  try {
    const result = await doctorApi.getExamination(examId.value)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      router.replace('/examinations')
      return
    }
    exam.value = result
  } finally {
    loading.value = false
  }
}

function openScheduleDialog() {
  scheduleAt.value = ''
  scheduleDialogVisible.value = true
}

async function handleSchedule() {
  if (!scheduleAt.value) {
    ElMessage.warning('请选择预约时间')
    return
  }
  scheduling.value = true
  try {
    const result = await doctorApi.scheduleExamination(examId.value, scheduleAt.value)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('预约成功')
    exam.value = result
    scheduleDialogVisible.value = false
  } finally {
    scheduling.value = false
  }
}

async function handleStart() {
  starting.value = true
  try {
    const result = await doctorApi.startExamination(examId.value)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('已开始检查')
    exam.value = result
  } finally {
    starting.value = false
  }
}

function openCompleteDialog() {
  completeForm.impression = exam.value?.impression || ''
  completeForm.conclusion = exam.value?.conclusion || ''
  completeForm.items = (exam.value?.items || []).map((it) => ({
    item_name: it.item_name,
    finding: it.finding || '',
    measurement: it.measurement || '',
    abnormal_flag: it.abnormal_flag,
  }))
  if (completeForm.items.length === 0) {
    completeForm.items.push({
      item_name: '',
      finding: '',
      measurement: '',
      abnormal_flag: false,
    })
  }
  completeDialogVisible.value = true
}

function addItem() {
  completeForm.items.push({
    item_name: '',
    finding: '',
    measurement: '',
    abnormal_flag: false,
  })
}

function removeItem(idx: number) {
  completeForm.items.splice(idx, 1)
}

async function handleComplete() {
  const items: ExaminationItemRequest[] = completeForm.items
    .filter((it) => it.item_name.trim())
    .map((it) => ({
      item_name: it.item_name.trim(),
      finding: it.finding?.trim() || undefined,
      measurement: it.measurement?.trim() || undefined,
      abnormal_flag: it.abnormal_flag,
    }))
  const request: ExaminationCompleteRequest = {
    impression: completeForm.impression.trim() || undefined,
    conclusion: completeForm.conclusion.trim() || undefined,
    items,
  }
  completing.value = true
  try {
    const result = await doctorApi.completeExamination(examId.value, request)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('检查已完成')
    exam.value = result
    completeDialogVisible.value = false
  } finally {
    completing.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确认取消该检查单？取消后无法恢复。', '取消检查', {
      type: 'warning',
    })
  } catch {
    return
  }
  cancelling.value = true
  try {
    const result = await doctorApi.cancelExamination(examId.value)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('已取消')
    exam.value = result
  } finally {
    cancelling.value = false
  }
}

async function handleGenerateReport() {
  generatingReport.value = true
  try {
    const result = await doctorApi.generateExaminationAiReport(examId.value)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    // 后端降级时 ai_interpretation 为 null
    if (!result.ai_interpretation) {
      ElMessage.warning('AI 服务暂不可用，未能生成报告，请稍后重试')
      exam.value = result
      return
    }
    ElMessage.success('AI 报告已生成')
    exam.value = result
  } finally {
    generatingReport.value = false
  }
}

async function handleAnalyzeImage() {
  analyzingImage.value = true
  try {
    const result = await doctorApi.analyzeExaminationImage(examId.value)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    // 后端降级时 image_analysis_result 为 null
    if (!result.image_analysis_result) {
      ElMessage.warning('AI 影像分析暂不可用，未能生成结果，请稍后重试')
      exam.value = result
      return
    }
    ElMessage.success('AI 影像分析完成')
    exam.value = result
  } finally {
    analyzingImage.value = false
  }
}

onMounted(() => {
  if (!examId.value || Number.isNaN(examId.value)) {
    ElMessage.error('检查单ID无效')
    router.replace('/examinations')
    return
  }
  loadExam()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.top-bar {
  margin-bottom: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}

.header-tags {
  display: flex;
  gap: 8px;
}

.section {
  margin-top: 24px;
}

.section-title {
  font-size: 15px;
  margin: 0 0 12px 0;
  color: #303133;
  border-left: 3px solid #409eff;
  padding-left: 8px;
}

.text-muted {
  color: #c0c4cc;
}

.actions {
  margin-top: 24px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.ai-block {
  white-space: pre-wrap;
}

.ai-text {
  font-size: 14px;
  line-height: 1.6;
}

.ai-confidence {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.items-editor {
  width: 100%;
}

.item-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
</style>
