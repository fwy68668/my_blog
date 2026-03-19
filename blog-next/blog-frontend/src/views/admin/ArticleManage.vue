<template>
  <div class="manage-container">
    <div class="page-header">
      <h2>📚 文章数据大盘</h2>
    </div>

    <el-table :data="articles" v-loading="loading" style="width: 100%" class="custom-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="文章标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="所属分类" width="150">
        <template #default="{ row }">
          <el-tag size="small" type="info" effect="light">{{ row.categoryName || '未分类' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="likeCount" label="获赞数" width="100">
        <template #default="{ row }">👍 {{ row.likeCount || 0 }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="right" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" text @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const articles = ref([])
const loading = ref(false)

const fetchArticles = async () => {
  loading.value = true
  try {
    articles.value = await request.get('/articles')
  } catch (error) {} finally {
    loading.value = false
  }
}

onMounted(() => fetchArticles())

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后文章无法恢复，及其所有评论也将被清空，确定吗？', '高危操作', {
    confirmButtonText: '挥泪删除',
    cancelButtonText: '手滑了',
    type: 'error',
    confirmButtonColor: '#d93025'
  }).then(async () => {
    try {
      // 调后端的 DELETE /api/articles/{id} (AdminOnly)
      await request.delete(`/articles/${id}`)
      ElMessage.success('文章已彻底删除')
      fetchArticles() // 刷新列表
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
.manage-container { background: #fff; padding: 30px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.02); min-height: calc(100vh - 120px); }
.page-header { margin-bottom: 30px; }
.page-header h2 { margin: 0; color: #2d3436; font-size: 20px; }
:deep(.custom-table) { border-radius: 8px; overflow: hidden; }
:deep(.el-table th.el-table__cell) { background-color: #f9fafb !important; color: #636e72; font-weight: 600; border-bottom: none; }
:deep(.el-table td.el-table__cell) { border-bottom: 1px solid #f1f2f6; }
</style>