<template>
  <section class="bg-white rounded-3xl p-8 border border-gray-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
    <div class="flex justify-between items-center mb-6">
      <h3 class="text-sm font-black text-gray-900 uppercase tracking-widest">Lịch sử giao dịch</h3>
    </div>
    
    <div v-if="transactions.length === 0" class="text-center py-6 text-sm font-bold text-gray-400">
      Chưa có giao dịch nào
    </div>
    <div v-else class="space-y-4 max-h-[300px] overflow-y-auto pr-2 custom-scrollbar">
      <div v-for="txn in transactions" :key="txn.id" class="flex items-center justify-between p-4 rounded-2xl border border-gray-100 bg-gray-50/50 hover:bg-gray-50 transition-colors">
        <div class="flex items-center gap-4">
          <div class="w-10 h-10 rounded-full flex items-center justify-center shrink-0" :class="txn.amount > 0 ? 'bg-emerald-100 text-emerald-600' : 'bg-rose-100 text-rose-600'">
            <span class="material-symbols-outlined">{{ txn.amount > 0 ? 'south_west' : 'north_east' }}</span>
          </div>
          <div>
            <h4 class="text-sm font-black text-gray-900">{{ txn.title }}</h4>
            <p class="text-xs font-medium text-gray-500">{{ txn.description }}</p>
            <p class="text-[10px] font-bold text-gray-400 mt-1">{{ new Date(txn.date).toLocaleString('vi-VN') }}</p>
          </div>
        </div>
        <div class="text-right">
          <p class="text-base font-black tracking-tight" :class="txn.color">
            {{ txn.amount > 0 ? '+' : '' }}{{ txn.amount.toLocaleString('vi-VN') }}₫
          </p>
          <span class="text-[10px] font-black uppercase tracking-widest" :class="txn.status === 'Thành công' ? 'text-emerald-500' : 'text-amber-500'">
            {{ txn.status }}
          </span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  transactions: {
    type: Array,
    required: true
  }
});
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
