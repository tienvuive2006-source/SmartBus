<template>
  <div class="p-6 md:p-8 max-w-7xl mx-auto bg-slate-50 min-h-screen font-sans">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
      <div>
        <h2 class="text-2xl font-extrabold text-slate-800 tracking-tight">
          Quản lý Tuyến đường
        </h2>
        <p class="text-xs font-semibold text-slate-500 mt-1">Thiết lập các tuyến đường cố định để sử dụng nhanh</p>
      </div>
      <button 
        @click="openAddModal" 
        class="bg-[#075955] hover:bg-[#064a47] text-white px-5 py-2.5 rounded-xl shadow-sm hover:shadow-md active:scale-95 transition-all duration-200 font-bold text-xs flex items-center justify-center gap-2"
      >
        <span class="material-symbols-outlined text-[18px]">add_circle</span>
        Tạo tuyến đường
      </button>
    </div>

    <!-- Main List -->
    <AdminRouteTable 
      :routes="routes"
      @toggle-visibility="toggleVisibility"
      @edit-route="editRoute"
      @delete-route="deleteRoute"
      @manage-stops="manageStops"
    />

    <!-- Create/Edit Route Modal -->
    <AdminRouteModal 
      ref="routeModal"
      @save="onRouteSaved"
    />
    <RouteStopManagerModal ref="stopModal" @notify="showStopToast" />
    <AdminActionToast
      :show="stopToast.show"
      :type="stopToast.type"
      :message="stopToast.message"
      @close="stopToast.show = false"
    />
  </div>
</template>

<script setup>
import { ref, onBeforeUnmount, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';
import AdminRouteTable from '@/components/admin/route/AdminRouteTable.vue';
import AdminRouteModal from '@/components/admin/route/AdminRouteModal.vue';
import RouteStopManagerModal from '@/components/admin/route/RouteStopManagerModal.vue';
import AdminActionToast from '@/components/admin/AdminActionToast.vue';

const api = useApi();
const routes = ref([]);
const routeModal = ref(null);
const stopModal = ref(null);
const stopToast = ref({ show: false, type: 'success', message: '' });
let stopToastTimer = null;

const showStopToast = ({ type, message }) => {
  if (stopToastTimer) clearTimeout(stopToastTimer);
  stopToast.value = { show: true, type, message };
  stopToastTimer = setTimeout(() => { stopToast.value.show = false; }, 3500);
};

const loadRoutes = async () => {
  try {
    const res = await api.get('/routes');
    routes.value = res.data;
  } catch (err) {
    console.error('Lỗi khi tải danh sách tuyến đường:', err);
    routes.value = [];
  }
};

const openAddModal = () => {
  if (routeModal.value) {
    routeModal.value.openModal(null, -1);
  }
};

const editRoute = (idx) => {
  if (routeModal.value) {
    routeModal.value.openModal(routes.value[idx], idx);
  }
};

const manageStops = idx => stopModal.value?.show(routes.value[idx]);

const deleteRoute = async (idx) => {
  if (confirm("Xóa tuyến đường này?")) {
    const route = routes.value[idx];
    
    try {
      if (route.id) {
        await api.delete(`/routes/${route.id}`);
      }
      
      if (route) {
        const shortDep = route.departurePoint.split(',')[0].trim();
        const shortArr = route.arrivalPoint.split(',')[0].trim();
        const routeImages = JSON.parse(localStorage.getItem('smartbus_route_images') || '{}');
        delete routeImages[`${shortDep}||${shortArr}`];
        delete routeImages[`${shortArr}||${shortDep}`];
        localStorage.setItem('smartbus_route_images', JSON.stringify(routeImages));
      }
      
      routes.value.splice(idx, 1);
    } catch (err) {
      console.error(err);
      alert('Lỗi xóa tuyến đường trên máy chủ!');
    }
  }
};

const toggleVisibility = async (idx) => {
  const route = routes.value[idx];
  const newStatus = route.isVisible === false ? true : false;
  
  try {
    const updatedRoute = { ...route, isVisible: newStatus };
    await api.put(`/routes/${route.id}`, updatedRoute);
    route.isVisible = newStatus;
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái hiển thị:', error);
    alert('Không thể cập nhật trạng thái, vui lòng thử lại!');
  }
};

const onRouteSaved = async ({ route, index, formImageUrl, shortDep, shortArr }) => {
  try {
    if (index >= 0) {
      const existingRouteId = routes.value[index].id;
      const res = await api.put(`/routes/${existingRouteId}`, route);
      routes.value[index] = res.data;
    } else {
      const res = await api.post('/routes', route);
      routes.value.push(res.data);
    }

    if (formImageUrl) {
      const routeImages = JSON.parse(localStorage.getItem('smartbus_route_images') || '{}');
      routeImages[`${shortDep}||${shortArr}`] = formImageUrl;
      routeImages[`${shortArr}||${shortDep}`] = formImageUrl;
      localStorage.setItem('smartbus_route_images', JSON.stringify(routeImages));
    }
  } catch (error) {
    console.error(error);
    alert('Không thể lưu tuyến đường lên máy chủ! Vui lòng thử lại.');
  }
};

onMounted(() => {
  loadRoutes();
});

onBeforeUnmount(() => {
  if (stopToastTimer) clearTimeout(stopToastTimer);
});
</script>
