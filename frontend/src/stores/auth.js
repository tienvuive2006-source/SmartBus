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
  const login = async (phone, password) => {
    const response = await axios.post(`${API_BASE}/auth/login`, { phone, password })
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
  const register = async (fullName, phone, password, email = null) => {
    const response = await axios.post(`${API_BASE}/auth/register`, { fullName, phone, password, email })
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
            message: `Quản trị viên đã ${diff > 0 ? 'cộng' : 'trừ'} ${Math.abs(diff).toLocaleString('vi-VN')}đ ${diff > 0 ? 'vào' : 'khỏi'} ví của bạn.`,
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

  // ─── PRIVATE ──────────────────────────────────────────────────────
  const _saveSession = (data) => {
    token.value = data.token
    user.value = {
      id: data.id,
      phone: data.phone,
      fullName: data.fullName,
      role: data.role,
      email: data.email,
      walletBalance: data.walletBalance,
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
    authHeader,
    unreadNotificationsCount,
    // actions
    login,
    googleLogin,
    register,
    logout,
    fetchMe,
    updateWalletBalance,
    updateUser,
    addNotification,
    markAllNotificationsRead,
    clearNotifications
  }
})
