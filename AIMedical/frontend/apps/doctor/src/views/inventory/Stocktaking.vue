<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>盘点管理</h2>
          <div class="header-actions">
            <el-select
              v-model="filterForm.status"
              placeholder="状态筛选"
              clearable
              style="width: 140px"
              @change="applyFilter"
            >
              <el-option label="全部" :value="''" />
              <el-option v-for="(label, val) in statusLabels" :key="val" :label="label" :value="val" />
            </el-select>
            <el-button type="primary" @click="openCreate">新建盘点</el-button>
            <el-button :loading="loading" @click="loadList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border style="width: 100%">
        <el-table-column label="盘点单号" prop="stocktaking_no" width="160" />
        <el-table-column label="盘点类型" width="100" align="center">
          <template #default="{ row }">{{ typeLabel(row.stocktaking_type) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作人" prop="operator_name" width="100" />
        <el-table-column label="明细数" width="80" align="center">
          <template #default="{ row }">{{ row.items?.length ?? row.total_items ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.start_time) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.end_time) }}</template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'DRAFT'" size="small" type="success" link @click="doStart(row)">开始</el-button>
            <el-button v-if="row.status === 'IN_PROGRESS'" size="small" type="warning" link @click="doSubmit(row)">提交审批</el-button>
            <el-button v-if="row.status === 'PENDING_APPROVAL'" size="small" type="success" link @click="doApprove(row)">通过</el-button>
            <el-button v-if="row.status === 'PENDING_APPROVAL'" size="small" type="danger" link @click="doReject(row)">驳回</el-button>
            <el-button v-if="row.status === 'APPROVED'" size="small" type="primary" link @click="doComplete(row)">完成</el-button>
            <el-button
              v-if="['DRAFT', 'IN_PROGRESS', 'PENDING_APPROVAL', 'APPROVED'].includes(row.status)"
              size="small" type="info" link @click="doCancel(row)">取消</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无盘点记录" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          background
          @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 创建盘点单对话框 -->
    <el-dialog v-model="createVisible" title="新建盘点单" width="800px">
      <el-form :model="createForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="盘点类型">
              <el-select v-model="createForm.stocktaking_type" placeholder="请选择" style="width: 100%">
                <el-option label="全面盘点" value="FULL" />
                <el-option label="部分盘点" value="PARTIAL" />
                <el-option label="抽样盘点" value="SPOT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="createForm.remark" placeholder="可选" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="盘点明细">
          <el-button type="primary" size="small" @click="addCreateItem">添加明细</el-button>
          <el-table :data="createForm.items" border size="small" style="margin-top: 8px">
            <el-table-column label="药品编码" width="140">
              <template #default="{ row }">
                <el-input v-model="row.drug_code" size="small" placeholder="必填" />
              </template>
            </el-table-column>
            <el-table-column label="药品名称" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.drug_name" size="small" placeholder="可选" />
              </template>
            </el-table-column>
            <el-table-column label="批次号" width="130">
              <template #default="{ row }">
                <el-input v-model="row.batch_no" size="small" placeholder="可选" />
              </template>
            </el-table-column>
            <el-table-column label="账面数量" width="110">
              <template #default="{ row }">
                <el-input-number v-model="row.book_quantity" :min="0" size="small" controls-position="right" style="width: 90px" />
              </template>
            </el-table-column>
            <el-table-column label="单位" width="80">
              <template #default="{ row }">
                <el-input v-model="row.unit" size="small" placeholder="可选" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template #default="{ $index }">
                <el-button size="small" type="danger" link @click="removeCreateItem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- 盘点详情对话框 -->
    <el-dialog v-model="detailVisible" title="盘点详情" width="900px" v-loading="detailLoading">
      <template v-if="detail">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="盘点单号">{{ detail.stocktaking_no ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="盘点类型">{{ typeLabel(detail.stocktaking_type) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作人">{{ detail.operator_name ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(detail.start_time) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatDateTime(detail.end_time) }}</el-descriptions-item>
          <el-descriptions-item label="审批人">{{ detail.approver_name ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="审批时间">{{ formatDateTime(detail.approved_at) }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detail.remark ?? '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.reject_reason" label="驳回原因" :span="3">
            {{ detail.reject_reason }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 16px 0 8px">盘点明细</h4>
        <el-table :data="detailItems" border size="small">
          <el-table-column label="药品编码" prop="drug_code" width="130" />
          <el-table-column label="药品名称" prop="drug_name" min-width="140" show-overflow-tooltip />
          <el-table-column label="批次号" prop="batch_no" width="120" />
          <el-table-column label="账面数量" prop="book_quantity" width="90" align="center" />
          <el-table-column label="实际数量" width="120" align="center">
            <template #default="{ row }">
              <el-input-number
                v-if="detail.status === 'IN_PROGRESS'"
                v-model="row.actual_quantity"
                :min="0"
                size="small"
                controls-position="right"
                style="width: 100px"
              />
              <span v-else>{{ row.actual_quantity ?? '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单位" prop="unit" width="70" align="center" />
          <el-table-column label="差异" width="80" align="center">
            <template #default="{ row }">
              <span v-if="row.actual_quantity != null && row.book_quantity != null">
                {{ row.actual_quantity - row.book_quantity }}
              </span>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" min-width="100" show-overflow-tooltip />
        </el-table>
      </template>
      <template #footer>
        <el-button
          v-if="detail?.status === 'IN_PROGRESS'"
          type="primary"
          :loading="saveItemsLoading"
          @click="submitItems">保存明细</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { isBusinessError } from '@aimedical/shared'
import { inventoryApi } from '@aimedical/shared'
import type {
  StocktakingItem,
  StocktakingItemRequest,
  StocktakingResponse,
  StocktakingStatus,
} from '@aimedical/shared'

const loading = ref(false)
const list = ref<StocktakingResponse[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filterForm = reactive({
  status: '',
})

const statusLabels: Record<string, string> = {
  DRAFT: '草稿',
  IN_PROGRESS: '进行中',
  PENDING_APPROVAL: '待审批',
  APPROVED: '已审批',
  REJECTED: '已驳回',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

const statusLabel = (status: string): string => statusLabels[status] ?? status

const statusTagType = (status: string): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<string, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    DRAFT: 'info',
    IN_PROGRESS: 'primary',
    PENDING_APPROVAL: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    COMPLETED: 'success',
    CANCELLED: 'info',
  }
  return map[status] ?? 'info'
}

const typeLabels: Record<string, string> = {
  FULL: '全面盘点',
  PARTIAL: '部分盘点',
  SPOT: '抽样盘点',
}

const typeLabel = (type?: string): string => (type ? (typeLabels[type] ?? type) : '—')

const formatDateTime = (iso: string | null | undefined): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

function applyFilter() {
  currentPage.value = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const result = await inventoryApi.queryStocktaking({
      status: filterForm.status || undefined,
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

// ---- 创建盘点 ----

const createVisible = ref(false)
const createLoading = ref(false)
const createForm = reactive<{
  stocktaking_type: string
  remark: string
  items: StocktakingItemRequest[]
}>({
  stocktaking_type: 'FULL',
  remark: '',
  items: [],
})

function openCreate() {
  createForm.stocktaking_type = 'FULL'
  createForm.remark = ''
  createForm.items = []
  addCreateItem()
  createVisible.value = true
}

function addCreateItem() {
  createForm.items.push({
    drug_code: '',
    drug_name: '',
    batch_no: '',
    book_quantity: 0,
    unit: '',
  })
}

function removeCreateItem(index: number) {
  createForm.items.splice(index, 1)
}

async function submitCreate() {
  if (createForm.items.length === 0) {
    ElMessage.warning('请至少添加一条盘点明细')
    return
  }
  const missingCode = createForm.items.some((item) => !item.drug_code.trim())
  if (missingCode) {
    ElMessage.warning('药品编码不能为空')
    return
  }
  createLoading.value = true
  try {
    const result = await inventoryApi.createStocktaking({
      stocktaking_type: createForm.stocktaking_type || undefined,
      remark: createForm.remark || undefined,
      items: createForm.items,
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('盘点单创建成功')
    createVisible.value = false
    loadList()
  } finally {
    createLoading.value = false
  }
}

// ---- 盘点详情 ----

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<StocktakingResponse | null>(null)
const detailItems = ref<StocktakingItem[]>([])
const saveItemsLoading = ref(false)

async function viewDetail(row: StocktakingResponse) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const result = await inventoryApi.getStocktaking(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    detail.value = result
    detailItems.value = (result.items ?? []).map((item) => ({ ...item }))
  } finally {
    detailLoading.value = false
  }
}

async function submitItems() {
  if (!detail.value) return
  saveItemsLoading.value = true
  try {
    const items: StocktakingItemRequest[] = detailItems.value.map((item) => ({
      drug_code: item.drug_code,
      drug_name: item.drug_name,
      batch_no: item.batch_no,
      book_quantity: item.book_quantity,
      actual_quantity: item.actual_quantity,
      unit: item.unit,
      remark: item.remark,
    }))
    const result = await inventoryApi.submitStocktakingItems(detail.value.id, items)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('明细保存成功')
    detail.value = result
    detailItems.value = (result.items ?? []).map((item) => ({ ...item }))
    loadList()
  } finally {
    saveItemsLoading.value = false
  }
}

// ---- 状态操作 ----

async function doStart(row: StocktakingResponse) {
  try {
    await ElMessageBox.confirm('确认开始盘点？', '提示', { type: 'info' })
  } catch {
    return
  }
  const result = await inventoryApi.startStocktaking(row.id)
  if (isBusinessError(result)) {
    ElMessage.error(result.message)
    return
  }
  ElMessage.success('盘点已开始')
  loadList()
}

async function doSubmit(row: StocktakingResponse) {
  try {
    await ElMessageBox.confirm('确认提交审批？提交后将无法修改明细。', '提示', { type: 'warning' })
  } catch {
    return
  }
  const result = await inventoryApi.submitStocktakingForApproval(row.id)
  if (isBusinessError(result)) {
    ElMessage.error(result.message)
    return
  }
  ElMessage.success('已提交审批')
  loadList()
}

async function doApprove(row: StocktakingResponse) {
  try {
    await ElMessageBox.confirm('确认审批通过？', '提示', { type: 'success' })
  } catch {
    return
  }
  const result = await inventoryApi.approveStocktaking(row.id)
  if (isBusinessError(result)) {
    ElMessage.error(result.message)
    return
  }
  ElMessage.success('审批通过')
  loadList()
}

async function doReject(row: StocktakingResponse) {
  let rejectReason = ''
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回盘点', {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入驳回原因',
      inputValidator: (input: string) => {
        if (!input || !input.trim()) return '驳回原因不能为空'
        return true
      },
    })
    rejectReason = value
  } catch {
    return
  }
  const result = await inventoryApi.rejectStocktaking(row.id, {
    reject_reason: rejectReason,
  })
  if (isBusinessError(result)) {
    ElMessage.error(result.message)
    return
  }
  ElMessage.success('已驳回')
  loadList()
}

async function doComplete(row: StocktakingResponse) {
  try {
    await ElMessageBox.confirm('确认完成盘点？', '提示', { type: 'success' })
  } catch {
    return
  }
  const result = await inventoryApi.completeStocktaking(row.id)
  if (isBusinessError(result)) {
    ElMessage.error(result.message)
    return
  }
  ElMessage.success('盘点已完成')
  loadList()
}

async function doCancel(row: StocktakingResponse) {
  try {
    await ElMessageBox.confirm('确认取消盘点？此操作不可撤销。', '警告', { type: 'warning' })
  } catch {
    return
  }
  const result = await inventoryApi.cancelStocktaking(row.id)
  if (isBusinessError(result)) {
    ElMessage.error(result.message)
    return
  }
  ElMessage.success('盘点已取消')
  loadList()
}

// 初始化加载
loadList()
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
</style>
