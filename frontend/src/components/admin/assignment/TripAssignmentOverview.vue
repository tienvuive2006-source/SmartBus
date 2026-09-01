<template>
  <section class="trip-overview-bar">
    <!-- 1. Ngày khởi hành -->
    <div class="overview-col date-col">
      <div class="icon-box blue-icon">
        <span class="material-symbols-outlined">calendar_month</span>
      </div>
      <div class="col-text">
        <span class="col-label">{{ weekdayLabel || 'Ngày khởi hành' }}</span>
        <strong class="col-val tabular-nums">{{ formatDate(trip?.departureDate) }}</strong>
      </div>
    </div>

    <!-- 2. Giờ khởi hành -->
    <div class="overview-col time-col">
      <div class="icon-box red-icon">
        <span class="material-symbols-outlined">schedule</span>
      </div>
      <div class="col-text">
        <span class="col-label">Giờ khởi hành</span>
        <strong class="col-val tabular-nums">{{ trip?.departureTime || '--:--' }}</strong>
      </div>
    </div>

    <!-- 3. Tuyến xe: Bến đi -> Bến đến -->
    <div class="overview-col route-col">
      <div class="route-box">
        <div class="point-info">
          <span class="col-label">BẾN ĐI</span>
          <strong class="point-name" :title="trip?.departurePoint">{{ shortPoint(trip?.departurePoint) }}</strong>
        </div>

        <div class="arrow-circle">
          <span class="material-symbols-outlined">arrow_forward</span>
        </div>

        <div class="point-info">
          <span class="col-label">BẾN ĐẾN</span>
          <strong class="point-name" :title="trip?.arrivalPoint">{{ shortPoint(trip?.arrivalPoint) }}</strong>
          <span class="code-sub">Mã chuyến: {{ trip?.id ? `#${trip.id}` : 'Chưa cập nhật' }}</span>
        </div>
      </div>
    </div>

    <!-- 4. Thời gian dự kiến & Khoảng cách -->
    <div class="overview-col duration-col">
      <div class="icon-box green-icon">
        <span class="material-symbols-outlined">timer</span>
      </div>
      <div class="col-text">
        <span class="col-label">Thời gian dự kiến</span>
        <strong class="col-val tabular-nums">~ {{ durationLabel }}</strong>
        <span v-if="distanceLabel" class="distance-sub">~ {{ distanceLabel }}</span>
      </div>
    </div>

    <!-- 5. Trạng thái chuyến -->
    <div class="overview-col status-col">
      <div class="col-text text-center">
        <span class="col-label">Trạng thái chuyến</span>
        <div class="status-badge" :class="ready ? 'ready' : 'waiting'">
          {{ ready ? 'Sẵn sàng' : 'Chờ phân công' }}
        </div>
      </div>
    </div>

    <!-- 6. Nút hành động -->
    <div class="overview-col actions-col">
      <button type="button" class="btn-auto-suggest" @click="$emit('suggest')">
        <span class="material-symbols-outlined">auto_awesome</span>
        <span>Gợi ý tự động</span>
      </button>

      <button type="button" class="btn-check-conflict" @click="$emit('check-conflict')">
        <span class="material-symbols-outlined">verified_user</span>
        <span>Kiểm tra xung đột</span>
      </button>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  trip: { type: Object, default: null },
  ready: Boolean
})
defineEmits(['suggest', 'check-conflict'])

const shortPoint = value => {
  if (!value) return 'Chưa xác định'
  const parts = String(value).split(',')
  return parts[0].trim()
}

const formatDate = value => {
  if (!value) return '--/--/----'
  const [year, month, day] = value.split('-')
  return `${day}/${month}/${year}`
}

const weekdayLabel = computed(() => {
  if (!props.trip?.departureDate) return 'Ngày khởi hành'
  try {
    const [year, month, day] = props.trip.departureDate.split('-').map(Number)
    const date = new Date(year, month - 1, day)
    const rawStr = new Intl.DateTimeFormat('vi-VN', { weekday: 'long' }).format(date)
    return rawStr.charAt(0).toUpperCase() + rawStr.slice(1)
  } catch {
    return 'Ngày khởi hành'
  }
})

const distanceLabel = computed(() => {
  const value = props.trip?.distanceKm ?? props.trip?.distance
  if (value === null || value === undefined || value === '') return ''
  return String(value).toLowerCase().includes('km') ? String(value) : `${Number(value).toLocaleString('vi-VN')} km`
})

const toMinutes = value => {
  if (!value) return null
  const [hour, minute] = value.split(':').map(Number)
  return hour * 60 + minute
}

const durationLabel = computed(() => {
  const departure = toMinutes(props.trip?.departureTime)
  let arrival = toMinutes(props.trip?.arrivalTime)
  if (departure === null || arrival === null) return 'Chưa cập nhật'
  if (arrival < departure) arrival += 1440
  const duration = arrival - departure
  const hours = Math.floor(duration / 60)
  const minutes = duration % 60
  return `${hours ? `${hours} giờ` : ''}${hours && minutes ? ' ' : ''}${minutes ? `${minutes} phút` : ''}` || '0 phút'
})
</script>

<style scoped>
.trip-overview-bar {
  display: flex;
  align-items: center;
  width: calc(100% - 1.5rem);
  margin: 0.6rem 0.75rem;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 0.85rem;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
  overflow: hidden;
  min-height: 5rem;
}

.overview-col {
  display: flex;
  align-items: center;
  padding: 0.65rem 0.9rem;
  border-right: 1px solid #e2e8f0;
  height: 100%;
  box-sizing: border-box;
}

.overview-col:last-child {
  border-right: none;
}

/* 1. Date & Time Cols */
.date-col, .time-col {
  gap: 0.65rem;
  min-width: 10.5rem;
  flex: none;
}

/* 2. Route Col */
.route-col {
  flex: 1;
  min-width: 22rem;
  justify-content: center;
}

.route-box {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  width: 100%;
}

.point-info {
  display: flex;
  flex-direction: column;
  flex: 1 1 0;
  min-width: 0;
}

.point-name {
  color: #0f172a;
  font-size: 0.88rem;
  font-weight: 850;
  white-space: normal;
  overflow: visible;
  text-overflow: clip;
  line-height: 1.25;
  overflow-wrap: anywhere;
  margin-top: 0.1rem;
}

.code-sub {
  color: #94a3b8;
  font-size: 0.65rem;
  font-weight: 600;
  margin-top: 0.1rem;
}

.arrow-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 9999px;
  background: #e6f4f1;
  color: #0d9488;
  flex-shrink: 0;
}

.arrow-circle span {
  font-size: 1.1rem;
}

/* 3. Duration Col */
.duration-col {
  gap: 0.65rem;
  min-width: 11.5rem;
  flex: none;
}

.distance-sub {
  color: #64748b;
  font-size: 0.68rem;
  font-weight: 600;
  margin-top: 0.1rem;
  display: block;
}

/* 4. Status Col */
.status-col {
  min-width: 9.5rem;
  flex: none;
  justify-content: center;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.3rem 0.7rem;
  border-radius: 0.45rem;
  font-size: 0.72rem;
  font-weight: 850;
  margin-top: 0.25rem;
  white-space: nowrap;
}

.status-badge.ready {
  background: #dcfce7;
  color: #15803d;
}

.status-badge.waiting {
  background: #fef3c7;
  color: #b45309;
}

/* 5. Actions Col */
.actions-col {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding-right: 1rem;
  flex: none;
  margin-left: auto;
}

.btn-auto-suggest {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 0.85rem;
  border-radius: 0.6rem;
  border: 1px solid #99f6e4;
  background: #f0fdf4;
  color: #0f766e;
  font-size: 0.72rem;
  font-weight: 850;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-auto-suggest:hover {
  background: #0f766e;
  color: #ffffff;
  border-color: #0f766e;
}

.btn-auto-suggest span {
  font-size: 1rem;
}

.btn-check-conflict {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 0.85rem;
  border-radius: 0.6rem;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #334155;
  font-size: 0.72rem;
  font-weight: 750;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-check-conflict:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #0f172a;
}

.btn-check-conflict span {
  font-size: 1rem;
  color: #64748b;
}

/* Icon Box standard styles */
.icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.35rem;
  height: 2.35rem;
  border-radius: 0.65rem;
  flex-shrink: 0;
}

.icon-box span {
  font-size: 1.25rem;
}

.blue-icon {
  background: #eff6ff;
  color: #2563eb;
}

.red-icon {
  background: #fef2f2;
  color: #ef4444;
}

.green-icon {
  background: #f0fdf4;
  color: #16a34a;
}

.col-text {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.col-label {
  color: #64748b;
  font-size: 0.64rem;
  font-weight: 700;
  text-transform: capitalize;
}

.col-val {
  color: #0f172a;
  font-size: 0.88rem;
  font-weight: 900;
  line-height: 1.2;
  margin-top: 0.1rem;
}

@media (max-width: 1280px) {
  .trip-overview-bar {
    overflow-x: auto;
  }
}

@media (max-width: 1450px) {
  .overview-col { padding-inline: 0.65rem; }
  .date-col { min-width: 8.6rem; }
  .time-col { min-width: 7.8rem; }
  .date-col, .time-col, .duration-col { gap: 0.45rem; }
  .route-col { min-width: 29rem; padding-inline: 0.8rem; }
  .route-box { gap: 0.55rem; }
  .duration-col { min-width: 9rem; }
  .status-col { min-width: 7.5rem; }
  .actions-col { width: 18.5rem; gap: 0.35rem; padding-inline: 0.55rem; }
  .btn-auto-suggest, .btn-check-conflict { flex: 1 1 0; justify-content: center; padding-inline: 0.55rem; }
  .icon-box { width: 2rem; height: 2rem; }
  .point-name, .col-val { font-size: 0.8rem; }
}
</style>
