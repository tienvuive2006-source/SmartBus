<template>
  <div class="min-h-screen bg-zinc-50 font-sans text-zinc-900 pb-20">
    <BookingHeader 
      :available-locations="availableLocations" 
      :format-date-display="formatDateDisplay"
      @location-change="handleLocationChange"
      @date-change="handleDateChange"
      @swap-locations="handleSwapLocations"
    />

    <main class="max-w-7xl mx-auto px-4 py-10 grid grid-cols-1 lg:grid-cols-4 gap-8">
      
      <BookingSidebar 
        v-model:modelValueSort="currentSort"
        v-model:modelValueTime="selectedTimeSlots"
        :sort-options="sortOptions"
        @clear-filters="clearFilters"
      />

      <section class="lg:col-span-3 space-y-6">
        
        <!-- ROUND-TRIP WIZARD PROGRESS BAR -->
        <div v-if="isRoundTrip" class="bg-white rounded-2xl border border-emerald-100 shadow-sm p-4 flex items-center justify-between relative overflow-hidden mb-6">
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

        <div class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-4">
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
          <BookingTripCard
            v-for="trip in filteredTrips" 
            :key="trip.id" 
            :trip="trip"
            :company-stats="companyStats"
            :all-buses="allBuses"
            :all-bus-types="allBusTypes"
            @open-map="openMapModal"
            @open-reviews="openReviewsModal"
            @select-trip="handleSelectTrip"
          />
          
          <div v-if="filteredTrips.length === 0" class="bg-white py-24 px-8 rounded-[2rem] border border-zinc-100 text-center shadow-[0_8px_30px_rgb(0,0,0,0.04)] flex flex-col items-center">
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
const availableLocations = ref([]);

// Wizard State
const isRoundTrip = computed(() => !!route.query.returnDate);
const bookingStep = ref('OUTBOUND'); // 'OUTBOUND' | 'RETURN'
const outboundTrip = ref(null);

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

const handleSelectTrip = (trip) => {
  if (isRoundTrip.value) {
    if (bookingStep.value === 'OUTBOUND') {
      outboundTrip.value = trip;
      setBookingStep('RETURN');
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else { // RETURN
      if (!outboundTrip.value) {
        alert('Vui lòng chọn chuyến đi trước khi chọn chuyến về!');
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
  return fullName.replace(/^(Bến xe Liên tỉnh|Bến xe Trung tâm|Bến xe|Trạm|Văn phòng|VP)\s+/i, '').trim();
};

const fetchAllLocations = async () => {
  try {
    const res = await api.get('/trips');
    const locationsSet = new Set();
    res.data.forEach(trip => {
      if (trip.departurePoint) locationsSet.add(extractCityName(trip.departurePoint));
      if (trip.arrivalPoint) locationsSet.add(extractCityName(trip.arrivalPoint));
    });
    availableLocations.value = Array.from(locationsSet).sort();
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
  });
};

const handleLocationChange = (type, val) => {
  const newQuery = { ...route.query };
  if (val) newQuery[type] = val;
  else delete newQuery[type];
  
  router.replace({ query: newQuery }).then(() => {
    resetWizard();
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
  { id: 'time_asc', name: 'Giờ đi sớm nhất' },
  { id: 'time_desc', name: 'Giờ đi muộn nhất' },
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

    return true;
  });

  if (currentSort.value === 'price_asc') results.sort((a, b) => a.price - b.price);
  else if (currentSort.value === 'price_desc') results.sort((a, b) => b.price - a.price);
  else if (currentSort.value === 'time_asc') results.sort((a, b) => a.departureTime.localeCompare(b.departureTime));
  else if (currentSort.value === 'time_desc') results.sort((a, b) => b.departureTime.localeCompare(a.departureTime));
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
    
    // Nếu là chuyến về, lọc thêm điều kiện: giờ khởi hành chuyến về phải SAU chuyến đi (nếu cùng ngày)
    let fetchedTrips = res.data.map(t => ({ ...t, showInfo: false }));
    
    if (isReturn && outboundTrip.value && date === route.query.date) {
      const outTimeParts = outboundTrip.value.arrivalTime ? outboundTrip.value.arrivalTime.split(':') : [0,0];
      const outMinutes = parseInt(outTimeParts[0]) * 60 + parseInt(outTimeParts[1]);
      
      fetchedTrips = fetchedTrips.filter(t => {
         if (!t.departureTime) return false;
         const retTimeParts = t.departureTime.split(':');
         const retMinutes = parseInt(retTimeParts[0]) * 60 + parseInt(retTimeParts[1]);
         return retMinutes > outMinutes + 120; // Phải sau 2 tiếng
      });
    }

    allTrips.value = fetchedTrips;
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
  fetchAllLocations();
  await fetchAdditionalInfo();
  await fetchTrips();
});

const isMapModalOpen = ref(false);
const selectedTripForMap = ref(null);

const openMapModal = (trip) => {
  selectedTripForMap.value = trip;
  isMapModalOpen.value = true;
};

const closeMapModal = () => {
  isMapModalOpen.value = false;
  selectedTripForMap.value = null;
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
</style>