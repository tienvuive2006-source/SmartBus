<template>
  <div class="min-h-screen bg-[#f2f5f8] font-sans text-slate-800">
    <nav class="bg-[#075955] text-white border-b border-[#05403d] sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <div class="flex items-center gap-2 cursor-pointer" @click="$router.push('/')">
          <span class="material-symbols-outlined text-white text-4xl">directions_bus</span>
          <div class="flex flex-col">
            <span class="text-xl font-bold leading-none tracking-tight">Trung - Nam</span>
            <span class="text-[9px] uppercase tracking-wider font-semibold">Nhà xe chuyên tuyến Miền Trung - Nam</span>
          </div>
        </div>
        <div class="flex items-center gap-6">
          <button @click="$router.push('/')" class="text-sm font-semibold hover:text-yellow-300 transition-colors flex items-center gap-1">
            <span class="material-symbols-outlined text-xl">home</span>
            Trang chủ
          </button>
        </div>
      </div>
    </nav>
    
    <div class="pb-12 px-4 animate-fade-in bg-[#f2f5f8]">
    
    <!-- Loading State -->
    <div v-if="loading" class="min-h-[70vh] flex flex-col items-center justify-center gap-3">
      <div class="w-10 h-10 border-4 border-[#075955] border-t-transparent rounded-full animate-spin"></div>
      <p class="text-sm font-bold text-slate-500 animate-pulse">Đang tải thông tin cá nhân...</p>
    </div>

    <!-- Main Profile View -->
    <main v-else-if="user" class="max-w-2xl mx-auto space-y-6 py-6">
      
      <!-- Banner Welcome -->
      <div class="bg-gradient-to-r from-[#075955] to-[#05403d] p-8 rounded-[32px] shadow-lg border border-[#05403d] text-white relative overflow-hidden">
        <div class="absolute -right-8 -bottom-8 w-40 h-40 bg-white/10 rounded-full blur-3xl"></div>
        <div class="relative z-10 flex flex-col md:flex-row items-center gap-6">
          <div class="relative">
            <img 
              alt="Profile Avatar" 
              class="w-24 h-24 rounded-3xl object-cover border-4 border-white/20 shadow-xl" 
              :src="`https://ui-avatars.com/api/?name=${encodeURIComponent(user.fullName)}&background=ffffff&color=075955&bold=true&size=128`"
            />
            <div class="absolute -bottom-2 -right-2 bg-yellow-400 border-2 border-white w-6 h-6 rounded-full flex items-center justify-center shadow-md">
              <span class="material-symbols-outlined text-[14px] text-[#075955] font-black">verified</span>
            </div>
          </div>
          <div class="text-center md:text-left">
            <div class="flex items-center justify-center md:justify-start gap-2 mb-1">
              <h1 class="text-2xl font-black">{{ user.fullName }}</h1>
              <span class="bg-white/20 backdrop-blur-sm text-white text-[9px] font-black uppercase px-2 py-0.5 rounded-full border border-white/10">
                {{ user.role === 'ADMIN' ? 'Quản trị viên' : 'Hạng VIP' }}
              </span>
            </div>
            <p class="text-white/70 font-bold flex items-center justify-center md:justify-start gap-1">
              <span class="material-symbols-outlined text-[18px]">call</span>
              {{ user.phone }}
            </p>
          </div>
        </div>
      </div>

      <!-- Wallet & Loyalty -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Wallet Card -->
        <section class="bg-white p-6 rounded-[32px] shadow-sm border border-slate-200 flex flex-col justify-between relative overflow-hidden group">
          <div class="relative z-10">
            <div class="flex justify-between items-start mb-4">
              <div class="w-12 h-12 bg-[#075955]/10 rounded-2xl flex items-center justify-center text-[#075955]">
                <span class="material-symbols-outlined text-2xl font-black">account_balance_wallet</span>
              </div>
              <p class="text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Ví Trung - Nam</p>
            </div>
            <p class="text-sm font-bold text-slate-500 mb-1">Số dư hiện tại</p>
            <h3 class="text-3xl font-black text-[#075955] tracking-tight">
              {{ user.walletBalance ? user.walletBalance.toLocaleString('vi-VN') : '0' }}<span class="text-sm ml-1 underline">đ</span>
            </h3>
          </div>
          <button class="mt-6 w-full bg-[#075955] hover:bg-[#05403d] text-white text-xs font-black py-3.5 rounded-2xl transition-all duration-200 active:scale-95 shadow-md">
            + NẠP THÊM TIỀN
          </button>
        </section>

        <!-- Points Card -->
        <section class="bg-white p-6 rounded-[32px] shadow-sm border border-slate-200 flex flex-col justify-between relative overflow-hidden">
          <div>
            <div class="flex justify-between items-start mb-4">
              <div class="w-12 h-12 bg-amber-50 rounded-2xl flex items-center justify-center text-amber-500">
                <span class="material-symbols-outlined text-2xl font-black">stars</span>
              </div>
              <p class="text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Điểm thưởng</p>
            </div>
            <p class="text-sm font-bold text-slate-500 mb-1">Loyalty Points</p>
            <h3 class="text-3xl font-black text-amber-500 tracking-tight">
              1,250<span class="text-sm ml-1">pts</span>
            </h3>
          </div>
          <div class="mt-6 flex items-center gap-2 bg-slate-50 p-3 rounded-2xl border border-slate-100">
            <span class="material-symbols-outlined text-emerald-500 text-sm">confirmation_number</span>
            <p class="text-[10px] font-bold text-slate-600">Bạn có 2 mã giảm giá chưa dùng</p>
          </div>
        </section>
      </div>

      <!-- Settings Menu -->
      <section class="bg-white rounded-[32px] shadow-sm border border-slate-200 overflow-hidden">
        <div class="p-6 border-b border-slate-50 bg-slate-50/50">
          <h3 class="text-xs font-black text-slate-800 uppercase tracking-[0.2em]">Cài đặt tài khoản</h3>
        </div>
        
        <div class="divide-y divide-slate-50">
          <div @click="$router.push('/history')" class="flex items-center justify-between p-6 hover:bg-slate-50 transition-all cursor-pointer group">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-500 group-hover:bg-[#075955]/10 group-hover:text-[#075955] transition-all duration-300">
                <span class="material-symbols-outlined text-xl font-black">receipt_long</span>
              </div>
              <div>
                <p class="text-sm font-black text-slate-800">Lịch sử đặt vé</p>
                <p class="text-[11px] font-bold text-slate-400">Xem lại tất cả chuyến đi đã mua</p>
              </div>
            </div>
            <span class="material-symbols-outlined text-slate-300 group-hover:text-[#075955] group-hover:translate-x-1 transition-transform">chevron_right</span>
          </div>

          <div @click="$router.push('/ai-assistant')" class="flex items-center justify-between p-6 hover:bg-slate-50 transition-all cursor-pointer group">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-500 group-hover:bg-[#075955]/10 group-hover:text-[#075955] transition-all duration-300">
                <span class="material-symbols-outlined text-xl font-black">robot_2</span>
              </div>
              <div>
                <p class="text-sm font-black text-slate-800">Trợ lý ảo AI</p>
                <p class="text-[11px] font-bold text-slate-400">Hỏi đáp về hành trình & chính sách</p>
              </div>
            </div>
            <span class="material-symbols-outlined text-slate-300 group-hover:text-[#075955] group-hover:translate-x-1 transition-transform">chevron_right</span>
          </div>

          <div @click="handleLogout" class="flex items-center justify-between p-6 hover:bg-red-50 transition-all cursor-pointer group">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-2xl bg-red-50 flex items-center justify-center text-red-500 group-hover:bg-red-500 group-hover:text-white transition-all duration-300">
                <span class="material-symbols-outlined text-xl font-black">logout</span>
              </div>
              <div>
                <p class="text-sm font-black text-red-600">Đăng xuất</p>
                <p class="text-[11px] font-bold text-red-400">Thoát khỏi phiên làm việc hiện tại</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Footer Info -->
      <footer class="py-8 text-center">
        <p class="text-[9px] font-black tracking-[0.3em] uppercase text-slate-300">Trung - Nam Enterprise v3.2.0 • Secure Session</p>
      </footer>
    </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const user = ref(null);
const loading = ref(true);

const checkAuth = async () => {
  if (!authStore.isLoggedIn) {
    router.push('/auth/login');
    loading.value = false;
    return;
  }

  try {
    // 📡 Đồng bộ thời gian thực từ server dùng JWT token
    const freshUser = await authStore.fetchMe();
    user.value = freshUser || authStore.currentUser;
  } catch (err) {
    console.error("Lỗi kết nối server, dùng cache:", err);
    user.value = authStore.currentUser;
    if (!user.value) router.push('/auth/login');
  } finally {
    loading.value = false;
  }
};

const handleLogout = () => {
  if (confirm("Bạn thực sự muốn đăng xuất khỏi hệ thống SkyBus?")) {
    authStore.logout();
    router.push('/auth/login');
  }
};

onMounted(() => {
  checkAuth();
});
</script>


<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fadeIn 0.35s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>
