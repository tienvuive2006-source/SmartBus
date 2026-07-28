<template>
  <Teleport to="body">
    <div v-if="show" class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 animate-fade-in">
      <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="closeModal"></div>
      <div class="relative w-full sm:max-w-[700px] bg-white sm:rounded-[2rem] rounded-t-[2rem] shadow-2xl overflow-hidden animate-slide-up sm:animate-fade-in-up flex flex-col max-h-[90vh]">
        
        <div class="px-6 py-5 border-b border-slate-100 flex justify-between items-center bg-white shrink-0 sticky top-0 z-10">
          <h3 class="text-title-md font-black text-slate-800">Sơ đồ ghế trên xe</h3>
          <button @click="closeModal" class="w-10 h-10 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        
        <div class="p-6 overflow-y-auto bg-slate-50">
          <!-- Legend -->
          <div class="bg-white p-4 border border-slate-100 rounded-2xl flex flex-wrap items-center justify-center gap-6 md:gap-12 shadow-sm mb-6">
            <div class="flex items-center gap-2">
              <div class="w-6 h-6 rounded-lg border-2 border-indigo-500 bg-indigo-50"></div>
              <span class="text-xs font-black text-indigo-600 uppercase tracking-widest">Đang chọn</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-6 h-6 rounded-lg border-2 border-slate-200 bg-white"></div>
              <span class="text-xs font-bold text-slate-500 uppercase tracking-widest">Trống</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-6 h-6 rounded-lg border-2 border-amber-300 bg-amber-100"></div>
              <span class="text-xs font-black text-amber-600 uppercase tracking-widest">Đã bán</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-6 h-6 rounded-lg border-2 border-emerald-500 bg-emerald-500"></div>
              <span class="text-xs font-black text-emerald-600 uppercase tracking-widest">Đã lên xe</span>
            </div>
          </div>

          <!-- Grid chứa 2 tầng cố định nằm ngang -->
          <div class="grid grid-cols-2 gap-4 overflow-x-auto pb-4">
            <!-- Tầng Dưới -->
            <div class="bg-white border border-slate-100 rounded-2xl p-4 relative shadow-sm min-w-[220px]">
              <div class="flex justify-between items-center mb-6">
                <h2 class="text-sm font-black text-slate-900 uppercase tracking-widest bg-slate-50 px-4 py-2 rounded-xl border border-slate-100">Tầng Dưới</h2>
                <span class="material-symbols-outlined text-slate-400">airline_seat_recline_normal</span>
              </div>
              
              <div class="max-w-[240px] mx-auto border-4 border-slate-200 rounded-t-[40px] p-4 pb-8 bg-[#f8faf9] relative">
                <div class="flex justify-between items-center mb-6 px-1 border-b-2 border-dashed border-slate-200 pb-3">
                  <div class="w-10 h-10 bg-white rounded-full flex items-center justify-center border-2 border-slate-200">
                    <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="9" /><circle cx="12" cy="12" r="2.5" /><path d="M12 14.5v6.5" /><path d="M9.8 10.8L3.5 7" /><path d="M14.2 10.8L20.5 7" />
                    </svg>
                  </div>
                  <div class="flex flex-col items-center">
                    <div class="w-8 h-10 bg-slate-200 border-2 border-slate-300 rounded-lg flex items-center justify-center mb-1">
                      <span class="material-symbols-outlined text-[14px] text-slate-400">person</span>
                    </div>
                    <span class="text-[8px] font-black text-slate-400 uppercase tracking-widest">Tài xế</span>
                  </div>
                </div>

                <div class="grid grid-cols-3 gap-y-3 gap-x-2">
                  <div v-for="seat in floor1Seats" :key="seat.id" class="flex justify-center">
                    <div 
                      @click="toggleSeat(seat.seatNumber)"
                      :class="['w-12 h-14 shrink-0 border-2 rounded-xl flex flex-col items-center justify-center font-black relative overflow-hidden cursor-pointer transition-all',
                        selectedSeats.includes(seat.seatNumber) ? 'bg-indigo-50 border-indigo-500 text-indigo-600 scale-105' :
                        getSeatStatus(seat.seatNumber) === 'CHECKED_IN' ? 'bg-emerald-500 border-emerald-500 text-white shadow-md' : 
                        getSeatStatus(seat.seatNumber) === 'BOOKED' ? 'bg-amber-100 border-amber-300 text-amber-700 shadow-sm' : 
                        'bg-white border-slate-200 text-slate-400']"
                    >
                      <span class="text-xs z-10">{{ seat.seatNumber }}</span>
                      <div :class="['absolute top-1.5 w-6 h-1.5 rounded-full', getSeatStatus(seat.seatNumber) === 'CHECKED_IN' ? 'bg-white/30' : 'bg-slate-300/50']"></div>
                      <div :class="['absolute bottom-1.5 w-8 h-1 rounded-full', getSeatStatus(seat.seatNumber) === 'CHECKED_IN' ? 'bg-white/50' : 'bg-slate-300']"></div>
                    </div>
                  </div>
                </div>
                
                <div class="mt-8 flex justify-center opacity-40">
                  <span class="text-[9px] font-black text-slate-500 uppercase tracking-[0.4em] rotate-90 my-10">Lối đi chung</span>
                </div>
              </div>
            </div>

            <!-- Tầng Trên -->
            <div class="bg-white border border-slate-100 rounded-2xl p-4 relative shadow-sm min-w-[220px]">
              <div class="flex justify-between items-center mb-6">
                <h2 class="text-sm font-black text-slate-900 uppercase tracking-widest bg-slate-50 px-4 py-2 rounded-xl border border-slate-100">Tầng Trên</h2>
                <span class="material-symbols-outlined text-slate-400">airline_seat_flat</span>
              </div>
              
              <div class="max-w-[240px] mx-auto border-4 border-slate-200 rounded-t-[40px] p-4 pb-8 bg-[#f8faf9] relative">
                <div class="flex justify-between items-center mb-6 px-1 border-b-2 border-dashed border-slate-200 pb-3">
                  <div class="w-10 h-10 bg-white rounded-full flex items-center justify-center border-2 border-slate-200 opacity-50">
                    <span class="material-symbols-outlined text-slate-400">deck</span>
                  </div>
                  <div class="w-10 h-10 bg-white rounded-full flex items-center justify-center border-2 border-slate-200 opacity-50">
                    <span class="material-symbols-outlined text-slate-400">ac_unit</span>
                  </div>
                </div>

                <div class="grid grid-cols-3 gap-y-3 gap-x-2">
                  <div v-for="seat in floor2Seats" :key="seat.id" class="flex justify-center">
                    <div 
                      @click="toggleSeat(seat.seatNumber)"
                      :class="['w-12 h-14 shrink-0 border-2 rounded-xl flex flex-col items-center justify-center font-black relative overflow-hidden cursor-pointer transition-all',
                        selectedSeats.includes(seat.seatNumber) ? 'bg-indigo-50 border-indigo-500 text-indigo-600 scale-105' :
                        getSeatStatus(seat.seatNumber) === 'CHECKED_IN' ? 'bg-emerald-500 border-emerald-500 text-white shadow-md' : 
                        getSeatStatus(seat.seatNumber) === 'BOOKED' ? 'bg-amber-100 border-amber-300 text-amber-700 shadow-sm' : 
                        'bg-white border-slate-200 text-slate-400']"
                    >
                      <span class="text-xs z-10">{{ seat.seatNumber }}</span>
                      <div :class="['absolute top-1.5 w-6 h-1.5 rounded-full', getSeatStatus(seat.seatNumber) === 'CHECKED_IN' ? 'bg-white/30' : 'bg-slate-300/50']"></div>
                      <div :class="['absolute bottom-1.5 w-8 h-1 rounded-full', getSeatStatus(seat.seatNumber) === 'CHECKED_IN' ? 'bg-white/50' : 'bg-slate-300']"></div>
                    </div>
                  </div>
                </div>
                
                <div class="mt-8 flex justify-center opacity-40">
                  <span class="text-[9px] font-black text-slate-500 uppercase tracking-[0.4em] rotate-90 my-10">Lối đi chung</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Bottom Fast Checkout Bar -->
        <div v-if="selectedSeats.length > 0" class="p-4 bg-white border-t border-slate-100 shadow-[0_-10px_20px_-10px_rgba(0,0,0,0.1)] z-20 shrink-0 sticky bottom-0">
          <div class="flex gap-2 mb-3">
            <input v-model="fastCustomerName" type="text" placeholder="Tên khách (Trống = Khách vãng lai)" class="w-1/2 bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:border-indigo-500 font-medium" />
            <input v-model="fastCustomerPhone" type="tel" placeholder="SĐT (Tùy chọn)" class="w-1/2 bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:border-indigo-500 font-medium" />
          </div>
          <button @click="handleFastCheckout" :disabled="isSubmittingBooking" class="w-full bg-emerald-500 hover:bg-emerald-600 text-white font-black py-3.5 rounded-2xl active:scale-95 transition-all flex items-center justify-center gap-2">
            <span v-if="isSubmittingBooking" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
            <template v-else>
              <span class="material-symbols-outlined text-[20px]">point_of_sale</span>
              THU {{(selectedSeats.length * (trip?.price || 0)).toLocaleString()}}đ & ĐẶT VÉ
            </template>
          </button>
        </div>

      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  tripId: {
    type: [String, Number],
    required: true
  },
  trip: {
    type: Object,
    default: null
  },
  bookings: {
    type: Array,
    default: () => []
  },
  floor1Seats: {
    type: Array,
    default: () => []
  },
  floor2Seats: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:show', 'checkout-success']);
const authStore = useAuthStore();

const selectedSeats = ref([]);
const fastCustomerName = ref('');
const fastCustomerPhone = ref('');
const isSubmittingBooking = ref(false);

watch(() => props.show, (newVal) => {
  if (newVal) {
    selectedSeats.value = [];
    fastCustomerName.value = '';
    fastCustomerPhone.value = '';
  }
});

const closeModal = () => {
  emit('update:show', false);
};

const getSeatStatus = (seatNumber) => {
  const booking = props.bookings.find(b => b.seatNumbers.includes(seatNumber));
  if (!booking || booking.status === 'CANCELLED') return 'EMPTY';
  if (booking.status === 'CHECKED_IN') return 'CHECKED_IN';
  return 'BOOKED';
};

const playBeep = (isSuccess) => {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    const ctx = new AudioContext();
    const osc = ctx.createOscillator();
    const gainNode = ctx.createGain();

    osc.type = isSuccess ? 'sine' : 'sawtooth';
    osc.frequency.setValueAtTime(isSuccess ? 800 : 300, ctx.currentTime);
    if (isSuccess) {
      osc.frequency.exponentialRampToValueAtTime(1200, ctx.currentTime + 0.1);
    }
    
    gainNode.gain.setValueAtTime(0.1, ctx.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.3);

    osc.connect(gainNode);
    gainNode.connect(ctx.destination);

    osc.start();
    osc.stop(ctx.currentTime + 0.3);
  } catch (e) {
    // Ignore if not supported
  }
};

const toggleSeat = (seatNumber) => {
  const status = getSeatStatus(seatNumber);
  if (status !== 'EMPTY') {
    playBeep(false);
    return; 
  }
  
  if (selectedSeats.value.includes(seatNumber)) {
    selectedSeats.value = selectedSeats.value.filter(s => s !== seatNumber);
  } else {
    selectedSeats.value.push(seatNumber);
  }
};

const handleFastCheckout = async () => {
  if (selectedSeats.value.length === 0) return;
  
  isSubmittingBooking.value = true;
  try {
    const payload = {
      customerName: fastCustomerName.value || 'Khách vãng lai',
      customerPhone: fastCustomerPhone.value || '0000000000',
      customerEmail: 'no-email@smartbus.com',
      seatNumbers: selectedSeats.value,
      trip: { id: props.tripId },
      paymentMethod: 'CASH',
      status: 'CHECKED_IN',
      sendEmail: false
    };
    
    await axios.post(`${import.meta.env.VITE_API_BASE_URL}/admin/bookings/create`, payload, authStore.authHeader);
    
    playBeep(true);
    alert('Đã tạo vé và check-in thành công!');
    closeModal();
    emit('checkout-success');
  } catch (err) {
    console.error(err);
    alert(err.response?.data?.error || 'Lỗi tạo vé! Vui lòng thử lại.');
    playBeep(false);
  } finally {
    isSubmittingBooking.value = false;
  }
};
</script>
