<template>
  <nav class="bottom-nav bg-surface shadow-[0px_-4px_12px_rgba(0,0,0,0.05)] fixed bottom-0 w-full z-50 rounded-t-xl left-0 flex justify-around items-center px-2 py-3 md:hidden" aria-label="Điều hướng tài khoản">
    <RouterLink to="/" custom v-slot="{ navigate, isActive }">
      <a @click="navigate" :class="[isActive ? 'text-primary bg-primary-fixed-dim/20' : 'text-on-surface-variant hover:text-primary', 'bottom-nav-link flex flex-col items-center justify-center rounded-xl p-2 scale-95 active:scale-90 transition-transform duration-150']">
        <span class="material-symbols-outlined" :style="isActive ? 'font-variation-settings: \'FILL\' 1;' : ''">home</span>
        <span class="bottom-nav-label text-label-md font-label-md mt-1">Trang chủ</span>
      </a>
    </RouterLink>
    
    <RouterLink to="/history" custom v-slot="{ navigate, isActive }">
      <a @click="navigate" :class="[isActive ? 'text-primary bg-primary-fixed-dim/20' : 'text-on-surface-variant hover:text-primary', 'bottom-nav-link flex flex-col items-center justify-center rounded-xl p-2 scale-95 active:scale-90 transition-transform duration-150']">
        <span class="material-symbols-outlined" :style="isActive ? 'font-variation-settings: \'FILL\' 1;' : ''">history</span>
        <span class="bottom-nav-label text-label-md font-label-md mt-1">Lịch sử</span>
      </a>
    </RouterLink>

    <RouterLink to="/profile" custom v-slot="{ navigate, isActive }">
      <a @click="navigate" :class="[isActive ? 'text-primary bg-primary-fixed-dim/20' : 'text-on-surface-variant hover:text-primary', 'bottom-nav-link flex flex-col items-center justify-center rounded-xl p-2 scale-95 active:scale-90 transition-transform duration-150']">
        <span class="material-symbols-outlined" :style="isActive ? 'font-variation-settings: \'FILL\' 1;' : ''">account_circle</span>
        <span class="bottom-nav-label text-label-md font-label-md mt-1">Cá nhân</span>
      </a>
    </RouterLink>
  </nav>
</template>

<script setup>
import { RouterLink } from 'vue-router';
</script>

<style scoped>
.bottom-nav {
  min-height: 4.25rem;
  padding: 0.4rem 0.75rem max(0.4rem, env(safe-area-inset-bottom)) !important;
}

.bottom-nav-link {
  min-width: 4.75rem;
  min-height: 3.25rem;
  padding: 0.4rem 0.65rem !important;
}

.bottom-nav-label {
  font-size: 0.64rem;
  line-height: 1;
  white-space: nowrap;
}
</style>
