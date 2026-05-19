/**
 * useApi - Axios instance tự động gắn JWT Authorization header
 * Dùng composable này thay cho import axios trực tiếp ở mọi component
 *
 * Cách dùng:
 *   import { useApi } from '@/composables/useApi'
 *   const api = useApi()
 *   const res = await api.get('/trips')
 *   const res = await api.post('/bookings', { ... })
 */
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const API_BASE = 'http://localhost:8080/api'

export function useApi() {
  const authStore = useAuthStore()

  // Tạo axios instance với baseURL cố định
  const instance = axios.create({
    baseURL: API_BASE,
    timeout: 15000
  })

  // ✅ Request Interceptor: Tự động gắn token vào mọi request
  instance.interceptors.request.use(
    (config) => {
      if (authStore.token) {
        config.headers.Authorization = `Bearer ${authStore.token}`
      }
      return config
    },
    (error) => Promise.reject(error)
  )

  // ✅ Response Interceptor: Xử lý lỗi 401 (token hết hạn)
  instance.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        // Token hết hạn -> tự động logout
        authStore.logout()
        // Redirect về login (dùng window.location để tránh circular dependency với router)
        if (!window.location.pathname.includes('/auth/')) {
          window.location.href = '/auth/login'
        }
      }
      return Promise.reject(error)
    }
  )

  return instance
}
