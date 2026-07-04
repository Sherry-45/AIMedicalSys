<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>用户管理</h2>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名/昵称/手机号"
              clearable
              size="default"
              style="width: 220px"
              @keyup.enter="applyFilter"
            />
            <el-select
              v-model="userTypeFilter"
              placeholder="用户类型"
              clearable
              size="default"
              style="width: 120px"
            >
              <el-option label="管理员" value="ADMIN" />
              <el-option label="医生" value="DOCTOR" />
              <el-option label="患者" value="PATIENT" />
            </el-select>
            <el-select
              v-model="enabledFilter"
              placeholder="状态"
              clearable
              size="default"
              style="width: 100px"
            >
              <el-option label="启用" :value="true" />
              <el-option label="停用" :value="false" />
            </el-select>
            <el-button :loading="loading" @click="applyFilter">查询</el-button>
            <el-button type="primary" @click="openCreate">新建用户</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border style="width: 100%">
        <el-table-column label="ID" prop="id" width="70" />
        <el-table-column label="用户名" prop="username" width="130" />
        <el-table-column label="昵称" prop="nickname" width="130" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="userTypeTagType(row.user_type)" size="small">
              {{ userTypeLabel(row.user_type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="手机号" width="130">
          <template #default="{ row }">{{ row.phone || '—' }}</template>
        </el-table-column>
        <el-table-column label="角色" min-width="160">
          <template #default="{ row }">
            <el-tag
              v-for="r in row.roles || []"
              :key="r.id"
              size="small"
              type="info"
              style="margin-right: 4px"
            >{{ r.name }}</el-tag>
            <span v-if="!row.roles || row.roles.length === 0" class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" link @click="openResetPassword(row)">重置密码</el-button>
            <el-button
              size="small"
              :type="row.enabled ? 'danger' : 'success'"
              link
              @click="handleToggleEnabled(row)"
            >{{ row.enabled ? '停用' : '启用' }}</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无用户记录" />
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
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑用户对话框 -->
    <el-dialog
      v-model="formVisible"
      :title="editMode ? '编辑用户' : '新建用户'"
      width="640px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item v-if="!editMode" label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="3-32位字母数字下划线" style="width: 240px" />
        </el-form-item>
        <el-form-item v-if="!editMode" label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            show-password
            placeholder="8-64位"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="昵称" style="width: 240px" />
        </el-form-item>
        <el-form-item label="用户类型" prop="user_type">
          <el-select v-model="formData.user_type" placeholder="选择类型" style="width: 200px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="医生" value="DOCTOR" />
            <el-option label="患者" value="PATIENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.phone" placeholder="手机号" style="width: 200px" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="邮箱" style="width: 280px" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="formData.gender" placeholder="性别" clearable style="width: 120px">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="formData.age" :min="0" :max="150" style="width: 140px" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="formData.role_ids"
            multiple
            placeholder="选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="r in roleOptions"
              :key="r.id"
              :label="r.name + ' (' + r.code + ')'"
              :value="r.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位">
          <el-select
            v-model="formData.post_ids"
            multiple
            placeholder="选择岗位"
            style="width: 100%"
          >
            <el-option
              v-for="p in postOptions"
              :key="p.id"
              :label="p.name + ' (' + p.code + ')'"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="editMode" label="启用状态">
          <el-switch v-model="formData.enabled" />
        </el-form-item>
        <el-form-item label="下次登录改密">
          <el-switch v-model="formData.password_change_required" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPwdVisible" title="重置密码" width="480px">
      <el-form label-width="120px">
        <el-form-item label="用户">
          <span>{{ resetPwdTarget?.username }} ({{ resetPwdTarget?.nickname }})</span>
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input
            v-model="resetPwdForm.new_password"
            type="password"
            show-password
            placeholder="8-64位"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item label="下次登录改密">
          <el-switch v-model="resetPwdForm.password_change_required" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitResetPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  userManagementApi,
  roleManagementApi,
  postManagementApi,
  isBusinessError,
} from '@aimedical/shared'
import type {
  UserResponse,
  RoleResponse,
  PostResponse,
  UserTypeEnum,
} from '@aimedical/shared'

const loading = ref(false)
const submitting = ref(false)
const list = ref<UserResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchKeyword = ref('')
const userTypeFilter = ref<UserTypeEnum | ''>('')
const enabledFilter = ref<boolean | ''>('')

const roleOptions = ref<RoleResponse[]>([])
const postOptions = ref<PostResponse[]>([])

// ---- 列表查询 ----
function applyFilter() {
  currentPage.value = 1
  loadList()
}

function onSizeChange() {
  currentPage.value = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const result = await userManagementApi.query({
      keyword: searchKeyword.value.trim() || undefined,
      userType: userTypeFilter.value === '' ? undefined : userTypeFilter.value,
      enabled: enabledFilter.value === '' ? undefined : enabledFilter.value,
      page: currentPage.value - 1,
      size: pageSize.value,
    })
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

async function loadOptions() {
  const [roleResult, postResult] = await Promise.all([
    roleManagementApi.listAllEnabled(),
    postManagementApi.listAllEnabled(),
  ])
  if (!isBusinessError(roleResult)) {
    roleOptions.value = roleResult
  }
  if (!isBusinessError(postResult)) {
    postOptions.value = postResult
  }
}

// ---- 创建/编辑 ----
const formVisible = ref(false)
const editMode = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const formData = reactive({
  username: '',
  password: '',
  nickname: '',
  user_type: 'DOCTOR' as UserTypeEnum,
  phone: '',
  email: '',
  gender: '',
  age: undefined as number | undefined,
  role_ids: [] as number[],
  post_ids: [] as number[],
  enabled: true,
  password_change_required: false,
  remark: '',
})

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '长度 3-32', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 64, message: '长度 8-64', trigger: 'blur' },
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  user_type: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
}

function resetForm() {
  formData.username = ''
  formData.password = ''
  formData.nickname = ''
  formData.user_type = 'DOCTOR'
  formData.phone = ''
  formData.email = ''
  formData.gender = ''
  formData.age = undefined
  formData.role_ids = []
  formData.post_ids = []
  formData.enabled = true
  formData.password_change_required = false
  formData.remark = ''
  editingId.value = null
  formRef.value?.clearValidate()
}

function openCreate() {
  resetForm()
  editMode.value = false
  formVisible.value = true
}

function openEdit(row: UserResponse) {
  resetForm()
  editMode.value = true
  editingId.value = row.id
  formData.nickname = row.nickname
  formData.user_type = row.user_type
  formData.phone = row.phone || ''
  formData.email = row.email || ''
  formData.gender = row.gender || ''
  formData.age = row.age
  formData.role_ids = (row.roles || []).map((r) => r.id)
  formData.post_ids = (row.posts || []).map((p) => p.id)
  formData.enabled = row.enabled
  formData.password_change_required = row.password_change_required
  formData.remark = row.remark || ''
  formVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (editMode.value && editingId.value !== null) {
        const result = await userManagementApi.update(editingId.value, {
          nickname: formData.nickname,
          phone: formData.phone || undefined,
          email: formData.email || undefined,
          user_type: formData.user_type,
          gender: formData.gender || undefined,
          age: formData.age,
          role_ids: formData.role_ids,
          post_ids: formData.post_ids,
          enabled: formData.enabled,
          password_change_required: formData.password_change_required,
          remark: formData.remark || undefined,
        })
        if (isBusinessError(result)) {
          ElMessage.error(result.message)
          return
        }
        ElMessage.success('用户已更新')
      } else {
        const result = await userManagementApi.create({
          username: formData.username,
          password: formData.password,
          nickname: formData.nickname,
          user_type: formData.user_type,
          phone: formData.phone || undefined,
          email: formData.email || undefined,
          gender: formData.gender || undefined,
          age: formData.age,
          role_ids: formData.role_ids.length > 0 ? formData.role_ids : undefined,
          post_ids: formData.post_ids.length > 0 ? formData.post_ids : undefined,
          password_change_required: formData.password_change_required,
          remark: formData.remark || undefined,
        })
        if (isBusinessError(result)) {
          ElMessage.error(result.message)
          return
        }
        ElMessage.success('用户已创建')
      }
      formVisible.value = false
      loadList()
    } finally {
      submitting.value = false
    }
  })
}

// ---- 重置密码 ----
const resetPwdVisible = ref(false)
const resetPwdTarget = ref<UserResponse | null>(null)
const resetPwdForm = reactive({
  new_password: '',
  password_change_required: true,
})

function openResetPassword(row: UserResponse) {
  resetPwdTarget.value = row
  resetPwdForm.new_password = ''
  resetPwdForm.password_change_required = true
  resetPwdVisible.value = true
}

async function submitResetPassword() {
  if (!resetPwdTarget.value) return
  if (!resetPwdForm.new_password || resetPwdForm.new_password.length < 8) {
    ElMessage.warning('密码长度不能少于8位')
    return
  }
  submitting.value = true
  try {
    const result = await userManagementApi.resetPassword(resetPwdTarget.value.id, {
      new_password: resetPwdForm.new_password,
      password_change_required: resetPwdForm.password_change_required,
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('密码已重置')
    resetPwdVisible.value = false
  } finally {
    submitting.value = false
  }
}

// ---- 启用/停用 ----
async function handleToggleEnabled(row: UserResponse) {
  try {
    await ElMessageBox.confirm(
      `确认${row.enabled ? '停用' : '启用'}用户「${row.username}」？`,
      '提示',
      { type: 'warning' },
    )
  } catch {
    return
  }
  loading.value = true
  try {
    const result = await userManagementApi.toggleEnabled(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success(row.enabled ? '已停用' : '已启用')
    loadList()
  } finally {
    loading.value = false
  }
}

// ---- 删除 ----
async function handleDelete(row: UserResponse) {
  try {
    await ElMessageBox.confirm(
      `确认删除用户「${row.username}」？此操作为软删除，可恢复。`,
      '危险操作',
      { type: 'error' },
    )
  } catch {
    return
  }
  loading.value = true
  try {
    const result = await userManagementApi.delete(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('用户已删除')
    loadList()
  } finally {
    loading.value = false
  }
}

// ---- 工具函数 ----
const formatDateTime = (iso: string | null | undefined): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

const userTypeLabel = (t: UserTypeEnum): string => {
  const map: Record<UserTypeEnum, string> = { ADMIN: '管理员', DOCTOR: '医生', PATIENT: '患者' }
  return map[t] || t
}

const userTypeTagType = (t: UserTypeEnum): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<UserTypeEnum, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    ADMIN: 'danger',
    DOCTOR: 'primary',
    PATIENT: 'success',
  }
  return map[t] || 'info'
}

onMounted(() => {
  loadOptions()
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
