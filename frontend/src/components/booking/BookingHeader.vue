<template>
  <header class="booking-search-header sticky top-0 z-50">
    <div class="mx-auto max-w-[1600px] px-4 py-4 xl:px-8">
      <div class="mobile-header-row lg:hidden">
        <button type="button" class="mobile-back-button" aria-label="Quay lại trang trước" @click="goBack">
          <span class="material-symbols-outlined">arrow_back</span>
        </button>
        <div>
          <p>Kết quả tìm chuyến</p>
          <span>Chọn hành trình phù hợp với bạn</span>
        </div>
      </div>

      <div class="flex items-center gap-5">
        <button
          type="button"
          class="hidden w-[210px] shrink-0 items-center justify-start lg:flex"
          aria-label="Về trang chủ"
          @click="$router.push('/')"
        >
          <img src="/logo2.png" alt="Trung Nam Limousine" class="h-14 w-auto origin-left scale-[1.5] object-contain" />
        </button>

        <div class="search-panel flex min-w-0 flex-1 items-stretch overflow-hidden rounded-xl bg-white">
          <div class="search-field relative min-w-0 flex-[1.15]">
            <span class="material-symbols-outlined field-icon">location_on</span>
            <div class="min-w-0">
              <span class="field-label">Điểm đón</span>
              <select
                :value="$route.query.from"
                class="field-control"
                aria-label="Điểm đón"
                @change="$emit('location-change', 'from', $event.target.value)"
              >
                <option value="" disabled>Chọn nơi đi</option>
                <option v-for="loc in availableLocations" :key="`from-${loc}`" :value="loc">{{ loc }}</option>
              </select>
            </div>
            <button type="button" class="swap-button" title="Đổi chiều" @click.stop="$emit('swap-locations')">
              <span class="material-symbols-outlined text-[18px]">swap_horiz</span>
            </button>
          </div>

          <div class="search-field min-w-0 flex-[1.15]">
            <span class="material-symbols-outlined field-icon">location_on</span>
            <div class="min-w-0">
              <span class="field-label">Điểm đến</span>
              <select
                :value="$route.query.to"
                class="field-control"
                aria-label="Điểm đến"
                @change="$emit('location-change', 'to', $event.target.value)"
              >
                <option value="" disabled>Chọn nơi đến</option>
                <option v-for="loc in availableLocations" :key="`to-${loc}`" :value="loc">{{ loc }}</option>
              </select>
            </div>
          </div>

          <div class="search-field outbound-date-field min-w-[175px] flex-1 cursor-pointer" role="button" tabindex="0" @click="openDepartureDatePicker" @keydown.enter.prevent="openDepartureDatePicker">
            <span class="material-symbols-outlined field-icon">calendar_month</span>
            <div>
              <span class="field-label">Ngày đi</span>
              <span class="field-control">{{ formatDateDisplay($route.query.date) }}</span>
              <span class="field-subtitle">{{ formatWeekday($route.query.date) }}</span>
            </div>
            <input
              ref="departureDateInput"
              type="date"
              :min="todayDate"
              :value="$route.query.date"
              class="date-native-input"
              aria-label="Ngày đi"
              @change="$emit('date-change', $event)"
            />
          </div>

          <div class="search-field return-date-field min-w-[175px] flex-1" :class="{ 'round-trip-visible': roundTripActive, 'cursor-pointer': !!$route.query.date, 'cursor-not-allowed': !$route.query.date, 'opacity-60': !$route.query.returnDate }" role="button" tabindex="0" @click="openReturnDatePicker($route.query.date)" @keydown.enter.prevent="openReturnDatePicker($route.query.date)">
            <span class="material-symbols-outlined field-icon">event</span>
            <div>
              <span class="field-label">Ngày về (tùy chọn)</span>
              <span class="field-control">{{ $route.query.returnDate ? formatDateDisplay($route.query.returnDate) : 'Chọn ngày về' }}</span>
              <span class="field-subtitle">{{ formatWeekday($route.query.returnDate) }}</span>
            </div>
            <input
              ref="returnDateInput"
              type="date"
              :min="$route.query.date"
              :value="$route.query.returnDate"
              class="date-native-input"
              aria-label="Ngày về"
              @change="$emit('return-date-change', $event)"
            />
          </div>

          <label class="search-field passenger-field min-w-[155px] flex-[0.8]">
            <span class="material-symbols-outlined field-icon">person</span>
            <div>
              <span class="field-label">Hành khách</span>
              <select
                :value="Number($route.query.passengers || 1)"
                class="field-control"
                aria-label="Số hành khách"
                @change="$emit('passenger-change', Number($event.target.value))"
              >
                <option v-for="count in 6" :key="count" :value="count">{{ count }} hành khách</option>
              </select>
            </div>
          </label>

          <button type="button" class="search-submit" @click="$emit('search')">Tìm chuyến</button>
        </div>
      </div>

      <div class="mt-2 flex items-center justify-between pl-0 lg:pl-[230px]">
        <div class="flex min-w-0 items-center gap-3">
          <div class="trip-switch" role="group" aria-label="Loại hành trình">
            <button type="button" :class="{ active: !roundTripActive }" @click="$emit('trip-type-change', 'one-way')">
              <span class="material-symbols-outlined text-[15px]">explore</span>Một chiều
            </button>
            <button type="button" :class="{ active: roundTripActive }" @click="$emit('trip-type-change', 'round-trip')">Khứ hồi</button>
          </div>
          <div v-if="roundTripActive && !$route.query.returnDate" class="return-suggestions">
            <span>Gợi ý ngày về:</span>
            <template v-if="returnDateSuggestions.length">
              <button v-for="date in returnDateSuggestions" :key="date" type="button" @click="$emit('select-return-date', date)">{{ formatSuggestionDate(date) }}</button>
            </template>
            <small v-else>{{ !$route.query.date ? 'Vui lòng chọn ngày đi' : 'Chưa có lịch chiều về phù hợp' }}</small>
          </div>
          <span v-if="$route.query.returnDate" class="return-availability" :class="returnTripCount > 0 ? 'available' : checkingReturnTrips ? 'checking' : 'unavailable'">
            <span class="status-dot"></span>
            {{ checkingReturnTrips ? 'Đang kiểm tra chuyến về...' : returnTripCount > 0 ? `Có ${returnTripCount} chuyến về` : 'Chưa có chuyến về ngày này' }}
          </span>
        </div>

        <div class="hidden items-center gap-5 text-sm font-semibold text-white/90 md:flex">
          <button type="button" class="flex items-center gap-2 transition hover:text-amber-300" @click="openVoucherStore">
            <span class="material-symbols-outlined text-amber-300">crown</span>
            <template v-if="authStore.isLoggedIn">Bạn có <b class="text-amber-300">{{ formattedLoyaltyPoints }} điểm</b></template>
            <template v-else>Đăng nhập để xem điểm</template>
          </button>
          <span class="h-5 w-px bg-white/25"></span>
          <button type="button" class="flex items-center gap-2 transition hover:text-amber-300" @click="openMyVouchers">
            <span class="material-symbols-outlined text-[19px]">sell</span>Ưu đãi của tôi
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { toBusinessDateString } from '@/utils/businessDate';

const router = useRouter();
const authStore = useAuthStore();

defineProps({
  availableLocations: { type: Array, required: true },
  formatDateDisplay: { type: Function, required: true },
  returnTripCount: { type: Number, default: 0 },
  checkingReturnTrips: { type: Boolean, default: false },
  roundTripActive: { type: Boolean, default: false },
  returnDateSuggestions: { type: Array, default: () => [] }
});

defineEmits([
  'location-change',
  'date-change',
  'return-date-change',
  'select-return-date',
  'passenger-change',
  'trip-type-change',
  'swap-locations',
  'search'
]);

const formatWeekday = (value) => {
  if (!value) return '';
  const date = new Date(`${value}T00:00:00`);
  return new Intl.DateTimeFormat('vi-VN', { weekday: 'long' }).format(date);
};

const formatSuggestionDate = (value) => {
  const [, month, day] = String(value).split('-');
  return `${day}/${month}`;
};

const departureDateInput = ref(null);
const returnDateInput = ref(null);
const todayDate = toBusinessDateString();
const formattedLoyaltyPoints = computed(() => Number(authStore.currentUser?.loyaltyPoints || 0).toLocaleString('vi-VN'));

const goToProfile = (query) => {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: router.resolve({ name: 'profile', query }).fullPath } });
    return;
  }
  router.push({ name: 'profile', query });
};

const openVoucherStore = () => goToProfile({ open: 'voucher-store' });
const openMyVouchers = () => goToProfile({ section: 'my-vouchers' });
const goBack = () => {
  if (window.history.state?.back) {
    router.back();
    return;
  }
  router.push('/');
};

onMounted(() => {
  if (authStore.isLoggedIn) authStore.fetchMe();
});

const openDatePicker = (input) => {
  if (!input) return;
  if (typeof input.showPicker === 'function') input.showPicker();
  else input.click();
};

const openDepartureDatePicker = () => openDatePicker(departureDateInput.value);
const openReturnDatePicker = (departureDate) => {
  if (!departureDate) return;
  openDatePicker(returnDateInput.value);
};
</script>

<style scoped>
.booking-search-header {
  background: radial-gradient(circle at 70% -60%, #167568 0, transparent 42%), #003f3b;
  box-shadow: 0 12px 28px rgba(0, 63, 59, 0.14);
}
.search-panel { box-shadow: 0 8px 24px rgba(0, 33, 31, 0.22); }
.search-field {
  position: relative;
  display: flex;
  align-items: center;
  gap: .75rem;
  min-height: 74px;
  padding: .75rem 1rem;
  border-right: 1px solid #e7eceb;
  transition: background-color .2s ease;
}
.search-field:hover { background: #f8fbfa; }
.outbound-date-field,
.return-date-field,
.passenger-field { display: none; }
.field-icon { color: #075955; font-size: 22px; flex: 0 0 auto; }
.field-label { display: block; color: #7c8785; font-size: 11px; font-weight: 600; line-height: 1.2; }
.field-control {
  display: block;
  width: 100%;
  overflow: hidden;
  color: #243331;
  background: transparent;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
  outline: none;
}
.field-subtitle { display: block; color: #87918f; font-size: 10px; text-transform: capitalize; }
.date-native-input { position: absolute; width: 1px; height: 1px; opacity: 0; pointer-events: none; }
.swap-button {
  position: absolute;
  right: 0;
  top: 50%;
  z-index: 10;
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  transform: translate(50%, -50%);
  border: 1px solid #dce5e3;
  border-radius: 999px;
  color: #075955;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 63, 59, .1);
}
.search-submit {
  align-self: stretch;
  min-width: 150px;
  margin: 10px;
  border-radius: 9px;
  color: #fff;
  background: #dcae39;
  font-size: 14px;
  font-weight: 800;
  text-transform: uppercase;
  box-shadow: 0 6px 14px rgba(220, 174, 57, .3);
  transition: transform .2s ease, background .2s ease;
}
.search-submit:hover { transform: translateY(-1px); background: #c99927; }
.search-submit:active { transform: translateY(1px); }
.trip-switch { display: flex; padding: 3px; border: 1px solid rgba(255,255,255,.32); border-radius: 999px; }
.trip-switch button {
  display: flex;
  min-width: 125px;
  align-items: center;
  justify-content: center;
  gap: .4rem;
  padding: .45rem 1rem;
  border-radius: 999px;
  color: rgba(255,255,255,.72);
  font-size: 12px;
  font-weight: 700;
  transition: .2s ease;
}
.trip-switch button.active { color: #fff; background: rgba(255,255,255,.14); box-shadow: inset 0 0 0 1px rgba(255,255,255,.16); }
.return-suggestions { display: flex; min-width: 0; align-items: center; gap: .4rem; color: rgba(255,255,255,.72); font-size: 10px; font-weight: 700; white-space: nowrap; }
.return-suggestions > span { color: #f4d77d; }
.return-suggestions button { padding: .32rem .55rem; border: 1px solid rgba(255,255,255,.3); border-radius: 999px; color: white; background: rgba(255,255,255,.09); font-size: 10px; font-weight: 800; transition: .2s ease; }
.return-suggestions button:hover { border-color: #f4d77d; color: #fbe7a8; background: rgba(244,215,125,.12); }
.return-suggestions small { overflow: hidden; max-width: 180px; color: #ffd2c8; text-overflow: ellipsis; }
.return-availability { display: flex; align-items: center; gap: .45rem; overflow: hidden; max-width: 230px; color: rgba(255,255,255,.8); font-size: 11px; font-weight: 700; text-overflow: ellipsis; white-space: nowrap; }
.return-availability .status-dot { width: 7px; height: 7px; flex: 0 0 auto; border-radius: 999px; background: #f1c453; box-shadow: 0 0 0 3px rgba(241,196,83,.14); }
.return-availability.available { color: #bdf4d8; }
.return-availability.available .status-dot { background: #45d18c; box-shadow: 0 0 0 3px rgba(69,209,140,.14); }
.return-availability.unavailable { color: #ffd2c8; }
.return-availability.unavailable .status-dot { background: #ff876f; box-shadow: 0 0 0 3px rgba(255,135,111,.14); }
@media (max-width: 767px) {
  .booking-search-header > div {
    padding-block: 0.65rem !important;
  }

  .mobile-header-row {
    display: flex;
    min-height: 2.75rem;
    align-items: center;
    gap: 0.7rem;
    margin-bottom: 0.55rem;
    color: white;
  }

  .mobile-back-button {
    display: grid;
    width: 2.75rem;
    height: 2.75rem;
    flex: 0 0 auto;
    place-items: center;
    border: 1px solid rgb(255 255 255 / 0.24);
    border-radius: 0.75rem;
    background: rgb(255 255 255 / 0.12);
    color: white;
    transition: background-color 160ms ease, transform 160ms ease;
  }

  .mobile-back-button:active {
    transform: scale(0.96);
    background: rgb(255 255 255 / 0.2);
  }

  .mobile-back-button .material-symbols-outlined {
    font-size: 1.25rem;
  }

  .mobile-header-row p {
    font-size: 0.82rem;
    font-weight: 800;
    line-height: 1.2;
  }

  .mobile-header-row span:not(.material-symbols-outlined) {
    display: block;
    margin-top: 0.15rem;
    color: rgb(255 255 255 / 0.68);
    font-size: 0.62rem;
    font-weight: 600;
  }

  .search-panel { overflow: visible; }
  .search-field { min-height: 62px; padding: .6rem .75rem; }
  .search-field:nth-child(2) { border-right: 0; }
  .search-submit { min-width: 52px; width: 52px; margin: 7px; font-size: 0; }
  .search-submit::before { content: 'search'; font-family: 'Material Symbols Outlined'; font-size: 22px; }
}
@media (min-width: 1280px) {
  .outbound-date-field { display: flex; }
  .return-date-field.round-trip-visible { display: flex; }
}
@media (min-width: 1536px) {
  .return-date-field,
  .passenger-field { display: flex; }
}
</style>
