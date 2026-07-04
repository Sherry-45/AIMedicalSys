<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>检验趋势图</h2>
        </div>
      </template>

      <el-form :model="form" label-position="top" class="trend-form">
        <el-form-item label="患者 ID">
          <el-input
            v-model="form.patientId"
            type="number"
            placeholder="请输入患者ID"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="检验项目名称">
          <el-input
            v-model="form.itemName"
            placeholder="如：白细胞计数"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleQuery">查询趋势</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-divider v-if="trend" />

      <div v-if="trend" class="trend-section">
        <div class="trend-title">
          {{ trend.item_name }}<span v-if="trend.unit" class="unit">（单位：{{ trend.unit }}）</span>
        </div>

        <!-- 简易 CSS 趋势图：仅当全部结果为数值时渲染 -->
        <div
          v-if="numericPoints.length === trend.points.length && numericPoints.length > 0"
          class="chart"
        >
          <div v-for="(p, i) in numericPoints" :key="i" class="bar-wrapper">
            <div class="bar-label">{{ p.result }}</div>
            <div
              class="bar"
              :style="{ height: barHeight(p.value) + '%', background: abnormalColor(p.abnormal_flag) }"
            />
            <div class="bar-date">{{ formatDate(p.test_date) }}</div>
          </div>
        </div>

        <el-table :data="trend.points" border style="width: 100%" class="trend-table">
          <el-table-column label="检验日期" min-width="180">
            <template #default="{ row }">{{ formatDate(row.test_date) }}</template>
          </el-table-column>
          <el-table-column label="结果" width="160">
            <template #default="{ row }">
              <span>{{ row.result }}</span><span v-if="trend.unit" class="unit">{{ trend.unit }}</span>
            </template>
          </el-table-column>
          <el-table-column label="异常标志" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="abnormalTagType(row.abnormal_flag)">{{ abnormalLabel(row.abnormal_flag) }}</el-tag>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无趋势数据" />
          </template>
        </el-table>
      </div>

      <el-empty v-else-if="queried && !trend" description="暂无趋势数据" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type { LabTestTrendResponse, AbnormalFlag } from '@aimedical/shared'

const loading = ref(false)
const queried = ref(false)
const trend = ref<LabTestTrendResponse | null>(null)

const form = reactive<{ patientId: string; itemName: string }>({
  patientId: '',
  itemName: '',
})

interface NumericPoint {
  test_date: string
  result: string
  value: number
  abnormal_flag: AbnormalFlag
}

// 仅当所有结果都能解析为数值时才渲染柱状趋势图
const numericPoints = computed<NumericPoint[]>(() => {
  if (!trend.value) return []
  const mapped = trend.value.points.map((p) => {
    const value = Number(p.result)
    return Number.isNaN(value) ? null : { test_date: p.test_date, result: p.result, value, abnormal_flag: p.abnormal_flag }
  })
  if (mapped.some((p) => p === null)) return []
  return mapped as NumericPoint[]
})

const trendMin = computed(() => Math.min(...numericPoints.value.map((p) => p.value)))
const trendMax = computed(() => Math.max(...numericPoints.value.map((p) => p.value)))

function barHeight(value: number): number {
  if (trendMax.value === trendMin.value) return 60
  const ratio = (value - trendMin.value) / (trendMax.value - trendMin.value)
  return Math.max(15, Math.round(ratio * 60) + 20)
}

const formatDate = (iso: string | null): string =>
  iso ? new Date(iso).toLocaleDateString('zh-CN') : '—'

const abnormalLabelMap: Record<AbnormalFlag, string> = {
  NORMAL: '正常',
  LOW: '偏低',
  HIGH: '偏高',
  CRITICAL_LOW: '危急低',
  CRITICAL_HIGH: '危急高',
}
const abnormalLabel = (f: AbnormalFlag): string => abnormalLabelMap[f] || f

const abnormalTagTypeMap: Record<AbnormalFlag, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
  NORMAL: 'success',
  LOW: 'warning',
  HIGH: 'warning',
  CRITICAL_LOW: 'danger',
  CRITICAL_HIGH: 'danger',
}
const abnormalTagType = (f: AbnormalFlag): 'primary' | 'success' | 'info' | 'warning' | 'danger' =>
  abnormalTagTypeMap[f] || 'info'

function abnormalColor(f: AbnormalFlag): string {
  switch (f) {
    case 'NORMAL':
      return '#67c23a'
    case 'LOW':
    case 'HIGH':
      return '#e6a23c'
    case 'CRITICAL_LOW':
    case 'CRITICAL_HIGH':
      return '#f56c6c'
    default:
      return '#409eff'
  }
}

async function handleQuery() {
  const pid = Number(form.patientId)
  if (!form.patientId || Number.isNaN(pid)) {
    ElMessage.warning('请输入有效的患者ID')
    return
  }
  if (!form.itemName.trim()) {
    ElMessage.warning('请输入检验项目名称')
    return
  }
  loading.value = true
  queried.value = true
  trend.value = null
  try {
    const res = await doctorApi.getLabTestTrend(pid, form.itemName.trim())
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      return
    }
    trend.value = res
  } finally {
    loading.value = false
  }
}

function handleReset() {
  form.patientId = ''
  form.itemName = ''
  trend.value = null
  queried.value = false
}
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

.trend-form {
  max-width: 500px;
}

.trend-section {
  margin-top: 16px;
}

.trend-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.unit {
  color: #909399;
  font-size: 13px;
  margin-left: 4px;
}

.chart {
  display: flex;
  align-items: flex-end;
  gap: 24px;
  height: 240px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
  overflow-x: auto;
  margin-bottom: 20px;
}

.bar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 64px;
  height: 100%;
  justify-content: flex-end;
}

.bar-label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 6px;
}

.bar {
  width: 36px;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s;
}

.bar-date {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
  text-align: center;
}

.trend-table {
  margin-top: 8px;
}
</style>
