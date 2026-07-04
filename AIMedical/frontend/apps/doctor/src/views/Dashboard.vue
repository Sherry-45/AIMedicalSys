<template>
  <div class="dashboard-container" v-loading="loading">
    <!-- 欢迎区 -->
    <div class="welcome-card">
      <div class="welcome-text">
        <h2>欢迎回来，{{ authStore.user?.real_name || '医生' }}</h2>
        <p class="welcome-sub">{{ today }} · 祝您工作顺利</p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" @click="router.push('/queue')">进入叫号台</el-button>
        <el-button @click="router.push('/prescriptions')">我的处方</el-button>
      </div>
    </div>

    <!-- 顶部统计卡片 -->
    <div class="stat-grid">
      <el-card shadow="hover" class="stat-card" @click="router.push('/queue')">
        <div class="stat-content">
          <div class="stat-icon waiting">
            <span>⏳</span>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ queueStats.waiting }}</div>
            <div class="stat-label">候诊中</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card" @click="router.push('/queue')">
        <div class="stat-content">
          <div class="stat-icon consulting">
            <span>🩺</span>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ queueStats.consulting }}</div>
            <div class="stat-label">接诊中</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card" @click="router.push('/prescriptions')">
        <div class="stat-content">
          <div class="stat-icon prescription">
            <span>💊</span>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ prescriptionStats.total }}</div>
            <div class="stat-label">我的处方</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card" @click="router.push('/prescriptions?status=PENDING_REVIEW')">
        <div class="stat-content">
          <div class="stat-icon pending">
            <span>📋</span>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ prescriptionStats.pending }}</div>
            <div class="stat-label">待审处方</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主体两栏 -->
    <div class="main-grid">
      <!-- 左侧：当前接诊 + 候诊队列 -->
      <el-card class="main-card">
        <template #header>
          <div class="card-header">
            <h3>叫号概览</h3>
            <el-button text type="primary" @click="router.push('/queue')">查看全部 →</el-button>
          </div>
        </template>

        <div v-if="currentConsultations.length === 0 && waitingList.length === 0" class="empty-block">
          <el-empty description="暂无叫号记录" />
        </div>

        <div v-else>
          <div v-if="currentConsultations.length > 0" class="section">
            <h4 class="section-title">
              <el-tag type="danger" size="small" effect="dark">接诊中</el-tag>
              <span class="section-count">{{ currentConsultations.length }}</span>
            </h4>
            <div class="patient-list">
              <div
                v-for="item in currentConsultations.slice(0, 3)"
                :key="item.id"
                class="patient-item"
                @click="goPatient(item.patient_id)"
              >
                <div class="patient-info">
                  <span class="patient-name">{{ item.patient_name }}</span>
                  <span class="patient-no">№ {{ item.queue_no }}</span>
                </div>
                <el-tag size="small" :type="statusTagType(item.status)">
                  {{ statusLabel(item.status) }}
                </el-tag>
              </div>
            </div>
          </div>

          <div v-if="waitingList.length > 0" class="section">
            <h4 class="section-title">
              <el-tag type="info" size="small" effect="dark">候诊</el-tag>
              <span class="section-count">{{ waitingList.length }}</span>
            </h4>
            <div class="patient-list">
              <div
                v-for="item in waitingList.slice(0, 5)"
                :key="item.id"
                class="patient-item"
                @click="goPatient(item.patient_id)"
              >
                <div class="patient-info">
                  <span class="patient-name">{{ item.patient_name }}</span>
                  <span class="patient-no">№ {{ item.queue_no }}</span>
                </div>
                <span class="patient-time">{{ formatTime(item.registered_at) }}</span>
              </div>
              <div v-if="waitingList.length > 5" class="more-link" @click="router.push('/queue')">
                查看其余 {{ waitingList.length - 5 }} 位 →
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 右侧：最近处方 + 快捷入口 -->
      <div class="right-column">
        <el-card class="main-card">
          <template #header>
            <div class="card-header">
              <h3>最近处方</h3>
              <el-button text type="primary" @click="router.push('/prescriptions')">全部 →</el-button>
            </div>
          </template>

          <div v-if="recentPrescriptions.length === 0" class="empty-block">
            <el-empty description="暂无处方记录" :image-size="80" />
          </div>
          <div v-else class="prescription-list">
            <div
              v-for="rx in recentPrescriptions"
              :key="rx.id"
              class="prescription-item"
              @click="router.push(`/prescriptions/${rx.id}`)"
            >
              <div class="rx-main">
                <div class="rx-patient">{{ rx.patient_name }}</div>
                <div class="rx-diagnosis">{{ rx.diagnosis || '（无诊断）' }}</div>
              </div>
              <div class="rx-meta">
                <el-tag size="small" :type="rxStatusTagType(rx.status)">{{ rxStatusLabel(rx.status) }}</el-tag>
                <div class="rx-time">{{ formatTime(rx.created_at) }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="main-card">
          <template #header>
            <div class="card-header">
              <h3>AI 辅助工具</h3>
            </div>
          </template>
          <div class="ai-grid">
            <div class="ai-item" @click="router.push('/ai/diagnosis')">
              <span class="ai-icon">🔍</span>
              <span class="ai-label">AI 辅助诊断</span>
            </div>
            <div class="ai-item" @click="router.push('/ai/examination')">
              <span class="ai-icon">🧪</span>
              <span class="ai-label">AI 开立检查</span>
            </div>
            <div class="ai-item" @click="router.push('/ai/prescription-assist')">
              <span class="ai-icon">💡</span>
              <span class="ai-label">AI 辅助开方</span>
            </div>
            <div class="ai-item" @click="router.push('/ai/prescription-audit')">
              <span class="ai-icon">🛡️</span>
              <span class="ai-label">AI 处方审核</span>
            </div>
            <div class="ai-item" @click="router.push('/ai/medical-record-gen')">
              <span class="ai-icon">📝</span>
              <span class="ai-label">AI 病历生成</span>
            </div>
            <div class="ai-item" @click="router.push('/ai/discussion-conclusion')">
              <span class="ai-icon">💬</span>
              <span class="ai-label">AI 讨论结论</span>
            </div>
          </div>
        </el-card>

        <el-card class="main-card">
          <template #header>
            <div class="card-header">
              <h3>诊疗工作台</h3>
            </div>
          </template>
          <div class="ai-grid">
            <div class="ai-item" @click="router.push('/examinations')">
              <span class="ai-icon">🏥</span>
              <span class="ai-label">检查管理</span>
            </div>
            <div class="ai-item" @click="router.push('/examinations/order-suggest')">
              <span class="ai-icon">📋</span>
              <span class="ai-label">检查推荐</span>
            </div>
            <div class="ai-item" @click="router.push('/lab-tests')">
              <span class="ai-icon">🧬</span>
              <span class="ai-label">检验管理</span>
            </div>
            <div class="ai-item" @click="router.push('/lab-tests/order-suggest')">
              <span class="ai-icon">📊</span>
              <span class="ai-label">检验推荐</span>
            </div>
            <div class="ai-item" @click="router.push('/medical-orders')">
              <span class="ai-icon">💉</span>
              <span class="ai-label">医嘱管理</span>
            </div>
            <div class="ai-item" @click="router.push('/profile')">
              <span class="ai-icon">👤</span>
              <span class="ai-label">个人中心</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type {
  ConsultationQueueResponse,
  ConsultationStatus,
  PrescriptionResponse,
  PrescriptionStatus,
} from '@aimedical/shared'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const queue = ref<ConsultationQueueResponse[]>([])
const prescriptions = ref<PrescriptionResponse[]>([])

const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long',
})

const queueStats = computed(() => ({
  waiting: queue.value.filter((q) => q.status === 'WAITING').length,
  consulting: queue.value.filter((q) => q.status === 'CALLED' || q.status === 'IN_CONSULTATION').length,
}))

const prescriptionStats = computed(() => ({
  total: prescriptions.value.length,
  pending: prescriptions.value.filter((p) => p.status === 'PENDING_REVIEW').length,
}))

const waitingList = computed(() => queue.value.filter((q) => q.status === 'WAITING'))
const currentConsultations = computed(() =>
  queue.value.filter((q) => q.status === 'CALLED' || q.status === 'IN_CONSULTATION'),
)
const recentPrescriptions = computed(() =>
  [...prescriptions.value]
    .sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime())
    .slice(0, 5),
)

const formatTime = (iso: string | null): string =>
  iso ? new Date(iso).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '—'

const statusLabel = (status: ConsultationStatus): string => {
  const map: Record<ConsultationStatus, string> = {
    WAITING: '候诊',
    CALLED: '已叫号',
    IN_CONSULTATION: '接诊中',
    FINISHED: '已完成',
    SKIPPED: '已过号',
  }
  return map[status] || status
}

const statusTagType = (status: ConsultationStatus): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<ConsultationStatus, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    WAITING: 'info',
    CALLED: 'warning',
    IN_CONSULTATION: 'danger',
    FINISHED: 'success',
    SKIPPED: 'info',
  }
  return map[status] || 'info'
}

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

function goPatient(patientId: number) {
  router.push(`/patient/${patientId}`)
}

async function loadDashboard() {
  loading.value = true
  try {
    const [queueResult, rxResult] = await Promise.all([
      doctorApi.listMyQueue(),
      doctorApi.listPrescriptionsByDoctor(),
    ])

    if (isBusinessError(queueResult)) {
      ElMessage.warning(`叫号队列加载失败：${queueResult.message}`)
    } else {
      queue.value = queueResult
    }

    if (isBusinessError(rxResult)) {
      ElMessage.warning(`处方列表加载失败：${rxResult.message}`)
    } else {
      prescriptions.value = rxResult
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 欢迎卡片 */
.welcome-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.18);
}

.welcome-text h2 {
  margin: 0 0 6px 0;
  font-size: 22px;
  font-weight: 600;
}

.welcome-sub {
  margin: 0;
  font-size: 13px;
  opacity: 0.9;
}

.welcome-actions {
  display: flex;
  gap: 10px;
}

.welcome-actions .el-button {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.5);
  color: #fff;
}

.welcome-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 统计卡片 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.stat-icon.waiting {
  background: linear-gradient(135deg, #909399, #c0c4cc);
}

.stat-icon.consulting {
  background: linear-gradient(135deg, #f56c6c, #ff9090);
}

.stat-icon.prescription {
  background: linear-gradient(135deg, #409eff, #79bbff);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #e6a23c, #f3d19e);
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  line-height: 1.1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

/* 主体两栏 */
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
  min-height: 200px;
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

.section {
  margin-bottom: 18px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.section-count {
  font-size: 13px;
  color: #909399;
}

.patient-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.patient-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 6px;
  border-left: 3px solid transparent;
  cursor: pointer;
  transition: all 0.15s;
}

.patient-item:hover {
  background: #ecf5ff;
  border-left-color: #409eff;
}

.patient-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.patient-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.patient-no {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.patient-time {
  font-size: 12px;
  color: #909399;
}

.more-link {
  font-size: 13px;
  color: #409eff;
  text-align: center;
  padding: 8px;
  cursor: pointer;
  border-radius: 4px;
}

.more-link:hover {
  background: #ecf5ff;
}

.empty-block {
  padding: 24px 0;
}

/* 处方列表 */
.prescription-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.prescription-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #fafbfc;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s;
}

.prescription-item:hover {
  background: #f0f9eb;
}

.rx-main {
  flex: 1;
  min-width: 0;
}

.rx-patient {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.rx-diagnosis {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rx-meta {
  text-align: right;
  flex-shrink: 0;
  margin-left: 8px;
}

.rx-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 2px;
}

/* AI 网格 */
.ai-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.ai-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
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
  font-size: 26px;
}

.ai-label {
  font-size: 12px;
  color: #606266;
  text-align: center;
}

@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .main-grid {
    grid-template-columns: 1fr;
  }
}
</style>
