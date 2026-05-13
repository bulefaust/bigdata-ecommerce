<template>
  <div class="review-page">
    <header class="review-header">
      <router-link to="/" class="back-link">← 返回商城</router-link>
      <div class="review-title-area">
        <h1>商品审核中心</h1>
        <div class="review-badge"><span class="review-dot"></span>ADMIN</div>
      </div>
      <div class="review-user">
        <span>{{ userStore.username }}</span>
        <span class="role-tag">管理员</span>
      </div>
    </header>

    <div class="review-body">
      <div class="review-stats">
        <div class="mini-stat">
          <span class="mini-num">{{ pendingProducts.length }}</span>
          <span class="mini-label">待审核</span>
        </div>
        <div class="mini-stat">
          <span class="mini-num">{{ approvedCount }}</span>
          <span class="mini-label">已通过</span>
        </div>
        <div class="mini-stat">
          <span class="mini-num">{{ rejectedCount }}</span>
          <span class="mini-label">已驳回</span>
        </div>
      </div>

      <div class="review-content">
        <div class="content-head">
          <h2>待审核商品</h2>
          <button class="btn-refresh" @click="loadPending">🔄 刷新</button>
        </div>

        <div v-if="!pendingProducts.length" class="empty-state">
          <div class="empty-icon">✅</div>
          <p>暂无待审核商品</p>
        </div>

        <div v-else class="product-cards">
          <div v-for="p in pendingProducts" :key="p.id" class="product-card">
            <div class="card-img">
              <img v-if="p.imageUrl" :src="p.imageUrl" :alt="p.name" />
              <div v-else class="img-placeholder">无图片</div>
            </div>
            <div class="card-info">
              <h3>{{ p.name }}</h3>
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">分类</span>
                  <span class="info-value">{{ p.category }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">价格</span>
                  <span class="info-value price">¥{{ p.price?.toLocaleString() }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">库存</span>
                  <span class="info-value">{{ p.stock }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">商家ID</span>
                  <span class="info-value">{{ p.sellerId }}</span>
                </div>
              </div>
              <p class="card-desc" v-if="p.description">{{ p.description }}</p>
            </div>
            <div class="card-actions">
              <button class="btn-approve" @click="approveProduct(p, 1)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                通过
              </button>
              <button class="btn-reject" @click="approveProduct(p, 0)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                驳回
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const userStore = useUserStore()
const pendingProducts = ref([])
const approvedCount = ref(0)
const rejectedCount = ref(0)

async function loadPending() {
  try {
    pendingProducts.value = await request.get('/seller/pending-products', {
      params: { userId: userStore.userId }
    })
  } catch { pendingProducts.value = [] }
}

async function approveProduct(product, status) {
  try {
    await request.put(`/seller/product/${product.id}/approve`, null, {
      params: { userId: userStore.userId, status }
    })
    pendingProducts.value = pendingProducts.value.filter(p => p.id !== product.id)
    if (status === 1) {
      approvedCount.value++
      ElMessage.success(`「${product.name}」已通过审核`)
    } else {
      rejectedCount.value++
      ElMessage.success(`「${product.name}」已驳回`)
    }
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => loadPending())
</script>

<style scoped>
.review-page {
  min-height: 100vh;
  background: var(--bg-body);
  color: var(--text-primary);
  font-family: var(--font-body);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 28px;
  background: var(--bg-surface);
  border-bottom: 1px solid var(--border-default);
  box-shadow: 0 2px 16px rgba(0,212,255,0.06);
}

.review-title-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.review-header h1 {
  font-size: 20px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2px;
}

.review-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  background: rgba(124,58,237,0.12);
  border: 1px solid rgba(124,58,237,0.3);
  border-radius: 12px;
  padding: 2px 10px;
  font-size: 11px;
  font-weight: 700;
  color: #a78bfa;
  letter-spacing: 1px;
}

.review-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #a78bfa;
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.back-link {
  color: var(--color-primary);
  text-decoration: none;
  font-size: 13px;
  padding: 5px 14px;
  border: 1px solid rgba(0,212,255,0.3);
  border-radius: 6px;
  transition: all 0.3s;
}

.back-link:hover {
  background: rgba(0,212,255,0.08);
  border-color: var(--color-primary);
}

.review-user {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--text-secondary);
}

.role-tag {
  background: var(--gradient-primary);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 8px;
}

.review-body {
  max-width: 1000px;
  margin: 0 auto;
  padding: 28px 24px;
}

.review-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
}

.mini-stat {
  flex: 1;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 20px;
  text-align: center;
}

.mini-num {
  display: block;
  font-size: 32px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.mini-stat:nth-child(1) .mini-num { color: var(--color-orange); }
.mini-stat:nth-child(2) .mini-num { color: var(--color-green); }
.mini-stat:nth-child(3) .mini-num { color: var(--color-red); }

.mini-label {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.content-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.content-head h2 {
  font-size: 18px;
  font-weight: 600;
}

.btn-refresh {
  padding: 6px 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: var(--font-body);
}

.btn-refresh:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: rgba(0,212,255,0.06);
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  color: var(--text-tertiary);
  font-size: 15px;
}

.product-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-card {
  display: flex;
  gap: 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: 20px;
  transition: all var(--transition-base);
}

.product-card:hover {
  border-color: var(--border-default);
  box-shadow: 0 0 24px rgba(0,212,255,0.06);
}

.card-img {
  width: 140px;
  height: 140px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  flex-shrink: 0;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--text-tertiary);
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  gap: 8px;
  font-size: 13px;
}

.info-label {
  color: var(--text-tertiary);
  min-width: 40px;
}

.info-value {
  color: var(--text-secondary);
}

.info-value.price {
  color: var(--color-orange);
  font-weight: 600;
}

.card-desc {
  font-size: 13px;
  color: var(--text-tertiary);
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
  flex-shrink: 0;
}

.btn-approve, .btn-reject {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 24px;
  border: none;
  border-radius: var(--radius-xl);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-base);
  font-family: var(--font-body);
  min-width: 110px;
}

.btn-approve {
  background: rgba(16,185,129,0.12);
  color: var(--color-green);
  border: 1px solid rgba(16,185,129,0.3);
}

.btn-approve:hover {
  background: rgba(16,185,129,0.2);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(16,185,129,0.15);
}

.btn-reject {
  background: rgba(239,68,68,0.08);
  color: var(--color-red);
  border: 1px solid rgba(239,68,68,0.2);
}

.btn-reject:hover {
  background: rgba(239,68,68,0.14);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(239,68,68,0.12);
}

@media (max-width: 768px) {
  .product-card {
    flex-direction: column;
  }
  .card-img {
    width: 100%;
    height: 180px;
  }
  .card-actions {
    flex-direction: row;
  }
}
</style>
