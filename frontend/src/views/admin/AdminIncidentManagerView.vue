<template>
  <div class="p-container-margin md:p-8 max-w-7xl mx-auto relative">
    
    <!-- Header Page Title -->
    <div class="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4 border-b border-outline-variant/20 pb-6">
      <div>
        <h2 class="text-headline-lg font-headline-lg font-black text-on-background flex items-center gap-3">
          <span class="material-symbols-outlined text-4xl text-error">warning</span>
          Quản Lý Sự Cố Khẩn Cấp
        </h2>
        <p class="text-body-lg font-body-lg text-on-surface-variant">Giám sát và xử lý các báo cáo sự cố từ Tài xế & Lơ xe</p>
      </div>
      
      <div class="flex items-center gap-3">
        <span class="text-label-md font-bold text-on-surface-variant bg-surface-container-high px-4 py-2 rounded-xl border border-outline-variant/30">
          Tổng số sự cố: <span class="font-black text-on-surface">{{ incidents.length }}</span>
        </span>
        <button 
          @click="fetchIncidents"
          class="bg-primary/10 text-primary hover:bg-primary/20 active:scale-95 px-4 py-2 rounded-xl font-bold tracking-wide transition-all flex items-center gap-2 shrink-0"
        >
          <span class="material-symbols-outlined text-sm">refresh</span>
          Làm mới
        </button>
      </div>
    </div>

    <!-- Table -->
    <div class="w-full">
      <div class="bg-white rounded-3xl border border-outline-variant/30 shadow-[0px_4px_24px_rgba(0,0,0,0.02)] overflow-hidden">
        
        <!-- Loading State -->
        <div v-if="loading" class="p-12 text-center">
          <div class="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
          <p class="text-body-md text-on-surface-variant">Đang tải danh sách sự cố...</p>
        </div>

        <!-- Empty State -->
        <div v-else-if="incidents.length === 0" class="p-12 text-center border-dashed border-outline-variant/50">
          <div class="w-20 h-20 bg-surface-container-high rounded-full flex items-center justify-center mx-auto mb-4">
            <span class="material-symbols-outlined text-5xl text-emerald-500">check_circle</span>
          </div>
          <h4 class="text-headline-sm font-bold text-on-surface">Không có sự cố nào!</h4>
          <p class="text-body-md text-on-surface-variant mt-1 max-w-sm mx-auto">Mọi chuyến xe đều đang hoạt động trơn tru và an toàn.</p>
        </div>

        <!-- Data Table -->
        <div v-else class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-surface-container-lowest border-b border-outline-variant/30">
                <th class="py-4 px-6 text-label-md font-black text-on-surface-variant uppercase tracking-wider whitespace-nowrap">Thời gian</th>
                <th class="py-4 px-6 text-label-md font-black text-on-surface-variant uppercase tracking-wider whitespace-nowrap">Chuyến (ID)</th>
                <th class="py-4 px-6 text-label-md font-black text-on-surface-variant uppercase tracking-wider whitespace-nowrap">Người báo cáo</th>
                <th class="py-4 px-6 text-label-md font-black text-on-surface-variant uppercase tracking-wider">Chi tiết sự cố</th>
                <th class="py-4 px-6 text-label-md font-black text-on-surface-variant uppercase tracking-wider whitespace-nowrap">Mức độ</th>
                <th class="py-4 px-6 text-label-md font-black text-on-surface-variant uppercase tracking-wider whitespace-nowrap text-right">Trạng thái</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-outline-variant/10 text-body-md text-on-surface">
              <tr 
                v-for="incident in sortedIncidents" 
                :key="incident.id"
                class="hover:bg-surface-container-lowest/50 transition-colors"
                :class="incident.status === 'PENDING' ? 'bg-error/5' : ''"
              >
                <td class="py-4 px-6 whitespace-nowrap font-medium">{{ formatDate(incident.createdAt) }}</td>
                <td class="py-4 px-6 whitespace-nowrap">
                  <span class="px-2 py-1 bg-surface-container-high text-on-surface font-mono text-label-sm font-black rounded-md border border-outline-variant/20">
                    #{{ incident.tripId }}
                  </span>
                </td>
                <td class="py-4 px-6 whitespace-nowrap font-bold">{{ incident.driverName }}</td>
                <td class="py-4 px-6 min-w-[250px]">{{ incident.description }}</td>
                <td class="py-4 px-6 whitespace-nowrap">
                  <span 
                    class="px-3 py-1 text-label-sm font-black rounded-lg uppercase tracking-wider shadow-sm"
                    :class="getSeverityClass(incident.severity)"
                  >
                    {{ incident.severity }}
                  </span>
                </td>
                <td class="py-4 px-6 whitespace-nowrap text-right">
                  <button 
                    v-if="incident.status === 'PENDING'"
                    @click="resolveIncident(incident.id)"
                    class="bg-error text-white hover:bg-error/90 active:scale-95 px-4 py-2 rounded-xl font-bold tracking-wide transition-all shadow-sm"
                  >
                    ĐÁNH DẤU XỬ LÝ
                  </button>
                  <span v-else class="text-emerald-600 font-black flex items-center justify-end gap-1">
                    <span class="material-symbols-outlined text-[18px]">verified</span>
                    ĐÃ XỬ LÝ
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';

const api = useApi();
const incidents = ref([]);
const loading = ref(true);

const fetchIncidents = async () => {
  loading.value = true;
  try {
    const response = await api.get('/admin/incidents');
    incidents.value = response.data;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const resolveIncident = async (id) => {
  if(!confirm('Xác nhận đã xử lý xong sự cố này?')) return;
  try {
    await api.put(`/admin/incidents/${id}/resolve`, {});
    await fetchIncidents();
  } catch (error) {
    console.error(error);
    alert("Cập nhật thất bại: " + (error.response?.data?.message || error.response?.statusText || error.message));
  }
};

onMounted(() => {
  fetchIncidents();
});

const sortedIncidents = computed(() => {
  return [...incidents.value].sort((a, b) => {
    if (a.status === 'PENDING' && b.status !== 'PENDING') return -1;
    if (a.status !== 'PENDING' && b.status === 'PENDING') return 1;
    return new Date(b.createdAt) - new Date(a.createdAt);
  });
});

const getSeverityClass = (severity) => {
  switch (severity) {
    case 'CRITICAL': return 'bg-error text-white border border-error';
    case 'HIGH': return 'bg-amber-500 text-white border border-amber-500';
    case 'LOW': return 'bg-slate-200 text-slate-700 border border-slate-300';
    default: return 'bg-primary text-white border border-primary';
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleString('vi-VN');
};
</script>
