<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>检查单列表</h2>
          <div class="header-actions">
            <el-select
              v-model="statusFilter"
              placeholder="状态筛选"
              clearable
              size="default"
              style="width: 140px"
              @change="applyFilter"
            >
              <el-option label="全部" :value="''" />
              <el-option label="待处理" value="PENDING" />
              <el-option label="已预约" value="SCHEDULED" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
            <el-input
              v-model="patientIdInput"
              placeholder="按患者ID筛选"
              clearable
              size="default"
              style="width: 200px"
              @keyup.enter="applyFilter"
              @clear="applyFilter"
            />
            <el-button :loading="loading" @click="loadExaminations">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="examinations" border style="width: 100%" @row-click="goDetail">
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="患者ID" prop="patient_id" width="100" align="center" />
        <el-table-column label="检查类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ typeLabel(row.examination_type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="部位" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.body_part || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="急诊" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.emergency_flag" type="danger" size="small">急诊</el-tag>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click.stop="goDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无检查单" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          background
          @current-change="loadExaminations"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type {
  ExaminationResponse,
  ExaminationStatus,
  ExaminationType,
} from '@aimedical/shared'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const examinations = ref<ExaminationResponse[]>([])

const statusFilter = ref<string>('')
const patientIdInput = ref('')
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)

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

function applyFilter() {
  currentPage.value = 1
  loadExaminations()
}

function goDetail(row: ExaminationResponse) {
  router.push(`/examinations/${row.id}`)
}

async function loadExaminations() {
  loading.value = true
  try {
    const params: {
      patientId?: number
      status?: string
      page?: number
      size?: number
    } = {
      page: currentPage.value - 1,
      size: pageSize,
    }
    if (statusFilter.value) params.status = statusFilter.value
    const pid = patientIdInput.value.trim()
    if (pid && !Number.isNaN(Number(pid))) params.patientId = Number(pid)

    const result = await doctorApi.listExaminations(params)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    examinations.value = result.content
    total.value = result.totalElements
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 支持 query 携带初始状态筛选（如 ?status=PENDING）
  const qStatus = route.query.status as string | undefined
  if (qStatus) statusFilter.value = qStatus
  loadExaminations()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
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

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.text-muted {
  color: #c0c4cc;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table__row) {
  cursor: pointer;
}
</style>
