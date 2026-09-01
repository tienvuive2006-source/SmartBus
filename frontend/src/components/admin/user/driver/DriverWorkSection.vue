<template>
  <section class="driver-section">
    <header class="driver-section__header">
      <span class="material-symbols-outlined">work</span>
      <strong>Thông tin công việc</strong>
    </header>

    <div class="driver-fields driver-fields--two">
      <label class="driver-field">
        <span>Trạng thái làm việc</span>
        <select v-model="form.driverStatus">
          <option value="FREE">Sẵn sàng</option>
          <option value="DRIVING">Đang chạy chuyến</option>
          <option value="ON_LEAVE">Đang nghỉ phép</option>
          <option value="SUSPENDED">Tạm đình chỉ</option>
        </select>
      </label>
      <label class="driver-field">
        <span>Ca làm việc</span>
        <select v-model="form.driverShift">
          <option value="">Chưa thiết lập</option>
          <option value="MORNING">Ca sáng (06:00 – 14:00)</option>
          <option value="AFTERNOON">Ca chiều (14:00 – 22:00)</option>
          <option value="NIGHT">Ca đêm (22:00 – 06:00)</option>
          <option value="FLEXIBLE">Linh hoạt theo chuyến</option>
        </select>
      </label>
      <div class="driver-field driver-field--wide">
        <span>Tuyến đường phụ trách</span>
        <div class="driver-route-picker">
          <div v-if="loadingRoutes" class="driver-route-picker__empty">Đang tải danh sách tuyến...</div>
          <div v-else-if="!sortedRoutes.length" class="driver-route-picker__empty">Chưa có tuyến đường nào.</div>
          <div v-for="route in sortedRoutes" v-else :key="route.id" class="driver-route-picker__item">
            <span class="material-symbols-outlined">route</span>
            <p>
              <strong>{{ route.name || `${shortPoint(route.departurePoint)} → ${shortPoint(route.arrivalPoint)}` }}</strong>
              <small>{{ shortPoint(route.departurePoint) }} → {{ shortPoint(route.arrivalPoint) }}</small>
            </p>
            <select :value="roleOf(route.id)" @change="setRole(route.id, $event.target.value)">
              <option value="">Không gán</option>
              <option value="PRIMARY">Tuyến chính</option>
              <option value="BACKUP">Dự phòng</option>
            </select>
          </div>
        </div>
        <small class="driver-route-picker__hint">Tuyến chiều ngược lại sẽ được gán tự động khi lưu.</small>
        <div class="driver-assignment-note">
          <span class="material-symbols-outlined">event_available</span>
          <p><strong>{{ assignmentText }}</strong><small>Cập nhật tại màn hình Phân công để tránh trùng lịch vận hành.</small></p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
  form: { type: Object, required: true },
  routes: { type: Array, default: () => [] },
  loadingRoutes: Boolean
})
const sortedRoutes = computed(() => [...props.routes]
  .sort((a, b) => String(a.name).localeCompare(String(b.name), 'vi')))
const shortPoint = value => String(value || '').split(',')[0].trim()
const roleOf = routeId => {
  if ((props.form.primaryRouteIds || []).includes(routeId)) return 'PRIMARY'
  if ((props.form.backupRouteIds || []).includes(routeId)) return 'BACKUP'
  return ''
}
const setRole = (routeId, role) => {
  props.form.primaryRouteIds = (props.form.primaryRouteIds || []).filter(id => id !== routeId)
  props.form.backupRouteIds = (props.form.backupRouteIds || []).filter(id => id !== routeId)
  if (role === 'PRIMARY') props.form.primaryRouteIds.push(routeId)
  if (role === 'BACKUP') props.form.backupRouteIds.push(routeId)
}
const assignmentText = computed(() => Number(props.form.activeTripCount || 0) > 0
  ? `${props.form.activeTripCount} chuyến đang được phân công`
  : 'Hiện chưa có chuyến đang hoạt động')
</script>
