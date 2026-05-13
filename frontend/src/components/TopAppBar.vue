<template>
  <header class="bg-surface shadow-sm docked full-width top-0 sticky z-40 flex items-center justify-between px-container-margin h-touch-target-min w-full max-w-7xl mx-auto transition-colors duration-200">
    <button v-if="showBack" @click="$router.back()" aria-label="Go back" class="text-on-surface-variant hover:bg-surface-variant rounded-full p-2 transition-colors">
      <span class="material-symbols-outlined">arrow_back</span>
    </button>
    <button v-else aria-label="Menu" class="text-on-surface-variant hover:bg-surface-variant rounded-full p-2 transition-colors">
      <span class="material-symbols-outlined">menu</span>
    </button>
    
    <div class="text-headline-md font-headline-md font-bold text-primary">{{ title }}</div>
    
    <button @click="$router.push('/profile')" aria-label="User profile photo" class="rounded-full overflow-hidden w-8 h-8 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 cursor-pointer transition-transform active:scale-90">
      <img alt="User profile photo" class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBevtWWa4mh9YcJIdsooADz8ktlDAgErvvIrDBpc1saMPPzR1XPTDp_QeTD3zHw_h1zBnT4TGJEangkTHy7iLiXRp3jQT-s0-cCt6gvyLiK1eEk86xnY6Y3Jp4c0Pf2ruH7rkN0NS_H9OQxjaIGQS7Sg_dhWHBBhVUmo_BoIwYgCBKnfmI9flUYytsXjRivK25Vto-gHraIz1Fbjh-g4xrY2cJQW23TTB2JJJFOr6KvHu49ouQhkovUzx_0A_5QyXd_C80X_mI4rhy2"/>
    </button>
  </header>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: 'TransLink Pro'
  },
  showBack: {
    type: Boolean,
    default: false
  }
});
</script>
