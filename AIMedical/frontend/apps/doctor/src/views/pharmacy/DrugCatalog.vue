<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>药品目录</h2>
          <div class="header-actions">
            <el-input
              v-model="queryForm.drugCode"
              placeholder="药品编码"
              clearable
              size="default"
              style="width: 150px"
              @keyup.enter="loadList"
            />
            <el-input
              v-model="queryForm.drugName"
              placeholder="药品名称"
              clearable
              size="default"
              style="width: 160px"
              @keyup.enter="loadList"
            />
            <el-button :loading="loading" @click="loadList">查询</el-button>
            <el-button type="warning" @click="loadLowStock">低库存预警</el-button>
          </div>
        </div>
      </template>

      <el-table :data="pagedList" border style="width: 100%">
        <el-table-column label="药品编码" prop="drugCode" width="140" />
        <el-table-column label="药品名称" prop="drugName" min-width="160" />
        <el-table-column label="规格" prop="specification" width="120" />
        <el-table-column label="批号" prop="batchNo" width="120" />
        <el-table-column label="库存量" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'low-stock-text': isLowStock(row) }">{{ row.quantity }}</span>
            <span v-if="row.unit" class="unit-text"> / {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100" align="right">
          <template #default="{ row }">{{ formatAmount(row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column label="有效期至" width="130">
          <template #default="{ row }">{{ formatDate(row.expiryDate) }}</template>
        </el-table-column>
        <el-table-column label="预警阈值" prop="warningThreshold" width="100" align="center" />
        <el-table-column label="库存状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="isLowStock(row)" type="danger">低库存</el-tag>
            <el-tag v-else-if="isExpiringSoon(row)" type="warning">临期</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="handleView(row.drugCode)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="showingLowStock ? '暂无低库存药品' : '暂无库存记录'" />
        </template>
      </el-table>

      <div v-if="list.length > pageSize" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="list.length"
          layout="prev, pager, next, total"
          background
        />
      </div>
    </el-card>

    <!-- 库存详情对话框 -->
    <el-dialog v-model="detailVisible" title="库存详情" width="560px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="药品编码">{{ detail.drugCode }}</el-descriptions-item>
        <el-descriptions-item label="药品名称">{{ detail.drugName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ detail.specification || '—' }}</el-descriptions-item>
        <el-descriptions-item label="批号">{{ detail.batchNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="库存量">
          {{ detail.quantity }}{{ detail.unit ? ' / ' + detail.unit : '' }}
        </el-descriptions-item>
        <el-descriptions-item label="单价">{{ formatAmount(detail.unitPrice) }}</el-descriptions-item>
        <el-descriptions-item label="预警阈值">{{ detail.warningThreshold ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="有效期至">{{ formatDate(detail.expiryDate) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(detail.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pharmacyApi, isBusinessError } from '@aimedical/shared'
import type { PharmacyStockResponse } from '@aimedical/shared'

const loading = ref(false)
const list = ref<PharmacyStockResponse[]>([])
const showingLowStock = ref(false)

const queryForm = reactive({
  drugCode: '',
  drugName: '',
})

const currentPage = ref(1)
const pageSize = 10

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return list.value.slice(start, start + pageSize)
})

// ---- 查询库存 ----
async function loadList() {
  loading.value = true
  showingLowStock.value = false
  try {
    const params: Record<string, unknown> = {}
    if (queryForm.drugCode) params.drugCode = queryForm.drugCode
    if (queryForm.drugName) params.drugName = queryForm.drugName
    const result = await pharmacyApi.queryStock(params)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    list.value = result
    currentPage.value = 1
  } finally {
    loading.value = false
  }
}

// ---- 低库存预警 ----
async function loadLowStock() {
  loading.value = true
  showingLowStock.value = true
  try {
    const result = await pharmacyApi.listLowStock()
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    list.value = result
    currentPage.value = 1
    if (result.length === 0) {
      ElMessage.success('暂无低库存药品')
    } else {
      ElMessage.warning(`发现 ${result.length} 条低库存药品`)
    }
  } finally {
    loading.value = false
  }
}

// ---- 详情 ----
const detailVisible = ref(false)
const detail = ref<PharmacyStockResponse | null>(null)

async function handleView(drugCode: string) {
  loading.value = true
  try {
    const result = await pharmacyApi.getStock(drugCode)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    detail.value = result
    detailVisible.value = true
  } finally {
    loading.value = false
  }
}

// ---- 工具函数 ----
const formatDateTime = (iso: string | null | undefined): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

const formatDate = (iso: string | null | undefined): string =>
  iso ? new Date(iso).toLocaleDateString('zh-CN') : '—'

const formatAmount = (n: number | null | undefined): string => {
  if (n === null || n === undefined) return '—'
  return '¥' + Number(n).toFixed(2)
}

function isLowStock(row: PharmacyStockResponse): boolean {
  if (row.warningThreshold === null || row.warningThreshold === undefined) return false
  return row.quantity <= row.warningThreshold
}

function isExpiringSoon(row: PharmacyStockResponse): boolean {
  if (!row.expiryDate) return false
  const expiry = new Date(row.expiryDate).getTime()
  const now = Date.now()
  const thirtyDays = 30 * 24 * 60 * 60 * 1000
  return expiry - now < thirtyDays && expiry > now
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

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.low-stock-text {
  color: #f56c6c;
  font-weight: bold;
}

.unit-text {
  color: #909399;
  font-size: 12px;
}
</style>
