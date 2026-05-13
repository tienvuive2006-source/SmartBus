<template>
  <div>
    <!-- Custom Header for Search Results -->
    <header class="bg-surface shadow-sm sticky top-0 z-40 -mx-container-margin px-container-margin -mt-stack-space pt-stack-space">
      <div class="flex items-center justify-between h-touch-target-min w-full max-w-7xl mx-auto">
        <button @click="$router.back()" class="text-on-surface-variant hover:bg-surface-variant transition-colors duration-200 p-2 rounded-full">
          <span class="material-symbols-outlined">arrow_back</span>
        </button>
        <div class="text-center">
          <h1 class="text-headline-sm font-headline-sm text-on-surface">Hà Nội <span class="material-symbols-outlined text-sm align-middle mx-1">arrow_forward</span> Sapa</h1>
          <p class="text-label-md font-label-md text-on-surface-variant">Thứ 6, 25 Tháng 10 • 2 Hành khách</p>
        </div>
        <button class="text-on-surface-variant hover:bg-surface-variant transition-colors duration-200 p-2 rounded-full">
          <span class="material-symbols-outlined">edit</span>
        </button>
      </div>

      <!-- Filter Bar -->
      <div class="flex overflow-x-auto gap-2 py-3 border-t border-outline-variant/30 scrollbar-hide">
        <button class="flex items-center gap-1 px-4 py-1.5 bg-surface-container rounded-full text-body-md font-body-md text-on-surface whitespace-nowrap hover:bg-surface-container-high transition-colors">
          <span class="material-symbols-outlined text-[18px]">sort</span> Sắp xếp
        </button>
        <button class="flex items-center gap-1 px-4 py-1.5 bg-surface-container rounded-full text-body-md font-body-md text-on-surface whitespace-nowrap hover:bg-surface-container-high transition-colors">
          Giá <span class="material-symbols-outlined text-[18px]">keyboard_arrow_down</span>
        </button>
        <button class="flex items-center gap-1 px-4 py-1.5 bg-surface-container rounded-full text-body-md font-body-md text-on-surface whitespace-nowrap hover:bg-surface-container-high transition-colors">
          Giờ chạy <span class="material-symbols-outlined text-[18px]">keyboard_arrow_down</span>
        </button>
        <button class="flex items-center gap-1 px-4 py-1.5 bg-surface-container rounded-full text-body-md font-body-md text-on-surface whitespace-nowrap hover:bg-surface-container-high transition-colors">
          Nhà xe <span class="material-symbols-outlined text-[18px]">keyboard_arrow_down</span>
        </button>
      </div>
    </header>

    <div class="py-4 space-y-4">
      <!-- Loading State -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-12 space-y-4">
        <div class="animate-spin rounded-full h-12 w-12 border-4 border-primary border-t-transparent"></div>
        <p class="text-body-md font-body-md text-on-surface-variant animate-pulse">Đang tải chuyến xe...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-error/10 text-error border border-error/20 rounded-xl p-6 text-center">
        <span class="material-symbols-outlined text-4xl mb-2">error</span>
        <p class="text-body-md font-body-md font-medium">{{ error }}</p>
        <button @click="fetchTrips" class="mt-4 px-4 py-2 bg-error text-white rounded-lg text-label-md font-bold">Thử lại</button>
      </div>

      <!-- Empty State -->
      <div v-else-if="trips.length === 0" class="text-center py-12 text-on-surface-variant bg-surface-container-low rounded-xl border border-dashed border-outline-variant">
        <span class="material-symbols-outlined text-5xl mb-2 text-outline">directions_bus_filled</span>
        <p class="text-body-lg font-body-lg font-semibold">Không tìm thấy chuyến xe nào</p>
        <p class="text-body-md font-body-md opacity-80">Hãy thử điều chỉnh điểm xuất phát hoặc điểm đến của bạn.</p>
      </div>

      <!-- Content Loaded -->
      <div v-else class="space-y-4">
        <p class="text-body-md font-body-md text-on-surface-variant mb-2">Tìm thấy {{ trips.length }} chuyến xe thực tế</p>

        <!-- Dynamic Bus Cards -->
        <article v-for="trip in trips" :key="trip.id" class="bg-surface-container-lowest rounded-xl shadow-[0px_4px_12px_rgba(0,0,0,0.05)] border border-outline-variant/20 overflow-hidden transition-transform active:scale-[0.99]">
          <div class="p-4">
            <div class="flex justify-between items-start mb-4">
              <div>
                <h2 class="text-headline-sm font-headline-sm text-on-surface font-bold">{{ trip.companyName }}</h2>
                <p class="text-label-md font-label-md text-on-surface-variant flex items-center gap-1">
                  {{ trip.busType }} 
                  <span v-if="trip.instantConfirmation" class="flex items-center text-tertiary font-bold">
                    <span class="material-symbols-outlined text-[14px]" style="font-variation-settings: 'FILL' 1;">bolt</span> Xác nhận ngay
                  </span>
                </p>
              </div>
              <div class="text-right">
                <span class="text-headline-sm font-headline-sm text-primary font-bold">{{ trip.price.toLocaleString('vi-VN') }}đ</span>
                <div class="flex items-center justify-end text-label-md font-label-md text-on-surface-variant gap-1 mt-1">
                  <span class="material-symbols-outlined text-[14px] text-secondary-container" style="font-variation-settings: 'FILL' 1;">star</span> {{ trip.rating }}
                </div>
              </div>
            </div>
            
            <div class="flex items-stretch gap-4 relative py-2">
              <div class="flex flex-col items-center w-6">
                <div class="w-2.5 h-2.5 rounded-full bg-primary ring-4 ring-primary/20"></div>
                <div class="w-0.5 h-full bg-outline-variant/50 my-1 border-dashed border-l"></div>
                <div class="w-2.5 h-2.5 rounded-full bg-secondary-container ring-4 ring-secondary-container/20"></div>
              </div>
              <div class="flex-1 space-y-6">
                <div class="flex justify-between">
                  <div>
                    <p class="text-headline-sm font-headline-sm text-on-surface font-bold">{{ trip.departureTime }}</p>
                    <p class="text-body-md font-body-md text-on-surface-variant">{{ trip.departurePoint }}</p>
                  </div>
                </div>
                <div class="flex justify-between">
                  <div>
                    <p class="text-headline-sm font-headline-sm text-on-surface font-bold">{{ trip.arrivalTime }}</p>
                    <p class="text-body-md font-body-md text-on-surface-variant">{{ trip.arrivalPoint }}</p>
                  </div>
                  <div class="text-right flex flex-col justify-end">
                    <p class="text-label-md font-label-md text-on-surface-variant bg-surface-container px-2 py-0.5 rounded-full border border-outline-variant/20">{{ trip.duration }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="border-t border-dashed border-outline-variant/30 bg-surface-container-low p-4 flex justify-between items-center">
            <span :class="[
              'inline-flex items-center px-2.5 py-0.5 rounded-full text-label-md font-label-md font-bold',
              trip.availableSeats < 5 ? 'bg-error-container/10 text-error' : 'bg-success/10 text-success'
            ]">
              Còn {{ trip.availableSeats }} chỗ trống
            </span>
            <button @click="$router.push('/booking/seat')" class="bg-secondary-container hover:bg-tertiary-container text-on-secondary-container px-6 py-2 rounded-xl text-body-md font-body-md font-semibold shadow-md transition-all active:scale-95">
              Chọn vé
            </button>
          </div>
        </article>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const trips = ref([]);
const loading = ref(true);
const error = ref(null);

const fetchTrips = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await axios.get('http://localhost:8080/api/trips');
    trips.value = response.data;
  } catch (err) {
    error.value = 'Không thể kết nối API. Hãy chắc chắn Backend Spring Boot đang chạy trên cổng 8080.';
    console.error('API Error:', err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchTrips();
});
</script>
