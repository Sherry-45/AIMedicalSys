<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>调拨管理</h2>
          <div class="header-actions">
            <el-select
              v-model="filterForm.status"
              placeholder="状态筛选"
              clearable
              style="width: 130px"
              @change="applyFilter"
            >
              <el-option label="全部" :value="''" />
              <el-option v-for="(label, val) in statusLabels" :key="val" :label="label" :value="val" />
            </el-select>
            <el-select
              v-model="filterForm.transferType"
              placeholder="类型筛选"
              clearable
              style="width: 130px"
              @change="applyFilter"
            >
              <el-option label="全部" :value="''" />
              <el-option v-for="(label, val) in typeLabels" :key="val" :label="label" :value="val" />
            </el-select>
            <el-button type="primary" @click="openCreate">新建调拨</el-button>
            <el-button :loading="loading" @click="loadList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border style="width: 100%">
        <el-table-column label="调拨单号" prop="transfer_no" width="160" />
        <el-table-column label="调拨类型" width="100" align="center">
          <template #default="{ row }">{{ typeLabel(row.transfer_type) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="调出部门" prop="source_dept" width="110" />
        <el-table-column label="调入部门" prop="target_dept" width="110" />
        <el-table-column label="明细数" width="80" align="center">
          <template #default="{ row }">{{ row.items?.length ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="创建人" prop="applicant_name" width="100" />
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'DRAFT'" size="small" type="success" link @click="doSubmit(row)">提交</el-button>
            <el-button v-if="row.status === 'PENDING_APPROVAL'" size="small" type="success" link @click="doApprove(row)">通过</el-button>
            <el-button v-if="row.status === 'PENDING_APPROVAL'" size="small" type="danger" link @click="doReject(row)">驳回</el-button>
            <el-button v-if="row.status === 'APPROVED'" size="small" type="primary" link @click="doShip(row)">发货</el-button>
            <el-button v-if="row.status === 'IN_TRANSIT'" size="small" type="success" link @click="doReceive(row)">接收</el-button>
            <el-button
              v-if="['DRAFT', 'PENDING_APPROVAL', 'APPROVED'].includes(row.status)"
              size="small" type="info" link @click="doCancel(row)">取消</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无调拨记录" />
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

    <!-- 创建调拨单对话框 -->
    <el-dialog v-model="createVisible" title="新建调拨单" width="850px">
      <el-form :model="createForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="调拨类型">
              <el-select v-model="createForm.transfer_type" style="width: 100%">
                <el-option v-for="(label, val) in typeLabels" :key="val" :label="label" :value="val" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="调出部门">
              <el-input v-model="createForm.source_dept" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="调入部门">
              <el-input v-model="createForm.target_dept" placeholder="可选" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" placeholder="可选" />
        </el-form-item>
        <el-form-item label="调拨明细">
          <el-button type="primary" size="small" @click="addCreateItem">添加明细</el-button>
          <el-table :data="createForm.items" border size="small" style="margin-top: 8px">
            <el-table-column label="药品编码" width="130">
              <template #default="{ row }">
                <el-input v-model="row.drug_code" size="small" placeholder="必填" />
              </template>
            </el-table-column>
            <el-table-column label="药品名称" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row.drug_name" size="small" placeholder="可选" />
              </template>
            </el-table-column>
            <el-table-column label="规格" width="110">
              <template #default="{ row }">
                <el-input v-model="row.specification" size="small" placeholder="可选" />
              </template>
            </el-table-column>
            <el-table-column label="批次号" width="120">
              <template #default="{ row }">
                <el-input v-model="row.batch_no" size="small" placeholder="可选" />
              </template>
            </el-table-column>
            <el-table-column label="数量" width="110">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" size="small" controls-position="right" style="width: 90px" />
              </template>
            </el-table-column>
            <el-table-column label="单位" width="70">
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

    <!-- 调拨详情对话框 -->
    <el-dialog v-model="detailVisible" title="调拨详情" width="800px" v-loading="detailLoading">
      <template v-if="detail">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="调拨单号">{{ detail.transfer_no ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="调拨类型">{{ typeLabel(detail.transfer_type) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="调出部门">{{ detail.source_dept ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="调入部门">{{ detail.target_dept ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detail.applicant_name ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="审批人">{{ detail.approver_name ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(detail.created_at) }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detail.remark ?? '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.reject_reason" label="驳回原因" :span="3">
            {{ detail.reject_reason }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 16px 0 8px">调拨明细</h4>
        <el-table :data="detail.items ?? []" border size="small">
          <el-table-column label="药品编码" prop="drug_code" width="130" />
          <el-table-column label="药品名称" prop="drug_name" min-width="140" show-overflow-tooltip />
          <el-table-column label="规格" prop="specification" width="110" show-overflow-tooltip />
          <el-table-column label="批次号" prop="batch_no" width="120" />
          <el-table-column label="数量" prop="quantity" width="80" align="center" />
          <el-table-column label="单位" prop="unit" width="70" align="center" />
          <el-table-column label="单价" width="90" align="right">
            <template #default="{ row }">{{ formatPrice(row.unit_price) }}</template>
          </el-table-column>
        </el-table>
      </template>
      <template #footer>
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
  TransferItemRequest,
  TransferOrderResponse,
} from '@aimedical/shared'

const loading = ref(false)
const list = ref<TransferOrderResponse[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filterForm = reactive({
  status: '',
  transferType: '',
})

const statusLabels: Record<string, string> = {
  DRAFT: '草稿',
  PENDING_APPROVAL: '待审批',
  APPROVED: '已审批',
  IN_TRANSIT: '在途',
  RECEIVED: '已接收',
  REJECTED: '已驳回',
  CANCELLED: '已取消',
}

const statusLabel = (status: string): string => statusLabels[status] ?? status

const statusTagType = (status: string): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<string, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    DRAFT: 'info',
    PENDING_APPROVAL: 'warning',
    APPROVED: 'success',
    IN_TRANSIT: 'primary',
    RECEIVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info',
  }
  return map[status] ?? 'info'
}

const typeLabels: Record<string, string> = {
  IN: '入库',
  OUT: '出库',
  DEPT: '科室调拨',
  RETURN: '退库',
}

const typeLabel = (type?: string): string => (type ? (typeLabels[type] ?? type) : '—')

const formatDateTime = (iso: string | null | undefined): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

const formatPrice = (val: number | null | undefined): string => {
  if (val === null || val === undefined) return '—'
  return `¥${val.toFixed(2)}`
}

function applyFilter() {
  currentPage.value = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const result = await inventoryApi.queryTransfer({
      status: filterForm.status || undefined,
      transferType: filterForm.transferType || undefined,
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

// ---- 创建调拨 ----

const createVisible = ref(false)
const createLoading = ref(false)
const createForm = reactive<{
  transfer_type: string
  source_dept: string
  target_dept: string
  remark: string
  items: TransferItemRequest[]
}>({
  transfer_type: 'DEPT',
  source_dept: '',
  target_dept: '',
  remark: '',
  items: [],
})

function openCreate() {
  createForm.transfer_type = 'DEPT'
  createForm.source_dept = ''
  createForm.target_dept = ''
  createForm.remark = ''
  createForm.items = []
  addCreateItem()
  createVisible.value = true
}

function addCreateItem() {
  createForm.items.push({
    drug_code: '',
    drug_name: '',
    specification: '',
    batch_no: '',
    quantity: 1,
    unit: '',
  })
}

function removeCreateItem(index: number) {
  createForm.items.splice(index, 1)
}

async function submitCreate() {
  if (createForm.items.length === 0) {
    ElMessage.warning('请至少添加一条调拨明细')
    return
  }
  const missingCode = createForm.items.some((item) => !item.drug_code.trim())
  if (missingCode) {
    ElMessage.warning('药品编码不能为空')
    return
  }
  createLoading.value = true
  try {
    const result = await inventoryApi.createTransfer({
      transfer_type: createForm.transfer_type,
      source_dept: createForm.source_dept || undefined,
      target_dept: createForm.target_dept || undefined,
      remark: createForm.remark || undefined,
      items: createForm.items,
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('调拨单创建成功')
    createVisible.value = false
    loadList()
  } finally {
    createLoading.value = false
  }
}

// ---- 调拨详情 ----

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<TransferOrderResponse | null>(null)

async function viewDetail(row: TransferOrderResponse) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const result = await inventoryApi.getTransfer(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    detail.value = result
  } finally {
    detailLoading.value = false
  }
}

// ---- 状态操作 ----

async function doSubmit(row: TransferOrderResponse) {
  try {
    await ElMessageBox.confirm('确认提交调拨单？', '提示', { type: 'info' })
  } catch {
    return
  }
  const result = await inventoryApi.submitTransfer(row.id)
  if (isBusinessError(result)) {
    ElMessage.error(result.message)
    return
  }
  ElMessage.success('调拨单已提交')
  loadList()
}

async function doApprove(row: TransferOrderResponse) {
  try {
    await ElMessageBox.confirm('确认审批通过？', '提示', { type: 'success' })
  } catch {
    return
  }
  const result = await inventoryApi.approveTransfer(row.id, { approved: true })
  if (isBusinessError(result)) {
    ElMessage.error(result.message)
    return
  }
  ElMessage.success('审批通过')
  loadList()
}

async function doReject(row: TransferOrderResponse) {
  let rejectReason = ''
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回调拨', {
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
  const result = await inventoryApi.approveTransfer(row.id, {
    approved: false,
    reject_reason: rejectReason,
  })
  if (isBusinessError(result)) {
    ElMessage.error(result.message)
    return
  }
  ElMessage.success('已驳回')
  loadList()
}

async function doShip(row: TransferOrderResponse) {
  try {
    await ElMessageBox.confirm('确认发货？', '提示', { type: 'info' })
  } catch {
    return
  }
  const result = await inventoryApi.shipTransfer(row.id)
  if (isBusinessError(result)) {
    ElMessage.error(result.message)
    return
  }
  ElMessage.success('已发货')
  loadList()
}

async function doReceive(row: TransferOrderResponse) {
  try {
    await ElMessageBox.confirm('确认接收？', '提示', { type: 'success' })
  } catch {
    return
  }
  const result = await inventoryApi.receiveTransfer(row.id)
  if (isBusinessError(result)) {
    ElMessage.error(result.message)
    return
  }
  ElMessage.success('已接收')
  loadList()
}

async function doCancel(row: TransferOrderResponse) {
  try {
    await ElMessageBox.confirm('确认取消调拨单？此操作不可撤销。', '警告', { type: 'warning' })
  } catch {
    return
  }
  const result = await inventoryApi.cancelTransfer(row.id)
  if (isBusinessError(result)) {
    ElMessage.error(result.message)
    return
  }
  ElMessage.success('调拨单已取消')
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
