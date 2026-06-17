<template>
  <div class="flex h-screen w-screen bg-[#f4f7f6] font-sans overflow-hidden fixed inset-0">
    <aside
      :class="isSidebarOpen ? 'translate-x-0' : '-translate-x-full'"
      class="fixed inset-y-0 left-0 z-50 w-64 bg-[#075955] text-white transition-transform duration-300 ease-in-out md:translate-x-0 shadow-2xl flex flex-col h-full"
    >
      <div class="flex items-center gap-3 justify-center h-20 border-b border-white/10 px-4 shrink-0">
        <div class="w-10 h-10 rounded-full bg-white/10 flex items-center justify-center">
          <span class="material-symbols-outlined text-2xl text-emerald-300">directions_bus</span>
        </div>
        <div class="flex flex-col">
          <h1 class="text-base font-black tracking-widest uppercase leading-none">Trung - Nam</h1>
          <span class="text-[10px] text-emerald-200 font-semibold uppercase tracking-widest mt-1">Admin Panel</span>
        </div>
      </div>

      <nav class="flex-1 px-4 py-6 space-y-1.5 overflow-y-auto">
        <p class="text-[10px] font-bold text-emerald-200/60 uppercase tracking-widest mb-4 ml-2">Hệ thống</p>
        
        <router-link
          v-for="item in menuItems"
          :key="item.name"
          :to="item.path"
          class="flex items-center gap-3 px-4 py-3.5 rounded-xl transition-all duration-200 hover:bg-white/10 group"
          active-class="bg-white/10 shadow-inner font-black"
          :class="{'text-emerald-100': $route.path !== item.path, 'text-white': $route.path === item.path}"
        >
          <span class="material-symbols-outlined text-[20px] transition-transform group-hover:scale-110" :class="{'text-emerald-400': $route.path === item.path}">{{ item.icon }}</span>
          <span class="text-sm tracking-wide">{{ item.name }}</span>
        </router-link>
      </nav>

      <div class="p-4 border-t border-white/10 shrink-0">
        <button @click="handleLogout" class="flex items-center gap-3 px-4 py-3.5 w-full rounded-xl text-red-300 hover:bg-red-500 hover:text-white transition-colors duration-200 font-semibold group">
          <span class="material-symbols-outlined text-[20px] group-hover:scale-110 transition-transform">logout</span>
          <span class="text-sm tracking-wide">Đăng xuất</span>
        </button>
      </div>
    </aside>

    <div
      v-if="isSidebarOpen"
      @click="isSidebarOpen = false"
      class="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm md:hidden transition-opacity"
    ></div>

    <div class="flex-1 flex flex-col min-w-0 md:ml-64 relative">
      <header class="bg-white/80 backdrop-blur-md shadow-sm border-b border-gray-200 h-20 flex items-center justify-between px-6 lg:px-8 z-10 shrink-0 sticky top-0">
        <div class="flex items-center gap-4">
          <button
            @click="isSidebarOpen = !isSidebarOpen"
            class="md:hidden w-10 h-10 flex items-center justify-center rounded-full text-gray-600 hover:bg-gray-100 focus:outline-none transition-colors"
          >
            <span class="material-symbols-outlined text-2xl">menu</span>
          </button>
          
          <h2 class="text-xl font-black text-gray-800 hidden sm:block tracking-tight">Trang quản trị</h2>
        </div>

        <div class="flex items-center gap-6">
          <button class="relative w-10 h-10 flex items-center justify-center text-gray-500 hover:text-[#075955] hover:bg-emerald-50 transition-colors rounded-full">
            <span class="material-symbols-outlined">notifications</span>
            <span class="absolute top-2.5 right-2.5 w-2 h-2 bg-red-500 rounded-full ring-2 ring-white"></span>
          </button>
          
          <div class="flex items-center gap-3 border-l pl-6 border-gray-200 cursor-pointer group">
            <div class="text-right hidden md:block">
              <p class="font-bold text-gray-800 text-sm">{{ authStore.currentUser?.fullName || 'Quản trị viên' }}</p>
              <p class="text-[#075955] text-[10px] font-black uppercase tracking-widest mt-0.5">Quản trị viên</p>
            </div>
            <div class="w-10 h-10 rounded-full bg-[#075955] text-white flex items-center justify-center font-bold shadow-md group-hover:ring-4 group-hover:ring-emerald-100 transition-all uppercase">
              {{ authStore.currentUser?.fullName ? authStore.currentUser.fullName.charAt(0) : 'A' }}
            </div>
          </div>
        </div>
      </header>

      <main class="flex-1 overflow-x-hidden overflow-y-auto bg-[#f4f7f6] p-6 lg:p-8 min-h-0 relative">
        <!-- Abstract Background Decoration -->
        <div class="absolute top-0 left-0 w-full h-64 bg-gradient-to-b from-white/50 to-transparent pointer-events-none"></div>
        <div class="max-w-7xl mx-auto relative z-10">
          <router-view v-slot="{ Component }">
            <component :is="Component" />
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const isSidebarOpen = ref(false)
const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  // Tự động đồng bộ thông tin mới nhất từ server mỗi khi vào trang admin
  // Giúp tên trên góc phải được cập nhật ngay lập tức nếu admin vừa đổi tên
  authStore.fetchMe()
})

const handleLogout = () => {
  // Bỏ qua hàm confirm() vì có thể trình duyệt đã block popup (Do alert lúc nãy)
  authStore.logout()
  window.location.href = '/auth/login'
}

const menuItems = [
  { name: 'Tổng quan', path: '/admin', icon: 'pie_chart' },
  { name: 'Quản lý Tuyến đường', path: '/admin/route-manager', icon: 'map' },
  { name: 'Quản lý Chuyến xe', path: '/admin/trip-manager', icon: 'route' },
  { name: 'Quản lý Đặt vé', path: '/admin/booking-manager', icon: 'receipt_long' },
  { name: 'Quản lý Đánh giá', path: '/admin/reviews', icon: 'star_rate' },
  { name: 'Quản lý Loại xe', path: '/admin/bus-type', icon: 'directions_bus_filled' },
  { name: 'Trạng thái Đội xe', path: '/admin/fleet-status', icon: 'local_shipping' },
  { name: 'Quản lý Người dùng', path: '/admin/users', icon: 'manage_accounts' },
  { name: 'Nhật ký Hoạt động', path: '/admin/audit-logs', icon: 'history' },
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