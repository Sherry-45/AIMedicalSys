<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>管理员仪表盘</h2>
          <el-button :loading="loading" link @click="loadStats">刷新</el-button>
        </div>
      </template>

      <p class="welcome-text">欢迎回来，{{ authStore.user?.real_name || authStore.user?.username || '管理员' }}</p>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stat-row">
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-users">
            <div class="stat-icon">👥</div>
            <div class="stat-content">
              <div class="stat-label">用户总数</div>
              <div class="stat-value">{{ stats.totalUsers }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-doctors">
            <div class="stat-icon">👨‍⚕️</div>
            <div class="stat-content">
              <div class="stat-label">医生数</div>
              <div class="stat-value">{{ stats.doctorCount }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-patients">
            <div class="stat-icon">🤒</div>
            <div class="stat-content">
              <div class="stat-label">患者数</div>
              <div class="stat-value">{{ stats.patientCount }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-prescriptions">
            <div class="stat-icon">📋</div>
            <div class="stat-content">
              <div class="stat-label">处方总数</div>
              <div class="stat-value">{{ stats.prescriptionCount }}</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 快捷入口 -->
      <h3 class="section-title">快捷入口</h3>
      <el-row :gutter="16" class="quick-row">
        <el-col :xs="12" :sm="8" :md="6">
          <router-link to="/system/users" class="quick-link">
            <el-card shadow="hover" class="quick-card">
              <div class="quick-icon">👤</div>
              <div class="quick-text">用户管理</div>
            </el-card>
          </router-link>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <router-link to="/system/roles" class="quick-link">
            <el-card shadow="hover" class="quick-card">
              <div class="quick-icon">🔑</div>
              <div class="quick-text">角色管理</div>
            </el-card>
          </router-link>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <router-link to="/patients" class="quick-link">
            <el-card shadow="hover" class="quick-card">
              <div class="quick-icon">🤒</div>
              <div class="quick-text">患者管理</div>
            </el-card>
          </router-link>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <router-link to="/prescriptions" class="quick-link">
            <el-card shadow="hover" class="quick-card">
              <div class="quick-icon">📋</div>
              <div class="quick-text">处方查询</div>
            </el-card>
          </router-link>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <router-link to="/pharmacy/dispense" class="quick-link">
            <el-card shadow="hover" class="quick-card">
              <div class="quick-icon">💊</div>
              <div class="quick-text">药房发药</div>
            </el-card>
          </router-link>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <router-link to="/inventory/stock" class="quick-link">
            <el-card shadow="hover" class="quick-card">
              <div class="quick-icon">📦</div>
              <div class="quick-text">药库库存</div>
            </el-card>
          </router-link>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <router-link to="/window/registration" class="quick-link">
            <el-card shadow="hover" class="quick-card">
              <div class="quick-icon">🏥</div>
              <div class="quick-text">线下挂号</div>
            </el-card>
          </router-link>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <router-link to="/health-record/query" class="quick-link">
            <el-card shadow="hover" class="quick-card">
              <div class="quick-icon">📊</div>
              <div class="quick-text">健康档案</div>
            </el-card>
          </router-link>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { userManagementApi, apiGet, isBusinessError } from '@aimedical/shared'
import type { PageResponse } from '@aimedical/shared'

const authStore = useAuthStore()
const loading = ref(false)

const stats = reactive({
  totalUsers: 0,
  doctorCount: 0,
  patientCount: 0,
  prescriptionCount: 0,
})

async function loadStats() {
  loading.value = true
  try {
    const [totalRes, doctorRes, patientRes, prescriptionRes] = await Promise.all([
      userManagementApi.query({ size: 1 }),
      userManagementApi.query({ userType: 'DOCTOR', size: 1 }),
      userManagementApi.query({ userType: 'PATIENT', size: 1 }),
      apiGet<PageResponse<unknown>>('/admin/prescriptions', { params: { size: 1 } }),
    ])

    if (!isBusinessError(totalRes)) stats.totalUsers = totalRes.total_elements ?? 0
    if (!isBusinessError(doctorRes)) stats.doctorCount = doctorRes.total_elements ?? 0
    if (!isBusinessError(patientRes)) stats.patientCount = patientRes.total_elements ?? 0
    if (!isBusinessError(prescriptionRes)) stats.prescriptionCount = prescriptionRes.total_elements ?? 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
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

.welcome-text {
  font-size: 16px;
  color: #606266;
  margin: 0 0 20px 0;
}

.stat-row {
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  color: #fff;
  min-height: 80px;
}

.stat-users {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-doctors {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-patients {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-prescriptions {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-icon {
  font-size: 36px;
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
}

.section-title {
  font-size: 16px;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.quick-row {
  margin-bottom: 8px;
}

.quick-link {
  text-decoration: none;
  display: block;
  margin-bottom: 16px;
}

.quick-card {
  text-align: center;
  padding: 10px;
  cursor: pointer;
  transition: transform 0.2s;
}

.quick-card:hover {
  transform: translateY(-2px);
}

.quick-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.quick-text {
  font-size: 14px;
  color: #606266;
}
</style>
