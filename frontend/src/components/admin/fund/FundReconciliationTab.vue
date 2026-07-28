<template>
  <div>
    <!-- Reconciliation Table -->
    <div class="bg-white rounded-[2rem] border border-gray-100 overflow-hidden shadow-[0_8px_30px_rgb(0,0,0,0.04)] animate-fade-in">
      <div class="p-6 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
        <div>
          <h2 class="text-lg font-black text-gray-900">Lịch sử đối soát quỹ</h2>
          <p class="text-sm font-medium text-gray-500 mt-1">Ghi nhận các lần kiểm đếm và điều chỉnh số dư thực tế</p>
        </div>
        <button @click="openReconModal" class="flex items-center gap-2 px-6 py-2.5 bg-amber-600 text-white rounded-xl text-sm font-bold hover:bg-amber-700 transition-colors shadow-lg shadow-amber-600/20">
          <span class="material-symbols-outlined text-lg">add_circle</span>
          Tạo phiếu đối soát
        </button>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-100">
              <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest whitespace-nowrap">Thời gian</th>
              <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest whitespace-nowrap">Quỹ</th>
              <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest whitespace-nowrap">Hệ thống</th>
              <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest whitespace-nowrap">Thực tế</th>
              <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest whitespace-nowrap">Chênh lệch</th>
              <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest">Ghi chú</th>
              <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest whitespace-nowrap">Người thực hiện</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50 text-sm">
            <tr v-if="loading" class="animate-pulse">
              <td colspan="7" class="px-6 py-12 text-center text-gray-400 font-medium">Đang tải lịch sử đối soát...</td>
            </tr>
            <tr v-else-if="reconciliations.length === 0">
              <td colspan="7" class="px-6 py-12 text-center text-gray-400 font-medium">Chưa có phiếu đối soát nào.</td>
            </tr>
            <tr v-for="rec in reconciliations" :key="rec.id" class="hover:bg-gray-50/50 transition-colors">
              <td class="px-6 py-4 whitespace-nowrap font-bold text-gray-600">{{ formatDateTime(rec.createdAt) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-3 py-1 rounded-lg text-xs font-black uppercase tracking-widest" :class="getFundTypeBadge(rec.fundType)">
                  {{ getFilterLabel(rec.fundType) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap font-bold text-gray-500">{{ formatCurrency(rec.systemBalance) }}</td>
              <td class="px-6 py-4 whitespace-nowrap font-black text-gray-900">{{ formatCurrency(rec.actualBalance) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span v-if="Math.abs(rec.discrepancy) < 0.01" class="px-3 py-1 bg-gray-100 text-gray-600 rounded-lg text-xs font-black uppercase tracking-widest">Khớp</span>
                <span v-else-if="rec.discrepancy > 0" class="px-3 py-1 bg-emerald-100 text-emerald-700 rounded-lg text-xs font-black tracking-widest">+{{ formatCurrency(rec.discrepancy) }}</span>
                <span v-else class="px-3 py-1 bg-red-100 text-red-700 rounded-lg text-xs font-black tracking-widest">{{ formatCurrency(rec.discrepancy) }}</span>
              </td>
              <td class="px-6 py-4 text-gray-600 font-medium">{{ rec.reason || '-' }}</td>
              <td class="px-6 py-4 whitespace-nowrap font-bold text-gray-600">{{ rec.performedBy }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Reconciliation Modal -->
    <Teleport to="body">
      <div v-if="showReconModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm animate-fade-in">
        <div class="bg-white rounded-3xl w-full max-w-lg shadow-2xl overflow-hidden flex flex-col">
          <div class="p-6 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-amber-100 text-amber-600 rounded-xl flex items-center justify-center">
                <span class="material-symbols-outlined">balance</span>
              </div>
              <h3 class="text-lg font-black text-gray-900">Tạo phiếu đối soát mới</h3>
            </div>
            <button @click="showReconModal = false" class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-200 text-gray-500 transition-colors">
              <span class="material-symbols-outlined text-[20px]">close</span>
            </button>
          </div>
          
          <div class="p-6 space-y-6">
            <div class="space-y-1.5">
              <label class="text-xs font-bold text-gray-700 uppercase tracking-widest">Chọn Quỹ Cần Đối Soát</label>
              <select v-model="reconForm.fundType" class="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm font-semibold text-gray-700 focus:outline-none focus:ring-2 focus:ring-amber-500/20 focus:border-amber-500 transition-all bg-white">
                <option value="CASH">Tiền mặt</option>
                <option value="BANK_TRANSFER">Ngân hàng</option>
                <option value="WALLET">Ví Nội Bộ SkyPay</option>
              </select>
            </div>
            
            <div class="p-4 bg-gray-50 border border-gray-100 rounded-xl flex justify-between items-center">
              <span class="text-sm font-bold text-gray-500">Số dư trên hệ thống hiện tại</span>
              <span class="text-xl font-black text-gray-900">{{ formatCurrency(stats[reconForm.fundType]?.balance || 0) }}</span>
            </div>

            <div class="space-y-1.5">
              <label class="text-xs font-bold text-gray-700 uppercase tracking-widest">Nhập Số Dư Thực Tế (VNĐ)</label>
              <div class="relative">
                <span class="absolute inset-y-0 left-0 flex items-center pl-4 text-gray-500 font-bold">đ</span>
                <input type="number" v-model="reconForm.actualBalance" placeholder="Ví dụ: 1000000" class="w-full pl-8 pr-4 py-3 border border-gray-200 rounded-xl text-sm font-black text-gray-900 focus:outline-none focus:ring-2 focus:ring-amber-500/20 focus:border-amber-500 transition-all" />
              </div>
            </div>

            <div v-if="reconForm.actualBalance !== null && reconForm.actualBalance !== ''" class="p-4 rounded-xl flex justify-between items-center" :class="reconDiscrepancy === 0 ? 'bg-gray-100 text-gray-700' : (reconDiscrepancy > 0 ? 'bg-emerald-50 text-emerald-700 border border-emerald-100' : 'bg-red-50 text-red-700 border border-red-100')">
              <span class="text-sm font-bold">Chênh lệch</span>
              <span class="text-lg font-black">{{ reconDiscrepancy > 0 ? '+' : '' }}{{ formatCurrency(reconDiscrepancy) }}</span>
            </div>
            <p v-if="reconDiscrepancy !== 0 && reconForm.actualBalance !== null && reconForm.actualBalance !== ''" class="text-xs font-medium text-gray-500 italic mt-2">
              Hệ thống sẽ tự động sinh giao dịch {{ reconDiscrepancy > 0 ? 'THU' : 'CHI' }} để cân bằng số dư thực tế.
            </p>

            <div class="space-y-1.5">
              <label class="text-xs font-bold text-gray-700 uppercase tracking-widest">Nguyên Nhân / Ghi Chú</label>
              <textarea v-model="reconForm.reason" rows="3" placeholder="Nhập nguyên nhân chênh lệch (nếu có)..." class="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm font-semibold text-gray-700 focus:outline-none focus:ring-2 focus:ring-amber-500/20 focus:border-amber-500 transition-all"></textarea>
            </div>
          </div>
          
          <div class="p-4 border-t border-gray-100 bg-gray-50 flex justify-end gap-3">
            <button @click="showReconModal = false" class="px-6 py-2.5 hover:bg-gray-200 text-gray-700 rounded-xl font-bold transition-colors">Hủy</button>
            <button @click="submitReconciliation" :disabled="submittingRecon || reconForm.actualBalance === null || reconForm.actualBalance === ''" class="px-6 py-2.5 bg-amber-600 text-white rounded-xl font-bold hover:bg-amber-700 transition-colors shadow-lg shadow-amber-600/20 disabled:opacity-50 flex items-center gap-2">
              <span v-if="submittingRecon" class="material-symbols-outlined animate-spin text-[20px]">progress_activity</span>
              Xác nhận đối soát
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  reconciliations: {
    type: Array,
    required: true,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  stats: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

const emit = defineEmits(['refresh'])

const showReconModal = ref(false)
const submittingRecon = ref(false)
const reconForm = ref({
  fundType: 'CASH',
  actualBalance: null,
  reason: ''
})

const reconDiscrepancy = computed(() => {
  if (reconForm.value.actualBalance === null || reconForm.value.actualBalance === '') return 0
  const systemBalance = props.stats[reconForm.value.fundType]?.balance || 0
  return Number(reconForm.value.actualBalance) - systemBalance
})

const openReconModal = () => {
  reconForm.value = {
    fundType: 'CASH',
    actualBalance: null,
    reason: ''
  }
  showReconModal.value = true
}

const submitReconciliation = async () => {
  if (reconForm.value.actualBalance === null || reconForm.value.actualBalance === '') return
  
  submittingRecon.value = true
  try {
    const token = localStorage.getItem('jwt_token')
    const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/admin/fund-reconciliations`, {
      method: 'POST',
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(reconForm.value)
    })
    if (res.ok) {
      alert('Tạo phiếu đối soát thành công!')
      showReconModal.value = false
      emit('refresh')
    } else {
      const errorData = await res.json()
      alert(`Lỗi: ${errorData.error}`)
    }
  } catch (error) {
    console.error('Error creating reconciliation:', error)
    alert('Đã xảy ra lỗi khi tạo đối soát.')
  } finally {
    submittingRecon.value = false
  }
}

const formatCurrency = (value) => {
  if (!value && value !== 0) return '0đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('vi-VN', { 
    hour: '2-digit', minute: '2-digit',
    day: '2-digit', month: '2-digit', year: 'numeric'
  }).format(date)
}

const getFilterLabel = (type) => {
  const map = {
    'ALL': 'Tất cả',
    'CASH': 'Tiền mặt',
    'BANK_TRANSFER': 'Ngân hàng',
    'WALLET': 'Ví nội bộ'
  }
  return map[type] || type
}

const getFundTypeBadge = (type) => {
  if (type === 'CASH') return 'bg-emerald-100 text-emerald-700'
  if (type === 'BANK_TRANSFER') return 'bg-blue-100 text-blue-700'
  if (type === 'WALLET') return 'bg-amber-100 text-amber-700'
  return 'bg-gray-100 text-gray-700'
}
</script>
