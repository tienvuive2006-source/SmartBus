<template>
  <div class="min-h-screen bg-slate-50 flex flex-col justify-center items-center px-4 py-12 -mx-container-margin -mt-stack-space animate-fade-in">
    <div class="bg-white w-full max-w-md rounded-3xl shadow-[0px_16px_48px_rgba(15,23,42,0.06)] overflow-hidden border border-outline-variant/20">
      
      <!-- Header Banner -->
      <div class="bg-gradient-to-r from-secondary to-primary p-8 text-white text-center relative overflow-hidden">
        <div class="absolute -right-6 -top-6 w-32 h-32 bg-white/10 rounded-full blur-xl"></div>
        <div class="w-16 h-16 bg-white rounded-2xl shadow-md flex items-center justify-center mx-auto mb-4 text-secondary animate-bounce-slow">
          <span class="material-symbols-outlined text-4xl font-black">person_add</span>
        </div>
        <h2 class="text-headline-md font-black tracking-tight">Khởi tạo tài khoản</h2>
        <p class="text-label-md font-bold opacity-70 tracking-wider uppercase mt-1">GIA NHẬP HỆ THỐNG TRUNG NAM</p>
      </div>

      <div class="p-8">
        <form @submit.prevent="handleRegister" class="space-y-5">
          
          <!-- Full Name -->
          <div>
            <label class="block text-label-md font-black text-slate-600 uppercase tracking-wider mb-2">Họ & Tên thật</label>
            <div class="relative">
              <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">badge</span>
              <input 
                v-model="fullName" 
                type="text" 
                placeholder="Ví dụ: Nguyễn Văn A" 
                required
                class="w-full pl-12 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-body-md font-bold text-slate-800 focus:bg-white focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
              />
            </div>
          </div>

          <!-- Phone Number -->
          <div>
            <label class="block text-label-md font-black text-slate-600 uppercase tracking-wider mb-2">Số điện thoại</label>
            <div class="relative">
              <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">call</span>
              <input 
                v-model="phone" 
                type="text" 
                placeholder="Nhập số điện thoại" 
                required
                class="w-full pl-12 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-body-md font-bold text-slate-800 focus:bg-white focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
              />
            </div>
          </div>

          <!-- Email -->
          <div>
            <label class="block text-label-md font-black text-slate-600 uppercase tracking-wider mb-2">Địa chỉ Email (Tùy chọn)</label>
            <div class="relative">
              <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">mail</span>
              <input 
                v-model="email" 
                type="email" 
                placeholder="Ví dụ: example@gmail.com" 
                class="w-full pl-12 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-body-md font-bold text-slate-800 focus:bg-white focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
              />
            </div>
          </div>

          <!-- Password -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-[10px] font-black text-slate-600 uppercase tracking-wider mb-2">Mật khẩu</label>
              <div class="relative">
                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg">lock</span>
                <input 
                  v-model="password" 
                  type="password" 
                  placeholder="Tối thiểu 6 ký tự" 
                  required
                  minlength="6"
                  class="w-full pl-10 pr-3 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-body-md font-bold text-slate-800 focus:bg-white focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                />
              </div>
            </div>
            <div>
              <label class="block text-[10px] font-black text-slate-600 uppercase tracking-wider mb-2">Nhập lại</label>
              <div class="relative">
                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg">lock_reset</span>
                <input 
                  v-model="confirmPassword" 
                  type="password" 
                  placeholder="Nhập lại MK" 
                  required
                  minlength="6"
                  class="w-full pl-10 pr-3 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-body-md font-bold text-slate-800 focus:bg-white focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                />
              </div>
            </div>
          </div>

          <!-- Success Message (Tặng tiền) -->
          <div class="bg-emerald-50 border border-emerald-100 text-emerald-800 px-4 py-3 rounded-2xl text-[11px] font-black uppercase tracking-wide flex items-center gap-2">
            <span class="material-symbols-outlined text-emerald-600 animate-pulse">redeem</span>
            <span>QUÀ KHỞI NGHIỆP: TẶNG 500K VÀO VÍ KHI TẠO MỚI!</span>
          </div>

          <!-- Error Alert -->
          <div v-if="errorMsg" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-2xl text-body-sm font-bold flex items-center gap-2">
            <span class="material-symbols-outlined text-[18px]">error</span>
            {{ errorMsg }}
          </div>

          <!-- Submit -->
          <button 
            type="submit" 
            :disabled="loading"
            class="w-full bg-gradient-to-r from-secondary to-primary text-white py-4 rounded-2xl font-black text-body-md shadow-md hover:brightness-110 transition-all active:scale-95 disabled:opacity-50 flex items-center justify-center gap-2 mt-2"
          >
            <span v-if="loading" class="w-5 h-5 border-3 border-white border-t-transparent rounded-full animate-spin"></span>
            {{ loading ? 'ĐANG KHỞI TẠO...' : 'TẠO TÀI KHOẢN & NHẬN 500K' }}
          </button>
        </form>

        <!-- Redirection -->
        <div class="mt-8 text-center border-t border-slate-100 pt-6">
          <p class="text-body-md font-bold text-slate-500">
            Đã có tài khoản rồi?
          </p>
          <button @click="$router.push('/auth/login')" class="text-secondary font-black text-body-md mt-1 hover:underline tracking-wide">
            QUAY LẠI ĐĂNG NHẬP NGAY
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const fullName = ref('');
const phone = ref('');
const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const loading = ref(false);
const errorMsg = ref('');

const handleRegister = async () => {
  errorMsg.value = '';
  
  if (password.value !== confirmPassword.value) {
    errorMsg.value = 'Mật khẩu nhập lại không khớp!';
    return;
  }
  
  loading.value = true;
  
  try {
    await authStore.register(fullName.value, phone.value, password.value, email.value);
    
    // 🚀 Tự động đăng nhập ngay sau khi đăng ký -> về profile hưởng 500k
    router.push('/');
  } catch (error) {
    console.error("Đăng ký thất bại:", error);
    if (error.response?.data) {
      errorMsg.value = typeof error.response.data === 'string'
        ? error.response.data
        : "Số điện thoại đã tồn tại!";
    } else {
      errorMsg.value = "Máy chủ đang bận! Vui lòng thử lại sau.";
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
