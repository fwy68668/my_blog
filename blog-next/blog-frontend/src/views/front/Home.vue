<template>
  <div class="fantasy-home">

    <header class="fantasy-nav">
      <div class="logo">✨ 冯文宇的奇妙幻想~</div>
      <div class="nav-actions">
        <a href="https://github.com" target="_blank" class="github-link">
          <svg viewBox="0 0 24 24" width="24" height="24"><path fill="currentColor" d="M12 2A10 10 0 0 0 2 12c0 4.42 2.87 8.17 6.84 9.5c.5.08.66-.23.66-.5v-1.69c-2.77.6-3.36-1.34-3.36-1.34c-.46-1.16-1.11-1.47-1.11-1.47c-.91-.62.07-.6.07-.6c1 .07 1.53 1.03 1.53 1.03c.87 1.52 2.34 1.07 2.91.83c.09-.65.35-1.09.63-1.34c-2.22-.25-4.55-1.11-4.55-4.92c0-1.11.38-2 1.03-2.71c-.1-.25-.45-1.29.1-2.64c0 0 .84-.27 2.75 1.02c.79-.22 1.65-.33 2.5-.33c.85 0 1.71.11 2.5.33c1.91-1.29 2.75-1.02 2.75-1.02c.55 1.35.2 2.39.1 2.64c.65.71 1.03 1.6 1.03 2.71c0 3.82-2.34 4.66-4.57 4.91c.36.31.69.92.69 1.85V21c0 .27.16.59.67.5C19.14 20.16 22 16.42 22 12A10 10 0 0 0 12 2Z"></path></svg>
        </a>

        <div class="theme-toggle" @click="toggleTheme">{{ isDark ? '🌙' : '☀️' }}</div>

        <el-button v-if="!isLogged" round type="primary" color="#6c5ce7" @click="router.push('/login')">登录 / 注册</el-button>
        <div v-else class="user-actions">

          <div class="user-profile-mini" @click="router.push('/profile')">
            <img v-if="currentUser?.avatar" :src="currentUser.avatar" class="mini-avatar" alt="avatar" />
            <div v-else class="mini-avatar-placeholder">{{ currentUser?.nickname?.charAt(0) || 'U' }}</div>
            <span class="welcome-text">{{ currentUser?.nickname || '幻想家' }}</span>
          </div>

          <el-button v-if="isAdmin" round plain @click="router.push('/admin')">👑 控制台</el-button>
          <el-button round plain @click="handleLogout">退出</el-button>
        </div>
      </div>
    </header>

    <section class="hero-section">
      <div class="hero-content">
        <div class="avatar-wrapper">
          <div class="hero-avatar-placeholder">宇</div>
        </div>
        <h1 class="hero-title">欢迎来到我的精神自留地</h1>
        <p class="hero-subtitle">这里记录着代码的严谨、生活的琐碎，以及那些天马行空的奇妙幻想。</p>
      </div>
    </section>

    <nav class="category-wrapper">
      <div class="category-scroll-container">
        <span :class="['tab', currentCategory === null ? 'active' : '']" @click="filterCategory(null)">全部宇宙</span>
        <span
            v-for="cat in categories"
            :key="cat.id"
            :class="['tab', currentCategory === cat.name ? 'active' : '']"
            @click="filterCategory(cat.name)"
        >
          {{ cat.name }}
        </span>
      </div>
    </nav>

    <main class="content-wrapper">
      <el-empty v-if="filteredArticles.length === 0" description="这片星空暂无故事..." />

      <div class="masonry-grid">
        <article
            v-for="item in filteredArticles"
            :key="item.id"
            class="article-card"
            @click="router.push(`/article/${item.id}`)"
        >
          <div class="article-header">
            <el-tag size="small" effect="light" color="#f0e6ff" style="border:none; color:#6c5ce7; margin-bottom: 10px;">
              {{ item.categoryName || '未分类' }}
            </el-tag>
            <h2 class="title">{{ item.title }}</h2>
          </div>
          <p class="excerpt">{{ extractExcerpt(item.content) }}</p>
          <div class="card-footer">
            <span class="time">{{ formatDate(item.createTime) }}</span>
            <div class="stats">
              <span>👍 {{ item.likeCount || 0 }}</span>
            </div>
          </div>
        </article>
      </div>
    </main>

    <footer class="fantasy-footer">
      <p>© 2026 冯文宇的奇妙幻想~ | Powered by Vue3 & SpringBoot</p>
    </footer>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const token = localStorage.getItem('blog_token')
const isLogged = ref(!!token)
const isAdmin = ref(false)
const isDark = ref(document.documentElement.classList.contains('dark'))
const currentUser = ref(null)

// 1. 解析 JWT 判断是否为管理员
if (token) {
  try {
    const payload = JSON.parse(decodeURIComponent(atob(token.split('.')[1]).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')))
    isAdmin.value = payload.role === 'ADMIN'
  } catch(e) {}
}

// 2. 如果已登录，拉取当前用户信息（包含头像和昵称）
if (isLogged.value) {
  request.get('/user/info').then(res => {
    currentUser.value = res
  }).catch(() => {})
}

// 3. 暗黑模式切换逻辑
const toggleTheme = () => {
  isDark.value = !isDark.value
  if (isDark.value) document.documentElement.classList.add('dark')
  else document.documentElement.classList.remove('dark')
}

// 4. 获取文章和分类数据
const articles = ref([])
const categories = ref([])
const currentCategory = ref(null)

onMounted(async () => {
  try {
    const [arts, cats] = await Promise.all([
      request.get('/articles'),
      request.get('/categories')
    ])
    categories.value = cats
    articles.value = arts
  } catch (error) {}
})

// 5. 分类过滤与摘要提取
const filteredArticles = computed(() => {
  if (!currentCategory.value) return articles.value
  return articles.value.filter(a => a.categoryName === currentCategory.value)
})

const filterCategory = (catName) => {
  currentCategory.value = catName
}

const extractExcerpt = (markdown) => {
  if (!markdown) return '...'
  const plainText = markdown.replace(/[#*`~>!-]/g, '').replace(/\[.*?\]\(.*?\)/g, '')
  return plainText.length > 80 ? plainText.substring(0, 80) + '...' : plainText
}

// 6. 登出逻辑
const handleLogout = () => {
  localStorage.removeItem('blog_token')
  isLogged.value = false
  isAdmin.value = false
  currentUser.value = null
  ElMessage.success('已退出登录')
}

// 7. 时间格式化
const formatDate = (dateString) => {
  if (!dateString) return ''
  const d = new Date(dateString)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}
</script>

<style scoped>
/* ================= 全局 & 布局 ================= */
.fantasy-home {
  min-height: 100vh;
  background: var(--bg-color, #fdfcff);
  color: var(--text-main, #2d3436);
  transition: background 0.3s, color 0.3s;
  display: flex;
  flex-direction: column;
}

/* ================= 导航栏 ================= */
.fantasy-nav {
  height: 65px;
  background: var(--nav-bg, rgba(255,255,255,0.8));
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10%;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid var(--border-color, #f1f2f6);
}
.logo { font-size: 20px; font-weight: bold; color: #6c5ce7; }
.nav-actions { display: flex; align-items: center; gap: 20px; }
.github-link { color: var(--text-main); transition: 0.3s; display: flex; align-items: center; }
.github-link:hover { color: #6c5ce7; }
.theme-toggle { cursor: pointer; font-size: 20px; user-select: none; transition: 0.3s;}
.theme-toggle:hover { transform: scale(1.1); }

/* ================= 🚨 终极头像锁死区 ================= */
.user-actions { display: flex; align-items: center; gap: 15px; }
.user-profile-mini {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 20px;
  transition: 0.3s;
}
.user-profile-mini:hover { background: var(--border-color, #f1f2f6); }

/* 强制覆盖原图尺寸，保证右上角头像绝对是 32x32 的正圆形 */
.mini-avatar {
  width: 32px !important;
  height: 32px !important;
  min-width: 32px !important;
  max-width: 32px !important;
  border-radius: 50% !important;
  object-fit: cover !important;
  display: block !important;
}
.mini-avatar-placeholder {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #fff;
  background: #6c5ce7;
}
.welcome-text { font-size: 14px; color: var(--text-sub, #666); font-weight: 500;}

/* ================= 巨幕区 ================= */
.hero-section { padding: 80px 20px; text-align: center; background: var(--hero-bg, linear-gradient(135deg, #f0e6ff 0%, #ffffff 100%)); border-bottom: 1px solid var(--border-color, transparent);}
.avatar-wrapper { margin: 0 auto 20px; display: flex; justify-content: center; }
.hero-avatar-placeholder { width: 80px; height: 80px; background: #6c5ce7; color: #fff; font-size: 36px; font-weight: bold; border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 10px 30px rgba(108, 92, 231, 0.3); }
.hero-title { font-size: 32px; font-weight: 800; margin-bottom: 15px; }
.hero-subtitle { font-size: 16px; color: var(--text-sub, #636e72); max-width: 600px; margin: 0 auto; line-height: 1.6; }

/* ================= 分类栏 ================= */
.category-wrapper { background: var(--nav-bg, rgba(255,255,255,0.9)); position: sticky; top: 65px; z-index: 99; border-bottom: 1px solid var(--border-color, #f1f2f6); padding: 15px 0; }
.category-scroll-container { display: flex; gap: 30px; overflow-x: auto; max-width: 900px; margin: 0 auto; padding: 0 20px; scrollbar-width: none; }
.category-scroll-container::-webkit-scrollbar { display: none; }
.tab { font-size: 15px; color: var(--text-sub, #636e72); cursor: pointer; white-space: nowrap; transition: 0.3s; padding-bottom: 5px; position: relative; }
.tab:hover { color: #6c5ce7; }
.tab.active { color: #6c5ce7; font-weight: bold; }
.tab.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 20px; height: 3px; background: #6c5ce7; border-radius: 2px; }

/* ================= 文章列表 ================= */
.content-wrapper { flex: 1; max-width: 1000px; margin: 40px auto; padding: 0 20px; width: 100%; box-sizing: border-box; }
.masonry-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 30px; }
.article-card { background: var(--card-bg, #fff); border-radius: 16px; padding: 25px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); border: 1px solid var(--border-color, transparent); cursor: pointer; transition: 0.3s; display: flex; flex-direction: column; }
.article-card:hover { transform: translateY(-5px); box-shadow: 0 10px 30px rgba(108, 92, 231, 0.1); border-color: rgba(108, 92, 231, 0.2); }
.title { font-size: 20px; margin: 0 0 10px 0; font-weight: 700; line-height: 1.4;}
.excerpt { font-size: 14px; color: var(--text-sub, #636e72); line-height: 1.6; margin-bottom: 20px; flex: 1; }
.card-footer { display: flex; justify-content: space-between; align-items: center; font-size: 13px; color: var(--text-sub, #a4b0be); border-top: 1px solid var(--border-color, #f1f2f6); padding-top: 15px; }

/* ================= 底部版权 ================= */
.fantasy-footer { background: var(--footer-bg, #2d3436); color: #b2bec3; text-align: center; padding: 30px 20px; margin-top: auto; font-size: 14px; }

/* ================= 全局暗黑模式变量 ================= */
:global(html.dark) {
  --bg-color: #0d1117;
  --nav-bg: rgba(13, 17, 23, 0.85);
  --card-bg: #161b22;
  --text-main: #c9d1d9;
  --text-sub: #8b949e;
  --border-color: #30363d;
  --hero-bg: linear-gradient(135deg, #161b22 0%, #0d1117 100%);
  --footer-bg: #010409;
}
</style>