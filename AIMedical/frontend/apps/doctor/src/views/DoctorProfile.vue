<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>个人中心</h2>
          <div>
            <el-button v-if="!editing" type="primary" @click="startEdit">编辑档案</el-button>
            <template v-else>
              <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
              <el-button @click="cancelEdit">取消</el-button>
            </template>
          </div>
        </div>
      </template>

      <div v-if="!editing && profile">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="医生ID">{{ profile.id }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ profile.user_id }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ profile.real_name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ genderLabel(profile.gender) }}</el-descriptions-item>
          <el-descriptions-item label="职称">{{ profile.title || '—' }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ profile.department || '—' }}</el-descriptions-item>
          <el-descriptions-item label="专长" :span="2">{{ profile.specialty || '—' }}</el-descriptions-item>
          <el-descriptions-item label="简介" :span="2">
            <span class="multiline-text">{{ profile.introduction || '—' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="执业许可证号">{{ profile.license_no || '—' }}</el-descriptions-item>
          <el-descriptions-item label="执业年限">
            {{ profile.practice_years != null ? `${profile.practice_years} 年` : '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="问诊费">
            {{ profile.consultation_fee != null ? `¥${Number(profile.consultation_fee).toFixed(2)}` : '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ profile.remark || '—' }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 16px"
        >
          <template #title>提示</template>
          姓名、用户ID、执业许可证号为敏感字段，如需修改请联系管理员。
        </el-alert>
      </div>

      <el-form v-if="editing && form" :model="form" label-position="top" class="profile-form">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="姓名（不可修改）">
              <el-input :model-value="profile?.real_name" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender" placeholder="选择性别" clearable>
                <el-option label="男" value="M" />
                <el-option label="女" value="F" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="职称">
              <el-input v-model="form.title" placeholder="如：主任医师" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科室">
              <el-input v-model="form.department" placeholder="如：内科" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="专长">
          <el-input v-model="form.specialty" placeholder="如：呼吸内科、心血管内科" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input
            v-model="form.introduction"
            type="textarea"
            :rows="4"
            placeholder="个人简介、研究方向、擅长领域等"
          />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="执业年限">
              <el-input-number v-model="form.practice_years" :min="0" :max="80" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="问诊费（元）">
              <el-input-number
                v-model="form.consultation_fee"
                :min="0"
                :precision="2"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { doctorApi, isBusinessError } from '@aimedical/shared'
import type { DoctorDto, DoctorProfileUpdateRequest } from '@aimedical/shared'

const loading = ref(false)
const saving = ref(false)
const editing = ref(false)
const profile = ref<DoctorDto | null>(null)
const form = ref<DoctorProfileUpdateRequest>({})

async function loadProfile() {
  loading.value = true
  try {
    const res = await doctorApi.getDoctorProfile()
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      return
    }
    profile.value = res
  } finally {
    loading.value = false
  }
}

function startEdit() {
  if (!profile.value) return
  form.value = {
    gender: profile.value.gender,
    title: profile.value.title,
    department: profile.value.department,
    specialty: profile.value.specialty,
    introduction: profile.value.introduction,
    practice_years: profile.value.practice_years,
    consultation_fee: profile.value.consultation_fee,
    remark: profile.value.remark,
  }
  editing.value = true
}

function cancelEdit() {
  editing.value = false
  form.value = {}
}

async function handleSave() {
  saving.value = true
  try {
    const res = await doctorApi.updateDoctorProfile(form.value)
    if (isBusinessError(res)) {
      ElMessage.error(res.message)
      return
    }
    profile.value = res
    editing.value = false
    form.value = {}
    ElMessage.success('档案已更新')
  } finally {
    saving.value = false
  }
}

function genderLabel(g: string | null | undefined): string {
  if (!g) return '—'
  if (g === 'M') return '男'
  if (g === 'F') return '女'
  return g
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  max-width: 900px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h2 {
  margin: 0;
}

.profile-form {
  max-width: 700px;
}

.multiline-text {
  white-space: pre-wrap;
}
</style>
