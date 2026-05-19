<template>
  <div class="min-h-screen bg-slate-50 flex flex-col justify-center items-center px-4 py-12 -mx-container-margin -mt-stack-space animate-fade-in">
    <div class="bg-white w-full max-w-md rounded-3xl shadow-[0px_16px_48px_rgba(15,23,42,0.06)] overflow-hidden border border-outline-variant/20">
      
      <!-- Header Banner -->
      <div class="bg-gradient-to-r from-primary to-slate-800 p-8 text-white text-center relative overflow-hidden">
        <div class="absolute -right-6 -top-6 w-32 h-32 bg-white/10 rounded-full blur-xl"></div>
        <div class="w-16 h-16 bg-white rounded-2xl shadow-md flex items-center justify-center mx-auto mb-4 text-primary animate-bounce-slow">
          <span class="material-symbols-outlined text-4xl font-black">directions_bus</span>
        </div>
        <h2 class="text-headline-md font-black tracking-tight">Chào mừng trở lại!</h2>
        <p class="text-label-md font-bold opacity-70 tracking-wider uppercase mt-1">ĐĂNG NHẬP HỆ THỐNG SKYBUS</p>
      </div>

      <div class="p-8">
        <!-- Form -->
        <form @submit.prevent="handleLogin" class="space-y-6">
          
          <!-- Phone Number -->
          <div>
            <label class="block text-label-md font-black text-slate-600 uppercase tracking-wider mb-2">Số điện thoại</label>
            <div class="relative">
              <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">call</span>
              <input 
                v-model="phone" 
                type="tel" 
                placeholder="Nhập số điện thoại của bạn" 
                required
                class="w-full pl-12 pr-4 py-3.5 bg-slate-50 border border-slate-200 rounded-2xl text-body-md font-bold text-slate-800 focus:bg-white focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
              />
            </div>
          </div>

          <!-- Password -->
          <div>
            <label class="block text-label-md font-black text-slate-600 uppercase tracking-wider mb-2">Mật khẩu</label>
            <div class="relative">
              <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">lock</span>
              <input 
                v-model="password" 
                type="password" 
                placeholder="Nhập mật khẩu bảo mật" 
                required
                class="w-full pl-12 pr-4 py-3.5 bg-slate-50 border border-slate-200 rounded-2xl text-body-md font-bold text-slate-800 focus:bg-white focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
              />
            </div>
          </div>

          <!-- Error Alert -->
          <div v-if="errorMsg" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-2xl text-body-sm font-bold flex items-center gap-2 animate-pulse">
            <span class="material-symbols-outlined text-[18px]">error</span>
            {{ errorMsg }}
          </div>

          <!-- Submit -->
          <button 
            type="submit" 
            :disabled="loading"
            class="w-full bg-primary text-white py-4 rounded-2xl font-black text-body-md shadow-md hover:bg-surface-tint transition-all active:scale-95 disabled:opacity-50 flex items-center justify-center gap-2"
          >
            <span v-if="loading" class="w-5 h-5 border-3 border-white border-t-transparent rounded-full animate-spin"></span>
            {{ loading ? 'ĐANG XÁC MINH...' : 'ĐĂNG NHẬP NGAY' }}
          </button>
        </form>

        <!-- Redirection -->
        <div class="mt-8 text-center border-t border-slate-100 pt-6">
          <p class="text-body-md font-bold text-slate-500">
            Chưa có tài khoản SkyBus?
          </p>
          <button @click="$router.push('/auth/register')" class="text-primary font-black text-body-md mt-1 hover:underline tracking-wide">
            ĐĂNG KÝ TÀI KHOẢN MỚI
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const phone = ref('');
const password = ref('');
const loading = ref(false);
const errorMsg = ref('');

const handleLogin = async () => {
  errorMsg.value = '';
  loading.value = true;
  
  try {
    await authStore.login(phone.value, password.value);
    
    // Nếu có redirect query param (bị chặn do chưa login) -> về đó
    const redirectTo = route.query.redirect || (authStore.isAdmin ? '/admin' : '/profile');
    router.push(redirectTo);
  } catch (error) {
    console.error("Đăng nhập thất bại:", error);
    if (error.response?.data) {
      errorMsg.value = typeof error.response.data === 'string'
        ? error.response.data
        : "Mật khẩu hoặc SĐT không hợp lệ!";
    } else {
      errorMsg.value = "Lỗi máy chủ! Vui lòng khởi động lại Backend Java.";
    }
  } finally {
    loading.value = false;
  }
};
</script>


<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
.animate-bounce-slow {
  animation: bounce 3s infinite;
}
@keyframes bounce {
  0%, 100% { transform: translateY(-5%); animation-timing-function: cubic-bezier(0.8,0,1,1); }
  50% { transform: none; animation-timing-function: cubic-bezier(0,0,0.2,1); }
}
</style>
