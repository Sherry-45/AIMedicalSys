<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>患者管理</h2>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索姓名/手机号"
              clearable
              size="default"
              style="width: 220px"
              @input="applyFilter"
            />
            <el-button :loading="loading" @click="loadList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="pagedList" border style="width: 100%" @row-click="goPatient">
        <el-table-column label="患者ID" prop="patient_id" width="100" />
        <el-table-column label="姓名" width="120">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click.stop="goPatient(row)">
              {{ row.patient_name || '—' }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="手机号" width="150">
          <template #default="{ row }">{{ row.patient_phone || '—' }}</template>
        </el-table-column>
        <el-table-column label="就诊次数" width="100" align="center">
          <template #default="{ row }">{{ row.visit_count }}</template>
        </el-table-column>
        <el-table-column label="最近就诊日期" width="140">
          <template #default="{ row }">{{ formatDate(row.last_visit_date) }}</template>
        </el-table-column>
        <el-table-column label="最近科室" width="140">
          <template #default="{ row }">{{ row.last_department || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click.stop="goPatient(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无患者记录" />
        </template>
      </el-table>

      <div v-if="filteredList.length > pageSize" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="filteredList.length"
          layout="prev, pager, next, total"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registrationApi, isBusinessError } from '@aimedical/shared'
import type { RegistrationResponse } from '@aimedical/shared'
import { useAuthStore } from '../stores/auth'

interface PatientRow {
  patient_id: number
  patient_name: string
  patient_phone: string
  visit_count: number
  last_visit_date: string
  last_department: string
}

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const patients = ref<PatientRow[]>([])

const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 10

const filteredList = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return patients.value
  return patients.value.filter(
    (p) =>
      (p.patient_name || '').toLowerCase().includes(kw) ||
      (p.patient_phone || '').toLowerCase().includes(kw),
  )
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredList.value.slice(start, start + pageSize)
})

function applyFilter() {
  currentPage.value = 1
}

const formatDate = (iso: string | null | undefined): string =>
  iso ? iso.substring(0, 10) : '—'

function goPatient(row: PatientRow) {
  router.push(`/patient/${row.patient_id}`)
}

/**
 * 从挂号记录中聚合去重，生成患者列表。
 * 每位患者保留最近一次就诊信息及就诊总次数。
 */
function aggregatePatients(registrations: RegistrationResponse[]): PatientRow[] {
  const map = new Map<number, PatientRow>()
  for (const r of registrations) {
    const existing = map.get(r.patient_id)
    const visitDate = r.scheduled_date || r.created_at || ''
    if (!existing) {
      map.set(r.patient_id, {
        patient_id: r.patient_id,
        patient_name: r.patient_name || '',
        patient_phone: r.patient_phone || '',
        visit_count: 1,
        last_visit_date: visitDate,
        last_department: r.department || '',
      })
    } else {
      existing.visit_count += 1
      // 保留最近一次就诊信息（按日期字符串比较，ISO 日期可字典序比较）
      if (visitDate && visitDate > existing.last_visit_date) {
        existing.last_visit_date = visitDate
        existing.last_department = r.department || existing.last_department
        // 用最新的记录覆盖姓名/电话（后续记录可能更完整）
        existing.patient_name = r.patient_name || existing.patient_name
        existing.patient_phone = r.patient_phone || existing.patient_phone
      }
    }
  }
  // 按最近就诊日期倒序
  return Array.from(map.values()).sort((a, b) =>
    b.last_visit_date.localeCompare(a.last_visit_date),
  )
}

async function loadList() {
  const doctorId = authStore.user?.id
  if (!doctorId) {
    ElMessage.warning('无法获取当前医生信息，请重新登录')
    return
  }
  loading.value = true
  try {
    // 拉取较大分页以聚合出患者列表（该医生接诊过的所有患者）
    const result = await registrationApi.listByDoctor(doctorId, 0, 1000)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      patients.value = []
      return
    }
    patients.value = aggregatePatients(result.content ?? [])
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

:deep(.el-table__row) {
  cursor: pointer;
}
</style>
