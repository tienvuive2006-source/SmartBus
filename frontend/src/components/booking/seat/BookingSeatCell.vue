<template>
  <button
    type="button"
    :disabled="seat.isBooked"
    :aria-pressed="selected"
    :aria-label="ariaLabel"
    :class="[
      'group relative flex h-14 w-12 flex-col items-center justify-center overflow-visible rounded-xl border-2 text-xs font-black transition-all duration-200 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-emerald-700/20',
      stateClass
    ]"
    @click="$emit('toggle', seat)"
  >
    <span
      :class="[
        'absolute top-1.5 h-1.5 w-6 rounded-full transition-colors',
        selected ? 'bg-white/30' : seat.isBooked ? 'bg-slate-400/30' : 'bg-current/15'
      ]"
    ></span>

    <span v-if="seat.isBooked" class="material-symbols-outlined text-[16px]">lock</span>
    <span v-else class="z-10 mt-1">{{ seat.seatNumber }}</span>

    <span
      v-if="!seat.isBooked && !selected && normalizedType !== 'STANDARD'"
      :class="[
        'material-symbols-outlined absolute -right-1.5 -top-1.5 flex h-5 w-5 items-center justify-center rounded-md bg-white text-[13px] shadow-sm',
        normalizedType === 'PRIORITY' ? 'text-amber-600' : 'text-rose-600'
      ]"
    >
      {{ normalizedType === 'PRIORITY' ? 'star' : 'child_care' }}
    </span>

    <span
      :class="[
        'absolute bottom-1.5 h-1 w-8 rounded-full transition-colors',
        selected ? 'bg-white/50' : seat.isBooked ? 'bg-slate-400/40' : 'bg-current/20'
      ]"
    ></span>
  </button>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  seat: {
    type: Object,
    required: true
  },
  selected: Boolean
});

defineEmits(['toggle']);

const normalizedType = computed(() => props.seat.seatType || 'STANDARD');

const typeLabel = computed(() => ({
  STANDARD: 'ghế trống',
  PRIORITY: 'ghế ưu tiên',
  CHILD: 'ghế dành cho trẻ em'
}[normalizedType.value] || 'ghế trống'));

const ariaLabel = computed(() => {
  if (props.seat.isBooked) return `${props.seat.seatNumber}, đã bán`;
  if (props.selected) return `${props.seat.seatNumber}, đang chọn`;
  return `${props.seat.seatNumber}, ${typeLabel.value}`;
});

const stateClass = computed(() => {
  if (props.seat.isBooked) {
    return 'cursor-not-allowed border-slate-300 bg-slate-200 text-slate-400 opacity-80';
  }
  if (props.selected) {
    return '-translate-y-1 scale-105 border-[#075955] bg-[#075955] text-white shadow-[0_8px_20px_rgba(7,89,85,0.35)]';
  }
  if (normalizedType.value === 'PRIORITY') {
    return 'border-amber-400 bg-amber-50 text-amber-800 hover:-translate-y-1 hover:shadow-md';
  }
  if (normalizedType.value === 'CHILD') {
    return 'border-sky-300 bg-sky-50 text-sky-700 hover:-translate-y-1 hover:shadow-md';
  }
  return 'border-slate-200 bg-white text-slate-600 hover:-translate-y-1 hover:border-[#075955] hover:bg-emerald-50 hover:text-[#075955] hover:shadow-md';
});
</script>
