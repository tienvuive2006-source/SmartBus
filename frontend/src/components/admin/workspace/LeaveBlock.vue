<template>
  <div 
    @click="$emit('click', leave)"
    class="rounded-xl p-2.5 shadow-sm border transition-all cursor-pointer overflow-hidden backdrop-blur-sm group shrink-0"
    :class="{
      'bg-rose-100/90 border-rose-200 text-rose-800': leave.status === 'APPROVED',
      'bg-slate-200/90 border-slate-300 text-slate-700': leave.status === 'PENDING',
      'bg-gray-100/90 border-gray-200 text-gray-500 opacity-60': leave.status === 'REJECTED'
    }"
  >
    <div class="flex items-center justify-between mb-1">
      <span class="font-black text-[10px] uppercase tracking-wider flex items-center gap-1">
        <span class="material-symbols-outlined text-[12px]">event_busy</span>
        Nghỉ phép
      </span>
      <span class="text-[9px] font-bold px-1.5 py-0.5 rounded-sm" :class="leave.status === 'APPROVED' ? 'bg-rose-200' : 'bg-slate-300'">{{ leave.status }}</span>
    </div>
    <p class="font-bold text-xs truncate">{{ leave.driverFullName }}</p>
    <p class="text-[10px] opacity-80 truncate">{{ leave.reason }}</p>
  </div>
</template>

<script setup>
defineProps({
  leave: {
    type: Object,
    required: true
  }
});
defineEmits(['click']);
</script>
