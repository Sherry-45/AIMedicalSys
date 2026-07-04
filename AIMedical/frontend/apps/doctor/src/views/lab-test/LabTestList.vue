<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>检验单列表</h2>
          <div class="header-actions">
            <el-select
              v-model="statusFilter"
              placeholder="状态筛选"
              clearable
              style="width: 140px"
              @change="onFilterChange"
            >
              <el-option label="全部" value="" />
              <el-option label="待采样" value="PENDING" />
              <el-option label="已采样" value="COLLECTED" />
              <el-option label="检验中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
            <el-input
              v-model="patientIdInput"
              type="number"
              placeholder="搜索患者ID"
              clearable
              style="width: 200px"
              @keyup.enter="onFilterChange"
              @clear="onFilterChange"
            />
            <el-button :loading="loading" @click="loadList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border style="width: 100%" @row-click="goDetail">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="patient_id" label="患者ID" width="100" />
        <el-table-column prop="test_type" label="检验类型" min-width="160" show-overflow-tooltip />
        <el-table-column label="样本类型" width="110">
          <template #default="{ row }">{{ sampleTypeLabel(row.sample_type) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报告时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.reported_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="goDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无检验单记录" />
        </template>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, total"
        background
        class="pagination"
        @current-change="loadList"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type { LabTestResponse, LabTestStatus, SampleType } from '@aimedical/shared'

const router = useRouter()
const loading = ref(false)
const list = ref<LabTestResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const statusFilter = ref<string>('')
const patientIdInput = ref<string>('')

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

function onFilterChange() {
  currentPage.value = 1
  loadList()
}

function goDetail(row: LabTestResponse) {
  router.push(`/lab-tests/${row.id}`)
}

async function loadList() {
  loading.value = true
  try {
    const params: { page: number; size: number; status?: string; patientId?: number } = {
      page: currentPage.value - 1,
      size: pageSize,
    }
    if (statusFilter.value) params.status = statusFilter.value
    const pid = patientIdInput.value.trim()
    if (pid) {
      const n = Number(pid)
      if (!Number.isNaN(n)) params.patientId = n
    }
    const res = await doctorApi.listLabTests(params)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      list.value = []
      total.value = 0
      return
    }
    list.value = res.content
    total.value = res.total_elements
  } finally {
    loading.value = false
  }
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

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

:deep(.el-table__row) {
  cursor: pointer;
}
</style>
