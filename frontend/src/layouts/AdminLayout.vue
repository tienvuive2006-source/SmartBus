<template>
  <div class="flex h-screen w-screen bg-[#f4f7f6] font-sans overflow-hidden fixed inset-0">
    <aside
      :class="isSidebarOpen ? 'translate-x-0' : '-translate-x-full'"
      class="fixed inset-y-0 left-0 z-50 w-64 bg-[#075955] border-r border-[#054340] text-white/80 transition-transform duration-300 ease-in-out md:translate-x-0 flex flex-col h-full shadow-2xl shadow-emerald-900/20"
    >
      <div class="flex items-center gap-2 h-16 border-b border-white/10 px-6 shrink-0 cursor-pointer" @click="$router.push('/')">
        <img src="/logo2.png" alt="Trung Nam Logo" class="h-10 scale-110 origin-left w-auto object-contain brightness-0 invert opacity-90" />
        <span class="text-white font-black tracking-tight text-lg translate-y-[2px]">Admin</span>
      </div>

      <nav class="flex-1 py-6 overflow-y-auto custom-scrollbar-dark">
        <p class="text-xs font-bold text-white/50 uppercase tracking-wider mb-2 px-6">Hệ thống</p>
        
        <template v-for="item in menuItems" :key="item.name">
          <!-- Normal Link -->
          <router-link
            v-if="!item.children"
            :to="item.path"
            class="flex items-center gap-3 px-6 py-3 transition-colors duration-200 hover:bg-black/10 group border-l-4 border-transparent font-semibold"
            active-class="bg-black/20 !border-amber-400 font-bold"
            :class="{'text-white/70': !isActiveLink(item.path), 'text-white': isActiveLink(item.path)}"
          >
            <span class="material-symbols-outlined text-[22px] transition-transform" :class="{'text-amber-400': isActiveLink(item.path)}">{{ item.icon }}</span>
            <span class="text-[15px] flex-1">{{ item.name }}</span>
            
            <!-- Badge Đặt vé (Thông báo khách mới) -->
            <span v-if="item.name === 'Đặt vé' && authStore.newBookingsCount > 0" class="bg-red-500 text-white text-[11px] font-bold px-2 py-0.5 rounded-full min-w-[20px] text-center">
              {{ authStore.newBookingsCount > 99 ? '99+' : authStore.newBookingsCount }}
            </span>
            
            <!-- Badge Quỹ (chi phí mới + yêu cầu hoàn tiền cần xử lý) -->
            <span v-if="item.name === 'Quản lý quỹ' && fundAttentionCount > 0" class="bg-red-500 text-white text-[11px] font-bold px-2 py-0.5 rounded-full min-w-[20px] text-center tabular-nums">
              {{ fundAttentionCount > 99 ? '99+' : fundAttentionCount }}
            </span>
            
            <!-- Badge Đánh giá (Thông báo review mới) -->
            <span v-if="item.name === 'Đánh giá' && authStore.newReviewsCount > 0" class="bg-red-500 text-white text-[11px] font-bold px-2 py-0.5 rounded-full min-w-[20px] text-center">
              {{ authStore.newReviewsCount > 99 ? '99+' : authStore.newReviewsCount }}
            </span>
          </router-link>

          <!-- Dropdown Wrapper -->
          <div v-else>
            <button
              @click="toggleMenu(item.name)"
              class="w-full flex items-center justify-between px-6 py-3 transition-colors duration-200 hover:bg-black/10 group border-l-4 border-transparent font-semibold cursor-pointer"
              :class="{
                'bg-black/20 !border-amber-400 font-bold text-white': $route.path.startsWith(item.path),
                'text-white/70': !$route.path.startsWith(item.path)
              }"
            >
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-[22px] transition-transform" :class="{'text-amber-400': $route.path.startsWith(item.path)}">{{ item.icon }}</span>
                <span class="text-[15px]">{{ item.name }}</span>
              </div>
              <span class="material-symbols-outlined text-lg transition-transform duration-200" :class="{'rotate-180': openMenus[item.name]}">expand_more</span>
            </button>
            <!-- Dropdown Items -->
            <div v-show="openMenus[item.name]" class="bg-black/20 py-1">
              <router-link
                v-for="child in item.children"
                :key="child.name"
                :to="child.path"
                class="flex items-center gap-3 pl-12 pr-6 py-2.5 transition-colors duration-200 hover:bg-white/5 font-semibold text-[14px] text-white/60 hover:text-white"
                :class="{'!text-amber-400 !font-bold': isChildActive(child.path)}"
              >
                <span class="w-1.5 h-1.5 rounded-full bg-current opacity-70 transition-all" :class="{'!opacity-100 scale-125': isChildActive(child.path)}"></span>
                <span>{{ child.name }}</span>
              </router-link>
            </div>
          </div>
        </template>
      </nav>

      <div class="p-4 border-t border-white/10 shrink-0">
        <button @click="handleLogout" class="flex items-center gap-3 px-4 py-3 w-full rounded-lg text-white/70 hover:bg-white/10 hover:text-white transition-colors duration-200 font-bold group">
          <span class="material-symbols-outlined text-[22px]">logout</span>
          <span class="text-[15px]">Đăng xuất</span>
        </button>
      </div>
    </aside>

    <div
      v-if="isSidebarOpen"
      @click="isSidebarOpen = false"
      class="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm md:hidden transition-opacity"
    ></div>

    <div class="flex-1 flex flex-col min-w-0 md:ml-64 relative">
      <header class="bg-white border-b border-gray-200 h-16 flex items-center justify-between px-6 lg:px-8 z-10 shrink-0 sticky top-0">
        <div class="flex items-center gap-4">
          <button
            @click="isSidebarOpen = !isSidebarOpen"
            class="md:hidden w-10 h-10 flex items-center justify-center rounded-full text-gray-500 hover:bg-gray-100 focus:outline-none transition-colors"
          >
            <span class="material-symbols-outlined text-2xl">menu</span>
          </button>
        </div>

        <div class="flex items-center gap-6">
          <NotificationBell class="text-gray-500 hover:text-gray-700" />
          
          <div class="flex items-center gap-3 border-l pl-6 border-gray-200 cursor-pointer">
            <div class="text-right hidden md:block">
              <p class="text-gray-800 text-sm font-medium">{{ authStore.currentUser?.fullName || 'Quản trị viên' }}</p>
              <p class="text-gray-500 text-xs">Admin</p>
            </div>
            <img src="https://ui-avatars.com/api/?name=Admin&background=eff6ff&color=2563eb" class="w-10 h-10 rounded-full border border-gray-200 object-cover" alt="Avatar">
          </div>
        </div>
      </header>

      <main class="flex-1 overflow-x-hidden overflow-y-auto bg-[#f8fafc] min-h-0 relative" :class="[$route.meta.fullScreen ? 'p-0' : ($route.meta.compactContent ? 'p-2 lg:p-3' : 'p-6 lg:p-8')]">
        <div class="relative z-10" :class="[$route.meta.fullScreen ? 'w-full min-h-[calc(100vh-64px)] flex flex-col' : ($route.meta.compactContent ? 'w-full max-w-none' : 'max-w-7xl mx-auto')]">
          <router-view v-slot="{ Component }">
            <component :is="Component" />
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import NotificationBell from '@/components/NotificationBell.vue'

const isSidebarOpen = ref(false)
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const fundAttentionCount = computed(() => authStore.newExpensesCount + authStore.newRefundsCount)

const openMenus = reactive({
  'Tài khoản': false
})

const toggleMenu = (name) => {
  openMenus[name] = !openMenus[name]
}

onMounted(() => {
  authStore.fetchMe()
  authStore.fetchNewRefundsCount()
  if (route.path.startsWith('/admin/users')) {
    openMenus['Tài khoản'] = true
  }
})

// Clear unread booking/expense/review notifications when entering the page
watch(route, (newRoute) => {
  if (newRoute.path.startsWith('/admin/booking-manager')) {
    authStore.clearNewBookingsCount()
  }
  if (newRoute.path.startsWith('/admin/reviews')) {
    authStore.clearNewReviewsCount()
  }
}, { immediate: true })

const isActiveLink = (path) => {
  if (path === '/admin') return route.path === '/admin'
  return route.path.startsWith(path)
}

const isChildActive = (childPath) => {
  // Extract tab param from child path like '/admin/users?tab=driver'
  try {
    const url = new URL(childPath, 'http://x')
    const childTab = url.searchParams.get('tab')
    const basePath = url.pathname
    // Must be on the same base path
    if (route.path !== basePath) return false
    // Match tab query param (default to 'users' if no tab in URL)
    return (route.query.tab || 'users') === childTab
  } catch {
    return route.fullPath === childPath
  }
}

const handleLogout = () => {
  authStore.logout()
  window.location.href = '/auth/login'
}

const menuItems = [
  { name: 'Tổng quan', path: '/admin', icon: 'dashboard' },
  { name: 'Quản lý quỹ', path: '/admin/funds', icon: 'account_balance_wallet' },
  { name: 'Tuyến đường', path: '/admin/route-manager', icon: 'route' },
  { name: 'Chuyến xe', path: '/admin/trip-manager', icon: 'directions_bus' },
  { name: 'Đặt vé', path: '/admin/booking-manager', icon: 'book_online' },
  { name: 'Khuyến mãi', path: '/admin/vouchers', icon: 'local_offer' },
  { name: 'Đánh giá', path: '/admin/reviews', icon: 'star' },
  { name: 'Tin tức', path: '/admin/articles', icon: 'article' },
  { name: 'Đội xe & Cấu hình', path: '/admin/fleet-status', icon: 'local_shipping' },
  { name: 'Phân công', path: '/admin/drivers', icon: 'assignment_ind' },
  { 
    name: 'Tài khoản', 
    path: '/admin/users', 
    icon: 'manage_accounts',
    children: [
      { name: 'Khách hàng', path: '/admin/users?tab=users' },
      { name: 'Tài xế', path: '/admin/users?tab=driver' },
      { name: 'Lơ xe', path: '/admin/users?tab=inspector' },
      { name: 'Quản trị viên', path: '/admin/users?tab=admin' }
    ]
  },
  { name: 'Banner', path: '/admin/banners', icon: 'view_carousel' },
  { name: 'Sự cố khẩn cấp', path: '/admin/incidents', icon: 'warning' },
  { name: 'Nhật ký hệ thống', path: '/admin/audit-logs', icon: 'history' },
]
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.custom-scrollbar-dark::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar-dark::-webkit-scrollbar-track {
  background: transparent; 
}
.custom-scrollbar-dark::-webkit-scrollbar-thumb {
  background: #334155; 
  border-radius: 4px;
}
.custom-scrollbar-dark::-webkit-scrollbar-thumb:hover {
  background: #475569; 
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent; 
}
::-webkit-scrollbar-thumb {
  background: #cbd5e1; 
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: #94a3b8; 
}
</style>
