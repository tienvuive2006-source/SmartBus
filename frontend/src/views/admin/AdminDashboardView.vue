<template>
  <div class="space-y-8">
    
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-gray-200 pb-6">
      <div>
        <h1 class="text-2xl md:text-3xl font-extrabold text-gray-900 flex items-center gap-3 tracking-tight">
          <span class="w-12 h-12 bg-[#075955] text-white rounded-2xl flex items-center justify-center shadow-lg shadow-[#075955]/30">
            <span class="material-symbols-outlined text-2xl">analytics</span>
          </span>
          Tổng Quan Hệ Thống
        </h1>
        <p class="text-sm font-semibold text-gray-500 mt-2 flex items-center gap-2">
          <span class="inline-flex w-2.5 h-2.5 bg-emerald-500 rounded-full animate-pulse shadow-[0_0_8px_rgba(16,185,129,0.8)]"></span>
          Dữ liệu trực tuyến • Cập nhật lúc {{ stats.lastUpdated || '--:--' }}, Hôm nay
        </p>
      </div>
      <div class="flex gap-3">
        <button 
          @click="fetchStats"
          class="bg-white text-gray-700 px-5 py-2.5 rounded-xl text-xs font-bold uppercase tracking-widest flex items-center gap-2 hover:bg-gray-50 hover:shadow-sm active:scale-95 transition-all border border-gray-200 shadow-[0_2px_4px_rgba(0,0,0,0.02)]"
        >
          <span class="material-symbols-outlined text-[18px]">refresh</span>
          Làm mới
        </button>
        <button class="bg-[#075955] text-white px-5 py-2.5 rounded-xl text-xs font-bold uppercase tracking-widest hover:bg-[#05403d] active:scale-95 transition-all shadow-md shadow-[#075955]/20 flex items-center gap-2">
          <span class="material-symbols-outlined text-[18px]">download</span>
          Báo cáo
        </button>
      </div>
    </div>

    <!-- Key Metrics Bento Grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      
      <!-- Total Trips Card -->
      <div class="bg-white rounded-3xl p-6 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] relative overflow-hidden group hover:-translate-y-1 transition-all duration-300">
        <div class="absolute -right-4 -top-4 w-24 h-24 bg-indigo-50 rounded-full opacity-50 group-hover:scale-150 transition-transform duration-500"></div>
        <div class="flex justify-between items-start mb-4 relative z-10">
          <div class="w-12 h-12 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center border border-indigo-100">
            <span class="material-symbols-outlined text-2xl">route</span>
          </div>
          <span class="text-[10px] font-black bg-indigo-50 border border-indigo-100 text-indigo-700 px-2.5 py-1 rounded-full uppercase tracking-widest">
            Tổng chuyến
          </span>
        </div>
        <div class="relative z-10">
           <h3 class="text-4xl font-extrabold text-gray-900 tracking-tight mb-1">{{ stats.totalTrips }} <span class="text-lg text-gray-400 font-semibold tracking-normal">Chuyến</span></h3>
           <p class="text-xs font-bold text-gray-400 uppercase tracking-widest">Chuyến xe hoạt động</p>
        </div>
      </div>

      <!-- Revenue Card -->
      <div class="bg-white rounded-3xl p-6 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] relative overflow-hidden group hover:-translate-y-1 transition-all duration-300">
        <div class="absolute -right-4 -top-4 w-24 h-24 bg-emerald-50 rounded-full opacity-50 group-hover:scale-150 transition-transform duration-500"></div>
        <div class="flex justify-between items-start mb-4 relative z-10">
          <div class="w-12 h-12 bg-emerald-50 text-emerald-600 rounded-2xl flex items-center justify-center border border-emerald-100">
            <span class="material-symbols-outlined text-2xl">payments</span>
          </div>
          <span class="text-[10px] font-black bg-emerald-50 border border-emerald-100 text-emerald-700 px-2.5 py-1 rounded-full uppercase tracking-widest">
            Doanh thu thật
          </span>
        </div>
        <div class="relative z-10">
           <h3 class="text-3xl font-extrabold text-[#075955] tracking-tight mb-1">{{ formatCurrency(stats.totalRevenue) }}</h3>
           <p class="text-xs font-bold text-gray-400 uppercase tracking-widest">Doanh thu hệ thống</p>
        </div>
      </div>

      <!-- Tickets Sold Card -->
      <div class="bg-white rounded-3xl p-6 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] relative overflow-hidden group hover:-translate-y-1 transition-all duration-300">
        <div class="absolute -right-4 -top-4 w-24 h-24 bg-amber-50 rounded-full opacity-50 group-hover:scale-150 transition-transform duration-500"></div>
        <div class="flex justify-between items-start mb-4 relative z-10">
          <div class="w-12 h-12 bg-amber-50 text-amber-600 rounded-2xl flex items-center justify-center border border-amber-100">
            <span class="material-symbols-outlined text-2xl">confirmation_number</span>
          </div>
          <span class="text-[10px] font-black bg-amber-50 border border-amber-100 text-amber-700 px-2.5 py-1 rounded-full uppercase tracking-widest">
            Đã thanh toán
          </span>
        </div>
        <div class="relative z-10">
           <h3 class="text-4xl font-extrabold text-gray-900 tracking-tight mb-1">{{ stats.totalTickets }} <span class="text-lg text-gray-400 font-semibold tracking-normal">Vé</span></h3>
           <p class="text-xs font-bold text-gray-400 uppercase tracking-widest">Vé bán thành công</p>
        </div>
      </div>

      <!-- Active Fleet Card -->
      <div class="bg-white rounded-3xl p-6 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] relative overflow-hidden group hover:-translate-y-1 transition-all duration-300">
        <div class="absolute -right-4 -top-4 w-24 h-24 bg-cyan-50 rounded-full opacity-50 group-hover:scale-150 transition-transform duration-500"></div>
        <div class="flex justify-between items-start mb-4 relative z-10">
          <div class="w-12 h-12 bg-cyan-50 text-cyan-600 rounded-2xl flex items-center justify-center border border-cyan-100">
            <span class="material-symbols-outlined text-2xl">directions_bus</span>
          </div>
          <span class="text-[10px] font-black bg-cyan-50 border border-cyan-100 text-cyan-700 px-2.5 py-1 rounded-full uppercase tracking-widest">
            Đang hoạt động
          </span>
        </div>
        <div class="relative z-10">
           <h3 class="text-4xl font-extrabold text-gray-900 tracking-tight mb-1 flex items-baseline gap-1">
             {{ stats.activeBuses }} <span class="text-lg text-gray-400 font-semibold tracking-normal">/ {{ stats.totalBuses }} xe</span>
           </h3>
           <p class="text-xs font-bold text-gray-400 uppercase tracking-widest">Quy mô hạm đội xe</p>
        </div>
      </div>
    </div>

    <!-- Charts and Notifications Row -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Revenue Chart Area -->
      <div class="lg:col-span-2 bg-white rounded-3xl p-6 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
        <div class="flex justify-between items-center mb-6 pb-4 border-b border-gray-100">
          <h2 class="text-lg font-black text-gray-900 uppercase tracking-widest">Biểu đồ xu hướng tuần</h2>
          <span class="text-[10px] font-black bg-emerald-50 text-[#075955] px-3 py-1.5 rounded-xl border border-emerald-100 flex items-center gap-1 uppercase tracking-widest">
            <span class="material-symbols-outlined text-sm">monitoring</span>
            Theo thời gian thực
          </span>
        </div>
        <div class="h-64 w-full relative flex items-end justify-between gap-2 pt-8">
          <div class="absolute left-0 top-0 h-full flex flex-col justify-between text-[10px] font-black text-gray-400 uppercase tracking-widest pr-4 pb-8">
            <span>{{ formatShortCurrency(maxWeeklyRevenue) }}</span>
            <span>{{ formatShortCurrency(maxWeeklyRevenue * 0.75) }}</span>
            <span>{{ formatShortCurrency(maxWeeklyRevenue * 0.5) }}</span>
            <span>{{ formatShortCurrency(maxWeeklyRevenue * 0.25) }}</span>
            <span>0</span>
          </div>
          <div class="flex-1 ml-12 h-full relative flex items-end justify-between px-2 pb-8 border-b-2 border-l-2 border-gray-100">
            <div class="absolute inset-0 flex flex-col justify-between pt-2">
              <div class="border-t border-dashed border-gray-200 w-full"></div>
              <div class="border-t border-dashed border-gray-200 w-full"></div>
              <div class="border-t border-dashed border-gray-200 w-full"></div>
              <div class="border-t border-dashed border-gray-200 w-full"></div>
            </div>
            <!-- Bars -->
            <div class="w-full flex justify-between items-end h-full z-10 px-4 md:px-8">
              <div 
                v-for="(rev, index) in stats.weeklyRevenue || [0,0,0,0,0,0,0]" 
                :key="index"
                :style="{ height: `${Math.max((rev / maxWeeklyRevenue) * 100, 5)}%` }"
                class="w-8 md:w-12 rounded-t-lg transition-all duration-700 hover:scale-105 relative group"
                :class="index === 6 ? 'bg-gradient-to-t from-emerald-500/80 to-[#075955] shadow-[0_0_15px_rgba(7,89,85,0.4)]' : 'bg-gradient-to-t from-[#075955]/30 to-[#075955]'"
              >
                <!-- Tooltip hover -->
                <div class="absolute -top-10 left-1/2 -translate-x-1/2 bg-gray-900 text-white text-[10px] font-bold px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none">
                  {{ formatCurrency(rev) }}
                </div>
              </div>
            </div>
          </div>
          <div class="absolute bottom-0 left-12 right-0 flex justify-between px-4 md:px-8 text-[10px] font-bold text-gray-400 uppercase tracking-widest">
            <span>T2</span>
            <span>T3</span>
            <span>T4</span>
            <span>T5</span>
            <span>T6</span>
            <span>T7</span>
            <span class="text-[#075955] font-black">CN</span>
          </div>
        </div>
      </div>

      <!-- Notifications List -->
      <div class="bg-white rounded-3xl p-6 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] flex flex-col">
        <div class="flex justify-between items-center mb-6 pb-4 border-b border-gray-100">
          <h2 class="text-lg font-black text-gray-900 uppercase tracking-widest">Thông báo</h2>
          <button class="text-[10px] font-black bg-gray-50 text-gray-600 px-3 py-1.5 rounded-xl border border-gray-200 uppercase tracking-widest hover:bg-gray-100 transition-colors">
            Tất cả
          </button>
        </div>
        <div class="flex-1 space-y-4 overflow-y-auto max-h-[300px] pr-2">
          <!-- Alert Item -->
          <div class="flex gap-4 p-4 bg-red-50/50 rounded-2xl border border-red-100/50 hover:bg-red-50 transition-colors">
            <div class="w-10 h-10 rounded-xl bg-red-500 text-white flex items-center justify-center shrink-0 shadow-md shadow-red-500/20">
              <span class="material-symbols-outlined text-[20px]">warning</span>
            </div>
            <div>
              <h4 class="text-sm font-black text-red-700">Đội xe cần bảo dưỡng</h4>
              <p class="text-xs font-semibold text-gray-500 mt-1">Dựa vào số km, có 1 xe đang ở trạng thái BẢO TRÌ.</p>
              <span class="text-[10px] font-black text-red-500/70 mt-2 block uppercase tracking-widest">Vừa xong</span>
            </div>
          </div>
          <!-- Info Item -->
          <div class="flex gap-4 p-4 bg-emerald-50/50 rounded-2xl border border-emerald-100/50 hover:bg-emerald-50 transition-colors">
            <div class="w-10 h-10 rounded-xl bg-[#075955] text-white flex items-center justify-center shrink-0 shadow-md shadow-[#075955]/20">
              <span class="material-symbols-outlined text-[20px]">info</span>
            </div>
            <div>
              <h4 class="text-sm font-black text-[#075955]">Khai thác quy mô chuyến</h4>
              <p class="text-xs font-semibold text-gray-500 mt-1">Hiện đang có {{ stats.totalTrips }} hành trình được lập lịch vận hành.</p>
              <span class="text-[10px] font-black text-emerald-600/60 mt-2 block uppercase tracking-widest">Hệ thống • 2 phút trước</span>
            </div>
          </div>
          <!-- Success Item -->
          <div class="flex gap-4 p-4 bg-gray-50/50 rounded-2xl border border-gray-100 hover:bg-gray-50 transition-colors">
            <div class="w-10 h-10 rounded-xl bg-gray-800 text-white flex items-center justify-center shrink-0 shadow-md">
              <span class="material-symbols-outlined text-[20px]">task_alt</span>
            </div>
            <div>
              <h4 class="text-sm font-black text-gray-800">Đồng bộ SePay API</h4>
              <p class="text-xs font-semibold text-gray-500 mt-1">Hệ thống đã nhận 12 giao dịch thanh toán tự động trong giờ qua.</p>
              <span class="text-[10px] font-black text-gray-400 mt-2 block uppercase tracking-widest">Webhook • 15 phút trước</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useApi } from '@/composables/useApi';

const api = useApi();

const stats = ref({
  totalRevenue: 0,
  totalTickets: 0,
  totalTrips: 0,
  totalBuses: 0,
  activeBuses: 0,
  weeklyRevenue: [0, 0, 0, 0, 0, 0, 0],
  lastUpdated: '--:--'
});

const maxWeeklyRevenue = computed(() => {
  const max = Math.max(...(stats.value.weeklyRevenue || [0,0,0,0,0,0,0]));
  return max > 0 ? max : 1000000; // Tránh chia cho 0
});

const fetchStats = async () => {
  try {
    const response = await api.get('/dashboard/stats');
    stats.value = response.data;
  } catch (error) {
    console.error("Lỗi tải số liệu Dashboard:", error);
  }
};

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const formatShortCurrency = (value) => {
  if (value >= 1000000) return (value / 1000000).toFixed(1) + 'Tr';
  if (value >= 1000) return (value / 1000).toFixed(0) + 'K';
  return value.toString();
};

onMounted(() => {
  fetchStats();
});
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

::-webkit-scrollbar {
  width: 4px;
}
::-webkit-scrollbar-track {
  background: transparent; 
}
::-webkit-scrollbar-thumb {
  background: #e2e8f0; 
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1; 
}
</style>
