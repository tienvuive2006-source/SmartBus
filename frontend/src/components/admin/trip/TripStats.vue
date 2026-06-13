<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
    <div class="bg-white p-4 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4 hover:shadow-md transition-shadow">
      <div class="w-10 h-10 bg-slate-50 rounded-xl flex items-center justify-center text-slate-500 border border-slate-100">
        <span class="material-symbols-outlined text-xl">route</span>
      </div>
      <div>
        <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Tổng lộ trình</p>
        <p class="text-xl font-extrabold text-slate-800">{{ totalTrips }}</p>
      </div>
    </div>
    
    <div class="bg-white p-4 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4 hover:shadow-md transition-shadow">
      <div class="w-10 h-10 bg-emerald-50 rounded-xl flex items-center justify-center text-emerald-600 border border-emerald-100">
        <span class="material-symbols-outlined text-xl">calendar_today</span>
      </div>
      <div>
        <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Chuyến hôm nay</p>
        <p class="text-xl font-extrabold text-emerald-600">{{ todayTrips }}</p>
      </div>
    </div>

    <div class="bg-white p-4 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4 hover:shadow-md transition-shadow">
      <div class="w-10 h-10 bg-amber-50 rounded-xl flex items-center justify-center text-amber-600 border border-amber-100">
        <span class="material-symbols-outlined text-xl">airline_seat_recline_normal</span>
      </div>
      <div>
        <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Tổng ghế trống</p>
        <p class="text-xl font-extrabold text-amber-600">{{ totalEmptySeats }}</p>
      </div>
    </div>

    <div class="bg-white p-4 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4 hover:shadow-md transition-shadow">
      <div class="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center text-blue-500 border border-blue-100">
        <span class="material-symbols-outlined text-xl">payments</span>
      </div>
      <div>
        <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Giá trung bình</p>
        <p class="text-xl font-extrabold text-blue-600">{{ (averagePrice / 1000).toFixed(0) }}k</p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  totalTrips: Number,
  todayTrips: Number,
  totalEmptySeats: Number,
  averagePrice: Number
});
</script>
