<template>
  <div class="page">
    <el-button round @click="goBack" class="back-btn">
      <el-icon><ArrowLeft /></el-icon>返回列表
    </el-button>

    <div class="page-card">
      <div class="page-header">
        <div class="page-title">
          <el-icon :size="18"><Edit v-if="isEdit" /><Plus v-else /></el-icon>
          <span>{{ isEdit ? '编辑菜品' : '添加菜品' }}</span>
        </div>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="dish-form">
        <div class="form-grid">
          <div class="form-left">
            <el-form-item label="菜品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入菜品名称" />
            </el-form-item>
            <el-form-item label="所属分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" />
            </el-form-item>
            <el-form-item label="状态">
              <el-switch v-model="form.statusBool" />
            </el-form-item>
          </div>
          <div class="form-right">
            <el-form-item label="描述">
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="菜品描述" />
            </el-form-item>
            <el-form-item label="图片">
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                :on-success="handleUploadSuccess"
                :before-upload="beforeUpload"
                :show-file-list="false"
              >
                <div v-if="form.image" class="upload-preview">
                  <img :src="form.image" />
                  <div class="upload-mask"><el-icon><Edit /></el-icon> 更换图片</div>
                </div>
                <div v-else class="upload-placeholder">
                  <el-icon :size="28"><Plus /></el-icon>
                  <span>上传图片</span>
                </div>
              </el-upload>
            </el-form-item>
          </div>
        </div>
      </el-form>

      <div class="form-actions">
        <el-button size="large" round @click="goBack">取消</el-button>
        <el-button type="primary" size="large" round @click="handleSave">保存菜品</el-button>
      </div>
    </div>

    <!-- Spec Groups Editor -->
    <div class="page-card" v-if="isEdit && dishId" style="margin-top: 16px;">
      <div class="page-header">
        <div class="page-title">
          <el-icon :size="18"><Menu /></el-icon>
          <span>规格管理</span>
        </div>
        <el-button type="primary" size="small" round @click="openAddGroup">
          <el-icon><Plus /></el-icon>添加规格组
        </el-button>
      </div>
      <div v-if="specGroups.length === 0" class="empty-spec">暂无规格组，点击上方按钮添加</div>
      <div v-for="group in specGroups" :key="group.id" class="spec-group">
        <div class="spec-group-header">
          <div class="spec-group-info">
            <span class="group-name">{{ group.name }}</span>
            <span class="group-meta">{{ group.selectType === 0 ? '单选' : '多选' }} · {{ group.isRequired ? '必选' : '可选' }}</span>
          </div>
          <div class="spec-group-actions">
            <el-button size="small" round @click="openAddItem(group)">添加项</el-button>
            <el-button size="small" type="danger" round plain @click="handleDeleteGroup(group.id)">删除组</el-button>
          </div>
        </div>
        <div class="spec-items">
          <el-tag
            v-for="item in group.items"
            :key="item.id"
            closable
            class="spec-item-tag"
            @close="handleDeleteItem(item.id)"
          >
            {{ item.name }}{{ item.priceAdjust > 0 ? ' (+¥' + item.priceAdjust + ')' : item.priceAdjust < 0 ? ' (-¥' + Math.abs(item.priceAdjust) + ')' : '' }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- Add Group Dialog -->
    <el-dialog v-model="groupDialogVisible" title="添加规格组" width="420px" class="styled-dialog">
      <el-form ref="groupFormRef" :model="groupForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="groupForm.name" placeholder="如: 辣度、份量" /></el-form-item>
        <el-form-item label="选择类型">
          <el-radio-group v-model="groupForm.selectType">
            <el-radio :value="0">单选</el-radio>
            <el-radio :value="1">多选</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否必选">
          <el-switch v-model="groupForm.isRequiredBool" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupDialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="handleAddGroup" round>确定</el-button>
      </template>
    </el-dialog>

    <!-- Add Item Dialog -->
    <el-dialog v-model="itemDialogVisible" title="添加规格项" width="420px" class="styled-dialog">
      <el-form ref="itemFormRef" :model="itemForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="itemForm.name" placeholder="如: 微辣、大份" /></el-form-item>
        <el-form-item label="价格调整">
          <el-input-number v-model="itemForm.priceAdjust" :min="-999" :max="999" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="handleAddItem" round>确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategories } from '@/api/category'
import { getDishDetail, addDish, updateDish, addSpecGroup, deleteSpecGroup, addSpecItem, deleteSpecItem } from '@/api/dish'

const route = useRoute()
const router = useRouter()
const dishId = computed(() => route.params.id as string)
const isEdit = computed(() => !!route.params.id)

const categories = ref<any[]>([])
const specGroups = ref<any[]>([])
const formRef = ref()
const form = reactive({
  name: '', categoryId: null as any, price: 0, description: '', image: '', sortOrder: 0, statusBool: true
})
const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const uploadUrl = computed(() => isEdit.value ? `/api/admin/dishes/${dishId.value}/image` : '/api/admin/dishes/0/image')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('admin_token')}`
}))

const groupDialogVisible = ref(false)
const itemDialogVisible = ref(false)
const currentGroupId = ref('')
const groupForm = reactive({ name: '', selectType: 0, isRequiredBool: true })
const itemForm = reactive({ name: '', priceAdjust: 0 })
const groupFormRef = ref()
const itemFormRef = ref()

onMounted(async () => {
  const res = await getCategories()
  categories.value = res.data || []
  if (isEdit.value) {
    const res2 = await getDishDetail(dishId.value)
    if (res2.data) {
      const d = res2.data
      form.name = d.name; form.categoryId = d.categoryId; form.price = d.price
      form.description = d.description || ''; form.image = d.image || ''
      form.sortOrder = d.sortOrder || 0; form.statusBool = d.status === 1
      specGroups.value = d.specGroups || []
    }
  }
})

function goBack() { router.back() }

function handleUploadSuccess(response: any) {
  if (response.data) form.image = response.data.url || response.data
}

function beforeUpload(file: File) {
  const isImage = /^image\/(jpeg|png|gif|webp)$/.test(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt2M) { ElMessage.error('图片大小不能超过2MB'); return false }
  return true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  const data: any = { ...form, status: form.statusBool ? 1 : 0 }
  delete data.statusBool
  if (isEdit.value) {
    await updateDish(dishId.value, data)
    ElMessage.success('更新成功')
    router.push('/dishes')
  } else {
    await addDish(data)
    ElMessage.success('添加成功')
    router.push('/dishes')
  }
}

function openAddGroup() {
  groupForm.name = ''; groupForm.selectType = 0; groupForm.isRequiredBool = true
  groupDialogVisible.value = true
}

async function handleAddGroup() {
  await addSpecGroup(dishId.value, {
    name: groupForm.name,
    selectType: groupForm.selectType,
    isRequired: groupForm.isRequiredBool ? 1 : 0
  })
  ElMessage.success('规格组添加成功')
  groupDialogVisible.value = false
  refreshDish()
}

async function handleDeleteGroup(id: string) {
  await ElMessageBox.confirm('确定删除该规格组？', '提示', { type: 'warning' })
  await deleteSpecGroup(id)
  ElMessage.success('删除成功')
  refreshDish()
}

function openAddItem(group: any) {
  currentGroupId.value = group.id
  itemForm.name = ''; itemForm.priceAdjust = 0
  itemDialogVisible.value = true
}

async function handleAddItem() {
  await addSpecItem(currentGroupId.value, { ...itemForm })
  ElMessage.success('规格项添加成功')
  itemDialogVisible.value = false
  refreshDish()
}

async function handleDeleteItem(id: string) {
  await deleteSpecItem(id)
  ElMessage.success('删除成功')
  refreshDish()
}

async function refreshDish() {
  const res = await getDishDetail(dishId.value)
  if (res.data) specGroups.value = res.data.specGroups || []
}
</script>

<style scoped>
.back-btn { margin-bottom: 16px; }

.page-card {
  background: var(--bg-card); border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card); padding: 24px;
}
.page-header { display: flex; align-items: center; margin-bottom: 24px; }
.page-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: var(--text-primary); }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 40px; }
@media (max-width: 900px) { .form-grid { grid-template-columns: 1fr; } }
.dish-form :deep(.el-input) { width: 100%; }
.dish-form :deep(.el-select) { width: 100%; }

.upload-preview {
  width: 140px; height: 140px; border-radius: var(--radius); overflow: hidden;
  position: relative; cursor: pointer;
}
.upload-preview img { width: 100%; height: 100%; object-fit: cover; display: block; }
.upload-mask {
  position: absolute; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 13px; gap: 4px;
  opacity: 0; transition: opacity var(--transition-fast);
}
.upload-preview:hover .upload-mask { opacity: 1; }

.upload-placeholder {
  width: 140px; height: 140px; border-radius: var(--radius);
  border: 2px dashed var(--border-color);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 6px; color: var(--text-muted); cursor: pointer;
  transition: all var(--transition-fast);
}
.upload-placeholder:hover { border-color: var(--primary); color: var(--primary); }

.form-actions {
  display: flex; justify-content: center; gap: 12px; margin-top: 24px;
  padding-top: 20px; border-top: 1px solid var(--border-color);
}

.empty-spec { text-align: center; padding: 32px; color: var(--text-muted); }

.spec-group {
  border: 1px solid var(--border-color); border-radius: var(--radius);
  padding: 16px; margin-bottom: 12px; transition: border-color var(--transition-fast);
}
.spec-group:hover { border-color: var(--primary-light); }
.spec-group-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.spec-group-info { display: flex; align-items: center; gap: 10px; }
.group-name { font-weight: 700; color: var(--text-primary); }
.group-meta { font-size: 12px; color: var(--text-muted); }
.spec-group-actions { display: flex; gap: 8px; }
.spec-items { padding-left: 0; display: flex; flex-wrap: wrap; gap: 6px; }
.spec-item-tag { margin: 0 !important; }
</style>
