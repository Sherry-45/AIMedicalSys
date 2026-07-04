<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>线下挂号</h2>
          <div class="header-actions">
            <el-input
              v-model.number="filter.patientId"
              placeholder="患者ID"
              clearable
              size="default"
              style="width: 120px"
              @keyup.enter="applyFilter"
            />
            <el-input
              v-model="filter.patientName"
              placeholder="患者姓名"
              clearable
              size="default"
              style="width: 150px"
              @keyup.enter="applyFilter"
            />
            <el-select
              v-model="filter.status"
              placeholder="状态筛选"
              clearable
              size="default"
              style="width: 140px"
              @change="applyFilter"
            >
              <el-option label="有效" value="ACTIVE" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
            <el-button type="primary" @click="applyFilter">查询</el-button>
            <el-button :loading="loading" @click="loadList">刷新</el-button>
            <el-button type="success" @click="openCreate">新建挂号</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border style="width: 100%">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="挂号号" prop="registration_no" width="150">
          <template #default="{ row }">{{ row.registration_no || '—' }}</template>
        </el-table-column>
        <el-table-column label="患者ID" prop="patient_id" width="90" />
        <el-table-column label="患者姓名" prop="patient_name" width="110">
          <template #default="{ row }">{{ row.patient_name || '—' }}</template>
        </el-table-column>
        <el-table-column label="电话" width="130">
          <template #default="{ row }">{{ row.patient_phone || '—' }}</template>
        </el-table-column>
        <el-table-column label="医生" width="110">
          <template #default="{ row }">{{ row.doctor_name || '—' }}</template>
        </el-table-column>
        <el-table-column label="科室" width="120">
          <template #default="{ row }">{{ row.department || '—' }}</template>
        </el-table-column>
        <el-table-column label="挂号类型" width="110">
          <template #default="{ row }">{{ row.registration_type || '—' }}</template>
        </el-table-column>
        <el-table-column label="挂号费" width="90" align="right">
          <template #default="{ row }">
            {{ row.registration_fee != null ? '¥' + row.registration_fee : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="canCancel(row.status)"
              size="small"
              type="danger"
              link
              @click="openCancel(row)"
            >
              取消
            </el-button>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无挂号记录" />
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

    <!-- 创建挂号对话框 -->
    <el-dialog v-model="createVisible" title="新建挂号" width="560px" @closed="resetCreateForm">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="患者ID" prop="patient_id">
          <el-input-number v-model="createForm.patient_id" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="患者姓名" prop="patient_name">
          <el-input v-model="createForm.patient_name" placeholder="请输入患者姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="createForm.patient_phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="createForm.id_card" placeholder="选填" />
        </el-form-item>
        <el-form-item label="医生ID">
          <el-input-number v-model="createForm.doctor_id" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="医生姓名">
          <el-input v-model="createForm.doctor_name" placeholder="选填" />
        </el-form-item>
        <el-form-item label="科室">
          <el-input v-model="createForm.department" placeholder="选填" />
        </el-form-item>
        <el-form-item label="挂号类型">
          <el-select v-model="createForm.registration_type" placeholder="选填" clearable>
            <el-option label="普通门诊" value="普通门诊" />
            <el-option label="专家门诊" value="专家门诊" />
            <el-option label="急诊" value="急诊" />
            <el-option label="特需门诊" value="特需门诊" />
          </el-select>
        </el-form-item>
        <el-form-item label="挂号费">
          <el-input-number
            v-model="createForm.registration_fee"
            :min="0"
            :precision="2"
            controls-position="right"
          />
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

    <!-- 取消挂号对话框 -->
    <el-dialog v-model="cancelVisible" title="取消挂号" width="480px">
      <el-form v-if="cancelTarget" label-width="100px">
        <el-form-item label="挂号ID">
          <span>{{ cancelTarget.id }}</span>
        </el-form-item>
        <el-form-item label="患者">
          <span>{{ cancelTarget.patient_name || '—' }}</span>
        </el-form-item>
      </el-form>
      <el-input
        v-model="cancelReason"
        type="textarea"
        :rows="3"
        placeholder="请输入取消原因"
        style="margin-top: 8px"
      />
      <template #footer>
        <el-button @click="cancelVisible = false">关闭</el-button>
        <el-button type="danger" :loading="submitting" @click="submitCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { isBusinessError } from '@aimedical/shared'
import { windowApi } from '@aimedical/shared'
import type {
  OfflineRegistrationCreateRequest,
  OfflineRegistrationResponse,
  OfflineRegistrationStatus,
} from '@aimedical/shared'

const loading = ref(false)
const submitting = ref(false)
const list = ref<OfflineRegistrationResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const filter = reactive({
  patientId: undefined as number | undefined,
  patientName: '',
  status: '' as OfflineRegistrationStatus | '',
})

function buildQuery() {
  return {
    patientId: filter.patientId || undefined,
    patientName: filter.patientName.trim() || undefined,
    status: filter.status || undefined,
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
    const result = await windowApi.queryRegistrations(buildQuery())
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      list.value = []
      total.value = 0
      return
    }
    list.value = result.content ?? []
    total.value = result.total_elements ?? 0
  } finally {
    loading.value = false
  }
}

// ---- 状态展示 ----

const statusLabel = (status: OfflineRegistrationStatus): string => {
  const map: Record<OfflineRegistrationStatus, string> = {
    ACTIVE: '有效',
    CANCELLED: '已取消',
  }
  return map[status] || status
}

const statusTagType = (
  status: OfflineRegistrationStatus,
): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<OfflineRegistrationStatus, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    ACTIVE: 'success',
    CANCELLED: 'info',
  }
  return map[status] || 'info'
}

const canCancel = (status: OfflineRegistrationStatus): boolean => {
  return status === 'ACTIVE'
}

const formatDateTime = (iso: string | null): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

// ---- 创建挂号 ----

const createVisible = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive<OfflineRegistrationCreateRequest>({
  patient_id: 0,
  patient_name: '',
  patient_phone: '',
  id_card: '',
  doctor_id: undefined,
  doctor_name: '',
  department: '',
  registration_type: '',
  registration_fee: 0,
  remark: '',
})

const createRules: FormRules = {
  patient_id: [{ required: true, message: '请输入患者ID', trigger: 'blur' }],
  patient_name: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
}

function openCreate() {
  createVisible.value = true
}

function resetCreateForm() {
  createFormRef.value?.resetFields()
  createForm.patient_id = 0
  createForm.patient_name = ''
  createForm.patient_phone = ''
  createForm.id_card = ''
  createForm.doctor_id = undefined
  createForm.doctor_name = ''
  createForm.department = ''
  createForm.registration_type = ''
  createForm.registration_fee = 0
  createForm.remark = ''
}

async function submitCreate() {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const result = await windowApi.createRegistration(createForm)
      if (isBusinessError(result)) {
        ElMessage.error(result.message)
        return
      }
      ElMessage.success('挂号创建成功')
      createVisible.value = false
      loadList()
    } finally {
      submitting.value = false
    }
  })
}

// ---- 取消挂号 ----

const cancelVisible = ref(false)
const cancelTarget = ref<OfflineRegistrationResponse | null>(null)
const cancelReason = ref('')

function openCancel(row: OfflineRegistrationResponse) {
  cancelTarget.value = row
  cancelReason.value = ''
  cancelVisible.value = true
}

async function submitCancel() {
  if (!cancelTarget.value) return
  if (!cancelReason.value.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }
  submitting.value = true
  try {
    const result = await windowApi.cancelRegistration(cancelTarget.value.id, {
      cancel_reason: cancelReason.value.trim(),
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('挂号已取消')
    cancelVisible.value = false
    loadList()
  } finally {
    submitting.value = false
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

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
