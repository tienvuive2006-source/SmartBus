<template>
  <div class="space-y-6 pb-12">
    <!-- Header Back Navigation -->
    <div class="flex items-center gap-3">
      <button @click="$router.push('/driver')" class="w-10 h-10 rounded-full bg-white shadow-sm flex items-center justify-center text-slate-600 hover:bg-slate-50 transition-colors">
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
      
      <!-- Thông tin xe & Lơ xe -->
      <div class="bg-white p-4 rounded-3xl shadow-sm border border-slate-100 flex gap-4 items-center mb-4 mt-2">
        <div class="w-20 h-14 rounded-lg overflow-hidden shrink-0 bg-slate-200 border border-slate-200 shadow-sm relative">
          <img v-if="trip.imageUrl" :src="trip.imageUrl" class="w-full h-full object-cover" />
          <div v-else class="w-full h-full flex items-center justify-center text-slate-400">
             <span class="material-symbols-outlined text-[24px]">directions_bus</span>
          </div>
        </div>
        <div class="flex flex-col flex-1 justify-center">
          <div class="flex items-center justify-between mb-1">
             <span class="text-sm font-black text-slate-800">{{ trip.busType || 'Limousine 24 phòng' }}</span>
             <span class="text-[9px] font-black text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded-md border border-indigo-100 uppercase tracking-widest shadow-sm">Lơ xe</span>
          </div>
          <div class="text-xs font-medium text-slate-500 flex items-center justify-between">
             <span>Họ & Tên:</span>
             <span class="font-bold text-slate-800">{{ trip.inspector ? trip.inspector.fullName : 'Chưa phân công' }}</span>
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
            :class="trip.status === 'SCHEDULED' && isTimeValidToDepart ? 'bg-amber-500 text-white shadow-md active:scale-95' : 'bg-slate-100 text-slate-400'"
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

      <!-- Bản đồ lộ trình -->
      <div class="bg-white p-4 rounded-3xl shadow-sm border border-slate-100 flex flex-col gap-3 mb-4 relative z-0">
        <div class="flex items-center justify-between px-1">
          <h3 class="text-sm font-black text-slate-800 uppercase tracking-widest flex items-center gap-2">
            <span class="material-symbols-outlined text-amber-500 text-[18px]">map</span>
            Bản đồ lộ trình
          </h3>
          <span class="text-[10px] font-black text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100 uppercase tracking-widest">GPS Live</span>
        </div>
        <div id="driver-route-map" class="w-full h-[400px] md:h-[500px] rounded-2xl border-2 border-slate-100 overflow-hidden z-0"></div>
      </div>

      <!-- Action Buttons -->
      <div class="grid grid-cols-2 gap-4">
        <button class="bg-white p-4 rounded-3xl shadow-sm border border-slate-100 flex flex-col items-center justify-center gap-2 text-indigo-600 hover:bg-indigo-50 transition-colors active:scale-95" @click="showSeatMap = true">
          <div class="w-12 h-12 rounded-full bg-indigo-100 flex items-center justify-center">
            <span class="material-symbols-outlined text-3xl">airline_seat_recline_normal</span>
          </div>
          <span class="font-black text-body-sm text-center">SƠ ĐỒ GHẾ</span>
        </button>
        <button class="bg-white p-4 rounded-3xl shadow-sm border border-error/20 flex flex-col items-center justify-center gap-2 text-error hover:bg-error/5 transition-colors active:scale-95" @click="showIncidentModal = true">
          <div class="w-12 h-12 rounded-full bg-error/10 flex items-center justify-center">
            <span class="material-symbols-outlined text-3xl">warning</span>
          </div>
          <span class="font-black text-body-sm text-center">BÁO SỰ CỐ</span>
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
                <button v-if="booking.status === 'CHECKED_IN'" disabled class="px-4 py-1.5 bg-primary/10 text-primary font-bold text-body-sm rounded-xl flex items-center gap-1">
                  <span class="material-symbols-outlined text-[18px]">check_circle</span> Hoàn tất
                </button>
              </div>
            </div>
          </div>
        </div>

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

      <!-- Modal Báo cáo sự cố -->
      <Teleport to="body">
        <div v-if="showIncidentModal" class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showIncidentModal = false"></div>
          <div class="relative w-full sm:max-w-md bg-white sm:rounded-[2rem] rounded-t-[2rem] shadow-2xl overflow-hidden flex flex-col animate-slide-up sm:animate-fade-in-up">
            
            <div class="px-6 py-5 border-b border-slate-100 flex justify-between items-center bg-white">
              <h3 class="text-title-md font-black text-error flex items-center gap-2">
                <span class="material-symbols-outlined">warning</span>
                Báo cáo sự cố khẩn cấp
              </h3>
              <button @click="showIncidentModal = false" class="w-10 h-10 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors">
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>
            
            <div class="p-6 overflow-y-auto bg-slate-50 space-y-4">
              <div>
                <label class="block text-label-md font-bold text-slate-700 mb-1">Mức độ sự cố</label>
                <select v-model="incidentForm.severity" class="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-body-md font-bold focus:outline-none focus:border-error focus:ring-2 focus:ring-error/20">
                  <option value="" disabled>-- Chọn mức độ sự cố --</option>
                  <option value="LOW">Thấp (Trễ giờ, Tắc đường)</option>
                  <option value="HIGH">Cao (Hư hỏng nhẹ, Khách ốm)</option>
                  <option value="CRITICAL">Khẩn cấp (Tai nạn, Hư hỏng nặng)</option>
                </select>
              </div>
              <div>
                <label class="block text-label-md font-bold text-slate-700 mb-1">Mô tả chi tiết</label>
                <textarea v-model="incidentForm.description" rows="4" class="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-body-md focus:outline-none focus:border-error focus:ring-2 focus:ring-error/20" placeholder="Nhập chi tiết sự cố xảy ra..."></textarea>
              </div>
            </div>

            <div class="p-6 bg-white border-t border-slate-100">
              <button @click="submitIncident" :disabled="!incidentForm.description || !incidentForm.severity" class="w-full bg-error text-white font-black py-4 rounded-2xl active:scale-95 transition-transform disabled:opacity-50">
                GỬI BÁO CÁO NGAY
              </button>
            </div>
          </div>
        </div>
      </Teleport>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import { decodePolyline, fetchPolylineFromCloudinary } from '@/utils/polyline';

const route = useRoute();
const authStore = useAuthStore();
const tripId = route.params.id;

const loading = ref(true);
const trip = ref(null);
const buses = ref([]);
const bookings = ref([]);
const seats = ref([]);
const showSeatMap = ref(false);
const leafletMap = ref(null);

const showIncidentModal = ref(false);
const incidentForm = ref({ severity: '', description: '' });
const incidents = ref([]);

const fetchIncidents = async () => {
  try {
    const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/driver/incidents/trip/${tripId}`, authStore.authHeader);
    incidents.value = res.data;
  } catch (error) { console.error("Lỗi tải sự cố:", error); }
};

const submitIncident = async () => {
  if (!incidentForm.value.description) return;
  try {
    const payload = {
      tripId: tripId,
      driverId: authStore.currentUser?.id,
      driverName: authStore.currentUser?.fullName,
      severity: incidentForm.value.severity,
      description: incidentForm.value.description
    };
    await axios.post(`${import.meta.env.VITE_API_BASE_URL}/driver/incidents`, payload, authStore.authHeader);
    alert('Đã gửi báo cáo sự cố thành công! Bộ phận điều phối sẽ liên hệ ngay.');
    showIncidentModal.value = false;
    incidentForm.value = { severity: '', description: '' };
    await fetchIncidents(); // Tải lại danh sách sự cố
  } catch (error) {
    console.error(error);
    alert('Không thể gửi báo cáo. Vui lòng gọi trực tiếp cho tổng đài!');
  }
};

const fetchData = async () => {
  loading.value = true;
  try {
    // Lấy thông tin chuyến bằng API public
    const tripsRes = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/trips/${tripId}`);
    trip.value = tripsRes.data;

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

    // Load Map after data is ready
    nextTick(() => {
      initMap();
    });
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
    await axios.put(`${import.meta.env.VITE_API_BASE_URL}/inspector/trips/${tripId}/status`, { status: newStatus }, authStore.authHeader);
    trip.value.status = newStatus;
  } catch (err) {
    alert("Cập nhật thất bại!");
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
    case 'IN_PROGRESS': return 'text-amber-600 bg-amber-50 border border-amber-200';
    case 'COMPLETED': return 'text-emerald-600 bg-emerald-50 border border-emerald-200';
    case 'CANCELLED': return 'text-rose-600 bg-rose-50 border border-rose-200';
    default: return 'text-slate-600 bg-slate-50 border border-slate-200';
  }
};



// Map Logic
const initMap = () => {
  if (!document.getElementById('leaflet-css')) {
    const link = document.createElement('link');
    link.id = 'leaflet-css'; link.rel = 'stylesheet';
    link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
    document.head.appendChild(link);
  }
  const scriptId = 'leaflet-script';
  if (!window.L && !document.getElementById(scriptId)) {
    const script = document.createElement('script');
    script.id = scriptId; script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
    script.onload = () => renderLeaflet();
    document.head.appendChild(script);
  } else { setTimeout(renderLeaflet, 400); }
};

const renderLeaflet = async () => {
  const L = window.L; if (!L) return;
  const container = document.getElementById('driver-route-map');
  if (!container || !trip.value) return;
  
  if (leafletMap.value) leafletMap.value.remove();
  
  // Set view mặc định vào Đà Nẵng nếu không có toạ độ
  const startLat = trip.value.departureLat || 16.0;
  const startLng = trip.value.departureLng || 108.0;
  
  leafletMap.value = L.map(container).setView([startLat, startLng], 6);
  L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
    maxZoom: 20, subdomains: ['mt0', 'mt1', 'mt2', 'mt3'], attribution: '© Google Maps'
  }).addTo(leafletMap.value);

  // Markers
  const from = [trip.value.departureLat, trip.value.departureLng];
  const to = [trip.value.arrivalLat, trip.value.arrivalLng];

  const startIcon = L.divIcon({ 
    html: `<div class="w-8 h-8 bg-emerald-500 border-4 border-white rounded-full shadow-lg flex items-center justify-center text-white"><span class="material-symbols-outlined text-sm">trip_origin</span></div>`, 
    className: '', iconSize: [32, 32] 
  });
  
  const endIcon = L.divIcon({ 
    html: `<div class="w-8 h-8 bg-rose-600 border-4 border-white rounded-full shadow-lg flex items-center justify-center text-white"><span class="material-symbols-outlined text-sm">location_on</span></div>`, 
    className: '', iconSize: [32, 32] 
  });

  if (from[0] > 1 && to[0] > 1) {
    L.marker(from, { icon: startIcon }).addTo(leafletMap.value).bindPopup('<b>Điểm đi:</b> ' + trip.value.departurePoint);
    L.marker(to, { icon: endIcon }).addTo(leafletMap.value).bindPopup('<b>Điểm đến:</b> ' + trip.value.arrivalPoint);
    
    // Default straight line
    const fallbackLine = L.polyline([from, to], { 
      color: '#f59e0b', weight: 2, dashArray: '5, 10', opacity: 0.5 
    }).addTo(leafletMap.value);
    leafletMap.value.fitBounds([from, to], { padding: [30, 30] });

    if (trip.value.routeData) {
      try {
        let routeStr = trip.value.routeData;
        if (routeStr.startsWith('http')) {
            routeStr = await fetchPolylineFromCloudinary(routeStr);
        }
        let coords = [];
        if (routeStr.startsWith('[')) {
            coords = JSON.parse(routeStr);
        } else if (routeStr) {
            coords = decodePolyline(routeStr);
        }
        if (coords && coords.length > 0) {
          leafletMap.value.removeLayer(fallbackLine);
          L.polyline(coords, { 
            color: '#f59e0b', weight: 5, opacity: 0.9, lineJoin: 'round', lineCap: 'round'
          }).addTo(leafletMap.value);
          leafletMap.value.fitBounds(coords, { padding: [30, 30] });
        }
      } catch (e) { console.error("Lỗi parse routeData", e); }
    }
  } else if (from[0] > 1) {
    L.marker(from, { icon: startIcon }).addTo(leafletMap.value); 
    leafletMap.value.setView(from, 13);
  }
};
</script>
