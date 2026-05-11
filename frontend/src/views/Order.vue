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
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrderList } from '../api'
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
</script>

<style scoped>
.order-page {
  padding: 20px 0 40px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
}

.empty {
  text-align: center;
  padding: 60px 20px;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
}

.empty p {
  font-size: 16px;
  color: var(--text-muted);
  margin-bottom: 20px;
}

.btn-primary {
  padding: 10px 32px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-lg);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 16px 20px;
  box-shadow: var(--shadow-sm);
}

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
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
  color: var(--text-muted);
}

.order-status {
  font-size: 13px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: var(--radius-sm);
}

.s0 { background: var(--color-gold-light); color: var(--color-orange); }
.s1 { background: var(--color-blue-light); color: var(--color-blue); }
.s2 { background: var(--color-blue-light); color: var(--color-blue); }
.s3 { background: var(--color-green-light); color: var(--color-green); }
.s4 { background: var(--bg-gray); color: var(--text-muted); }

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-amount {
  font-size: 22px;
  font-weight: 900;
  color: var(--color-primary);
}

.order-btns {
  display: flex;
  gap: 8px;
}

.btn-sm {
  padding: 6px 16px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  background: var(--bg-white);
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}

.btn-sm:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.btn-primary-sm {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}

.btn-primary-sm:hover {
  background: var(--color-primary-dark);
  color: #fff;
}
</style>
