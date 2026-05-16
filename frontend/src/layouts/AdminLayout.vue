<template>
  <div class="flex h-screen w-screen bg-slate-50 font-sans overflow-hidden fixed inset-0">
    <aside
      :class="isSidebarOpen ? 'translate-x-0' : '-translate-x-full'"
      class="fixed inset-y-0 left-0 z-50 w-64 bg-[#075955] text-white transition-transform duration-300 ease-in-out md:translate-x-0 shadow-2xl flex flex-col h-full"
    >
      <div class="flex items-center justify-center h-20 border-b border-[#0a7a75] px-4 shrink-0">
        <span class="material-symbols-outlined text-3xl mr-3 text-yellow-400">directions_bus</span>
        <h1 class="text-xl font-bold tracking-wider uppercase">Admin Panel</h1>
      </div>

      <nav class="flex-1 px-4 py-6 space-y-2 overflow-y-auto">
        <p class="text-xs font-semibold text-gray-300 uppercase tracking-widest mb-4 ml-2">Quản lý hệ thống</p>
        
        <router-link
          v-for="item in menuItems"
          :key="item.name"
          :to="item.path"
          class="flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 hover:bg-[#0a7a75]"
          active-class="bg-[#0a7a75] border-l-4 border-yellow-400 shadow-md font-semibold"
        >
          <span class="material-symbols-outlined text-[20px]">{{ item.icon }}</span>
          <span class="text-sm">{{ item.name }}</span>
        </router-link>
      </nav>

      <div class="p-4 border-t border-[#0a7a75] shrink-0">
        <button class="flex items-center gap-3 px-4 py-3 w-full rounded-lg text-red-200 hover:bg-red-500 hover:text-white transition-colors duration-200">
          <span class="material-symbols-outlined text-[20px]">logout</span>
          <span class="text-sm font-semibold">Đăng xuất</span>
        </button>
      </div>
    </aside>

    <div
      v-if="isSidebarOpen"
      @click="isSidebarOpen = false"
      class="fixed inset-0 z-40 bg-black bg-opacity-50 md:hidden transition-opacity"
    ></div>

    <div class="flex-1 flex flex-col min-w-0 md:ml-64">
      <header class="bg-white shadow-sm border-b border-gray-200 h-20 flex items-center justify-between px-6 lg:px-10 z-10 shrink-0">
        <div class="flex items-center gap-4">
          <button
            @click="isSidebarOpen = !isSidebarOpen"
            class="md:hidden p-2 rounded-lg text-gray-600 hover:bg-gray-100 focus:outline-none transition-colors"
          >
            <span class="material-symbols-outlined text-2xl">menu</span>
          </button>
          
          <h2 class="text-xl font-bold text-gray-800 hidden sm:block">Dashboard</h2>
        </div>

        <div class="flex items-center gap-6">
          <button class="relative p-2 text-gray-500 hover:text-[#075955] transition-colors rounded-full hover:bg-gray-100">
            <span class="material-symbols-outlined">notifications</span>
            <span class="absolute top-1 right-2 w-2.5 h-2.5 bg-red-500 rounded-full border-2 border-white"></span>
          </button>
          
          <div class="flex items-center gap-3 border-l pl-6 border-gray-200 cursor-pointer group">
            <div class="w-10 h-10 rounded-full bg-[#075955] text-white flex items-center justify-center font-bold shadow-md group-hover:scale-105 transition-transform">
              T
            </div>
            <div class="hidden md:block text-sm">
              <p class="font-bold text-gray-800">Quản trị viên</p>
              <p class="text-gray-500 text-xs">admin@saomaifly.com</p>
            </div>
          </div>
        </div>
      </header>

      <main class="flex-1 overflow-x-hidden overflow-y-auto bg-slate-50 p-6 lg:p-8 min-h-0">
        <div class="max-w-7xl mx-auto">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :key="$route.path" :is="Component" />
            </transition>
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const isSidebarOpen = ref(false)

// Danh sách menu được map dựa trên các file View admin mà bạn đang có
const menuItems = [
  { name: 'Tổng quan', path: '/admin', icon: 'pie_chart' },
  { name: 'Quản lý Đặt vé', path: '/admin/booking-manager', icon: 'receipt_long' },
  { name: 'Quản lý Chuyến xe', path: '/admin/trip-manager', icon: 'route' },
  { name: 'Quản lý Loại xe', path: '/admin/bus-type', icon: 'directions_bus_filled' },
  { name: 'Trạng thái Đội xe', path: '/admin/fleet-status', icon: 'local_shipping' },
  { name: 'Quản lý Người dùng', path: '/admin/users', icon: 'manage_accounts' },
]
</script>

<style scoped>
/* Hiệu ứng chuyển cảnh mượt mà khi đổi trang trong Admin */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* Tùy chỉnh thanh cuộn cho đẹp mắt hơn */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1; 
}
::-webkit-scrollbar-thumb {
  background: #c1c1c1; 
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8; 
}
</style>