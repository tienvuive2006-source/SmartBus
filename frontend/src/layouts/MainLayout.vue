<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <TopAppBar v-if="!$route.meta.hideHeader" />

    <main class="flex-grow">
      <router-view />
    </main>

    <AppFooter />

    <BottomNavBar class="md:hidden" />

    <!-- AI Chatbot Floating Widget -->
    <AiChatbot />
  </div>
</template>

<script setup>
import TopAppBar from '../components/TopAppBar.vue'
import BottomNavBar from '../components/BottomNavBar.vue'
import AppFooter from '../components/AppFooter.vue'
import AiChatbot from '../components/AiChatbot.vue' // Import AI Chatbot
</script>