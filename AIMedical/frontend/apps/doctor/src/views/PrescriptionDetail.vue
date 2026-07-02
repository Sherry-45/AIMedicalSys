<template>
  <div class="page-container" v-loading="loading">
    <!-- 顶部返回 -->
    <div class="top-bar">
      <el-button text @click="router.back()">← 返回</el-button>
    </div>

    <div v-if="prescription" class="detail-grid">
      <!-- 主信息卡 -->
      <el-card class="main-card">
        <template #header>
          <div class="card-header">
            <h2>处方 #{{ prescription.id }}</h2>
            <div class="header-tags">
              <el-tag :type="statusTagType(prescription.status)" size="large">
                {{ statusLabel(prescription.status) }}
              </el-tag>
              <el-tag v-if="prescription.ai_checked" type="success" size="large">AI 已检</el-tag>
            </div>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="患者">
            <el-link type="primary" :underline="false" @click="goPatient(prescription.patient_id)">
              {{ prescription.patient_name }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="科室">{{ prescription.department || '—' }}</el-descriptions-item>
          <el-descriptions-item label="诊断" :span="2">
            {{ prescription.diagnosis || '（无诊断）' }}
          </el-descriptions-item>
          <el-descriptions-item label="开具时间">{{ formatDateTime(prescription.created_at) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(prescription.updated_at) }}</el-descriptions-item>
          <el-descriptions-item v-if="prescription.audited_by" label="审核人">
            医生 #{{ prescription.audited_by }}
          </el-descriptions-item>
          <el-descriptions-item v-if="prescription.audited_at" label="审核时间">
            {{ formatDateTime(prescription.audited_at) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="prescription.audit_remark" label="审核备注" :span="2">
            {{ prescription.audit_remark }}
          </el-descriptions-item>
          <el-descriptions-item v-if="prescription.remark" label="备注" :span="2">
            {{ prescription.remark }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 药品明细 -->
        <div class="section">
          <h3 class="section-title">药品明细</h3>
          <el-table :data="prescription.items" border style="width: 100%">
            <el-table-column type="index" label="#" width="50" align="center" />
            <el-table-column prop="drug_name" label="药品名称" min-width="140" />
            <el-table-column prop="specification" label="规格" width="110" />
            <el-table-column prop="dosage" label="用量" width="90" />
            <el-table-column prop="usage_method" label="用法" width="90" />
            <el-table-column prop="frequency" label="频次" width="100" />
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
            <el-table-column prop="unit" label="单位" width="70" align="center" />
            <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
            <template #empty>
              <el-empty description="暂无药品明细" :image-size="60" />
            </template>
          </el-table>
        </div>

        <!-- 操作区 -->
        <div class="actions">
          <el-button
            v-if="canSubmit"
            type="primary"
            :loading="submitting"
            @click="handleSubmit"
          >
            提交审核
          </el-button>
          <el-button
            v-if="canAudit"
            type="success"
            :loading="auditing"
            @click="openAuditDialog(true)"
          >
            审核通过
          </el-button>
          <el-button
            v-if="canAudit"
            type="danger"
            :loading="auditing"
            @click="openAuditDialog(false)"
          >
            驳回
          </el-button>
          <el-button @click="goPatient(prescription.patient_id)">查看患者</el-button>
        </div>
      </el-card>

      <!-- 右侧：状态流转 -->
      <el-card class="side-card">
        <template #header>
          <div class="card-header">
            <h3>状态流转</h3>
          </div>
        </template>
        <div class="status-flow">
          <div
            v-for="(step, idx) in statusFlow"
            :key="step.key"
            class="flow-step"
            :class="{
              active: isStatusActive(step.key),
              current: prescription.status === step.key,
            }"
          >
            <div class="step-no">{{ idx + 1 }}</div>
            <div class="step-info">
              <div class="step-label">{{ step.label }}</div>
              <div class="step-desc">{{ step.desc }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditApprove ? '审核通过' : '驳回处方'"
      width="480px"
      :close-on-click-modal="false"
    >
      <div v-if="!auditApprove" class="dialog-warning">
        <el-alert
          title="驳回时必须填写驳回原因"
          type="warning"
          :closable="false"
          show-icon
        />
      </div>
      <el-form label-width="80px" style="margin-top: 12px">
        <el-form-item label="审核备注">
          <el-input
            v-model="auditRemark"
            type="textarea"
            :rows="4"
            :placeholder="auditApprove ? '可填写审核意见（选填）' : '请填写驳回原因（必填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button
          :type="auditApprove ? 'success' : 'danger'"
          :loading="auditing"
          @click="handleAudit"
        >
          确认{{ auditApprove ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type { PrescriptionResponse, PrescriptionStatus } from '@aimedical/shared'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const auditing = ref(false)
const prescription = ref<PrescriptionResponse | null>(null)

const auditDialogVisible = ref(false)
const auditApprove = ref(true)
const auditRemark = ref('')

const prescriptionId = computed(() => Number(route.params.id))

const canSubmit = computed(
  () => prescription.value && ['DRAFT', 'REJECTED'].includes(prescription.value.status),
)

const canAudit = computed(() => prescription.value?.status === 'PENDING_REVIEW')

const statusFlow = [
  { key: 'DRAFT' as PrescriptionStatus, label: '草稿', desc: '医生开立，尚未提交' },
  { key: 'PENDING_REVIEW' as PrescriptionStatus, label: '待审', desc: '已提交，等待其他医生审核' },
  { key: 'APPROVED' as PrescriptionStatus, label: '已通过', desc: '审核通过，处方生效' },
]

const formatDateTime = (iso: string | null): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

const statusLabel = (status: PrescriptionStatus): string => {
  const map: Record<PrescriptionStatus, string> = {
    DRAFT: '草稿',
    PENDING_REVIEW: '待审',
    APPROVED: '已通过',
    REJECTED: '已驳回',
  }
  return map[status] || status
}

const statusTagType = (status: PrescriptionStatus): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<PrescriptionStatus, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    DRAFT: 'info',
    PENDING_REVIEW: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
  }
  return map[status] || 'info'
}

function isStatusActive(status: PrescriptionStatus): boolean {
  if (!prescription.value) return false
  if (prescription.value.status === 'REJECTED') return status === 'DRAFT' || status === 'PENDING_REVIEW'
  const order: PrescriptionStatus[] = ['DRAFT', 'PENDING_REVIEW', 'APPROVED']
  return order.indexOf(status) <= order.indexOf(prescription.value.status)
}

function goPatient(patientId: number) {
  router.push(`/patient/${patientId}`)
}

async function loadPrescription() {
  loading.value = true
  try {
    const result = await doctorApi.getPrescription(prescriptionId.value)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      router.replace('/prescriptions')
      return
    }
    prescription.value = result
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  try {
    await ElMessageBox.confirm('确认提交此处方进入审核流程？提交后将无法直接修改。', '提交审核', {
      type: 'warning',
    })
  } catch {
    return
  }
  submitting.value = true
  try {
    const result = await doctorApi.submitPrescription(prescriptionId.value)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('已提交审核')
    prescription.value = result
  } finally {
    submitting.value = false
  }
}

function openAuditDialog(approve: boolean) {
  auditApprove.value = approve
  auditRemark.value = ''
  auditDialogVisible.value = true
}

async function handleAudit() {
  if (!auditApprove.value && !auditRemark.value.trim()) {
    ElMessage.warning('驳回时必须填写驳回原因')
    return
  }
  auditing.value = true
  try {
    const result = await doctorApi.auditPrescription(prescriptionId.value, {
      approve: auditApprove.value,
      audit_remark: auditRemark.value.trim(),
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success(auditApprove.value ? '已审核通过' : '已驳回')
    prescription.value = result
    auditDialogVisible.value = false
  } finally {
    auditing.value = false
  }
}

onMounted(() => {
  if (!prescriptionId.value || Number.isNaN(prescriptionId.value)) {
    ElMessage.error('处方ID无效')
    router.replace('/prescriptions')
    return
  }
  loadPrescription()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.top-bar {
  margin-bottom: 12px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2,
.card-header h3 {
  margin: 0;
  font-size: 20px;
}

.card-header h3 {
  font-size: 16px;
}

.header-tags {
  display: flex;
  gap: 8px;
}

.section {
  margin-top: 24px;
}

.section-title {
  font-size: 15px;
  margin: 0 0 12px 0;
  color: #303133;
  border-left: 3px solid #409eff;
  padding-left: 8px;
}

.actions {
  margin-top: 24px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 状态流转 */
.status-flow {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.flow-step {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f5f7fa;
  opacity: 0.5;
  transition: all 0.2s;
}

.flow-step.active {
  opacity: 1;
  background: #ecf5ff;
}

.flow-step.current {
  background: #e1f3d8;
  box-shadow: 0 0 0 2px #67c23a;
}

.step-no {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #c0c4cc;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.flow-step.active .step-no {
  background: #409eff;
}

.flow-step.current .step-no {
  background: #67c23a;
}

.step-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.step-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.dialog-warning {
  margin-bottom: 8px;
}

@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
