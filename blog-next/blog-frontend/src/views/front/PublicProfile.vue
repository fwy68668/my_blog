<template>
  <div class="profile-container">
    <header class="detail-nav">
      <el-button plain @click="$router.back()">🔙 返回</el-button>
      <div class="nav-title">TA 的主页</div>
    </header>

    <main class="profile-body" v-if="targetUser">
      <div class="user-card">
        <img v-if="targetUser.avatar" :src="targetUser.avatar" class="avatar" />
        <div v-else class="avatar-placeholder">{{ targetUser.nickname?.charAt(0) }}</div>

        <div class="user-info">
          <h1 class="username">{{ targetUser.nickname }}</h1>
          <el-tag :type="targetUser.role === 'ADMIN' ? 'danger' : 'info'" effect="dark" round>
            {{ targetUser.role === 'ADMIN' ? '👑 创世神' : '🌟 幻想家' }}
          </el-tag>
          <p class="join-time">TA 在 {{ formatDate(targetUser.createTime) }} 加入了奇妙幻想</p>
        </div>
      </div>

      <el-empty description="TA 还没有公开的动态哦..." />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const targetUser = ref(null)

onMounted(async () => {
  try {
    targetUser.value = await request.get(`/user/public/${route.params.id}`)
  } catch (error) {}
})

const formatDate = (date) => {
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}
</script>

<style scoped>
/* 这里直接把 Profile.vue 的 <style> 复制过来即可，完全通用！ */
.profile-container { min-height: 100vh; background: var(--bg-color, #fdfcff); transition: 0.3s; }
.detail-nav { height: 65px; display: flex; align-items: center; justify-content: space-between; padding: 0 10%; background: var(--nav-bg, rgba(255,255,255,0.8)); backdrop-filter: blur(10px); border-bottom: 1px solid var(--border-color, #f1f2f6); color: var(--text-main); }
.profile-body { max-width: 900px; margin: 40px auto; padding: 0 20px; }
.user-card { display: flex; align-items: center; gap: 30px; background: var(--card-bg, #fff); padding: 40px; border-radius: 20px; border: 1px solid var(--border-color); }
.avatar, .avatar-placeholder { width: 100px; height: 100px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 36px; background: #f0e6ff; color: #6c5ce7; font-weight: bold; object-fit: cover; }
.user-info h1 { margin: 0 0 10px 0; font-size: 28px; color: var(--text-main); }
.join-time { font-size: 13px; color: var(--text-sub); margin-top: 10px; }
</style>