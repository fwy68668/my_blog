<template>
  <div class="manage-container">
    <div class="page-header">
      <h2>👥 幻想家管理 (用户大盘)</h2>
    </div>

    <el-table :data="users" v-loading="loading" style="width: 100%" class="custom-table">
      <el-table-column prop="id" label="ID" width="80" />

      <el-table-column label="用户身份" min-width="200">
        <template #default="{ row }">
          <div class="user-info-cell">
            <img v-if="row.avatar" :src="row.avatar" class="avatar" />
            <div v-else class="avatar-placeholder">{{ row.nickname?.charAt(0) || row.username.charAt(0) }}</div>
            <div class="name-block">
              <span class="nickname">{{ row.nickname || '未设置昵称' }}</span>
              <span class="username">@{{ row.username }}</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="role" label="权限角色" width="120">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'success'" effect="light" round>
            {{ row.role === 'ADMIN' ? '👑 创世神' : '🌟 幻想家' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="加入时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" align="right" fixed="right">
        <template #default="{ row }">
          <el-button
              v-if="row.role !== 'ADMIN'"
              type="danger"
              text
              @click="handleDelete(row.id)"
          >
            封禁踢出
          </el-button>
          <span v-else class="admin-protect">不可操作</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const loading = ref(false)

const fetchUsers = async () => {
  loading.value = true
  try {
    users.value = await request.get('/user/list')
  } catch (error) {} finally {
    loading.value = false
  }
}

onMounted(() => fetchUsers())

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要将该用户移出奇妙幻想吗？此操作不可逆！', '高危操作', {
    confirmButtonText: '确定踢出',
    cancelButtonText: '手滑了',
    type: 'error',
    confirmButtonColor: '#d93025'
  }).then(async () => {
    try {
      await request.delete(`/user/${id}`)
      ElMessage.success('用户已被清理')
      fetchUsers() // 刷新列表
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

/* 用户信息单元格美化 */
.user-info-cell { display: flex; align-items: center; gap: 12px; }
.avatar, .avatar-placeholder { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
.avatar-placeholder { display: flex; align-items: center; justify-content: center; background: #6c5ce7; color: #fff; font-weight: bold; font-size: 16px; }
.name-block { display: flex; flex-direction: column; }
.nickname { font-size: 14px; font-weight: 600; color: #2d3436; }
.username { font-size: 12px; color: #a4b0be; }
.admin-protect { font-size: 12px; color: #b2bec3; }
</style>