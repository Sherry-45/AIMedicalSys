<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>退药处理</h2>
          <div class="header-actions">
            <el-input
              v-model.number="queryForm.patientId"
              placeholder="患者ID"
              clearable
              size="default"
              style="width: 130px"
              @keyup.enter="search"
            />
            <el-select
              v-model="queryForm.status"
              placeholder="状态筛选"
              clearable
              size="default"
              style="width: 140px"
              @change="search"
            >
              <el-option label="全部" :value="''" />
              <el-option label="待处理" value="PENDING" />
              <el-option label="已退药" value="REFUNDED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="default"
              style="width: 260px"
              value-format="YYYY-MM-DD"
              @change="onDateChange"
            />
            <el-button :loading="loading" @click="search">查询</el-button>
            <el-button type="primary" @click="openCreateDialog">新建退药</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border style="width: 100%">
        <el-table-column label="退药单号" prop="refund_no" width="160">
          <template #default="{ row }">{{ row.refund_no || row.id }}</template>
        </el-table-column>
        <el-table-column label="关联发药ID" prop="dispensing_id" width="120" align="center" />
        <el-table-column label="患者" width="130">
          <template #default="{ row }">
            {{ row.patient_name || (row.patient_id ? '#' + row.patient_id : '—') }}
          </template>
        </el-table-column>
        <el-table-column label="退药明细" width="90" align="center">
          <template #default="{ row }">{{ row.items?.length ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="退款金额" width="110" align="right">
          <template #default="{ row }">{{ formatAmount(row.total_amount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="退药原因" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.refund_reason || '—' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="success"
              @click="handleApprove(row.id)"
            >通过</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="danger"
              @click="openRejectDialog(row.id)"
            >驳回</el-button>
            <el-button size="small" link @click="handleView(row.id)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无退药记录" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
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

    <!-- 新建退药对话框 -->
    <el-dialog v-model="createVisible" title="新建退药单" width="720px" @closed="resetCreateForm">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="关联发药ID" required>
          <el-input
            v-model.number="createForm.dispensing_id"
            placeholder="请输入发药单ID"
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="退药原因">
          <el-input
            v-model="createForm.refund_reason"
            type="textarea"
            :rows="2"
            placeholder="可选"
          />
        </el-form-item>

        <el-form-item label="退药明细" required>
          <el-table :data="createForm.items" border size="small" style="width: 100%">
            <el-table-column label="发药明细ID" width="140">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.dispensing_item_id"
                  :min="1"
                  size="small"
                  controls-position="right"
                  style="width: 130px"
                />
              </template>
            </el-table-column>
            <el-table-column label="药品编码" width="130">
              <template #default="{ row }">
                <el-input v-model="row.drug_code" size="small" placeholder="药品编码" />
              </template>
            </el-table-column>
            <el-table-column label="药品名称" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.drug_name" size="small" placeholder="药品名称" />
              </template>
            </el-table-column>
            <el-table-column label="批号" width="120">
              <template #default="{ row }">
                <el-input v-model="row.batch_no" size="small" placeholder="批号" />
              </template>
            </el-table-column>
            <el-table-column label="数量" width="100">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  size="small"
                  controls-position="right"
                  style="width: 90px"
                />
              </template>
            </el-table-column>
            <el-table-column label="单位" width="90">
              <template #default="{ row }">
                <el-input v-model="row.unit" size="small" placeholder="单位" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center" fixed="right">
              <template #default="{ $index }">
                <el-button size="small" type="danger" link @click="removeItem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button
            size="small"
            type="primary"
            plain
            style="margin-top: 8px"
            @click="addItem"
          >+ 添加明细</el-button>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="createForm.remark"
            type="textarea"
            :rows="2"
            placeholder="可选"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">提交退药</el-button>
      </template>
    </el-dialog>

    <!-- 驳回原因对话框 -->
    <el-dialog v-model="rejectVisible" title="驳回退药" width="460px">
      <el-form label-width="80px">
        <el-form-item label="驳回原因" required>
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="3"
            placeholder="请输入驳回原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <!-- 退药详情对话框 -->
    <el-dialog v-model="detailVisible" title="退药单详情" width="640px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="退药单号">{{ detail.refund_no || detail.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关联发药ID">{{ detail.dispensing_id }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patient_name || (detail.patient_id ? '#' + detail.patient_id : '—') }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">{{ formatAmount(detail.total_amount) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.pharmacist_name || '—' }}</el-descriptions-item>
        <el-descriptions-item label="退药原因" :span="2">{{ detail.refund_reason || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detail.created_at) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(detail.updated_at) }}</el-descriptions-item>
      </el-descriptions>
      <el-table
        v-if="detail?.items?.length"
        :data="detail.items"
        border
        size="small"
        style="margin-top: 12px"
      >
        <el-table-column label="发药明细ID" prop="dispensing_item_id" width="130" />
        <el-table-column label="药品编码" prop="drug_code" width="130" />
        <el-table-column label="药品名称" prop="drug_name" min-width="140" />
        <el-table-column label="数量" prop="quantity" width="80" align="center" />
        <el-table-column label="单位" prop="unit" width="80" />
        <el-table-column label="单价" width="90" align="right">
          <template #default="{ row }">{{ formatAmount(row.unit_price) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pharmacyApi, isBusinessError } from '@aimedical/shared'
import type {
  PharmacyRefundResponse,
  PharmacyRefundCreateRequest,
  PharmacyRefundItemRequest,
} from '@aimedical/shared'

const loading = ref(false)
const submitting = ref(false)
const list = ref<PharmacyRefundResponse[]>([])

const queryForm = reactive({
  patientId: undefined as number | undefined,
  status: '',
  startTime: '' as string | undefined,
  endTime: '' as string | undefined,
})

const dateRange = ref<[string, string] | null>(null)

function onDateChange(val: [string, string] | null) {
  queryForm.startTime = val?.[0] || undefined
  queryForm.endTime = val?.[1] || undefined
}

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// ---- 新建退药 ----
const createVisible = ref(false)
const createForm = reactive<PharmacyRefundCreateRequest>({
  dispensing_id: 0,
  refund_reason: '',
  items: [],
  remark: '',
})

function openCreateDialog() {
  resetCreateForm()
  createVisible.value = true
}

function resetCreateForm() {
  createForm.dispensing_id = 0
  createForm.refund_reason = ''
  createForm.items = []
  createForm.remark = ''
}

function addItem() {
  createForm.items.push({
    dispensing_item_id: 0,
    drug_code: '',
    drug_name: '',
    batch_no: '',
    quantity: 1,
    unit: '',
  } as PharmacyRefundItemRequest)
}

function removeItem(index: number) {
  createForm.items.splice(index, 1)
}

async function submitCreate() {
  if (!createForm.dispensing_id) {
    ElMessage.warning('请填写关联发药ID')
    return
  }
  if (createForm.items.length === 0) {
    ElMessage.warning('请至少添加一条退药明细')
    return
  }
  const invalid = createForm.items.some((it) => !it.dispensing_item_id || !it.drug_code || !it.drug_name || !it.quantity)
  if (invalid) {
    ElMessage.warning('请完善退药明细（明细ID/编码/名称/数量必填）')
    return
  }

  submitting.value = true
  try {
    const result = await pharmacyApi.createRefund(createForm)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('退药单创建成功')
    createVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

// ---- 审批操作 ----
async function handleApprove(id: number) {
  try {
    await ElMessageBox.confirm('确认通过该退药申请？', '提示', { type: 'success' })
  } catch {
    return
  }
  loading.value = true
  try {
    const result = await pharmacyApi.approveRefund(id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('已通过退药申请')
    loadList()
  } finally {
    loading.value = false
  }
}

// ---- 驳回操作 ----
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTargetId = ref(0)

function openRejectDialog(id: number) {
  rejectReason.value = ''
  rejectTargetId.value = id
  rejectVisible.value = true
}

async function submitReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  submitting.value = true
  try {
    const result = await pharmacyApi.rejectRefund(rejectTargetId.value, rejectReason.value.trim())
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('已驳回退药申请')
    rejectVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

// ---- 详情 ----
const detailVisible = ref(false)
const detail = ref<PharmacyRefundResponse | null>(null)

async function handleView(id: number) {
  loading.value = true
  try {
    const result = await pharmacyApi.getRefund(id)
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

// ---- 查询 ----
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
  try {
    const result = await pharmacyApi.queryRefund({
      patientId: queryForm.patientId || undefined,
      status: queryForm.status || undefined,
      startTime: queryForm.startTime || undefined,
      endTime: queryForm.endTime || undefined,
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

// ---- 工具函数 ----
const formatDateTime = (iso: string | null | undefined): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

const formatAmount = (n: number | null | undefined): string => {
  if (n === null || n === undefined) return '—'
  return '¥' + Number(n).toFixed(2)
}

const statusLabel = (status: string): string => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    REFUNDED: '已退药',
    REJECTED: '已驳回',
  }
  return map[status] || status
}

const statusTagType = (status: string): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<string, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    PENDING: 'warning',
    REFUNDED: 'success',
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

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
