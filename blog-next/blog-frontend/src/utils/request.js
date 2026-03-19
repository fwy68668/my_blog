import axios from 'axios'
import { ElMessage } from 'element-plus'
// 注意：这里引入路由是为了在 401 时自动跳转
import router from '@/router'

// 创建 Axios 实例
const service = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API, // 对应 .env.development 里的 /api
    timeout: 10000 // 超时时间
})

// 1. 请求拦截器 (Request Interceptor)
service.interceptors.request.use(
    config => {
        // 每次发送请求前，自动从 localStorage 去拿 Token
        const token = localStorage.getItem('blog_token')
        if (token) {
            // 完美契合我们后端的 Bearer 校验规则
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 2. 响应拦截器 (Response Interceptor)
service.interceptors.response.use(
    response => {
        const res = response.data
        // 我们后端定义的成功状态码是 200
        if (res.code === 200) {
            return res.data // 直接返回核心数据，剥离外层的 code 和 message
        }
        // 401 代表 Token 过期或未登录
        else if (res.code === 401) {
            ElMessage.error('登录已过期或未授权，请重新登录')
            localStorage.removeItem('blog_token') // 清除失效的 Token
            router.push('/login') // 强制踢回登录页
            return Promise.reject(new Error(res.message))
        }
        // 其他业务报错 (如密码错误、用户名已存在)
        else {
            ElMessage.error(res.message || '系统开小差了')
            return Promise.reject(new Error(res.message))
        }
    },
    error => {
        // 处理 HTTP 级别的网络错误 (如 500, 404)
        let msg = '网络错误，请稍后再试'
        if (error.response && error.response.status === 403) {
            msg = '权限不足，只有管理员可操作！'
        }
        ElMessage.error(msg)
        return Promise.reject(error)
    }
)

export default service