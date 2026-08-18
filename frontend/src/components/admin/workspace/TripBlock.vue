<template>
  <div 
    @click="$emit('click', trip)"
    class="rounded-xl p-3 shadow-md border transition-all cursor-pointer overflow-hidden relative group hover:shadow-lg hover:-translate-y-0.5 shrink-0"
    :class="{
      'bg-blue-500 border-blue-600 text-white': trip.status === 'IN_PROGRESS',
      'bg-emerald-500 border-emerald-600 text-white': trip.status === 'ASSIGNED',
      'bg-amber-400 border-amber-500 text-amber-950 shadow-amber-900/10': trip.status === 'PENDING',
      'bg-slate-500 border-slate-600 text-white': trip.status === 'COMPLETED',
      'bg-slate-700 border-slate-800 text-slate-200 opacity-70': trip.status === 'CANCELLED'
    }"
  >
    <div class="absolute right-0 top-0 w-16 h-16 rounded-bl-full -mr-4 -mt-4 pointer-events-none transition-colors"
         :class="trip.status === 'PENDING' ? 'bg-amber-500/10' : 'bg-white/10'"></div>
    
    <div class="flex items-center justify-between mb-1 relative z-10">
      <span class="font-black text-[11px] uppercase tracking-wider opacity-90">{{ trip.departureTime }} - {{ trip.arrivalTime }}</span>
      <span class="text-[9px] font-black px-1.5 py-0.5 rounded border border-white/20 bg-black/10 uppercase tracking-widest backdrop-blur-sm">
        {{ getStatusText(trip.status) }}
      </span>
    </div>
    <p class="font-bold text-xs truncate relative z-10">{{ trip.departurePoint.split(',')[0] }} ➔ {{ trip.arrivalPoint.split(',')[0] }}</p>
    
    <div class="mt-2.5 pt-2.5 border-t flex flex-col gap-1.5 relative z-10 transition-colors"
         :class="trip.status === 'PENDING' ? 'border-amber-900/10' : 'border-white/20'">
      <div class="flex items-center gap-1.5 text-[10px] font-semibold">
        <span class="material-symbols-outlined text-[12px] opacity-80">badge</span>
        <span class="truncate">{{ trip.assignedDriverFullName || 'Chưa gán tài xế' }}</span>
      </div>
      <div v-if="trip.secondaryDriverFullName" class="flex items-center gap-1.5 text-[10px] font-semibold">
        <span class="material-symbols-outlined text-[12px] opacity-80">group</span>
        <span class="truncate">Phụ: {{ trip.secondaryDriverFullName }}</span>
      </div>
      <div class="flex items-center gap-1.5 text-[10px] font-semibold">
        <span class="material-symbols-outlined text-[12px] opacity-80">directions_bus</span>
        <span class="truncate">{{ trip.assignedLicensePlate ? 'Biển số: ' + trip.assignedLicensePlate : trip.busType }}</span>
      </div>
      <div v-if="trip.inspector" class="flex items-center gap-1.5 text-[10px] font-semibold">
        <span class="material-symbols-outlined text-[12px] opacity-80">support_agent</span>
        <span class="truncate">Lơ xe: {{ trip.inspector.fullName }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  trip: {
    type: Object,
    required: true
  }
});
defineEmits(['click']);

const getStatusText = (status) => {
  switch(status) {
    case 'ASSIGNED': return 'Đã phân công';
    case 'IN_PROGRESS': return 'Đang chạy';
    case 'COMPLETED': return 'Đã hoàn thành';
    case 'CANCELLED': return 'Đã hủy';
    case 'PENDING':
    case 'SCHEDULED':
    default: return 'Chưa phân công';
  }
};
</script>
