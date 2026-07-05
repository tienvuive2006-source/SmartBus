<template>
  <div class="h-full flex flex-col font-sans p-6 bg-slate-50">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
      <div>
        <h2 class="text-2xl font-extrabold text-slate-800 tracking-tight">
          Quản lý Nghỉ phép Tài xế
        </h2>
        <p class="text-xs font-semibold text-slate-500 mt-1">Duyệt hoặc từ chối đơn xin nghỉ phép của tài xế</p>
      </div>
      <button 
        @click="fetchLeaveRequests" 
        class="bg-white border border-slate-200 hover:bg-slate-100 text-slate-700 px-5 py-2.5 rounded-xl shadow-sm hover:shadow-md transition-all font-bold text-xs flex items-center gap-2"
      >
        <span class="material-symbols-outlined text-[18px]" :class="{'animate-spin': loading}">sync</span>
        Làm mới
      </button>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
      <div class="bg-white p-5 rounded-3xl border border-slate-100 shadow-sm flex items-center justify-between group hover:shadow-md transition-shadow">
        <div>
          <p class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Tổng số đơn</p>
          <h3 class="text-2xl font-black text-slate-800">{{ requests.length }}</h3>
        </div>
        <div class="w-12 h-12 rounded-full bg-slate-50 flex items-center justify-center group-hover:bg-slate-100 transition-colors">
          <span class="material-symbols-outlined text-slate-400 text-2xl">receipt_long</span>
        </div>
      </div>
      <div class="bg-white p-5 rounded-3xl border border-amber-100 shadow-sm flex items-center justify-between group hover:shadow-md transition-shadow">
        <div>
          <p class="text-[10px] font-black text-amber-500 uppercase tracking-widest mb-1">Chờ duyệt</p>
          <h3 class="text-2xl font-black text-amber-600">{{ pendingCount }}</h3>
        </div>
        <div class="w-12 h-12 rounded-full bg-amber-50 flex items-center justify-center group-hover:bg-amber-100 transition-colors">
          <span class="material-symbols-outlined text-amber-500 text-2xl">pending_actions</span>
        </div>
      </div>
    </div>

    <!-- Data Table -->
    <div class="flex-1 bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden flex flex-col">
      <div v-if="loading" class="p-12 flex justify-center shrink-0">
        <div class="w-8 h-8 border-4 border-slate-200 border-t-emerald-500 rounded-full animate-spin"></div>
      </div>
      <div v-else-if="requests.length === 0" class="p-12 text-center text-slate-500 font-bold">
        Chưa có đơn xin nghỉ phép nào.
      </div>
      <div v-else class="flex-1 overflow-auto">
        <table class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-slate-50 border-b border-slate-100">
            <th class="p-4 text-xs font-black text-slate-500 uppercase tracking-widest">Mã đơn</th>
            <th class="p-4 text-xs font-black text-slate-500 uppercase tracking-widest">Tài xế</th>
            <th class="p-4 text-xs font-black text-slate-500 uppercase tracking-widest">Ngày nghỉ</th>
            <th class="p-4 text-xs font-black text-slate-500 uppercase tracking-widest">Lý do</th>
            <th class="p-4 text-xs font-black text-slate-500 uppercase tracking-widest">Trạng thái</th>
            <th class="p-4 text-xs font-black text-slate-500 uppercase tracking-widest text-right">Hành động</th>
          </tr>
        </thead>
        <tbody class="text-sm">
          <tr v-for="req in sortedRequests" :key="req.id" class="border-b border-slate-50 hover:bg-slate-50/50 transition-colors">
            <td class="p-4 font-black text-slate-400">#{{ req.id }}</td>
            <td class="p-4">
               <div class="font-bold text-slate-800">{{ req.driverFullName }}</div>
               <div class="text-[10px] text-slate-400">{{ req.driverUsername }}</div>
            </td>
            <td class="p-4 font-bold text-slate-700">
               {{ formatDate(req.startDate) }} <span class="text-slate-300 mx-1">➔</span> {{ formatDate(req.endDate) }}
            </td>
            <td class="p-4 text-slate-600 max-w-[200px] truncate" :title="req.reason">{{ req.reason }}</td>
            <td class="p-4">
              <span class="px-3 py-1 rounded-full text-[10px] font-black tracking-widest uppercase border" :class="getStatusClasses(req.status)">
                {{ req.status === 'PENDING' ? 'Chờ duyệt' : (req.status === 'APPROVED' ? 'Đã duyệt' : 'Từ chối') }}
              </span>
            </td>
            <td class="p-4 text-right">
               <div class="flex items-center justify-end gap-2" v-if="req.status === 'PENDING'">
                  <button @click="updateStatus(req.id, 'APPROVED')" class="w-8 h-8 rounded-full bg-emerald-50 text-emerald-600 hover:bg-emerald-500 hover:text-white flex items-center justify-center transition-all border border-emerald-100" title="Duyệt">
                     <span class="material-symbols-outlined text-[16px]">check</span>
                  </button>
                  <button @click="updateStatus(req.id, 'REJECTED')" class="w-8 h-8 rounded-full bg-rose-50 text-rose-600 hover:bg-rose-500 hover:text-white flex items-center justify-center transition-all border border-rose-100" title="Từ chối">
                     <span class="material-symbols-outlined text-[16px]">close</span>
                  </button>
               </div>
            </td>
          </tr>
        </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useApi } from '../../composables/useApi';

const api = useApi();
const requests = ref([]);
const loading = ref(true);

const fetchLeaveRequests = async () => {
   loading.value = true;
   try {
      const res = await api.get('/leave-requests');
      requests.value = res.data;
   } catch(e) {
      console.error(e);
   } finally {
      loading.value = false;
   }
};

const pendingCount = computed(() => requests.value.filter(r => r.status === 'PENDING').length);

const sortedRequests = computed(() => {
   return [...requests.value].sort((a, b) => {
      // PENDING lên đầu
      if (a.status === 'PENDING' && b.status !== 'PENDING') return -1;
      if (a.status !== 'PENDING' && b.status === 'PENDING') return 1;
      return new Date(b.createdAt) - new Date(a.createdAt); // Mới nhất lên trên
   });
});

const updateStatus = async (id, status) => {
   if(!confirm(`Xác nhận ${status === 'APPROVED' ? 'Duyệt' : 'Từ chối'} đơn nghỉ phép này?`)) return;
   try {
      await api.patch(`/leave-requests/${id}/status`, { status });
      await fetchLeaveRequests();
   } catch(e) {
      alert("Cập nhật thất bại: " + (e.response?.data?.error || e.response?.data?.message || e.message));
   }
};

const formatDate = (dateStr) => {
   if(!dateStr) return '';
   const [y, m, d] = dateStr.split('-');
   return `${d}/${m}/${y}`;
};

const getStatusClasses = (status) => {
   switch(status) {
      case 'PENDING': return 'bg-amber-50 text-amber-600 border-amber-200';
      case 'APPROVED': return 'bg-emerald-50 text-emerald-600 border-emerald-200';
      case 'REJECTED': return 'bg-rose-50 text-rose-600 border-rose-200';
      default: return 'bg-slate-50 text-slate-600 border-slate-200';
   }
};

onMounted(() => {
   fetchLeaveRequests();
});
</script>
