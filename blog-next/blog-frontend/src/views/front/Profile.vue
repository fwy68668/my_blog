<template>
  <div class="profile-container">
    <header class="detail-nav">
      <el-button plain @click="router.push('/')">🔙 返回博客主页</el-button>
      <div class="nav-title">个人空间</div>
    </header>

    <main class="profile-body" v-if="userInfo">
      <div class="user-card">
        <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
        >
          <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar" />
          <div v-else class="avatar-placeholder">{{ userInfo.username?.charAt(0).toUpperCase() }}</div>
          <div class="avatar-hover-text">更换头像</div>
        </el-upload>

        <div class="user-info">
          <h1 class="username">{{ userInfo.username }}</h1>
          <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : 'info'" effect="dark" round>
            {{ userInfo.role === 'ADMIN' ? '👑 创世神' : '🌟 幻想家' }}
          </el-tag>
          <p class="join-time">加入于：{{ formatDate(userInfo.createTime) }}</p>
        </div>
      </div>

      <div class="liked-section">
        <h2>💖 我赞过的奇妙幻想</h2>
        <el-empty v-if="likedArticles.length === 0" description="还没留下过你的印记哦..." />

        <div class="masonry-grid">
          <article
              v-for="item in likedArticles"
              :key="item.id"
              class="article-card"
              @click="router.push(`/article/${item.id}`)"
          >
            <div class="article-header">
              <el-tag size="small" effect="light" color="#f0e6ff" style="border:none; color:#6c5ce7; margin-bottom: 10px;">
                {{ item.categoryName || '未分类' }}
              </el-tag>
              <h3 class="title">{{ item.title }}</h3>
            </div>
            <div class="card-footer">
              <span class="time">{{ formatDate(item.createTime) }}</span>
              <span>👍 {{ item.likeCount || 0 }}</span>
            </div>
          </article>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const userInfo = ref(null)
const likedArticles = ref([])

onMounted(async () => {
  try {
    // 1. 获取个人信息
    userInfo.value = await request.get('/user/info')
    // 2. 获取点赞列表
    likedArticles.value = await request.get('/articles/my/liked')
  } catch (error) {}
})

// 头像上传成功后的回调
const handleAvatarSuccess = async (res) => {
  // 注意：因为我们后端直接返回了 URL 字符串，如果 request.js 没有拦截这里，res.data 就是 URL
  // Vite 代理模式下，el-upload 返回的是原生 Axios 结构
  const avatarUrl = res.data || res
  userInfo.value.avatar = avatarUrl

  try {
    // 调用更新接口保存到数据库
    await request.put('/user/update', { avatar: avatarUrl })
    ElMessage.success('头像更新成功！')
  } catch (error) {}
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('头像只能是图片格式!')
  if (!isLt2M) ElMessage.error('头像图片大小不能超过 2MB!')
  return isImage && isLt2M
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const d = new Date(dateString)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}
</script>

<style scoped>
.profile-container { min-height: 100vh; background: #fdfcff; }
.detail-nav { height: 65px; display: flex; align-items: center; justify-content: space-between; padding: 0 10%; position: sticky; top: 0; background: rgba(255,255,255,0.8); backdrop-filter: blur(10px); z-index: 100; border-bottom: 1px solid #f1f2f6; }
.nav-title { font-weight: bold; color: #111; }

.profile-body { max-width: 900px; margin: 40px auto; padding: 0 20px; }

/* 用户信息卡片 */
.user-card { display: flex; align-items: center; gap: 30px; background: #fff; padding: 40px; border-radius: 20px; box-shadow: 0 10px 40px rgba(0,0,0,0.04); margin-bottom: 40px; }
.avatar-uploader { position: relative; width: 100px; height: 100px; border-radius: 50%; overflow: hidden; cursor: pointer; border: 2px dashed #dcdfe6; transition: 0.3s; }
.avatar-uploader:hover { border-color: #6c5ce7; }
.avatar, .avatar-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 36px; background: #f0e6ff; color: #6c5ce7; font-weight: bold; object-fit: cover; }
.avatar-hover-text { position: absolute; bottom: 0; left: 0; width: 100%; background: rgba(0,0,0,0.5); color: #fff; font-size: 12px; text-align: center; padding: 4px 0; opacity: 0; transition: 0.3s; }
.avatar-uploader:hover .avatar-hover-text { opacity: 1; }

.user-info h1 { margin: 0 0 10px 0; font-size: 28px; color: #2d3436; }
.join-time { font-size: 13px; color: #a4b0be; margin-top: 10px; }

/* 点赞列表 */
.liked-section h2 { font-size: 22px; color: #2d3436; margin-bottom: 20px; }
.masonry-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
.article-card { background: #fff; border-radius: 12px; padding: 20px; border: 1px solid #f1f2f6; cursor: pointer; transition: 0.3s; }
.article-card:hover { transform: translateY(-3px); box-shadow: 0 10px 20px rgba(108, 92, 231, 0.08); border-color: #dcdde1; }
.title { font-size: 18px; margin: 0 0 15px 0; color: #2d3436; }
.card-footer { display: flex; justify-content: space-between; font-size: 13px; color: #a4b0be; border-top: 1px solid #f1f2f6; padding-top: 10px; }
</style>