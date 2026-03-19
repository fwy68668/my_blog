<template>
  <div class="manage-container">
    <div class="page-header">
      <h2>🏷️ 分类管理</h2>
      <el-button type="primary" color="#111" @click="dialogVisible = true">
        + 新增分类
      </el-button>
    </div>

    <el-table :data="categories" v-loading="loading" style="width: 100%" class="custom-table">
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="createTime" label="创建时间">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="right">
        <template #default="{ row }">
          <el-button type="danger" text @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增分类" width="400px" :show-close="false">
      <el-input v-model="newCategoryName" placeholder="输入你想创建的分类名称" @keyup.enter="submitAdd" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" color="#111" @click="submitAdd">确认创建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const newCategoryName = ref('')

const fetchCategories = async () => {
  loading.value = true
  try {
    categories.value = await request.get('/categories')
  } catch (error) {} finally {
    loading.value = false
  }
}

onMounted(() => fetchCategories())

const submitAdd = async () => {
  if (!newCategoryName.value.trim()) return ElMessage.warning('分类名称不能为空')
  try {
    // 调后端的 POST /api/categories (AdminOnly)
    await request.post('/categories', { name: newCategoryName.value })
    ElMessage.success('分类创建成功')
    dialogVisible.value = false
    newCategoryName.value = ''
    fetchCategories() // 刷新列表
  } catch (error) {}
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这个分类吗？这可能会影响该分类下的文章！', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonColor: '#d93025'
  }).then(async () => {
    try {
      await request.delete(`/categories/${id}`)
      ElMessage.success('删除成功')
      fetchCategories()
    } catch (error) {}
  }).catch(() => {})
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const d = new Date(dateString)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<style scoped>
.manage-container { background: #fff; padding: 30px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.02); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.page-header h2 { margin: 0; color: #2d3436; font-size: 20px; }
:deep(.custom-table) { border-radius: 8px; overflow: hidden; }
:deep(.el-table th.el-table__cell) { background-color: #f9fafb !important; color: #636e72; font-weight: 600; border-bottom: none; }
:deep(.el-table td.el-table__cell) { border-bottom: 1px solid #f1f2f6; }
</style>