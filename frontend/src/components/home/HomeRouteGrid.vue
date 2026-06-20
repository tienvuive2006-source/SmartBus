<template>
  <main v-if="popularRoutes.length > 0" class="max-w-[95%] 2xl:max-w-[1600px] mx-auto px-4 pb-8">
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 gap-y-8 md:gap-8">
      <div v-for="route in popularRoutes" :key="route.id" @click="$emit('quick-search', route.from, route.to)" class="group rounded-2xl overflow-hidden hover:shadow-2xl hover:-translate-y-1.5 transition-all duration-300 cursor-pointer flex flex-col shadow-md">
        <div class="relative h-56 sm:h-48 md:h-56 overflow-hidden bg-slate-200">
          <template v-if="route.image">
            <img 
              :src="route.image" 
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500 ease-out"
              @error="(e) => e.target.style.display = 'none'"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent"></div>
          </template>
          <template v-else>
            <div class="w-full h-full flex flex-col items-center justify-center text-slate-400 group-hover:scale-105 transition-transform duration-500 ease-out">
              <span class="material-symbols-outlined text-4xl mb-2 opacity-30">directions_bus</span>
              <span class="text-xs font-semibold uppercase tracking-wider opacity-60">Chưa có ảnh</span>
            </div>
            <div class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent"></div>
          </template>

          <div class="absolute bottom-3 left-4 right-4 text-white">
            <h3 class="font-bold text-[17px] truncate" :title="route.customName || `${route.shortFrom} ➝ ${route.shortTo}`">
              {{ route.customName || `${route.shortFrom} ➝ ${route.shortTo}` }}
            </h3>
          </div>
        </div>

      </div>
    </div>
  </main>
</template>

<script setup>
defineProps({
  popularRoutes: {
    type: Array,
    required: true
  }
});

defineEmits(['quick-search']);
</script>
