<template>
  <div class="page-container">
    <!-- 库存查询 -->
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>药库库存</h2>
          <div class="header-actions">
            <el-input
              v-model="filterForm.drugCode"
              placeholder="药品编码"
              clearable
              style="width: 160px"
              @keyup.enter="applyFilter"
            />
            <el-input
              v-model="filterForm.batchNo"
              placeholder="批次号"
              clearable
              style="width: 160px"
              @keyup.enter="applyFilter"
            />
            <el-button type="primary" @click="applyFilter">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
            <el-button :loading="loading" @click="loadStock">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="stockList" border style="width: 100%">
        <el-table-column label="药品编码" prop="drug_code" width="130" />
        <el-table-column label="药品名称" prop="drug_name" min-width="160" show-overflow-tooltip />
        <el-table-column label="规格" prop="specification" width="120" show-overflow-tooltip />
        <el-table-column label="批次号" prop="batch_no" width="130" />
        <el-table-column label="库存量" prop="quantity" width="90" align="center" />
        <el-table-column label="单位" prop="unit" width="70" align="center" />
        <el-table-column label="单价" width="90" align="right">
          <template #default="{ row }">{{ formatPrice(row.unit_price) }}</template>
        </el-table-column>
        <el-table-column label="生产厂家" prop="manufacturer" width="140" show-overflow-tooltip />
        <el-table-column label="有效期至" width="120">
          <template #default="{ row }">{{ formatDate(row.expiry_date) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.enabled" size="small" type="success">启用</el-tag>
            <el-tag v-else size="small" type="info">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openAdjust(row)">调整</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无库存记录" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          background
          @current-change="loadStock"
        />
      </div>
    </el-card>

    <!-- 低库存预警 -->
    <el-card class="alert-card" v-loading="lowStockLoading">
      <template #header>
        <div class="card-header">
          <h3>低库存预警</h3>
          <el-tag type="danger" size="small">{{ lowStockList.length }} 种</el-tag>
        </div>
      </template>
      <el-table :data="lowStockList" border size="small">
        <el-table-column label="药品编码" prop="drug_code" width="130" />
        <el-table-column label="药品名称" prop="drug_name" min-width="160" show-overflow-tooltip />
        <el-table-column label="规格" prop="specification" width="120" show-overflow-tooltip />
        <el-table-column label="批次号" prop="batch_no" width="130" />
        <el-table-column label="库存量" prop="quantity" width="90" align="center" />
        <el-table-column label="单位" prop="unit" width="70" align="center" />
        <template #empty>
          <el-empty description="暂无低库存预警" :image-size="60" />
        </template>
      </el-table>
    </el-card>

    <!-- 近效期药品 -->
    <el-card class="alert-card" v-loading="expiringLoading">
      <template #header>
        <div class="card-header">
          <h3>近效期药品（{{ expiringDays }} 天内）</h3>
          <div class="header-actions">
            <el-input-number
              v-model="expiringDays"
              :min="1"
              :max="365"
              size="small"
              style="width: 110px"
              @change="loadExpiring"
            />
            <el-tag type="warning" size="small">{{ expiringList.length }} 种</el-tag>
          </div>
        </div>
      </template>
      <el-table :data="expiringList" border size="small">
        <el-table-column label="药品编码" prop="drug_code" width="130" />
        <el-table-column label="药品名称" prop="drug_name" min-width="160" show-overflow-tooltip />
        <el-table-column label="规格" prop="specification" width="120" show-overflow-tooltip />
        <el-table-column label="批次号" prop="batch_no" width="130" />
        <el-table-column label="库存量" prop="quantity" width="90" align="center" />
        <el-table-column label="有效期至" width="120">
          <template #default="{ row }">
            <span :class="{ 'text-danger': isExpiringSoon(row.expiry_date) }">
              {{ formatDate(row.expiry_date) }}
            </span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无近效期药品" :image-size="60" />
        </template>
      </el-table>
    </el-card>

    <!-- 库存调整对话框 -->
    <el-dialog v-model="adjustVisible" title="库存调整" width="500px">
      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="药品编码">
          <el-input v-model="adjustForm.drug_code" disabled />
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="adjustForm.batch_no" disabled />
        </el-form-item>
        <el-form-item label="调整数量">
          <el-input-number v-model="adjustForm.quantity" :step="1" style="width: 100%" />
          <div class="form-tip">正数增加库存，负数减少库存</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="adjustForm.remark" type="textarea" :rows="3" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" :loading="adjustLoading" @click="submitAdjust">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { isBusinessError } from '@aimedical/shared'
import { inventoryApi } from '@aimedical/shared'
import type { InventoryStockResponse, StockAdjustRequest } from '@aimedical/shared'

const loading = ref(false)
const stockList = ref<InventoryStockResponse[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filterForm = reactive({
  drugCode: '',
  batchNo: '',
})

const lowStockLoading = ref(false)
const lowStockList = ref<InventoryStockResponse[]>([])

const expiringLoading = ref(false)
const expiringList = ref<InventoryStockResponse[]>([])
const expiringDays = ref(30)

const adjustVisible = ref(false)
const adjustLoading = ref(false)
const adjustForm = reactive<StockAdjustRequest>({
  drug_code: '',
  batch_no: '',
  quantity: 0,
  remark: '',
})

const formatDate = (iso: string | null | undefined): string =>
  iso ? new Date(iso).toLocaleDateString('zh-CN') : '—'

const formatPrice = (val: number | null | undefined): string => {
  if (val === null || val === undefined) return '—'
  return `¥${val.toFixed(2)}`
}

const isExpiringSoon = (iso: string | null | undefined): boolean => {
  if (!iso) return false
  const diff = new Date(iso).getTime() - Date.now()
  return diff <= 7 * 24 * 60 * 60 * 1000
}

function applyFilter() {
  currentPage.value = 1
  loadStock()
}

function resetFilter() {
  filterForm.drugCode = ''
  filterForm.batchNo = ''
  currentPage.value = 1
  loadStock()
}

async function loadStock() {
  loading.value = true
  try {
    const result = await inventoryApi.queryStock({
      drugCode: filterForm.drugCode || undefined,
      batchNo: filterForm.batchNo || undefined,
      page: currentPage.value - 1,
      size: pageSize.value,
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    stockList.value = result.content ?? []
    total.value = result.total_elements ?? 0
  } finally {
    loading.value = false
  }
}

async function loadLowStock() {
  lowStockLoading.value = true
  try {
    const result = await inventoryApi.listLowStock()
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    lowStockList.value = result
  } finally {
    lowStockLoading.value = false
  }
}

async function loadExpiring() {
  expiringLoading.value = true
  try {
    const result = await inventoryApi.listExpiringSoon(expiringDays.value)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    expiringList.value = result
  } finally {
    expiringLoading.value = false
  }
}

function openAdjust(row: InventoryStockResponse) {
  adjustForm.drug_code = row.drug_code
  adjustForm.batch_no = row.batch_no ?? ''
  adjustForm.quantity = 0
  adjustForm.remark = ''
  adjustVisible.value = true
}

async function submitAdjust() {
  if (!adjustForm.drug_code || !adjustForm.batch_no) {
    ElMessage.warning('药品编码和批次号不能为空')
    return
  }
  if (adjustForm.quantity === 0) {
    ElMessage.warning('调整数量不能为 0')
    return
  }
  adjustLoading.value = true
  try {
    const result = await inventoryApi.adjustStock({
      drug_code: adjustForm.drug_code,
      batch_no: adjustForm.batch_no,
      quantity: adjustForm.quantity,
      remark: adjustForm.remark || undefined,
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('库存调整成功')
    adjustVisible.value = false
    loadStock()
    loadLowStock()
  } finally {
    adjustLoading.value = false
  }
}

onMounted(() => {
  loadStock()
  loadLowStock()
  loadExpiring()
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

.card-header h3 {
  margin: 0;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.alert-card {
  margin-top: 16px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
