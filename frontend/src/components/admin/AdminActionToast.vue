<template>
  <Teleport to="body">
    <Transition name="admin-toast">
      <div
        v-if="show"
        class="admin-action-toast"
        :class="type === 'success' ? 'admin-action-toast--success' : 'admin-action-toast--error'"
        role="status"
        aria-live="polite"
      >
        <span class="admin-action-toast__icon material-symbols-outlined">
          {{ type === 'success' ? 'check_circle' : 'error' }}
        </span>
        <div class="min-w-0 flex-1">
          <p class="text-sm font-black text-slate-900">
            {{ type === 'success' ? 'Lưu thành công' : 'Lưu thất bại' }}
          </p>
          <p class="mt-0.5 text-xs font-semibold leading-5 text-slate-500">{{ message }}</p>
        </div>
        <button type="button" class="admin-action-toast__close" aria-label="Đóng thông báo" @click="$emit('close')">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
defineProps({
  show: Boolean,
  type: { type: String, default: 'success' },
  message: { type: String, default: '' }
})

defineEmits(['close'])
</script>

<style scoped>
.admin-action-toast { position: fixed; z-index: 10020; top: 1.5rem; right: 1.5rem; display: flex; width: min(24rem, calc(100vw - 2rem)); align-items: flex-start; gap: .75rem; border: 1px solid rgb(226 232 240); border-left-width: 4px; border-radius: 1rem; background: white; padding: 1rem; box-shadow: 0 18px 45px rgb(15 23 42 / .18); }
.admin-action-toast--success { border-left-color: #059669; }
.admin-action-toast--error { border-left-color: #e11d48; }
.admin-action-toast__icon { font-size: 1.5rem; }
.admin-action-toast--success .admin-action-toast__icon { color: #059669; }
.admin-action-toast--error .admin-action-toast__icon { color: #e11d48; }
.admin-action-toast__close { display: inline-flex; color: rgb(148 163 184); transition: color .2s, transform .2s; }
.admin-action-toast__close:hover { color: rgb(51 65 85); }
.admin-action-toast__close:active { transform: scale(.9); }
.admin-action-toast__close .material-symbols-outlined { font-size: 1.1rem; }
.admin-toast-enter-active, .admin-toast-leave-active { transition: opacity .22s ease, transform .22s ease; }
.admin-toast-enter-from, .admin-toast-leave-to { opacity: 0; transform: translateY(-.75rem); }
</style>
