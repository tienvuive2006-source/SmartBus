<template>
  <section aria-label="Chú thích trạng thái ghế" class="flex flex-wrap items-center justify-center gap-x-7 gap-y-3 rounded-2xl border border-slate-100 bg-white p-4 shadow-sm">
    <div v-for="item in legendItems" :key="item.label" class="flex items-center gap-2">
      <span :class="['relative flex h-6 w-6 items-center justify-center rounded-lg border-2', item.className]">
        <span v-if="item.icon" class="material-symbols-outlined text-[13px]">{{ item.icon }}</span>
      </span>
      <span class="text-[11px] font-bold text-slate-600">{{ item.label }}</span>
    </div>
  </section>
</template>

<script setup>
const legendItems = [
  { label: 'Ghế trống', className: 'border-slate-200 bg-white' },
  { label: 'Đang chọn', className: 'border-[#075955] bg-[#075955]' },
  { label: 'Đã bán', className: 'border-slate-300 bg-slate-200 text-slate-500', icon: 'lock' },
  { label: 'Ghế ưu tiên', className: 'border-amber-400 bg-amber-50 text-amber-600', icon: 'star' },
  { label: 'Dành cho trẻ em', className: 'border-sky-300 bg-sky-50 text-sky-600', icon: 'child_care' }
];
</script>
