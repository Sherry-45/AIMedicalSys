<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <h2>AI 影像分析</h2>
      </template>

      <AiDegradedBanner :visible="degraded" :reason="fallbackReason" />

      <el-form :model="form" label-position="top" class="ai-form">
        <el-form-item label="检查单 ID">
          <el-input
            v-model="form.examId"
            placeholder="请输入检查单ID（需含影像）"
            clearable
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            AI 影像分析
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="router.back()">返回</el-button>
        </el-form-item>
      </el-form>

      <el-divider v-if="result" />

      <div v-if="result" class="result-section">
        <h3>AI 影像分析结果</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="检查单ID">{{ result.id }}</el-descriptions-item>
          <el-descriptions-item label="检查类型">{{ typeLabel(result.examination_type) }}</el-descriptions-item>
          <el-descriptions-item v-if="result.image_url" label="影像地址">
            <el-link type="primary" :href="result.image_url" target="_blank" :underline="false">
              {{ result.image_url }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="影像分析结果">
            <div v-if="result.image_analysis_result" class="ai-text">
              {{ result.image_analysis_result }}
            </div>
            <span v-else class="empty-text">暂无分析结果</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="result.image_confidence != null" label="置信度">
            {{ (result.image_confidence * 100).toFixed(1) }}%
          </el-descriptions-item>
        </el-descriptions>
        <p v-if="degraded" class="degraded-note">
          注：AI 服务当前不可用，未能生成影像分析结果。请稍后重试或结合临床判断人工阅片。
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type { ExaminationResponse, ExaminationType } from '@aimedical/shared'
import AiDegradedBanner from '../../components/AiDegradedBanner.vue'

const router = useRouter()

const loading = ref(false)
const degraded = ref(false)
const fallbackReason = ref<string | null>(null)
const result = ref<ExaminationResponse | null>(null)

const form = reactive({
  examId: '' as string,
})

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

async function handleSubmit() {
  const id = Number(form.examId)
  if (!form.examId.trim() || Number.isNaN(id)) {
    ElMessage.warning('请输入有效的检查单ID')
    return
  }
  loading.value = true
  degraded.value = false
  fallbackReason.value = null
  result.value = null

  const res = await doctorApi.analyzeExaminationImage(id)
  loading.value = false

  if (isBusinessError(res)) {
    ElMessage.error(res.message)
    return
  }

  result.value = res
  // 该接口非 AiResultResponse 包装，后端降级时 image_analysis_result 为 null
  if (!res.image_analysis_result) {
    degraded.value = true
    fallbackReason.value = 'AI 服务暂不可用，未能生成影像分析结果。'
    ElMessage.warning('AI 不可用，未生成影像分析结果')
  } else {
    ElMessage.success('AI 影像分析完成')
  }
}

function handleReset() {
  form.examId = ''
  result.value = null
  degraded.value = false
  fallbackReason.value = null
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.ai-form {
  max-width: 500px;
}

.result-section {
  margin-top: 16px;
}

.ai-text {
  white-space: pre-wrap;
  line-height: 1.6;
}

.empty-text {
  color: #999;
}

.degraded-note {
  margin-top: 12px;
  color: #92400e;
  font-size: 13px;
}
</style>
