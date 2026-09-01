<template>
  <div>
    <SeatTypeNotice :notice="specialSeatNotice" @close="hideSpecialSeatNotice" />

    <div class="grid gap-8 md:grid-cols-2">
    <section v-for="floor in floors" :key="floor" class="rounded-3xl border border-slate-200 bg-slate-50 p-5">
      <div class="mb-5 flex items-center justify-between">
        <h3 class="text-sm font-black uppercase tracking-[0.18em] text-slate-700">
          Tầng {{ floor === 1 ? 'dưới' : 'trên' }}
        </h3>
        <span class="material-symbols-outlined text-slate-400">airline_seat_recline_normal</span>
      </div>

      <div class="grid grid-cols-3 gap-3">
        <button
          v-for="seat in seatsByFloor(floor)"
          :key="seat.id"
          type="button"
          :disabled="!seat.isAvailable"
          :title="seatTitle(seat)"
          :class="seatClass(seat)"
          @click="toggleSeat(seat)"
        >
          <span class="text-xs font-black">{{ seat.seatNumber }}</span>
          <span
            v-if="seat.seatType && seat.seatType !== 'STANDARD'"
            class="material-symbols-outlined absolute -right-1.5 -top-1.5 flex h-5 w-5 items-center justify-center rounded-md bg-white text-[13px] shadow-sm"
            :class="seat.seatType === 'PRIORITY' ? 'text-amber-600' : 'text-sky-600'"
          >{{ seat.seatType === 'PRIORITY' ? 'star' : 'child_care' }}</span>
          <span v-if="seat.isCurrent" class="mt-1 text-[8px] font-black uppercase">Hiện tại</span>
          <span v-else-if="!seat.isAvailable" class="material-symbols-outlined mt-1 text-[13px]">lock</span>
        </button>
      </div>
    </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import SeatTypeNotice from '@/components/booking/seat/SeatTypeNotice.vue'
import { getSpecialSeatNotice } from '@/utils/seatSelectionNotice'
import { getSeatTypeMeta } from '@/utils/seatTypePresentation'

const props = defineProps({
  seats: { type: Array, default: () => [] },
  modelValue: { type: Array, default: () => [] },
  requiredCount: { type: Number, required: true }
})

const emit = defineEmits(['update:modelValue'])
const specialSeatNotice = ref(null)
let specialSeatNoticeTimer = null

const floors = computed(() => {
  const values = [...new Set(props.seats.map(seat => seat.seatFloor))]
  return values.length ? values.sort() : [1]
})

const seatsByFloor = floor => props.seats.filter(seat => seat.seatFloor === floor)

const toggleSeat = seat => {
  if (!seat.isAvailable) return
  const next = [...props.modelValue]
  const index = next.indexOf(seat.seatNumber)
  if (index >= 0) {
    next.splice(index, 1)
  } else if (next.length < props.requiredCount) {
    showSpecialSeatNotice(seat)
    next.push(seat.seatNumber)
  }
  emit('update:modelValue', next)
}

const hideSpecialSeatNotice = () => {
  specialSeatNotice.value = null
  if (specialSeatNoticeTimer) clearTimeout(specialSeatNoticeTimer)
  specialSeatNoticeTimer = null
}

const showSpecialSeatNotice = seat => {
  const notice = getSpecialSeatNotice(seat)
  if (!notice) return
  hideSpecialSeatNotice()
  specialSeatNotice.value = { ...notice, seatNumber: seat.seatNumber }
  specialSeatNoticeTimer = setTimeout(hideSpecialSeatNotice, 5000)
}

const seatTitle = seat => {
  if (seat.isCurrent) return `Ghế hiện tại · ${getSeatTypeMeta(seat.seatType).label}`
  if (!seat.isAvailable) return 'Ghế đã được đặt'
  return `${seat.seatNumber} · ${getSeatTypeMeta(seat.seatType).label}`
}

const seatClass = seat => {
  const selected = props.modelValue.includes(seat.seatNumber)
  return [
    'relative flex h-16 flex-col items-center justify-center rounded-xl border-2 transition-all',
    selected
      ? 'border-[#075955] bg-[#075955] text-white shadow-md -translate-y-0.5'
      : seat.isCurrent
        ? 'border-amber-400 bg-amber-50 text-amber-700'
        : seat.isAvailable
          ? seat.seatType === 'PRIORITY'
            ? 'border-amber-300 bg-amber-50 text-amber-700 hover:border-amber-500'
            : seat.seatType === 'CHILD'
              ? 'border-sky-300 bg-sky-50 text-sky-700 hover:border-sky-500'
              : 'border-slate-200 bg-white text-slate-700 hover:border-[#075955] hover:text-[#075955]'
          : 'cursor-not-allowed border-slate-200 bg-slate-200 text-slate-400 opacity-70'
  ]
}

onBeforeUnmount(hideSpecialSeatNotice)
</script>
