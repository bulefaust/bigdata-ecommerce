<template>
  <div class="layout">
    <header class="top-bar">
      <div class="top-inner">
        <span class="welcome">欢迎来到 DataMall！</span>
        <div class="top-right">
          <template v-if="userStore.userId">
            <span class="user-greeting">Hi, {{ userStore.username }}</span>
            <a href="javascript:;" @click="handleLogout">退出登录</a>
          </template>
          <template v-else>
            <router-link to="/login">请登录</router-link>
            <router-link to="/register">免费注册</router-link>
          </template>
          <router-link to="/order">我的订单</router-link>
          <router-link v-if="userStore.role === 'seller' || userStore.isAdmin" to="/seller" class="seller-link">卖家中心</router-link>
          <router-link v-if="userStore.isAdmin" to="/admin" class="dash-link">管理后台</router-link>
          <router-link v-if="userStore.isAdmin" to="/review" class="review-link">商品审核</router-link>
          <router-link v-if="userStore.isAdmin" to="/dashboard" class="dash-link">数据大屏</router-link>
          <router-link v-if="userStore.isAdmin" to="/bigdata" class="bigdata-link">大数据分析</router-link>
        </div>
      </div>
    </header>

    <header class="main-header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">D</span>
          <div class="logo-text-wrap">
            <span class="logo-text">DataMall</span>
            <span class="logo-sub">智能电商平台</span>
          </div>
        </div>

        <div class="search-wrap">
          <div class="search-box">
            <input
              v-model="searchKeyword"
              placeholder="搜索你想要的商品..."
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
              搜索
            </button>
          </div>
          <div class="hot-words">
            <span v-for="w in hotWords" :key="w" @click="quickSearch(w)">{{ w }}</span>
          </div>
        </div>

        <div class="header-cart" @click="$router.push('/cart')">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="8" cy="21" r="1"/><circle cx="19" cy="21" r="1"/><path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"/></svg>
          <em v-if="cartStore.totalCount > 0">{{ cartStore.totalCount }}</em>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-top">
          <div class="footer-brand">
            <span class="footer-logo">DataMall</span>
            <p>数据驱动智慧购物，让每一次选择更懂你</p>
          </div>
          <div class="footer-links">
            <div class="footer-col">
              <h4>购物指南</h4>
              <a href="javascript:;">购物流程</a>
              <a href="javascript:;">会员介绍</a>
              <a href="javascript:;">常见问题</a>
            </div>
            <div class="footer-col">
              <h4>配送方式</h4>
              <a href="javascript:;">上门自提</a>
              <a href="javascript:;">快递运输</a>
              <a href="javascript:;">特快专递</a>
            </div>
            <div class="footer-col">
              <h4>支付方式</h4>
              <a href="javascript:;">货到付款</a>
              <a href="javascript:;">在线支付</a>
              <a href="javascript:;">分期付款</a>
            </div>
            <div class="footer-col">
              <h4>售后服务</h4>
              <a href="javascript:;">退换货政策</a>
              <a href="javascript:;">退换货流程</a>
              <a href="javascript:;">价格保护</a>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p>© 2026 DataMall 智能电商平台 · 数据驱动智慧购物</p>
        </div>
      </div>
    </footer>
    <a
      class="test-btn"
      href="javascript:;"
      @click="goRandomLink"
      target="_blank"
      rel="noopener"
    >我的喜好</a>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { useCartStore } from '../store/cart'
import { trackSearch } from '../tracker'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const hotWords = ['iPhone', '华为', '降噪耳机', 'MacBook', 'PS5', '无人机']

function handleSearch() {
  if (searchKeyword.value.trim()) {
    trackSearch(searchKeyword.value.trim())
    router.push({ path: '/', query: { keyword: searchKeyword.value.trim() } })
  }
}

function quickSearch(w) {
  searchKeyword.value = w
  handleSearch()
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

function goRandomLink() {
  const url = Math.random() < 0.5
    ? 'https://www.bilibili.com/video/BV1g9kBYzEdi/?spm_id_from=333.337.search-card.all.click&vd_source=68dccab6b08a022fa6dc2519a0360464'
    : 'https://ak.hypergryph.com/'
  window.open(url, '_blank')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-body);
}

.top-bar {
  background: transparent;
  border-bottom: 1px solid var(--border-subtle);
  font-size: 12px;
  color: var(--text-tertiary);
}

.top-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.top-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.top-right a {
  color: var(--text-tertiary);
  font-size: 12px;
  text-decoration: none;
  transition: color var(--transition-fast);
}

.top-right a:hover {
  color: var(--text-primary);
}

.user-greeting {
  color: var(--color-primary-light);
  font-weight: 600;
  font-size: 12px;
}

.main-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(11,17,32,0.85);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid var(--border-subtle);
  box-shadow: 0 1px 12px rgba(0,212,255,0.08);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 40px;
}

.logo {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: var(--gradient-primary);
  color: #fff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 900;
  font-family: var(--font-display);
  box-shadow: 0 0 20px rgba(0,212,255,0.3);
}

.logo-text-wrap {
  display: flex;
  flex-direction: column;
}

.logo-text {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.1;
  letter-spacing: -0.3px;
}

.logo-sub {
  font-size: 10px;
  color: var(--text-tertiary);
  line-height: 1;
  margin-top: 2px;
  letter-spacing: 0.5px;
}

.search-wrap {
  flex: 1;
  max-width: 520px;
}

.search-box {
  display: flex;
  border: 1px solid var(--border-default);
  border-radius: 12px;
  overflow: hidden;
  height: 40px;
  transition: all var(--transition-base);
  background: var(--bg-card);
}

.search-box:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(0,212,255,0.1);
  background: var(--bg-card-hover);
}

.search-input {
  flex: 1;
  border: none;
  padding: 0 16px;
  font-size: 14px;
  outline: none;
  font-family: var(--font-body);
  color: var(--text-primary);
  background: transparent;
}

.search-input::placeholder {
  color: var(--text-tertiary);
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 0 20px;
  background: transparent;
  color: var(--text-secondary);
  border: none;
  border-left: 1px solid var(--border-subtle);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: var(--font-body);
  transition: all var(--transition-fast);
}

.search-btn:hover {
  color: var(--color-primary);
  background: rgba(0,212,255,0.08);
}

.hot-words {
  display: flex;
  gap: 16px;
  margin-top: 6px;
  padding-left: 16px;
}

.hot-words span {
  font-size: 11px;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: color var(--transition-fast);
  padding: 2px 8px;
  border-radius: 6px;
}

.hot-words span:hover {
  color: var(--text-primary);
  background: var(--bg-card);
}

.header-cart {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  transition: all var(--transition-fast);
  background: var(--bg-card);
  color: var(--text-secondary);
  flex-shrink: 0;
}

.header-cart:hover {
  background: var(--bg-card-hover);
  color: var(--text-primary);
}

.header-cart em {
  position: absolute;
  top: -2px;
  right: -2px;
  background: var(--color-primary);
  color: #fff;
  font-size: 10px;
  font-style: normal;
  font-weight: 700;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  border-radius: 8px;
  padding: 0 3px;
  font-size: 9px;
}

.main-content {
  flex: 1;
}

.site-footer {
  background: var(--bg-surface);
  border-top: 1px solid var(--border-subtle);
  box-shadow: 0 -1px 12px rgba(0,212,255,0.06);
  margin-top: 60px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px 24px;
}

.footer-top {
  display: flex;
  justify-content: space-between;
  gap: 60px;
  padding-bottom: 36px;
  border-bottom: 1px solid var(--border-subtle);
}

.footer-brand {
  max-width: 260px;
}

.footer-logo {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  display: block;
  margin-bottom: 10px;
}

.footer-brand p {
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-tertiary);
}

.footer-links {
  display: flex;
  gap: 60px;
}

.footer-col h4 {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 14px;
  letter-spacing: 0.5px;
}

.footer-col a {
  display: block;
  font-size: 12px;
  color: var(--text-tertiary);
  text-decoration: none;
  margin-bottom: 10px;
  transition: color var(--transition-fast);
}

.footer-col a:hover {
  color: var(--text-primary);
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
}

.footer-bottom p {
  font-size: 11px;
  color: var(--text-muted);
}

.dash-link {
  color: var(--color-primary) !important;
  font-weight: 600;
}

.seller-link {
  color: var(--color-green) !important;
  font-weight: 600;
}

.review-link {
  color: var(--color-orange) !important;
  font-weight: 600;
}

.bigdata-link {
  color: #a855f7 !important;
  font-weight: 600;
}

.test-btn {
  position: fixed;
  left: 16px;
  bottom: 16px;
  z-index: 999;
  padding: 8px 18px;
  font-size: 12px;
  color: var(--text-tertiary);
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: 20px;
  text-decoration: none;
  backdrop-filter: blur(10px);
  transition: all var(--transition-base);
  font-weight: 500;
}

.test-btn:hover {
  background: var(--bg-card-hover);
  color: var(--text-primary);
  border-color: var(--border-default);
  box-shadow: 0 0 12px rgba(0,212,255,0.2);
}

@media (max-width: 768px) {
  .header-inner {
    height: 56px;
    gap: 12px;
    padding: 0 16px;
  }

  .logo-icon {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }

  .logo-text {
    font-size: 18px;
  }

  .logo-sub {
    display: none;
  }

  .search-wrap {
    max-width: none;
  }

  .hot-words {
    display: none;
  }

  .header-cart span {
    display: none;
  }

  .footer-top {
    flex-direction: column;
    gap: 24px;
  }

  .footer-links {
    flex-wrap: wrap;
    gap: 24px;
  }

  .footer-col {
    min-width: 40%;
  }
}
</style>
