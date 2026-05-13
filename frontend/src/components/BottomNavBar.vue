<template>
  <nav class="bg-surface shadow-[0px_-4px_12px_rgba(0,0,0,0.05)] fixed bottom-0 w-full z-50 rounded-t-xl left-0 flex justify-around items-center px-2 py-3 md:hidden">
    <RouterLink to="/" custom v-slot="{ navigate, isActive }">
      <a @click="navigate" :class="[isActive ? 'text-primary bg-primary-fixed-dim/20' : 'text-on-surface-variant hover:text-primary', 'flex flex-col items-center justify-center rounded-xl p-2 scale-95 active:scale-90 transition-transform duration-150']">
        <span class="material-symbols-outlined" :style="isActive ? 'font-variation-settings: \'FILL\' 1;' : ''">home</span>
        <span class="text-label-md font-label-md mt-1">Home</span>
      </a>
    </RouterLink>
    
    <RouterLink to="/history" custom v-slot="{ navigate, isActive }">
      <a @click="navigate" :class="[isActive ? 'text-primary bg-primary-fixed-dim/20' : 'text-on-surface-variant hover:text-primary', 'flex flex-col items-center justify-center rounded-xl p-2 scale-95 active:scale-90 transition-transform duration-150']">
        <span class="material-symbols-outlined" :style="isActive ? 'font-variation-settings: \'FILL\' 1;' : ''">history</span>
        <span class="text-label-md font-label-md mt-1">History</span>
      </a>
    </RouterLink>

    <RouterLink to="/ai-assistant" custom v-slot="{ navigate, isActive }">
      <a @click="navigate" :class="[isActive ? 'text-primary bg-primary-fixed-dim/20' : 'text-on-surface-variant hover:text-primary', 'flex flex-col items-center justify-center rounded-xl p-2 scale-95 active:scale-90 transition-transform duration-150']">
        <span class="material-symbols-outlined" :style="isActive ? 'font-variation-settings: \'FILL\' 1;' : ''">smart_toy</span>
        <span class="text-label-md font-label-md mt-1">AI Assistant</span>
      </a>
    </RouterLink>

    <RouterLink to="/notifications" custom v-slot="{ navigate, isActive }">
      <a @click="navigate" :class="[isActive ? 'text-primary bg-primary-fixed-dim/20' : 'text-on-surface-variant hover:text-primary', 'flex flex-col items-center justify-center rounded-xl p-2 scale-95 active:scale-90 transition-transform duration-150']">
        <span class="material-symbols-outlined" :style="isActive ? 'font-variation-settings: \'FILL\' 1;' : ''">notifications</span>
        <span class="text-label-md font-label-md mt-1">Notifications</span>
      </a>
    </RouterLink>

    <RouterLink to="/profile" custom v-slot="{ navigate, isActive }">
      <a @click="navigate" :class="[isActive ? 'text-primary bg-primary-fixed-dim/20' : 'text-on-surface-variant hover:text-primary', 'flex flex-col items-center justify-center rounded-xl p-2 scale-95 active:scale-90 transition-transform duration-150']">
        <span class="material-symbols-outlined" :style="isActive ? 'font-variation-settings: \'FILL\' 1;' : ''">account_circle</span>
        <span class="text-label-md font-label-md mt-1">Profile</span>
      </a>
    </RouterLink>
  </nav>
</template>

<script setup>
import { RouterLink } from 'vue-router';
</script>
