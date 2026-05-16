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
import { useLocationSearch } from '../../composables/useLocationSearch';
import TripStats from '../../components/admin/trip/TripStats.vue';
import TripTable from '../../components/admin/trip/TripTable.vue';
import TripModal from '../../components/admin/trip/TripModal.vue';

const API_BASE = 'http://localhost:8080/api/trips';
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

let debounceTimer = null;
const debouncedGeocode = (val, target) => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => autoGeocode(val, target), 800);
};

const autoGeocode = async (address, target) => {
  if (!address || address.length < 3) return;
  geocoding.value[target] = true;
  lastGeocodeTarget.value = target;
  
  try {
    // 🇻🇳 Rào chắn tọa độ Việt Nam cực kỳ nghiêm ngặt (từ Cà Mau đến Lạng Sơn)
    const viewbox = "102.1,8.5,109.5,23.4"; 
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}&limit=3&countrycodes=vn&viewbox=${viewbox}&bounded=1`;
    
    const res = await axios.get(url);
    if (res.data && res.data.length > 0) {
      // Ưu tiên kết quả có tên trùng khớp nhất hoặc nằm ở vị trí hợp lý
      let bestMatch = res.data[0];
      if (address.toLowerCase().includes('quy nhơn')) {
        const qn = res.data.find(d => d.display_name.toLowerCase().includes('quy nhơn'));
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
      const fallbackRes = await axios.get(fallbackUrl);
      if (fallbackRes.data?.[0]) {
        const { lat, lon } = fallbackRes.data[0];
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
    const res = await axios.get(API_BASE);
    trips.value = res.data.sort((a, b) => b.id - a.id);
  } catch (err) { console.error(err); }
};

const fetchBusTypes = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/bus-types');
    busTypes.value = res.data;
  } catch (err) { console.error(err); }
};

const openAddModal = async () => { 
  isEditMode.value = false; 
  form.value = { ...defaultForm }; 
  await fetchBusTypes(); // Làm tươi danh sách dòng xe để lấy ảnh mới nhất
  updateTripImage(); // Cập nhật ảnh ngay lập tức
  isModalOpen.value = true; 
  initAdminMap(); 
};
const openEditModal = async (trip) => { 
  isEditMode.value = true; 
  form.value = { ...trip }; 
  await fetchBusTypes();
  isModalOpen.value = true; 
  initAdminMap(); 
};
const closeModal = () => { isModalOpen.value = false; };

const handleFormSubmit = async () => {
  try {
    if (isEditMode.value) await axios.put(`${API_BASE}/${form.value.id}`, form.value);
    else await axios.post(API_BASE, form.value);
    closeModal(); fetchTrips();
  } catch (err) { alert('Lỗi lưu dữ liệu!'); }
};

const deleteTrip = async (id) => {
  if (confirm('Xóa chuyến xe này?')) {
    try { await axios.delete(`${API_BASE}/${id}`); fetchTrips(); }
    catch (err) { alert('Lỗi xóa!'); }
  }
};

onMounted(() => { fetchTrips(); fetchBusTypes(); });
</script>

<style scoped>
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
</style>
