<template>
  <div class="page-container">
    <!-- 顶部搜索区 -->
    <el-card shadow="never">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="患者 ID">
          <el-input
            v-model="patientIdInput"
            placeholder="请输入患者ID"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 健康摘要卡片 -->
    <el-card shadow="never" v-loading="summaryLoading">
      <template #header>
        <div class="card-header"><h3>健康摘要</h3></div>
      </template>
      <el-empty v-if="!summary" description="请先查询患者" :image-size="60" />
      <el-descriptions v-else :column="3" border>
        <el-descriptions-item label="患者">
          {{ summary.patient_name || `#${summary.patient_id}` }}
        </el-descriptions-item>
        <el-descriptions-item label="总记录数">{{ summary.total_records }}</el-descriptions-item>
        <el-descriptions-item label="过敏数">{{ summary.allergy_count ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="慢病数">
          {{ summary.chronic_disease_count ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="最近就诊日期">
          {{ formatDate(summary.last_visit_date) }}
        </el-descriptions-item>
        <el-descriptions-item label="最近就诊机构">
          {{ summary.last_visit_organization || '—' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 主体 Tabs -->
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 档案查询 -->
        <el-tab-pane label="档案查询" name="records">
          <!-- 高级筛选区 -->
          <el-form :inline="true" class="filter-form">
            <el-form-item label="记录类型">
              <el-input
                v-model="filters.recordType"
                placeholder="如 VISIT / EXAM"
                clearable
                style="width: 160px"
              />
            </el-form-item>
            <el-form-item label="记录类别">
              <el-input
                v-model="filters.recordCategory"
                placeholder="记录类别"
                clearable
                style="width: 160px"
              />
            </el-form-item>
            <el-form-item label="就诊机构">
              <el-input
                v-model="filters.organization"
                placeholder="就诊机构"
                clearable
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                value-format="YYYY-MM-DD"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 260px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadRecords">筛选</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table
            :data="pagedRecords"
            v-loading="recordsLoading"
            border
            style="width: 100%"
            @row-click="openDetail"
          >
            <el-table-column label="标题" prop="title" min-width="180" show-overflow-tooltip />
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                {{ row.record_type_desc || row.record_type }}
              </template>
            </el-table-column>
            <el-table-column label="类别" width="120">
              <template #default="{ row }">
                {{ row.record_category_desc || row.record_category || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="机构" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ row.organization || '—' }}</template>
            </el-table-column>
            <el-table-column label="科室" width="110">
              <template #default="{ row }">{{ row.department || '—' }}</template>
            </el-table-column>
            <el-table-column label="医生" width="100">
              <template #default="{ row }">{{ row.doctor_name || '—' }}</template>
            </el-table-column>
            <el-table-column label="记录日期" width="120">
              <template #default="{ row }">{{ formatDate(row.record_date) }}</template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无档案记录" />
            </template>
          </el-table>

          <div v-if="records.length > pageSize" class="pagination-wrapper">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="records.length"
              layout="prev, pager, next, total"
              background
            />
          </div>
        </el-tab-pane>

        <!-- 缴费记录 -->
        <el-tab-pane label="缴费记录" name="payments">
          <el-table
            :data="paymentRecords"
            v-loading="paymentsLoading"
            border
            style="width: 100%"
          >
            <el-table-column label="标题" prop="title" min-width="180" show-overflow-tooltip />
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                {{ row.record_type_desc || row.record_type }}
              </template>
            </el-table-column>
            <el-table-column label="机构" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ row.organization || '—' }}</template>
            </el-table-column>
            <el-table-column label="内容" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">{{ row.content || '—' }}</template>
            </el-table-column>
            <el-table-column label="记录日期" width="120">
              <template #default="{ row }">{{ formatDate(row.record_date) }}</template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无缴费记录" />
            </template>
          </el-table>
        </el-tab-pane>

        <!-- 取药记录 -->
        <el-tab-pane label="取药记录" name="dispensing">
          <el-table
            :data="dispensingRecords"
            v-loading="dispensingLoading"
            border
            style="width: 100%"
          >
            <el-table-column label="标题" prop="title" min-width="180" show-overflow-tooltip />
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                {{ row.record_type_desc || row.record_type }}
              </template>
            </el-table-column>
            <el-table-column label="机构" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ row.organization || '—' }}</template>
            </el-table-column>
            <el-table-column label="详情" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">{{ row.content || '—' }}</template>
            </el-table-column>
            <el-table-column label="记录日期" width="120">
              <template #default="{ row }">{{ formatDate(row.record_date) }}</template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无取药记录" />
            </template>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="档案详情" width="720px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          {{ detail.record_type_desc || detail.record_type }}
        </el-descriptions-item>
        <el-descriptions-item label="类别">
          {{ detail.record_category_desc || detail.record_category || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="记录日期">
          {{ formatDate(detail.record_date) }}
        </el-descriptions-item>
        <el-descriptions-item label="就诊机构">
          {{ detail.organization || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="科室">{{ detail.department || '—' }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ detail.doctor_name || '—' }}</el-descriptions-item>
        <el-descriptions-item label="来源">
          {{ detail.source_table || '—' }} #{{ detail.source_id ?? '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          {{ detail.content || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ detail.remark || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(detail.created_at) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatDateTime(detail.updated_at) }}
        </el-descriptions-item>
        <el-descriptions-item label="报告数据" :span="2">
          <pre v-if="reportDataFormatted" class="report-data">{{ reportDataFormatted }}</pre>
          <span v-else>—</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { isBusinessError } from '@aimedical/shared'
import { healthRecordApi } from '@aimedical/shared'
import type {
  HealthRecordQueryRequest,
  HealthRecordResponse,
  HealthSummaryResponse,
} from '@aimedical/shared'

const patientIdInput = ref('')
const currentPatientId = ref<number | undefined>(undefined)

const loading = ref(false)
const summaryLoading = ref(false)
const recordsLoading = ref(false)
const paymentsLoading = ref(false)
const dispensingLoading = ref(false)

const summary = ref<HealthSummaryResponse | null>(null)
const records = ref<HealthRecordResponse[]>([])
const paymentRecords = ref<HealthRecordResponse[]>([])
const dispensingRecords = ref<HealthRecordResponse[]>([])

const activeTab = ref<'records' | 'payments' | 'dispensing'>('records')

const filters = reactive<HealthRecordQueryRequest>({
  recordType: '',
  recordCategory: '',
  organization: '',
  startDate: undefined,
  endDate: undefined,
})
const dateRange = ref<[string, string] | null>(null)

const currentPage = ref(1)
const pageSize = 10

const pagedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return records.value.slice(start, start + pageSize)
})

const detail = ref<HealthRecordResponse | null>(null)
const detailVisible = ref(false)

const reportDataFormatted = computed(() => {
  const raw = detail.value?.report_data
  if (!raw) return ''
  try {
    return JSON.stringify(JSON.parse(raw), null, 2)
  } catch {
    return raw
  }
})

const formatDate = (iso?: string): string => (iso ? iso.substring(0, 10) : '—')
const formatDateTime = (iso?: string): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

function handleSearch() {
  const id = Number(patientIdInput.value)
  if (!patientIdInput.value || Number.isNaN(id)) {
    ElMessage.warning('请输入有效的患者ID')
    return
  }
  currentPatientId.value = id
  activeTab.value = 'records'
  paymentRecords.value = []
  dispensingRecords.value = []
  loadSummary()
  loadRecords()
}

async function loadSummary() {
  if (!currentPatientId.value) return
  summaryLoading.value = true
  try {
    const result = await healthRecordApi.getSummary(currentPatientId.value)
    if (isBusinessError(result)) {
      ElMessage.error(`摘要加载失败：${result.message}`)
      summary.value = null
      return
    }
    summary.value = result
  } finally {
    summaryLoading.value = false
  }
}

async function loadRecords() {
  if (!currentPatientId.value) return
  recordsLoading.value = true
  currentPage.value = 1
  try {
    const params: HealthRecordQueryRequest = {}
    if (filters.recordType) params.recordType = filters.recordType
    if (filters.recordCategory) params.recordCategory = filters.recordCategory
    if (filters.organization) params.organization = filters.organization
    if (dateRange.value && dateRange.value[0]) params.startDate = dateRange.value[0]
    if (dateRange.value && dateRange.value[1]) params.endDate = dateRange.value[1]
    const result = await healthRecordApi.query(params, currentPatientId.value)
    if (isBusinessError(result)) {
      ElMessage.error(`档案加载失败：${result.message}`)
      records.value = []
      return
    }
    records.value = result
  } finally {
    recordsLoading.value = false
  }
}

function resetFilters() {
  filters.recordType = ''
  filters.recordCategory = ''
  filters.organization = ''
  dateRange.value = null
  loadRecords()
}

async function loadPayments() {
  if (!currentPatientId.value) return
  paymentsLoading.value = true
  try {
    const result = await healthRecordApi.queryPaymentRecords(currentPatientId.value)
    if (isBusinessError(result)) {
      ElMessage.error(`缴费记录加载失败：${result.message}`)
      paymentRecords.value = []
      return
    }
    paymentRecords.value = result
  } finally {
    paymentsLoading.value = false
  }
}

async function loadDispensing() {
  if (!currentPatientId.value) return
  dispensingLoading.value = true
  try {
    const result = await healthRecordApi.queryDispensingRecords(currentPatientId.value)
    if (isBusinessError(result)) {
      ElMessage.error(`取药记录加载失败：${result.message}`)
      dispensingRecords.value = []
      return
    }
    dispensingRecords.value = result
  } finally {
    dispensingLoading.value = false
  }
}

function handleTabChange(name: string | number) {
  if (!currentPatientId.value) {
    ElMessage.warning('请先查询患者')
    return
  }
  if (name === 'payments') loadPayments()
  else if (name === 'dispensing') loadDispensing()
}

function openDetail(row: HealthRecordResponse) {
  detail.value = row
  detailVisible.value = true
}
</script>

<style scoped>
.page-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.filter-form {
  margin-bottom: 12px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.report-data {
  margin: 0;
  max-height: 240px;
  overflow: auto;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}

:deep(.el-table__row) {
  cursor: pointer;
}
</style>
