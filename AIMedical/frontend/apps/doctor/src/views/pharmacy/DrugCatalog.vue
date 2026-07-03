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
              @keyup.enter="search"
            />
            <el-input
              v-model="queryForm.drugName"
              placeholder="药品名称"
              clearable
              size="default"
              style="width: 160px"
              @keyup.enter="search"
            />
            <el-button :loading="loading" @click="search">查询</el-button>
            <el-button type="warning" @click="loadLowStock">低库存预警</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border style="width: 100%">
        <el-table-column label="药品编码" prop="drug_code" width="140" />
        <el-table-column label="药品名称" prop="drug_name" min-width="160" />
        <el-table-column label="规格" prop="specification" width="120" />
        <el-table-column label="批号" prop="batch_no" width="120" />
        <el-table-column label="库存量" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'low-stock-text': isLowStock(row) }">{{ row.quantity }}</span>
            <span v-if="row.unit" class="unit-text"> / {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100" align="right">
          <template #default="{ row }">{{ formatAmount(row.retail_price) }}</template>
        </el-table-column>
        <el-table-column label="有效期至" width="130">
          <template #default="{ row }">{{ formatDate(row.expiry_date) }}</template>
        </el-table-column>
        <el-table-column label="预警阈值" prop="safety_stock" width="100" align="center" />
        <el-table-column label="库存状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="isLowStock(row)" type="danger">低库存</el-tag>
            <el-tag v-else-if="isExpiringSoon(row)" type="warning">临期</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="handleView(row.drug_code)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="showingLowStock ? '暂无低库存药品' : '暂无库存记录'" />
        </template>
      </el-table>

      <div v-if="!showingLowStock" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="loadList"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <!-- 库存详情对话框 -->
    <el-dialog v-model="detailVisible" title="库存详情" width="560px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="药品编码">{{ detail.drug_code }}</el-descriptions-item>
        <el-descriptions-item label="药品名称">{{ detail.drug_name || '—' }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ detail.specification || '—' }}</el-descriptions-item>
        <el-descriptions-item label="批号">{{ detail.batch_no || '—' }}</el-descriptions-item>
        <el-descriptions-item label="库存量">
          {{ detail.quantity }}{{ detail.unit ? ' / ' + detail.unit : '' }}
        </el-descriptions-item>
        <el-descriptions-item label="单价">{{ formatAmount(detail.retail_price) }}</el-descriptions-item>
        <el-descriptions-item label="预警阈值">{{ detail.safety_stock ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="有效期至">{{ formatDate(detail.expiry_date) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detail.created_at) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(detail.updated_at) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
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
const pageSize = ref(10)
const total = ref(0)

// ---- 查询库存 ----
function search() {
  currentPage.value = 1
  loadList()
}

function onSizeChange() {
  currentPage.value = 1
  loadList()
}

async function loadList() {
  loading.value = true
  showingLowStock.value = false
  try {
    const result = await pharmacyApi.queryStock({
      drugCode: queryForm.drugCode || undefined,
      drugName: queryForm.drugName || undefined,
      page: currentPage.value - 1,
      size: pageSize.value,
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    list.value = result.content ?? []
    total.value = result.totalElements ?? 0
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
  if (row.safety_stock === null || row.safety_stock === undefined) return false
  return row.quantity <= row.safety_stock
}

function isExpiringSoon(row: PharmacyStockResponse): boolean {
  if (!row.expiry_date) return false
  const expiry = new Date(row.expiry_date).getTime()
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
