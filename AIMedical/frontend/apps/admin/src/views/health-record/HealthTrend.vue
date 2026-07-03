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

    <el-empty v-if="!trend" description="请先查询患者" :image-size="80" />

    <template v-else>
      <!-- 趋势概览 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header"><h3>趋势概览</h3></div>
        </template>
        <el-descriptions :column="4" border>
          <el-descriptions-item label="患者ID">{{ trend.patient_id }}</el-descriptions-item>
          <el-descriptions-item label="总记录数">{{ trend.total_records }}</el-descriptions-item>
          <el-descriptions-item label="最早记录">
            {{ formatDate(trend.earliest_record_date) }}
          </el-descriptions-item>
          <el-descriptions-item label="最新记录">
            {{ formatDate(trend.latest_record_date) }}
          </el-descriptions-item>
          <el-descriptions-item label="就诊机构" :span="4">
            <el-tag
              v-for="org in (trend.organizations ?? [])"
              :key="org"
              class="org-tag"
              type="info"
            >
              {{ org }}
            </el-tag>
            <span v-if="!trend.organizations.length">—</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 按类型 / 按类别 统计 -->
      <div class="stat-grid">
        <el-card shadow="never">
          <template #header>
            <div class="card-header"><h3>按类型统计</h3></div>
          </template>
          <div v-if="typeEntries.length === 0" class="empty-text">暂无数据</div>
          <div v-else class="stat-list">
            <div v-for="[name, count] in typeEntries" :key="name" class="stat-row">
              <div class="stat-label">{{ name }}</div>
              <el-progress
                :percentage="pct(count, typeMax)"
                :stroke-width="14"
                :format="() => `${count}`"
              />
            </div>
          </div>
        </el-card>
        <el-card shadow="never">
          <template #header>
            <div class="card-header"><h3>按类别统计</h3></div>
          </template>
          <div v-if="categoryEntries.length === 0" class="empty-text">暂无数据</div>
          <div v-else class="stat-list">
            <div v-for="[name, count] in categoryEntries" :key="name" class="stat-row">
              <div class="stat-label">{{ name }}</div>
              <el-progress
                :percentage="pct(count, categoryMax)"
                :stroke-width="14"
                :color="'#67c23a'"
                :format="() => `${count}`"
              />
            </div>
          </div>
        </el-card>
      </div>

      <!-- 最近记录 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header"><h3>最近记录</h3></div>
        </template>
        <el-table :data="recentRecords" border style="width: 100%">
          <el-table-column label="标题" prop="title" min-width="180" show-overflow-tooltip />
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              {{ row.record_type_desc || row.record_type }}
            </template>
          </el-table-column>
          <el-table-column label="机构" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.organization || '—' }}</template>
          </el-table-column>
          <el-table-column label="记录日期" width="120">
            <template #default="{ row }">{{ formatDate(row.record_date) }}</template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无记录" />
          </template>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { isBusinessError } from '@aimedical/shared'
import { healthRecordApi } from '@aimedical/shared'
import type { HealthTrendResponse } from '@aimedical/shared'

const patientIdInput = ref('')
const loading = ref(false)
const trend = ref<HealthTrendResponse | null>(null)

const typeEntries = computed<[string, number][]>(() =>
  trend.value ? Object.entries(trend.value.records_by_type ?? {}) : [],
)
const categoryEntries = computed<[string, number][]>(() =>
  trend.value ? Object.entries(trend.value.records_by_category ?? {}) : [],
)
const typeMax = computed(() => Math.max(1, ...typeEntries.value.map((e) => e[1])))
const categoryMax = computed(() => Math.max(1, ...categoryEntries.value.map((e) => e[1])))
const recentRecords = computed(() => trend.value?.recent_records?.slice(0, 10) ?? [])

const pct = (count: number, max: number) => Math.round((count / max) * 100)
const formatDate = (iso?: string): string => (iso ? iso.substring(0, 10) : '—')

function handleSearch() {
  const id = Number(patientIdInput.value)
  if (!patientIdInput.value || Number.isNaN(id)) {
    ElMessage.warning('请输入有效的患者ID')
    return
  }
  loadTrend(id)
}

async function loadTrend(patientId: number) {
  loading.value = true
  try {
    const result = await healthRecordApi.getTrend(patientId)
    if (isBusinessError(result)) {
      ElMessage.error(`趋势加载失败：${result.message}`)
      trend.value = null
      return
    }
    trend.value = result
  } finally {
    loading.value = false
  }
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

.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.stat-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.stat-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: #606266;
}

.empty-text {
  color: #909399;
  font-size: 13px;
  padding: 12px 0;
}

.org-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}

@media (max-width: 1024px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
