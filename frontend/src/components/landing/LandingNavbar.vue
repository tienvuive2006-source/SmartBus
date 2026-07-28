<template>
  <nav class="fixed top-0 w-full z-[100] transition-all duration-500" :class="(isScrolled || forceSolid) ? 'bg-white/90 backdrop-blur-2xl border-b border-zinc-200/50 py-3 shadow-sm' : 'bg-transparent py-6'">
    <div class="max-w-[1400px] mx-auto px-6 flex items-center justify-between">
      <div class="flex items-center cursor-pointer group" @click="router.push('/')">
        <img src="/logo2.png" alt="Trung Nam Logo" class="w-auto object-contain transform origin-left transition-all duration-500 drop-shadow-md -ml-4"
             :class="(isScrolled || forceSolid) ? 'h-14 scale-[1.2] group-hover:scale-[1.3]' : 'h-20 scale-[1.7] group-hover:scale-[1.8]'" />
      </div>
      
      <div class="hidden md:flex items-center gap-8 text-[13px] font-bold transition-all duration-500"
           :class="(isScrolled || forceSolid) ? 'text-zinc-600' : 'text-white/90'">
        <button @click="$route.path === '/' ? scrollToTop() : router.push('/')" class="relative group transition-colors duration-300 border-none bg-transparent cursor-pointer font-bold" :class="(isScrolled || forceSolid) ? 'hover:text-yellow-600' : 'hover:text-yellow-400'">
          Mua vé
          <span class="absolute -bottom-1 left-0 w-0 h-0.5 bg-yellow-400 transition-all duration-300 group-hover:w-full"></span>
        </button>
        <button @click="showTrackingModal = true" class="relative group transition-colors duration-300 border-none bg-transparent cursor-pointer font-bold" :class="(isScrolled || forceSolid) ? 'hover:text-emerald-600 text-emerald-700' : 'hover:text-emerald-300 text-emerald-400'">
          <span class="flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">search_check</span>Tra cứu vé</span>
        </button>
        <button @click="openInfoModal('benxe')" class="transition-colors duration-300 border-none bg-transparent cursor-pointer font-bold" :class="(isScrolled || forceSolid) ? 'hover:text-yellow-600' : 'hover:text-yellow-400'">Bến Xe</button>
        <button @click="openInfoModal('nhaxe')" class="transition-colors duration-300 border-none bg-transparent cursor-pointer font-bold" :class="(isScrolled || forceSolid) ? 'hover:text-yellow-600' : 'hover:text-yellow-400'">Nhà Xe</button>
        <button @click="openInfoModal('thongtin')" class="transition-colors duration-300 border-none bg-transparent cursor-pointer font-bold" :class="(isScrolled || forceSolid) ? 'hover:text-yellow-600' : 'hover:text-yellow-400'">Thông tin</button>
        <button @click="router.push('/tin-tuc')" class="transition-colors duration-300 border-none bg-transparent cursor-pointer font-bold" :class="(isScrolled || forceSolid) ? 'hover:text-yellow-600' : 'hover:text-yellow-400'">Tin Tức</button>
      </div>

      <div class="flex items-center gap-2 sm:gap-3">
        <div v-if="currentUser" class="rounded-full flex items-center justify-center transition-all w-9 h-9"
             :class="(isScrolled || forceSolid) ? '' : 'bg-black/20 backdrop-blur-md hover:bg-black/30 border border-white/10 shadow-sm'">
          <NotificationBell class="transition-colors shrink-0 scale-90" 
                            :class="(isScrolled || forceSolid) ? 'text-zinc-600 hover:text-emerald-600' : 'text-white drop-shadow-md hover:text-amber-300'" />
        </div>
        
        <button @click="router.push(currentUser ? '/profile' : '/auth/login')" class="relative overflow-hidden group pr-4 pl-1.5 py-1.5 rounded-full flex items-center gap-2 hover:-translate-y-0.5 active:translate-y-px transition-all duration-500 cursor-pointer border"
                :class="(isScrolled || forceSolid) ? 'bg-white shadow-sm hover:shadow-md border-zinc-200' : 'bg-black/20 backdrop-blur-md border-white/10 hover:bg-black/30 shadow-sm'">
          <div class="w-7 h-7 rounded-full flex items-center justify-center transition-all duration-300 relative z-10 shrink-0"
               :class="(isScrolled || forceSolid) ? 'bg-emerald-50 text-emerald-700' : 'bg-emerald-500 text-white shadow-inner'">
            <span class="material-symbols-outlined text-[16px] transition-colors duration-500">person_outline</span>
          </div>
          <span class="font-bold text-[11px] tracking-widest relative z-10 transition-colors duration-500 whitespace-nowrap"
                :class="(isScrolled || forceSolid) ? 'text-zinc-700 group-hover:text-emerald-700' : 'text-white drop-shadow-md group-hover:text-amber-100'">
            {{ currentUser?.fullName?.toUpperCase() || 'ĐĂNG NHẬP' }}
          </span>
        </button>
      </div>
    </div>
    <!-- Modals (Teleported to body to prevent cutoff) -->
    <Teleport to="body">
      <LandingInfoModal 
        v-if="showInfoModal"
        :activeModalType="infoModalType" 
        :uniqueStations="uniqueStations"
        :uniqueCompanies="uniqueCompanies"
        @close="showInfoModal = false" 
        @book-now="scrollToTop(); showInfoModal = false"
      />
      <LandingTrackingModal :is-open="showTrackingModal" @close="showTrackingModal = false" />
    </Teleport>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useApi } from '@/composables/useApi';
import NotificationBell from '@/components/NotificationBell.vue';
import LandingInfoModal from '@/components/landing/LandingInfoModal.vue';
import LandingTrackingModal from '@/components/landing/LandingTrackingModal.vue';

const props = defineProps({
  forceSolid: {
    type: Boolean,
    default: false
  }
});

const router = useRouter();
const route = useRoute();
const api = useApi();
const authStore = useAuthStore();
const currentUser = computed(() => authStore.user);

const isScrolled = ref(false);
const handleScroll = () => {
  isScrolled.value = window.scrollY > 50;
};

// Modal state
const showInfoModal = ref(false);
const showTrackingModal = ref(false);
const infoModalType = ref('');
const uniqueStations = ref([]);
const uniqueCompanies = ref([]);

const openInfoModal = (type) => {
  infoModalType.value = type;
  showInfoModal.value = true;
};

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const fetchData = async () => {
  try {
    const response = await api.get('/trips/home-summary');
    uniqueStations.value = response.data.uniqueStations || [];
    uniqueCompanies.value = response.data.uniqueCompanies || [];
  } catch (err) {
    console.error(err);
  }
};

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
  fetchData();
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>
