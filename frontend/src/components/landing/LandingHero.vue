<template>
  <header class="landing-hero relative pt-32 pb-16 px-6 w-full min-h-[60vh] flex flex-col justify-end items-start text-left">
    <!-- Full background image -->
    <div class="absolute inset-0 z-0 overflow-hidden bg-zinc-900">
      
      <!-- Fallback / Cached image (Always stays behind to prevent black flash) -->
      <img :src="activeBanner" fetchpriority="high" class="hero-image absolute inset-0 w-full h-full object-cover object-[center_bottom] scale-105 animate-[kenburns_20s_ease-out_infinite_alternate] z-0" alt="Xe limousine Trung Nam trên hành trình" @error="useLocalBannerFallback" />

      <!-- Slideshow images from API -->
      <img v-for="(banner, index) in heroBanners" :key="banner.url || index"
           :src="banner.url" 
           class="hero-image absolute inset-0 w-full h-full object-cover object-[center_bottom] scale-105 transition-opacity duration-1000 ease-in-out animate-[kenburns_20s_ease-out_infinite_alternate]"
           :class="index === currentBannerIndex && heroBanners.length > 0 ? 'opacity-100 z-10' : 'opacity-0 -z-10'"
           alt="Xe limousine Trung Nam trên hành trình"
           @error="useLocalBannerFallback" />
      
      <!-- Lớp phủ tối mờ để dễ đọc chữ -->
      <div class="hero-overlay-side absolute inset-0 bg-gradient-to-r from-black/70 via-black/20 to-transparent z-10"></div>
      <div class="hero-overlay-bottom absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent z-10"></div>
    </div>

    <div class="hero-content relative z-10 w-full max-w-[1400px] mx-auto mb-8 flex flex-col items-start">
      <div class="hero-eyebrow inline-flex items-center gap-3 px-5 py-2.5 rounded-full bg-black/30 backdrop-blur-md border border-white/20 shadow-sm mb-4 hover:bg-black/40 transition-colors">
        <span class="flex h-2 w-2 relative">
          <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
          <span class="relative inline-flex rounded-full h-2 w-2 bg-amber-500"></span>
        </span>
        <span class="text-[11px] font-bold tracking-widest uppercase text-white/90 drop-shadow-md">THẾ HỆ XE GIƯỜNG NẰM VIP LIMOUSINE</span>
      </div>
      
      <h1 class="hero-title text-[2.5rem] md:text-[4.5rem] leading-[1.1] text-white mb-4 drop-shadow-2xl max-w-3xl">
        <span class="font-light tracking-wide text-white/90">Nâng tầm</span> <br />
        <span class="elegant-text bg-gradient-to-r from-amber-200 via-yellow-400 to-amber-600 text-transparent bg-clip-text drop-shadow-[0_2px_15px_rgba(251,191,36,0.3)]">
          chuyến đi của bạn.
        </span>
      </h1>
      
      <p class="hero-copy text-base md:text-lg text-white/80 font-light tracking-wide max-w-xl mb-8 leading-relaxed drop-shadow-md">
        Không gian riêng tư, tiện nghi cao cấp và dịch vụ tận tâm trên từng hành trình.
      </p>

      <!-- FLOATING GLASSMORPHISM BOOKING WIDGET -->
      <div class="booking-widget relative z-20 w-full max-w-[750px] flex flex-col items-start mt-2 mb-8">
        
        <!-- LỰA CHỌN LOẠI VÉ -->
      <div class="trip-type flex items-center gap-6 mb-3 ml-4">
        <label class="flex items-center gap-2 text-white/90 font-bold text-[13px] cursor-pointer drop-shadow-md hover:text-white transition-colors">
          <input type="radio" v-model="tripType" value="one-way" class="accent-emerald-500 w-4 h-4 cursor-pointer">
          Một chiều
        </label>
        <label class="flex items-center gap-2 text-white/90 font-bold text-[13px] cursor-pointer drop-shadow-md hover:text-white transition-colors">
          <input type="radio" v-model="tripType" value="round-trip" class="accent-emerald-500 w-4 h-4 cursor-pointer">
          Khứ hồi
        </label>
      </div>

      <div class="booking-shell w-full bg-white/10 backdrop-blur-xl border border-white/20 shadow-[0_40px_80px_-20px_rgba(0,0,0,0.5)] rounded-[2rem] p-2 flex flex-col md:flex-row items-center gap-2 hover:shadow-[0_40px_100px_-20px_rgba(5,150,105,0.3)] hover:bg-white/15 transition-all duration-700">
        <div class="search-fields flex-1 flex flex-col md:flex-row items-center bg-white rounded-[1.5rem] md:rounded-full p-2 border border-zinc-200 w-full shadow-inner relative gap-y-2 md:gap-y-0">
        <!-- KHU VỰC NƠI ĐI -->
        <div class="search-field location-from flex-1 px-4 border-r border-zinc-200 relative">
          <p class="text-[8px] font-black uppercase text-zinc-400 tracking-wider mb-0.5">Nơi đi</p>
          <input 
            v-model="fromQuery" 
            @focus="showFromDropdown = true"
            @blur="closeFromDropdown"
            class="font-bold text-sm text-zinc-900 bg-transparent outline-none w-full placeholder-zinc-300" 
            placeholder="Chọn nơi đi" 
          />
          
          <Transition enter-active-class="transition duration-200 ease-out" enter-from-class="translate-y-2 opacity-0" enter-to-class="translate-y-0 opacity-100" leave-active-class="transition duration-150 ease-in" leave-from-class="translate-y-0 opacity-100" leave-to-class="translate-y-2 opacity-0">
            <div v-if="showFromDropdown" class="location-dropdown from-dropdown absolute left-0 bottom-[calc(100%+1rem)] w-max min-w-[150%] pr-2 bg-white/95 backdrop-blur-2xl border border-white/50 shadow-[0_-20px_60px_-10px_rgba(0,0,0,0.2)] rounded-2xl overflow-hidden z-[100]">
              <div class="max-h-64 overflow-y-auto p-2 scrollbar-hide">
                <div v-for="loc in filteredFrom" :key="loc" @click="selectFrom(loc)" class="px-3 py-2.5 hover:bg-emerald-50 hover:text-emerald-700 rounded-xl cursor-pointer transition-colors text-xs font-semibold text-zinc-700 flex items-center gap-3">
                  <span class="material-symbols-outlined text-base opacity-50 shrink-0">location_on</span>
                  <span class="whitespace-nowrap">{{ loc }}</span>
                </div>
                <div v-if="filteredFrom.length === 0" class="px-3 py-2.5 text-xs text-zinc-500 italic text-center">Không tìm thấy</div>
              </div>
            </div>
          </Transition>
        </div>

        <!-- NÚT HOÁN ĐỔI -->
        <button type="button" aria-label="Đổi nơi đi và nơi đến" @click="swapLocations" class="swap-button w-8 h-8 md:w-7 md:h-7 -ml-3 md:-ml-4 bg-zinc-900 rounded-full flex items-center justify-center shadow-md border-[2px] border-white z-10 cursor-pointer hover:scale-110 active:scale-95 transition-transform shrink-0 relative aspect-square">
          <span class="material-symbols-outlined text-white text-[14px]">swap_horiz</span>
        </button>

        <!-- KHU VỰC NƠI ĐẾN -->
        <div class="search-field location-to flex-1 px-4 border-r border-zinc-200 pl-5 relative">
          <p class="text-[8px] font-black uppercase text-zinc-400 tracking-wider mb-0.5">Nơi đến</p>
          <input 
            v-model="toQuery" 
            @focus="showToDropdown = true"
            @blur="closeToDropdown"
            class="font-bold text-sm text-zinc-900 bg-transparent outline-none w-full placeholder-zinc-300" 
            placeholder="Chọn nơi đến" 
          />
          
          <Transition enter-active-class="transition duration-200 ease-out" enter-from-class="translate-y-2 opacity-0" enter-to-class="translate-y-0 opacity-100" leave-active-class="transition duration-150 ease-in" leave-from-class="translate-y-0 opacity-100" leave-to-class="translate-y-2 opacity-0">
            <div v-if="showToDropdown" class="location-dropdown to-dropdown absolute left-0 bottom-[calc(100%+1rem)] w-max min-w-[150%] pr-2 bg-white/95 backdrop-blur-2xl border border-white/50 shadow-[0_-20px_60px_-10px_rgba(0,0,0,0.2)] rounded-2xl overflow-hidden z-[100]">
              <div class="max-h-64 overflow-y-auto p-2 scrollbar-hide">
                <div v-for="loc in filteredTo" :key="loc" @click="selectTo(loc)" class="px-3 py-2.5 hover:bg-emerald-50 hover:text-emerald-700 rounded-xl cursor-pointer transition-colors text-xs font-semibold text-zinc-700 flex items-center gap-3">
                  <span class="material-symbols-outlined text-base opacity-50 shrink-0">pin_drop</span>
                  <span class="whitespace-nowrap">{{ loc }}</span>
                </div>
                <div v-if="filteredTo.length === 0" class="px-3 py-2.5 text-xs text-zinc-500 italic text-center">Không tìm thấy</div>
              </div>
            </div>
          </Transition>
        </div>
        <!-- KHU VỰC NGÀY ĐI -->
        <div class="search-field departure-date flex-1 px-4 pl-5 relative border-r border-zinc-200 group">
          <div class="flex justify-between items-center mb-0.5 relative z-20">
            <p class="text-[8px] font-black uppercase text-zinc-400 tracking-wider">Ngày đi</p>
            <span v-if="dateQuery" @click.stop="dateQuery = ''" class="text-[9px] font-bold text-rose-400 cursor-pointer hover:text-rose-600 opacity-0 group-hover:opacity-100 transition-opacity">Xóa</span>
          </div>
          <div class="relative w-full cursor-pointer group/input mt-0.5">
            <input type="date" v-model="dateQuery" :min="todayDate" class="absolute inset-0 opacity-0 cursor-pointer w-full h-full z-10" />
            <div class="font-bold text-sm flex items-center justify-between w-full transition-colors gap-1" :class="dateQuery ? 'text-[#059669]' : 'text-zinc-500'">
              <span class="whitespace-nowrap truncate min-w-0">{{ dateQuery ? formatDisplayDate(dateQuery) : 'Tất cả các ngày' }}</span>
              <span class="material-symbols-outlined text-lg transition-colors shrink-0" :class="dateQuery ? 'text-emerald-500/50 group-hover/input:text-emerald-500' : 'text-zinc-400 group-hover/input:text-emerald-500'">calendar_month</span>
            </div>
          </div>
        </div>

        <!-- KHU VỰC NGÀY VỀ -->
        <div class="search-field return-date flex-1 px-4 pl-5 relative cursor-pointer group" @click="tripType = 'round-trip'">
          <div class="flex justify-between items-center mb-0.5 relative z-20">
            <p class="text-[8px] font-black uppercase text-zinc-400 tracking-wider">Ngày về</p>
            <span v-if="tripType === 'round-trip' && returnDateQuery" @click.stop="returnDateQuery = ''" class="text-[9px] font-bold text-rose-400 cursor-pointer hover:text-rose-600 opacity-0 group-hover:opacity-100 transition-opacity">Xóa</span>
          </div>
          <div class="relative w-full group/input mt-0.5" :class="tripType === 'one-way' ? 'opacity-50' : ''">
            <input v-if="tripType === 'round-trip'" type="date" v-model="returnDateQuery" :min="dateQuery || todayDate" class="absolute inset-0 opacity-0 cursor-pointer w-full h-full z-10" />
            <div class="font-bold text-sm flex items-center justify-between w-full transition-colors gap-1" :class="tripType === 'round-trip' && returnDateQuery ? 'text-[#059669]' : 'text-zinc-400'">
              <span class="whitespace-nowrap truncate min-w-0">{{ tripType === 'one-way' ? '+ Thêm ngày về' : (returnDateQuery ? formatDisplayDate(returnDateQuery) : 'Tất cả các ngày') }}</span>
              <span class="material-symbols-outlined text-lg transition-colors shrink-0" :class="tripType === 'round-trip' ? (returnDateQuery ? 'text-emerald-500/50 group-hover/input:text-emerald-500' : 'text-zinc-400 group-hover/input:text-emerald-500') : ''">calendar_month</span>
            </div>
          </div>
        </div>

      </div>
      <button @click="handleSearch" class="search-cta w-full md:w-auto h-[48px] px-8 bg-[#059669] text-white rounded-full font-black text-[13px] tracking-wider uppercase hover:bg-emerald-500 hover:scale-105 transition-all duration-500 flex items-center justify-center gap-2 shadow-[0_10px_30px_-10px_rgba(5,150,105,0.8)] shrink-0 whitespace-nowrap">
        Tìm vé <span class="material-symbols-outlined text-base shrink-0">search</span>
      </button>
    </div>
    </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useApi } from '@/composables/useApi';
import { useHomeSummary } from '@/composables/useHomeSummary';
import { removeAccents } from '@/composables/useLocationSearch';
import { toBusinessDateString } from '@/utils/businessDate';

const router = useRouter();
const api = useApi();
const { getHomeSummary } = useHomeSummary();

// --- BANNERS LOGIC ---
const cachedBanner = localStorage.getItem('cached_landing_banner');
const heroBanners = ref([]);
const heroBannerUrl = ref(cachedBanner || '');

const currentBannerIndex = ref(0);
let bannerInterval = null;
const localBannerFallback = '/fleet/Trung-Nam-Limousine.png';

const useLocalBannerFallback = (event) => {
  const image = event.currentTarget;
  if (image.dataset.fallbackApplied === 'true') return;
  image.dataset.fallbackApplied = 'true';
  image.src = localBannerFallback;
};

const activeBanner = computed(() => {
  if (heroBannerUrl.value) {
    return heroBannerUrl.value;
  }
  return localBannerFallback;
});

// --- SEARCH FORM LOGIC ---
const tripType = ref('one-way');
const fromQuery = ref('');
const toQuery = ref('');
const todayDate = toBusinessDateString();
const dateQuery = ref(todayDate);
const returnDateQuery = ref('');

const formatDisplayDate = (d) => {
  if (!d) return 'Tất cả các ngày';
  const parts = d.split('-');
  return `${parts[2]}/${parts[1]}/${parts[0]}`;
};

const showFromDropdown = ref(false);
const showToDropdown = ref(false);

const allDeparturePoints = ref([]);
const allArrivalPoints = ref([]);

const filteredFrom = computed(() => {
  if (!fromQuery.value) return allDeparturePoints.value.slice(0, 8);
  const q = removeAccents(fromQuery.value);
  return allDeparturePoints.value.filter(loc => removeAccents(loc).includes(q)).slice(0, 8);
});

const filteredTo = computed(() => {
  if (!toQuery.value) return allArrivalPoints.value.slice(0, 8);
  const q = removeAccents(toQuery.value);
  return allArrivalPoints.value.filter(loc => removeAccents(loc).includes(q)).slice(0, 8);
});

const selectFrom = (loc) => {
  fromQuery.value = loc;
  showFromDropdown.value = false;
};

const selectTo = (loc) => {
  toQuery.value = loc;
  showToDropdown.value = false;
};

const closeFromDropdown = () => {
  setTimeout(() => {
    showFromDropdown.value = false;
  }, 200);
};

const closeToDropdown = () => {
  setTimeout(() => {
    showToDropdown.value = false;
  }, 200);
};

const swapLocations = () => {
  const temp = fromQuery.value;
  fromQuery.value = toQuery.value;
  toQuery.value = temp;
};

const handleSearch = () => {
  if (!fromQuery.value || !toQuery.value) return alert('Vui lòng nhập nơi đi và nơi đến!');
  
  const queryParams = { 
    from: fromQuery.value, 
    to: toQuery.value, 
    date: dateQuery.value 
  };
  
  if (tripType.value === 'round-trip') {
    if (!returnDateQuery.value) return alert('Vui lòng chọn ngày về cho chuyến khứ hồi!');
    if (new Date(returnDateQuery.value) < new Date(dateQuery.value)) return alert('Ngày về không thể trước ngày đi!');
    queryParams.returnDate = returnDateQuery.value;
  }
  
  router.push({ 
    path: '/booking/search', 
    query: queryParams
  });
};

const extractCityName = (fullName) => {
  if (!fullName) return '';
  return fullName.replace(/^(Bến xe Liên tỉnh|Bến xe Trung tâm|Bến xe|Trạm|Văn phòng|VP)\s+/i, '').trim();
};

const fetchActivePoints = async () => {
  try {
    const summary = await getHomeSummary();
    if (summary) {
      const rawFrom = summary.allDeparturePoints || [];
      const rawTo = summary.allArrivalPoints || [];
      
      allDeparturePoints.value = Array.from(new Set(rawFrom.map(extractCityName)));
      allArrivalPoints.value = Array.from(new Set(rawTo.map(extractCityName)));
    }
  } catch (err) {
    console.error("Lỗi tải danh sách tuyến đường:", err);
  }
};

onMounted(async () => {
  fetchActivePoints();
  
  try {
    const resBanners = await api.get('/settings/HERO_BANNERS');
    if (resBanners.data && resBanners.data.value) {
      const parsed = JSON.parse(resBanners.data.value).filter(b => b.isActive !== false);
      heroBanners.value = parsed;
      if (parsed.length > 0) {
        localStorage.setItem('cached_landing_banner', parsed[0].url);
        if (parsed.length > 1) {
          bannerInterval = setInterval(() => {
            currentBannerIndex.value = (currentBannerIndex.value + 1) % parsed.length;
          }, 5000);
        }
      }
    }
  } catch (e) { console.error("Failed to load banners array", e); }

  try {
    const resBanner = await api.get('/settings/hero_banner_url');
    if (resBanner.data && resBanner.data.value) {
      heroBannerUrl.value = resBanner.data.value;
      if (heroBanners.value.length === 0) {
        localStorage.setItem('cached_landing_banner', resBanner.data.value);
      }
    }
  } catch (err) { console.error("Failed to load hero_banner_url", err); }
});

onUnmounted(() => {
  if (bannerInterval) clearInterval(bannerInterval);
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,600;1,600&display=swap');

.elegant-text {
  font-family: 'Playfair Display', serif;
  font-style: italic;
  font-weight: 600;
}

@media (max-width: 767px) {
  .landing-hero {
    min-height: 100dvh;
    padding: 5.5rem 1rem 5.25rem;
    justify-content: flex-start;
  }

  .hero-image {
    object-position: 58% center !important;
    transform: scale(1.02) !important;
  }

  .hero-overlay-side {
    background: linear-gradient(to bottom, rgb(5 16 22 / 0.72), rgb(5 16 22 / 0.26) 46%, rgb(5 16 22 / 0.68));
  }

  .hero-overlay-bottom {
    background: linear-gradient(to top, rgb(4 18 19 / 0.76), transparent 58%);
  }

  .hero-content {
    margin-bottom: 0 !important;
  }

  .hero-eyebrow {
    display: none;
  }

  .hero-title {
    max-width: 21rem;
    margin: 0 0 0.7rem !important;
    font-size: clamp(2.05rem, 10vw, 2.6rem) !important;
    line-height: 1.08 !important;
    letter-spacing: -0.035em;
  }

  .hero-title .elegant-text {
    display: inline-block;
    padding-bottom: 0.15rem;
    line-height: 1.12;
  }

  .hero-copy {
    max-width: 33ch;
    margin-bottom: 1.2rem !important;
    font-size: 0.86rem !important;
    line-height: 1.55 !important;
    letter-spacing: 0.005em !important;
    color: rgb(255 255 255 / 0.86) !important;
  }

  .booking-widget {
    width: 100%;
    max-width: none;
    margin: clamp(5rem, 14dvh, 8rem) 0 0 !important;
  }

  .trip-type {
    margin: 0 0 0.55rem 0.2rem !important;
    gap: 1.25rem !important;
  }

  .trip-type label {
    font-size: 0.72rem !important;
  }

  .booking-shell {
    gap: 0.45rem !important;
    padding: 0.4rem !important;
    border-radius: 1.4rem !important;
    background: rgb(255 255 255 / 0.14) !important;
    box-shadow: 0 1.4rem 3rem rgb(0 0 0 / 0.28) !important;
  }

  .search-fields {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0 !important;
    padding: 0.35rem !important;
    border-radius: 1.05rem !important;
    align-items: stretch !important;
  }

  .search-field {
    min-width: 0;
    min-height: 3.65rem;
    padding: 0.65rem 0.75rem !important;
    border-right: 0 !important;
  }

  .search-field p {
    margin-bottom: 0.15rem !important;
    font-size: 0.58rem !important;
  }

  .search-field input,
  .search-field .font-bold {
    font-size: 0.78rem !important;
  }

  .location-from,
  .location-to {
    border-bottom: 1px solid #e4e4e7 !important;
  }

  .location-from,
  .departure-date {
    border-right: 1px solid #e4e4e7 !important;
  }

  .swap-button {
    position: absolute !important;
    top: 3.9rem;
    left: 50%;
    width: 1.8rem !important;
    height: 1.8rem !important;
    margin: 0 !important;
    transform: translate(-50%, -50%);
  }

  .swap-button:hover {
    transform: translate(-50%, -50%) scale(1.05);
  }

  .location-dropdown {
    top: calc(100% + 0.4rem) !important;
    bottom: auto !important;
    width: calc(200% + 0.35rem) !important;
    min-width: 0 !important;
    padding-right: 0 !important;
    border-radius: 0.9rem !important;
    box-shadow: 0 1rem 2.5rem rgb(15 23 42 / 0.2) !important;
  }

  .to-dropdown {
    left: calc(-100% - 0.35rem) !important;
  }

  .search-cta {
    height: 2.8rem !important;
    font-size: 0.75rem !important;
    box-shadow: 0 0.75rem 1.8rem rgb(5 150 105 / 0.3) !important;
  }

  .search-cta .material-symbols-outlined {
    display: none;
  }
}

@media (max-width: 380px) and (max-height: 720px) {
  .landing-hero {
    padding-top: 4.8rem;
  }

  .hero-title {
    font-size: 1.9rem !important;
  }

  .hero-copy {
    margin-bottom: 0.85rem !important;
    font-size: 0.78rem !important;
  }

  .booking-widget {
    margin-top: 1.5rem !important;
  }

  .search-field {
    min-height: 3.35rem;
  }

  .swap-button {
    top: 3.6rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero-image {
    animation: none !important;
    transition: none !important;
  }
}
</style>
