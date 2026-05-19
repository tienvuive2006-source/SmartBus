<template>
  <header class="bg-surface shadow-sm docked full-width top-0 sticky z-40 flex items-center justify-between px-container-margin h-touch-target-min w-full max-w-7xl mx-auto transition-colors duration-200">
    <button v-if="showBack" @click="$router.back()" aria-label="Go back" class="text-on-surface-variant hover:bg-surface-variant rounded-full p-2 transition-colors">
      <span class="material-symbols-outlined">arrow_back</span>
    </button>
    <button v-else aria-label="Menu" class="text-on-surface-variant hover:bg-surface-variant rounded-full p-2 transition-colors">
      <span class="material-symbols-outlined">menu</span>
    </button>
    
    <!-- Dynamic Title -->
    <div 
      @click="$router.push('/')" 
      class="text-headline-md font-headline-md font-black text-primary cursor-pointer tracking-tight hover:opacity-80 transition-opacity flex items-center gap-1"
    >
      <span class="material-symbols-outlined text-primary text-2xl font-black">directions_bus</span>
      {{ title }}
    </div>
    
    <!-- 🔑 KHU VỰC AUTH THÔNG MINH -->
    <div class="flex items-center gap-2">
      <!-- Nút Admin (Chỉ hiện nếu là ADMIN) -->
      <button 
        v-if="authStore.isAdmin" 
        @click="$router.push('/admin')" 
        class="flex items-center gap-1 bg-slate-900 text-white text-[10px] font-black uppercase tracking-widest px-3 py-1.5 rounded-xl hover:bg-primary transition-all shadow-sm"
      >
        <span class="material-symbols-outlined text-sm">dashboard</span>
        Quản trị
      </button>

      <!-- 1. Nếu ĐÃ ĐĂNG NHẬP: Hiện Avatar viết tắt Tên cực Pro -->
      <button 
        v-if="authStore.isLoggedIn" 
        @click="$router.push('/profile')" 
        aria-label="Trang cá nhân" 
        class="rounded-xl overflow-hidden w-9 h-9 focus:outline-none ring-2 ring-primary/20 shadow-md cursor-pointer transition-all active:scale-90 hover:scale-105 border-2 border-white shrink-0"
      >
        <img 
          alt="User avatar" 
          class="w-full h-full object-cover" 
          :src="`https://ui-avatars.com/api/?name=${encodeURIComponent(authStore.currentUser?.fullName || 'U')}&background=6366f1&color=fff&bold=true&size=64`"
        />
      </button>

      <!-- 2. Nếu CHƯA ĐĂNG NHẬP: Nút dẫn trực diện vô màn Login -->
      <button 
        v-else 
        @click="$router.push('/auth/login')" 
        class="bg-primary/10 text-primary text-[10px] font-black uppercase tracking-widest px-4 py-1.5 rounded-xl border border-primary/15 hover:bg-primary hover:text-white hover:shadow-md transition-all duration-200 active:scale-95 cursor-pointer shadow-sm"
      >
        Đăng nhập
      </button>
    </div>
  </header>
</template>

<script setup>
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const props = defineProps({
  title: {
    type: String,
    default: 'SkyBus'
  },
  showBack: {
    type: Boolean,
    default: false
  }
});

const route = useRoute();
const authStore = useAuthStore();

// computed reactive: tự cập nhật khi user login/logout
const currentUser = authStore.currentUser;
</script>
