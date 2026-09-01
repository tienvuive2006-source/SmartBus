<template>
  <Teleport to="body">
    <Transition name="access-toast">
      <div v-if="notice" class="access-toast" role="status" aria-live="polite">
        <span class="material-symbols-outlined access-toast__icon">shield_lock</span>
        <div class="min-w-0 flex-1">
          <p class="text-sm font-black text-slate-900">Yêu cầu đăng nhập</p>
          <p class="mt-0.5 text-xs font-semibold leading-5 text-slate-500">{{ notice.message }}</p>
        </div>
        <button type="button" class="access-toast__close" aria-label="Đóng thông báo" @click="hide">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { consumeRouteAccessNotice, ROUTE_ACCESS_NOTICE_EVENT } from '@/utils/routeAccessNotice'

const notice = ref(null)
let timer = null

const hide = () => {
  notice.value = null
  if (timer) clearTimeout(timer)
  timer = null
}

const show = value => {
  hide()
  notice.value = value
  timer = setTimeout(hide, 4000)
}

const handleNotice = event => show(event.detail)

onMounted(() => {
  window.addEventListener(ROUTE_ACCESS_NOTICE_EVENT, handleNotice)
  const queued = consumeRouteAccessNotice()
  if (queued) show(queued)
})

onBeforeUnmount(() => {
  window.removeEventListener(ROUTE_ACCESS_NOTICE_EVENT, handleNotice)
  hide()
})
</script>

<style scoped>
.access-toast { position: fixed; z-index: 10050; top: 1.5rem; right: 1.5rem; display: flex; width: min(25rem, calc(100vw - 2rem)); align-items: flex-start; gap: .75rem; border: 1px solid #fde68a; border-left: 4px solid #d99a16; border-radius: 1rem; background: #fffdf7; padding: 1rem; box-shadow: 0 18px 45px rgb(15 23 42 / .16); }
.access-toast__icon { color: #c7830a; font-size: 1.5rem; }
.access-toast__close { display: inline-flex; color: #94a3b8; transition: color .2s, transform .2s; }
.access-toast__close:hover { color: #334155; }
.access-toast__close:active { transform: scale(.9); }
.access-toast__close .material-symbols-outlined { font-size: 1.1rem; }
.access-toast-enter-active, .access-toast-leave-active { transition: opacity .22s ease, transform .22s ease; }
.access-toast-enter-from, .access-toast-leave-to { opacity: 0; transform: translateY(-.75rem); }
</style>
