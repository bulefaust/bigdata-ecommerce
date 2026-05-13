<template>
  <div class="admin-page">
    <header class="admin-header">
      <router-link to="/" class="back-link">← 返回商城</router-link>
      <div class="admin-title-area">
        <h1>DataMall 管理后台</h1>
        <div class="admin-badge"><span class="admin-dot"></span>ADMIN</div>
      </div>
      <div class="admin-user">
        <span>{{ userStore.username }}</span>
        <span class="role-tag">管理员</span>
      </div>
    </header>

    <div class="admin-body">
      <nav class="admin-nav">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="nav-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          <span class="nav-icon">{{ tab.icon }}</span>
          <span class="nav-label">{{ tab.label }}</span>
        </div>
      </nav>

      <div class="admin-content">
        <div v-if="activeTab === 'overview'" class="content-panel">
          <h2>系统概览</h2>
          <div class="stat-grid">
            <div class="stat-card cyan">
              <div class="stat-icon">📦</div>
              <div class="stat-value">{{ stats.productCount }}</div>
              <div class="stat-label">商品总数</div>
            </div>
            <div class="stat-card purple">
              <div class="stat-icon">👥</div>
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">注册用户</div>
            </div>
            <div class="stat-card green">
              <div class="stat-icon">🛒</div>
              <div class="stat-value">{{ stats.orderCount }}</div>
              <div class="stat-label">订单总数</div>
            </div>
            <div class="stat-card orange">
              <div class="stat-icon">💰</div>
              <div class="stat-value">¥{{ stats.totalRevenue }}</div>
              <div class="stat-label">总销售额</div>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'users'" class="content-panel">
          <h2>用户管理</h2>
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>用户名</th>
                <th>角色</th>
                <th>注册时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.username }}</td>
                <td>
                  <span class="role-badge" :class="user.role === 'admin' ? 'admin' : 'user'">
                    {{ user.role === 'admin' ? '管理员' : '普通用户' }}
                  </span>
                </td>
                <td>{{ user.createTime }}</td>
                <td>
                  <button
                    class="btn-sm"
                    :class="{ 'btn-danger': user.role === 'admin' }"
                    @click="toggleRole(user)"
                    :disabled="user.username === 'admin'"
                  >
                    {{ user.role === 'admin' ? '降为用户' : '升为管理员' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="activeTab === 'products'" class="content-panel">
          <h2>商品管理</h2>
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
                  <span class="status-badge" :class="productStatusClass(p.status)">{{ productStatusText(p.status) }}</span>
                </td>
                <td>
                  <button class="btn-sm btn-edit" @click="openEditProduct(p)">编辑</button>
                  <button class="btn-sm btn-del" @click="deleteProduct(p)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="pagination" v-if="productTotal > productPageSize">
            <button :disabled="productPage <= 1" @click="productPage--; loadProducts()">上一页</button>
            <span>{{ productPage }} / {{ Math.ceil(productTotal / productPageSize) }}</span>
            <button :disabled="productPage >= Math.ceil(productTotal / productPageSize)" @click="productPage++; loadProducts()">下一页</button>
          </div>
        </div>

        <div v-if="activeTab === 'orders'" class="content-panel">
          <h2>订单管理</h2>
          <table class="data-table">
            <thead>
              <tr>
                <th>订单ID</th>
                <th>用户ID</th>
                <th>金额</th>
                <th>状态</th>
                <th>创建时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="o in orders" :key="o.id">
                <td>{{ o.id }}</td>
                <td>{{ o.userId }}</td>
                <td class="price-cell">¥{{ o.totalAmount?.toLocaleString() }}</td>
                <td>
                  <span class="status-badge" :class="'status-' + o.status">{{ orderStatusText(o.status) }}</span>
                </td>
                <td>{{ o.createTime }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="activeTab === 'dashboard'" class="content-panel">
          <h2>数据大屏</h2>
          <p class="panel-desc">点击下方按钮进入实时数据大屏</p>
          <router-link to="/dashboard" class="btn-goto-dashboard">
            进入数据大屏 →
          </router-link>
        </div>
      </div>
    </div>

    <div v-if="showEditProduct" class="modal-overlay" @click.self="showEditProduct = false">
      <div class="modal-card">
        <h3>编辑商品</h3>
        <div class="form-grid">
          <div class="field">
            <label>商品名称</label>
            <input v-model="editProductData.name" placeholder="请输入商品名称" />
          </div>
          <div class="field">
            <label>分类</label>
            <select v-model="editProductData.category">
              <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
            </select>
          </div>
          <div class="field">
            <label>价格</label>
            <input v-model.number="editProductData.price" type="number" placeholder="0.00" />
          </div>
          <div class="field">
            <label>库存</label>
            <input v-model.number="editProductData.stock" type="number" placeholder="0" />
          </div>
          <div class="field full">
            <label>描述</label>
            <input v-model="editProductData.description" placeholder="商品描述" />
          </div>
          <div class="field full">
            <label>图片URL</label>
            <input v-model="editProductData.imageUrl" placeholder="https://..." />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showEditProduct = false">取消</button>
          <button class="btn-submit" @click="saveEditProduct">保存修改</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import { getProducts, getOrderList } from '../api'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const userStore = useUserStore()
const activeTab = ref('overview')
const showEditProduct = ref(false)
const categories = ['手机', '电脑', '耳机', '平板', '穿戴', '游戏', '摄影', '配件']
const editProductData = reactive({ id: null, name: '', category: '手机', price: null, stock: null, description: '', imageUrl: '' })

const tabs = [
  { key: 'overview', icon: '📊', label: '系统概览' },
  { key: 'users', icon: '👥', label: '用户管理' },
  { key: 'products', icon: '📦', label: '商品管理' },
  { key: 'orders', icon: '🛒', label: '订单管理' },
  { key: 'dashboard', icon: '📈', label: '数据大屏' },
]

const stats = reactive({
  productCount: 0,
  userCount: 0,
  orderCount: 0,
  totalRevenue: '0',
})

const users = ref([])
const products = ref([])
const orders = ref([])
const productPage = ref(1)
const productPageSize = ref(20)
const productTotal = ref(0)

function orderStatusText(s) {
  const map = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }
  return map[s] ?? '未知'
}

async function loadStats() {
  try {
    const [ov] = await Promise.all([
      request.get('/dashboard/overview'),
    ])
    stats.productCount = ov.totalProducts || 0
    stats.orderCount = ov.totalOrders || 0
    stats.totalRevenue = ov.totalRevenue ? Number(ov.totalRevenue).toLocaleString() : '0'
  } catch {}
  try {
    const res = await request.get('/user/list')
    stats.userCount = res.length || res.total || 0
  } catch {}
}

async function loadUsers() {
  try {
    const res = await request.get('/user/list')
    users.value = Array.isArray(res) ? res : []
  } catch { users.value = [] }
}

async function loadProducts() {
  try {
    const res = await getProducts({ pageNum: productPage.value, pageSize: productPageSize.value })
    products.value = res.records || []
    productTotal.value = res.total || 0
  } catch { products.value = [] }
}

async function loadOrders() {
  try {
    const res = await getOrderList({ pageNum: 1, pageSize: 50 })
    orders.value = res.records || Array.isArray(res) ? res : []
  } catch { orders.value = [] }
}

async function toggleRole(user) {
  try {
    await request.post('/user/update-role', { id: user.id, role: user.role === 'admin' ? 'user' : 'admin' })
    user.role = user.role === 'admin' ? 'user' : 'admin'
  } catch {}
}

function productStatusClass(status) {
  if (status === 1) return 'status-1'
  if (status === 2) return 'status-pending'
  return 'status-0'
}

function productStatusText(status) {
  if (status === 1) return '在售'
  if (status === 2) return '待审核'
  return '已下架'
}

function openEditProduct(p) {
  Object.assign(editProductData, {
    id: p.id,
    name: p.name,
    category: p.category,
    price: p.price,
    stock: p.stock,
    description: p.description || '',
    imageUrl: p.imageUrl || ''
  })
  showEditProduct.value = true
}

async function saveEditProduct() {
  if (!editProductData.name || !editProductData.price) {
    ElMessage.warning('请填写商品名称和价格')
    return
  }
  try {
    await request.put('/seller/product', {
      id: editProductData.id,
      name: editProductData.name,
      category: editProductData.category,
      price: editProductData.price,
      stock: editProductData.stock,
      description: editProductData.description,
      imageUrl: editProductData.imageUrl
    }, { params: { userId: userStore.userId } })
    showEditProduct.value = false
    ElMessage.success('修改成功')
    loadProducts()
  } catch { ElMessage.error('修改失败') }
}

async function deleteProduct(p) {
  try {
    await request.delete(`/seller/product/${p.id}`, { params: { userId: userStore.userId } })
    ElMessage.success('已删除')
    loadProducts()
  } catch { ElMessage.error('删除失败') }
}

onMounted(() => {
  loadStats()
  loadUsers()
  loadProducts()
  loadOrders()
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: var(--bg-body);
  color: var(--text-primary);
  font-family: var(--font-body);
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 28px;
  background: var(--bg-surface);
  border-bottom: 1px solid var(--border-default);
  box-shadow: 0 2px 16px rgba(0,212,255,0.06);
}

.admin-title-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-header h1 {
  font-size: 20px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2px;
}

.admin-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  background: rgba(124,58,237,0.12);
  border: 1px solid rgba(124,58,237,0.3);
  border-radius: 12px;
  padding: 2px 10px;
  font-size: 11px;
  font-weight: 700;
  color: var(--color-accent-light);
  letter-spacing: 1px;
}

.admin-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-accent);
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

.admin-user {
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
  letter-spacing: 0.5px;
}

.admin-body {
  display: flex;
  min-height: calc(100vh - 60px);
}

.admin-nav {
  width: 200px;
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

.nav-icon {
  font-size: 16px;
}

.admin-content {
  flex: 1;
  padding: 28px;
  overflow-y: auto;
}

.content-panel h2 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--text-primary);
}

.panel-desc {
  color: var(--text-secondary);
  margin-bottom: 20px;
  font-size: 14px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
.stat-card.purple::before { background: var(--color-accent); }
.stat-card.green::before { background: var(--color-green); }
.stat-card.orange::before { background: var(--color-orange); }

.stat-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  margin-bottom: 4px;
}

.stat-card.cyan .stat-value { color: var(--color-primary); }
.stat-card.purple .stat-value { color: var(--color-accent-light); }
.stat-card.green .stat-value { color: var(--color-green); }
.stat-card.orange .stat-value { color: var(--color-orange); }

.stat-label {
  font-size: 13px;
  color: var(--text-tertiary);
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
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--border-default);
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-subtle);
  color: var(--text-secondary);
}

.data-table tr:hover td {
  background: rgba(0,212,255,0.03);
}

.name-cell {
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary) !important;
}

.price-cell {
  color: var(--color-orange) !important;
  font-weight: 600;
}

.low-stock {
  color: var(--color-red) !important;
  font-weight: 600;
}

.role-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
}

.role-badge.admin {
  background: rgba(124,58,237,0.12);
  color: var(--color-accent-light);
  border: 1px solid rgba(124,58,237,0.2);
}

.role-badge.user {
  background: rgba(0,212,255,0.08);
  color: var(--color-primary);
  border: 1px solid rgba(0,212,255,0.15);
}

.status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
}

.status-0 { background: rgba(245,158,11,0.12); color: var(--color-orange); }
.status-1 { background: rgba(0,212,255,0.1); color: var(--color-primary); }
.status-2 { background: rgba(16,185,129,0.1); color: var(--color-green); }
.status-3 { background: rgba(16,185,129,0.12); color: var(--color-green-light); }
.status-4 { background: rgba(239,68,68,0.1); color: var(--color-red); }

.btn-sm {
  padding: 4px 14px;
  font-size: 12px;
  border: 1px solid rgba(0,212,255,0.2);
  background: rgba(0,212,255,0.06);
  color: var(--color-primary);
  border-radius: 6px;
  cursor: pointer;
  font-family: var(--font-body);
  transition: all var(--transition-fast);
}

.btn-sm:hover {
  background: rgba(0,212,255,0.12);
  border-color: var(--color-primary);
}

.btn-sm.btn-danger {
  border-color: rgba(239,68,68,0.2);
  background: rgba(239,68,68,0.06);
  color: var(--color-red);
}

.btn-sm.btn-danger:hover {
  background: rgba(239,68,68,0.12);
  border-color: var(--color-red);
}

.btn-sm:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.btn-sm.btn-edit {
  border-color: rgba(0,212,255,0.3);
  color: var(--color-primary);
  background: rgba(0,212,255,0.06);
}

.btn-sm.btn-edit:hover {
  background: rgba(0,212,255,0.12);
  border-color: var(--color-primary);
}

.btn-sm.btn-del {
  border-color: rgba(239,68,68,0.2);
  color: var(--color-red);
  background: rgba(239,68,68,0.04);
}

.btn-sm.btn-del:hover {
  background: rgba(239,68,68,0.1);
}

.status-pending {
  background: rgba(245,158,11,0.12);
  color: var(--color-orange);
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
  box-shadow: 0 8px 32px rgba(0,0,0,0.3);
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

.btn-goto-dashboard {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: var(--gradient-primary);
  color: #fff;
  border-radius: var(--radius-md);
  text-decoration: none;
  font-size: 15px;
  font-weight: 600;
  transition: all var(--transition-base);
  box-shadow: 0 4px 16px rgba(0,212,255,0.25);
}

.btn-goto-dashboard:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(0,212,255,0.35);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}

.pagination button {
  padding: 8px 24px;
  border: 1px solid var(--border-default);
  background: var(--bg-card);
  border-radius: 10px;
  cursor: pointer;
  font-family: var(--font-body);
  font-size: 13px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
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
  font-variant-numeric: tabular-nums;
}

@media (max-width: 900px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .admin-nav {
    width: 60px;
  }
  .nav-label {
    display: none;
  }
  .nav-item {
    justify-content: center;
    padding: 12px;
  }
}
</style>
