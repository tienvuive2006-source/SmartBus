<template>
  <div v-if="show" class="fixed inset-0 z-[9999] bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
    <div class="bg-white rounded-3xl w-full max-w-sm shadow-2xl border border-slate-200 overflow-hidden transform transition-all">
      <div class="p-6 text-center">
        <div class="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-4">
          <span class="material-symbols-outlined text-3xl text-red-500">logout</span>
        </div>
        <h3 class="text-lg font-black text-slate-800 mb-2">Xác nhận đăng xuất</h3>
        <p class="text-sm font-medium text-slate-500 mb-6">Bạn có chắc chắn muốn đăng xuất khỏi hệ thống Trung Nam không?</p>
        
        <div class="flex gap-3">
          <button @click="$emit('close')" class="flex-1 py-3 px-4 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-sm font-bold transition-colors">
            Hủy bỏ
          </button>
          <button @click="$emit('confirm')" class="flex-1 py-3 px-4 bg-red-500 hover:bg-red-600 text-white rounded-xl text-sm font-bold shadow-lg shadow-red-500/30 transition-all active:scale-95">
            Đăng xuất
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  show: Boolean
});
defineEmits(['close', 'confirm']);
</script>
