<template>
  <!-- Stats Cards -->
  <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
    <!-- CASH Fund -->
    <div class="bg-white rounded-3xl p-6 border border-emerald-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] relative overflow-hidden group">
      <div class="absolute top-0 right-0 w-32 h-32 bg-emerald-50 rounded-bl-full -z-10 group-hover:scale-110 transition-transform duration-500"></div>
      <div class="flex justify-between items-start mb-6">
        <div class="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-600 flex items-center justify-center">
          <span class="material-symbols-outlined text-2xl">payments</span>
        </div>
        <span class="px-3 py-1 bg-emerald-100 text-emerald-700 text-xs font-bold uppercase tracking-widest rounded-full">Tiền mặt</span>
      </div>
      <div>
        <p class="text-sm font-bold text-gray-500 mb-1 uppercase tracking-widest">Số dư Quỹ Tiền Mặt</p>
        <h3 class="text-3xl font-black text-gray-900">{{ formatCurrency(stats.CASH?.balance || 0) }}</h3>
        <div class="mt-4 flex gap-4 text-xs font-semibold text-gray-500">
           <div><span class="text-emerald-500">Thu:</span> {{ formatCurrency(stats.CASH?.totalIncome || 0) }}</div>
           <div><span class="text-red-500">Chi:</span> {{ formatCurrency(stats.CASH?.totalExpense || 0) }}</div>
        </div>
      </div>
    </div>

    <!-- BANK Fund -->
    <div class="bg-white rounded-3xl p-6 border border-blue-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] relative overflow-hidden group">
      <div class="absolute top-0 right-0 w-32 h-32 bg-blue-50 rounded-bl-full -z-10 group-hover:scale-110 transition-transform duration-500"></div>
      <div class="flex justify-between items-start mb-6">
        <div class="w-12 h-12 rounded-2xl bg-blue-100 text-blue-600 flex items-center justify-center">
          <span class="material-symbols-outlined text-2xl">qr_code_scanner</span>
        </div>
        <span class="px-3 py-1 bg-blue-100 text-blue-700 text-xs font-bold uppercase tracking-widest rounded-full">Ngân hàng</span>
      </div>
      <div>
        <p class="text-sm font-bold text-gray-500 mb-1 uppercase tracking-widest">Số dư Quỹ Chuyển Khoản</p>
        <h3 class="text-3xl font-black text-gray-900">{{ formatCurrency(stats.BANK_TRANSFER?.balance || 0) }}</h3>
        <div class="mt-4 flex gap-4 text-xs font-semibold text-gray-500">
           <div><span class="text-emerald-500">Thu:</span> {{ formatCurrency(stats.BANK_TRANSFER?.totalIncome || 0) }}</div>
           <div><span class="text-red-500">Chi:</span> {{ formatCurrency(stats.BANK_TRANSFER?.totalExpense || 0) }}</div>
        </div>
      </div>
    </div>

    <!-- WALLET Fund -->
    <div class="bg-white rounded-3xl p-6 border border-amber-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] relative overflow-hidden group">
      <div class="absolute top-0 right-0 w-32 h-32 bg-amber-50 rounded-bl-full -z-10 group-hover:scale-110 transition-transform duration-500"></div>
      <div class="flex justify-between items-start mb-6">
        <div class="w-12 h-12 rounded-2xl bg-amber-100 text-amber-600 flex items-center justify-center">
          <span class="material-symbols-outlined text-2xl">account_balance_wallet</span>
        </div>
        <span class="px-3 py-1 bg-amber-100 text-amber-700 text-xs font-bold uppercase tracking-widest rounded-full">Ví SkyPay</span>
      </div>
      <div>
        <p class="text-sm font-bold text-gray-500 mb-1 uppercase tracking-widest">Số dư Quỹ Nội Bộ</p>
        <h3 class="text-3xl font-black text-gray-900">{{ formatCurrency(stats.WALLET?.balance || 0) }}</h3>
        <div class="mt-4 flex gap-4 text-xs font-semibold text-gray-500">
           <div><span class="text-emerald-500">Thu:</span> {{ formatCurrency(stats.WALLET?.totalIncome || 0) }}</div>
           <div><span class="text-red-500">Chi:</span> {{ formatCurrency(stats.WALLET?.totalExpense || 0) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  stats: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

const formatCurrency = (value) => {
  if (!value && value !== 0) return '0đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}
</script>
