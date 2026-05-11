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

.cart-table {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.cart-header {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  background: var(--bg-gray);
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 500;
}

.col-product { flex: 2; min-width: 0; }
.col-price { width: 100px; text-align: center; }
.col-qty { width: 120px; text-align: center; }
.col-subtotal { width: 100px; text-align: center; }
.col-action { width: 60px; text-align: center; }

.cart-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
}

.cart-item:last-child {
  border-bottom: none;
}

.col-product {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-img {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--bg-gray);
  cursor: pointer;
  flex-shrink: 0;
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
}

.item-text h4:hover {
  color: var(--color-primary);
}

.item-cat {
  font-size: 12px;
  color: var(--text-muted);
}

.col-price {
  font-size: 14px;
  color: var(--text-secondary);
  text-align: center;
}

.qty-box {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--border-medium);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.qty-box button {
  width: 28px;
  height: 28px;
  border: none;
  background: var(--bg-gray);
  font-size: 14px;
  cursor: pointer;
  color: var(--text-primary);
}

.qty-box button:hover {
  background: var(--bg-hover);
}

.qty-box span {
  width: 36px;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  line-height: 28px;
}

.col-subtotal {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-primary);
  text-align: center;
}

.btn-del {
  border: none;
  background: none;
  color: var(--text-muted);
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
  padding: 4px 8px;
}

.btn-del:hover {
  color: var(--color-primary);
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding: 16px 20px;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.btn-clear {
  border: 1px solid var(--border-light);
  background: var(--bg-white);
  color: var(--text-muted);
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
}

.btn-clear:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
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
}

.total-price {
  font-size: 24px;
  font-weight: 900;
  color: var(--color-primary);
}

.btn-checkout {
  padding: 10px 40px;
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

.btn-checkout:hover {
  background: var(--color-primary-dark);
}

.recommend {
  margin-top: 24px;
}

.recommend h3 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.recommend-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border-light);
  transition: all 0.2s;
}

.recommend-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary-light);
}

.recommend-img {
  height: 120px;
  background: var(--bg-gray);
  overflow: hidden;
}

.recommend-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recommend-info {
  padding: 8px 10px;
}

.recommend-info h4 {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.recommend-info span {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-primary);
}
</style>
