<template>
  <div class="min-h-screen bg-[#f2f5f8] font-sans text-slate-900">
    <header class="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between gap-4">
        <div class="flex items-center gap-6 flex-1">
          <div @click="$router.push('/')" class="cursor-pointer flex items-center gap-2">
             <span class="material-symbols-outlined text-[#075955] text-3xl">directions_bus</span>
             <span class="text-xl font-bold tracking-tight text-gray-800">Trung - Nam</span>
          </div>
          
          <div class="hidden md:flex items-center bg-gray-50 border border-gray-200 rounded-md divide-x divide-gray-200 overflow-hidden flex-1 shadow-inner">
             <div class="px-3 py-2.5 flex items-center gap-2 flex-1 min-w-0">
                <span class="material-symbols-outlined text-gray-400 text-sm shrink-0">location_on</span>
                <span class="text-sm font-semibold truncate text-gray-700" :title="$route.query.from">{{ $route.query.from }}</span>
             </div>
             <div class="px-3 py-2.5 flex items-center gap-2 flex-1 min-w-0">
                <span class="material-symbols-outlined text-gray-400 text-sm shrink-0">near_me</span>
                <span class="text-sm font-semibold truncate text-gray-700" :title="$route.query.to">{{ $route.query.to }}</span>
             </div>
             <div class="px-4 py-2.5 flex items-center gap-2 shrink-0 bg-gray-100/50">
                <span class="material-symbols-outlined text-gray-400 text-sm">calendar_month</span>
                <span class="text-sm font-semibold text-gray-600">{{ formatDateDisplay($route.query.date) }}</span>
             </div>
             <button @click="$router.push('/')" class="px-6 py-2.5 bg-[#f03a17] hover:bg-[#d63314] text-white font-black text-xs uppercase tracking-widest transition-all shrink-0">Sửa</button>
          </div>
        </div>
        
        <div class="flex items-center gap-4">
           <!-- 🔑 AUTH LOGIC -->
           <div v-if="authStore.isLoggedIn" @click="$router.push('/profile')" class="flex items-center gap-2 cursor-pointer group">
              <div class="w-8 h-8 rounded-full bg-[#075955]/10 flex items-center justify-center border border-[#075955]/20 overflow-hidden">
                <img :src="`https://ui-avatars.com/api/?name=${encodeURIComponent(authStore.currentUser?.fullName || 'U')}&background=075955&color=fff`" class="w-full h-full object-cover" />
              </div>
              <span class="text-sm font-semibold text-gray-700 group-hover:text-[#075955] transition-colors">{{ authStore.currentUser?.fullName }}</span>
           </div>
           <button v-else @click="$router.push('/auth/login')" class="text-sm font-semibold text-[#075955] border border-[#075955] px-4 py-1.5 rounded hover:bg-[#075955]/10 transition-colors">Đăng nhập</button>
        </div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto px-4 py-6 grid grid-cols-1 lg:grid-cols-4 gap-6">
      
      <aside class="hidden lg:block space-y-4">
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
          <div class="p-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
             <h3 class="text-sm font-bold text-gray-800">Sắp xếp</h3>
             <button @click="clearFilters" class="text-[11px] font-bold text-[#075955] hover:text-[#0a7a75] active:scale-95 transition-all uppercase cursor-pointer">Xóa lọc</button>
          </div>
          <div class="p-4 space-y-3">
             <label v-for="sort in sortOptions" :key="sort.id" class="flex items-center gap-3 cursor-pointer group">
                <input type="radio" name="sort" :value="sort.id" v-model="currentSort" class="w-4 h-4 accent-[#075955]" />
                <span class="text-sm font-medium text-gray-600 group-hover:text-[#075955] transition-colors">{{ sort.name }}</span>
             </label>
          </div>
        </div>

        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
          <div class="p-4 border-b border-gray-100 bg-gray-50/50">
             <h3 class="text-sm font-bold text-gray-800">Giờ đi</h3>
          </div>
          <div class="p-4 space-y-3">
             <label class="flex items-center gap-3 cursor-pointer group">
               <input type="checkbox" v-model="selectedTimeSlots" value="early" class="w-4 h-4 rounded accent-[#075955]"/>
               <span class="text-sm font-medium text-gray-600 group-hover:text-gray-900">Sáng sớm (00:00 - 06:00)</span>
             </label>
             <label class="flex items-center gap-3 cursor-pointer group">
               <input type="checkbox" v-model="selectedTimeSlots" value="morning" class="w-4 h-4 rounded accent-[#075955]"/>
               <span class="text-sm font-medium text-gray-600 group-hover:text-gray-900">Buổi sáng (06:00 - 12:00)</span>
             </label>
             <label class="flex items-center gap-3 cursor-pointer group">
               <input type="checkbox" v-model="selectedTimeSlots" value="afternoon" class="w-4 h-4 rounded accent-[#075955]"/>
               <span class="text-sm font-medium text-gray-600 group-hover:text-gray-900">Buổi chiều (12:00 - 18:00)</span>
             </label>
             <label class="flex items-center gap-3 cursor-pointer group">
               <input type="checkbox" v-model="selectedTimeSlots" value="evening" class="w-4 h-4 rounded accent-[#075955]"/>
               <span class="text-sm font-medium text-gray-600 group-hover:text-gray-900">Buổi tối (18:00 - 24:00)</span>
             </label>
          </div>
        </div>
      </aside>

      <section class="lg:col-span-3 space-y-4">
        
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-2">
           <h2 class="text-lg font-bold text-gray-800">Kết quả: {{ filteredTrips.length }} chuyến</h2>
           <div class="flex gap-2 overflow-x-auto pb-2 md:pb-0 scrollbar-hide">
              <button class="bg-white border border-gray-200 px-4 py-1.5 rounded-full text-xs font-semibold text-gray-600 hover:border-[#075955] hover:text-[#075955] transition-all whitespace-nowrap shadow-sm">Ưu đãi 50%</button>
              <button class="bg-white border border-gray-200 px-4 py-1.5 rounded-full text-xs font-semibold text-gray-600 hover:border-[#075955] hover:text-[#075955] transition-all whitespace-nowrap shadow-sm">Xe Limousine</button>
              <button class="bg-white border border-gray-200 px-4 py-1.5 rounded-full text-xs font-semibold text-gray-600 hover:border-[#075955] hover:text-[#075955] transition-all whitespace-nowrap shadow-sm">Giá rẻ nhất</button>
           </div>
        </div>

        <div v-if="loading" class="flex justify-center py-20"><div class="w-10 h-10 border-4 border-gray-200 border-t-[#075955] rounded-full animate-spin"></div></div>

        <div v-else class="space-y-4">
          <article 
            v-for="trip in filteredTrips" :key="trip.id" 
            class="bg-white rounded-xl border border-gray-200 hover:border-[#075955]/50 transition-all duration-300 shadow-sm overflow-hidden flex flex-col md:flex-row"
          >
            <div class="md:w-48 bg-gray-100 relative group overflow-hidden shrink-0">
               <img 
                 :src="trip.imageUrl || 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&q=80&w=400'" 
                 class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" 
                 @error="(e) => e.target.src = 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&q=80&w=400'"
               />
               <div class="absolute top-2 left-2 flex flex-col gap-1">
                  <span class="bg-[#075955] text-white text-[9px] font-bold px-2 py-0.5 rounded flex items-center gap-1 shadow-sm">
                     <span class="material-symbols-outlined text-[10px]">verified</span> Xác nhận tức thì
                  </span>
                  <span v-if="trip.rating >= 4.8" class="bg-yellow-400 text-gray-900 text-[9px] font-bold px-2 py-0.5 rounded shadow-sm flex items-center gap-1">
                     <span class="material-symbols-outlined text-[10px]">star</span> {{ trip.rating }}
                  </span>
               </div>
            </div>

            <div class="flex-1 p-5 flex flex-col justify-between border-r border-gray-100">
               <div>
                  <div class="flex justify-between items-start mb-4">
                     <div>
                        <h3 class="text-lg font-bold text-gray-900 leading-tight">{{ trip.companyName }}</h3>
                        <p class="text-xs font-semibold text-gray-500 mt-1">{{ trip.busType.replace(/Luxyry/g, 'Luxury') }}</p>
                     </div>
                  </div>
                  
                  <div class="flex items-center gap-6 mb-4">
                     <div class="flex flex-col items-center">
                        <span class="text-xl font-bold text-gray-900">{{ trip.departureTime }}</span>
                         <span class="text-[10px] font-bold text-blue-600 bg-blue-50 px-1.5 rounded mt-1 whitespace-nowrap">{{ formatDate(trip.departureDate) }}</span>
                        <div class="w-2 h-2 rounded-full border-2 border-[#075955] mt-2"></div>
                     </div>
                     <div class="flex-1 flex flex-col items-center group">
                        <span class="text-xs text-gray-400">{{ trip.duration }}</span>
                        <div class="w-full h-px border-t border-dashed border-gray-300 relative mt-2">
                           <span class="material-symbols-outlined absolute left-1/2 -translate-x-1/2 -translate-y-1/2 text-gray-300 text-lg bg-white px-1">directions_bus</span>
                        </div>
                     </div>
                     <div class="flex flex-col items-center">
                        <span class="text-xl font-bold text-gray-900">{{ trip.arrivalTime }}</span>
                        <div class="w-2 h-2 rounded-full bg-gray-300 mt-2"></div>
                     </div>
                  </div>

                   <div class="flex items-center justify-between text-[13px] font-medium text-gray-600">
                      <span class="flex items-center gap-1"><span class="material-symbols-outlined text-sm">location_on</span> {{ trip.departurePoint }}</span>
                      <span class="flex items-center gap-1 text-right">{{ trip.arrivalPoint }} <span class="material-symbols-outlined text-sm">location_on</span></span>
                   </div>
                   
                   <div class="mt-4 pt-4 border-t border-gray-50 flex items-center justify-between">
                      <span class="flex items-center gap-1 text-[11px] font-semibold text-emerald-600 bg-emerald-50 px-2 py-1 rounded">
                         <span class="material-symbols-outlined text-xs">sell</span> Giảm 10% khi đặt hôm nay
                      </span>
                      <button 
                        @click="openMapModal(trip)"
                        class="flex items-center gap-1 text-[11px] font-bold text-[#075955] hover:underline"
                      >
                        <span class="material-symbols-outlined text-xs">map</span> Xem bản đồ hành trình
                      </button>
                   </div>
                </div>
             </div>

            <div class="md:w-56 p-5 bg-gray-50/50 flex flex-col justify-center items-center md:items-end">
               <div class="text-center md:text-right mb-5">
                  <p class="text-xs font-semibold text-gray-500 mb-1">Giá vé</p>
                  <p class="text-2xl font-bold text-[#075955]">{{ trip.price.toLocaleString() }}<span class="text-sm ml-0.5 underline">đ</span></p>
                  <p class="text-[11px] font-semibold text-emerald-500 mt-1">Còn {{ trip.availableSeats }} chỗ trống</p>
               </div>
               
               <button 
                 @click="$router.push({ path: '/booking/seat', query: { tripId: trip.id } })"
                 class="w-full bg-[#f03a17] hover:bg-[#d63314] text-white py-3 rounded-md font-bold text-sm transition-all shadow-sm active:scale-95"
               >
                 Chọn chỗ
               </button>
               <p class="text-[10px] text-gray-400 mt-3 italic text-center w-full">KHÔNG CẦN THANH TOÁN TRƯỚC</p>
            </div>
          </article>
          
          <div v-if="filteredTrips.length === 0" class="bg-white p-20 rounded-xl border border-gray-200 text-center shadow-sm">
             <span class="material-symbols-outlined text-6xl text-gray-300 mb-4">search_off</span>
             <h3 class="text-lg font-bold text-gray-900">Không tìm thấy chuyến xe nào</h3>
             <p class="text-sm text-gray-500 mt-2">Vui lòng thử lại với lộ trình hoặc thời gian khác.</p>
             <button @click="$router.push('/')" class="mt-6 px-8 py-2.5 bg-[#075955] text-white rounded-md font-bold text-sm">Quay lại trang chủ</button>
          </div>
        </div>
      </section>
    </main>

    <!-- 🗺️ BẢN ĐỒ HÀNH TRÌNH MODAL -->
    <Teleport to="body">
      <div v-if="isMapModalOpen" class="fixed inset-0 z-[1000] bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 animate-fade-in" @click.self="closeMapModal">
        <div class="bg-white w-full max-w-4xl h-[80vh] rounded-3xl overflow-hidden shadow-2xl flex flex-col relative animate-scale-up">
          <div class="p-5 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
            <div>
               <h3 class="text-lg font-black text-[#075955] flex items-center gap-2">
                 <span class="material-symbols-outlined">explore</span> Bản đồ hành trình chi tiết
               </h3>
               <p class="text-[11px] font-bold text-gray-500 uppercase tracking-widest">
                 {{ selectedTripForMap?.departurePoint }} ➝ {{ selectedTripForMap?.arrivalPoint }}
                 <span v-if="routeDistance" class="ml-3 text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">
                   <span class="material-symbols-outlined text-[10px] align-middle mr-1">distance</span>
                   {{ routeDistance }} km
                 </span>
               </p>
            </div>
            <button @click="closeMapModal" class="w-10 h-10 rounded-full hover:bg-gray-200 flex items-center justify-center transition-all">
              <span class="material-symbols-outlined">close</span>
            </button>
          </div>
          
          <div class="flex-1 relative bg-gray-100">
            <div id="route-map" class="w-full h-full"></div>
            
            <div v-if="mapLoading" class="absolute inset-0 bg-white/80 flex flex-col items-center justify-center z-10">
              <div class="w-12 h-12 border-4 border-[#075955] border-t-transparent rounded-full animate-spin mb-4"></div>
              <p class="text-sm font-bold text-[#075955]">Đang tải bản đồ vệ tinh...</p>
            </div>
          </div>

          <div class="p-5 bg-white border-t border-gray-100 flex flex-col md:flex-row justify-between items-center gap-4">
             <div class="flex items-center gap-6">
                <div class="flex items-center gap-2">
                   <div class="w-3 h-3 rounded-full bg-blue-500 border-2 border-white shadow-sm"></div>
                   <span class="text-xs font-bold text-gray-600">Điểm đón</span>
                </div>
                <div class="flex items-center gap-2">
                   <div class="w-3 h-3 rounded-full bg-red-500 border-2 border-white shadow-sm"></div>
                   <span class="text-xs font-bold text-gray-600">Điểm trả</span>
                </div>
             </div>
             <button 
               @click="$router.push({ path: '/booking/seat', query: { tripId: selectedTripForMap?.id } })"
               class="px-8 py-2.5 bg-[#f03a17] text-white rounded-xl font-black text-xs uppercase tracking-widest hover:bg-[#d63314] transition-all shadow-md active:scale-95"
             >
               Chọn chỗ chuyến này
             </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
// Giữ nguyên toàn bộ phần <script setup> của bạn
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useApi } from '@/composables/useApi';
import { removeAccents } from '../../composables/useLocationSearch';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const api = useApi();
const authStore = useAuthStore();
const allTrips = ref([]);
const loading = ref(true);
const currentSort = ref('default');
const selectedTimeSlots = ref([]);

// ─── MAP MODAL STATE ─────────────────────────────────────────────────────────
const isMapModalOpen = ref(false);
const selectedTripForMap = ref(null);
const mapLoading = ref(true);
const routeDistance = ref(null);
const useGoogleMaps = ref(false);
const googleMap = ref(null);
const directionsRenderer = ref(null);

const cityCoordinates = {
  'Ha Noi': [21.028511, 105.804817],
  'Hai Phong': [20.844912, 106.688087],
  'SaPa': [22.336404, 103.843848],
  'Da Nang': [16.047079, 108.206230],
  'Nha Trang': [12.238791, 109.196747],
  'Ho Chi Minh': [10.823099, 106.629664],
  'Sai Gon': [10.823099, 106.629664],
  'Can Tho': [10.045162, 105.746857],
  'Da Lat': [11.940419, 108.458313],
  'Hue': [16.463713, 107.590866],
  'Vung Tau': [10.345995, 107.084052]
};

const formatDate = (d) => {
  if (!d) return '';
  const datePart = d.split('T')[0];
  const [year, month, day] = datePart.split('-');
  return `${day}/${month}/${year}`;
};


const sortOptions = [
  { id: 'default', name: 'Mặc định' },
  { id: 'price_asc', name: 'Giá thấp nhất' },
  { id: 'price_desc', name: 'Giá cao nhất' },
  { id: 'time_asc', name: 'Giờ đi sớm nhất' },
  { id: 'time_desc', name: 'Giờ đi muộn nhất' },
  { id: 'rating_desc', name: 'Đánh giá cao nhất' }
];

const formatDateDisplay = (d) => {
  if (!d) return 'Chọn ngày';
  return new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
};

const normalize = (s) => {
  return removeAccents(s || '')
    .toLowerCase()
    .replace(/hcm|sai gon/g, 'ho chi minh')
    .replace(/hn/g, 'ha noi')
    .replace(/[,.-]/g, ' ')
    .replace(/\b(ben xe|thanh pho|tp|tinh|huyen|xa|quan|phuong)\b/gi, '')
    .replace(/\s+/g, ' ')
    .trim();
};

const filteredTrips = computed(() => {
  const from = route.query.from || '';
  const to = route.query.to || '';
  const date = route.query.date || '';
  const company = route.query.company || '';
  
  const cFrom = normalize(from);
  const cTo = normalize(to);

  let results = allTrips.value.filter(t => {
    // 0. Lọc theo hãng xe (nếu được truyền)
    if (company && company !== 'all') {
      if (t.companyName !== company) return false;
    }

    // 1. Lọc theo ngày
    if (date && t.departureDate) {
      if (t.departureDate.split('T')[0] !== date) return false;
    }
    
    // 2. Lọc theo địa điểm
    const tFrom = normalize(t.departurePoint);
    const tTo = normalize(t.arrivalPoint);
    const matchLocation = (tFrom.includes(cFrom) || cFrom.includes(tFrom)) && (tTo.includes(cTo) || cTo.includes(tTo));
    if (!matchLocation) return false;

    // 3. Lọc theo khung giờ đi
    if (selectedTimeSlots.value.length > 0) {
      const hour = parseInt(t.departureTime.split(':')[0]);
      let slot = '';
      if (hour >= 0 && hour < 6) slot = 'early';
      else if (hour >= 6 && hour < 12) slot = 'morning';
      else if (hour >= 12 && hour < 18) slot = 'afternoon';
      else slot = 'evening';
      
      if (!selectedTimeSlots.value.includes(slot)) return false;
    }

    return true;
  });

  // 4. Sắp xếp kết quả
  if (currentSort.value === 'price_asc') results.sort((a, b) => a.price - b.price);
  else if (currentSort.value === 'price_desc') results.sort((a, b) => b.price - a.price);
  else if (currentSort.value === 'time_asc') results.sort((a, b) => a.departureTime.localeCompare(b.departureTime));
  else if (currentSort.value === 'time_desc') results.sort((a, b) => b.departureTime.localeCompare(a.departureTime));
  else if (currentSort.value === 'rating_desc') results.sort((a, b) => (b.rating || 0) - (a.rating || 0));

  return results;
});

const fetchTrips = async () => {
  loading.value = true;
  try {
    const { from, to, date } = route.query;
    // 🚀 CHUẨN ĐỒ ÁN: Gọi API Search tại Backend thay vì lấy hết về
    const res = await api.get('/trips/search', {
      params: { from, to, date }
    });
    allTrips.value = res.data;
  } catch (err) { 
    console.error("Lỗi fetch trips:", err); 
  } finally { 
    loading.value = false; 
  }
};

onMounted(() => {
  fetchTrips();
});

let leafletMap = null;

const openMapModal = (trip) => {
  selectedTripForMap.value = trip;
  isMapModalOpen.value = true;
  mapLoading.value = true;
  
  // 🛰️ KIỂM TRA CHẾ ĐỘ BẢN ĐỒ (GOOGLE VS LEAFLET)
  if (window.google && window.google.maps) {
    useGoogleMaps.value = true;
    nextTick(() => initGoogleMap());
  } else {
    useGoogleMaps.value = false;
    // Inject Leaflet CSS if not present
    if (!document.getElementById('leaflet-css')) {
      const link = document.createElement('link');
      link.id = 'leaflet-css';
      link.rel = 'stylesheet';
      link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
      document.head.appendChild(link);
    }

    // Inject Leaflet JS if not present
    if (!window.L) {
      const script = document.createElement('script');
      script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
      script.onload = () => setTimeout(initMap, 300);
      document.head.appendChild(script);
    } else {
      setTimeout(initMap, 300);
    }
  }
};

const initGoogleMap = () => {
  const container = document.getElementById('route-map');
  if (!container) return;
  
  googleMap.value = new window.google.maps.Map(container, {
    center: { lat: 16.0, lng: 106.0 },
    zoom: 6,
    disableDefaultUI: false,
    mapId: 'CUSTOMER_VIEW_MAP'
  });

  directionsRenderer.value = new window.google.maps.DirectionsRenderer({
    map: googleMap.value,
    polylineOptions: { strokeColor: '#075955', strokeWeight: 6 }
  });

  updateGoogleMap();
};

const updateGoogleMap = () => {
  if (!googleMap.value || !selectedTripForMap.value) return;
  
  const from = { lat: Number(selectedTripForMap.value.departureLat), lng: Number(selectedTripForMap.value.departureLng) };
  const to = { lat: Number(selectedTripForMap.value.arrivalLat), lng: Number(selectedTripForMap.value.arrivalLng) };

  if (from.lat > 1 && to.lat > 1) {
    const directionsService = new window.google.maps.DirectionsService();
    directionsService.route({
      origin: from,
      destination: to,
      travelMode: window.google.maps.TravelMode.DRIVING
    }, (result, status) => {
      if (status === 'OK') {
        directionsRenderer.value.setDirections(result);
        const route = result.routes[0];
        routeDistance.value = (route.legs[0].distance.value / 1000).toFixed(1);
        mapLoading.value = false;
      }
    });
  }
};

const initMap = () => {
  const L = window.L;
  if (!L) return;
  
  nextTick(() => {
    if (leafletMap) {
      leafletMap.remove();
    }
    
    leafletMap = L.map('route-map').setView([16.0, 106.0], 6);
    
    L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
      maxZoom: 20,
      subdomains: ['mt0', 'mt1', 'mt2', 'mt3'],
      attribution: '© Google Maps'
    }).addTo(leafletMap);

    const from = normalize(selectedTripForMap.value.departurePoint);
    const to = normalize(selectedTripForMap.value.arrivalPoint);
    
    // 🌍 ƯU TIÊN 1: Lấy tọa độ chính xác từ Backend (nếu Admin đã nhập)
    let fromCoords = selectedTripForMap.value.departureLat && selectedTripForMap.value.departureLng 
      ? [Number(selectedTripForMap.value.departureLat), Number(selectedTripForMap.value.departureLng)] 
      : null;
      
    let toCoords = selectedTripForMap.value.arrivalLat && selectedTripForMap.value.arrivalLng 
      ? [Number(selectedTripForMap.value.arrivalLat), Number(selectedTripForMap.value.arrivalLng)] 
      : null;
    
    // 🏙️ ƯU TIÊN 2: Fallback tìm kiếm theo tên thành phố nếu Backend chưa có tọa độ
    if (!fromCoords || !toCoords) {
      for (const city in cityCoordinates) {
        const normalizedCity = normalize(city);
        if (!fromCoords && from.includes(normalizedCity)) fromCoords = cityCoordinates[city];
        if (!toCoords && to.includes(normalizedCity)) toCoords = cityCoordinates[city];
      }
    }
    
    // 🔎 ƯU TIÊN 3: Fallback cuối cùng tìm kiếm thô theo text
    if (!fromCoords || !toCoords) {
       for (const city in cityCoordinates) {
         const cityKey = city.toLowerCase();
         if (!fromCoords && from.toLowerCase().includes(cityKey)) fromCoords = cityCoordinates[city];
         if (!toCoords && to.toLowerCase().includes(cityKey)) toCoords = cityCoordinates[city];
       }
    }
    
    if (fromCoords && toCoords) {
      const startIcon = L.divIcon({
        html: `<div class="w-6 h-6 bg-blue-500 border-2 border-white rounded-full shadow-lg flex items-center justify-center text-white"><span class="material-symbols-outlined text-xs font-black">trip_origin</span></div>`,
        className: '', iconSize: [24, 24]
      });
      const endIcon = L.divIcon({
        html: `<div class="w-6 h-6 bg-red-500 border-2 border-white rounded-full shadow-lg flex items-center justify-center text-white"><span class="material-symbols-outlined text-xs font-black">location_on</span></div>`,
        className: '', iconSize: [24, 24]
      });

      L.marker(fromCoords, { icon: startIcon }).addTo(leafletMap).bindPopup('Điểm khởi hành');
      L.marker(toCoords, { icon: endIcon }).addTo(leafletMap).bindPopup('Điểm đến');
      
      // 🚀 GỌI API OSRM ĐỂ LẤY ĐƯỜNG ĐI THẬT (FOLLOW ROADS)
      let points = `${fromCoords[1]},${fromCoords[0]};${toCoords[1]},${toCoords[0]}`;
      
      const latDiff = Math.abs(fromCoords[0] - toCoords[0]);
      const isSouthBound = fromCoords[0] > toCoords[0];
      
      // 🇻🇳 THUẬT TOÁN ĐIỂM NEO THÔNG MINH (CHỈ THÊM KHI NẰM GIỮA)
      if (latDiff > 3) {
        if (isSouthBound && toCoords[0] < 12.2) { // Nếu đi vào Nam và đích đến xa hơn Nha Trang
          const waypoint = [109.196747, 12.238791]; // Nha Trang
          points = `${fromCoords[1]},${fromCoords[0]};${waypoint[0]},${waypoint[1]};${toCoords[1]},${toCoords[0]}`;
        } else if (!isSouthBound && toCoords[0] > 16.0) { // Nếu đi ra Bắc và đích đến xa hơn Đà Nẵng
          const waypoint = [108.206230, 16.047079]; // Đà Nẵng
          points = `${fromCoords[1]},${fromCoords[0]};${waypoint[0]},${waypoint[1]};${toCoords[1]},${toCoords[0]}`;
        }
      }

      const osrmUrl = `https://router.project-osrm.org/route/v1/driving/${points}?overview=full&geometries=geojson`;
      
      fetch(osrmUrl)
        .then(res => res.json())
        .then(data => {
          if (data.routes && data.routes.length > 0) {
            const route = data.routes[0];
            const coordinates = route.geometry.coordinates.map(c => [c[1], c[0]]);
            
            L.polyline(coordinates, { 
              color: '#075955', 
              weight: 5, 
              opacity: 0.8,
              lineJoin: 'round'
            }).addTo(leafletMap);
            
            routeDistance.value = (route.distance / 1000).toFixed(1);
            
            const bounds = L.latLngBounds(coordinates);
            leafletMap.fitBounds(bounds, { padding: [50, 50] });
          } else {
            L.polyline([fromCoords, toCoords], { color: '#075955', weight: 4, dashArray: '10, 10' }).addTo(leafletMap);
            leafletMap.fitBounds([fromCoords, toCoords], { padding: [50, 50] });
          }
        })
        .catch(err => {
          console.error("OSRM Error:", err);
          L.polyline([fromCoords, toCoords], { color: '#075955', weight: 4, dashArray: '10, 10' }).addTo(leafletMap);
        });
    }
    
    mapLoading.value = false;
  });
};

const closeMapModal = () => {
  isMapModalOpen.value = false;
  routeDistance.value = null;
  if (leafletMap) {
    leafletMap.remove();
    leafletMap = null;
  }
};

const clearFilters = () => {
  currentSort.value = 'default';
  selectedTimeSlots.value = [];
};
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar { display: none; }
.scrollbar-hide { -ms-overflow-style: none; scrollbar-width: none; }

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}

@keyframes scaleUp {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}
</style>