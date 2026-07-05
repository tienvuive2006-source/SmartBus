<template>
  <div class="p-6 md:p-8 max-w-[1600px] mx-auto bg-slate-50 min-h-screen font-sans">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
      <div>
        <h2 class="text-2xl font-extrabold text-slate-800 tracking-tight">
          Lịch Phân Công Tài Xế
        </h2>
        <p class="text-xs font-semibold text-slate-500 mt-1">Điều phối lịch trình và trạng thái thời gian thực</p>
      </div>
      <div class="flex flex-wrap items-center gap-3">
        <input 
          type="date" 
          v-model="selectedDate" 
          class="bg-white border border-slate-200 px-4 py-2.5 rounded-xl text-sm font-bold text-slate-700 outline-none focus:border-[#075955] focus:ring-2 focus:ring-[#075955]/20 shadow-sm transition-all cursor-pointer"
          @change="fetchTrips"
        />
        <div class="relative">
          <input 
            v-model="filterDriverName" 
            type="text" 
            placeholder="🔍 Lọc tài xế..."
            class="bg-white border border-slate-200 px-4 py-2.5 rounded-xl text-sm font-bold text-slate-700 outline-none focus:border-[#075955] focus:ring-2 focus:ring-[#075955]/20 shadow-sm transition-all w-[180px]"
          />
        </div>
        <select 
          v-model="filterStatus" 
          class="bg-white border border-slate-200 px-4 py-2.5 rounded-xl text-sm font-bold text-slate-700 outline-none focus:border-[#075955] shadow-sm cursor-pointer"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="FREE">🟢 Đang rảnh</option>
          <option value="DRIVING">🔵 Đang chạy</option>
          <option value="ON_LEAVE">🟠 Nghỉ phép</option>
        </select>
        <button 
          @click="fetchData" 
          class="bg-[#075955] hover:bg-[#064a47] text-white px-4 py-2.5 rounded-xl shadow-sm hover:shadow-md transition-all font-bold text-xs flex items-center gap-2"
        >
          <span class="material-symbols-outlined text-[18px]" :class="{'animate-spin': loading}">sync</span>
          Tải lại
        </button>
      </div>
    </div>

    <!-- Status Legend -->
    <div class="flex flex-wrap items-center gap-6 mb-6 bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
      <div class="flex items-center gap-2 text-xs font-bold text-slate-600">
        <div class="w-3 h-3 rounded-full bg-slate-200"></div> Đang rảnh (Trống lịch)
      </div>
      <div class="flex items-center gap-2 text-xs font-bold text-slate-600">
        <div class="w-3 h-3 rounded-full bg-amber-400"></div> Chờ khởi hành (Assigned)
      </div>
      <div class="flex items-center gap-2 text-xs font-bold text-slate-600">
        <div class="w-3 h-3 rounded-full bg-blue-500"></div> Đang chạy (In Progress)
      </div>
      <div class="flex items-center gap-2 text-xs font-bold text-slate-600">
        <div class="w-3 h-3 rounded-full bg-emerald-500"></div> Đã hoàn thành (Completed)
      </div>
      <div class="flex items-center gap-2 text-xs font-bold text-slate-600">
        <div class="w-3 h-3 rounded-full bg-rose-500 border border-rose-600"></div> Đụng lịch (Xung đột)
      </div>
    </div>

    <!-- Timeline Board -->
    <div class="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden flex flex-col relative min-h-[600px]">
      
      <!-- Loading Overlay -->
      <div v-if="loading" class="absolute inset-0 bg-white/60 backdrop-blur-sm z-50 flex flex-col items-center justify-center">
        <div class="w-12 h-12 border-4 border-[#075955] border-t-transparent rounded-full animate-spin mb-4"></div>
        <span class="text-sm font-bold text-slate-600">Đang đồng bộ lịch trình...</span>
      </div>

      <!-- Scrollable Timeline Area -->
      <div class="flex-1 overflow-auto relative bg-[#f8fafc]">
         <div class="min-w-max">
            <!-- X-Axis Header (Hours) -->
            <div class="flex border-b border-slate-200 bg-slate-100/50 sticky top-0 z-40 w-fit min-w-full">
               <div class="w-64 shrink-0 p-4 border-r border-slate-200 bg-slate-50 flex items-center justify-between shadow-[2px_0_5px_rgba(0,0,0,0.02)] sticky left-0 z-50">
                  <span class="text-xs font-black uppercase text-slate-500 tracking-widest">Tài xế</span>
                  <span class="text-[10px] font-bold text-slate-400 bg-slate-200 px-2 py-0.5 rounded-md">{{ drivers.length }}</span>
               </div>
               <!-- 24 Hours Timeline Header (1440px min width = 1px per min, but let's use min-w-[2400px] on desktop for better readability) -->
               <div class="w-[1440px] md:w-[2400px] relative h-14 shrink-0">
                  <div class="absolute inset-0 flex">
                     <div v-for="hour in 24" :key="hour" class="flex-1 border-r border-slate-200/50 flex flex-col justify-end pb-1 relative">
                        <span class="absolute -left-3 bottom-1 text-[10px] font-black text-slate-400 select-none">{{ (hour-1).toString().padStart(2, '0') }}:00</span>
                     </div>
                  </div>
                  <!-- Current Time Indicator Line -->
                  <div v-if="isToday" class="absolute top-0 bottom-0 w-[2px] bg-red-500 z-10" :style="{ left: currentTimePercentage + '%' }">
                     <div class="absolute -top-0 -left-1.5 w-3.5 h-3.5 bg-red-500 rounded-full border-2 border-white shadow-sm"></div>
                  </div>
               </div>
            </div>

            <!-- Empty State -->
            <div v-if="!loading && drivers.length === 0" class="flex flex-col items-center justify-center py-20 text-slate-400 sticky left-0 w-full">
               <span class="material-symbols-outlined text-5xl mb-2">no_accounts</span>
               <p class="font-bold">Không tìm thấy tài xế nào trong hệ thống.</p>
            </div>

            <!-- Drivers & Timelines Content -->
            <div v-for="driver in filteredDrivers" :key="driver.id" class="flex border-b border-slate-100 hover:bg-slate-50 transition-colors group w-fit min-w-full">
               <!-- Driver Info Card (Sticky left) -->
               <div class="w-64 shrink-0 p-3.5 border-r border-slate-200 bg-white shadow-[2px_0_5px_rgba(0,0,0,0.02)] z-30 sticky left-0 flex items-center gap-3 relative">
                  <div class="relative">
                     <div class="w-10 h-10 rounded-full bg-emerald-100 flex items-center justify-center text-emerald-700 font-black border border-emerald-200 uppercase">
                        {{ driver.fullName.charAt(0) }}
                     </div>
                     <div class="absolute -bottom-0.5 -right-0.5 w-3.5 h-3.5 rounded-full border-2 border-white" :class="getDriverStatusColor(driver)"></div>
                  </div>
                  <div class="flex flex-col overflow-hidden">
                     <span class="text-sm font-black text-slate-800 truncate" :title="driver.fullName">{{ driver.fullName }}</span>
                     <span class="text-[10px] font-bold text-slate-400 truncate">{{ driver.phone }}</span>
                  </div>
               </div>

               <!-- Driver Timeline -->
               <div class="w-[1440px] md:w-[2400px] shrink-0 relative min-h-[64px] bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxkZWZzPjxwYXR0ZXJuIGlkPSJncmlkIiB3aWR0aD0iNC4xNjY2JSIgaGVpZ2h0PSIxMDAlIiBwYXR0ZXJuVW5pdHM9InVzZXJTcGFjZU9uVXNlIj48bGluZSB4MT0iMTAwJSIgeTE9IjAiIHgyPSIxMDAlIiB5Mj0iMTAwJSIgc3Ryb2tlPSIjZTFlNWU5IiBzdHJva2Utd2lkdGg9IjEiIG9wYWNpdHk9IjAuNSIvPjwvcGF0dGVybj48L2RlZnM+PHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0idXJsKCNncmlkKSIvPjwvc3ZnPg==')]">
                  <!-- Current Time Indicator Continuation -->
                  <div v-if="isToday" class="absolute top-0 bottom-0 w-px bg-red-500/50 z-0 pointer-events-none" :style="{ left: currentTimePercentage + '%' }"></div>
                  
                  <!-- Trips Pills -->
                  <div 
                    v-for="trip in getDriverTrips(driver.phone)" 
                    :key="trip.id"
                    class="absolute top-2 bottom-2 rounded-xl shadow-sm border overflow-hidden cursor-pointer group/pill transition-all hover:scale-y-105 hover:z-20 hover:shadow-md"
                    :class="getTripColorClasses(trip)"
                    :style="getTripStyle(trip)"
                    @click="openTripDetails(trip, driver)"
                  >
                     <div class="absolute top-0 left-0 right-0 h-1 bg-white/30"></div>
                     <div class="px-2.5 py-1 flex flex-col h-full justify-center">
                        <div class="flex items-center justify-between gap-1">
                           <span class="text-[9px] font-black uppercase tracking-widest text-white/90 drop-shadow-sm truncate">{{ trip.assignedLicensePlate || 'Chưa xếp xe' }}</span>
                           <span class="text-[9px] font-bold text-white/80 bg-black/20 px-1 rounded">{{ trip.departureTime }}</span>
                        </div>
                        <span class="text-[10px] font-semibold text-white drop-shadow-sm truncate">{{ trip.departurePoint.split(',')[0] }} ➔ {{ trip.arrivalPoint.split(',')[0] }}</span>
                     </div>
                  </div>
               </div>
            </div>
         </div>
      </div>
    </div>

    <!-- Quick Info Modal -->
    <div v-if="selectedTrip" class="fixed inset-0 z-[100] bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4" @click.self="selectedTrip = null">
       <div class="bg-white rounded-3xl w-full max-w-sm shadow-2xl overflow-hidden border border-slate-200 animate-scale-up">
          <div class="p-4 flex items-start justify-between border-b border-slate-100" :class="getTripBgColor(selectedTrip.status)">
             <div class="flex flex-col text-white">
                <span class="text-[10px] font-black uppercase tracking-widest bg-white/20 px-2 py-0.5 rounded w-fit mb-2">{{ selectedTrip.status }}</span>
                <span class="text-xl font-black">{{ selectedTrip.departureTime }} ➔ {{ selectedTrip.arrivalTime }}</span>
             </div>
             <button @click="selectedTrip = null" class="w-8 h-8 rounded-full bg-black/10 text-white flex items-center justify-center hover:bg-black/20">
                <span class="material-symbols-outlined text-sm">close</span>
             </button>
          </div>
          <div class="p-5 space-y-4">
             <div class="flex flex-col">
                <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Tuyến đường</span>
                <span class="text-sm font-bold text-slate-800">{{ selectedTrip.departurePoint }}</span>
                <span class="text-xs text-slate-400 my-1 ml-1 pl-2 border-l-2 border-slate-200">đến</span>
                <span class="text-sm font-bold text-slate-800">{{ selectedTrip.arrivalPoint }}</span>
             </div>
             
             <div class="grid grid-cols-2 gap-3">
                <div class="bg-slate-50 p-3 rounded-xl border border-slate-100">
                   <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest block mb-1">Xe phân công</span>
                   <span class="text-sm font-black text-slate-800">{{ selectedTrip.assignedLicensePlate || 'Trống' }}</span>
                </div>
                <div class="bg-slate-50 p-3 rounded-xl border border-slate-100">
                   <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest block mb-1">Tài xế</span>
                   <span class="text-sm font-black text-slate-800">{{ selectedDriver?.fullName || 'N/A' }}</span>
                </div>
             </div>
          </div>
          <div class="p-4 bg-slate-50 border-t border-slate-100 flex gap-2">
             <button @click="$router.push('/admin/trip-manager')" class="flex-1 bg-white border border-slate-200 text-slate-700 py-2.5 rounded-xl text-xs font-bold hover:bg-slate-100 transition-colors">Sửa chuyến</button>
          </div>
       </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import axios from 'axios';
import { useApi } from '../../composables/useApi';
import { useRouter } from 'vue-router';

const api = useApi();
const router = useRouter();

const drivers = ref([]);
const driverStatuses = ref([]);
const trips = ref([]);
const loading = ref(true);
const selectedTrip = ref(null);
const selectedDriver = ref(null);
const filterDriverName = ref('');
const filterStatus = ref('');

const getTodayStr = () => {
  const d = new Date();
  return [d.getFullYear(), String(d.getMonth() + 1).padStart(2, '0'), String(d.getDate()).padStart(2, '0')].join('-');
};

const selectedDate = ref(getTodayStr());

const isToday = computed(() => {
  return selectedDate.value === getTodayStr();
});

const filteredDrivers = computed(() => {
  let list = drivers.value;
  if (filterDriverName.value) {
    const q = filterDriverName.value.toLowerCase();
    list = list.filter(d => d.fullName.toLowerCase().includes(q) || d.phone.includes(q));
  }
  if (filterStatus.value) {
    const statusMap = {};
    driverStatuses.value.forEach(ds => { statusMap[ds.phone] = ds.status; });
    list = list.filter(d => statusMap[d.phone] === filterStatus.value);
  }
  return list;
});

const currentTimePercentage = ref(0);
let timeInterval = null;

const updateCurrentTime = () => {
   if (!isToday.value) return;
   const now = new Date();
   const totalMinutes = now.getHours() * 60 + now.getMinutes();
   currentTimePercentage.value = (totalMinutes / 1440) * 100;
};

const fetchData = async () => {
  loading.value = true;
  try {
    const [driversRes, tripsRes, statusRes] = await Promise.all([
      api.get('/users/role/DRIVER'),
      api.get('/trips'),
      api.get(`/users/drivers/status?date=${selectedDate.value}`)
    ]);
    
    drivers.value = driversRes.data;
    driverStatuses.value = statusRes.data;
    trips.value = tripsRes.data.filter(t => t.departureDate === selectedDate.value);
    
  } catch (error) {
    console.error("Lỗi tải dữ liệu lịch:", error);
  } finally {
    loading.value = false;
  }
};

const fetchTrips = async () => {
   loading.value = true;
   try {
     const [tripsRes, statusRes] = await Promise.all([
       api.get('/trips'),
       api.get(`/users/drivers/status?date=${selectedDate.value}`)
     ]);
     trips.value = tripsRes.data.filter(t => t.departureDate === selectedDate.value);
     driverStatuses.value = statusRes.data;
   } catch(e) {
      console.error(e);
   } finally {
      loading.value = false;
   }
};

const getDriverTrips = (phone) => {
   return trips.value.filter(t => t.assignedDriverUsername === phone);
};

const parseTimeToMinutes = (timeStr) => {
   if (!timeStr) return 0;
   const [h, m] = timeStr.split(':').map(Number);
   return (h || 0) * 60 + (m || 0);
};

const parseDurationToMinutes = (durStr) => {
   if (!durStr) return 120; // Default 2 hours if unknown
   const match = durStr.match(/(\d+)h(?:\s*(\d+)m)?/);
   if (match) {
      const h = parseInt(match[1]) || 0;
      const m = parseInt(match[2]) || 0;
      return h * 60 + m;
   }
   return 120;
};

const getTripStyle = (trip) => {
   const startMinutes = parseTimeToMinutes(trip.departureTime);
   let durationMinutes = 0;
   
   if (trip.arrivalTime) {
      const endMinutes = parseTimeToMinutes(trip.arrivalTime);
      durationMinutes = endMinutes - startMinutes;
      if (durationMinutes < 0) durationMinutes += 24 * 60; // Cross midnight
   } else {
      durationMinutes = parseDurationToMinutes(trip.duration);
   }

   const leftPct = (startMinutes / 1440) * 100;
   let widthPct = (durationMinutes / 1440) * 100;
   
   // Đảm bảo không vượt quá màn hình bên phải
   if (leftPct + widthPct > 100) widthPct = 100 - leftPct;

   return {
      left: `${leftPct}%`,
      width: `${Math.max(widthPct, 2)}%` // Tối thiểu 2% độ rộng để nhìn thấy
   };
};

const getTripColorClasses = (trip) => {
   switch (trip.status) {
      case 'COMPLETED': return 'bg-emerald-500 border-emerald-600';
      case 'IN_PROGRESS': return 'bg-blue-500 border-blue-600';
      case 'CANCELLED': return 'bg-slate-400 border-slate-500';
      case 'SCHEDULED': 
      default: return 'bg-amber-500 border-amber-600';
   }
};

const getTripBgColor = (status) => {
   switch (status) {
      case 'COMPLETED': return 'bg-emerald-500';
      case 'IN_PROGRESS': return 'bg-blue-500';
      case 'CANCELLED': return 'bg-slate-500';
      case 'SCHEDULED': 
      default: return 'bg-amber-500';
   }
};

const getDriverStatusColor = (driver) => {
   // Ưu tiên hiển thị trạng thái từ API
   const statusInfo = driverStatuses.value.find(ds => ds.phone === driver.phone);
   if (statusInfo) {
     switch(statusInfo.status) {
       case 'ON_LEAVE': return 'bg-orange-400 border-white';
       case 'SUSPENDED': return 'bg-slate-500 border-white';
       case 'DRIVING': return 'bg-blue-500 border-white';
     }
   }

   const driverTrips = getDriverTrips(driver.phone);
   if (driverTrips.length === 0) return 'bg-slate-300 border-white';
   
   const hasInProgress = driverTrips.some(t => t.status === 'IN_PROGRESS');
   if (hasInProgress) return 'bg-blue-500 border-white';
   
   // Kiểm tra đụng lịch (Overlapping trips)
   const activeTrips = driverTrips.filter(t => t.status !== 'CANCELLED' && t.status !== 'COMPLETED');
   for (let i = 0; i < activeTrips.length; i++) {
     for (let j = i + 1; j < activeTrips.length; j++) {
       const a = activeTrips[i], b = activeTrips[j];
       if (a.departureTime < b.arrivalTime && a.arrivalTime > b.departureTime) {
         return 'bg-rose-500 border-white animate-pulse';
       }
     }
   }
   
   return 'bg-amber-400 border-white';
};

const openTripDetails = (trip, driver) => {
   selectedTrip.value = trip;
   selectedDriver.value = driver;
};

onMounted(() => {
   fetchData();
   updateCurrentTime();
   timeInterval = setInterval(updateCurrentTime, 60000); // Cập nhật vạch đỏ mỗi phút
});

onUnmounted(() => {
   if (timeInterval) clearInterval(timeInterval);
});
</script>

<style scoped>
@keyframes scaleUp {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}
.animate-scale-up {
  animation: scaleUp 0.2s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>
