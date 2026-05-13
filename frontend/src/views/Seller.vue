<template>
  <div class="seller-page">
    <header class="seller-header">
      <router-link to="/" class="back-link">← 返回商城</router-link>
      <div class="seller-title-area">
        <h1>DataMall {{ isAdmin ? '管理后台' : '卖家中心' }}</h1>
        <div class="seller-badge"><span class="seller-dot"></span>{{ isAdmin ? 'ADMIN' : 'SELLER' }}</div>
      </div>
      <div class="seller-user">
        <span>{{ userStore.username }}</span>
        <span class="role-tag">{{ isAdmin ? '管理员' : '卖家' }}</span>
      </div>
    </header>

    <div class="seller-body">
      <nav class="seller-nav">
        <div v-for="tab in tabs" :key="tab.key" class="nav-item" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
          <span class="nav-icon">{{ tab.icon }}</span>
          <span class="nav-label">{{ tab.label }}</span>
        </div>
      </nav>

      <div class="seller-content">
        <div v-if="activeTab === 'overview'" class="content-panel">
          <h2>数据概览</h2>
          <div class="stat-grid">
            <div class="stat-card cyan">
              <div class="stat-icon">📦</div>
              <div class="stat-value">{{ stats.totalProducts || 0 }}</div>
              <div class="stat-label">商品总数</div>
            </div>
            <div class="stat-card green">
              <div class="stat-icon">✅</div>
              <div class="stat-value">{{ stats.onShelf || 0 }}</div>
              <div class="stat-label">在售商品</div>
            </div>
            <div class="stat-card orange">
              <div class="stat-icon">🛒</div>
              <div class="stat-value">{{ stats.cartCount || 0 }}</div>
              <div class="stat-label">加购次数</div>
            </div>
            <div class="stat-card purple">
              <div class="stat-icon">💳</div>
              <div class="stat-value">{{ stats.purchaseCount || 0 }}</div>
              <div class="stat-label">购买次数</div>
            </div>
            <div class="stat-card blue">
              <div class="stat-icon">👀</div>
              <div class="stat-value">{{ stats.viewCount || 0 }}</div>
              <div class="stat-label">浏览次数</div>
            </div>
            <div class="stat-card red">
              <div class="stat-icon">⏸️</div>
              <div class="stat-value">{{ stats.offShelf || 0 }}</div>
              <div class="stat-label">已下架</div>
            </div>
            <div class="stat-card yellow">
              <div class="stat-icon">⏳</div>
              <div class="stat-value">{{ stats.pendingReview || 0 }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'products'" class="content-panel">
          <div class="panel-head">
            <h2>我的商品</h2>
            <button class="btn-add" @click="showAddForm = true">+ 提交新商品</button>
          </div>
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>商品名</th>
                <th>分类</th>
                <th>价格</th>
                <th>库存</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in products" :key="p.id">
                <td>{{ p.id }}</td>
                <td class="name-cell">{{ p.name }}</td>
                <td>{{ p.category }}</td>
                <td class="price-cell">¥{{ p.price?.toLocaleString() }}</td>
                <td :class="{ 'low-stock': p.stock < 20 }">{{ p.stock }}</td>
                <td>
                  <span class="status-tag" :class="statusClass(p.status)">
                    {{ statusText(p.status) }}
                  </span>
                </td>
                <td class="action-cell">
                  <button class="btn-sm btn-edit" @click="openEdit(p)">编辑</button>
                  <button v-if="p.status === 1" class="btn-sm btn-warn" @click="toggleStatus(p, 0)">下架</button>
                  <button v-if="p.status === 0" class="btn-sm btn-ok" @click="toggleStatus(p, 1)">上架</button>
                  <button v-if="p.status === 2" class="btn-sm btn-disabled" disabled>审核中</button>
                  <button class="btn-sm btn-del" @click="deleteProduct(p)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="activeTab === 'behavior'" class="content-panel">
          <h2>用户行为分析</h2>
          <p class="panel-desc">查看用户对你商品的浏览、加购和购买数据</p>
          <table class="data-table">
            <thead>
              <tr>
                <th>商品名</th>
                <th>分类</th>
                <th>价格</th>
                <th>浏览</th>
                <th>加购</th>
                <th>购买</th>
                <th>转化率</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in behaviorStats" :key="item.productId">
                <td class="name-cell">{{ item.productName }}</td>
                <td>{{ item.category }}</td>
                <td class="price-cell">¥{{ item.price?.toLocaleString() }}</td>
                <td>{{ item.views }}</td>
                <td class="cart-cell">{{ item.carts }}</td>
                <td class="buy-cell">{{ item.purchases }}</td>
                <td>
                  <div class="conversion">
                    <div class="conv-bar">
                      <div class="conv-fill" :style="{ width: convRate(item) + '%' }"></div>
                    </div>
                    <span class="conv-text">{{ convRate(item) }}%</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-if="showAddForm" class="modal-overlay" @click.self="showAddForm = false">
      <div class="modal-card">
        <h3>提交新商品</h3>
        <div class="form-grid">
          <div class="field">
            <label>商品名称</label>
            <input v-model="newProduct.name" placeholder="请输入商品名称" />
          </div>
          <div class="field">
            <label>分类</label>
            <select v-model="newProduct.category">
              <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
            </select>
          </div>
          <div class="field">
            <label>价格</label>
            <input v-model.number="newProduct.price" type="number" placeholder="0.00" />
          </div>
          <div class="field">
            <label>库存</label>
            <input v-model.number="newProduct.stock" type="number" placeholder="0" />
          </div>
          <div class="field full">
            <label>描述</label>
            <input v-model="newProduct.description" placeholder="商品描述" />
          </div>
          <div class="field full">
            <label>图片URL</label>
            <input v-model="newProduct.imageUrl" placeholder="https://..." />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showAddForm = false">取消</button>
          <button class="btn-submit" @click="addProduct">提交审核</button>
        </div>
      </div>
    </div>

    <div v-if="showEditForm" class="modal-overlay" @click.self="showEditForm = false">
      <div class="modal-card">
        <h3>编辑商品</h3>
        <div class="form-grid">
          <div class="field">
            <label>商品名称</label>
            <input v-model="editProduct.name" placeholder="请输入商品名称" />
          </div>
          <div class="field">
            <label>分类</label>
            <select v-model="editProduct.category">
              <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
            </select>
          </div>
          <div class="field">
            <label>价格</label>
            <input v-model.number="editProduct.price" type="number" placeholder="0.00" />
          </div>
          <div class="field">
            <label>库存</label>
            <input v-model.number="editProduct.stock" type="number" placeholder="0" />
          </div>
          <div class="field full">
            <label>描述</label>
            <input v-model="editProduct.description" placeholder="商品描述" />
          </div>
          <div class="field full">
            <label>图片URL</label>
            <input v-model="editProduct.imageUrl" placeholder="https://..." />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showEditForm = false">取消</button>
          <button class="btn-submit" @click="saveEdit">保存修改</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.role === 'admin')
const activeTab = ref('overview')
const showAddForm = ref(false)
const showEditForm = ref(false)
const editProduct = reactive({ id: null, name: '', category: '手机', price: null, stock: null, description: '', imageUrl: '' })

const tabs = [
  { key: 'overview', icon: '📊', label: '数据概览' },
  { key: 'products', icon: '📦', label: '商品管理' },
  { key: 'behavior', icon: '📈', label: '行为分析' },
]

const categories = ['手机', '电脑', '耳机', '平板', '穿戴', '游戏', '摄影', '配件']

const stats = reactive({ totalProducts: 0, onShelf: 0, offShelf: 0, cartCount: 0, purchaseCount: 0, viewCount: 0 })
const products = ref([])
const behaviorStats = ref([])

const newProduct = reactive({
  name: '', category: '手机', price: null, stock: null, description: '', imageUrl: ''
})

function convRate(item) {
  if (!item.views) return 0
  return Math.round((item.purchases / item.views) * 100)
}

function statusClass(status) {
  if (status === 1) return 'on'
  if (status === 2) return 'pending'
  return 'off'
}

function statusText(status) {
  if (status === 1) return '在售'
  if (status === 2) return '待审核'
  return '已下架'
}

async function loadStats() {
  try {
    const res = await request.get('/seller/stats', { params: { userId: userStore.userId } })
    Object.assign(stats, res)
  } catch {}
}

async function loadProducts() {
  try {
    products.value = await request.get('/seller/products', { params: { userId: userStore.userId } })
  } catch { products.value = [] }
}

async function loadBehavior() {
  try {
    behaviorStats.value = await request.get('/seller/product-behavior', { params: { userId: userStore.userId } })
  } catch { behaviorStats.value = [] }
}

async function toggleStatus(product, status) {
  try {
    await request.put(`/seller/product/${product.id}/status`, null, { params: { status, userId: userStore.userId } })
    product.status = status
    ElMessage.success(status === 1 ? '已上架' : '已下架')
    loadStats()
  } catch { ElMessage.error('操作失败') }
}

async function deleteProduct(product) {
  try {
    await request.delete(`/seller/product/${product.id}`, { params: { userId: userStore.userId } })
    products.value = products.value.filter(p => p.id !== product.id)
    ElMessage.success('已删除')
    loadStats()
  } catch { ElMessage.error('删除失败') }
}

async function addProduct() {
  if (!newProduct.name || !newProduct.price) {
    ElMessage.warning('请填写商品名称和价格')
    return
  }
  try {
    await request.post('/seller/product', {
      ...newProduct,
      sellerId: userStore.userId,
      status: 2
    })
    showAddForm.value = false
    ElMessage.success('已提交审核，请等待管理员审批')
    Object.assign(newProduct, { name: '', category: '手机', price: null, stock: null, description: '', imageUrl: '' })
    loadProducts()
    loadStats()
  } catch { ElMessage.error('上架失败') }
}

function openEdit(p) {
  Object.assign(editProduct, {
    id: p.id,
    name: p.name,
    category: p.category,
    price: p.price,
    stock: p.stock,
    description: p.description || '',
    imageUrl: p.imageUrl || ''
  })
  showEditForm.value = true
}

async function saveEdit() {
  if (!editProduct.name || !editProduct.price) {
    ElMessage.warning('请填写商品名称和价格')
    return
  }
  try {
    await request.put('/seller/product', {
      id: editProduct.id,
      name: editProduct.name,
      category: editProduct.category,
      price: editProduct.price,
      stock: editProduct.stock,
      description: editProduct.description,
      imageUrl: editProduct.imageUrl
    }, { params: { userId: userStore.userId } })
    showEditForm.value = false
    ElMessage.success('修改成功')
    loadProducts()
    loadStats()
  } catch { ElMessage.error('修改失败') }
}

onMounted(() => {
  loadStats()
  loadProducts()
  loadBehavior()
})
</script>

<style scoped>
.seller-page {
  min-height: 100vh;
  background: var(--bg-body);
  color: var(--text-primary);
  font-family: var(--font-body);
}

.seller-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 28px;
  background: var(--bg-surface);
  border-bottom: 1px solid var(--border-default);
  box-shadow: 0 2px 16px rgba(0,212,255,0.06);
}

.seller-title-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.seller-header h1 {
  font-size: 20px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2px;
}

.seller-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  background: rgba(16,185,129,0.12);
  border: 1px solid rgba(16,185,129,0.3);
  border-radius: 12px;
  padding: 2px 10px;
  font-size: 11px;
  font-weight: 700;
  color: var(--color-green-light);
  letter-spacing: 1px;
}

.seller-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-green);
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

.seller-user {
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

.seller-body {
  display: flex;
  min-height: calc(100vh - 60px);
}

.seller-nav {
  width: 180px;
  background: var(--bg-surface);
  border-right: 1px solid var(--border-subtle);
  padding: 20px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 14px;
  color: var(--text-tertiary);
  transition: all var(--transition-fast);
  font-weight: 500;
}

.nav-item:hover {
  background: rgba(0,212,255,0.05);
  color: var(--text-secondary);
}

.nav-item.active {
  background: rgba(0,212,255,0.1);
  color: var(--color-primary);
  border: 1px solid rgba(0,212,255,0.15);
}

.nav-icon { font-size: 16px; }

.seller-content {
  flex: 1;
  padding: 28px;
  overflow-y: auto;
}

.content-panel h2 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-head h2 { margin-bottom: 0; }

.panel-desc {
  color: var(--text-secondary);
  margin-bottom: 20px;
  font-size: 14px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 24px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
}

.stat-card.cyan::before { background: var(--color-primary); }
.stat-card.green::before { background: var(--color-green); }
.stat-card.orange::before { background: var(--color-orange); }
.stat-card.purple::before { background: var(--color-accent); }
.stat-card.blue::before { background: var(--color-primary-light); }
.stat-card.red::before { background: var(--color-red); }
.stat-card.yellow::before { background: var(--color-orange); }

.stat-icon { font-size: 28px; margin-bottom: 8px; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  margin-bottom: 4px;
}

.stat-card.cyan .stat-value { color: var(--color-primary); }
.stat-card.green .stat-value { color: var(--color-green); }
.stat-card.orange .stat-value { color: var(--color-orange); }
.stat-card.purple .stat-value { color: var(--color-accent-light); }
.stat-card.blue .stat-value { color: var(--color-primary-light); }
.stat-card.red .stat-value { color: var(--color-red); }
.stat-card.yellow .stat-value { color: var(--color-orange); }

.stat-label { font-size: 13px; color: var(--text-tertiary); }

.btn-add {
  padding: 8px 20px;
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: var(--font-body);
  transition: all var(--transition-base);
  box-shadow: 0 2px 12px rgba(0,212,255,0.2);
}

.btn-add:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(0,212,255,0.3);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table th {
  text-align: left;
  padding: 12px 16px;
  background: var(--bg-surface);
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 12px;
  border-bottom: 1px solid var(--border-default);
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
  color: var(--text-secondary);
}

.data-table tr:hover td { background: rgba(0,212,255,0.03); }

.name-cell {
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary) !important;
}

.price-cell { color: var(--color-orange) !important; font-weight: 600; }
.cart-cell { color: var(--color-orange) !important; font-weight: 600; }
.buy-cell { color: var(--color-green) !important; font-weight: 600; }
.low-stock { color: var(--color-red) !important; font-weight: 600; }

.status-tag {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
}

.status-tag.on {
  background: rgba(16,185,129,0.1);
  color: var(--color-green);
  border: 1px solid rgba(16,185,129,0.2);
}

.status-tag.off {
  background: rgba(239,68,68,0.08);
  color: var(--color-red);
  border: 1px solid rgba(239,68,68,0.15);
}

.status-tag.pending {
  background: rgba(245,158,11,0.08);
  color: var(--color-orange);
  border: 1px solid rgba(245,158,11,0.2);
}

.action-cell { display: flex; gap: 6px; }

.btn-sm {
  padding: 4px 14px;
  font-size: 12px;
  border: 1px solid var(--border-default);
  background: var(--bg-card);
  color: var(--text-secondary);
  border-radius: 6px;
  cursor: pointer;
  font-family: var(--font-body);
  transition: all var(--transition-fast);
}

.btn-sm:hover { background: var(--bg-card-hover); }

.btn-sm.btn-ok {
  border-color: rgba(16,185,129,0.3);
  color: var(--color-green);
  background: rgba(16,185,129,0.06);
}

.btn-sm.btn-ok:hover { background: rgba(16,185,129,0.12); }

.btn-sm.btn-warn {
  border-color: rgba(245,158,11,0.3);
  color: var(--color-orange);
  background: rgba(245,158,11,0.06);
}

.btn-sm.btn-warn:hover { background: rgba(245,158,11,0.12); }

.btn-sm.btn-del {
  border-color: rgba(239,68,68,0.2);
  color: var(--color-red);
  background: rgba(239,68,68,0.04);
}

.btn-sm.btn-del:hover { background: rgba(239,68,68,0.1); }

.btn-sm.btn-edit {
  border-color: rgba(0,212,255,0.3);
  color: var(--color-primary);
  background: rgba(0,212,255,0.06);
}

.btn-sm.btn-edit:hover { background: rgba(0,212,255,0.12); }

.btn-sm.btn-disabled {
  border-color: var(--border-subtle);
  color: var(--text-tertiary);
  background: var(--bg-elevated);
  cursor: not-allowed;
  opacity: 0.6;
}

.conversion {
  display: flex;
  align-items: center;
  gap: 8px;
}

.conv-bar {
  flex: 1;
  height: 6px;
  background: rgba(255,255,255,0.06);
  border-radius: 3px;
  overflow: hidden;
  max-width: 80px;
}

.conv-fill {
  height: 100%;
  background: var(--gradient-primary);
  border-radius: 3px;
  transition: width 0.5s;
}

.conv-text {
  font-size: 11px;
  color: var(--color-primary);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  min-width: 32px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-card {
  background: var(--bg-elevated);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-xl);
  padding: 32px;
  width: 560px;
  max-width: 90vw;
  box-shadow: var(--shadow-lg);
}

.modal-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 24px;
  color: var(--text-primary);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.field { display: flex; flex-direction: column; gap: 6px; }
.field.full { grid-column: 1 / -1; }

.field label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.field input, .field select {
  padding: 10px 14px;
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  font-size: 14px;
  font-family: var(--font-body);
  outline: none;
  transition: border-color var(--transition-fast);
}

.field input:focus, .field select:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(0,212,255,0.1);
}

.field select { cursor: pointer; }
.field select option { background: var(--bg-elevated); }

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.btn-cancel {
  padding: 10px 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  color: var(--text-secondary);
  border-radius: var(--radius-md);
  cursor: pointer;
  font-family: var(--font-body);
  font-size: 14px;
  transition: all var(--transition-fast);
}

.btn-cancel:hover { background: var(--bg-card-hover); }

.btn-submit {
  padding: 10px 24px;
  background: var(--gradient-primary);
  border: none;
  color: #fff;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-family: var(--font-body);
  font-size: 14px;
  font-weight: 600;
  transition: all var(--transition-base);
  box-shadow: 0 2px 12px rgba(0,212,255,0.2);
}

.btn-submit:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(0,212,255,0.3);
}

@media (max-width: 900px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .seller-nav { width: 60px; }
  .nav-label { display: none; }
  .nav-item { justify-content: center; padding: 12px; }
}
</style>
