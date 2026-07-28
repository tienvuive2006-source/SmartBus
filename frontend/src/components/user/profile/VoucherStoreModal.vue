<template>
  <div v-if="show" class="fixed inset-0 z-[9999] bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
    <div class="bg-white rounded-3xl w-full max-w-lg shadow-2xl border border-slate-200 overflow-hidden transform transition-all flex flex-col max-h-[80vh]">
      <div class="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
        <div class="flex items-center gap-3">
          <span class="material-symbols-outlined text-amber-500 text-3xl">redeem</span>
          <div>
            <h3 class="text-lg font-black text-gray-900">Cửa hàng Đổi Điểm</h3>
            <p class="text-xs font-bold text-gray-500">Bạn đang có: <span class="text-amber-600">{{ user.loyaltyPoints || 0 }} pts</span></p>
          </div>
        </div>
        <button @click="$emit('close')" class="p-2 hover:bg-gray-200 rounded-full transition-colors text-gray-500">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
      
      <div class="p-6 overflow-y-auto custom-scrollbar bg-gray-50 flex-1">
        <div v-if="loading" class="text-center py-8">
          <div class="w-8 h-8 border-4 border-gray-200 border-t-amber-500 rounded-full animate-spin mx-auto"></div>
        </div>
        <div v-else-if="availableVouchers.length === 0" class="text-center py-8 text-gray-500 font-medium text-sm border-2 border-dashed border-gray-200 rounded-2xl">
          Hiện tại chưa có mã giảm giá nào để đổi.
        </div>
        <div v-else class="space-y-4">
          <div v-for="v in availableVouchers" :key="v.id" class="bg-white border border-gray-200 rounded-2xl p-5 flex items-center justify-between shadow-sm hover:shadow-md transition-shadow">
            <div>
              <span class="font-mono font-black text-lg text-emerald-600 block">{{ v.code }}</span>
              <span class="text-sm font-bold text-gray-800">Giảm {{ v.discountAmount.toLocaleString('vi-VN') }}đ</span>
            </div>
            <button 
              @click="$emit('redeem', v)"
              :disabled="redeeming || (user.loyaltyPoints || 0) < v.pointsCost"
              class="flex flex-col items-center justify-center px-4 py-2 rounded-xl transition-all font-bold text-sm min-w-[100px]"
              :class="(user.loyaltyPoints || 0) >= v.pointsCost ? 'bg-amber-500 hover:bg-amber-600 text-white shadow-md active:scale-95' : 'bg-gray-100 text-gray-400 cursor-not-allowed'"
            >
              <span v-if="redeeming" class="material-symbols-outlined animate-spin text-sm mb-1">progress_activity</span>
              <span v-else>ĐỔI NGAY</span>
              <span class="text-[10px]">{{ v.pointsCost.toLocaleString('vi-VN') }} pts</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  show: Boolean,
  user: {
    type: Object,
    required: true
  },
  availableVouchers: Array,
  loading: Boolean,
  redeeming: Boolean
});
defineEmits(['close', 'redeem']);
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 10px;
}
</style>
