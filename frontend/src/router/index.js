import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('../views/ProductDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('../views/Cart.vue') },
      { path: 'order', name: 'Order', component: () => import('../views/Order.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue') },
    ],
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAdmin: true },
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: { requiresAdmin: true },
  },
  {
    path: '/review',
    name: 'Review',
    component: () => import('../views/Review.vue'),
    meta: { requiresAdmin: true },
  },
  {
    path: '/seller',
    name: 'Seller',
    component: () => import('../views/Seller.vue'),
    meta: { requiresSeller: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAdmin) {
    const role = localStorage.getItem('userRole')
    const userId = localStorage.getItem('userId')
    if (!userId) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    if (role !== 'admin') {
      next('/')
      return
    }
  }
  if (to.meta.requiresSeller) {
    const role = localStorage.getItem('userRole')
    const userId = localStorage.getItem('userId')
    if (!userId) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    if (role !== 'seller' && role !== 'admin') {
      next('/')
      return
    }
  }
  next()
})

export default router
