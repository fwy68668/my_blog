<template>
  <div class="detail-container">
    <header class="detail-nav">
      <el-button plain @click="router.push('/')" class="back-btn">🔙 返回星海</el-button>
      <div class="theme-toggle" @click="toggleTheme">
        {{ isDark ? '🌙 暗夜' : '☀️ 破晓' }}
      </div>
    </header>

    <main class="article-body" v-if="article">
      <h1 class="title">{{ article.title }}</h1>
      <div class="meta">
        <el-tag size="small" effect="light" color="#f0e6ff" style="border:none; color:#6c5ce7">
          {{ article.categoryName || '未分类' }}
        </el-tag>
        <span class="time">{{ formatDate(article.createTime) }}</span>
      </div>

      <MdPreview
          :modelValue="article.content"
          :id="'preview-' + article.id"
          :theme="isDark ? 'dark' : 'light'"
      />

      <div class="interaction-bar">
        <div :class="['action-btn', hasLiked ? 'liked' : '']" @click="handleLike">
          <span class="emoji">👍</span>
          <span>{{ article.likeCount || 0 }} {{ hasLiked ? '已赞' : '点赞' }}</span>
        </div>
      </div>

      <div class="comment-section">
        <h3>💭 读者回音 ({{ comments.length }})</h3>

        <div class="comment-input-box">
          <el-input
              v-model="newComment"
              placeholder="写下你的奇思妙想... (需登录)"
              @keyup.enter="submitComment"
              :disabled="!isLogged"
          />
          <el-button type="primary" color="#6c5ce7" @click="submitComment">发射</el-button>
        </div>

        <div v-for="c in comments" :key="c.id" class="comment-item">
          <img
              v-if="c.avatar"
              :src="c.avatar"
              class="comment-avatar"
              @click="router.push(`/user/${c.userId}`)"
              alt="avatar"
          />
          <div
              v-else
              class="comment-avatar-placeholder"
              @click="router.push(`/user/${c.userId}`)"
          >
            {{ c.nickname?.charAt(0) || 'U' }}
          </div>

          <div class="comment-content">
            <div class="comment-meta-info">
              <span class="comment-nickname" @click="router.push(`/user/${c.userId}`)">
                {{ c.nickname || '匿名幻想家' }}
              </span>
              <span class="comment-time">{{ formatDate(c.createTime) }}</span>
            </div>
            <p class="comment-text">{{ c.content }}</p>
          </div>
        </div>

        <el-empty v-if="comments.length === 0" description="暂无回音，来做第一个摘星人吧！" />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { MdPreview } from 'md-editor-v3'

const route = useRoute()
const router = useRouter()
const articleId = route.params.id
const article = ref(null)
const comments = ref([])
const newComment = ref('')

const isLogged = ref(!!localStorage.getItem('blog_token'))
const isDark = ref(document.documentElement.classList.contains('dark'))

// 检查本地点赞缓存
const likedSet = ref(new Set(JSON.parse(localStorage.getItem('liked_articles') || '[]')))
const hasLiked = ref(likedSet.value.has(Number(articleId)))

const toggleTheme = () => {
  isDark.value = !isDark.value
  if (isDark.value) document.documentElement.classList.add('dark')
  else document.documentElement.classList.remove('dark')
}

const fetchData = async () => {
  try {
    article.value = await request.get(`/articles/${articleId}`)
    comments.value = await request.get(`/comments/article/${articleId}`)
  } catch (error) {}
}

onMounted(() => fetchData())

const handleLike = async () => {
  if (!isLogged.value) return ElMessage.warning('请先登录后操作！')

  try {
    const isCurrentlyLiked = await request.post(`/articles/${articleId}/like`)

    if (isCurrentlyLiked) {
      article.value.likeCount = (article.value.likeCount || 0) + 1
      hasLiked.value = true
      likedSet.value.add(Number(articleId))
      ElMessage.success('点赞成功！')
    } else {
      article.value.likeCount = Math.max(0, (article.value.likeCount || 0) - 1)
      hasLiked.value = false
      likedSet.value.delete(Number(articleId))
      ElMessage.success('已取消点赞！')
    }
    localStorage.setItem('liked_articles', JSON.stringify([...likedSet.value]))
  } catch (error) {}
}

const submitComment = async () => {
  if (!isLogged.value) return ElMessage.warning('请先登录后参与讨论！')
  if (!newComment.value.trim()) return ElMessage.warning('内容不能为空哦')
  try {
    await request.post('/comments', {
      articleId: articleId,
      content: newComment.value
    })
    ElMessage.success('发射成功！')
    newComment.value = ''
    comments.value = await request.get(`/comments/article/${articleId}`)
  } catch (error) {}
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const d = new Date(dateString)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<style scoped>
/* 全局容器 */
.detail-container { min-height: 100vh; background: var(--bg-color, #fdfcff); transition: background 0.3s, color 0.3s; padding-bottom: 50px; }
.detail-nav { height: 65px; display: flex; align-items: center; justify-content: space-between; padding: 0 10%; position: sticky; top: 0; background: var(--nav-bg, rgba(255,255,255,0.8)); backdrop-filter: blur(10px); z-index: 100; border-bottom: 1px solid var(--border-color, #f1f2f6); color: var(--text-main); }
.theme-toggle { cursor: pointer; font-size: 14px; user-select: none; }
.back-btn { border-radius: 20px; }

/* 文章主体 */
.article-body { max-width: 800px; margin: 40px auto; padding: 0 20px; color: var(--text-main, #2d3436); }
.title { font-size: 32px; font-weight: 800; margin-bottom: 15px; }
.meta { display: flex; gap: 15px; font-size: 14px; color: var(--text-sub, #a4b0be); margin-bottom: 40px; border-bottom: 1px solid var(--border-color, #f1f2f6); padding-bottom: 20px; }

/* 互动区 */
.interaction-bar { margin-top: 50px; display: flex; justify-content: center; padding-bottom: 30px; border-bottom: 1px dashed var(--border-color, #eee); }
.action-btn { display: flex; align-items: center; gap: 8px; font-size: 16px; color: var(--text-sub, #636e72); cursor: pointer; padding: 12px 30px; border-radius: 30px; background: var(--card-bg, #fff); border: 1px solid var(--border-color, #f1f2f6); transition: 0.3s; box-shadow: 0 4px 15px rgba(0,0,0,0.02); }
.action-btn:hover { border-color: #6c5ce7; color: #6c5ce7; }
.action-btn.liked { background: #6c5ce7; color: #fff; border-color: #6c5ce7; }

/* 评论区 */
.comment-section { margin-top: 40px; }
.comment-section h3 { color: var(--text-main); margin-bottom: 25px; }
.comment-input-box { display: flex; gap: 10px; margin-bottom: 40px; }

/* 单条评论项 */
.comment-item { display: flex; gap: 15px; margin-bottom: 30px; }

/* 🚨 锁死评论区头像大小 */
.comment-avatar, .comment-avatar-placeholder {
  width: 42px !important;
  height: 42px !important;
  min-width: 42px !important;
  border-radius: 50% !important;
  object-fit: cover !important;
  cursor: pointer;
  transition: transform 0.2s;
}
.comment-avatar:hover, .comment-avatar-placeholder:hover { transform: scale(1.05); }
.comment-avatar-placeholder { display: flex; align-items: center; justify-content: center; font-weight: bold; color: #fff; background: #eccc68; font-size: 18px; }

.comment-content { flex: 1; border-bottom: 1px solid var(--border-color, #f1f2f6); padding-bottom: 15px; }
.comment-meta-info { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.comment-nickname { font-size: 14px; font-weight: 600; color: var(--text-main); cursor: pointer; transition: color 0.2s; }
.comment-nickname:hover { color: #6c5ce7; }
.comment-time { font-size: 12px; color: var(--text-sub); }
.comment-text { margin: 0; font-size: 15px; color: var(--text-main); line-height: 1.6; }

/* 暗黑模式变量同步 */
:global(html.dark) {
  --bg-color: #0d1117;
  --nav-bg: rgba(13, 17, 23, 0.85);
  --card-bg: #161b22;
  --text-main: #c9d1d9;
  --text-sub: #8b949e;
  --border-color: #30363d;
}
:global(html.dark .md-editor-preview) { color: #c9d1d9 !important; }
:global(html.dark .md-editor-preview pre) { background-color: #161b22 !important; border: 1px solid #30363d !important; }
:global(html.dark .el-input__wrapper) { background-color: #161b22; box-shadow: 0 0 0 1px #30363d inset; }
</style>