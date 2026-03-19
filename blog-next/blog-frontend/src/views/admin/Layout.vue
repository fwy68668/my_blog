<template>
  <el-container class="admin-layout">
    <el-aside width="240px" class="sidebar">
      <div class="logo">👑 控制台</div>
      <el-menu
          :default-active="$route.path"
          router
          class="admin-menu"
          background-color="#111"
          text-color="#a4b0be"
          active-text-color="#fff"
      >
        <el-menu-item index="/admin/publish">
          <span>✍️ 发布文章</span>
        </el-menu-item>
        <el-menu-item index="/admin/articles">
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Collection /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <span class="page-title">{{ getPageTitle }}</span>
        </div>
        <div class="header-right">
          <el-button plain @click="$router.push('/')">🏠 返回博客主页</el-button>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const getPageTitle = computed(() => {
  const pathMap = {
    '/admin/publish': '创作新文章',
    '/admin/articles': '文章数据管理',
    '/admin/categories': '博客模块分类'
  }
  return pathMap[route.path] || '管理中心'
})
</script>

<style scoped>
.admin-layout { height: 100vh; display: flex; }
.sidebar { background: #111; display: flex; flex-direction: column; box-shadow: 2px 0 10px rgba(0,0,0,0.1); z-index: 10; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 20px; font-weight: bold; letter-spacing: 2px; border-bottom: 1px solid #222; }
.admin-menu { flex: 1; border-right: none; }
.el-menu-item { font-size: 15px; margin: 10px; border-radius: 8px; }
.el-menu-item.is-active { background: #6c5ce7 !important; font-weight: bold; }

.admin-header { background: #fff; height: 60px; display: flex; align-items: center; justify-content: space-between; padding: 0 30px; border-bottom: 1px solid #f1f2f6; }
.page-title { font-size: 18px; font-weight: 600; color: #2d3436; }
.admin-main { background: #f4f5f7; padding: 30px; overflow-y: auto; }

/* 子页面切换动画 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s, transform 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(10px); }
</style>