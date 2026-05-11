<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>注册</h1>
      <p class="auth-sub">创建你的 DataMall 账号</p>
      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="field">
          <label>用户名</label>
          <input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="new-password" />
        </div>
        <div class="field">
          <label>手机号</label>
          <input v-model="form.phone" placeholder="请输入手机号" autocomplete="tel" />
        </div>
        <button type="submit" class="btn-login" :disabled="loading">{{ loading ? '注册中...' : '注册' }}</button>
      </form>
      <p class="auth-link">已有账号？<router-link to="/login">去登录</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', password: '', phone: '' })

async function handleRegister() {
  loading.value = true
  try {
    await register(form.value)
    ElMessage.success('注册成功')
    router.push('/login')
  } catch {
    ElMessage.error('注册失败')
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
}

.auth-card {
  width: 400px;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 40px;
  box-shadow: var(--shadow-lg);
}

.auth-card h1 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.auth-sub {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 28px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}

.field input {
  padding: 10px 14px;
  border: 1px solid var(--border-medium);
  border-radius: var(--radius-sm);
  font-size: 14px;
  outline: none;
  font-family: inherit;
  color: var(--text-primary);
  transition: border-color 0.2s;
}

.field input:focus {
  border-color: var(--color-primary);
}

.field input::placeholder {
  color: var(--text-placeholder);
}

.btn-login {
  padding: 12px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  margin-top: 8px;
  transition: background 0.2s;
}

.btn-login:hover:not(:disabled) {
  background: var(--color-primary-dark);
}

.btn-login:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.auth-link {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: var(--text-muted);
}

.auth-link a {
  color: var(--color-primary);
  font-weight: 600;
}
</style>
