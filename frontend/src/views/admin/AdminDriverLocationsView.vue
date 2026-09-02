<template>
  <div class="absolute inset-0 flex flex-col md:flex-row bg-white overflow-hidden font-sans">
    
    <!-- LEFT SIDEBAR: DRIVER LIST -->
    <div class="w-full md:w-80 bg-white border-r border-slate-200 flex flex-col h-full shrink-0 z-20">
      <div class="p-5 border-b border-slate-100 shrink-0 bg-slate-50/50">
        <h2 class="text-lg font-black text-slate-800 flex items-center gap-2">
          <span class="material-symbols-outlined text-primary">pin_drop</span>
          Bản đồ Vị trí Tài xế
        </h2>
        <p class="text-xs font-semibold text-slate-500 mt-1">Theo dõi vị trí dựa trên trạng thái chuyến đi</p>
      </div>

      <div class="flex-1 overflow-y-auto p-3 space-y-2 bg-slate-50/30">
        <div v-if="loading" class="p-8 flex justify-center">
          <div class="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin"></div>
        </div>
        <div v-else-if="activeDrivers.length === 0" class="p-4 text-center text-sm text-slate-500 font-medium">
          Không có tài xế nào đang có chuyến trong ngày hôm nay.
        </div>
        <div 
          v-else
          v-for="driver in activeDrivers" :key="driver.id"
          @click="focusDriver(driver)"
          class="p-3 rounded-xl cursor-pointer transition-all flex items-center gap-3 border bg-white border-slate-100 hover:border-slate-300 hover:shadow-sm"
        >
          <div class="w-10 h-10 rounded-full bg-slate-800 text-white flex items-center justify-center font-bold shrink-0 uppercase shadow-sm overflow-hidden border" :style="{ borderColor: getRingColor(driver.currentTrip?.status) }">
            <img v-if="driver.avatarUrl" :src="driver.avatarUrl" class="w-full h-full object-cover" />
            <span v-else>{{ driver.fullName.charAt(0) }}</span>
          </div>
          <div class="flex-1 min-w-0">
            <p class="font-bold text-sm text-slate-800 truncate">{{ driver.fullName }}</p>
            <p class="text-[11px] text-slate-500 font-semibold mt-0.5 truncate">{{ formatCityName(driver.currentTrip?.departurePoint) }} &rarr; {{ formatCityName(driver.currentTrip?.arrivalPoint) }}</p>
          </div>
          <div class="shrink-0">
            <span :class="['px-2 py-1 text-[10px] font-black uppercase rounded-md tracking-wider', getStatusColor(driver.currentTrip?.status)]">
              {{ getStatusText(driver.currentTrip?.status) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- RIGHT MAIN: LEAFLET MAP -->
    <div class="flex-1 relative z-0">
      <div id="admin-global-map" class="absolute inset-0"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import { useApi } from '@/composables/useApi';
import { toBusinessDateString } from '@/utils/businessDate';

const api = useApi();
const loading = ref(true);
const isFirstLoad = ref(true);
const drivers = ref([]);
const trips = ref([]);
let leafletMap = null;
let markers = {}; 
let pollingInterval = null;

const fetchData = async () => {
  if (isFirstLoad.value) loading.value = true;
  try {
    const [usersRes, tripsRes] = await Promise.all([
      api.get('/users/role/DRIVER'),
      api.get('/trips')
    ]);
    drivers.value = usersRes.data || [];
    trips.value = tripsRes.data || [];
    
    nextTick(() => {
      initMap();
    });
  } catch (err) {
    console.error("Lỗi lấy dữ liệu:", err);
  } finally {
    loading.value = false;
    isFirstLoad.value = false;
  }
};

onMounted(() => {
  fetchData();
  pollingInterval = setInterval(() => {
    fetchData();
  }, 5000);
});

import { onUnmounted } from 'vue';
onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval);
});

const todayStr = toBusinessDateString();

const activeDrivers = computed(() => {
  const result = [];
  drivers.value.forEach(driver => {
    // Lấy TẤT CẢ các chuyến của tài xế này
    const driverTrips = trips.value.filter(t =>
      t.assignedDriverUsername === driver.phone || t.secondaryDriverUsername === driver.phone
    );
    if (driverTrips.length === 0) return;
    
    // Sắp xếp các chuyến theo thời gian gần nhất đến xa nhất (mới nhất ở đầu)
    driverTrips.sort((a, b) => {
      const dateA = new Date(a.departureDate + 'T' + (a.departureTime || '00:00:00'));
      const dateB = new Date(b.departureDate + 'T' + (b.departureTime || '00:00:00'));
      return dateB - dateA;
    });
    
    // Ưu tiên 1: Đang chạy (bất kể ngày nào)
    let currentTrip = driverTrips.find(t => t.status === 'IN_PROGRESS');
    
    // Ưu tiên 2: Chờ chạy (nhưng phải từ hôm nay trở đi, bỏ qua các chuyến rác bị kẹt trong quá khứ)
    if (!currentTrip) {
      currentTrip = driverTrips.find(t => (t.status === 'ASSIGNED' || t.status === 'PENDING') && t.departureDate >= todayStr);
    }
    
    // Ưu tiên 3: Đã hoàn thành (Lấy chuyến COMPLETED gần đây nhất)
    if (!currentTrip) {
      currentTrip = driverTrips.find(t => t.status === 'COMPLETED');
    }
    
    // Dự phòng: Chuyến mới nhất bất kỳ
    if (!currentTrip) currentTrip = driverTrips[0];
    
    if (currentTrip && currentTrip.departureLat && currentTrip.departureLng) {
      let lat = currentTrip.departureLat;
      let lng = currentTrip.departureLng;
      // Nếu đã ĐẾN BẾN CUỐI thì hiện ở điểm đến
      if (currentTrip.status === 'COMPLETED' && currentTrip.arrivalLat) {
        lat = currentTrip.arrivalLat;
        lng = currentTrip.arrivalLng;
      }
      
      result.push({
        ...driver,
        currentTrip,
        simulatedLat: lat,
        simulatedLng: lng,
        baseLat: lat, // Lưu lại tọa độ gốc để focus
        baseLng: lng
      });
    }
  });
  return result;
});

const focusDriver = (driver) => {
  if (leafletMap && driver.baseLat) {
    // Focus vào điểm gốc thay vì điểm đã offset để giữ trung tâm
    leafletMap.setView([driver.baseLat, driver.baseLng], 14, { animate: true });
    if (markers[driver.id]) {
      // Mở popup
      markers[driver.id].openPopup();
    }
  }
};

const getStatusText = (status) => {
  switch(status) {
    case 'ASSIGNED': return 'Chờ chạy';
    case 'IN_PROGRESS': return 'Đang chạy';
    case 'COMPLETED': return 'Đã xong';
    default: return 'Khác';
  }
};

const getStatusColor = (status) => {
  switch(status) {
    case 'ASSIGNED': return 'bg-amber-100 text-amber-700';
    case 'IN_PROGRESS': return 'bg-emerald-100 text-emerald-700 border border-emerald-300 shadow-sm';
    case 'COMPLETED': return 'bg-slate-200 text-slate-600';
    default: return 'bg-slate-100 text-slate-500';
  }
};

const getRingColor = (status) => {
  switch(status) {
    case 'ASSIGNED': return '#f59e0b'; // amber-500
    case 'IN_PROGRESS': return '#10b981'; // emerald-500
    case 'COMPLETED': return '#94a3b8'; // slate-400
    default: return '#cbd5e1';
  }
};

const formatCityName = (name) => {
  if (!name) return 'Chưa rõ';
  return name.replace(/Bến xe Trung tâm |Bến xe Phía Nam |Bến xe Khách |Bến xe |Trạm |VP /gi, '').trim();
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
    script.onload = () => renderMap();
    document.head.appendChild(script);
  } else { setTimeout(renderMap, 400); }
};

const renderMap = () => {
  const L = window.L; if (!L) return;
  const container = document.getElementById('admin-global-map');
  if (!container) return;
  
  // Chỉ tạo map 1 lần, các lần sau chỉ update markers
  if (!leafletMap) {
    leafletMap = L.map(container).setView([16.0, 108.0], 6);
    L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
      maxZoom: 20, subdomains: ['mt0', 'mt1', 'mt2', 'mt3'], attribution: '© Google Maps'
    }).addTo(leafletMap);
  }

  // Xóa markers cũ
  Object.values(markers).forEach(m => leafletMap.removeLayer(m));
  markers = {};

  // Nhóm các tài xế có cùng tọa độ gốc
  const locationGroups = {};
  activeDrivers.value.forEach(driver => {
    const key = `${driver.simulatedLat}_${driver.simulatedLng}`;
    if (!locationGroups[key]) locationGroups[key] = [];
    locationGroups[key].push(driver);
  });

  // Duyệt qua từng nhóm để vẽ marker
  Object.values(locationGroups).forEach(group => {
    const count = group.length;
    
    group.forEach((driver, index) => {
      let finalLat = driver.simulatedLat;
      let finalLng = driver.simulatedLng;
      
      // Thuật toán tản vòng tròn (Spiderfy) nếu có nhiều xe trùng nhau
      if (count > 1) {
        const radius = 0.005; // Bán kính vòng tròn tản ra (~500m)
        const angle = (index / count) * Math.PI * 2;
        finalLat += Math.sin(angle) * radius;
        finalLng += Math.cos(angle) * radius;
      }

      const ringColor = getRingColor(driver.currentTrip?.status);
      const innerHtml = driver.avatarUrl 
        ? `<img src="${driver.avatarUrl}" style="width: 100%; height: 100%; border-radius: 50%; object-fit: cover;" />`
        : `<span style="font-weight: 900; font-size: 16px;">${driver.fullName.charAt(0)}</span>`;

      const htmlIcon = `
        <div style="width: 48px; height: 48px; border-radius: 50%; background: #1e293b; color: white; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 15px rgba(0,0,0,0.4); border: 4px solid ${ringColor}; position: relative;">
          ${innerHtml}
          <div style="position: absolute; bottom: -8px; right: -8px; background: ${ringColor}; width: 18px; height: 18px; border-radius: 50%; border: 3px solid white; display: flex; align-items: center; justify-content: center;"></div>
        </div>
      `;

      const customIcon = L.divIcon({ 
        html: htmlIcon, 
        className: '', 
        iconSize: [48, 48],
        iconAnchor: [24, 24],
        popupAnchor: [0, -24]
      });

      const marker = L.marker([finalLat, finalLng], { icon: customIcon }).addTo(leafletMap);
      
      // Nếu là xe bị tản ra, ta có thể vẽ thêm 1 đường line mờ nối về điểm gốc (tùy chọn, ở đây tạm không vẽ để đỡ rối)
      
      const popupContent = `
        <div style="text-align: center; min-width: 160px; padding: 4px;">
          <h4 style="margin: 0 0 4px 0; font-size: 15px; font-weight: 900; color: #0f172a;">${driver.fullName}</h4>
          <p style="margin: 0; font-size: 12px; color: #64748b; font-weight: 600;">${driver.phone}</p>
          <hr style="margin: 10px 0; border: none; border-top: 1px dashed #cbd5e1;">
          <span style="display: inline-block; padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: 900; text-transform: uppercase; background: ${ringColor}20; color: ${ringColor}; border: 1px solid ${ringColor}; margin-bottom: 8px;">
            ${getStatusText(driver.currentTrip?.status)}
          </span>
          <p style="margin: 0; font-size: 12px; font-weight: 700; color: #334155;">${formatCityName(driver.currentTrip?.departurePoint)} - ${formatCityName(driver.currentTrip?.arrivalPoint)}</p>
          <p style="margin: 4px 0 0 0; font-size: 11px; font-weight: 500; color: #94a3b8;">(${driver.currentTrip?.departureTime})</p>
        </div>
      `;
      
      marker.bindPopup(popupContent);
      markers[driver.id] = marker;
    });
  });

  // Fit bounds if drivers exist AND it's the first load
  if (isFirstLoad.value && activeDrivers.value.length > 0) {
    const latlngs = activeDrivers.value.map(d => [d.simulatedLat, d.simulatedLng]);
    const bounds = L.latLngBounds(latlngs);
    leafletMap.fitBounds(bounds, { padding: [50, 50], maxZoom: 14 });
  }
};
</script>

<style scoped>
.overflow-y-auto::-webkit-scrollbar { width: 6px; }
.overflow-y-auto::-webkit-scrollbar-track { background: transparent; }
.overflow-y-auto::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }
.overflow-y-auto::-webkit-scrollbar-thumb:hover { background: #94a3b8; }
</style>
