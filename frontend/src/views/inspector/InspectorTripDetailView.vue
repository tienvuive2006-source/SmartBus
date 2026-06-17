<template>
  <div class="space-y-6 pb-12">
    <!-- Header Back Navigation -->
    <div class="flex items-center gap-3">
      <button @click="$router.push('/inspector')" class="w-10 h-10 rounded-full bg-white shadow-sm flex items-center justify-center text-slate-600 hover:bg-slate-50 transition-colors">
        <span class="material-symbols-outlined">arrow_back</span>
      </button>
      <div>
        <h2 class="text-title-lg font-black tracking-tight">Chi tiết chuyến xe</h2>
        <p class="text-body-sm text-slate-500 font-medium" v-if="trip">
          {{ trip.departureTime }} ({{ trip.departurePoint }})
        </p>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center py-12">
      <span class="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></span>
    </div>

    <template v-else-if="trip">
      
      <!-- Thông tin xe & Tài xế -->
      <div class="bg-white p-4 rounded-3xl shadow-sm border border-slate-100 flex gap-4 items-center mb-4">
        <div class="w-20 h-14 rounded-lg overflow-hidden shrink-0 bg-slate-200 border border-slate-200 shadow-sm relative">
          <img v-if="trip.imageUrl" :src="trip.imageUrl" class="w-full h-full object-cover" />
          <div v-else class="w-full h-full flex items-center justify-center text-slate-400">
             <span class="material-symbols-outlined text-[24px]">directions_bus</span>
          </div>
        </div>
        <div class="flex flex-col flex-1 justify-center">
          <div class="flex items-center justify-between mb-1">
             <span class="text-sm font-black text-slate-800">{{ trip.busType || 'Limousine 24 phòng' }}</span>
             <span class="text-[9px] font-black text-[#075955] bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-100 uppercase tracking-widest shadow-sm">Tài xế</span>
          </div>
          <div class="text-xs font-medium text-slate-500 flex items-center justify-between">
             <span>Họ & Tên:</span>
             <span class="font-bold text-slate-800">{{ getRealDriverName(trip.assignedLicensePlate) }}</span>
          </div>
        </div>
      </div>


      <!-- Trip Status Control -->
      <div class="bg-white p-5 rounded-3xl shadow-sm border border-slate-100 flex flex-col gap-4 mb-4">
        <div class="flex justify-between items-center">
          <span class="text-label-md font-bold text-slate-500 uppercase tracking-wider">Trạng thái chuyến:</span>
          <span class="px-3 py-1 bg-slate-100 text-slate-700 font-bold rounded-lg text-body-sm" :class="getStatusColorClass(trip.status)">
            {{ getStatusText(trip.status) }}
          </span>
        </div>
        
        <div class="grid grid-cols-2 gap-3">
          <button 
            @click="updateStatus('IN_PROGRESS')" 
            :disabled="trip.status !== 'SCHEDULED' || !isTimeValidToDepart"
            class="py-3 rounded-2xl font-black text-body-sm transition-all disabled:opacity-50"
            :class="trip.status === 'SCHEDULED' && isTimeValidToDepart ? 'bg-primary text-white shadow-md active:scale-95' : 'bg-slate-100 text-slate-400'"
          >
            {{ trip.status === 'SCHEDULED' && !isTimeValidToDepart ? 'CHƯA ĐẾN GIỜ' : 'XE XUẤT BẾN' }}
          </button>
          <button 
            @click="updateStatus('COMPLETED')" 
            :disabled="trip.status !== 'IN_PROGRESS'"
            class="py-3 rounded-2xl font-black text-body-sm transition-all disabled:opacity-50"
            :class="trip.status === 'IN_PROGRESS' ? 'bg-emerald-500 text-white shadow-md active:scale-95' : 'bg-slate-100 text-slate-400'"
          >
            ĐẾN BẾN CUỐI
          </button>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="grid grid-cols-2 sm:grid-cols-3 gap-4">
        <button class="bg-white p-4 rounded-3xl shadow-sm border border-slate-100 flex flex-col items-center justify-center gap-2 text-indigo-600 hover:bg-indigo-50 transition-colors active:scale-95" @click="showSeatMap = true">
          <div class="w-12 h-12 rounded-full bg-indigo-100 flex items-center justify-center">
            <span class="material-symbols-outlined text-3xl">airline_seat_recline_normal</span>
          </div>
          <span class="font-black text-body-sm text-center">SƠ ĐỒ GHẾ</span>
        </button>
        <button class="bg-white p-4 rounded-3xl shadow-sm border border-slate-100 flex flex-col items-center justify-center gap-2 text-primary hover:bg-primary/5 transition-colors active:scale-95" @click="openQRScanner">
          <div class="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
            <span class="material-symbols-outlined text-3xl">qr_code_scanner</span>
          </div>
          <span class="font-black text-body-sm text-center">QUÉT MÃ QR</span>
        </button>
        <button class="bg-white p-4 rounded-3xl shadow-sm border border-slate-100 flex flex-col items-center justify-center gap-2 text-emerald-600 hover:bg-emerald-50 transition-colors active:scale-95" @click="handleBuyOffline">
          <div class="w-12 h-12 rounded-full bg-emerald-100 flex items-center justify-center">
            <span class="material-symbols-outlined text-3xl">point_of_sale</span>
          </div>
          <span class="font-black text-body-sm text-center">BÁN VÉ TẠI XE</span>
        </button>
      </div>

      <div class="mt-8 items-start">
        
        <!-- Danh sách hành khách -->
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
                  @click="manualCheckIn(booking)"
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

        </div>

        <!-- Modal Sơ đồ ghế -->
        <Teleport to="body">
          <div v-if="showSeatMap" class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 animate-fade-in">
            <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showSeatMap = false"></div>
            <div class="relative w-full sm:max-w-[700px] bg-white sm:rounded-[2rem] rounded-t-[2rem] shadow-2xl overflow-hidden animate-slide-up sm:animate-fade-in-up flex flex-col max-h-[90vh]">
              
              <div class="px-6 py-5 border-b border-slate-100 flex justify-between items-center bg-white shrink-0 sticky top-0 z-10">
                <h3 class="text-title-md font-black text-slate-800">Sơ đồ ghế trên xe</h3>
                <button @click="showSeatMap = false" class="w-10 h-10 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors">
                  <span class="material-symbols-outlined">close</span>
                </button>
              </div>
              
              <div class="p-6 overflow-y-auto bg-slate-50">

          <!-- Legend -->
          <div class="bg-white p-4 border border-slate-100 rounded-2xl flex flex-wrap items-center justify-center gap-6 md:gap-12 shadow-sm mb-6">
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
                  <span class="text-xs font-black text-white uppercase tracking-widest">Đã lên xe</span>
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
                        :class="['w-12 h-14 shrink-0 border-2 rounded-xl flex flex-col items-center justify-center font-black relative overflow-hidden',
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
                        :class="['w-12 h-14 shrink-0 border-2 rounded-xl flex flex-col items-center justify-center font-black relative overflow-hidden',
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
          </div>
        </div>
      </Teleport>

      <QRScannerModal 
        :isOpen="showQRScanner"
        :feedback="qrFeedback"
        @close="showQRScanner = false"
        @scan="onScanSuccess"
      />

    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import QRScannerModal from '@/components/inspector/QRScannerModal.vue';

const route = useRoute();
const authStore = useAuthStore();
const tripId = route.params.id;

const loading = ref(true);
const trip = ref(null);
const buses = ref([]);
const bookings = ref([]);
const seats = ref([]);
const showSeatMap = ref(false);

const showQRScanner = ref(false);
const qrFeedback = ref(null);

const fetchData = async () => {
  loading.value = true;
  try {
    // Lấy thông tin chuyến (Lấy từ mảng trips phân công)
    const inspectorId = authStore.currentUser?.id;
    const tripsRes = await axios.get(`http://localhost:8080/api/inspector/trips/${inspectorId}`, authStore.authHeader);
    trip.value = tripsRes.data.find(t => t.id == tripId);

    // Lấy song song bookings, seats, và buses
    const [bookingsRes, seatsRes, busRes] = await Promise.all([
      axios.get(`http://localhost:8080/api/inspector/trips/${tripId}/bookings`, authStore.authHeader),
      axios.get(`http://localhost:8080/api/trips/${tripId}/seats`),
      axios.get(`http://localhost:8080/api/buses`)
    ]);

    bookings.value = bookingsRes.data;
    seats.value = seatsRes.data;
    buses.value = busRes.data;
  } catch (error) {
    console.error("Lỗi tải chi tiết:", error);
    alert("Không thể tải thông tin chuyến xe!");
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
});

const checkedInCount = computed(() => {
  return bookings.value.filter(b => b.status === 'CHECKED_IN').length;
});

const floor1Seats = computed(() => seats.value.filter(s => s.seatFloor === 1));
const floor2Seats = computed(() => seats.value.filter(s => s.seatFloor === 2));

const getSeatStatus = (seatNumber) => {
  const booking = bookings.value.find(b => b.seatNumbers.includes(seatNumber));
  if (!booking || booking.status === 'CANCELLED') return 'EMPTY';
  if (booking.status === 'CHECKED_IN') return 'CHECKED_IN';
  return 'BOOKED';
};

const isTimeValidToDepart = computed(() => {
  if (!trip.value) return false;
  try {
    const depDateStr = trip.value.departureDate;
    let depTimeStr = trip.value.departureTime;
    if (!depDateStr || !depTimeStr) return false;
    
    // Đảm bảo định dạng hh:mm:ss
    if (depTimeStr.length === 5) depTimeStr += ':00';
    
    const depDateTime = new Date(`${depDateStr}T${depTimeStr}`);
    const now = new Date();
    
    return now >= depDateTime;
  } catch (e) {
    return false;
  }
});

const updateStatus = async (newStatus) => {
  if(!confirm(`Xác nhận cập nhật trạng thái chuyến xe thành: ${getStatusText(newStatus)}?`)) return;
  try {
    await axios.put(`http://localhost:8080/api/inspector/trips/${tripId}/status`, { status: newStatus }, authStore.authHeader);
    trip.value.status = newStatus;
  } catch (err) {
    alert("Cập nhật thất bại!");
  }
};



const manualCheckIn = async (booking) => {
  let msg = `Xác nhận khách hàng ${booking.customerName} đã lên xe?`;
  if (booking.status === 'PENDING') {
    msg = `⚠️ Khách hàng ${booking.customerName} CHƯA THANH TOÁN.\nXác nhận ĐÃ THU TIỀN (${booking.totalPrice?.toLocaleString() || 0}đ) và cho khách lên xe?`;
  }
  if(!confirm(msg)) return;
  try {
    await axios.put(`http://localhost:8080/api/inspector/bookings/${booking.id}/checkin`, {}, authStore.authHeader);
    booking.status = 'CHECKED_IN';
  } catch (err) {
    alert("Cập nhật thất bại!");
  }
};

const openQRScanner = () => {
  showQRScanner.value = true;
  qrFeedback.value = null;
};

// Cờ chống quét liên tục cùng 1 vé
let isProcessingQR = false;

const onScanSuccess = async (decodedText) => {
  if (isProcessingQR) return;
  isProcessingQR = true;

  try {
    const idMatch = decodedText.match(/Mã đặt vé: #(\d+)/);
    let qrBookingId = null;
    
    if (idMatch && idMatch[1]) {
      qrBookingId = parseInt(idMatch[1]);
    } else {
      const num = parseInt(decodedText);
      if (!isNaN(num)) qrBookingId = num;
    }

    if (!qrBookingId) {
      qrFeedback.value = { type: 'error', message: '❌ Mã QR không hợp lệ hoặc không phải vé xe!' };
      return;
    }

    const booking = bookings.value.find(b => b.id === qrBookingId);
    
    if (!booking) {
      qrFeedback.value = { type: 'error', message: `❌ Vé #${qrBookingId} KHÔNG HỢP LỆ (Không đúng chuyến hoặc ĐÃ BỊ HỦY)!` };
      playBeep(false);
      return;
    }

    if (booking.status === 'CHECKED_IN') {
      qrFeedback.value = { type: 'error', message: `⚠️ CẢNH BÁO: Vé #${qrBookingId} ĐÃ ĐƯỢC QUÉT TRƯỚC ĐÓ!` };
      playBeep(false);
      return;
    }

    const wasPending = booking.status === 'PENDING';

    if (wasPending) {
      // Show warning directly on the scanner UI and DO NOT check in automatically
      qrFeedback.value = { 
        type: 'warning', 
        message: `⚠️ CHƯA THANH TOÁN: Cần thu ${booking.totalPrice?.toLocaleString() || 0}đ\nKhách: ${booking.customerName}. Vui lòng thu tiền rồi Check-in thủ công ở danh sách bên dưới!` 
      };
      playBeep(false);
      return;
    }

    await axios.put(`http://localhost:8080/api/inspector/bookings/${booking.id}/checkin`, {}, authStore.authHeader);
    booking.status = 'CHECKED_IN';
    
    qrFeedback.value = { type: 'success', message: `✅ Check-in thành công: ${booking.customerName}` };
    playBeep(true);

  } catch (error) {
    console.error(error);
    qrFeedback.value = { type: 'error', message: '❌ Máy chủ từ chối Check-in. Vui lòng thử lại!' };
  } finally {
    setTimeout(() => {
      isProcessingQR = false;
    }, 2500);
  }
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
    console.warn("Trình duyệt không hỗ trợ Web Audio API");
  }
};

const handleBuyOffline = () => {
  alert("Tính năng bán vé trực tiếp trên xe sẽ được liên kết tới màn hình Chọn ghế trong tương lai.");
};

// Utils
const getStatusText = (status) => {
  switch(status) {
    case 'SCHEDULED': return 'Chưa khởi hành';
    case 'IN_PROGRESS': return 'Đang chạy';
    case 'COMPLETED': return 'Đã hoàn thành';
    case 'CANCELLED': return 'Đã hủy';
    default: return 'Chưa khởi hành';
  }
};
const getStatusColorClass = (status) => {
  switch(status) {
    case 'SCHEDULED': return 'text-amber-600 bg-amber-50 border border-amber-200';
    case 'IN_PROGRESS': return 'text-primary bg-primary/10 border border-primary/20';
    case 'COMPLETED': return 'text-emerald-600 bg-emerald-50 border border-emerald-200';
    case 'CANCELLED': return 'text-rose-600 bg-rose-50 border border-rose-200';
    default: return 'text-slate-600 bg-slate-50 border border-slate-200';
  }
};

const getRealDriverName = (licensePlate) => {
   if (!licensePlate) return 'Chưa phân công xe';
   const bus = buses.value.find(b => b.licensePlate === licensePlate);
   return bus && bus.driverName ? bus.driverName : 'Chưa cập nhật tài xế';
};
</script>
