<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>角色管理</h2>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索编码/名称"
              clearable
              size="default"
              style="width: 200px"
              @keyup.enter="applyFilter"
            />
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
            <el-button type="primary" @click="openCreate">新建角色</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border style="width: 100%">
        <el-table-column label="ID" prop="id" width="70" />
        <el-table-column label="编码" prop="code" width="160" />
        <el-table-column label="名称" prop="name" width="160" />
        <el-table-column label="描述" min-width="180">
          <template #default="{ row }">{{ row.description || '—' }}</template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="140">
          <template #default="{ row }">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
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
          <el-empty description="暂无角色记录" />
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

    <!-- 创建/编辑角色对话框 -->
    <el-dialog
      v-model="formVisible"
      :title="editMode ? '编辑角色' : '新建角色'"
      width="560px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item v-if="!editMode" label="编码" prop="code">
          <el-input v-model="formData.code" placeholder="唯一编码，如 DOCTOR" style="width: 240px" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="角色名称" style="width: 240px" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" placeholder="角色描述" style="width: 360px" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" style="width: 140px" />
        </el-form-item>
        <el-form-item v-if="editMode" label="启用状态">
          <el-switch v-model="formData.enabled" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { roleManagementApi, isBusinessError } from '@aimedical/shared'
import type { RoleResponse } from '@aimedical/shared'

const loading = ref(false)
const submitting = ref(false)
const list = ref<RoleResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchKeyword = ref('')
const enabledFilter = ref<boolean | ''>('')

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
    const result = await roleManagementApi.query({
      keyword: searchKeyword.value.trim() || undefined,
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
    total.value = result.totalElements ?? 0
  } finally {
    loading.value = false
  }
}

// ---- 创建/编辑 ----
const formVisible = ref(false)
const editMode = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const formData = reactive({
  code: '',
  name: '',
  description: '',
  sort: 0,
  enabled: true,
  remark: '',
})

const formRules: FormRules = {
  code: [
    { required: true, message: '请输入编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度 2-50', trigger: 'blur' },
  ],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
}

function resetForm() {
  formData.code = ''
  formData.name = ''
  formData.description = ''
  formData.sort = 0
  formData.enabled = true
  formData.remark = ''
  editingId.value = null
  formRef.value?.clearValidate()
}

function openCreate() {
  resetForm()
  editMode.value = false
  formVisible.value = true
}

function openEdit(row: RoleResponse) {
  resetForm()
  editMode.value = true
  editingId.value = row.id
  formData.name = row.name
  formData.description = row.description || ''
  formData.sort = row.sort ?? 0
  formData.enabled = row.enabled
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
        const result = await roleManagementApi.update(editingId.value, {
          name: formData.name,
          description: formData.description || undefined,
          sort: formData.sort,
          enabled: formData.enabled,
          remark: formData.remark || undefined,
        })
        if (isBusinessError(result)) {
          ElMessage.error(result.message)
          return
        }
        ElMessage.success('角色已更新')
      } else {
        const result = await roleManagementApi.create({
          code: formData.code,
          name: formData.name,
          description: formData.description || undefined,
          sort: formData.sort,
          remark: formData.remark || undefined,
        })
        if (isBusinessError(result)) {
          ElMessage.error(result.message)
          return
        }
        ElMessage.success('角色已创建')
      }
      formVisible.value = false
      loadList()
    } finally {
      submitting.value = false
    }
  })
}

// ---- 启用/停用 ----
async function handleToggleEnabled(row: RoleResponse) {
  try {
    await ElMessageBox.confirm(
      `确认${row.enabled ? '停用' : '启用'}角色「${row.name}」？`,
      '提示',
      { type: 'warning' },
    )
  } catch {
    return
  }
  loading.value = true
  try {
    const result = await roleManagementApi.update(row.id, { enabled: !row.enabled })
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
async function handleDelete(row: RoleResponse) {
  try {
    await ElMessageBox.confirm(
      `确认删除角色「${row.name}」？该角色若仍关联用户将无法删除。`,
      '危险操作',
      { type: 'error' },
    )
  } catch {
    return
  }
  loading.value = true
  try {
    const result = await roleManagementApi.delete(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('角色已删除')
    loadList()
  } finally {
    loading.value = false
  }
}

// ---- 工具函数 ----
const formatDateTime = (iso: string | null | undefined): string =>
  iso ? new Date(iso).toLocaleString('zh-CN') : '—'

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

.text-muted {
  color: #c0c4cc;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
