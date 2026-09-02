<template>
  <div 
    @click="$emit('click', trip)"
    class="rounded-xl p-3 shadow-md border transition-all cursor-pointer overflow-hidden relative group hover:shadow-lg hover:-translate-y-0.5 shrink-0"
    :class="{
      'bg-blue-500 border-blue-600 text-white': trip.status === 'IN_PROGRESS',
      'bg-emerald-500 border-emerald-600 text-white': trip.status === 'ASSIGNED',
      'bg-amber-400 border-amber-500 text-amber-950 shadow-amber-900/10': trip.status === 'PENDING' && !isUrgent,
      'bg-rose-500 border-rose-600 text-white shadow-rose-900/20 ring-2 ring-rose-400 ring-offset-1': trip.status === 'PENDING' && isUrgent,
      'bg-slate-500 border-slate-600 text-white': trip.status === 'COMPLETED',
      'bg-slate-700 border-slate-800 text-slate-200 opacity-70': trip.status === 'CANCELLED'
    }"
  >
    <div class="absolute right-0 top-0 w-16 h-16 rounded-bl-full -mr-4 -mt-4 pointer-events-none transition-colors"
         :class="trip.status === 'PENDING' && !isUrgent ? 'bg-amber-500/10' : 'bg-white/10'"></div>
    
    <div class="flex items-center justify-between mb-1 relative z-10">
      <span class="font-black text-[11px] uppercase tracking-wider opacity-90">{{ trip.departureTime }} - {{ trip.arrivalTime }}</span>
      <div class="flex items-center gap-1">
        <span v-if="isUrgent" class="flex items-center justify-center w-4 h-4 rounded-full bg-rose-700 text-white border border-rose-400 animate-pulse" title="Khẩn cấp">
          <span class="material-symbols-outlined text-[10px]" style="font-variation-settings: 'FILL' 1">warning</span>
        </span>
        <span class="text-[9px] font-black px-1.5 py-0.5 rounded border border-white/20 bg-black/10 uppercase tracking-widest backdrop-blur-sm">
          {{ getStatusText(trip.status) }}
        </span>
      </div>
    </div>
    <p class="font-bold text-xs truncate relative z-10">{{ trip.departurePoint.split(',')[0] }} ➔ {{ trip.arrivalPoint.split(',')[0] }}</p>
    
    <div class="mt-2.5 pt-2.5 border-t flex flex-col gap-1.5 relative z-10 transition-colors"
         :class="trip.status === 'PENDING' && !isUrgent ? 'border-amber-900/10' : 'border-white/20'">
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
const props = defineProps({
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

const getTripDepartureTime = (trip) => {
  if (!trip?.departureDate || !trip?.departureTime) return null;
  try {
    const dateParts = trip.departureDate.split('T')[0].split('-');
    if (dateParts.length !== 3) return null;
    const [year, month, day] = dateParts;
    const timeParts = trip.departureTime.split(':');
    if (timeParts.length < 2) return null;
    const [hour, minute] = timeParts;
    return new Date(year, month - 1, day, hour, minute);
  } catch (e) {
    return null;
  }
};

import { computed } from 'vue';

const isTripPassed = (trip) => {
  const depTime = getTripDepartureTime(trip);
  return depTime ? new Date() > depTime : false;
};

const isUrgent = computed(() => {
  const trip = props.trip;
  if (trip.status !== 'PENDING' && trip.status !== 'SCHEDULED') return false;
  if (isTripPassed(trip)) return false;
  const hasDriver = trip.assignedDriverUsername && trip.assignedDriverUsername.trim() !== '';
  if (hasDriver) return false;
  
  const depTime = getTripDepartureTime(trip);
  if (!depTime) return false;
  const msUntilDeparture = depTime.getTime() - Date.now();
  return msUntilDeparture > 0 && msUntilDeparture <= 24 * 60 * 60 * 1000;
});
</script>
