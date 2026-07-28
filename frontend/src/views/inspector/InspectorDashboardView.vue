<template>
  <div class="space-y-6">
    <!-- Header/Greeting -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h2 class="text-headline-sm font-black tracking-tight">Lịch trình của bạn</h2>
        <p class="text-body-md text-slate-500 mt-1">Danh sách tất cả chuyến xe bạn được phân công</p>
      </div>
      <div class="flex items-center gap-2 w-full sm:w-auto">
         <div class="relative flex-1 sm:w-[180px]">
           <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">calendar_month</span>
           <input 
             type="date" 
             v-model="filterDate"
             class="w-full pl-10 pr-3 py-2.5 bg-white border border-slate-200 rounded-xl text-body-md font-bold focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm text-slate-700" 
           />
         </div>
         <button 
           v-if="filterDate"
           @click="filterDate = ''"
           class="shrink-0 px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-sm rounded-xl transition-colors shadow-sm"
           title="Xem tất cả lịch trình"
         >
           Tất cả
         </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-12 text-slate-400 space-y-3">
      <span class="material-symbols-outlined text-4xl animate-spin">sync</span>
      <p class="text-body-sm font-medium animate-pulse">Đang tải dữ liệu chuyến xe...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border-l-4 border-red-500 p-4 rounded-r-2xl">
      <div class="flex items-start gap-3">
        <span class="material-symbols-outlined text-red-500 mt-0.5">error</span>
        <div>
          <h3 class="text-body-md font-bold text-red-800">Lỗi kết nối</h3>
          <p class="text-body-sm text-red-600 mt-1">{{ error }}</p>
          <button @click="fetchTrips" class="mt-3 text-sm font-bold text-red-700 bg-red-100 px-3 py-1.5 rounded-lg active:scale-95 transition-transform">
            Thử lại
          </button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="filteredTrips.length === 0" class="bg-white rounded-3xl p-8 text-center border border-slate-100 shadow-sm flex flex-col items-center">
      <div class="w-20 h-20 bg-slate-50 rounded-full flex items-center justify-center mb-4">
        <span class="material-symbols-outlined text-4xl text-slate-300">event_available</span>
      </div>
      <h3 class="text-title-md font-bold text-slate-700">Chưa có lịch trình</h3>
      <p class="text-body-sm text-slate-500 mt-2">Không có chuyến xe nào được phân công trong ngày này.</p>
      <button v-if="filterDate" @click="filterDate = ''" class="mt-4 text-sm font-bold text-primary bg-primary/10 px-4 py-2 rounded-xl active:scale-95 transition-transform">
        Xem tất cả ngày
      </button>
    </div>

    <!-- Trip List -->
    <div v-else class="space-y-4">
      <div 
        v-for="trip in filteredTrips" 
        :key="trip.id" 
        class="bg-white rounded-3xl overflow-hidden border border-slate-100 shadow-sm hover:shadow-md transition-shadow cursor-pointer relative"
        @click="$router.push(`/inspector/trip/${trip.id}`)"
      >
        <!-- Status Indicator Strip -->
        <div class="absolute left-0 top-0 bottom-0 w-1.5" :class="getStatusColor(trip.status)"></div>
        
        <div class="p-5 pl-6">
          <div class="flex justify-between items-start mb-3">
            <div class="flex flex-col">
              <span class="text-label-sm font-bold tracking-wider uppercase mb-1" :class="getStatusTextColor(trip.status)">
                {{ getStatusText(trip.status) }}
              </span>
              <h3 class="text-title-lg font-black tracking-tight">{{ trip.departureTime }} - {{ trip.arrivalTime }}</h3>
              <p class="text-body-sm font-medium text-slate-500 mt-0.5">Ngày: {{ formatDate(trip.departureDate) }}</p>
            </div>
            
            <div class="bg-slate-50 border border-slate-100 rounded-xl px-3 py-1.5 text-center">
              <span class="block text-label-xs text-slate-400 font-bold uppercase tracking-wider mb-0.5">Biển số</span>
              <span class="block text-body-sm font-black text-slate-700">{{ trip.assignedLicensePlate || 'Chưa xếp xe' }}</span>
            </div>
          </div>
          
          <div class="flex items-center gap-2 mt-4 relative">
            <div class="w-3 h-3 rounded-full border-2 border-primary bg-white z-10"></div>
            <div class="flex-1 h-px bg-slate-200 border-dashed border-t border-slate-300 absolute left-3 right-3 top-1/2 -translate-y-1/2"></div>
            <div class="w-3 h-3 rounded-full border-2 border-rose-500 bg-white z-10 ml-auto"></div>
          </div>
          <div class="flex justify-between items-center mt-2 text-body-sm font-bold">
            <span class="truncate max-w-[45%]">{{ trip.departurePoint }}</span>
            <span class="truncate max-w-[45%] text-right">{{ trip.arrivalPoint }}</span>
          </div>

          <!-- Thông tin xe & Tài xế -->
          <div class="mt-5 flex gap-4 items-center bg-slate-50 p-3 rounded-xl border border-slate-100 group-hover:bg-white transition-colors">
            <div class="w-20 h-14 rounded-lg overflow-hidden shrink-0 bg-slate-200 border border-slate-200 shadow-sm relative">
              <img v-if="getBusImageUrl(trip)" :src="getBusImageUrl(trip)" class="w-full h-full object-cover" />
              <div v-else class="w-full h-full flex items-center justify-center text-slate-400">
                 <span class="material-symbols-outlined text-[24px]">directions_bus</span>
              </div>
            </div>
            <div class="flex flex-col flex-1 justify-center">
              <div class="flex items-center justify-between mb-1">
                 <span class="text-xs font-black text-slate-800">{{ trip.busType || 'Limousine 24 phòng' }}</span>
                 <span class="text-[9px] font-black text-[#075955] bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-100 uppercase tracking-widest shadow-sm">Tài xế</span>
              </div>
              <div class="text-xs font-medium text-slate-500 flex items-center justify-between">
                 <span>Họ & Tên:</span>
                 <span class="font-bold text-slate-800">{{ getRealDriverName(trip) }}</span>
              </div>
            </div>
          </div>

          <!-- Divider -->
          <hr class="border-slate-100 my-4" />

          <div class="flex items-center justify-between text-body-sm">
            <div class="flex items-center gap-2 text-slate-600 font-medium">
              <span class="material-symbols-outlined text-[18px]">airline_seat_recline_normal</span>
              <span>Ghế trống: <strong class="text-primary">{{ trip.availableSeats }}</strong>/{{ trip.totalSeats || 24 }}</span>
            </div>
            <div class="flex items-center text-primary font-bold gap-1 group-hover:translate-x-1 transition-transform">
              Xem chi tiết <span class="material-symbols-outlined text-[18px]">arrow_forward</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Spacer for bottom navigation -->
    <div class="h-24 sm:h-32 w-full"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const trips = ref([]);
const buses = ref([]);
const loading = ref(true);
const error = ref('');
const getTodayStr = () => {
  const d = new Date();
  const month = '' + (d.getMonth() + 1);
  const day = '' + d.getDate();
  const year = d.getFullYear();
  return [year, month.padStart(2, '0'), day.padStart(2, '0')].join('-');
};

const filterDate = ref(getTodayStr()); // Mặc định hiển thị ngày hôm nay

const filteredTrips = computed(() => {
  if (!filterDate.value) return trips.value;
  return trips.value.filter(t => t.departureDate === filterDate.value);
});

const fetchTripsAndBuses = async () => {
  loading.value = true;
  error.value = '';
  try {
    const inspectorId = authStore.currentUser?.id;
    if (!inspectorId) {
      throw new Error("Không xác định được danh tính nhân viên.");
    }
    
    // Gọi song song API lấy danh sách chuyến xe và danh sách xe
    const [tripRes, busRes] = await Promise.all([
       axios.get(`${import.meta.env.VITE_API_BASE_URL}/inspector/trips/${inspectorId}`, authStore.authHeader),
       axios.get(`${import.meta.env.VITE_API_BASE_URL}/buses`)
    ]);
    
    buses.value = busRes.data;

    // Sort: Trạng thái SCHEDULED / IN_PROGRESS lên đầu, theo giờ xuất phát
    trips.value = tripRes.data.sort((a, b) => {
      if (a.status === 'COMPLETED' && b.status !== 'COMPLETED') return 1;
      if (a.status !== 'COMPLETED' && b.status === 'COMPLETED') return -1;
      return a.departureTime.localeCompare(b.departureTime);
    });
  } catch (err) {
    console.error(err);
    error.value = err.response?.data?.message || 'Không thể kết nối đến máy chủ.';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchTripsAndBuses();
});

// Utilities
const formatDate = (dateStr) => {
  if(!dateStr) return 'Hôm nay';
  try {
    const parts = dateStr.split('-');
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
  } catch(e) { return dateStr; }
};

const getStatusText = (status) => {
  switch(status) {
    case 'SCHEDULED': return 'Chưa khởi hành';
    case 'IN_PROGRESS': return 'Đang chạy';
    case 'COMPLETED': return 'Đã hoàn thành';
    case 'CANCELLED': return 'Đã hủy';
    default: return 'Chưa khởi hành';
  }
};

const getStatusColor = (status) => {
  switch(status) {
    case 'SCHEDULED': return 'bg-amber-400';
    case 'IN_PROGRESS': return 'bg-primary';
    case 'COMPLETED': return 'bg-emerald-500';
    case 'CANCELLED': return 'bg-rose-500';
    default: return 'bg-amber-400';
  }
};

const getStatusTextColor = (status) => {
  switch(status) {
    case 'SCHEDULED': return 'text-amber-600';
    case 'IN_PROGRESS': return 'text-primary';
    case 'COMPLETED': return 'text-emerald-600';
    case 'CANCELLED': return 'text-rose-600';
    default: return 'text-amber-600';
  }
};

const getRealDriverName = (trip) => {
   if (trip?.assignedDriverFullName) return trip.assignedDriverFullName;
   if (!trip?.assignedLicensePlate) return 'Chưa phân công xe';
   const bus = buses.value.find(b => b.licensePlate === trip.assignedLicensePlate);
   return bus && bus.driverName ? bus.driverName : 'Chưa cập nhật tài xế';
};

const getBusImageUrl = (trip) => {
   if (trip?.imageUrl) return trip.imageUrl;
   if (trip?.assignedLicensePlate && buses.value) {
       const bus = buses.value.find(b => b.licensePlate === trip.assignedLicensePlate);
       if (bus && bus.imageUrl) return bus.imageUrl;
   }
   return null;
};
</script>
