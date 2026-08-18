<template>
  <div class="bg-white p-5 rounded-3xl shadow-sm border border-slate-100 mt-4" :class="collapsible ? 'mb-4' : 'mb-24'">
    <div class="flex flex-wrap items-center justify-between gap-3" :class="isOpen ? 'mb-4' : ''">
      <div class="flex min-w-0 items-center gap-3">
        <span class="material-symbols-outlined flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">payments</span>
        <div class="min-w-0">
          <h3 class="text-label-md font-bold text-slate-700 uppercase tracking-wider">Chi phí dọc đường</h3>
          <p v-if="collapsible" class="mt-0.5 text-xs font-semibold text-slate-400">{{ expenses.length }} khoản · {{ formatCurrency(totalAmount) }}</p>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button v-if="isOpen" @click="$emit('open-expense-modal')" class="text-sm font-bold text-emerald-600 bg-emerald-50 px-3 py-1.5 rounded-lg active:scale-95 transition-transform flex items-center gap-1">
          <span class="material-symbols-outlined text-sm">add</span> Khai báo
        </button>
        <button v-if="collapsible" type="button" class="expense-toggle" :aria-expanded="isOpen" @click="isOpen = !isOpen">
          {{ isOpen ? 'Thu gọn' : 'Xem chi phí' }}
          <span class="material-symbols-outlined" :class="isOpen ? 'rotate-180' : ''">expand_more</span>
        </button>
      </div>
    </div>

    <Transition name="expense-collapse">
      <div v-if="isOpen" class="expense-content border-t border-slate-100 pt-4">
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
    </Transition>
  </div>
</template>

<script setup>
import { computed, defineProps, defineEmits, ref } from 'vue'

const props = defineProps({
  expenses: { type: Array, required: true },
  loading: { type: Boolean, default: false },
  collapsible: { type: Boolean, default: false }
})

defineEmits(['open-expense-modal'])

const isOpen = ref(!props.collapsible)
const totalAmount = computed(() => props.expenses.reduce((total, expense) => total + Number(expense.amount || 0), 0))

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

<style scoped>
.expense-toggle { display: inline-flex; align-items: center; gap: .25rem; border-radius: .65rem; background: rgb(241 245 249); padding: .45rem .7rem; color: rgb(71 85 105); font-size: .72rem; font-weight: 800; transition: background .2s, color .2s, transform .2s; }
.expense-toggle:hover { background: rgb(226 232 240); color: rgb(15 23 42); }
.expense-toggle:active { transform: scale(.96); }
.expense-toggle .material-symbols-outlined { font-size: 1.1rem; transition: transform .2s; }
.expense-collapse-enter-active, .expense-collapse-leave-active { transition: opacity .2s ease, transform .2s ease; }
.expense-collapse-enter-from, .expense-collapse-leave-to { opacity: 0; transform: translateY(-.4rem); }
</style>
