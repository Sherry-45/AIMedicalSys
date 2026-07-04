<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>医嘱管理</h2>
          <div class="header-actions">
            <el-radio-group v-model="queryScope" size="default" @change="handleScopeChange">
              <el-radio-button label="doctor">我的医嘱</el-radio-button>
              <el-radio-button label="patient">按患者查询</el-radio-button>
            </el-radio-group>
            <el-input
              v-if="queryScope === 'patient'"
              v-model="patientIdInput"
              placeholder="患者 ID"
              clearable
              size="default"
              style="width: 140px"
              @keyup.enter="loadOrders"
            />
            <el-select
              v-model="statusFilter"
              placeholder="状态筛选"
              clearable
              size="default"
              style="width: 140px"
              @change="applyFilter"
            >
              <el-option label="全部" :value="''" />
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已提交" value="SUBMITTED" />
              <el-option label="已收费" value="CHARGED" />
              <el-option label="已发药" value="DISPENSED" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
            <el-button type="primary" @click="openCreateDialog">新建医嘱</el-button>
            <el-button :loading="loading" @click="loadOrders">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredList" border style="width: 100%" @row-click="openDetail">
        <el-table-column label="医嘱号" prop="order_no" width="160" />
        <el-table-column label="患者ID" prop="patient_id" width="100" align="center" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="orderTypeTagType(row.order_type)">{{ orderTypeLabel(row.order_type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.order_status)">{{ statusLabel(row.order_status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="诊断" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.diagnosis || '—' }}</template>
        </el-table-column>
        <el-table-column label="项目数" width="90" align="center">
          <template #default="{ row }">{{ row.items?.length ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">{{ formatAmount(row.total_amount) }}</template>
        </el-table-column>
        <el-table-column label="紧急" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.is_urgent" size="small" type="danger">急</el-tag>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click.stop="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.order_status === 'DRAFT'"
              size="small"
              type="success"
              link
              @click.stop="handleSubmit(row)"
            >提交</el-button>
            <el-button
              v-if="['DRAFT', 'SUBMITTED'].includes(row.order_status)"
              size="small"
              type="warning"
              link
              @click.stop="handleCancel(row)"
            >取消</el-button>
            <el-button
              v-if="row.order_status === 'DISPENSED'"
              size="small"
              type="success"
              link
              @click.stop="handleComplete(row)"
            >完成</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无医嘱记录" />
        </template>
      </el-table>
    </el-card>

    <!-- 医嘱详情对话框 -->
    <el-dialog v-model="detailVisible" title="医嘱详情" width="720px" :close-on-click-modal="false">
      <div v-if="detailLoading" v-loading="true" style="height: 200px"></div>
      <div v-else-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="医嘱号">{{ detail.order_no }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.order_status)">{{ statusLabel(detail.order_status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="类型">{{ orderTypeLabel(detail.order_type) }}</el-descriptions-item>
          <el-descriptions-item label="紧急">
            <el-tag v-if="detail.is_urgent" size="small" type="danger">急</el-tag>
            <span v-else>否</span>
          </el-descriptions-item>
          <el-descriptions-item label="患者ID">{{ detail.patient_id }}</el-descriptions-item>
          <el-descriptions-item label="医生ID">{{ detail.doctor_id }}</el-descriptions-item>
          <el-descriptions-item label="挂号ID">{{ detail.registration_id }}</el-descriptions-item>
          <el-descriptions-item label="总金额">{{ formatAmount(detail.total_amount) }}</el-descriptions-item>
          <el-descriptions-item label="诊断" :span="2">{{ detail.diagnosis || '—' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '—' }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 16px 0 8px">医嘱明细</h4>
        <el-table :data="detail.items" border size="small">
          <el-table-column label="类型" prop="item_type" width="90" />
          <el-table-column label="编码" prop="item_code" width="120" />
          <el-table-column label="名称" prop="item_name" min-width="160" />
          <el-table-column label="规格" prop="specification" width="120" />
          <el-table-column label="数量" prop="quantity" width="80" align="right" />
          <el-table-column label="单位" prop="unit" width="80" />
          <el-table-column label="单价" prop="unit_price" width="100" align="right" />
          <el-table-column label="金额" prop="amount" width="100" align="right" />
          <el-table-column v-if="detail.order_type === 'DRUG'" label="用法" width="240">
            <template #default="{ row }">
              <span v-if="row.dosage || row.usage_method || row.frequency">
                {{ [row.dosage, row.usage_method, row.frequency].filter(Boolean).join(' / ') }}
                <span v-if="row.days">× {{ row.days }}天</span>
              </span>
              <span v-else>—</span>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top: 16px; text-align: right">
          <el-button
            v-if="detail.order_status === 'DRAFT'"
            type="success"
            @click="handleSubmit(detail)"
          >提交医嘱</el-button>
          <el-button
            v-if="['DRAFT', 'SUBMITTED'].includes(detail.order_status)"
            type="warning"
            @click="handleCancel(detail)"
          >取消医嘱</el-button>
          <el-button
            v-if="detail.order_status === 'DISPENSED'"
            type="success"
            @click="handleComplete(detail)"
          >完成医嘱</el-button>
          <el-button
            v-if="detail.order_type === 'DRUG' && detail.order_status === 'SUBMITTED'"
            @click="handleGenerateChargePre(detail)"
          >生成预收费</el-button>
          <el-button
            v-if="detail.order_type === 'DRUG' && ['SUBMITTED', 'CHARGED'].includes(detail.order_status)"
            @click="handleViewMedicationContract(detail)"
          >查看发药合同</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 预收费订单对话框 -->
    <el-dialog v-model="chargePreVisible" title="预收费订单" width="600px">
      <div v-if="chargePreLoading" v-loading="true" style="height: 200px"></div>
      <div v-else-if="chargePre">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预收费号">{{ chargePre.charge_no }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ chargePre.charge_status }}</el-descriptions-item>
          <el-descriptions-item label="总金额" :span="2">{{ formatAmount(chargePre.total_amount) }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="chargePre.items" border size="small" style="margin-top: 12px">
          <el-table-column label="类型" prop="charge_item_type" width="100" />
          <el-table-column label="编码" prop="charge_item_code" width="120" />
          <el-table-column label="名称" prop="charge_item_name" min-width="160" />
          <el-table-column label="数量" prop="quantity" width="80" align="right" />
          <el-table-column label="单价" prop="unit_price" width="100" align="right" />
          <el-table-column label="金额" prop="amount" width="100" align="right" />
        </el-table>
      </div>
    </el-dialog>

    <!-- 发药合同对话框 -->
    <el-dialog v-model="medicationVisible" title="发药合同" width="600px">
      <div v-if="medicationLoading" v-loading="true" style="height: 200px"></div>
      <div v-else-if="medication">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="医嘱号">{{ medication.order_no }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ medication.patient_name }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ medication.doctor_name }}</el-descriptions-item>
          <el-descriptions-item label="紧急">
            <el-tag v-if="medication.is_urgent" size="small" type="danger">急</el-tag>
            <span v-else>否</span>
          </el-descriptions-item>
          <el-descriptions-item label="诊断" :span="2">{{ medication.diagnosis || '—' }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="medication.items" border size="small" style="margin-top: 12px">
          <el-table-column label="编码" prop="item_code" width="120" />
          <el-table-column label="名称" prop="item_name" min-width="160" />
          <el-table-column label="规格" prop="specification" width="120" />
          <el-table-column label="数量" prop="quantity" width="80" align="right" />
          <el-table-column label="单位" prop="unit" width="80" />
          <el-table-column label="用法" min-width="200">
            <template #default="{ row }">
              <span v-if="row.dosage || row.usage_method || row.frequency">
                {{ [row.dosage, row.usage_method, row.frequency].filter(Boolean).join(' / ') }}
                <span v-if="row.days">× {{ row.days }}天</span>
              </span>
              <span v-else>—</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 新建医嘱对话框 -->
    <el-dialog v-model="createVisible" title="新建医嘱" width="800px" :close-on-click-modal="false">
      <el-form :model="createForm" label-position="top">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="患者ID" required>
              <el-input v-model.number="createForm.patient_id" type="number" placeholder="患者ID" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="挂号ID" required>
              <el-input v-model.number="createForm.registration_id" type="number" placeholder="挂号记录ID" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="医嘱类型" required>
              <el-select v-model="createForm.order_type" style="width: 100%">
                <el-option label="药品" value="DRUG" />
                <el-option label="检查" value="EXAMINATION" />
                <el-option label="检验" value="LAB_TEST" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="诊断">
          <el-input v-model="createForm.diagnosis" placeholder="诊断" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="备注" />
        </el-form-item>
        <el-form-item label="紧急">
          <el-switch v-model="createForm.is_urgent" />
        </el-form-item>

        <h4>医嘱明细</h4>
        <el-button size="small" type="primary" @click="addItem">+ 添加明细</el-button>
        <el-table :data="createForm.items" border size="small" style="margin-top: 8px">
          <el-table-column label="类型" width="110">
            <template #default="{ row }">
              <el-select v-model="row.item_type" size="small">
                <el-option label="药品" value="DRUG" />
                <el-option label="检查" value="EXAMINATION" />
                <el-option label="检验" value="LAB_TEST" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="编码" width="120">
            <template #default="{ row }">
              <el-input v-model="row.item_code" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="名称" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.item_name" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="规格" width="120">
            <template #default="{ row }">
              <el-input v-model="row.specification" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="数量" width="90">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" :precision="2" size="small" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80">
            <template #default="{ row }">
              <el-input v-model="row.unit" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="110">
            <template #default="{ row }">
              <el-input-number v-model="row.unit_price" :min="0" :precision="2" size="small" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }">
              <el-button size="small" type="danger" link @click="removeItem($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  doctorApi,
  isBusinessError,
  MEDICAL_ORDER_STATUS_LABELS,
  MEDICAL_ORDER_STATUS_TAG_TYPES,
  MEDICAL_ORDER_TYPE_LABELS,
  MEDICAL_ORDER_TYPE_TAG_TYPES,
  labelOf,
  tagTypeOf,
} from '@aimedical/shared'
import type {
  ChargePreOrderDTO,
  MedicalOrderDTO,
  MedicalOrderItemDTO,
  MedicationOrderDTO,
} from '@aimedical/shared'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const orderList = ref<MedicalOrderDTO[]>([])
const queryScope = ref<'doctor' | 'patient'>('doctor')
const patientIdInput = ref('')
const statusFilter = ref('')

// 当前医生ID（从认证Store获取，与 Dashboard 一致）
const currentDoctorId = computed(() => authStore.user?.id ?? null)

const filteredList = computed(() => {
  if (!statusFilter.value) return orderList.value
  return orderList.value.filter((o) => o.order_status === statusFilter.value)
})

function applyFilter() {
  // 仅前端筛选，无需重新拉取
}

function handleScopeChange() {
  if (queryScope.value === 'doctor') {
    loadOrders()
  }
}

async function loadOrders() {
  loading.value = true
  try {
    let res
    if (queryScope.value === 'doctor') {
      const did = currentDoctorId.value
      if (!did) {
        ElMessage.warning('无法获取当前医生ID，请重新登录')
        loading.value = false
        return
      }
      res = await doctorApi.listMedicalOrdersByDoctor(did, { page: 0, size: 50 })
    } else {
      const pid = Number(patientIdInput.value)
      if (!patientIdInput.value.trim() || Number.isNaN(pid)) {
        ElMessage.warning('请输入有效的患者ID')
        loading.value = false
        return
      }
      res = await doctorApi.listMedicalOrdersByPatient(pid, { page: 0, size: 50 })
    }

    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      return
    }
    orderList.value = res.content || []
  } finally {
    loading.value = false
  }
}

// ---- 详情对话框 ----
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<MedicalOrderDTO | null>(null)

async function openDetail(row: MedicalOrderDTO) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await doctorApi.getMedicalOrder(row.id)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      detailVisible.value = false
      return
    }
    detail.value = res
  } finally {
    detailLoading.value = false
  }
}

// ---- 状态转换操作 ----
async function handleSubmit(order: MedicalOrderDTO) {
  try {
    await ElMessageBox.confirm(`确认提交医嘱 ${order.order_no}？提交后将无法修改`, '确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  const res = await doctorApi.submitMedicalOrder(order.id)
  if (isBusinessError(res)) {
    ElMessage.error(res.message)
    return
  }
  ElMessage.success('医嘱已提交')
  detail.value = res
  await loadOrders()
}

async function handleCancel(order: MedicalOrderDTO) {
  try {
    await ElMessageBox.confirm(`确认取消医嘱 ${order.order_no}？`, '确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  const res = await doctorApi.cancelMedicalOrder(order.id)
  if (isBusinessError(res)) {
    ElMessage.error(res.message)
    return
  }
  ElMessage.success('医嘱已取消')
  detail.value = res
  await loadOrders()
}

async function handleComplete(order: MedicalOrderDTO) {
  try {
    await ElMessageBox.confirm(`确认完成医嘱 ${order.order_no}？`, '确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  const res = await doctorApi.completeMedicalOrder(order.id)
  if (isBusinessError(res)) {
    ElMessage.error(res.message)
    return
  }
  ElMessage.success('医嘱已完成')
  detail.value = res
  await loadOrders()
}

// ---- 预收费 ----
const chargePreVisible = ref(false)
const chargePreLoading = ref(false)
const chargePre = ref<ChargePreOrderDTO | null>(null)

async function handleGenerateChargePre(order: MedicalOrderDTO) {
  chargePreVisible.value = true
  chargePreLoading.value = true
  chargePre.value = null
  try {
    const res = await doctorApi.generateChargePreOrder(order.id)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      chargePreVisible.value = false
      return
    }
    chargePre.value = res
  } finally {
    chargePreLoading.value = false
  }
}

// ---- 发药合同 ----
const medicationVisible = ref(false)
const medicationLoading = ref(false)
const medication = ref<MedicationOrderDTO | null>(null)

async function handleViewMedicationContract(order: MedicalOrderDTO) {
  medicationVisible.value = true
  medicationLoading.value = true
  medication.value = null
  try {
    const res = await doctorApi.getMedicationContract(order.id)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      medicationVisible.value = false
      return
    }
    medication.value = res
  } finally {
    medicationLoading.value = false
  }
}

// ---- 新建医嘱 ----
const createVisible = ref(false)
const createLoading = ref(false)

interface CreateForm {
  patient_id: number | null
  registration_id: number | null
  order_type: 'DRUG' | 'EXAMINATION' | 'LAB_TEST'
  diagnosis: string
  is_urgent: boolean
  remark: string
  items: MedicalOrderItemDTO[]
}

const createForm = ref<CreateForm>({
  patient_id: null,
  registration_id: null,
  order_type: 'DRUG',
  diagnosis: '',
  is_urgent: false,
  remark: '',
  items: [],
})

function openCreateDialog() {
  createForm.value = {
    patient_id: null,
    registration_id: null,
    order_type: 'DRUG',
    diagnosis: '',
    is_urgent: false,
    remark: '',
    items: [],
  }
  createVisible.value = true
}

function addItem() {
  createForm.value.items.push({
    item_type: createForm.value.order_type,
    item_code: '',
    item_name: '',
    specification: '',
    quantity: 1,
    unit: '',
    unit_price: 0,
    dosage: '',
    usage_method: '',
    frequency: '',
    days: null,
    remark: '',
  })
}

function removeItem(idx: number) {
  createForm.value.items.splice(idx, 1)
}

async function handleCreate() {
  const f = createForm.value
  if (!f.patient_id || !f.registration_id) {
    ElMessage.warning('请填写患者ID和挂号ID')
    return
  }
  if (!f.items.length) {
    ElMessage.warning('请至少添加一条医嘱明细')
    return
  }
  const invalid = f.items.some((it) => !it.item_code.trim() || !it.item_name.trim())
  if (invalid) {
    ElMessage.warning('存在明细缺少编码或名称')
    return
  }

  createLoading.value = true
  try {
    const did = currentDoctorId.value
    if (!did) {
      ElMessage.warning('无法获取当前医生ID，请重新登录')
      createLoading.value = false
      return
    }
    const payload = {
      patient_id: f.patient_id,
      doctor_id: did,
      registration_id: f.registration_id,
      order_type: f.order_type,
      diagnosis: f.diagnosis || null,
      is_urgent: f.is_urgent,
      remark: f.remark || null,
      items: f.items.map((it) => ({
        item_type: it.item_type,
        item_code: it.item_code,
        item_name: it.item_name,
        specification: it.specification || null,
        quantity: it.quantity,
        unit: it.unit || null,
        unit_price: it.unit_price,
        dosage: it.dosage || null,
        usage_method: it.usage_method || null,
        frequency: it.frequency || null,
        days: it.days,
        remark: it.remark || null,
      })),
    }
    const res = await doctorApi.createMedicalOrder(payload)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      return
    }
    ElMessage.success(`医嘱 ${res.order_no} 已创建`)
    createVisible.value = false
    await loadOrders()
  } finally {
    createLoading.value = false
  }
}

// ---- 工具方法 ----
function statusLabel(s: string): string {
  return labelOf(MEDICAL_ORDER_STATUS_LABELS, s)
}
function statusTagType(s: string) {
  return tagTypeOf(MEDICAL_ORDER_STATUS_TAG_TYPES, s)
}

function orderTypeLabel(t: string): string {
  return labelOf(MEDICAL_ORDER_TYPE_LABELS, t)
}
function orderTypeTagType(t: string) {
  return tagTypeOf(MEDICAL_ORDER_TYPE_TAG_TYPES, t)
}

function formatAmount(v: number | null | undefined): string {
  if (v == null) return '—'
  return `¥${Number(v).toFixed(2)}`
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.text-muted {
  color: #909399;
}
</style>
