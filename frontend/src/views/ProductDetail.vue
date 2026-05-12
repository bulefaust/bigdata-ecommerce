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
          <div class="review-list" v-if="reviews.length">
            <div class="review-item" v-for="review in reviews" :key="review.id">
              <div class="review-head">
                <span class="review-user">{{ review.username }}</span>
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
import { getProductById, createOrder, getSimilarProducts, getProductSpecs, getProductReviews } from '../api'
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

function toggleFav() {
  isFav.value = !isFav.value
  ElMessage.success(isFav.value ? '已收藏' : '已取消收藏')
}

function goDetail(id) {
  router.push(`/product/${id}`)
}
</script>

<style scoped>
.detail-page {
  padding: 16px 0 40px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.breadcrumb {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.breadcrumb a {
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.2s;
}

.breadcrumb a:hover {
  color: var(--color-primary);
}

.current {
  color: var(--text-primary);
}

.product-main {
  display: flex;
  gap: 32px;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-sm);
  margin-bottom: 20px;
}

.product-gallery {
  width: 420px;
  flex-shrink: 0;
}

.main-img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-gray);
  border: 1px solid var(--border-light);
}

.main-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.main-img:hover img {
  transform: scale(1.05);
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
  margin-bottom: 8px;
}

.product-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 20px;
  line-height: 1.6;
}

.price-box {
  background: var(--color-primary-light);
  border-radius: var(--radius-md);
  padding: 16px 20px;
  margin-bottom: 20px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 10px;
}

.price-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.price-value {
  font-size: 32px;
  font-weight: 900;
  color: var(--color-primary);
}

.promo-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.promo-label {
  font-size: 12px;
  color: var(--text-muted);
}

.promo-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  color: var(--color-primary);
  border: 1px solid var(--color-primary);
  padding: 1px 6px;
  border-radius: 2px;
  background: #fff;
}

.promo-text {
  font-size: 12px;
  color: var(--color-primary);
}

.info-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-light);
}

.info-label {
  font-size: 13px;
  color: var(--text-muted);
  min-width: 40px;
  flex-shrink: 0;
}

.info-value {
  font-size: 13px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.stock-low {
  color: var(--color-primary);
}

.stock-warn {
  font-size: 12px;
  color: var(--color-primary);
  font-weight: 600;
}

.service-tag {
  display: inline-block;
  font-size: 11px;
  color: var(--color-green);
  border: 1px solid var(--color-green);
  padding: 1px 6px;
  border-radius: 2px;
  background: var(--color-green-light);
}

.quantity-row {
  padding: 14px 0;
}

.quantity-box {
  display: flex;
  align-items: center;
  border: 1px solid var(--border-medium);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.qty-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--bg-gray);
  font-size: 16px;
  cursor: pointer;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.qty-btn:hover {
  background: var(--bg-hover);
}

.qty-input {
  width: 48px;
  height: 32px;
  border: none;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  outline: none;
  font-family: inherit;
  -moz-appearance: textfield;
}

.qty-input::-webkit-inner-spin-button,
.qty-input::-webkit-outer-spin-button {
  -webkit-appearance: none;
}

.qty-total {
  font-size: 13px;
  color: var(--text-secondary);
  margin-left: 16px;
}

.qty-total strong {
  color: var(--color-primary);
  font-size: 18px;
}

.action-row {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn-buy {
  padding: 12px 48px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-lg);
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.2s;
}

.btn-buy:hover {
  background: var(--color-primary-dark);
}

.btn-cart {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 32px;
  background: var(--color-orange-light);
  color: var(--color-orange);
  border: 1px solid var(--color-orange);
  border-radius: var(--radius-lg);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}

.btn-cart:hover {
  background: var(--color-orange);
  color: #fff;
}

.btn-fav {
  padding: 12px 24px;
  background: var(--bg-gray);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  font-size: 14px;
  cursor: pointer;
  font-family: inherit;
  color: var(--text-secondary);
  transition: all 0.2s;
}

.btn-fav:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.detail-sections {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  margin-bottom: 20px;
}

.detail-tabs {
  display: flex;
  border-bottom: 1px solid var(--border-light);
}

.tab {
  padding: 14px 28px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.tab:hover {
  color: var(--color-primary);
}

.tab.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  font-weight: 600;
}

.detail-content {
  padding: 24px;
}

.spec-table {
  width: 100%;
}

.spec-row {
  display: flex;
  border-bottom: 1px solid var(--border-light);
  padding: 10px 0;
}

.spec-row:last-child {
  border-bottom: none;
}

.spec-label {
  width: 120px;
  flex-shrink: 0;
  font-size: 13px;
  color: var(--text-muted);
}

.spec-value {
  font-size: 13px;
  color: var(--text-primary);
}

.empty-tab {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-muted);
  font-size: 14px;
}

.review-summary {
  display: flex;
  gap: 32px;
  padding: 20px;
  background: var(--bg-gray);
  border-radius: var(--radius-md);
  margin-bottom: 20px;
}

.review-score {
  text-align: center;
  min-width: 100px;
}

.score-num {
  display: block;
  font-size: 40px;
  font-weight: 900;
  color: var(--color-primary);
  line-height: 1;
}

.score-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.review-bars {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  justify-content: center;
}

.bar-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bar-label {
  font-size: 12px;
  color: var(--text-muted);
  width: 30px;
}

.bar-track {
  flex: 1;
  height: 8px;
  background: var(--border-light);
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 4px;
  transition: width 0.3s;
}

.bar-pct {
  font-size: 12px;
  color: var(--text-muted);
  width: 36px;
  text-align: right;
}

.review-list {
  display: flex;
  flex-direction: column;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--border-light);
}

.review-item:last-child {
  border-bottom: none;
}

.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.review-user {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.review-stars {
  display: flex;
  gap: 1px;
}

.star {
  font-size: 14px;
  color: var(--border-medium);
}

.star.filled {
  color: var(--color-gold);
}

.review-time {
  font-size: 12px;
  color: var(--text-muted);
  margin-left: auto;
}

.review-content {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.6;
}

.similar-section {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 20px 24px;
}

.similar-section h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.similar-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.similar-card {
  cursor: pointer;
  transition: all 0.2s;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-light);
}

.similar-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary-light);
}

.similar-img {
  height: 120px;
  background: var(--bg-gray);
  overflow: hidden;
}

.similar-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.similar-card:hover .similar-img img {
  transform: scale(1.05);
}

.similar-info {
  padding: 8px 10px 10px;
}

.similar-info h4 {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.similar-price {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-primary);
}

@media (max-width: 900px) {
  .product-main {
    flex-direction: column;
  }
  .product-gallery {
    width: 100%;
  }
  .similar-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
