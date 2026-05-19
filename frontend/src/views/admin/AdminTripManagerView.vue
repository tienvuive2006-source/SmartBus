<template>
  <div class="p-6 md:p-10 max-w-7xl mx-auto bg-slate-50 min-h-screen font-sans animate-fade-in">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-8 mb-10 border-b border-slate-200 pb-8">
      <div class="flex items-center gap-5">
        <div class="w-16 h-16 bg-[#075955] rounded-3xl flex items-center justify-center shadow-lg shadow-[#075955]/20">
          <span class="material-symbols-outlined text-white text-4xl">alt_route</span>
        </div>
        <div>
          <h2 class="text-3xl font-black text-slate-900 tracking-tight">
            Quản lý Chuyến xe
          </h2>
          <p class="text-sm font-bold text-slate-400 mt-1 uppercase tracking-widest">Hệ thống điều hành lộ trình toàn quốc</p>
        </div>
      </div>
      <button 
        @click="openAddModal" 
        class="bg-[#075955] text-white hover:bg-[#0a7a75] px-10 py-4 rounded-2xl shadow-xl shadow-[#075955]/10 active:scale-95 transition-all duration-300 font-black text-xs uppercase tracking-widest flex items-center justify-center gap-3"
      >
        <span class="material-symbols-outlined">add_circle</span>
        Tạo lộ trình mới
      </button>
    </div>

    <!-- 1. Stats Dashboard -->
    <TripStats 
      :totalTrips="totalTrips"
      :todayTrips="todayTrips"
      :totalEmptySeats="totalEmptySeats"
      :averagePrice="averagePrice"
    />

    <!-- 2. Main List -->
    <TripTable 
      :trips="trips"
      @edit="openEditModal"
      @delete="deleteTrip"
    />

    <!-- 3. Edit Modal -->
    <TripModal 
      :isOpen="isModalOpen"
      :isEditMode="isEditMode"
      :form="form"
      :busTypes="busTypes"
      :geocoding="geocoding"
      :mapLoading="adminMapLoading"
      @close="closeModal"
      @submit="handleFormSubmit"
      @geocode="autoGeocode"
      @from-focus="onFromFocus"
      @to-focus="onToFocus"
      @upload-click="$refs.fileInput?.click()"
      @swap-route="swapRoute"
    >
      <template #from-suggestions>
        <ul v-if="showFromDropdown && fromSuggestions.length" class="absolute left-0 right-0 top-full mt-2 bg-white border border-slate-200 shadow-2xl rounded-2xl z-[1000] overflow-hidden max-h-48 overflow-y-auto">
          <li v-for="loc in fromSuggestions" :key="loc" @click="selectFromLocation(loc)" class="px-5 py-3 hover:bg-slate-50 cursor-pointer text-xs font-bold text-slate-700 border-b border-slate-50 last:border-0">
            {{ loc }}
          </li>
        </ul>
      </template>
      <template #to-suggestions>
        <ul v-if="showToDropdown && toSuggestions.length" class="absolute left-0 right-0 top-full mt-2 bg-white border border-slate-200 shadow-2xl rounded-2xl z-[1000] overflow-hidden max-h-48 overflow-y-auto">
          <li v-for="loc in toSuggestions" :key="loc" @click="selectToLocation(loc)" class="px-5 py-3 hover:bg-slate-50 cursor-pointer text-xs font-bold text-slate-700 border-b border-slate-50 last:border-0">
            {{ loc }}
          </li>
        </ul>
      </template>
    </TripModal>

    <!-- Hidden File Input for Modal -->
    <input type="file" ref="fileInput" class="hidden" accept="image/*" @change="handleImageUpload" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import axios from 'axios';
import { useApi } from '../../composables/useApi';
import { useLocationSearch } from '../../composables/useLocationSearch';
import TripStats from '../../components/admin/trip/TripStats.vue';
import TripTable from '../../components/admin/trip/TripTable.vue';
import TripModal from '../../components/admin/trip/TripModal.vue';

const api = useApi();
const trips = ref([]);
const busTypes = ref([]);
const isModalOpen = ref(false);
const isEditMode = ref(false);
const adminMapLoading = ref(false);
const leafletMap = ref(null);
const fileInput = ref(null);

const defaultForm = {
  id: null, companyName: 'Saomaifly', busType: 'Luxury', departurePoint: '', arrivalPoint: '',
  departureDate: new Date().toISOString().split('T')[0], departureTime: '08:00', arrivalTime: '12:00',
  duration: '4h', price: 250000, rating: 4.8, availableSeats: 36, imageUrl: '', instantConfirmation: true,
  departureLat: 0, departureLng: 0, arrivalLat: 0, arrivalLng: 0
};

const form = ref({ ...defaultForm });
const geocoding = ref({ departure: false, arrival: false });
const lastGeocodeTarget = ref('departure');

// --- 🛣️ TỰ ĐỘNG TÍNH TOÀN KM & GIỜ ĐẾN (CHỈ SỐ THỜI GIAN THỰC) ---
const currentDurationSeconds = ref(0);
const currentDistanceMeters = ref(0);

const updateArrivalAndDuration = () => {
  if (currentDurationSeconds.value <= 0) return;
  
  // 🇻🇳 HỆ SỐ ĐIỀU CHỈNH XE KHÁCH TẠI VIỆT NAM (1.35x)
  // OSRM mặc định tính theo tốc độ tối ưu của xe con (vd: phóng 120km/h cao tốc).
  // Với xe giường nằm lớn, giới hạn tốc độ thấp hơn, cộng thêm thời gian dừng chân trạm nghỉ, đón trả khách
  // và kẹt xe đô thị, hệ số chuẩn thực tế Việt Nam là 1.35x (tương đương tốc độ trung bình 60-65km/h).
  const coachDurationSeconds = currentDurationSeconds.value * 1.35;
  const distanceKm = Math.round(currentDistanceMeters.value / 1000);
  
  const hours = Math.floor(coachDurationSeconds / 3600);
  const minutes = Math.round((coachDurationSeconds % 3600) / 60);
  
  let durationStr = '';
  if (hours > 0) durationStr += `${hours}h`;
  if (minutes > 0) durationStr += ` ${minutes}m`;
  durationStr = durationStr.trim() || '1h';
  
  form.value.duration = `${durationStr} (${distanceKm} Km)`;
  
  // 2. Tính toán Giờ đến tự động
  const depTime = form.value.departureTime || '08:00';
  const timeRegex = /^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/;
  if (!timeRegex.test(depTime)) return;
  
  const [depHour, depMin] = depTime.split(':').map(Number);
  const totalDepMinutes = depHour * 60 + depMin;
  
  const durationMinutes = Math.round(coachDurationSeconds / 60);
  const totalArrMinutes = (totalDepMinutes + durationMinutes) % (24 * 60);
  
  const arrHour = Math.floor(totalArrMinutes / 60);
  const arrMin = totalArrMinutes % 60;
  
  form.value.arrivalTime = `${String(arrHour).padStart(2, '0')}:${String(arrMin).padStart(2, '0')}`;
};

watch(() => form.value.departureTime, () => {
  updateArrivalAndDuration();
});

// --- Stats Logic ---
const totalTrips = computed(() => trips.value.length);
const todayTrips = computed(() => {
  const today = new Date().toISOString().split('T')[0];
  return trips.value.filter(t => t.departureDate.startsWith(today)).length;
});
const totalEmptySeats = computed(() => {
  const sum = trips.value.reduce((acc, t) => acc + t.availableSeats, 0);
  const total = trips.value.reduce((acc, t) => acc + 36, 0); // Giả sử trung bình 36 ghế
  return total > 0 ? Math.round((sum / total) * 100) : 0;
});
const averagePrice = computed(() => {
  if (trips.value.length === 0) return 0;
  return Math.round(trips.value.reduce((acc, t) => acc + t.price, 0) / trips.value.length);
});

// --- Location Search & Geocoding ---
const { suggestions: fromSuggestions, showDropdown: showFromDropdown, handleFocus: onFromFocus, handleSelect: fromSelect, performSearch: searchFrom } = useLocationSearch();
const { suggestions: toSuggestions, showDropdown: showToDropdown, handleFocus: onToFocus, handleSelect: toSelect, performSearch: searchTo } = useLocationSearch();

// 🚌 TỰ ĐỘNG CẬP NHẬT SỐ GHẾ KHI CHỌN DÒNG XE
const updateTripImage = () => {
  const selected = busTypes.value.find(t => t.name === form.value.busType);
  if (selected) {
    form.value.availableSeats = selected.seatCount;
    if (selected.imageUrl) {
      form.value.imageUrl = selected.imageUrl; 
    }
  }
};

watch(() => form.value.busType, () => {
  updateTripImage();
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
  
  updateMap();
};

let debounceTimer = null;
const debouncedGeocode = (val, target) => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => autoGeocode(val, target), 800);
};

const stationCoordinates = {
  "Bến xe Miền Đông, Hồ Chí Minh": [10.8164, 106.7118],
  "Bến xe Miền Tây, Hồ Chí Minh": [10.7516, 106.6174],
  "Bến xe An Sương, Hồ Chí Minh": [10.8492, 106.6231],
  "Bến xe Ngã Tư Ga, Hồ Chí Minh": [10.8524, 106.6806],
  "Bến xe Trung tâm Đà Nẵng": [16.0678, 108.1884],
  "Bến xe Đức Long, Gia Lai": [13.9647, 108.0161],
  "Bến xe Quy Nhơn, Bình Định": [13.7592, 109.2131],
  "Bến xe Bồng Sơn, Hoài Nhơn": [14.4258, 109.0195],
  "Bến xe Phía Nam Nha Trang": [12.2472, 109.1678],
  "Bến xe Phía Bắc Nha Trang": [12.2858, 109.1864],
  "Bến xe Cam Ranh": [11.9167, 109.15],
  "Bến xe Phan Thiết": [10.9333, 108.1],
  "Bến xe Vĩnh Long": [10.25, 105.9667],
  "Bến xe Trung tâm Cần Thơ": [9.9986, 105.7483],
  "Bến xe Rạch Sỏi, Kiên Giang": [9.9486, 105.1328],
  "Bến xe Hà Tiên": [10.3758, 104.4925],
  "Bến xe Vũng Tàu": [10.3547, 107.0944],
  "Bến xe Bà Rịa": [10.4939, 107.1681],
  "Bến xe Long Điền": [10.4753, 107.2183],
  "Bến xe Bến Tre": [10.2458, 106.3686],
  "Bến xe Trà Vinh": [9.9336, 106.3333],
  "Bến xe Cà Mau": [9.1914, 105.1486],
  "Bến xe Bạc Liêu": [9.2942, 105.7239],
  "Bến xe Sóc Trăng": [9.6017, 105.975],
  "Bến xe Cao Lãnh": [10.4578, 105.6322],
  "Bến xe Sa Đéc": [10.2917, 105.7583],
  "Bến xe Long Xuyên": [10.3831, 105.4225],
  "Bến xe Châu Đốc": [10.5667, 105.1167],
  "Bến xe Tây Ninh": [11.3167, 106.1],
  "Bến xe Đồng Xoài": [11.5333, 106.8833],
  "Bến xe Gia Nghĩa": [12.0167, 107.6833],
  "Bến xe Buôn Ma Thuột": [12.6828, 108.0531],
  "Bến xe Liên tỉnh Đà Lạt": [11.9331, 108.4419],
  "Bến xe Kon Tum": [14.3547, 108.0053],
  "Bến xe Tuy Hòa": [13.0894, 109.3031],
  "Bến xe Quảng Ngãi": [15.1214, 108.7944],
  "Bến xe Tam Kỳ, Quảng Nam": [15.5667, 108.4833],
  "Bến xe Phía Nam, Huế": [16.4461, 107.6031],
  "Bến xe Phía Bắc, Huế": [16.4864, 107.5678],
  "Bến xe Đồng Hới": [17.4667, 106.6167],
  "Bến xe Đông Hà, Quảng Trị": [16.8167, 107.1],
  "Bến xe Hà Tĩnh": [18.3333, 105.9],
  "Bến xe Vinh": [18.6667, 105.6667],
  "Bến xe Thanh Hóa": [19.8, 105.7833],
  "Bến xe Ninh Bình": [20.2539, 105.975],
  "Bến xe Thái Bình": [20.45, 106.3333],
  "Bến xe Nam Định": [20.4167, 106.1667],
  "Bến xe Phủ Lý": [20.5414, 105.9139],
  "Bến xe Hòa Bình": [20.8167, 105.3333],
  "Bến xe Mỹ Đình, Hà Nội": [21.0286, 105.7797],
  "Bến xe Giáp Bát, Hà Nội": [20.9858, 105.8431],
  "Bến xe Nước Ngầm, Hà Nội": [20.9753, 105.8394],
  "Bến xe Yên Nghĩa, Hà Nội": [20.9575, 105.7533],
  "Bến xe Gia Lâm, Hà Nội": [21.0422, 105.8797],
  "Bến xe Thượng Lý, Hải Phòng": [20.8719, 106.6631],
  "Bến xe Cầu Rào, Hải Phòng": [20.8286, 106.6975],
  "Bến xe Lạng Sơn": [21.85, 106.75],
  "Bến xe Cao Bằng": [22.6667, 106.25],
  "Bến xe Hà Giang": [22.8167, 104.9833],
  "Bến xe Lào Cai": [22.4833, 103.9667],
  "Bến xe Điện Biên Phủ": [21.3833, 103.0167]
};

const autoGeocode = async (address, target) => {
  if (!address || address.length < 3) return;
  geocoding.value[target] = true;
  lastGeocodeTarget.value = target;
  
  // ⚡ TRUY XUẤT NHANH TỌA ĐỘ BẾN XE PRESET (KHÔNG CẦN CHỜ API - TRÁNH RATE LIMIT & CORS)
  const normalizedAddress = address.trim();
  if (stationCoordinates[normalizedAddress]) {
    const [lat, lng] = stationCoordinates[normalizedAddress];
    if (target === 'departure') {
      form.value.departureLat = lat;
      form.value.departureLng = lng;
    } else {
      form.value.arrivalLat = lat;
      form.value.arrivalLng = lng;
    }
    geocoding.value[target] = false;
    updateMap();
    return;
  }
  
  try {
    // 🇻🇳 Rào chắn tọa độ Việt Nam cực kỳ nghiêm ngặt (từ Cà Mau đến Lạng Sơn)
    const viewbox = "102.1,8.5,109.5,23.4"; 
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}&limit=3&countrycodes=vn&viewbox=${viewbox}&bounded=1`;
    
    // 🌐 Sử dụng FETCH để tránh Axios Global Authorization CORS block
    const response = await fetch(url);
    const data = await response.json();
    
    if (data && data.length > 0) {
      // Ưu tiên kết quả có tên trùng khớp nhất hoặc nằm ở vị trí hợp lý
      let bestMatch = data[0];
      if (address.toLowerCase().includes('quy nhơn')) {
        const qn = data.find(d => d.display_name.toLowerCase().includes('quy nhơn'));
        if (qn) bestMatch = qn;
      }

      const { lat, lon } = bestMatch;
      if (target === 'departure') { 
        form.value.departureLat = parseFloat(lat); 
        form.value.departureLng = parseFloat(lon); 
      } else { 
        form.value.arrivalLat = parseFloat(lat); 
        form.value.arrivalLng = parseFloat(lon); 
      }
      updateMap();
    } else {
      // Fallback: Tìm theo từ khóa rút gọn nếu tìm cả chuỗi không ra
      const fallbackUrl = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address.split(',')[0])}&limit=1&countrycodes=vn`;
      const fallbackRes = await fetch(fallbackUrl);
      const fallbackData = await fallbackRes.json();
      if (fallbackData?.[0]) {
        const { lat, lon } = fallbackData[0];
        if (target === 'departure') { form.value.departureLat = parseFloat(lat); form.value.departureLng = parseFloat(lon); }
        else { form.value.arrivalLat = parseFloat(lat); form.value.arrivalLng = parseFloat(lon); }
        updateMap();
      }
    }
  } catch (err) { 
    console.error("Geocoding Error:", err); 
  } finally { 
    geocoding.value[target] = false; 
  }
};

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
    leafletMap.value = L.map(container).setView([16.0, 108.0], 6);
    L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
      maxZoom: 20, subdomains: ['mt0', 'mt1', 'mt2', 'mt3'], attribution: '© Google Maps'
    }).addTo(leafletMap.value);
    leafletMap.value.on('click', (e) => {
      const { lat, lng } = e.latlng;
      if (lastGeocodeTarget.value === 'departure') { form.value.departureLat = lat; form.value.departureLng = lng; }
      else { form.value.arrivalLat = lat; form.value.arrivalLng = lng; }
      updateMap();
    });
    setTimeout(() => { if (leafletMap.value) { leafletMap.value.invalidateSize(); updateMap(); } }, 500);
  });
};

const updateMap = () => {
  const L = window.L; if (!L || !leafletMap.value) return;
  leafletMap.value.eachLayer((layer) => { if (layer instanceof L.Marker || layer instanceof L.Polyline) leafletMap.value.removeLayer(layer); });
  
  const from = [form.value.departureLat, form.value.departureLng];
  const to = [form.value.arrivalLat, form.value.arrivalLng];
  
  // 🟢 Marker Điểm đi (Xanh)
  const startIcon = L.divIcon({ 
    html: `<div class="w-8 h-8 bg-emerald-500 border-4 border-white rounded-full shadow-2xl flex items-center justify-center text-white"><span class="material-symbols-outlined text-sm">trip_origin</span></div>`, 
    className: '', iconSize: [32, 32] 
  });
  
  // 🔴 Marker Điểm đến (Đỏ)
  const endIcon = L.divIcon({ 
    html: `<div class="w-8 h-8 bg-rose-600 border-4 border-white rounded-full shadow-2xl flex items-center justify-center text-white"><span class="material-symbols-outlined text-sm">location_on</span></div>`, 
    className: '', iconSize: [32, 32] 
  });

  if (from[0] > 1 && to[0] > 1) {
    L.marker(from, { icon: startIcon }).addTo(leafletMap.value).bindPopup('<b>Điểm đi:</b> ' + form.value.departurePoint);
    L.marker(to, { icon: endIcon }).addTo(leafletMap.value).bindPopup('<b>Điểm đến:</b> ' + form.value.arrivalPoint);
    
    // ⚡ VẼ ĐƯỜNG CHIM BAY NGAY LẬP TỨC (KHÔNG CẦN ĐỢI SERVER)
    const fallbackLine = L.polyline([from, to], { 
      color: '#075955', weight: 2, dashArray: '5, 10', opacity: 0.5 
    }).addTo(leafletMap.value);
    leafletMap.value.fitBounds([from, to], { padding: [100, 100] });

    adminMapLoading.value = true;
    
    // 🛣️ Tải đường bộ ngầm
    let points = `${from[1]},${from[0]};${to[1]},${to[0]}`;
    const latDiff = Math.abs(from[0] - to[0]);
    const isSouthBound = from[0] > to[0];
    
    // 🇻🇳 THUẬT TOÁN ĐIỂM NEO THÔNG MINH (CHỈ THÊM KHI NẰM GIỮA)
    if (latDiff > 3) {
      if (isSouthBound && to[0] < 12.2) { // Nếu đi vào Nam và đích đến xa hơn Nha Trang
        const waypoint = [109.196747, 12.238791]; 
        points = `${from[1]},${from[0]};${waypoint[0]},${waypoint[1]};${to[1]},${to[0]}`;
      } else if (!isSouthBound && to[0] > 16.0) { // Nếu đi ra Bắc và đích đến xa hơn Đà Nẵng
        const waypoint = [108.206230, 16.047079]; 
        points = `${from[1]},${from[0]};${waypoint[0]},${waypoint[1]};${to[1]},${to[0]}`;
      }
    }

    const osrmUrl = `https://router.project-osrm.org/route/v1/driving/${points}?overview=full&geometries=geojson&steps=true`;
    
    // Thêm AbortController để giới hạn thời gian chờ (Timeout 5 giây)
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 5000);

    fetch(osrmUrl, { signal: controller.signal })
      .then(r => r.json())
      .then(data => {
        clearTimeout(timeoutId);
        if (data.routes?.[0]) {
          // Lưu trữ thông số OSRM thời gian thực
          currentDurationSeconds.value = data.routes[0].duration;
          currentDistanceMeters.value = data.routes[0].distance;
          updateArrivalAndDuration();

          // Xóa đường chim bay khi đã có đường bộ chuẩn
          leafletMap.value.removeLayer(fallbackLine);
          
          const coords = data.routes[0].geometry.coordinates.map(c => [c[1], c[0]]);
          L.polyline(coords, { 
            color: '#075955', weight: 7, opacity: 0.85, lineJoin: 'round', dashArray: '1, 12', lineCap: 'round'
          }).addTo(leafletMap.value);
          L.polyline(coords, { color: '#000', weight: 8, opacity: 0.1 }).addTo(leafletMap.value);
          leafletMap.value.fitBounds(coords, { padding: [100, 100] });
        }
      })
      .catch(err => {
        console.warn("Lộ trình road-path quá lâu, dùng đường chim bay làm dự phòng.");
      })
      .finally(() => {
        adminMapLoading.value = false;
      });
  } else if (from[0] > 1) { 
    leafletMap.value.setView(from, 13); 
    L.marker(from, { icon: startIcon }).addTo(leafletMap.value); 
  } else if (to[0] > 1) { 
    leafletMap.value.setView(to, 13); 
    L.marker(to, { icon: endIcon }).addTo(leafletMap.value); 
  }
};

const handleImageUpload = async (e) => {
  const file = e.target.files[0]; if (!file) return;
  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', 'skybus_preset');
  try {
    const res = await axios.post('https://api.cloudinary.com/v1_1/dzydry2xn/image/upload', formData);
    form.value.imageUrl = res.data.secure_url;
  } catch (err) { alert("Lỗi tải ảnh!"); }
};

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

const openAddModal = async () => { 
  isEditMode.value = false; 
  form.value = { ...defaultForm }; 
  currentDurationSeconds.value = 0;
  currentDistanceMeters.value = 0;
  await fetchBusTypes(); // Làm tươi danh sách dòng xe để lấy ảnh mới nhất
  updateTripImage(); // Cập nhật ảnh ngay lập tức
  isModalOpen.value = true; 
  initAdminMap(); 
};
const openEditModal = async (trip) => { 
  isEditMode.value = true; 
  form.value = { ...trip }; 
  currentDurationSeconds.value = 0;
  currentDistanceMeters.value = 0;
  if (trip.duration && trip.duration.includes('Km')) {
    const match = trip.duration.match(/\((\d+)\s*Km\)/i);
    if (match) {
      currentDistanceMeters.value = parseInt(match[1]) * 1000;
    }
  }
  await fetchBusTypes();
  isModalOpen.value = true; 
  initAdminMap(); 
};
const closeModal = () => { isModalOpen.value = false; };

const handleFormSubmit = async () => {
  try {
    if (isEditMode.value) await api.put(`/trips/${form.value.id}`, form.value);
    else await api.post('/trips', form.value);
    closeModal(); fetchTrips();
  } catch (err) { alert('Lỗi lưu dữ liệu!'); }
};

const deleteTrip = async (id) => {
  if (confirm('Xóa chuyến xe này?')) {
    try { await api.delete(`/trips/${id}`); fetchTrips(); }
    catch (err) { alert('Lỗi xóa!'); }
  }
};

onMounted(() => { fetchTrips(); fetchBusTypes(); });
</script>

<style scoped>
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
</style>
