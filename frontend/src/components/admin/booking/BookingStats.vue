<template>
  <div class="grid grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
    <div class="bg-white p-4 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4">
      <div class="w-10 h-10 bg-slate-50 rounded-xl flex items-center justify-center text-slate-500 border border-slate-100">
        <span class="material-symbols-outlined text-xl">receipt_long</span>
      </div>
      <div>
        <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Tổng đơn</p>
        <p class="text-xl font-extrabold text-slate-800">{{ totalCount }}</p>
      </div>
    </div>

    <div class="bg-white p-4 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4">
      <div class="w-10 h-10 bg-emerald-50 rounded-xl flex items-center justify-center text-emerald-600 border border-emerald-100">
        <span class="material-symbols-outlined text-xl">check_circle</span>
      </div>
      <div>
        <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Đã thanh toán</p>
        <p class="text-xl font-extrabold text-emerald-600">{{ paidCount }}</p>
      </div>
    </div>

    <div class="bg-white p-4 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4">
      <div class="w-10 h-10 bg-amber-50 rounded-xl flex items-center justify-center text-amber-600 border border-amber-100">
        <span class="material-symbols-outlined text-xl">pending_actions</span>
      </div>
      <div>
        <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Chờ thanh toán</p>
        <p class="text-xl font-extrabold text-amber-600">{{ pendingCount }}</p>
      </div>
    </div>

    <div class="bg-white p-4 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4">
      <div class="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600 border border-blue-100">
        <span class="material-symbols-outlined text-xl">directions_bus</span>
      </div>
      <div>
        <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Đã lên xe</p>
        <p class="text-xl font-extrabold text-blue-600">{{ checkedInCount }}</p>
      </div>
    </div>

    <div class="bg-white p-4 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-4">
      <div class="w-10 h-10 bg-rose-50 rounded-xl flex items-center justify-center text-rose-600 border border-rose-100">
        <span class="material-symbols-outlined text-xl">cancel</span>
      </div>
      <div>
        <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Đã hủy</p>
        <p class="text-xl font-extrabold text-rose-600">{{ cancelledCount }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  totalCount: Number,
  paidCount: Number,
  pendingCount: Number,
  checkedInCount: Number,
  cancelledCount: Number
});
</script>
