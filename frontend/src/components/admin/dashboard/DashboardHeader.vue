<template>
  <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-gray-100 pb-6 mb-6">
    <div>
      <h1 class="text-2xl md:text-3xl font-extrabold text-gray-900 flex items-center gap-3 tracking-tight">
        Xin chào, Admin! <span class="text-2xl">👋</span>
      </h1>
      <p class="text-sm font-medium text-gray-500 mt-1">
        Tổng quan hoạt động hệ thống hôm nay
      </p>
    </div>
    
    <div class="flex items-center gap-3">
      <!-- Widget Thời Tiết (Hoạt động thật) -->
      <div v-if="weather" class="hidden md:flex items-center gap-2 bg-white border border-gray-200 px-4 py-2.5 rounded-xl shadow-sm text-sm font-semibold text-gray-700">
        <span class="material-symbols-outlined text-[18px]" :class="weather.color">{{ weather.icon }}</span>
        <span>Đà Nẵng: {{ weather.temp }}°C • {{ weather.text }}</span>
      </div>
      <div v-else-if="loadingWeather" class="hidden md:flex items-center gap-2 bg-gray-50 border border-gray-200 px-4 py-2.5 rounded-xl text-sm font-semibold text-gray-400">
        <span class="w-4 h-4 border-2 border-gray-300 border-t-gray-500 rounded-full animate-spin"></span>
        <span>Đang tải thời tiết...</span>
      </div>

      <button 
        @click="$emit('refresh')"
        :disabled="isRefreshing"
        class="bg-white text-gray-700 px-4 py-2.5 rounded-xl text-xs font-bold uppercase tracking-widest flex items-center gap-2 hover:bg-gray-50 transition-all border border-gray-200 shadow-sm disabled:opacity-50"
      >
        <span class="material-symbols-outlined text-[18px]" :class="{ 'animate-spin': isRefreshing }">refresh</span>
        {{ isRefreshing ? 'Đang tải...' : 'Làm mới' }}
      </button>

      <button class="bg-[#075955] text-white px-5 py-2.5 rounded-xl text-xs font-bold uppercase tracking-widest hover:bg-[#05403d] transition-all shadow-md shadow-[#075955]/20 flex items-center gap-2">
        <span class="material-symbols-outlined text-[18px]">download</span>
        Xuất báo cáo
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  lastUpdated: {
    type: String,
    default: '--:--'
  },
  isRefreshing: {
    type: Boolean,
    default: false
  }
});
defineEmits(['refresh']);

import { ref, onMounted } from 'vue';

const weather = ref(null);
const loadingWeather = ref(true);

const getWeatherDetails = (code) => {
  if (code === 0) return { text: 'Nắng đẹp', icon: 'light_mode', color: 'text-amber-500' };
  if (code === 1 || code === 2 || code === 3) return { text: 'Có mây', icon: 'partly_cloudy_day', color: 'text-sky-500' };
  if (code >= 45 && code <= 48) return { text: 'Sương mù', icon: 'foggy', color: 'text-gray-400' };
  if (code >= 51 && code <= 67) return { text: 'Mưa rải rác', icon: 'rainy', color: 'text-blue-400' };
  if (code >= 71 && code <= 77) return { text: 'Tuyết rơi', icon: 'ac_unit', color: 'text-cyan-500' };
  if (code >= 80 && code <= 82) return { text: 'Mưa rào', icon: 'rainy', color: 'text-blue-600' };
  if (code >= 95 && code <= 99) return { text: 'Giông bão', icon: 'thunderstorm', color: 'text-indigo-600' };
  return { text: 'Nhiều mây', icon: 'cloud', color: 'text-gray-500' };
};

const fetchWeather = async () => {
  try {
    // Tọa độ Đà Nẵng
    const lat = 16.0678;
    const lon = 108.2208;
    const res = await fetch(`https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,weather_code&timezone=Asia%2FBangkok`);
    const data = await res.json();
    
    if (data && data.current) {
      const details = getWeatherDetails(data.current.weather_code);
      weather.value = {
        temp: Math.round(data.current.temperature_2m),
        text: details.text,
        icon: details.icon,
        color: details.color
      };
    }
  } catch (e) {
    console.error("Lỗi lấy thời tiết:", e);
  } finally {
    loadingWeather.value = false;
  }
};

onMounted(() => {
  fetchWeather();
});
</script>
