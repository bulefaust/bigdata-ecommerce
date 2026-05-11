import axios from 'axios'
import { useUserStore } from '../store/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.userId) {
    config.headers['X-User-Id'] = userStore.userId
  }
  return config
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

export default request
