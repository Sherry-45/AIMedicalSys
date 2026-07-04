<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>设备消息记录</h2>
          <div class="header-actions">
            <el-button @click="router.push('/devices')">返回设备列表</el-button>
            <el-button :loading="loading" @click="loadMessages">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="messages" border style="width: 100%">
        <el-table-column label="ID" prop="id" width="90" />
        <el-table-column label="消息类型" prop="message_type" width="140" show-overflow-tooltip />
        <el-table-column label="协议" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.protocol }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="原始内容" prop="raw_content" min-width="240" show-overflow-tooltip />
        <el-table-column label="解析内容" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ row.parsed_content || '—' }}</template>
        </el-table-column>
        <el-table-column label="是否已处理" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.processed" type="success" size="small">已处理</el-tag>
            <el-tag v-else type="info" size="small">未处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="接收时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.received_at) }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无消息记录" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          background
          @current-change="loadMessages"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminApi, isBusinessError } from '@aimedical/shared'
import type { DeviceMessageResponse } from '@aimedical/shared'

const route = useRoute()
const router = useRouter()
const deviceId = Number(route.params.id)
const loading = ref(false)
const messages = ref<DeviceMessageResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const formatDateTime = (iso: string | null): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

async function loadMessages() {
  loading.value = true
  try {
    const result = await adminApi.listDeviceMessages(deviceId, currentPage.value - 1, pageSize)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    messages.value = result.content
    total.value = result.totalElements
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMessages()
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
