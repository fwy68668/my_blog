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
              <input
                  v-model="registerForm.username"
                  type="text"
                  placeholder="设置账号 (最少6位)"
                  @blur="validateUsername"
                  :class="getInputClass('username')"
              />
              <span v-if="feedback.username.msg" :class="['feedback-text', feedback.username.type]">
                {{ feedback.username.msg }}
              </span>
            </div>

            <div class="input-group">
              <input
                  v-model="registerForm.nickname"
                  type="text"
                  placeholder="设置炫酷的昵称"
                  @blur="validateNickname"
                  :class="getInputClass('nickname')"
              />
              <span v-if="feedback.nickname.msg" :class="['feedback-text', feedback.nickname.type]">
                {{ feedback.nickname.msg }}
              </span>
            </div>

            <div class="input-group">
              <input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="设置密码 (最少8位，含英文和数字)"
                  @blur="validatePassword"
                  :class="getInputClass('password')"
              />
              <span v-if="feedback.password.msg" :class="['feedback-text', feedback.password.type]">
                {{ feedback.password.msg }}
              </span>
            </div>

            <div class="input-group">
              <input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请再次确认密码"
                  @blur="validateConfirm"
                  @keyup.enter="handleRegister"
                  :class="getInputClass('confirmPassword')"
              />
              <span v-if="feedback.confirmPassword.msg" :class="['feedback-text', feedback.confirmPassword.type]">
                {{ feedback.confirmPassword.msg }}
              </span>
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
const registerForm = reactive({ username: '', nickname: '', password: '', confirmPassword: '' })

// ================= 实时校验系统 =================
// 存储每个字段的反馈信息 type: 'error' | 'success', msg: 提示文字
const feedback = reactive({
  username: { type: '', msg: '' },
  nickname: { type: '', msg: '' },
  password: { type: '', msg: '' },
  confirmPassword: { type: '', msg: '' }
})

// 动态获取输入框的红/蓝边框样式
const getInputClass = (field) => {
  if (feedback[field].type === 'error') return 'input-error'
  if (feedback[field].type === 'success') return 'input-success'
  return ''
}

const validateUsername = () => {
  const val = registerForm.username
  if (!val) feedback.username = { type: 'error', msg: '账号不能为空' }
  else if (val.length < 6) feedback.username = { type: 'error', msg: '账号长度不能少于6位' }
  else feedback.username = { type: 'success', msg: '✓ 账号可用' }
}

const validateNickname = () => {
  const val = registerForm.nickname
  if (!val) feedback.nickname = { type: 'error', msg: '起个响亮的昵称吧' }
  else feedback.nickname = { type: 'success', msg: '✓ 昵称很棒' }
}

const validatePassword = () => {
  const val = registerForm.password
  const pwdRegex = /(?=.*[a-zA-Z])(?=.*[0-9])/
  if (!val) feedback.password = { type: 'error', msg: '密码不能为空' }
  else if (val.length < 8) feedback.password = { type: 'error', msg: '密码不能少于8位' }
  else if (!pwdRegex.test(val)) feedback.password = { type: 'error', msg: '必须同时包含英文和数字' }
  else feedback.password = { type: 'success', msg: '✓ 密码强度合格' }

  // 如果确认密码框已经填了，修改密码时联动重新校验一次
  if (registerForm.confirmPassword) validateConfirm()
}

const validateConfirm = () => {
  const val = registerForm.confirmPassword
  if (!val) feedback.confirmPassword = { type: 'error', msg: '请确认密码' }
  else if (val !== registerForm.password) feedback.confirmPassword = { type: 'error', msg: '两次密码不一致' }
  else feedback.confirmPassword = { type: 'success', msg: '✓ 密码一致' }
}


// ================= 接口提交逻辑 =================
const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) return ElMessage.warning('请填写完整信息')
  try {
    loading.value = true
    const token = await request.post('/user/login', loginForm)
    localStorage.setItem('blog_token', token)
    ElMessage.success('登录成功，欢迎来到奇妙幻想！')
    router.push('/')
  } catch (error) {
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  // 提交前，强行触发一遍所有校验（防止用户什么都不填直接点注册）
  validateUsername()
  validateNickname()
  validatePassword()
  validateConfirm()

  // 检查是否有 error 类型的反馈
  const hasError = Object.values(feedback).some(f => f.type === 'error')
  if (hasError) {
    return ElMessage.warning('请先修正标红的错误项哦！')
  }

  try {
    loading.value = true
    await request.post('/user/register', {
      username: registerForm.username,
      nickname: registerForm.nickname,
      password: registerForm.password
    })

    ElMessage.success('🎉 注册成功，赶快登录体验吧！')

    // 清空表单与校验状态
    Object.keys(registerForm).forEach(k => registerForm[k] = '')
    Object.keys(feedback).forEach(k => feedback[k] = { type: '', msg: '' })
    isLogin.value = true

  } catch (error) {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 基础样式保持不变 */
.login-container { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #fafafa; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
.glass-box { display: flex; width: 850px; height: 500px; background: #fff; border-radius: 20px; box-shadow: 0 20px 60px -15px rgba(0,0,0,0.05); overflow: hidden; border: 1px solid rgba(0,0,0,0.04); }
.brand-section { flex: 1; background: #111; color: white; padding: 40px; position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.brand-content { position: relative; z-index: 2; text-align: center; }
.brand-text { font-size: 28px; font-weight: 700; margin: 0; letter-spacing: -0.5px; }
.brand-subtext { margin-top: 12px; font-size: 14px; opacity: 0.6; }
.glow-ball { position: absolute; width: 300px; height: 300px; background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0) 70%); top: -50px; left: -50px; border-radius: 50%; }
.form-section { flex: 1.2; padding: 50px; display: flex; flex-direction: column; justify-content: center; }
.form-header { display: flex; align-items: center; margin-bottom: 35px; font-size: 20px; font-weight: 600; color: #a0a0a0; }
.form-header h3 { margin: 0; cursor: pointer; transition: 0.3s; }
.form-header h3.active { color: #111; }
.divider { margin: 0 15px; font-weight: 300; }

/* ================= 增强的输入框与反馈样式 ================= */
.input-group {
  margin-bottom: 22px; /* 加大间距给提示文字留出空间 */
  position: relative;
}

.input-group input {
  width: 100%;
  padding: 14px 18px;
  border: 1px solid #eaeaea;
  background: #fafafa;
  border-radius: 10px;
  font-size: 14px;
  color: #333;
  outline: none;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

/* 默认聚焦变黑 */
.input-group input:focus { border-color: #111; background: #fff; }

/* 失焦报错态 (极简红) */
.input-group input.input-error { border-color: #e00; background: #fffafa; }
.input-group input.input-error:focus { border-color: #e00; box-shadow: 0 0 0 1px #e00 inset; }

/* 失焦正确态 (Vercel 蓝) */
.input-group input.input-success { border-color: #0070f3; background: #fff; }
.input-group input.input-success:focus { border-color: #0070f3; box-shadow: 0 0 0 1px #0070f3 inset; }

/* 提示文字绝对定位，防止撑开布局导致页面抖动 */
.feedback-text {
  position: absolute;
  bottom: -18px;
  left: 4px;
  font-size: 12px;
  font-weight: 500;
  animation: fadeIn 0.2s ease;
}
.feedback-text.error { color: #e00; }
.feedback-text.success { color: #0070f3; }

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-2px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 按钮与其它 */
.primary-btn { width: 100%; padding: 14px; border: none; background: #111; color: #fff; border-radius: 10px; font-size: 15px; font-weight: 600; cursor: pointer; margin-top: 10px; transition: 0.2s; }
.primary-btn:hover { background: #333; }
.primary-btn.outline { background: transparent; color: #111; border: 1px solid #111; }
.primary-btn.outline:hover { background: #111; color: #fff; }
.guest-link { margin-top: 20px; text-align: center; font-size: 13px; color: #666; }
.guest-link span { cursor: pointer; transition: 0.2s; }
.guest-link span:hover { color: #111; text-decoration: underline; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s, transform 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(5px); }
</style>