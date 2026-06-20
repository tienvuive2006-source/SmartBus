<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
      <!-- Backdrop -->
      <div @click="$emit('close')" class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm animate-fade-in"></div>
      
      <!-- Modal Content -->
      <div class="bg-white w-full max-w-sm rounded-3xl shadow-2xl overflow-hidden animate-scale-up relative flex flex-col p-6 text-center">
        <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center text-red-500 mx-auto mb-4">
          <span class="material-symbols-outlined text-4xl">warning</span>
        </div>
        <h3 class="text-xl font-black text-slate-800 mb-2">Xác nhận xoá</h3>
        <p class="text-sm font-medium text-slate-500 mb-6">Bạn có chắc chắn muốn xoá người dùng này không? Hành động này không thể khôi phục!</p>
        
        <div class="flex gap-3">
          <button @click="$emit('close')" class="flex-1 px-4 py-3 rounded-xl text-sm font-bold text-slate-600 bg-slate-100 hover:bg-slate-200 transition-all active:scale-95">
            HỦY BỎ
          </button>
          <button @click="$emit('confirm')" :disabled="isDeleting" class="flex-1 px-4 py-3 rounded-xl text-sm font-bold text-white bg-red-500 hover:bg-red-600 shadow-lg shadow-red-500/30 transition-all active:scale-95 disabled:opacity-50">
            {{ isDeleting ? 'ĐANG XOÁ...' : 'XOÁ NGAY' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({
  isOpen: Boolean,
  isDeleting: Boolean
});

defineEmits(['close', 'confirm']);
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
@keyframes scaleUp {
  from { transform: scale(0.92); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}
</style>
