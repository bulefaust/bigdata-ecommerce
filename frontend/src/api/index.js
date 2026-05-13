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
export const submitReview = (id, data) => request.post(`/product/${id}/reviews`, data)

export const createOrder = (userId, productId, quantity) =>
  request.post('/order/create', { userId, productId, quantity })

export const getOrderList = (params) => request.get('/order/list', { params })
export const deleteOrder = (id) => request.delete(`/order/${id}`)

export const toggleFavorite = (userId, productId) => request.post('/favorite/toggle', { userId, productId })
export const checkFavorite = (userId, productId) => request.get('/favorite/check', { params: { userId, productId } })
export const getFavoriteList = (userId) => request.get('/favorite/list', { params: { userId } })
export const removeFavorite = (id) => request.delete(`/favorite/${id}`)

export const login = (username, password) => request.post('/user/login', { username, password })
export const register = (data) => request.post('/user/register', data)
export const getUserInfo = (id) => request.get(`/user/${id}`)

export const trackBehavior = (data) => request.post('/track/behavior', data)

export const getPersonalRecommend = (userId, limit = 8) => request.get('/recommend/personal', { params: { userId, limit } })
export const getUserProfile = (userId) => request.get('/recommend/profile', { params: { userId } })

export const getCurrentFlashSales = () => request.get('/flash-sale/current')
export const getUpcomingFlashSales = () => request.get('/flash-sale/upcoming')

export const getDashboardOverview = () => request.get('/dashboard/overview')
export const getDashboardRevenueTrend = (days = 7) => request.get('/dashboard/revenue-trend', { params: { days } })
export const getDashboardCategoryStats = () => request.get('/dashboard/category-stats')
export const getDashboardBehaviorStats = () => request.get('/dashboard/behavior-stats')
export const getDashboardHotProducts = () => request.get('/dashboard/hot-products')
export const getDashboardRealtimeOrders = () => request.get('/dashboard/realtime-orders')

export const getHotProductsAnalytics = (limit = 10, timeRange = '1h') => request.get('/analytics/hot', { params: { limit, timeRange } })
export const getUserActivity = (startDate, endDate) => request.get('/analytics/user-activity', { params: { startDate, endDate } })
export const getUserRetention = (days = 7) => request.get('/analytics/retention', { params: { days } })
export const getConversionFunnel = (startDate, endDate) => request.get('/analytics/funnel', { params: { startDate, endDate } })
export const getSalesForecast = (days = 30) => request.get('/analytics/sales-forecast', { params: { days } })
export const updateHotProducts = () => request.post('/analytics/update-hot')
