<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <h2>AI 讨论结论生成</h2>
      </template>

      <AiDegradedBanner :visible="degraded" :reason="fallbackReason" />

      <el-form label-position="top" class="ai-form">
        <div class="transcripts-header">
          <h3>讨论发言记录</h3>
          <el-button type="primary" size="small" @click="addTranscript">
            + 添加发言
          </el-button>
        </div>

        <el-empty
          v-if="!transcripts.length"
          description="暂无发言记录，请点击「添加发言」"
          :image-size="60"
        />

        <div
          v-for="(t, idx) in transcripts"
          :key="idx"
          class="transcript-item"
        >
          <el-row :gutter="12">
            <el-col :span="6">
              <el-form-item label="发言角色">
                <el-select v-model="t.speaker_role" placeholder="选择角色" filterable allow-create>
                  <el-option label="主持人" value="MODERATOR" />
                  <el-option label="医生" value="DOCTOR" />
                  <el-option label="护士" value="NURSE" />
                  <el-option label="专家" value="EXPERT" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="发言人">
                <el-input v-model="t.speaker_name" placeholder="如：张医生" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="时间">
                <el-input v-model="t.timestamp" placeholder="如：10:30" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label=" ">
                <el-button type="danger" size="small" @click="removeTranscript(idx)">
                  删除
                </el-button>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="发言内容">
            <el-input
              v-model="t.content"
              type="textarea"
              :rows="2"
              placeholder="发言内容"
            />
          </el-form-item>
        </div>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            :disabled="!transcripts.length"
            @click="handleSubmit"
          >
            生成讨论结论
          </el-button>
          <el-button @click="handleReset">清空</el-button>
          <el-button @click="router.back()">返回</el-button>
        </el-form-item>
      </el-form>

      <el-divider v-if="result" />

      <div v-if="result" class="result-section">
        <h3>讨论结论</h3>

        <div class="conclusion-block">
          <h4>结论摘要</h4>
          <p class="conclusion-text">{{ result.conclusion_summary || '—' }}</p>
        </div>

        <div v-if="result.key_points" class="conclusion-block">
          <h4>关键要点</h4>
          <p class="conclusion-text">{{ result.key_points }}</p>
        </div>

        <div v-if="result.action_items" class="conclusion-block">
          <h4>后续行动项</h4>
          <p class="conclusion-text">{{ result.action_items }}</p>
        </div>

        <el-alert
          type="info"
          :closable="false"
          show-icon
          class="disclaimer-alert"
        >
          <template #title>免责声明</template>
          以上结论由 AI 生成，仅供参考，不能替代主持人人工归纳。请结合讨论实际情况审核后使用。
        </el-alert>

        <p v-if="degraded" class="degraded-note">
          注：AI 服务当前不可用，以上为降级提示，请主持人根据讨论记录人工归纳结论。
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type {
  AiDiscussionConclusionResponse,
  AiDiscussionTranscript,
  BusinessError,
} from '@aimedical/shared'
import AiDegradedBanner from '../../components/AiDegradedBanner.vue'

const router = useRouter()

const loading = ref(false)
const degraded = ref(false)
const fallbackReason = ref<string | null>(null)
const result = ref<AiDiscussionConclusionResponse | null>(null)
const transcripts = ref<AiDiscussionTranscript[]>([])

function addTranscript() {
  transcripts.value.push({
    speaker_role: 'DOCTOR',
    speaker_name: '',
    timestamp: '',
    content: '',
  })
}

function removeTranscript(idx: number) {
  transcripts.value.splice(idx, 1)
}

async function handleSubmit() {
  if (!transcripts.value.length) {
    ElMessage.warning('请至少添加一条发言记录')
    return
  }
  // 简单校验：发言人或内容至少有一项
  const invalid = transcripts.value.some(
    (t) => !t.speaker_name.trim() && !t.content.trim(),
  )
  if (invalid) {
    ElMessage.warning('存在发言记录缺少发言人和内容，请补全')
    return
  }

  loading.value = true
  degraded.value = false
  fallbackReason.value = null
  result.value = null

  const payload = {
    transcripts: transcripts.value.map((t) => ({
      speaker_role: t.speaker_role || 'DOCTOR',
      speaker_name: t.speaker_name,
      timestamp: t.timestamp,
      content: t.content,
    })),
  }

  const res = await doctorApi.aiDiscussionConclusion(payload)
  loading.value = false

  if (isBusinessError(res)) {
    ElMessage.error((res as BusinessError).message)
    return
  }

  degraded.value = res.degraded
  fallbackReason.value = res.fallback_reason

  if (!res.success && !res.degraded) {
    ElMessage.error(`AI 调用失败：${res.error_code ?? '未知错误'}`)
    return
  }

  if (res.data) {
    result.value = res.data
    if (res.degraded) {
      ElMessage.warning('AI 不可用，已返回降级提示')
    } else {
      ElMessage.success('讨论结论已生成')
    }
  }
}

function handleReset() {
  transcripts.value = []
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
  max-width: 900px;
}

.transcripts-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.transcripts-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.transcript-item {
  padding: 12px;
  margin-bottom: 12px;
  background-color: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.result-section {
  margin-top: 16px;
}

.conclusion-block {
  margin-bottom: 16px;
}

.conclusion-block h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #303133;
}

.conclusion-text {
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
