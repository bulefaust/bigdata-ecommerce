<template>
  <div class="home">
    <div class="banner-section" v-if="!searchMode">
      <canvas ref="particleCanvas" class="particle-canvas"></canvas>
      <div class="container">
        <div class="banner">
          <div class="banner-content">
            <div class="banner-tag">🔥 限时特惠</div>
            <h2>新品首发<br/>低至5折起</h2>
            <p>精选180+款科技好物，数据驱动智能推荐</p>
            <button class="banner-btn" @click="scrollToProducts">立即选购</button>
          </div>
          <div class="banner-stats">
            <div class="stat-item">
              <strong>180+</strong>
              <span>精选商品</span>
            </div>
            <div class="stat-item">
              <strong>8</strong>
              <span>品类覆盖</span>
            </div>
            <div class="stat-item">
              <strong>24h</strong>
              <span>极速发货</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="container" ref="productsRef">
      <div class="section" v-if="searchMode">
        <div class="section-head">
          <h3>搜索 "{{ searchKeyword }}" · 找到 {{ total }} 件商品</h3>
          <button class="btn-text" @click="clearSearch">清除搜索</button>
        </div>
      </div>

      <div class="section" v-if="!searchMode && flashSales.length">
        <div class="section-head">
          <h3>⚡ 限时秒杀</h3>
          <div class="flash-countdown">
            <span class="countdown-label">距结束</span>
            <span class="countdown-box">{{ countdown.h }}</span>
            <span class="countdown-sep">:</span>
            <span class="countdown-box">{{ countdown.m }}</span>
            <span class="countdown-sep">:</span>
            <span class="countdown-box">{{ countdown.s }}</span>
          </div>
        </div>
        <div class="flash-grid">
          <div
            v-for="item in flashSales"
            :key="'f'+item.id"
            class="flash-card"
            @click="goDetail({ id: item.productId })"
          >
            <div class="flash-img">
              <img :src="item.productImage" :alt="item.productName" v-if="item.productImage" loading="lazy" />
              <span class="flash-badge">{{ item.discount }}折</span>
            </div>
            <div class="flash-info">
              <h4>{{ item.productName }}</h4>
              <div class="flash-prices">
                <span class="flash-sale">¥{{ item.salePrice }}</span>
                <span class="flash-original">¥{{ item.originalPrice }}</span>
              </div>
              <div class="flash-progress">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: ((item.totalStock - item.remainStock) / item.totalStock * 100) + '%' }"></div>
                </div>
                <span class="progress-text">已抢{{ Math.round((item.totalStock - item.remainStock) / item.totalStock * 100) }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="section" v-if="!searchMode">
        <div class="section-head">
          <h3>🔥 热门推荐</h3>
          <button class="btn-text" @click="refreshHot">换一批</button>
        </div>
        <div class="product-grid">
          <div
            v-for="product in hotProducts"
            :key="'h'+product.id"
            class="product-card"
            @click="goDetail(product)"
          >
            <div class="card-img">
              <img :src="product.imageUrl" :alt="product.name" v-if="product.imageUrl" loading="lazy" />
              <span class="card-tag" v-if="product.stock < 50">热卖</span>
            </div>
            <div class="card-info">
              <h4>{{ product.name }}</h4>
              <p>{{ product.description }}</p>
              <div class="card-bottom">
                <span class="price">¥{{ product.price.toLocaleString() }}</span>
                <span class="sold">{{ Math.floor(Math.random() * 5000 + 500) }}人付款</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="section" v-if="!searchMode && personalProducts.length">
        <div class="section-head">
          <h3>🎯 为你推荐</h3>
          <span class="recommend-tip">基于你的浏览和购买行为智能推荐</span>
        </div>
        <div class="product-grid">
          <div
            v-for="product in personalProducts"
            :key="'p'+product.id"
            class="product-card"
            @click="goDetail(product)"
          >
            <div class="card-img">
              <img :src="product.imageUrl" :alt="product.name" v-if="product.imageUrl" loading="lazy" />
            </div>
            <div class="card-info">
              <h4>{{ product.name }}</h4>
              <p>{{ product.description }}</p>
              <div class="card-bottom">
                <span class="price">¥{{ product.price.toLocaleString() }}</span>
                <span class="sold">{{ Math.floor(Math.random() * 3000 + 200) }}人付款</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="section" v-if="!searchMode">
        <div class="section-head">
          <h3>✨ 猜你喜欢</h3>
          <button class="btn-text" @click="refreshRandom">换一批</button>
        </div>
        <div class="product-grid">
          <div
            v-for="product in randomProducts"
            :key="'r'+product.id"
            class="product-card"
            @click="goDetail(product)"
          >
            <div class="card-img">
              <img :src="product.imageUrl" :alt="product.name" v-if="product.imageUrl" loading="lazy" />
            </div>
            <div class="card-info">
              <h4>{{ product.name }}</h4>
              <p>{{ product.description }}</p>
              <div class="card-bottom">
                <span class="price">¥{{ product.price.toLocaleString() }}</span>
                <span class="sold">{{ Math.floor(Math.random() * 3000 + 200) }}人付款</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="section">
        <div class="section-head">
          <h3>{{ searchMode ? '' : (currentCat === '全部' ? '全部商品' : currentCat) }}</h3>
          <span class="total-text" v-if="total">共 {{ total }} 件</span>
        </div>
        <div class="product-grid">
          <div
            v-for="product in products"
            :key="product.id"
            class="product-card"
            @click="goDetail(product)"
          >
            <div class="card-img">
              <img :src="product.imageUrl" :alt="product.name" v-if="product.imageUrl" loading="lazy" />
              <span class="card-tag orange" v-if="product.price > 5000">高端</span>
            </div>
            <div class="card-info">
              <h4>{{ product.name }}</h4>
              <p>{{ product.description }}</p>
              <div class="card-bottom">
                <span class="price">¥{{ product.price.toLocaleString() }}</span>
                <span class="sold">{{ Math.floor(Math.random() * 5000 + 500) }}人付款</span>
              </div>
            </div>
          </div>
        </div>
        <div class="pagination" v-if="total > pageSize">
          <button :disabled="pageNum <= 1" @click="pageNum--; loadProducts()">上一页</button>
          <span>{{ pageNum }} / {{ Math.ceil(total / pageSize) }}</span>
          <button :disabled="pageNum >= Math.ceil(total / pageSize)" @click="pageNum++; loadProducts()">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getProducts, searchProducts, getHotProducts, getRandomProducts, getPersonalRecommend, getCurrentFlashSales } from '../api'
import { useUserStore } from '../store/user'
import { useCartStore } from '../store/cart'
import { useUiStore } from '../store/ui'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const uiStore = useUiStore()

const hotProducts = ref([])
const randomProducts = ref([])
const personalProducts = ref([])
const flashSales = ref([])
const countdown = ref({ h: '00', m: '00', s: '00' })
let countdownTimer = null
const products = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const searchMode = ref(false)
const searchKeyword = ref('')
const currentCat = ref('全部')
const productsRef = ref(null)
const particleCanvas = ref(null)
let particleAnim = null

async function loadHot() {
  try { hotProducts.value = await getHotProducts(8) } catch { hotProducts.value = [] }
}

async function loadRandom() {
  try { randomProducts.value = await getRandomProducts(8) } catch { randomProducts.value = [] }
}

async function loadPersonal() {
  if (userStore.userId) {
    try { personalProducts.value = await getPersonalRecommend(userStore.userId, 8) } catch { personalProducts.value = [] }
  }
}

async function loadFlashSales() {
  try {
    flashSales.value = await getCurrentFlashSales()
    if (flashSales.value.length) startCountdown()
  } catch { flashSales.value = [] }
}

function startCountdown() {
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    if (!flashSales.value.length) return
    const end = new Date(flashSales.value[0].endTime).getTime()
    const diff = end - Date.now()
    if (diff <= 0) {
      countdown.value = { h: '00', m: '00', s: '00' }
      clearInterval(countdownTimer)
      return
    }
    const h = Math.floor(diff / 3600000)
    const m = Math.floor((diff % 3600000) / 60000)
    const s = Math.floor((diff % 60000) / 1000)
    countdown.value = {
      h: String(h).padStart(2, '0'),
      m: String(m).padStart(2, '0'),
      s: String(s).padStart(2, '0')
    }
  }, 1000)
}

async function loadProducts() {
  const cat = currentCat.value === '全部' ? undefined : currentCat.value
  try {
    const res = await getProducts({ pageNum: pageNum.value, pageSize: pageSize.value, category: cat })
    products.value = res.records
    total.value = res.total
  } catch (e) {
    console.error('[Home] loadProducts error:', e)
    products.value = []
  }
}

async function doSearch() {
  try {
    const res = await searchProducts({ keyword: searchKeyword.value, pageNum: 1, pageSize: 40 })
    products.value = res.records
    total.value = res.total
    searchMode.value = true
  } catch { products.value = []; total.value = 0 }
}

function clearSearch() {
  searchMode.value = false
  searchKeyword.value = ''
  pageNum.value = 1
  loadProducts()
}

function refreshHot() { loadHot() }
function refreshRandom() { loadRandom() }

function goDetail(product) {
  router.push(`/product/${product.id}`)
}

function scrollToProducts() {
  productsRef.value?.scrollIntoView({ behavior: 'smooth' })
}

watch(() => uiStore.selectedCategory, (newVal) => {
  currentCat.value = newVal || '全部'
  pageNum.value = 1
  searchMode.value = false
  loadProducts()
})

watch(() => route.query.keyword, (val) => {
  if (val) {
    searchKeyword.value = val
    doSearch()
  } else if (searchMode.value) {
    searchMode.value = false
    searchKeyword.value = ''
    pageNum.value = 1
    currentCat.value = '全部'
    loadProducts()
    loadHot()
    loadRandom()
    loadPersonal()
    loadFlashSales()
  }
})

onMounted(() => {
  searchMode.value = false
  searchKeyword.value = ''
  currentCat.value = uiStore.selectedCategory || '全部'
  loadProducts()
  loadHot()
  loadRandom()
  loadPersonal()
  loadFlashSales()
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword
    doSearch()
  }
  initParticles()
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
  if (particleAnim) cancelAnimationFrame(particleAnim)
})

function initParticles() {
  const canvas = particleCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  let w, h, particles, dataStreams

  function resize() {
    const rect = canvas.parentElement.getBoundingClientRect()
    w = canvas.width = rect.width
    h = canvas.height = rect.height
  }

  function createParticles() {
    particles = []
    const count = Math.min(Math.floor(w * h / 6000), 120)
    for (let i = 0; i < count; i++) {
      particles.push({
        x: Math.random() * w,
        y: Math.random() * h,
        vx: (Math.random() - 0.5) * 0.4,
        vy: (Math.random() - 0.5) * 0.4,
        r: Math.random() * 1.8 + 0.5,
        color: Math.random() > 0.6 ? '0, 212, 255' : '124, 58, 237',
        alpha: Math.random() * 0.5 + 0.2
      })
    }
  }

  function createDataStreams() {
    dataStreams = []
    const streamCount = Math.floor(w / 160)
    for (let i = 0; i < streamCount; i++) {
      dataStreams.push({
        x: Math.random() * w,
        y: Math.random() * h,
        speed: Math.random() * 1.2 + 0.3,
        length: Math.random() * 60 + 30,
        alpha: Math.random() * 0.15 + 0.05,
        color: Math.random() > 0.5 ? '0, 212, 255' : '124, 58, 237',
        chars: Array.from({ length: 20 }, () => String.fromCharCode(0x30A0 + Math.random() * 96))
      })
    }
  }

  function drawDataStreams() {
    dataStreams.forEach(s => {
      s.y += s.speed
      if (s.y > h + s.length) {
        s.y = -s.length
        s.x = Math.random() * w
      }
      for (let i = 0; i < s.chars.length; i++) {
        const cy = s.y - i * 14
        if (cy < 0 || cy > h) continue
        const fade = 1 - i / s.chars.length
        ctx.fillStyle = `rgba(${s.color}, ${s.alpha * fade})`
        ctx.font = '11px monospace'
        ctx.fillText(s.chars[i], s.x, cy)
        if (Math.random() < 0.02) {
          s.chars[i] = String.fromCharCode(0x30A0 + Math.random() * 96)
        }
      }
    })
  }

  function draw() {
    ctx.clearRect(0, 0, w, h)

    drawDataStreams()

    for (let i = 0; i < particles.length; i++) {
      const p = particles[i]
      p.x += p.vx
      p.y += p.vy
      if (p.x < 0) p.x = w
      if (p.x > w) p.x = 0
      if (p.y < 0) p.y = h
      if (p.y > h) p.y = 0

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(${p.color}, ${p.alpha})`
      ctx.fill()

      for (let j = i + 1; j < particles.length; j++) {
        const q = particles[j]
        const dx = p.x - q.x
        const dy = p.y - q.y
        const dist = dx * dx + dy * dy
        if (dist < 12000) {
          ctx.beginPath()
          ctx.moveTo(p.x, p.y)
          ctx.lineTo(q.x, q.y)
          ctx.strokeStyle = `rgba(0, 212, 255, ${0.06 * (1 - dist / 12000)})`
          ctx.lineWidth = 0.5
          ctx.stroke()
        }
      }
    }

    particleAnim = requestAnimationFrame(draw)
  }

  resize()
  createParticles()
  createDataStreams()
  draw()

  window.addEventListener('resize', () => {
    resize()
    createParticles()
    createDataStreams()
  })
}
</script>

<style scoped>
.home {
  padding-bottom: 20px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.banner-section {
  background: var(--bg-body);
  padding: 0;
  position: relative;
  overflow: hidden;
}

.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.banner-section::before {
  content: '';
  position: absolute;
  top: -200px;
  left: 50%;
  transform: translateX(-50%);
  width: 800px;
  height: 400px;
  background: radial-gradient(ellipse, rgba(0,212,255,0.12) 0%, transparent 70%);
  pointer-events: none;
}

.banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 64px 0;
  position: relative;
  z-index: 1;
}

.banner-content {
  color: var(--text-primary);
}

.banner-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: rgba(0,212,255,0.1);
  border: 1px solid rgba(0,212,255,0.2);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary-light);
  margin-bottom: 20px;
}

.banner-tag::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  animation: tagPulse 2s ease-in-out infinite;
}

@keyframes tagPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.banner-content h2 {
  font-size: 44px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #fff 0%, rgba(255,255,255,0.7) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.banner-content p {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 28px;
  line-height: 1.6;
}

.banner-btn {
  padding: 14px 32px;
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: var(--font-body);
  transition: all var(--transition-base);
  box-shadow: 0 4px 16px rgba(0,212,255,0.3);
}

.banner-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(0,212,255,0.4);
}

.banner-stats {
  display: flex;
  gap: 56px;
}

.stat-item {
  text-align: center;
  position: relative;
}

.stat-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: -28px;
  top: 50%;
  transform: translateY(-50%);
  width: 1px;
  height: 40px;
  background: var(--border-subtle);
}

.stat-item strong {
  display: block;
  font-size: 36px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.5px;
  margin-bottom: 4px;
  background: var(--gradient-hero);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-item span {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: 400;
}

.section {
  margin-top: 48px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-head h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.2px;
}

.total-text {
  font-size: 13px;
  color: var(--text-tertiary);
}

.recommend-tip {
  font-size: 11px;
  color: var(--color-primary);
  background: rgba(0,212,255,0.08);
  border: 1px solid rgba(0,212,255,0.12);
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 500;
}

.flash-countdown {
  display: flex;
  align-items: center;
  gap: 6px;
}

.countdown-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-right: 8px;
  font-weight: 500;
}

.countdown-box {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 8px;
  min-width: 32px;
  font-variant-numeric: tabular-nums;
  font-family: var(--font-mono);
}

.countdown-sep {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-primary);
}

.flash-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.flash-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-slow);
}

.flash-card:hover {
  background: var(--bg-card-hover);
  border-color: var(--border-default);
  transform: translateY(-4px);
  box-shadow: var(--shadow-glow);
}

.flash-img {
  height: 180px;
  background: var(--bg-surface);
  overflow: hidden;
  position: relative;
}

.flash-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.flash-card:hover .flash-img img {
  transform: scale(1.05);
}

.flash-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--gradient-orange);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 10px;
}

.flash-info {
  padding: 16px;
}

.flash-info h4 {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 10px;
}

.flash-prices {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 14px;
}

.flash-sale {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-orange);
  letter-spacing: -0.3px;
}

.flash-original {
  font-size: 13px;
  color: var(--text-tertiary);
  text-decoration: line-through;
}

.flash-progress {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: rgba(255,255,255,0.06);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--gradient-orange);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 11px;
  color: var(--color-orange);
  font-weight: 600;
  white-space: nowrap;
}

.btn-text {
  border: none;
  background: none;
  color: var(--text-tertiary);
  font-size: 13px;
  cursor: pointer;
  font-family: var(--font-body);
  padding: 6px 14px;
  transition: all var(--transition-fast);
  font-weight: 500;
  border-radius: 10px;
}

.btn-text:hover {
  background: rgba(255,255,255,0.05);
  color: var(--text-primary);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.product-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-slow);
}

.product-card:hover {
  background: var(--bg-card-hover);
  border-color: var(--border-default);
  transform: translateY(-4px);
  box-shadow: var(--shadow-glow);
}

.card-img {
  position: relative;
  height: 200px;
  background: var(--bg-surface);
  overflow: hidden;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.product-card:hover .card-img img {
  transform: scale(1.05);
}

.card-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(0,212,255,0.8);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 8px;
}

.card-tag.orange {
  background: rgba(249,115,22,0.8);
}

.card-info {
  padding: 16px;
}

.card-info h4 {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
  line-height: 1.4;
}

.card-info p {
  font-size: 12px;
  color: var(--text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 12px;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.price {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.3px;
}

.sold {
  font-size: 11px;
  color: var(--text-muted);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 36px;
  padding-bottom: 16px;
}

.pagination button {
  padding: 10px 28px;
  border: 1px solid var(--border-default);
  background: var(--bg-card);
  border-radius: 12px;
  cursor: pointer;
  font-family: var(--font-body);
  font-size: 14px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  font-weight: 500;
}

.pagination button:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: rgba(0,212,255,0.08);
}

.pagination button:disabled {
  opacity: 0.25;
  cursor: not-allowed;
}

.pagination span {
  font-size: 13px;
  color: var(--text-tertiary);
  font-weight: 500;
  font-variant-numeric: tabular-nums;
  min-width: 60px;
  text-align: center;
}

@media (max-width: 1100px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 800px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .flash-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .banner {
    flex-direction: column;
    text-align: center;
    padding: 48px 0;
  }
  .banner-stats {
    margin-top: 28px;
  }
  .banner-content h2 {
    font-size: 32px;
  }
  .stat-item:not(:last-child)::after {
    display: none;
  }
}
</style>
