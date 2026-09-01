<template>
  <span class="inline-flex flex-wrap items-center gap-1.5">
    <span
      v-for="seatNumber in seatNumbers"
      :key="seatNumber"
      class="inline-flex items-center gap-1 rounded-md border px-1.5 py-0.5 font-bold"
      :class="badgeClass(seatNumber)"
      :title="meta(seatNumber).label"
    >
      <span class="material-symbols-outlined text-[12px]">{{ meta(seatNumber).icon }}</span>
      {{ seatNumber }}
      <span v-if="typeOf(seatNumber) !== 'STANDARD'" class="text-[8px] uppercase tracking-wide">
        {{ meta(seatNumber).label }}
      </span>
    </span>
  </span>
</template>

<script setup>
import { getSeatTypeMeta } from '@/utils/seatTypePresentation';

const props = defineProps({
  seatNumbers: { type: Array, default: () => [] },
  seatTypes: { type: Object, default: () => ({}) }
});

const typeOf = seatNumber => props.seatTypes?.[seatNumber] || 'STANDARD';
const meta = seatNumber => getSeatTypeMeta(typeOf(seatNumber));
const badgeClass = seatNumber => ({
  PRIORITY: 'border-amber-200 bg-amber-50 text-amber-700',
  CHILD: 'border-sky-200 bg-sky-50 text-sky-700',
  STANDARD: 'border-emerald-100 bg-emerald-50/60 text-[#075955]'
}[typeOf(seatNumber)] || 'border-slate-200 bg-slate-50 text-slate-600');
</script>
