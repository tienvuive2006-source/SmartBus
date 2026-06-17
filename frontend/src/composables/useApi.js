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

// const API_BASE = 'http://localhost:8080/api'
const API_BASE = 'https://smartbus-6uf5.onrender.com/api'

let apiInstance = null;

export function useApi() {
  const authStore = useAuthStore()

  if (!apiInstance) {
    // Tạo axios instance với baseURL cố định chỉ 1 lần
    apiInstance = axios.create({
      baseURL: API_BASE,
      timeout: 15000
    })

    // ✅ Request Interceptor: Tự động gắn token vào mọi request
    apiInstance.interceptors.request.use(
      (config) => {
        // Luôn lấy token mới nhất từ store tại thời điểm request
        const currentToken = useAuthStore().token;
        if (currentToken) {
          config.headers.Authorization = `Bearer ${currentToken}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // ✅ Response Interceptor: Xử lý lỗi 401 (token hết hạn)
    apiInstance.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          // Token hết hạn -> tự động logout
          useAuthStore().logout()
          // Redirect về login
          if (!window.location.pathname.includes('/auth/')) {
            window.location.href = '/auth/login'
          }
        } else if (error.response?.status === 403 && error.response?.data?.error === 'ACCOUNT_LOCKED') {
          // Tài khoản vừa bị khóa -> Thông báo và văng ngay lập tức
          useAuthStore().logout()
          alert("⛔ TÀI KHOẢN BỊ KHÓA!\nPhiên đăng nhập của bạn đã bị Quản trị viên vô hiệu hóa.")
          if (!window.location.pathname.includes('/auth/')) {
            window.location.href = '/auth/login'
          }
        }
        return Promise.reject(error)
      }
    )
  }

  return apiInstance
}
