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
        </div>
      </div>
    </header>

    <header class="main-header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-text">DataMall</span>
          <span class="logo-sub">智能电商</span>
        </div>

        <div class="search-wrap">
          <div class="search-box">
            <input
              v-model="searchKeyword"
              placeholder="搜索商品..."
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>
          <div class="hot-words">
            <span v-for="w in hotWords" :key="w" @click="quickSearch(w)">{{ w }}</span>
          </div>
        </div>

        <div class="header-cart" @click="$router.push('/cart')">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="8" cy="21" r="1"/><circle cx="19" cy="21" r="1"/><path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"/></svg>
          <span>购物车</span>
          <em v-if="cartStore.totalCount > 0">{{ cartStore.totalCount }}</em>
        </div>
      </div>
    </header>

    <nav class="category-nav" v-if="route.path === '/'">
      <div class="nav-inner">
        <div
          v-for="cat in categories"
          :key="cat"
          class="nav-item"
          :class="{ active: selectedCat === cat }"
          @click="selectCat(cat)"
        >{{ cat }}</div>
      </div>
    </nav>

    <main class="main-content">
      <router-view v-slot="{ Component }">
        <component :is="Component" :selectedCategory="selectedCat" @update:selectedCategory="selectedCat = $event" />
      </router-view>
    </main>

    <footer class="site-footer">
      <div class="footer-inner">
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
import { ref, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { useCartStore } from '../store/cart'
import { trackSearch } from '../tracker'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const selectedCat = ref('全部')
const hotWords = ['iPhone', '华为', '降噪耳机', 'MacBook', 'PS5', '无人机']

const categories = ['全部', '手机', '电脑', '耳机', '平板', '穿戴', '游戏', '摄影', '配件']

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

function selectCat(cat) {
  selectedCat.value = cat
  if (route.path !== '/') {
    router.push('/')
  }
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

provide('selectedCat', selectedCat)
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-body);
}

.top-bar {
  background: var(--bg-gray);
  border-bottom: 1px solid var(--border-light);
  font-size: 12px;
  color: var(--text-muted);
}

.top-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.top-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.top-right a {
  color: var(--text-secondary);
  font-size: 12px;
  text-decoration: none;
  transition: color 0.2s;
}

.top-right a:hover {
  color: var(--color-primary);
}

.user-greeting {
  color: var(--text-primary);
  font-weight: 500;
}

.main-header {
  background: var(--bg-white);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 80px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 40px;
}

.logo {
  cursor: pointer;
  display: flex;
  align-items: baseline;
  gap: 6px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 28px;
  font-weight: 900;
  color: var(--color-primary);
  letter-spacing: -1px;
}

.logo-sub {
  font-size: 12px;
  color: var(--text-muted);
}

.search-wrap {
  flex: 1;
  max-width: 560px;
}

.search-box {
  display: flex;
  border: 2px solid var(--color-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  height: 40px;
}

.search-input {
  flex: 1;
  border: none;
  padding: 0 16px;
  font-size: 14px;
  outline: none;
  font-family: inherit;
  color: var(--text-primary);
}

.search-input::placeholder {
  color: var(--text-placeholder);
}

.search-btn {
  width: 80px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.2s;
}

.search-btn:hover {
  background: var(--color-primary-dark);
}

.hot-words {
  display: flex;
  gap: 12px;
  margin-top: 6px;
}

.hot-words span {
  font-size: 12px;
  color: var(--text-muted);
  cursor: pointer;
  transition: color 0.2s;
}

.hot-words span:hover {
  color: var(--color-primary);
}

.header-cart {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
  background: var(--bg-white);
  flex-shrink: 0;
}

.header-cart:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.header-cart em {
  position: absolute;
  top: -6px;
  right: -6px;
  background: var(--color-primary);
  color: #fff;
  font-size: 10px;
  font-style: normal;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  padding: 0 4px;
}

.category-nav {
  background: var(--bg-white);
  border-bottom: 1px solid var(--border-light);
}

.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  padding: 0 16px;
  gap: 0;
}

.nav-item {
  padding: 12px 20px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  white-space: nowrap;
}

.nav-item:hover {
  color: var(--color-primary);
}

.nav-item.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  font-weight: 600;
}

.main-content {
  flex: 1;
}

.site-footer {
  background: var(--bg-white);
  border-top: 1px solid var(--border-light);
  margin-top: 40px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 16px 16px;
}

.footer-links {
  display: flex;
  justify-content: space-around;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-light);
}

.footer-col h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.footer-col a {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  text-decoration: none;
  margin-bottom: 8px;
  transition: color 0.2s;
}

.footer-col a:hover {
  color: var(--color-primary);
}

.footer-bottom {
  text-align: center;
  padding-top: 16px;
}

.footer-bottom p {
  font-size: 12px;
  color: var(--text-muted);
}

.test-btn {
  position: fixed;
  left: 12px;
  bottom: 12px;
  z-index: 999;
  padding: 8px 18px;
  font-size: 14px;
  color: var(--text-muted);
  background: var(--bg-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  text-decoration: none;
  opacity: 0.5;
  transition: opacity 0.2s;
}

.test-btn:hover {
  opacity: 1;
  color: var(--color-primary);
  border-color: var(--color-primary);
}
</style>
