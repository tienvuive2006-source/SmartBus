<template>
  <header class="relative pt-32 pb-16 px-6 w-full min-h-[60vh] flex flex-col justify-end items-start text-left">
    <!-- Full background image -->
    <div class="absolute inset-0 z-0 overflow-hidden bg-zinc-900">
      
      <!-- Fallback / Cached image (Always stays behind to prevent black flash) -->
      <img :src="activeBanner" fetchpriority="high" class="absolute inset-0 w-full h-full object-cover object-[center_bottom] scale-105 animate-[kenburns_20s_ease-out_infinite_alternate] z-0" alt="Luxury bus background" />

      <!-- Slideshow images from API -->
      <img v-for="(banner, index) in heroBanners" :key="banner.url || index"
           :src="banner.url" 
           class="absolute inset-0 w-full h-full object-cover object-[center_bottom] scale-105 transition-opacity duration-1000 ease-in-out animate-[kenburns_20s_ease-out_infinite_alternate]"
           :class="index === currentBannerIndex && heroBanners.length > 0 ? 'opacity-100 z-10' : 'opacity-0 -z-10'"
           alt="Luxury bus background" />
      
      <!-- Lớp phủ tối mờ để dễ đọc chữ -->
      <div class="absolute inset-0 bg-gradient-to-r from-black/70 via-black/20 to-transparent z-10"></div>
      <div class="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent z-10"></div>
    </div>

    <div class="relative z-10 w-full max-w-[1400px] mx-auto mb-8 flex flex-col items-start">
      <div class="inline-flex items-center gap-3 px-5 py-2.5 rounded-full bg-black/30 backdrop-blur-md border border-white/20 shadow-sm mb-4 hover:bg-black/40 transition-colors cursor-pointer">
        <span class="flex h-2 w-2 relative">
          <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
          <span class="relative inline-flex rounded-full h-2 w-2 bg-amber-500"></span>
        </span>
        <span class="text-[11px] font-bold tracking-widest uppercase text-white/90 drop-shadow-md">THẾ HỆ XE GIƯỜNG NẰM VIP LIMOUSINE</span>
      </div>
      
      <h1 class="text-[2.5rem] md:text-[4.5rem] leading-[1.1] text-white mb-4 drop-shadow-2xl max-w-3xl">
        <span class="font-light tracking-wide text-white/90">Nâng tầm</span> <br />
        <span class="elegant-text bg-gradient-to-r from-amber-200 via-yellow-400 to-amber-600 text-transparent bg-clip-text drop-shadow-[0_2px_15px_rgba(251,191,36,0.3)]">
          chuyến đi của bạn.
        </span>
      </h1>
      
      <p class="text-base md:text-lg text-white/80 font-light tracking-wide max-w-xl mb-8 leading-relaxed drop-shadow-md">
        Tận hưởng không gian riêng tư tuyệt đối cùng dịch vụ chăm sóc tận tâm nhất. Hơn cả một chuyến đi, đó là sự tận hưởng.
      </p>

      <!-- FLOATING GLASSMORPHISM BOOKING WIDGET -->
      <div class="relative z-20 w-full max-w-[750px] flex flex-col items-start mt-2 mb-8">
        
        <!-- LỰA CHỌN LOẠI VÉ -->
      <div class="flex items-center gap-6 mb-3 ml-4">
        <label class="flex items-center gap-2 text-white/90 font-bold text-[13px] cursor-pointer drop-shadow-md hover:text-white transition-colors">
          <input type="radio" v-model="tripType" value="one-way" class="accent-emerald-500 w-4 h-4 cursor-pointer">
          Một chiều
        </label>
        <label class="flex items-center gap-2 text-white/90 font-bold text-[13px] cursor-pointer drop-shadow-md hover:text-white transition-colors">
          <input type="radio" v-model="tripType" value="round-trip" class="accent-emerald-500 w-4 h-4 cursor-pointer">
          Khứ hồi
        </label>
      </div>

      <div class="w-full bg-white/10 backdrop-blur-xl border border-white/20 shadow-[0_40px_80px_-20px_rgba(0,0,0,0.5)] rounded-[2rem] p-2 flex flex-col md:flex-row items-center gap-2 hover:shadow-[0_40px_100px_-20px_rgba(5,150,105,0.3)] hover:bg-white/15 transition-all duration-700">
        <div class="flex-1 flex flex-col md:flex-row items-center bg-white rounded-[1.5rem] md:rounded-full p-2 border border-zinc-200 w-full shadow-inner relative gap-y-2 md:gap-y-0">
        <!-- KHU VỰC NƠI ĐI -->
        <div class="flex-1 px-4 border-r border-zinc-200 relative">
          <p class="text-[8px] font-black uppercase text-zinc-400 tracking-wider mb-0.5">Nơi đi</p>
          <input 
            v-model="fromQuery" 
            @focus="showFromDropdown = true"
            @blur="closeFromDropdown"
            class="font-bold text-sm text-zinc-900 bg-transparent outline-none w-full placeholder-zinc-300" 
            placeholder="Chọn nơi đi" 
          />
          
          <Transition enter-active-class="transition duration-200 ease-out" enter-from-class="translate-y-2 opacity-0" enter-to-class="translate-y-0 opacity-100" leave-active-class="transition duration-150 ease-in" leave-from-class="translate-y-0 opacity-100" leave-to-class="translate-y-2 opacity-0">
            <div v-if="showFromDropdown" class="absolute left-0 bottom-[calc(100%+1rem)] w-max min-w-[150%] pr-2 bg-white/95 backdrop-blur-2xl border border-white/50 shadow-[0_-20px_60px_-10px_rgba(0,0,0,0.2)] rounded-2xl overflow-hidden z-[100]">
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
        <div @click="swapLocations" class="w-8 h-8 md:w-7 md:h-7 -ml-3 md:-ml-4 bg-zinc-900 rounded-full flex items-center justify-center shadow-md border-[2px] border-white z-10 cursor-pointer hover:scale-110 active:scale-95 transition-transform shrink-0 relative aspect-square">
          <span class="material-symbols-outlined text-white text-[14px]">swap_horiz</span>
        </div>

        <!-- KHU VỰC NƠI ĐẾN -->
        <div class="flex-1 px-4 border-r border-zinc-200 pl-5 relative">
          <p class="text-[8px] font-black uppercase text-zinc-400 tracking-wider mb-0.5">Nơi đến</p>
          <input 
            v-model="toQuery" 
            @focus="showToDropdown = true"
            @blur="closeToDropdown"
            class="font-bold text-sm text-zinc-900 bg-transparent outline-none w-full placeholder-zinc-300" 
            placeholder="Chọn nơi đến" 
          />
          
          <Transition enter-active-class="transition duration-200 ease-out" enter-from-class="translate-y-2 opacity-0" enter-to-class="translate-y-0 opacity-100" leave-active-class="transition duration-150 ease-in" leave-from-class="translate-y-0 opacity-100" leave-to-class="translate-y-2 opacity-0">
            <div v-if="showToDropdown" class="absolute left-0 bottom-[calc(100%+1rem)] w-max min-w-[150%] pr-2 bg-white/95 backdrop-blur-2xl border border-white/50 shadow-[0_-20px_60px_-10px_rgba(0,0,0,0.2)] rounded-2xl overflow-hidden z-[100]">
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
        <div class="flex-1 px-4 pl-5 relative border-r border-zinc-200 group">
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
        <div class="flex-1 px-4 pl-5 relative cursor-pointer group" @click="tripType = 'round-trip'">
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
      <button @click="handleSearch" class="w-full md:w-auto h-[48px] px-8 bg-[#059669] text-white rounded-full font-black text-[13px] tracking-wider uppercase hover:bg-emerald-500 hover:scale-105 transition-all duration-500 flex items-center justify-center gap-2 shadow-[0_10px_30px_-10px_rgba(5,150,105,0.8)] shrink-0 whitespace-nowrap">
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
import { removeAccents } from '@/composables/useLocationSearch';

const router = useRouter();
const api = useApi();

// --- BANNERS LOGIC ---
const cachedBanner = localStorage.getItem('cached_landing_banner');
const heroBanners = ref([]);
const heroBannerUrl = ref(cachedBanner || '');

const currentBannerIndex = ref(0);
let bannerInterval = null;

const activeBanner = computed(() => {
  if (heroBannerUrl.value) {
    return heroBannerUrl.value;
  }
  return '/fleet/bus_exterior.png'; // local fallback
});

// --- SEARCH FORM LOGIC ---
const tripType = ref('one-way');
const fromQuery = ref('');
const toQuery = ref('');
const todayDate = new Date().toISOString().split('T')[0];
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
    const res = await api.get('/trips/home-summary');
    if (res.data) {
      const rawFrom = res.data.allDeparturePoints || [];
      const rawTo = res.data.allArrivalPoints || [];
      
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
</style>
