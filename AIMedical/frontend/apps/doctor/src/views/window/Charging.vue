<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>收费退费</h2>
          <div class="header-actions">
            <el-input-number
              v-model="patientId"
              :min="1"
              placeholder="患者ID"
              controls-position="right"
              style="width: 150px"
            />
            <el-button type="primary" @click="loadByPatient">查询患者缴费</el-button>
            <el-button :loading="loading" @click="loadByPatient">刷新</el-button>
            <el-button type="success" @click="openCreate">新建缴费单</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border style="width: 100%">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="缴费号" prop="payment_no" width="150">
          <template #default="{ row }">{{ row.payment_no || '—' }}</template>
        </el-table-column>
        <el-table-column label="患者ID" prop="patient_id" width="90" />
        <el-table-column label="患者姓名" width="110">
          <template #default="{ row }">{{ row.patient_name || '—' }}</template>
        </el-table-column>
        <el-table-column label="来源类型" width="110">
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
        <el-table-column label="支付方式" width="100">
          <template #default="{ row }">{{ paymentMethodLabel(row.payment_method) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="success"
              link
              @click="openPay(row)"
            >
              支付
            </el-button>
            <el-button
              v-if="row.status === 'PAID'"
              size="small"
              type="warning"
              link
              @click="openRefund(row)"
            >
              退费
            </el-button>
            <span v-if="row.status !== 'PENDING' && row.status !== 'PAID'" class="text-muted">—</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="请输入患者ID查询缴费记录" />
        </template>
      </el-table>
    </el-card>

    <!-- 创建缴费单对话框 -->
    <el-dialog v-model="createVisible" title="新建缴费单" width="760px" @closed="resetCreateForm">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="患者ID" prop="patient_id">
              <el-input-number
                v-model="createForm.patient_id"
                :min="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="患者姓名">
              <el-input v-model="createForm.patient_name" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="来源ID">
              <el-input-number
                v-model="createForm.source_id"
                :min="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源类型">
              <el-select v-model="createForm.source_type" placeholder="选填" clearable>
                <el-option label="挂号" value="REGISTRATION" />
                <el-option label="处方" value="PRESCRIPTION" />
                <el-option label="检查" value="EXAMINATION" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源单号">
              <el-input v-model="createForm.source_no" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="明细列表" required>
          <el-table :data="createForm.items" border size="small" style="width: 100%">
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                <el-input v-model="row.item_type" placeholder="如 挂号/药品" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="名称" min-width="150">
              <template #default="{ row }">
                <el-input v-model="row.item_name" placeholder="项目名称" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="数量" width="100">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  :controls="false"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.unit_price"
                  :min="0"
                  :precision="2"
                  :controls="false"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100" align="right">
              <template #default="{ row }">
                ¥{{ ((row.quantity ?? 1) * row.unit_price).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template #default="{ $index }">
                <el-button
                  size="small"
                  type="danger"
                  link
                  :disabled="createForm.items.length <= 1"
                  @click="removeItem($index)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button size="small" style="margin-top: 8px" @click="addItem">+ 添加明细</el-button>
        </el-form-item>

        <el-form-item label="总额">
          <span class="total-amount">¥{{ computedTotal.toFixed(2) }}</span>
          <span class="text-muted" style="margin-left: 8px">（按明细自动计算）</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 支付对话框 -->
    <el-dialog v-model="payVisible" title="支付" width="460px">
      <el-form v-if="payTarget" label-width="100px">
        <el-form-item label="缴费ID">
          <span>{{ payTarget.id }}</span>
        </el-form-item>
        <el-form-item label="应付总额">
          <span class="total-amount">¥{{ payTarget.total_amount }}</span>
        </el-form-item>
      </el-form>
      <el-form :model="payForm" label-width="100px" style="margin-top: 8px">
        <el-form-item label="支付方式" required>
          <el-select v-model="payForm.payment_method" placeholder="请选择支付方式">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银行卡" value="CARD" />
            <el-option label="医保" value="INSURANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款人">
          <el-input v-model="payForm.payer_name" placeholder="选填" />
        </el-form-item>
        <el-form-item label="支付金额" required>
          <el-input-number
            v-model="payForm.paid_amount"
            :min="0"
            :precision="2"
            controls-position="right"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="success" :loading="submitting" @click="submitPay">确认支付</el-button>
      </template>
    </el-dialog>

    <!-- 退费对话框 -->
    <el-dialog v-model="refundVisible" title="退费" width="460px">
      <el-form v-if="refundTarget" label-width="100px">
        <el-form-item label="缴费ID">
          <span>{{ refundTarget.id }}</span>
        </el-form-item>
        <el-form-item label="已付金额">
          <span class="total-amount">¥{{ refundTarget.paid_amount }}</span>
        </el-form-item>
      </el-form>
      <el-input
        v-model="refundReason"
        type="textarea"
        :rows="3"
        placeholder="请输入退费原因"
        style="margin-top: 8px"
      />
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="warning" :loading="submitting" @click="submitRefund">确认退费</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { isBusinessError } from '@aimedical/shared'
import { windowApi } from '@aimedical/shared'
import type {
  PayRequest,
  PaymentCreateRequest,
  PaymentItemRequest,
  PaymentRecordResponse,
  PaymentStatus,
} from '@aimedical/shared'

const loading = ref(false)
const submitting = ref(false)
const list = ref<PaymentRecordResponse[]>([])
const patientId = ref<number>(0)

async function loadByPatient() {
  if (!patientId.value || patientId.value <= 0) {
    ElMessage.warning('请输入患者ID')
    return
  }
  loading.value = true
  try {
    const result = await windowApi.queryPaymentsByPatient(patientId.value)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      list.value = []
      return
    }
    list.value = result
  } finally {
    loading.value = false
  }
}

// ---- 状态展示 ----

const statusLabel = (status: PaymentStatus): string => {
  const map: Record<PaymentStatus, string> = {
    PENDING: '待支付',
    PAID: '已支付',
    REFUNDED: '已退费',
    RECONCILED: '已对账',
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

// ---- 创建缴费单 ----

const createVisible = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive<PaymentCreateRequest>({
  patient_id: 0,
  patient_name: '',
  source_id: undefined,
  source_type: '',
  source_no: '',
  total_amount: 0,
  items: [makeEmptyItem()],
  remark: '',
})

function makeEmptyItem(): PaymentItemRequest {
  return {
    item_type: '',
    item_name: '',
    quantity: 1,
    unit_price: 0,
    remark: '',
  }
}

const createRules: FormRules = {
  patient_id: [{ required: true, message: '请输入患者ID', trigger: 'blur' }],
}

const computedTotal = computed(() => {
  return createForm.items.reduce((sum, item) => {
    const qty = item.quantity ?? 1
    return sum + qty * (item.unit_price || 0)
  }, 0)
})

function addItem() {
  createForm.items.push(makeEmptyItem())
}

function removeItem(index: number) {
  createForm.items.splice(index, 1)
}

function openCreate() {
  createVisible.value = true
}

function resetCreateForm() {
  createFormRef.value?.resetFields()
  createForm.patient_id = 0
  createForm.patient_name = ''
  createForm.source_id = undefined
  createForm.source_type = ''
  createForm.source_no = ''
  createForm.total_amount = 0
  createForm.items = [makeEmptyItem()]
  createForm.remark = ''
}

async function submitCreate() {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (createForm.items.length === 0) {
      ElMessage.warning('请至少添加一条明细')
      return
    }
    const invalid = createForm.items.some(
      (i) => !i.item_name || !i.item_type || i.unit_price < 0,
    )
    if (invalid) {
      ElMessage.warning('请完善明细的类型、名称和单价')
      return
    }
    submitting.value = true
    try {
      const payload: PaymentCreateRequest = {
        ...createForm,
        total_amount: Number(computedTotal.value.toFixed(2)),
      }
      const result = await windowApi.createPayment(payload)
      if (isBusinessError(result)) {
        ElMessage.error(result.message)
        return
      }
      ElMessage.success('缴费单创建成功')
      createVisible.value = false
      patientId.value = result.patient_id
      loadByPatient()
    } finally {
      submitting.value = false
    }
  })
}

// ---- 支付 ----

const payVisible = ref(false)
const payTarget = ref<PaymentRecordResponse | null>(null)
const payForm = reactive<PayRequest>({
  payment_method: '',
  payer_name: '',
  paid_amount: 0,
})

function openPay(row: PaymentRecordResponse) {
  payTarget.value = row
  payForm.payment_method = ''
  payForm.payer_name = ''
  payForm.paid_amount = row.total_amount
  payVisible.value = true
}

async function submitPay() {
  if (!payTarget.value) return
  if (!payForm.payment_method) {
    ElMessage.warning('请选择支付方式')
    return
  }
  submitting.value = true
  try {
    const result = await windowApi.payPayment(payTarget.value.id, {
      payment_method: payForm.payment_method,
      payer_name: payForm.payer_name || undefined,
      paid_amount: payForm.paid_amount,
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('支付成功')
    payVisible.value = false
    loadByPatient()
  } finally {
    submitting.value = false
  }
}

// ---- 退费 ----

const refundVisible = ref(false)
const refundTarget = ref<PaymentRecordResponse | null>(null)
const refundReason = ref('')

function openRefund(row: PaymentRecordResponse) {
  refundTarget.value = row
  refundReason.value = ''
  refundVisible.value = true
}

async function submitRefund() {
  if (!refundTarget.value) return
  if (!refundReason.value.trim()) {
    ElMessage.warning('请输入退费原因')
    return
  }
  submitting.value = true
  try {
    const result = await windowApi.refundPayment(refundTarget.value.id, {
      refund_reason: refundReason.value.trim(),
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('退费成功')
    refundVisible.value = false
    loadByPatient()
  } finally {
    submitting.value = false
  }
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

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.text-muted {
  color: #c0c4cc;
}

.total-amount {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}
</style>
