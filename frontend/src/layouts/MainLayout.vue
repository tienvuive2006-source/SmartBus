<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <LandingNavbar v-if="!$route.meta.hideHeader" :forceSolid="true" />

    <main class="flex-grow" :class="{ 'pt-[72px] md:pt-[104px]': !$route.meta.hideHeader }">
      <router-view />
    </main>

    <AppFooter v-if="!$route.meta.hideFooter" />

    <BottomNavBar v-if="!$route.meta.hideHeader && !$route.meta.hideFooter" class="md:hidden" />

    <AiChatbot />
  </div>
</template>

<script setup>
import LandingNavbar from '../components/landing/LandingNavbar.vue'
import BottomNavBar from '../components/BottomNavBar.vue'
import AppFooter from '../components/AppFooter.vue'
import AiChatbot from '../components/AiChatbot.vue'
</script>
