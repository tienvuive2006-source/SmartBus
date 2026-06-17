<template>
  <div class="space-y-6 animate-fade-in">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
      <div class="flex items-center gap-4">
        <div class="w-12 h-12 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center shadow-inner">
          <span class="material-symbols-outlined text-2xl">history</span>
        </div>
        <div>
          <h1 class="text-xl font-black text-gray-900 tracking-tight">Nhật ký hoạt động (Audit Logs)</h1>
          <p class="text-sm font-medium text-gray-500 mt-1">Lịch sử thao tác của tất cả quản trị viên và người dùng trong hệ thống</p>
        </div>
      </div>
      <button @click="fetchLogs" class="bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold px-5 py-2.5 rounded-xl transition-all flex items-center gap-2">
        <span class="material-symbols-outlined text-[20px]" :class="{ 'animate-spin': loading }">refresh</span>
        Làm mới
      </button>
    </div>

    <!-- Tabs Navigation -->
    <div class="flex items-center gap-2 border-b border-gray-200 px-1 mt-2 mb-4">
      <button 
        @click="activeTab = 'audit'"
        :class="[
          'px-6 py-3 text-sm font-black transition-all border-b-2',
          activeTab === 'audit' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
        ]"
      >
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[18px]">verified_user</span>
          Hoạt động thường
        </div>
      </button>
      <button 
        @click="activeTab = 'error'"
        :class="[
          'px-6 py-3 text-sm font-black transition-all border-b-2',
          activeTab === 'error' ? 'border-red-600 text-red-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
        ]"
      >
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-[18px]">warning</span>
          Lỗi hệ thống
          <span v-if="errorCount > 0" class="ml-1 bg-red-100 text-red-600 py-0.5 px-2 rounded-full text-[10px]">
            {{ errorCount }}
          </span>
        </div>
      </button>
    </div>

    <!-- Table -->
    <div class="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-gray-50/50 border-b border-gray-100 text-gray-500 text-xs font-black uppercase tracking-widest">
              <th class="py-4 px-6 font-black w-24">ID</th>
              <th class="py-4 px-6 font-black">Thời gian</th>
              <th class="py-4 px-6 font-black">User ID</th>
              <th class="py-4 px-6 font-black">Hành động</th>
              <th class="py-4 px-6 font-black">Đối tượng</th>
              <th class="py-4 px-6 font-black">Chi tiết</th>
              <th class="py-4 px-6 font-black">IP Address</th>
            </tr>
          </thead>
          <tbody class="text-sm text-gray-700 divide-y divide-gray-50">
            <tr v-if="loading" class="animate-pulse">
              <td colspan="7" class="py-12 text-center text-gray-400 font-bold">
                <div class="w-8 h-8 border-4 border-gray-200 border-t-indigo-500 rounded-full animate-spin mx-auto mb-3"></div>
                Đang tải dữ liệu...
              </td>
            </tr>
            <tr v-else-if="filteredLogs.length === 0">
              <td colspan="7" class="py-12 text-center text-gray-400 font-bold">
                Chưa có dữ liệu cho mục này
              </td>
            </tr>
            <tr v-for="log in filteredLogs" :key="log.id" class="hover:bg-gray-50 transition-colors">
              <td class="py-4 px-6 font-black text-gray-400">#{{ log.id }}</td>
              <td class="py-4 px-6 font-bold">{{ new Date(log.createdAt).toLocaleString('vi-VN') }}</td>
              <td class="py-4 px-6 font-bold text-indigo-600">{{ log.userId || 'Hệ thống / Ẩn danh' }}</td>
              <td class="py-4 px-6">
                <span :class="[
                  'px-3 py-1 rounded-lg text-[10px] font-black uppercase tracking-wider border',
                  log.actionName === 'SYSTEM_ERROR' ? 'bg-red-50 text-red-700 border-red-200 shadow-[0_0_10px_rgba(239,68,68,0.2)] animate-pulse' : 'bg-indigo-50 text-indigo-700 border-indigo-100'
                ]">
                  {{ log.actionName }}
                </span>
              </td>
              <td class="py-4 px-6 font-bold text-gray-600">{{ log.entityName }}</td>
              <td class="py-4 px-6">
                <div class="flex items-center gap-2">
                  <span class="text-xs text-gray-500 truncate max-w-[200px]" :title="log.details">{{ log.details }}</span>
                  <button @click="openDetails(log)" class="w-6 h-6 rounded-full bg-indigo-50 text-indigo-600 flex items-center justify-center hover:bg-indigo-100 transition-colors shrink-0" title="Xem chi tiết">
                    <span class="material-symbols-outlined text-[14px]">info</span>
                  </button>
                </div>
              </td>
              <td class="py-4 px-6 font-mono text-xs text-gray-400">{{ log.ipAddress }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Chi Tiết -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm animate-fade-in">
        <div class="bg-white rounded-3xl w-full max-w-2xl shadow-2xl overflow-hidden flex flex-col max-h-[80vh]">
          <div class="p-6 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-indigo-100 text-indigo-600 rounded-xl flex items-center justify-center">
                <span class="material-symbols-outlined">data_object</span>
              </div>
              <div>
                <h3 class="text-lg font-black text-gray-900">Chi tiết dữ liệu (Payload)</h3>
                <p class="text-xs font-bold text-gray-500">Log ID: #{{ selectedLog?.id }}</p>
              </div>
            </div>
            <button @click="showModal = false" class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-200 text-gray-500 transition-colors">
              <span class="material-symbols-outlined text-[20px]">close</span>
            </button>
          </div>
          <div class="p-6 overflow-y-auto bg-slate-900 text-emerald-400 font-mono text-sm whitespace-pre-wrap leading-relaxed shadow-inner">
            {{ formatDetails(selectedLog?.details) }}
          </div>
          <div class="p-4 border-t border-gray-100 bg-gray-50 flex justify-end">
            <button @click="showModal = false" class="px-6 py-2.5 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded-xl font-bold transition-colors">
              Đóng
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useApi } from '@/composables/useApi'

const api = useApi()
const logs = ref([])
const loading = ref(false)
const showModal = ref(false)
const selectedLog = ref(null)
const activeTab = ref('audit')

const filteredLogs = computed(() => {
  if (activeTab.value === 'error') {
    return logs.value.filter(log => log.actionName === 'SYSTEM_ERROR')
  }
  return logs.value.filter(log => log.actionName !== 'SYSTEM_ERROR')
})

const errorCount = computed(() => {
  return logs.value.filter(log => log.actionName === 'SYSTEM_ERROR').length
})

const formatDetails = (text) => {
  if (!text) return 'Không có dữ liệu chi tiết';
  
  // Bỏ chữ Method args mặc định
  let formatted = text.replace('Method args:', '').trim();

  // Tách các tham số (ngăn cách bởi dấu |)
  let params = formatted.split('|').map(p => p.trim()).filter(p => p !== '');
  
  let result = '';
  
  params.forEach((param, index) => {
    result += `[Tham số ${index + 1}]:\n`;
    
    // Nếu là object kiểu Java: Trip(id=1, name=ABC)
    if (param.includes('(') && param.endsWith(')')) {
      let objName = param.substring(0, param.indexOf('('));
      let content = param.substring(param.indexOf('(') + 1, param.length - 1);
      
      result += `Đối tượng: ${objName}\n`;
      // Tách các thuộc tính
      let props = content.split(', ');
      props.forEach(prop => {
        let parts = prop.split('=');
        if (parts.length >= 2) {
           result += `   ➤ ${parts[0]}: ${parts.slice(1).join('=')}\n`;
        } else {
           result += `   ➤ ${prop}\n`;
        }
      });
    } 
    // Nếu là Map/JSON kiểu Java: {key=value, key2=value2}
    else if (param.startsWith('{') && param.endsWith('}')) {
      let content = param.substring(1, param.length - 1);
      let props = content.split(', ');
      props.forEach(prop => {
        let parts = prop.split('=');
        if (parts.length >= 2) {
           result += `   ➤ ${parts[0]}: ${parts.slice(1).join('=')}\n`;
        } else {
           result += `   ➤ ${prop}\n`;
        }
      });
    }
    // Dữ liệu thô (vd: số ID)
    else {
      result += `   ➤ Giá trị: ${param}\n`;
    }
    result += '\n';
  });

  return result.trim();
}

const openDetails = (log) => {
  selectedLog.value = log
  showModal.value = true
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const response = await api.get('/admin/audit-logs')
    logs.value = response.data
  } catch (error) {
    console.error("Lỗi khi tải nhật ký hoạt động:", error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>
