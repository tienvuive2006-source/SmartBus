<template>
  <div class="bg-white rounded-[2rem] border border-gray-100 overflow-hidden shadow-[0_8px_30px_rgb(0,0,0,0.04)] animate-fade-in">
    <div class="p-6 border-b border-gray-100 flex flex-wrap gap-4 items-center justify-between bg-gray-50/50">
      <div class="flex flex-col gap-4">
        <div class="flex gap-2 bg-gray-100 p-1 rounded-xl w-fit">
          <button 
            v-for="filter in ['ALL', 'CASH', 'BANK_TRANSFER', 'WALLET']" 
            :key="filter"
            @click="$emit('filter-changed', filter)"
            class="px-6 py-2.5 rounded-lg text-sm font-bold transition-all duration-300"
            :class="activeFilter === filter ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700 hover:bg-gray-200/50'"
          >
            {{ getFilterLabel(filter) }}
          </button>
        </div>
      </div>
      
      <div class="flex items-center gap-3">
        <button @click="$emit('sync-history')" :disabled="syncing" class="flex items-center gap-2 px-6 py-2.5 bg-emerald-600 text-white rounded-xl text-sm font-bold hover:bg-emerald-700 transition-colors shadow-lg shadow-emerald-600/20 disabled:opacity-50">
          <span class="material-symbols-outlined text-lg">{{ syncing ? 'sync' : 'history' }}</span>
          {{ syncing ? 'Đang đồng bộ...' : 'Đồng bộ dữ liệu cũ' }}
        </button>
        
        <button @click="$emit('export-report')" class="flex items-center gap-2 px-6 py-2.5 bg-gray-900 text-white rounded-xl text-sm font-bold hover:bg-gray-800 transition-colors shadow-lg shadow-gray-900/20">
          <span class="material-symbols-outlined text-lg">download</span>
          Xuất Báo Cáo
        </button>
      </div>
    </div>

    <!-- Table -->
    <div class="overflow-x-auto">
      <table class="w-full">
        <thead>
          <tr class="bg-gray-50 border-b border-gray-100">
            <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest whitespace-nowrap">Thời gian</th>
            <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest whitespace-nowrap">Nguồn Quỹ</th>
            <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest whitespace-nowrap">Loại GD</th>
            <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest whitespace-nowrap">Số tiền</th>
            <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest">Nội dung</th>
            <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-widest whitespace-nowrap">Người thực hiện</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-if="loading" class="animate-pulse">
            <td colspan="6" class="px-6 py-12 text-center text-gray-400 font-medium">Đang tải dữ liệu sổ cái...</td>
          </tr>
          <tr v-else-if="transactions.length === 0">
            <td colspan="6" class="px-6 py-12 text-center text-gray-400 font-medium">Không có giao dịch nào trong quỹ này.</td>
          </tr>
          <tr 
            v-for="tx in paginatedTransactions" 
            :key="tx.id"
            class="hover:bg-gray-50/50 transition-colors group"
          >
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-600">
              {{ formatDateTime(tx.transactionDate) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="px-3 py-1 rounded-lg text-xs font-black uppercase tracking-widest" :class="getFundTypeBadge(tx.fundType)">
                {{ getFilterLabel(tx.fundType) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center gap-1.5" :class="tx.transactionType === 'INCOME' ? 'text-emerald-600' : 'text-red-600'">
                <span class="material-symbols-outlined text-[16px]">{{ tx.transactionType === 'INCOME' ? 'arrow_downward' : 'arrow_upward' }}</span>
                <span class="text-xs font-black uppercase tracking-widest">{{ tx.transactionType === 'INCOME' ? 'THU' : 'CHI' }}</span>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="text-sm font-black" :class="tx.transactionType === 'INCOME' ? 'text-emerald-600' : 'text-red-600'">
                {{ tx.transactionType === 'INCOME' ? '+' : '-' }}{{ formatCurrency(tx.amount) }}
              </span>
            </td>
            <td class="px-6 py-4">
              <div class="text-sm font-bold text-gray-900">{{ displayDescription(tx.description) }}</div>
              <div v-if="tx.referenceId" class="text-xs text-gray-500 mt-0.5 font-medium">Ref: {{ tx.referenceId }}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-600">
              {{ tx.performedBy || 'Hệ thống' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="p-4 border-t border-gray-100 flex items-center justify-between bg-white">
      <div class="text-sm font-medium text-gray-500">
        Hiển thị <span class="font-bold text-gray-900">{{ startIndex + 1 }}</span> - <span class="font-bold text-gray-900">{{ endIndex }}</span> trong tổng số <span class="font-bold text-gray-900">{{ transactions.length }}</span> giao dịch
      </div>
      <div class="flex items-center gap-2">
        <button 
          @click="currentPage--" 
          :disabled="currentPage === 1"
          class="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:hover:bg-transparent transition-colors"
        >
          <span class="material-symbols-outlined text-[20px]">chevron_left</span>
        </button>
        
        <div class="flex items-center gap-1">
          <button 
            v-for="page in visiblePages" 
            :key="page"
            @click="currentPage = page"
            class="w-8 h-8 flex items-center justify-center rounded-lg text-sm font-bold transition-colors"
            :class="currentPage === page ? 'bg-emerald-600 text-white' : 'text-gray-600 hover:bg-gray-50'"
          >
            {{ page }}
          </button>
        </div>

        <button 
          @click="currentPage++" 
          :disabled="currentPage === totalPages"
          class="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:hover:bg-transparent transition-colors"
        >
          <span class="material-symbols-outlined text-[20px]">chevron_right</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  transactions: {
    type: Array,
    required: true,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  syncing: {
    type: Boolean,
    default: false
  },
  activeFilter: {
    type: String,
    default: 'ALL'
  }
})

defineEmits(['filter-changed', 'sync-history', 'export-report'])

// Pagination
const currentPage = ref(1)
const itemsPerPage = 20

const totalPages = computed(() => Math.ceil(props.transactions.length / itemsPerPage))

const startIndex = computed(() => (currentPage.value - 1) * itemsPerPage)
const endIndex = computed(() => Math.min(startIndex.value + itemsPerPage, props.transactions.length))

const paginatedTransactions = computed(() => {
  return props.transactions.slice(startIndex.value, endIndex.value)
})

const visiblePages = computed(() => {
  let pages = []
  const maxVisible = 5
  let startPage = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let endPage = startPage + maxVisible - 1
  
  if (endPage > totalPages.value) {
    endPage = totalPages.value
    startPage = Math.max(1, endPage - maxVisible + 1)
  }
  
  for (let i = startPage; i <= endPage; i++) {
    pages.push(i)
  }
  return pages
})

watch(() => props.transactions, () => {
  currentPage.value = 1
})

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

const displayDescription = description => String(description || '')
  .replace(/\s*\(SePay Polling\)/gi, '')

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
