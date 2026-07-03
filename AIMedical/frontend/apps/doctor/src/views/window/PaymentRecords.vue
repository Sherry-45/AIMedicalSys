<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>缴费记录</h2>
          <div class="header-actions">
            <el-input
              v-model.number="filter.patientId"
              placeholder="患者ID"
              clearable
              size="default"
              style="width: 120px"
              @keyup.enter="applyFilter"
            />
            <el-select
              v-model="filter.status"
              placeholder="状态"
              clearable
              size="default"
              style="width: 130px"
              @change="applyFilter"
            >
              <el-option label="待支付" value="PENDING" />
              <el-option label="已支付" value="PAID" />
              <el-option label="已退费" value="REFUNDED" />
              <el-option label="已对账" value="RECONCILED" />
            </el-select>
            <el-select
              v-model="filter.sourceType"
              placeholder="来源类型"
              clearable
              size="default"
              style="width: 140px"
              @change="applyFilter"
            >
              <el-option label="挂号" value="REGISTRATION" />
              <el-option label="处方" value="PRESCRIPTION" />
              <el-option label="检查" value="EXAMINATION" />
              <el-option label="其他" value="OTHER" />
            </el-select>
            <el-button type="primary" @click="applyFilter">查询</el-button>
            <el-button :loading="loading" @click="loadList">刷新</el-button>
            <el-button
              type="warning"
              :disabled="selectedRows.length === 0"
              @click="openReconcile"
            >
              对账（{{ selectedRows.length }}）
            </el-button>
            <el-input
              v-model="batchNoQuery"
              placeholder="按批次号查询"
              clearable
              size="default"
              style="width: 180px"
              @keyup.enter="loadByBatch"
            />
            <el-button @click="loadByBatch">查批次</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="list"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="45" :selectable="canSelectRow" />
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="缴费号" prop="payment_no" width="150">
          <template #default="{ row }">{{ row.payment_no || '—' }}</template>
        </el-table-column>
        <el-table-column label="患者ID" prop="patient_id" width="90" />
        <el-table-column label="患者姓名" width="110">
          <template #default="{ row }">{{ row.patient_name || '—' }}</template>
        </el-table-column>
        <el-table-column label="来源类型" width="100">
          <template #default="{ row }">{{ row.source_type || '—' }}</template>
        </el-table-column>
        <el-table-column label="来源单号" width="130">
          <template #default="{ row }">{{ row.source_no || '—' }}</template>
        </el-table-column>
        <el-table-column label="总额" width="100" align="right">
          <template #default="{ row }">¥{{ row.total_amount }}</template>
        </el-table-column>
        <el-table-column label="已付" width="100" align="right">
          <template #default="{ row }">
            {{ row.paid_amount != null ? '¥' + row.paid_amount : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="90">
          <template #default="{ row }">{{ paymentMethodLabel(row.payment_method) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="对账批次" width="130">
          <template #default="{ row }">{{ row.reconcile_batch_no || '—' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无缴费记录" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadList"
          @size-change="loadList"
        />
      </div>
    </el-card>

    <!-- 缴费详情对话框 -->
    <el-dialog v-model="detailVisible" title="缴费详情" width="720px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="缴费ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="缴费号">{{ detail.payment_no || '—' }}</el-descriptions-item>
          <el-descriptions-item label="患者ID">{{ detail.patient_id }}</el-descriptions-item>
          <el-descriptions-item label="患者姓名">{{ detail.patient_name || '—' }}</el-descriptions-item>
          <el-descriptions-item label="来源类型">{{ detail.source_type || '—' }}</el-descriptions-item>
          <el-descriptions-item label="来源单号">{{ detail.source_no || '—' }}</el-descriptions-item>
          <el-descriptions-item label="来源ID">{{ detail.source_id || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总额">¥{{ detail.total_amount }}</el-descriptions-item>
          <el-descriptions-item label="已付">
            {{ detail.paid_amount != null ? '¥' + detail.paid_amount : '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">
            {{ paymentMethodLabel(detail.payment_method) }}
          </el-descriptions-item>
          <el-descriptions-item label="付款人">{{ detail.payer_name || '—' }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">
            {{ formatDateTime(detail.paid_at) }}
          </el-descriptions-item>
          <el-descriptions-item label="退费金额">
            {{ detail.refund_amount != null ? '¥' + detail.refund_amount : '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="退费原因" :span="2">
            {{ detail.refund_reason || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="退费时间">
            {{ formatDateTime(detail.refunded_at) }}
          </el-descriptions-item>
          <el-descriptions-item label="是否对账">
            {{ detail.reconciled_at ? '是' : '否' }}
          </el-descriptions-item>
          <el-descriptions-item label="对账批次">
            {{ detail.reconcile_batch_no || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="对账时间">
            {{ formatDateTime(detail.reconciled_at) }}
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
        </el-descriptions>

        <h4 class="section-title">缴费明细</h4>
        <el-table :data="detail.items || []" border size="small">
          <el-table-column label="类型" prop="item_type" />
          <el-table-column label="名称" prop="item_name" min-width="150" />
          <el-table-column label="数量" prop="quantity" width="80" align="center" />
          <el-table-column label="单价" width="100" align="right">
            <template #default="{ row }">¥{{ row.unit_price }}</template>
          </el-table-column>
          <el-table-column label="小计" width="110" align="right">
            <template #default="{ row }">
              ¥{{ ((row.quantity ?? 1) * row.unit_price).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark">
            <template #default="{ row }">{{ row.remark || '—' }}</template>
          </el-table-column>
          <template #empty>
            <el-empty description="无明细" :image-size="40" />
          </template>
        </el-table>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 对账对话框 -->
    <el-dialog v-model="reconcileVisible" title="提交对账" width="480px">
      <el-form label-width="100px">
        <el-form-item label="已选记录">
          <span>{{ selectedRows.length }} 条</span>
        </el-form-item>
        <el-form-item label="总金额">
          <span class="total-amount">¥{{ reconcileTotal.toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="对账批次号">
          <el-input v-model="reconcileBatchNo" placeholder="选填，留空由系统生成" />
        </el-form-item>
        <el-form-item label="选中明细">
          <el-table :data="selectedRows" border size="small" max-height="200">
            <el-table-column label="ID" prop="id" width="70" />
            <el-table-column label="缴费号" prop="payment_no" />
            <el-table-column label="金额" width="100" align="right">
              <template #default="{ row }">¥{{ row.total_amount }}</template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reconcileVisible = false">取消</el-button>
        <el-button type="warning" :loading="submitting" @click="submitReconcile">提交对账</el-button>
      </template>
    </el-dialog>

    <!-- 批次查询结果对话框 -->
    <el-dialog v-model="batchVisible" title="批次对账记录" width="820px">
      <el-table :data="batchList" border size="small">
        <el-table-column label="ID" prop="id" width="70" />
        <el-table-column label="缴费号" prop="payment_no" width="140" />
        <el-table-column label="患者ID" prop="patient_id" width="80" />
        <el-table-column label="总额" width="90" align="right">
          <template #default="{ row }">¥{{ row.total_amount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="对账时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.reconciled_at) }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="该批次无记录" />
        </template>
      </el-table>
      <template #footer>
        <el-button @click="batchVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { isBusinessError } from '@aimedical/shared'
import { windowApi } from '@aimedical/shared'
import type {
  PaymentQueryRequest,
  PaymentRecordResponse,
  PaymentStatus,
} from '@aimedical/shared'

const loading = ref(false)
const submitting = ref(false)
const list = ref<PaymentRecordResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRows = ref<PaymentRecordResponse[]>([])

const filter = reactive({
  patientId: undefined as number | undefined,
  status: '' as PaymentStatus | '',
  sourceType: '',
})

const batchNoQuery = ref('')

function buildQuery(): PaymentQueryRequest {
  return {
    patientId: filter.patientId || undefined,
    status: filter.status || undefined,
    sourceType: filter.sourceType || undefined,
    page: currentPage.value - 1,
    size: pageSize.value,
  }
}

function applyFilter() {
  currentPage.value = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const result = await windowApi.queryPayments(buildQuery())
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      list.value = []
      total.value = 0
      return
    }
    list.value = result.content ?? []
    total.value = result.totalElements ?? 0
  } finally {
    loading.value = false
  }
}

function handleSelectionChange(rows: PaymentRecordResponse[]) {
  selectedRows.value = rows
}

// 只有已支付的记录可被选中对账
function canSelectRow(row: PaymentRecordResponse): boolean {
  return row.status === 'PAID'
}

// ---- 状态展示 ----

const statusLabel = (status: PaymentStatus): string => {
  const map: Record<PaymentStatus, string> = {
    PENDING: '待支付',
    PAID: '已支付',
    REFUNDED: '已退费',
    RECONCILED: '已对账',
    CANCELLED: '已取消',
  }
  return map[status] || status
}

const statusTagType = (
  status: PaymentStatus,
): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<PaymentStatus, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    PENDING: 'warning',
    PAID: 'success',
    REFUNDED: 'danger',
    RECONCILED: 'primary',
    CANCELLED: 'info',
  }
  return map[status] || 'info'
}

const paymentMethodLabel = (method: string | null): string => {
  if (!method) return '—'
  const map: Record<string, string> = {
    CASH: '现金',
    WECHAT: '微信',
    ALIPAY: '支付宝',
    CARD: '银行卡',
    INSURANCE: '医保',
  }
  return map[method] || method
}

const formatDateTime = (iso: string | null): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

// ---- 详情 ----

const detailVisible = ref(false)
const detail = ref<PaymentRecordResponse | null>(null)

async function openDetail(row: PaymentRecordResponse) {
  detailVisible.value = true
  detail.value = null
  loading.value = true
  try {
    const result = await windowApi.getPayment(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      detailVisible.value = false
      return
    }
    detail.value = result
  } finally {
    loading.value = false
  }
}

// ---- 对账 ----

const reconcileVisible = ref(false)
const reconcileBatchNo = ref('')

const reconcileTotal = computed(() => {
  return selectedRows.value.reduce((sum, r) => sum + (r.total_amount || 0), 0)
})

function openReconcile() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  reconcileBatchNo.value = ''
  reconcileVisible.value = true
}

async function submitReconcile() {
  submitting.value = true
  try {
    const result = await windowApi.reconcile({
      payment_ids: selectedRows.value.map((r) => r.id),
      reconcile_batch_no: reconcileBatchNo.value.trim() || undefined,
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success(
      `对账成功，批次号：${result.reconcile_batch_no ?? '—'}，共 ${result.reconciled_count ?? 0} 条`,
    )
    reconcileVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

// ---- 按批次号查询 ----

const batchVisible = ref(false)
const batchList = ref<PaymentRecordResponse[]>([])

async function loadByBatch() {
  if (!batchNoQuery.value.trim()) {
    ElMessage.warning('请输入批次号')
    return
  }
  loading.value = true
  try {
    const result = await windowApi.listByBatchNo(batchNoQuery.value.trim())
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    batchList.value = result
    batchVisible.value = true
  } finally {
    loading.value = false
  }
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
  flex-wrap: wrap;
  gap: 8px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.text-muted {
  color: #c0c4cc;
}

.total-amount {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.section-title {
  margin: 16px 0 8px;
  font-size: 15px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
