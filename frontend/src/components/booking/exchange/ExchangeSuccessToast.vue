<template>
  <Transition name="exchange-toast">
    <div
      v-if="show"
      class="fixed right-4 top-5 z-[9999] w-[calc(100%-2rem)] max-w-sm overflow-hidden rounded-2xl border border-emerald-200 bg-white shadow-2xl shadow-slate-900/20 sm:right-6 sm:top-6"
      role="status"
      aria-live="polite"
    >
      <div class="flex items-start gap-3 p-4">
        <span class="material-symbols-outlined rounded-xl bg-emerald-100 p-2 text-2xl text-emerald-700">check_circle</span>
        <div class="min-w-0 flex-1 pt-0.5">
          <p class="font-black text-slate-900">{{ title }}</p>
          <p class="mt-1 text-sm font-medium leading-5 text-slate-600">{{ message }}</p>
        </div>
      </div>
      <div class="h-1 bg-emerald-100">
        <div class="exchange-toast__progress h-full bg-emerald-500" />
      </div>
    </div>
  </Transition>
</template>

<script setup>
defineProps({
  show: { type: Boolean, default: false },
  title: { type: String, default: 'Đổi vé thành công' },
  message: { type: String, default: '' }
})
</script>

<style scoped>
.exchange-toast-enter-active,
.exchange-toast-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.exchange-toast-enter-from,
.exchange-toast-leave-to {
  opacity: 0;
  transform: translateY(-12px) scale(0.98);
}

.exchange-toast__progress {
  animation: exchange-toast-progress 2.2s linear forwards;
}

@keyframes exchange-toast-progress {
  from { width: 100%; }
  to { width: 0%; }
}
</style>
