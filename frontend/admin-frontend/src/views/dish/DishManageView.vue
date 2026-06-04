<template>
  <div class="page">
    <div class="page-card">
      <div class="page-header">
        <div class="page-title">
          <el-icon :size="18"><Food /></el-icon>
          <span>菜品管理</span>
        </div>
        <el-button type="primary" @click="goCreate">
          <el-icon><Plus /></el-icon>
          添加菜品
        </el-button>
      </div>

      <div class="filter-bar">
        <el-input v-model="keyword" placeholder="搜索菜品名称..." clearable class="search-input" @change="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="categoryId" placeholder="全部分类" clearable @change="loadData">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>

      <el-table :data="dishes" stripe class="data-table">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column label="图片" width="90">
          <template #default="{ row }">
            <div class="thumb-wrapper">
              <el-image :src="row.image || '/placeholder.png'" class="dish-thumb" fit="cover" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="菜品名称" />
        <el-table-column label="价格" width="110">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="salesCount" label="销量" width="80" align="center" />
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
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button size="small" round @click="goEdit(row.id)">编辑</el-button>
            <el-button size="small" type="danger" round plain @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import { getDishes, deleteDish, updateDishStatus } from '@/api/dish'
import { getCategories } from '@/api/category'

const router = useRouter()
const dishes = ref<any[]>([])
const categories = ref<any[]>([])
const keyword = ref('')
const categoryId = ref<number | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(async () => {
  const res = await getCategories()
  categories.value = res.data || []
  loadData()
})

async function loadData() {
  const res = await getDishes({ keyword: keyword.value, categoryId: categoryId.value, page: page.value, size: size.value })
  if (res.data) {
    dishes.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

function goEdit(id: string) { router.push(`/dishes/${id}/edit`) }
function goCreate() { router.push('/dishes/create') }

async function handleDelete(id: string) {
  await ElMessageBox.confirm('确定删除该菜品？', '提示', { type: 'warning' })
  await deleteDish(id)
  ElMessage.success('删除成功')
  loadData()
}

async function toggleStatus(row: any) {
  const newStatus = row.status === 1 ? 0 : 1
  await updateDishStatus(row.id, newStatus)
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

.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.search-input { width: 240px; }
.search-input :deep(.el-input__wrapper) { border-radius: 24px; }

.data-table { border-radius: var(--radius); overflow: hidden; }
.data-table :deep(.el-table__row:hover) { background: rgba(var(--primary-rgb), 0.03); }

.thumb-wrapper { width: 56px; height: 56px; border-radius: 10px; overflow: hidden; }
.dish-thumb { width: 56px; height: 56px; display: block; transition: transform var(--transition); }
.thumb-wrapper:hover .dish-thumb { transform: scale(1.1); }

.price-text { font-weight: 600; color: #e74c3c; }

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
