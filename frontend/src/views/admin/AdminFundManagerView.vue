<template>
  <div class="space-y-8">
    <FundStatsCards :stats="stats" />

    <!-- Main Tabs & Global Filters -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between border-b border-gray-200 mt-8 mb-4 pb-2 gap-4">
      <div class="flex items-center gap-2">
        <button 
          @click="activeMainTab = 'TRANSACTIONS'"
          class="px-6 py-3 text-sm font-black transition-all border-b-2"
          :class="activeMainTab === 'TRANSACTIONS' ? 'border-emerald-600 text-emerald-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'"
        >
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[18px]">receipt_long</span>
            Sổ giao dịch
          </div>
        </button>
        <button 
          @click="activeMainTab = 'RECONCILIATION'"
          class="px-6 py-3 text-sm font-black transition-all border-b-2"
          :class="activeMainTab === 'RECONCILIATION' ? 'border-amber-600 text-amber-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'"
        >
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[18px]">balance</span>
            Đối soát quỹ
          </div>
        </button>
        <button 
          @click="activeMainTab = 'EXPENSES'; authStore.clearNewExpensesCount()"
          class="px-6 py-3 text-sm font-black transition-all border-b-2"
          :class="activeMainTab === 'EXPENSES' ? 'border-purple-600 text-purple-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'"
        >
          <div class="flex items-center gap-2 relative">
            <span class="material-symbols-outlined text-[18px]">receipt</span>
            Duyệt chi phí
            <span v-if="authStore.newExpensesCount > 0" class="absolute -top-2 -right-3 flex items-center justify-center min-w-[20px] h-5 px-1 bg-red-500 text-white text-[10px] font-bold rounded-full border-2 border-white animate-bounce">
              {{ authStore.newExpensesCount }}
            </span>
          </div>
        </button>
        <button
          @click="activeMainTab = 'REFUNDS'"
          class="px-6 py-3 text-sm font-black transition-all border-b-2"
          :class="activeMainTab === 'REFUNDS' ? 'border-rose-600 text-rose-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'"
        >
          <div class="relative flex items-center gap-2">
            <span class="material-symbols-outlined text-[18px]">currency_exchange</span>
            Yêu cầu hoàn tiền
            <span v-if="authStore.newRefundsCount > 0" class="inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-rose-500 px-1.5 text-[10px] font-black tabular-nums text-white shadow-sm ring-2 ring-white">
              {{ authStore.newRefundsCount > 99 ? '99+' : authStore.newRefundsCount }}
            </span>
          </div>
        </button>
      </div>

      <div class="flex flex-wrap items-center gap-3" v-if="['TRANSACTIONS', 'RECONCILIATION'].includes(activeMainTab)">
        <button @click="refreshAll" class="w-10 h-10 flex items-center justify-center bg-slate-100 text-slate-600 border border-slate-200 rounded-lg hover:bg-slate-200 transition-colors" title="Làm mới dữ liệu">
          <span class="material-symbols-outlined text-[20px]" :class="{'animate-spin': loading || reconLoading}">refresh</span>
        </button>
        <select v-model="timeRange" @change="applyTimeFilter" class="px-4 py-2 border border-gray-200 rounded-lg text-sm font-semibold text-gray-700 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500">
          <option value="ALL">Tất cả thời gian</option>
          <option value="WEEK">Tuần này</option>
          <option value="MONTH">Tháng này</option>
          <option value="QUARTER">Quý này</option>
          <option value="YEAR">Năm nay</option>
          <option value="CUSTOM">Tùy chỉnh...</option>
        </select>
        
        <div v-if="timeRange === 'CUSTOM'" class="flex items-center gap-2">
          <input type="date" v-model="customStartDate" @change="applyCustomFilter" class="px-3 py-2 border border-gray-200 rounded-lg text-sm text-gray-700" />
          <span class="text-gray-400">-</span>
          <input type="date" v-model="customEndDate" @change="applyCustomFilter" class="px-3 py-2 border border-gray-200 rounded-lg text-sm text-gray-700" />
        </div>
      </div>
    </div>

    <!-- Ledger Table -->
    <FundTransactionsTable 
      v-if="activeMainTab === 'TRANSACTIONS'"
      :transactions="transactions"
      :loading="loading"
      :syncing="syncing"
      :activeFilter="activeFilter"
      @filter-changed="onFilterChanged"
      @sync-history="syncHistory"
      @export-report="exportReport"
    />

    <!-- Reconciliation Tab -->
    <FundReconciliationTab 
      v-else-if="activeMainTab === 'RECONCILIATION'"
      :reconciliations="reconciliations"
      :loading="reconLoading"
      :stats="stats"
      @refresh="refreshAll"
    />

    <!-- Expenses Tab -->
    <FundExpensesTab v-else-if="activeMainTab === 'EXPENSES'" />

    <!-- Refund requests Tab -->
    <AdminRefundManagerView v-else-if="activeMainTab === 'REFUNDS'" embedded />

  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import FundStatsCards from '@/components/admin/fund/FundStatsCards.vue'
import FundTransactionsTable from '@/components/admin/fund/FundTransactionsTable.vue'
import FundReconciliationTab from '@/components/admin/fund/FundReconciliationTab.vue'
import FundExpensesTab from '@/components/admin/fund/FundExpensesTab.vue'
import AdminRefundManagerView from '@/views/admin/AdminRefundManagerView.vue'
import { useAuthStore } from '@/stores/auth'
import { useRoute, useRouter } from 'vue-router'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const tabMap = { transactions: 'TRANSACTIONS', reconciliation: 'RECONCILIATION', expenses: 'EXPENSES', refunds: 'REFUNDS' }
const activeMainTab = ref(tabMap[String(route.query.tab || '').toLowerCase()] || 'TRANSACTIONS')

const loading = ref(false)
const syncing = ref(false)
const stats = ref({})
const transactions = ref([])
const activeFilter = ref('ALL')
const timeRange = ref('ALL')
const customStartDate = ref('')
const customEndDate = ref('')

// Reconciliation state
const reconLoading = ref(false)
const reconciliations = ref([])

const computedDateParams = () => {
  let start = null
  let end = null
  const now = new Date()
  
  if (timeRange.value === 'WEEK') {
    const day = now.getDay()
    const diff = now.getDate() - day + (day == 0 ? -6 : 1)
    start = new Date(now.setDate(diff))
    start.setHours(0,0,0,0)
    end = new Date(start)
    end.setDate(end.getDate() + 6)
    end.setHours(23,59,59,999)
  } else if (timeRange.value === 'MONTH') {
    start = new Date(now.getFullYear(), now.getMonth(), 1)
    start.setHours(0,0,0,0)
    end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
    end.setHours(23,59,59,999)
  } else if (timeRange.value === 'QUARTER') {
    const quarter = Math.floor(now.getMonth() / 3)
    start = new Date(now.getFullYear(), quarter * 3, 1)
    start.setHours(0,0,0,0)
    end = new Date(now.getFullYear(), start.getMonth() + 3, 0)
    end.setHours(23,59,59,999)
  } else if (timeRange.value === 'YEAR') {
    start = new Date(now.getFullYear(), 0, 1)
    start.setHours(0,0,0,0)
    end = new Date(now.getFullYear(), 11, 31)
    end.setHours(23,59,59,999)
  } else if (timeRange.value === 'CUSTOM') {
    if (customStartDate.value) {
      start = new Date(customStartDate.value)
      start.setHours(0,0,0,0)
    }
    if (customEndDate.value) {
      end = new Date(customEndDate.value)
      end.setHours(23,59,59,999)
    }
  }
  
  let query = ''
  if (start && end) {
    const tzOffset = start.getTimezoneOffset() * 60000
    const localStart = new Date(start.getTime() - tzOffset)
    const localEnd = new Date(end.getTime() - tzOffset)
    query = `&startDate=${localStart.toISOString().slice(0,-1)}&endDate=${localEnd.toISOString().slice(0,-1)}`
  }
  return query
}

const applyTimeFilter = () => {
  if (timeRange.value !== 'CUSTOM') refreshAll()
}

const applyCustomFilter = () => {
  if (customStartDate.value && customEndDate.value) refreshAll()
}

const onFilterChanged = (filter) => {
  activeFilter.value = filter
  fetchTransactions()
}

const fetchStats = async () => {
  try {
    const token = localStorage.getItem('jwt_token')
    const dateParams = computedDateParams().replace('&', '?')
    const url = dateParams ? `${import.meta.env.VITE_API_BASE_URL}/admin/funds/stats${dateParams}` : `${import.meta.env.VITE_API_BASE_URL}/admin/funds/stats`
    
    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (res.ok) {
      stats.value = await res.json()
    }
  } catch (error) {
    console.error('Error fetching fund stats:', error)
  }
}

const fetchTransactions = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('jwt_token')
    const dateParams = computedDateParams()
    const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/admin/funds/transactions?fundType=${activeFilter.value}${dateParams}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (res.ok) {
      transactions.value = await res.json()
    }
  } catch (error) {
    console.error('Error fetching fund transactions:', error)
  } finally {
    loading.value = false
  }
}

const fetchReconciliations = async () => {
  reconLoading.value = true
  try {
    const token = localStorage.getItem('jwt_token')
    const dateParams = computedDateParams().replace('&', '?')
    const url = dateParams ? `${import.meta.env.VITE_API_BASE_URL}/admin/fund-reconciliations${dateParams}` : `${import.meta.env.VITE_API_BASE_URL}/admin/fund-reconciliations`
    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (res.ok) {
      reconciliations.value = await res.json()
    }
  } catch (error) {
    console.error('Error fetching reconciliations:', error)
  } finally {
    reconLoading.value = false
  }
}

const refreshAll = () => {
  fetchStats()
  fetchTransactions()
  if (activeMainTab.value === 'RECONCILIATION') fetchReconciliations()
}

const syncHistory = async () => {
  if (!confirm('Bạn có chắc chắn muốn xóa sổ cái hiện tại và đồng bộ lại toàn bộ dữ liệu từ lịch sử đặt vé?')) return;
  
  syncing.value = true
  try {
    const token = localStorage.getItem('jwt_token')
    const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/admin/funds/sync-history`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (res.ok) {
      const data = await res.json()
      alert(`Đồng bộ thành công ${data.synced} giao dịch!`)
      refreshAll()
    } else {
      alert('Đồng bộ thất bại, vui lòng thử lại sau.')
    }
  } catch (error) {
    console.error('Error syncing history:', error)
  } finally {
    syncing.value = false
  }
}

const exportReport = async () => {
  try {
    const token = localStorage.getItem('jwt_token')
    const dateParams = computedDateParams()
    const url = `${import.meta.env.VITE_API_BASE_URL}/admin/funds/export?fundType=${activeFilter.value}${dateParams}`
    
    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    
    if (res.ok) {
      const blob = await res.blob()
      const downloadUrl = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = downloadUrl
      const dateStr = new Date().toISOString().slice(0,10)
      a.download = `bao-cao-quy-${activeFilter.value}-${timeRange.value}-${dateStr}.csv`
      document.body.appendChild(a)
      a.click()
      a.remove()
      window.URL.revokeObjectURL(downloadUrl)
    } else {
      alert('Xuất báo cáo thất bại.')
    }
  } catch (error) {
    console.error('Error exporting report:', error)
    alert('Đã xảy ra lỗi khi xuất báo cáo.')
  }
}

watch(activeMainTab, (newVal) => {
  const queryTab = newVal.toLowerCase()
  if (route.query.tab !== queryTab) router.replace({ query: { ...route.query, tab: queryTab } })
  if (newVal === 'RECONCILIATION' && reconciliations.value.length === 0) {
    fetchReconciliations()
  }
  if (newVal === 'EXPENSES') {
    authStore.clearNewExpensesCount()
  }
})

watch(() => route.query.tab, value => {
  const nextTab = tabMap[String(value || '').toLowerCase()]
  if (nextTab && nextTab !== activeMainTab.value) activeMainTab.value = nextTab
})

onMounted(() => {
  fetchStats()
  fetchTransactions()
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
