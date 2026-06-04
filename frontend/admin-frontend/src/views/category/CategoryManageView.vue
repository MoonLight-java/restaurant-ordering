<template>
  <div class="page">
    <div class="page-card">
      <div class="page-header">
        <div class="page-title">
          <el-icon :size="18"><Menu /></el-icon>
          <span>分类管理</span>
        </div>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          添加分类
        </el-button>
      </div>
      <el-table :data="categories" stripe class="data-table">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column label="类型" width="140">
          <template #default="{ row }">
            <span class="type-tag" :class="'type-' + row.type">{{ typeMap[row.type] || '热菜' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :active-action-icon="Check"
              :inactive-action-icon="Close"
              @change="toggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button size="small" round @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" round plain @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '添加分类'" width="460px" class="styled-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type">
            <el-option :value="0" label="热菜" />
            <el-option :value="1" label="套餐" />
            <el-option :value="2" label="饮品" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.statusBool" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="handleSave" round>保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import { getAllCategories, addCategory, updateCategory, deleteCategory } from '@/api/category'

const typeMap: Record<number, string> = { 0: '热菜', 1: '套餐', 2: '饮品' }
const categories = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<string | number>('')
const formRef = ref()
const form = reactive({ name: '', type: 0, sortOrder: 0, statusBool: true })
const rules = { name: [{ required: true, message: '请输入名称', trigger: 'blur' }] }

onMounted(loadData)

async function loadData() {
  const res = await getAllCategories()
  categories.value = res.data || []
}

function openAddDialog() {
  isEdit.value = false; editId.value = 0
  form.name = ''; form.type = 0; form.sortOrder = 0; form.statusBool = true
  dialogVisible.value = true
}

function openEditDialog(row: any) {
  isEdit.value = true; editId.value = row.id
  form.name = row.name; form.type = row.type; form.sortOrder = row.sortOrder
  form.statusBool = row.status === 1
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  const data = { ...form, status: form.statusBool ? 1 : 0 }
  delete (data as any).statusBool
  if (isEdit.value) {
    await updateCategory(String(editId.value), data)
    ElMessage.success('更新成功')
  } else {
    await addCategory(data)
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
  await deleteCategory(String(id))
  ElMessage.success('删除成功')
  loadData()
}

async function toggleStatus(row: any) {
  const newStatus = row.status === 1 ? 0 : 1
  await updateCategory(row.id, { ...row, status: newStatus })
  ElMessage.success('状态更新成功')
  loadData()
}
</script>

<style scoped>
.page-card {
  background: var(--bg-card); border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card); padding: 24px;
}
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
}
.page-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: var(--text-primary); }
.data-table { border-radius: var(--radius); overflow: hidden; }
.data-table :deep(.el-table__row:hover) { background: rgba(var(--primary-rgb), 0.03); }
.type-tag {
  display: inline-block; padding: 2px 10px; border-radius: 20px; font-size: 12px;
  border: 1px solid var(--border-color);
}
.type-0 { background: #fef3e2; color: #E67E22; border-color: #fde3c8; }
.type-1 { background: #eaf7ee; color: #27ae60; border-color: #c8e6d0; }
.type-2 { background: #edf2fd; color: #3498db; border-color: #d0dff8; }
</style>
