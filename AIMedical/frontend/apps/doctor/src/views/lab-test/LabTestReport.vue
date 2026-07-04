<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <h2>AI 检验报告</h2>
      </template>

      <AiDegradedBanner :visible="degraded" :reason="fallbackReason" />

      <el-form :model="form" label-position="top" class="report-form">
        <el-form-item label="检验单 ID">
          <el-input
            v-model="form.labTestId"
            type="number"
            placeholder="请输入检验单ID"
            clearable
            @keyup.enter="handleGenerate"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleGenerate">生成 AI 检验报告</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="router.back()">返回</el-button>
        </el-form-item>
      </el-form>

      <el-divider v-if="report" />

      <div v-if="report" class="result-section">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="检验单ID">{{ report.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(report.status)">{{ statusLabel(report.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="检验类型">{{ report.test_type }}</el-descriptions-item>
          <el-descriptions-item label="样本类型">{{ sampleTypeLabel(report.sample_type) }}</el-descriptions-item>
          <el-descriptions-item label="报告时间">{{ formatDateTime(report.reported_at) }}</el-descriptions-item>
          <el-descriptions-item label="报告结论" :span="2">{{ report.report_conclusion || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div class="interpretation-block">
          <div class="interpretation-title">AI 解读</div>
          <div v-if="report.ai_interpretation" class="interpretation-content">{{ report.ai_interpretation }}</div>
          <el-empty v-else description="AI 解读为空，请结合检验结果人工判读" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type { LabTestResponse, LabTestStatus, SampleType } from '@aimedical/shared'
import AiDegradedBanner from '../../components/AiDegradedBanner.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const degraded = ref(false)
const fallbackReason = ref<string | null>(null)
const report = ref<LabTestResponse | null>(null)

const form = reactive<{ labTestId: string }>({
  labTestId: '',
})

onMounted(() => {
  // 支持从详情页跳转时携带 id 预填
  const qId = route.query.id
  if (qId) form.labTestId = String(qId)
})

const formatDateTime = (iso: string | null): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

const sampleTypeMap: Record<SampleType, string> = {
  BLOOD: '血液',
  SERUM: '血清',
  PLASMA: '血浆',
  URINE: '尿液',
  STOOL: '粪便',
  SPUTUM: '痰液',
  OTHER: '其他',
}
const sampleTypeLabel = (t: SampleType): string => sampleTypeMap[t] || t

const statusLabelMap: Record<LabTestStatus, string> = {
  PENDING: '待采样',
  COLLECTED: '已采样',
  IN_PROGRESS: '检验中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}
const statusLabel = (s: LabTestStatus): string => statusLabelMap[s] || s

const statusTagTypeMap: Record<LabTestStatus, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
  PENDING: 'info',
  COLLECTED: 'warning',
  IN_PROGRESS: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'danger',
}
const statusTagType = (s: LabTestStatus): 'primary' | 'success' | 'info' | 'warning' | 'danger' =>
  statusTagTypeMap[s] || 'info'

async function handleGenerate() {
  const id = Number(form.labTestId)
  if (!form.labTestId || Number.isNaN(id)) {
    ElMessage.warning('请输入有效的检验单ID')
    return
  }
  loading.value = true
  degraded.value = false
  fallbackReason.value = null
  report.value = null
  try {
    const res = await doctorApi.generateLabTestAiReport(id)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      return
    }
    report.value = res
    if (!res.ai_interpretation) {
      degraded.value = true
      fallbackReason.value = 'AI 服务暂不可用，未能生成检验解读，请结合检验结果人工判读。'
      ElMessage.warning('AI 解读生成失败，已降级处理')
    } else {
      ElMessage.success('AI 检验报告已生成')
    }
  } finally {
    loading.value = false
  }
}

function handleReset() {
  form.labTestId = ''
  report.value = null
  degraded.value = false
  fallbackReason.value = null
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.report-form {
  max-width: 500px;
}

.result-section {
  margin-top: 16px;
}

.interpretation-block {
  margin-top: 20px;
}

.interpretation-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.interpretation-content {
  background: #f5f7fa;
  border-left: 3px solid #409eff;
  padding: 12px 16px;
  border-radius: 4px;
  white-space: pre-wrap;
  line-height: 1.7;
}
</style>
