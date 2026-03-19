<template>
  <div class="login-container">
    <div class="glass-box">
      <div class="brand-section">
        <div class="brand-content">
          <h2 class="brand-text">Vercel Style</h2>
          <p class="brand-subtext">Focus on your ideas, we handle the rest.</p>
        </div>
        <div class="glow-ball"></div>
      </div>

      <div class="form-section">
        <div class="form-header">
          <h3 :class="{ active: isLogin }" @click="isLogin = true">登录</h3>
          <span class="divider">/</span>
          <h3 :class="{ active: !isLogin }" @click="isLogin = false">注册</h3>
        </div>

        <transition name="fade" mode="out-in">
          <div v-if="isLogin" key="login" class="form-body">
            <div class="input-group">
              <input v-model="loginForm.username" type="text" placeholder="用户名" />
            </div>
            <div class="input-group">
              <input v-model="loginForm.password" type="password" placeholder="密码" @keyup.enter="handleLogin" />
            </div>
            <button class="primary-btn" @click="handleLogin" :disabled="loading">
              {{ loading ? '验证中...' : '进入控制台' }}
            </button>
            <div class="guest-link">
              <span @click="$router.push('/')">👈 作为游客先去逛逛博客主页</span>
            </div>
          </div>

          <div v-else key="register" class="form-body">
            <div class="input-group">
              <input v-model="registerForm.username" type="text" placeholder="设置账号 (用于登录)" />
            </div>
            <div class="input-group">
              <input v-model="registerForm.nickname" type="text" placeholder="设置炫酷的昵称" />
            </div>
            <div class="input-group">
              <input v-model="registerForm.password" type="password" placeholder="设置密码" @keyup.enter="handleRegister" />
            </div>
            <button class="primary-btn outline" @click="handleRegister" :disabled="loading">
              {{ loading ? '注册中...' : '立即注册' }}
            </button>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLogin = ref(true)
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', email: '', code: '', password: '',nickname:'' })


// 登录逻辑
const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) return ElMessage.warning('请填写完整信息')
  try {
    loading.value = true
    const token = await request.post('/user/login', loginForm)
    localStorage.setItem('blog_token', token)
    ElMessage.success('登录成功，欢迎来到奇妙幻想！')

    // 👇 修改这里：登录成功后默认回到主页
    router.push('/')

  } catch (error) {
  } finally {
    loading.value = false
  }
}

// 注册逻辑
const handleRegister = async () => {
  if (!registerForm.username || !registerForm.password) return ElMessage.warning('用户名和密码必填')
  try {
    loading.value = true
    await request.post('/user/register', {
      username: registerForm.username,
      password: registerForm.password
    })
    ElMessage.success('注册成功，请登录！')
    isLogin.value = true
  } catch (error) {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.glass-box {
  display: flex;
  width: 850px;
  height: 500px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 20px 60px -15px rgba(0,0,0,0.05);
  overflow: hidden;
  border: 1px solid rgba(0,0,0,0.04);
}

.brand-section {
  flex: 1;
  background: #111;
  color: white;
  padding: 40px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
}

.brand-text { font-size: 28px; font-weight: 700; margin: 0; letter-spacing: -0.5px; }
.brand-subtext { margin-top: 12px; font-size: 14px; opacity: 0.6; }

.glow-ball {
  position: absolute;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0) 70%);
  top: -50px;
  left: -50px;
  border-radius: 50%;
}

.form-section {
  flex: 1.2;
  padding: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  display: flex;
  align-items: center;
  margin-bottom: 35px;
  font-size: 20px;
  font-weight: 600;
  color: #a0a0a0;
}

.form-header h3 { margin: 0; cursor: pointer; transition: 0.3s; }
.form-header h3.active { color: #111; }
.divider { margin: 0 15px; font-weight: 300; }

.input-group { margin-bottom: 15px; }
.input-group input {
  width: 100%;
  padding: 14px 18px;
  border: 1px solid #eaeaea;
  background: #fafafa;
  border-radius: 10px;
  font-size: 14px;
  color: #333;
  outline: none;
  transition: 0.2s;
  box-sizing: border-box;
}
.input-group input:focus {
  border-color: #111;
  background: #fff;
}

.code-group { display: flex; gap: 10px; }
.code-btn {
  white-space: nowrap;
  padding: 0 15px;
  border: 1px solid #eaeaea;
  background: #fff;
  color: #111;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  transition: 0.2s;
}
.code-btn:hover:not(:disabled) { border-color: #111; }
.code-btn:disabled { color: #a0a0a0; cursor: not-allowed; }

.primary-btn {
  width: 100%;
  padding: 14px;
  border: none;
  background: #111;
  color: #fff;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 15px;
  transition: 0.2s;
}
.primary-btn:hover { background: #333; }
.primary-btn.outline { background: transparent; color: #111; border: 1px solid #111; }
.primary-btn.outline:hover { background: #111; color: #fff; }

.guest-link {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: #666;
}
.guest-link span { cursor: pointer; transition: 0.2s; }
.guest-link span:hover { color: #111; text-decoration: underline; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s, transform 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(5px); }
</style>