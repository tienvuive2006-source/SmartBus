<template>
  <Transition
    enter-active-class="transition duration-300 ease-out"
    enter-from-class="translate-y-3 opacity-0"
    enter-to-class="translate-y-0 opacity-100"
    leave-active-class="transition duration-200 ease-in"
    leave-from-class="translate-y-0 opacity-100"
    leave-to-class="translate-y-2 opacity-0"
  >
    <aside
      v-if="notice"
      class="fixed right-5 top-24 z-[100] flex w-[min(390px,calc(100vw-2.5rem))] items-start gap-3 rounded-2xl border bg-white p-4 shadow-[0_18px_50px_rgba(15,23,42,0.16)]"
      :class="notice.type === 'CHILD' ? 'border-sky-200' : 'border-amber-200'"
      role="status"
      aria-live="polite"
    >
      <div
        class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl"
        :class="notice.type === 'CHILD' ? 'bg-sky-50 text-sky-600' : 'bg-amber-50 text-amber-600'"
      >
        <span class="material-symbols-outlined text-[21px]">{{ notice.icon }}</span>
      </div>

      <div class="min-w-0 flex-1 pt-0.5">
        <p class="text-sm font-black text-slate-900">{{ notice.title }}</p>
        <p class="mt-1 text-xs font-medium leading-5 text-slate-600">{{ notice.message }}</p>
      </div>

      <button
        type="button"
        class="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-slate-700"
        aria-label="Đóng thông báo"
        @click="$emit('close')"
      >
        <span class="material-symbols-outlined text-[18px]">close</span>
      </button>
    </aside>
  </Transition>
</template>

<script setup>
defineProps({
  notice: {
    type: Object,
    default: null
  }
});

defineEmits(['close']);
</script>
