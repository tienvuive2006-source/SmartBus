<template>
  <div class="fixed inset-0 z-[9999] flex items-center justify-center bg-[#082f2c]/70 p-3 backdrop-blur-sm sm:p-6" role="dialog" aria-modal="true" aria-labelledby="compare-title" @click.self="$emit('close')">
    <section class="flex max-h-[92dvh] w-full max-w-6xl flex-col overflow-hidden rounded-[26px] bg-[#f5f8f7] shadow-2xl shadow-emerald-950/30">
      <header class="flex items-start justify-between gap-5 bg-[#075955] px-5 py-5 text-white sm:px-7">
        <div>
          <p class="text-[11px] font-bold tracking-wide text-emerald-200">ĐÃ CHỌN {{ trips.length }} CHUYẾN</p>
          <h2 id="compare-title" class="mt-1 text-2xl font-black tracking-tight">So sánh chuyến xe</h2>
          <p class="mt-1 text-xs text-emerald-100/75">Đối chiếu các thông tin quan trọng trước khi chọn chỗ.</p>
        </div>
        <button type="button" class="grid h-10 w-10 place-items-center rounded-full border border-white/20 bg-white/10 transition hover:bg-white/20" aria-label="Đóng so sánh" @click="$emit('close')">
          <span class="material-symbols-outlined">close</span>
        </button>
      </header>

      <div class="min-h-0 flex-1 overflow-auto p-4 sm:p-6">
        <div class="comparison-grid" :style="{ '--trip-count': trips.length }">
          <div class="comparison-label sticky left-0 z-10 flex items-end pb-4 text-xs font-bold text-slate-400">Thông tin</div>
          <article v-for="trip in trips" :key="`head-${trip.id}`" class="trip-heading">
            <button type="button" class="remove-button" :aria-label="`Bỏ ${trip.companyName} khỏi so sánh`" @click="$emit('remove', trip)">
              <span class="material-symbols-outlined">close</span>
            </button>
            <span class="text-[10px] font-bold text-emerald-700">{{ trip.busType }}</span>
            <h3 class="mt-1 truncate text-lg font-black text-[#073e3b]">{{ trip.companyName }}</h3>
            <p class="mt-2 text-2xl font-black tracking-tight text-[#075955]">{{ money(trip.price) }}</p>
            <button type="button" class="mt-4 w-full rounded-xl bg-[#087269] px-4 py-2.5 text-xs font-extrabold text-white transition hover:-translate-y-0.5 hover:bg-[#055d56]" @click="$emit('select', trip)">Chọn chuyến này</button>
          </article>

          <template v-for="row in rows" :key="row.key">
            <div class="comparison-label sticky left-0 z-10">
              <span class="material-symbols-outlined">{{ row.icon }}</span>{{ row.label }}
            </div>
            <div v-for="trip in trips" :key="`${row.key}-${trip.id}`" class="comparison-value" :class="{ best: isBest(row, trip) }">
              <span>{{ row.value(trip) }}</span>
              <small v-if="isBest(row, trip)">Tốt nhất</small>
            </div>
          </template>
        </div>
      </div>

      <footer class="flex flex-col gap-2 border-t border-slate-200 bg-white px-5 py-3 text-[11px] text-slate-500 sm:flex-row sm:items-center sm:justify-between sm:px-7">
        <span>Giá vé và số chỗ có thể thay đổi đến khi hoàn tất đặt vé.</span>
        <button type="button" class="font-bold text-[#075955] hover:underline" @click="$emit('clear')">Xóa danh sách so sánh</button>
      </footer>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  trips: { type: Array, required: true },
  companyStats: { type: Object, default: () => ({}) }
});

defineEmits(['close', 'remove', 'clear', 'select']);

const money = (value) => `${Number(value || 0).toLocaleString('vi-VN')}đ`;
const statsFor = (trip) => props.companyStats[`${trip.companyName}|${trip.busType}`] || {};
const ratingFor = (trip) => Number(statsFor(trip).averageRating || 0);
const pointsFor = (trip) => Math.floor(Number(trip.price || 0) / 5000);

const rows = computed(() => [
  { key: 'schedule', label: 'Khởi hành', icon: 'schedule', value: (trip) => `${trip.departureTime} · ${formatDate(trip.departureDate)}` },
  { key: 'arrival', label: 'Đến nơi', icon: 'flag', value: (trip) => `${trip.arrivalTime || 'Đang cập nhật'} · ${formatDate(trip.arrivalDate || trip.departureDate)}` },
  { key: 'duration', label: 'Thời gian', icon: 'timelapse', value: (trip) => trip.duration || 'Đang cập nhật' },
  { key: 'busType', label: 'Loại xe', icon: 'directions_bus', value: (trip) => trip.busType || 'Xe tiêu chuẩn' },
  { key: 'seats', label: 'Chỗ trống', icon: 'airline_seat_recline_extra', value: (trip) => `${trip.availableSeats || 0} chỗ`, metric: (trip) => Number(trip.availableSeats || 0), best: 'max' },
  { key: 'rating', label: 'Đánh giá', icon: 'star', value: (trip) => ratingFor(trip) ? `${ratingFor(trip).toFixed(1)} / 5 · ${statsFor(trip).totalReviews || 0} đánh giá` : 'Chưa có đánh giá', metric: ratingFor, best: 'max' },
  { key: 'points', label: 'Điểm nhận', icon: 'stars', value: (trip) => `${pointsFor(trip)} điểm`, metric: pointsFor, best: 'max' },
  { key: 'price', label: 'Giá vé', icon: 'payments', value: (trip) => money(trip.price), metric: (trip) => Number(trip.price || 0), best: 'min' }
]);

const formatDate = (value) => {
  if (!value) return '';
  const [year, month, day] = String(value).split('T')[0].split('-');
  return `${day}/${month}/${year}`;
};

const isBest = (row, trip) => {
  if (!row.metric || props.trips.length < 2) return false;
  const values = props.trips.map(row.metric);
  const target = row.best === 'min' ? Math.min(...values) : Math.max(...values);
  return row.metric(trip) === target && values.filter((value) => value === target).length < values.length;
};
</script>

<style scoped>
.comparison-grid { display: grid; min-width: calc(165px + var(--trip-count) * 245px); grid-template-columns: 165px repeat(var(--trip-count), minmax(220px, 1fr)); }
.trip-heading { position: relative; margin: 0 .35rem .7rem; padding: 1.1rem; border: 1px solid #dce7e5; border-radius: 18px; background: #fff; box-shadow: 0 8px 22px rgba(7,89,85,.06); }
.remove-button { position: absolute; right: .7rem; top: .7rem; display: grid; width: 1.8rem; height: 1.8rem; place-items: center; border-radius: 50%; color: #94a3a0; transition: .2s ease; }
.remove-button:hover { color: #be3e3e; background: #fff0f0; }
.remove-button span { font-size: 17px; }
.comparison-label, .comparison-value { min-height: 58px; border-bottom: 1px solid #e3ebe9; }
.comparison-label { display: flex; align-items: center; gap: .5rem; padding: .85rem .75rem; color: #60716e; background: #f5f8f7; font-size: 11px; font-weight: 800; }
.comparison-label span { color: #087269; font-size: 18px; }
.comparison-value { display: flex; align-items: center; justify-content: space-between; gap: .5rem; margin: 0 .35rem; padding: .85rem .75rem; color: #2d403d; background: rgba(255,255,255,.68); font-size: 12px; font-weight: 650; }
.comparison-value.best { color: #06705c; background: #ebf8f3; }
.comparison-value small { padding: .2rem .35rem; border-radius: 5px; color: #06705c; background: #d5f1e5; font-size: 8px; font-weight: 800; text-transform: uppercase; white-space: nowrap; }
@media (max-width: 639px) {
  .comparison-grid { min-width: calc(115px + var(--trip-count) * 215px); grid-template-columns: 115px repeat(var(--trip-count), minmax(195px, 1fr)); }
  .comparison-label { padding-left: .4rem; }
}
</style>
