<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>我的处方</h2>
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
              <el-option label="草稿" value="DRAFT" />
              <el-option label="待审" value="PENDING_REVIEW" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索患者/诊断"
              clearable
              size="default"
              style="width: 220px"
              @input="applyFilter"
            />
            <el-button :loading="loading" @click="loadPrescriptions">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="pagedList" border style="width: 100%" @row-click="goDetail">
        <el-table-column label="处方ID" prop="id" width="90" />
        <el-table-column label="患者" width="120">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click.stop="goPatient(row.patient_id)">
              {{ row.patient_name }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="诊断" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.diagnosis || '（无诊断）' }}</template>
        </el-table-column>
        <el-table-column label="药品数" width="90" align="center">
          <template #default="{ row }">{{ row.items?.length ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="AI 检查" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.ai_checked" size="small" type="success">已检</el-tag>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="开具时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click.stop="goDetail(row)">查看</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无处方记录" />
        </template>
      </el-table>

      <div v-if="filteredList.length > pageSize" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="filteredList.length"
          layout="prev, pager, next, total"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type { PrescriptionResponse, PrescriptionStatus } from '@aimedical/shared'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const prescriptions = ref<PrescriptionResponse[]>([])

const statusFilter = ref<string>('')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 10

const filteredList = computed(() => {
  let list = prescriptions.value
  if (statusFilter.value) {
    list = list.filter((p) => p.status === statusFilter.value)
  }
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    list = list.filter(
      (p) =>
        p.patient_name.toLowerCase().includes(kw) ||
        (p.diagnosis || '').toLowerCase().includes(kw),
    )
  }
  return list
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredList.value.slice(start, start + pageSize)
})

function applyFilter() {
  currentPage.value = 1
}

watch(filteredList, () => {
  if (currentPage.value > Math.ceil(filteredList.value.length / pageSize)) {
    currentPage.value = 1
  }
})

const formatDateTime = (iso: string | null): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

const statusLabel = (status: PrescriptionStatus): string => {
  const map: Record<PrescriptionStatus, string> = {
    DRAFT: '草稿',
    PENDING_REVIEW: '待审',
    APPROVED: '已通过',
    REJECTED: '已驳回',
  }
  return map[status] || status
}

const statusTagType = (status: PrescriptionStatus): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<PrescriptionStatus, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    DRAFT: 'info',
    PENDING_REVIEW: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
  }
  return map[status] || 'info'
}

function goDetail(row: PrescriptionResponse) {
  router.push(`/prescriptions/${row.id}`)
}

function goPatient(patientId: number) {
  router.push(`/patient/${patientId}`)
}

async function loadPrescriptions() {
  loading.value = true
  try {
    const result = await doctorApi.listPrescriptionsByDoctor()
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    prescriptions.value = result
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 支持 Dashboard 跳转时携带状态筛选（如 ?status=PENDING_REVIEW）
  const qStatus = route.query.status as string | undefined
  if (qStatus) {
    statusFilter.value = qStatus
  }
  loadPrescriptions()
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
