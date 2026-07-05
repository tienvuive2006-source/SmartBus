<template>
  <div class="min-h-screen bg-[#f4f7f6] font-sans text-slate-800 pb-20">


    <main class="max-w-6xl mx-auto px-4 py-8 flex flex-col xl:flex-row gap-8">
      
      <!-- CỘT TRÁI: SƠ ĐỒ GHẾ -->
      <div class="flex-1 space-y-6">
        
        <!-- Legend (Chú thích) -->
        <div class="bg-white p-4 border border-gray-100 rounded-2xl flex flex-wrap items-center justify-center gap-6 md:gap-12 shadow-sm">
             <div class="flex items-center gap-2 group">
                <div class="w-6 h-6 rounded-lg border-2 border-gray-200 bg-white group-hover:border-gray-300 transition-colors"></div>
                <span class="text-xs font-bold text-gray-500 uppercase tracking-widest">Ghế Trống</span>
             </div>
             <div class="flex items-center gap-2">
                <div class="w-6 h-6 rounded-lg border-2 border-[#075955] bg-[#075955] shadow-[0_0_10px_rgba(7,89,85,0.4)] relative flex items-center justify-center">
                   <div class="w-2.5 h-1 bg-white/60 rounded-full absolute bottom-1"></div>
                </div>
                <span class="text-xs font-black text-[#075955] uppercase tracking-widest">Đang Chọn</span>
             </div>
             <div class="flex items-center gap-2 opacity-70">
                <div class="w-6 h-6 rounded-lg border-2 border-gray-300 bg-gray-200 flex items-center justify-center">
                   <span class="material-symbols-outlined text-[14px] text-gray-500 font-black">lock</span>
                </div>
                <span class="text-xs font-bold text-gray-500 uppercase tracking-widest">Đã Bán</span>
             </div>
        </div>

        <!-- ROUND TRIP TABS -->
        <div v-if="isRoundTrip" class="flex gap-4 p-1 bg-gray-200/50 rounded-2xl w-fit">
          <button @click="currentTab = 'outbound'" :class="['px-6 py-2.5 rounded-xl text-sm font-black uppercase tracking-widest transition-all', currentTab === 'outbound' ? 'bg-white text-[#075955] shadow-sm' : 'text-gray-500 hover:text-gray-700']">
             Chuyến Đi
             <span v-if="selectedOutboundSeats.length" class="ml-2 bg-[#075955] text-white px-2 py-0.5 rounded-md text-[10px]">{{ selectedOutboundSeats.length }}</span>
          </button>
          <button @click="currentTab = 'return'" :class="['px-6 py-2.5 rounded-xl text-sm font-black uppercase tracking-widest transition-all', currentTab === 'return' ? 'bg-white text-[#075955] shadow-sm' : 'text-gray-500 hover:text-gray-700']">
             Chuyến Về
             <span v-if="selectedReturnSeats.length" class="ml-2 bg-[#075955] text-white px-2 py-0.5 rounded-md text-[10px]">{{ selectedReturnSeats.length }}</span>
          </button>
        </div>

        <!-- Sơ đồ 2 tầng -->
        <div class="bg-white border border-gray-100 rounded-3xl p-6 lg:p-10 relative shadow-sm">
          <div class="flex flex-col md:flex-row gap-10 md:gap-16 justify-center items-start">
          
          <!-- Tầng Dưới -->
          <div class="w-full max-w-[280px] mx-auto md:mx-0 relative">
            <div class="flex justify-center items-center mb-6">
               <h2 class="text-sm font-black text-gray-900 uppercase tracking-widest bg-gray-50 px-6 py-2 rounded-xl border border-gray-100 shadow-sm flex items-center gap-2">
                 Tầng Dưới <span class="material-symbols-outlined text-gray-400 text-sm">airline_seat_recline_normal</span>
               </h2>
            </div>
            
            <!-- Bus Shell -->
            <div class="border-4 border-gray-200 rounded-t-[50px] p-5 pb-10 bg-[#f8faf9] relative shadow-inner">
               <!-- Vô lăng & Tài xế -->
               <div class="flex justify-between items-center mb-8 px-2 border-b-2 border-dashed border-gray-200 pb-4">
                  <div class="w-10 h-10 bg-white rounded-full flex items-center justify-center border-2 border-gray-200 shadow-sm">
                     <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                       <circle cx="12" cy="12" r="9" />
                       <circle cx="12" cy="12" r="2.5" />
                       <path d="M12 14.5v6.5" />
                       <path d="M9.8 10.8L3.5 7" />
                       <path d="M14.2 10.8L20.5 7" />
                     </svg>
                  </div>
                  <div class="flex flex-col items-center">
                     <div class="w-8 h-10 bg-gray-200 border-2 border-gray-300 rounded-lg relative overflow-hidden flex items-center justify-center mb-1">
                        <span class="material-symbols-outlined text-[14px] text-gray-400">person</span>
                     </div>
                     <span class="text-[8px] font-black text-gray-400 uppercase tracking-widest">Tài xế</span>
                  </div>
               </div>

               <!-- Grid Ghế -->
               <div class="grid grid-cols-3 gap-y-4 gap-x-4">
                 <div v-for="seat in floor1Seats" :key="seat.id" class="flex justify-center">
                    <button 
                      @click="toggleSeat(seat)"
                      :disabled="seat.isBooked"
                      :class="['w-12 h-14 border-2 rounded-xl flex flex-col items-center justify-center transition-all duration-300 font-black relative overflow-hidden group',
                        seat.isBooked ? 'bg-gray-200 border-gray-300 text-gray-400 cursor-not-allowed opacity-80' : 
                        activeSelectedSeats.includes(seat.seatNumber) ? 'bg-[#075955] border-[#075955] text-white shadow-[0_8px_20px_rgba(7,89,85,0.4)] -translate-y-1 scale-105 z-10' : 
                        'bg-white border-gray-200 hover:border-[#075955] hover:bg-emerald-50 text-gray-600 hover:text-[#075955] hover:-translate-y-1 hover:shadow-md']"
                    >
                      <span v-if="seat.isBooked" class="material-symbols-outlined text-[16px]">lock</span>
                      <span v-else class="text-xs z-10">{{ seat.seatNumber }}</span>
                      
                      <!-- Gối tựa -->
                      <div :class="['absolute top-1.5 w-6 h-1.5 rounded-full transition-colors duration-300', activeSelectedSeats.includes(seat.seatNumber) ? 'bg-white/30' : seat.isBooked ? 'bg-gray-400/30' : 'bg-gray-200 group-hover:bg-emerald-200']"></div>
                      <!-- Chỗ để chân -->
                      <div :class="['absolute bottom-1.5 w-8 h-1 rounded-full transition-colors duration-300', activeSelectedSeats.includes(seat.seatNumber) ? 'bg-white/50' : seat.isBooked ? 'bg-gray-400/50' : 'bg-gray-300 group-hover:bg-emerald-300']"></div>
                    </button>
                 </div>
               </div>
               
               <div class="mt-8 flex justify-center opacity-40">
                  <span class="text-[9px] font-black text-gray-500 uppercase tracking-[0.4em] rotate-90 my-10">Lối đi chung</span>
               </div>
            </div>
          </div>

          <!-- Tầng Trên -->
          <div class="w-full max-w-[280px] mx-auto md:mx-0 relative">
            <div class="flex justify-center items-center mb-6">
               <h2 class="text-sm font-black text-gray-900 uppercase tracking-widest bg-gray-50 px-6 py-2 rounded-xl border border-gray-100 shadow-sm flex items-center gap-2">
                 Tầng Trên <span class="material-symbols-outlined text-gray-400 text-sm">airline_seat_flat</span>
               </h2>
            </div>
            
            <!-- Bus Shell -->
            <div class="border-4 border-gray-200 rounded-t-[50px] p-5 pb-10 bg-[#f8faf9] relative shadow-inner">
               <!-- Info Tầng trên -->
               <div class="flex justify-between items-center mb-8 px-2 border-b-2 border-dashed border-gray-200 pb-4">
                  <div class="w-10 h-10 bg-white rounded-full flex items-center justify-center border-2 border-gray-200 shadow-sm opacity-50">
                     <span class="material-symbols-outlined text-gray-400">deck</span>
                  </div>
                  <div class="w-10 h-10 bg-white rounded-full flex items-center justify-center border-2 border-gray-200 shadow-sm opacity-50">
                     <span class="material-symbols-outlined text-gray-400">ac_unit</span>
                  </div>
               </div>

               <!-- Grid Ghế -->
               <div class="grid grid-cols-3 gap-y-4 gap-x-4">
                 <div v-for="seat in floor2Seats" :key="seat.id" class="flex justify-center">
                    <button 
                      @click="toggleSeat(seat)"
                      :disabled="seat.isBooked"
                      :class="['w-12 h-14 border-2 rounded-xl flex flex-col items-center justify-center transition-all duration-300 font-black relative overflow-hidden group',
                        seat.isBooked ? 'bg-gray-200 border-gray-300 text-gray-400 cursor-not-allowed opacity-80' : 
                        activeSelectedSeats.includes(seat.seatNumber) ? 'bg-[#075955] border-[#075955] text-white shadow-[0_8px_20px_rgba(7,89,85,0.4)] -translate-y-1 scale-105 z-10' : 
                        'bg-white border-gray-200 hover:border-[#075955] hover:bg-emerald-50 text-gray-600 hover:text-[#075955] hover:-translate-y-1 hover:shadow-md']"
                    >
                      <span v-if="seat.isBooked" class="material-symbols-outlined text-[16px]">lock</span>
                      <span v-else class="text-xs z-10">{{ seat.seatNumber }}</span>
                      
                      <!-- Gối tựa -->
                      <div :class="['absolute top-1.5 w-6 h-1.5 rounded-full transition-colors duration-300', activeSelectedSeats.includes(seat.seatNumber) ? 'bg-white/30' : seat.isBooked ? 'bg-gray-400/30' : 'bg-gray-200 group-hover:bg-emerald-200']"></div>
                      <!-- Chỗ để chân -->
                      <div :class="['absolute bottom-1.5 w-8 h-1 rounded-full transition-colors duration-300', activeSelectedSeats.includes(seat.seatNumber) ? 'bg-white/50' : seat.isBooked ? 'bg-gray-400/50' : 'bg-gray-300 group-hover:bg-emerald-300']"></div>
                    </button>
                 </div>
               </div>
               
               <div class="mt-8 flex justify-center opacity-40">
                  <span class="text-[9px] font-black text-gray-500 uppercase tracking-[0.4em] rotate-90 my-10">Lối đi chung</span>
               </div>
            </div>
          </div>

          </div>
        </div>
      </div>

      <!-- CỘT PHẢI: TÓM TẮT ĐƠN HÀNG (STICKY) -->
      <div class="w-full xl:w-[380px] shrink-0">
        <div class="sticky top-24 space-y-6">
          <div class="bg-white rounded-3xl overflow-hidden shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-gray-100">
             <!-- Header Card -->
             <div class="relative h-24 bg-[#075955] p-6 text-white overflow-hidden">
                <div class="absolute inset-0 opacity-10 bg-[radial-gradient(circle_at_center,rgba(255,255,255,0.8)_0,transparent_100%)]"></div>
                <h3 class="text-[10px] font-black uppercase tracking-widest text-emerald-200 relative z-10">Tạm tính đơn hàng</h3>
                <p class="text-xl font-black truncate mt-1 relative z-10">{{ activeTrip?.companyName }}</p>
             </div>
             
             <!-- Body Card -->
             <div class="p-6 md:p-8 space-y-6">
                
                <!-- Vị trí ghế -->
                <div>
                   <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-3">Ghế đã chọn</p>
                   <div class="flex flex-wrap gap-2 min-h-[40px]">
                      <template v-if="activeSelectedSeats && activeSelectedSeats.length > 0">
                         <span v-for="s in activeSelectedSeats" :key="s" class="bg-emerald-50 text-emerald-700 px-3 py-1.5 rounded-lg text-xs font-black border border-emerald-200 shadow-sm animate-fade-in-up">
                            {{ s }}
                         </span>
                      </template>
                      <div v-else class="flex items-center gap-2 text-amber-500 bg-amber-50 px-4 py-2 rounded-xl border border-amber-100 w-full">
                         <span class="material-symbols-outlined text-[18px] animate-bounce">touch_app</span>
                         <span class="text-xs font-black uppercase tracking-widest">Vui lòng chọn ghế</span>
                      </div>
                   </div>
                </div>
                
                <!-- Chi tiết giá -->
                <div class="pt-6 border-t border-gray-100 space-y-4">
                   <div v-if="!isRoundTrip" class="flex justify-between items-center text-xs font-bold text-gray-500 uppercase tracking-wide">
                      <span>Giá mỗi ghế</span>
                      <span class="text-gray-900">{{ (outboundTrip?.price || 0).toLocaleString() }}đ</span>
                   </div>
                   <div v-if="!isRoundTrip" class="flex justify-between items-center text-xs font-bold text-gray-500 uppercase tracking-wide">
                      <span>Số lượng</span>
                      <span class="text-gray-900 bg-gray-100 px-2 py-0.5 rounded-md">{{ selectedOutboundSeats.length }}</span>
                   </div>
                   
                   <!-- ROUND TRIP BREAKDOWN -->
                   <template v-if="isRoundTrip">
                     <div class="flex justify-between items-center text-xs font-bold text-gray-500 uppercase tracking-wide">
                        <span>Chuyến đi ({{ selectedOutboundSeats.length }}x)</span>
                        <span class="text-gray-900">{{ (selectedOutboundSeats.length * (outboundTrip?.price || 0)).toLocaleString() }}đ</span>
                     </div>
                     <div class="flex justify-between items-center text-xs font-bold text-gray-500 uppercase tracking-wide">
                        <span>Chuyến về ({{ selectedReturnSeats.length }}x)</span>
                        <span class="text-gray-900">{{ (selectedReturnSeats.length * (returnTrip?.price || 0)).toLocaleString() }}đ</span>
                     </div>
                   </template>
                   <div class="flex justify-between items-end pt-4 border-t border-gray-100 border-dashed">
                      <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1.5">Tổng cộng</span>
                      <span class="text-3xl font-black text-[#f03a17] tracking-tighter">{{ totalPrice.toLocaleString() }}<span class="text-xl ml-0.5">đ</span></span>
                   </div>
                </div>

                <!-- Nút Action -->
                <div class="pt-2">
                     <button 
                       @click="goToPayment"
                       :disabled="!isReadyToPay"
                       class="w-full bg-[#f03a17] hover:bg-[#d63314] disabled:bg-gray-200 disabled:text-gray-400 text-white py-4 rounded-2xl font-black text-sm uppercase tracking-widest transition-all shadow-[0_4px_14px_rgba(240,58,23,0.3)] hover:shadow-[0_6px_20px_rgba(240,58,23,0.4)] hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.98] disabled:transform-none disabled:shadow-none flex items-center justify-center gap-2"
                     >
                       Tiếp tục thanh toán
                       <span v-if="isReadyToPay" class="material-symbols-outlined text-lg">arrow_forward</span>
                     </button>
                   <p class="text-[10px] text-gray-400 font-semibold text-center mt-4 uppercase tracking-wide">
                      Cho phép chọn tối đa 5 ghế mỗi lần đặt.
                   </p>
                </div>
             </div>
          </div>

          <!-- Security Badge -->
          <div class="bg-white border border-gray-100 shadow-[0_4px_20px_rgba(0,0,0,0.03)] p-5 rounded-3xl flex items-start gap-4">
             <div class="w-10 h-10 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[20px]">verified_user</span>
             </div>
             <div>
                <p class="text-xs font-black text-gray-800 uppercase tracking-widest mb-1">Thanh toán bảo mật</p>
                <p class="text-[10px] font-bold text-gray-500 leading-relaxed uppercase tracking-wide">Thông tin đặt chỗ sẽ được mã hóa và gửi qua SMS / Email của bạn.</p>
             </div>
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

const isRoundTrip = computed(() => !!route.query.outboundTripId && !!route.query.returnTripId);

const outboundTripId = route.query.tripId || route.query.outboundTripId;
const returnTripId = route.query.returnTripId;

const currentTab = ref('outbound'); // 'outbound' | 'return'

const outboundTrip = ref(null);
const returnTrip = ref(null);
const outboundSeats = ref([]);
const returnSeats = ref([]);

const selectedOutboundSeats = ref([]);
const selectedReturnSeats = ref([]);

const activeTrip = computed(() => currentTab.value === 'outbound' ? outboundTrip.value : returnTrip.value);
const activeSeats = computed(() => currentTab.value === 'outbound' ? outboundSeats.value : returnSeats.value);
const activeSelectedSeats = computed(() => currentTab.value === 'outbound' ? selectedOutboundSeats.value : selectedReturnSeats.value);

const floor1Seats = computed(() => activeSeats.value.filter(s => s.seatFloor === 1));
const floor2Seats = computed(() => activeSeats.value.filter(s => s.seatFloor === 2));

const totalPrice = computed(() => {
  const outTotal = selectedOutboundSeats.value.length * (outboundTrip.value?.price || 0);
  const retTotal = selectedReturnSeats.value.length * (returnTrip.value?.price || 0);
  return outTotal + retTotal;
});

const isReadyToPay = computed(() => {
  if (isRoundTrip.value) {
    return selectedOutboundSeats.value.length > 0 && selectedReturnSeats.value.length > 0;
  }
  return selectedOutboundSeats.value.length > 0;
});

const fetchTripData = async () => {
  try {
    const pOutbound = api.get(`/trips/${outboundTripId}`);
    const pOutboundSeats = api.get(`/trips/${outboundTripId}/seats`);
    
    let pReturn = null, pReturnSeats = null;
    if (isRoundTrip.value) {
      pReturn = api.get(`/trips/${returnTripId}`);
      pReturnSeats = api.get(`/trips/${returnTripId}/seats`);
    }
    
    const [outRes, outSeatsRes, retRes, retSeatsRes] = await Promise.all([
      pOutbound, pOutboundSeats, 
      pReturn || Promise.resolve(null), 
      pReturnSeats || Promise.resolve(null)
    ]);

    outboundTrip.value = outRes.data;
    outboundSeats.value = outSeatsRes.data;
    
    if (isRoundTrip.value) {
      returnTrip.value = retRes.data;
      returnSeats.value = retSeatsRes.data;
    }
  } catch (err) {
    console.error("Lỗi tải dữ liệu:", err);
  }
};

const toggleSeat = (seat) => {
  const arr = currentTab.value === 'outbound' ? selectedOutboundSeats.value : selectedReturnSeats.value;
  const index = arr.indexOf(seat.seatNumber);
  
  if (index > -1) {
    arr.splice(index, 1);
  } else {
    if (arr.length >= 5) {
      alert('Bạn chỉ có thể chọn tối đa 5 ghế cho mỗi chuyến!');
      return;
    }
    arr.push(seat.seatNumber);
  }
};

const goToPayment = () => {
  const query = {
    tripId: outboundTripId,
    seats: selectedOutboundSeats.value.join(','),
    total: totalPrice.value
  };
  
  if (isRoundTrip.value) {
    query.returnTripId = returnTripId;
    query.returnSeats = selectedReturnSeats.value.join(',');
  }
  
  router.push({ path: '/booking/payment', query });
};

onMounted(fetchTripData);
</script>