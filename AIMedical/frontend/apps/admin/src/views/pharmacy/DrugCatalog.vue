<template>
  <div class="page-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>药品目录</h2>
          <div class="header-actions">
            <el-input
              v-model="queryForm.drugCode"
              placeholder="药品编码"
              clearable
              size="default"
              style="width: 140px"
              @keyup.enter="search"
            />
            <el-input
              v-model="queryForm.drugName"
              placeholder="药品名称"
              clearable
              size="default"
              style="width: 160px"
              @keyup.enter="search"
            />
            <el-select
              v-model="queryForm.drugCategory"
              placeholder="药品分类"
              clearable
              size="default"
              style="width: 140px"
            >
              <el-option label="西药" value="WESTERN_MEDICINE" />
              <el-option label="中成药" value="CHINESE_MEDICINE" />
              <el-option label="生物制品" value="BIOLOGICAL" />
              <el-option label="器械" value="DEVICE" />
            </el-select>
            <el-select
              v-model="queryForm.enabled"
              placeholder="状态"
              clearable
              size="default"
              style="width: 100px"
            >
              <el-option label="启用" :value="true" />
              <el-option label="停用" :value="false" />
            </el-select>
            <el-button :loading="loading" @click="search">查询</el-button>
            <el-button type="primary" @click="handleAdd">新增药品</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" border style="width: 100%">
        <el-table-column label="药品编码" prop="drug_code" width="120" />
        <el-table-column label="药品名称" prop="drug_name" min-width="160" />
        <el-table-column label="通用名" prop="generic_name" width="120">
          <template #default="{ row }">{{ row.generic_name || '—' }}</template>
        </el-table-column>
        <el-table-column label="规格" prop="specification" width="120">
          <template #default="{ row }">{{ row.specification || '—' }}</template>
        </el-table-column>
        <el-table-column label="厂家" prop="manufacturer" width="120">
          <template #default="{ row }">{{ row.manufacturer || '—' }}</template>
        </el-table-column>
        <el-table-column label="剂型" prop="drug_form" width="100" align="center">
          <template #default="{ row }">{{ formatForm(row.drug_form) }}</template>
        </el-table-column>
        <el-table-column label="分类" prop="drug_category" width="110" align="center">
          <template #default="{ row }">{{ formatCategory(row.drug_category) }}</template>
        </el-table-column>
        <el-table-column label="单位" prop="unit" width="70" align="center" />
        <el-table-column label="零售价" width="100" align="right">
          <template #default="{ row }">{{ formatAmount(row.retail_price) }}</template>
        </el-table-column>
        <el-table-column label="采购价" width="100" align="right">
          <template #default="{ row }">{{ formatAmount(row.purchase_price) }}</template>
        </el-table-column>
        <el-table-column label="OTC" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.otc_flag" type="success" size="small">OTC</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.enabled" type="success" size="small">启用</el-tag>
            <el-tag v-else type="info" size="small">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" link @click="handleToggle(row)">
              {{ row.enabled ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无药品记录" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="loadList"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="formVisible" :title="editingId ? '编辑药品' : '新增药品'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="药品编码" prop="drug_code">
              <el-input v-model="form.drug_code" :disabled="!!editingId" placeholder="如 AMX001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="药品名称" prop="drug_name">
              <el-input v-model="form.drug_name" placeholder="如 阿莫西林胶囊" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="通用名">
              <el-input v-model="form.generic_name" placeholder="如 阿莫西林" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格">
              <el-input v-model="form.specification" placeholder="如 0.25g*24粒" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="厂家">
              <el-input v-model="form.manufacturer" placeholder="如 华北制药" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="剂型">
              <el-select v-model="form.drug_form" placeholder="选择剂型" clearable style="width: 100%">
                <el-option label="胶囊" value="CAPSULE" />
                <el-option label="片剂" value="TABLET" />
                <el-option label="颗粒" value="GRANULE" />
                <el-option label="注射液" value="INJECTION" />
                <el-option label="器械" value="DEVICE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="药品分类" prop="drug_category">
              <el-select v-model="form.drug_category" placeholder="选择分类" style="width: 100%">
                <el-option label="西药" value="WESTERN_MEDICINE" />
                <el-option label="中成药" value="CHINESE_MEDICINE" />
                <el-option label="生物制品" value="BIOLOGICAL" />
                <el-option label="器械" value="DEVICE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位">
              <el-input v-model="form.unit" placeholder="如 盒" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="零售价">
              <el-input-number
                v-model="form.retail_price"
                :min="0"
                :precision="2"
                :step="0.5"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购价">
              <el-input-number
                v-model="form.purchase_price"
                :min="0"
                :precision="2"
                :step="0.5"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="OTC 标志">
          <el-switch v-model="form.otc_flag" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { inventoryApi, isBusinessError } from '@aimedical/shared'
import type { DrugCatalogResponse, DrugCatalogCreateRequest } from '@aimedical/shared'

const loading = ref(false)
const submitting = ref(false)
const list = ref<DrugCatalogResponse[]>([])

const queryForm = reactive({
  drugCode: '',
  drugName: '',
  drugCategory: undefined as string | undefined,
  enabled: undefined as boolean | undefined,
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// ---- 查询 ----
function search() {
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
    const result = await inventoryApi.queryDrugs({
      drugCode: queryForm.drugCode || undefined,
      drugName: queryForm.drugName || undefined,
      drugCategory: queryForm.drugCategory || undefined,
      enabled: queryForm.enabled,
      page: currentPage.value - 1,
      size: pageSize.value,
    })
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    list.value = result.content ?? []
    total.value = result.total_elements ?? 0
  } finally {
    loading.value = false
  }
}

// ---- 新增/编辑 ----
const formVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive<DrugCatalogCreateRequest>({
  drug_code: '',
  drug_name: '',
  generic_name: '',
  specification: '',
  manufacturer: '',
  drug_form: '',
  drug_category: '',
  unit: '',
  retail_price: 0,
  purchase_price: 0,
  otc_flag: false,
  remark: '',
})

const rules: FormRules = {
  drug_code: [{ required: true, message: '请输入药品编码', trigger: 'blur' }],
  drug_name: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  drug_category: [{ required: true, message: '请选择药品分类', trigger: 'change' }],
}

function resetForm() {
  form.drug_code = ''
  form.drug_name = ''
  form.generic_name = ''
  form.specification = ''
  form.manufacturer = ''
  form.drug_form = ''
  form.drug_category = ''
  form.unit = ''
  form.retail_price = 0
  form.purchase_price = 0
  form.otc_flag = false
  form.remark = ''
}

function handleAdd() {
  editingId.value = null
  resetForm()
  formVisible.value = true
}

function handleEdit(row: DrugCatalogResponse) {
  editingId.value = row.id
  form.drug_code = row.drug_code
  form.drug_name = row.drug_name
  form.generic_name = row.generic_name ?? ''
  form.specification = row.specification ?? ''
  form.manufacturer = row.manufacturer ?? ''
  form.drug_form = row.drug_form ?? ''
  form.drug_category = row.drug_category
  form.unit = row.unit ?? ''
  form.retail_price = row.retail_price ?? 0
  form.purchase_price = row.purchase_price ?? 0
  form.otc_flag = row.otc_flag ?? false
  form.remark = row.remark ?? ''
  formVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const request: DrugCatalogCreateRequest = {
        drug_code: form.drug_code,
        drug_name: form.drug_name,
        generic_name: form.generic_name || undefined,
        specification: form.specification || undefined,
        manufacturer: form.manufacturer || undefined,
        drug_form: form.drug_form || undefined,
        drug_category: form.drug_category,
        unit: form.unit || undefined,
        retail_price: form.retail_price,
        purchase_price: form.purchase_price,
        otc_flag: form.otc_flag,
        remark: form.remark || undefined,
      }
      const result = editingId.value
        ? await inventoryApi.updateDrug(editingId.value, request)
        : await inventoryApi.createDrug(request)
      if (isBusinessError(result)) {
        ElMessage.error(result.message)
        return
      }
      ElMessage.success(editingId.value ? '更新成功' : '创建成功')
      formVisible.value = false
      loadList()
    } finally {
      submitting.value = false
    }
  })
}

// ---- 启用/停用 ----
async function handleToggle(row: DrugCatalogResponse) {
  loading.value = true
  try {
    const result = await inventoryApi.toggleDrugEnabled(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success(result.enabled ? '已启用' : '已停用')
    loadList()
  } finally {
    loading.value = false
  }
}

// ---- 删除 ----
async function handleDelete(row: DrugCatalogResponse) {
  try {
    await ElMessageBox.confirm(
      `确认删除药品「${row.drug_name}」？此操作为软删除。`,
      '确认删除',
      { type: 'warning' },
    )
  } catch {
    return
  }
  loading.value = true
  try {
    const result = await inventoryApi.deleteDrug(row.id)
    if (isBusinessError(result)) {
      ElMessage.error(result.message)
      return
    }
    ElMessage.success('删除成功')
    loadList()
  } finally {
    loading.value = false
  }
}

// ---- 工具函数 ----
const formatAmount = (n: number | null | undefined): string => {
  if (n === null || n === undefined) return '—'
  return '¥' + Number(n).toFixed(2)
}

const FORM_LABELS: Record<string, string> = {
  CAPSULE: '胶囊',
  TABLET: '片剂',
  GRANULE: '颗粒',
  INJECTION: '注射液',
  DEVICE: '器械',
}

const CATEGORY_LABELS: Record<string, string> = {
  WESTERN_MEDICINE: '西药',
  CHINESE_MEDICINE: '中成药',
  BIOLOGICAL: '生物制品',
  DEVICE: '器械',
}

const formatForm = (code: string | null | undefined): string =>
  (code && FORM_LABELS[code]) || '—'

const formatCategory = (code: string | null | undefined): string =>
  (code && CATEGORY_LABELS[code]) || '—'

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
  flex-wrap: wrap;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
