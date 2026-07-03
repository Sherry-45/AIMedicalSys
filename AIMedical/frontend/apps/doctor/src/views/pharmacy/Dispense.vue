<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>发药工作台</h2>
          <div class="header-actions">
            <el-input
              v-model.number="queryForm.patientId"
              placeholder="患者ID"
              clearable
              size="default"
              style="width: 140px"
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
              <el-option label="待发药" value="PENDING" />
              <el-option label="已发药" value="DISPENSED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
            <el-button :loading="loading" @click="loadList">查询</el-button>
            <el-button type="primary" @click="openCreateDialog">新建发药</el-button>
          </div>
        </div>
      </template>

      <el-table :data="pagedList" border style="width: 100%">
        <el-table-column label="发药单号" prop="dispensingNo" width="160">
          <template #default="{ row }">{{ row.dispensingNo || row.id }}</template>
        </el-table-column>
        <el-table-column label="患者" width="140">
          <template #default="{ row }">
            {{ row.patientName || ('#' + row.patientId) }}
          </template>
        </el-table-column>
        <el-table-column label="药品数" width="90" align="center">
          <template #default="{ row }">{{ row.items?.length ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="总金额" width="110" align="right">
          <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作人" width="110">
          <template #default="{ row }">{{ row.operatorName || '—' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="primary"
              @click="handleDispense(row.id)"
            >发药</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="danger"
              @click="handleCancel(row.id)"
            >取消</el-button>
            <el-button size="small" link @click="handleView(row.id)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无发药记录" />
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

    <!-- 新建发药对话框 -->
    <el-dialog v-model="createVisible" title="新建发药单" width="720px" @closed="resetCreateForm">
      <el-form ref="createFormRef" :model="createForm" label-width="100px">
        <el-form-item label="患者ID" required>
          <el-input
            v-model.number="createForm.patientId"
            placeholder="请输入患者ID"
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="患者姓名">
          <el-input
            v-model="createForm.patientName"
            placeholder="可选"
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="处方ID">
          <el-input
            v-model.number="createForm.prescriptionId"
            placeholder="可选"
            style="width: 220px"
          />
        </el-form-item>

        <el-form-item label="药品明细" required>
          <el-table :data="createForm.items" border size="small" style="width: 100%">
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
            <el-table-column label="规格" width="120">
              <template #default="{ row }">
                <el-input v-model="row.specification" size="small" placeholder="规格" />
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
            <el-table-column label="用法" width="120">
              <template #default="{ row }">
                <el-input v-model="row.usageMethod" size="small" placeholder="用法" />
              </template>
            </el-table-column>
            <el-table-column label="频率" width="120">
              <template #default="{ row }">
                <el-input v-model="row.frequency" size="small" placeholder="频率" />
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
          >+ 添加药品</el-button>
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
        <el-button type="primary" :loading="submitting" @click="submitCreate">确认发药</el-button>
      </template>
    </el-dialog>

    <!-- 发药详情对话框 -->
    <el-dialog v-model="detailVisible" title="发药单详情" width="640px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="发药单号">{{ detail.dispensingNo || detail.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName || ('#' + detail.patientId) }}</el-descriptions-item>
        <el-descriptions-item label="总金额">{{ formatAmount(detail.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operatorName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="发药时间">{{ formatDateTime(detail.dispensedAt) }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
      <el-table
        v-if="detail?.items?.length"
        :data="detail.items"
        border
        size="small"
        style="margin-top: 12px"
      >
        <el-table-column label="药品编码" prop="drugCode" width="130" />
        <el-table-column label="药品名称" prop="drugName" min-width="140" />
        <el-table-column label="规格" prop="specification" width="110" />
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
  DispensingResponse,
  DispensingCreateRequest,
  DispensingItemRequest,
} from '@aimedical/shared'

const loading = ref(false)
const submitting = ref(false)
const list = ref<DispensingResponse[]>([])

const queryForm = reactive({
  patientId: undefined as number | undefined,
  status: '',
})

const currentPage = ref(1)
const pageSize = 10

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return list.value.slice(start, start + pageSize)
})

// ---- 新建发药 ----
const createVisible = ref(false)
const createForm = reactive<DispensingCreateRequest>({
  patientId: 0,
  patientName: '',
  prescriptionId: undefined,
  items: [],
  remark: '',
})

function openCreateDialog() {
  resetCreateForm()
  createVisible.value = true
}

function resetCreateForm() {
  createForm.patientId = 0
  createForm.patientName = ''
  createForm.prescriptionId = undefined
  createForm.items = []
  createForm.remark = ''
}

function addItem() {
  createForm.items.push({
    drugCode: '',
    drugName: '',
    specification: '',
    quantity: 1,
    unit: '',
    usageMethod: '',
    frequency: '',
  } as DispensingItemRequest)
}

function removeItem(index: number) {
  createForm.items.splice(index, 1)
}

async function submitCreate() {
  if (!createForm.patientId) {
    ElMessage.warning('请填写患者ID')
    return
  }
  if (createForm.items.length === 0) {
    ElMessage.warning('请至少添加一条药品明细')
    return
  }
  const invalid = createForm.items.some((it) => !it.drugCode || !it.drugName || !it.quantity)
  if (invalid) {
    ElMessage.warning('请完善药品明细（编码/名称/数量必填）')
    return
  }

  submitting.value = true
  try {
    const result = await pharmacyApi.createDispensing(createForm)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('发药单创建成功')
    createVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

// ---- 发药操作 ----
async function handleDispense(id: number) {
  try {
    await ElMessageBox.confirm('确认执行发药操作？', '提示', { type: 'warning' })
  } catch {
    return
  }
  loading.value = true
  try {
    const result = await pharmacyApi.dispense(id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('发药成功')
    loadList()
  } finally {
    loading.value = false
  }
}

async function handleCancel(id: number) {
  try {
    await ElMessageBox.confirm('确认取消该发药单？', '提示', { type: 'warning' })
  } catch {
    return
  }
  loading.value = true
  try {
    const result = await pharmacyApi.cancelDispensing(id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('已取消发药')
    loadList()
  } finally {
    loading.value = false
  }
}

// ---- 详情 ----
const detailVisible = ref(false)
const detail = ref<DispensingResponse | null>(null)

async function handleView(id: number) {
  loading.value = true
  try {
    const result = await pharmacyApi.getDispensing(id)
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
    const result = await pharmacyApi.queryDispensing(params)
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
    PENDING: '待发药',
    DISPENSED: '已发药',
    CANCELLED: '已取消',
  }
  return map[status] || status
}

const statusTagType = (status: string): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<string, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    PENDING: 'warning',
    DISPENSED: 'success',
    CANCELLED: 'info',
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
