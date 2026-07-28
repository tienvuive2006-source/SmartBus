<template>
  <section class="bg-white rounded-3xl p-8 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
    <div class="flex justify-between items-center mb-6">
      <h3 class="text-sm font-black text-gray-900 uppercase tracking-widest">Ví Voucher của tôi</h3>
    </div>
    
    <div v-if="myVouchers.length === 0" class="text-center py-6 text-sm font-bold text-gray-400 border border-dashed border-gray-200 rounded-2xl">
      Bạn chưa đổi mã giảm giá nào
    </div>
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <div 
        v-for="uv in myVouchers" 
        :key="uv.id" 
        class="flex flex-col p-4 rounded-2xl border relative overflow-hidden"
        :class="uv.isUsed ? 'bg-gray-50 border-gray-200 opacity-70' : 'bg-emerald-50/50 border-emerald-100'"
      >
        <div class="flex justify-between items-start mb-2">
          <span class="font-mono font-black text-lg" :class="uv.isUsed ? 'text-gray-500' : 'text-emerald-700'">{{ uv.voucher.code }}</span>
          <span v-if="uv.isUsed" class="text-[10px] bg-gray-200 text-gray-600 px-2 py-1 rounded font-bold uppercase tracking-widest">Đã dùng</span>
          <span v-else class="text-[10px] bg-emerald-500 text-white px-2 py-1 rounded font-bold uppercase tracking-widest shadow-sm">Sẵn sàng</span>
        </div>
        <p class="text-sm font-bold text-gray-800 mb-1">Giảm {{ uv.voucher.discountAmount.toLocaleString('vi-VN') }}đ</p>
        <p class="text-[10px] text-gray-500">Đổi ngày: {{ new Date(uv.acquiredAt).toLocaleDateString('vi-VN') }}</p>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  myVouchers: {
    type: Array,
    required: true
  }
});
</script>
