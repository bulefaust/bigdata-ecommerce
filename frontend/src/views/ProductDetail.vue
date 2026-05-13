<template>
  <div class="detail-page" v-if="product">
    <div class="container">
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span>&gt;</span>
        <a href="javascript:;" @click="$router.push('/')">{{ product.category }}</a>
        <span>&gt;</span>
        <span class="current">{{ product.name }}</span>
      </div>

      <div class="product-main">
        <div class="product-gallery">
          <div class="main-img">
            <img :src="product.imageUrl" :alt="product.name" v-if="product.imageUrl" />
          </div>
        </div>

        <div class="product-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <p class="product-desc">{{ product.description }}</p>

          <div class="price-box">
            <div class="price-row">
              <span class="price-label">促销价</span>
              <span class="price-value">¥{{ product.price.toLocaleString() }}</span>
            </div>
            <div class="promo-row">
              <span class="promo-label">优惠</span>
              <span class="promo-tag">满减</span>
              <span class="promo-text">满2000减100</span>
              <span class="promo-tag">免运费</span>
            </div>
          </div>

          <div class="info-row">
            <span class="info-label">配送</span>
            <span class="info-value">
              <span class="icon-location">📍</span>
              全国包邮 · 预计3-5个工作日送达
            </span>
          </div>

          <div class="info-row">
            <span class="info-label">服务</span>
            <span class="info-value">
              <span class="service-tag">7天无理由</span>
              <span class="service-tag">运费险</span>
              <span class="service-tag">正品保障</span>
            </span>
          </div>

          <div class="info-row">
            <span class="info-label">库存</span>
            <span class="info-value" :class="{ 'stock-low': product.stock < 50 }">
              {{ product.stock }}件
              <span class="stock-warn" v-if="product.stock < 50">· 库存紧张，欲购从速！</span>
            </span>
          </div>

          <div class="info-row quantity-row">
            <span class="info-label">数量</span>
            <div class="quantity-box">
              <button class="qty-btn" @click="quantity > 1 && quantity--">−</button>
              <input class="qty-input" v-model.number="quantity" type="number" min="1" :max="product.stock" />
              <button class="qty-btn" @click="quantity < product.stock && quantity++">+</button>
            </div>
            <span class="qty-total">小计：<strong>¥{{ (product.price * quantity).toLocaleString() }}</strong></span>
          </div>

          <div class="action-row">
            <button class="btn-buy" @click="buyNow">立即购买</button>
            <button class="btn-cart" @click="addToCart">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="8" cy="21" r="1"/><circle cx="19" cy="21" r="1"/><path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"/></svg>
              加入购物车
            </button>
            <button class="btn-fav" @click="toggleFav">
              {{ isFav ? '❤️ 已收藏' : '🤍 收藏' }}
            </button>
          </div>
        </div>
      </div>

      <div class="detail-sections">
        <div class="detail-tabs">
          <div class="tab" :class="{ active: activeTab === 'detail' }" @click="activeTab = 'detail'">商品详情</div>
          <div class="tab" :class="{ active: activeTab === 'specs' }" @click="activeTab = 'specs'">规格参数</div>
          <div class="tab" :class="{ active: activeTab === 'reviews' }" @click="activeTab = 'reviews'">用户评价（{{ reviews.length }}）</div>
        </div>

        <div class="detail-content" v-if="activeTab === 'detail'">
          <div class="spec-table">
            <div class="spec-row" v-for="spec in specs.slice(0, 5)" :key="spec.id">
              <span class="spec-label">{{ spec.specName }}</span>
              <span class="spec-value">{{ spec.specValue }}</span>
            </div>
          </div>
        </div>

        <div class="detail-content" v-if="activeTab === 'specs'">
          <div class="spec-table" v-if="specs.length">
            <div class="spec-row" v-for="spec in specs" :key="spec.id">
              <span class="spec-label">{{ spec.specName }}</span>
              <span class="spec-value">{{ spec.specValue }}</span>
            </div>
          </div>
          <div class="empty-tab" v-else>暂无规格参数</div>
        </div>

        <div class="detail-content" v-if="activeTab === 'reviews'">
          <div class="review-summary" v-if="reviews.length">
            <div class="review-score">
              <span class="score-num">{{ avgRating }}</span>
              <span class="score-label">综合评分</span>
            </div>
            <div class="review-bars">
              <div class="bar-row" v-for="s in 5" :key="s">
                <span class="bar-label">{{ 6 - s }}星</span>
                <div class="bar-track"><div class="bar-fill" :style="{ width: ratingPercent(6 - s) + '%' }"></div></div>
                <span class="bar-pct">{{ ratingPercent(6 - s) }}%</span>
              </div>
            </div>
          </div>

          <div class="review-form" v-if="userStore.userId && !isSellerOfProduct">
            <h4 class="form-title">发表评价</h4>
            <div class="form-rating">
              <span class="form-label">评分</span>
              <div class="star-select">
                <span v-for="i in 5" :key="i" class="star-btn" :class="{ active: newReview.rating >= i }" @click="newReview.rating = i">★</span>
              </div>
              <span class="rating-text">{{ newReview.rating }}分</span>
            </div>
            <div class="form-content">
              <textarea v-model="newReview.content" placeholder="分享您的使用体验..." rows="3"></textarea>
            </div>
            <button class="btn-submit-review" @click="submitReview" :disabled="submittingReview">
              {{ submittingReview ? '提交中...' : '提交评价' }}
            </button>
          </div>
          <div class="review-seller-tip" v-else-if="isSellerOfProduct">
            您是本商品的商家，不能评价自己的商品
          </div>
          <div class="review-login-tip" v-else-if="!userStore.userId">
            <router-link to="/login">登录</router-link> 后即可发表评价
          </div>

          <div class="review-list" v-if="reviews.length">
            <div class="review-item" v-for="review in reviews" :key="review.id">
              <div class="review-head">
                <span class="review-user">{{ review.username }}</span>
                <span class="role-badge admin-badge" v-if="review.role === 'admin'">管理员</span>
                <span class="role-badge seller-badge" v-else-if="review.role === 'seller'">商家</span>
                <span class="review-stars">
                  <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= review.rating }">★</span>
                </span>
                <span class="review-time">{{ review.createTime }}</span>
              </div>
              <p class="review-content">{{ review.content }}</p>
            </div>
          </div>
          <div class="empty-tab" v-else>暂无用户评价</div>
        </div>
      </div>

      <div class="similar-section">
        <h3>看了又看</h3>
        <div class="similar-grid">
          <div
            v-for="item in similarProducts"
            :key="item.id"
            class="similar-card"
            @click="goDetail(item.id)"
          >
            <div class="similar-img">
              <img :src="item.imageUrl" :alt="item.name" v-if="item.imageUrl" loading="lazy" />
            </div>
            <div class="similar-info">
              <h4>{{ item.name }}</h4>
              <span class="similar-price">¥{{ item.price.toLocaleString() }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductById, createOrder, getSimilarProducts, getProductSpecs, getProductReviews, submitReview as submitReviewApi, toggleFavorite, checkFavorite } from '../api'
import { startBrowseTimer, endBrowseTimer, trackCart, trackPurchase } from '../tracker'
import { useUserStore } from '../store/user'
import { useCartStore } from '../store/cart'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const product = ref(null)
const quantity = ref(1)
const similarProducts = ref([])
const specs = ref([])
const reviews = ref([])
const isFav = ref(false)
const activeTab = ref('detail')
const newReview = ref({ rating: 5, content: '' })
const submittingReview = ref(false)

const isSellerOfProduct = computed(() => {
  return product.value && product.value.sellerId && userStore.userId &&
    Number(product.value.sellerId) === Number(userStore.userId)
})

const avgRating = computed(() => {
  if (!reviews.value.length) return '0.0'
  const sum = reviews.value.reduce((a, r) => a + r.rating, 0)
  return (sum / reviews.value.length).toFixed(1)
})

function ratingPercent(star) {
  if (!reviews.value.length) return 0
  const count = reviews.value.filter(r => r.rating === star).length
  return Math.round(count / reviews.value.length * 100)
}

async function loadProduct(id) {
  product.value = await getProductById(id)
  if (product.value) {
    startBrowseTimer(product.value.id, product.value.category)
    loadSimilar(id)
    loadSpecs(id)
    loadReviews(id)
    if (userStore.userId) {
      try {
        const res = await checkFavorite(userStore.userId, id)
        isFav.value = res.favorited
      } catch { isFav.value = false }
    }
  }
}

async function loadSimilar(id) {
  try { similarProducts.value = await getSimilarProducts(id, 6) } catch { similarProducts.value = [] }
}

async function loadSpecs(id) {
  try { specs.value = await getProductSpecs(id) } catch { specs.value = [] }
}

async function loadReviews(id) {
  try { reviews.value = await getProductReviews(id) } catch { reviews.value = [] }
}

onMounted(() => loadProduct(route.params.id))
onUnmounted(() => endBrowseTimer())

watch(() => route.params.id, (newId) => {
  if (newId) {
    endBrowseTimer()
    quantity.value = 1
    loadProduct(newId)
  }
})

function addToCart() {
  if (!userStore.userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  cartStore.addItem({ ...product.value, quantity: quantity.value })
  trackCart(product.value.id, product.value.category)
  ElMessage.success('已加入购物车')
}

async function buyNow() {
  if (!userStore.userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await createOrder(userStore.userId, product.value.id, quantity.value)
    trackPurchase(product.value.id, product.value.category)
    ElMessage.success('下单成功！')
    router.push('/order')
  } catch {
    ElMessage.error('下单失败，请重试')
  }
}

async function submitReview() {
  if (!newReview.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  submittingReview.value = true
  try {
    const result = await submitReviewApi(product.value.id, {
      userId: userStore.userId,
      rating: newReview.value.rating,
      content: newReview.value.content.trim()
    })
    result.role = userStore.role
    reviews.value.unshift(result)
    newReview.value = { rating: 5, content: '' }
    ElMessage.success('评价发表成功')
  } catch {
    ElMessage.error('评价发表失败')
  } finally {
    submittingReview.value = false
  }
}

async function toggleFav() {
  if (!userStore.userId) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await toggleFavorite(userStore.userId, product.value.id)
    isFav.value = res.favorited
    ElMessage.success(isFav.value ? '已收藏' : '已取消收藏')
  } catch { ElMessage.error('操作失败') }
}

function goDetail(id) {
  router.push(`/product/${id}`)
}
</script>

<style scoped>
.detail-page {
  padding: 24px 0 60px;
  background: var(--bg-body);
  min-height: 100vh;
  font-family: var(--font-body);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.breadcrumb {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.breadcrumb a {
  color: var(--color-primary-light);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.breadcrumb a:hover {
  color: var(--color-primary);
}

.breadcrumb span {
  color: var(--border-default);
  font-size: 12px;
}

.current {
  color: var(--text-secondary);
}

.product-main {
  display: flex;
  gap: 40px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: 36px;
  margin-bottom: 24px;
  transition: all var(--transition-base);
}

.product-gallery {
  width: 440px;
  flex-shrink: 0;
}

.main-img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
}

.main-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.main-img:hover img {
  transform: scale(1.03);
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: 10px;
}

.product-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 24px;
  line-height: 1.7;
}

.price-box {
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: 20px 24px;
  margin-bottom: 24px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 12px;
}

.price-label {
  font-size: 12px;
  color: var(--text-tertiary);
}

.price-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
}

.promo-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.promo-label {
  font-size: 12px;
  color: var(--text-tertiary);
}

.promo-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 500;
  color: var(--color-orange);
  border: 1px solid rgba(249, 115, 22, 0.25);
  padding: 2px 10px;
  border-radius: 20px;
  background: rgba(249, 115, 22, 0.08);
}

.promo-text {
  font-size: 12px;
  color: var(--color-orange);
}

.info-row {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-subtle);
}

.info-label {
  font-size: 12px;
  color: var(--text-tertiary);
  min-width: 40px;
  flex-shrink: 0;
}

.info-value {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.stock-low {
  color: var(--color-red);
}

.stock-warn {
  font-size: 11px;
  color: var(--color-red);
  font-weight: 500;
}

.service-tag {
  display: inline-block;
  font-size: 11px;
  color: var(--color-green);
  border: 1px solid rgba(16, 185, 129, 0.25);
  padding: 2px 10px;
  border-radius: 20px;
  background: rgba(16, 185, 129, 0.08);
}

.quantity-row {
  padding: 16px 0;
}

.quantity-box {
  display: flex;
  align-items: center;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-elevated);
}

.qty-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  font-size: 16px;
  cursor: pointer;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.qty-btn:hover {
  background: rgba(0, 212, 255, 0.12);
  color: var(--color-primary-light);
}

.qty-input {
  width: 48px;
  height: 36px;
  border: none;
  border-left: 1px solid var(--border-subtle);
  border-right: 1px solid var(--border-subtle);
  text-align: center;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-primary-light);
  outline: none;
  background: var(--bg-elevated);
  -moz-appearance: textfield;
}

.qty-input::-webkit-inner-spin-button,
.qty-input::-webkit-outer-spin-button {
  -webkit-appearance: none;
}

.qty-total {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-left: 20px;
}

.qty-total strong {
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 700;
}

.action-row {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  align-items: center;
}

.btn-buy {
  padding: 13px 52px;
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-xl);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-base);
  letter-spacing: 0.02em;
}

.btn-buy:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow);
}

.btn-buy:active {
  transform: translateY(0);
  box-shadow: none;
}

.btn-cart {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 13px 32px;
  background: transparent;
  color: var(--color-primary-light);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-xl);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-cart:hover {
  border-color: var(--color-primary);
  background: rgba(0, 212, 255, 0.08);
  transform: translateY(-2px);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.1);
}

.btn-cart:active {
  transform: translateY(0);
}

.btn-fav {
  padding: 13px 20px;
  background: transparent;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  font-size: 13px;
  cursor: pointer;
  color: var(--text-tertiary);
  transition: all var(--transition-base);
}

.btn-fav:hover {
  border-color: var(--color-red);
  color: var(--color-red);
  background: rgba(239, 68, 68, 0.08);
}

.detail-sections {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  margin-bottom: 24px;
}

.detail-tabs {
  display: flex;
  border-bottom: 1px solid var(--border-subtle);
  position: relative;
}

.tab {
  padding: 16px 32px;
  font-size: 14px;
  color: var(--text-tertiary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: all var(--transition-fast);
  position: relative;
}

.tab::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--gradient-primary);
  transition: all var(--transition-slow);
  transform: translateX(-50%);
  border-radius: 1px;
}

.tab:hover {
  color: var(--color-primary-light);
}

.tab:hover::after {
  width: 60%;
}

.tab.active {
  color: var(--color-primary-light);
  font-weight: 700;
}

.tab.active::after {
  width: 100%;
  background: var(--gradient-primary);
}

.detail-content {
  padding: 32px;
}

.spec-table {
  width: 100%;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-subtle);
}

.spec-row {
  display: flex;
  padding: 14px 20px;
  transition: background var(--transition-fast);
}

.spec-row:nth-child(odd) {
  background: var(--bg-surface);
}

.spec-row:nth-child(even) {
  background: var(--bg-card);
}

.spec-row:last-child {
  border-bottom: none;
}

.spec-label {
  width: 140px;
  flex-shrink: 0;
  font-size: 13px;
  color: var(--text-tertiary);
  font-weight: 500;
}

.spec-value {
  font-size: 13px;
  color: var(--text-primary);
}

.empty-tab {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-tertiary);
  font-size: 14px;
}

.review-summary {
  display: flex;
  gap: 40px;
  padding: 28px 32px;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  margin-bottom: 28px;
}

.review-score {
  text-align: center;
  min-width: 110px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-num {
  display: block;
  font-size: 52px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
}

.score-label {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 6px;
}

.review-bars {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.bar-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bar-label {
  font-size: 12px;
  color: var(--text-tertiary);
  width: 28px;
}

.bar-track {
  flex: 1;
  height: 6px;
  background: rgba(0, 212, 255, 0.08);
  border-radius: 3px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: var(--gradient-primary);
  border-radius: 3px;
  transition: width var(--transition-slow);
}

.bar-pct {
  font-size: 12px;
  color: var(--text-tertiary);
  width: 36px;
  text-align: right;
}

.review-list {
  display: flex;
  flex-direction: column;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid var(--border-subtle);
}

.review-item:last-child {
  border-bottom: none;
}

.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.review-user {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.review-stars {
  display: flex;
  gap: 1px;
}

.star {
  font-size: 14px;
  color: var(--border-default);
}

.star.filled {
  color: var(--color-orange);
}

.review-time {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-left: auto;
}

.review-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.role-badge {
  display: inline-block;
  font-size: 10px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: 10px;
  letter-spacing: 0.5px;
}

.admin-badge {
  background: rgba(124, 58, 237, 0.15);
  color: #a78bfa;
  border: 1px solid rgba(124, 58, 237, 0.3);
}

.seller-badge {
  background: rgba(16, 185, 129, 0.12);
  color: #34d399;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.review-form {
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 28px;
}

.form-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px;
}

.form-rating {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.form-label {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: 500;
}

.star-select {
  display: flex;
  gap: 4px;
}

.star-btn {
  font-size: 22px;
  color: var(--border-default);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.star-btn:hover,
.star-btn.active {
  color: var(--color-orange);
  transform: scale(1.15);
}

.rating-text {
  font-size: 13px;
  color: var(--color-orange);
  font-weight: 600;
}

.form-content textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--text-primary);
  background: var(--bg-surface);
  outline: none;
  resize: vertical;
  font-family: var(--font-body);
  line-height: 1.6;
  transition: border-color var(--transition-fast);
  box-sizing: border-box;
}

.form-content textarea:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
}

.form-content textarea::placeholder {
  color: var(--text-tertiary);
}

.btn-submit-review {
  margin-top: 14px;
  padding: 10px 28px;
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-xl);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-submit-review:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-glow);
}

.btn-submit-review:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.review-login-tip {
  text-align: center;
  padding: 20px;
  margin-bottom: 20px;
  font-size: 13px;
  color: var(--text-tertiary);
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}

.review-login-tip a {
  color: var(--color-primary-light);
  font-weight: 600;
  text-decoration: none;
}

.review-login-tip a:hover {
  color: var(--color-primary);
}

.review-seller-tip {
  text-align: center;
  padding: 20px;
  margin-bottom: 20px;
  font-size: 13px;
  color: var(--color-orange);
  background: rgba(249, 115, 22, 0.06);
  border: 1px solid rgba(249, 115, 22, 0.2);
  border-radius: var(--radius-lg);
}

.similar-section {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: 28px 32px;
}

.similar-section h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 20px;
}

.similar-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.similar-card {
  cursor: pointer;
  transition: all var(--transition-base);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-subtle);
  background: var(--bg-card);
}

.similar-card:hover {
  transform: translateY(-4px);
  background: var(--bg-card-hover);
  border-color: var(--border-default);
  box-shadow: 0 0 30px rgba(0, 212, 255, 0.08);
}

.similar-img {
  height: 130px;
  background: var(--bg-elevated);
  overflow: hidden;
}

.similar-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.similar-card:hover .similar-img img {
  transform: scale(1.05);
}

.similar-info {
  padding: 10px 12px 12px;
}

.similar-info h4 {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}

.similar-price {
  font-size: 15px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

@media (max-width: 900px) {
  .product-main {
    flex-direction: column;
    padding: 20px;
    gap: 24px;
  }
  .product-gallery {
    width: 100%;
  }
  .similar-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .action-row {
    flex-wrap: wrap;
  }
  .btn-buy {
    width: 100%;
    text-align: center;
  }
  .btn-cart {
    flex: 1;
    justify-content: center;
  }
}
</style>
