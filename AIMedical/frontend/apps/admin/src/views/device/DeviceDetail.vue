<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>设备详情</h2>
          <div class="header-actions">
            <el-button
              v-if="device && device.status !== 'ONLINE'"
              type="success"
              @click="handleStatusChange('ONLINE')"
            >上线</el-button>
            <el-button
              v-if="device && device.status !== 'OFFLINE'"
              type="info"
              @click="handleStatusChange('OFFLINE')"
            >下线</el-button>
            <el-button
              v-if="device && device.status !== 'MAINTENANCE'"
              type="warning"
              @click="handleStatusChange('MAINTENANCE')"
            >维护</el-button>
            <el-button
              v-if="device"
              type="primary"
              @click="router.push(`/devices/${device.id}/messages`)"
            >查看消息记录</el-button>
            <el-button @click="router.push('/devices')">返回</el-button>
          </div>
        </div>
      </template>

      <el-descriptions v-if="device" :column="2" border>
        <el-descriptions-item label="设备编码">{{ device.device_code }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ device.device_name }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">
          <el-tag>{{ deviceTypeLabel(device.device_type) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="通信协议">
          <el-tag type="info">{{ device.protocol }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(device.status)">{{ statusLabel(device.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="厂商">{{ device.manufacturer || '—' }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ device.model || '—' }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ device.location || '—' }}</el-descriptions-item>
        <el-descriptions-item label="最后心跳时间">
          {{ formatDateTime(device.last_heartbeat_at) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(device.created_at) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(device.updated_at) }}</el-descriptions-item>
        <el-descriptions-item label="连接配置" :span="2">
          <pre v-if="device.connection_config" class="config-block">{{ device.connection_config }}</pre>
          <span v-else>—</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminApi, isBusinessError } from '@aimedical/shared'
import type { DeviceResponse, DeviceType, DeviceStatus } from '@aimedical/shared'

const route = useRoute()
const router = useRouter()
const deviceId = Number(route.params.id)
const loading = ref(false)
const device = ref<DeviceResponse | null>(null)

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

async function loadDevice() {
  loading.value = true
  try {
    const result = await adminApi.getDevice(deviceId)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    device.value = result
  } finally {
    loading.value = false
  }
}

async function handleStatusChange(status: DeviceStatus) {
  loading.value = true
  try {
    const result = await adminApi.updateDeviceStatus(deviceId, status)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('设备状态已更新')
    device.value = result
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDevice()
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

.config-block {
  margin: 0;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 240px;
  overflow: auto;
}
</style>
