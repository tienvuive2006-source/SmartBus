<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
      <!-- Backdrop -->
      <div @click="$emit('close')" class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm animate-fade-in"></div>
      
      <!-- Modal Content -->
      <div class="bg-white w-full max-w-3xl rounded-[32px] shadow-[0_32px_64px_-12px_rgba(0,0,0,0.2)] border border-slate-100 overflow-hidden animate-scale-up relative flex flex-col max-h-[85vh]">
        <!-- Header -->
        <div class="bg-indigo-50/50 p-6 flex items-center justify-between border-b border-indigo-100">
          <div class="flex items-center gap-4">
            <div class="w-12 h-12 bg-indigo-100 rounded-2xl flex items-center justify-center text-indigo-600 shadow-sm">
              <span class="material-symbols-outlined text-2xl font-black">receipt_long</span>
            </div>
            <div>
              <h3 class="text-xl font-black text-slate-800 tracking-tight">Lịch sử đặt vé</h3>
              <p class="text-sm font-bold text-indigo-600/80">Khách hàng: {{ user?.fullName }} - {{ user?.phone }}</p>
            </div>
          </div>
          <button @click="$emit('close')" class="w-10 h-10 flex items-center justify-center bg-white rounded-xl text-slate-400 hover:bg-slate-100 hover:text-red-500 transition-colors shadow-sm">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <!-- Body -->
        <div class="p-6 overflow-y-auto custom-scrollbar flex-1 bg-slate-50">
          <div v-if="loading" class="flex flex-col items-center justify-center py-12">
            <span class="w-10 h-10 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin mb-4"></span>
            <span class="text-sm font-bold text-slate-500">Đang tải lịch sử...</span>
          </div>
          <div v-else-if="bookings.length === 0" class="text-center py-12 bg-white rounded-3xl border border-slate-100 shadow-sm">
            <div class="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center text-slate-300 mx-auto mb-4">
              <span class="material-symbols-outlined text-3xl">receipt_long</span>
            </div>
            <p class="text-body-md font-bold text-slate-500">Người dùng này chưa đặt chuyến xe nào.</p>
          </div>
          <div v-else class="space-y-4">
            <div v-for="booking in bookings" :key="booking.id" class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow relative overflow-hidden">
              <div class="absolute left-0 top-0 w-1 h-full" :class="booking.status === 'CANCELLED' ? 'bg-red-500' : 'bg-emerald-500'"></div>
              <div class="flex justify-between items-start mb-3">
                <div>
                  <span class="text-xs font-black px-2 py-1 rounded-md uppercase tracking-wider mb-2 inline-block" :class="booking.status === 'CANCELLED' ? 'bg-red-50 text-red-600' : 'bg-emerald-50 text-emerald-600'">
                    {{ booking.status === 'CANCELLED' ? 'Đã hủy' : 'Thành công' }}
                  </span>
                  <h4 class="text-base font-black text-slate-800">
                    {{ booking.trip?.departurePoint || 'N/A' }} 
                    <span class="material-symbols-outlined text-sm align-middle mx-1 text-slate-400">arrow_forward</span> 
                    {{ booking.trip?.arrivalPoint || 'N/A' }}
                  </h4>
                </div>
                <div class="text-right">
                  <p class="text-lg font-black" :class="booking.status === 'CANCELLED' ? 'text-slate-400 line-through' : 'text-emerald-600'">
                    {{ booking.totalPrice ? booking.totalPrice.toLocaleString('vi-VN') : '0' }}đ
                  </p>
                  <p class="text-xs font-bold text-slate-400">Mã vé: #{{ booking.id }}</p>
                </div>
              </div>
              
              <div class="grid grid-cols-2 gap-4 text-sm bg-slate-50 p-3 rounded-xl border border-slate-100">
                <div class="flex items-center gap-2 text-slate-600">
                  <span class="material-symbols-outlined text-[16px] text-slate-400">event</span>
                  <span class="font-medium">{{ booking.trip?.departureDate ? new Date(booking.trip.departureDate).toLocaleDateString('vi-VN') : 'N/A' }} - {{ booking.trip?.departureTime || 'N/A' }}</span>
                </div>
                <div class="flex items-center gap-2 text-slate-600">
                  <span class="material-symbols-outlined text-[16px] text-slate-400">airline_seat_recline_normal</span>
                  <span class="font-medium">Ghế: <strong class="text-slate-800 font-black">{{ booking.seatNumbers || 'Trống' }}</strong></span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({
  isOpen: Boolean,
  user: Object,
  bookings: Array,
  loading: Boolean
});

defineEmits(['close']);
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
@keyframes scaleUp {
  from { transform: scale(0.92); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 20px;
}
</style>
