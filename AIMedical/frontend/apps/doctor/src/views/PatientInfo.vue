<template>
  <div class="page-container">
    <!-- 患者头部卡 -->
    <el-card class="patient-card" v-loading="loading">
      <div class="patient-header">
        <div class="patient-avatar">
          <span class="avatar-emoji">👤</span>
        </div>
        <div class="patient-meta">
          <h2>患者档案 #{{ patientId }}</h2>
          <p class="patient-hint">
            由于后端暂未提供医生端的患者档案查询接口，此处仅展示编号与诊疗记录。完整档案信息待后端接口就绪后补充。
          </p>
        </div>
        <div class="patient-actions">
          <el-button type="primary" @click="router.push(`/patient/${patientId}/condition`)">
            病情录入
          </el-button>
          <el-button type="primary" @click="router.push(`/patient/${patientId}/medical-records/new`)">
            新建病历
          </el-button>
          <el-button type="primary" @click="router.push(`/patient/${patientId}/prescriptions/new`)">
            开具处方
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计卡 -->
    <div class="stat-grid">
      <el-card shadow="hover" class="stat-card" @click="router.push(`/patient/${patientId}/medical-records`)">
        <div class="stat-content">
          <div class="stat-icon record"><span>📋</span></div>
          <div class="stat-info">
            <div class="stat-value">{{ medicalRecords.length }}</div>
            <div class="stat-label">病历数</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon rx"><span>💊</span></div>
          <div class="stat-info">
            <div class="stat-value">{{ prescriptions.length }}</div>
            <div class="stat-label">处方数</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon pending"><span>⏰</span></div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingRxCount }}</div>
            <div class="stat-label">待审处方</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主体两栏 -->
    <div class="main-grid">
      <!-- 左侧：病历列表 -->
      <el-card class="main-card">
        <template #header>
          <div class="card-header">
            <h3>最近病历</h3>
            <el-button text type="primary" @click="router.push(`/patient/${patientId}/medical-records`)">
              全部 →
            </el-button>
          </div>
        </template>
        <div v-loading="recordsLoading">
          <div v-if="recentRecords.length === 0" class="empty-block">
            <el-empty description="暂无病历" :image-size="80" />
          </div>
          <div v-else class="record-list">
            <div
              v-for="r in recentRecords"
              :key="r.id"
              class="record-item"
              @click="router.push(`/patient/${patientId}/medical-records/${r.id}`)"
            >
              <div class="record-main">
                <div class="record-title">
                  <el-tag size="small" :type="r.status === 'OFFICIAL' ? 'success' : 'info'">
                    {{ r.status === 'OFFICIAL' ? '正式' : '草稿' }}
                  </el-tag>
                  <span class="record-version">v{{ r.version_no }}</span>
                  <span v-if="r.ai_generated" class="ai-tag">AI</span>
                </div>
                <div class="record-complaint">{{ r.chief_complaint || '（无主诉）' }}</div>
                <div class="record-diagnosis">诊断：{{ r.diagnosis || '—' }}</div>
              </div>
              <div class="record-time">{{ formatTime(r.created_at) }}</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 右侧：处方列表 + AI辅助 -->
      <div class="right-column">
        <el-card class="main-card">
          <template #header>
            <div class="card-header">
              <h3>处方记录</h3>
              <el-button
                text
                type="primary"
                @click="router.push(`/patient/${patientId}/prescriptions/new`)"
              >
                新建 →
              </el-button>
            </div>
          </template>
          <div v-loading="rxLoading">
            <div v-if="recentPrescriptions.length === 0" class="empty-block">
              <el-empty description="暂无处方" :image-size="80" />
            </div>
            <div v-else class="rx-list">
              <div
                v-for="rx in recentPrescriptions"
                :key="rx.id"
                class="rx-item"
                @click="router.push(`/prescriptions/${rx.id}`)"
              >
                <div class="rx-main">
                  <div class="rx-title">
                    <el-tag size="small" :type="rxStatusTagType(rx.status)">
                      {{ rxStatusLabel(rx.status) }}
                    </el-tag>
                    <span class="rx-id">#{{ rx.id }}</span>
                  </div>
                  <div class="rx-diagnosis">{{ rx.diagnosis || '（无诊断）' }}</div>
                  <div class="rx-meta">{{ rx.items?.length ?? 0 }} 项药品</div>
                </div>
                <div class="rx-time">{{ formatTime(rx.created_at) }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="main-card">
          <template #header>
            <div class="card-header">
              <h3>AI 辅助</h3>
            </div>
          </template>
          <div class="ai-grid">
            <div class="ai-item" @click="router.push(`/ai/diagnosis?patient_id=${patientId}`)">
              <span class="ai-icon">🔍</span>
              <span class="ai-label">AI 辅助诊断</span>
            </div>
            <div class="ai-item" @click="router.push(`/ai/examination?patient_id=${patientId}`)">
              <span class="ai-icon">🧪</span>
              <span class="ai-label">AI 开立检查</span>
            </div>
            <div class="ai-item" @click="router.push(`/ai/prescription-assist?patient_id=${patientId}`)">
              <span class="ai-icon">💡</span>
              <span class="ai-label">AI 辅助开方</span>
            </div>
            <div class="ai-item" @click="router.push(`/ai/medical-record-gen?patient_id=${patientId}`)">
              <span class="ai-icon">📝</span>
              <span class="ai-label">AI 病历生成</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type {
  MedicalRecordResponse,
  PrescriptionResponse,
  PrescriptionStatus,
} from '@aimedical/shared'

const route = useRoute()
const router = useRouter()
const patientId = Number(route.params.patientId)

const loading = ref(false)
const recordsLoading = ref(false)
const rxLoading = ref(false)

const medicalRecords = ref<MedicalRecordResponse[]>([])
const prescriptions = ref<PrescriptionResponse[]>([])

const pendingRxCount = computed(
  () => prescriptions.value.filter((p) => p.status === 'PENDING_REVIEW').length,
)

const recentRecords = computed(() =>
  [...medicalRecords.value]
    .sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime())
    .slice(0, 5),
)

const recentPrescriptions = computed(() =>
  [...prescriptions.value]
    .sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime())
    .slice(0, 5),
)

const formatTime = (iso: string | null): string =>
  iso
    ? new Date(iso).toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      })
    : '—'

const rxStatusLabel = (status: PrescriptionStatus): string => {
  const map: Record<PrescriptionStatus, string> = {
    DRAFT: '草稿',
    PENDING_REVIEW: '待审',
    APPROVED: '已通过',
    REJECTED: '已驳回',
  }
  return map[status] || status
}

const rxStatusTagType = (status: PrescriptionStatus): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<PrescriptionStatus, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    DRAFT: 'info',
    PENDING_REVIEW: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
  }
  return map[status] || 'info'
}

async function loadRecords() {
  recordsLoading.value = true
  try {
    const result = await doctorApi.listMedicalRecordsByPatient(patientId)
    if (isBusinessError(result)) {
      ElMessage.warning(`病历加载失败：${result.message}`)
      return
    }
    medicalRecords.value = result
  } finally {
    recordsLoading.value = false
  }
}

async function loadPrescriptions() {
  rxLoading.value = true
  try {
    const result = await doctorApi.listPrescriptionsByPatient(patientId)
    if (isBusinessError(result)) {
      ElMessage.warning(`处方加载失败：${result.message}`)
      return
    }
    prescriptions.value = result
  } finally {
    rxLoading.value = false
  }
}

onMounted(() => {
  if (!patientId || Number.isNaN(patientId)) {
    ElMessage.error('患者ID无效')
    router.replace('/queue')
    return
  }
  loading.value = true
  Promise.all([loadRecords(), loadPrescriptions()]).finally(() => {
    loading.value = false
  })
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 患者头部 */
.patient-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.patient-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-emoji {
  font-size: 36px;
  line-height: 1;
}

.patient-meta {
  flex: 1;
  min-width: 0;
}

.patient-meta h2 {
  margin: 0 0 6px 0;
  font-size: 20px;
  color: #303133;
}

.patient-hint {
  margin: 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.patient-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 统计卡 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08) !important;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
}

.stat-icon.record {
  background: linear-gradient(135deg, #409eff, #79bbff);
}

.stat-icon.rx {
  background: linear-gradient(135deg, #67c23a, #95d475);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #e6a23c, #f3d19e);
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

/* 主体 */
.main-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
}

.right-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.main-card {
  min-height: 180px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.empty-block {
  padding: 16px 0;
}

/* 病历列表 */
.record-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s;
  gap: 8px;
}

.record-item:hover {
  background: #ecf5ff;
}

.record-main {
  flex: 1;
  min-width: 0;
}

.record-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.record-version {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.ai-tag {
  background: #67c23a;
  color: #fff;
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
}

.record-complaint {
  font-size: 13px;
  color: #303133;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-diagnosis {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-time {
  font-size: 11px;
  color: #c0c4cc;
  flex-shrink: 0;
  white-space: nowrap;
}

/* 处方列表 */
.rx-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.rx-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s;
  gap: 8px;
}

.rx-item:hover {
  background: #f0f9eb;
}

.rx-main {
  flex: 1;
  min-width: 0;
}

.rx-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.rx-id {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.rx-diagnosis {
  font-size: 13px;
  color: #303133;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rx-meta {
  font-size: 11px;
  color: #c0c4cc;
}

.rx-time {
  font-size: 11px;
  color: #c0c4cc;
  flex-shrink: 0;
  white-space: nowrap;
}

/* AI 网格 */
.ai-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.ai-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 8px;
  background: #fafbfc;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.ai-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.ai-icon {
  font-size: 24px;
}

.ai-label {
  font-size: 12px;
  color: #606266;
  text-align: center;
}

@media (max-width: 1024px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
  .main-grid {
    grid-template-columns: 1fr;
  }
  .patient-header {
    flex-wrap: wrap;
  }
}
</style>
