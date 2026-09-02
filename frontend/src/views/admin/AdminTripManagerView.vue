<template>
  <div class="p-6 md:p-8 max-w-7xl mx-auto bg-slate-50 min-h-screen font-sans">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
      <div>
        <h2 class="text-2xl font-extrabold text-slate-800 tracking-tight">
          Quản lý Chuyến xe
        </h2>
        <p class="text-xs font-semibold text-slate-500 mt-1">Hệ thống điều hành lộ trình toàn quốc</p>
      </div>
      <button 
        @click="openAddModal" 
        class="bg-[#075955] hover:bg-[#064a47] text-white px-5 py-2.5 rounded-xl shadow-sm hover:shadow-md active:scale-95 transition-all duration-200 font-bold text-xs flex items-center justify-center gap-2"
      >
        <span class="material-symbols-outlined text-[18px]">add_circle</span>
        Tạo lộ trình mới
      </button>
    </div>

    <!-- 1. Stats Dashboard -->
    <TripStats 
      :totalTrips="totalTrips"
      :todayTrips="todayTrips"
      :totalEmptySeats="totalEmptySeats"
      :averagePrice="averagePrice"
      class="mb-6"
    />

    <!-- Filters Section -->
    <div class="flex flex-col sm:flex-row flex-wrap gap-4 mb-6">
      <div class="relative flex-1 min-w-[200px] sm:max-w-[260px]">
        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none text-[18px]">search</span>
        <input 
          v-model="filterSearch" 
          type="text" 
          placeholder="Tìm tài xế, biển số xe..." 
          class="w-full bg-white border border-slate-200 pl-10 pr-4 py-3 rounded-xl text-sm font-bold text-slate-700 outline-none focus:border-[#075955] focus:ring-2 focus:ring-[#075955]/20 shadow-sm transition-all" 
        />
      </div>

      <div class="relative flex-1 min-w-[160px] sm:max-w-[200px]">
        <select v-model="filterStatus" class="w-full bg-white border border-slate-200 px-4 py-3 rounded-xl text-sm font-bold text-slate-700 outline-none focus:border-[#075955] focus:ring-2 focus:ring-[#075955]/20 shadow-sm appearance-none cursor-pointer transition-all">
          <option value="">Tất cả trạng thái</option>
          <option value="upcoming">Đang mở bán</option>
          <option value="passed">Đã khởi hành</option>
        </select>
        <span class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none">expand_more</span>
      </div>

      <div class="relative flex-1 min-w-[200px]">
        <select v-model="filterRoute" class="w-full bg-white border border-slate-200 px-4 py-3 rounded-xl text-sm font-bold text-slate-700 outline-none focus:border-[#075955] focus:ring-2 focus:ring-[#075955]/20 shadow-sm appearance-none cursor-pointer transition-all">
          <option value="">Tất cả tuyến đường</option>
          <option v-for="route in uniqueRoutesForFilter" :key="route" :value="route">{{ route }}</option>
        </select>
        <span class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none">expand_more</span>
      </div>
      
      <div class="relative flex-1 min-w-[180px] sm:max-w-[280px]">
        <select v-model="filterBusType" class="w-full bg-white border border-slate-200 px-4 py-3 rounded-xl text-sm font-bold text-slate-700 outline-none focus:border-[#075955] focus:ring-2 focus:ring-[#075955]/20 shadow-sm appearance-none cursor-pointer transition-all">
          <option value="">Tất cả dòng xe</option>
          <option v-for="b in busTypes" :key="b.id" :value="b.name">{{ b.name }}</option>
        </select>
        <span class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none">expand_more</span>
      </div>
      
      <div class="relative flex-1 min-w-[160px] sm:max-w-[180px] flex items-center gap-2">
        <input 
          type="date" 
          v-model="filterDate" 
          title="Chọn ngày khởi hành"
          class="w-full bg-white border border-slate-200 px-4 py-3 rounded-xl text-sm font-bold text-slate-700 outline-none focus:border-[#075955] focus:ring-2 focus:ring-[#075955]/20 shadow-sm transition-all" 
        />
        <button 
          v-if="filterDate" 
          @click="filterDate = ''" 
          class="text-slate-400 hover:text-red-500 transition-colors w-8 h-8 flex items-center justify-center rounded-full hover:bg-red-50"
          title="Bỏ lọc ngày"
        >
          <span class="material-symbols-outlined text-[20px]">close</span>
        </button>
      </div>
    </div>

    <!-- 2. Main List -->
    <TripTable 
      :trips="paginatedTrips"
      :selectable-trips="filteredTrips"
      :total-count="filteredTrips.length"
      :page="currentPage"
      :total-pages="totalPages"
      :page-size="pageSize"
      :bulk-deleting="bulkDeleting"
      @update:page="currentPage = $event"
      @edit="openEditModal"
      @report="openReportModal"
      @delete="deleteTrip"
      @bulk-delete="bulkDeleteTrips"
    />

    <!-- 3. Edit Modal -->
    <TripModal 
      ref="tripModal"
      :busTypes="busTypes"
      :buses="buses"
      :inspectors="inspectors"
      :drivers="drivers"
      @saved="fetchTrips"
    />

    <!-- 4. Report Modal -->
    <TripReportModal
      :show="showReportModal"
      :trip="selectedReportTrip"
      @close="showReportModal = false"
    />

  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useApi } from '@/composables/useApi';
import TripStats from '@/components/admin/trip/TripStats.vue';
import TripTable from '@/components/admin/trip/TripTable.vue';
import TripModal from '@/components/admin/trip/TripModal.vue';
import TripReportModal from '@/components/admin/trip/TripReportModal.vue';
import { toBusinessDateString } from '@/utils/businessDate';

const api = useApi();
const trips = ref([]);
const busTypes = ref([]);
const buses = ref([]);
const inspectors = ref([]);
const drivers = ref([]);

const tripModal = ref(null);
const bulkDeleting = ref(false);

// --- FILTERING LOGIC ---
const filterRoute = ref('');
const filterBusType = ref('');
const filterDate = ref('');
const filterSearch = ref('');
const filterStatus = ref('');
const currentPage = ref(0);
const pageSize = 10;

const isTripPassed = (trip) => {
  if (!trip || !trip.departureDate || !trip.departureTime) return false;
  try {
    const dateParts = trip.departureDate.split('T')[0].split('-');
    if (dateParts.length !== 3) return false;
    const [year, month, day] = dateParts;
    const timeParts = trip.departureTime.split(':');
    if (timeParts.length < 2) return false;
    const [hour, minute] = timeParts;
    const depTime = new Date(year, month - 1, day, hour, minute);
    return new Date() > depTime;
  } catch (e) {
    console.error("Lỗi parse ngày tháng trip:", trip.id, e);
    return false;
  }
};

const simplifyRouteLocation = (location) => {
  if (!location) return '';
  const parts = location.split(',').map(part => part.trim()).filter(Boolean);
  if (parts.length > 1) return parts[parts.length - 1].replace(/^(Thành phố|TP\.?|Tỉnh)\s+/i, '').trim();

  return location
    .replace(/^Bến xe\s+/i, '')
    .replace(/^(Trung tâm|Liên tỉnh|Phía Nam)\s+/i, '')
    .replace(/^(Thành phố|TP\.?|Tỉnh)\s+/i, '')
    .trim();
};

const uniqueRoutesForFilter = computed(() => {
  const routes = new Set();
  trips.value.forEach(t => {
    if (!t.departurePoint || !t.arrivalPoint) return;
    const from = simplifyRouteLocation(t.departurePoint);
    const to = simplifyRouteLocation(t.arrivalPoint);
    routes.add(`${from} ➔ ${to}`);
  });
  return Array.from(routes).sort();
});

const filteredTrips = computed(() => {
  return trips.value.filter(t => {
    let pass = true;
    
    if (filterStatus.value) {
      const passed = isTripPassed(t);
      if (filterStatus.value === 'passed' && !passed) pass = false;
      if (filterStatus.value === 'upcoming' && passed) pass = false;
    }
    
    if (filterBusType.value && t.busType !== filterBusType.value) {
      pass = false;
    }
    if (filterRoute.value) {
      const from = simplifyRouteLocation(t.departurePoint);
      const to = simplifyRouteLocation(t.arrivalPoint);
      if (`${from} ➔ ${to}` !== filterRoute.value) {
        pass = false;
      }
    }
    if (filterDate.value) {
      if (!t.departureDate || !t.departureDate.startsWith(filterDate.value)) {
        pass = false;
      }
    }
    if (filterSearch.value) {
      const q = filterSearch.value.toLowerCase();
      const driverName = (t.assignedDriverFullName || '').toLowerCase();
      const driverPhone = (t.assignedDriverUsername || '').toLowerCase();
      const plate = (t.assignedLicensePlate || '').toLowerCase();
      if (!driverName.includes(q) && !driverPhone.includes(q) && !plate.includes(q)) {
        pass = false;
      }
    }
    return pass;
  });
});

const totalPages = computed(() => Math.ceil(filteredTrips.value.length / pageSize));
const paginatedTrips = computed(() => {
  const start = currentPage.value * pageSize;
  return filteredTrips.value.slice(start, start + pageSize);
});

watch([filterRoute, filterBusType, filterDate, filterSearch, filterStatus], () => {
  currentPage.value = 0;
});

watch(totalPages, (pages) => {
  if (pages === 0) currentPage.value = 0;
  else if (currentPage.value >= pages) currentPage.value = pages - 1;
});

// --- Stats Logic ---
const totalTrips = computed(() => trips.value.length);
const todayTrips = computed(() => {
  const today = toBusinessDateString();
  return trips.value.filter(t => String(t.departureDate || '').split('T')[0] === today).length;
});
const totalEmptySeats = computed(() => {
  return trips.value.reduce((acc, t) => acc + (t.availableSeats || 0), 0);
});
const averagePrice = computed(() => {
  if (trips.value.length === 0) return 0;
  return Math.round(trips.value.reduce((acc, t) => acc + t.price, 0) / trips.value.length);
});

// --- Data Fetching ---
const fetchTrips = async () => {
  try {
    const res = await api.get('/trips');
    trips.value = res.data.sort((a, b) => b.id - a.id);
  } catch (err) { console.error(err); }
};

const fetchBusTypes = async () => {
  try {
    const res = await api.get('/bus-types');
    busTypes.value = res.data;
  } catch (err) { console.error(err); }
};

const fetchBuses = async () => {
  try {
    const res = await api.get('/buses');
    buses.value = res.data;
  } catch (err) { console.error(err); }
};

const fetchInspectors = async () => {
  try {
    const res = await api.get('/inspector/all');
    inspectors.value = res.data;
  } catch (err) { console.error('Lỗi tải danh sách lơ xe:', err); }
};

const fetchDrivers = async () => {
  try {
    const res = await api.get('/users/role/DRIVER');
    drivers.value = res.data;
  } catch (err) { console.error('Lỗi tải danh sách tài xế:', err); }
};

const openAddModal = async () => { 
  await Promise.all([fetchBusTypes(), fetchBuses(), fetchInspectors(), fetchDrivers()]);
  tripModal.value?.openModal(null);
};

const openEditModal = async (trip) => { 
  await Promise.all([fetchBusTypes(), fetchBuses(), fetchInspectors(), fetchDrivers()]);
  tripModal.value?.openModal(trip);
};

const showReportModal = ref(false);
const selectedReportTrip = ref(null);

const openReportModal = (trip) => {
  selectedReportTrip.value = trip;
  showReportModal.value = true;
};

const deleteTrip = async (id) => {
  if (confirm('Xóa chuyến xe này?')) {
    try {
      await api.delete(`/trips/${id}`)
      await fetchTrips()
    } catch (err) {
      const message = err.response?.data?.error || err.response?.data?.message || 'Không thể xóa chuyến xe.'
      alert(message)
    }
  }
};

const bulkDeleteTrips = async (ids) => {
  if (!Array.isArray(ids) || ids.length === 0 || bulkDeleting.value) return;

  const accepted = confirm(
    `Bạn sắp xóa vĩnh viễn ${ids.length} chuyến xe chưa có khách đặt.\n\n` +
    'Thao tác này không thể hoàn tác. Bạn có chắc muốn tiếp tục?'
  );
  if (!accepted) return;

  bulkDeleting.value = true;
  try {
    // Xóa nhiều chuyến kéo theo sơ đồ ghế nên có thể lâu hơn timeout mặc định 15 giây.
    const response = await api.delete('/trips/bulk', {
      data: { ids },
      timeout: 120000
    });
    await fetchTrips();
    alert(response.data?.message || `Đã xóa ${ids.length} chuyến xe.`);
  } catch (err) {
    // Nếu client mất kết nối/timeout, backend vẫn có thể đã hoàn tất giao dịch.
    // Tải lại trước khi kết luận để tránh báo thất bại trong khi dữ liệu đã được xóa.
    await fetchTrips();
    const remainingIds = new Set(trips.value.map(trip => trip.id));
    const deletedCount = ids.filter(id => !remainingIds.has(id)).length;
    if (deletedCount === ids.length) {
      alert(`Đã xóa ${deletedCount} chuyến xe.`);
      return;
    }

    const message = err.response?.data?.error || err.response?.data?.message
      || `Đã xóa ${deletedCount}/${ids.length} chuyến. Vui lòng kiểm tra lại danh sách.`;
    alert(message);
  } finally {
    bulkDeleting.value = false;
  }
};

onMounted(() => { 
  fetchTrips(); 
  fetchBusTypes(); 
  fetchBuses(); 
  fetchInspectors();
  fetchDrivers();
});
</script>
