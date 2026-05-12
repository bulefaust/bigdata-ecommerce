import request from './request'

export const getProducts = (params) => request.get('/product/list', { params })
export const getProductById = (id) => request.get(`/product/${id}`)
export const clickProduct = (userId, productId) => request.post('/product/click', { userId, productId })
export const searchProducts = (params) => request.get('/product/search', { params })
export const getHotProducts = (limit = 8) => request.get('/product/hot', { params: { limit } })
export const getRecommendProducts = (params) => request.get('/product/recommend', { params })
export const getSimilarProducts = (id, limit = 6) => request.get(`/product/similar/${id}`, { params: { limit } })
export const getRandomProducts = (limit = 8) => request.get('/product/random', { params: { limit } })
export const getProductSpecs = (id) => request.get(`/product/${id}/specs`)
export const getProductReviews = (id) => request.get(`/product/${id}/reviews`)

export const createOrder = (userId, productId, quantity) =>
  request.post('/order/create', { userId, productId, quantity })

export const getOrderList = (params) => request.get('/order/list', { params })

export const login = (username, password) => request.post('/user/login', { username, password })
export const register = (data) => request.post('/user/register', data)
export const getUserInfo = (id) => request.get(`/user/${id}`)

export const trackBehavior = (data) => request.post('/track/behavior', data)
