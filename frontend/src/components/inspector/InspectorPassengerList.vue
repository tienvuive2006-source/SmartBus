<template>
  <div class="w-full">
    <div class="flex justify-between items-end mb-4 px-1">
      <h3 class="text-title-md font-black">Danh sách hành khách</h3>
      <span class="text-body-sm font-bold text-slate-500 bg-slate-200 px-2 py-0.5 rounded-md">
        {{ checkedInCount }}/{{ bookings.length }} Đã lên xe
      </span>
    </div>

    <div v-if="bookings.length === 0" class="bg-white p-8 rounded-3xl text-center border border-slate-100 text-slate-500 font-medium">
      Chưa có hành khách nào đặt vé.
    </div>
    
    <div v-else class="space-y-3">
      <div 
        v-for="booking in bookings" 
        :key="booking.id"
        class="bg-white p-4 rounded-2xl border transition-all"
        :class="booking.status === 'CHECKED_IN' ? 'border-primary shadow-[0_0_0_1px_rgba(var(--color-primary),0.2)]' : 'border-slate-100'"
      >
        <div class="flex flex-col sm:flex-row justify-between items-start gap-4 mb-2">
          <div class="min-w-0 flex-1">
            <h4 class="font-bold text-body-lg text-slate-800 truncate">{{ booking.customerName }}</h4>
            <p class="text-body-sm text-slate-500 flex flex-wrap items-center gap-1 mt-0.5">
              <span class="material-symbols-outlined text-[16px]">call</span>
              {{ booking.customerPhone }}
              <span class="mx-1 text-slate-300 hidden sm:inline">•</span>
              <span class="font-black text-primary w-full sm:w-auto mt-1 sm:mt-0">{{ booking.seatNumbers.length }} vé</span>
            </p>
          </div>
          <div class="text-left sm:text-right shrink-0 bg-slate-50 sm:bg-transparent p-2 sm:p-0 rounded-lg w-full sm:w-auto mt-2 sm:mt-0">
            <span class="block text-title-md font-black text-primary">{{ booking.seatNumbers.join(', ') }}</span>
            <span class="text-label-xs font-bold uppercase block mt-1" :class="booking.status === 'CHECKED_IN' ? 'text-primary' : (booking.status === 'PAID' ? 'text-emerald-500' : 'text-amber-500')">
              {{ booking.status === 'CHECKED_IN' ? 'ĐÃ LÊN XE' : (booking.status === 'PAID' ? 'ĐÃ THANH TOÁN' : 'CHỜ T.TOÁN') }}
            </span>
          </div>
        </div>
        
        <hr class="border-slate-100 my-3" />
        
        <div class="flex justify-between items-center">
          <span class="text-body-sm font-medium text-slate-600">ID: #{{ booking.id }}</span>
          <button 
            v-if="booking.status !== 'CHECKED_IN'"
            @click="$emit('manual-checkin', booking)"
            class="px-4 py-1.5 bg-slate-800 text-white font-bold text-body-sm rounded-xl active:scale-95 transition-transform"
          >
            XÁC NHẬN LÊN XE
          </button>
          <button v-else disabled class="px-4 py-1.5 bg-primary/10 text-primary font-bold text-body-sm rounded-xl flex items-center gap-1">
            <span class="material-symbols-outlined text-[18px]">check_circle</span> Hoàn tất
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  bookings: {
    type: Array,
    required: true
  }
});

defineEmits(['manual-checkin']);

const checkedInCount = computed(() => {
  return props.bookings.filter(b => b.status === 'CHECKED_IN').length;
});
</script>
