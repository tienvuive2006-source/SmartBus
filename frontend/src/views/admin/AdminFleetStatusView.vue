<template>
  <div class="p-container-margin md:p-8 max-w-7xl mx-auto relative animate-fade-in">
    
    <!-- Header Page Title -->
    <div class="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4 border-b border-outline-variant/20 pb-6">
      <div>
        <h2 class="text-headline-lg font-headline-lg font-black text-on-background flex items-center gap-3">
          <span class="material-symbols-outlined text-4xl text-primary">garage</span>
          Quản Lý Hạm Đội Xe Thật
        </h2>
        <p class="text-body-lg font-body-lg text-on-surface-variant">Khởi tạo, cấu hình và giám sát cơ sở hạ tầng xe SkyBus từ SQL Server</p>
      </div>
      
      <button 
        @click="openCreateModal"
        class="bg-primary text-on-primary hover:bg-surface-tint hover:shadow-lg active:scale-95 px-6 py-3 rounded-2xl font-black tracking-wide shadow-md transition-all flex items-center gap-2 shrink-0"
      >
        <span class="material-symbols-outlined">add_circle</span>
        THÊM XE MỚI VÀO KHO
      </button>
    </div>

    <!-- 1. Stats Overview -->
    <FleetStats 
      :totalBuses="totalBuses"
      :activeBuses="activeBuses"
      :maintenanceBuses="maintenanceBuses"
      :idleBuses="idleBuses"
    />

    <!-- 2. Fleet List & Map Section -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
      <FleetTable 
        :buses="buses"
        :loading="loading"
        @open-create="openCreateModal"
        @locate="locateOnMap"
        @edit="openEditModal"
        @delete="handleDeleteBus"
      />

      <FleetMap :mapLoading="mapLoading" />
    </div>

    <!-- 3. Teleport Modal -->
    <FleetModal 
      :isOpen="isModalOpen"
      :isEditMode="isEditMode"
      :form="form"
      :busTypes="busTypes"
      @close="closeModal"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import { useApi } from '@/composables/useApi';
import FleetStats from '../../components/admin/fleet/FleetStats.vue';
import FleetTable from '../../components/admin/fleet/FleetTable.vue';
import FleetMap from '../../components/admin/fleet/FleetMap.vue';
import FleetModal from '../../components/admin/fleet/FleetModal.vue';

const api = useApi();
const buses = ref([]);
const busTypes = ref([]); 
const loading = ref(true);
const mapLoading = ref(true);

const fetchBusTypes = async () => {
  try {
    const response = await api.get('/bus-types');
    busTypes.value = response.data;
    if (busTypes.value.length > 0 && !form.value.busType) {
      form.value.busType = busTypes.value[0].name;
    }
  } catch (error) { console.error(error); }
};

const cityCoordinates = {
  'Hà Nội': [21.028511, 105.804817],
  'Hải Phòng': [20.844912, 106.688087],
  'SaPa': [22.336404, 103.843848],
  'Đà Nẵng': [16.047079, 108.206230],
  'Nha Trang': [12.238791, 109.196747],
  'Sài Gòn': [10.823099, 106.629664],
  'Cần Thơ': [10.045162, 105.746857]
};

const totalBuses = computed(() => buses.value.length);
const activeBuses = computed(() => buses.value.filter(b => b.status === 'ĐANG CHẠY').length);
const maintenanceBuses = computed(() => buses.value.filter(b => b.status === 'BẢO TRÌ').length);
const idleBuses = computed(() => buses.value.filter(b => b.status === 'ĐANG NGHỈ').length);

let leafletMap = null;
let mapMarkers = [];

const fetchBuses = async () => {
  loading.value = true;
  try {
    const response = await api.get('/buses');
    buses.value = response.data;
  } catch (error) { console.error(error); }
  finally { loading.value = false; }
};

const initLeafletMap = () => {
  if (!document.getElementById('leaflet-css')) {
    const link = document.createElement('link');
    link.id = 'leaflet-css'; link.rel = 'stylesheet';
    link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
    document.head.appendChild(link);
  }

  const script = document.createElement('script');
  script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
  script.onload = () => {
    nextTick(() => {
      const L = window.L; if (!L) return;
      leafletMap = L.map('leaflet-map').setView([16.303652, 107.991233], 6);
      L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
        maxZoom: 20, subdomains: ['mt0', 'mt1', 'mt2', 'mt3'], attribution: '© Google Maps'
      }).addTo(leafletMap);
      mapLoading.value = false;
      updateMapMarkers();
    });
  };
  document.head.appendChild(script);
};

const updateMapMarkers = () => {
  const L = window.L; if (!L || !leafletMap) return;
  mapMarkers.forEach(m => leafletMap.removeLayer(m));
  mapMarkers = [];

  buses.value.forEach(bus => {
    const coords = cityCoordinates[bus.currentStation] || cityCoordinates['Hà Nội'];
    let color = '#64748b'; let iconClass = 'directions_bus';
    if (bus.status === 'ĐANG CHẠY') color = '#10b981';
    else if (bus.status === 'BẢO TRÌ') { color = '#f59e0b'; iconClass = 'build'; }

    const iconStr = `<div style="background:${color}; border:2px solid #fff; border-radius:50%; width:28px; height:28px; display:flex; align-items:center; justify-content:center; box-shadow:0 3px 10px rgba(0,0,0,0.3);"><span class="material-symbols-outlined" style="font-size:14px; color:#fff; font-weight:bold;">${iconClass}</span></div>`;
    const customIcon = L.divIcon({ html: iconStr, className: 'bus-gps-icon', iconSize: [28, 28], iconAnchor: [14, 14] });
    const marker = L.marker(coords, { icon: customIcon }).addTo(leafletMap).bindPopup(`
        <div style="font-family: sans-serif; min-width:150px; padding: 2px;">
          <div style="background:#e0f2fe; color:#0369a1; border-radius:4px; padding:2px 6px; text-align:center; font-weight:900; letter-spacing:1px; font-family:monospace; border:1px solid #bae6fd; margin-bottom:6px;">🚍 ${bus.licensePlate}</div>
          <div style="font-size:12px; line-height:1.5;">
            <b>Loại xe:</b> ${bus.busType}<br><b>Tài xế:</b> ${bus.driverName || 'N/A'}<br><b>Vị trí:</b> Trạm ${bus.currentStation}<br><b style="color:${color}">Trạng thái: ${bus.status}</b>
          </div>
        </div>
      `);
    mapMarkers.push(marker);
  });
};

const locateOnMap = (bus) => {
  const L = window.L; if (!L || !leafletMap) return;
  const coords = cityCoordinates[bus.currentStation] || cityCoordinates['Hà Nội'];
  leafletMap.flyTo(coords, 13, { animate: true, duration: 1.5 });
};

const isModalOpen = ref(false);
const isEditMode = ref(false);
const form = ref({ id: null, licensePlate: '', busType: '', driverName: '', currentStation: 'Hà Nội', status: 'ĐANG NGHỈ', imageUrl: '' });

const openCreateModal = () => {
  isEditMode.value = false;
  form.value = { id: null, licensePlate: '', busType: busTypes.value[0]?.name || '', driverName: '', currentStation: 'Hà Nội', status: 'ĐANG NGHỈ', imageUrl: '' };
  isModalOpen.value = true;
};

const openEditModal = (bus) => {
  isEditMode.value = true;
  form.value = { ...bus };
  isModalOpen.value = true;
};

const closeModal = () => { isModalOpen.value = false; };

const handleFormSubmit = async () => {
  if (!form.value.licensePlate || !form.value.driverName) return alert("Vui lòng nhập đầy đủ!");
  try {
    if (isEditMode.value) await api.put(`/buses/${form.value.id}`, form.value);
    else await api.post('/buses', form.value);
    closeModal(); await fetchBuses(); updateMapMarkers();
  } catch (error) { alert("Lỗi lưu dữ liệu!"); }
};

const handleDeleteBus = async (bus) => {
  if (confirm(`Xóa xe [${bus.licensePlate}]?`)) {
    try { await api.delete(`/buses/${bus.id}`); await fetchBuses(); updateMapMarkers(); }
    catch (error) { alert("Không thể xóa!"); }
  }
};

onMounted(async () => {
  await fetchBusTypes();
  await fetchBuses();
  initLeafletMap();
});
</script>

<style scoped>
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in { animation: fadeIn 0.3s ease-out forwards; }
</style>
