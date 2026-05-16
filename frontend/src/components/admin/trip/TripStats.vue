<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
    <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col justify-between hover:shadow-md transition-shadow">
      <span class="text-slate-500 font-black text-[10px] uppercase tracking-widest">Tổng số lộ trình</span>
      <div class="flex items-end justify-between mt-3">
        <span class="font-black text-4xl text-[#075955] tracking-tighter">{{ totalTrips }}</span>
        <div class="w-12 h-12 rounded-xl bg-[#075955]/10 flex items-center justify-center text-[#075955]">
          <span class="material-symbols-outlined text-3xl">route</span>
        </div>
      </div>
    </div>
    
    <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 border-l-4 border-emerald-500 flex flex-col justify-between hover:shadow-md transition-shadow">
      <span class="text-emerald-700 font-black text-[10px] uppercase tracking-widest">Chuyến xe hôm nay</span>
      <div class="flex items-end justify-between mt-3">
        <span class="font-black text-4xl text-emerald-600 tracking-tighter">{{ todayTrips }}</span>
        <div class="w-12 h-12 rounded-xl bg-emerald-50 flex items-center justify-center text-emerald-500">
          <span class="material-symbols-outlined text-3xl">calendar_today</span>
        </div>
      </div>
    </div>

    <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 border-l-4 border-amber-500 flex flex-col justify-between hover:shadow-md transition-shadow">
      <span class="text-amber-700 font-black text-[10px] uppercase tracking-widest">Tỷ lệ ghế trống</span>
      <div class="flex items-end justify-between mt-3">
        <span class="font-black text-4xl text-amber-600 tracking-tighter">{{ totalEmptySeats }}</span>
        <div class="w-12 h-12 rounded-xl bg-amber-50 flex items-center justify-center text-amber-500">
          <span class="material-symbols-outlined text-3xl">airline_seat_recline_normal</span>
        </div>
      </div>
    </div>

    <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 border-l-4 border-blue-500 flex flex-col justify-between hover:shadow-md transition-shadow">
      <span class="text-blue-700 font-black text-[10px] uppercase tracking-widest">Giá vé trung bình</span>
      <div class="flex items-end justify-between mt-3">
        <span class="font-black text-3xl text-blue-600 tracking-tighter">{{ averagePrice.toLocaleString() }}đ</span>
        <div class="w-12 h-12 rounded-xl bg-blue-50 flex items-center justify-center text-blue-500">
          <span class="material-symbols-outlined text-3xl">payments</span>
        </div>
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
