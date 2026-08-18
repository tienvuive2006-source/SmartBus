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
             class="w-full pl-10 pr-3 py-2.5 bg-white border border-slate-200 rounded-xl text-body-md font-bold focus:outline-none focus:ring-2 focus:ring-amber-500/20 focus:border-amber-500 transition-all shadow-sm text-slate-700" 
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

    <!-- Phân nhóm lịch trình -->
    <nav class="grid grid-cols-2 gap-3 rounded-2xl bg-slate-200/70 p-1.5" aria-label="Nhóm chuyến xe">
      <button
        type="button"
        class="flex items-center justify-between gap-3 rounded-xl px-4 py-3 text-left transition-all active:scale-[0.99]"
        :class="activeTripTab === 'upcoming' ? 'bg-white text-amber-700 shadow-sm' : 'text-slate-500 hover:bg-white/50'"
        @click="activeTripTab = 'upcoming'"
      >
        <span class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[20px]">departure_board</span>
          <span><strong class="block text-sm">Sắp chạy</strong><small class="text-[10px] font-semibold opacity-70">Gồm chuyến đang chạy</small></span>
        </span>
        <b class="flex h-7 min-w-7 items-center justify-center rounded-lg px-2 text-xs" :class="activeTripTab === 'upcoming' ? 'bg-amber-100' : 'bg-slate-100'">{{ upcomingTrips.length }}</b>
      </button>
      <button
        type="button"
        class="flex items-center justify-between gap-3 rounded-xl px-4 py-3 text-left transition-all active:scale-[0.99]"
        :class="activeTripTab === 'completed' ? 'bg-white text-emerald-700 shadow-sm' : 'text-slate-500 hover:bg-white/50'"
        @click="activeTripTab = 'completed'"
      >
        <span class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[20px]">task_alt</span>
          <span><strong class="block text-sm">Đã hoàn thành</strong><small class="text-[10px] font-semibold opacity-70">Lịch sử chuyến xe</small></span>
        </span>
        <b class="flex h-7 min-w-7 items-center justify-center rounded-lg px-2 text-xs" :class="activeTripTab === 'completed' ? 'bg-emerald-100' : 'bg-slate-100'">{{ completedTrips.length }}</b>
      </button>
    </nav>

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
      <h3 class="text-title-md font-bold text-slate-700">{{ activeTripTab === 'upcoming' ? 'Không có chuyến sắp chạy' : 'Chưa có chuyến hoàn thành' }}</h3>
      <p class="text-body-sm text-slate-500 mt-2">{{ activeTripTab === 'upcoming' ? 'Không có chuyến đang chờ hoặc đang chạy trong ngày này.' : 'Không có chuyến đã hoàn thành trong ngày này.' }}</p>
      <button v-if="filterDate" @click="filterDate = ''" class="mt-4 text-sm font-bold text-amber-600 bg-amber-50 px-4 py-2 rounded-xl active:scale-95 transition-transform">
        Xem tất cả ngày
      </button>
    </div>


    <!-- Trip List -->
    <div v-else class="space-y-4">
      <div 
        v-for="trip in filteredTrips" 
        :key="trip.id" 
        class="bg-white rounded-3xl overflow-hidden border border-slate-100 shadow-sm hover:shadow-md transition-shadow cursor-pointer relative"
        @click="$router.push(`/driver/trip/${trip.id}`)"
      >
        <!-- Status Indicator Strip -->
        <div class="absolute left-0 top-0 bottom-0 w-1.5" :class="getStatusColor(trip.status)"></div>
        
        <div class="p-5 pl-6">
          <div class="flex justify-between items-start mb-3">
            <div class="flex flex-col">
              <span v-if="isSecondaryDriver(trip)" class="mb-2 w-fit rounded-lg bg-sky-100 px-2.5 py-1 text-[10px] font-black uppercase tracking-wider text-sky-700">Tài xế phụ</span>
              <span v-else-if="trip.assignedDriverUsername === authStore.currentUser?.phone" class="mb-2 w-fit rounded-lg bg-amber-100 px-2.5 py-1 text-[10px] font-black uppercase tracking-wider text-amber-700">Tài xế chính</span>
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
            <div class="w-3 h-3 rounded-full border-2 border-amber-500 bg-white z-10"></div>
            <div class="flex-1 h-px bg-slate-200 border-dashed border-t border-slate-300 absolute left-3 right-3 top-1/2 -translate-y-1/2"></div>
            <div class="w-3 h-3 rounded-full border-2 border-rose-500 bg-white z-10 ml-auto"></div>
          </div>
          <div class="flex justify-between items-center mt-2 text-body-sm font-bold">
            <span class="truncate max-w-[45%]">{{ trip.departurePoint }}</span>
            <span class="truncate max-w-[45%] text-right">{{ trip.arrivalPoint }}</span>
          </div>

          <!-- Thông tin xe & Lơ xe -->
          <div class="mt-5 flex gap-4 items-center bg-slate-50 p-3 rounded-xl border border-slate-100 group-hover:bg-white transition-colors">
            <div class="w-20 h-14 rounded-lg overflow-hidden shrink-0 bg-slate-200 border border-slate-200 shadow-sm relative">
              <img v-if="trip.busInfo && trip.busInfo.imageUrl" :src="trip.busInfo.imageUrl" class="w-full h-full object-cover" />
              <div v-else class="w-full h-full flex items-center justify-center text-slate-400">
                 <span class="material-symbols-outlined text-[24px]">directions_bus</span>
              </div>
            </div>
            <div class="flex flex-col flex-1 justify-center">
              <div class="flex items-center justify-between mb-1">
                 <span class="text-xs font-black text-slate-800">{{ (trip.busInfo && trip.busInfo.busType) ? trip.busInfo.busType : 'Limousine VIP' }}</span>
                 <span class="text-[9px] font-black text-amber-700 bg-amber-50 px-2 py-0.5 rounded-md border border-amber-100 uppercase tracking-widest shadow-sm">Lơ xe</span>
              </div>
              <div class="text-xs font-medium text-slate-500 flex items-center justify-between">
                 <span>Họ & Tên:</span>
                 <span class="font-bold text-slate-800">{{ trip.inspector ? trip.inspector.fullName : 'Chưa có' }}</span>
              </div>
            </div>
          </div>

          <!-- Divider -->
          <hr class="border-slate-100 my-4" />

          <div class="flex items-center justify-between text-body-sm">
            <div class="flex items-center gap-2 text-slate-600 font-medium">
              <span class="material-symbols-outlined text-[18px]">airline_seat_recline_normal</span>
              <span>Ghế trống: <strong class="text-amber-600">{{ trip.availableSeats }}</strong>/{{ trip.totalSeats || 24 }}</span>
            </div>
            <div class="flex items-center text-amber-600 font-bold gap-1 group-hover:translate-x-1 transition-transform">
              Xem chi tiết <span class="material-symbols-outlined text-[18px]">arrow_forward</span>
            </div>
          </div>
          
          <!-- Nút Xác Nhận / Từ Chối -->
          <div v-if="!isSecondaryDriver(trip) && !trip.driverAccepted && (trip.status === 'SCHEDULED' || trip.status === 'PENDING')" class="mt-4 pt-4 border-t border-slate-100 flex gap-3">
             <button @click.stop="acceptTrip(trip.id)" class="flex-1 bg-emerald-500 hover:bg-emerald-600 text-white py-2.5 rounded-xl font-bold text-xs uppercase tracking-widest shadow-sm active:scale-95 transition-all">
                Nhận chuyến
             </button>
             <button @click.stop="rejectTrip(trip.id)" class="flex-1 bg-slate-100 hover:bg-rose-50 text-slate-600 hover:text-rose-600 py-2.5 rounded-xl font-bold text-xs uppercase tracking-widest active:scale-95 transition-all">
                Từ chối
             </button>
          </div>
          
          <!-- Nút Bắt Đầu / Hoàn Thành -->
          <div v-else-if="!isSecondaryDriver(trip) && trip.driverAccepted && (trip.status === 'SCHEDULED' || trip.status === 'PENDING')" class="mt-4 pt-4 border-t border-slate-100">
             <button @click.stop="updateTripStatus(trip.id, 'IN_PROGRESS')" class="w-full bg-blue-500 hover:bg-blue-600 text-white py-3 rounded-xl font-black text-sm uppercase tracking-widest shadow-md active:scale-95 transition-all flex items-center justify-center gap-2">
                <span class="material-symbols-outlined">play_circle</span>
                Bắt Đầu Hành Trình
             </button>
          </div>
          
          <div v-else-if="!isSecondaryDriver(trip) && trip.status === 'IN_PROGRESS'" class="mt-4 pt-4 border-t border-slate-100">
             <button @click.stop="updateTripStatus(trip.id, 'COMPLETED')" class="w-full bg-emerald-500 hover:bg-emerald-600 text-white py-3 rounded-xl font-black text-sm uppercase tracking-widest shadow-md active:scale-95 transition-all flex items-center justify-center gap-2">
                <span class="material-symbols-outlined">task_alt</span>
                Hoàn Thành Chuyến
             </button>
          </div>
          
        </div>
      </div>
    </div>
    
    <!-- Spacer for bottom navigation -->
    <div class="h-24 sm:h-32 w-full"></div>
    <!-- Floating Action Button for Leave Request -->
    <button @click="showLeaveModal = true" class="fixed bottom-24 right-6 w-14 h-14 bg-amber-600 text-white rounded-full shadow-lg flex items-center justify-center hover:bg-amber-700 active:scale-90 transition-transform z-40">
      <span class="material-symbols-outlined text-2xl">event_busy</span>
    </button>

    <!-- Modal Xin Nghỉ Phép -->
    <Teleport to="body">
      <div v-if="showLeaveModal" class="fixed inset-0 z-[100] flex items-end sm:items-center justify-center p-0 sm:p-4">
        <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showLeaveModal = false"></div>
        <div class="relative w-full sm:max-w-md bg-white sm:rounded-[2rem] rounded-t-[2rem] shadow-2xl overflow-hidden flex flex-col animate-slide-up sm:animate-fade-in-up">
          <div class="px-6 py-5 border-b border-slate-100 flex justify-between items-center bg-white">
            <h3 class="text-title-md font-black text-slate-800 flex items-center gap-2">
              <span class="material-symbols-outlined text-amber-600">event_busy</span>
              Đơn xin nghỉ phép
            </h3>
            <button @click="showLeaveModal = false" class="w-10 h-10 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors">
              <span class="material-symbols-outlined">close</span>
            </button>
          </div>
          
          <div class="p-6 bg-slate-50 space-y-4">
            <div class="grid grid-cols-2 gap-4">
               <div>
                  <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Từ ngày</label>
                  <input type="date" v-model="leaveForm.startDate" class="w-full bg-white border border-slate-200 px-3 py-2.5 rounded-xl font-bold text-slate-700 outline-none focus:border-amber-500 focus:ring-2 focus:ring-amber-500/20" />
               </div>
               <div>
                  <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Đến ngày</label>
                  <input type="date" v-model="leaveForm.endDate" class="w-full bg-white border border-slate-200 px-3 py-2.5 rounded-xl font-bold text-slate-700 outline-none focus:border-amber-500 focus:ring-2 focus:ring-amber-500/20" />
               </div>
            </div>
            <div>
               <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Lý do</label>
               <textarea v-model="leaveForm.reason" rows="3" class="w-full bg-white border border-slate-200 px-3 py-2.5 rounded-xl text-sm outline-none focus:border-amber-500 focus:ring-2 focus:ring-amber-500/20" placeholder="Lý do xin nghỉ..."></textarea>
            </div>
            
            <!-- List of my requests -->
            <div class="mt-6 pt-4 border-t border-slate-200">
               <h4 class="text-xs font-black text-slate-800 uppercase tracking-widest mb-3">Lịch sử xin nghỉ</h4>
               <div v-if="myLeaveRequests.length === 0" class="text-xs font-bold text-slate-400">Chưa có đơn nào.</div>
               <div class="space-y-2 max-h-32 overflow-y-auto pr-2">
                  <div v-for="req in myLeaveRequests" :key="req.id" class="bg-white p-3 rounded-xl border border-slate-100 flex justify-between items-center">
                     <div>
                        <div class="text-[10px] font-bold text-slate-700">{{ req.startDate }} - {{ req.endDate }}</div>
                        <div class="text-[9px] text-slate-500 truncate max-w-[150px]" :title="req.reason">{{ req.reason }}</div>
                     </div>
                     <span class="text-[9px] font-black tracking-widest uppercase px-2 py-0.5 rounded border" :class="req.status === 'PENDING' ? 'text-amber-600 bg-amber-50 border-amber-200' : (req.status === 'APPROVED' ? 'text-emerald-600 bg-emerald-50 border-emerald-200' : 'text-rose-600 bg-rose-50 border-rose-200')">
                        {{ req.status }}
                     </span>
                  </div>
               </div>
            </div>
          </div>
          
          <div class="p-6 bg-white border-t border-slate-100">
            <button @click="submitLeaveRequest" :disabled="!leaveForm.startDate || !leaveForm.endDate || !leaveForm.reason" class="w-full bg-amber-600 hover:bg-amber-700 text-white font-black py-3.5 rounded-xl active:scale-95 transition-all disabled:opacity-50 disabled:active:scale-100">
              GỬI ĐƠN
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useApi } from '@/composables/useApi';

const api = useApi();
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

const filterDate = ref(getTodayStr());
const activeTripTab = ref('upcoming');

const showLeaveModal = ref(false);
const leaveForm = ref({ startDate: '', endDate: '', reason: '' });
const myLeaveRequests = ref([]);

const dateFilteredTrips = computed(() => {
  if (!filterDate.value) return trips.value;
  return trips.value.filter(t => t.departureDate === filterDate.value);
});

const upcomingTrips = computed(() => dateFilteredTrips.value.filter(trip => !['COMPLETED', 'CANCELLED'].includes(trip.status)));
const completedTrips = computed(() => dateFilteredTrips.value.filter(trip => ['COMPLETED', 'CANCELLED'].includes(trip.status)));
const filteredTrips = computed(() => activeTripTab.value === 'upcoming' ? upcomingTrips.value : completedTrips.value);
const isSecondaryDriver = (trip) => trip?.secondaryDriverUsername === authStore.currentUser?.phone;

const getStatusColor = (status) => {
  switch(status) {
    case 'SCHEDULED': return 'bg-amber-500';
    case 'IN_PROGRESS': return 'bg-blue-500';
    case 'COMPLETED': return 'bg-emerald-500';
    case 'CANCELLED': return 'bg-rose-500';
    default: return 'bg-slate-300';
  }
};

const getStatusTextColor = (status) => {
  switch(status) {
    case 'SCHEDULED': return 'text-amber-600';
    case 'IN_PROGRESS': return 'text-blue-600';
    case 'COMPLETED': return 'text-emerald-600';
    case 'CANCELLED': return 'text-rose-600';
    default: return 'text-slate-500';
  }
};

const getStatusText = (status) => {
  switch(status) {
    case 'SCHEDULED': return 'Sắp chạy';
    case 'IN_PROGRESS': return 'Đang chạy';
    case 'COMPLETED': return 'Hoàn thành';
    case 'CANCELLED': return 'Đã hủy';
    default: return 'Sắp chạy';
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const [y, m, d] = dateStr.split('-');
  return `${d}/${m}/${y}`;
};

const fetchMyLeaveRequests = async () => {
   try {
      const res = await api.get(`/leave-requests/my/${authStore.currentUser?.phone}`);
      myLeaveRequests.value = res.data;
   } catch(e) {
      console.error(e);
   }
};

const submitLeaveRequest = async () => {
   try {
      const payload = {
         driverUsername: authStore.currentUser?.phone,
         driverFullName: authStore.currentUser?.fullName,
         startDate: leaveForm.value.startDate,
         endDate: leaveForm.value.endDate,
         reason: leaveForm.value.reason
      };
      await api.post(`/leave-requests`, payload);
      alert('Gửi đơn xin nghỉ phép thành công!');
      leaveForm.value = { startDate: '', endDate: '', reason: '' };
      fetchMyLeaveRequests();
   } catch(e) {
      alert('Lỗi gửi đơn xin nghỉ!');
   }
};

const fetchTripsAndBuses = async () => {
  loading.value = true;
  error.value = '';
  try {
    const userPhone = authStore.currentUser?.phone;
    if (!userPhone) {
      throw new Error("Không xác định được danh tính tài xế.");
    }
    
    // Gọi song song API lấy danh sách toàn bộ chuyến xe và danh sách xe
    const [tripRes, busRes] = await Promise.all([
       api.get(`/trips`),
       api.get(`/buses`)
    ]);
    
    buses.value = busRes.data;

    // Lọc các chuyến xe được gán thẳng cho Username của tài xế HOẶC SĐT của Lơ xe
    const myTrips = tripRes.data
      .filter(t => t.assignedDriverUsername === userPhone || t.secondaryDriverUsername === userPhone || (t.inspector && (t.inspector.phone === userPhone || t.inspector.employeeCode === userPhone)))
      .map(t => {
        const busInfo = buses.value.find(b => b.licensePlate === t.assignedLicensePlate);
        return { ...t, busInfo };
      });

    // Sort: Trạng thái SCHEDULED / IN_PROGRESS lên đầu, theo giờ xuất phát
    trips.value = myTrips.sort((a, b) => {
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

const acceptTrip = async (tripId) => {
   try {
      await axios.patch(`${import.meta.env.VITE_API_BASE_URL}/trips/${tripId}/accept`);
      alert("Đã nhận chuyến thành công!");
      fetchTripsAndBuses();
   } catch(e) {
      alert("Lỗi khi nhận chuyến!");
   }
};

const rejectTrip = async (tripId) => {
   if(confirm("Bạn có chắc chắn muốn từ chối chuyến xe này? Hệ thống sẽ báo cáo lên Admin.")) {
      try {
         await axios.patch(`${import.meta.env.VITE_API_BASE_URL}/trips/${tripId}/reject`);
         alert("Đã từ chối chuyến!");
         fetchTripsAndBuses();
      } catch(e) {
         alert("Lỗi khi từ chối chuyến!");
      }
   }
};

const updateTripStatus = async (tripId, status) => {
   const actionMap = {
     'IN_PROGRESS': 'bắt đầu hành trình',
     'COMPLETED': 'kết thúc hành trình'
   };
   
   if(confirm(`Xác nhận ${actionMap[status]}?`)) {
      try {
         await axios.patch(`${import.meta.env.VITE_API_BASE_URL}/trips/${tripId}/status`, { status });
         alert(`Đã ${actionMap[status]} thành công!`);
         fetchTripsAndBuses();
      } catch(e) {
         console.error(e);
         alert("Lỗi khi cập nhật trạng thái!");
      }
   }
};

onMounted(() => {
  fetchTripsAndBuses();
  fetchMyLeaveRequests();
});




</script>
