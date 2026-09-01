<template>
  <button
    type="button"
    :aria-label="`${seat.seatNumber}, ${typeLabel}`"
    :title="`Gán ${typeLabel.toLowerCase()} cho ${seat.seatNumber}`"
    :class="[
      'group relative flex h-14 w-12 flex-col items-center justify-center rounded-xl border-2 text-xs font-black tabular-nums transition-all duration-200',
      styleClass
    ]"
    @click="$emit('select', seat)"
  >
    <span
      :class="[
        'absolute top-1.5 h-1.5 w-6 rounded-full transition-colors',
        seat.seatType === 'STANDARD' ? 'bg-slate-200 group-hover:bg-emerald-200' : 'bg-current/20'
      ]"
    ></span>
    <span class="mt-1">{{ seat.seatNumber }}</span>
    <span
      v-if="seat.seatType !== 'STANDARD'"
      class="material-symbols-outlined absolute -right-1.5 -top-1.5 flex h-5 w-5 items-center justify-center rounded-md bg-white text-[13px] shadow-sm"
    >
      {{ seat.seatType === 'PRIORITY' ? 'star' : 'child_care' }}
    </span>
  </button>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  seat: {
    type: Object,
    required: true
  }
});

defineEmits(['select']);

const typeLabel = computed(() => ({
  STANDARD: 'Ghế thường',
  PRIORITY: 'Ghế ưu tiên',
  CHILD: 'Ghế dành cho trẻ em'
}[props.seat.seatType] || 'Ghế thường'));

const styleClass = computed(() => ({
  STANDARD: 'border-slate-200 bg-white text-slate-600 hover:-translate-y-0.5 hover:border-emerald-600 hover:text-emerald-800',
  PRIORITY: 'border-amber-400 bg-amber-50 text-amber-800 shadow-[0_6px_16px_rgba(217,150,29,0.14)] hover:-translate-y-0.5',
  CHILD: 'border-sky-300 bg-sky-50 text-sky-700 shadow-[0_6px_16px_rgba(14,165,233,0.1)] hover:-translate-y-0.5'
}[props.seat.seatType] || 'border-slate-200 bg-white text-slate-600'));
</script>
