<template>
  <header class="relative pt-16 pb-16 flex flex-col items-center justify-center min-h-[420px] overflow-hidden">
    <!-- Carousel Banners -->
    <div v-if="heroBanners.length > 0" class="absolute inset-0 z-0">
      <div 
        v-for="(banner, idx) in heroBanners" 
        :key="idx"
        class="absolute inset-0 bg-cover bg-center transition-opacity duration-1000 ease-in-out"
        :class="currentBannerIdx === idx ? 'opacity-100 z-10' : 'opacity-0 z-0'"
        :style="`background-image: url('${banner.url}');`"
      ></div>
    </div>
    <!-- Fallback nếu chưa có banner nào -->
    <div v-else
      class="absolute inset-0 bg-cover bg-center z-0"
      :style="`background-image: url('${heroBannerUrl}');`"
    ></div>

    <!-- Nút Đổi Ảnh Bìa (Chỉ dành cho Admin) -->
    <div v-if="isAdmin" class="absolute top-4 right-4 z-30">
      <button @click="$router.push('/admin/banners')" class="flex items-center gap-2 px-4 py-2 bg-white/20 hover:bg-white/40 backdrop-blur-md border border-white/30 text-white rounded-xl font-bold text-sm cursor-pointer transition-all shadow-lg hover:shadow-xl group">
        <span class="material-symbols-outlined text-[18px] group-hover:scale-110 transition-transform">view_carousel</span>
        Quản lý Banner
      </button>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';

const props = defineProps({
  heroBanners: {
    type: Array,
    default: () => []
  },
  heroBannerUrl: {
    type: String,
    default: ''
  },
  isAdmin: {
    type: Boolean,
    default: false
  }
});

const currentBannerIdx = ref(0);
let bannerInterval = null;

const startCarousel = () => {
  if (bannerInterval) clearInterval(bannerInterval);
  if (props.heroBanners.length > 1) {
    bannerInterval = setInterval(() => {
      currentBannerIdx.value = (currentBannerIdx.value + 1) % props.heroBanners.length;
    }, 3000);
  }
};

watch(() => props.heroBanners, startCarousel, { deep: true });

onMounted(() => {
  startCarousel();
});

onUnmounted(() => {
  if (bannerInterval) clearInterval(bannerInterval);
});
</script>
