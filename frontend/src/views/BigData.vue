<template>
  <div class="analytics-page">
    <div class="page-header">
      <h1><span class="icon">📊</span> 大数据分析中心</h1>
      <p class="subtitle">基于Kafka + HBase + Spark的实时数据洞察</p>
    </div>

    <div class="tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.id"
        :class="['tab-btn', { active: activeTab === tab.id }]"
        @click="activeTab = tab.id"
      >
        {{ tab.icon }} {{ tab.name }}
      </button>
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else class="content">
      <div v-show="activeTab === 'hot'" class="tab-content">
        <div class="section-header">
          <h2>🔥 实时热榜</h2>
          <div class="time-filter">
            <button 
              v-for="t in timeRanges" 
              :key="t.value"
              :class="['filter-btn', { active: selectedTimeRange === t.value }]"
              @click="selectedTimeRange = t.value; loadHotProducts()"
            >
              {{ t.label }}
            </button>
          </div>
        </div>
        
        <div class="hot-grid">
          <div 
            v-for="(item, index) in hotProducts" 
            :key="item.productId"
            class="hot-card"
            @click="goToProduct(item.productId)"
          >
            <div :class="['rank', getRankClass(index)]">{{ index + 1 }}</div>
            <div class="hot-info">
              <h3>{{ item.productName || '商品 #' + item.productId }}</h3>
              <p class="category">{{ item.category }}</p>
              <div class="stats-row">
                <span class="stat views"><span class="label">浏览</span>{{ item.clicks || 0 }}</span>
                <span class="stat purchases"><span class="label">购买</span>{{ item.purchases || 0 }}</span>
                <span class="stat carts"><span class="label">加购</span>{{ item.carts || 0 }}</span>
              </div>
              <div class="hot-score">
                <div class="score-bar" :style="{ width: getScoreWidth(item.hotScore) + '%' }"></div>
                <span class="score-text">热度: {{ (item.hotScore || 0).toFixed(0) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="hotProducts.length === 0" class="empty-state">
          <p>暂无热榜数据，请确保有足够的用户行为数据</p>
        </div>
      </div>

      <div v-show="activeTab === 'activity'" class="tab-content">
        <div class="section-header">
          <h2>👥 用户行为分析</h2>
          <div class="date-picker">
            <input type="date" v-model="startDate" @change="loadUserActivity">
            <span>至</span>
            <input type="date" v-model="endDate" @change="loadUserActivity">
          </div>
        </div>

        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-icon blue">👤</div>
            <div class="stat-info">
              <span class="stat-value">{{ activityStats.totalBehaviors || 0 }}</span>
              <span class="stat-label">总行为数</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon green">📈</div>
            <div class="stat-info">
              <span class="stat-value">{{ getMaxDAU() }}</span>
              <span class="stat-label">日活峰值</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon orange">⏰</div>
            <div class="stat-info">
              <span class="stat-value">{{ getPeakHour() }}:00</span>
              <span class="stat-label">活跃高峰</span>
            </div>
          </div>
        </div>

        <div class="charts-grid">
          <div class="chart-card">
            <h3>📅 日活趋势</h3>
            <div class="bar-chart">
              <div 
                v-for="day in activityStats.dailyActive" 
                :key="day.date"
                class="bar-item"
              >
                <div class="bar" :style="{ height: getBarHeight(day.dau) + '%' }"></div>
                <span class="bar-label">{{ formatDate(day.date) }}</span>
              </div>
            </div>
          </div>

          <div class="chart-card">
            <h3>⏰ 时段分布</h3>
            <div class="line-chart">
              <div class="line-points">
                <div 
                  v-for="hour in activityStats.hourlyDistribution" 
                  :key="hour.hour"
                  class="line-point"
                >
                  <div class="point-dot" :style="{ top: getHourHeight(hour.cnt) + '%' }"></div>
                  <span class="hour-label">{{ hour.hour }}h</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="retention-section">
          <h3>📊 用户留存</h3>
          <div class="retention-stats">
            <div class="retention-item">
              <span class="retention-value">{{ getRetention('d1') }}</span>
              <span class="retention-label">次日留存</span>
            </div>
            <div class="retention-item">
              <span class="retention-value">{{ getRetention('d7') }}</span>
              <span class="retention-label">7日留存</span>
            </div>
            <div class="retention-item">
              <span class="retention-value">{{ activityStats.totalUsers || 0 }}</span>
              <span class="retention-label">总用户数</span>
            </div>
          </div>
        </div>
      </div>

      <div v-show="activeTab === 'funnel'" class="tab-content">
        <div class="section-header">
          <h2>🔄 转化漏斗</h2>
          <div class="date-picker">
            <input type="date" v-model="funnelStartDate" @change="loadFunnel">
            <span>至</span>
            <input type="date" v-model="funnelEndDate" @change="loadFunnel">
          </div>
        </div>

        <div class="funnel-container">
          <div class="funnel-chart">
            <div 
              v-for="(value, key, index) in funnelData" 
              :key="key"
              class="funnel-step"
            >
              <div class="funnel-bar-container">
                <div 
                  class="funnel-bar"
                  :style="{ width: getFunnelWidth(value) + '%' }"
                  :class="getFunnelClass(index)"
                >
                  <span class="funnel-value">{{ value }}</span>
                </div>
              </div>
              <span class="funnel-label">{{ key }}</span>
              <span v-if="conversionRates[index]" class="funnel-rate">
                ↓ {{ conversionRates[index].rate }}
              </span>
            </div>
          </div>

          <div class="funnel-insights">
            <h3>💡 关键洞察</h3>
            <div class="insight-item" v-for="(rate, index) in conversionRates" :key="index">
              <span class="insight-icon">📉</span>
              <span>{{ rate.step }}: <strong>{{ rate.rate }}</strong></span>
            </div>
          </div>
        </div>
      </div>

      <div v-show="activeTab === 'forecast'" class="tab-content">
        <div class="section-header">
          <h2>📈 销售预测</h2>
          <select v-model="forecastDays" @change="loadForecast" class="forecast-select">
            <option :value="7">近7天</option>
            <option :value="14">近14天</option>
            <option :value="30">近30天</option>
          </select>
        </div>

        <div class="forecast-stats">
          <div class="forecast-card">
            <span class="forecast-icon">📊</span>
            <div class="forecast-info">
              <span class="forecast-value">{{ forecastData.avgDaily || 0 }}</span>
              <span class="forecast-label">日均订单</span>
            </div>
          </div>
          <div class="forecast-card">
            <span class="forecast-icon" :class="forecastData.trend === '上升' ? 'up' : 'down'">
              {{ forecastData.trend === '上升' ? '📈' : '📉' }}
            </span>
            <div class="forecast-info">
              <span class="forecast-value">{{ forecastData.trendValue || '0%' }}</span>
              <span class="forecast-label">{{ forecastData.trend || '稳定' }}</span>
            </div>
          </div>
        </div>

        <div class="forecast-chart">
          <h3>历史数据与预测</h3>
          <div class="forecast-bars">
            <div class="forecast-section">
              <span class="section-label">历史</span>
              <div class="bars-row">
                <div 
                  v-for="day in forecastData.historical" 
                  :key="'h-' + day.date"
                  class="forecast-bar historical"
                  :style="{ height: getForecastHeight(day.purchases) + 'px' }"
                  :title="day.date + ': ' + day.purchases + ' 订单'"
                ></div>
              </div>
            </div>
            <div class="forecast-section">
              <span class="section-label">预测</span>
              <div class="bars-row">
                <div 
                  v-for="day in forecastData.forecast" 
                  :key="'f-' + day.date"
                  class="forecast-bar forecast"
                  :style="{ height: getForecastHeight(day.predicted) + 'px' }"
                  :title="day.date + ': ' + day.predicted + ' 预测订单'"
                ></div>
              </div>
            </div>
          </div>
          <div class="forecast-legend">
            <span class="legend-item"><span class="dot historical"></span> 历史数据</span>
            <span class="legend-item"><span class="dot forecast"></span> AI预测</span>
          </div>
        </div>

        <div class="forecast-insights">
          <h3>🔮 预测分析</h3>
          <p v-if="forecastData.trend === '上升'">
            根据历史数据分析，系统预测销量呈现<strong>上升趋势</strong>，
            预计未来一段时间日均订单量将达到 <strong>{{ Math.round(forecastData.avgDaily * 1.2) }}</strong> 单。
          </p>
          <p v-else>
            当前销量趋势较为<strong>平稳</strong>，建议关注商品质量和用户运营，
            提升用户转化率以实现增长。
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  getHotProductsAnalytics, 
  getUserActivity, 
  getUserRetention,
  getConversionFunnel,
  getSalesForecast,
  updateHotProducts
} from '@/api'

const router = useRouter()

const tabs = [
  { id: 'hot', name: '实时热榜', icon: '🔥' },
  { id: 'activity', name: '用户分析', icon: '👥' },
  { id: 'funnel', name: '转化漏斗', icon: '🔄' },
  { id: 'forecast', name: '销售预测', icon: '📈' }
]

const timeRanges = [
  { label: '1小时', value: '1h' },
  { label: '6小时', value: '6h' },
  { label: '24小时', value: '1d' },
  { label: '7天', value: '7d' }
]

const activeTab = ref('hot')
const loading = ref(false)
const selectedTimeRange = ref('1h')

const hotProducts = ref([])
const activityStats = ref({ dailyActive: [], hourlyDistribution: [], totalBehaviors: 0, totalUsers: 0, d1_retained: 0, d7_retained: 0 })
const funnelData = ref({})
const conversionRates = ref([])
const forecastData = ref({ historical: [], forecast: [], avgDaily: 0, trend: '稳定' })

const startDate = ref(getDefaultStartDate())
const endDate = ref(getDefaultEndDate())
const funnelStartDate = ref(getDefaultStartDate())
const funnelEndDate = ref(getDefaultEndDate())
const forecastDays = ref(30)

function getDefaultStartDate() {
  const d = new Date()
  d.setDate(d.getDate() - 30)
  return d.toISOString().split('T')[0]
}

function getDefaultEndDate() {
  return new Date().toISOString().split('T')[0]
}

async function loadHotProducts() {
  try {
    const res = await getHotProductsAnalytics(20, selectedTimeRange.value)
    hotProducts.value = res.products || []
  } catch (e) {
    console.error('加载热榜失败:', e)
    hotProducts.value = []
  }
}

async function loadUserActivity() {
  try {
    const res = await getUserActivity(startDate.value, endDate.value)
    activityStats.value = {
      ...res,
      totalBehaviors: res.totalBehaviors || 0
    }
    
    const retentionRes = await getUserRetention(7)
    if (retentionRes && retentionRes.length > 0) {
      const r = retentionRes[0]
      activityStats.value.totalUsers = r.total_users || 0
      activityStats.value.d1_retained = r.d1_retained || 0
      activityStats.value.d7_retained = r.d7_retained || 0
    }
  } catch (e) {
    console.error('加载用户活动失败:', e)
  }
}

async function loadFunnel() {
  try {
    const res = await getConversionFunnel(funnelStartDate.value, funnelEndDate.value)
    funnelData.value = res.funnel || {}
    conversionRates.value = res.conversionRates || []
  } catch (e) {
    console.error('加载漏斗失败:', e)
    funnelData.value = {}
  }
}

async function loadForecast() {
  try {
    const res = await getSalesForecast(forecastDays.value)
    forecastData.value = res || {}
  } catch (e) {
    console.error('加载预测失败:', e)
  }
}

function getRankClass(index) {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return ''
}

function getScoreWidth(score) {
  const maxScore = Math.max(...hotProducts.value.map(p => p.hotScore || 0), 1)
  return ((score || 0) / maxScore * 100)
}

function getBarHeight(value) {
  const max = Math.max(...(activityStats.value.dailyActive || []).map(d => d.dau || 0), 1)
  return ((value || 0) / max * 100)
}

function getHourHeight(cnt) {
  const max = Math.max(...(activityStats.value.hourlyDistribution || []).map(h => h.cnt || 0), 1)
  return 100 - ((cnt || 0) / max * 100)
}

function getFunnelWidth(value) {
  const max = Math.max(...Object.values(funnelData.value), 1)
  return ((value || 0) / max * 100)
}

function getFunnelClass(index) {
  const classes = ['blue', 'cyan', 'teal', 'green', 'lime']
  return classes[index % classes.length]
}

function getForecastHeight(value) {
  const all = [...(forecastData.value.historical || []), ...(forecastData.value.forecast || [])]
  const max = Math.max(...all.map(d => Math.max(d.purchases || 0, d.predicted || 0)), 1)
  return Math.max(10, ((value || 0) / max * 100))
}

function getMaxDAU() {
  const arr = activityStats.value.dailyActive || []
  return arr.length ? Math.max(...arr.map(d => d.dau || 0)) : 0
}

function getPeakHour() {
  const arr = activityStats.value.hourlyDistribution || []
  if (!arr.length) return 0
  const max = arr.reduce((a, b) => (a.cnt || 0) > (b.cnt || 0) ? a : b)
  return max.hour || 0
}

function getRetention(type) {
  if (type === 'd1') {
    const total = activityStats.value.totalUsers || 0
    const retained = activityStats.value.d1_retained || 0
    return total > 0 ? ((retained / total) * 100).toFixed(1) + '%' : '0%'
  }
  if (type === 'd7') {
    const total = activityStats.value.totalUsers || 0
    const retained = activityStats.value.d7_retained || 0
    return total > 0 ? ((retained / total) * 100).toFixed(1) + '%' : '0%'
  }
  return '0%'
}

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function goToProduct(id) {
  router.push(`/product/${id}`)
}

onMounted(async () => {
  loading.value = true
  await Promise.all([
    loadHotProducts(),
    loadUserActivity(),
    loadFunnel(),
    loadForecast()
  ])
  loading.value = false
})
</script>

<style scoped>
.analytics-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0f1a 0%, #0d1525 50%, #0a1628 100%);
  padding: 2rem;
  color: #e8f4f8;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  background: linear-gradient(90deg, #00d4ff, #00ff88);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header .subtitle {
  color: #7a9bb8;
  font-size: 1.1rem;
}

.tabs {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.tab-btn {
  padding: 0.8rem 1.5rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  background: rgba(0, 212, 255, 0.05);
  color: #7a9bb8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 1rem;
}

.tab-btn:hover {
  background: rgba(0, 212, 255, 0.1);
  border-color: #00d4ff;
  color: #00d4ff;
}

.tab-btn.active {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.2), rgba(0, 255, 136, 0.1));
  border-color: #00d4ff;
  color: #00d4ff;
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(0, 212, 255, 0.3);
  border-top-color: #00d4ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.section-header h2 {
  font-size: 1.5rem;
  color: #00d4ff;
}

.time-filter {
  display: flex;
  gap: 0.5rem;
}

.filter-btn {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  background: transparent;
  color: #7a9bb8;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-btn:hover,
.filter-btn.active {
  background: rgba(0, 212, 255, 0.2);
  border-color: #00d4ff;
  color: #00d4ff;
}

.date-picker {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.date-picker input {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  background: rgba(0, 0, 0, 0.3);
  color: #e8f4f8;
  border-radius: 6px;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.hot-card {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: rgba(30, 58, 95, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.hot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.2);
  border-color: #00d4ff;
}

.rank {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  font-weight: bold;
  border-radius: 8px;
  background: rgba(0, 212, 255, 0.1);
  color: #00d4ff;
  flex-shrink: 0;
}

.rank.gold { background: linear-gradient(135deg, #ffd700, #ff8c00); color: #000; }
.rank.silver { background: linear-gradient(135deg, #c0c0c0, #808080); color: #000; }
.rank.bronze { background: linear-gradient(135deg, #cd7f32, #8b4513); color: #fff; }

.hot-info {
  flex: 1;
  min-width: 0;
}

.hot-info h3 {
  font-size: 1rem;
  margin-bottom: 0.3rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category {
  font-size: 0.85rem;
  color: #7a9bb8;
  margin-bottom: 0.5rem;
}

.stats-row {
  display: flex;
  gap: 0.8rem;
  margin-bottom: 0.5rem;
}

.stat {
  font-size: 0.85rem;
}

.stat .label {
  color: #7a9bb8;
  margin-right: 0.3rem;
}

.stat.views { color: #00d4ff; }
.stat.purchases { color: #00ff88; }
.stat.carts { color: #f59e0b; }

.hot-score {
  position: relative;
  height: 20px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
  overflow: hidden;
}

.score-bar {
  height: 100%;
  background: linear-gradient(90deg, #00d4ff, #00ff88);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.score-text {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.75rem;
  color: #fff;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: rgba(30, 58, 95, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  border-radius: 10px;
}

.stat-icon.blue { background: rgba(0, 212, 255, 0.2); }
.stat-icon.green { background: rgba(0, 255, 136, 0.2); }
.stat-icon.orange { background: rgba(245, 158, 11, 0.2); }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: bold;
  color: #00d4ff;
}

.stat-label {
  color: #7a9bb8;
  font-size: 0.9rem;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.chart-card {
  padding: 1.5rem;
  background: rgba(30, 58, 95, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
}

.chart-card h3 {
  margin-bottom: 1rem;
  color: #00d4ff;
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 150px;
  padding-top: 1rem;
}

.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.bar {
  width: 100%;
  background: linear-gradient(180deg, #00d4ff, #0066cc);
  border-radius: 4px 4px 0 0;
  margin-top: auto;
  transition: height 0.5s ease;
}

.bar-label {
  font-size: 0.7rem;
  color: #7a9bb8;
  margin-top: 0.3rem;
  writing-mode: vertical-rl;
  transform: rotate(180deg);
}

.line-chart {
  height: 150px;
  position: relative;
  padding: 1rem 0;
}

.line-points {
  display: flex;
  justify-content: space-between;
  height: 100%;
  position: relative;
}

.line-point {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
}

.point-dot {
  width: 8px;
  height: 8px;
  background: #00d4ff;
  border-radius: 50%;
  position: absolute;
  box-shadow: 0 0 10px #00d4ff;
  transition: top 0.3s ease;
}

.hour-label {
  position: absolute;
  bottom: -20px;
  font-size: 0.7rem;
  color: #7a9bb8;
}

.retention-section {
  padding: 1.5rem;
  background: rgba(30, 58, 95, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
}

.retention-section h3 {
  margin-bottom: 1rem;
  color: #00d4ff;
}

.retention-stats {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
}

.retention-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.retention-value {
  font-size: 2rem;
  font-weight: bold;
  color: #00ff88;
}

.retention-label {
  color: #7a9bb8;
  font-size: 0.9rem;
}

.funnel-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

@media (max-width: 768px) {
  .funnel-container {
    grid-template-columns: 1fr;
  }
}

.funnel-chart {
  padding: 1.5rem;
  background: rgba(30, 58, 95, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
}

.funnel-step {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
  gap: 1rem;
}

.funnel-label {
  width: 80px;
  color: #7a9bb8;
  font-size: 0.9rem;
  text-align: right;
}

.funnel-bar-container {
  flex: 1;
  height: 40px;
  display: flex;
  align-items: center;
}

.funnel-bar {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: width 0.5s ease;
  color: #fff;
  font-weight: bold;
}

.funnel-bar.blue { background: linear-gradient(90deg, #0066cc, #00d4ff); }
.funnel-bar.cyan { background: linear-gradient(90deg, #008b8b, #00ced1); }
.funnel-bar.teal { background: linear-gradient(90deg, #008080, #20b2aa); }
.funnel-bar.green { background: linear-gradient(90deg, #00aa55, #00ff88); }
.funnel-bar.lime { background: linear-gradient(90deg, #00cc66, #66ff99); }

.funnel-rate {
  width: 80px;
  color: #f59e0b;
  font-size: 0.85rem;
}

.funnel-insights {
  padding: 1.5rem;
  background: rgba(30, 58, 95, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
}

.funnel-insights h3 {
  margin-bottom: 1rem;
  color: #00d4ff;
}

.insight-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.8rem;
  color: #7a9bb8;
}

.insight-item strong {
  color: #00d4ff;
}

.forecast-stats {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.forecast-card {
  flex: 1;
  min-width: 200px;
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: rgba(30, 58, 95, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
}

.forecast-icon {
  font-size: 2rem;
}

.forecast-icon.up { color: #00ff88; }
.forecast-icon.down { color: #ef4444; }

.forecast-info {
  display: flex;
  flex-direction: column;
}

.forecast-value {
  font-size: 1.8rem;
  font-weight: bold;
  color: #00d4ff;
}

.forecast-label {
  color: #7a9bb8;
  font-size: 0.9rem;
}

.forecast-select {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  background: rgba(0, 0, 0, 0.3);
  color: #e8f4f8;
  border-radius: 6px;
}

.forecast-chart {
  padding: 1.5rem;
  background: rgba(30, 58, 95, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  margin-bottom: 1.5rem;
}

.forecast-chart h3 {
  margin-bottom: 1rem;
  color: #00d4ff;
}

.forecast-bars {
  display: flex;
  gap: 2rem;
}

.forecast-section {
  flex: 1;
}

.section-label {
  display: block;
  margin-bottom: 0.5rem;
  color: #7a9bb8;
  font-size: 0.9rem;
}

.bars-row {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 120px;
}

.forecast-bar {
  flex: 1;
  min-width: 8px;
  border-radius: 3px 3px 0 0;
  transition: height 0.5s ease;
}

.forecast-bar.historical {
  background: linear-gradient(180deg, #00d4ff, #0066cc);
}

.forecast-bar.forecast {
  background: linear-gradient(180deg, #00ff88, #00aa55);
  opacity: 0.8;
}

.forecast-legend {
  display: flex;
  gap: 2rem;
  justify-content: center;
  margin-top: 1rem;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #7a9bb8;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.dot.historical { background: #00d4ff; }
.dot.forecast { background: #00ff88; }

.forecast-insights {
  padding: 1.5rem;
  background: rgba(30, 58, 95, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
}

.forecast-insights h3 {
  margin-bottom: 1rem;
  color: #00d4ff;
}

.forecast-insights p {
  color: #7a9bb8;
  line-height: 1.8;
}

.forecast-insights strong {
  color: #00d4ff;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #7a9bb8;
}
</style>
