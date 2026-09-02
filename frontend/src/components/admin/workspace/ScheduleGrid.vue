<template>
  <div class="flex-1 flex flex-col h-full bg-slate-50 overflow-hidden relative">
    <!-- Toolbar -->
    <div class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-6 shrink-0 shadow-sm z-20 relative">
      <div class="flex items-center gap-4">
        <div class="flex flex-col">
          <h2 class="text-lg font-black text-slate-800">
            <span v-if="selectedDriver">Lịch của {{ selectedDriver.fullName }}</span>
            <span v-else>Lịch Trình Tổng Hợp</span>
          </h2>
          <div class="flex items-center gap-2 mt-0.5" v-if="!loadingSchedule">
            <span class="text-[9px] font-black uppercase tracking-wider text-slate-400">Toàn bộ</span>
            <span class="text-[10px] font-bold text-slate-500 bg-slate-100 px-2 py-0.5 rounded border border-slate-200 shadow-sm">Tổng: {{ tripStats.total }}</span>
            <span class="text-[10px] font-bold text-amber-600 bg-amber-50 px-2 py-0.5 rounded border border-amber-200 shadow-sm">Chưa phân công: {{ tripStats.unassigned }}</span>
            <span class="text-[10px] font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100 shadow-sm">Đã gán tài xế: {{ tripStats.assigned }}</span>
            <span class="text-[10px] font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded border border-blue-200 shadow-sm">Đang chạy: {{ tripStats.inProgress }}</span>
            <span class="text-[10px] font-bold text-slate-600 bg-slate-200 px-2 py-0.5 rounded border border-slate-300 shadow-sm">Đã hoàn thành: {{ tripStats.completed }}</span>
          </div>
        </div>
        <span v-if="loadingSchedule" class="text-xs font-bold text-primary animate-pulse ml-2">Đang đồng bộ...</span>
      </div>

      <div class="flex items-center gap-2">
        <button @click="$emit('change-week', -1)" class="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-600 transition-colors">
          <span class="material-symbols-outlined text-sm">chevron_left</span>
        </button>
        <div class="px-4 py-1.5 bg-slate-100 rounded-lg text-sm font-bold text-slate-700 cursor-pointer hover:bg-slate-200 transition-colors" @click="$emit('reset-week')">
          {{ weekLabel }}
        </div>
        <button @click="$emit('change-week', 1)" class="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-600 transition-colors">
          <span class="material-symbols-outlined text-sm">chevron_right</span>
        </button>
      </div>
    </div>

    <ScheduleFilters
      :search="filterSearch"
      :status="filterTripStatus"
      :route="filterRoute"
      :vehicle="filterVehicle"
      :time-period="filterTimePeriod"
      :route-options="routeOptions"
      :vehicle-options="vehicleOptions"
      :result-count="resultCount"
      :has-active-filters="hasActiveFilters"
      @update:search="$emit('update:filterSearch', $event)"
      @update:status="$emit('update:filterTripStatus', $event)"
      @update:route="$emit('update:filterRoute', $event)"
      @update:vehicle="$emit('update:filterVehicle', $event)"
      @update:time-period="$emit('update:filterTimePeriod', $event)"
      @reset="$emit('reset-filters')"
    />

    <!-- ⚠️ Banner cảnh báo khẩn: chưa phân công trong 24h -->
    <div
      v-if="urgentCount > 0"
      class="flex items-center gap-3 px-5 py-2.5 bg-rose-50 border-b-2 border-rose-300 shrink-0 z-10"
    >
      <div class="flex items-center gap-1.5 shrink-0">
        <span
          class="material-symbols-outlined text-rose-600 text-[18px] animate-pulse"
          style="font-variation-settings: 'FILL' 1"
        >warning</span>
        <span class="text-[10px] font-black text-rose-700 uppercase tracking-wider">Khẩn cấp</span>
      </div>
      <p class="text-xs font-bold text-rose-800 flex-1">
        <b class="text-rose-600">{{ urgentCount }} chuyến</b>
        sắp khởi hành trong <b>24 giờ tới</b> chưa được phân công tài xế!
        Hãy click vào thẻ vàng để phân công ngay.
      </p>
      <span
        class="shrink-0 w-7 h-7 rounded-full bg-rose-200 text-rose-800 flex items-center justify-center text-xs font-black"
      >{{ urgentCount }}</span>
    </div>

    <!-- Grid Body -->
    <div class="flex-1 overflow-y-auto overflow-x-auto relative bg-white custom-scrollbar" ref="gridContainer">
      <div class="min-w-[800px] flex flex-col relative" :style="{ height: gridHeight + 'px' }">
        
        <!-- Header Days (X-Axis) -->
        <div class="flex border-b border-slate-200 bg-white sticky top-0 z-30 shadow-sm">
          <div v-for="day in weekDays" :key="day.date" class="flex-1 py-3 text-center border-r border-slate-200 flex flex-col justify-center items-center" :class="{'bg-primary/5': isToday(day.date)}">
             <span class="text-[10px] font-black text-slate-400 uppercase tracking-widest">{{ day.dayName }}</span>
             <span class="text-sm font-bold mt-0.5" :class="isToday(day.date) ? 'text-primary' : 'text-slate-800'">{{ day.dateLabel }}</span>
          </div>
        </div>

        <!-- TimeGrid Area (Kanban Style) -->
        <div class="flex-1 flex relative bg-slate-50/30">
          <div 
            v-for="day in weekDays" :key="'col-' + day.date" 
            class="flex-1 flex flex-col gap-3 p-2.5 border-r border-slate-200/60 overflow-y-auto hide-scrollbar" 
            :class="{'bg-primary/5': isToday(day.date)}"
          >
            
            <!-- ===== LEAVE REQUEST BLOCKS ===== -->
            <LeaveBlock 
              v-for="leave in getLeavesForDay(day.date)" 
              :key="'leave-'+leave.id"
              :leave="leave"
              @click="$emit('open-leave', leave)"
            />

            <!-- ===== TRIP BLOCKS ===== -->
            <TripBlock 
              v-for="trip in getTripsForDay(day.date)" 
              :key="'trip-'+trip.id"
              :trip="trip"
              @click="$emit('open-trip', trip)"
            />

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import TripBlock from './TripBlock.vue';
import LeaveBlock from './LeaveBlock.vue';
import ScheduleFilters from './ScheduleFilters.vue';
import { toBusinessDateString } from '@/utils/businessDate';

const props = defineProps({
  selectedDriver: { type: Object, default: null },
  weekDays: { type: Array, required: true },
  weekLabel: { type: String, required: true },
  loadingSchedule: { type: Boolean, default: false },
  statsTrips: { type: Array, default: () => [] },
  allTrips: { type: Array, default: () => [] },
  getTripsForDay: { type: Function, required: true },
  getLeavesForDay: { type: Function, required: true },
  filterSearch: { type: String, default: '' },
  filterTripStatus: { type: String, default: '' },
  filterRoute: { type: String, default: '' },
  filterVehicle: { type: String, default: '' },
  filterTimePeriod: { type: String, default: '' },
  routeOptions: { type: Array, default: () => [] },
  vehicleOptions: { type: Array, default: () => [] },
  resultCount: { type: Number, default: 0 },
  hasActiveFilters: Boolean
});

defineEmits(['change-week', 'reset-week', 'open-trip', 'open-leave', 'update:filterSearch', 'update:filterTripStatus', 'update:filterRoute', 'update:filterVehicle', 'update:filterTimePeriod', 'reset-filters']);

const tripStats = computed(() => {
  let total = 0;
  let assigned = 0;
  let unassigned = 0;
  let inProgress = 0;
  let completed = 0;

  const trips = props.statsTrips || [];
  total = trips.length;
  trips.forEach(t => {
    if (t.status === 'IN_PROGRESS') {
      inProgress++;
    } else if (t.status === 'COMPLETED') {
      completed++;
    } else if (t.assignedDriverUsername) {
      assigned++;
    } else {
      unassigned++;
    }
  });
  
  return { total, assigned, unassigned, inProgress, completed };
});

const hours = Array.from({ length: 19 }, (_, i) => i + 5); // 05:00 to 23:00
const hourHeight = 96;
const gridHeight = hours.length * hourHeight + 48; // + header

const gridContainer = ref(null);

const isToday = (dateStr) => {
  const today = toBusinessDateString();
  return dateStr === today;
};

// Chưa phân công + khởi hành trong 24h tới
const urgentCount = computed(() => {
  const now = Date.now();
  const in24h = now + 24 * 60 * 60 * 1000;
  return (props.allTrips || []).filter(trip => {
    if (trip.assignedDriverUsername) return false;
    if (!trip.departureDate || !trip.departureTime) return false;
    try {
      const [y, m, d] = trip.departureDate.split('T')[0].split('-');
      const [hh, mm] = trip.departureTime.split(':');
      const depMs = new Date(y, m - 1, d, hh, mm).getTime();
      return depMs > now && depMs <= in24h;
    } catch { return false; }
  }).length;
});
</script>

<style scoped>
.hide-scrollbar::-webkit-scrollbar {
  display: none;
}
.hide-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent; 
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1; 
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8; 
}
</style>
