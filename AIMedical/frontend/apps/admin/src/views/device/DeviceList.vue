<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>设备列表</h2>
          <div class="header-actions">
            <el-select
              v-model="filters.deviceType"
              placeholder="设备类型"
              clearable
              size="default"
              style="width: 140px"
              @change="handleFilterChange"
            >
              <el-option label="全部" value="" />
              <el-option label="影像设备" value="IMAGING" />
              <el-option label="检验分析仪" value="LAB_ANALYZER" />
              <el-option label="监护仪" value="MONITOR" />
              <el-option label="其他" value="OTHER" />
            </el-select>
            <el-select
              v-model="filters.status"
              placeholder="状态"
              clearable
              size="default"
              style="width: 120px"
              @change="handleFilterChange"
            >
              <el-option label="全部" value="" />
              <el-option label="在线" value="ONLINE" />
              <el-option label="离线" value="OFFLINE" />
              <el-option label="故障" value="ERROR" />
              <el-option label="维护中" value="MAINTENANCE" />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索编码/名称"
              clearable
              size="default"
              style="width: 200px"
            />
            <el-button type="primary" @click="openCreateDialog">新增设备</el-button>
            <el-button :loading="loading" @click="loadDevices">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredDevices" border style="width: 100%" @row-click="goDetail">
        <el-table-column label="设备编码" prop="device_code" width="140" />
        <el-table-column label="设备名称" prop="device_name" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag>{{ deviceTypeLabel(row.device_type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="协议" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.protocol }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="厂商" prop="manufacturer" width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.manufacturer || '—' }}</template>
        </el-table-column>
        <el-table-column label="位置" prop="location" width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.location || '—' }}</template>
        </el-table-column>
        <el-table-column label="最后心跳时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.last_heartbeat_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click.stop="goDetail(row)">详情</el-button>
            <el-button size="small" type="warning" link @click.stop="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="info" link @click.stop="goMessages(row)">消息记录</el-button>
            <el-button size="small" type="danger" link @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无设备记录" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          background
          @current-change="loadDevices"
        />
      </div>
    </el-card>

    <!-- 设备表单弹窗（新增/编辑共用） -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="设备编码" prop="device_code">
          <el-input
            v-model="form.device_code"
            placeholder="请输入设备编码"
            :disabled="isEditing"
          />
        </el-form-item>
        <el-form-item label="设备名称" prop="device_name">
          <el-input v-model="form.device_name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="device_type">
          <el-select v-model="form.device_type" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="影像设备" value="IMAGING" />
            <el-option label="检验分析仪" value="LAB_ANALYZER" />
            <el-option label="监护仪" value="MONITOR" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="通信协议" prop="protocol">
          <el-select v-model="form.protocol" placeholder="请选择通信协议" style="width: 100%">
            <el-option label="HL7" value="HL7" />
            <el-option label="DICOM" value="DICOM" />
            <el-option label="ASTM" value="ASTM" />
          </el-select>
        </el-form-item>
        <el-form-item label="厂商">
          <el-input v-model="form.manufacturer" placeholder="请输入厂商" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.model" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="连接配置">
          <el-input
            v-model="form.connection_config"
            type="textarea"
            :rows="3"
            placeholder="请输入连接配置（JSON 等格式）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { adminApi, isBusinessError } from '@aimedical/shared'
import type {
  DeviceResponse,
  DeviceType,
  DeviceStatus,
  DeviceProtocol,
} from '@aimedical/shared'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const devices = ref<DeviceResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const filters = reactive({
  deviceType: '',
  status: '',
})
const searchKeyword = ref('')

const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  device_code: '',
  device_name: '',
  device_type: '' as DeviceType | '',
  protocol: '' as DeviceProtocol | '',
  manufacturer: '',
  model: '',
  location: '',
  connection_config: '',
})

const rules: FormRules = {
  device_code: [{ required: true, message: '请输入设备编码', trigger: 'blur' }],
  device_name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  device_type: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  protocol: [{ required: true, message: '请选择通信协议', trigger: 'change' }],
}

const dialogTitle = computed(() => (isEditing.value ? '编辑设备' : '新增设备'))

// 搜索关键字对当前页数据做客户端过滤
const filteredDevices = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return devices.value
  return devices.value.filter(
    (d) =>
      d.device_code.toLowerCase().includes(kw) ||
      d.device_name.toLowerCase().includes(kw),
  )
})

const formatDateTime = (iso: string | null): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

const deviceTypeLabel = (type: DeviceType): string => {
  const map: Record<DeviceType, string> = {
    IMAGING: '影像设备',
    LAB_ANALYZER: '检验分析仪',
    MONITOR: '监护仪',
    OTHER: '其他',
  }
  return map[type] || type
}

const statusLabel = (status: DeviceStatus): string => {
  const map: Record<DeviceStatus, string> = {
    ONLINE: '在线',
    OFFLINE: '离线',
    ERROR: '故障',
    MAINTENANCE: '维护中',
  }
  return map[status] || status
}

const statusTagType = (
  status: DeviceStatus,
): 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<DeviceStatus, 'success' | 'info' | 'warning' | 'danger'> = {
    ONLINE: 'success',
    OFFLINE: 'info',
    ERROR: 'danger',
    MAINTENANCE: 'warning',
  }
  return map[status] || 'info'
}

function handleFilterChange() {
  currentPage.value = 1
  loadDevices()
}

function goDetail(row: DeviceResponse) {
  router.push(`/devices/${row.id}`)
}

function goMessages(row: DeviceResponse) {
  router.push(`/devices/${row.id}/messages`)
}

function openCreateDialog() {
  isEditing.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: DeviceResponse) {
  isEditing.value = true
  editingId.value = row.id
  form.device_code = row.device_code
  form.device_name = row.device_name
  form.device_type = row.device_type
  form.protocol = row.protocol
  form.manufacturer = row.manufacturer || ''
  form.model = row.model || ''
  form.location = row.location || ''
  form.connection_config = row.connection_config || ''
  dialogVisible.value = true
}

function resetForm() {
  form.device_code = ''
  form.device_name = ''
  form.device_type = ''
  form.protocol = ''
  form.manufacturer = ''
  form.model = ''
  form.location = ''
  form.connection_config = ''
  formRef.value?.clearValidate()
}

async function loadDevices() {
  loading.value = true
  try {
    const result = await adminApi.listDevices({
      deviceType: filters.deviceType || undefined,
      status: filters.status || undefined,
      page: currentPage.value - 1,
      size: pageSize,
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    devices.value = result.content
    total.value = result.total_elements
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEditing.value && editingId.value !== null) {
        const result = await adminApi.updateDevice(editingId.value, {
          device_name: form.device_name,
          device_type: form.device_type as DeviceType,
          protocol: form.protocol as DeviceProtocol,
          manufacturer: form.manufacturer || undefined,
          model: form.model || undefined,
          location: form.location || undefined,
          connection_config: form.connection_config || undefined,
        })
        if (isBusinessError(result)) {
          ElMessage.error(result.message)
          return
        }
        ElMessage.success('设备已更新')
      } else {
        const result = await adminApi.registerDevice({
          device_code: form.device_code,
          device_name: form.device_name,
          device_type: form.device_type as DeviceType,
          protocol: form.protocol as DeviceProtocol,
          manufacturer: form.manufacturer || undefined,
          model: form.model || undefined,
          location: form.location || undefined,
          connection_config: form.connection_config || undefined,
        })
        if (isBusinessError(result)) {
          ElMessage.error(result.message)
          return
        }
        ElMessage.success('设备已新增')
      }
      dialogVisible.value = false
      await loadDevices()
    } finally {
      submitting.value = false
    }
  })
}

async function handleDelete(row: DeviceResponse) {
  try {
    await ElMessageBox.confirm(
      `确定要删除设备「${row.device_name}」(${row.device_code}) 吗？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
      },
    )
  } catch {
    return
  }
  loading.value = true
  try {
    const result = await adminApi.deleteDevice(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('设备已删除')
    // 删除后若当前页空了，回退到上一页
    if (devices.value.length === 1 && currentPage.value > 1) {
      currentPage.value -= 1
    }
    await loadDevices()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDevices()
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
