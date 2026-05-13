<template>
  <div class="bg-background text-on-background min-h-screen pb-20">
    <TopAppBar v-if="!route.meta.hideHeader" :title="route.meta.title" :showBack="route.meta.showBack" />
    
    <main class="max-w-7xl mx-auto px-container-margin py-stack-space space-y-stack-space">
      <RouterView />
    </main>

    <BottomNavBar v-if="!route.meta.hideFooter" />
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router';
import TopAppBar from '../components/TopAppBar.vue';
import BottomNavBar from '../components/BottomNavBar.vue';

const route = useRoute();
</script>
