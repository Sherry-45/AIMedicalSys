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
              @keyup.enter="loadList"
            />
            <el-select
              v-model="queryForm.status"
              placeholder="状态筛选"
              clearable
              size="default"
              style="width: 140px"
              @change="loadList"
            >
              <el-option label="全部" :value="''" />
              <el-option label="待审批" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
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
            <el-button :loading="loading" @click="loadList">查询</el-button>
            <el-button type="primary" @click="openCreateDialog">新建退药</el-button>
          </div>
        </div>
      </template>

      <el-table :data="pagedList" border style="width: 100%">
        <el-table-column label="退药单号" prop="refundNo" width="160">
          <template #default="{ row }">{{ row.refundNo || row.id }}</template>
        </el-table-column>
        <el-table-column label="关联发药ID" prop="dispensingId" width="120" align="center" />
        <el-table-column label="患者" width="130">
          <template #default="{ row }">
            {{ row.patientName || (row.patientId ? '#' + row.patientId : '—') }}
          </template>
        </el-table-column>
        <el-table-column label="退药明细" width="90" align="center">
          <template #default="{ row }">{{ row.items?.length ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="退款金额" width="110" align="right">
          <template #default="{ row }">{{ formatAmount(row.totalRefundAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="退药原因" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.refundReason || '—' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
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

    <!-- 新建退药对话框 -->
    <el-dialog v-model="createVisible" title="新建退药单" width="720px" @closed="resetCreateForm">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="关联发药ID" required>
          <el-input
            v-model.number="createForm.dispensingId"
            placeholder="请输入发药单ID"
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="退药原因">
          <el-input
            v-model="createForm.refundReason"
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
                  v-model="row.dispensingItemId"
                  :min="1"
                  size="small"
                  controls-position="right"
                  style="width: 130px"
                />
              </template>
            </el-table-column>
            <el-table-column label="药品编码" width="130">
              <template #default="{ row }">
                <el-input v-model="row.drugCode" size="small" placeholder="药品编码" />
              </template>
            </el-table-column>
            <el-table-column label="药品名称" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.drugName" size="small" placeholder="药品名称" />
              </template>
            </el-table-column>
            <el-table-column label="批号" width="120">
              <template #default="{ row }">
                <el-input v-model="row.batchNo" size="small" placeholder="批号" />
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
        <el-descriptions-item label="退药单号">{{ detail.refundNo || detail.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关联发药ID">{{ detail.dispensingId }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName || (detail.patientId ? '#' + detail.patientId : '—') }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">{{ formatAmount(detail.totalRefundAmount) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operatorName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="退药原因" :span="2">{{ detail.refundReason || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(detail.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
      <el-table
        v-if="detail?.items?.length"
        :data="detail.items"
        border
        size="small"
        style="margin-top: 12px"
      >
        <el-table-column label="发药明细ID" prop="dispensingItemId" width="130" />
        <el-table-column label="药品编码" prop="drugCode" width="130" />
        <el-table-column label="药品名称" prop="drugName" min-width="140" />
        <el-table-column label="数量" prop="quantity" width="80" align="center" />
        <el-table-column label="单位" prop="unit" width="80" />
        <el-table-column label="单价" width="90" align="right">
          <template #default="{ row }">{{ formatAmount(row.unitPrice) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
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
const pageSize = 10

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return list.value.slice(start, start + pageSize)
})

// ---- 新建退药 ----
const createVisible = ref(false)
const createForm = reactive<PharmacyRefundCreateRequest>({
  dispensingId: 0,
  refundReason: '',
  items: [],
  remark: '',
})

function openCreateDialog() {
  resetCreateForm()
  createVisible.value = true
}

function resetCreateForm() {
  createForm.dispensingId = 0
  createForm.refundReason = ''
  createForm.items = []
  createForm.remark = ''
}

function addItem() {
  createForm.items.push({
    dispensingItemId: 0,
    drugCode: '',
    drugName: '',
    batchNo: '',
    quantity: 1,
    unit: '',
  } as PharmacyRefundItemRequest)
}

function removeItem(index: number) {
  createForm.items.splice(index, 1)
}

async function submitCreate() {
  if (!createForm.dispensingId) {
    ElMessage.warning('请填写关联发药ID')
    return
  }
  if (createForm.items.length === 0) {
    ElMessage.warning('请至少添加一条退药明细')
    return
  }
  const invalid = createForm.items.some((it) => !it.dispensingItemId || !it.drugCode || !it.drugName || !it.quantity)
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
async function loadList() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {}
    if (queryForm.patientId) params.patientId = queryForm.patientId
    if (queryForm.status) params.status = queryForm.status
    if (queryForm.startTime) params.startTime = queryForm.startTime
    if (queryForm.endTime) params.endTime = queryForm.endTime
    const result = await pharmacyApi.queryRefund(params)
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

// ---- 工具函数 ----
const formatDateTime = (iso: string | null | undefined): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

const formatAmount = (n: number | null | undefined): string => {
  if (n === null || n === undefined) return '—'
  return '¥' + Number(n).toFixed(2)
}

const statusLabel = (status: string): string => {
  const map: Record<string, string> = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已驳回',
  }
  return map[status] || status
}

const statusTagType = (status: string): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<string, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    PENDING: 'warning',
    APPROVED: 'success',
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
