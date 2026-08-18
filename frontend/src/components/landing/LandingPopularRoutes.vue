<template>
  <section v-if="popularRoutes.length > 0" class="pt-8 pb-16 px-6 max-w-[1400px] mx-auto relative z-10">
    <div class="flex flex-col md:flex-row items-end justify-between mb-8 gap-6">
      <div>
        <h2 class="text-[2.5rem] md:text-[3.5rem] font-semibold tracking-tight leading-[1.1] text-zinc-900">
          Tuyến đường <span class="text-[#059669]">phổ biến nhất.</span>
        </h2>
      </div>
      <button @click="$router.push('/booking/search')" class="px-8 py-4 rounded-full bg-zinc-100 text-zinc-900 font-semibold hover:bg-zinc-200 transition-colors flex items-center gap-2 group shrink-0">
        Xem tất cả hành trình <span class="material-symbols-outlined text-sm group-hover:translate-x-1 transition-transform">arrow_forward</span>
      </button>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      <div v-for="(route, index) in popularRoutes.slice(0, 8)" :key="route.id" 
           @click="bookPopularRoute(route)"
           class="group relative h-[280px] rounded-[2rem] overflow-hidden cursor-pointer shadow-sm hover:shadow-2xl transition-shadow duration-700">
        
        <img v-if="route.image" :src="route.image" class="absolute inset-0 w-full h-full object-cover transition-transform duration-[1.5s] ease-out group-hover:scale-105" :alt="route.shortTo" />
        <div v-else class="absolute inset-0 w-full h-full bg-slate-200 flex items-center justify-center transition-transform duration-[1.5s] ease-out group-hover:scale-105">
          <span class="text-slate-400 font-medium tracking-widest uppercase text-sm">Chưa có ảnh</span>
        </div>
        
        <!-- Soft Gradient Overlay -->
        <div class="absolute inset-0 bg-gradient-to-t from-black/90 via-black/10 to-transparent opacity-70 group-hover:opacity-90 transition-opacity duration-500"></div>
        
        <!-- Glassmorphism Border Overlay -->
        <div class="absolute inset-0 border border-white/10 rounded-[2rem] z-10 pointer-events-none transition-colors duration-500 group-hover:border-white/30"></div>
        
        <div class="absolute bottom-0 left-0 w-full p-6 flex flex-col justify-end z-20">
          <div class="flex items-center justify-between w-full mb-4 transform translate-y-4 group-hover:translate-y-0 transition-all duration-500 ease-out opacity-0 group-hover:opacity-100">
            <p class="text-white/90 font-medium text-[11px] tracking-[0.2em] uppercase border border-white/30 bg-white/10 px-4 py-2 rounded-full backdrop-blur-md">
              Chỉ từ <span class="text-white font-bold ml-1">{{ formatPrice(route.price) }}</span>
            </p>
            
            <div class="w-12 h-12 rounded-full bg-white/20 backdrop-blur-md flex items-center justify-center -rotate-45 group-hover:rotate-0 transition-transform duration-500 border border-white/30">
              <span class="material-symbols-outlined text-white text-base">arrow_forward</span>
            </div>
          </div>
          
          <h3 :title="route.customName || `${route.shortFrom} → ${route.shortTo}`" class="text-xl lg:text-2xl font-bold text-white leading-tight tracking-tight drop-shadow-md truncate">
            <template v-if="route.customName">
              {{ route.customName }}
            </template>
            <template v-else>
              {{ route.shortFrom }} <span class="text-white/50 font-light mx-1">→</span> {{ route.shortTo }}
            </template>
          </h3>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useApi } from '@/composables/useApi';
import { usePopularRoutes } from '@/composables/usePopularRoutes';

const router = useRouter();
const api = useApi();

const trips = ref([]);
const filterFrom = ref('');
const filterTo = ref('');
const filterCompany = ref('');
const filterDate = ref('');

const { popularRoutes } = usePopularRoutes(trips, filterFrom, filterTo, filterCompany, filterDate);

const formatPrice = (price) => {
  if (!price) return 'Liên hệ';
  return price.toLocaleString('vi-VN') + 'đ';
};



const bookPopularRoute = (route) => {
  router.push({
    path: '/booking/search',
    query: { from: route.shortFrom, to: route.shortTo }
  });
};

onMounted(async () => {
  try {
    const cachedTrips = sessionStorage.getItem('landing_trips');
    if (cachedTrips) {
      trips.value = JSON.parse(cachedTrips);
    }
    const res = await api.get('/trips/search');
    trips.value = res.data;
    sessionStorage.setItem('landing_trips', JSON.stringify(res.data));
  } catch (err) {
    console.error("Failed to fetch trips for landing page demo:", err);
  }
});
</script>
