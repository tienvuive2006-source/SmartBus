<template>
  <div class="p-container-margin md:p-8 max-w-7xl mx-auto space-y-8 animate-fade-in">
    
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-surface-variant/30 pb-6">
      <div>
        <h1 class="text-headline-lg font-headline-lg font-black text-on-background flex items-center gap-3">
          <span class="material-symbols-outlined text-4xl text-primary">analytics</span>
          Tổng Quan Hệ Thống
        </h1>
        <p class="text-body-md font-body-md text-on-surface-variant mt-1 flex items-center gap-1.5">
          <span class="inline-block w-2 h-2 bg-emerald-500 rounded-full animate-ping"></span>
          Dữ liệu trực tuyến • Cập nhật lúc {{ stats.lastUpdated || '--:--' }}, Hôm nay
        </p>
      </div>
      <div class="flex gap-4">
        <button 
          @click="fetchStats"
          class="bg-surface-container text-on-surface-variant px-5 py-2.5 rounded-xl text-label-md font-black flex items-center gap-2 hover:bg-surface-container-high active:scale-95 transition-all border border-outline-variant/50"
        >
          <span class="material-symbols-outlined text-[20px]">refresh</span>
          LÀM MỚI SỐ LIỆU
        </button>
        <button class="bg-primary text-on-primary px-5 py-2.5 rounded-xl text-label-md font-black hover:bg-surface-tint active:scale-95 transition-all shadow-sm flex items-center gap-2">
          <span class="material-symbols-outlined text-[20px]">download</span>
          XUẤT BÁO CÁO
        </button>
      </div>
    </div>

    <!-- Key Metrics Bento Grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      
      <!-- Total Trips Card (NEW) -->
      <div class="bg-white rounded-3xl p-6 border border-outline-variant/25 shadow-[0px_8px_24px_rgba(0,0,0,0.02)] relative overflow-hidden hover:shadow-md transition-all duration-300">
        <div class="flex justify-between items-start mb-4">
          <div class="w-12 h-12 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center">
            <span class="material-symbols-outlined text-2xl">route</span>
          </div>
          <span class="text-[11px] font-black bg-indigo-100 text-indigo-700 px-2.5 py-0.5 rounded-full uppercase tracking-wider">
            Tổng chuyến
          </span>
        </div>
        <p class="text-body-md font-bold text-on-surface-variant mb-0.5">Chuyến xe hoạt động</p>
        <h3 class="text-headline-lg font-black text-on-background tracking-tight">{{ stats.totalTrips }} chuyến</h3>
      </div>

      <!-- Revenue Card -->
      <div class="bg-white rounded-3xl p-6 border border-outline-variant/25 shadow-[0px_8px_24px_rgba(0,0,0,0.02)] relative overflow-hidden hover:shadow-md transition-all duration-300">
        <div class="flex justify-between items-start mb-4">
          <div class="w-12 h-12 bg-emerald-50 text-emerald-600 rounded-2xl flex items-center justify-center">
            <span class="material-symbols-outlined text-2xl">payments</span>
          </div>
          <span class="text-[11px] font-black bg-emerald-100 text-emerald-700 px-2.5 py-0.5 rounded-full uppercase tracking-wider">
            Doanh Thu Thật
          </span>
        </div>
        <p class="text-body-md font-bold text-on-surface-variant mb-0.5">Doanh thu hệ thống</p>
        <h3 class="text-headline-lg font-black text-on-background tracking-tight">{{ stats.totalRevenue.toLocaleString('vi-VN') }} ₫</h3>
      </div>

      <!-- Tickets Sold Card -->
      <div class="bg-white rounded-3xl p-6 border border-outline-variant/25 shadow-[0px_8px_24px_rgba(0,0,0,0.02)] relative overflow-hidden hover:shadow-md transition-all duration-300">
        <div class="flex justify-between items-start mb-4">
          <div class="w-12 h-12 bg-amber-50 text-amber-600 rounded-2xl flex items-center justify-center">
            <span class="material-symbols-outlined text-2xl">confirmation_number</span>
          </div>
          <span class="text-[11px] font-black bg-amber-100 text-amber-700 px-2.5 py-0.5 rounded-full uppercase tracking-wider">
            Đã Thanh Toán
          </span>
        </div>
        <p class="text-body-md font-bold text-on-surface-variant mb-0.5">Vé đã bán thành công</p>
        <h3 class="text-headline-lg font-black text-on-background tracking-tight">{{ stats.totalTickets }} vé</h3>
      </div>

      <!-- Active Fleet Card -->
      <div class="bg-white rounded-3xl p-6 border border-outline-variant/25 shadow-[0px_8px_24px_rgba(0,0,0,0.02)] relative overflow-hidden hover:shadow-md transition-all duration-300">
        <div class="flex justify-between items-start mb-4">
          <div class="w-12 h-12 bg-cyan-50 text-cyan-600 rounded-2xl flex items-center justify-center">
            <span class="material-symbols-outlined text-2xl">directions_bus</span>
          </div>
          <span class="text-[11px] font-black bg-cyan-100 text-cyan-700 px-2.5 py-0.5 rounded-full uppercase tracking-wider">
            Đang Hoạt Động
          </span>
        </div>
        <p class="text-body-md font-bold text-on-surface-variant mb-0.5">Quy mô hạm đội xe</p>
        <div class="flex items-baseline gap-2">
          <h3 class="text-headline-lg font-black text-on-background tracking-tight">{{ stats.activeBuses }}</h3>
          <span class="text-body-md font-black text-on-surface-variant">/ {{ stats.totalBuses }} xe trong kho</span>
        </div>
      </div>
    </div>

    <!-- Charts and Notifications Row -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Revenue Chart Area (Đẹp mê hồn) -->
      <div class="lg:col-span-2 bg-white rounded-3xl p-6 border border-outline-variant/25 shadow-[0px_8px_24px_rgba(0,0,0,0.02)]">
        <div class="flex justify-between items-center mb-6 pb-4 border-b border-outline-variant/20">
          <h2 class="text-headline-sm font-headline-sm font-black text-on-background">Biểu đồ xu hướng tuần</h2>
          <span class="text-[11px] font-black bg-primary/10 text-primary px-3 py-1 rounded-lg flex items-center gap-1">
            <span class="material-symbols-outlined text-sm">monitoring</span>
            Theo thời gian thực
          </span>
        </div>
        <div class="h-64 w-full relative flex items-end justify-between gap-2 pt-8">
          <div class="absolute left-0 top-0 h-full flex flex-col justify-between text-label-md font-black text-on-surface-variant/60 pr-4 pb-8">
            <span>1.2M</span>
            <span>900k</span>
            <span>600k</span>
            <span>300k</span>
            <span>0</span>
          </div>
          <div class="flex-1 ml-12 h-full relative flex items-end justify-between px-2 pb-8 border-b-2 border-l-2 border-outline-variant/50">
            <div class="absolute inset-0 flex flex-col justify-between pt-2">
              <div class="border-t border-dashed border-outline-variant/20 w-full"></div>
              <div class="border-t border-dashed border-outline-variant/20 w-full"></div>
              <div class="border-t border-dashed border-outline-variant/20 w-full"></div>
              <div class="border-t border-dashed border-outline-variant/20 w-full"></div>
            </div>
            <!-- Dynamic height based on proportional mockup scaled beautifully -->
            <div class="w-full flex justify-between items-end h-full z-10 px-4 md:px-8">
              <div class="w-8 md:w-10 bg-gradient-to-t from-primary/50 to-primary rounded-t-xl h-[35%] hover:scale-105 hover:shadow-md transition-all relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-black text-white text-[10px] font-black px-2 py-0.5 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">350k</div>
              </div>
              <div class="w-8 md:w-10 bg-gradient-to-t from-primary/50 to-primary rounded-t-xl h-[50%] hover:scale-105 hover:shadow-md transition-all relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-black text-white text-[10px] font-black px-2 py-0.5 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">500k</div>
              </div>
              <div class="w-8 md:w-10 bg-gradient-to-t from-primary/50 to-primary rounded-t-xl h-[40%] hover:scale-105 hover:shadow-md transition-all relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-black text-white text-[10px] font-black px-2 py-0.5 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">400k</div>
              </div>
              <div class="w-8 md:w-10 bg-gradient-to-t from-primary/50 to-primary rounded-t-xl h-[70%] hover:scale-105 hover:shadow-md transition-all relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-black text-white text-[10px] font-black px-2 py-0.5 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">700k</div>
              </div>
              <div class="w-8 md:w-10 bg-gradient-to-t from-primary/50 to-primary rounded-t-xl h-[55%] hover:scale-105 hover:shadow-md transition-all relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-black text-white text-[10px] font-black px-2 py-0.5 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">550k</div>
              </div>
              <div class="w-8 md:w-10 bg-gradient-to-t from-primary/50 to-primary rounded-t-xl h-[85%] hover:scale-105 hover:shadow-md transition-all relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-black text-white text-[10px] font-black px-2 py-0.5 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">850k</div>
              </div>
              <div class="w-8 md:w-10 bg-gradient-to-t from-primary/80 to-primary-container rounded-t-xl h-[95%] hover:scale-105 hover:shadow-md transition-all relative group">
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-black text-white text-[10px] font-black px-2 py-0.5 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">1.0M</div>
              </div>
            </div>
          </div>
          <div class="absolute bottom-0 left-12 right-0 flex justify-between px-4 md:px-8 text-[11px] font-black text-on-surface-variant uppercase">
            <span>Thứ 2</span>
            <span>Thứ 3</span>
            <span>Thứ 4</span>
            <span>Thứ 5</span>
            <span>Thứ 6</span>
            <span>Thứ 7</span>
            <span>Chủ Nhật</span>
          </div>
        </div>
      </div>

      <!-- Notifications List -->
      <div class="bg-white rounded-3xl p-6 border border-outline-variant/25 shadow-[0px_8px_24px_rgba(0,0,0,0.02)] flex flex-col">
        <div class="flex justify-between items-center mb-6 pb-4 border-b border-outline-variant/20">
          <h2 class="text-headline-sm font-headline-sm font-black text-on-background">Trung tâm thông báo</h2>
          <a class="text-primary text-label-md font-black hover:underline" href="#">TẤT CẢ</a>
        </div>
        <div class="flex-1 space-y-4 overflow-y-auto max-h-[250px]">
          <!-- Alert Item -->
          <div class="flex gap-3 p-3.5 bg-error/5 rounded-2xl border border-error/10">
            <div class="w-10 h-10 rounded-xl bg-error text-white flex items-center justify-center shrink-0 shadow-md">
              <span class="material-symbols-outlined text-[20px]">warning</span>
            </div>
            <div>
              <h4 class="text-body-md font-black text-error">Đội xe cần kiểm tra bảo dưỡng</h4>
              <p class="text-label-md text-on-surface-variant mt-0.5">Dựa vào số km, có 1 xe đang ở trạng thái BẢO TRÌ.</p>
              <span class="text-[10px] font-black text-error/70 mt-1 block uppercase tracking-wide">Vừa xong</span>
            </div>
          </div>
          <!-- Info Item -->
          <div class="flex gap-3 p-3.5 bg-primary/5 rounded-2xl border border-primary/10">
            <div class="w-10 h-10 rounded-xl bg-primary text-white flex items-center justify-center shrink-0 shadow-md">
              <span class="material-symbols-outlined text-[20px]">info</span>
            </div>
            <div>
              <h4 class="text-body-md font-black text-primary">Khai thác quy mô Chuyến Xe</h4>
              <p class="text-label-md text-on-surface-variant mt-0.5">Hiện đang có {{ stats.totalTrips }} hành trình được lập lịch vận hành.</p>
              <span class="text-[10px] font-black text-primary/70 mt-1 block uppercase tracking-wide">Cập nhật bởi hệ thống</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';

const api = useApi();

// Khởi tạo trạng thái ban đầu
const stats = ref({
  totalRevenue: 0,
  totalTickets: 0,
  totalTrips: 0,
  totalBuses: 0,
  activeBuses: 0,
  lastUpdated: '--:--'
});

const fetchStats = async () => {
  try {
    const response = await api.get('/dashboard/stats');
    stats.value = response.data;
  } catch (error) {
    console.error("Lỗi tải số liệu Dashboard:", error);
  }
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
</style>
