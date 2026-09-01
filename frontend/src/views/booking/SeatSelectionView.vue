<template>
  <div class="min-h-screen bg-[#f4f7f6] font-sans text-slate-800 pb-20">
    <SeatTypeNotice :notice="specialSeatNotice" @close="hideSpecialSeatNotice" />

    <div class="max-w-6xl mx-auto px-4 pt-3">
      <BookingProgress :active-step="1" />
    </div>

    <main class="max-w-6xl mx-auto px-4 pt-5 pb-8 flex flex-col xl:flex-row gap-8">
      
      <!-- CỘT TRÁI: SƠ ĐỒ GHẾ -->
      <div class="flex-1 space-y-6">
        
        <BookingSeatLegend />

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
                    <BookingSeatCell
                      :seat="seat"
                      :selected="activeSelectedSeats.includes(seat.seatNumber)"
                      @toggle="toggleSeatRealtime"
                    />
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
                    <BookingSeatCell
                      :seat="seat"
                      :selected="activeSelectedSeats.includes(seat.seatNumber)"
                      @toggle="toggleSeatRealtime"
                    />
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
          <SeatHoldTimer
            :pending="!holdExpiresAt"
            :expires-at="holdExpiresAt"
            status-text="Ghế đang được giữ, vui lòng tiếp tục thanh toán"
            @expired="handleHoldExpired"
          />

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
                         <span v-for="s in activeSelectedSeats" :key="s" class="flex items-center gap-1 bg-emerald-50 text-emerald-700 px-3 py-1.5 rounded-lg text-xs font-black border border-emerald-200 shadow-sm animate-fade-in-up">
                            <span v-if="getActiveSeatType(s) === 'PRIORITY'" class="material-symbols-outlined text-[13px] text-amber-600">star</span>
                            <span v-else-if="getActiveSeatType(s) === 'CHILD'" class="material-symbols-outlined text-[13px] text-sky-600">child_care</span>
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
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useApi } from '@/composables/useApi';
import BookingSeatCell from '@/components/booking/seat/BookingSeatCell.vue';
import BookingSeatLegend from '@/components/booking/seat/BookingSeatLegend.vue';
import BookingProgress from '@/components/booking/flow/BookingProgress.vue';
import SeatHoldTimer from '@/components/booking/flow/SeatHoldTimer.vue';
import { getOrCreateSeatHoldToken } from '@/utils/seatHoldToken';
import { createSeatRealtimeClient } from '@/services/seatRealtime';
import { getSpecialSeatNotice } from '@/utils/seatSelectionNotice';
import SeatTypeNotice from '@/components/booking/seat/SeatTypeNotice.vue';

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
const holdExpiresAt = ref(null);
const holdToken = getOrCreateSeatHoldToken();
const pendingSeatActions = new Set();
const specialSeatNotice = ref(null);
let proceedingToPayment = false;
let realtimeClient = null;
let specialSeatNoticeTimer = null;

const activeTrip = computed(() => currentTab.value === 'outbound' ? outboundTrip.value : returnTrip.value);
const activeSeats = computed(() => currentTab.value === 'outbound' ? outboundSeats.value : returnSeats.value);
const activeSelectedSeats = computed(() => currentTab.value === 'outbound' ? selectedOutboundSeats.value : selectedReturnSeats.value);

const floor1Seats = computed(() => activeSeats.value.filter(s => s.seatFloor === 1));
const floor2Seats = computed(() => activeSeats.value.filter(s => s.seatFloor === 2));

const getActiveSeatType = seatNumber => (
  activeSeats.value.find(seat => seat.seatNumber === seatNumber)?.seatType || 'STANDARD'
);

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
    await restoreOwnedHolds();
  } catch (err) {
    console.error("Lỗi tải dữ liệu:", err);
  }
};

const getTripState = tripId => String(tripId) === String(outboundTripId)
  ? { seats: outboundSeats.value, selected: selectedOutboundSeats.value }
  : { seats: returnSeats.value, selected: selectedReturnSeats.value };

const restoreOwnedHolds = async () => {
  try {
    const { data } = await api.get('/seat-holds/session', { params: { holdToken } });
    for (const held of data.seats || []) {
      if (![String(outboundTripId), String(returnTripId)].includes(String(held.tripId))) continue;
      const state = getTripState(held.tripId);
      if (!state.selected.includes(held.seatNumber)) state.selected.push(held.seatNumber);
      const seat = state.seats.find(item => item.seatNumber === held.seatNumber);
      if (seat) seat.isBooked = false;
    }
    holdExpiresAt.value = data.expiresAt || null;
  } catch (error) {
    console.error('Không khôi phục được phiên giữ ghế:', error);
  }
};

const applyRealtimeEvent = event => {
  const state = getTripState(event.tripId);
  const seat = state.seats.find(item => item.seatNumber === event.seatNumber);
  if (!seat) return;
  const selectedIndex = state.selected.indexOf(event.seatNumber);

  if (event.status === 'AVAILABLE') {
    seat.isBooked = false;
    if (selectedIndex > -1) state.selected.splice(selectedIndex, 1);
  } else if (selectedIndex === -1) {
    seat.isBooked = true;
  }
};

const hideSpecialSeatNotice = () => {
  specialSeatNotice.value = null;
  if (specialSeatNoticeTimer) clearTimeout(specialSeatNoticeTimer);
  specialSeatNoticeTimer = null;
};

const showSpecialSeatNotice = seat => {
  const notice = getSpecialSeatNotice(seat);
  if (!notice) return;
  hideSpecialSeatNotice();
  specialSeatNotice.value = { ...notice, seatNumber: seat.seatNumber };
  specialSeatNoticeTimer = setTimeout(hideSpecialSeatNotice, 5000);
};

const toggleSeatRealtime = async seat => {
  const arr = currentTab.value === 'outbound' ? selectedOutboundSeats.value : selectedReturnSeats.value;
  const tripId = currentTab.value === 'outbound' ? outboundTripId : returnTripId;
  const actionKey = `${tripId}:${seat.seatNumber}`;
  if (pendingSeatActions.has(actionKey)) return;

  const index = arr.indexOf(seat.seatNumber);
  pendingSeatActions.add(actionKey);
  if (index > -1) {
    arr.splice(index, 1);
    try {
      await api.post('/seat-holds/release', { tripId, seatNumber: seat.seatNumber, holdToken });
      if (selectedOutboundSeats.value.length + selectedReturnSeats.value.length === 0) holdExpiresAt.value = null;
    } catch (error) {
      arr.push(seat.seatNumber);
      alert('Không thể bỏ giữ ghế lúc này. Vui lòng thử lại.');
    } finally {
      pendingSeatActions.delete(actionKey);
    }
    return;
  }

  if (arr.length >= 5) {
    pendingSeatActions.delete(actionKey);
    alert('Bạn chỉ có thể chọn tối đa 5 ghế cho mỗi chuyến!');
    return;
  }

  showSpecialSeatNotice(seat);

  arr.push(seat.seatNumber);
  try {
    const { data } = await api.post('/seat-holds', { tripId, seatNumber: seat.seatNumber, holdToken });
    holdExpiresAt.value = data.expiresAt;
    seat.isBooked = false;
  } catch (error) {
    const selectedIndex = arr.indexOf(seat.seatNumber);
    if (selectedIndex > -1) arr.splice(selectedIndex, 1);
    if (error.response?.status === 409) seat.isBooked = true;
    alert(error.response?.data?.error || 'Không thể giữ ghế. Vui lòng thử lại.');
  } finally {
    pendingSeatActions.delete(actionKey);
  }
};

const handleHoldExpired = async () => {
  selectedOutboundSeats.value = [];
  selectedReturnSeats.value = [];
  holdExpiresAt.value = null;
  alert('Thời gian giữ ghế đã hết. Vui lòng chọn lại ghế.');
  await fetchTripData();
};

const goToPayment = () => {
  proceedingToPayment = true;
  const query = {
    tripId: outboundTripId,
    seats: selectedOutboundSeats.value.join(','),
    total: totalPrice.value,
    holdToken
  };
  
  if (isRoundTrip.value) {
    query.returnTripId = returnTripId;
    query.returnSeats = selectedReturnSeats.value.join(',');
  }
  
  router.push({ path: '/booking/payment', query });
};

onMounted(() => {
  realtimeClient = createSeatRealtimeClient({
    tripIds: [outboundTripId, returnTripId],
    onSeatChanged: applyRealtimeEvent
  });
  realtimeClient.connect();
  fetchTripData();
});

onBeforeUnmount(() => {
  hideSpecialSeatNotice();
  realtimeClient?.disconnect();
  if (!proceedingToPayment && (selectedOutboundSeats.value.length || selectedReturnSeats.value.length)) {
    api.post('/seat-holds/release-session', { holdToken }).catch(() => {});
  }
});
</script>
