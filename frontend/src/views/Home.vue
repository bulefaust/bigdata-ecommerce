<template>
  <div class="home">
    <div class="banner-section" v-if="!searchMode">
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
import { ref, onMounted, watch, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getProducts, searchProducts, getHotProducts, getRandomProducts } from '../api'
import { useUserStore } from '../store/user'
import { useCartStore } from '../store/cart'

const props = defineProps({
  selectedCategory: String
})

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const hotProducts = ref([])
const randomProducts = ref([])
const products = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const searchMode = ref(false)
const searchKeyword = ref('')
const currentCat = ref('全部')
const productsRef = ref(null)

async function loadHot() {
  try { hotProducts.value = await getHotProducts(8) } catch { hotProducts.value = [] }
}

async function loadRandom() {
  try { randomProducts.value = await getRandomProducts(8) } catch { randomProducts.value = [] }
}

async function loadProducts() {
  try {
    const cat = currentCat.value === '全部' ? undefined : currentCat.value
    const res = await getProducts({ pageNum: pageNum.value, pageSize: pageSize.value, category: cat })
    products.value = res.records
    total.value = res.total
  } catch { products.value = [] }
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

watch(() => props.selectedCategory, (val) => {
  currentCat.value = val || '全部'
  pageNum.value = 1
  searchMode.value = false
  loadProducts()
})

watch(() => route.query.keyword, (val) => {
  if (val) {
    searchKeyword.value = val
    doSearch()
  }
})

onMounted(() => {
  loadHot()
  loadRandom()
  loadProducts()
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword
    doSearch()
  }
})
</script>

<style scoped>
.home {
  padding-bottom: 20px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.banner-section {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7a45 50%, #faad14 100%);
  padding: 0;
}

.banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40px 0;
}

.banner-content {
  color: #fff;
}

.banner-tag {
  display: inline-block;
  background: rgba(255,255,255,0.25);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 16px;
}

.banner-content h2 {
  font-size: 36px;
  font-weight: 900;
  line-height: 1.2;
  margin-bottom: 8px;
}

.banner-content p {
  font-size: 15px;
  opacity: 0.9;
  margin-bottom: 20px;
}

.banner-btn {
  padding: 10px 32px;
  background: #fff;
  color: var(--color-primary);
  border: none;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: transform 0.2s;
}

.banner-btn:hover {
  transform: scale(1.05);
}

.banner-stats {
  display: flex;
  gap: 32px;
}

.stat-item {
  text-align: center;
  color: #fff;
}

.stat-item strong {
  display: block;
  font-size: 28px;
  font-weight: 900;
}

.stat-item span {
  font-size: 12px;
  opacity: 0.8;
}

.section {
  margin-top: 24px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-head h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.total-text {
  font-size: 13px;
  color: var(--text-muted);
}

.btn-text {
  border: none;
  background: none;
  color: var(--color-blue);
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
  padding: 4px 8px;
}

.btn-text:hover {
  color: var(--color-primary);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.product-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.product-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
  border-color: var(--color-primary-light);
}

.card-img {
  position: relative;
  height: 180px;
  background: var(--bg-gray);
  overflow: hidden;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .card-img img {
  transform: scale(1.05);
}

.card-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: var(--color-primary);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 2px;
}

.card-tag.orange {
  background: var(--color-orange);
}

.card-info {
  padding: 10px 12px 12px;
}

.card-info h4 {
  font-size: 13px;
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
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.price {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
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
  margin-top: 24px;
  padding-bottom: 16px;
}

.pagination button {
  padding: 8px 20px;
  border: 1px solid var(--border-light);
  background: var(--bg-white);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-family: inherit;
  font-size: 13px;
  color: var(--text-secondary);
  transition: all 0.2s;
}

.pagination button:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination span {
  font-size: 13px;
  color: var(--text-muted);
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
  .banner {
    flex-direction: column;
    text-align: center;
    padding: 24px 0;
  }
  .banner-stats {
    margin-top: 16px;
  }
}
</style>
