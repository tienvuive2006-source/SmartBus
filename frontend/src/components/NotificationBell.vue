<template>
  <div class="relative" v-if="authStore.isLoggedIn" ref="dropdownRef">
    <!-- Nút chuông thông báo -->
    <button @click="toggleDropdown" class="relative w-10 h-10 rounded-full hover:bg-white/10 transition-colors flex items-center justify-center text-white focus:outline-none">
      <span class="material-symbols-outlined text-2xl" :class="{'animate-shake-slow': totalUnread > 0}">notifications_active</span>
      <!-- Badge số lượng thông báo -->
      <span v-if="totalUnread > 0" class="absolute top-0.5 right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 border border-[#075955] text-[10px] font-black text-white shadow-[0_0_8px_rgba(239,68,68,0.8)] animate-bounce">
        {{ totalUnread }}
      </span>
    </button>

    <!-- Dropdown Menu -->
    <div v-if="isOpen" class="absolute right-0 mt-3 w-80 bg-white rounded-3xl shadow-2xl border border-gray-100 overflow-hidden z-50 animate-scale-up origin-top-right flex flex-col max-h-[420px]">
      
      <!-- HEADER -->
      <div class="p-4 bg-gray-50 border-b border-gray-100 flex justify-between items-center shrink-0">
        <h3 class="text-sm font-black text-gray-800 uppercase tracking-widest">Thông báo</h3>
        <div class="flex gap-3">
          <button v-if="authStore.unreadNotificationsCount > 0" @click="authStore.markAllNotificationsRead()" class="text-[10px] font-bold text-emerald-600 hover:text-emerald-700 uppercase tracking-widest transition-colors">Đánh dấu đã đọc</button>
          <button v-if="authStore.notifications.length > 0" @click="authStore.clearNotifications()" class="text-[10px] font-bold text-gray-400 hover:text-red-500 uppercase tracking-widest transition-colors">Xóa tất cả</button>
        </div>
      </div>

      <!-- BODY -->
      <div class="overflow-y-auto custom-scrollbar flex-1 bg-white">
        <!-- 1. CẢNH BÁO GG_ (Luôn nằm trên cùng nếu có) -->
        <div v-if="hasGGWarning" class="p-4 border-b border-red-100 bg-red-50/50 hover:bg-red-50 transition-colors cursor-pointer" @click="goToProfile">
          <div class="flex items-start gap-3">
            <div class="w-10 h-10 rounded-full bg-red-100 text-red-600 flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-xl animate-pulse">gpp_maybe</span>
            </div>
            <div>
              <h4 class="text-sm font-black text-red-700 tracking-tight">Thiếu SĐT liên hệ!</h4>
              <p class="text-xs font-medium text-gray-600 mt-1 leading-snug">Vui lòng cập nhật ngay để nhà xe có thể liên lạc đón bạn.</p>
            </div>
          </div>
        </div>
        
        <!-- 2. THÔNG BÁO TỪ STORE (Biến động số dư, v.v...) -->
        <div v-for="notif in authStore.notifications" :key="notif.id" 
             class="p-4 border-b border-gray-100 transition-colors flex items-start gap-3 relative"
             :class="notif.read ? 'bg-white hover:bg-gray-50' : 'bg-emerald-50/30 hover:bg-emerald-50/60'">
          
          <div v-if="!notif.read" class="absolute top-4 right-4 w-2 h-2 bg-emerald-500 rounded-full shadow-[0_0_5px_rgba(16,185,129,0.5)]"></div>
          
          <!-- ICON Tùy loại thông báo -->
          <div class="w-10 h-10 rounded-full flex items-center justify-center shrink-0" 
               :class="notif.amount > 0 ? 'bg-emerald-100 text-emerald-600' : 'bg-rose-100 text-rose-600'">
            <span class="material-symbols-outlined">{{ notif.amount > 0 ? 'add_card' : 'credit_score' }}</span>
          </div>
          
          <div class="pr-4">
            <h4 class="text-sm font-black tracking-tight" :class="notif.amount > 0 ? 'text-emerald-700' : 'text-rose-700'">{{ notif.title }}</h4>
            <p class="text-xs font-medium text-gray-600 mt-1 leading-snug" v-html="notif.message"></p>
            <p class="text-[10px] font-bold text-gray-400 mt-2 uppercase tracking-widest">{{ new Date(notif.date).toLocaleString('vi-VN') }}</p>
          </div>
        </div>

        <div v-if="!hasGGWarning && authStore.notifications.length === 0" class="p-10 text-center flex flex-col items-center">
          <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-3">
             <span class="material-symbols-outlined text-3xl text-gray-300">notifications_off</span>
          </div>
          <p class="text-xs font-bold text-gray-400 uppercase tracking-widest">Không có thông báo mới</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const isOpen = ref(false);
const dropdownRef = ref(null);

const hasGGWarning = computed(() => {
  return authStore.isLoggedIn && authStore.currentUser?.phone?.startsWith('GG_');
});

const totalCount = computed(() => {
  return (hasGGWarning.value ? 1 : 0) + authStore.notifications.length;
});

const totalUnread = computed(() => {
  return (hasGGWarning.value ? 1 : 0) + authStore.unreadNotificationsCount;
});

const toggleDropdown = () => {
  isOpen.value = !isOpen.value;
};

const goToProfile = () => {
  isOpen.value = false;
  router.push('/profile');
};

const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    isOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside);
});
</script>

<style scoped>
.animate-scale-up {
  animation: scale-up 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}
@keyframes scale-up {
  0% { transform: scale(0.95); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
.animate-shake-slow {
  animation: shake 4s ease-in-out infinite;
}
@keyframes shake {
  0%, 100% { transform: rotate(0deg); }
  5%, 15% { transform: rotate(15deg); }
  10%, 20% { transform: rotate(-15deg); }
  25% { transform: rotate(0deg); }
}
.animate-ping-slow {
  animation: ping 2s cubic-bezier(0, 0, 0.2, 1) infinite;
}
@keyframes ping {
  75%, 100% { transform: scale(2); opacity: 0; }
}
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 10px;
}
</style>
