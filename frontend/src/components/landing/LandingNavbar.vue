<template>
  <nav class="landing-navbar fixed top-0 w-full z-[100] transition-all duration-500" :class="(isScrolled || forceSolid) ? 'bg-white/90 backdrop-blur-2xl border-b border-zinc-200/50 py-3 shadow-sm' : 'bg-transparent py-6'">
    <div class="navbar-inner max-w-[1400px] mx-auto px-6 flex items-center justify-between">
      <div class="flex items-center cursor-pointer group" @click="router.push('/')">
        <img src="/logo2.png" alt="Trung Nam Logo" class="navbar-logo w-auto object-contain transform origin-left transition-all duration-500 drop-shadow-md -ml-4"
             :class="(isScrolled || forceSolid) ? 'h-14 scale-[1.2] group-hover:scale-[1.3]' : 'h-20 scale-[1.7] group-hover:scale-[1.8]'" />
      </div>
      
      <div class="hidden md:flex items-center gap-8 text-[13px] font-bold transition-all duration-500"
           :class="(isScrolled || forceSolid) ? 'text-zinc-600' : 'text-white/90'">
        <button @click="showTrackingModal = true" class="relative group transition-colors duration-300 border-none bg-transparent cursor-pointer font-bold" :class="(isScrolled || forceSolid) ? 'hover:text-emerald-600 text-emerald-700' : 'hover:text-emerald-300 text-emerald-400'">
          <span class="flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">search_check</span>Tra cứu vé</span>
        </button>
        <button @click="openInfoModal('benxe')" class="transition-colors duration-300 border-none bg-transparent cursor-pointer font-bold" :class="(isScrolled || forceSolid) ? 'hover:text-yellow-600' : 'hover:text-yellow-400'">Bến Xe</button>
        <button @click="openInfoModal('nhaxe')" class="transition-colors duration-300 border-none bg-transparent cursor-pointer font-bold" :class="(isScrolled || forceSolid) ? 'hover:text-yellow-600' : 'hover:text-yellow-400'">Nhà Xe</button>
        <button @click="openInfoModal('thongtin')" class="transition-colors duration-300 border-none bg-transparent cursor-pointer font-bold" :class="(isScrolled || forceSolid) ? 'hover:text-yellow-600' : 'hover:text-yellow-400'">Thông tin</button>
        <button @click="router.push('/tin-tuc')" class="transition-colors duration-300 border-none bg-transparent cursor-pointer font-bold" :class="(isScrolled || forceSolid) ? 'hover:text-yellow-600' : 'hover:text-yellow-400'">Tin Tức</button>
      </div>

      <div class="navbar-actions flex items-center gap-2 sm:gap-3">
        <div v-if="currentUser" class="rounded-full flex items-center justify-center transition-all w-9 h-9"
             :class="(isScrolled || forceSolid) ? '' : 'bg-black/20 backdrop-blur-md hover:bg-black/30 border border-white/10 shadow-sm'">
          <NotificationBell class="transition-colors shrink-0 scale-90"
                            :class="(isScrolled || forceSolid) ? 'text-zinc-600 hover:text-emerald-600' : 'text-white drop-shadow-md hover:text-amber-300'" />
        </div>

        <button @click="router.push(currentUser ? '/profile' : '/auth/login')" :aria-label="currentUser ? `Mở hồ sơ ${currentUser.fullName}` : 'Đăng nhập'" class="account-button relative overflow-hidden group pr-4 pl-1.5 py-1.5 rounded-full flex items-center gap-2 hover:-translate-y-0.5 active:translate-y-px transition-all duration-500 cursor-pointer border"
                :class="(isScrolled || forceSolid) ? 'bg-white shadow-sm hover:shadow-md border-zinc-200' : 'bg-black/20 backdrop-blur-md border-white/10 hover:bg-black/30 shadow-sm'">
          <div class="w-7 h-7 rounded-full flex items-center justify-center transition-all duration-300 relative z-10 shrink-0"
               :class="(isScrolled || forceSolid) ? 'bg-emerald-50 text-emerald-700' : 'bg-emerald-500 text-white shadow-inner'">
            <img
              v-if="currentUser"
              :src="currentUser.avatarUrl || createAvatarFallback(currentUser.fullName)"
              :alt="`Ảnh đại diện ${currentUser.fullName}`"
              class="h-full w-full rounded-full object-cover"
              referrerpolicy="no-referrer"
              @error="handleAvatarError($event, currentUser.fullName)"
            />
            <span v-else class="material-symbols-outlined text-[16px] transition-colors duration-500">person_outline</span>
          </div>
          <span class="account-label hidden sm:inline font-bold text-[11px] tracking-widest relative z-10 transition-colors duration-500 whitespace-nowrap"
                :class="(isScrolled || forceSolid) ? 'text-zinc-700 group-hover:text-emerald-700' : 'text-white drop-shadow-md group-hover:text-amber-100'">
            {{ currentUser?.fullName?.toUpperCase() || 'ĐĂNG NHẬP' }}
          </span>
        </button>
      </div>
    </div>

    <div v-if="route.name === 'home' || route.name === 'landing-demo'" class="mobile-bottom-nav md:hidden" aria-label="Điều hướng nhanh">
      <button
        type="button"
        class="mobile-nav-item"
        :class="route.path === '/' ? 'is-active' : ''"
        @click="route.path === '/' ? scrollToTop() : router.push('/')"
      >
        <span class="material-symbols-outlined">home</span>
        <span>Trang chủ</span>
      </button>
      <button type="button" class="mobile-nav-item" @click="openInfoModal('benxe')">
        <span class="material-symbols-outlined">directions_bus</span>
        <span>Bến xe</span>
      </button>
      <button type="button" class="mobile-nav-item mobile-nav-primary" @click="showTrackingModal = true">
        <span class="material-symbols-outlined">search</span>
        <span>Tra cứu</span>
      </button>
      <button type="button" class="mobile-nav-item" @click="openInfoModal('nhaxe')">
        <span class="material-symbols-outlined">apartment</span>
        <span>Nhà xe</span>
      </button>
      <button
        type="button"
        class="mobile-nav-item"
        :class="route.path.startsWith('/tin-tuc') ? 'is-active' : ''"
        @click="router.push('/tin-tuc')"
      >
        <span class="material-symbols-outlined">newspaper</span>
        <span>Tin tức</span>
      </button>
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
import { useHomeSummary } from '@/composables/useHomeSummary';
import NotificationBell from '@/components/NotificationBell.vue';
import LandingInfoModal from '@/components/landing/LandingInfoModal.vue';
import LandingTrackingModal from '@/components/landing/LandingTrackingModal.vue';
import { createAvatarFallback, handleAvatarError } from '@/utils/avatar';

const props = defineProps({
  forceSolid: {
    type: Boolean,
    default: false
  }
});

const router = useRouter();
const route = useRoute();
const { getHomeSummary } = useHomeSummary();
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
    const summary = await getHomeSummary();
    uniqueStations.value = summary.uniqueStations || [];
    uniqueCompanies.value = summary.uniqueCompanies || [];
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

<style scoped>
@media (max-width: 767px) {
  .landing-navbar {
    padding-block: 0.85rem !important;
  }

  .navbar-inner {
    padding-inline: 1rem !important;
  }

  .navbar-logo {
    width: 9rem !important;
    height: 2.75rem !important;
    margin-left: -0.35rem !important;
    transform: none !important;
  }

  .navbar-actions {
    gap: 0.45rem !important;
  }

  .account-button {
    width: 2.4rem;
    height: 2.4rem;
    padding: 0.25rem !important;
    justify-content: center;
  }

  .mobile-bottom-nav {
    position: fixed;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 100;
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    min-height: 4.25rem;
    padding: 0.35rem 0.4rem max(0.35rem, env(safe-area-inset-bottom));
    border-top: 1px solid rgb(228 228 231 / 0.9);
    background: rgb(255 255 255 / 0.96);
    box-shadow: 0 -0.75rem 2rem rgb(15 23 42 / 0.12);
    backdrop-filter: blur(18px);
    -webkit-backdrop-filter: blur(18px);
  }

  .mobile-nav-item {
    display: flex;
    min-width: 0;
    min-height: 3.4rem;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0.15rem;
    border: 0;
    border-radius: 0.75rem;
    background: transparent;
    color: #71717a;
    font-size: 0.61rem;
    font-weight: 700;
    white-space: nowrap;
    transition: color 160ms ease, background-color 160ms ease, transform 160ms ease;
  }

  .mobile-nav-item:active {
    transform: scale(0.96);
  }

  .mobile-nav-item .material-symbols-outlined {
    font-size: 1.25rem;
  }

  .mobile-nav-item.is-active {
    color: #047857;
    background: #ecfdf5;
  }

  .mobile-nav-primary {
    color: #047857;
  }

  .mobile-nav-primary .material-symbols-outlined {
    display: grid;
    width: 2.15rem;
    height: 2.15rem;
    margin-top: -1rem;
    place-items: center;
    border: 3px solid white;
    border-radius: 999px;
    background: #059669;
    color: white;
    box-shadow: 0 0.45rem 1.1rem rgb(5 150 105 / 0.28);
  }
}
</style>
