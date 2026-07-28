<template>
  <div class="space-y-6">
    <!-- Header & Filter -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div class="flex gap-2 overflow-x-auto pb-2 sm:pb-0">
        <button v-for="filter in filters" :key="filter.id" @click="activeFilter = filter.id"
          class="px-4 py-2 rounded-xl text-sm font-bold border transition-colors shrink-0"
          :class="activeFilter === filter.id ? 'bg-slate-900 text-white border-slate-900' : 'bg-white text-slate-500 border-slate-200 hover:bg-slate-50'">
          {{ filter.label }}
        </button>
      </div>
      <button @click="fetchExpenses" class="flex items-center justify-center gap-2 px-4 py-2 bg-slate-100 text-slate-600 border border-slate-200 rounded-xl font-bold hover:bg-slate-200 transition-colors shrink-0">
        <span class="material-symbols-outlined text-[20px]" :class="{'animate-spin': loading}">refresh</span>
        <span>Làm mới</span>
      </button>
    </div>

    <!-- Expenses List -->
    <div v-if="loading" class="flex justify-center py-12">
      <span class="w-8 h-8 border-4 border-slate-900 border-t-transparent rounded-full animate-spin"></span>
    </div>

    <div v-else-if="filteredExpenses.length === 0" class="bg-white rounded-3xl border border-slate-100 p-12 text-center flex flex-col items-center justify-center">
      <div class="w-20 h-20 rounded-full bg-slate-50 flex items-center justify-center mb-4">
        <span class="material-symbols-outlined text-4xl text-slate-300">receipt_long</span>
      </div>
      <p class="text-slate-500 font-medium">Không có khoản chi phí nào phù hợp với bộ lọc hiện tại.</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="group in groupedExpenses" :key="group.key" class="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden flex flex-col">
        <div class="p-5 flex-1 flex flex-col space-y-4">
          <div class="flex items-center justify-between">
            <span class="text-[10px] font-bold px-2 py-1 rounded-lg uppercase tracking-wider flex items-center gap-1 bg-slate-100 text-slate-700 border border-slate-200">
              <span class="material-symbols-outlined text-[14px]">receipt_long</span>
              {{ group.expenses.length }} KHOẢN CHI
            </span>
            <span class="text-[10px] font-bold px-2 py-1 rounded-md uppercase tracking-wider" :class="getStatusBadgeClass(group.status)">
              {{ getStatusText(group.status) }}
            </span>
          </div>

          <div>
            <h4 class="text-2xl font-black text-slate-900">{{ formatCurrency(group.totalAmount) }}</h4>
          </div>

          <div class="text-sm text-slate-600 bg-slate-50 p-3 rounded-xl border border-slate-100 space-y-2">
            <div class="flex items-center justify-between border-b border-slate-200/60 pb-2 mb-2">
               <div class="flex items-center gap-1.5">
                  <span class="material-symbols-outlined text-[16px] text-slate-400">tag</span>
                  <span class="font-bold text-slate-900">Chuyến #{{ group.trip?.id || 'N/A' }}</span>
               </div>
               <div class="text-[10px] font-semibold text-slate-500 bg-white px-2 py-0.5 rounded border border-slate-200" v-if="group.trip?.departureDate">
                 {{ group.trip.departureDate }}
               </div>
            </div>
            
            <p class="flex items-start gap-2" v-if="group.trip?.departurePoint && group.trip?.arrivalPoint">
              <span class="material-symbols-outlined text-[16px] text-slate-400 mt-0.5">route</span>
              <span class="font-medium text-slate-900">{{ group.trip.departurePoint }} <span class="text-slate-400 mx-0.5">→</span> {{ group.trip.arrivalPoint }}</span>
            </p>
            <p class="flex items-center gap-2" v-if="group.trip?.assignedDriverFullName || group.trip?.assignedDriverUsername">
              <span class="material-symbols-outlined text-[16px] text-slate-400">person</span>
              <span>Tài xế: <span class="font-bold text-slate-900">{{ group.trip.assignedDriverFullName || group.trip.assignedDriverUsername }}</span></span>
            </p>
            <p class="flex items-center gap-2" v-if="group.trip?.busType || group.trip?.assignedLicensePlate">
              <span class="material-symbols-outlined text-[16px] text-slate-400">directions_car</span>
              <span>Xe: <span class="font-bold text-slate-900">{{ group.trip.assignedLicensePlate ? group.trip.assignedLicensePlate + ' - ' + group.trip.busType : group.trip.busType }}</span></span>
            </p>
          </div>

          <button @click="toggleExpand(group.key)" class="w-full flex items-center justify-center gap-1 py-2 text-xs font-bold text-slate-500 hover:text-slate-700 bg-slate-50 hover:bg-slate-100 rounded-xl transition-colors border border-slate-200/60">
             <span>{{ expandedState[group.key] ? 'Ẩn chi tiết các khoản' : 'Xem chi tiết các khoản' }}</span>
             <span class="material-symbols-outlined text-[18px] transition-transform duration-200" :class="{'rotate-180': expandedState[group.key]}">expand_more</span>
          </button>

          <div v-show="expandedState[group.key]" class="space-y-2">
            <div v-for="exp in group.expenses" :key="exp.id" class="text-sm text-slate-600 bg-white p-3 rounded-xl border border-slate-100 shadow-sm">
              <div class="flex justify-between items-center mb-1">
                <span class="font-bold text-[10px] px-1.5 py-0.5 rounded uppercase tracking-wider flex items-center gap-1" :class="getTypeClass(exp.expenseType)">
                  <span class="material-symbols-outlined text-[12px]">{{ getExpenseIcon(exp.expenseType) }}</span>
                  {{ getExpenseTypeName(exp.expenseType) }}
                </span>
                <span class="font-black text-slate-800">{{ formatCurrency(exp.amount) }}</span>
              </div>
              <p class="font-medium text-slate-500 mb-2 mt-2 text-[13px]">{{ exp.description || 'Không có ghi chú' }}</p>
              
              <div class="flex items-center justify-between mt-2 pt-2 border-t border-slate-100/60">
                 <a v-if="exp.receiptImageUrl" :href="exp.receiptImageUrl" target="_blank" class="text-blue-600 font-bold text-[11px] flex items-center gap-1 hover:underline">
                   <span class="material-symbols-outlined text-[14px]">image</span> Xem hóa đơn
                 </a>
                 <span v-else class="text-[10px] text-slate-400 italic">Không có hóa đơn</span>
              </div>
              
              <div v-if="exp.status === 'REJECTED' && exp.rejectReason" class="mt-2 text-[11px] font-semibold text-error bg-error/10 p-1.5 rounded">
                 Lý do: {{ exp.rejectReason }}
              </div>
            </div>
          </div>

          <div class="flex items-center justify-between text-xs font-medium text-slate-500 pt-4 border-t border-slate-100 mt-auto">
            <span>Bởi: <span class="font-bold text-slate-800">{{ group.reportedBy }}</span></span>
            <span>{{ new Date(group.createdAt).toLocaleString('vi-VN') }}</span>
          </div>
        </div>

        <!-- Action Area -->
        <div v-if="group.status === 'PENDING'" class="p-4 border-t border-slate-100 bg-slate-50 flex gap-2">
          <button @click="openApproveModal(group)" class="flex-1 py-2.5 px-1 sm:px-2 bg-emerald-600 text-white rounded-xl font-bold hover:bg-emerald-700 transition-colors flex items-center justify-center gap-1 text-[12px] sm:text-[13px]">
            <span class="material-symbols-outlined text-[18px]">done_all</span> Duyệt tất cả
          </button>
          <button @click="openRejectModal(group)" class="flex-1 py-2.5 px-1 sm:px-2 bg-white text-error border border-error/20 rounded-xl font-bold hover:bg-error/5 transition-colors flex items-center justify-center gap-1 text-[12px] sm:text-[13px]">
            <span class="material-symbols-outlined text-[18px]">close</span> Từ chối
          </button>
          <button @click="toggleExpand(group.key)" class="w-[42px] shrink-0 bg-slate-200 text-slate-600 border border-slate-300/50 rounded-xl font-bold hover:bg-slate-300 hover:text-slate-800 transition-colors flex items-center justify-center" title="Xem chi tiết các khoản chi">
            <span class="material-symbols-outlined text-[20px]">{{ expandedState[group.key] ? 'visibility_off' : 'visibility' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Approve Modal -->
    <Teleport to="body">
      <div v-if="showApproveModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
        <div class="bg-white rounded-3xl w-full max-w-md shadow-2xl p-6">
          <h3 class="text-xl font-black text-slate-900 mb-2">Duyệt chi phí</h3>
          <p class="text-slate-500 text-sm mb-6">Bạn sẽ duyệt toàn bộ <strong class="text-slate-900">{{ selectedGroup?.expenses.length }}</strong> khoản chi (tổng <strong class="text-slate-900">{{ formatCurrency(selectedGroup?.totalAmount) }}</strong>) vào quỹ nào?</p>
          
          <div class="space-y-4 mb-8">
            <label class="flex items-center justify-between p-4 rounded-2xl border-2 cursor-pointer transition-all"
                   :class="approveForm.fundType === 'CASH' ? 'border-slate-900 bg-slate-50' : 'border-slate-100 hover:border-slate-200'">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center">
                  <span class="material-symbols-outlined">payments</span>
                </div>
                <div>
                  <div class="font-bold text-slate-900">Quỹ Tiền mặt</div>
                  <div class="text-xs text-slate-500">Trừ trực tiếp vào két</div>
                </div>
              </div>
              <input type="radio" v-model="approveForm.fundType" value="CASH" class="w-5 h-5 accent-slate-900" />
            </label>

            <label class="flex items-center justify-between p-4 rounded-2xl border-2 cursor-pointer transition-all"
                   :class="approveForm.fundType === 'BANK' ? 'border-slate-900 bg-slate-50' : 'border-slate-100 hover:border-slate-200'">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center">
                  <span class="material-symbols-outlined">account_balance</span>
                </div>
                <div>
                  <div class="font-bold text-slate-900">Quỹ Ngân hàng</div>
                  <div class="text-xs text-slate-500">Trừ vào tài khoản NH</div>
                </div>
              </div>
              <input type="radio" v-model="approveForm.fundType" value="BANK" class="w-5 h-5 accent-slate-900" />
            </label>

            <label class="flex items-center justify-between p-4 rounded-2xl border-2 cursor-pointer transition-all"
                   :class="approveForm.fundType === 'WALLET' ? 'border-slate-900 bg-slate-50' : 'border-slate-100 hover:border-slate-200'">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-purple-100 text-purple-600 flex items-center justify-center">
                  <span class="material-symbols-outlined">account_balance_wallet</span>
                </div>
                <div>
                  <div class="font-bold text-slate-900">Quỹ Ví nội bộ</div>
                  <div class="text-xs text-slate-500">Trừ vào số dư ví HT</div>
                </div>
              </div>
              <input type="radio" v-model="approveForm.fundType" value="WALLET" class="w-5 h-5 accent-slate-900" />
            </label>
          </div>

          <div class="flex gap-3">
            <button @click="showApproveModal = false" class="flex-1 py-3 bg-slate-100 text-slate-700 font-bold rounded-xl hover:bg-slate-200 transition-colors">Hủy</button>
            <button @click="submitApprove" :disabled="processing" class="flex-1 py-3 bg-emerald-600 text-white font-bold rounded-xl hover:bg-emerald-700 transition-colors flex items-center justify-center gap-2">
              <span v-if="processing" class="material-symbols-outlined animate-spin text-[20px]">progress_activity</span>
              Xác nhận Duyệt
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Reject Modal -->
    <Teleport to="body">
      <div v-if="showRejectModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
        <div class="bg-white rounded-3xl w-full max-w-md shadow-2xl p-6">
          <h3 class="text-xl font-black text-slate-900 mb-2">Từ chối chi phí</h3>
          <p class="text-slate-500 text-sm mb-6">Toàn bộ <strong class="text-slate-900">{{ selectedGroup?.expenses.length }}</strong> khoản chi (tổng <strong class="text-slate-900">{{ formatCurrency(selectedGroup?.totalAmount) }}</strong>) sẽ bị từ chối.</p>
          
          <div class="mb-8">
            <label class="text-xs font-bold text-slate-500 uppercase tracking-widest mb-1.5 block">Lý do từ chối</label>
            <textarea v-model="rejectForm.reason" rows="3" placeholder="Nhập lý do chi tiết để tài xế biết..." class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm font-medium focus:outline-none focus:border-error"></textarea>
          </div>

          <div class="flex gap-3">
            <button @click="showRejectModal = false" class="flex-1 py-3 bg-slate-100 text-slate-700 font-bold rounded-xl hover:bg-slate-200 transition-colors">Hủy</button>
            <button @click="submitReject" :disabled="processing || !rejectForm.reason" class="flex-1 py-3 bg-error text-white font-bold rounded-xl hover:bg-red-600 transition-colors disabled:opacity-50 flex items-center justify-center gap-2">
              <span v-if="processing" class="material-symbols-outlined animate-spin text-[20px]">progress_activity</span>
              Từ chối
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const expenses = ref([])
const loading = ref(true)
const processing = ref(false)
const expandedState = ref({})

const toggleExpand = (key) => {
  expandedState.value[key] = !expandedState.value[key]
}

const filters = [
  { id: 'PENDING', label: '⏳ Chờ duyệt' },
  { id: 'APPROVED', label: '✅ Đã duyệt' },
  { id: 'REJECTED', label: '❌ Bị từ chối' },
  { id: 'ALL', label: 'Tất cả' }
]
const activeFilter = ref('PENDING')

const filteredExpenses = computed(() => {
  if (activeFilter.value === 'ALL') return expenses.value
  return expenses.value.filter(e => e.status === activeFilter.value)
})

const groupedExpenses = computed(() => {
  const groups = {}
  filteredExpenses.value.forEach(exp => {
    const tripId = exp.trip?.id || 'NO_TRIP'
    const key = `${tripId}_${exp.status}`
    
    if (!groups[key]) {
      groups[key] = {
        key,
        trip: exp.trip,
        status: exp.status,
        reportedBy: exp.reportedBy,
        createdAt: exp.createdAt,
        expenses: [],
        totalAmount: 0
      }
    }
    groups[key].expenses.push(exp)
    groups[key].totalAmount += exp.amount
  })
  
  return Object.values(groups).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

const fetchExpenses = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('jwt_token')
    const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/admin/expenses`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (res.ok) {
      expenses.value = await res.json()
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchExpenses()
})

// Modals
const showApproveModal = ref(false)
const showRejectModal = ref(false)
const selectedGroup = ref(null)

const approveForm = ref({ fundType: 'CASH' })
const rejectForm = ref({ reason: '' })

const openApproveModal = (group) => {
  selectedGroup.value = group
  approveForm.value.fundType = 'CASH'
  showApproveModal.value = true
}

const openRejectModal = (group) => {
  selectedGroup.value = group
  rejectForm.value.reason = ''
  showRejectModal.value = true
}

const submitApprove = async () => {
  processing.value = true
  try {
    const token = localStorage.getItem('jwt_token')
    const userPhone = localStorage.getItem('user_phone')
    let hasError = false;

    // Approve each expense sequentially
    for (const exp of selectedGroup.value.expenses) {
      const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/admin/expenses/${exp.id}/approve`, {
        method: 'PUT',
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ 
          fundType: approveForm.value.fundType,
          approvedBy: userPhone 
        })
      })
      if (!res.ok) {
        hasError = true;
      }
    }

    if (hasError) {
      alert('Đã xảy ra lỗi khi duyệt một vài khoản chi phí. Vui lòng kiểm tra lại.')
    }
    
    await fetchExpenses()
    showApproveModal.value = false
  } catch (error) {
    console.error(error)
  } finally {
    processing.value = false
  }
}

const submitReject = async () => {
  processing.value = true
  try {
    const token = localStorage.getItem('jwt_token')
    let hasError = false;

    // Reject each expense sequentially
    for (const exp of selectedGroup.value.expenses) {
      const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/admin/expenses/${exp.id}/reject`, {
        method: 'PUT',
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ rejectReason: rejectForm.value.reason })
      })
      if (!res.ok) {
        hasError = true;
      }
    }

    if (hasError) {
      alert('Đã xảy ra lỗi khi từ chối một vài khoản chi phí. Vui lòng kiểm tra lại.')
    }

    await fetchExpenses()
    showRejectModal.value = false
  } catch (error) {
    console.error(error)
  } finally {
    processing.value = false
  }
}

// Formatters
const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

const getExpenseTypeName = (type) => {
  const types = {
    'FUEL': 'Tiền dầu',
    'TOLL': 'Cầu đường/BOT',
    'FOOD': 'Cơm ca',
    'REPAIR': 'Sửa xe',
    'OTHER': 'Khác'
  }
  return types[type] || type
}

const getExpenseIcon = (type) => {
  const icons = {
    'FUEL': 'local_gas_station',
    'TOLL': 'toll',
    'FOOD': 'restaurant',
    'REPAIR': 'build',
    'OTHER': 'receipt_long'
  }
  return icons[type] || 'receipt_long'
}

const getTypeClass = (type) => {
  if (type === 'FUEL') return 'bg-orange-100 text-orange-700 border border-orange-200'
  if (type === 'TOLL') return 'bg-blue-100 text-blue-700 border border-blue-200'
  if (type === 'FOOD') return 'bg-amber-100 text-amber-700 border border-amber-200'
  if (type === 'REPAIR') return 'bg-purple-100 text-purple-700 border border-purple-200'
  return 'bg-slate-100 text-slate-700 border border-slate-200'
}

const getStatusText = (status) => {
  const statuses = {
    'PENDING': 'Đang chờ duyệt',
    'APPROVED': 'Đã duyệt',
    'REJECTED': 'Bị từ chối'
  }
  return statuses[status] || status
}

const getStatusBadgeClass = (status) => {
  if (status === 'PENDING') return 'bg-amber-100 text-amber-700'
  if (status === 'APPROVED') return 'bg-emerald-100 text-emerald-700'
  if (status === 'REJECTED') return 'bg-red-100 text-red-700'
  return 'bg-slate-100 text-slate-700'
}
</script>
