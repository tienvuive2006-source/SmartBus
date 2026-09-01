<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[9999] bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-3 lg:p-5">
      <div class="route-modal-shell bg-white rounded-3xl w-full shadow-2xl border border-slate-200 overflow-hidden flex flex-col animate-scale-up">
        <header class="route-modal-header px-7 py-5 text-white flex justify-between items-center shrink-0">
          <div class="flex items-center gap-3">
            <span class="route-header-icon material-symbols-outlined">{{ editingIndex >= 0 ? 'edit_square' : 'add_circle' }}</span>
            <div>
              <h3 class="text-base font-black tracking-tight">{{ editingIndex >= 0 ? 'Chỉnh sửa tuyến đường ' : 'Tạo tuyến đường ' }}</h3>
              <p class="mt-0.5 text-[11px] font-medium text-emerald-50/75">Thiết lập lộ trình, dòng xe và phương tiện khai thác</p>
            </div>
          </div>
          <button @click="closeModal" class="route-close-button flex items-center justify-center" aria-label="Đóng">
            <span class="material-symbols-outlined text-sm">close</span>
          </button>
        </header>
        
        <div class="flex-1 flex overflow-hidden">
          <!-- Left: Form Column -->
          <div class="route-form-pane flex min-h-0 flex-col border-r border-slate-200 bg-[#f6f8f7]">
          <form id="route-editor-form" ref="formScroll" @submit.prevent="saveRoute" class="route-form-scroll flex-1 space-y-6 overflow-y-auto p-7 lg:p-8">
            <section class="route-section-card space-y-5">
              <div class="route-section-heading">
                <span class="material-symbols-outlined">route</span>
                <div>
                  <h4>Thông tin lộ trình</h4>
                  <p>Nhập điểm đi, điểm đến và thông tin cơ bản của tuyến.</p>
                </div>
              </div>
            <div class="space-y-1.5 relative">
              <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">
                <span>Tên tuyến đường (Tùy chọn)</span>
              </label>
              <div class="relative flex items-center">
                <input 
                  v-model="form.name" 
                  placeholder="VD: Tuyến Cao Nguyên, Tuyến Biển..." 
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3.5 text-[15px] font-bold outline-none transition-all" 
                />
              </div>
            </div>

            <div class="space-y-1.5 relative">
              <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 flex justify-between">
                <span>Điểm khởi hành</span>
                <span v-if="geocoding.departure" class="text-[#075955] animate-pulse text-[8px]">Đang lấy tọa độ...</span>
              </label>
              <div class="relative flex items-center">
                <input 
                  v-model="form.departurePoint" 
                  @focus="openLocationDropdown('departure')"
                  @blur="closeLocationDropdown('departure')"
                  @keyup.enter="autoGeocode(form.departurePoint, 'departure')"
                  required placeholder="Ví dụ: Bến xe Đà Nẵng" 
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl pl-4 pr-10 py-4 text-[15px] font-bold outline-none transition-all" 
                />
                <button type="button" @click="autoGeocode(form.departurePoint, 'departure')" class="absolute right-3 text-slate-400 hover:text-[#075955] p-1">
                  <span class="material-symbols-outlined text-lg">my_location</span>
                </button>
              </div>
              <div class="flex gap-2 items-center pt-1 px-1">
                <span class="text-[9px] text-slate-400 font-bold uppercase w-12 shrink-0" title="Dán tọa độ Google Maps vào đây">Tọa độ:</span>
                <input :value="form.departureLat && form.departureLng ? `${form.departureLat}, ${form.departureLng}` : ''" @input="e => parseCoordinates(e.target.value, 'departure')" type="text" placeholder="Dán tọa độ (VD: 13.092, 109.293)" class="w-full bg-slate-50 hover:bg-white border border-slate-200 text-[11px] font-mono p-2 rounded-lg focus:border-[#075955] outline-none transition-colors shadow-sm" />
              </div>

              <div v-if="showFromDropdown" class="absolute left-0 right-0 top-[100%] mt-2 bg-white border border-slate-200 shadow-2xl rounded-2xl z-[1000] overflow-hidden">
                <div v-if="savedLocationsLoading" class="px-5 py-4 text-xs font-bold text-slate-400 flex items-center gap-2">
                  <span class="material-symbols-outlined text-base animate-spin">progress_activity</span> Đang tải vị trí đã lưu
                </div>
                <div v-else-if="savedLocationsError" class="px-5 py-4 text-xs font-bold text-rose-500">{{ savedLocationsError }}</div>
                <ul v-else-if="fromSuggestions.length" class="max-h-56 overflow-y-auto">
                  <li v-for="loc in fromSuggestions" :key="loc.id" @mousedown.prevent="selectSavedLocation(loc, 'departure')" class="px-5 py-3 hover:bg-emerald-50 cursor-pointer border-b border-slate-50 last:border-0">
                    <strong class="block text-xs text-slate-700">{{ loc.name }}</strong>
                  </li>
                </ul>
                <div v-else class="px-5 py-4 text-xs text-slate-400">
                  Không tìm thấy vị trí phù hợp trong danh mục.
                </div>
              </div>
            </div>

            <div class="flex justify-center -my-2 relative z-10">
              <button 
                type="button"
                @click="swapRoute" 
                class="w-8 h-8 rounded-full bg-[#075955] text-white hover:bg-[#0a7a75] hover:rotate-180 active:scale-95 transition-all duration-300 flex items-center justify-center cursor-pointer shadow-md border border-white"
                title="Đảo chiều lộ trình"
              >
                <span class="material-symbols-outlined text-sm">sync_alt</span>
              </button>
            </div>

            <div class="space-y-1.5 relative">
              <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 flex justify-between">
                <span>Điểm kết thúc</span>
                <span v-if="geocoding.arrival" class="text-[#075955] animate-pulse text-[8px]">Đang lấy tọa độ...</span>
              </label>
              <div class="relative flex items-center">
                <input 
                  v-model="form.arrivalPoint" 
                  @focus="openLocationDropdown('arrival')"
                  @blur="closeLocationDropdown('arrival')"
                  @keyup.enter="autoGeocode(form.arrivalPoint, 'arrival')"
                  required placeholder="Ví dụ: Bến xe Miền Tây" 
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl pl-4 pr-10 py-4 text-[15px] font-bold outline-none transition-all" 
                />
                <button type="button" @click="autoGeocode(form.arrivalPoint, 'arrival')" class="absolute right-3 text-slate-400 hover:text-[#075955] p-1">
                  <span class="material-symbols-outlined text-lg">my_location</span>
                </button>
              </div>
              <div class="flex gap-2 items-center pt-1 px-1">
                <span class="text-[9px] text-slate-400 font-bold uppercase w-12 shrink-0" title="Dán tọa độ Google Maps vào đây">Tọa độ:</span>
                <input :value="form.arrivalLat && form.arrivalLng ? `${form.arrivalLat}, ${form.arrivalLng}` : ''" @input="e => parseCoordinates(e.target.value, 'arrival')" type="text" placeholder="Dán tọa độ (VD: 13.092, 109.293)" class="w-full bg-slate-50 hover:bg-white border border-slate-200 text-[11px] font-mono p-2 rounded-lg focus:border-[#075955] outline-none transition-colors shadow-sm" />
              </div>

              <div v-if="showToDropdown" class="absolute left-0 right-0 top-[100%] mt-2 bg-white border border-slate-200 shadow-2xl rounded-2xl z-[1000] overflow-hidden">
                <div v-if="savedLocationsLoading" class="px-5 py-4 text-xs font-bold text-slate-400 flex items-center gap-2">
                  <span class="material-symbols-outlined text-base animate-spin">progress_activity</span> Đang tải vị trí đã lưu
                </div>
                <div v-else-if="savedLocationsError" class="px-5 py-4 text-xs font-bold text-rose-500">{{ savedLocationsError }}</div>
                <ul v-else-if="toSuggestions.length" class="max-h-56 overflow-y-auto">
                  <li v-for="loc in toSuggestions" :key="loc.id" @mousedown.prevent="selectSavedLocation(loc, 'arrival')" class="px-5 py-3 hover:bg-emerald-50 cursor-pointer border-b border-slate-50 last:border-0">
                    <strong class="block text-xs text-slate-700">{{ loc.name }}</strong>
                  </li>
                </ul>
                <div v-else class="px-5 py-4 text-xs text-slate-400">
                  Không tìm thấy vị trí phù hợp trong danh mục.
                </div>
              </div>
            </div>

            <div class="space-y-1.5 relative">
              <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 flex justify-between">
                <span>Giá cơ bản (VNĐ)</span>
              </label>
              <div class="relative flex items-center">
                <input 
                  v-model.number="form.basePrice"
                  type="number"
                  placeholder="VD: 100000" 
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3.5 text-[15px] font-bold outline-none transition-all font-mono" 
                />
              </div>
            </div>

            <div class="space-y-1.5 relative">
              <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1 flex justify-between">
                <span>Ảnh đại diện tuyến (Không bắt buộc)</span>
              </label>
              <div class="relative flex items-center gap-2">
                <div class="relative flex-1 flex items-center">
                  <input 
                    v-model="form.imageUrl" 
                    placeholder="Nhập đường dẫn ảnh hoặc tải lên ➔" 
                    class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl pl-4 pr-12 py-3.5 text-sm font-bold outline-none transition-all" 
                  />
                  <div v-if="form.imageUrl" class="absolute right-2 w-9 h-9 rounded-lg overflow-hidden border border-slate-200 bg-white">
                     <img :src="form.imageUrl" class="w-full h-full object-cover" @error="(e) => e.target.style.display='none'" />
                  </div>
                </div>
                <button 
                  type="button"
                  @click="fileInput.click()"
                  class="px-4 py-3 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl flex items-center justify-center transition-all active:scale-95 border border-slate-200 shrink-0"
                  :disabled="uploading"
                  title="Tải ảnh lên Cloudinary"
                >
                  <span :class="['material-symbols-outlined', uploading ? 'animate-spin' : '']">{{ uploading ? 'sync' : 'upload_file' }}</span>
                </button>
              </div>
            </div>

            </section>

            <RouteBusTypeSelector
              v-model="allowedBusTypeIds"
              v-model:unrestricted="unrestrictedBusTypes"
              v-model:default-bus-type-id="defaultBusTypeId"
              :bus-types="busTypes"
              :loading="busTypeConfigLoading"
            />

            <RouteVehicleSelector
              v-model:primary-bus-ids="primaryBusIds"
              v-model:backup-bus-ids="backupBusIds"
              v-model:unrestricted="unrestrictedVehicles"
              :buses="buses"
              :bus-types="busTypes"
              :allowed-bus-type-ids="allowedBusTypeIds"
              :unrestricted-bus-types="unrestrictedBusTypes"
              :loading="vehicleConfigLoading"
            />

          </form>
          <footer class="route-form-footer flex shrink-0 items-center justify-between gap-3">
            <p class="hidden text-[11px] font-medium text-slate-500 lg:block">Các trường có dấu * là bắt buộc</p>
            <div class="ml-auto flex items-center gap-3">
              <button type="button" @click="closeModal" class="route-cancel-button">Hủy bỏ</button>
              <button type="submit" form="route-editor-form" class="route-save-button">
                <span class="material-symbols-outlined text-base">save</span>
                Lưu tuyến đường
              </button>
            </div>
          </footer>
          </div>

          <!-- Right: Map Preview Column -->
          <div class="route-map-pane bg-slate-50 relative flex flex-col border-l border-slate-100">
              <div class="route-map-summary absolute top-5 left-5 right-5 z-[1000] bg-white rounded-2xl p-4 border border-slate-200">
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
                         <span>Lộ trình ước tính: <strong class="text-[#075955] font-black">{{ form.duration }}</strong></span>
                       </div>
                     </div>
                     <div class="mt-2 text-[9px] italic text-slate-400">Mẹo: Click lên bản đồ để tinh chỉnh vị trí nếu cần</div>
                  </div>
                 <div v-else class="text-[10px] font-bold text-rose-500 flex flex-col gap-1 animate-pulse">
                    <div class="flex items-center gap-1">
                      <span class="material-symbols-outlined text-[12px]">warning</span> Chờ xác định tọa độ...
                    </div>
                    <div class="text-[9px] text-slate-400 font-normal">Hãy nhập địa chỉ hoặc click trực tiếp lên bản đồ</div>
                 </div>
              </div>

             <div id="route-map" class="flex-1 w-full h-full"></div>
             
             <div v-if="mapLoading" class="absolute inset-0 bg-slate-900/10 backdrop-blur-[2px] flex flex-col items-center justify-center z-[1001]">
                <div class="w-10 h-10 border-4 border-[#075955] border-t-transparent rounded-full animate-spin"></div>
             </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- Hidden File Input for Image Upload (route template) -->
  <input type="file" ref="fileInput" class="hidden" accept="image/*" @change="handleImageUpload" />
</template>

<script setup>
import { computed, ref, watch, nextTick } from 'vue';
import axios from 'axios';
import { useApi } from '@/composables/useApi';
import { decodePolyline, uploadPolylineToCloudinary, fetchPolylineFromCloudinary } from '@/utils/polyline';
import RouteBusTypeSelector from './RouteBusTypeSelector.vue';
import { useRouteBusTypeApi } from '@/services/routeBusTypeApi';
import RouteVehicleSelector from './RouteVehicleSelector.vue';
import { useRouteVehicleApi } from '@/services/routeVehicleApi';

const props = defineProps({
  busTypes: { type: Array, default: () => [] },
  buses: { type: Array, default: () => [] }
});

const emit = defineEmits(['save']);

const isOpen = ref(false);
const editingIndex = ref(-1);
const form = ref({ name: '', departurePoint: '', arrivalPoint: '', departureLat: 0, departureLng: 0, arrivalLat: 0, arrivalLng: 0, duration: '', basePrice: null, imageUrl: '', routeData: '', isVisible: true });

const geocoding = ref({ departure: false, arrival: false });
const lastGeocodeTarget = ref('departure');
const mapLoading = ref(false);
const leafletMap = ref(null);
const fileInput = ref(null);
const uploading = ref(false);
const api = useApi();
const routeBusTypeApi = useRouteBusTypeApi();
const routeVehicleApi = useRouteVehicleApi();
const allowedBusTypeIds = ref([]);
const unrestrictedBusTypes = ref(true);
const defaultBusTypeId = ref(null);
const busTypeConfigLoading = ref(false);
const primaryBusIds = ref([]);
const backupBusIds = ref([]);
const unrestrictedVehicles = ref(true);
const vehicleConfigLoading = ref(false);
const savedLocations = ref([]);
const savedLocationsLoading = ref(false);
const savedLocationsError = ref('');
const showFromDropdown = ref(false);
const showToDropdown = ref(false);
const formScroll = ref(null);

const filterSavedLocations = query => {
  const normalizedQuery = String(query || '').trim().toLocaleLowerCase('vi-VN');
  if (!normalizedQuery) return savedLocations.value;
  return savedLocations.value.filter(location => [location.name, location.address]
    .filter(Boolean)
    .some(value => String(value).toLocaleLowerCase('vi-VN').includes(normalizedQuery)));
};

const fromSuggestions = computed(() => filterSavedLocations(form.value.departurePoint));
const toSuggestions = computed(() => filterSavedLocations(form.value.arrivalPoint));

watch(() => form.value.imageUrl, async (newVal) => {
  if (newVal && newVal.startsWith('data:image')) {
    uploading.value = true;
    try {
      const formData = new FormData();
      formData.append('file', newVal);
      formData.append('upload_preset', 'skybus_preset');
      const res = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', formData);
      form.value.imageUrl = res.data.secure_url;
    } catch (err) {
      console.error("Lỗi tự động upload Base64:", err);
      form.value.imageUrl = '';
      alert("Đoạn mã ảnh bạn vừa dán bị lỗi! Vui lòng dùng nút Upload hình tệp tin thay thế.");
    } finally {
      uploading.value = false;
    }
  }
});

const loadSavedLocations = async () => {
  savedLocationsLoading.value = true;
  savedLocationsError.value = '';
  try {
    const response = await api.get('/saved-locations');
    savedLocations.value = Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    savedLocationsError.value = error.response?.data?.message || 'Không tải được danh mục vị trí.';
  } finally {
    savedLocationsLoading.value = false;
  }
};

const openLocationDropdown = target => {
  lastGeocodeTarget.value = target;
  showFromDropdown.value = target === 'departure';
  showToDropdown.value = target === 'arrival';
  if (!savedLocations.value.length && !savedLocationsLoading.value) loadSavedLocations();
};

const closeLocationDropdown = target => {
  window.setTimeout(() => {
    if (target === 'departure') showFromDropdown.value = false;
    else showToDropdown.value = false;
  }, 120);
};

const selectSavedLocation = (location, target) => {
  const latitude = Number(location.latitude);
  const longitude = Number(location.longitude);
  if (target === 'departure') {
    form.value.departurePoint = location.name;
    form.value.departureLat = latitude;
    form.value.departureLng = longitude;
    showFromDropdown.value = false;
  } else {
    form.value.arrivalPoint = location.name;
    form.value.arrivalLat = latitude;
    form.value.arrivalLng = longitude;
    showToDropdown.value = false;
  }
  lastGeocodeTarget.value = target;
  form.value.routeData = '';
  updateMap();
};

const loadBusTypeConfig = async routeId => {
  allowedBusTypeIds.value = [];
  unrestrictedBusTypes.value = true;
  defaultBusTypeId.value = null;
  if (!routeId) return;

  busTypeConfigLoading.value = true;
  try {
    const response = await routeBusTypeApi.getConfig(routeId);
    const config = response.data;
    unrestrictedBusTypes.value = config.unrestricted !== false;
    allowedBusTypeIds.value = unrestrictedBusTypes.value
      ? []
      : (config.busTypes || []).map(busType => busType.id);
    defaultBusTypeId.value = config.defaultBusTypeId || allowedBusTypeIds.value[0] || null;
  } catch (error) {
    console.error('Không tải được cấu hình dòng xe của tuyến:', error);
  } finally {
    busTypeConfigLoading.value = false;
  }
};

const loadVehicleConfig = async routeId => {
  primaryBusIds.value = [];
  backupBusIds.value = [];
  unrestrictedVehicles.value = true;
  if (!routeId) return;

  vehicleConfigLoading.value = true;
  try {
    const response = await routeVehicleApi.getConfig(routeId);
    const config = response.data;
    unrestrictedVehicles.value = config.unrestricted !== false;
    primaryBusIds.value = (config.vehicles || [])
      .filter(vehicle => vehicle.role === 'PRIMARY')
      .map(vehicle => vehicle.busId);
    backupBusIds.value = (config.vehicles || [])
      .filter(vehicle => vehicle.role === 'BACKUP')
      .map(vehicle => vehicle.busId);
  } catch (error) {
    console.error('Không tải được nhóm xe của tuyến:', error);
  } finally {
    vehicleConfigLoading.value = false;
  }
};

const openModal = (route, idx = -1) => {
  editingIndex.value = idx;
  if (route) {
    const initialName = route.name && !route.name.includes('➔') ? route.name : '';
    form.value = { ...route, name: initialName, imageUrl: route.imageUrl || '', routeData: route.routeData || '', isVisible: route.isVisible !== false };
  } else {
    form.value = { name: '', departurePoint: '', arrivalPoint: '', departureLat: 0, departureLng: 0, arrivalLat: 0, arrivalLng: 0, duration: '', basePrice: null, imageUrl: '', routeData: '', isVisible: true };
  }
  isOpen.value = true;
  nextTick(() => {
    if (formScroll.value) formScroll.value.scrollTop = 0;
  });
  loadBusTypeConfig(route?.id);
  loadVehicleConfig(route?.id);
  loadSavedLocations();
  initMap();
};

const closeModal = () => {
  isOpen.value = false;
};

const swapRoute = () => {
  const tempPoint = form.value.departurePoint;
  const tempLat = form.value.departureLat;
  const tempLng = form.value.departureLng;
  form.value.departurePoint = form.value.arrivalPoint;
  form.value.departureLat = form.value.arrivalLat;
  form.value.departureLng = form.value.arrivalLng;
  form.value.arrivalPoint = tempPoint;
  form.value.arrivalLat = tempLat;
  form.value.arrivalLng = tempLng;
  form.value.routeData = '';
  updateMap();
};

const parseCoordinates = (val, target) => {
  if (!val) return;
  const parts = val.split(',');
  if (parts.length >= 2) {
    const lat = parseFloat(parts[0].trim());
    const lng = parseFloat(parts[1].trim());
    if (!isNaN(lat) && !isNaN(lng)) {
      if (target === 'departure') {
        form.value.departureLat = lat;
        form.value.departureLng = lng;
      } else {
        form.value.arrivalLat = lat;
        form.value.arrivalLng = lng;
      }
      form.value.routeData = ''; 
      updateMap();
    }
  }
};

const saveRoute = () => {
  if (!unrestrictedBusTypes.value && !allowedBusTypeIds.value.length) {
    return alert('Vui lòng chọn ít nhất một dòng xe được phép khai thác.');
  }
  if (!unrestrictedVehicles.value && !primaryBusIds.value.length && !backupBusIds.value.length) {
    return alert('Vui lòng gán ít nhất một biển số xe cho tuyến.');
  }
  if (!form.value.departurePoint || !form.value.arrivalPoint) return alert('Vui lòng nhập điểm đi và điểm đến!');
  
  const shortDep = form.value.departurePoint.split(',')[0].trim();
  const shortArr = form.value.arrivalPoint.split(',')[0].trim();
  const finalName = form.value.name ? form.value.name.trim() : `${shortDep} ➔ ${shortArr}`;

  const newRoute = {
    name: finalName,
    departurePoint: form.value.departurePoint,
    arrivalPoint: form.value.arrivalPoint,
    departureLat: form.value.departureLat || 0,
    departureLng: form.value.departureLng || 0,
    arrivalLat: form.value.arrivalLat || 0,
    arrivalLng: form.value.arrivalLng || 0,
    duration: form.value.duration,
    basePrice: form.value.basePrice || 0,
    imageUrl: form.value.imageUrl || '',
    routeData: form.value.routeData || '',
    isVisible: form.value.isVisible !== false
  };
  
  emit('save', {
    route: newRoute,
    index: editingIndex.value,
    formImageUrl: form.value.imageUrl,
    shortDep,
    shortArr,
    busTypeConfig: {
      unrestricted: unrestrictedBusTypes.value,
      busTypeIds: unrestrictedBusTypes.value ? [] : allowedBusTypeIds.value,
      defaultBusTypeId: unrestrictedBusTypes.value
        ? null
        : (defaultBusTypeId.value || allowedBusTypeIds.value[0])
    },
    vehicleConfig: {
      unrestricted: unrestrictedVehicles.value,
      primaryBusIds: unrestrictedVehicles.value ? [] : primaryBusIds.value,
      backupBusIds: unrestrictedVehicles.value ? [] : backupBusIds.value
    }
  });
  closeModal();
};

const handleImageUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;
  
  uploading.value = true;
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', 'skybus_preset');
  
  try {
    const res = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', formData);
    form.value.imageUrl = res.data.secure_url;
  } catch (err) {
    console.error("Lỗi tải ảnh Cloudinary:", err);
    alert("Không thể tải ảnh lên! Vui lòng kiểm tra kết nối mạng.");
  } finally {
    uploading.value = false;
  }
};

const autoGeocode = async (address, target) => {
  if (!address || address.length < 3) return;
  geocoding.value[target] = true;
  lastGeocodeTarget.value = target;
  
  try {
    const viewbox = "102.1,8.5,109.5,23.4"; 
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}&limit=3&countrycodes=vn&viewbox=${viewbox}&bounded=1`;
    const response = await fetch(url);
    const data = await response.json();
    
    if (data && data.length > 0) {
      const { lat, lon } = data[0];
      if (target === 'departure') { form.value.departureLat = parseFloat(lat); form.value.departureLng = parseFloat(lon); } 
      else { form.value.arrivalLat = parseFloat(lat); form.value.arrivalLng = parseFloat(lon); }
      form.value.routeData = '';
      updateMap();
    }
  } catch (err) { 
    console.error(err); 
  } finally { 
    geocoding.value[target] = false; 
  }
};

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

const renderLeaflet = () => {
  const L = window.L; if (!L) return;
  nextTick(() => {
    const container = document.getElementById('route-map');
    if (!container) return;
    if (leafletMap.value) leafletMap.value.remove();
    leafletMap.value = L.map(container).setView([16.0, 108.0], 6);
    L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
      maxZoom: 20, subdomains: ['mt0', 'mt1', 'mt2', 'mt3'], attribution: '© Google Maps'
    }).addTo(leafletMap.value);
    
    leafletMap.value.on('click', (e) => {
      const { lat, lng } = e.latlng;
      if (lastGeocodeTarget.value === 'departure') { form.value.departureLat = lat; form.value.departureLng = lng; }
      else { form.value.arrivalLat = lat; form.value.arrivalLng = lng; }
      form.value.routeData = '';
      updateMap();
    });
    setTimeout(() => { if (leafletMap.value) { leafletMap.value.invalidateSize(); updateMap(); } }, 500);
  });
};

const updateMap = async () => {
  const L = window.L; if (!L || !leafletMap.value) return;
  leafletMap.value.eachLayer((layer) => { if (layer instanceof L.Marker || layer instanceof L.Polyline) leafletMap.value.removeLayer(layer); });
  
  const from = [form.value.departureLat, form.value.departureLng];
  const to = [form.value.arrivalLat, form.value.arrivalLng];
  
  const startIcon = L.divIcon({ html: `<div class="w-8 h-8 bg-emerald-500 border-4 border-white rounded-full shadow-2xl flex items-center justify-center text-white"><span class="material-symbols-outlined text-sm">trip_origin</span></div>`, iconSize: [32, 32] });
  const endIcon = L.divIcon({ html: `<div class="w-8 h-8 bg-rose-600 border-4 border-white rounded-full shadow-2xl flex items-center justify-center text-white"><span class="material-symbols-outlined text-sm">location_on</span></div>`, iconSize: [32, 32] });

  if (from[0] > 1 && to[0] > 1) {
    L.marker(from, { icon: startIcon }).addTo(leafletMap.value);
    L.marker(to, { icon: endIcon }).addTo(leafletMap.value);
    
    const fallbackLine = L.polyline([from, to], { color: '#075955', weight: 2, dashArray: '5, 10', opacity: 0.5 }).addTo(leafletMap.value);
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
           L.polyline(coords, { 
             color: '#075955', weight: 7, opacity: 0.85, lineJoin: 'round', dashArray: '1, 12', lineCap: 'round'
           }).addTo(leafletMap.value);
           L.polyline(coords, { color: '#000', weight: 8, opacity: 0.1 }).addTo(leafletMap.value);
           leafletMap.value.fitBounds(coords, { padding: [100, 100] });
           return;
         }
       } catch (e) { console.error("Lỗi parse routeData", e); }
    }

    mapLoading.value = true;
    
    let points = `${from[1]},${from[0]};${to[1]},${to[0]}`;
    const latDiff = Math.abs(from[0] - to[0]);
    const isSouthBound = from[0] > to[0];
    
    if (latDiff > 2) {
      let waypoints = [];
      const minLat = Math.min(from[0], to[0]);
      const maxLat = Math.max(from[0], to[0]);
      
      if (minLat < 13.0 && maxLat > 13.0) waypoints.push([109.2887, 13.0645]);
      if (minLat < 15.1 && maxLat > 15.1) waypoints.push([108.8268, 15.1522]);
      if (minLat < 17.5 && maxLat > 17.5) waypoints.push([106.5960, 17.4912]);
      
      if (isSouthBound) waypoints.reverse();
      
      if (waypoints.length > 0) {
         const waypointsStr = waypoints.map(wp => `${wp[0]},${wp[1]}`).join(';');
         points = `${from[1]},${from[0]};${waypointsStr};${to[1]},${to[0]}`;
      }
    }

    const osrmUrl = `https://router.project-osrm.org/route/v1/driving/${points}?overview=full&geometries=polyline&steps=true`;
    
    fetch(osrmUrl)
      .then(r => r.json())
      .then(data => {
        if (data && data.code === 'Ok' && data.routes && data.routes.length > 0) {
          const distanceKm = Math.round(data.routes[0].distance / 1000);
          const coachDurationSeconds = data.routes[0].duration * 1.35;
          const hours = Math.floor(coachDurationSeconds / 3600);
          const minutes = Math.round((coachDurationSeconds % 3600) / 60);
          form.value.duration = `${hours}h ${minutes}m (${distanceKm} Km)`;

          leafletMap.value.removeLayer(fallbackLine);
          
          const encodedPolyline = data.routes[0].geometry;
          
          uploadPolylineToCloudinary(encodedPolyline).then(url => {
              form.value.routeData = url;
          });
          
          const coords = decodePolyline(encodedPolyline);
          
          L.polyline(coords, { 
            color: '#075955', weight: 7, opacity: 0.85, lineJoin: 'round', dashArray: '1, 12', lineCap: 'round'
          }).addTo(leafletMap.value);
          L.polyline(coords, { color: '#000', weight: 8, opacity: 0.1 }).addTo(leafletMap.value);
          leafletMap.value.fitBounds(coords, { padding: [100, 100] });
        } else {
           console.warn("OSRM returned NoRoute. Falling back to straight line.");
        }
      })
      .catch(err => {
        console.error("Lộ trình road-path bị lỗi, dùng đường chim bay làm dự phòng.", err);
      })
      .finally(() => { mapLoading.value = false; });
  } else if (from[0] > 1) { leafletMap.value.setView(from, 13); L.marker(from, { icon: startIcon }).addTo(leafletMap.value); }
    else if (to[0] > 1) { leafletMap.value.setView(to, 13); L.marker(to, { icon: endIcon }).addTo(leafletMap.value); }
};

defineExpose({
  openModal,
  closeModal
});
</script>

<style scoped>
@keyframes scaleUp {
  from { opacity: 0; transform: scale(0.98) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.route-modal-shell {
  width: min(94vw, 1320px);
  height: min(94dvh, 920px);
  border-radius: 1.5rem;
  box-shadow: 0 32px 90px rgba(4, 47, 44, 0.28);
}

.route-modal-header {
  min-height: 76px;
  background: #075955;
  border-bottom: 1px solid rgba(255, 255, 255, 0.14);
}

.route-header-icon {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 13px;
  background: rgba(255, 255, 255, 0.12);
}

.route-close-button {
  width: 38px;
  height: 38px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  transition: background-color 180ms ease, transform 180ms ease;
}

.route-close-button:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: rotate(4deg);
}

.route-form-pane {
  width: 54%;
}

.route-map-pane {
  width: 46%;
  min-width: 0;
  box-shadow: inset 1px 0 rgba(15, 88, 83, 0.08);
}

.route-map-summary {
  box-shadow: 0 16px 36px rgba(7, 66, 62, 0.16);
  backdrop-filter: blur(12px);
}

.route-form-scroll {
  scrollbar-color: #9bbcb8 transparent;
  scrollbar-width: thin;
}

.route-section-card {
  border: 1px solid #dce7e5;
  border-radius: 18px;
  background: #fff;
  padding: 1.25rem;
  box-shadow: 0 8px 24px rgba(18, 78, 73, 0.055);
}

.route-section-heading {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-bottom: 0.9rem;
  border-bottom: 1px solid #e8efee;
}

.route-section-heading > span {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 11px;
  background: #e8f4f1;
  color: #075955;
  font-size: 19px;
}

.route-section-heading h4 {
  color: #173b38;
  font-size: 13px;
  font-weight: 800;
}

.route-section-heading p {
  margin-top: 2px;
  color: #7b928f;
  font-size: 10px;
  font-weight: 600;
}

.route-form-footer {
  min-height: 72px;
  padding: 0.85rem 1.75rem;
  border-top: 1px solid #dce7e5;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 -12px 30px rgba(27, 75, 71, 0.06);
}

.route-cancel-button,
.route-save-button {
  min-height: 42px;
  border-radius: 11px;
  padding: 0 1.15rem;
  font-size: 11px;
  font-weight: 800;
  transition: transform 180ms ease, box-shadow 180ms ease, background-color 180ms ease;
}

.route-cancel-button {
  border: 1px solid #d6e1df;
  background: #fff;
  color: #536b68;
}

.route-cancel-button:hover {
  background: #f4f7f6;
  color: #173b38;
}

.route-save-button {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: #075955;
  color: #fff;
  box-shadow: 0 8px 20px rgba(7, 89, 85, 0.2);
}

.route-save-button:hover {
  background: #064b48;
  box-shadow: 0 10px 24px rgba(7, 89, 85, 0.28);
  transform: translateY(-1px);
}

.route-cancel-button:active,
.route-save-button:active {
  transform: translateY(1px) scale(0.99);
}

@media (max-width: 900px) {
  .route-modal-shell {
    width: 96vw;
    height: 94dvh;
  }

  .route-form-pane {
    width: 58%;
    padding: 1.5rem;
  }

  .route-map-pane {
    width: 42%;
  }
}
</style>
