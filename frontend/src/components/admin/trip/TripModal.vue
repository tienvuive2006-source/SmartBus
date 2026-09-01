<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[9999] bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div class="bg-white rounded-3xl w-full max-w-6xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col max-h-[90vh] animate-scale-up">
        <div class="p-6 bg-[#075955] text-white flex justify-between items-center shrink-0">
          <div class="flex items-center gap-3">
             <span class="material-symbols-outlined">{{ isEditMode ? 'edit_square' : 'add_circle' }}</span>
             <h3 class="text-sm font-black uppercase tracking-widest">{{ isEditMode ? 'Cập nhật lộ trình' : 'Tạo lộ trình mới' }}</h3>
          </div>
          <button @click="closeModal" class="hover:rotate-90 transition-transform bg-white/10 p-1.5 rounded-full flex items-center justify-center">
            <span class="material-symbols-outlined text-sm">close</span>
          </button>
        </div>
        
        <div class="flex-1 flex overflow-hidden">
          <!-- Left: Form Column -->
          <form @submit.prevent="handleFormSubmit" class="w-1/2 p-8 space-y-6 overflow-y-auto border-r border-slate-100 bg-white">
            
            <div class="space-y-1.5 mb-6 relative">
              <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 flex items-center justify-between">
                 <span>
                   Chọn Tuyến Đường Cố Định <span class="text-rose-500">*</span>
                   <span v-if="isEditMode && form.originalAvailableSeats < form.originalTotalSeats" class="text-rose-500 lowercase normal-case italic ml-1">(Đã có khách đặt, không thể đổi)</span>
                 </span>
                 <span v-if="!savedRoutes || savedRoutes.length === 0" class="text-rose-400 text-[9px] italic">Chưa có tuyến mẫu nào</span>
              </label>
              <select 
                :value="selectedRouteOptionValue"
                @change="handleRouteOptionChange"
                required
                :disabled="isEditMode && form.originalAvailableSeats < form.originalTotalSeats"
                class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3.5 text-sm font-bold outline-none transition-all cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed"
              >
                <option value="" disabled>-- Vui lòng chọn một tuyến đường --</option>
                <optgroup label="Tuyến chiều đi">
                  <option v-for="route in savedRoutes" :key="`forward-${route.id}`" :value="`${route.id}:FORWARD`">
                    {{ route.name }}
                  </option>
                </optgroup>
                <optgroup v-if="roundTripRoutes.length" label="Tuyến chiều ngược (khứ hồi)">
                  <option v-for="route in roundTripRoutes" :key="`reverse-${route.id}`" :value="`${route.id}:REVERSE`">
                    {{ reverseRouteName(route) }}
                  </option>
                </optgroup>
                <!-- Fallback option for return trips or unsaved routes -->
                <option 
                  v-if="selectedRouteOptionValue === 'CURRENT'"
                  value="CURRENT"
                >
                  {{ form.departurePoint.split(',')[0] }} → {{ form.arrivalPoint.split(',')[0] }} (Tuyến hiện tại)
                </option>
              </select>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">Hãng xe vận hành</label>
                <input v-model="form.companyName" required class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all" />
              </div>
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">
                  Dòng xe khai thác
                  <span v-if="isEditMode && form.originalAvailableSeats < form.originalTotalSeats" class="text-rose-500 lowercase normal-case italic ml-1">(Đã có khách đặt, không thể đổi)</span>
                </label>
                <select 
                  v-model="form.busType" 
                  required 
                  @change="handleBusTypeChange"
                  :disabled="isEditMode && form.originalAvailableSeats < form.originalTotalSeats"
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  <option v-for="t in availableBusTypes" :key="t.id" :value="t.name">{{ t.name }} ({{ t.seatCount }} Ghế)</option>
                </select>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">Ngày vận hành</label>
                <input v-model="form.departureDate" type="date" required class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all" />
              </div>
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">
                  Giá vé (VNĐ)
                  <span v-if="form.departurePoint && form.arrivalPoint && form.busType && !isEditMode" class="text-rose-500 normal-case font-bold ml-1">
                    (Giá đề xuất)
                  </span>
                </label>
                <input 
                  v-model.number="form.price"
                  type="number" 
                  required 
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all font-mono" 
                />
              </div>
            </div>

            <div class="grid grid-cols-3 gap-4">
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 text-center block">Giờ đi</label>
                <input 
                  v-model="form.departureTime" 
                  type="text"
                  placeholder="15:00"
                  @change="() => {
                    let timeVal = form.departureTime?.trim() || '';
                    if (/^\d{1,2}$/.test(timeVal)) timeVal = `${timeVal.padStart(2, '0')}:00`;
                    else if (/^\d{3,4}$/.test(timeVal)) timeVal = `${timeVal.length === 3 ? '0' + timeVal[0] : timeVal.slice(0,2)}:${timeVal.slice(-2)}`;
                    else if (/^\d{1,2}:\d{1,2}$/.test(timeVal)) {
                       const parts = timeVal.split(':');
                       timeVal = `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`;
                    }
                    form.departureTime = timeVal;

                    if (form.departureTime && form.duration) {
                      const match = form.duration.match(/(\d+)h(?:\s*(\d+)m)?/);
                      if (match) {
                        const durationH = parseInt(match[1]) || 0;
                        const durationM = parseInt(match[2]) || 0;
                        const [depH, depM] = form.departureTime.split(':').map(Number);
                        if (!isNaN(depH) && !isNaN(depM)) {
                          let arrH = depH + durationH;
                          let arrM = depM + durationM;
                          if (arrM >= 60) {
                            arrH += Math.floor(arrM / 60);
                            arrM = arrM % 60;
                          }
                          arrH = arrH % 24;
                          form.arrivalTime = `${String(arrH).padStart(2, '0')}:${String(arrM).padStart(2, '0')}`;
                        }
                      }
                    }
                  }"
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all text-center" 
                />
              </div>
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 text-center block">Giờ đến</label>
                <input v-model="form.arrivalTime" type="text" placeholder="19:00" class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all text-center" />
              </div>
              <div class="space-y-1.5">
                <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 text-center block">Số ghế</label>
                <input 
                  v-model.number="form.availableSeats" 
                  type="number" 
                  :disabled="isEditMode && form.originalAvailableSeats < form.originalTotalSeats"
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all text-center disabled:opacity-60 disabled:cursor-not-allowed" 
                />
              </div>
            </div>

            <!-- Tự động đẻ chuyến về (chỉ hiện khi tạo mới) -->
            <div v-if="!isEditMode" class="mt-6 border-2 border-dashed border-[#075955]/30 bg-[#075955]/[0.02] p-5 rounded-2xl transition-all hover:border-[#075955]/50 relative overflow-hidden group">
              <div class="absolute -right-6 -top-6 w-24 h-24 bg-[#075955]/5 rounded-full blur-xl group-hover:bg-[#075955]/10 transition-all"></div>
              
              <label class="flex items-center gap-3 cursor-pointer mb-2 relative z-10">
                <input type="checkbox" v-model="form.createReturnTrip" class="w-5 h-5 rounded border-slate-300 text-[#075955] focus:ring-[#075955] cursor-pointer" />
                <div class="flex flex-col">
                  <span class="text-sm font-black text-[#075955] tracking-wide uppercase">Tự động đẻ kèm chuyến Về (Khứ hồi)</span>
                  <span class="text-[10px] font-bold text-slate-400">Tiết kiệm thao tác tạo lại từ đầu</span>
                </div>
              </label>
              
              <div v-if="form.createReturnTrip" class="mt-5 animate-fade-in-up relative z-10">
                <div class="mb-5 flex items-center gap-2 text-xs font-bold text-slate-500 bg-white/60 backdrop-blur-sm px-4 py-2.5 rounded-xl border border-slate-200/60 shadow-sm w-fit">
                  <span class="material-symbols-outlined text-[16px] text-emerald-600">sync_alt</span>
                  <span class="uppercase tracking-wider text-[9px] font-black">Lộ trình về:</span>
                  <span v-if="form.departurePoint && form.arrivalPoint" class="text-slate-800 font-black">
                    {{ form.arrivalPoint.split(',')[0] }} ➔ {{ form.departurePoint.split(',')[0] }}
                  </span>
                  <span v-else class="text-rose-400 italic">Vui lòng chọn tuyến đường ở trên trước</span>
                </div>

                <div class="grid grid-cols-3 gap-4">
                  <div class="space-y-1.5">
                    <label class="text-[10px] font-black text-slate-600 uppercase tracking-wider ml-1">Ngày về</label>
                    <input v-model="form.returnDate" type="date" required class="w-full border-2 border-slate-200 focus:border-[#075955] bg-white rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all shadow-sm" />
                  </div>
                  <div class="space-y-1.5">
                    <label class="text-[10px] font-black text-slate-600 uppercase tracking-wider ml-1">Giờ về (Tự tính đến)</label>
                    <input 
                      v-model="form.returnTime" 
                      type="text" 
                      placeholder="14:00" 
                      required
                      @change="() => {
                        let timeVal = form.returnTime?.trim() || '';
                        if (/^\d{1,2}$/.test(timeVal)) timeVal = `${timeVal.padStart(2, '0')}:00`;
                        else if (/^\d{3,4}$/.test(timeVal)) timeVal = `${timeVal.length === 3 ? '0' + timeVal[0] : timeVal.slice(0,2)}:${timeVal.slice(-2)}`;
                        else if (/^\d{1,2}:\d{1,2}$/.test(timeVal)) {
                           const parts = timeVal.split(':');
                           timeVal = `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`;
                        }
                        form.returnTime = timeVal;
                      }"
                      class="w-full border-2 border-slate-200 focus:border-[#075955] bg-white rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all text-center shadow-sm" 
                    />
                  </div>
                  <div class="space-y-1.5">
                    <label class="text-[10px] font-black text-slate-600 uppercase tracking-wider ml-1">Dòng xe về</label>
                    <select 
                      v-model="form.returnBusType" 
                      class="w-full border-2 border-slate-200 focus:border-[#075955] bg-white rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all shadow-sm text-center"
                    >
                      <option value="">-- Giống chuyến đi --</option>
                      <option v-for="t in availableBusTypes" :key="t.id" :value="t.name">{{ t.name }}</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>

            <div class="pt-8 flex justify-end gap-3 shrink-0">
              <button type="button" @click="closeModal" :disabled="isSubmitting" class="px-8 py-3 text-xs font-black uppercase text-slate-400 hover:text-slate-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">Hủy bỏ</button>
              <button
                type="submit"
                :disabled="isSubmitting"
                class="bg-[#075955] text-white px-12 py-3.5 rounded-xl text-xs font-black uppercase tracking-widest shadow-xl hover:shadow-[#075955]/20 active:scale-95 transition-all disabled:opacity-70 disabled:cursor-wait disabled:active:scale-100"
              >
                <span class="inline-flex items-center gap-2">
                  <span v-if="isSubmitting" class="w-3.5 h-3.5 rounded-full border-2 border-white/40 border-t-white animate-spin"></span>
                  {{ isSubmitting ? 'Đang lưu...' : 'Lưu & Đăng tải' }}
                </span>
              </button>
            </div>
          </form>

          <!-- Right: Map Preview Column -->
          <div class="w-1/2 bg-slate-50 relative flex flex-col border-l border-slate-100">
              <div class="absolute top-5 left-5 right-5 z-[1000] bg-white shadow-xl rounded-2xl p-4 border border-slate-200">
                 <div class="flex items-center justify-between mb-2">
                    <h4 class="text-[11px] font-black text-[#075955] uppercase flex items-center gap-2">
                       <span class="material-symbols-outlined text-[#075955] text-base">explore</span> Bản đồ lộ trình
                    </h4>
                    <span class="text-[9px] font-black text-emerald-600 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-100 tracking-tighter">GPS ACTIVE</span>
                 </div>
                 <div v-if="form.departureLat && form.arrivalLat" class="text-[10px] font-bold text-slate-500 leading-tight">
                     <div class="flex items-center gap-1 text-[#075955] mb-2">
                       <span class="material-symbols-outlined text-[12px]">check_circle</span>
                       Tọa độ lộ trình đã xác nhận chuẩn xác
                     </div>
                     <div v-if="form.duration" class="bg-slate-50 border border-slate-100 p-2.5 rounded-xl flex gap-4 text-xs">
                       <div class="flex items-center gap-1 text-slate-700">
                         <span class="material-symbols-outlined text-slate-400 text-sm">route</span>
                         <span>Lộ trình: <strong class="text-[#075955] font-black">{{ form.duration }}</strong></span>
                       </div>
                     </div>
                  </div>
                 <div v-else class="text-[10px] font-bold text-rose-500 flex flex-col gap-1 animate-pulse">
                    <div class="flex items-center gap-1">
                      <span class="material-symbols-outlined text-[12px]">warning</span> Chờ xác định tọa độ...
                    </div>
                    <div class="text-[9px] text-slate-400 font-normal">Vui lòng chọn Tuyến đường cố định ở cột bên trái</div>
                 </div>
              </div>

             <div id="admin-route-map" class="flex-1 w-full h-full"></div>
             
             <div v-if="mapLoading" class="absolute inset-0 bg-slate-900/10 backdrop-blur-[2px] flex flex-col items-center justify-center z-[1001]">
                <div class="w-10 h-10 border-4 border-[#075955] border-t-transparent rounded-full animate-spin"></div>
             </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, ref, watch, nextTick } from 'vue';
import { useApi } from '@/composables/useApi';
import { decodePolyline, fetchPolylineFromCloudinary } from '@/utils/polyline';
import { useRouteBusTypeApi } from '@/services/routeBusTypeApi';

const props = defineProps({
  busTypes: Array,
  buses: Array,
  inspectors: Array,
  drivers: Array
});

const emit = defineEmits(['saved']);
const api = useApi();
const routeBusTypeApi = useRouteBusTypeApi();

const isOpen = ref(false);
const isEditMode = ref(false);
const isSubmitting = ref(false);
const mapLoading = ref(false);
const leafletMap = ref(null);
const savedRoutes = ref([]);
const routeBusTypeConfig = ref({ unrestricted: true, busTypes: [], defaultBusTypeId: null });

const tomorrow = new Date();
tomorrow.setDate(tomorrow.getDate() + 1);
const tomorrowStr = tomorrow.toISOString().split('T')[0];

const defaultForm = {
  id: null, routeId: null, companyName: 'Trung - Nam', busType: 'Luxury', departurePoint: '', arrivalPoint: '',
  assignedLicensePlate: '',
  departureDate: tomorrowStr, departureTime: '08:00', arrivalTime: '12:00',
  duration: '4h', price: null, rating: 4.8, availableSeats: 36, imageUrl: '', instantConfirmation: true,
  departureLat: 0, departureLng: 0, arrivalLat: 0, arrivalLng: 0, inspectorId: '', routeData: '',
  createReturnTrip: false, returnDate: tomorrowStr, returnTime: '14:00', returnBusType: ''
};

const form = ref({ ...defaultForm });

const availableBusTypes = computed(() => {
  const configuredTypes = routeBusTypeConfig.value.unrestricted
    ? (props.busTypes || [])
    : (routeBusTypeConfig.value.busTypes || []);
  const currentType = (props.busTypes || []).find(type => type.name === form.value.busType);
  if (isEditMode.value && currentType && !configuredTypes.some(type => type.id === currentType.id)) {
    return [...configuredTypes, currentType];
  }
  return configuredTypes;
});

const roundTripRoutes = computed(() => savedRoutes.value.filter(route => route.roundTripEnabled === true));

const reverseRouteName = route => {
  const parts = String(route?.name || '').split(/\s+-\s+/).filter(Boolean);
  if (parts.length >= 2) return [...parts].reverse().join(' - ');
  const departure = String(route?.departurePoint || '').split(',')[0];
  const arrival = String(route?.arrivalPoint || '').split(',')[0];
  return `${arrival} - ${departure}`;
};

const reverseRouteTemplate = route => ({
  ...route,
  name: reverseRouteName(route),
  departurePoint: route.arrivalPoint,
  arrivalPoint: route.departurePoint,
  departureLat: route.arrivalLat,
  departureLng: route.arrivalLng,
  arrivalLat: route.departureLat,
  arrivalLng: route.departureLng,
  isReverseOption: true
});

const selectedRouteOptionValue = computed(() => {
  if (!form.value.departurePoint || !form.value.arrivalPoint) return '';
  const route = savedRoutes.value.find(item =>
    Number(item.id) === Number(form.value.routeId)
    || (item.departurePoint === form.value.departurePoint && item.arrivalPoint === form.value.arrivalPoint)
    || (item.departurePoint === form.value.arrivalPoint && item.arrivalPoint === form.value.departurePoint)
  );
  if (!route) return 'CURRENT';
  const reversed = route.departurePoint === form.value.arrivalPoint
    && route.arrivalPoint === form.value.departurePoint;
  return `${route.id}:${reversed ? 'REVERSE' : 'FORWARD'}`;
});

const handleRouteOptionChange = event => {
  const value = String(event.target.value || '');
  if (!value || value === 'CURRENT') return;
  const [routeId, direction] = value.split(':');
  const route = savedRoutes.value.find(item => Number(item.id) === Number(routeId));
  if (!route) return;
  applyRouteTemplate(direction === 'REVERSE' ? reverseRouteTemplate(route) : route);
};

const fetchSavedRoutes = async () => {
  try {
    const res = await api.get('/routes');
    savedRoutes.value = res.data;
  } catch (err) {
    console.error('Lỗi tải danh sách tuyến đường mẫu:', err);
  }
};

const loadAllowedBusTypes = async route => {
  if (!route?.id) {
    routeBusTypeConfig.value = { unrestricted: true, busTypes: [], defaultBusTypeId: null };
    return;
  }

  try {
    const response = await routeBusTypeApi.getConfig(route.id);
    routeBusTypeConfig.value = response.data;
  } catch (error) {
    console.error('Không tải được dòng xe được phép của tuyến:', error);
    routeBusTypeConfig.value = { unrestricted: true, busTypes: [], defaultBusTypeId: null };
  }
};

const applyRouteTemplate = async (route) => {
  form.value.routeId = route.id || null;
  form.value.departurePoint = route.departurePoint;
  form.value.arrivalPoint = route.arrivalPoint;
  form.value.departureLat = route.departureLat;
  form.value.departureLng = route.departureLng;
  form.value.arrivalLat = route.arrivalLat;
  form.value.arrivalLng = route.arrivalLng;
  form.value.routeData = route.routeData;
  form.value.duration = route.duration || '4h';
  await loadAllowedBusTypes(route);

  if (!availableBusTypes.value.some(type => type.name === form.value.busType)) {
    const defaultType = availableBusTypes.value.find(
      type => type.id === routeBusTypeConfig.value.defaultBusTypeId
    ) || availableBusTypes.value[0];
    if (defaultType) {
      form.value.busType = defaultType.name;
      handleBusTypeChange({ target: { value: defaultType.name } });
    }
  }
  
  if (route.basePrice) {
     const matchedBus = props.busTypes.find(b => b.name === form.value.busType);
     const multiplier = matchedBus?.priceMultiplier || 1.0;
     form.value.price = Math.round(route.basePrice * multiplier);
  }

  updateMap();
};

watch(() => form.value.createReturnTrip, enabled => {
  if (enabled || !selectedRouteOptionValue.value.endsWith(':REVERSE')) return;
  const routeId = selectedRouteOptionValue.value.split(':')[0];
  const route = savedRoutes.value.find(item => Number(item.id) === Number(routeId));
  if (route) applyRouteTemplate(route);
});

const handleBusTypeChange = (event) => {
  const selectedTypeName = event.target.value;
  const selectedType = props.busTypes?.find((type) => type.name === selectedTypeName);

  if (selectedType?.seatCount) {
    form.value.availableSeats = Number(selectedType.seatCount);
    form.value.totalSeats = Number(selectedType.seatCount);
  }

  if (selectedType?.imageUrl) {
    form.value.imageUrl = selectedType.imageUrl;
  }

  if (form.value.assignedLicensePlate) {
    const assignedBus = props.buses?.find((bus) => bus.licensePlate === form.value.assignedLicensePlate);
    if (assignedBus && assignedBus.busType !== selectedTypeName) {
      form.value.assignedLicensePlate = '';
    }
  }
};

const openModal = async (trip = null) => {
  isSubmitting.value = false;
  await fetchSavedRoutes();
  routeBusTypeConfig.value = { unrestricted: true, busTypes: [], defaultBusTypeId: null };
  if (trip) {
    isEditMode.value = true;
    form.value = { 
      ...trip,
      originalAvailableSeats: trip.availableSeats,
      originalTotalSeats: trip.totalSeats,
      inspectorId: trip.inspector ? trip.inspector.id : ''
    };
  } else {
    isEditMode.value = false;
    form.value = { ...defaultForm };
  }
  const matchingRoute = savedRoutes.value.find(route =>
    route.departurePoint === form.value.departurePoint &&
    route.arrivalPoint === form.value.arrivalPoint
  );
  if (matchingRoute) {
    if (!form.value.routeId) form.value.routeId = matchingRoute.id;
    await loadAllowedBusTypes(matchingRoute);
  }
  isOpen.value = true;
  initAdminMap();
};

const closeModal = () => {
  isOpen.value = false;
};

// --- Map Logic ---
const initAdminMap = () => {
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

const renderLeaflet = () => {
  const L = window.L; if (!L) return;
  nextTick(() => {
    const container = document.getElementById('admin-route-map');
    if (!container) return;
    if (leafletMap.value) leafletMap.value.remove();
    leafletMap.value = L.map(container, { preferCanvas: true }).setView([16.0, 108.0], 6);
    L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
      maxZoom: 20, subdomains: ['mt0', 'mt1', 'mt2', 'mt3'], attribution: '© Google Maps'
    }).addTo(leafletMap.value);
    
    setTimeout(() => { if (leafletMap.value) { leafletMap.value.invalidateSize(); updateMap(); } }, 500);
  });
};

const updateMap = async () => {
  const L = window.L; if (!L || !leafletMap.value) return;
  leafletMap.value.eachLayer((layer) => { if (layer instanceof L.Marker || layer instanceof L.Polyline) leafletMap.value.removeLayer(layer); });
  
  const from = [form.value.departureLat, form.value.departureLng];
  const to = [form.value.arrivalLat, form.value.arrivalLng];
  
  const startIcon = L.divIcon({ 
    html: `<div class="w-8 h-8 bg-emerald-500 border-4 border-white rounded-full shadow-2xl flex items-center justify-center text-white"><span class="material-symbols-outlined text-sm">trip_origin</span></div>`, 
    className: '', iconSize: [32, 32] 
  });
  
  const endIcon = L.divIcon({ 
    html: `<div class="w-8 h-8 bg-rose-600 border-4 border-white rounded-full shadow-2xl flex items-center justify-center text-white"><span class="material-symbols-outlined text-sm">location_on</span></div>`, 
    className: '', iconSize: [32, 32] 
  });

  if (from[0] > 1 && to[0] > 1) {
    L.marker(from, { icon: startIcon }).addTo(leafletMap.value).bindPopup('<b>Điểm đi:</b> ' + form.value.departurePoint);
    L.marker(to, { icon: endIcon }).addTo(leafletMap.value).bindPopup('<b>Điểm đến:</b> ' + form.value.arrivalPoint);
    
    const fallbackLine = L.polyline([from, to], { 
      color: '#075955', weight: 2, dashArray: '5, 10', opacity: 0.5 
    }).addTo(leafletMap.value);
    leafletMap.value.fitBounds([from, to], { padding: [100, 100] });

    if (form.value.routeData) {
       try {
         let routeStr = form.value.routeData;
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
           L.polyline(coords, { color: '#000', weight: 6, opacity: 0.15, smoothFactor: 2 }).addTo(leafletMap.value);
           L.polyline(coords, { 
             color: '#075955', weight: 5, opacity: 0.9, lineJoin: 'round', smoothFactor: 2
           }).addTo(leafletMap.value);
           leafletMap.value.fitBounds(coords, { padding: [100, 100] });
         }
       } catch (e) { console.error("Lỗi parse routeData", e); }
    }
  } else if (from[0] > 1) { 
    leafletMap.value.setView(from, 13); 
    L.marker(from, { icon: startIcon }).addTo(leafletMap.value); 
  } else if (to[0] > 1) { 
    leafletMap.value.setView(to, 13); 
    L.marker(to, { icon: endIcon }).addTo(leafletMap.value); 
  }
};

const handleFormSubmit = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    const { createReturnTrip, returnDate, returnTime, returnBusType, originalAvailableSeats, originalTotalSeats, ...payload } = form.value;

    if (isEditMode.value) {
      await api.put(`/trips/${form.value.id}`, payload);
    } else {
      await api.post('/trips', payload);
      
      if (form.value.createReturnTrip) {
        let returnArrTime = '19:00'; 
        if (form.value.returnTime && form.value.duration) {
          const match = form.value.duration.match(/(\d+)h(?:\s*(\d+)m)?/);
          if (match) {
            const durationH = parseInt(match[1]) || 0;
            const durationM = parseInt(match[2]) || 0;
            const [depH, depM] = form.value.returnTime.split(':').map(Number);
            if (!isNaN(depH) && !isNaN(depM)) {
              let arrH = depH + durationH;
              let arrM = depM + durationM;
              if (arrM >= 60) {
                arrH += Math.floor(arrM / 60);
                arrM = arrM % 60;
              }
              arrH = arrH % 24;
              returnArrTime = `${String(arrH).padStart(2, '0')}:${String(arrM).padStart(2, '0')}`;
            }
          }
        }
        
        const returnPayload = {
          ...payload,
          departurePoint: payload.arrivalPoint,
          arrivalPoint: payload.departurePoint,
          departureLat: payload.arrivalLat,
          departureLng: payload.arrivalLng,
          arrivalLat: payload.departureLat,
          arrivalLng: payload.departureLng,
          departureDate: form.value.returnDate,
          departureTime: form.value.returnTime,
          arrivalTime: returnArrTime,
          routeData: ''
        };
        
        if (form.value.returnBusType) {
          returnPayload.busType = form.value.returnBusType;
          const rbt = props.busTypes.find(t => t.name === form.value.returnBusType);
          if (rbt) {
             returnPayload.availableSeats = rbt.seatCount;
             returnPayload.totalSeats = rbt.seatCount;
             if (rbt.imageUrl) returnPayload.imageUrl = rbt.imageUrl;
          }
        }
        
        await api.post('/trips', returnPayload);
        alert('Đã tạo thành công 2 chuyến xe: Đi và Về!');
      }
    }
    
    closeModal(); 
    emit('saved');
    if (!form.value.createReturnTrip && !isEditMode.value) alert('Tạo chuyến xe thành công!');
    if (isEditMode.value) alert('Cập nhật chuyến xe thành công!');
  } catch (err) { 
    console.error("Lỗi:", err);
    alert(err.response?.data?.message || 'Lỗi lưu dữ liệu! Vui lòng kiểm tra lại.'); 
  } finally {
    isSubmitting.value = false;
  }
};

defineExpose({ openModal, closeModal });
</script>

<style scoped>
@keyframes scaleUp {
  from { opacity: 0; transform: scale(0.98) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in-up { animation: fadeIn 0.4s ease-out forwards; }
</style>
