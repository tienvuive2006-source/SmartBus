<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 2xl:grid-cols-6 gap-4">
    
    <!-- Doanh thu Card -->
    <div class="bg-white rounded-2xl p-4 border border-gray-100 shadow-[0_4px_20px_rgb(0,0,0,0.03)] hover:shadow-[0_4px_25px_rgb(0,0,0,0.06)] transition-all relative group cursor-help">
      <div class="flex items-start gap-3">
        <div class="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined text-[20px]">payments</span>
        </div>
        <div class="min-w-0 flex-1">
          <p class="text-[12px] font-bold text-gray-500 mb-1">Doanh thu hôm nay</p>
          <h3 class="text-xl font-black text-gray-900 tracking-tight whitespace-nowrap">{{ formatCurrency(stats.todayRevenue) }}</h3>
          <p class="text-[10px] font-bold mt-1.5 flex items-center gap-1 whitespace-nowrap" :class="getGrowthClass(stats.revenueGrowth)">
            <span class="material-symbols-outlined text-[12px]">{{ getGrowthIcon(stats.revenueGrowth) }}</span>
            {{ Math.abs(stats.revenueGrowth || 0) }}% <span class="text-gray-400 font-medium">so với hôm qua</span>
          </p>
        </div>
      </div>

      <!-- Tooltip Breakdown -->
      <div class="absolute left-0 top-full mt-2 w-full z-50 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
        <div class="bg-slate-800 text-white rounded-xl p-4 shadow-xl text-xs space-y-2 relative border border-slate-700">
          <!-- Arrow pointing up -->
          <div class="absolute -top-1.5 left-6 w-3 h-3 bg-slate-800 rotate-45 border-l border-t border-slate-700"></div>
          
          <div class="flex justify-between items-center pb-2 border-b border-slate-700">
            <span class="font-bold text-slate-300">Tổng tất cả (All-time)</span>
            <span class="font-black text-emerald-400">{{ formatCurrency(stats.totalRevenue) }}</span>
          </div>
          <div class="py-2 border-b border-slate-700">
            <div class="flex justify-between items-center mt-1.5">
              <span class="text-slate-300">Tuần này</span>
              <span class="font-bold tracking-tight text-white">{{ formatCurrency(stats.thisWeekRevenue) }}</span>
            </div>
            <div class="flex justify-between items-center mt-1.5">
              <span class="text-slate-300">Tháng này</span>
              <span class="font-bold tracking-tight text-white">{{ formatCurrency(stats.thisMonthRevenue) }}</span>
            </div>
          </div>
          <div class="pt-1">
            <p class="text-slate-400 font-semibold mb-2">Cơ cấu doanh thu (All-time):</p>
            <div class="flex justify-between items-center mt-1.5">
              <span class="text-slate-300 flex items-center gap-1.5"><span class="w-1.5 h-1.5 rounded-full bg-blue-400"></span> CK/Ngân hàng</span>
              <span class="font-bold tracking-tight">{{ formatCurrency(stats.bankRevenue) }}</span>
            </div>
            <div class="flex justify-between items-center mt-1.5">
              <span class="text-slate-300 flex items-center gap-1.5"><span class="w-1.5 h-1.5 rounded-full bg-amber-400"></span> Tiền mặt</span>
              <span class="font-bold tracking-tight">{{ formatCurrency(stats.cashRevenue) }}</span>
            </div>
            <div class="flex justify-between items-center mt-1.5">
              <span class="text-slate-300 flex items-center gap-1.5"><span class="w-1.5 h-1.5 rounded-full bg-purple-400"></span> Ví SmartBus</span>
              <span class="font-bold tracking-tight">{{ formatCurrency(stats.walletRevenue) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Vé đã bán Card -->
    <div class="bg-white rounded-2xl p-4 border border-gray-100 shadow-[0_4px_20px_rgb(0,0,0,0.03)] hover:shadow-[0_4px_25px_rgb(0,0,0,0.06)] transition-all relative group cursor-help">
      <div class="flex items-start gap-3">
        <div class="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined text-[20px]">confirmation_number</span>
        </div>
        <div class="min-w-0 flex-1">
          <p class="text-[12px] font-bold text-gray-500 mb-1">Vé bán hôm nay</p>
          <h3 class="text-xl font-black text-gray-900 tracking-tight whitespace-nowrap">{{ stats.todayTickets }} <span class="text-sm font-medium text-gray-400">vé</span></h3>
          <p class="text-[10px] font-bold mt-1.5 flex items-center gap-1 whitespace-nowrap" :class="getGrowthClass(stats.ticketsGrowth)">
            <span class="material-symbols-outlined text-[12px]">{{ getGrowthIcon(stats.ticketsGrowth) }}</span>
            {{ Math.abs(stats.ticketsGrowth || 0) }}% <span class="text-gray-400 font-medium">so với hôm qua</span>
          </p>
        </div>
      </div>

      <!-- Tooltip Breakdown -->
      <div class="absolute left-0 top-full mt-2 w-full z-50 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
        <div class="bg-slate-800 text-white rounded-xl p-4 shadow-xl text-xs space-y-2 relative border border-slate-700">
          <!-- Arrow pointing up -->
          <div class="absolute -top-1.5 left-6 w-3 h-3 bg-slate-800 rotate-45 border-l border-t border-slate-700"></div>
          
          <div class="flex justify-between items-center pb-2 border-b border-slate-700">
            <span class="font-bold text-slate-300">Tổng tất cả (All-time)</span>
            <span class="font-black text-blue-400">{{ stats.totalTickets }} vé</span>
          </div>
          <div class="pt-2">
            <div class="flex justify-between items-center mt-1.5">
              <span class="text-slate-300">Tuần này</span>
              <span class="font-bold tracking-tight text-white">{{ stats.thisWeekTickets }} vé</span>
            </div>
            <div class="flex justify-between items-center mt-1.5">
              <span class="text-slate-300">Tháng này</span>
              <span class="font-bold tracking-tight text-white">{{ stats.thisMonthTickets }} vé</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Chuyến xe Card -->
    <div class="bg-white rounded-2xl p-4 border border-gray-100 shadow-[0_4px_20px_rgb(0,0,0,0.03)] hover:shadow-[0_4px_25px_rgb(0,0,0,0.06)] transition-all">
      <div class="flex items-start gap-3">
        <div class="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined text-[20px]">directions_bus</span>
        </div>
        <div class="min-w-0 flex-1">
          <p class="text-[12px] font-bold text-gray-500 mb-1">Chuyến xe hoạt động</p>
          <h3 class="text-xl font-black text-gray-900 tracking-tight whitespace-nowrap">{{ stats.totalTrips }}</h3>
          <p class="text-[10px] font-bold text-gray-500 mt-1.5 flex items-center gap-1 whitespace-nowrap">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 shrink-0"></span>
            <span>Đang chạy: {{ Math.floor(stats.totalTrips * 0.8) }}</span>
          </p>
        </div>
      </div>
    </div>

    <!-- Khách hàng Card -->
    <div class="bg-white rounded-2xl p-4 border border-gray-100 shadow-[0_4px_20px_rgb(0,0,0,0.03)] hover:shadow-[0_4px_25px_rgb(0,0,0,0.06)] transition-all">
      <div class="flex items-start gap-3">
        <div class="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined text-[20px]">group</span>
        </div>
        <div class="min-w-0 flex-1">
          <p class="text-[12px] font-bold text-gray-500 mb-1">Khách hàng mới hôm nay</p>
          <h3 class="text-xl font-black text-gray-900 tracking-tight whitespace-nowrap">{{ stats.totalCustomers }}</h3>
          <p class="text-[10px] font-bold mt-1.5 flex items-center gap-1 whitespace-nowrap" :class="getGrowthClass(stats.customersGrowth)">
            <span class="material-symbols-outlined text-[12px]">{{ getGrowthIcon(stats.customersGrowth) }}</span>
            {{ Math.abs(stats.customersGrowth || 0) }}% <span class="text-gray-400 font-medium">so với hôm qua</span>
          </p>
        </div>
      </div>
    </div>

    <!-- Tỉ lệ lấp đầy Card -->
    <div class="bg-white rounded-2xl p-4 border border-gray-100 shadow-[0_4px_20px_rgb(0,0,0,0.03)] hover:shadow-[0_4px_25px_rgb(0,0,0,0.06)] transition-all">
      <div class="flex items-start gap-3">
        <div class="w-10 h-10 rounded-xl bg-teal-50 text-teal-600 flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined text-[20px]">airline_seat_recline_normal</span>
        </div>
        <div class="min-w-0 flex-1">
          <p class="text-[12px] font-bold text-gray-500 mb-1">Tỷ lệ lấp đầy ghế</p>
          <h3 class="text-xl font-black text-gray-900 tracking-tight whitespace-nowrap">{{ stats.occupancyRate }}%</h3>
          <p class="text-[10px] font-bold mt-1.5 flex items-center gap-1 whitespace-nowrap" :class="getGrowthClass(stats.occupancyGrowth)">
            <span class="material-symbols-outlined text-[12px]">{{ getGrowthIcon(stats.occupancyGrowth) }}</span>
            {{ Math.abs(stats.occupancyGrowth || 0) }}% <span class="text-gray-400 font-medium">so với hôm qua</span>
          </p>
        </div>
      </div>
    </div>

    <!-- Xe hoạt động Card -->
    <div class="bg-white rounded-2xl p-4 border border-gray-100 shadow-[0_4px_20px_rgb(0,0,0,0.03)] hover:shadow-[0_4px_25px_rgb(0,0,0,0.06)] transition-all">
      <div class="flex items-start gap-3">
        <div class="w-10 h-10 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined text-[20px]">local_shipping</span>
        </div>
        <div class="min-w-0 flex-1">
          <p class="text-[12px] font-bold text-gray-500 mb-1">Xe đang hoạt động</p>
          <h3 class="text-xl font-black text-gray-900 tracking-tight whitespace-nowrap">{{ stats.activeBuses }} <span class="text-sm font-medium text-gray-400">/{{ stats.totalBuses }}</span></h3>
          <p class="text-[10px] font-bold text-gray-500 mt-1.5 flex items-center gap-1 whitespace-nowrap">
            <span class="w-1.5 h-1.5 rounded-full bg-rose-500 shrink-0"></span>
            <span>Bảo dưỡng: {{ stats.totalBuses - stats.activeBuses }}</span>
          </p>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
defineProps({
  stats: {
    type: Object,
    required: true
  }
});

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
};

const getGrowthClass = (growth) => {
  if (!growth || growth === 0) return 'text-gray-400';
  return growth > 0 ? 'text-emerald-600' : 'text-rose-600';
};

const getGrowthIcon = (growth) => {
  if (!growth || growth === 0) return 'remove';
  return growth > 0 ? 'arrow_upward' : 'arrow_downward';
};
</script>
