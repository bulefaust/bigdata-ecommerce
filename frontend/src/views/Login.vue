<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>登录</h1>
      <p class="auth-sub">欢迎回到 DataMall</p>
      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="field">
          <label>用户名</label>
          <input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="current-password" />
        </div>
        <button type="submit" class="btn-login" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
      </form>
      <p class="auth-link">还没有账号？<router-link to="/register">免费注册</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api'
import { useUserStore } from '../store/user'
import { setTrackUserId } from '../tracker'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = ref({ username: '', password: '' })

async function handleLogin() {
  loading.value = true
  try {
    const user = await login(form.value.username, form.value.password)
    if (user) {
      userStore.setUser(user.id, user.username, user.role)
      setTrackUserId(user.id)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error('用户名或密码错误')
    }
  } catch {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-body);
  font-family: var(--font-body);
}

.auth-card {
  width: 420px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  position: relative;
}

.auth-card::before {
  content: '';
  display: block;
  height: 130px;
  background: var(--gradient-primary);
}

.auth-card h1 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: -90px 48px 6px;
  position: relative;
}

.auth-sub {
  font-size: 13px;
  color: rgba(255,255,255,0.65);
  margin: 0 48px 36px;
  position: relative;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 0 48px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.field input {
  padding: 14px 16px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  font-size: 14px;
  outline: none;
  color: var(--text-primary);
  background: var(--bg-elevated);
  transition: all var(--transition-base);
}

.field input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.15);
  background: var(--bg-surface);
}

.field input::placeholder {
  color: var(--text-tertiary);
}

.btn-login {
  padding: 14px;
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-xl);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
  transition: all var(--transition-base);
}

.btn-login:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow);
}

.btn-login:active:not(:disabled) {
  transform: translateY(0);
}

.btn-login:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.auth-link {
  text-align: center;
  margin-top: 24px;
  padding-bottom: 36px;
  font-size: 13px;
  color: var(--text-secondary);
}

.auth-link a {
  color: var(--color-primary-light);
  font-weight: 600;
  text-decoration: none;
  transition: color var(--transition-base);
}

.auth-link a:hover {
  color: var(--color-primary);
}
</style>
