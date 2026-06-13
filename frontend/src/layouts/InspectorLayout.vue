<template>
  <div class="min-h-screen bg-slate-50 flex flex-col font-sans text-slate-800">
    <!-- Header Mobile-friendly -->
    <header class="bg-primary text-white shadow-md sticky top-0 z-50">
      <div class="flex items-center justify-between px-4 py-3">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-3xl font-black">directions_bus</span>
          <h1 class="text-title-lg font-black tracking-tight leading-none">
            Trung Nam <span class="text-body-sm font-medium bg-white/20 px-2 py-0.5 rounded-full ml-1 uppercase">Lơ xe</span>
          </h1>
        </div>
        
        <div class="flex items-center gap-3">
          <!-- User info -->
          <div class="text-right hidden sm:block">
            <p class="text-body-sm font-bold">{{ authStore.currentUser?.fullName }}</p>
            <p class="text-label-sm opacity-80">{{ authStore.currentUser?.phone }}</p>
          </div>
          <!-- Logout Btn -->
          <button @click="handleLogout" class="w-10 h-10 flex items-center justify-center rounded-full bg-white/10 hover:bg-white/20 active:bg-white/30 transition-colors">
            <span class="material-symbols-outlined text-xl">logout</span>
          </button>
        </div>
      </div>
    </header>

    <!-- Main Content Area -->
    <main class="flex-1 w-full max-w-3xl mx-auto p-4 sm:p-6 pb-12">
      <!-- Transition cho việc chuyển trang mượt mà -->
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const handleLogout = () => {
  authStore.logout()
  window.location.href = '/auth/login'
}
</script>

<style scoped>
/* Thêm class hỗ trợ safe area cho iOS (tai thỏ) */
.pb-safe {
  padding-bottom: env(safe-area-inset-bottom);
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease-out;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Material Symbols Fill State */
.fill-1 {
  font-variation-settings: 'FILL' 1;
}
</style>
