<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
    <div class="bg-white p-6 rounded-2xl shadow-[0px_4px_24px_rgba(0,0,0,0.03)] border border-outline-variant/20 flex flex-col justify-between">
      <span class="text-on-surface-variant font-black text-label-md uppercase tracking-wider">Tổng xe trong kho</span>
      <div class="flex items-end justify-between mt-3">
        <span class="font-black text-5xl text-primary tracking-tighter">{{ totalBuses }}</span>
        <div class="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center text-primary">
          <span class="material-symbols-outlined text-3xl">inventory_2</span>
        </div>
      </div>
    </div>
    
    <div class="bg-white p-6 rounded-2xl shadow-[0px_4px_24px_rgba(0,0,0,0.03)] border border-outline-variant/20 border-l-4 border-emerald-500 flex flex-col justify-between">
      <span class="text-emerald-700 font-black text-label-md uppercase tracking-wider">Xe đang lăn bánh</span>
      <div class="flex items-end justify-between mt-3">
        <span class="font-black text-5xl text-emerald-600 tracking-tighter">{{ activeBuses }}</span>
        <div class="w-12 h-12 rounded-xl bg-emerald-50 flex items-center justify-center text-emerald-500">
          <span class="material-symbols-outlined text-3xl animate-pulse">local_shipping</span>
        </div>
      </div>
    </div>

    <div class="bg-white p-6 rounded-2xl shadow-[0px_4px_24px_rgba(0,0,0,0.03)] border border-outline-variant/20 border-l-4 border-amber-500 flex flex-col justify-between">
      <span class="text-amber-700 font-black text-label-md uppercase tracking-wider">Đang bảo dưỡng</span>
      <div class="flex items-end justify-between mt-3">
        <span class="font-black text-5xl text-amber-600 tracking-tighter">{{ maintenanceBuses }}</span>
        <div class="w-12 h-12 rounded-xl bg-amber-50 flex items-center justify-center text-amber-500">
          <span class="material-symbols-outlined text-3xl animate-spin">engineering</span>
        </div>
      </div>
    </div>

    <div class="bg-white p-6 rounded-2xl shadow-[0px_4px_24px_rgba(0,0,0,0.03)] border border-outline-variant/20 border-l-4 border-slate-400 flex flex-col justify-between">
      <span class="text-slate-500 font-black text-label-md uppercase tracking-wider">Đang đậu trong bãi</span>
      <div class="flex items-end justify-between mt-3">
        <span class="font-black text-5xl text-slate-600 tracking-tighter">{{ idleBuses }}</span>
        <div class="w-12 h-12 rounded-xl bg-slate-50 flex items-center justify-center text-slate-400">
          <span class="material-symbols-outlined text-3xl">garage_home</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  totalBuses: Number,
  activeBuses: Number,
  maintenanceBuses: Number,
  idleBuses: Number
});
</script>
