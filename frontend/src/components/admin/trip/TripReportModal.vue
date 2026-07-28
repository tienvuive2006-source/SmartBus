<template>
  <Teleport to="body">
    <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
      <div class="bg-white rounded-3xl w-full max-w-md shadow-2xl overflow-hidden flex flex-col">
        <div class="p-5 border-b border-slate-100 flex items-center justify-between bg-slate-50">
          <h3 class="text-lg font-black text-slate-900 flex items-center gap-2">
            <span class="material-symbols-outlined text-emerald-600">monitoring</span> 
            Báo cáo tài chính
          </h3>
          <button @click="$emit('close')" class="w-8 h-8 flex items-center justify-center rounded-full bg-slate-200 text-slate-600 hover:bg-slate-300">
            <span class="material-symbols-outlined text-[20px]">close</span>
          </button>
        </div>

        <div v-if="loading" class="p-12 flex justify-center">
          <span class="w-8 h-8 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></span>
        </div>

        <div v-else class="p-6 space-y-6">
          <div class="text-center">
            <div class="text-sm font-bold text-slate-500 uppercase tracking-widest mb-1">Chuyến xe</div>
            <div class="text-xl font-black text-slate-900">#{{ trip?.id }}</div>
            <div class="text-xs text-slate-400 mt-1">{{ trip?.departurePoint }} ➔ {{ trip?.arrivalPoint }}</div>
          </div>

          <div class="space-y-3">
            <div class="flex items-center justify-between p-4 bg-emerald-50 rounded-2xl border border-emerald-100">
              <div class="flex items-center gap-2 text-emerald-700">
                <span class="material-symbols-outlined">payments</span>
                <span class="font-bold text-sm">Tổng thu (Bán vé)</span>
              </div>
              <span class="text-lg font-black text-emerald-700">{{ formatCurrency(reportData?.totalRevenue) }}</span>
            </div>

            <div class="flex items-center justify-between p-4 bg-rose-50 rounded-2xl border border-rose-100">
              <div class="flex items-center gap-2 text-rose-700">
                <span class="material-symbols-outlined">receipt_long</span>
                <span class="font-bold text-sm">Tổng chi (Đã duyệt)</span>
              </div>
              <span class="text-lg font-black text-rose-700">{{ formatCurrency(reportData?.totalExpense) }}</span>
            </div>
          </div>

          <div class="border-t-2 border-dashed border-slate-200 pt-6">
            <div class="flex items-center justify-between p-4 bg-slate-900 rounded-2xl shadow-lg">
              <span class="font-bold text-sm text-slate-300 uppercase tracking-widest">Lợi nhuận gộp</span>
              <span class="text-2xl font-black text-white">{{ formatCurrency(reportData?.netProfit) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  trip: Object
})

defineEmits(['close'])

const loading = ref(false)
const reportData = ref(null)

const fetchReport = async () => {
  if (!props.trip) return
  loading.value = true
  try {
    const token = localStorage.getItem('jwt_token')
    const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/admin/expenses/trips/${props.trip.id}/report`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (res.ok) {
      reportData.value = await res.json()
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

watch(() => props.show, (newVal) => {
  if (newVal) {
    fetchReport()
  } else {
    reportData.value = null
  }
})

const formatCurrency = (value) => {
  if (value == null) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}
</script>
