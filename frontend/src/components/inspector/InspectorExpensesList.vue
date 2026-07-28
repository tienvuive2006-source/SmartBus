<template>
  <div class="bg-white p-5 rounded-3xl shadow-sm border border-slate-100 mt-4 mb-24">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-label-md font-bold text-slate-500 uppercase tracking-wider">Chi phí dọc đường</h3>
      <button @click="$emit('open-expense-modal')" class="text-sm font-bold text-emerald-600 bg-emerald-50 px-3 py-1.5 rounded-lg active:scale-95 transition-transform flex items-center gap-1">
        <span class="material-symbols-outlined text-sm">add</span> Khai báo
      </button>
    </div>

    <div v-if="loading" class="text-center py-4 text-slate-400 text-sm">Đang tải...</div>
    <div v-else-if="expenses.length === 0" class="text-center py-6 text-slate-400 text-sm italic">
      Chưa có chi phí nào được khai báo.
    </div>
    
    <div v-else class="space-y-3">
      <div v-for="exp in expenses" :key="exp.id" class="p-3 rounded-2xl border border-slate-100 bg-slate-50 flex flex-col gap-2 relative overflow-hidden">
        <div class="absolute top-0 right-0 h-full w-1" :class="getStatusBorderColor(exp.status)"></div>
        <div class="flex justify-between items-start pr-2">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-slate-500 text-[20px]">{{ getExpenseIcon(exp.expenseType) }}</span>
            <span class="font-bold text-slate-800 text-sm">{{ getExpenseTypeName(exp.expenseType) }}</span>
          </div>
          <span class="font-black text-slate-900">{{ formatCurrency(exp.amount) }}</span>
        </div>
        <p v-if="exp.description" class="text-xs text-slate-500 line-clamp-2 pr-2">{{ exp.description }}</p>
        
        <div class="flex items-center justify-between mt-1">
          <span class="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-md" :class="getStatusBadgeClass(exp.status)">
            {{ getStatusText(exp.status) }}
          </span>
          <a v-if="exp.receiptImageUrl" :href="exp.receiptImageUrl" target="_blank" class="text-xs text-blue-500 font-bold flex items-center gap-1 hover:underline">
            <span class="material-symbols-outlined text-[14px]">receipt_long</span> Xem hóa đơn
          </a>
        </div>
        <div v-if="exp.status === 'REJECTED' && exp.rejectReason" class="mt-1 text-xs font-semibold text-error bg-error/10 p-2 rounded-lg">
          Lý do từ chối: {{ exp.rejectReason }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  expenses: { type: Array, required: true },
  loading: { type: Boolean, default: false }
})

defineEmits(['open-expense-modal'])

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

const getExpenseTypeName = (type) => {
  const types = {
    'FUEL': 'Tiền dầu',
    'TOLL': 'Cầu đường/BOT',
    'FOOD': 'Cơm ca',
    'REPAIR': 'Sửa chữa',
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

const getStatusBorderColor = (status) => {
  if (status === 'PENDING') return 'bg-amber-400'
  if (status === 'APPROVED') return 'bg-emerald-500'
  if (status === 'REJECTED') return 'bg-red-500'
  return 'bg-slate-300'
}
</script>
