<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>处方查询</h2>
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
            <el-button :loading="loading" @click="loadList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border style="width: 100%">
        <el-table-column label="处方ID" prop="id" width="90" />
        <el-table-column label="患者" width="140">
          <template #default="{ row }">
            {{ row.patient_name || ('#' + row.patient_id) }}
          </template>
        </el-table-column>
        <el-table-column label="开方医生" width="120">
          <template #default="{ row }">
            {{ row.doctor_id ? ('#' + row.doctor_id) : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="科室" width="120">
          <template #default="{ row }">{{ row.department || '—' }}</template>
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
        <template #empty>
          <el-empty description="暂无处方记录" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadList"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiGet, isBusinessError } from '@aimedical/shared'
import type { BusinessError, PageResponse } from '@aimedical/shared'
import type { PrescriptionResponse, PrescriptionStatus } from '@aimedical/shared'

const loading = ref(false)
const list = ref<PrescriptionResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const statusFilter = ref<string>('')

function applyFilter() {
  currentPage.value = 1
  loadList()
}

function onSizeChange() {
  currentPage.value = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      page: currentPage.value - 1,
      size: pageSize.value,
    }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    const result = await apiGet<PageResponse<PrescriptionResponse>>('/admin/prescriptions', { params })
    if (isBusinessError(result)) {
      ElMessage.error((result as BusinessError).message)
      list.value = []
      total.value = 0
      return
    }
    list.value = result.content ?? []
    total.value = result.totalElements ?? 0
  } finally {
    loading.value = false
  }
}

const formatDateTime = (iso: string | null | undefined): string =>
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

onMounted(() => {
  loadList()
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
</style>
