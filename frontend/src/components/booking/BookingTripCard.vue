<template>
  <article class="trip-card group relative overflow-hidden rounded-xl border border-slate-200 bg-white" :class="{ 'is-compared': isCompared }">
    <div v-if="isFeatured" class="featured-ribbon">Nổi bật</div>

    <div class="trip-layout grid min-h-[330px] grid-cols-1">
      <section class="relative min-h-[240px] overflow-hidden bg-slate-900 md:min-h-full">
        <img
          :src="computedImageUrl"
          :alt="`Xe ${trip.companyName} ${trip.busType}`"
          class="absolute inset-0 h-full w-full object-cover transition duration-700 group-hover:scale-[1.035]"
          @error="handleImageError"
        />
        <div class="absolute inset-0 bg-gradient-to-t from-black/75 via-black/5 to-black/5"></div>

        <span class="absolute bottom-4 right-4 flex items-center gap-1.5 rounded-md bg-black/35 px-2.5 py-2 text-[11px] font-semibold text-white backdrop-blur-md">
          <span class="material-symbols-outlined text-[16px]">meeting_room</span>{{ seatLabel }}
        </span>
      </section>

      <section class="flex min-w-0 flex-col px-5 pb-0 pt-4 sm:px-6">
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <span class="status-badge status-confirmed">Xác nhận tức thì</span>
          <span class="status-badge status-sale">Ưu đãi -10%</span>
        </div>

        <div class="flex items-start justify-between gap-4">
          <div class="min-w-0">
            <h3 class="flex items-center gap-1.5 text-[19px] font-extrabold tracking-tight text-[#073e3b]">
              {{ trip.companyName }}
              <span class="material-symbols-outlined filled text-[17px] text-[#075955]" title="Nhà xe đã xác minh">verified</span>
            </h3>
            <p class="mt-0.5 truncate text-xs font-medium text-slate-500">{{ cleanBusType }}</p>
          </div>
        </div>

        <div class="mt-4 grid grid-cols-[auto_minmax(110px,1fr)_auto] items-start gap-3">
          <div>
            <strong class="time-value">{{ trip.departureTime }}</strong>
            <span class="date-value">{{ formatDate(trip.departureDate) }}</span>
            <span class="weekday-value">{{ formatWeekday(trip.departureDate) }}</span>
          </div>

          <div class="pt-1 text-center">
            <p class="text-[11px] font-bold text-slate-600">{{ trip.duration || 'Đang cập nhật' }}</p>
            <p class="text-[10px] text-slate-400">{{ trip.distance ? `(${trip.distance})` : '' }}</p>
            <div class="relative mt-3 h-px border-t border-dashed border-slate-300">
              <span class="material-symbols-outlined absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 bg-white px-2 text-[18px] text-[#075955]">directions_bus</span>
            </div>
          </div>

          <div class="text-right">
            <strong class="time-value">{{ trip.arrivalTime }}</strong>
            <span class="date-value">{{ formatDate(arrivalDate) }}</span>
            <span class="weekday-value">{{ formatWeekday(arrivalDate) }}</span>
          </div>
        </div>

        <div class="mt-5 grid grid-cols-1 gap-3 sm:grid-cols-2">
          <div class="location-block">
            <span class="material-symbols-outlined">location_on</span>
            <div class="min-w-0">
              <b>{{ trip.departurePoint }}</b>
              <p>{{ trip.departureAddress || 'Điểm đón theo lịch trình của nhà xe' }}</p>
            </div>
          </div>
          <div class="location-block">
            <span class="material-symbols-outlined">location_on</span>
            <div class="min-w-0">
              <b>{{ trip.arrivalPoint }}</b>
              <p>{{ trip.arrivalAddress || 'Điểm trả theo lịch trình của nhà xe' }}</p>
            </div>
          </div>
        </div>

        <div class="mt-4 flex flex-wrap items-center gap-x-4 gap-y-2 rounded-lg border border-slate-100 bg-[#f7faf9] px-3 py-2 text-[11px] font-medium text-slate-600">
          <span v-for="utility in visibleUtilities" :key="utility.name" class="flex items-center gap-1.5">
            <span class="material-symbols-outlined text-[16px] text-[#075955]">{{ utility.icon }}</span>{{ utility.name }}
          </span>
          <button v-if="utilityItems.length > 5" type="button" class="ml-auto font-bold text-[#075955]">Xem thêm ({{ utilityItems.length - 5 }})</button>
        </div>

        <nav class="mt-auto flex min-w-0 items-center gap-6 overflow-x-auto border-t border-slate-100 pt-3 text-[11px] font-semibold text-slate-500 scrollbar-hide" aria-label="Chi tiết chuyến">
          <button type="button" :class="{ active: activeTab === 'info' }" @click="toggleInfo">Thông tin</button>
          <button type="button" @click="$emit('open-map', trip)">Lộ trình</button>
          <button type="button" @click="$emit('open-stops', trip)">Điểm dừng</button>
          <button type="button" :class="{ active: activeTab === 'policy' }" @click="activeTab = activeTab === 'policy' ? '' : 'policy'">Chính sách</button>
          <button type="button" @click="$emit('open-reviews', trip)">Đánh giá ({{ reviewCount }})</button>
        </nav>
      </section>

      <aside class="flex flex-col border-t border-slate-100 bg-[#fbfcfc] p-5 md:col-start-2 xl:col-start-auto xl:border-l xl:border-t-0">
        <div class="flex items-center gap-2 text-sm">
          <span
            class="material-symbols-outlined text-[19px]"
            :class="hasReviews ? 'filled text-[#e0a91f]' : 'text-slate-300'"
          >star</span>
          <template v-if="hasReviews">
            <b class="text-[#bd8610]">{{ rating }}</b>
            <span class="text-[11px] text-slate-500">{{ reviewCount }} đánh giá</span>
          </template>
          <span v-else class="text-[11px] font-semibold text-slate-500">Chưa có đánh giá</span>
        </div>

        <div class="mt-7 xl:text-right">
          <p class="text-[26px] font-extrabold tracking-tight text-[#075955]">{{ formatCurrency(trip.price) }}</p>
          <div class="mt-1 flex items-center gap-2 xl:justify-end">
            <span class="text-sm text-slate-400 line-through">{{ formatCurrency(Math.round(trip.price * 1.1 / 1000) * 1000) }}</span>
            <span class="rounded border border-red-200 bg-red-50 px-1.5 py-0.5 text-[11px] font-bold text-red-500">-10%</span>
          </div>
        </div>

        <p class="mt-5 flex items-center gap-2 text-[13px] font-semibold text-emerald-600 xl:justify-end">
          <span class="material-symbols-outlined text-[19px]">airline_seat_recline_extra</span>Còn {{ trip.availableSeats }} chỗ trống
        </p>

        <button
          type="button"
          class="mt-5 w-full rounded-lg bg-[#006057] px-4 py-3 text-sm font-bold text-white shadow-[0_6px_14px_rgba(0,96,87,0.18)] transition hover:-translate-y-0.5 hover:bg-[#004d47] active:translate-y-px disabled:cursor-not-allowed disabled:bg-slate-300"
          :disabled="isTripPassed(trip)"
          @click="!isTripPassed(trip) && $emit('select-trip', trip)"
        >{{ isTripPassed(trip) ? 'Đã khởi hành' : 'Chọn chỗ' }}</button>

        <label class="mt-4 flex cursor-pointer items-center gap-2 border-t border-slate-100 pt-4 text-[11px] font-semibold text-slate-500">
          <input :checked="isCompared" type="checkbox" class="h-4 w-4 rounded accent-[#075955]" @change="$emit('toggle-compare', trip)" />So sánh
        </label>
      </aside>
    </div>

    <div v-if="activeTab" class="border-t border-slate-100 bg-[#f8faf9] px-6 py-5 text-sm text-slate-600">
      <div v-if="activeTab === 'info'" class="grid gap-4 md:grid-cols-2">
        <p><b class="text-slate-800">Phương tiện:</b> {{ cleanBusType }}</p>
        <p><b class="text-slate-800">Biển số:</b> {{ trip.assignedLicensePlate || 'Cập nhật trước giờ khởi hành' }}</p>
        <p><b class="text-slate-800">Tài xế:</b> {{ trip.assignedDriverFullName || 'Nhà xe đang phân công' }}</p>
        <p><b class="text-slate-800">Thanh toán:</b> Có thể thanh toán trực tuyến hoặc tại quầy</p>
      </div>
      <div v-else>
        <b class="text-slate-800">Chính sách đổi, hủy vé:</b>
        <span class="ml-1">Vui lòng liên hệ nhà xe trước giờ khởi hành. Mức hoàn tiền phụ thuộc thời điểm yêu cầu và chính sách của chuyến.</span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed, ref } from 'vue';

const props = defineProps({
  trip: { type: Object, required: true },
  companyStats: { type: Object, required: true },
  allBuses: { type: Array, required: true },
  allBusTypes: { type: Array, required: true },
  isCompared: { type: Boolean, default: false }
});

defineEmits(['open-map', 'open-stops', 'open-reviews', 'select-trip', 'toggle-compare']);

const activeTab = ref('');

const cleanBusType = computed(() => props.trip.busType?.replace(/Luxyry/g, 'Luxury') || 'Xe tiêu chuẩn');
const stats = computed(() => props.companyStats[`${props.trip.companyName}|${props.trip.busType}`] || {});
const reviewCount = computed(() => Number(stats.value.totalReviews || 0));
const hasReviews = computed(() => reviewCount.value > 0 && Number.isFinite(Number(stats.value.averageRating)));
const rating = computed(() => hasReviews.value ? Number(stats.value.averageRating).toFixed(1) : null);
const isFeatured = computed(() => hasReviews.value && Number(rating.value) >= 4.5);
const seatLabel = computed(() => {
  const match = cleanBusType.value.match(/\d+\s*(phòng|chỗ|ghế|giường)?/i);
  return match ? match[0] : `${props.trip.availableSeats || 0} chỗ`;
});

const computedImageUrl = computed(() => {
  const bus = props.allBuses.find((item) => item.licensePlate === props.trip.assignedLicensePlate);
  const typeName = cleanBusType.value.toLowerCase().trim();
  const busType = props.allBusTypes.find((item) => item.name?.replace(/Luxyry/g, 'Luxury').toLowerCase().trim() === typeName);
  return busType?.imageUrl || bus?.imageUrl || props.trip.imageUrl || fallbackImage;
});

const fallbackImage = 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&q=85&w=900';
const handleImageError = (event) => { event.target.src = fallbackImage; };

const utilityIcon = (name) => {
  const value = name.toLowerCase();
  if (value.includes('wifi') || value.includes('wi-fi')) return 'wifi';
  if (value.includes('usb') || value.includes('sạc')) return 'usb';
  if (value.includes('nước')) return 'water_bottle';
  if (value.includes('toilet') || value.includes('vệ sinh')) return 'wc';
  if (value.includes('chăn') || value.includes('giường')) return 'bed';
  if (value.includes('tivi') || value.includes('màn hình')) return 'tv';
  return 'check_circle';
};

const utilityItems = computed(() => {
  const busType = props.allBusTypes.find((item) => item.name?.replace(/Luxyry/g, 'Luxury').toLowerCase().trim() === cleanBusType.value.toLowerCase().trim());
  const values = busType?.description?.split(',').map((item) => item.trim()).filter(Boolean) || ['Wi-Fi', 'USB', 'Nước uống', 'Chăn đắp', 'Toilet'];
  return values.map((name) => ({ name, icon: utilityIcon(name) }));
});
const visibleUtilities = computed(() => utilityItems.value.slice(0, 5));

const formatDate = (value) => {
  if (!value) return '';
  const [year, month, day] = value.split('T')[0].split('-');
  return `${day}/${month}/${year}`;
};
const formatWeekday = (value) => {
  if (!value) return '';
  return new Intl.DateTimeFormat('vi-VN', { weekday: 'long' }).format(new Date(`${value.split('T')[0]}T00:00:00`));
};
const arrivalDate = computed(() => props.trip.arrivalDate || props.trip.departureDate);
const formatCurrency = (value) => `${Number(value || 0).toLocaleString('vi-VN')}đ`;

const isTripPassed = (trip) => {
  if (!trip?.departureDate || !trip?.departureTime) return false;
  const date = trip.departureDate.split('T')[0];
  return new Date(`${date}T${trip.departureTime}:00`) < new Date();
};

const toggleInfo = () => { activeTab.value = activeTab.value === 'info' ? '' : 'info'; };
</script>

<style scoped>
.trip-card { box-shadow: 0 8px 24px rgba(17, 68, 63, .055); transition: border-color .25s ease, box-shadow .25s ease, transform .25s ease; }
.trip-card:hover { border-color: rgba(213, 165, 39, .7); box-shadow: 0 16px 38px rgba(17, 68, 63, .1); transform: translateY(-1px); }
.trip-card.is-compared { border-color: #087269; box-shadow: 0 0 0 2px rgba(8,114,105,.11), 0 16px 38px rgba(17,68,63,.1); }
.featured-ribbon { position: absolute; left: 0; top: 0; z-index: 20; min-width: 95px; padding: .65rem 1rem; border-radius: 0 0 14px 0; color: #765000; background: #edc75b; font-size: 12px; font-weight: 800; letter-spacing: .04em; text-align: center; text-transform: uppercase; }
.filled { font-variation-settings: 'FILL' 1; }
.status-badge { border-radius: 5px; padding: .25rem .55rem; font-size: 10px; font-weight: 800; letter-spacing: .04em; text-transform: uppercase; }
.status-confirmed { border: 1px solid #9dd8ba; color: #0b8b50; background: #effbf4; }
.status-sale { border: 1px solid #efd18a; color: #b07700; background: #fff9e9; }
.time-value { display: block; color: #142422; font-size: 22px; font-variant-numeric: tabular-nums; letter-spacing: -.04em; line-height: 1; }
.date-value { display: block; margin-top: .4rem; color: #52605e; font-size: 11px; font-weight: 600; }
.weekday-value { display: block; color: #899390; font-size: 10px; text-transform: capitalize; }
.location-block { display: flex; min-width: 0; gap: .5rem; }
.location-block > span { color: #075955; font-size: 17px; flex: 0 0 auto; }
.location-block b { display: block; overflow: hidden; color: #33413f; font-size: 11px; line-height: 1.3; text-overflow: ellipsis; white-space: nowrap; }
.location-block p { display: -webkit-box; overflow: hidden; margin-top: .2rem; color: #8b9492; font-size: 9.5px; line-height: 1.35; -webkit-box-orient: vertical; -webkit-line-clamp: 1; }
nav button { flex: 0 0 auto; border-bottom: 2px solid transparent; padding-bottom: .65rem; transition: color .2s ease, border-color .2s ease; }
nav button:hover, nav button.active { border-color: #075955; color: #075955; }
.scrollbar-hide::-webkit-scrollbar { display: none; }
.scrollbar-hide { scrollbar-width: none; }
@media (min-width: 768px) {
  .trip-layout { grid-template-columns: 300px minmax(0, 1fr); }
  .trip-layout > section:first-child { min-height: 100%; }
  .trip-layout > aside { grid-column: 2; }
}
@media (min-width: 1280px) {
  .trip-layout { grid-template-columns: 330px minmax(0, 1fr) 230px; }
  .trip-layout > aside { grid-column: auto; }
}
@media (min-width: 768px) and (max-width: 1279px) {
  .trip-card aside { grid-column: 2; flex-direction: row; flex-wrap: wrap; align-items: center; gap: .75rem 1rem; }
  .trip-card aside > div, .trip-card aside > p { margin-top: 0; }
  .trip-card aside > button { width: auto; margin-top: 0; }
  .trip-card aside > label { margin-left: auto; padding-top: 0; }
}
</style>
