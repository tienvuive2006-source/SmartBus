import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

export const useAuthStore = defineStore('auth', () => {
  // ─── STATE ────────────────────────────────────────────────────────
  const token = ref(localStorage.getItem('jwt_token') || null)
  const user = ref(JSON.parse(localStorage.getItem('jwt_user') || 'null'))

  // ─── GETTERS ──────────────────────────────────────────────────────
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const currentUser = computed(() => user.value)

  // ─── HELPERS ──────────────────────────────────────────────────────
  // Header Authorization chuẩn Bearer
  const authHeader = computed(() => ({
    headers: { Authorization: `Bearer ${token.value}` }
  }))

  // ─── ACTIONS ──────────────────────────────────────────────────────

  // ✅ Đăng nhập
  const login = async (phone, password) => {
    const response = await axios.post(`${API_BASE}/auth/login`, { phone, password })
    _saveSession(response.data)
    return response.data
  }

  // ✅ Đăng ký
  const register = async (fullName, phone, password) => {
    const response = await axios.post(`${API_BASE}/auth/register`, { fullName, phone, password })
    _saveSession(response.data)
    return response.data
  }

  // ✅ Đăng xuất
  const logout = () => {
    token.value = null
    user.value = null
    localStorage.removeItem('jwt_token')
    localStorage.removeItem('jwt_user')
    // Xóa luôn key cũ nếu còn tồn tại
    localStorage.removeItem('currentUser')
  }

  // ✅ Refresh thông tin user từ server (dùng token hiện tại)
  const fetchMe = async () => {
    if (!token.value) return null
    try {
      const response = await axios.get(`${API_BASE}/auth/me`, authHeader.value)
      user.value = response.data
      localStorage.setItem('jwt_user', JSON.stringify(response.data))
      return response.data
    } catch {
      logout() // Token hết hạn -> đăng xuất
      return null
    }
  }

  // ✅ Cập nhật số dư ví (dùng khi sau khi thanh toán)
  const updateWalletBalance = (newBalance) => {
    if (user.value) {
      user.value = { ...user.value, walletBalance: newBalance }
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
      walletBalance: data.walletBalance
    }
    localStorage.setItem('jwt_token', data.token)
    localStorage.setItem('jwt_user', JSON.stringify(user.value))
  }

  return {
    // state
    token,
    user,
    // getters
    isLoggedIn,
    isAdmin,
    currentUser,
    authHeader,
    // actions
    login,
    register,
    logout,
    fetchMe,
    updateWalletBalance
  }
})
