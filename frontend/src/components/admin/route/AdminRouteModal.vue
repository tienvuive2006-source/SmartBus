<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[9999] bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div class="bg-white rounded-3xl w-full max-w-6xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col max-h-[90vh] animate-scale-up">
        <div class="p-6 bg-[#075955] text-white flex justify-between items-center shrink-0">
          <div class="flex items-center gap-3">
           <span class="material-symbols-outlined">{{ editingIndex >= 0 ? 'edit_square' : 'add_circle' }}</span>
             <h3 class="text-sm font-black uppercase tracking-widest">{{ editingIndex >= 0 ? 'Chỉnh sửa tuyến đường ' : 'Tạo tuyến đường ' }}</h3>
          </div>
          <button @click="closeModal" class="hover:rotate-90 transition-transform bg-white/10 p-1.5 rounded-full flex items-center justify-center">
            <span class="material-symbols-outlined text-sm">close</span>
          </button>
        </div>
        
        <div class="flex-1 flex overflow-hidden">
          <!-- Left: Form Column -->
          <form @submit.prevent="saveRoute" class="w-1/2 p-8 space-y-6 overflow-y-auto border-r border-slate-100 bg-white relative">
            <div class="space-y-1.5 relative">
              <label class="text-[10px] font-black text-slate-500 uppercase tracking-wider ml-1">
                <span>Tên tuyến đường (Tùy chọn)</span>
              </label>
              <div class="relative flex items-center">
                <input 
                  v-model="form.name" 
                  placeholder="VD: Tuyến Cao Nguyên, Tuyến Biển..." 
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all" 
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
                  @focus="onFromFocus" 
                  @keyup.enter="autoGeocode(form.departurePoint, 'departure')"
                  required placeholder="Ví dụ: Bến xe Đà Nẵng" 
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl pl-4 pr-10 py-3.5 text-sm font-bold outline-none transition-all" 
                />
                <button type="button" @click="autoGeocode(form.departurePoint, 'departure')" class="absolute right-3 text-slate-400 hover:text-[#075955] p-1">
                  <span class="material-symbols-outlined text-lg">my_location</span>
                </button>
              </div>
              <div class="flex gap-2 items-center pt-1 px-1">
                <span class="text-[9px] text-slate-400 font-bold uppercase w-12 shrink-0" title="Dán tọa độ Google Maps vào đây">Tọa độ:</span>
                <input :value="form.departureLat && form.departureLng ? `${form.departureLat}, ${form.departureLng}` : ''" @input="e => parseCoordinates(e.target.value, 'departure')" type="text" placeholder="Dán tọa độ (VD: 13.092, 109.293)" class="w-full bg-slate-50 hover:bg-white border border-slate-200 text-[11px] font-mono p-2 rounded-lg focus:border-[#075955] outline-none transition-colors shadow-sm" />
              </div>

              <ul v-if="showFromDropdown && fromSuggestions.length" class="absolute left-0 right-0 top-[100%] mt-2 bg-white border border-slate-200 shadow-2xl rounded-2xl z-[1000] overflow-hidden max-h-48 overflow-y-auto">
                <li v-for="loc in fromSuggestions" :key="loc" @click="selectFromLocation(loc)" class="px-5 py-3 hover:bg-slate-50 cursor-pointer text-xs font-bold text-slate-700 border-b border-slate-50 last:border-0">
                  {{ loc }}
                </li>
              </ul>
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
                  @focus="onToFocus" 
                  @keyup.enter="autoGeocode(form.arrivalPoint, 'arrival')"
                  required placeholder="Ví dụ: Bến xe Miền Tây" 
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl pl-4 pr-10 py-3.5 text-sm font-bold outline-none transition-all" 
                />
                <button type="button" @click="autoGeocode(form.arrivalPoint, 'arrival')" class="absolute right-3 text-slate-400 hover:text-[#075955] p-1">
                  <span class="material-symbols-outlined text-lg">my_location</span>
                </button>
              </div>
              <div class="flex gap-2 items-center pt-1 px-1">
                <span class="text-[9px] text-slate-400 font-bold uppercase w-12 shrink-0" title="Dán tọa độ Google Maps vào đây">Tọa độ:</span>
                <input :value="form.arrivalLat && form.arrivalLng ? `${form.arrivalLat}, ${form.arrivalLng}` : ''" @input="e => parseCoordinates(e.target.value, 'arrival')" type="text" placeholder="Dán tọa độ (VD: 13.092, 109.293)" class="w-full bg-slate-50 hover:bg-white border border-slate-200 text-[11px] font-mono p-2 rounded-lg focus:border-[#075955] outline-none transition-colors shadow-sm" />
              </div>

              <ul v-if="showToDropdown && toSuggestions.length" class="absolute left-0 right-0 top-[100%] mt-2 bg-white border border-slate-200 shadow-2xl rounded-2xl z-[1000] overflow-hidden max-h-48 overflow-y-auto">
                <li v-for="loc in toSuggestions" :key="loc" @click="selectToLocation(loc)" class="px-5 py-3 hover:bg-slate-50 cursor-pointer text-xs font-bold text-slate-700 border-b border-slate-50 last:border-0">
                  {{ loc }}
                </li>
              </ul>
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
                  class="w-full border-2 border-slate-100 focus:border-[#075955] bg-slate-50 rounded-xl px-4 py-3 text-sm font-bold outline-none transition-all font-mono" 
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

            <div class="pt-10 flex justify-end gap-3 shrink-0 mt-auto">
              <button type="button" @click="closeModal" class="px-8 py-3 text-xs font-black uppercase text-slate-400 hover:text-slate-900 transition-colors">Hủy bỏ</button>
              <button type="submit" class="bg-[#075955] text-white px-12 py-3.5 rounded-xl text-xs font-black uppercase tracking-widest shadow-xl hover:shadow-[#075955]/20 active:scale-95 transition-all">Lưu & Lưu mẫu</button>
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
import { ref, watch, nextTick } from 'vue';
import axios from 'axios';
import { useLocationSearch } from '@/composables/useLocationSearch';
import { decodePolyline, uploadPolylineToCloudinary, fetchPolylineFromCloudinary } from '@/utils/polyline';

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

const { suggestions: fromSuggestions, showDropdown: showFromDropdown, handleFocus: onFromFocus, handleSelect: fromSelect, performSearch: searchFrom } = useLocationSearch();
const { suggestions: toSuggestions, showDropdown: showToDropdown, handleFocus: onToFocus, handleSelect: toSelect, performSearch: searchTo } = useLocationSearch();

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

watch(() => form.value.departurePoint, (v) => { searchFrom(v); });
watch(() => form.value.arrivalPoint, (v) => { searchTo(v); });

const selectFromLocation = (loc) => fromSelect(loc, (val) => { 
  form.value.departurePoint = val; 
  autoGeocode(val, 'departure'); 
});
const selectToLocation = (loc) => toSelect(loc, (val) => { 
  form.value.arrivalPoint = val; 
  autoGeocode(val, 'arrival'); 
});

const openModal = (route, idx = -1) => {
  editingIndex.value = idx;
  if (route) {
    const initialName = route.name && !route.name.includes('➔') ? route.name : '';
    form.value = { ...route, name: initialName, imageUrl: route.imageUrl || '', routeData: route.routeData || '', isVisible: route.isVisible !== false };
  } else {
    form.value = { name: '', departurePoint: '', arrivalPoint: '', departureLat: 0, departureLng: 0, arrivalLat: 0, arrivalLng: 0, duration: '', basePrice: null, imageUrl: '', routeData: '', isVisible: true };
  }
  isOpen.value = true;
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
  
  emit('save', { route: newRoute, index: editingIndex.value, formImageUrl: form.value.imageUrl, shortDep, shortArr });
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

const stationCoordinates = {
  "Bến xe Miền Đông, Hồ Chí Minh": [10.8164, 106.7118],
  "Bến xe Miền Tây, Hồ Chí Minh": [10.7516, 106.6174],
  "Bến xe An Sương, Hồ Chí Minh": [10.8492, 106.6231],
  "Bến xe Trung tâm Đà Nẵng": [16.0678, 108.1884],
  "Bến xe Phía Nam Nha Trang": [12.2472, 109.1678],
  "Bến xe Quy Nhơn, Bình Định": [13.7592, 109.2131],
  "Bến xe Vinh": [18.6667, 105.6667],
  "Bến xe Mỹ Đình, Hà Nội": [21.0286, 105.7797],
  "Bến xe Giáp Bát, Hà Nội": [20.9858, 105.8431],
};

const autoGeocode = async (address, target) => {
  if (!address || address.length < 3) return;
  geocoding.value[target] = true;
  lastGeocodeTarget.value = target;
  
  const normalizedAddress = address.trim();
  if (stationCoordinates[normalizedAddress]) {
    const [lat, lng] = stationCoordinates[normalizedAddress];
    if (target === 'departure') { form.value.departureLat = lat; form.value.departureLng = lng; } 
    else { form.value.arrivalLat = lat; form.value.arrivalLng = lng; }
    geocoding.value[target] = false;
    updateMap();
    return;
  }
  
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
</style>
