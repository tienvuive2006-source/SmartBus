<template>
  <div class="p-container-margin md:p-8 max-w-7xl mx-auto relative">
    
    <!-- Header Page Title -->
    <div class="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4 border-b border-outline-variant/20 pb-6">
      <div>
        <h2 class="text-headline-lg font-headline-lg font-black text-on-background flex items-center gap-3">
          <span class="material-symbols-outlined text-4xl text-primary">garage</span>
          Quản Lý Hạm Đội Xe Thật
        </h2>
        <p class="text-body-lg font-body-lg text-on-surface-variant">Khởi tạo, cấu hình và giám sát cơ sở hạ tầng xe Trung Nam từ SQL Server</p>
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

    <!-- 2. Fleet List -->
    <div class="w-full">
      <FleetTable 
        :buses="buses"
        :loading="loading"
        @open-create="openCreateModal"
        @edit="openEditModal"
        @delete="handleDeleteBus"
      />
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
import FleetModal from '../../components/admin/fleet/FleetModal.vue';

const api = useApi();
const buses = ref([]);
const busTypes = ref([]); 
const loading = ref(true);

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

const fetchBuses = async () => {
  loading.value = true;
  try {
    const response = await api.get('/buses');
    buses.value = response.data;
  } catch (error) { console.error(error); }
  finally { loading.value = false; }
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
    closeModal(); await fetchBuses();
  } catch (error) { alert("Lỗi lưu dữ liệu!"); }
};

const handleDeleteBus = async (bus) => {
  if (confirm(`Xóa xe [${bus.licensePlate}]?`)) {
    try { await api.delete(`/buses/${bus.id}`); await fetchBuses(); }
    catch (error) { alert("Không thể xóa!"); }
  }
};

onMounted(async () => {
  await fetchBusTypes();
  await fetchBuses();
});
</script>

<style scoped>
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.animate-fade-in { animation: fadeIn 0.3s ease-out forwards; }
</style>
