<template>
  <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
    <!-- Toolbar -->
    <div class="p-4 border-b border-slate-100 flex flex-wrap justify-between items-center gap-3 bg-slate-50/50">
      <h3 class="text-sm font-bold text-slate-800 flex items-center gap-2">
         <span class="material-symbols-outlined text-[#075955] text-[20px]">list_alt</span>
         Danh sách vận hành
      </h3>
      <div class="flex flex-wrap items-center justify-end gap-2">
        <button
          v-if="selectedIds.length"
          type="button"
          class="px-3 py-2 rounded-lg border border-slate-200 bg-white text-[10px] font-black uppercase text-slate-600 hover:bg-slate-100 transition-colors"
          @click="clearSelection"
        >
          Bỏ chọn
        </button>
        <button
          v-if="eligiblePoolIds.length > eligiblePageIds.length && selectedIds.length < eligiblePoolIds.length"
          type="button"
          class="px-3 py-2 rounded-lg border border-[#075955]/20 bg-[#075955]/5 text-[10px] font-black uppercase text-[#075955] hover:bg-[#075955]/10 transition-colors"
          @click="selectAllFiltered"
        >
          Chọn tất cả {{ eligiblePoolIds.length }} chuyến có thể xóa
        </button>
        <button
          v-if="selectedIds.length"
          type="button"
          :disabled="bulkDeleting"
          class="px-3 py-2 rounded-lg bg-rose-600 text-[10px] font-black uppercase text-white hover:bg-rose-700 disabled:opacity-60 disabled:cursor-wait transition-colors flex items-center gap-1.5"
          @click="requestBulkDelete"
        >
          <span class="material-symbols-outlined text-[16px]">delete_sweep</span>
          {{ bulkDeleting ? 'Đang xóa...' : `Xóa đã chọn (${selectedIds.length})` }}
        </button>
        <div class="flex items-center gap-2 px-3 py-1 bg-emerald-50 rounded-lg border border-emerald-100">
          <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
          <span class="text-[10px] font-bold text-emerald-700 uppercase">{{ totalCount }} chuyến</span>
        </div>
      </div>
    </div>

    <!-- Table Content -->
    <div class="overflow-x-auto">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-slate-50 border-b border-slate-100">
            <th class="pl-4 pr-1 py-4 w-10 text-center">
              <input
                type="checkbox"
                class="w-4 h-4 accent-[#075955] cursor-pointer"
                :checked="allPageSelected"
                :indeterminate.prop="somePageSelected"
                :disabled="eligiblePageIds.length === 0"
                aria-label="Chọn các chuyến có thể xóa trong trang này"
                title="Chọn các chuyến có thể xóa trong trang này"
                @change="togglePageSelection"
              />
            </th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">ID</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Lộ trình & Nhà xe</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Dòng xe</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400">Thời gian</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-right">Giá vé</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-center">Trạng thái</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-center">Hiển thị</th>
            <th class="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-slate-400 text-center">Thao tác</th>
          </tr>
        </thead>
        <tbody v-if="trips.length > 0">
          <tr 
            v-for="trip in trips" 
            :key="trip.id" 
            class="group border-b border-slate-50 hover:bg-[#075955]/[0.02] transition-all duration-200"
            :class="selectedIds.includes(trip.id) ? 'bg-rose-50/60' : ''"
          >
            <td class="pl-4 pr-1 py-5 text-center">
              <input
                v-model="selectedIds"
                type="checkbox"
                :value="trip.id"
                :disabled="!isDeletable(trip)"
                :aria-label="`Chọn chuyến #${trip.id} để xóa`"
                :title="isDeletable(trip) ? 'Chọn chuyến này' : (isCompleted(trip) ? 'Chuyến đã hoàn thành chỉ được phép xem' : 'Chuyến đã có khách đặt nên không thể xóa')"
                class="w-4 h-4 accent-[#075955] cursor-pointer disabled:cursor-not-allowed disabled:opacity-30"
              />
            </td>
            <td class="px-6 py-5 font-mono text-[11px] font-black text-slate-300 group-hover:text-[#075955]">#{{ trip.id }}</td>
            <td class="px-6 py-5">
              <div class="flex items-start gap-3">
                <div class="flex flex-col gap-1 relative pl-4 mt-1">
                  <div class="absolute left-1 top-2 bottom-2 w-[2px] bg-slate-200"></div>
                  <div class="flex items-center gap-2">
                    <div class="w-2 h-2 rounded-full bg-[#075955] shrink-0 -ml-[17px] z-10 border-2 border-white shadow-sm"></div>
                    <span class="font-black text-slate-800 text-sm leading-none">{{ simplifyLocation(trip.departurePoint) }}</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <div class="w-2 h-2 rounded-full bg-slate-400 shrink-0 -ml-[17px] z-10 border-2 border-white shadow-sm"></div>
                    <span class="font-bold text-slate-500 text-sm leading-none">{{ simplifyLocation(trip.arrivalPoint) }}</span>
                  </div>
                </div>
                <div class="ml-auto shrink-0">
                   <span class="text-[9px] font-black text-[#075955] bg-[#075955]/5 px-2.5 py-1 rounded border border-[#075955]/10 uppercase tracking-tighter whitespace-nowrap">{{ trip.companyName }}</span>
                </div>
              </div>
            </td>
            <td class="px-6 py-5">
              <div class="flex flex-col gap-1 items-start">
                <span class="text-[10px] font-black text-slate-700 uppercase">{{ trip.busType }}</span>
                <div class="flex gap-1 mb-1">
                   <span v-for="i in 3" :key="i" class="w-1.5 h-1.5 rounded-full bg-emerald-400/30"></span>
                </div>
              </div>
            </td>
            <td class="px-6 py-5">
              <div class="flex flex-col leading-tight">
                <span class="text-sm font-black text-slate-800">{{ trip.departureTime }}</span>
                <span class="text-[10px] text-slate-400 font-bold uppercase mt-0.5 tracking-tighter">{{ formatDate(trip.departureDate) }}</span>
              </div>
            </td>
            <td class="px-6 py-5 text-right font-black text-[#075955] text-base tabular-nums">
              {{ trip.price ? trip.price.toLocaleString() : '0' }}<span class="text-[10px] ml-0.5">đ</span>
            </td>
            <td class="px-6 py-5 text-center">
              <div v-if="isTripPassed(trip)" class="inline-flex whitespace-nowrap px-3 py-1 rounded-full text-[9px] font-black uppercase tracking-widest border shadow-sm bg-slate-100 border-slate-200 text-slate-500">
                Đã khởi hành
              </div>
              <div v-else
                :class="[
                  'inline-flex whitespace-nowrap px-3 py-1 rounded-full text-[9px] font-black uppercase tracking-widest border shadow-sm',
                  trip.availableSeats <= 0 
                    ? 'bg-rose-50 border-rose-100 text-rose-600' 
                    : trip.availableSeats < 10 
                      ? 'bg-amber-50 border-amber-100 text-amber-600'
                      : 'bg-emerald-50 border-emerald-100 text-emerald-600'
                ]"
              >
                {{ trip.availableSeats }} chỗ trống
              </div>
            </td>
            <td class="px-6 py-5 text-center">
              <button 
                @click="toggleVisibility(trip)"
                class="relative inline-flex items-center h-5 w-9 rounded-full transition-colors duration-300 ease-in-out focus:outline-none focus:ring-2 focus:ring-[#075955]/30"
                :class="trip.isVisible !== false ? 'bg-[#075955]' : 'bg-slate-300'"
                :title="trip.isVisible !== false ? 'Đang hiển thị trên Home' : 'Đã bị ẩn'"
              >
                <span 
                  class="inline-block w-4 h-4 transform bg-white rounded-full transition-transform duration-300 ease-in-out shadow-sm"
                  :class="trip.isVisible !== false ? 'translate-x-4' : 'translate-x-0.5'"
                ></span>
              </button>
            </td>
            <td class="px-6 py-5">
              <div class="flex items-center justify-center gap-2">
                <button 
                  @click="$emit('report', trip)" 
                  class="w-9 h-9 flex items-center justify-center rounded-xl bg-emerald-50 text-emerald-600 hover:bg-emerald-600 hover:text-white transition-all shadow-sm border border-emerald-100"
                  title="Báo cáo tài chính chuyến xe"
                >
                  <span class="material-symbols-outlined text-lg">monitoring</span>
                </button>
                <button 
                  @click="$emit('edit', trip)" 
                  class="w-9 h-9 flex items-center justify-center rounded-xl transition-all shadow-sm border"
                  :class="isCompleted(trip)
                    ? 'bg-blue-50 text-blue-600 hover:bg-blue-600 hover:text-white border-blue-100'
                    : 'bg-slate-50 text-slate-600 hover:bg-[#075955] hover:text-white border-slate-100'"
                  :title="isCompleted(trip) ? 'Xem chuyến xe đã hoàn thành' : 'Sửa chuyến xe'"
                >
                  <span class="material-symbols-outlined text-lg">{{ isCompleted(trip) ? 'visibility' : 'edit_note' }}</span>
                </button>
                <button 
                  v-if="isDeletable(trip)"
                  @click="$emit('delete', trip.id)" 
                  class="w-9 h-9 flex items-center justify-center rounded-xl bg-slate-50 text-rose-500 hover:bg-rose-500 hover:text-white transition-all shadow-sm border border-slate-100"
                  title="Xoá lộ trình này"
                >
                  <span class="material-symbols-outlined text-lg">delete_sweep</span>
                </button>
                <button 
                  v-else
                  class="w-9 h-9 flex items-center justify-center rounded-xl bg-slate-100 text-slate-300 cursor-not-allowed shadow-sm border border-slate-200 opacity-50"
                  :title="isCompleted(trip) ? 'Chuyến đã hoàn thành chỉ được phép xem' : 'Không thể xoá chuyến xe đã có khách mua vé'"
                >
                  <span class="material-symbols-outlined text-lg">delete_sweep</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr>
            <td colspan="9" class="py-20 text-center">
              <div class="flex flex-col items-center">
                <div class="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mb-4">
                  <span class="material-symbols-outlined text-4xl text-slate-300">search_off</span>
                </div>
                <h4 class="font-black text-slate-400 uppercase tracking-widest">Không có dữ liệu lộ trình</h4>
                <p class="text-xs text-slate-400 mt-1">Hãy bấm "Tạo lộ trình mới" để bắt đầu vận hành</p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <AdminPagination
      :page="page"
      :total-pages="totalPages"
      :total-elements="totalCount"
      :page-size="pageSize"
      :current-count="trips.length"
      @update:page="$emit('update:page', $event)"
    />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useApi } from '@/composables/useApi';
import AdminPagination from '@/components/admin/common/AdminPagination.vue';

const api = useApi();

const props = defineProps({
  trips: { type: Array, default: () => [] },
  selectableTrips: { type: Array, default: () => [] },
  totalCount: { type: Number, default: 0 },
  page: { type: Number, default: 0 },
  totalPages: { type: Number, default: 0 },
  pageSize: { type: Number, default: 10 },
  bulkDeleting: { type: Boolean, default: false }
});

const emit = defineEmits(['edit', 'delete', 'report', 'bulk-delete', 'update:page']);
const selectedIds = ref([]);

const isCompleted = trip => String(trip?.status || '').toUpperCase() === 'COMPLETED';
const isDeletable = (trip) => !isCompleted(trip)
  && (!trip.totalSeats || trip.availableSeats === trip.totalSeats);
const eligiblePageIds = computed(() => props.trips.filter(isDeletable).map(trip => trip.id));
const selectionPool = computed(() => props.selectableTrips.length ? props.selectableTrips : props.trips);
const eligiblePoolIds = computed(() => selectionPool.value.filter(isDeletable).map(trip => trip.id));
const allPageSelected = computed(() => eligiblePageIds.value.length > 0
  && eligiblePageIds.value.every(id => selectedIds.value.includes(id)));
const somePageSelected = computed(() => !allPageSelected.value
  && eligiblePageIds.value.some(id => selectedIds.value.includes(id)));

const togglePageSelection = () => {
  if (allPageSelected.value) {
    selectedIds.value = selectedIds.value.filter(id => !eligiblePageIds.value.includes(id));
  } else {
    selectedIds.value = [...new Set([...selectedIds.value, ...eligiblePageIds.value])];
  }
};

const selectAllFiltered = () => {
  selectedIds.value = [...eligiblePoolIds.value];
};

const clearSelection = () => {
  selectedIds.value = [];
};

const requestBulkDelete = () => {
  emit('bulk-delete', [...selectedIds.value]);
};

watch(eligiblePoolIds, (validIds) => {
  selectedIds.value = selectedIds.value.filter(id => validIds.includes(id));
});

const toggleVisibility = async (trip) => {
  try {
    const res = await api.patch(`/trips/${trip.id}/visibility`);
    trip.isVisible = res.data.isVisible;
  } catch (error) {
    alert("Không thể thay đổi trạng thái hiển thị!");
    console.error(error);
  }
};

const simplifyLocation = (loc) => {
  if (!loc) return '';
  const parts = loc.split(',');
  return parts[parts.length - 1].trim().replace(/\b(Thành phố|TP|Tỉnh)\b/gi, '').trim();
};

const formatDate = (d) => {
  if (!d) return '';
  try {
    const parts = d.split('T');
    if (parts.length > 0 && parts[0].includes('-')) {
      return parts[0].split('-').reverse().join('/');
    }
    return d;
  } catch (e) {
    return d;
  }
};

const getTripDepartureTime = (trip) => {
  if (!trip?.departureDate || !trip?.departureTime) return null;
  try {
    const dateParts = trip.departureDate.split('T')[0].split('-');
    if (dateParts.length !== 3) return null;
    const [year, month, day] = dateParts;
    const timeParts = trip.departureTime.split(':');
    if (timeParts.length < 2) return null;
    const [hour, minute] = timeParts;
    return new Date(year, month - 1, day, hour, minute);
  } catch (e) {
    return null;
  }
};

const isTripPassed = (trip) => {
  const depTime = getTripDepartureTime(trip);
  return depTime ? new Date() > depTime : false;
};

// Chuyến trong vòng 24h tới mà chưa phân công tài xế
const isUnassignedUrgent = (trip) => {
  if (isTripPassed(trip)) return false;
  const hasDriver = trip.assignedDriverUsername && trip.assignedDriverUsername.trim() !== '';
  if (hasDriver) return false;
  const depTime = getTripDepartureTime(trip);
  if (!depTime) return false;
  const msUntilDeparture = depTime.getTime() - Date.now();
  return msUntilDeparture > 0 && msUntilDeparture <= 24 * 60 * 60 * 1000;
};

const urgentUnassignedTrips = computed(() =>
  props.trips.filter(isUnassignedUrgent)
);
</script>
