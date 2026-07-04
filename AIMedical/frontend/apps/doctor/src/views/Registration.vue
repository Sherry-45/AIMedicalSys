<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>挂号管理</h2>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索患者姓名/ID"
              clearable
              size="default"
              style="width: 200px"
            />
            <el-select
              v-model="statusFilter"
              placeholder="状态筛选"
              clearable
              size="default"
              style="width: 140px"
            >
              <el-option label="待确认" value="PENDING" />
              <el-option label="已确认" value="CONFIRMED" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
              <el-option label="爽约" value="NO_SHOW" />
            </el-select>
            <el-button :loading="loading" @click="loadList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredList" border style="width: 100%">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="挂号号" prop="registration_no" width="140">
          <template #default="{ row }">{{ row.registration_no || '—' }}</template>
        </el-table-column>
        <el-table-column label="患者" width="120">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="goPatient(row.patient_id)">
              {{ row.patient_name || '—' }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="电话" width="130">
          <template #default="{ row }">{{ row.patient_phone || '—' }}</template>
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
        <el-table-column label="预约日期" width="120">
          <template #default="{ row }">{{ formatDate(row.scheduled_date) }}</template>
        </el-table-column>
        <el-table-column label="时段" width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.scheduled_time_slot || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="primary"
              link
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
            <el-button
              v-if="row.status === 'CONFIRMED'"
              size="small"
              type="success"
              link
              @click="handleComplete(row)"
            >
              完成
            </el-button>
            <el-button
              v-if="canCancel(row.status)"
              size="small"
              type="danger"
              link
              @click="openCancel(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="canNoShow(row.status)"
              size="small"
              type="warning"
              link
              @click="handleNoShow(row)"
            >
              爽约
            </el-button>
            <span v-if="!hasAction(row.status)" class="text-muted">—</span>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { registrationApi, isBusinessError } from '@aimedical/shared'
import type { RegistrationResponse, RegistrationStatus } from '@aimedical/shared'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const list = ref<RegistrationResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchKeyword = ref('')
const statusFilter = ref<RegistrationStatus | ''>('')

const filteredList = computed(() => {
  let result = list.value
  if (statusFilter.value) {
    result = result.filter((r) => r.status === statusFilter.value)
  }
  const kw = searchKeyword.value.trim().toLowerCase()
  if (kw) {
    result = result.filter(
      (r) =>
        (r.patient_name || '').toLowerCase().includes(kw) ||
        String(r.patient_id).includes(kw),
    )
  }
  return result
})

const formatDate = (iso: string | null | undefined): string =>
  iso ? iso.substring(0, 10) : '—'

const statusLabel = (status: RegistrationStatus): string => {
  const map: Record<RegistrationStatus, string> = {
    PENDING: '待确认',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    NO_SHOW: '爽约',
  }
  return map[status] || status
}

const statusTagType = (
  status: RegistrationStatus,
): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<RegistrationStatus, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    PENDING: 'warning',
    CONFIRMED: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'info',
    NO_SHOW: 'danger',
  }
  return map[status] || 'info'
}

const canCancel = (status: RegistrationStatus): boolean =>
  status === 'PENDING' || status === 'CONFIRMED'

const canNoShow = (status: RegistrationStatus): boolean =>
  status === 'PENDING' || status === 'CONFIRMED'

const hasAction = (status: RegistrationStatus): boolean =>
  canCancel(status) || status === 'PENDING' || status === 'CONFIRMED'

function goPatient(patientId: number) {
  router.push(`/patient/${patientId}`)
}

async function loadList() {
  const doctorId = authStore.user?.id
  if (!doctorId) {
    ElMessage.warning('无法获取当前医生信息，请重新登录')
    return
  }
  loading.value = true
  try {
    const result = await registrationApi.listByDoctor(
      doctorId,
      currentPage.value - 1,
      pageSize.value,
    )
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

async function handleConfirm(row: RegistrationResponse) {
  try {
    await ElMessageBox.confirm(`确认挂号「${row.patient_name || row.id}」？`, '确认挂号', {
      type: 'info',
    })
  } catch {
    return
  }
  submitting.value = true
  try {
    const result = await registrationApi.confirm(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('挂号已确认')
    loadList()
  } finally {
    submitting.value = false
  }
}

async function handleComplete(row: RegistrationResponse) {
  try {
    await ElMessageBox.confirm(`完成挂号「${row.patient_name || row.id}」？`, '完成挂号', {
      type: 'success',
    })
  } catch {
    return
  }
  submitting.value = true
  try {
    const result = await registrationApi.complete(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('挂号已完成')
    loadList()
  } finally {
    submitting.value = false
  }
}

async function handleNoShow(row: RegistrationResponse) {
  try {
    await ElMessageBox.confirm(
      `标记挂号「${row.patient_name || row.id}」为爽约？`,
      '标记爽约',
      { type: 'warning' },
    )
  } catch {
    return
  }
  submitting.value = true
  try {
    const result = await registrationApi.markNoShow(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('已标记为爽约')
    loadList()
  } finally {
    submitting.value = false
  }
}

// ---- 取消挂号 ----

const cancelVisible = ref(false)
const cancelTarget = ref<RegistrationResponse | null>(null)
const cancelReason = ref('')

function openCancel(row: RegistrationResponse) {
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
    const result = await registrationApi.cancel(cancelTarget.value.id, cancelReason.value.trim())
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
