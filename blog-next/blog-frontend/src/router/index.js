import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const parseJwt = (token) => {
    try {
        const base64Url = token.split('.')[1]
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
        return JSON.parse(decodeURIComponent(atob(base64).split('').map(c =>
            '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        ).join('')))
    } catch (e) {
        return null
    }
}

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('@/views/front/Home.vue')
    },
    {
        path: '/article/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/front/ArticleDetail.vue')
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue')
    },
    {
        // 🌟 新增：个人主页（任何人登录后都能看自己的点赞和改头像）
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/front/Profile.vue'),
        meta: { requiresAuth: true }
    },
    {
        // 🌟 修改：大后台管理布局（只有管理员能进）
        path: '/admin',
        component: () => import('@/views/admin/Layout.vue'),
        meta: { requiresAdmin: true },
        redirect: '/admin/publish', // 默认重定向到发布页
        children: [
            {
                path: 'publish',
                name: 'Publish',
                component: () => import('@/views/admin/Publish.vue')
            },
            {
                path: 'articles',
                name: 'ManageArticles',
                component: () => import('@/views/admin/ArticleManage.vue') // 稍后建
            },{
                path: '/admin/users',  // 👈 修复在这里：补上完整路径前缀和斜杠
                name: 'UserManage',
                component: () => import('@/views/admin/UserManage.vue'),
                meta: { requiresAdmin: true } // 别忘了加权限校验哦
            },
            {
                path: 'categories',
                name: 'ManageCategories',
                component: () => import('@/views/admin/CategoryManage.vue') // 稍后建
            }
        ]
    },
    {
        path: '/user/:id',
        name: 'PublicProfile',
        component: () => import('@/views/front/PublicProfile.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局路由守卫
router.beforeEach((to, from) => {
    const token = localStorage.getItem('blog_token')
    let payload = null

    // 🌟 修复 1：如果有 Token，提前解密并验证是否过期
    if (token) {
        payload = parseJwt(token)
        // JWT 的 exp 是秒，Date.now() 是毫秒，所以要乘 1000
        if (payload && payload.exp && (payload.exp * 1000 < Date.now())) {
            localStorage.removeItem('blog_token') // 清除死尸 Token
            ElMessage.warning('登录身份已过期，请重新登录！')

            // 如果去的页面需要权限，直接打回登录页；如果去主页，就当游客放行
            if (to.meta.requiresAuth || to.meta.requiresAdmin) {
                return '/login'
            }
            return true
        }
    }

    // 拦截未登录但需要权限的页面
    if ((to.meta.requiresAuth || to.meta.requiresAdmin) && !token) {
        ElMessage.warning('请先登录！')
        return '/login'
    }

    // 🌟 修复 2：只有管理员能进后台，防止普通用户硬敲 URL 强闯
    if (to.meta.requiresAdmin) {
        if (!payload || payload.role !== 'ADMIN') {
            ElMessage.error('权限不足：只有【冯文宇】本人才能进入控制台！')
            return '/'
        }
    }

    return true
})

export default router