import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const API_BASE = import.meta.env.VITE_API_BASE_URL

export const useAuthStore = defineStore('auth', () => {
  // ─── STATE ────────────────────────────────────────────────────────
  const token = ref(localStorage.getItem('jwt_token') || null)
  const user = ref(JSON.parse(localStorage.getItem('jwt_user') || 'null'))
  const notifications = ref([])
  const newBookingsCount = ref(0)
  const newExpensesCount = ref(0)
  const newReviewsCount = ref(0)
  const newRefundsCount = ref(0)

  // ✅ Nạp thông báo riêng của User hiện tại
  const loadNotifications = () => {
    if (user.value && user.value.id) {
      notifications.value = JSON.parse(localStorage.getItem(`app_notifications_${user.value.id}`) || '[]')
    } else {
      notifications.value = []
    }
  }

  // Gọi lần đầu khi khởi tạo store
  loadNotifications()

  // ─── GETTERS ──────────────────────────────────────────────────────
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isInspector = computed(() => user.value?.role === 'INSPECTOR')
  const isDriver = computed(() => user.value?.role === 'DRIVER')
  const currentUser = computed(() => user.value)
  const needsPhoneCompletion = computed(() =>
    user.value?.authProvider === 'GOOGLE' && user.value?.phone?.startsWith('GG_')
  )

  // ─── HELPERS ──────────────────────────────────────────────────────
  // Header Authorization chuẩn Bearer
  const authHeader = computed(() => ({
    headers: { Authorization: `Bearer ${token.value}` }
  }))

  const unreadNotificationsCount = computed(() => {
    return notifications.value.filter(n => !n.read).length
  })

  // ─── ACTIONS ──────────────────────────────────────────────────────

  // ✅ Đăng nhập
  const login = async (identifier, password) => {
    const response = await axios.post(`${API_BASE}/auth/login`, { username: identifier, password })
    _saveSession(response.data)
    return response.data
  }

  // ✅ Đăng nhập bằng Google
  const googleLogin = async (credential) => {
    const response = await axios.post(`${API_BASE}/auth/google`, { credential })
    _saveSession(response.data)
    return response.data
  }

  // ✅ Đăng ký
  const sendRegistrationCode = async (email, phone) => {
    const response = await axios.post(`${API_BASE}/auth/register/send-code`, { email, phone })
    return response.data
  }

  const requestPasswordReset = async (email) => {
    const response = await axios.post(`${API_BASE}/auth/password/forgot`, { email })
    return response.data
  }

  const resetPassword = async (email, code, newPassword) => {
    const response = await axios.post(`${API_BASE}/auth/password/reset`, { email, code, newPassword })
    return response.data
  }

  const register = async (fullName, phone, password, email, verificationCode) => {
    const response = await axios.post(`${API_BASE}/auth/register`, { fullName, phone, password, email, verificationCode })
    _saveSession(response.data)
    return response.data
  }

  // ✅ Đăng xuất
  const logout = () => {
    disconnectWebSocket()
    token.value = null
    user.value = null
    notifications.value = []
    localStorage.removeItem('jwt_token')
    localStorage.removeItem('jwt_user')
    // Xóa luôn key cũ nếu còn tồn tại
    localStorage.removeItem('currentUser')
    // Xóa lịch sử đặt vé để bảo mật (tránh lộ thông tin khi đổi tài khoản)
    localStorage.removeItem('trungnam_history')
    localStorage.removeItem('saomaifly_history')
    localStorage.removeItem('skybus_history')
  }

  // ✅ Refresh thông tin user từ server (dùng token hiện tại)
  const fetchMe = async () => {
    if (!token.value) return null
    try {
      const response = await axios.get(`${API_BASE}/auth/me`, authHeader.value)
      const freshUser = response.data

      // So sánh số dư ví (nếu có user cũ và số dư thay đổi)
      if (user.value && user.value.walletBalance !== undefined && freshUser.walletBalance !== undefined) {
        const diff = freshUser.walletBalance - user.value.walletBalance
        if (diff !== 0) {
          addNotification({
            type: 'WALLET',
            title: `Biến động số dư (${diff > 0 ? '+' : ''}${diff.toLocaleString('vi-VN')}đ)`,
            message: `Tài khoản của bạn vừa ${diff > 0 ? 'được cộng' : 'bị trừ'} ${Math.abs(diff).toLocaleString('vi-VN')}đ.`,
            amount: diff,
            date: new Date().toISOString()
          })
        }
      }

      user.value = freshUser
      localStorage.setItem('jwt_user', JSON.stringify(response.data))
      return response.data
    } catch {
      logout() // Token hết hạn -> đăng xuất
      return null
    }
  }

  // ✅ Quản lý thông báo
  const addNotification = (notif) => {
    if (!user.value?.id) return
    notifications.value.unshift({ ...notif, id: Date.now(), read: false })
    localStorage.setItem(`app_notifications_${user.value.id}`, JSON.stringify(notifications.value))
  }

  const markAllNotificationsRead = () => {
    if (!user.value?.id) return
    notifications.value.forEach(n => n.read = true)
    localStorage.setItem(`app_notifications_${user.value.id}`, JSON.stringify(notifications.value))
  }

  const clearNotifications = () => {
    if (!user.value?.id) return
    notifications.value = []
    localStorage.setItem(`app_notifications_${user.value.id}`, JSON.stringify([]))
  }

  // ✅ Cập nhật số dư ví (dùng khi sau khi thanh toán)
  const updateWalletBalance = (newBalance) => {
    if (user.value) {
      user.value = { ...user.value, walletBalance: newBalance }
      localStorage.setItem('jwt_user', JSON.stringify(user.value))
    }
  }

  // ✅ Cập nhật thông tin User (dùng sau khi sửa Profile)
  const updateUser = (newUserObj) => {
    if (user.value) {
      user.value = { ...user.value, ...newUserObj }
      localStorage.setItem('jwt_user', JSON.stringify(user.value))
    }
  }

  const clearNewBookingsCount = () => {
    newBookingsCount.value = 0
  }

  const clearNewExpensesCount = () => {
    newExpensesCount.value = 0
  }

  const clearNewReviewsCount = () => {
    newReviewsCount.value = 0
  }

  const completeGooglePhone = async (phone) => {
    const response = await axios.put(`${API_BASE}/auth/me/phone`, { phone }, authHeader.value)
    disconnectWebSocket()
    _saveSession(response.data)
    return response.data
  }

  const setNewRefundsCount = (count) => {
    newRefundsCount.value = Math.max(0, Number(count) || 0)
  }

  const fetchNewRefundsCount = async () => {
    if (!token.value || user.value?.role !== 'ADMIN') return 0
    try {
      const response = await axios.get(`${API_BASE}/refund-requests/admin`, authHeader.value)
      const refunds = Array.isArray(response.data) ? response.data : []
      const count = refunds.filter(item =>
        item.refundMethod === 'BANK_TRANSFER' && ['PENDING', 'APPROVED'].includes(item.status)
      ).length
      setNewRefundsCount(count)
      return count
    } catch (error) {
      console.error('Không tải được số yêu cầu hoàn tiền:', error)
      return newRefundsCount.value
    }
  }

  // ─── PRIVATE ──────────────────────────────────────────────────────
  const _saveSession = (data) => {
    token.value = data.token
    user.value = {
      id: data.id,
      username: data.username || '',
      phone: data.phone,
      fullName: data.fullName,
      role: data.role,
      email: data.email,
      walletBalance: data.walletBalance,
      loyaltyPoints: data.loyaltyPoints || 0,
      authProvider: data.authProvider || 'LOCAL',
      avatarUrl: data.avatarUrl || null
    }
    localStorage.setItem('jwt_token', data.token)
    localStorage.setItem('jwt_user', JSON.stringify(user.value))
    loadNotifications() // Nạp thông báo khi có session mới
    connectWebSocket() // Kết nối WebSocket
  }

  // ─── WEBSOCKET (REALTIME PUSH NOTIFICATIONS) ──────────────────────
  let stompClient = null

  const connectWebSocket = () => {
    if (!user.value || !user.value.id) return

    stompClient = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE}/ws`),
      reconnectDelay: 5000,
      onConnect: () => {
        stompClient.subscribe(`/topic/wallet/${user.value.id}`, (message) => {
          if (message.body === 'UPDATE') {
            fetchMe() // Nhận tín hiệu từ Admin -> cập nhật số dư ngay lập tức
          }
        })

        stompClient.subscribe(`/topic/refunds/${user.value.id}`, (message) => {
          try {
            const data = JSON.parse(message.body)
            const titles = {
              REFUND_APPROVED: 'Yêu cầu hoàn tiền đã được duyệt',
              REFUND_COMPLETED: 'Hoàn tiền thành công',
              REFUND_NEEDS_INFO: 'Cần bổ sung thông tin hoàn tiền',
              REFUND_REJECTED: 'Yêu cầu hoàn tiền bị từ chối'
            }
            addNotification({
              type: data.type || 'REFUND',
              title: titles[data.type] || 'Cập nhật hoàn tiền',
              message: data.message,
              amount: Number(data.amount || 0),
              status: data.status,
              refundId: data.refundId,
              bookingId: data.bookingId,
              date: new Date().toISOString()
            })
            window.dispatchEvent(new CustomEvent('refund-status-updated', { detail: data }))
          } catch (error) {
            console.error('Không đọc được thông báo hoàn tiền:', error)
          }
        })
        
        // Nhận tín hiệu khi có vé mới (Chỉ dành cho Admin)
        if (user.value.role === 'ADMIN') {
          stompClient.subscribe(`/topic/admin/bookings/new`, (message) => {
            if (message.body === 'NEW_BOOKING') {
              newBookingsCount.value++
            }
          })
          
          stompClient.subscribe(`/topic/admin/expenses/new`, (message) => {
            if (message.body === 'NEW_EXPENSE') {
              newExpensesCount.value++
            }
          })
          
          stompClient.subscribe(`/topic/admin/reviews/new`, (message) => {
            if (message.body === 'NEW_REVIEW') {
              newReviewsCount.value++
            }
          })

          stompClient.subscribe(`/topic/admin/refunds/new`, (message) => {
            if (message.body === 'NEW_REFUND_REQUEST') {
              newRefundsCount.value++
            }
          })

          stompClient.subscribe(`/topic/admin/maintenance`, (message) => {
            try {
              const data = JSON.parse(message.body)
              addNotification({
                type: 'MAINTENANCE_DUE',
                title: data.title || 'Nhắc lịch bảo trì xe',
                message: data.message,
                busId: data.busId,
                licensePlate: data.licensePlate,
                date: data.date || new Date().toISOString()
              })
              window.dispatchEvent(new CustomEvent('maintenance-alert', { detail: data }))
            } catch (error) {
              console.error('Không đọc được cảnh báo bảo trì:', error)
            }
          })
        }
      }
    })

    stompClient.activate()
  }

  const disconnectWebSocket = () => {
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
    }
  }

  // Bật WebSocket lúc app vừa load xong nếu user đang đăng nhập
  if (user.value && user.value.id) {
    connectWebSocket()
  }

  return {
    // state
    token,
    user,
    notifications,
    // getters
    isLoggedIn,
    isAdmin,
    isInspector,
    isDriver,
    currentUser,
    needsPhoneCompletion,
    authHeader,
    unreadNotificationsCount,
    newBookingsCount,
    newExpensesCount,
    newReviewsCount,
    newRefundsCount,
    // actions
    login,
    googleLogin,
    completeGooglePhone,
    sendRegistrationCode,
    requestPasswordReset,
    resetPassword,
    register,
    logout,
    fetchMe,
    updateWalletBalance,
    updateUser,
    addNotification,
    markAllNotificationsRead,
    clearNotifications,
    clearNewBookingsCount,
    clearNewExpensesCount,
    clearNewReviewsCount,
    setNewRefundsCount,
    fetchNewRefundsCount
  }
})
