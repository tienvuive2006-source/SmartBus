<template>
  <section class="rounded-3xl bg-white p-5 shadow-[0_8px_28px_rgba(13,65,58,0.05)]">
    <div class="mb-4 flex items-center justify-between">
      <h3 class="font-black text-slate-900">{{ title }}</h3>
      <span class="text-xs font-bold tabular-nums text-slate-400">{{ seats.length }} ghế</span>
    </div>

    <div class="mx-auto max-w-xs rounded-t-[2.5rem] border-[3px] border-slate-200 bg-slate-50 px-5 pb-7 pt-5">
      <div class="mb-6 flex items-center justify-between border-b border-dashed border-slate-300 pb-4 text-slate-400">
        <span class="material-symbols-outlined">trip_origin</span>
        <span class="text-[10px] font-bold uppercase tracking-[0.18em]">Đầu xe</span>
      </div>
      <div class="grid grid-cols-3 justify-items-center gap-x-4 gap-y-4">
        <SeatLayoutCell v-for="seat in seats" :key="seat.seatNumber" :seat="seat" @select="$emit('select', $event)" />
      </div>
    </div>
  </section>
</template>

<script setup>
import SeatLayoutCell from './SeatLayoutCell.vue';

defineProps({
  title: {
    type: String,
    required: true
  },
  seats: {
    type: Array,
    default: () => []
  }
});

defineEmits(['select']);
</script>
