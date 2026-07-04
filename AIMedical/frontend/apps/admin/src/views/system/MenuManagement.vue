<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>菜单管理</h2>
          <div class="header-actions">
            <el-button :loading="loading" @click="loadList">刷新</el-button>
            <el-button type="primary" @click="openCreate(null)">新建顶级菜单</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="treeData"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children' }"
        style="width: 100%"
      >
        <el-table-column label="名称" prop="name" min-width="200" />
        <el-table-column label="路径" prop="path" min-width="180">
          <template #default="{ row }">{{ row.path || '—' }}</template>
        </el-table-column>
        <el-table-column label="组件" prop="component" min-width="180">
          <template #default="{ row }">{{ row.component || '—' }}</template>
        </el-table-column>
        <el-table-column label="权限标识" prop="permission" min-width="160">
          <template #default="{ row }">{{ row.permission || '—' }}</template>
        </el-table-column>
        <el-table-column label="图标" prop="icon" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.icon">{{ row.icon }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" width="80" align="center" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openCreate(row)">新增子菜单</el-button>
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无菜单数据" />
        </template>
      </el-table>
    </el-card>

    <!-- 创建/编辑菜单对话框 -->
    <el-dialog
      v-model="formVisible"
      :title="editMode ? '编辑菜单' : '新建菜单'"
      width="640px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-select
            v-model="formData.parent_id"
            placeholder="顶级菜单（不选）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="m in parentOptions"
              :key="m.id"
              :label="m.label"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="菜单名称" style="width: 280px" />
        </el-form-item>
        <el-form-item label="权限标识" prop="permission">
          <el-input v-model="formData.permission" placeholder="唯一权限标识，如 menu:user:list" style="width: 320px" />
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="formData.path" placeholder="路由路径，如 /system/users" style="width: 320px" />
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="formData.component" placeholder="组件路径，如 system/UserManagement" style="width: 320px" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="formData.icon" placeholder="图标名称/emoji" style="width: 200px" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" style="width: 140px" />
        </el-form-item>
        <el-form-item label="是否可见">
          <el-switch v-model="formData.visible" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { menuApi, isBusinessError, type MenuItem } from '@aimedical/shared'

const loading = ref(false)
const submitting = ref(false)
const rawList = ref<MenuItem[]>([])

// ---- 列表查询 ----
async function loadList() {
  loading.value = true
  try {
    const result = await menuApi.all()
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      rawList.value = []
      return
    }
    rawList.value = result ?? []
  } finally {
    loading.value = false
  }
}

// 后端 /menu/all 返回的是扁平列表（含 children 字段为空数组或子菜单），
// 此处将扁平列表按 children 字段构建为树。
const treeData = computed<MenuItem[]>(() => {
  return rawList.value
})

// 父菜单下拉选项：扁平展示所有可选父菜单（排除自己，避免循环引用）
const parentOptions = computed(() => {
  const options: { id: number; label: string }[] = []
  const buildLabel = (name: string, indent: number): string => '　'.repeat(indent) + name

  function walk(items: MenuItem[], depth = 0) {
    items.forEach((item) => {
      options.push({ id: item.id, label: buildLabel(item.name, depth) })
      if (item.children && item.children.length > 0) {
        walk(item.children, depth + 1)
      }
    })
  }
  walk(rawList.value)
  return options
})

// ---- 创建/编辑 ----
const formVisible = ref(false)
const editMode = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const formData = reactive({
  parent_id: null as number | null,
  name: '',
  permission: '',
  path: '',
  component: '',
  icon: '',
  sort: 0,
  visible: true,
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  permission: [{ required: true, message: '请输入权限标识', trigger: 'blur' }],
}

function resetForm() {
  formData.parent_id = null
  formData.name = ''
  formData.permission = ''
  formData.path = ''
  formData.component = ''
  formData.icon = ''
  formData.sort = 0
  formData.visible = true
  editingId.value = null
  formRef.value?.clearValidate()
}

function openCreate(parent: MenuItem | null) {
  resetForm()
  editMode.value = false
  if (parent) {
    formData.parent_id = parent.id
  }
  formVisible.value = true
}

function openEdit(row: MenuItem) {
  resetForm()
  editMode.value = true
  editingId.value = row.id
  formData.name = row.name
  formData.permission = row.permission || ''
  formData.path = row.path || ''
  formData.component = row.component || ''
  formData.icon = row.icon || ''
  formData.sort = row.sort ?? 0
  // 编辑时不知道 parent_id（后端 MenuResponse 没有返回），保持顶级
  formData.parent_id = null
  formVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (editMode.value && editingId.value !== null) {
        const result = await menuApi.update(editingId.value, {
          name: formData.name,
          permission: formData.permission,
          path: formData.path || undefined,
          component: formData.component || undefined,
          icon: formData.icon || undefined,
          sort: formData.sort,
          visible: formData.visible,
        })
        if (isBusinessError(result)) {
          ElMessage.error(result.message)
          return
        }
        ElMessage.success('菜单已更新')
      } else {
        const result = await menuApi.create({
          name: formData.name,
          permission: formData.permission,
          parent_id: formData.parent_id,
          path: formData.path || undefined,
          component: formData.component || undefined,
          icon: formData.icon || undefined,
          sort: formData.sort,
          visible: formData.visible,
        })
        if (isBusinessError(result)) {
          ElMessage.error(result.message)
          return
        }
        ElMessage.success('菜单已创建')
      }
      formVisible.value = false
      loadList()
    } finally {
      submitting.value = false
    }
  })
}

// ---- 删除 ----
async function handleDelete(row: MenuItem) {
  const hasChildren = row.children && row.children.length > 0
  try {
    await ElMessageBox.confirm(
      `确认删除菜单「${row.name}」？${hasChildren ? '该菜单含子菜单，删除后子菜单也将一并移除。' : ''}`,
      '危险操作',
      { type: 'error' },
    )
  } catch {
    return
  }
  loading.value = true
  try {
    const result = await menuApi.delete(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('菜单已删除')
    loadList()
  } finally {
    loading.value = false
  }
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

.text-muted {
  color: #c0c4cc;
}
</style>
