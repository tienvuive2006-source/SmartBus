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
    <div class="flex flex-col sm:flex-row sm:items-center justify-between border-b border-gray-200 px-1 mt-2 mb-4 gap-4">
      <div class="flex items-center gap-2">
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
      <div v-if="activeTab === 'error' && errorCount > 0">
        <button @click="clearErrorLogs" class="text-xs font-bold text-red-600 bg-red-50 hover:bg-red-100 px-4 py-2.5 rounded-xl transition-all flex items-center gap-1.5 active:scale-95">
          <span class="material-symbols-outlined text-[16px]">delete_sweep</span> Dọn sạch lỗi
        </button>
      </div>
      <div v-if="activeTab === 'audit' && logs.length > 0">
        <button @click="clearNormalLogs" class="text-xs font-bold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 px-4 py-2.5 rounded-xl transition-all flex items-center gap-1.5 active:scale-95">
          <span class="material-symbols-outlined text-[16px]">delete_sweep</span> Xóa tất cả nhật ký
        </button>
      </div>
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
            <tr v-else-if="logs.length === 0">
              <td colspan="7" class="py-12 text-center text-gray-400 font-bold">
                Chưa có dữ liệu cho mục này
              </td>
            </tr>
            <tr v-for="log in logs" :key="log.id" class="hover:bg-gray-50 transition-colors">
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
                  <button @click="deleteLog(log.id)" class="w-6 h-6 rounded-full bg-red-50 text-red-600 flex items-center justify-center hover:bg-red-100 transition-colors shrink-0" title="Xóa bản ghi này">
                    <span class="material-symbols-outlined text-[14px]">delete</span>
                  </button>
                </div>
              </td>
              <td class="py-4 px-6 font-mono text-xs text-gray-400">{{ log.ipAddress }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination Controls -->
      <div v-if="totalPages > 1" class="flex items-center justify-between px-6 py-4 border-t border-gray-100 bg-gray-50/50">
        <div class="text-sm font-semibold text-gray-500">
          Trang <span class="text-indigo-600 font-black">{{ currentPage + 1 }}</span> / <span class="font-black">{{ totalPages }}</span>
        </div>
        <div class="flex items-center gap-2">
          <button @click="prevPage" :disabled="currentPage === 0" class="px-4 py-2 rounded-lg text-sm font-bold transition-all" :class="currentPage === 0 ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white border border-gray-200 text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 hover:border-indigo-200'">
            Trước
          </button>
          <button @click="nextPage" :disabled="currentPage >= totalPages - 1" class="px-4 py-2 rounded-lg text-sm font-bold transition-all" :class="currentPage >= totalPages - 1 ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white border border-gray-200 text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 hover:border-indigo-200'">
            Sau
          </button>
        </div>
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
                <h3 class="text-lg font-black text-gray-900">Chi tiết thao tác</h3>
                <p class="text-xs font-bold text-gray-500">Log ID: #{{ selectedLog?.id }}</p>
              </div>
            </div>
            <button @click="showModal = false" class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-200 text-gray-500 transition-colors">
              <span class="material-symbols-outlined text-[20px]">close</span>
            </button>
          </div>
          
          <!-- Modal Tabs -->
          <div class="flex border-b border-gray-200 bg-gray-50/50 px-4">
            <button 
              @click="modalTab = 'summary'"
              :class="[
                'px-6 py-3 text-sm font-black transition-all border-b-2 outline-none',
                modalTab === 'summary' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              ]"
            >
              <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-[18px]">translate</span>
                Bản dịch (Cho Admin)
              </div>
            </button>
            <button 
              @click="modalTab = 'raw'"
              :class="[
                'px-6 py-3 text-sm font-black transition-all border-b-2 outline-none',
                modalTab === 'raw' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              ]"
            >
              <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-[18px]">code</span>
                Dữ liệu thô (Cho IT)
              </div>
            </button>
          </div>
          
          <!-- BẢN DỊCH CHO NGƯỜI BÌNH THƯỜNG -->
          <div v-if="modalTab === 'summary'" class="p-6 bg-indigo-50/30 overflow-y-auto">
            <p class="text-sm text-indigo-900 font-medium leading-relaxed whitespace-pre-wrap">
              {{ generateHumanReadableSummary(selectedLog) }}
            </p>
          </div>

          <!-- DỮ LIỆU THÔ DÀNH CHO LẬP TRÌNH VIÊN -->
          <div v-if="modalTab === 'raw'" class="p-6 overflow-y-auto bg-slate-900 text-emerald-400 font-mono text-sm whitespace-pre-wrap leading-relaxed shadow-inner">
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
const modalTab = ref('summary')

// Pagination state
const currentPage = ref(0)
const totalPages = ref(0)
const errorCount = ref(0)

import { watch } from 'vue'

watch(activeTab, () => {
  currentPage.value = 0
  fetchLogs()
})

const generateHumanReadableSummary = (log) => {
  if (!log) return '';
  
  let summary = '';
  const actionMap = {
    // === USER & AUTH ===
    'UPDATE_USER': 'Cập nhật thông tin / Nạp tiền',
    'REGISTER_USER': 'Khách hàng đăng ký tài khoản',
    'TOGGLE_USER_LOCK': 'Khóa/Mở khóa tài khoản Người dùng',
    'DELETE_USER': 'Xóa tài khoản Người dùng',
    
    // === BOOKING ===
    'CREATE_BOOKING': 'Khách hàng Đặt vé mới',
    'UPDATE_BOOKING_STATUS': 'Cập nhật trạng thái Vé',
    'CANCEL_BOOKING': 'Hủy vé',

    // === TRIP (CHUYẾN XE) ===
    'CREATE_TRIP': 'Tạo Chuyến Xe mới',
    'UPDATE_TRIP': 'Cập nhật Chuyến Xe',
    'DELETE_TRIP': 'Xóa Chuyến Xe',
    'TOGGLE_TRIP_VISIBILITY': 'Bật/tắt trạng thái hiển thị Chuyến Xe',

    // === ROUTE (TUYẾN ĐƯỜNG) ===
    'CREATE_ROUTE': 'Thêm Tuyến Đường',
    'UPDATE_ROUTE': 'Sửa Tuyến Đường',
    'DELETE_ROUTE': 'Xóa Tuyến Đường',

    // === BUS & BUS TYPE (XE & LOẠI XE) ===
    'CREATE_BUS': 'Thêm Xe Bus mới',
    'UPDATE_BUS': 'Cập nhật Xe Bus',
    'DELETE_BUS': 'Xóa Xe Bus',
    'CREATE_BUS_TYPE': 'Thêm Loại Xe',
    'UPDATE_BUS_TYPE': 'Sửa Loại Xe',
    'DELETE_BUS_TYPE': 'Xóa Loại Xe',

    // === REVIEW (ĐÁNH GIÁ) ===
    'CREATE_REVIEW': 'Khách hàng Viết Đánh giá',
    'REPLY_REVIEW': 'Phản hồi Đánh giá',
    'DELETE_REVIEW': 'Xóa Đánh giá',

    // === INSPECTOR (LƠ XE / SOÁT VÉ) ===
    'ASSIGN_INSPECTOR': 'Phân công Nhân viên phụ trách chuyến',
    'UNASSIGN_INSPECTOR': 'Hủy phân công Nhân viên',
    'UPDATE_TRIP_STATUS': 'Cập nhật trạng thái chạy của xe (Đang chạy/Hoàn thành)',
    'CHECK_IN_BOOKING': 'Quét mã soát vé lên xe',

    // === HỆ THỐNG ===
    'UPDATE_SYSTEM_SETTING': 'Cập nhật cài đặt hệ thống (Banner/Cấu hình)'
  };
  
  summary += `📌 Thao tác: ${actionMap[log.actionName] || log.actionName}\n`;
  summary += `📦 Đối tượng: ${log.entityName}\n`;

  if (log.details) {
    let raw = log.details.replace('Method args:', '').trim();
    let parts = raw.split('|').map(p => p.trim()).filter(p => p !== '');
    
    // Hàm parse chuỗi Java Object
    const parseJavaObj = (str) => {
      let obj = {};
      let contentMatch = str.match(/\((.*)\)/);
      if (contentMatch) {
        let props = contentMatch[1].split(', ');
        props.forEach(prop => {
          let [key, ...val] = prop.split('=');
          if (key) obj[key.trim()] = val.join('=').trim();
        });
      }
      return obj;
    };

    if (log.actionName === 'UPDATE_USER' || log.actionName === 'REGISTER_USER') {
      let id = parts[0];
      let inputData = parts.length > 1 ? parseJavaObj(parts[1]) : parseJavaObj(parts[0]);
      summary += `\n📝 Chi tiết:\n`;
      if (log.actionName === 'UPDATE_USER') {
          summary += `   • ID Người dùng: ${id}\n`;
      }
      if (inputData.walletBalance && inputData.walletBalance !== 'null') {
         summary += `   • Số dư ví: ${parseFloat(inputData.walletBalance).toLocaleString('vi-VN')} VNĐ\n`;
      }
      if (inputData.role && inputData.role !== 'null') summary += `   • Cấp quyền: ${inputData.role}\n`;
      if (inputData.fullName && inputData.fullName !== 'null') summary += `   • Họ tên: ${inputData.fullName}\n`;
      if (inputData.phone && inputData.phone !== 'null') summary += `   • Số ĐT: ${inputData.phone}\n`;
      if (inputData.email && inputData.email !== 'null') summary += `   • Email: ${inputData.email}\n`;
    } 
    else if (log.actionName === 'TOGGLE_USER_LOCK') {
      let id = parts[0];
      let outputData = parts.length > 1 ? parseJavaObj(parts[1]) : parseJavaObj(parts[0]);
      summary += `\n📝 Chi tiết:\n`;
      summary += `   • ID Người dùng: ${id}\n`;
      if (outputData.fullName && outputData.fullName !== 'null') summary += `   • Khách hàng: ${outputData.fullName}\n`;
      if (outputData.isLocked && outputData.isLocked !== 'null') {
        summary += `   • Trạng thái mới: ${outputData.isLocked === 'true' ? '🔴 ĐÃ BỊ KHÓA (Cấm đăng nhập)' : '🟢 ĐÃ MỞ KHÓA (Cho phép hoạt động)'}\n`;
      }
    }
    else if (log.actionName.includes('TRIP') && parts.length > 0) {
      let dataPart = parts.length > 1 ? parts[1] : parts[0];
      let inputData = parseJavaObj(dataPart);
      summary += `\n📝 Thông tin chuyến xe:\n`;
      if (inputData.departurePoint) summary += `   • Tuyến: ${inputData.departurePoint.split(',')[0]} ➝ ${inputData.arrivalPoint.split(',')[0]}\n`;
      if (inputData.companyName) summary += `   • Nhà xe: ${inputData.companyName}\n`;
      if (inputData.departureTime) summary += `   • Khởi hành: ${inputData.departureTime} ${inputData.departureDate || ''}\n`;
      if (inputData.price && inputData.price !== 'null') summary += `   • Giá vé: ${parseFloat(inputData.price).toLocaleString('vi-VN')} VNĐ\n`;
    }
    else {
      // Mặc định bóc tách vài trường quan trọng nếu có
      let dataPart = parts.length > 1 ? parts[1] : parts[0];
      let inputData = parseJavaObj(dataPart);
      if (Object.keys(inputData).length > 0) {
        summary += `\n📝 Dữ liệu thay đổi chính:\n`;
        ['name', 'title', 'status', 'amount', 'email', 'phone'].forEach(key => {
          if (inputData[key] && inputData[key] !== 'null') {
            summary += `   • ${key.toUpperCase()}: ${inputData[key]}\n`;
          }
        });
      }
    }
  }
  
  return summary.trim();
}

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
  modalTab.value = 'summary'
  showModal.value = true
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const response = await api.get(`/admin/audit-logs?type=${activeTab.value}&page=${currentPage.value}&size=20`)
    logs.value = response.data.content
    totalPages.value = response.data.totalPages
  } catch (error) {
    console.error("Lỗi khi tải nhật ký hoạt động:", error)
  } finally {
    loading.value = false
  }
}

const fetchErrorCount = async () => {
  try {
    const response = await api.get('/admin/audit-logs/error-count')
    errorCount.value = response.data
  } catch (error) {
    console.error("Lỗi đếm số lượng lỗi hệ thống:", error)
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    fetchLogs()
  }
}

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--
    fetchLogs()
  }
}

const deleteLog = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xóa bản ghi Audit Log này không?')) return;
  try {
    await api.delete(`/admin/audit-logs/${id}`);
    fetchLogs();
  } catch (err) {
    alert('Không thể xóa log!');
  }
}

const clearErrorLogs = async () => {
  if (!confirm('⚠️ CẢNH BÁO: Bạn có chắc chắn muốn dọn sạch TẤT CẢ log lỗi hệ thống không? Hành động này không thể hoàn tác!')) return;
  try {
    await api.delete(`/admin/audit-logs/clear-errors`);
    fetchLogs();
  } catch (err) {
    alert('Không thể dọn dẹp lỗi!');
  }
}

const clearNormalLogs = async () => {
  if (!confirm('⚠️ CẢNH BÁO: Bạn có chắc chắn muốn dọn sạch TẤT CẢ nhật ký hoạt động thường không? Hành động này không thể hoàn tác!')) return;
  try {
    await api.delete(`/admin/audit-logs/clear-normal`);
    fetchLogs();
  } catch (err) {
    alert('Không thể dọn dẹp nhật ký!');
  }
}

onMounted(() => {
  fetchLogs()
  fetchErrorCount()
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
