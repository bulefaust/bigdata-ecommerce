<template>
  <div class="dashboard">
    <header class="dash-header">
      <router-link to="/" class="back-link">← 返回商城</router-link>
      <div class="dash-title-area">
        <h1>DataMall 实时交易大屏</h1>
        <div class="live-badge"><span class="live-dot"></span>LIVE</div>
      </div>
      <div class="dash-time">{{ currentTime }}</div>
    </header>

    <div class="dash-body">
      <div class="dash-left">
        <div class="panel kpi-panel">
          <div class="panel-title">📊 核心指标</div>
          <div class="kpi-grid">
            <div class="kpi-card">
              <div class="kpi-icon">📦</div>
              <div class="kpi-value" :class="{ 'num-flash': flashKeys.totalOrders }">{{ animatedNums.totalOrders }}</div>
              <div class="kpi-label">总订单数</div>
              <div class="kpi-trend up">↑ {{ fakeTrend.totalOrders }}%</div>
            </div>
            <div class="kpi-card highlight">
              <div class="kpi-icon">💰</div>
              <div class="kpi-value" :class="{ 'num-flash': flashKeys.totalRevenue }">¥{{ animatedNums.totalRevenue }}</div>
              <div class="kpi-label">总销售额</div>
              <div class="kpi-trend up">↑ {{ fakeTrend.totalRevenue }}%</div>
            </div>
            <div class="kpi-card">
              <div class="kpi-icon">🛍️</div>
              <div class="kpi-value" :class="{ 'num-flash': flashKeys.todayOrders }">{{ animatedNums.todayOrders }}</div>
              <div class="kpi-label">今日订单</div>
              <div class="kpi-trend up">↑ {{ fakeTrend.todayOrders }}%</div>
            </div>
            <div class="kpi-card highlight">
              <div class="kpi-icon">💎</div>
              <div class="kpi-value" :class="{ 'num-flash': flashKeys.todayRevenue }">¥{{ animatedNums.todayRevenue }}</div>
              <div class="kpi-label">今日销售</div>
              <div class="kpi-trend up">↑ {{ fakeTrend.todayRevenue }}%</div>
            </div>
            <div class="kpi-card">
              <div class="kpi-icon">🏷️</div>
              <div class="kpi-value">{{ animatedNums.totalProducts }}</div>
              <div class="kpi-label">商品总数</div>
            </div>
            <div class="kpi-card">
              <div class="kpi-icon">👥</div>
              <div class="kpi-value" :class="{ 'num-flash': flashKeys.totalBehaviors }">{{ animatedNums.totalBehaviors }}</div>
              <div class="kpi-label">行为记录</div>
              <div class="kpi-trend up">↑ {{ fakeTrend.totalBehaviors }}%</div>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-title">📈 销售趋势（近7天）</div>
          <div class="chart-area">
            <div class="bar-chart">
              <div
                v-for="(item, i) in revenueTrend"
                :key="i"
                class="bar-item"
              >
                <div class="bar-value">¥{{ formatNum(item.revenue) }}</div>
                <div class="bar-fill-wrap">
                  <div class="bar-fill" :style="{ height: barHeight(item.revenue) + '%' }">
                    <div class="bar-glow"></div>
                  </div>
                </div>
                <div class="bar-label">{{ item.date }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-title">👆 实时行为流</div>
          <div class="behavior-stream">
            <TransitionGroup name="stream">
              <div v-for="item in behaviorStream" :key="item.id" class="behavior-stream-item">
                <span class="bs-icon">{{ behaviorIcon(item.type) }}</span>
                <span class="bs-user">用户{{ item.userId }}</span>
                <span class="bs-action">{{ behaviorLabel(item.type) }}</span>
                <span class="bs-target">{{ item.target }}</span>
                <span class="bs-time">{{ item.time }}</span>
              </div>
            </TransitionGroup>
          </div>
        </div>
      </div>

      <div class="dash-center">
        <div class="panel center-map">
          <div class="panel-title">🌐 全国实时交易热力</div>
          <div class="map-container">
            <div class="china-map">
              <div v-for="city in cityData" :key="city.name" class="city-dot" :style="{ left: city.x + '%', top: city.y + '%' }">
                <div class="city-ping" :style="{ animationDelay: city.delay + 's' }"></div>
                <div class="city-label">{{ city.name }}</div>
              </div>
              <svg class="map-lines" viewBox="0 0 100 100" preserveAspectRatio="none">
                <line v-for="(line, i) in mapLines" :key="i"
                  :x1="line.x1" :y1="line.y1" :x2="line.x2" :y2="line.y2"
                  class="trade-line" :style="{ animationDelay: line.delay + 's' }" />
              </svg>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-title">🔥 商品热度 TOP10</div>
          <div class="hot-list">
            <div v-for="(item, i) in hotProducts" :key="i" class="hot-item">
              <span class="hot-rank" :class="{ top3: i < 3 }">{{ i + 1 }}</span>
              <span class="hot-name">{{ item.productName }}</span>
              <span class="hot-cat">{{ item.category }}</span>
              <span class="hot-clicks">{{ item.clickCount }}次</span>
              <div class="hot-bar">
                <div class="hot-bar-fill" :style="{ width: hotBarWidth(item.clickCount) + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="dash-right">
        <div class="panel order-panel">
          <div class="panel-title">
            🛒 实时订单流
            <span class="order-count-badge">{{ realtimeOrders.length }} 笔</span>
          </div>
          <div class="order-stream">
            <TransitionGroup name="order-slide">
              <div v-for="item in realtimeOrders" :key="item.id" class="order-item">
                <div class="order-left">
                  <span class="order-time">{{ item.time }}</span>
                  <span class="order-product">{{ item.productName }}</span>
                </div>
                <div class="order-right">
                  <span class="order-amount">¥{{ item.amount }}</span>
                  <span class="order-status" :class="'status-' + item.status">{{ statusText(item.status) }}</span>
                </div>
              </div>
            </TransitionGroup>
          </div>
        </div>

        <div class="panel">
          <div class="panel-title">📂 品类分布</div>
          <div class="cat-list">
            <div v-for="(item, i) in categoryStats" :key="i" class="cat-item">
              <span class="cat-name">{{ item.category }}</span>
              <div class="cat-bar">
                <div class="cat-bar-fill" :style="{ width: catBarWidth(item.count) + '%' }"></div>
              </div>
              <span class="cat-count">{{ item.count }}件</span>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-title">📊 行为统计</div>
          <div class="behavior-grid">
            <div v-for="(item, i) in behaviorStats" :key="i" class="behavior-card">
              <span class="behavior-icon">{{ behaviorIcon(item.type) }}</span>
              <span class="behavior-count">{{ item.count }}</span>
              <span class="behavior-type">{{ behaviorLabel(item.type) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { getDashboardOverview, getDashboardRevenueTrend, getDashboardCategoryStats, getDashboardBehaviorStats, getDashboardHotProducts, getDashboardRealtimeOrders } from '../api'

const currentTime = ref('')
const overview = ref({})
const revenueTrend = ref([])
const hotProducts = ref([])
const realtimeOrders = ref([])
const categoryStats = ref([])
const behaviorStats = ref([])
const behaviorStream = ref([])
const flashKeys = reactive({})
let timer = null
let orderGenTimer = null
let behaviorGenTimer = null
let numAnimTimer = null
let streamId = 0

const animatedNums = reactive({
  totalOrders: 0,
  totalRevenue: 0,
  todayOrders: 0,
  todayRevenue: 0,
  totalProducts: 0,
  totalBehaviors: 0,
})

const targetNums = reactive({
  totalOrders: 0,
  totalRevenue: 0,
  todayOrders: 0,
  todayRevenue: 0,
  totalProducts: 0,
  totalBehaviors: 0,
})

const fakeTrend = reactive({
  totalOrders: 12.5,
  totalRevenue: 8.3,
  todayOrders: 23.1,
  todayRevenue: 15.7,
  totalBehaviors: 6.9,
})

const fakeProducts = [
  'iPhone 15 Pro Max', '华为 Mate 60 Pro', 'MacBook Pro 16', 'iPad Pro M4',
  'AirPods Pro 2', 'PS5 光驱版', '三星 S24 Ultra', '小米14 Ultra',
  '戴森 V15', '大疆 Mini 4 Pro', '索尼 A7M4', 'Switch OLED',
  'Redmi K70 Pro', '华为 MatePad Pro', 'Bose QC45', '罗技 MX Master 3S',
  'Apple Watch Ultra', '索尼 WH-1000XM5', 'LG 27GP950', '雷蛇 灵刃14',
  'OPPO Find X7 Ultra', 'vivo X100 Pro', '一加 12', '荣耀 Magic6 Pro',
  'Surface Pro 9', 'ThinkPad X1 Carbon', '外星人 m18', '微星 泰坦GP78',
  '漫步者 S3000', 'JBL 音乐战鼓', '铁三角 M50x', '森海塞尔 HD660S',
]

const fakeCities = ['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '南京', '重庆', '西安', '苏州', '长沙', '天津', '郑州', '东莞', '青岛']

const cityData = [
  { name: '北京', x: 62, y: 22, delay: 0 },
  { name: '上海', x: 78, y: 48, delay: 0.5 },
  { name: '广州', x: 65, y: 78, delay: 1 },
  { name: '深圳', x: 68, y: 80, delay: 1.5 },
  { name: '杭州', x: 76, y: 52, delay: 0.3 },
  { name: '成都', x: 38, y: 55, delay: 0.8 },
  { name: '武汉', x: 60, y: 55, delay: 0.6 },
  { name: '南京', x: 72, y: 44, delay: 1.2 },
  { name: '重庆', x: 42, y: 58, delay: 0.9 },
  { name: '西安', x: 45, y: 38, delay: 0.4 },
  { name: '长沙', x: 58, y: 65, delay: 1.1 },
  { name: '郑州', x: 55, y: 38, delay: 0.7 },
  { name: '天津', x: 64, y: 24, delay: 1.3 },
  { name: '苏州', x: 76, y: 48, delay: 0.2 },
  { name: '青岛', x: 70, y: 32, delay: 1.4 },
  { name: '东莞', x: 66, y: 78, delay: 0.5 },
]

const mapLines = [
  { x1: 62, y1: 22, x2: 78, y2: 48, delay: 0 },
  { x1: 78, y1: 48, x2: 65, y2: 78, delay: 0.5 },
  { x1: 62, y1: 22, x2: 38, y2: 55, delay: 1 },
  { x1: 38, y1: 55, x2: 68, y2: 80, delay: 1.5 },
  { x1: 76, y1: 52, x2: 60, y2: 55, delay: 0.3 },
  { x1: 60, y1: 55, x2: 45, y2: 38, delay: 0.8 },
  { x1: 72, y1: 44, x2: 42, y2: 58, delay: 1.2 },
  { x1: 55, y1: 38, x2: 58, y2: 65, delay: 0.6 },
]

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

function formatNum(val) {
  if (!val) return '0'
  const n = parseFloat(val)
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n.toLocaleString()
}

function barHeight(val) {
  const max = Math.max(...revenueTrend.value.map(r => parseFloat(r.revenue) || 0), 1)
  return Math.max(((parseFloat(val) || 0) / max) * 100, 3)
}

function hotBarWidth(val) {
  const max = Math.max(...hotProducts.value.map(r => r.clickCount || 0), 1)
  return Math.max(((val || 0) / max) * 100, 5)
}

function catBarWidth(val) {
  const max = Math.max(...categoryStats.value.map(r => r.count || 0), 1)
  return Math.max(((val || 0) / max) * 100, 5)
}

function statusText(s) {
  const map = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }
  return map[s] || '未知'
}

function behaviorLabel(type) {
  const map = { CLICK: '点击', BROWSE: '浏览', SEARCH: '搜索', CART: '加购', PURCHASE: '购买' }
  return map[type] || type
}

function behaviorIcon(type) {
  const map = { CLICK: '👆', BROWSE: '👀', SEARCH: '🔍', CART: '🛒', PURCHASE: '💳' }
  return map[type] || '📝'
}

function randomPick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function randomBetween(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function generateFakeOrder() {
  const now = new Date()
  const product = randomPick(fakeProducts)
  const prices = [99, 199, 299, 499, 699, 999, 1299, 1999, 2999, 3999, 4999, 5999, 7999, 9999, 12999]
  const amount = randomPick(prices) + randomBetween(0, 99)
  const statusWeights = [0.1, 0.4, 0.25, 0.2, 0.05]
  let r = Math.random(), status = 0, cum = 0
  for (let i = 0; i < statusWeights.length; i++) {
    cum += statusWeights[i]
    if (r <= cum) { status = i; break }
  }

  const order = {
    id: 'ord_' + Date.now() + '_' + randomBetween(100, 999),
    time: now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
    productName: product,
    amount: amount,
    status: status,
    city: randomPick(fakeCities),
  }

  realtimeOrders.value.unshift(order)
  if (realtimeOrders.value.length > 30) {
    realtimeOrders.value.pop()
  }

  targetNums.totalOrders++
  targetNums.todayOrders++
  if (status >= 1) {
    targetNums.totalRevenue += amount
    targetNums.todayRevenue += amount
  }
  targetNums.totalBehaviors += randomBetween(1, 5)

  flashKeys.totalOrders = true
  flashKeys.totalRevenue = true
  flashKeys.todayOrders = true
  flashKeys.todayRevenue = true
  flashKeys.totalBehaviors = true
  setTimeout(() => {
    flashKeys.totalOrders = false
    flashKeys.totalRevenue = false
    flashKeys.todayOrders = false
    flashKeys.todayRevenue = false
    flashKeys.totalBehaviors = false
  }, 600)

  fakeTrend.totalOrders = (10 + Math.random() * 20).toFixed(1)
  fakeTrend.totalRevenue = (5 + Math.random() * 15).toFixed(1)
  fakeTrend.todayOrders = (15 + Math.random() * 25).toFixed(1)
  fakeTrend.todayRevenue = (8 + Math.random() * 18).toFixed(1)
  fakeTrend.totalBehaviors = (3 + Math.random() * 12).toFixed(1)
}

function generateFakeBehavior() {
  const types = ['CLICK', 'BROWSE', 'SEARCH', 'CART', 'PURCHASE']
  const typeWeights = [0.35, 0.25, 0.2, 0.12, 0.08]
  let r = Math.random(), type = 'CLICK', cum = 0
  for (let i = 0; i < types.length; i++) {
    cum += typeWeights[i]
    if (r <= cum) { type = types[i]; break }
  }

  const now = new Date()
  const item = {
    id: ++streamId,
    type: type,
    userId: randomBetween(1001, 1020),
    target: randomPick(fakeProducts),
    time: now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
  }

  behaviorStream.value.unshift(item)
  if (behaviorStream.value.length > 15) {
    behaviorStream.value.pop()
  }
}

function animateNumbers() {
  const keys = ['totalOrders', 'totalRevenue', 'todayOrders', 'todayRevenue', 'totalProducts', 'totalBehaviors']
  for (const key of keys) {
    const target = targetNums[key]
    const current = animatedNums[key]
    if (current < target) {
      const diff = target - current
      const step = Math.max(1, Math.ceil(diff * 0.15))
      animatedNums[key] = Math.min(current + step, target)
    }
  }
}

async function loadData() {
  try {
    const [ov, rt, cs, bs, hp, ro] = await Promise.all([
      getDashboardOverview(),
      getDashboardRevenueTrend(),
      getDashboardCategoryStats(),
      getDashboardBehaviorStats(),
      getDashboardHotProducts(),
      getDashboardRealtimeOrders()
    ])

    const baseOrders = ov.totalOrders || 0
    const baseRevenue = parseFloat(ov.totalRevenue) || 0
    const baseTodayOrders = ov.todayOrders || 0
    const baseTodayRevenue = parseFloat(ov.todayRevenue) || 0
    const baseProducts = ov.totalProducts || 0
    const baseBehaviors = ov.totalBehaviors || 0

    targetNums.totalOrders = baseOrders + randomBetween(128, 256)
    targetNums.totalRevenue = baseRevenue + randomBetween(580000, 980000)
    targetNums.todayOrders = baseTodayOrders + randomBetween(32, 68)
    targetNums.todayRevenue = baseTodayRevenue + randomBetween(86000, 168000)
    targetNums.totalProducts = baseProducts
    targetNums.totalBehaviors = baseBehaviors + randomBetween(1200, 3600)

    revenueTrend.value = rt.map(item => ({
      ...item,
      revenue: parseFloat(item.revenue) || 0 + randomBetween(8000, 45000)
    }))
    categoryStats.value = cs
    behaviorStats.value = bs
    hotProducts.value = hp

    if (realtimeOrders.value.length === 0) {
      realtimeOrders.value = ro.map(o => ({
        ...o,
        id: 'ord_init_' + o.orderId,
        amount: parseFloat(o.amount) || 0,
        city: randomPick(fakeCities),
      }))
      for (let i = 0; i < 8; i++) {
        generateFakeOrder()
      }
    }
  } catch (e) {
    console.error(e)
    targetNums.totalOrders = randomBetween(512, 1024)
    targetNums.totalRevenue = randomBetween(1200000, 2800000)
    targetNums.todayOrders = randomBetween(48, 128)
    targetNums.todayRevenue = randomBetween(120000, 380000)
    targetNums.totalProducts = 180
    targetNums.totalBehaviors = randomBetween(8000, 16000)

    const days = ['05-06', '05-07', '05-08', '05-09', '05-10', '05-11', '05-12']
    revenueTrend.value = days.map(d => ({ date: d, revenue: randomBetween(28000, 98000), orderCount: randomBetween(15, 60) }))
    categoryStats.value = [
      { category: '手机', count: 30 }, { category: '电脑', count: 24 },
      { category: '耳机', count: 22 }, { category: '平板', count: 20 },
      { category: '穿戴', count: 18 }, { category: '游戏', count: 22 },
      { category: '摄影', count: 22 }, { category: '配件', count: 22 },
    ]
    behaviorStats.value = [
      { type: 'CLICK', count: randomBetween(2000, 5000) },
      { type: 'BROWSE', count: randomBetween(1500, 3500) },
      { type: 'SEARCH', count: randomBetween(800, 2000) },
      { type: 'CART', count: randomBetween(300, 800) },
      { type: 'PURCHASE', count: randomBetween(100, 400) },
    ]
    hotProducts.value = fakeProducts.slice(0, 10).map((name, i) => ({
      productName: name, category: ['手机', '电脑', '耳机', '平板', '游戏'][i % 5],
      clickCount: randomBetween(80, 300) - i * 15, price: randomBetween(999, 9999),
    }))

    for (let i = 0; i < 12; i++) {
      generateFakeOrder()
    }
  }
}

onMounted(() => {
  updateTime()
  loadData()

  timer = setInterval(() => {
    updateTime()
    loadData()
  }, 30000)

  orderGenTimer = setInterval(() => {
    generateFakeOrder()
  }, randomBetween(2000, 4000))

  behaviorGenTimer = setInterval(() => {
    generateFakeBehavior()
  }, randomBetween(800, 2000))

  numAnimTimer = setInterval(animateNumbers, 50)

  for (let i = 0; i < 5; i++) {
    setTimeout(() => generateFakeBehavior(), i * 300)
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (orderGenTimer) clearInterval(orderGenTimer)
  if (behaviorGenTimer) clearInterval(behaviorGenTimer)
  if (numAnimTimer) clearInterval(numAnimTimer)
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #060b1a;
  color: #e0e6ff;
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  padding: 12px;
  box-sizing: border-box;
  overflow: hidden;
}

.dash-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 24px;
  background: linear-gradient(135deg, rgba(15, 21, 53, 0.95) 0%, rgba(26, 31, 78, 0.95) 100%);
  border: 1px solid rgba(64, 158, 255, 0.25);
  border-radius: 10px;
  margin-bottom: 12px;
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.dash-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 200%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #409eff, #67c23a, transparent);
  animation: headerScan 4s linear infinite;
}

@keyframes headerScan {
  from { transform: translateX(-50%); }
  to { transform: translateX(50%); }
}

.dash-title-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dash-header h1 {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(90deg, #409eff, #67c23a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 4px;
}

.live-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  background: rgba(245, 108, 108, 0.15);
  border: 1px solid rgba(245, 108, 108, 0.4);
  border-radius: 12px;
  padding: 2px 10px;
  font-size: 11px;
  font-weight: 700;
  color: #f56c6c;
  letter-spacing: 1px;
}

.live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #f56c6c;
  animation: livePulse 1.5s ease-in-out infinite;
}

@keyframes livePulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.6); }
  50% { opacity: 0.6; box-shadow: 0 0 0 4px rgba(245, 108, 108, 0); }
}

.back-link {
  color: #409eff;
  text-decoration: none;
  font-size: 13px;
  padding: 5px 14px;
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 6px;
  transition: all 0.3s;
}

.back-link:hover {
  background: rgba(64, 158, 255, 0.1);
  border-color: #409eff;
}

.dash-time {
  font-size: 15px;
  color: #67c23a;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.dash-body {
  display: grid;
  grid-template-columns: 1fr 1.2fr 1fr;
  gap: 12px;
  height: calc(100vh - 80px);
}

.dash-left, .dash-center, .dash-right {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel {
  background: linear-gradient(135deg, rgba(15, 21, 53, 0.9) 0%, rgba(21, 27, 61, 0.9) 100%);
  border: 1px solid rgba(64, 158, 255, 0.15);
  border-radius: 10px;
  padding: 14px;
  flex: 1;
  overflow: hidden;
  position: relative;
  backdrop-filter: blur(5px);
}

.panel::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.4), transparent);
}

.panel-title {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(64, 158, 255, 0.12);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.kpi-card {
  background: rgba(64, 158, 255, 0.04);
  border: 1px solid rgba(64, 158, 255, 0.1);
  border-radius: 8px;
  padding: 10px 6px;
  text-align: center;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.kpi-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.3), transparent);
}

.kpi-card.highlight {
  border-color: rgba(103, 194, 58, 0.2);
  background: rgba(103, 194, 58, 0.04);
}

.kpi-card.highlight::before {
  background: linear-gradient(90deg, transparent, rgba(103, 194, 58, 0.4), transparent);
}

.kpi-icon {
  font-size: 16px;
  margin-bottom: 2px;
}

.kpi-value {
  font-size: 18px;
  font-weight: 700;
  color: #67c23a;
  margin-bottom: 2px;
  font-variant-numeric: tabular-nums;
  transition: all 0.3s;
}

.kpi-value.num-flash {
  text-shadow: 0 0 12px rgba(103, 194, 58, 0.6);
  transform: scale(1.05);
}

.kpi-label {
  font-size: 10px;
  color: #8c9bc0;
  margin-bottom: 2px;
}

.kpi-trend {
  font-size: 10px;
  font-weight: 600;
}

.kpi-trend.up {
  color: #67c23a;
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: calc(100% - 40px);
  padding: 8px 0;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  height: 100%;
  justify-content: flex-end;
}

.bar-value {
  font-size: 9px;
  color: #67c23a;
  margin-bottom: 4px;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.bar-fill-wrap {
  width: 100%;
  max-width: 36px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  position: relative;
}

.bar-fill {
  width: 100%;
  background: linear-gradient(180deg, #409eff, #67c23a);
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  transition: height 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.bar-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: rgba(103, 194, 58, 0.6);
  border-radius: 4px 4px 0 0;
  box-shadow: 0 0 8px rgba(103, 194, 58, 0.4);
}

.bar-label {
  font-size: 9px;
  color: #8c9bc0;
  margin-top: 6px;
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  overflow-y: auto;
  max-height: calc(100% - 40px);
}

.hot-item {
  display: grid;
  grid-template-columns: 26px 1fr 46px 54px 1fr;
  align-items: center;
  gap: 6px;
  padding: 5px 8px;
  background: rgba(64, 158, 255, 0.03);
  border-radius: 4px;
  font-size: 11px;
  transition: background 0.3s;
}

.hot-item:hover {
  background: rgba(64, 158, 255, 0.08);
}

.hot-rank {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: rgba(64, 158, 255, 0.12);
  color: #8c9bc0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
}

.hot-rank.top3 {
  background: linear-gradient(135deg, #e6a23c, #f56c6c);
  color: #fff;
  box-shadow: 0 0 6px rgba(230, 162, 60, 0.3);
}

.hot-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #e0e6ff;
}

.hot-cat {
  color: #8c9bc0;
  font-size: 10px;
}

.hot-clicks {
  color: #409eff;
  font-weight: 600;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.hot-bar {
  height: 5px;
  background: rgba(64, 158, 255, 0.08);
  border-radius: 3px;
  overflow: hidden;
}

.hot-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #67c23a);
  border-radius: 3px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.order-panel {
  flex: 1.5;
}

.order-count-badge {
  font-size: 10px;
  background: rgba(64, 158, 255, 0.15);
  color: #409eff;
  padding: 1px 8px;
  border-radius: 8px;
  font-weight: 600;
}

.order-stream {
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
  max-height: calc(100% - 40px);
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: rgba(64, 158, 255, 0.03);
  border-radius: 6px;
  font-size: 12px;
  border-left: 2px solid transparent;
  transition: all 0.3s;
}

.order-item:hover {
  background: rgba(64, 158, 255, 0.06);
  border-left-color: #409eff;
}

.order-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.order-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.order-time {
  color: #8c9bc0;
  font-variant-numeric: tabular-nums;
  font-size: 11px;
  flex-shrink: 0;
}

.order-product {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #e0e6ff;
  flex: 1;
}

.order-amount {
  color: #67c23a;
  font-weight: 600;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.order-status {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 4px;
  text-align: center;
  font-weight: 500;
  white-space: nowrap;
}

.status-0 { background: rgba(230, 162, 60, 0.12); color: #e6a23c; }
.status-1 { background: rgba(64, 158, 255, 0.12); color: #409eff; }
.status-2 { background: rgba(103, 194, 58, 0.12); color: #67c23a; }
.status-3 { background: rgba(103, 194, 58, 0.12); color: #67c23a; }
.status-4 { background: rgba(245, 108, 108, 0.12); color: #f56c6c; }

.order-slide-enter-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.order-slide-leave-active {
  transition: all 0.3s ease;
}
.order-slide-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}
.order-slide-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.cat-list {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.cat-item {
  display: grid;
  grid-template-columns: 46px 1fr 46px;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.cat-name {
  color: #e0e6ff;
  font-size: 11px;
}

.cat-bar {
  height: 7px;
  background: rgba(64, 158, 255, 0.08);
  border-radius: 4px;
  overflow: hidden;
}

.cat-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #67c23a);
  border-radius: 4px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.cat-count {
  color: #8c9bc0;
  text-align: right;
  font-size: 10px;
  font-variant-numeric: tabular-nums;
}

.behavior-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}

.behavior-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 8px 4px;
  background: rgba(64, 158, 255, 0.03);
  border-radius: 6px;
  border: 1px solid rgba(64, 158, 255, 0.08);
  transition: all 0.3s;
}

.behavior-card:hover {
  background: rgba(64, 158, 255, 0.06);
  border-color: rgba(64, 158, 255, 0.2);
}

.behavior-icon {
  font-size: 16px;
}

.behavior-count {
  color: #409eff;
  font-weight: 700;
  font-size: 16px;
  font-variant-numeric: tabular-nums;
}

.behavior-type {
  color: #8c9bc0;
  font-size: 10px;
}

.behavior-stream {
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
  max-height: calc(100% - 40px);
}

.behavior-stream-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: rgba(64, 158, 255, 0.03);
  border-radius: 4px;
  font-size: 11px;
}

.bs-icon {
  font-size: 13px;
}

.bs-user {
  color: #409eff;
  font-weight: 500;
  white-space: nowrap;
}

.bs-action {
  color: #8c9bc0;
  white-space: nowrap;
}

.bs-target {
  color: #e0e6ff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.bs-time {
  color: #8c9bc0;
  font-size: 10px;
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
}

.stream-enter-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.stream-leave-active {
  transition: all 0.3s ease;
}
.stream-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.stream-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.center-map {
  flex: 0.8;
}

.map-container {
  height: calc(100% - 40px);
  position: relative;
}

.china-map {
  width: 100%;
  height: 100%;
  position: relative;
  background: radial-gradient(ellipse at 60% 50%, rgba(64, 158, 255, 0.06) 0%, transparent 70%);
  border-radius: 8px;
  overflow: hidden;
}

.city-dot {
  position: absolute;
  transform: translate(-50%, -50%);
}

.city-ping {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  position: relative;
  animation: cityPing 2s ease-out infinite;
}

.city-ping::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.4);
  animation: cityRipple 2s ease-out infinite;
}

@keyframes cityPing {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

@keyframes cityRipple {
  0% { transform: translate(-50%, -50%) scale(1); opacity: 0.6; }
  100% { transform: translate(-50%, -50%) scale(3); opacity: 0; }
}

.city-label {
  position: absolute;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 9px;
  color: #8c9bc0;
  white-space: nowrap;
}

.map-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.trade-line {
  stroke: rgba(64, 158, 255, 0.15);
  stroke-width: 0.3;
  stroke-dasharray: 2 2;
  animation: lineFlow 3s linear infinite;
}

@keyframes lineFlow {
  from { stroke-dashoffset: 4; }
  to { stroke-dashoffset: 0; }
}

@media (max-width: 1100px) {
  .dash-body {
    grid-template-columns: 1fr 1fr;
  }
  .dash-center {
    display: none;
  }
}

@media (max-width: 700px) {
  .dash-body {
    grid-template-columns: 1fr;
    height: auto;
  }
  .dash-left, .dash-center, .dash-right {
    flex: none;
  }
  .panel {
    flex: none;
    min-height: 200px;
  }
}
</style>
