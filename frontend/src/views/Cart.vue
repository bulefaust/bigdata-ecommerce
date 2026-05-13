<template>
  <div class="cart-page">
    <div class="container">
      <h1 class="page-title">我的购物车</h1>

      <div class="empty" v-if="!cartStore.items.length">
        <p>🛒 购物车还是空的，快去挑选商品吧！</p>
        <button class="btn-primary" @click="$router.push('/')">去购物</button>
      </div>

      <template v-else>
        <div class="cart-table">
          <div class="cart-header">
            <span class="col-product">商品信息</span>
            <span class="col-price">单价</span>
            <span class="col-qty">数量</span>
            <span class="col-subtotal">小计</span>
            <span class="col-action">操作</span>
          </div>

          <div v-for="item in cartStore.items" :key="item.id" class="cart-item">
            <div class="col-product">
              <div class="item-img" @click="$router.push(`/product/${item.id}`)">
                <img :src="item.imageUrl" v-if="item.imageUrl" />
              </div>
              <div class="item-text">
                <h4 @click="$router.push(`/product/${item.id}`)">{{ item.name }}</h4>
                <span class="item-cat">{{ item.category }}</span>
              </div>
            </div>
            <div class="col-price">¥{{ item.price.toLocaleString() }}</div>
            <div class="col-qty">
              <div class="qty-box">
                <button @click="cartStore.updateQuantity(item.id, item.quantity - 1)">−</button>
                <span>{{ item.quantity }}</span>
                <button @click="cartStore.updateQuantity(item.id, item.quantity + 1)">+</button>
              </div>
            </div>
            <div class="col-subtotal">¥{{ (item.price * item.quantity).toLocaleString() }}</div>
            <div class="col-action">
              <button class="btn-del" @click="cartStore.removeItem(item.id)">删除</button>
            </div>
          </div>
        </div>

        <div class="cart-footer">
          <button class="btn-clear" @click="cartStore.clear()">清空购物车</button>
          <div class="cart-total">
            <span>共 <strong>{{ cartStore.totalCount }}</strong> 件商品</span>
            <span class="total-label">合计：</span>
            <span class="total-price">¥{{ cartStore.totalAmount.toLocaleString() }}</span>
            <button class="btn-checkout" @click="checkout">去结算</button>
          </div>
        </div>

        <div class="recommend" v-if="recommendProducts.length">
          <h3>猜你需要</h3>
          <div class="recommend-grid">
            <div v-for="item in recommendProducts" :key="item.id" class="recommend-card" @click="$router.push(`/product/${item.id}`)">
              <div class="recommend-img">
                <img :src="item.imageUrl" v-if="item.imageUrl" loading="lazy" />
              </div>
              <div class="recommend-info">
                <h4>{{ item.name }}</h4>
                <span>¥{{ item.price.toLocaleString() }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../store/cart'
import { useUserStore } from '../store/user'
import { createOrder, getRandomProducts } from '../api'
import { trackPurchase } from '../tracker'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const recommendProducts = ref([])

onMounted(async () => {
  try { recommendProducts.value = await getRandomProducts(5) } catch { recommendProducts.value = [] }
})

async function checkout() {
  if (!userStore.userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    for (const item of cartStore.items) {
      await createOrder(userStore.userId, item.id, item.quantity)
      trackPurchase(item.id, item.category)
    }
    cartStore.clear()
    ElMessage.success('全部下单成功！')
    router.push('/order')
  } catch {
    ElMessage.error('下单失败')
  }
}
</script>

<style scoped>
.cart-page {
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
  letter-spacing: 0.02em;
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

.cart-table {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.cart-header {
  display: flex;
  align-items: center;
  padding: 14px 24px;
  background: var(--bg-elevated);
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.col-product { flex: 2; min-width: 0; }
.col-price { width: 100px; text-align: center; }
.col-qty { width: 120px; text-align: center; }
.col-subtotal { width: 100px; text-align: center; }
.col-action { width: 60px; text-align: center; }

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-subtle);
  transition: all var(--transition-base);
}

.cart-item:hover {
  background: var(--bg-card-hover);
}

.cart-item:last-child {
  border-bottom: none;
}

.col-product {
  display: flex;
  align-items: center;
  gap: 16px;
}

.item-img {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  cursor: pointer;
  flex-shrink: 0;
  transition: all var(--transition-base);
}

.item-img:hover {
  transform: translateY(-2px);
  border-color: var(--border-default);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.08);
}

.item-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-text h4 {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
  transition: color var(--transition-base);
}

.item-text h4:hover {
  color: var(--color-primary-light);
}

.item-cat {
  font-size: 12px;
  color: var(--text-tertiary);
}

.col-price {
  font-size: 14px;
  color: var(--text-secondary);
  text-align: center;
  font-weight: 400;
}

.qty-box {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  background: var(--bg-elevated);
}

.qty-box button {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 14px;
  cursor: pointer;
  color: var(--color-primary-light);
  transition: all var(--transition-fast);
  font-weight: 600;
}

.qty-box button:hover {
  background: rgba(0, 212, 255, 0.12);
}

.qty-box span {
  width: 36px;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  line-height: 32px;
  color: var(--text-primary);
}

.col-subtotal {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-orange);
  text-align: center;
}

.btn-del {
  border: none;
  background: none;
  color: var(--text-tertiary);
  font-size: 13px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-base);
}

.btn-del:hover {
  color: var(--color-red);
  background: rgba(239, 68, 68, 0.08);
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding: 20px 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}

.btn-clear {
  border: 1px solid var(--border-subtle);
  background: transparent;
  color: var(--text-secondary);
  padding: 10px 20px;
  border-radius: var(--radius-xl);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-clear:hover {
  border-color: var(--color-red);
  color: var(--color-red);
  background: rgba(239, 68, 68, 0.06);
  transform: translateY(-2px);
}

.cart-total {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: var(--text-secondary);
}

.total-label {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.total-price {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-orange);
}

.btn-checkout {
  padding: 12px 44px;
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-xl);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-checkout:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow);
}

.recommend {
  margin-top: 40px;
}

.recommend h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.recommend-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
}

.recommend-card:hover {
  transform: translateY(-4px);
  background: var(--bg-card-hover);
  border-color: var(--border-default);
  box-shadow: 0 0 30px rgba(0, 212, 255, 0.08);
}

.recommend-img {
  height: 140px;
  background: var(--bg-elevated);
  overflow: hidden;
}

.recommend-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.recommend-card:hover .recommend-img img {
  transform: scale(1.05);
}

.recommend-info {
  padding: 12px 14px;
}

.recommend-info h4 {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}

.recommend-info span {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-orange);
}
</style>
