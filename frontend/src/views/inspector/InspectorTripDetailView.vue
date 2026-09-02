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
          <img v-if="getBusImageUrl(trip)" :src="getBusImageUrl(trip)" class="w-full h-full object-cover" />
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
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-4">
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
        <button class="bg-white p-4 rounded-3xl shadow-sm border border-slate-100 flex flex-col items-center justify-center gap-2 text-emerald-600 hover:bg-emerald-50 transition-colors active:scale-95" @click="showSeatMap = true">
          <div class="w-12 h-12 rounded-full bg-emerald-100 flex items-center justify-center">
            <span class="material-symbols-outlined text-3xl">point_of_sale</span>
          </div>
          <span class="font-black text-body-sm text-center">BÁN VÉ BẾN</span>
        </button>
        <button class="bg-white p-4 rounded-3xl shadow-sm border border-error/20 flex flex-col items-center justify-center gap-2 text-error hover:bg-error/5 transition-colors active:scale-95" @click="showIncidentModal = true">
          <div class="w-12 h-12 rounded-full bg-error/10 flex items-center justify-center">
            <span class="material-symbols-outlined text-3xl">warning</span>
          </div>
          <span class="font-black text-body-sm text-center">BÁO SỰ CỐ</span>
        </button>
      </div>

      <!-- Expenses List -->
      <InspectorExpensesList 
        :expenses="expenses" 
        :loading="expensesLoading"
        @open-expense-modal="showExpenseModal = true"
      />

      <div class="mt-8 items-start">
        
        <!-- Danh sách hành khách -->
        <InspectorPassengerList :bookings="bookings" @manual-checkin="manualCheckIn" />

        </div>

        <!-- Danh sách sự cố đã báo cáo -->
        <div v-if="incidents.length > 0" class="w-full mt-8">
          <div class="flex items-center gap-2 mb-4 px-1">
             <span class="material-symbols-outlined text-error">warning</span>
             <h3 class="text-title-md font-black">Lịch sử sự cố</h3>
          </div>
          <div class="space-y-3">
             <div v-for="incident in incidents" :key="incident.id" class="bg-white p-4 rounded-2xl border border-slate-100 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
                <div>
                   <p class="font-bold text-slate-800">{{ incident.description }}</p>
                   <p class="text-xs text-slate-500 mt-1">{{ new Date(incident.createdAt).toLocaleString('vi-VN') }} - {{ incident.severity === 'CRITICAL' ? 'Khẩn cấp' : (incident.severity === 'HIGH' ? 'Nghiêm trọng' : 'Thấp') }}</p>
                </div>
                <div class="shrink-0">
                   <span v-if="incident.status === 'PENDING'" class="text-xs font-bold bg-amber-100 text-amber-700 px-3 py-1 rounded-full border border-amber-200">Đang chờ xử lý</span>
                   <span v-else class="text-xs font-bold bg-emerald-100 text-emerald-700 px-3 py-1 rounded-full border border-emerald-200 flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">check</span>Đã giải quyết</span>
                </div>
             </div>
          </div>
        </div>

      <!-- Modal Sơ đồ ghế -->
      <InspectorSeatMapModal 
        v-model:show="showSeatMap"
        :tripId="tripId"
        :trip="trip"
        :bookings="bookings"
        :floor1Seats="floor1Seats"
        :floor2Seats="floor2Seats"
        @checkout-success="fetchData"
      />

      <!-- Modal Báo cáo sự cố -->
      <InspectorIncidentModal 
        v-model:show="showIncidentModal"
        @submit="submitIncident"
      />

      <!-- Expense Modal -->
      <InspectorExpenseModal
        :show="showExpenseModal"
        :tripId="trip.id"
        @close="showExpenseModal = false"
        @refresh="fetchExpenses"
      />

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
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import QRScannerModal from '@/components/inspector/QRScannerModal.vue';
import InspectorSeatMapModal from '@/components/inspector/InspectorSeatMapModal.vue';
import InspectorIncidentModal from '@/components/inspector/InspectorIncidentModal.vue';
import InspectorPassengerList from '@/components/inspector/InspectorPassengerList.vue';
import InspectorExpensesList from '@/components/inspector/InspectorExpensesList.vue';
import InspectorExpenseModal from '@/components/inspector/InspectorExpenseModal.vue';

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

const showIncidentModal = ref(false);
const incidents = ref([]);

const showExpenseModal = ref(false);
const expenses = ref([]);
const expensesLoading = ref(false);

const fetchIncidents = async () => {
  try {
    const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/driver/incidents/trip/${tripId}`, authStore.authHeader);
    incidents.value = res.data;
  } catch (error) { console.error("Lỗi tải sự cố:", error); }
};

const submitIncident = async (form) => {
  if (!form.description) return;
  try {
    const payload = {
      tripId: tripId,
      driverId: authStore.currentUser?.id,
      driverName: authStore.currentUser?.fullName,
      severity: form.severity,
      description: form.description
    };
    await axios.post(`${import.meta.env.VITE_API_BASE_URL}/driver/incidents`, payload, authStore.authHeader);
    alert('Đã gửi báo cáo sự cố thành công! Bộ phận điều phối sẽ liên hệ ngay.');
    showIncidentModal.value = false;
    await fetchIncidents();
  } catch (error) {
    console.error(error);
    alert('Không thể gửi báo cáo. Vui lòng gọi trực tiếp cho tổng đài!');
  }
};

const fetchData = async () => {
  if (!trip.value) loading.value = true;
  try {
    // Lấy thông tin chuyến (Lấy từ mảng trips phân công)
    const inspectorId = authStore.currentUser?.id;
    const tripsRes = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/inspector/trips/${inspectorId}`, authStore.authHeader);
    trip.value = tripsRes.data.find(t => t.id == tripId);

    // Lấy song song bookings, seats, và buses
    const [bookingsRes, seatsRes, busRes] = await Promise.all([
      axios.get(`${import.meta.env.VITE_API_BASE_URL}/inspector/trips/${tripId}/bookings`, authStore.authHeader),
      axios.get(`${import.meta.env.VITE_API_BASE_URL}/trips/${tripId}/seats`),
      axios.get(`${import.meta.env.VITE_API_BASE_URL}/buses`)
    ]);

    bookings.value = bookingsRes.data;
    seats.value = seatsRes.data;
    buses.value = busRes.data;
    await fetchIncidents();
  } catch (error) {
    console.error("Lỗi tải chi tiết:", error);
    alert("Không thể tải thông tin chuyến xe!");
  } finally {
    loading.value = false;
  }
};

const fetchExpenses = async () => {
  if (expenses.value.length === 0) expensesLoading.value = true;
  try {
    const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/inspector/trips/${tripId}/expenses`, authStore.authHeader);
    expenses.value = res.data;
  } catch (error) { 
    console.error("Lỗi tải chi phí:", error); 
  } finally {
    expensesLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
  fetchExpenses();
});

const floor1Seats = computed(() => seats.value.filter(s => s.seatFloor === 1));
const floor2Seats = computed(() => seats.value.filter(s => s.seatFloor === 2));

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
    await axios.put(`${import.meta.env.VITE_API_BASE_URL}/inspector/trips/${tripId}/status`, { status: newStatus }, authStore.authHeader);
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
    await axios.put(`${import.meta.env.VITE_API_BASE_URL}/inspector/bookings/${booking.id}/checkin`, {}, authStore.authHeader);
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
    // Ưu tiên parse ticketCode dạng TN-XXXXXX từ QR email
    const ticketMatch = decodedText.match(/Mã đặt vé:\s*(TN-[A-Z0-9]+)/i);
    let booking = null;

    if (ticketMatch && ticketMatch[1]) {
      const ticketCode = ticketMatch[1].toUpperCase();
      booking = bookings.value.find(b => b.ticketCode?.toUpperCase() === ticketCode);
    } else {
      // Fallback: thử parse số ID thuần (quét trực tiếp ID)
      const num = parseInt(decodedText.trim());
      if (!isNaN(num)) {
        booking = bookings.value.find(b => b.id === num);
      }
    }

    if (!booking) {
      qrFeedback.value = { type: 'error', message: '❌ Mã QR không hợp lệ hoặc không thuộc chuyến này!' };
      playBeep(false);
      return;
    }


    if (booking.status === 'CHECKED_IN') {
      qrFeedback.value = { type: 'error', message: `⚠️ CẢNH BÁO: Vé ${booking.ticketCode} ĐÃ ĐƯỢC QUÉT TRƯỚC ĐÓ!` };
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

    await axios.put(`${import.meta.env.VITE_API_BASE_URL}/inspector/bookings/${booking.id}/checkin`, {}, authStore.authHeader);
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
   if (trip.value?.assignedDriverFullName) return trip.value.assignedDriverFullName;
   if (!licensePlate) return 'Chưa phân công xe';
   const bus = buses.value.find(b => b.licensePlate === licensePlate);
   return bus && bus.driverName ? bus.driverName : 'Chưa cập nhật tài xế';
};

const getBusImageUrl = (t) => {
   if (t?.imageUrl) return t.imageUrl;
   if (t?.assignedLicensePlate && buses.value) {
       const bus = buses.value.find(b => b.licensePlate === t.assignedLicensePlate);
       if (bus && bus.imageUrl) return bus.imageUrl;
   }
   return null;
};
</script>
