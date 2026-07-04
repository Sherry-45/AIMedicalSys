<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <h2>AI 检查执行顺序推荐</h2>
      </template>

      <AiDegradedBanner :visible="degraded" :reason="fallbackReason" />

      <el-form :model="form" label-position="top" class="ai-form">
        <el-form-item label="患者 ID">
          <el-input
            v-model="form.patientId"
            placeholder="请输入患者ID"
            clearable
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            推荐执行顺序
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="router.back()">返回</el-button>
        </el-form-item>
      </el-form>

      <el-divider v-if="result" />

      <div v-if="result" class="result-section">
        <h3>推荐执行顺序</h3>
        <el-table :data="result.execution_order" border stripe>
          <el-table-column prop="task_id" label="任务ID" width="100" align="center" />
          <el-table-column label="优先级" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="priorityTagType(row.priority)">{{ row.priority }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="建议时间" width="180">
            <template #default="{ row }">{{ formatDateTime(row.recommended_time) }}</template>
          </el-table-column>
          <el-table-column prop="reason" label="理由" min-width="260" show-overflow-tooltip />
          <template #empty>
            <el-empty description="暂无推荐任务" :image-size="60" />
          </template>
        </el-table>

        <div v-if="result.summary" class="summary-block">
          <h4>汇总</h4>
          <p class="summary-text">{{ result.summary }}</p>
        </div>

        <el-alert
          v-if="result.disclaimer_required"
          type="warning"
          :closable="false"
          show-icon
          class="disclaimer-alert"
        >
          <template #title>免责声明</template>
          以上推荐结果由 AI 生成，仅供参考，不能替代医生临床判断。请结合患者实际情况决定最终执行顺序。
        </el-alert>

        <p v-if="degraded" class="degraded-note">
          注：AI 服务当前不可用，以上为降级兜底建议，请医生根据临床需要人工排序。
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
import type { ExecutionOrderResponse } from '@aimedical/shared'
import AiDegradedBanner from '../../components/AiDegradedBanner.vue'

const router = useRouter()

const loading = ref(false)
const degraded = ref(false)
const fallbackReason = ref<string | null>(null)
const result = ref<ExecutionOrderResponse | null>(null)

const form = reactive({
  patientId: '' as string,
})

const formatDateTime = (iso: string | null | undefined): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

const priorityTagType = (
  priority: string,
): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<string, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    HIGH: 'danger',
    MEDIUM: 'warning',
    LOW: 'info',
    URGENT: 'danger',
    NORMAL: 'primary',
    ROUTINE: 'info',
  }
  return map[priority] || 'info'
}

async function handleSubmit() {
  const pid = Number(form.patientId)
  if (!form.patientId.trim() || Number.isNaN(pid)) {
    ElMessage.warning('请输入有效的患者ID')
    return
  }
  loading.value = true
  degraded.value = false
  fallbackReason.value = null
  result.value = null

  const res = await doctorApi.recommendExaminationOrder(pid)
  loading.value = false

  if (isBusinessError(res)) {
    ElMessage.error(res.message)
    return
  }

  result.value = res
  degraded.value = res.degraded
  if (res.degraded) {
    fallbackReason.value = 'AI 服务暂不可用，已返回降级兜底建议。'
    ElMessage.warning('AI 不可用，已返回降级兜底建议')
  } else {
    ElMessage.success('执行顺序推荐已生成')
  }
}

function handleReset() {
  form.patientId = ''
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

.summary-block {
  margin-top: 20px;
}

.summary-block h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #303133;
}

.summary-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.disclaimer-alert {
  margin-top: 16px;
}

.degraded-note {
  margin-top: 12px;
  color: #92400e;
  font-size: 13px;
}
</style>
