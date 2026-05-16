<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
    <div class="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm hover:shadow-md transition-shadow">
      <div class="w-12 h-12 bg-[#075955]/10 rounded-2xl flex items-center justify-center text-[#075955] mb-4">
        <span class="material-symbols-outlined text-2xl">confirmation_number</span>
      </div>
      <p class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Tổng đơn đặt vé</p>
      <p class="text-3xl font-black text-slate-800 mt-1">{{ totalCount }}</p>
    </div>

    <div class="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm border-l-4 border-emerald-500 hover:shadow-md transition-shadow">
      <div class="w-12 h-12 bg-emerald-50 rounded-2xl flex items-center justify-center text-emerald-600 mb-4">
        <span class="material-symbols-outlined text-2xl">check_circle</span>
      </div>
      <p class="text-[10px] font-black text-emerald-700 uppercase tracking-widest">Đã thanh toán</p>
      <p class="text-3xl font-black text-emerald-600 mt-1">{{ paidCount }}</p>
    </div>

    <div class="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm border-l-4 border-amber-500 hover:shadow-md transition-shadow">
      <div class="w-12 h-12 bg-amber-50 rounded-2xl flex items-center justify-center text-amber-600 mb-4">
        <span class="material-symbols-outlined text-2xl">hourglass_empty</span>
      </div>
      <p class="text-[10px] font-black text-amber-700 uppercase tracking-widest">Chờ thanh toán</p>
      <p class="text-3xl font-black text-amber-600 mt-1">{{ pendingCount }}</p>
    </div>

    <div class="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm border-l-4 border-rose-500 hover:shadow-md transition-shadow">
      <div class="w-12 h-12 bg-rose-50 rounded-2xl flex items-center justify-center text-rose-600 mb-4">
        <span class="material-symbols-outlined text-2xl">cancel</span>
      </div>
      <p class="text-[10px] font-black text-rose-700 uppercase tracking-widest">Đơn đã hủy</p>
      <p class="text-3xl font-black text-rose-600 mt-1">{{ cancelledCount }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  totalCount: Number,
  paidCount: Number,
  pendingCount: Number,
  cancelledCount: Number
});
</script>
