<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
    <!-- Wallet Card -->
    <section class="bg-[#075955] text-white p-8 rounded-3xl shadow-lg shadow-[#075955]/20 relative overflow-hidden group">
      <div class="absolute -right-8 -top-8 w-32 h-32 bg-white/10 rounded-full group-hover:scale-150 transition-transform duration-500 blur-xl"></div>
      <div class="relative z-10">
        <div class="flex items-center gap-3 mb-6">
          <span class="material-symbols-outlined text-emerald-300">account_balance_wallet</span>
          <p class="text-[10px] font-black uppercase tracking-widest text-emerald-100">Ví điện tử</p>
        </div>
        <p class="text-xs font-semibold text-emerald-100 mb-1">Số dư khả dụng</p>
        <h3 class="text-3xl font-black tracking-tight mb-8">
          {{ user.walletBalance ? user.walletBalance.toLocaleString('vi-VN') : '0' }}<span class="text-lg ml-1 text-emerald-200">₫</span>
        </h3>
        <button @click="$emit('open-topup')" class="w-full bg-white text-[#075955] hover:bg-gray-50 text-xs font-black uppercase tracking-widest py-3.5 rounded-xl transition-all duration-200 shadow-sm active:scale-95">
          Nạp thêm tiền
        </button>
      </div>
    </section>

    <!-- Points Card -->
    <section class="bg-white p-8 rounded-3xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-gray-100 relative overflow-hidden flex flex-col h-full">
      <div class="flex items-center gap-3 mb-6">
        <span class="material-symbols-outlined text-amber-500">stars</span>
        <p class="text-[10px] font-black uppercase tracking-widest text-gray-500">Điểm thưởng</p>
      </div>
      <p class="text-xs font-semibold text-gray-500 mb-1">Loyalty Points</p>
      <h3 class="text-3xl font-black tracking-tight text-gray-900 mb-6">
        {{ user.loyaltyPoints ? user.loyaltyPoints.toLocaleString('vi-VN') : '0' }}<span class="text-lg ml-1 text-gray-400 font-semibold tracking-normal">pts</span>
      </h3>
      
      <div class="mt-auto">
        <button @click="$emit('open-voucher-store')" class="w-full flex items-center justify-between bg-amber-50 hover:bg-amber-100 p-4 rounded-xl border border-amber-200 transition-colors cursor-pointer group">
          <div class="flex items-center gap-3">
            <span class="material-symbols-outlined text-amber-600 text-lg group-hover:scale-110 transition-transform">redeem</span>
            <p class="text-sm font-bold text-amber-800">Cửa hàng Đổi Điểm</p>
          </div>
          <span class="material-symbols-outlined text-amber-600 text-sm">arrow_forward_ios</span>
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
defineProps({
  user: {
    type: Object,
    required: true
  }
});
defineEmits(['open-topup', 'open-voucher-store']);
</script>
