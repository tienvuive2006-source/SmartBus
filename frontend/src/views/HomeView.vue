<template>
  <div class="min-h-screen bg-[#f2f5f8] font-sans text-slate-800">
    <nav class="bg-[#075955] text-white border-b border-[#05403d] sticky top-0 z-50">
      <div class="max-w-[95%] 2xl:max-w-[1600px] mx-auto px-4 h-16 flex items-center justify-between">
        
        <div class="flex items-center gap-8">
          <div class="flex items-center gap-2 cursor-pointer" @click="$router.push('/')">
            <span class="material-symbols-outlined text-white text-4xl">directions_bus</span>
            <div class="flex flex-col">
              <span class="text-xl font-bold leading-none tracking-tight">Trung - Nam</span>
              <span class="text-[9px] uppercase tracking-wider font-semibold">Nhà xe chuyên tuyến Miền Trung - Nam</span>
            </div>
          </div>
          
          <div class="hidden lg:flex items-center gap-5 text-[13px] font-semibold">
            <button @click="scrollToSection('searchSection')" class="hover:text-yellow-300 transition-colors flex items-center gap-1 bg-transparent border-none outline-none cursor-pointer text-white font-semibold">
              <span class="material-symbols-outlined text-[16px]">home</span> Mua vé
            </button>
            <button @click="openInfoModal('benxe')" class="hover:text-yellow-300 transition-colors bg-transparent border-none outline-none cursor-pointer text-white font-semibold">Bến Xe</button>
            <button @click="openInfoModal('nhaxe')" class="hover:text-yellow-300 transition-colors bg-transparent border-none outline-none cursor-pointer text-white font-semibold">Nhà Xe</button>
            <button @click="openInfoModal('diemden')" class="hover:text-yellow-300 transition-colors bg-transparent border-none outline-none cursor-pointer text-white font-semibold">Điểm đến</button>
            <button @click="openInfoModal('thongtin')" class="hover:text-yellow-300 transition-colors bg-transparent border-none outline-none cursor-pointer text-white font-semibold">Thông tin ngành vận tải</button>
            <button @click="scrollToSection('searchSection')" class="hover:text-yellow-300 transition-colors bg-transparent border-none outline-none cursor-pointer text-white font-semibold">Các tuyến đường chính</button>
          </div>
        </div>

        <div class="flex items-center gap-6">
          <div class="hidden md:flex items-center gap-2 text-yellow-400 font-bold text-lg">
            1900.59.99.97
          </div>

          <!-- Component Chuông thông báo -->
          <NotificationBell />

          <button @click="$router.push('/profile')" class="flex items-center gap-2 text-sm font-semibold hover:text-yellow-300 transition-colors ml-2">
            <span class="material-symbols-outlined text-2xl">person_outline</span>
            {{ currentUser?.fullName || '' }}
          </button>
          <div class="flex items-center gap-1 cursor-pointer">
            <img src="https://flagcdn.com/w20/vn.png" alt="VN" class="w-5 h-3.5 object-cover" />
            <img src="https://flagcdn.com/w20/gb.png" alt="EN" class="w-5 h-3.5 object-cover opacity-50 hover:opacity-100" />
          </div>
        </div>
      </div>
    </nav>

    <HomeBanner 
      :heroBanners="heroBanners" 
      :heroBannerUrl="heroBannerUrl" 
      :isAdmin="authStore.isAdmin" 
    />

    <HomeFilterBar 
      :popularRoutesCount="popularRoutes.length"
      v-model:filterDate="filterDate"
      v-model:filterFrom="filterFrom"
      v-model:filterTo="filterTo"
      v-model:filterCompany="filterCompany"
      :allDeparturePoints="allDeparturePoints"
      :allArrivalPoints="allArrivalPoints"
      :uniqueCompanies="uniqueCompanies"
      @clear-filters="clearAllFilters"
    />

    <HomeRouteGrid 
      :popularRoutes="popularRoutes"
      @quick-search="(from, to) => quickSearch(from, to, '')"
    />

    <HomeReviews 
      :topReviews="topReviews"
    />

    <!-- Info Modal for Bến Xe, Nhà Xe, Điểm Đến, Thông Tin -->
    <HomeInfoModal 
      v-if="showInfoModal"
      :activeModalType="activeModalType"
      :uniqueStations="uniqueStations"
      :uniqueCompanies="uniqueCompanies"
      :filterFrom="filterFrom"
      :filterTo="filterTo"
      @close="showInfoModal = false"
      @book-now="scrollToSection('searchSection'); showInfoModal = false"
      @select-station="selectStation"
      @select-company="selectCompany"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useApi } from '@/composables/useApi';
import AppFooter from '@/components/AppFooter.vue';
import NotificationBell from '@/components/NotificationBell.vue';
import HomeInfoModal from '@/components/home/HomeInfoModal.vue';
import HomeBanner from '@/components/home/HomeBanner.vue';
import HomeFilterBar from '@/components/home/HomeFilterBar.vue';
import HomeRouteGrid from '@/components/home/HomeRouteGrid.vue';
import HomeReviews from '@/components/home/HomeReviews.vue';
import { useLocationSearch } from '@/composables/useLocationSearch';
import { usePopularRoutes } from '@/composables/usePopularRoutes';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';

const router = useRouter();
const authStore = useAuthStore();
const api = useApi();
const currentUser = authStore.currentUser; // reactive computed
const trips = ref([]);
const fromQuery = ref('');
const toQuery = ref('');
const dateQuery = ref(new Date().toISOString().split('T')[0]);
const busCompanyQuery = ref('all'); // Khai báo thêm cho trường 'Nhà xe yêu thích'

// Bộ lọc tại danh sách hàng ngày
const filterDate = ref('');
const filterFrom = ref('');
const filterTo = ref('');
const filterCompany = ref('');

const clearAllFilters = () => {
  filterDate.value = '';
  filterFrom.value = '';
  filterTo.value = '';
  filterCompany.value = '';
};

// 🚀 CHUẨN ĐỒ ÁN: Quản lý xem chi tiết thông tin nhà xe, bến xe, điểm đến qua modal (ĐỒNG BỘ TỪ ADMIN ĐĂNG)
const showInfoModal = ref(false);
const activeModalType = ref(''); // '', 'benxe', 'nhaxe', 'diemden', 'thongtin'

const uniqueStations = ref([]);
const uniqueCompanies = ref([]);

const scrollToSection = (id) => {
  const element = document.getElementById(id);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
};

const openInfoModal = (type) => {
  activeModalType.value = type;
  showInfoModal.value = true;
};

const selectStation = (stationName, role) => {
  if (role === 'from') {
    filterFrom.value = stationName;
    filterTo.value = ''; // Xoá điểm đến để hiện tất cả chuyến đi từ bến này
  } else {
    filterTo.value = stationName;
    filterFrom.value = ''; // Xoá điểm đi để hiện tất cả chuyến đến bến này
  }
  showInfoModal.value = false;
  scrollToSection('scheduleSection');
};

const selectCompany = (companyName) => {
  filterCompany.value = companyName;
  showInfoModal.value = false;
  scrollToSection('scheduleSection');
};

const { 
  suggestions: fromSuggestions, 
  showDropdown: showFromDropdown, 
  handleFocus: onFromFocus, 
  handleSelect: fromSelect,
  performSearch: searchFrom,
  closeDropdown: closeFrom
} = useLocationSearch();

const { 
  suggestions: toSuggestions, 
  showDropdown: showToDropdown, 
  handleFocus: onToFocus, 
  handleSelect: toSelect,
  performSearch: searchTo,
  closeDropdown: closeTo
} = useLocationSearch();

watch(fromQuery, (val) => searchFrom(val));
watch(toQuery, (val) => searchTo(val));

const selectFromLocation = (loc) => fromSelect(loc, (val) => fromQuery.value = val);
const selectToLocation = (loc) => toSelect(loc, (val) => toQuery.value = val);

const fetchTrips = async () => {
  try {
    const response = await api.get('/trips/home-summary');
    allDeparturePoints.value = response.data.allDeparturePoints || [];
    allArrivalPoints.value = response.data.allArrivalPoints || [];
    uniqueStations.value = response.data.uniqueStations || [];
    uniqueCompanies.value = response.data.uniqueCompanies || [];
    trips.value = response.data.uniqueTrips || [];
  } catch (err) { console.error("Lỗi tải trang chủ:", err); }
};

const fromContainer = ref(null);
const toContainer = ref(null);
const handleClickOutside = (event) => {
  if (fromContainer.value && !fromContainer.value.contains(event.target)) closeFrom();
  if (toContainer.value && !toContainer.value.contains(event.target)) closeTo();
};

function handleSearch() {
  if (!fromQuery.value || !toQuery.value) return alert('Vui lòng chọn điểm đi/đến!');
  // Có thể truyền thêm busCompanyQuery vào nếu BE hỗ trợ
  router.push({ path: '/booking/search', query: { from: fromQuery.value, to: toQuery.value, date: dateQuery.value, company: busCompanyQuery.value } });
}

const simplifyLocation = (loc) => {
  if (!loc) return '';
  const parts = loc.split(',');
  let s = parts[parts.length - 1].trim();
  s = s.replace(/\b(Thành phố|TP|Tỉnh|Hà Nội|Hồ Chí Minh|Đà Nẵng|Cần Thơ|Hải Phòng)\b/gi, (match) => {
    if (match.toLowerCase() === 'thành phố' || match.toLowerCase() === 'tp' || match.toLowerCase() === 'tỉnh') return '';
    return match;
  }).trim();
  if (!s) s = parts[0].replace(/\b(Bến xe|Phường|Quận|Huyện|Xã|TT)\b/gi, '').trim();
  return s;
};

const quickSearch = (from, to, date) => {
  fromQuery.value = from;
  toQuery.value = to;
  if (date) {
    dateQuery.value = date.split('T')[0];
  } else {
    dateQuery.value = '';
  }
  handleSearch();
};

const allDeparturePoints = ref([]);
const allArrivalPoints = ref([]);

const debugRoutes = ref([]);
const topReviews = ref([]);

const { popularRoutes } = usePopularRoutes(trips, filterFrom, filterTo, filterCompany, filterDate);

const currentUser2 = ref(null);

const heroBannerUrl = ref(localStorage.getItem('cached_hero_banner') || '');
const heroBanners = ref([]);

const fetchSettings = async () => {
  try {
    // Tải mảng Banner
    const resBanners = await api.get('/settings/HERO_BANNERS');
    if (resBanners.data && resBanners.data.value) {
      // Chỉ lấy các banner đang bật (isActive !== false)
      heroBanners.value = JSON.parse(resBanners.data.value).filter(b => b.isActive !== false);
    }
  } catch (e) { console.error("Lỗi tải banners mảng", e) }

  try {
    // Tải Banner cũ làm fallback
    const resBanner = await api.get('/settings/hero_banner_url');
    if (resBanner.data && resBanner.data.value) {
      heroBannerUrl.value = resBanner.data.value;
      localStorage.setItem('cached_hero_banner', resBanner.data.value);
    } else if (!heroBannerUrl.value) {
      heroBannerUrl.value = 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?q=80&w=2069&auto=format&fit=crop';
    }
  } catch (err) {
    console.error("Lỗi khi tải cài đặt:", err);
    if (!heroBannerUrl.value) {
      heroBannerUrl.value = 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?q=80&w=2069&auto=format&fit=crop';
    }
  }
};

const fetchTopReviews = async () => {
  try {
    const res = await api.get('/reviews/all');
    if (res.data && Array.isArray(res.data)) {
      topReviews.value = res.data
        .filter(r => r.rating >= 4 && r.comment && r.comment.length > 10)
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 3);
    }
  } catch (err) {
    console.error("Lỗi khi tải đánh giá nổi bật:", err);
  }
};

onMounted(() => {
  window.scrollTo(0, 0);
  fetchTrips();
  fetchSettings();
  fetchTopReviews();
  window.addEventListener('click', handleClickOutside);
});
onUnmounted(() => {
  window.removeEventListener('click', handleClickOutside);
});
</script>