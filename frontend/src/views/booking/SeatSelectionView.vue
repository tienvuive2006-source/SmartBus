<template>
  <div class="min-h-screen bg-[#f2f5f8] font-sans text-gray-800 pb-20">
    <header class="bg-[#075955] border-b border-[#05403d] sticky top-0 z-50 shadow-md">
      <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <div class="flex items-center gap-4">
          <button @click="$router.back()" class="w-8 h-8 rounded-full hover:bg-white/10 flex items-center justify-center transition-all border border-white/20">
            <span class="material-symbols-outlined text-sm font-black text-white">arrow_back</span>
          </button>
          <div>
            <h1 class="text-base font-black text-white uppercase tracking-wider">Chọn ghế chuyến xe</h1>
            <div v-if="trip" class="flex items-center gap-2">
               <span class="text-[10px] font-bold text-yellow-300 uppercase">{{ trip.departurePoint }}</span>
               <span class="material-symbols-outlined text-[10px] text-white/70">east</span>
               <span class="text-[10px] font-bold text-yellow-300 uppercase">{{ trip.arrivalPoint }}</span>
            </div>
          </div>
        </div>
        <div v-if="trip" class="hidden md:flex items-center gap-6">
           <div class="text-right">
              <p class="text-[9px] font-black text-white/70 uppercase tracking-widest">Khởi hành</p>
              <p class="text-xs font-black text-white">{{ trip.departureTime }} • {{ trip.departureDate }}</p>
           </div>
        </div>
      </div>
    </header>

    <main class="max-w-6xl mx-auto px-4 py-6 flex flex-col xl:flex-row gap-6">
      
      <div class="flex-1 space-y-4">
        
        <div class="bg-white p-3 border border-gray-200 rounded-lg flex items-center justify-around shadow-sm">
             <div class="flex items-center gap-2">
                <div class="w-4 h-4 rounded border border-gray-300 bg-white"></div>
                <span class="text-[10px] font-black text-gray-500 uppercase">Trống</span>
             </div>
             <div class="flex items-center gap-2">
                <div class="w-4 h-4 rounded border border-[#075955] bg-[#075955] shadow-sm"></div>
                <span class="text-[10px] font-black text-[#075955] uppercase">Đang chọn</span>
             </div>
             <div class="flex items-center gap-2">
                <div class="w-4 h-4 rounded border border-gray-300 bg-gray-300 flex items-center justify-center">
                   <span class="material-symbols-outlined text-[10px] text-white font-black">lock</span>
                </div>
                <span class="text-[10px] font-black text-gray-500 uppercase">Đã bán</span>
             </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="bg-white border border-gray-200 rounded-xl p-6 relative shadow-sm">
            <div class="absolute top-3 left-3 bg-gray-100 px-2 py-0.5 rounded text-[9px] font-black text-gray-600 uppercase border border-gray-200">Tầng Dưới</div>
            
            <div class="max-w-[240px] mx-auto mt-4 border-x-2 border-t-8 border-gray-300 rounded-t-[40px] p-4 pb-8 bg-gray-50/50">
               <div class="flex justify-between items-center mb-6 opacity-40 px-2">
                  <div class="w-8 h-8 border-2 border-gray-400 rounded-lg flex items-center justify-center">
                     <span class="material-symbols-outlined text-sm text-gray-600">steering_wheel</span>
                  </div>
                  <div class="w-6 h-10 bg-gray-300 rounded-sm"></div>
               </div>

               <div class="grid grid-cols-3 gap-y-2 gap-x-3">
                 <div v-for="seat in floor1Seats" :key="seat.id" class="flex justify-center">
                    <button 
                      @click="toggleSeat(seat)"
                      :disabled="seat.isBooked"
                      :class="['w-10 h-11 border rounded flex flex-col items-center justify-center transition-all font-black relative',
                        seat.isBooked ? 'bg-gray-300 border-gray-400 text-white cursor-not-allowed opacity-80' : 
                        selectedSeats.includes(seat.seatNumber) ? 'bg-[#075955] border-[#05403d] text-white shadow-md scale-105 z-10' : 
                        'bg-white border-gray-300 hover:border-[#075955] text-gray-600 hover:text-[#075955]']"
                    >
                      <span v-if="seat.isBooked" class="material-symbols-outlined text-[12px]">lock</span>
                      <span v-else class="text-[10px]">{{ seat.seatNumber }}</span>
                      <div :class="['w-6 h-1 rounded-full mt-0.5', selectedSeats.includes(seat.seatNumber) ? 'bg-white/50' : seat.isBooked ? 'bg-white/30' : 'bg-gray-200']"></div>
                    </button>
                 </div>
               </div>
               
               <div class="mt-4 text-center">
                  <span class="text-[8px] font-black text-gray-400 uppercase tracking-[0.3em]">Hành lang xe</span>
               </div>
            </div>
          </div>

          <div class="bg-white border border-gray-200 rounded-xl p-6 relative shadow-sm">
            <div class="absolute top-3 left-3 bg-gray-100 px-2 py-0.5 rounded text-[9px] font-black text-gray-600 uppercase border border-gray-200">Tầng Trên</div>
            
            <div class="max-w-[240px] mx-auto mt-4 border-x-2 border-t-8 border-gray-300 rounded-t-[40px] p-4 pb-8 bg-gray-50/50">
               <div class="flex justify-between items-center mb-6 opacity-30 px-2 text-gray-600">
                  <span class="material-symbols-outlined text-lg">deck</span>
                  <span class="material-symbols-outlined text-xl">air</span>
               </div>

               <div class="grid grid-cols-3 gap-y-2 gap-x-3">
                 <div v-for="seat in floor2Seats" :key="seat.id" class="flex justify-center">
                    <button 
                      @click="toggleSeat(seat)"
                      :disabled="seat.isBooked"
                      :class="['w-10 h-11 border rounded flex flex-col items-center justify-center transition-all font-black relative',
                        seat.isBooked ? 'bg-gray-300 border-gray-400 text-white cursor-not-allowed opacity-80' : 
                        selectedSeats.includes(seat.seatNumber) ? 'bg-[#075955] border-[#05403d] text-white shadow-md scale-105 z-10' : 
                        'bg-white border-gray-300 hover:border-[#075955] text-gray-600 hover:text-[#075955]']"
                    >
                      <span v-if="seat.isBooked" class="material-symbols-outlined text-[12px]">lock</span>
                      <span v-else class="text-[10px]">{{ seat.seatNumber }}</span>
                      <div :class="['w-6 h-1 rounded-full mt-0.5', selectedSeats.includes(seat.seatNumber) ? 'bg-white/50' : seat.isBooked ? 'bg-white/30' : 'bg-gray-200']"></div>
                    </button>
                 </div>
               </div>
               
               <div class="mt-4 text-center">
                  <span class="text-[8px] font-black text-gray-400 uppercase tracking-[0.3em]">Hành lang xe</span>
               </div>
            </div>
          </div>
        </div>
      </div>

      <div class="w-full xl:w-[320px]">
        <div class="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-lg sticky top-24">
           <div class="bg-[#075955] p-4 text-white">
              <h3 class="text-[10px] font-black uppercase tracking-widest text-yellow-300">Đặt chỗ trực tuyến</h3>
              <p class="text-base font-black truncate mt-0.5">{{ trip?.companyName }}</p>
           </div>
           
           <div class="p-5 space-y-5">
              <div class="space-y-4">
                 <div>
                    <p class="text-[9px] font-black text-gray-400 uppercase tracking-wider mb-2">Vị trí ghế</p>
                    <div class="flex flex-wrap gap-1.5">
                       <span v-for="s in selectedSeats" :key="s" class="bg-[#075955]/10 text-[#075955] px-2 py-0.5 rounded text-[10px] font-black border border-[#075955]/20">
                          {{ s }}
                       </span>
                       <span v-if="selectedSeats.length === 0" class="text-xs font-bold text-gray-400 italic">Chọn ít nhất 1 ghế</span>
                    </div>
                 </div>
                 
                 <div class="pt-4 border-t border-gray-200 space-y-2">
                    <div class="flex justify-between text-[10px] font-bold text-gray-500 uppercase">
                       <span>Giá mỗi ghế</span>
                       <span class="text-gray-800">{{ trip?.price.toLocaleString() }}đ</span>
                    </div>
                    <div class="flex justify-between items-end pt-2">
                       <span class="text-xs font-black text-gray-800 uppercase tracking-wider">Tổng cộng</span>
                       <span class="text-2xl font-black text-[#075955] tracking-tighter">{{ totalPrice.toLocaleString() }}đ</span>
                    </div>
                 </div>
              </div>

              <button 
                @click="goToPayment"
                :disabled="selectedSeats.length === 0"
                class="w-full bg-[#f03a17] hover:bg-[#d63314] disabled:bg-gray-300 disabled:text-gray-500 text-white py-4 rounded-md font-black text-xs uppercase tracking-[0.2em] transition-all active:scale-[0.98] shadow-md"
              >
                Tiếp tục
              </button>
           </div>
        </div>

        <div class="mt-4 bg-emerald-50 border border-emerald-100 p-4 rounded-xl">
           <div class="flex items-start gap-3">
              <span class="material-symbols-outlined text-emerald-600 text-sm">shield</span>
              <p class="text-[9px] font-bold text-emerald-800 leading-relaxed uppercase">Hệ thống bảo mật Trung - Nam. Thông tin vé sẽ được gửi qua SMS/Email sau khi hoàn tất thanh toán.</p>
           </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useApi } from '@/composables/useApi';

const route = useRoute();
const router = useRouter();
const api = useApi();
const tripId = route.query.tripId;

const trip = ref(null);
const seats = ref([]);
const selectedSeats = ref([]);

const fetchTripData = async () => {
  try {
    const [tripRes, seatsRes] = await Promise.all([
      api.get(`/trips/${tripId}`),
      api.get(`/trips/${tripId}/seats`)
    ]);
    trip.value = tripRes.data;
    seats.value = seatsRes.data;
  } catch (err) {
    console.error("Lỗi tải dữ liệu:", err);
  }
};

const floor1Seats = computed(() => seats.value.filter(s => s.seatFloor === 1));
const floor2Seats = computed(() => seats.value.filter(s => s.seatFloor === 2));

const totalPrice = computed(() => selectedSeats.value.length * (trip.value?.price || 0));

const toggleSeat = (seat) => {
  const index = selectedSeats.value.indexOf(seat.seatNumber);
  if (index > -1) {
    selectedSeats.value.splice(index, 1);
  } else {
    if (selectedSeats.value.length >= 5) {
      alert('Bạn chỉ có thể chọn tối đa 5 ghế!');
      return;
    }
    selectedSeats.value.push(seat.seatNumber);
  }
};

const goToPayment = () => {
  router.push({
    path: '/booking/payment',
    query: {
      tripId,
      seats: selectedSeats.value.join(','),
      total: totalPrice.value
    }
  });
};

onMounted(fetchTripData);
</script>