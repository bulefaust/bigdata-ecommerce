<template>
  <div class="order-page">
    <div class="container">
      <h1 class="page-title">我的订单</h1>

      <div class="empty" v-if="!userStore.userId">
        <p>请先登录查看订单</p>
        <button class="btn-primary" @click="$router.push('/login')">去登录</button>
      </div>

      <div class="empty" v-else-if="!orders.length">
        <p>暂无订单记录</p>
        <button class="btn-primary" @click="$router.push('/')">去购物</button>
      </div>

      <div class="order-list" v-else>
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-head">
            <div class="order-left">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ order.createTime }}</span>
            </div>
            <span class="order-status" :class="'s' + order.status">{{ statusText(order.status) }}</span>
          </div>
          <div class="order-body">
            <span class="order-amount">¥{{ order.totalAmount.toLocaleString() }}</span>
            <div class="order-btns">
              <button class="btn-sm btn-primary-sm" v-if="order.status === 0" @click="payOrder(order)">立即支付</button>
              <button class="btn-sm" v-if="order.status === 3" @click="$router.push('/')">再次购买</button>
              <button class="btn-sm btn-danger-sm" @click="deleteOrder(order)">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrderList, deleteOrder as deleteOrderApi } from '../api'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const orders = ref([])

onMounted(async () => {
  if (userStore.userId) {
    try {
      const res = await getOrderList({ userId: userStore.userId, pageNum: 1, pageSize: 50 })
      orders.value = res.records || []
    } catch { orders.value = [] }
  }
})

function statusText(s) {
  return { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }[s] || '未知'
}

function payOrder(order) {
  order.status = 1
  ElMessage.success('支付成功！')
}

async function deleteOrder(order) {
  try {
    await deleteOrderApi(order.id)
    orders.value = orders.value.filter(o => o.id !== order.id)
    ElMessage.success('订单已删除')
  } catch { ElMessage.error('删除失败') }
}
</script>

<style scoped>
.order-page {
  padding: 32px 0 60px;
  background: var(--bg-body);
  min-height: 100vh;
  font-family: var(--font-body);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 32px;
}

.empty {
  text-align: center;
  padding: 80px 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}

.empty p {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.btn-primary {
  padding: 12px 36px;
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-xl);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow);
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 24px;
  transition: all var(--transition-base);
}

.order-card:hover {
  transform: translateY(-4px);
  background: var(--bg-card-hover);
  border-color: var(--border-default);
  box-shadow: 0 0 30px rgba(0, 212, 255, 0.08);
}

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-subtle);
}

.order-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.order-no {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.order-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.order-status {
  font-size: 12px;
  font-weight: 600;
  padding: 5px 14px;
  border-radius: var(--radius-xl);
}

.s0 { background: rgba(249, 115, 22, 0.1); color: var(--color-orange); border: 1px solid rgba(249, 115, 22, 0.2); }
.s1 { background: rgba(0, 212, 255, 0.1); color: var(--color-primary-light); border: 1px solid rgba(0, 212, 255, 0.2); }
.s2 { background: rgba(139, 92, 246, 0.1); color: #a78bfa; border: 1px solid rgba(139, 92, 246, 0.2); }
.s3 { background: rgba(16, 185, 129, 0.1); color: var(--color-green); border: 1px solid rgba(16, 185, 129, 0.2); }
.s4 { background: rgba(255, 255, 255, 0.03); color: var(--text-tertiary); border: 1px solid var(--border-subtle); }

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-amount {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-orange);
}

.order-btns {
  display: flex;
  gap: 8px;
}

.btn-sm {
  padding: 8px 20px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  background: transparent;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-sm:hover {
  border-color: var(--color-primary);
  color: var(--color-primary-light);
  transform: translateY(-2px);
  background: rgba(0, 212, 255, 0.06);
}

.btn-primary-sm {
  background: var(--gradient-primary);
  color: #fff;
  border-color: transparent;
}

.btn-primary-sm:hover {
  box-shadow: var(--shadow-glow);
  color: #fff;
}

.btn-danger-sm {
  border-color: rgba(239,68,68,0.3);
  color: var(--color-red);
}

.btn-danger-sm:hover {
  background: rgba(239,68,68,0.08);
  border-color: var(--color-red);
  color: var(--color-red);
}
</style>
