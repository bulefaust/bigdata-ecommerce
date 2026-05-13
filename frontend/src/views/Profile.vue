<template>
  <div class="profile">
    <h2>个人中心</h2>
    <el-empty v-if="!userStore.userId" description="请先登录" />
    <div v-else class="profile-content">
      <div class="user-card">
        <div class="user-avatar">{{ userStore.username.charAt(0).toUpperCase() }}</div>
        <div class="user-info">
          <h3>{{ userStore.username }}</h3>
          <span class="role-badge" :class="userStore.role">{{ roleText }}</span>
        </div>
      </div>

      <div class="section">
        <div class="section-head">
          <h3>我的收藏 <span class="count">{{ favorites.length }}</span></h3>
        </div>
        <div v-if="!favorites.length" class="empty-section">
          <p>暂无收藏商品</p>
          <router-link to="/" class="link-btn">去逛逛</router-link>
        </div>
        <div v-else class="fav-grid">
          <div v-for="fav in favorites" :key="fav.id" class="fav-card">
            <router-link :to="`/product/${fav.productId}`" class="fav-link">
              <div class="fav-img">
                <img v-if="fav.imageUrl" :src="fav.imageUrl" :alt="fav.name" />
                <div v-else class="img-ph">无图</div>
              </div>
              <div class="fav-info">
                <p class="fav-name">{{ fav.name }}</p>
                <span class="fav-price">¥{{ fav.price?.toLocaleString() }}</span>
              </div>
            </router-link>
            <button class="btn-unfav" @click="unfav(fav)">取消收藏</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import { getFavoriteList, removeFavorite } from '../api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const favorites = ref([])

const roleText = computed(() => {
  const map = { admin: '管理员', seller: '商家', user: '普通用户' }
  return map[userStore.role] || '普通用户'
})

async function loadFavorites() {
  if (!userStore.userId) return
  try {
    favorites.value = await getFavoriteList(userStore.userId)
  } catch { favorites.value = [] }
}

async function unfav(fav) {
  try {
    await removeFavorite(fav.id)
    favorites.value = favorites.value.filter(f => f.id !== fav.id)
    ElMessage.success('已取消收藏')
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => loadFavorites())
</script>

<style scoped>
.profile {
  background: var(--bg-body);
  min-height: 100vh;
  padding: 32px 24px 60px;
  font-family: var(--font-body);
  max-width: 1200px;
  margin: 0 auto;
}

.profile h2 {
  font-size: 24px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 32px;
  letter-spacing: 0.02em;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: 28px 32px;
  margin-bottom: 32px;
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.user-info h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.role-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 12px;
  border-radius: 10px;
  letter-spacing: 0.5px;
}

.role-badge.admin {
  background: rgba(124,58,237,0.12);
  color: #a78bfa;
  border: 1px solid rgba(124,58,237,0.3);
}

.role-badge.seller {
  background: rgba(16,185,129,0.1);
  color: #34d399;
  border: 1px solid rgba(16,185,129,0.3);
}

.role-badge.user {
  background: rgba(0,212,255,0.08);
  color: var(--color-primary-light);
  border: 1px solid rgba(0,212,255,0.2);
}

.section {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: 24px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-head h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.count {
  font-size: 13px;
  color: var(--text-tertiary);
  font-weight: 400;
  margin-left: 6px;
}

.empty-section {
  text-align: center;
  padding: 48px 20px;
}

.empty-section p {
  color: var(--text-tertiary);
  font-size: 14px;
  margin-bottom: 16px;
}

.link-btn {
  display: inline-block;
  padding: 8px 24px;
  background: var(--gradient-primary);
  color: #fff;
  border-radius: var(--radius-xl);
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: all var(--transition-base);
}

.link-btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-glow);
}

.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.fav-card {
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all var(--transition-base);
  position: relative;
}

.fav-card:hover {
  border-color: var(--border-default);
  box-shadow: 0 0 20px rgba(0,212,255,0.06);
}

.fav-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

.fav-img {
  width: 100%;
  height: 140px;
  overflow: hidden;
  background: var(--bg-surface);
}

.fav-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-base);
}

.fav-card:hover .fav-img img {
  transform: scale(1.05);
}

.img-ph {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--text-tertiary);
}

.fav-info {
  padding: 12px 14px;
}

.fav-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fav-price {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-orange);
}

.btn-unfav {
  display: block;
  width: 100%;
  padding: 8px;
  background: rgba(239,68,68,0.04);
  border: none;
  border-top: 1px solid var(--border-subtle);
  color: var(--text-tertiary);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: var(--font-body);
}

.btn-unfav:hover {
  background: rgba(239,68,68,0.1);
  color: var(--color-red);
}
</style>
