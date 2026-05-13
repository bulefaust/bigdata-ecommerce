<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>注册</h1>
      <p class="auth-sub">创建你的 DataMall 账号</p>
      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="role-switch">
          <div class="role-option" :class="{ active: form.role === 'user' }" @click="switchRole('user')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            普通用户
          </div>
          <div class="role-option" :class="{ active: form.role === 'seller' }" @click="switchRole('seller')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            商家入驻
          </div>
        </div>

        <div class="field">
          <label>用户名</label>
          <input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
          <span class="field-error" v-if="errors.username">{{ errors.username }}</span>
        </div>
        <div class="field">
          <label>密码</label>
          <div class="password-wrapper">
            <input v-model="form.password" :type="showPwd ? 'text' : 'password'" placeholder="请输入密码（至少6位）" autocomplete="new-password" />
            <span class="pwd-toggle" @click="showPwd = !showPwd">{{ showPwd ? '🙈' : '👁️' }}</span>
          </div>
          <span class="field-error" v-if="errors.password">{{ errors.password }}</span>
        </div>
        <div class="field">
          <label>确认密码</label>
          <input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" autocomplete="new-password" />
          <span class="field-error" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</span>
        </div>
        <div class="field">
          <label>手机号</label>
          <input v-model="form.phone" placeholder="请输入手机号" autocomplete="tel" />
          <span class="field-error" v-if="errors.phone">{{ errors.phone }}</span>
        </div>

        <div class="field" v-if="form.role === 'seller'">
          <label>门店名称</label>
          <input v-model="form.storeName" placeholder="请输入您的门店名称" />
          <span class="field-error" v-if="errors.storeName">{{ errors.storeName }}</span>
        </div>

        <div class="privacy-box">
          <label class="privacy-check">
            <input type="checkbox" v-model="privacyAgreed" />
            <span class="checkmark"></span>
            <span class="privacy-text">
              我已阅读并同意
              <a href="javascript:;" @click.stop="showPrivacy = true">《用户隐私保护协议》</a>
              和
              <a href="javascript:;" @click.stop="showPrivacy = true">《信息安全保密告知书》</a>
            </span>
          </label>
          <span class="field-error" v-if="errors.privacy">{{ errors.privacy }}</span>
        </div>

        <button type="submit" class="btn-login" :disabled="loading">{{ loading ? '注册中...' : (form.role === 'seller' ? '商家入驻' : '注册') }}</button>
      </form>
      <p class="auth-link">已有账号？<router-link to="/login">去登录</router-link></p>
    </div>

    <div class="privacy-modal" v-if="showPrivacy" @click.self="showPrivacy = false">
      <div class="privacy-content">
        <h2>隐私保护与信息安全告知</h2>
        <div class="privacy-body">
          <h3>一、信息收集范围</h3>
          <p>本平台将收集您注册时提供的用户名、密码、手机号等信息。商家用户还需提供门店名称。我们仅收集为提供服务所必需的信息。</p>
          <h3>二、信息使用目的</h3>
          <p>您的个人信息仅用于：账号注册与身份验证、订单处理与交易服务、商品推荐与个性化服务、客户服务与投诉处理、法律法规要求的用途。</p>
          <h3>三、信息安全保障</h3>
          <p>我们采用行业标准的安全技术和管理措施保护您的个人信息：数据传输采用SSL/TLS加密、密码采用单向哈希加密存储、严格限制数据访问权限、定期进行安全审计与漏洞修复。</p>
          <h3>四、信息共享与披露</h3>
          <p>未经您的同意，我们不会向任何第三方共享您的个人信息，但以下情况除外：获得您的明确授权、法律法规要求、保护平台及用户的合法权益。</p>
          <h3>五、您的权利</h3>
          <p>您有权：查询和更正您的个人信息、删除您的账号及相关数据、撤回授权同意、获取您的个人信息副本。</p>
          <h3>六、保密义务</h3>
          <p>您应妥善保管账号和密码，因您个人原因导致账号密码泄露所引起的损失由您自行承担。如发现账号被盗用或存在安全风险，请立即通知我们。</p>
        </div>
        <button class="btn-privacy-close" @click="showPrivacy = false">我已知晓</button>
      </div>
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
const showPwd = ref(false)
const showPrivacy = ref(false)
const privacyAgreed = ref(false)
const form = ref({ username: '', password: '', confirmPassword: '', phone: '', role: 'user', storeName: '' })
const errors = ref({})

function switchRole(role) {
  form.value.role = role
  errors.value = {}
}

function validate() {
  const e = {}
  if (!form.value.username.trim()) e.username = '用户名不能为空'
  if (!form.value.password) e.password = '密码不能为空'
  else if (form.value.password.length < 6) e.password = '密码长度不能少于6位'
  if (!form.value.confirmPassword) e.confirmPassword = '请确认密码'
  else if (form.value.password !== form.value.confirmPassword) e.confirmPassword = '两次密码输入不一致'
  if (!form.value.phone.trim()) e.phone = '手机号不能为空'
  else if (!/^1[3-9]\d{9}$/.test(form.value.phone.trim())) e.phone = '请输入正确的手机号'
  if (form.value.role === 'seller' && !form.value.storeName.trim()) e.storeName = '门店名称不能为空'
  if (!privacyAgreed.value) e.privacy = '请阅读并同意隐私保护协议'
  errors.value = e
  return Object.keys(e).length === 0
}

async function handleRegister() {
  if (!validate()) return
  loading.value = true
  try {
    const data = {
      username: form.value.username.trim(),
      password: form.value.password,
      phone: form.value.phone.trim(),
      role: form.value.role
    }
    if (form.value.role === 'seller') {
      data.storeName = form.value.storeName.trim()
    }
    await register(data)
    ElMessage.success(form.value.role === 'seller' ? '商家入驻成功' : '注册成功')
    router.push('/login')
  } catch {
    ElMessage.error('注册失败，用户名可能已存在')
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
  width: 440px;
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
  margin: 0 48px 28px;
  position: relative;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 0 48px;
}

.role-switch {
  display: flex;
  gap: 10px;
}

.role-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-tertiary);
  background: var(--bg-elevated);
  transition: all var(--transition-base);
}

.role-option:hover {
  border-color: var(--border-default);
  color: var(--text-secondary);
}

.role-option.active {
  border-color: var(--color-primary);
  background: rgba(0, 212, 255, 0.08);
  color: var(--color-primary-light);
  box-shadow: 0 0 16px rgba(0, 212, 255, 0.1);
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
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

.password-wrapper {
  position: relative;
}

.password-wrapper input {
  width: 100%;
  padding-right: 44px;
  box-sizing: border-box;
}

.pwd-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  font-size: 16px;
  user-select: none;
}

.field-error {
  font-size: 12px;
  color: var(--color-red, #ef4444);
  padding-left: 2px;
}

.privacy-box {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.privacy-check {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.privacy-check input[type="checkbox"] {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  min-width: 18px;
  border: 1.5px solid var(--border-default);
  border-radius: 4px;
  background: var(--bg-elevated);
  position: relative;
  margin-top: 1px;
  transition: all var(--transition-fast);
}

.privacy-check input[type="checkbox"]:checked + .checkmark {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.privacy-check input[type="checkbox"]:checked + .checkmark::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 2px;
  width: 5px;
  height: 9px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.privacy-text {
  font-size: 12px;
  color: var(--text-tertiary);
  line-height: 1.6;
}

.privacy-text a {
  color: var(--color-primary-light);
  text-decoration: none;
  font-weight: 500;
}

.privacy-text a:hover {
  color: var(--color-primary);
  text-decoration: underline;
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
  margin-top: 4px;
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

.privacy-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.privacy-content {
  width: 560px;
  max-height: 80vh;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.privacy-content h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  padding: 24px 28px 16px;
  border-bottom: 1px solid var(--border-subtle);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.privacy-body {
  padding: 24px 28px;
  overflow-y: auto;
  flex: 1;
}

.privacy-body h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary-light);
  margin: 16px 0 8px;
}

.privacy-body h3:first-child {
  margin-top: 0;
}

.privacy-body p {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin: 0;
}

.btn-privacy-close {
  margin: 16px 28px 24px;
  padding: 12px;
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-xl);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-privacy-close:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-glow);
}
</style>
