<template>
  <div class="min-h-screen bg-[#f7f9f8] pb-20 font-sans text-slate-900">
    <BookingHeader 
      :available-locations="availableLocations" 
      :format-date-display="formatDateDisplay"
      :return-trip-count="returnTripCount"
      :checking-return-trips="checkingReturnTrips"
      :round-trip-active="isRoundTrip"
      :return-date-suggestions="returnDateSuggestions"
      @location-change="handleLocationChange"
      @date-change="handleDateChange"
      @return-date-change="handleReturnDateChange"
      @select-return-date="handleReturnDateSuggestion"
      @passenger-change="handlePassengerChange"
      @trip-type-change="handleTripTypeChange"
      @swap-locations="handleSwapLocations"
      @search="fetchTrips"
    />

    <main class="mx-auto grid max-w-[1600px] grid-cols-1 gap-7 px-4 py-5 lg:grid-cols-[260px_minmax(0,1fr)] xl:px-8">
      
      <BookingSidebar 
        v-model:modelValueSort="currentSort"
        v-model:modelValueTime="selectedTimeSlots"
        v-model:modelValuePriceMax="priceMax"
        v-model:modelValueBusTypes="selectedBusTypes"
        v-model:modelValueSeatClasses="selectedSeatClasses"
        v-model:modelValueUtilities="selectedUtilities"
        :sort-options="sortOptions"
        :bus-type-options="busTypeFilterOptions"
        :seat-class-options="seatClassFilterOptions"
        :utility-options="utilityFilterOptions"
        :price-ceiling="priceMaxDefault"
        @clear-filters="clearFilters"
      />

      <section class="min-w-0 space-y-4">
        
        <!-- ROUND-TRIP WIZARD PROGRESS BAR -->
        <div v-if="isRoundTrip && ($route.query.returnDate || outboundTrip)" class="bg-white rounded-2xl border border-emerald-100 shadow-sm p-4 flex items-center justify-between relative overflow-hidden mb-6">
           <div class="absolute inset-0 bg-gradient-to-r from-emerald-50/50 to-transparent pointer-events-none"></div>
           
           <!-- Tab Chuyến Đi -->
           <div class="flex-1 flex flex-col items-center relative z-10 transition-opacity" :class="bookingStep === 'OUTBOUND' ? 'opacity-100' : 'opacity-40 cursor-pointer hover:opacity-80'" @click="setBookingStep('OUTBOUND')">
              <div class="w-10 h-10 rounded-full flex items-center justify-center font-black text-sm mb-2 transition-colors relative" :class="bookingStep === 'OUTBOUND' ? 'bg-[#075955] text-white shadow-md' : 'bg-zinc-100 text-zinc-400'">
                 <span v-if="outboundTrip && bookingStep === 'RETURN'" class="material-symbols-outlined text-lg text-[#075955] absolute">check</span>
                 <span v-else>1</span>
              </div>
              <span class="text-xs font-bold uppercase tracking-widest" :class="bookingStep === 'OUTBOUND' ? 'text-[#075955]' : 'text-zinc-400'">Chuyến đi</span>
              <span class="text-[10px] text-zinc-400 mt-1">{{ formatDateDisplay($route.query.date) }}</span>
           </div>

           <div class="w-24 h-px bg-zinc-200 relative z-10">
              <div class="absolute inset-y-0 left-0 bg-[#075955] transition-all duration-500" :style="{ width: bookingStep === 'RETURN' ? '100%' : '0%' }"></div>
           </div>

           <!-- Tab Chuyến Về -->
           <div class="flex-1 flex flex-col items-center relative z-10 transition-opacity" :class="bookingStep === 'RETURN' ? 'opacity-100' : 'opacity-40 cursor-pointer hover:opacity-80'" @click="setBookingStep('RETURN')">
              <div class="w-10 h-10 rounded-full flex items-center justify-center font-black text-sm mb-2 transition-colors relative" :class="bookingStep === 'RETURN' ? 'bg-[#075955] text-white shadow-md' : 'bg-zinc-100 text-zinc-400'">
                 <span v-if="outboundTrip && bookingStep === 'RETURN'" class="absolute -top-1 -right-1 flex h-3 w-3"><span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span><span class="relative inline-flex rounded-full h-3 w-3 bg-emerald-500 border border-white"></span></span>
                 2
              </div>
              <span class="text-xs font-bold uppercase tracking-widest" :class="bookingStep === 'RETURN' ? 'text-[#075955]' : 'text-zinc-400'">Chuyến về</span>
              <span class="text-[10px] text-zinc-400 mt-1">{{ formatDateDisplay($route.query.returnDate) }}</span>
           </div>
        </div>

        <div class="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
          <div class="inline-flex w-fit overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
            <button type="button" class="flex items-center gap-2 border-b-2 border-[#075955] bg-[#f5faf8] px-5 py-3 text-[13px] font-bold text-[#075955]">
              <span class="material-symbols-outlined text-[20px]">format_list_bulleted</span>Danh sách
            </button>
            <button type="button" class="flex items-center gap-2 px-5 py-3 text-[13px] font-semibold text-slate-600 transition hover:bg-slate-50" @click="openFirstTripMap">
              <span class="material-symbols-outlined text-[20px]">map</span>Bản đồ
            </button>
          </div>

          <div class="flex min-w-0 items-center gap-2 overflow-x-auto pb-1 scrollbar-hide">
            <span class="mr-2 shrink-0 text-xs font-medium text-slate-500">Tìm thấy <b class="text-slate-700">{{ filteredTrips.length }}</b> chuyến</span>
          </div>
        </div>

        <div class="hidden">
           <div>
             <h2 class="text-2xl md:text-3xl font-black text-zinc-900 tracking-tight">Tuyển tập chuyến đi</h2>
             <p class="text-sm font-bold text-zinc-500 mt-1 uppercase tracking-widest">Tìm thấy {{ filteredTrips.length }} kết quả phù hợp</p>
           </div>
           
           <div class="flex gap-3 overflow-x-auto pb-2 md:pb-0 scrollbar-hide">
              <button class="bg-white border border-zinc-200/80 px-4 py-2.5 rounded-[1rem] text-[11px] font-bold uppercase tracking-widest text-zinc-500 hover:border-emerald-500 hover:text-emerald-600 hover:shadow-[0_8px_20px_-10px_rgba(5,150,105,0.2)] transition-all whitespace-nowrap shadow-sm active:scale-95 flex items-center gap-1.5"><span class="material-symbols-outlined text-[14px]">local_fire_department</span> Ưu đãi 50%</button>
              <button class="bg-white border border-zinc-200/80 px-4 py-2.5 rounded-[1rem] text-[11px] font-bold uppercase tracking-widest text-zinc-500 hover:border-emerald-500 hover:text-emerald-600 hover:shadow-[0_8px_20px_-10px_rgba(5,150,105,0.2)] transition-all whitespace-nowrap shadow-sm active:scale-95 flex items-center gap-1.5"><span class="material-symbols-outlined text-[14px]">directions_car</span> Xe Limousine</button>
              <button class="bg-white border border-zinc-200/80 px-4 py-2.5 rounded-[1rem] text-[11px] font-bold uppercase tracking-widest text-zinc-500 hover:border-emerald-500 hover:text-emerald-600 hover:shadow-[0_8px_20px_-10px_rgba(5,150,105,0.2)] transition-all whitespace-nowrap shadow-sm active:scale-95 flex items-center gap-1.5"><span class="material-symbols-outlined text-[14px]">payments</span> Giá rẻ nhất</button>
           </div>
        </div>

        <div v-if="loading" class="flex justify-center py-32"><div class="w-12 h-12 border-4 border-zinc-200 border-t-[#075955] rounded-full animate-spin"></div></div>

        <div v-else class="space-y-6">
          <div v-if="awaitingReturnDate && filteredTrips.length === 0" class="flex items-center gap-4 rounded-xl border border-emerald-100 bg-white px-5 py-4 shadow-sm">
            <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-emerald-50 text-[#075955]">
              <span class="material-symbols-outlined text-2xl">event</span>
            </div>
            <div class="min-w-0 text-left">
              <h3 class="text-sm font-extrabold text-slate-900">Đã chọn chuyến đi</h3>
              <p class="mt-1 text-xs text-slate-500">Chọn ngày về trong thanh tìm kiếm hoặc dùng ngày được gợi ý để xem chuyến chiều về.</p>
            </div>
          </div>

          <BookingTripCard
            v-for="trip in filteredTrips" 
            :key="trip.id" 
            :trip="trip"
            :company-stats="companyStats"
            :all-buses="allBuses"
            :all-bus-types="allBusTypes"
            :is-compared="isTripCompared(trip)"
            @open-map="openMapModal"
            @open-stops="openStopsModal"
            @open-reviews="openReviewsModal"
            @select-trip="handleSelectTrip"
            @toggle-compare="toggleComparedTrip"
          />
          
          <div v-if="filteredTrips.length === 0 && !awaitingReturnDate" class="bg-white py-24 px-8 rounded-[2rem] border border-zinc-100 text-center shadow-[0_8px_30px_rgb(0,0,0,0.04)] flex flex-col items-center">
             <div class="w-20 h-20 bg-zinc-50 rounded-full flex items-center justify-center mb-6 border border-zinc-100">
               <span class="material-symbols-outlined text-4xl text-zinc-300">search_off</span>
             </div>
             <h3 class="text-2xl font-black text-zinc-900 tracking-tight">Không tìm thấy chuyến xe</h3>
             <p class="text-sm font-medium text-zinc-500 mt-3 max-w-md">Chúng tôi không tìm thấy chuyến đi nào khớp với bộ lọc của bạn. Vui lòng thử lại với thời gian hoặc lộ trình khác.</p>
             <button @click="$router.push('/')" class="mt-8 px-8 py-3.5 bg-zinc-900 hover:bg-zinc-800 text-white rounded-xl font-bold text-xs uppercase tracking-widest transition-colors shadow-lg active:scale-95 flex items-center gap-2">
                <span class="material-symbols-outlined text-[16px]">home</span> Về trang chủ
             </button>
          </div>
        </div>
      </section>
    </main>

    <transition name="compare-bar">
      <aside v-if="comparedTrips.length" class="fixed bottom-5 left-1/2 z-40 w-[min(92vw,820px)] -translate-x-1/2 rounded-2xl border border-white/20 bg-[#064f4a]/95 p-3 text-white shadow-2xl shadow-emerald-950/30 backdrop-blur-xl">
        <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div class="flex min-w-0 items-center gap-2 overflow-x-auto scrollbar-hide">
            <span class="shrink-0 px-2 text-xs font-bold">So sánh {{ comparedTrips.length }}/3</span>
            <button v-for="trip in comparedTrips" :key="`compare-chip-${trip.id}`" type="button" class="flex shrink-0 items-center gap-1 rounded-lg bg-white/12 px-2.5 py-2 text-[10px] font-semibold hover:bg-white/18" @click="removeComparedTrip(trip)">
              <span class="max-w-[120px] truncate">{{ trip.departureTime }} · {{ trip.busType }}</span>
              <span class="material-symbols-outlined text-[14px]">close</span>
            </button>
          </div>
          <div class="flex shrink-0 gap-2">
            <button type="button" class="px-3 py-2 text-[11px] font-bold text-emerald-100 hover:text-white" @click="clearComparedTrips">Xóa</button>
            <button type="button" class="rounded-xl bg-[#e2b43c] px-4 py-2.5 text-xs font-extrabold text-[#173b37] transition hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-45" :disabled="comparedTrips.length < 2" @click="isCompareModalOpen = true">
              {{ comparedTrips.length < 2 ? 'Chọn thêm 1 chuyến' : 'So sánh ngay' }}
            </button>
          </div>
        </div>
      </aside>
    </transition>

    <transition name="toast">
      <div v-if="actionToast" class="fixed right-5 top-24 z-[60] flex max-w-sm items-start gap-2 rounded-xl px-4 py-3 text-xs font-bold shadow-xl" :class="actionToast.type === 'error' ? 'bg-rose-50 text-rose-700' : 'bg-white text-[#075955]'" role="status">
        <span class="material-symbols-outlined text-[19px]">{{ actionToast.type === 'error' ? 'error' : 'check_circle' }}</span>{{ actionToast.message }}
      </div>
    </transition>

    <!-- 🗺️ BẢN ĐỒ HÀNH TRÌNH MODAL -->
    <BookingMapModal 
      v-if="isMapModalOpen"
      :trip="selectedTripForMap"
      @close="closeMapModal"
    />

    <!-- ĐÁNH GIÁ MODAL -->
    <BookingReviewsModal
      v-if="isReviewsModalOpen"
      :trip="selectedTripForReviews"
      @close="closeReviewsModal"
    />

    <BookingStopPreviewModal
      v-if="isStopsModalOpen"
      :trip="selectedTripForStops"
      @close="closeStopsModal"
    />

    <BookingCompareModal
      v-if="isCompareModalOpen"
      :trips="comparedTrips"
      :company-stats="companyStats"
      @close="isCompareModalOpen = false"
      @remove="removeComparedTrip"
      @clear="clearComparedTrips"
      @select="selectComparedTrip"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useApi } from '@/composables/useApi';
import { removeAccents } from '../../composables/useLocationSearch';

import BookingHeader from '@/components/booking/BookingHeader.vue';
import BookingSidebar from '@/components/booking/BookingSidebar.vue';
import BookingTripCard from '@/components/booking/BookingTripCard.vue';
import BookingMapModal from '@/components/booking/BookingMapModal.vue';
import BookingReviewsModal from '@/components/booking/BookingReviewsModal.vue';
import BookingStopPreviewModal from '@/components/booking/BookingStopPreviewModal.vue';
import BookingCompareModal from '@/components/booking/BookingCompareModal.vue';

const route = useRoute();
const router = useRouter();
const api = useApi();

const allTrips = ref([]);
const allBuses = ref([]);
const allBusTypes = ref([]);
const companyStats = ref({});
const loading = ref(true);
const currentSort = ref(route.query.sort || 'default');
const selectedTimeSlots = ref([]);
const priceMaxDefault = computed(() => {
  if (!allTrips.value.length) return 1200000;
  const max = Math.max(...allTrips.value.map(t => Number(t.price || 0)));
  // Làm tròn lên bội số 100.000 gần nhất
  return Math.ceil(max / 100000) * 100000;
});
const priceMax = ref(1200000);
const selectedBusTypes = ref([]);
const selectedSeatClasses = ref([]);
const selectedUtilities = ref([]);
const availableLocations = ref([]);
const returnTripCount = ref(0);
const checkingReturnTrips = ref(false);
const returnDateSuggestions = ref([]);
const comparedTrips = ref([]);
const isCompareModalOpen = ref(false);
const actionToast = ref(null);
let toastTimer = null;

const showActionToast = (message, type = 'success') => {
  actionToast.value = { message, type };
  if (toastTimer) clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { actionToast.value = null; }, 2800);
};

const isTripCompared = (trip) => comparedTrips.value.some((item) => String(item.id) === String(trip.id));

const toggleComparedTrip = (trip) => {
  if (isTripCompared(trip)) {
    removeComparedTrip(trip);
    return;
  }
  if (comparedTrips.value.length >= 3) {
    showActionToast('Bạn chỉ có thể so sánh tối đa 3 chuyến.', 'error');
    return;
  }
  comparedTrips.value = [...comparedTrips.value, trip];
};

const removeComparedTrip = (trip) => {
  comparedTrips.value = comparedTrips.value.filter((item) => String(item.id) !== String(trip.id));
  if (comparedTrips.value.length < 2) isCompareModalOpen.value = false;
};

const clearComparedTrips = () => {
  comparedTrips.value = [];
  isCompareModalOpen.value = false;
};

const selectComparedTrip = (trip) => {
  isCompareModalOpen.value = false;
  handleSelectTrip(trip);
};

const getUtilityIcon = (name) => {
  const value = normalize(name);
  if (value.includes('wifi') || value.includes('wi fi')) return 'wifi';
  if (value.includes('usb') || value.includes('sac')) return 'usb';
  if (value.includes('nuoc')) return 'water_bottle';
  if (value.includes('ve sinh') || value.includes('toilet') || value.includes('wc')) return 'wc';
  if (value.includes('chan') || value.includes('giuong')) return 'bed';
  if (value.includes('tu lanh')) return 'kitchen';
  if (value.includes('massage')) return 'airline_seat_recline_extra';
  if (value.includes('tivi') || value.includes('man hinh')) return 'tv';
  if (value.includes('tai nghe')) return 'headphones';
  return 'check_circle';
};

const busTypeFilterOptions = computed(() => allBusTypes.value
  .filter((item) => item?.name)
  .map((item) => ({ value: item.name, label: item.name }))
  .sort((a, b) => a.label.localeCompare(b.label, 'vi')));

const seatClassFilterOptions = computed(() => {
  const seatCounts = [...new Set(allBusTypes.value
    .map((item) => Number(item?.seatCount))
    .filter((count) => Number.isFinite(count) && count > 0))]
    .sort((a, b) => a - b);
  return seatCounts.map((count) => ({ value: String(count), label: `${count} chỗ` }));
});

const utilityFilterOptions = computed(() => {
  const names = [...new Set(allBusTypes.value
    .flatMap((item) => String(item?.description || '').split(','))
    .map((item) => item.trim())
    .filter(Boolean))]
    .sort((a, b) => a.localeCompare(b, 'vi'));
  return names.map((name) => ({ value: name, label: name, icon: getUtilityIcon(name) }));
});

// Wizard State
const isRoundTrip = computed(() => route.query.tripType === 'round-trip' || !!route.query.returnDate);
const bookingStep = ref('OUTBOUND'); // 'OUTBOUND' | 'RETURN'
const outboundTrip = ref(null);
const awaitingReturnDate = computed(() => isRoundTrip.value && bookingStep.value === 'RETURN' && !route.query.returnDate);

const setBookingStep = (step) => {
  if (bookingStep.value === step) return;
  bookingStep.value = step;
  fetchTrips();
};

const resetWizard = () => {
  bookingStep.value = 'OUTBOUND';
  outboundTrip.value = null;
  fetchTrips();
};

const handleSelectTrip = async (trip) => {
  if (isRoundTrip.value) {
    if (bookingStep.value === 'OUTBOUND') {
      outboundTrip.value = trip;
      bookingStep.value = 'RETURN';

      // Khi người dùng chọn chuyến từ danh sách chưa lọc ngày, lấy chính ngày
      // khởi hành của chuyến đó làm ngày đi để có thể tìm chiều về ngay lập tức.
      const selectedDepartureDate = trip.departureDate?.split('T')[0];
      const currentReturnDate = route.query.returnDate ? String(route.query.returnDate) : '';
      if (selectedDepartureDate) {
        await updateQuery({
          date: selectedDepartureDate,
          tripType: 'round-trip',
          returnDate: currentReturnDate && currentReturnDate >= selectedDepartureDate
            ? currentReturnDate
            : undefined
        });
      }

      if (!route.query.returnDate) {
        // Luôn tải lại gợi ý theo ngày của chuyến vừa chọn, không dùng danh sách
        // gợi ý cũ được tạo khi thanh tìm kiếm chưa có ngày đi.
        await fetchReturnDateSuggestions();
      }

      // Không tự điền ngày về. Khi ô ngày về để trống, fetchTrips tải tất cả
      // chuyến ngược chiều hợp lệ để khách tự chọn; ngày chỉ là bộ lọc tùy chọn.
      await refreshReturnTripData();
      await fetchTrips();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else { // RETURN
      if (!outboundTrip.value) {
        setBookingStep('OUTBOUND');
        window.scrollTo({ top: 0, behavior: 'smooth' });
        return;
      }
      router.push({
        path: '/booking/seat',
        query: {
          outboundTripId: outboundTrip.value.id,
          returnTripId: trip.id
        }
      });
    }
  } else {
    // One-way
    router.push({ path: '/booking/seat', query: { tripId: trip.id } });
  }
};

const extractCityName = (fullName) => {
  if (!fullName) return '';
  let city = String(fullName).trim();
  if (city.includes(',')) city = city.split(',').pop().trim();

  const prefixPattern = /^(Bến xe|Trạm|Văn phòng|VP|Liên tỉnh|Trung tâm|Phía Nam|Phía Bắc)\s+/i;
  while (prefixPattern.test(city)) city = city.replace(prefixPattern, '').trim();
  return city;
};

const fetchAllLocations = async () => {
  try {
    const res = await api.get('/trips');
    const locationsSet = new Set();
    res.data.forEach(trip => {
      if (trip.departurePoint) locationsSet.add(extractCityName(trip.departurePoint));
      if (trip.arrivalPoint) locationsSet.add(extractCityName(trip.arrivalPoint));
    });
    availableLocations.value = Array.from(locationsSet).filter(Boolean).sort((a, b) => a.localeCompare(b, 'vi'));

    const normalizedFrom = extractCityName(route.query.from);
    const normalizedTo = extractCityName(route.query.to);
    if (normalizedFrom !== route.query.from || normalizedTo !== route.query.to) {
      await router.replace({
        query: {
          ...route.query,
          ...(normalizedFrom ? { from: normalizedFrom } : {}),
          ...(normalizedTo ? { to: normalizedTo } : {})
        }
      });
    }
  } catch (err) {
    console.error("Lỗi fetch all locations:", err);
  }
};

watch(() => route.query.sort, (newSort) => {
  if (newSort) {
    currentSort.value = newSort;
  } else {
    currentSort.value = 'default';
  }
});

const handleDateChange = (e) => {
  const newDate = e.target.value;
  const newQuery = { ...route.query };
  if (newDate) newQuery.date = newDate;
  else delete newQuery.date;
  
  router.replace({ query: newQuery }).then(() => {
    fetchTrips();
    refreshReturnTripData();
  });
};

const updateQuery = async (updates) => {
  const newQuery = { ...route.query };
  Object.entries(updates).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') delete newQuery[key];
    else newQuery[key] = value;
  });
  await router.replace({ query: newQuery });
};

const checkReturnTripAvailability = async () => {
  if (!route.query.returnDate || !route.query.from || !route.query.to) {
    returnTripCount.value = 0;
    return;
  }

  checkingReturnTrips.value = true;
  try {
    const response = await api.get('/trips/search', {
      params: {
        from: route.query.to,
        to: route.query.from,
        date: route.query.returnDate
      }
    });
    returnTripCount.value = (Array.isArray(response.data) ? response.data : [])
      .filter((trip) => trip.isVisible !== false && Number(trip.availableSeats || 0) > 0)
      .length;
  } catch (error) {
    console.error('Không thể kiểm tra chuyến về:', error);
    returnTripCount.value = 0;
  } finally {
    checkingReturnTrips.value = false;
  }
};

const fetchReturnDateSuggestions = async () => {
  if (!isRoundTrip.value || !route.query.date || !route.query.from || !route.query.to) {
    returnDateSuggestions.value = [];
    return;
  }

  try {
    const response = await api.get('/trips/search', {
      params: {
        from: route.query.to,
        to: route.query.from
      }
    });
    const departureDate = String(route.query.date);
    returnDateSuggestions.value = [...new Set((Array.isArray(response.data) ? response.data : [])
      .filter((trip) => trip.isVisible !== false && Number(trip.availableSeats || 0) > 0)
      .map((trip) => trip.departureDate?.split('T')[0])
      .filter((date) => date && date >= departureDate))]
      .sort()
      .slice(0, 4);
  } catch (error) {
    console.error('Không thể tải ngày về gợi ý:', error);
    returnDateSuggestions.value = [];
  }
};

const refreshReturnTripData = async () => {
  await Promise.all([checkReturnTripAvailability(), fetchReturnDateSuggestions()]);
};

const handleReturnDateChange = async (event) => {
  await updateQuery({ returnDate: event.target.value, tripType: 'round-trip' });
  if (outboundTrip.value && bookingStep.value === 'RETURN') {
    await fetchTrips();
  } else {
    resetWizard();
  }
  await refreshReturnTripData();
};

const handleReturnDateSuggestion = async (date) => {
  await updateQuery({ returnDate: date, tripType: 'round-trip' });
  if (outboundTrip.value && bookingStep.value === 'RETURN') await fetchTrips();
  else resetWizard();
  await refreshReturnTripData();
};

const handlePassengerChange = (count) => updateQuery({ passengers: count > 1 ? count : undefined });

const handleTripTypeChange = async (type) => {
  if (type === 'one-way') {
    await updateQuery({ returnDate: undefined, tripType: undefined });
  } else {
    await updateQuery({ tripType: 'round-trip' });
  }
  resetWizard();
  await refreshReturnTripData();
};

const handleLocationChange = (type, val) => {
  const newQuery = { ...route.query };
  if (val) newQuery[type] = val;
  else delete newQuery[type];
  
  router.replace({ query: newQuery }).then(() => {
    resetWizard();
    refreshReturnTripData();
  });
};

const handleSwapLocations = () => {
  const currentFrom = route.query.from;
  const currentTo = route.query.to;
  
  if (!currentFrom && !currentTo) return;
  
  const newQuery = { ...route.query };
  
  if (currentTo) newQuery.from = currentTo;
  else delete newQuery.from;
  
  if (currentFrom) newQuery.to = currentFrom;
  else delete newQuery.to;
  
  router.replace({ query: newQuery }).then(() => {
    resetWizard();
    refreshReturnTripData();
  });
};

const formatDateDisplay = (d) => {
  if (!d) return 'Chọn ngày';
  return new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
};

const sortOptions = [
  { id: 'default', name: 'Mặc định' },
  { id: 'price_asc', name: 'Giá thấp nhất' },
  { id: 'price_desc', name: 'Giá cao nhất' },
  { id: 'time_asc', name: 'Ngày đi sớm nhất' },
  { id: 'time_desc', name: 'Ngày đi muộn nhất' },
  { id: 'duration_asc', name: 'Thời gian di chuyển ngắn nhất' },
  { id: 'rating_desc', name: 'Đánh giá cao nhất' }
];

const normalize = (s) => {
  return removeAccents(s || '')
    .toLowerCase()
    .replace(/hcm|sai gon/g, 'ho chi minh')
    .replace(/hn/g, 'ha noi')
    .replace(/[,.-]/g, ' ')
    .replace(/\b(ben xe|thanh pho|tp|tinh|huyen|xa|quan|phuong)\b/gi, '')
    .replace(/\s+/g, ' ')
    .trim();
};

const departureSortValue = (trip) => {
  const date = String(trip?.departureDate || '').split('T')[0];
  const time = String(trip?.departureTime || '00:00').padStart(5, '0');
  return `${date}T${time}`;
};

const tripDepartureTimestamp = (trip) => {
  const date = String(trip?.departureDate || '').split('T')[0];
  const time = String(trip?.departureTime || '00:00').slice(0, 5);
  if (!date) return Number.NaN;
  return new Date(`${date}T${time}:00`).getTime();
};

const tripArrivalTimestamp = (trip) => {
  const departure = tripDepartureTimestamp(trip);
  if (!Number.isFinite(departure)) return Number.NaN;
  const date = String(trip?.departureDate || '').split('T')[0];
  const time = String(trip?.arrivalTime || trip?.departureTime || '00:00').slice(0, 5);
  let arrival = new Date(`${date}T${time}:00`).getTime();
  if (arrival <= departure) arrival += 24 * 60 * 60 * 1000;
  return arrival;
};

const durationSortValue = (trip) => {
  const departure = tripDepartureTimestamp(trip);
  const arrival = tripArrivalTimestamp(trip);
  if (Number.isFinite(departure) && Number.isFinite(arrival) && arrival >= departure) {
    return arrival - departure;
  }
  const normalizedDuration = removeAccents(String(trip?.duration || '')).toLowerCase();
  const hours = Number(normalizedDuration.match(/(\d+)\s*(?:gio|h)/)?.[1] || 0);
  const minutes = Number(normalizedDuration.match(/(\d+)\s*(?:phut|p)/)?.[1] || 0);
  return hours || minutes ? (hours * 60 + minutes) * 60 * 1000 : Number.MAX_SAFE_INTEGER;
};

const filteredTrips = computed(() => {
  const isReturn = bookingStep.value === 'RETURN';
  const from = isReturn ? (route.query.to || '') : (route.query.from || '');
  const to = isReturn ? (route.query.from || '') : (route.query.to || '');
  const date = isReturn ? (route.query.returnDate || '') : (route.query.date || '');
  const company = route.query.company || '';
  
  const cFrom = normalize(from);
  const cTo = normalize(to);

  let results = allTrips.value.filter(t => {
    if (t.isVisible === false) return false;

    const tripIdQuery = route.query.tripId;
    if (tripIdQuery) {
        return t.id.toString() === tripIdQuery.toString();
    }

    if (company && company !== 'all') {
      if (t.companyName !== company) return false;
    }

    const busTypeQuery = route.query.busType || '';
    if (busTypeQuery && busTypeQuery !== 'all') {
      const dbType = t.busType.toLowerCase();
      const queryType = busTypeQuery.toLowerCase();
      if (queryType === 'premium') {
         if (!dbType.includes('premium') && !dbType.includes('prenium')) return false;
      } else {
         if (!dbType.includes(queryType)) return false;
      }
    }

    if (date && t.departureDate) {
      if (t.departureDate.split('T')[0] !== date) return false;
    }
    
    const tFrom = normalize(t.departurePoint);
    const tTo = normalize(t.arrivalPoint);
    const matchLocation = (tFrom.includes(cFrom) || cFrom.includes(tFrom)) && (tTo.includes(cTo) || cTo.includes(tTo));
    if (!matchLocation) return false;

    if (selectedTimeSlots.value.length > 0) {
      const hour = parseInt(t.departureTime.split(':')[0]);
      let slot = '';
      if (hour >= 0 && hour < 6) slot = 'early';
      else if (hour >= 6 && hour < 12) slot = 'morning';
      else if (hour >= 12 && hour < 18) slot = 'afternoon';
      else slot = 'evening';
      
      if (!selectedTimeSlots.value.includes(slot)) return false;
    }

    if (Number(t.price || 0) > priceMax.value) return false;

    const normalizedBusType = normalize(t.busType).replace(/luxyry/g, 'luxury');
    const busTypeInfo = allBusTypes.value.find((item) => normalize(item.name).replace(/luxyry/g, 'luxury') === normalizedBusType);
    if (selectedBusTypes.value.length > 0) {
      const matchesBusType = selectedBusTypes.value.some((type) => normalize(type).replace(/luxyry/g, 'luxury') === normalizedBusType);
      if (!matchesBusType) return false;
    }

    if (selectedSeatClasses.value.length > 0) {
      const matchesSeatClass = selectedSeatClasses.value.includes(String(busTypeInfo?.seatCount || ''));
      if (!matchesSeatClass) return false;
    }

    if (selectedUtilities.value.length > 0) {
      const databaseUtilities = String(busTypeInfo?.description || '')
        .split(',')
        .map((utility) => normalize(utility.trim()))
        .filter(Boolean);
      const hasAllUtilities = selectedUtilities.value.every((utility) => databaseUtilities.includes(normalize(utility)));
      if (!hasAllUtilities) return false;
    }

    return true;
  });

  if (currentSort.value === 'price_asc') results.sort((a, b) => a.price - b.price);
  else if (currentSort.value === 'price_desc') results.sort((a, b) => b.price - a.price);
  else if (currentSort.value === 'time_asc') results.sort((a, b) => departureSortValue(a).localeCompare(departureSortValue(b)));
  else if (currentSort.value === 'time_desc') results.sort((a, b) => departureSortValue(b).localeCompare(departureSortValue(a)));
  else if (currentSort.value === 'duration_asc') results.sort((a, b) => durationSortValue(a) - durationSortValue(b));
  else if (currentSort.value === 'rating_desc') results.sort((a, b) => (companyStats.value[b.companyName + '|' + b.busType]?.averageRating || 0) - (companyStats.value[a.companyName + '|' + a.busType]?.averageRating || 0));

  return results;
});

const fetchTrips = async () => {
  loading.value = true;
  try {
    const isReturn = bookingStep.value === 'RETURN';
    const from = isReturn ? route.query.to : route.query.from;
    const to = isReturn ? route.query.from : route.query.to;
    const date = isReturn ? route.query.returnDate : route.query.date;

    const res = await api.get('/trips/search', {
      params: { from, to, date }
    });
    
    // Ngày về là tùy chọn. Nếu để trống, vẫn chỉ lấy các chuyến ngược chiều
    // khởi hành sau lúc chuyến đi đến tối thiểu 1 giờ.
    let fetchedTrips = res.data.map(t => ({ ...t, showInfo: false }));

    if (isReturn && outboundTrip.value) {
      const earliestReturn = tripArrivalTimestamp(outboundTrip.value) + 60 * 60 * 1000;
      fetchedTrips = fetchedTrips.filter(t => {
        const departure = tripDepartureTimestamp(t);
        return Number.isFinite(departure) && departure >= earliestReturn;
      });
    }

    allTrips.value = fetchedTrips;
    // Cập nhật priceMax theo dữ liệu thực tế (chỉ khi người dùng chưa kéo bộ lọc)
    priceMax.value = priceMaxDefault.value;
    comparedTrips.value = comparedTrips.value.filter((item) => fetchedTrips.some((trip) => String(trip.id) === String(item.id)));
  } catch (err) { 
    console.error("Lỗi fetch trips:", err); 
  } finally { 
    loading.value = false; 
  }
};

const fetchAdditionalInfo = async () => {
  try {
    const busesRes = await api.get('/buses').catch(() => ({ data: [] }));
    const busTypesRes = await api.get('/bus-types').catch(() => ({ data: [] }));
    const statsRes = await api.get('/reviews/stats/all').catch(() => ({ data: {} }));
    
    allBuses.value = busesRes.data;
    allBusTypes.value = busTypesRes.data;
    companyStats.value = statsRes.data || {};
  } catch (err) {
    console.error("Lỗi fetch additional info:", err);
  }
};

onMounted(async () => {
  loading.value = true;
  await fetchAllLocations();
  await fetchAdditionalInfo();
  await fetchTrips();
  await refreshReturnTripData();
});

const isMapModalOpen = ref(false);
const selectedTripForMap = ref(null);

const openMapModal = (trip) => {
  selectedTripForMap.value = trip;
  isMapModalOpen.value = true;
};

const openFirstTripMap = () => {
  if (filteredTrips.value.length > 0) openMapModal(filteredTrips.value[0]);
};

const closeMapModal = () => {
  isMapModalOpen.value = false;
  selectedTripForMap.value = null;
};

const isStopsModalOpen = ref(false);
const selectedTripForStops = ref(null);

const openStopsModal = (trip) => {
  selectedTripForStops.value = trip;
  isStopsModalOpen.value = true;
};

const closeStopsModal = () => {
  isStopsModalOpen.value = false;
  selectedTripForStops.value = null;
};

const isReviewsModalOpen = ref(false);
const selectedTripForReviews = ref(null);

const openReviewsModal = (trip) => {
  selectedTripForReviews.value = trip;
  isReviewsModalOpen.value = true;
};

const closeReviewsModal = () => {
  isReviewsModalOpen.value = false;
  selectedTripForReviews.value = null;
};

const clearFilters = () => {
  currentSort.value = 'default';
  selectedTimeSlots.value = [];
  priceMax.value = priceMaxDefault.value;
  selectedBusTypes.value = [];
  selectedSeatClasses.value = [];
  selectedUtilities.value = [];
};
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar { display: none; }
.scrollbar-hide { -ms-overflow-style: none; scrollbar-width: none; }

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}

@keyframes scaleUp {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.animate-scale-up {
  animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}
.compare-bar-enter-active, .compare-bar-leave-active, .toast-enter-active, .toast-leave-active { transition: opacity .22s ease, transform .22s ease; }
.compare-bar-enter-from, .compare-bar-leave-to { opacity: 0; transform: translate(-50%, 12px); }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(12px); }
</style>
