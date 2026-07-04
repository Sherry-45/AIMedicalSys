<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>检验单详情</h2>
          <div class="header-actions">
            <el-button
              v-if="record?.status === 'COMPLETED'"
              type="primary"
              :loading="loading"
              @click="handleGenerateAi"
            >生成 AI 报告</el-button>
            <el-button @click="router.push('/lab-tests')">返回列表</el-button>
          </div>
        </div>
      </template>

      <AiDegradedBanner :visible="degraded" :reason="fallbackReason" />

      <el-descriptions v-if="record" :column="2" border>
        <el-descriptions-item label="检验单ID">{{ record.id }}</el-descriptions-item>
        <el-descriptions-item label="患者ID">{{ record.patient_id }}</el-descriptions-item>
        <el-descriptions-item label="医生ID">{{ record.doctor_id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(record.status)">{{ statusLabel(record.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检验类型">{{ record.test_type }}</el-descriptions-item>
        <el-descriptions-item label="样本类型">{{ sampleTypeLabel(record.sample_type) }}</el-descriptions-item>
        <el-descriptions-item label="采样时间">{{ formatDateTime(record.collected_at) }}</el-descriptions-item>
        <el-descriptions-item label="报告时间">{{ formatDateTime(record.reported_at) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(record.created_at) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(record.updated_at) }}</el-descriptions-item>
        <el-descriptions-item label="报告结论" :span="2">{{ record.report_conclusion || '—' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="record" class="section">
        <div class="section-title">状态操作</div>
        <div class="actions">
          <el-button v-if="record.status === 'PENDING'" type="primary" :loading="loading" @click="handleCollect">采样</el-button>
          <el-button v-if="record.status === 'PENDING'" type="danger" :loading="loading" @click="handleCancel">取消</el-button>
          <el-button v-if="record.status === 'COLLECTED'" type="primary" :loading="loading" @click="handleStart">开始检验</el-button>
          <el-button v-if="record.status === 'COLLECTED'" type="danger" :loading="loading" @click="handleCancel">取消</el-button>
          <el-button v-if="record.status === 'IN_PROGRESS'" type="success" :loading="loading" @click="openCompleteDialog">完成检验</el-button>
        </div>
      </div>

      <div v-if="record" class="section">
        <div class="section-title">检验明细</div>
        <el-table :data="record.items" border style="width: 100%">
          <el-table-column prop="item_name" label="项目名称" min-width="160" />
          <el-table-column label="结果" width="150">
            <template #default="{ row }">
              <span>{{ row.result ?? '—' }}</span><span v-if="row.unit" class="unit">{{ row.unit }}</span>
            </template>
          </el-table-column>
          <el-table-column label="参考范围" min-width="160">
            <template #default="{ row }">{{ row.reference_range || '—' }}</template>
          </el-table-column>
          <el-table-column label="异常标志" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="abnormalTagType(row.abnormal_flag)">{{ abnormalLabel(row.abnormal_flag) }}</el-tag>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无检验明细" />
          </template>
        </el-table>
      </div>

      <div v-if="record?.ai_interpretation" class="section">
        <div class="section-title">AI 解读</div>
        <div class="ai-interpretation">{{ record.ai_interpretation }}</div>
      </div>
    </el-card>

    <!-- 完成检验弹窗 -->
    <el-dialog v-model="completeDialogVisible" title="完成检验" width="820px">
      <el-form label-position="top">
        <el-form-item label="报告结论">
          <el-input
            v-model="completeForm.report_conclusion"
            type="textarea"
            :rows="3"
            placeholder="可选，输入报告结论"
          />
        </el-form-item>
        <el-form-item label="检验明细（项目名称必填，至少一项）">
          <el-table :data="completeForm.items" border style="width: 100%">
            <el-table-column label="项目名称" min-width="150">
              <template #default="{ row }">
                <el-input v-model="row.item_name" placeholder="项目名称" />
              </template>
            </el-table-column>
            <el-table-column label="结果" width="120">
              <template #default="{ row }">
                <el-input v-model="row.result" placeholder="结果" />
              </template>
            </el-table-column>
            <el-table-column label="单位" width="100">
              <template #default="{ row }">
                <el-input v-model="row.unit" placeholder="单位" />
              </template>
            </el-table-column>
            <el-table-column label="参考范围" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.reference_range" placeholder="参考范围" />
              </template>
            </el-table-column>
            <el-table-column label="异常标志" width="140">
              <template #default="{ row }">
                <el-select v-model="row.abnormal_flag">
                  <el-option label="正常" value="NORMAL" />
                  <el-option label="偏低" value="LOW" />
                  <el-option label="偏高" value="HIGH" />
                  <el-option label="危急低" value="CRITICAL_LOW" />
                  <el-option label="危急高" value="CRITICAL_HIGH" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button
                  type="danger"
                  link
                  :disabled="completeForm.items.length <= 1"
                  @click="removeItem($index)"
                >删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button class="add-item-btn" @click="addItem">+ 添加项目</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleComplete">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type {
  LabTestResponse,
  LabTestStatus,
  LabTestItemRequest,
  LabTestCompleteRequest,
  SampleType,
  AbnormalFlag,
} from '@aimedical/shared'
import AiDegradedBanner from '../../components/AiDegradedBanner.vue'

const route = useRoute()
const router = useRouter()
const recordId = Number(route.params.id)
const loading = ref(false)
const record = ref<LabTestResponse | null>(null)

const degraded = ref(false)
const fallbackReason = ref<string | null>(null)

const completeDialogVisible = ref(false)
const completeForm = reactive<{ report_conclusion: string; items: LabTestItemRequest[] }>({
  report_conclusion: '',
  items: [],
})

const formatDateTime = (iso: string | null): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

const sampleTypeMap: Record<SampleType, string> = {
  BLOOD: '血液',
  SERUM: '血清',
  PLASMA: '血浆',
  URINE: '尿液',
  STOOL: '粪便',
  SPUTUM: '痰液',
  OTHER: '其他',
}
const sampleTypeLabel = (t: SampleType): string => sampleTypeMap[t] || t

const statusLabelMap: Record<LabTestStatus, string> = {
  PENDING: '待采样',
  COLLECTED: '已采样',
  IN_PROGRESS: '检验中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}
const statusLabel = (s: LabTestStatus): string => statusLabelMap[s] || s

const statusTagTypeMap: Record<LabTestStatus, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
  PENDING: 'info',
  COLLECTED: 'warning',
  IN_PROGRESS: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'danger',
}
const statusTagType = (s: LabTestStatus): 'primary' | 'success' | 'info' | 'warning' | 'danger' =>
  statusTagTypeMap[s] || 'info'

const abnormalLabelMap: Record<AbnormalFlag, string> = {
  NORMAL: '正常',
  LOW: '偏低',
  HIGH: '偏高',
  CRITICAL_LOW: '危急低',
  CRITICAL_HIGH: '危急高',
}
const abnormalLabel = (f: AbnormalFlag): string => abnormalLabelMap[f] || f

const abnormalTagTypeMap: Record<AbnormalFlag, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
  NORMAL: 'success',
  LOW: 'warning',
  HIGH: 'warning',
  CRITICAL_LOW: 'danger',
  CRITICAL_HIGH: 'danger',
}
const abnormalTagType = (f: AbnormalFlag): 'primary' | 'success' | 'info' | 'warning' | 'danger' =>
  abnormalTagTypeMap[f] || 'info'

function newItem(): LabTestItemRequest {
  return { item_name: '', result: '', unit: '', reference_range: '', abnormal_flag: 'NORMAL' }
}

function openCompleteDialog() {
  completeForm.report_conclusion = ''
  completeForm.items = [newItem()]
  completeDialogVisible.value = true
}

function addItem() {
  completeForm.items.push(newItem())
}

function removeItem(index: number) {
  if (completeForm.items.length > 1) completeForm.items.splice(index, 1)
}

async function loadRecord() {
  loading.value = true
  try {
    const res = await doctorApi.getLabTest(recordId)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      return
    }
    record.value = res
  } finally {
    loading.value = false
  }
}

async function handleCollect() {
  loading.value = true
  try {
    const res = await doctorApi.collectSample(recordId)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      return
    }
    ElMessage.success('采样成功')
    record.value = res
  } finally {
    loading.value = false
  }
}

async function handleStart() {
  loading.value = true
  try {
    const res = await doctorApi.startLabTest(recordId)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      return
    }
    ElMessage.success('检验已开始')
    record.value = res
  } finally {
    loading.value = false
  }
}

async function handleCancel() {
  loading.value = true
  try {
    const res = await doctorApi.cancelLabTest(recordId)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      return
    }
    ElMessage.success('检验单已取消')
    record.value = res
  } finally {
    loading.value = false
  }
}

async function handleComplete() {
  const validItems = completeForm.items.filter((it) => it.item_name.trim())
  if (validItems.length === 0) {
    ElMessage.warning('请至少填写一项检验明细（项目名称必填）')
    return
  }
  loading.value = true
  try {
    const payload: LabTestCompleteRequest = {
      report_conclusion: completeForm.report_conclusion || undefined,
      items: validItems.map((it) => ({
        item_name: it.item_name.trim(),
        result: it.result || undefined,
        unit: it.unit || undefined,
        reference_range: it.reference_range || undefined,
        abnormal_flag: it.abnormal_flag,
      })),
    }
    const res = await doctorApi.completeLabTest(recordId, payload)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      return
    }
    ElMessage.success('检验已完成')
    completeDialogVisible.value = false
    record.value = res
  } finally {
    loading.value = false
  }
}

async function handleGenerateAi() {
  loading.value = true
  degraded.value = false
  fallbackReason.value = null
  try {
    const res = await doctorApi.generateLabTestAiReport(recordId)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      return
    }
    record.value = res
    if (!res.ai_interpretation) {
      degraded.value = true
      fallbackReason.value = 'AI 服务暂不可用，未能生成检验解读，请结合检验结果人工判读。'
      ElMessage.warning('AI 解读生成失败，已降级处理')
    } else {
      ElMessage.success('AI 检验报告已生成')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecord()
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
}

.section {
  margin-top: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.actions {
  display: flex;
  gap: 8px;
}

.unit {
  color: #909399;
  margin-left: 6px;
}

.ai-interpretation {
  background: #f5f7fa;
  border-left: 3px solid #409eff;
  padding: 12px 16px;
  border-radius: 4px;
  white-space: pre-wrap;
  line-height: 1.7;
}

.add-item-btn {
  margin-top: 12px;
}
</style>
